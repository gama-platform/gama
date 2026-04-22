/*******************************************************************************************************
 *
 * GamaOsmFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.v0_6.impl.OsmHandler;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.topology.ICoordinateReferenceSystem;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.IEnvelope;
import gama.core.topology.gis.GamaCRS;
import gama.gaml.operators.spatial.SpatialOperators;
import gama.gaml.operators.spatial.SpatialTransformations;

/**
 * The Class GamaOsmFile.
 */
@file (
		name = "osm",
		extensions = { "osm", "pbf", "bz2", "gz" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		buffer_index = IType.INT,
		concept = { IConcept.OSM, IConcept.FILE },
		doc = @doc ("Represents files that contain OSM GIS information. The internal representation is a list of geometries. See https://en.wikipedia.org/wiki/OpenStreetMap for more information"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaOsmFile extends GamaGisFile {

	/**
	 * A PBF reader for OSM data that uses protobuf-java 3.x, replacing the older
	 * {@code crosby.binary.osmosis.OsmosisReader} which requires protobuf 2.x.
	 *
	 * <p>
	 * This reader directly parses the OSM PBF binary format (fileformat.proto + osmformat.proto) using
	 * {@link CodedInputStream} from protobuf-java 3.x, bypassing the class initialization conflicts that arise when the
	 * old {@code osmpbf-1.6.0.jar} is used together with {@code protobuf-java-3.x}.
	 * </p>
	 *
	 * <p>
	 * Supports raw, zlib-compressed, and lzma-compressed blobs. The reader processes OSMHeader and OSMData block types
	 * and sends Nodes, Ways, Relations, and Bounds to the configured {@link Sink}.
	 * </p>
	 *
	 * @author GAMA Team (based on original OsmosisReader by Scott A. Crosby)
	 */
	public class GamaPbfReader implements RunnableSource {

		// =========================================================================
		// Fileformat proto field numbers
		// =========================================================================

		/** BlobHeader field: type (string, required). */
		private static final int BLOB_HEADER_TYPE = 1;
		/** BlobHeader field: datasize (int32, required). */
		private static final int BLOB_HEADER_DATASIZE = 3;

		/** Blob field: raw (bytes). */
		private static final int BLOB_RAW = 1;
		/** Blob field: raw_size (int32). */
		private static final int BLOB_RAW_SIZE = 2;
		/** Blob field: zlib_data (bytes). */
		private static final int BLOB_ZLIB_DATA = 3;

		// =========================================================================
		// Osmformat proto field numbers (HeaderBlock)
		// =========================================================================

		/** HeaderBlock field: bbox (HeaderBBox). */
		private static final int HEADER_BBOX = 1;
		/** HeaderBlock field: writingprogram (string). */
		// private static final int HEADER_WRITINGPROGRAM = 16;
		/** HeaderBlock field: source (string). */
		private static final int HEADER_SOURCE = 17;

		/** HeaderBBox field: left. */
		private static final int HEADERBBOX_LEFT = 1;
		/** HeaderBBox field: right. */
		private static final int HEADERBBOX_RIGHT = 2;
		/** HeaderBBox field: top. */
		private static final int HEADERBBOX_TOP = 3;
		/** HeaderBBox field: bottom. */
		private static final int HEADERBBOX_BOTTOM = 4;

		// =========================================================================
		// Osmformat proto field numbers (PrimitiveBlock)
		// =========================================================================

		/** PrimitiveBlock field: stringtable. */
		private static final int PRIMITIVE_BLOCK_STRINGTABLE = 1;
		/** PrimitiveBlock field: primitivegroup (repeated). */
		private static final int PRIMITIVE_BLOCK_PRIMITIVEGROUP = 2;
		/** PrimitiveBlock field: granularity (default 100). */
		private static final int PRIMITIVE_BLOCK_GRANULARITY = 17;
		/** PrimitiveBlock field: lat_offset (default 0). */
		private static final int PRIMITIVE_BLOCK_LAT_OFFSET = 19;
		/** PrimitiveBlock field: lon_offset (default 0). */
		private static final int PRIMITIVE_BLOCK_LON_OFFSET = 20;
		/** PrimitiveBlock field: date_granularity (default 1000). */
		private static final int PRIMITIVE_BLOCK_DATE_GRANULARITY = 18;

		/** StringTable field: s (repeated bytes). */
		private static final int STRINGTABLE_S = 1;

		/** PrimitiveGroup field: nodes (repeated Node). */
		private static final int PRIMITIVEGROUP_NODES = 1;
		/** PrimitiveGroup field: dense (DenseNodes). */
		private static final int PRIMITIVEGROUP_DENSE = 2;
		/** PrimitiveGroup field: ways (repeated Way). */
		private static final int PRIMITIVEGROUP_WAYS = 3;
		/** PrimitiveGroup field: relations (repeated Relation). */
		private static final int PRIMITIVEGROUP_RELATIONS = 4;

		/** Node field: id. */
		private static final int NODE_ID = 1;
		/** Node field: keys (repeated). */
		private static final int NODE_KEYS = 2;
		/** Node field: vals (repeated). */
		private static final int NODE_VALS = 3;
		/** Node field: lat. */
		private static final int NODE_LAT = 8;
		/** Node field: lon. */
		private static final int NODE_LON = 9;

		/** DenseNodes field: id (packed sint64). */
		private static final int DENSENODES_ID = 1;
		/** DenseNodes field: denseinfo. */
		// private static final int DENSENODES_DENSEINFO = 5;
		/** DenseNodes field: lat (packed sint64). */
		private static final int DENSENODES_LAT = 8;
		/** DenseNodes field: lon (packed sint64). */
		private static final int DENSENODES_LON = 9;
		/** DenseNodes field: keys_vals (packed int32). */
		private static final int DENSENODES_KEYS_VALS = 10;

		/** Way field: id. */
		private static final int WAY_ID = 1;
		/** Way field: keys (repeated). */
		private static final int WAY_KEYS = 2;
		/** Way field: vals (repeated). */
		private static final int WAY_VALS = 3;
		/** Way field: refs (packed sint64). */
		private static final int WAY_REFS = 8;

		/** Relation field: id. */
		private static final int RELATION_ID = 1;
		/** Relation field: keys (repeated). */
		private static final int RELATION_KEYS = 2;
		/** Relation field: vals (repeated). */
		private static final int RELATION_VALS = 3;
		/** Relation field: roles_sid (packed int32). */
		private static final int RELATION_ROLES_SID = 8;
		/** Relation field: memids (packed sint64). */
		private static final int RELATION_MEMIDS = 9;
		/** Relation field: types (packed enum). */
		private static final int RELATION_TYPES = 10;

		/** Relation member type: NODE. */
		private static final int RELATION_MEMBER_NODE = 0;
		/** Relation member type: WAY. */
		private static final int RELATION_MEMBER_WAY = 1;
		/** Relation member type: RELATION. */
		private static final int RELATION_MEMBER_RELATION = 2;

		/** Coordinate conversion multiplier (nanodegrees to degrees). */
		private static final double NANO_DEGREES = 1e-9;

		/** Maximum protobuf blob size (32 MB). */
		private static final int MAX_BLOB_SIZE = 32 * 1024 * 1024;

		/** The input stream to read from. */
		private final InputStream input;

		/** The sink receiving all parsed OSM entities. */
		private Sink sink;

		/**
		 * Creates a new GamaPbfReader for the given input stream.
		 *
		 * @param input
		 *            the PBF input stream; must not be null
		 * @throws IllegalArgumentException
		 *             if input is null
		 */
		public GamaPbfReader(final InputStream input) {
			if (input == null) throw new IllegalArgumentException("Input stream must not be null");
			this.input = input;
		}

		@Override
		public void setSink(final Sink sink) { this.sink = sink; }

		@Override
		public void run() {
			try {
				sink.initialize(Collections.emptyMap());
				final DataInputStream dis = new DataInputStream(input);
				while (true) {
					// Read header length (4 bytes, big-endian)
					final int headerLen;
					try {
						headerLen = dis.readInt();
					} catch (final java.io.EOFException eof) {
						break; // normal end of file
					}
					if (headerLen < 0 || headerLen > MAX_BLOB_SIZE)
						throw new IOException("Invalid header length: " + headerLen);

					// Read BlobHeader
					final byte[] headerBytes = new byte[headerLen];
					dis.readFully(headerBytes);
					final BlobHeader blobHeader = parseBlobHeader(headerBytes);

					// Read Blob
					final int dataSize = blobHeader.datasize;
					if (dataSize < 0 || dataSize > MAX_BLOB_SIZE)
						throw new IOException("Invalid blob data size: " + dataSize);
					final byte[] blobBytes = new byte[dataSize];
					dis.readFully(blobBytes);
					final byte[] blobData = decompressBlob(blobBytes);

					// Process block based on type
					if ("OSMHeader".equals(blobHeader.type)) {
						processHeader(blobData);
					} else if ("OSMData".equals(blobHeader.type)) { processData(blobData); }
					// else: skip unknown block types
				}
				sink.complete();
			} catch (final IOException e) {
				throw new RuntimeException("Unable to process PBF stream", e);
			}
		}

		// =========================================================================
		// BlobHeader parsing
		// =========================================================================

		/**
		 * A simple structure holding the parsed fields of an OSM PBF BlobHeader.
		 *
		 * @see <a href="https://wiki.openstreetmap.org/wiki/PBF_Format">PBF Format</a>
		 */
		private static class BlobHeader {

			/** The block type string (e.g. {@code "OSMHeader"} or {@code "OSMData"}). */
			String type = "";

			/** The size in bytes of the following Blob. */
			int datasize = 0;
		}

		/**
		 * Parses a {@link BlobHeader} from raw bytes using the protobuf wire format.
		 *
		 * @param bytes
		 *            the raw bytes of the BlobHeader protobuf message
		 * @return the parsed {@link BlobHeader}
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private BlobHeader parseBlobHeader(final byte[] bytes) throws IOException {
			final BlobHeader header = new BlobHeader();
			final CodedInputStream cis = CodedInputStream.newInstance(bytes);
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0:
						done = true;
						break;
					case BLOB_HEADER_TYPE << 3 | 2: // LEN type
						header.type = cis.readString();
						break;
					case BLOB_HEADER_DATASIZE << 3 | 0: // VARINT type
						header.datasize = cis.readInt32();
						break;
					default:
						cis.skipField(tag);
						break;
				}
			}
			return header;
		}

		// =========================================================================
		// Blob decompression
		// =========================================================================

		/**
		 * Decompresses (if necessary) a Blob message and returns the raw data bytes.
		 *
		 * <p>
		 * Supports raw (uncompressed) and zlib-deflate-compressed blobs.
		 * </p>
		 *
		 * @param blobBytes
		 *            the raw bytes of the Blob protobuf message
		 * @return the decompressed data bytes
		 * @throws IOException
		 *             if an I/O error or decompression error occurs
		 */
		private byte[] decompressBlob(final byte[] blobBytes) throws IOException {
			final CodedInputStream cis = CodedInputStream.newInstance(blobBytes);
			byte[] raw = null;
			int rawSize = -1;
			byte[] zlibData = null;
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0 -> done = true;
					case BLOB_RAW << 3 | 2 -> raw = cis.readByteArray();
					case BLOB_RAW_SIZE << 3 | 0 -> rawSize = cis.readInt32();
					case BLOB_ZLIB_DATA << 3 | 2 -> zlibData = cis.readByteArray();
					default -> cis.skipField(tag);
				}
			}
			if (raw != null) return raw;
			if (zlibData != null) {
				if (rawSize <= 0) throw new IOException("zlib_data present but raw_size missing or invalid");
				return zlibDecompress(zlibData, rawSize);
			}
			throw new IOException("Blob contains neither raw nor zlib_data");
		}

		/**
		 * Decompresses zlib (DEFLATE) compressed data.
		 *
		 * @param compressed
		 *            the compressed data bytes
		 * @param expectedSize
		 *            the expected uncompressed size
		 * @return the decompressed bytes
		 * @throws IOException
		 *             if decompression fails
		 */
		private byte[] zlibDecompress(final byte[] compressed, final int expectedSize) throws IOException {
			final Inflater inflater = new Inflater();
			inflater.setInput(compressed);
			final byte[] result = new byte[expectedSize];
			try {
				final int n = inflater.inflate(result);
				if (n != expectedSize)
					throw new IOException("Decompressed size mismatch: expected " + expectedSize + ", got " + n);
				return result;
			} catch (final DataFormatException e) {
				throw new IOException("Failed to decompress zlib data", e);
			} finally {
				inflater.end();
			}
		}

		// =========================================================================
		// OSMHeader block processing
		// =========================================================================

		/**
		 * Processes an OSMHeader block, extracting the bounding box if present and sending a {@link BoundContainer} to
		 * the sink.
		 *
		 * @param data
		 *            the raw bytes of the HeaderBlock protobuf message
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private void processHeader(final byte[] data) throws IOException {
			final CodedInputStream cis = CodedInputStream.newInstance(data);
			double left = 0, right = 0, top = 0, bottom = 0;
			boolean hasBbox = false;
			String source = "";
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0:
						done = true;
						break;
					case HEADER_BBOX << 3 | 2: {
						final byte[] bboxBytes = cis.readByteArray();
						final long[] bbox = parseBbox(bboxBytes);
						left = bbox[0] * NANO_DEGREES;
						right = bbox[1] * NANO_DEGREES;
						top = bbox[2] * NANO_DEGREES;
						bottom = bbox[3] * NANO_DEGREES;
						hasBbox = true;
						break;
					}
					case HEADER_SOURCE << 3 | 2:
						source = cis.readString();
						break;
					default:
						cis.skipField(tag);
						break;
				}
			}
			if (hasBbox) {
				final Bound bound = new Bound(right, left, top, bottom, source);
				sink.process(new BoundContainer(bound));
			}
		}

		/**
		 * Parses the HeaderBBox sub-message and returns [left, right, top, bottom] in nano-degrees.
		 *
		 * @param bboxBytes
		 *            the raw bytes of the HeaderBBox protobuf message
		 * @return an array [left, right, top, bottom]
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private long[] parseBbox(final byte[] bboxBytes) throws IOException {
			final CodedInputStream cis = CodedInputStream.newInstance(bboxBytes);
			long left = 0, right = 0, top = 0, bottom = 0;
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0 -> done = true;
					case HEADERBBOX_LEFT << 3 | 0 -> left = cis.readSInt64();
					case HEADERBBOX_RIGHT << 3 | 0 -> right = cis.readSInt64();
					case HEADERBBOX_TOP << 3 | 0 -> top = cis.readSInt64();
					case HEADERBBOX_BOTTOM << 3 | 0 -> bottom = cis.readSInt64();
					default -> cis.skipField(tag);
				}
			}
			return new long[] { left, right, top, bottom };
		}

		// =========================================================================
		// OSMData block processing
		// =========================================================================

		/**
		 * A container for the state of a PrimitiveBlock being parsed.
		 *
		 * <p>
		 * Holds the string table, coordinate offsets, granularity, and date granularity used when decoding dense nodes,
		 * ways, and relations from an OSMData blob.
		 * </p>
		 */
		private static class BlockContext {

			/** The decoded string table from the PrimitiveBlock. */
			String[] strings;

			/** The coordinate granularity in nanodegrees (default 100). */
			int granularity = 100;

			/** The latitude offset in nanodegrees (default 0). */
			long latOffset = 0;

			/** The longitude offset in nanodegrees (default 0). */
			long lonOffset = 0;

			/** The date granularity in milliseconds (default 1000). */
			int dateGranularity = 1000;
		}

		/**
		 * Processes an OSMData block: parses the PrimitiveBlock and sends all contained entities to the sink.
		 *
		 * @param data
		 *            the raw bytes of the PrimitiveBlock protobuf message
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private void processData(final byte[] data) throws IOException {
			final BlockContext ctx = new BlockContext();
			// Two-pass: first extract stringtable and block metadata, then parse groups
			final List<byte[]> groups = new ArrayList<>();
			final CodedInputStream cis = CodedInputStream.newInstance(data);
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0 -> done = true;
					case PRIMITIVE_BLOCK_STRINGTABLE << 3 | 2 -> ctx.strings = parseStringTable(cis.readByteArray());
					case PRIMITIVE_BLOCK_PRIMITIVEGROUP << 3 | 2 -> groups.add(cis.readByteArray());
					case PRIMITIVE_BLOCK_GRANULARITY << 3 | 0 -> ctx.granularity = cis.readInt32();
					case PRIMITIVE_BLOCK_DATE_GRANULARITY << 3 | 0 -> ctx.dateGranularity = cis.readInt32();
					case PRIMITIVE_BLOCK_LAT_OFFSET << 3 | 0 -> ctx.latOffset = cis.readInt64();
					case PRIMITIVE_BLOCK_LON_OFFSET << 3 | 0 -> ctx.lonOffset = cis.readInt64();
					default -> cis.skipField(tag);
				}
			}
			if (ctx.strings == null) { ctx.strings = new String[0]; }
			for (final byte[] groupData : groups) { parsePrimitiveGroup(groupData, ctx); }
		}

		/**
		 * Parses the StringTable from the raw bytes of a PrimitiveBlock StringTable sub-message.
		 *
		 * @param stBytes
		 *            the raw bytes of the StringTable protobuf message
		 * @return the array of decoded strings (index 0 is always empty per OSM PBF spec)
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private String[] parseStringTable(final byte[] stBytes) throws IOException {
			final List<String> strings = new ArrayList<>();
			final CodedInputStream cis = CodedInputStream.newInstance(stBytes);
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0 -> done = true;
					case STRINGTABLE_S << 3 | 2 -> strings.add(cis.readString());
					default -> cis.skipField(tag);
				}
			}
			return strings.toArray(new String[0]);
		}

		/**
		 * Parses a PrimitiveGroup from the given bytes and dispatches entities to the sink.
		 *
		 * @param groupData
		 *            the raw bytes of the PrimitiveGroup protobuf message
		 * @param ctx
		 *            the block context (string table, granularity, offsets)
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private void parsePrimitiveGroup(final byte[] groupData, final BlockContext ctx) throws IOException {
			final CodedInputStream cis = CodedInputStream.newInstance(groupData);
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0 -> done = true;
					case PRIMITIVEGROUP_NODES << 3 | 2 -> parseNode(cis.readByteArray(), ctx);
					case PRIMITIVEGROUP_DENSE << 3 | 2 -> parseDenseNodes(cis.readByteArray(), ctx);
					case PRIMITIVEGROUP_WAYS << 3 | 2 -> parseWay(cis.readByteArray(), ctx);
					case PRIMITIVEGROUP_RELATIONS << 3 | 2 -> parseRelation(cis.readByteArray(), ctx);
					default -> cis.skipField(tag);
				}
			}
		}

		// =========================================================================
		// Helper: convert raw coord to degrees
		// =========================================================================

		/**
		 * Converts a raw (sint64) coordinate value from the PBF format to degrees.
		 *
		 * @param raw
		 *            the raw coordinate value
		 * @param offset
		 *            the offset in nanodegrees
		 * @param granularity
		 *            the granularity
		 * @return the coordinate in degrees
		 */
		private double toCoord(final long raw, final long offset, final int granularity) {
			return (granularity * raw + offset) * NANO_DEGREES;
		}

		// =========================================================================
		// Individual node parsing
		// =========================================================================

		/**
		 * Parses a single Node message and sends it to the sink.
		 *
		 * @param nodeData
		 *            the raw bytes of the Node protobuf message
		 * @param ctx
		 *            the block context
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private void parseNode(final byte[] nodeData, final BlockContext ctx) throws IOException {
			final CodedInputStream cis = CodedInputStream.newInstance(nodeData);
			long id = 0;
			long lat = 0, lon = 0;
			final List<Integer> keys = new ArrayList<>(), vals = new ArrayList<>();
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0:
						done = true;
						break;
					case NODE_ID << 3 | 0:
						id = cis.readSInt64();
						break;
					case NODE_LAT << 3 | 0:
						lat = cis.readSInt64();
						break;
					case NODE_LON << 3 | 0:
						lon = cis.readSInt64();
						break;
					case NODE_KEYS << 3 | 0:
						keys.add(cis.readInt32());
						break;
					case NODE_VALS << 3 | 0:
						vals.add(cis.readInt32());
						break;
					case NODE_KEYS << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { keys.add(ci2.readInt32()); }
						break;
					}
					case NODE_VALS << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { vals.add(ci2.readInt32()); }
						break;
					}
					default:
						cis.skipField(tag);
						break;
				}
			}
			final Collection<Tag> tags = buildTags(keys, vals, ctx.strings);
			final double latitude = toCoord(lat, ctx.latOffset, ctx.granularity);
			final double longitude = toCoord(lon, ctx.lonOffset, ctx.granularity);
			final CommonEntityData ced = new CommonEntityData(id, 0, new Date(0), OsmUser.NONE, 0, tags);
			sink.process(new NodeContainer(new Node(ced, latitude, longitude)));
		}

		// =========================================================================
		// DenseNodes parsing
		// =========================================================================

		/**
		 * Parses a DenseNodes message (the most common form of nodes in PBF files) and sends all nodes to the sink.
		 *
		 * @param denseData
		 *            the raw bytes of the DenseNodes protobuf message
		 * @param ctx
		 *            the block context
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private void parseDenseNodes(final byte[] denseData, final BlockContext ctx) throws IOException {
			final CodedInputStream cis = CodedInputStream.newInstance(denseData);
			final List<Long> ids = new ArrayList<>(), lats = new ArrayList<>(), lons = new ArrayList<>();
			final List<Integer> keysVals = new ArrayList<>();
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0:
						done = true;
						break;
					case DENSENODES_ID << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { ids.add(ci2.readSInt64()); }
						break;
					}
					case DENSENODES_LAT << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { lats.add(ci2.readSInt64()); }
						break;
					}
					case DENSENODES_LON << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { lons.add(ci2.readSInt64()); }
						break;
					}
					case DENSENODES_KEYS_VALS << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { keysVals.add(ci2.readInt32()); }
						break;
					}
					default:
						cis.skipField(tag);
						break;
				}
			}
			// Decode delta-encoded ids, lats, lons
			long idAccum = 0, latAccum = 0, lonAccum = 0;
			int kvIdx = 0;
			for (int i = 0; i < ids.size(); i++) {
				idAccum += ids.get(i);
				latAccum += lats.get(i);
				lonAccum += lons.get(i);
				final double latitude = toCoord(latAccum, ctx.latOffset, ctx.granularity);
				final double longitude = toCoord(lonAccum, ctx.lonOffset, ctx.granularity);
				// Extract tags for this node from the flat keys_vals list (terminated by 0)
				final Collection<Tag> tags = new ArrayList<>();
				while (kvIdx < keysVals.size()) {
					final int k = keysVals.get(kvIdx++);
					if (k == 0) { break; }
					final int v = kvIdx < keysVals.size() ? keysVals.get(kvIdx++) : 0;
					if (k < ctx.strings.length && v < ctx.strings.length) {
						tags.add(new Tag(ctx.strings[k], ctx.strings[v]));
					}
				}
				final CommonEntityData ced = new CommonEntityData(idAccum, 0, new Date(0), OsmUser.NONE, 0, tags);
				sink.process(new NodeContainer(new Node(ced, latitude, longitude)));
			}
		}

		// =========================================================================
		// Way parsing
		// =========================================================================

		/**
		 * Parses a single Way message and sends it to the sink.
		 *
		 * @param wayData
		 *            the raw bytes of the Way protobuf message
		 * @param ctx
		 *            the block context
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private void parseWay(final byte[] wayData, final BlockContext ctx) throws IOException {
			final CodedInputStream cis = CodedInputStream.newInstance(wayData);
			long id = 0;
			final List<Integer> keys = new ArrayList<>(), vals = new ArrayList<>();
			final List<Long> refs = new ArrayList<>();
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0:
						done = true;
						break;
					case WAY_ID << 3 | 0:
						id = cis.readInt64();
						break;
					case WAY_KEYS << 3 | 0:
						keys.add(cis.readInt32());
						break;
					case WAY_VALS << 3 | 0:
						vals.add(cis.readInt32());
						break;
					case WAY_KEYS << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { keys.add(ci2.readInt32()); }
						break;
					}
					case WAY_VALS << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { vals.add(ci2.readInt32()); }
						break;
					}
					case WAY_REFS << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { refs.add(ci2.readSInt64()); }
						break;
					}
					default:
						cis.skipField(tag);
						break;
				}
			}
			final Collection<Tag> tags = buildTags(keys, vals, ctx.strings);
			// Delta-decode node refs
			final List<WayNode> wayNodes = new ArrayList<>(refs.size());
			long refAccum = 0;
			for (final long delta : refs) {
				refAccum += delta;
				wayNodes.add(new WayNode(refAccum));
			}
			final CommonEntityData ced = new CommonEntityData(id, 0, new Date(0), OsmUser.NONE, 0, tags);
			sink.process(new WayContainer(new Way(ced, wayNodes)));
		}

		// =========================================================================
		// Relation parsing
		// =========================================================================

		/**
		 * Parses a single Relation message and sends it to the sink.
		 *
		 * @param relData
		 *            the raw bytes of the Relation protobuf message
		 * @param ctx
		 *            the block context
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private void parseRelation(final byte[] relData, final BlockContext ctx) throws IOException {
			final CodedInputStream cis = CodedInputStream.newInstance(relData);
			long id = 0;
			final List<Integer> keys = new ArrayList<>(), vals = new ArrayList<>();
			final List<Integer> rolesSid = new ArrayList<>(), types = new ArrayList<>();
			final List<Long> memids = new ArrayList<>();
			boolean done = false;
			while (!done) {
				final int tag = cis.readTag();
				switch (tag) {
					case 0:
						done = true;
						break;
					case RELATION_ID << 3 | 0:
						id = cis.readInt64();
						break;
					case RELATION_KEYS << 3 | 0:
						keys.add(cis.readInt32());
						break;
					case RELATION_VALS << 3 | 0:
						vals.add(cis.readInt32());
						break;
					case RELATION_KEYS << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { keys.add(ci2.readInt32()); }
						break;
					}
					case RELATION_VALS << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { vals.add(ci2.readInt32()); }
						break;
					}
					case RELATION_ROLES_SID << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { rolesSid.add(ci2.readInt32()); }
						break;
					}
					case RELATION_MEMIDS << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { memids.add(ci2.readSInt64()); }
						break;
					}
					case RELATION_TYPES << 3 | 2: {
						final ByteString packed = cis.readBytes();
						final CodedInputStream ci2 = packed.newCodedInput();
						while (!ci2.isAtEnd()) { types.add(ci2.readEnum()); }
						break;
					}
					default:
						cis.skipField(tag);
						break;
				}
			}
			final Collection<Tag> tags = buildTags(keys, vals, ctx.strings);
			// Delta-decode member ids
			final List<RelationMember> members = new ArrayList<>();
			long memidAccum = 0;
			for (int i = 0; i < memids.size(); i++) {
				memidAccum += memids.get(i);
				final String role =
						i < rolesSid.size() && rolesSid.get(i) < ctx.strings.length ? ctx.strings[rolesSid.get(i)] : "";
				final int typeVal = i < types.size() ? types.get(i) : 0;
				final EntityType memberType = switch (typeVal) {
					case RELATION_MEMBER_NODE -> EntityType.Node;
					case RELATION_MEMBER_WAY -> EntityType.Way;
					case RELATION_MEMBER_RELATION -> EntityType.Relation;
					default -> EntityType.Node;
				};
				members.add(new RelationMember(memidAccum, memberType, role));
			}
			final CommonEntityData ced = new CommonEntityData(id, 0, new Date(0), OsmUser.NONE, 0, tags);
			sink.process(new RelationContainer(new Relation(ced, members)));
		}

		// =========================================================================
		// Tag building helper
		// =========================================================================

		/**
		 * Builds a collection of {@link Tag} objects from parallel key and value index lists and the string table.
		 *
		 * @param keys
		 *            the list of key indices into the string table
		 * @param vals
		 *            the list of value indices into the string table
		 * @param strings
		 *            the string table
		 * @return the collection of {@link Tag} objects
		 */
		private Collection<Tag> buildTags(final List<Integer> keys, final List<Integer> vals, final String[] strings) {
			final List<Tag> tags = new ArrayList<>(keys.size());
			for (int i = 0; i < keys.size() && i < vals.size(); i++) {
				final int ki = keys.get(i), vi = vals.get(i);
				if (ki < strings.length && vi < strings.length) { tags.add(new Tag(strings[ki], strings[vi])); }
			}
			return tags;
		}
	}

	/** The env. */
	final ReferencedEnvelope env = new ReferencedEnvelope();

	/** The Constant RESERVED_KEYS. */
	static final List<String> RESERVED_KEYS = List.of("location", "shape");

	/** The filtering options. */
	IMap<String, IList> filteringOptions;

	/** The attributes. */
	Map<String, String> attributes = new HashMap<>();

	/** The layers. */
	final IMap<String, List<IShape>> layers = GamaMapFactory.create(Types.STRING, Types.LIST);

	/** The Constant featureTypes. */
	final static List<String> featureTypes = Arrays.asList("aerialway", "aeroway", "amenity", "barrier", "boundary",
			"building", "craft", "emergency", "geological", "highway", "historic", "landuse", "leisure", "man_made",
			"military", "natural", "office", "place", "power", "public_transport", "railway", "route", "shop", "sport",
			"tourism", "waterway", "water");

	/** The nb objects. */
	int nbObjects;

	/**
	 * Gets the nb objects.
	 *
	 * @return the nb objects
	 */
	public int getNbObjects() { return nbObjects; }

	/**
	 * @throws GamaRuntimeException
	 * @param scope
	 * @param pathName
	 */
	@doc (
			value = "This file constructor allows to read a osm (.osm, .pbf, .bz2, .gz) file (using WGS84 coordinate system for the data)",
			examples = { @example (
					value = "file f <- osm_file(\"file\");",
					isExecutable = false) })
	public GamaOsmFile(final IScope scope, final String pathName) {
		super(scope, pathName, (Integer) null);
	}

	/**
	 * Instantiates a new gama osm file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param filteringOptions
	 *            the filtering options
	 */
	@doc (
			value = """
					This file constructor allows to read an osm (.osm, .pbf, .bz2, .gz) file (using WGS84 coordinate system for the data)\
					The map is used to filter the objects in the file according their attributes: for each key (string) of the map, only the objects that have a value for the  attribute \
					contained in the value set are kept.\
					 For an exhaustive list of the attibute of OSM data, see: http://wiki.openstreetmap.org/wiki/Map_Features""",

			examples = { @example (
					value = "file f <- osm_file(\"file\", map([\"highway\"::[\"primary\", \"secondary\"], \"building\"::[\"yes\"], \"amenity\"::[]]));",
					equals = "f will contain all the objects of file that have the attibute 'highway' with the value 'primary' or 'secondary', and the objects that have the attribute 'building' with the value 'yes', "
							+ "and all the objects that have the attribute 'aminity' (whatever the value).",
					isExecutable = false) })

	public GamaOsmFile(final IScope scope, final String pathName, final IMap<String, IList> filteringOptions) {
		super(scope, pathName, (Integer) null);
		this.filteringOptions = filteringOptions;
	}

	@Override
	protected String fetchFromURL(final IScope scope) {
		String pathName = super.fetchFromURL(scope);
		if (pathName.endsWith(".osm.xml")) { pathName = pathName.replace(".xml", ""); }
		return pathName;
	}

	/**
	 * Gets the feature iterator.
	 *
	 * @param scope
	 *            the scope
	 * @param returnIt
	 *            the return it
	 * @return the feature iterator
	 */
	public void getFeatureIterator(final IScope scope, final boolean returnIt) {
		final Map<Long, IShape> nodesPt = new HashMap<>();
		final Map<Long, Node> nodesFromId = new HashMap<>();
		final Set<Node> nodes = new LinkedHashSet<>();
		final List<Way> ways = new ArrayList<>();
		final List<Relation> relations = new ArrayList<>();
		final Set<Long> intersectionNodes = new LinkedHashSet<>();
		final Set<Long> usedNodes = new LinkedHashSet<>();

		final Sink sinkImplementation = new Sink() {

			@Override
			public void process(final EntityContainer entityContainer) {
				final Entity entity = entityContainer.getEntity();
				final boolean toFilter = filteringOptions != null && !filteringOptions.isEmpty();
				if (entity instanceof Bound bound) {
					final IEnvelope e = GamaEnvelopeFactory.of(bound.getLeft(), bound.getRight(), bound.getBottom(),
							bound.getTop(), 0, 0);
					computeProjection(scope, e);
				} else if (returnIt) {
					if (entity instanceof Node node) {
						final Geometry g = gis == null
								? GamaPointFactory.create(node.getLongitude(), node.getLatitude()).getInnerGeometry()
								: gis.transform(GamaPointFactory.create(node.getLongitude(), node.getLatitude())
										.getInnerGeometry());

						// final Geometry g = GamaPointFactory.create(node.getLongitude(),
						// node.getLatitude()).getInnerGeometry();
						// env.expandToInclude(g.getCoordinate());
						nodesPt.put(node.getId(), GamaShapeFactory.createFrom(g));
						nodesFromId.put(node.getId(), node);
						boolean keepObject = keepEntity(toFilter, entity);
						if (!keepObject) return;
						nodes.add(node);

					} else if (entity instanceof Way) {
						/*
						 * boolean keepObject = keepEntity(toFilter, entity); if (!keepObject) return;
						 */
						registerHighway((Way) entity, usedNodes, intersectionNodes);
						ways.add((Way) entity);

					} else if (entity instanceof Relation) {
						boolean keepObject = keepEntity(toFilter, entity);
						if (!keepObject) return;
						relations.add((Relation) entity);
					}
				}

			}

			@Override
			public void complete() {}

			@Override
			public void initialize(final Map<String, Object> arg0) {}

			@Override
			public void close() {}
		};
		readFile(scope, sinkImplementation, getFile(scope));

		if (returnIt) {
			setBuffer(buildGeometries(scope, nodes, ways, relations, intersectionNodes, nodesPt, nodesFromId));
		}

	}

	/**
	 * Keep entity.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param toFilter
	 *            the to filter
	 * @param entity
	 *            the entity
	 * @return true, if successful
	 * @date 17 août 2023
	 */
	boolean keepEntity(final boolean toFilter, final Entity entity) {
		if (toFilter) {
			boolean keepObject = false;
			for (final String keyN : filteringOptions.getKeys()) {
				final IList valsPoss = filteringOptions.get(keyN);
				for (final Tag tagN : entity.getTags()) {
					if (keyN.equals(tagN.getKey())
							&& (valsPoss == null || valsPoss.isEmpty() || valsPoss.contains(tagN.getValue()))) {
						keepObject = true;
						break;
					}

				}
			}
			if (!keepObject) return false;
		}
		return true;
	}

	/**
	 * Adds the attribute.
	 *
	 * @param atts
	 *            the atts
	 * @param nameAt
	 *            the name at
	 * @param val
	 *            the val
	 */
	private void addAttribute(final Map<String, String> atts, final String nameAt, final Object val) {
		if (RESERVED_KEYS.contains(nameAt)) return;

		final String type = atts.get(nameAt);
		if ("string".equals(type)) return;
		String newType = "int";
		try {
			Integer.parseInt(val.toString());
		} catch (final Exception e) {
			try {
				Double.parseDouble(val.toString());
			} catch (final Exception e2) {
				newType = "string";
			}
		}

		if (type == null || "string".equals(newType)) { atts.put(nameAt, newType); }
	}

	/**
	 * @see gama.core.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
		getFeatureIterator(scope, true);
	}

	/**
	 * Builds the geometries.
	 *
	 * @param scope
	 *            the scope
	 * @param nodes
	 *            the nodes
	 * @param ways
	 *            the ways
	 * @param relations
	 *            the relations
	 * @param intersectionNodes
	 *            the intersection nodes
	 * @param nodesPt
	 *            the nodes pt
	 * @return the i list
	 */
	public IList<IShape> buildGeometries(final IScope scope, final Set<Node> nodes, final List<Way> ways,
			final List<Relation> relations, final Set<Long> intersectionNodes, final Map<Long, IShape> nodesPt,
			final Map<Long, Node> nodesFromId) {

		boolean toFilter = filteringOptions != null && !filteringOptions.isEmpty();
		for (final Way way : ways) {
			for (WayNode wn : way.getWayNodes()) { nodes.add(nodesFromId.get(wn.getNodeId())); }
		}

		final IList<IShape> geometries = GamaListFactory.create(Types.GEOMETRY);
		if (gis == null) {
			computeProjection(scope, GamaEnvelopeFactory.of(env));
			if (gis != null) {
				for (Long id : nodesPt.keySet()) {
					IShape sp = GamaShapeFactory.createFrom(gis.transform(nodesPt.get(id).getInnerGeometry()));
					nodesPt.put(id, sp);
				}
			}
		}
		final Map<Long, Entity> geomMap = new HashMap<>();

		for (final Node node : nodes) {
			// geomMap.put(node.getId(), node);
			final IShape pt = nodesPt.get(node.getId());
			final boolean hasAttributes = !node.getTags().isEmpty();
			final Map<String, String> atts = new HashMap<>();
			if (pt != null) {
				env.expandToInclude(pt.getLocation().toCoordinate());

				for (final Tag tg : node.getTags()) {
					final String key = tg.getKey();
					if (RESERVED_KEYS.contains(key)) { continue; }

					final Object val = tg.getValue();
					if (val != null) { addAttribute(atts, key, val); }
					pt.setAttribute(key, val);
					if ("highway".equals(key)) { intersectionNodes.add(node.getId()); }
				}
				if (hasAttributes) {
					geometries.add(pt);

					pt.forEachAttribute((att, val) -> {

						if (featureTypes.contains(att)) {
							final String idType = att + " (point)";
							List objs = layers.get(idType);
							if (objs == null) {
								objs = GamaListFactory.create(Types.GEOMETRY);
								layers.put(idType, objs);
							}
							objs.add(pt);
							for (final String v : atts.keySet()) {
								final String id = idType + ";" + v;
								attributes.put(id, atts.get(v));
							}
							return false;
						}

						return true;
					});
				}
			}
		}
		for (final Way way : ways) {
			geomMap.put(way.getId(), way);
			boolean keepObject = keepEntity(toFilter, way);
			if (!keepObject) { continue; }
			final IMap<String, Object> values = GamaMapFactory.create();
			final Map<String, String> atts = GamaMapFactory.createUnordered();

			for (final Tag tg : way.getTags()) {
				final String key = tg.getKey();
				if (RESERVED_KEYS.contains(key)) { continue; }
				final Object val = tg.getValue();
				if (val != null) { addAttribute(atts, key, val); }
				values.put(key, tg.getValue());
			}
			values.put("osm_id", way.getId());

			final boolean isPolyline = values.containsKey("highway") || way.getWayNodes().get(0).getNodeId() != way
					.getWayNodes().get(way.getWayNodes().size() - 1).getNodeId();
			if (isPolyline) {
				final List<IShape> geoms = createSplitRoad(way.getWayNodes(), values, intersectionNodes, nodesPt);
				geometries.addAll(geoms);
				if (!geoms.isEmpty()) {
					for (final Object att : values.keySet()) {
						final String idType = att + " (line)";
						if (featureTypes.contains(att)) {
							List objs = layers.get(idType);
							if (objs == null) {
								objs = GamaListFactory.create(Types.GEOMETRY);
								layers.put(idType, objs);
							}
							objs.addAll(geoms);
							for (final String v : atts.keySet()) {
								final String id = idType + ";" + v;
								attributes.put(id, atts.get(v));
							}
							break;
						}
					}
				}
			} else {
				final List<IShape> points = GamaListFactory.create(Types.GEOMETRY);
				for (final WayNode node : way.getWayNodes()) {
					final IShape pp = nodesPt.get(node.getNodeId());
					if (pp == null) { continue; }
					points.add(pp);
				}
				if (points.size() < 3) { continue; }

				final IShape geom = GamaShapeFactory.buildPolygon(points);

				if (geom != null && geom.getInnerGeometry() != null && !geom.getInnerGeometry().isEmpty()
						&& geom.getInnerGeometry().getArea() > 0) {

					values.forEach((k, v) -> geom.setAttribute(k, v));
					geometries.add(geom);

					geom.forEachAttribute((att, val) -> {
						final String idType = att + " (polygon)";
						if (featureTypes.contains(att)) {
							List objs = layers.get(idType);
							if (objs == null) {
								objs = GamaListFactory.create(Types.GEOMETRY);
								layers.put(idType, objs);
							}
							objs.add(geom);
							for (final String v : atts.keySet()) {
								final String id = idType + ";" + v;
								attributes.put(id, atts.get(v));
							}
							return false;
						}
						return true;
					});
				}
			}

		}
		for (final Relation relation : relations) {
			final Map<String, String> atts = GamaMapFactory.createUnordered();
			final Map<String, Object> values = GamaMapFactory.create();

			for (final Tag tg : relation.getTags()) {
				final String key = tg.getKey();
				if (RESERVED_KEYS.contains(key)) { continue; }
				values.put(key, tg.getValue());
			}
			String type = (String) values.get(IKeyword.TYPE);
			if ("polygon".equals(type) || "multipolygon".equals(type)) {

				managePolygonRelation(scope, relation, geometries, geomMap, values, nodesPt, intersectionNodes, atts);

			} else {
				manageNormalRelation(scope, relation, geometries, geomMap, values, nodesPt, intersectionNodes);

			}

		}
		nbObjects = geometries == null ? 0 : geometries.size();
		return geometries;
	}

	/**
	 * Manage polygon relation.
	 *
	 * @param scope
	 *            the scope
	 * @param relation
	 *            the relation
	 * @param geometries
	 *            the geometries
	 * @param geomMap
	 *            the geom map
	 * @param values
	 *            the values
	 * @param nodesPt
	 *            the nodes pt
	 * @param intersectionNodes
	 *            the intersection nodes
	 * @param atts
	 *            the atts
	 */
	private void managePolygonRelation(final IScope scope, final Relation relation, final IList<IShape> geometries,
			final Map<Long, Entity> geomMap, final Map<String, Object> values, final Map<Long, IShape> nodesPt,
			final Set<Long> intersectionNodes, final Map<String, String> atts) {
		final List<IShape> points = GamaListFactory.create(Types.GEOMETRY);

		IList<IList<IShape>> ptsList = GamaListFactory.create();
		IList<IShape> inner = GamaListFactory.create();
		for (final RelationMember member : relation.getMembers()) {
			final Entity entity = geomMap.get(member.getMemberId());
			if (entity instanceof final Way way) {
				IList<IShape> pts = GamaListFactory.create();
				for (final WayNode node : way.getWayNodes()) {
					final IShape pp = nodesPt.get(node.getNodeId());
					if (pp == null) { continue; }
					pts.add(pp);

				}
				if ("outer".equals(member.getMemberRole())) {
					ptsList.add(pts);
				} else {
					inner.add(GamaShapeFactory.buildPolygon(pts));
				}

			}
		}
		if (ptsList.size() > 1) {
			IList<IShape> ptsCurrent = ptsList.get(0);
			ptsList.remove(ptsCurrent);
			for (IList<IShape> pts : ptsList) {
				int id = ptsCurrent.indexOf(pts.get(0));
				if (id >= 0) {
					if (id == 0) {
						ptsCurrent.addAll(id, pts.reversed());
					} else {
						ptsCurrent.addAll(id, pts);
					}

				}

			}
			points.addAll(ptsCurrent);

		} else if (!ptsList.isEmpty()) { points.addAll(ptsList.get(0)); }

		if (points.size() < 3) return;

		IShape geomTmp = GamaShapeFactory.buildPolygon(points);

		if (geomTmp != null && geomTmp.getInnerGeometry() != null && !geomTmp.getInnerGeometry().isEmpty()
				&& geomTmp.getInnerGeometry().getArea() > 0) {

			if (inner != null && !inner.isEmpty()) { 
				IShape geomTmp2 = SpatialOperators.minus(scope, geomTmp, inner); 
				if (geomTmp2 != null) {
					geomTmp = geomTmp2;
				}
			}

			final IShape geom = SpatialTransformations.clean(scope, geomTmp);
			
			values.forEach((k, v) -> geom.setAttribute(k, v));
			
			geometries.add(geom);
			geom.forEachAttribute((att, val) -> {
				final String idType = att + " (polygon)";
				if (featureTypes.contains(att)) {
					List objs = layers.get(idType);
					if (objs == null) {
						objs = GamaListFactory.create(Types.GEOMETRY);
						layers.put(idType, objs);
					}
					objs.add(geom);
					for (final String v : atts.keySet()) {
						final String id = idType + ";" + v;
						attributes.put(id, atts.get(v));
					}
					return false;
				}
				return true;
			});
		}

	}

	/**
	 * Manage normal relation.
	 *
	 * @param scope
	 *            the scope
	 * @param relation
	 *            the relation
	 * @param geometries
	 *            the geometries
	 * @param geomMap
	 *            the geom map
	 * @param values
	 *            the values
	 * @param nodesPt
	 *            the nodes pt
	 * @param intersectionNodes
	 *            the intersection nodes
	 */

	private void manageNormalRelation(final IScope scope, final Relation relation, final IList<IShape> geometries,
			final Map<Long, Entity> geomMap, final Map<String, Object> values, final Map<Long, IShape> nodesPt,
			final Set<Long> intersectionNodes) {
		int order = 0;
		for (final RelationMember member : relation.getMembers()) {
			final Entity entity = geomMap.get(member.getMemberId());
			
			if (entity instanceof Way) {
				final List<WayNode> relationWays = ((Way) entity).getWayNodes();
				final Map<String, Object> wayValues = GamaMapFactory.create();
				
				// 1. Add structural metadata
				wayValues.put("entity_order", order++);
				wayValues.put("osm_way_id", entity.getId());
				wayValues.put("osm_relation_id", relation.getId());
				
				// 2. Dynamically inject all the relation's tags (attributes)
				if (values != null) {
					wayValues.putAll(values);
				}

				if (relationWays.size() > 0) {
					final List<IShape> geoms = createSplitRoad(relationWays, wayValues, intersectionNodes, nodesPt);
					geometries.addAll(geoms);
				}
				
			} else if (entity instanceof Node) {
				final IShape pt = nodesPt.get(entity.getId());
				final IShape pt2 = pt.copy(scope);

				// 1. Add structural metadata
				pt2.setAttribute("osm_relation_id", relation.getId());
				
				// 2. Dynamically inject all the relation's tags (attributes)
				if (values != null) {
					for (Map.Entry<String, Object> entry : values.entrySet()) {
						pt2.setAttribute(entry.getKey(), entry.getValue());
					}
				}

				geometries.add(pt2);
			}
		}
	}
	/**
	 * Creates the split road.
	 *
	 * @param wayNodes
	 *            the way nodes
	 * @param values
	 *            the values
	 * @param intersectionNodes
	 *            the intersection nodes
	 * @param nodesPt
	 *            the nodes pt
	 * @return the list
	 */
	public List<IShape> createSplitRoad(final List<WayNode> wayNodes, final Map<String, Object> values,
			final Set<Long> intersectionNodes, final Map<Long, IShape> nodesPt) {
		final List<List<IShape>> pointsList = GamaListFactory.create(Types.LIST.of(Types.GEOMETRY));
		List<IShape> points = GamaListFactory.create(Types.GEOMETRY);
		final IList<IShape> geometries = GamaListFactory.create(Types.GEOMETRY);
		final WayNode endNode = wayNodes.get(wayNodes.size() - 1);
		for (final WayNode node : wayNodes) {
			final Long id = node.getNodeId();
			final IShape pt = nodesPt.get(id);
			if (pt == null) { continue; }
			points.add(pt);
			if (intersectionNodes.contains(id) || node == endNode) {
				if (points.size() > 1) { pointsList.add(points); }
				points = GamaListFactory.create(Types.GEOMETRY);
				points.add(pt);

			}
		}
		int index = 0;
		for (final List<IShape> pts : pointsList) {
			final Map<String, Object> tempValues = new HashMap<>(values);
			tempValues.put("way_order", index++);
			final IShape g = createRoad(pts, tempValues);
			if (g != null) { geometries.add(g); }
		}
		return geometries;

	}

	/**
	 * Creates the road.
	 *
	 * @param points
	 *            the points
	 * @param values
	 *            the values
	 * @return the i shape
	 */
	private IShape createRoad(final List<IShape> points, final Map<String, Object> values) {
		if (points.size() < 2) return null;
		final IShape geom = GamaShapeFactory.buildPolyline(points);
		if (geom != null && geom.getInnerGeometry() != null && !geom.getInnerGeometry().isEmpty()
				&& geom.getInnerGeometry().isSimple() && geom.getPerimeter() > 0) {
			for (final String key : values.keySet()) { geom.setAttribute(key, values.get(key)); }
			return geom;
		}
		return null;
	}

	/**
	 * Register highway.
	 *
	 * @param way
	 *            the way
	 * @param usedNodes
	 *            the used nodes
	 * @param intersectionNodes
	 *            the intersection nodes
	 */
	void registerHighway(final Way way, final Set<Long> usedNodes, final Set<Long> intersectionNodes) {
		for (final Tag tg : way.getTags()) {
			final String key = tg.getKey();
			if ("highway".equals(key)) {
				final List<WayNode> nodes = way.getWayNodes();
				for (final WayNode node : nodes) {
					final long id = node.getNodeId();
					if (usedNodes.contains(id)) {
						intersectionNodes.add(id);
					} else {
						usedNodes.add(id);
					}
				}
				if (nodes.size() > 2 && nodes.get(0) == nodes.get(nodes.size() - 1)) {
					intersectionNodes.add(nodes.get(nodes.size() / 2).getNodeId());
				}
			}
		}
	}

	/**
	 * Read file.
	 *
	 * @param scope
	 *            the scope
	 * @param sink
	 *            the sink
	 * @param osmFile
	 *            the osm file
	 */
	private void readFile(final IScope scope, final Sink sink, final File osmFile) {
		final String ext = getExtension(scope);
		switch (ext) {
			case "pbf" -> {
				try (InputStream pbfStream = Files.newInputStream(osmFile.toPath())) {
					final RunnableSource reader = new GamaPbfReader(pbfStream);
					reader.setSink(sink);
					reader.run();
				} catch (final Exception e) {
					throw GamaRuntimeException
							.error("Unable to parse PBF file " + getName(scope) + ": " + e.getMessage(), scope);
				} catch (final Error e) {
					throw GamaRuntimeException.error("Unable to parse PBF file " + getName(scope)
							+ " (possibly a dependency version conflict: " + e.getMessage() + ")", scope);
				}
			}
			default -> readXML(scope, sink);
		}
	}

	/**
	 * Read XML.
	 *
	 * @param scope
	 *            the scope
	 * @param sink
	 *            the sink
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private void readXML(final IScope scope, final Sink sink) throws GamaRuntimeException {
		try {
			InputStream inputStream = Files.newInputStream(getFile(scope).toPath());
			final String ext = getExtension(scope);
			switch (ext) {
				case "gz":
					inputStream = new GZIPInputStream(inputStream);
					break;
				case "bz2":
					inputStream = new BZip2CompressorInputStream(inputStream);
					break;
			}
			try (InputStream stream = inputStream) {
				final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				parser.parse(stream, new OsmHandler(sink, false));
			}
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Unable to parse xml file " + getName(scope) + ": " + e.getMessage(),
					scope);
		}

	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		if (gis == null) { getFeatureIterator(scope, false); }
		if (gis == null) return GamaEnvelopeFactory.of(env);
		return gis.getProjectedEnvelope();

	}

	/** The Constant DefaultCRS. */
	private static final ICoordinateReferenceSystem DefaultCRS = new GamaCRS(DefaultGeographicCRS.WGS84);

	/**
	 * Method getExistingCRS()
	 *
	 * @see gama.core.util.file.GamaGisFile#getExistingCRS()
	 */
	@Override
	public ICoordinateReferenceSystem getOwnCRS(final IScope scope) {
		// Is it always true ?
		return DefaultCRS;
	}

	/**
	 * Gets the OSM attributes.
	 *
	 * @param scope
	 *            the scope
	 * @return the OSM attributes
	 */
	public Map<String, String> getOSMAttributes(final IScope scope) {
		if (attributes == null) {
			attributes = new HashMap<>();
			getFeatureIterator(scope, true);
		}
		return attributes;
	}

	/**
	 * Gets the layers.
	 *
	 * @return the layers
	 */
	public Map<String, List<IShape>> getLayers() { return layers; }

	/**
	 * Gets the feature types.
	 *
	 * @return the feature types
	 */
	public List<String> getFeatureTypes() { return featureTypes; }

	@Override
	protected SimpleFeatureCollection getFeatureCollection(final IScope scope) {
		return null;
	}

}
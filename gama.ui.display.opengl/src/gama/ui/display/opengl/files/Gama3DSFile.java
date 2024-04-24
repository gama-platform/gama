/*******************************************************************************************************
 *
 * Gama3DSFile.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.files;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.core.common.geometry.GeometryUtils;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.file.Gama3DGeometryFile;
import gama.dev.DEBUG;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 *
 * Class Gama3DSFile. A basic loader (only loads vertices and faces).
 *
 * @author drogoul
 * @since 31 déc. 2013
 *
 */

@file (
		name = "threeds",
		extensions = { "3ds", "max" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY)
@doc ("Autodesk 3DS Max file format: https://en.wikipedia.org/wiki/.3ds")
public class Gama3DSFile extends Gama3DGeometryFile {

	/**
	 * The Class Chunk.
	 */
	class Chunk {

		/** The id. */
		public int id = 0;

		/** The length. */
		public int length = 0;

		/** The bytes read. */
		public int bytesRead = 0;
	}

	/**
	 * The Class Obj.
	 */
	class Obj {

		/** The verts. */
		public GamaPoint verts[] = null;

		/** The faces. */
		public List<Geometry> faces;
	}

	/** The objects. */
	List<Obj> objects = new ArrayList<>();

	/** The Constant PRIMARY. */
	// Primary Chunk, at the beginning of each file
	private static final int PRIMARY = 0x4D4D;

	/** The Constant VERSION. */
	private static final int VERSION = 0x0002;

	/** The Constant EDITOR. */
	// Main Chunks
	private static final int EDITOR = 0x3D3D;

	/** The Constant OBJECT. */
	private static final int OBJECT = 0x4000;

	/** The Constant OBJECT_MESH. */
	private static final int OBJECT_MESH = 0x4100;

	/** The Constant OBJECT_VERTICES. */
	// Sub defines of OBJECT_MESH
	private static final int OBJECT_VERTICES = 0x4110;

	/** The Constant OBJECT_FACES. */
	private static final int OBJECT_FACES = 0x4120;

	/** The data input stream. */
	// File reader
	private DataInputStream dataInputStream;

	/** The current chunk. */
	// Global chunks
	private Chunk currentChunk = new Chunk();

	/**
	 * Instantiates a new gama 3 DS file.
	 *
	 * @param scope
	 *            the scope
	 * @param fileName
	 *            the file name
	 */
	// Constructor
	@doc (
			value = "This file constructor allows to read a 3DS Max file. Only loads vertices and faces",
			examples = { @example (
					value = "threeds_file f <- threeds_file(\"file\");",
					isExecutable = false)

			})
	public Gama3DSFile(final IScope scope, final String fileName) {
		super(scope, fileName);
	}

	// Verified
	@Override
	public void fillBuffer(final IScope scope) {
		setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
		try {
			final InputStream fileInputStream = Files.newInputStream(getFile(scope).toPath());
			dataInputStream = new DataInputStream(fileInputStream);
			readChunkHeader(currentChunk);
			if (currentChunk.id != PRIMARY) { DEBUG.ERR("Unable to load PRIMARY chunk from file " + getPath(scope)); }
			processNextChunk(currentChunk);
			dataInputStream.close();
			fileInputStream.close();
		} catch (final IOException e) {
			DEBUG.ERR("Error:  File IO error in: Closing File");
		}
		for (final Obj obj : objects) {
			final Geometry g = GeometryUtils.GEOMETRY_FACTORY.buildGeometry(obj.faces);
			getBuffer().add(GamaShapeFactory.createFrom(g));
		}

	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// TODO are there attributes ?
		return GamaListFactory.create(Types.STRING);
	}

	/**
	 * Process next chunk.
	 *
	 * @param previousChunk
	 *            the previous chunk
	 */
	// Verified
	void processNextChunk(final Chunk previousChunk) {
		// final int version = 0;
		byte buffer[] = null;
		currentChunk = new Chunk();
		try {
			while (previousChunk.bytesRead < previousChunk.length) {
				readChunkHeader(currentChunk);
				switch (currentChunk.id) {
					case VERSION:
						currentChunk.bytesRead += 4;
						break;

					case EDITOR:
						final Chunk tempChunk = new Chunk();
						readChunkHeader(tempChunk);
						buffer = new byte[tempChunk.length - tempChunk.bytesRead];
						tempChunk.bytesRead += dataInputStream.read(buffer, 0, tempChunk.length - tempChunk.bytesRead);
						currentChunk.bytesRead += tempChunk.bytesRead;
						processNextChunk(currentChunk);
						break;

					case OBJECT:
						final Obj obj = new Obj();
						objects.add(obj);
						processNextObjectChunk(obj, currentChunk);
						break;

					default:
						buffer = new byte[currentChunk.length - currentChunk.bytesRead];
						currentChunk.bytesRead +=
								dataInputStream.read(buffer, 0, currentChunk.length - currentChunk.bytesRead);
						break;
				}
				previousChunk.bytesRead += currentChunk.bytesRead;
			}
		} catch (final IOException e) {
			DEBUG.ERR("Error:  File IO error in: Process Next Chunk");
			return;
		}
		currentChunk = previousChunk;
	}

	/**
	 * Read chunk header.
	 *
	 * @param chunk
	 *            the chunk
	 */
	// Verified
	private void readChunkHeader(final Chunk chunk) {
		// byte buffer[] = new byte[2];

		try {
			chunk.id = swap(dataInputStream.readShort());
			chunk.id &= 0x0000FFFF;
			chunk.bytesRead = 2;
			chunk.length = swap(dataInputStream.readInt());
			chunk.bytesRead += 4;

		} catch (final IOException e) {
			DEBUG.ERR("Error:  File IO error in: Read Chunk Header");
			return;
		}
	}

	/**
	 * Process next object chunk.
	 *
	 * @param object
	 *            the object
	 * @param previousChunk
	 *            the previous chunk
	 */
	// Verified
	private void processNextObjectChunk(final Obj object, final Chunk previousChunk) {
		byte buffer[] = null;

		currentChunk = new Chunk();

		try {
			while (previousChunk.bytesRead < previousChunk.length) {
				readChunkHeader(currentChunk);

				switch (currentChunk.id) {
					case OBJECT_MESH:
						processNextObjectChunk(object, currentChunk);
						break;

					case OBJECT_VERTICES:
						readVertices(object, currentChunk);
						break;

					case OBJECT_FACES:
						readFaceList(object, currentChunk);
						break;

					default:
						buffer = new byte[currentChunk.length - currentChunk.bytesRead];
						currentChunk.bytesRead +=
								dataInputStream.read(buffer, 0, currentChunk.length - currentChunk.bytesRead);
						break;
				}
				previousChunk.bytesRead += currentChunk.bytesRead;
			}
		} catch (final IOException e) {
			DEBUG.ERR("Error:  File IO error in: Process Next Object Chunk");
			return;
		}
		currentChunk = previousChunk;
	}

	/**
	 * Read vertices.
	 *
	 * @param object
	 *            the object
	 * @param previousChunk
	 *            the previous chunk
	 */
	// Verified
	private void readVertices(final Obj object, final Chunk previousChunk) {
		try {
			final int numOfVerts = swap(dataInputStream.readShort());
			previousChunk.bytesRead += 2;

			object.verts = new GamaPoint[numOfVerts];
			for (int i = 0; i < numOfVerts; i++) {
				object.verts[i] = new GamaPoint(swap(dataInputStream.readFloat()), swap(dataInputStream.readFloat()),
						swap(dataInputStream.readFloat()));

				previousChunk.bytesRead += 12;
			}
		} catch (final IOException e) {
			DEBUG.ERR("Error: File IO error in: Read Vertices");
			return;
		}
	}

	/**
	 * Read face list.
	 *
	 * @param object
	 *            the object
	 * @param previousChunk
	 *            the previous chunk
	 */
	// Verified
	private void readFaceList(final Obj object, final Chunk previousChunk) {
		try {
			final int numOfFaces = swap(dataInputStream.readShort());
			previousChunk.bytesRead += 2;

			object.faces = new ArrayList<>(numOfFaces);
			for (int i = 0; i < numOfFaces; i++) {
				final List<IShape> points = new ArrayList<>();
				points.add(object.verts[swap(dataInputStream.readShort())]);
				points.add(object.verts[swap(dataInputStream.readShort())]);
				points.add(object.verts[swap(dataInputStream.readShort())]);
				final IShape face = GamaGeometryType.buildPolygon(points);
				object.faces.add(face.getInnerGeometry());

				// Read in the extra face info
				dataInputStream.readShort();

				// Account for how much data was read in (4 * 2bytes)
				previousChunk.bytesRead += 8;
			}
		} catch (final IOException e) {
			DEBUG.ERR("Error: File IO error in: Read Face List");
			return;
		}
	}

	/**
	 * Swap.
	 *
	 * @param value
	 *            the value
	 * @return the short
	 */
	private static short swap(final short value) {
		final int b1 = value & 0xff;
		final int b2 = value >> 8 & 0xff;
		return (short) (b1 << 8 | b2 << 0);
	}

	/**
	 * Swap.
	 *
	 * @param value
	 *            the value
	 * @return the int
	 */
	private static int swap(final int value) {
		final int b1 = value >> 0 & 0xff;
		final int b2 = value >> 8 & 0xff;
		final int b3 = value >> 16 & 0xff;
		final int b4 = value >> 24 & 0xff;
		return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
	}

	/**
	 * Swap.
	 *
	 * @param value
	 *            the value
	 * @return the float
	 */
	private static float swap(final float value) {
		int intValue = Float.floatToIntBits(value);
		intValue = swap(intValue);
		return Float.intBitsToFloat(intValue);
	}

}

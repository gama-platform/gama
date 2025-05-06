/*******************************************************************************************************
 *
 * GamaOsmFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.IConcept;
import gama.core.common.geometry.Envelope3D;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dependencies.osmosis.Bound;
import gama.dependencies.osmosis.Entity;
import gama.dependencies.osmosis.EntityContainer;
import gama.dependencies.osmosis.Node;
import gama.dependencies.osmosis.OsmHandler;
import gama.dependencies.osmosis.OsmosisReader;
import gama.dependencies.osmosis.Relation;
import gama.dependencies.osmosis.RelationMember;
import gama.dependencies.osmosis.RunnableSource;
import gama.dependencies.osmosis.Sink;
import gama.dependencies.osmosis.Tag;
import gama.dependencies.osmosis.Way;
import gama.dependencies.osmosis.WayNode;
import gama.dev.DEBUG;
import gama.gaml.operators.Strings;
import gama.gaml.operators.spatial.SpatialOperators;
import gama.gaml.operators.spatial.SpatialTransformations;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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

	/** The env. */
	final ReferencedEnvelope env = new ReferencedEnvelope();

	static final List<String> RESERVED_KEYS = List.of("location", "shape");

	/**
	 * The Class OSMInfo.
	 */
	public static class OSMInfo extends GamaFileMetaData {

		/** The item number. */
		int itemNumber;

		/** The crs. */
		CoordinateReferenceSystem crs;

		/** The width. */
		final double width;

		/** The height. */
		final double height;

		/** The attributes. */
		final Map<String, String> attributes = new LinkedHashMap();

		
		/**
		 * Instantiates a new OSM info.
		 *
		 * @param url
		 *            the url
		 * @param modificationStamp
		 *            the modification stamp
		 */
		public OSMInfo(final URL url, final long modificationStamp) {
			super(modificationStamp);
			CoordinateReferenceSystem crs = null;
			ReferencedEnvelope env2 = new ReferencedEnvelope();

			int number = 0;
			try {
				final File f = new File(url.toURI());
				final GamaOsmFile osmfile = new GamaOsmFile(null, f.getAbsolutePath());
				attributes.putAll(osmfile.getOSMAttributes(GAMA.getRuntimeScope()));

				final SimpleFeatureType TYPE = DataUtilities.createType("geometries", "geom:LineString");
				final ArrayList<SimpleFeature> list = new ArrayList<>();
				for (final IShape shape : osmfile.iterable(null)) {
					list.add(SimpleFeatureBuilder.build(TYPE, new Object[] { shape.getInnerGeometry() }, null));
				}
				final SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, list);
				final SimpleFeatureSource featureSource = DataUtilities.source(collection);

				env2 = featureSource.getBounds();
				number = osmfile.nbObjects;
				crs = osmfile.getOwnCRS(null);
			} catch (final Exception e) {
				DEBUG.ERR("Error in reading metadata of " + url);
				hasFailed = true;

			} finally {

				// approximation of the width and height in meters.
				width = env2 != null ? env2.getWidth() * (Math.PI / 180) * 6378137 : 0;
				height = env2 != null ? env2.getHeight() * (Math.PI / 180) * 6378137 : 0;
				itemNumber = number;
				this.crs = crs;
			}

		}

		/**
		 * Gets the crs.
		 *
		 * @return the crs
		 */
		public CoordinateReferenceSystem getCRS() { return crs; }

		/**
		 * Instantiates a new OSM info.
		 *
		 * @param propertiesString
		 *            the properties string
		 * @throws NoSuchAuthorityCodeException
		 *             the no such authority code exception
		 * @throws FactoryException
		 *             the factory exception
		 */
		public OSMInfo(final String propertiesString) throws NoSuchAuthorityCodeException, FactoryException {
			super(propertiesString);
			if (!hasFailed) {
				final String[] segments = split(propertiesString);
				itemNumber = Integer.parseInt(segments[1]);
				final String crsString = segments[2];
				if ("null".equals(crsString)) {
					crs = null;
				} else {
					crs = CRS.parseWKT(crsString);
				}
				width = Double.parseDouble(segments[3]);
				height = Double.parseDouble(segments[4]);
				if (segments.length > 5) {
					final String[] names = splitByWholeSeparatorPreserveAllTokens(segments[5], SUB_DELIMITER);
					final String[] types = splitByWholeSeparatorPreserveAllTokens(segments[6], SUB_DELIMITER);
					for (int i = 0; i < names.length; i++) { attributes.put(names[i], types[i]); }
				}
			} else {
				itemNumber = 0;
				width = 0.0;
				height = 0.0;
				crs = null;
			}
		}

		/**
		 * Method getSuffix()
		 *
		 * @see gama.core.util.file.GamaFileMetaInformation#getSuffix()
		 */
		@Override
		public String getSuffix() {
			return hasFailed ? "error: decompress the file to a .osm file"
					: "" + itemNumber + " objects | " + Math.round(width) + "m x " + Math.round(height) + "m";
		}

		@Override
		public void appendSuffix(final StringBuilder sb) {
			if (hasFailed) {
				sb.append("error: decompress the file to a .osm file");
				return;
			}
			sb.append(itemNumber).append(" object");
			if (itemNumber > 1) { sb.append("s"); }
			sb.append(SUFFIX_DEL);
			sb.append(Math.round(width)).append("m x ");
			sb.append(Math.round(height)).append("m");
		}

		@Override
		public String getDocumentation() {
			final StringBuilder sb = new StringBuilder();
			if (hasFailed) {
				sb.append("Unreadable OSM file").append(Strings.LN)
						.append("Decompress the file to an .osm file and retry");
			} else {
				sb.append("OSM file").append(Strings.LN);
				sb.append(itemNumber).append(" objects").append(Strings.LN);
				sb.append("Dimensions: ").append(Math.round(width) + "m x " + Math.round(height) + "m")
						.append(Strings.LN);
				sb.append("Coordinate Reference System: ").append(crs == null ? "No CRS" : crs.getName().getCode())
						.append(Strings.LN);
				if (!attributes.isEmpty()) {
					sb.append("Attributes: ").append(Strings.LN);
					attributes.forEach((k, v) -> sb.append("<li>").append(k).append(" (" + v + ")").append("</li>"));
				}
			}
			return sb.toString();
		}

		/**
		 * Gets the attributes.
		 *
		 * @return the attributes
		 */
		public Map<String, String> getAttributes() { return attributes; }

		@Override
		public String toPropertyString() {
			final String attributeNames = String.join(SUB_DELIMITER, attributes.keySet());
			final String types = String.join(SUB_DELIMITER, attributes.values());
			final String[] toSave =
					{ super.toPropertyString(), String.valueOf(itemNumber), crs == null ? "null" : crs.toWKT(),
							String.valueOf(width), String.valueOf(height), attributeNames, types };
			return String.join(DELIMITER, toSave);
		}
	}

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
			value = "This file constructor allows to read an osm (.osm, .pbf, .bz2, .gz) file (using WGS84 coordinate system for the data)"
					+ "The map is used to filter the objects in the file according their attributes: for each key (string) of the map, only the objects that have a value for the  attribute "
					+ "contained in the value set are kept."
					+ " For an exhaustive list of the attibute of OSM data, see: http://wiki.openstreetmap.org/wiki/Map_Features",

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
		final Map<Long, GamaShape> nodesPt = new HashMap<>();
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
					final Envelope3D e =
							Envelope3D.of(bound.getLeft(), bound.getRight(), bound.getBottom(), bound.getTop(), 0, 0);
					computeProjection(scope, e);
				} else if (returnIt) {
					if (entity instanceof Node node) {
						final Geometry g = gis == null
								? new GamaPoint(node.getLongitude(), node.getLatitude()).getInnerGeometry()
								: gis.transform(
										new GamaPoint(node.getLongitude(), node.getLatitude()).getInnerGeometry());

						// final Geometry g = new GamaPoint(node.getLongitude(), node.getLatitude()).getInnerGeometry();
						// env.expandToInclude(g.getCoordinate());
						nodesPt.put(node.getId(), GamaShapeFactory.createFrom(g));
						nodesFromId.put(node.getId(), node);
						boolean keepObject = keepEntity(toFilter, entity);
						if (!keepObject) return;
						nodes.add(node);

					} else if (entity instanceof Way) {
						/*boolean keepObject = keepEntity(toFilter, entity);
						if (!keepObject) return;*/
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
			final List<Relation> relations, final Set<Long> intersectionNodes, final Map<Long, GamaShape> nodesPt,
			final Map<Long, Node> nodesFromId) {
		
		boolean toFilter = filteringOptions != null && !filteringOptions.isEmpty();
		for (final Way way : ways) {
			for (WayNode wn : way.getWayNodes()) { nodes.add(nodesFromId.get(wn.getNodeId())); }
		}

		final IList<IShape> geometries = GamaListFactory.create(Types.GEOMETRY);
		if (gis == null) {
			computeProjection(scope, Envelope3D.of(env));
			if (gis != null) {
				for (Long id : nodesPt.keySet()) {
					GamaShape sp = GamaShapeFactory.createFrom(gis.transform(nodesPt.get(id).getInnerGeometry()));
					nodesPt.put(id, sp);
				}
			}
		}
		final Map<Long, Entity> geomMap = new HashMap<>();

		for (final Node node : nodes) {
			//geomMap.put(node.getId(), node);
			final GamaShape pt = nodesPt.get(node.getId());
			final boolean hasAttributes = !node.getTags().isEmpty();
			final Map<String, String> atts = new HashMap<>();
			if (pt != null) {
				env.expandToInclude(pt.getLocation());

				for (final Tag tg : node.getTags()) {
					final String key = tg.getKey();
					if (RESERVED_KEYS.contains(key)) continue;
					
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
			if (!keepObject) continue;
			final IMap<String, Object> values = GamaMapFactory.create();
			final Map<String, String> atts = GamaMapFactory.createUnordered();

			for (final Tag tg : way.getTags()) {
				final String key = tg.getKey();
				if (RESERVED_KEYS.contains(key)) continue;
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
					final GamaShape pp = nodesPt.get(node.getNodeId());
					if (pp == null) { continue; }
					points.add(pp);
				}
				if (points.size() < 3) { continue; }

				final IShape geom = GamaGeometryType.buildPolygon(points);

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
				if (RESERVED_KEYS.contains(key)) continue;
				values.put(key, tg.getValue());
			}
			String type = (String) values.get("type");
			if ("polygon".equals(type) || "multipolygon".equals(type)) {
				
				managePolygonRelation(scope, relation, geometries, geomMap, values,  nodesPt,intersectionNodes , atts) ;
						
			} else {
				 manageNormalRelation(scope, relation, geometries, geomMap, values,  nodesPt,intersectionNodes ) ;
					
			}

			
		}
		nbObjects = geometries == null ? 0 : geometries.size();
		return geometries;
	}
	
	
	private void managePolygonRelation(IScope scope, Relation relation, final IList<IShape> geometries, final Map<Long, Entity> geomMap, final Map<String, Object> values,  final Map<Long, GamaShape> nodesPt, final Set<Long> intersectionNodes, Map<String, String> atts ) {
		final List<IShape> points = GamaListFactory.create(Types.GEOMETRY);
		
		IList<IList<IShape>> ptsList = GamaListFactory.create() ;
		IList<IShape> inner = GamaListFactory.create() ;
		for (final RelationMember member : relation.getMembers()) {
			final Entity entity = geomMap.get(member.getMemberId());
			if (entity instanceof Way) {
				IList<IShape> pts = GamaListFactory.create() ;
				final Way way = ((Way) entity);
				for (final WayNode node : way.getWayNodes()) {
					final GamaShape pp = nodesPt.get(node.getNodeId());
					if (pp == null) { continue; }
					pts.add(pp);
					
					
				}
				if (member.getMemberRole().equals("outer"))
					ptsList.add(pts);
				else {
					inner.add(GamaGeometryType.buildPolygon(pts));
				}
				
			}
		}
		if (ptsList.size() > 1) {
			IList<IShape> ptsCurrent = ptsList.get(0);
			ptsList.remove(ptsCurrent);
			for(IList<IShape>  pts : ptsList) {
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
			
		} else if (!ptsList.isEmpty()){
			points.addAll(ptsList.get(0));
		}
		
		if (points.size() < 3) { return; }

		IShape geomTmp = GamaGeometryType.buildPolygon(points);
		
		
		if (geomTmp != null && geomTmp.getInnerGeometry() != null && !geomTmp.getInnerGeometry().isEmpty()
				&& geomTmp.getInnerGeometry().getArea() > 0) {
			
			if (inner != null &&!inner.isEmpty())
				geomTmp = SpatialOperators.minus(scope, geomTmp, inner);
			
			
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
	
	private void manageNormalRelation(IScope scope, Relation relation, final IList<IShape> geometries, final Map<Long, Entity> geomMap, final Map<String, Object> values,  final Map<Long, GamaShape> nodesPt, final Set<Long> intersectionNodes ) {
		int order = 0;
		for (final RelationMember member : relation.getMembers()) {
			final Entity entity = geomMap.get(member.getMemberId());
			if (entity instanceof Way) {
				final List<WayNode> relationWays = ((Way) entity).getWayNodes();
				final Map<String, Object> wayValues = GamaMapFactory.create();
				wayValues.put("entity_order", order++);
				// TODO FIXME AD: What's that ??
				wayValues.put("gama_bus_line", values.get("name"));
				wayValues.put("osm_way_id", ((Way) entity).getId());
				if (relationWays.size() > 0) {
					final List<IShape> geoms = createSplitRoad(relationWays, wayValues, intersectionNodes, nodesPt);
					geometries.addAll(geoms);
				}
			} else if (entity instanceof Node) {
				final GamaShape pt = nodesPt.get(((Node) entity).getId());
				final GamaShape pt2 = pt.copy(scope);

				final List objs = GamaListFactory.create(Types.GEOMETRY);
				objs.add(pt2);

				pt2.setAttribute("gama_bus_line", values.get("name"));

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
			final Set<Long> intersectionNodes, final Map<Long, GamaShape> nodesPt) {
		final List<List<IShape>> pointsList = GamaListFactory.create(Types.LIST.of(Types.GEOMETRY));
		List<IShape> points = GamaListFactory.create(Types.GEOMETRY);
		final IList<IShape> geometries = GamaListFactory.create(Types.GEOMETRY);
		final WayNode endNode = wayNodes.get(wayNodes.size() - 1);
		for (final WayNode node : wayNodes) {
			final Long id = node.getNodeId();
			final GamaShape pt = nodesPt.get(id);
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
		final IShape geom = GamaGeometryType.buildPolyline(points);
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
		RunnableSource reader = null;
		switch (ext) {
			case "pbf":
				try (InputStream stream = Files.newInputStream(osmFile.toPath())) {
					reader = new OsmosisReader(stream);
					reader.setSink(sink);
					reader.run();
				} catch (final IOException e) {
					throw GamaRuntimeException.create(e, scope);
				}
				break;
			default:
				readXML(scope, sink);
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
	public Envelope3D computeEnvelope(final IScope scope) {
		if (gis == null) { getFeatureIterator(scope, false); }
		if (gis == null) return Envelope3D.of(env);
		return gis.getProjectedEnvelope();

	}

	/**
	 * Method getExistingCRS()
	 *
	 * @see gama.core.util.file.GamaGisFile#getExistingCRS()
	 */
	@Override
	protected CoordinateReferenceSystem getOwnCRS(final IScope scope) {
		// Is it always true ?
		return DefaultGeographicCRS.WGS84;
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

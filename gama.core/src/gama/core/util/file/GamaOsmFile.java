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

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.v0_6.impl.OsmHandler;

import crosby.binary.osmosis.OsmosisReader;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.IConcept;
import gama.core.common.geometry.GamaEnvelopeFactory;
import gama.core.common.geometry.IEnvelope;
import gama.core.metamodel.shape.GamaPointFactory;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.list.GamaListFactory;
import gama.core.util.list.IList;
import gama.core.util.map.GamaMapFactory;
import gama.core.util.map.IMap;
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
			final List<Relation> relations, final Set<Long> intersectionNodes, final Map<Long, GamaShape> nodesPt,
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
					GamaShape sp = GamaShapeFactory.createFrom(gis.transform(nodesPt.get(id).getInnerGeometry()));
					nodesPt.put(id, sp);
				}
			}
		}
		final Map<Long, Entity> geomMap = new HashMap<>();

		for (final Node node : nodes) {
			// geomMap.put(node.getId(), node);
			final GamaShape pt = nodesPt.get(node.getId());
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
				if (RESERVED_KEYS.contains(key)) { continue; }
				values.put(key, tg.getValue());
			}
			String type = (String) values.get("type");
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
			final Map<Long, Entity> geomMap, final Map<String, Object> values, final Map<Long, GamaShape> nodesPt,
			final Set<Long> intersectionNodes, final Map<String, String> atts) {
		final List<IShape> points = GamaListFactory.create(Types.GEOMETRY);

		IList<IList<IShape>> ptsList = GamaListFactory.create();
		IList<IShape> inner = GamaListFactory.create();
		for (final RelationMember member : relation.getMembers()) {
			final Entity entity = geomMap.get(member.getMemberId());
			if (entity instanceof final Way way) {
				IList<IShape> pts = GamaListFactory.create();
				for (final WayNode node : way.getWayNodes()) {
					final GamaShape pp = nodesPt.get(node.getNodeId());
					if (pp == null) { continue; }
					pts.add(pp);

				}
				if ("outer".equals(member.getMemberRole())) {
					ptsList.add(pts);
				} else {
					inner.add(GamaGeometryType.buildPolygon(pts));
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

		IShape geomTmp = GamaGeometryType.buildPolygon(points);

		if (geomTmp != null && geomTmp.getInnerGeometry() != null && !geomTmp.getInnerGeometry().isEmpty()
				&& geomTmp.getInnerGeometry().getArea() > 0) {

			if (inner != null && !inner.isEmpty()) { geomTmp = SpatialOperators.minus(scope, geomTmp, inner); }

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
			final Map<Long, Entity> geomMap, final Map<String, Object> values, final Map<Long, GamaShape> nodesPt,
			final Set<Long> intersectionNodes) {
		int order = 0;
		for (final RelationMember member : relation.getMembers()) {
			final Entity entity = geomMap.get(member.getMemberId());
			if (entity instanceof Way) {
				final List<WayNode> relationWays = ((Way) entity).getWayNodes();
				final Map<String, Object> wayValues = GamaMapFactory.create();
				wayValues.put("entity_order", order++);
				// TODO FIXME AD: What's that ??
				wayValues.put("gama_bus_line", values.get("name"));
				wayValues.put("osm_way_id", entity.getId());
				if (relationWays.size() > 0) {
					final List<IShape> geoms = createSplitRoad(relationWays, wayValues, intersectionNodes, nodesPt);
					geometries.addAll(geoms);
				}
			} else if (entity instanceof Node) {
				final GamaShape pt = nodesPt.get(entity.getId());
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
			case "pbf" -> {
				reader = new OsmosisReader(osmFile);
				reader.setSink(sink);
				reader.run();
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

	/**
	 * Method getExistingCRS()
	 *
	 * @see gama.core.util.file.GamaGisFile#getExistingCRS()
	 */
	@Override
	public CoordinateReferenceSystem getOwnCRS(final IScope scope) {
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
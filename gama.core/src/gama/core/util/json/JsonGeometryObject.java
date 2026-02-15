/*******************************************************************************************************
 *
 * JsonGeometryObject.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.json;

import static gama.api.utils.geometry.GeometryUtils.getGeometryFactory;

import java.util.ArrayList;
import java.util.List;

import org.geotools.api.referencing.FactoryException;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;

import gama.api.GAMA;
import gama.api.data.factories.GamaCoordinateSequenceFactory;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.json.IJson;
import gama.api.data.json.IJsonArray;
import gama.api.data.json.IJsonObject;
import gama.api.data.json.IJsonValue;
import gama.api.data.objects.ICoordinates;
import gama.api.data.objects.IShape;
import gama.api.data.objects.IShape.Type;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.GamaPointFactory;
import gama.core.topology.gis.ProjectionFactory;
import one.util.streamex.StreamEx;

/**
 * The Class JsonGeometryObject. Takes care of encoding geometries using the GeoJson format, adding the SRID of the
 * current CRS.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 nov. 2023
 */
public class JsonGeometryObject extends JsonGamlObject {

	/**
	 * The prefix for EPSG codes in the <code>crs</code> property.
	 */
	public static final String EPSG_PREFIX = "EPSG:";

	/**
	 * Instantiates a new json shape object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param json
	 *            the json
	 * @date 4 nov. 2023
	 */
	public JsonGeometryObject(final Geometry geometry, final IJson json) {
		this(toGeoJsonObject(geometry, json), json);
		try {
			int srid = CRS.lookupEpsgCode(ProjectionFactory.getTargetCRSOrDefault(GAMA.getRuntimeScope()).getCRS(), true);
			add(IJson.Labels.NAME_CRS, json.object(IJson.Labels.NAME_TYPE, IJson.Labels.NAME_NAME,
					IJson.Labels.NAME_PROPERTIES, json.object(IJson.Labels.NAME_NAME, EPSG_PREFIX + srid)));
		} catch (FactoryException e) {}
	}

	/**
	 * Instantiates a new json geometry object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the object
	 * @param json
	 *            the json
	 * @date 5 nov. 2023
	 */
	public JsonGeometryObject(final IJsonObject object, final IJson json) {
		super(Types.GEOMETRY.getName(), object, json);
	}

	/**
	 * Creates the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param geometry
	 *            the geometry
	 * @param encodeCRS
	 *            the encode CRS
	 * @return the map
	 * @date 4 nov. 2023
	 */
	private static JsonAbstractObject toGeoJsonObject(final Geometry geometry, final IJson json) {
		JsonAbstractObject result = new JsonObject(json);
		result.add(IJson.Labels.NAME_TYPE, geometry.getGeometryType());
		IJsonArray components = json.array();
		String key = IJson.Labels.NAME_COORDINATES;
		if (geometry instanceof Point || geometry instanceof LineString) {
			components = (JsonArray) json.valueOf(GamaCoordinateSequenceFactory.pointsOf(geometry));
		} else {
			switch (geometry) {
				case Polygon polygon -> components = toJsonArray(polygon, json);
				case MultiPoint multiPoint -> components = toJsonArray(multiPoint, json);
				case MultiLineString multiLineString -> components = toJsonArray(multiLineString, json);
				case MultiPolygon multiPolygon -> components = toJsonArray(multiPolygon, json);
				case GeometryCollection geometryCollection -> {
					for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
						components.add(toGeoJsonObject(geometryCollection.getGeometryN(i), json));
					}
					key = IJson.Labels.NAME_GEOMETRIES;
				}
				default -> throw new IllegalArgumentException(
						"Unable to encode geometry " + geometry.getGeometryType());
			}
		}
		result.add(key, components);
		return result;
	}

	/**
	 * Make json aware.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param poly
	 *            the poly
	 * @return the list
	 * @date 4 nov. 2023
	 */
	private static IJsonArray toJsonArray(final Polygon poly, final IJson json) {
		IJsonArray result = json.array();
		result.add(json.valueOf(GamaCoordinateSequenceFactory.pointsOf(poly)));
		for (int i = 0; i < poly.getNumInteriorRing(); i++) {
			result.add(json.valueOf(GamaCoordinateSequenceFactory.pointsOf(poly.getInteriorRingN(i))));
		}
		return result;
	}

	/**
	 * Make json aware.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param geometryCollection
	 *            the geometry collection
	 * @return the list
	 * @date 4 nov. 2023
	 */
	private static IJsonArray toJsonArray(final GeometryCollection geometryCollection, final IJson json) {
		IJsonArray list = json.array();
		for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
			Geometry geometry = geometryCollection.getGeometryN(i);
			if (geometry instanceof Polygon polygon) {
				list.add(toJsonArray(polygon, json));
			} else if (geometry instanceof LineString || geometry instanceof Point) {
				list.add(json.valueOf(GamaCoordinateSequenceFactory.pointsOf(geometry)));
			}
		}
		return list;
	}

	@Override
	public IShape toGamlValue(final IScope scope) {
		Geometry g = fromGeoJsonObject(this);
		// int srid = readSRID(this);
		// TODO what to do with SRID ?
		// g = GeometryUtils.cleanGeometry(g);
		// g = scope.getSimulation().getProjectionFactory().getWorld().transform(g);

		IShape shape = GamaShapeFactory.createFrom(g);
		shape.setGeometricalType(Type.valueOf(get("inner_type").asString()));
		if (contains("depth")) { shape.setDepth(get("depth").asDouble()); }
		return shape;
	}

	/**
	 * Creates the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the geometry map
	 * @param factory
	 *            the geometry factory
	 * @return the geometry
	 * @throws ParseException
	 *             the parse exception
	 * @date 5 nov. 2023
	 */
	private static Geometry fromGeoJsonObject(final JsonGeometryObject object) {
		return switch (object.get(IJson.Labels.NAME_TYPE).asString()) {
			case IJson.Labels.NAME_POINT -> buildPoint(object);
			case IJson.Labels.NAME_LINESTRING -> buildLineString(object);
			case IJson.Labels.NAME_POLYGON -> buildPolygon(object);
			case IJson.Labels.NAME_MULTIPOINT -> buildMultiPoint(object);
			case IJson.Labels.NAME_MULTILINESTRING -> buildMultiLineString(object);
			case IJson.Labels.NAME_MULTIPOLYGON -> buildMultiPolygon(object);
			case IJson.Labels.NAME_GEOMETRYCOLLECTION -> buildGeometryCollection(object);
			case IJson.Labels.NAME_FEATURE -> manageFeature(object);
			case IJson.Labels.NAME_FEATURECOLLECTION -> manageFeatureCollection(object);
			default -> throw new RuntimeException("Unexpected value for type");
		};
	}

	/**
	 * Creates a feature collection.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param geometryMap
	 *            the geometry map
	 * @param geometryFactory
	 *            the geometry factory
	 * @return the geometry
	 * @throws ParseException
	 *             the parse exception
	 * @date 5 nov. 2023
	 */
	private static Geometry manageFeatureCollection(final JsonGeometryObject geometryMap) {
		return getGeometryFactory().createGeometryCollection(
				StreamEx.of(geometryMap.get(IJson.Labels.NAME_FEATURES).asArray()).select(JsonGeometryObject.class)
						.map(JsonGeometryObject::manageFeature).toArray(Geometry.class));
	}

	/**
	 * Creates a feature.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param geometryMap
	 *            the geometry map
	 * @param geometryFactory
	 *            the geometry factory
	 * @return the geometry
	 * @throws ParseException
	 *             the parse exception
	 * @date 5 nov. 2023
	 */
	private static Geometry manageFeature(final JsonGeometryObject geometryMap) {
		return fromGeoJsonObject((JsonGeometryObject) geometryMap.get(IJson.Labels.NAME_GEOMETRY));
	}

	/**
	 * Creates the geometry collection.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param geometryMap
	 *            the geometry map
	 * @param geometryFactory
	 *            the geometry factory
	 * @return the geometry
	 * @throws ParseException
	 *             the parse exception
	 * @date 5 nov. 2023
	 */
	private static Geometry buildGeometryCollection(final JsonGeometryObject geometryMap) {
		return getGeometryFactory().createGeometryCollection(
				StreamEx.of(geometryMap.get(IJson.Labels.NAME_GEOMETRIES).asArray()).select(JsonGeometryObject.class)
						.map(JsonGeometryObject::fromGeoJsonObject).toArray(Geometry.class));
	}

	/**
	 * Creates a multi polygon.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param geometryMap
	 *            the geometry map
	 * @param geometryFactory
	 *            the geometry factory
	 * @return the geometry
	 * @throws ParseException
	 *             the parse exception
	 * @date 5 nov. 2023
	 */
	private static Geometry buildMultiPolygon(final JsonGeometryObject geometryMap) {
		IJsonArray polygonsList = geometryMap.get(IJson.Labels.NAME_COORDINATES).asArray();
		Polygon[] polygons = new Polygon[polygonsList.size()];
		int p = 0;
		for (IJsonValue ringsList : polygonsList) {
			List<CoordinateSequence> rings = new ArrayList<>();
			for (IJsonValue coordinates : ringsList.asArray()) {
				rings.add(createCoordinateSequence(coordinates.asArray()));
			}
			if (rings.isEmpty()) { continue; }
			LinearRing outer = getGeometryFactory().createLinearRing(rings.get(0));
			LinearRing[] inner = null;
			if (rings.size() > 1) {
				inner = new LinearRing[rings.size() - 1];
				for (int i = 1; i < rings.size(); i++) {
					inner[i - 1] = getGeometryFactory().createLinearRing(rings.get(i));
				}
			}
			polygons[p] = getGeometryFactory().createPolygon(outer, inner);
			++p;
		}
		Geometry result = getGeometryFactory().createMultiPolygon(polygons);
		return result;
	}

	/**
	 * Creates a multi linestring.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param geometryMap
	 *            the geometry map
	 * @param geometryFactory
	 *            the geometry factory
	 * @return the geometry
	 * @throws ParseException
	 *             the parse exception
	 * @date 5 nov. 2023
	 */
	private static Geometry buildMultiLineString(final JsonGeometryObject geometryMap) {
		return getGeometryFactory()
				.createMultiLineString(StreamEx.of(geometryMap.get(IJson.Labels.NAME_COORDINATES).asArray())
						.map(c -> getGeometryFactory().createLineString(createCoordinateSequence(c.asArray())))
						.toArray(LineString.class));
	}

	/**
	 * Creates the multi point.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param geometryMap
	 *            the geometry map
	 * @param geometryFactory
	 *            the geometry factory
	 * @return the geometry
	 * @throws ParseException
	 *             the parse exception
	 * @date 5 nov. 2023
	 */
	private static Geometry buildMultiPoint(final JsonGeometryObject geometryMap) {
		return getGeometryFactory()
				.createMultiPoint(createCoordinateSequence(geometryMap.get(IJson.Labels.NAME_COORDINATES).asArray()));
	}

	/**
	 * Creates the polygon.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param geometryMap
	 *            the geometry map
	 * @param geometryFactory
	 *            the geometry factory
	 * @return the geometry
	 * @throws ParseException
	 *             the parse exception
	 * @date 5 nov. 2023
	 */
	private static Geometry buildPolygon(final JsonGeometryObject geometryMap) {
		IJsonArray ringsList = geometryMap.get(IJson.Labels.NAME_COORDINATES).asArray();
		if (ringsList.isEmpty()) return getGeometryFactory().createPolygon();
		List<CoordinateSequence> rings = new ArrayList<>();
		for (IJsonValue coordinates : ringsList) { rings.add(createCoordinateSequence(coordinates.asArray())); }
		LinearRing outer = getGeometryFactory().createLinearRing(rings.get(0));
		LinearRing[] inner = null;
		if (rings.size() > 1) {
			inner = new LinearRing[rings.size() - 1];
			for (int i = 1; i < rings.size(); i++) { inner[i - 1] = getGeometryFactory().createLinearRing(rings.get(i)); }
		}
		Geometry result = getGeometryFactory().createPolygon(outer, inner);
		return result;
	}

	/**
	 * Creates the line string.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param geometryMap
	 *            the geometry map
	 * @param geometryFactory
	 *            the geometry factory
	 * @return the geometry
	 * @throws ParseException
	 *             the parse exception
	 * @date 5 nov. 2023
	 */
	private static Geometry buildLineString(final JsonGeometryObject geometryMap) {
		return getGeometryFactory()
				.createLineString(createCoordinateSequence(geometryMap.get(IJson.Labels.NAME_COORDINATES).asArray()));
	}

	/**
	 * Creates the point.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the geometry map
	 * @param geometryFactory
	 *            the geometry factory
	 * @return the geometry
	 * @throws ParseException
	 *             the parse exception
	 * @date 5 nov. 2023
	 */
	private static Geometry buildPoint(final JsonGeometryObject object) {
		IJsonArray c = object.get(IJson.Labels.NAME_COORDINATES).asArray();
		if (c.isEmpty()) return null;
		c = c.get(0).asArray();
		CoordinateSequence coordinate = GamaCoordinateSequenceFactory
				.create(GamaPointFactory.create(c.get(0).asDouble(), c.get(1).asDouble(), c.get(2).asDouble()));
		return getGeometryFactory().createPoint(coordinate);
	}

	/**
	 * Creates the coordinate sequence.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param coordinates
	 *            the coordinates
	 * @return the coordinate sequence
	 * @date 5 nov. 2023
	 */
	private static CoordinateSequence createCoordinateSequence(final IJsonArray coordinates) {
		ICoordinates result = GamaCoordinateSequenceFactory.ofLength(coordinates.size());
		int i = 0;
		for (IJsonValue ordinates : coordinates) {
			IJsonArray c = ordinates.asArray();
			if (c.size() > 0) { result.setOrdinate(i, 0, c.get(0).asDouble()); }
			if (c.size() > 1) { result.setOrdinate(i, 1, c.get(1).asDouble()); }
			if (c.size() > 2) { result.setOrdinate(i++, 2, c.get(2).asDouble()); }
		}
		return result;
	}

	/**
	 * Read SRID.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the object
	 * @return the int
	 * @date 5 nov. 2023
	 */
	private static int readSRID(final JsonGeometryObject object) {
		IJsonObject crs = object.get(IJson.Labels.NAME_CRS).asObject();
		if (crs != null) {
			IJsonObject properties = crs.get(IJson.Labels.NAME_PROPERTIES).asObject();
			String name = properties.get(IJson.Labels.NAME_NAME).asString();
			String[] split = name.split(":");
			String epsg = split[1];
			return Integer.parseInt(epsg);
		}
		return 4326;
		// The default CRS is a geographic coordinate reference
		// system, using the WGS84 datum, and with longitude and
		// latitude units of decimal degrees. SRID 4326
	}

}

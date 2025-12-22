/*******************************************************************************************************
 *
 * FeatureHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import gama.dependencies.geojson.DelegatingHandler;
import gama.dependencies.geojson.IContentHandler;
import gama.dependencies.geojson.geom.GeometryCollectionHandler;
import gama.dependencies.geojson.geom.GeometryHandler;

/**
 * The Class FeatureHandler.
 */
public class FeatureHandler extends DelegatingHandler<SimpleFeature> {

	/** The fid. */
	private int fid = 0;

	/** The separator. */
	private String separator = "-";

	/** The id. */
	String id;

	/** The geometry. */
	Geometry geometry;

	/** The values. */
	List<Object> values;

	/** The properties. */
	List<String> properties;

	/** The crs. */
	CoordinateReferenceSystem crs;

	/** The builder. */
	SimpleFeatureBuilder builder;

	/** The attio. */
	AttributeIO attio;

	/** The feature. */
	SimpleFeature feature;

	/** The base id. */
	private String baseId = "feature";

	/** should we attempt to automatically build fids */
	private boolean autoFID = false;

	/**
	 * Instantiates a new feature handler.
	 */
	public FeatureHandler() {
		this(null, new DefaultAttributeIO());
	}

	/**
	 * Instantiates a new feature handler.
	 *
	 * @param builder
	 *            the builder
	 * @param attio
	 *            the attio
	 */
	public FeatureHandler(final SimpleFeatureBuilder builder, final AttributeIO attio) {
		this.builder = builder;
		this.attio = attio;
	}

	/**
	 * Start object.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean startObject() throws ParseException, IOException {
		if (properties == NULL_LIST) {
			properties = new ArrayList<>();
		} else if (properties != null) {
			// start of a new object in properties means a geometry
			delegate = new GeometryHandler(new GeometryFactory());
		}

		return super.startObject();
	}

	/**
	 * Start object entry.
	 *
	 * @param key
	 *            the key
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean startObjectEntry(final String key) throws ParseException, IOException {
		if ("id".equals(key) && properties == null) {
			id = "";
			return true;
		}
		if ("crs".equals(key) && properties == null /* it's top level, not a property */) {
			delegate = new CRSHandler();
			return true;
		}
		if ("geometry".equals(key) && properties == null /* it's top level, not a property */) {
			delegate = new GeometryHandler(new GeometryFactory());
			return true;
		}
		if ("properties".equals(key) && delegate == NULL) {
			properties = NULL_LIST;
			values = new ArrayList<>();
		} else if (properties != null && delegate == NULL) {
			properties.add(key);
			return true;
		}

		return super.startObjectEntry(key);
	}

	/**
	 * Start array.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean startArray() throws ParseException, IOException {
		if (properties != null && delegate == NULL) {
			// array inside of properties
			delegate = new ArrayHandler();
		}

		return super.startArray();
	}

	/**
	 * End array.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean endArray() throws ParseException, IOException {
		if (delegate instanceof ArrayHandler) {
			super.endArray();
			values.add(((ArrayHandler) delegate).getValue());
			delegate = NULL;
		}
		return super.endArray();
	}

	/**
	 * End object.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean endObject() throws ParseException, IOException {
		if (delegate instanceof IContentHandler) {
			delegate.endObject();
			if (delegate instanceof GeometryHandler geometryHandler) {
				Geometry g = geometryHandler.getValue();
				if (g != null || !(((GeometryHandler) delegate).getDelegate() instanceof GeometryCollectionHandler)) {
					if (properties != null) {
						// this is a regular property
						values.add(g);
					} else {
						// its the default geometry
						geometry = g;
					}
					delegate = NULL;
				}
			} else if (delegate instanceof CRSHandler) {
				crs = ((CRSHandler) delegate).getValue();
				delegate = UNINITIALIZED;
			}
		} else if (delegate == UNINITIALIZED) {
			delegate = NULL;
		} else {
			if (properties != null) {
				if (builder == null) {
					// no builder specified, build on the fly
					builder = createBuilder();
				}
				for (int i = 0; i < properties.size(); i++) {
					String att = properties.get(i);
					Object val = values.get(i);
					if (val instanceof String) { val = attio.parse(att, (String) val); }
					builder.set(att, val);
				}
			} else {
				feature = buildFeature();
				id = null;
				geometry = null;
			}
			properties = null;
			values = null;
		}
		return true;
	}

	/**
	 * Primitive.
	 *
	 * @param value
	 *            the value
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean primitive(final Object value) throws ParseException, IOException {
		if (delegate instanceof GeometryHandler && value == null) {
			delegate = NULL;
			return true;
		}
		if ("".equals(id)) {
			id = value.toString();
			setFID(id);
			return true;
		}
		if (values != null && delegate == NULL) {
			// use the attribute parser
			values.add(value);
			return true;
		}

		return super.primitive(value);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	@Override
	public SimpleFeature getValue() { return feature; }

	/**
	 * Gets the crs.
	 *
	 * @return the crs
	 */
	public CoordinateReferenceSystem getCRS() { return crs; }

	/**
	 * Sets the crs.
	 *
	 * @param crs
	 *            the new crs
	 */
	public void setCRS(final CoordinateReferenceSystem crs) { this.crs = crs; }

	/**
	 * Inits the.
	 */
	public void init() {
		feature = null;
	}

	/**
	 * Creates the builder.
	 *
	 * @return the simple feature builder
	 */
	SimpleFeatureBuilder createBuilder() {
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName("feature");
		typeBuilder.setNamespaceURI("http://geotools.org");
		typeBuilder.setCRS(crs);

		if (properties != null) {
			for (int i = 0; i < properties.size(); i++) {
				String prop = properties.get(i);
				Object valu = values.get(i);
				typeBuilder.add(prop, valu != null ? valu.getClass() : Object.class);
			}
		}
		if (geometry != null) { addGeometryType(typeBuilder, geometry); }

		return new SimpleFeatureBuilder(typeBuilder.buildFeatureType());
	}

	/**
	 * Adds the geometry type.
	 *
	 * @param typeBuilder
	 *            the type builder
	 * @param geometry
	 *            the geometry
	 */
	void addGeometryType(final SimpleFeatureTypeBuilder typeBuilder, final Geometry geometry) {
		typeBuilder.add("geometry", geometry != null ? geometry.getClass() : Geometry.class);
		typeBuilder.setDefaultGeometry("geometry");
	}

	/**
	 * Builds the feature.
	 *
	 * @return the simple feature
	 */
	SimpleFeature buildFeature() {

		SimpleFeatureBuilder builder = this.builder != null ? this.builder : createBuilder();
		SimpleFeatureType featureType = builder.getFeatureType();
		SimpleFeature f = builder.buildFeature(getFID());
		if (geometry != null) {
			if (featureType.getGeometryDescriptor() == null) {
				// GEOT-4293, case of geometry coming after properties, we have to retype
				// the builder
				// JD: this is ugly, we should really come up with a better way to store internal
				// state of properties, and avoid creating the builder after the properties object
				// is completed
				SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
				typeBuilder.init(featureType);
				addGeometryType(typeBuilder, geometry);

				featureType = typeBuilder.buildFeatureType();
				SimpleFeatureBuilder newBuilder = new SimpleFeatureBuilder(featureType);
				newBuilder.init(f);
				f = newBuilder.buildFeature(getFID());
			}
			f.setAttribute(featureType.getGeometryDescriptor().getLocalName(), geometry);
		}
		incrementFID();
		return f;
	}
	// "{" +
	// " 'type': 'Feature'," +
	// " 'geometry': {" +
	// " 'type': 'Point'," +
	// " 'coordinates': [" + val + "," + val + "]" +
	// " }, " +
	// "' properties': {" +
	// " 'int': 1," +
	// " 'double': " + (double)val + "," +
	// " 'string': '" + toString(val) + "'" +
	// " }," +
	// " 'id':'widgets." + val + "'" +
	// "}";

	/** Add one to the current ID */
	private void incrementFID() {
		fid = fid + 1;
	}

	/**
	 * Sets the fid.
	 *
	 * @param f
	 *            the new fid
	 */
	private void setFID(final String f) {
		int index = f.lastIndexOf('.');
		if (index < 0) {
			index = f.indexOf('-');
			if (index < 0) {
				autoFID = false;
				id = f;
				return;
			}
			separator = "-";
		} else {
			separator = ".";
		}
		baseId = f.substring(0, index);
		try {
			fid = Integer.parseInt(f.substring(index + 1));
		} catch (NumberFormatException e) {
			autoFID = false;
			id = f;
		}
	}

	/**
	 * Gets the fid.
	 *
	 * @return the fid
	 */
	private String getFID() { return id == null || autoFID ? baseId + separator + fid : id; }
}

/*******************************************************************************************************
 *
 * FeatureTypeHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.feature;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.json.simple.parser.ParseException;

import gama.dependencies.geojson.DelegatingHandler;
import gama.dependencies.geojson.IContentHandler;

/**
 * Obtains a complete feature type from GeoJSON by parsing beyond first feature and finding attributes that did not
 * appear in the first feature or had null values.
 *
 * <p>
 * If null values are encoded, parsing will stop when all data types are found. In the worst case, all features will be
 * parsed. If null values are not encoded, all features will be parsed anyway.
 */
public class FeatureTypeHandler extends DelegatingHandler<SimpleFeatureType>
		implements IContentHandler<SimpleFeatureType> {

	/** The feature type. */
	SimpleFeatureType featureType;

	/** The in features. */
	private boolean inFeatures = false;

	/** The property types. */
	private final Map<String, Class<?>> propertyTypes = new LinkedHashMap<>();

	/** The in properties. */
	private boolean inProperties;

	/** The current prop. */
	private String currentProp;

	/** The crs. */
	private CoordinateReferenceSystem crs;

	/** The null values encoded. */
	private final boolean nullValuesEncoded;

	/** The geom. */
	private GeometryDescriptor geom;

	/**
	 * Instantiates a new feature type handler.
	 *
	 * @param nullValuesEncoded
	 *            the null values encoded
	 */
	public FeatureTypeHandler(final boolean nullValuesEncoded) {
		this.nullValuesEncoded = nullValuesEncoded;
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
		if ("crs".equals(key)) {
			delegate = new CRSHandler();
			return true;
		}
		if ("features".equals(key)) {
			delegate = UNINITIALIZED;
			inFeatures = true;
			return true;
		}
		if (inFeatures && delegate == NULL) {
			if ("properties".equals(key)) {
				inProperties = true;
				return true;
			}
			if (inProperties) {
				if (!propertyTypes.containsKey(key)) {
					// found previously unknown property
					propertyTypes.put(key, Object.class);
				}
				currentProp = key;
				return true;
			}
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

		/*
		 * Use FeatureHandler for the first feature only, to initialize the property list and obtain the geometry
		 * attribute descriptor
		 */
		if (delegate == UNINITIALIZED) {
			delegate = new FeatureHandler(null, new DefaultAttributeIO());
			return true;
		}

		return super.startArray();
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
		super.endObject();

		if (delegate instanceof FeatureHandler) {
			// obtain a type from the first feature
			SimpleFeature feature = ((FeatureHandler) delegate).getValue();
			if (feature != null) {
				geom = feature.getFeatureType().getGeometryDescriptor();
				List<AttributeDescriptor> attributeDescriptors = feature.getFeatureType().getAttributeDescriptors();
				for (AttributeDescriptor ad : attributeDescriptors) {
					if (!ad.equals(geom)) { propertyTypes.put(ad.getLocalName(), ad.getType().getBinding()); }
				}
				delegate = NULL;

				if (foundAllValues()) {
					buildType();
					return false;
				}
			}
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

		if (value != null) {
			Class<?> newType = value.getClass();
			if (currentProp != null) {
				Class<?> knownType = propertyTypes.get(currentProp);
				if (knownType == Object.class) {
					propertyTypes.put(currentProp, newType);

					if (foundAllValues()) {
						// found the last unknown type, stop parsing
						buildType();
						return false;
					}
				} else if (knownType != newType) {
					if (!Number.class.isAssignableFrom(knownType) || newType != Double.class)
						throw new IllegalStateException("Found conflicting types " + knownType.getSimpleName() + " and "
								+ newType.getSimpleName() + " for property " + currentProp);
					propertyTypes.put(currentProp, Double.class);
				}
			}
		}

		return super.primitive(value);
	}

	/**
	 * Found all values.
	 *
	 * @return true, if successful
	 */
	/*
	 * When null values are encoded there's the possibility of stopping the parsing earlier, i.e.: as soon as all data
	 * types and the crs are found.
	 */
	private boolean foundAllValues() {
		return nullValuesEncoded && geom != null && crs != null && !thereAreUnknownDataTypes();
	}

	/**
	 * There are unknown data types.
	 *
	 * @return true, if successful
	 */
	private boolean thereAreUnknownDataTypes() {

		for (Class<?> clazz : propertyTypes.values()) { if (clazz == Object.class) return true; }
		return false;
	}

	/**
	 * End object entry.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean endObjectEntry() throws ParseException, IOException {

		super.endObjectEntry();

		if (delegate instanceof CRSHandler) {
			crs = ((CRSHandler) delegate).getValue();
			if (crs != null) { delegate = NULL; }
		} else if (currentProp != null) {
			currentProp = null;
		} else if (inProperties) { inProperties = false; }
		return true;
	}

	/**
	 * End JSON.
	 *
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void endJSON() throws ParseException, IOException {
		buildType();
	}

	/**
	 * Builds the type.
	 */
	private void buildType() {

		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName("feature");
		typeBuilder.setNamespaceURI("http://geotools.org");

		if (geom != null) { typeBuilder.add(geom.getLocalName(), geom.getType().getBinding(), crs); }

		if (propertyTypes != null) {
			Set<Entry<String, Class<?>>> entrySet = propertyTypes.entrySet();
			for (Entry<String, Class<?>> entry : entrySet) {
				Class<?> binding = entry.getValue();
				if (binding.equals(Object.class)) { binding = String.class; }
				typeBuilder.add(entry.getKey(), binding);
			}
		}

		if (crs != null) { typeBuilder.setCRS(crs); }

		featureType = typeBuilder.buildFeatureType();
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	@Override
	public SimpleFeatureType getValue() { return featureType; }
}

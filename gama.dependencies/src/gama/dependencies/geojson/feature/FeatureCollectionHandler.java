/*******************************************************************************************************
 *
 * FeatureCollectionHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
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

import gama.dependencies.geojson.DelegatingHandler;

/**
 * The Class FeatureCollectionHandler.
 */
public class FeatureCollectionHandler extends DelegatingHandler<SimpleFeature> implements IFeatureCollectionHandler {

	/** The builder. */
	SimpleFeatureBuilder builder;

	/** The attio. */
	AttributeIO attio;

	/** The feature. */
	SimpleFeature feature;

	/** The crs. */
	CoordinateReferenceSystem crs;

	/** The stack. */
	protected List<Integer> stack;

	/**
	 * Instantiates a new feature collection handler.
	 */
	public FeatureCollectionHandler() {
		this(null, null);
	}

	/**
	 * Instantiates a new feature collection handler.
	 *
	 * @param featureType
	 *            the feature type
	 * @param attio
	 *            the attio
	 */
	public FeatureCollectionHandler(final SimpleFeatureType featureType, AttributeIO attio) {
		if (featureType != null) { builder = new SimpleFeatureBuilder(featureType); }

		if (attio == null) {
			if (featureType != null) {
				attio = new FeatureTypeAttributeIO(featureType);
			} else {
				attio = new DefaultAttributeIO();
			}
		}

		this.attio = attio;
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
		if ("features".equals(key)) {
			delegate = UNINITIALIZED;

			return true;
		}
		if ("crs".equals(key)) {
			delegate = new CRSHandler();
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
		if (delegate == UNINITIALIZED) {
			delegate = new FeatureHandler(builder, attio);
			if (crs != null) {
				// build might not be initialized yet, since its build for the first feature, if
				// we have already seen a crs, ensure we set it
				((FeatureHandler) delegate).setCRS(crs);
			}
			// maintain a stack to track when the "features" array ends
			stack = new ArrayList<>();

			return true;
		}

		// are we handling a feature collection? stack is null otherwise
		if (stack != null) { stack.add(null); }
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
		// are we handling a feature collection? stack is null otherwise
		if (stack != null) {
			if (stack.isEmpty()) {
				// end of features array, clear the delegate
				delegate = NULL;
				return true;
			}

			stack.remove(0);
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
		super.endObject();

		if (delegate instanceof FeatureHandler) {
			feature = ((FeatureHandler) delegate).getValue();
			if (feature != null) {
				// check for a null builder, if it is null set it with the feature type
				// from this feature
				if (builder == null) {
					SimpleFeatureType featureType = feature.getFeatureType();
					if (featureType.getCoordinateReferenceSystem() == null && crs != null) {
						// retype with a crs
						featureType = SimpleFeatureTypeBuilder.retype(featureType, crs);
					}
					builder = new SimpleFeatureBuilder(featureType);
				}

				((FeatureHandler) delegate).init();
				// we want to pause at this point
				return false;
			}
		} else if (delegate instanceof CRSHandler) {
			crs = ((CRSHandler) delegate).getValue();
			if (crs != null) { delegate = NULL; }
		}

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
		delegate = null;
		feature = null;
		// crs = null; //JD: keep crs around because we need it post parsing json
	}

	// public boolean hasMoreFeatures() {
	// return delegate != null;
	// }

	/**
	 * Gets the crs.
	 *
	 * @return the crs
	 */
	public CoordinateReferenceSystem getCRS() { return crs; }

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	@Override
	public SimpleFeature getValue() { return feature; }
}

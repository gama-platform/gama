/*******************************************************************************************************
 *
 * FeatureTypeAttributeIO.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.feature;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;

import gama.dependencies.geojson.GeoJSONUtil;

/**
 * The Class FeatureTypeAttributeIO.
 */
public class FeatureTypeAttributeIO implements AttributeIO {

	/** The ios. */
	HashMap<String, AttributeIO> ios = new HashMap<>();

	/**
	 * Instantiates a new feature type attribute IO.
	 *
	 * @param featureType
	 *            the feature type
	 */
	public FeatureTypeAttributeIO(final SimpleFeatureType featureType) {
		for (AttributeDescriptor ad : featureType.getAttributeDescriptors()) {
			AttributeIO io = null;
			if (Date.class.isAssignableFrom(ad.getType().getBinding())) {
				io = new DateAttributeIO();
			} else {
				io = new DefaultAttributeIO();
			}
			ios.put(ad.getLocalName(), io);
		}
	}

	@Override
	public String encode(final String att, final Object value) {
		return ios.get(att).encode(att, value);
	}

	@Override
	public Object parse(final String att, final String value) {
		return ios.get(att).parse(att, value);
	}

	/**
	 * The Class DateAttributeIO.
	 */
	static class DateAttributeIO implements AttributeIO {

		@Override
		public String encode(final String att, final Object value) {
			return GeoJSONUtil.dateFormatter.format((Date) value);
		}

		@Override
		public Object parse(final String att, final String value) {
			try {
				final SimpleDateFormat sdf = new SimpleDateFormat(GeoJSONUtil.DATE_FORMAT);
				return sdf.parse(value);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
	}
}

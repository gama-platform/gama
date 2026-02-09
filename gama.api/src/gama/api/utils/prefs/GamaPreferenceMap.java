/*******************************************************************************************************
 *
 * GamaPreferenceMap.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.prefs;

import java.util.LinkedHashMap;

import gama.api.data.objects.IDate;
import gama.api.data.objects.IPoint;

/**
 *
 */
public class GamaPreferenceMap extends LinkedHashMap<String, Pref> {

	/**
	 * Gets the boolean.
	 *
	 * @param key
	 *            the key
	 * @return the boolean
	 */
	public Boolean getBoolean(final String key) {
		return getBoolean(key, false);
	}

	/**
	 * Gets the boolean.
	 *
	 * @param key
	 *            the key
	 * @return the boolean
	 */
	public Boolean getBoolean(final String key, final Boolean defaultValue) {
		Pref<Boolean> pref = get(key);
		if (pref != null) return pref.getValue();
		return defaultValue;
	}

	/**
	 * Gets the integer.
	 *
	 * @param key
	 *            the key
	 * @return the integer
	 */
	public Integer getInteger(final String key) {
		return getInteger(key, 0);
	}

	/**
	 * Gets the integer.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the integer
	 */
	public Integer getInteger(final String key, final Integer defaultValue) {
		Pref<Integer> pref = get(key);
		if (pref != null) return pref.getValue();
		return defaultValue;
	}

	/**
	 * Gets the double.
	 *
	 * @param key
	 *            the key
	 * @return the double
	 */
	public Double getDouble(final String key) {
		return getDouble(key, 0.0);
	}

	/**
	 * Gets the double.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the double
	 */
	public Double getDouble(final String key, final Double defaultValue) {
		Pref<Double> pref = get(key);
		if (pref != null) return pref.getValue();
		return defaultValue;
	}

	/**
	 * Gets the string.
	 *
	 * @param key
	 *            the key
	 * @return the string
	 */
	public String getString(final String key) {
		return getString(key, "");
	}

	/**
	 * Gets the string.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the string
	 */
	public String getString(final String key, final String defaultValue) {
		Pref<String> pref = get(key);
		if (pref != null) return pref.getValue();
		return defaultValue;
	}

	/**
	 * Gets the date.
	 *
	 * @param key
	 *            the key
	 * @return the date
	 */
	public IDate getDate(final String key) {
		return getDate(key, null);
	}

	/**
	 * Gets the date.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the date
	 */
	public IDate getDate(final String key, final IDate defaultValue) {
		Pref<IDate> pref = get(key);
		if (pref != null) return pref.getValue();
		return defaultValue;
	}

	/**
	 * Gets the point.
	 *
	 * @param key
	 *            the key
	 * @return the point
	 */
	public IPoint getPoint(final String key) {
		return getPoint(key, null);
	}

	/**
	 * Gets the point.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the point
	 */
	public IPoint getPoint(final String key, final IPoint defaultValue) {
		Pref<IPoint> pref = get(key);
		if (pref != null) return pref.getValue();
		return defaultValue;
	}

}

/*******************************************************************************************************
 *
 * GamlEObjectImpl.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.serializer;

import java.util.Map;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import gaml.compiler.gaml.Facet;

/**
 * The Class GamlEObjectImpl.
 */
// 1. Extend the standard Xtext/EMF base class
public class GamlEObjectImpl extends MinimalEObjectImpl.Container {

	/** Cached name computed by EGaml. */
	private String cachedName;

	/** Cached key computed by EGaml. */
	private String cachedKey;

	/** Cached facets map computed by EGaml. */
	private Map<String, Facet> cachedFacetsMap;

	@Override
	public String toString() {
		// 2. Delegate the toString() call to GamlSerializerToString
		return GamlSerializerToString.asString(this);
	}

	/**
	 * As string.
	 *
	 * @return the string
	 */
	public String asString() {
		// 2. Delegate the toString() call to GamlSerializerToString
		return GamlSerializerToString.asString(this);
	}

	/**
	 * Super to string.
	 *
	 * @return the string
	 */
	// Helper method just in case you ever need the original EMF string
	public String superToString() {
		return super.toString();
	}

	/**
	 * Gets the cached name.
	 *
	 * @return the cached name, or null if absent
	 */
	public String getCachedName() { return cachedName; }

	/**
	 * Sets the cached name.
	 *
	 * @param name
	 *            the name to cache
	 */
	public void setCachedName(final String name) { cachedName = name; }

	/**
	 * Gets the cached key.
	 *
	 * @return the cached key, or null if absent
	 */
	public String getCachedKey() { return cachedKey; }

	/**
	 * Sets the cached key.
	 *
	 * @param key
	 *            the key to cache
	 */
	public void setCachedKey(final String key) { cachedKey = key; }

	/**
	 * Gets the cached facets map.
	 *
	 * @return the cached facets map, or null if absent
	 */
	public Map<String, Facet> getCachedFacetsMap() { return cachedFacetsMap; }

	/**
	 * Sets the cached facets map.
	 *
	 * @param map
	 *            the facets map to cache
	 */
	public void setCachedFacetsMap(final Map<String, Facet> map) { cachedFacetsMap = map; }

	/**
	 * Clears all EGaml-computed cached values.
	 */
	public void clearEGamlCache() {
		cachedName = null;
		cachedKey = null;
		cachedFacetsMap = null;
	}
}
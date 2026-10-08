/*******************************************************************************************************
 *
 * GamaGraphMLNodeImporter.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class GamaGraphMLNodeImporter.
 */
/*
 * A custom graph vertex. From jGrapht Example
 */
public class GamaGraphMLNodeImporter {

	/** The id. */
	private String id;

	/** The attributes. */
	private final Map<String, String> attributes;

	/**
	 * Instantiates a new gama graph ML node importer.
	 *
	 * @param id
	 *            the id
	 */
	public GamaGraphMLNodeImporter(final String id) {
		this.id = id;
		this.attributes = new HashMap<>();
	}

	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if ((obj == null) || (getClass() != obj.getClass())) return false;
		GamaGraphMLNodeImporter other = (GamaGraphMLNodeImporter) obj;
		if (id == null) return other.id == null;
		return id.equals(other.id);
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() { return id; }

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the new id
	 */
	public void setId(final String id) { this.id = id; }

	/**
	 * Adds the attribute.
	 *
	 * @param k
	 *            the k
	 * @param v
	 *            the v
	 */
	public void addAttribute(final String k, final String v) {
		attributes.put(k, v);
	}

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() { return attributes; }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		return sb.toString();
	}
}

/*******************************************************************************************************
 *
 * IJsonConstants.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.json;

/**
 * The Interface IJsonConstants.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 nov. 2023
 */
public interface IJsonConstants {

	/**
	 * Represents the JSON literal <code>null</code>.
	 */
	JsonValue NULL = new JsonNull();

	/**
	 * Represents the JSON literal <code>true</code>.
	 */
	JsonValue TRUE = new JsonTrue();

	/**
	 * Represents the JSON literal <code>false</code>.
	 */
	JsonValue FALSE = new JsonFalse();

	/** The nan. */
	JsonValue NAN = new JsonFloat(Double.NaN);

	/** The positive infinity. */
	JsonValue POSITIVE_INFINITY = new JsonFloat(Double.POSITIVE_INFINITY);

	/** The negative infinity. */
	JsonValue NEGATIVE_INFINITY = new JsonFloat(Double.NEGATIVE_INFINITY);

}

/*******************************************************************************************************
 *
 * INamed.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.interfaces;

/**
 * Interface INamed. Represents objects that can be provided with a name.
 *
 * @author A. Drogoul
 * @since 10 nov. 2009
 */
public interface INamed extends IGamlable {

	/** The comparator. */
	java.util.Comparator<? super INamed> COMPARATOR = (a, b) -> a.getName().compareToIgnoreCase(b.getName());

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	default String getName() { return toString(); }

	/**
	 * Sets the name.
	 *
	 * @param newName
	 *            the new name
	 */
	default void setName(final String newName) {}

}

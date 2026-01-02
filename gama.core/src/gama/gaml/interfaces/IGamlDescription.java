/*******************************************************************************************************
 *
 * IGamlDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.interfaces;

import java.util.function.Consumer;

import gama.annotations.precompiler.GamlProperties;
import gama.annotations.precompiler.OkForAPI;

/**
 * The interface IGamlDescription. Represents objects that can be presented in the online documentation.
 *
 * @author drogoul
 * @since 27 avr. 2012
 *
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
/**
 * The Interface IGamlDescription.
 */
public interface IGamlDescription extends INamed {

	/**
	 * Returns the title of this object (ie. the first line in the online documentation)
	 *
	 * @return a string representing the title of this object (default is its name)
	 */
	default String getTitle() { return getName(); }

	/**
	 * Returns the documentation attached to this object. Never null. Default is an empty documentation
	 *
	 * @return a string that represents the documentation of this object
	 */
	default IGamlDocumentation getDocumentation() { return IGamlDocumentation.EMPTY_DOC; }

	/**
	 * Returns the plugin in which this object has been defined (if it has one)
	 *
	 * @return a string containing the identifier of the plugin in which this object has been defined, or null. Default
	 *         is null
	 */
	default String getDefiningPlugin() { return null; }

	/**
	 * Collect meta information.
	 *
	 * @param meta
	 *            the meta
	 */
	default void collectMetaInformation(final GamlProperties meta) {
		meta.put(GamlProperties.PLUGINS, getDefiningPlugin());
	}

	/**
	 * @return
	 */
	default Consumer<IGamlDescription> getContextualAction() { return null; }

}

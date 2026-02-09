/*******************************************************************************************************
 *
 * IActionDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.lang.reflect.AccessibleObject;
import java.util.List;

import gama.api.additions.IGamaHelper;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.symbols.Arguments;

/**
 *
 */
public interface IActionDescription extends IStatementDescription {

	/**
	 * @return
	 */
	List<String> getArgNames();

	/**
	 * @param arg
	 * @return
	 */
	boolean containsArg(String arg);

	/**
	 * @param b
	 * @return
	 */
	IGamlDocumentation getShortDocumentation(boolean b);

	/**
	 * @param callerContext
	 * @param arguments
	 * @return
	 */
	boolean verifyArgs(IDescription callerContext, Arguments arguments);

	/**
	 * Used only by PrimitiveDescription
	 *
	 * @param e
	 * @param method
	 */
	default void setHelper(final IGamaHelper helper, final AccessibleObject method) {}

	/**
	 * @param child
	 */
	IDescription addChild(IDescription child);

	/**
	 * @return
	 */
	default IGamaHelper getHelper() { return null; }

}

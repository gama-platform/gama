/*******************************************************************************************************
 *
 * ICategory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.experiment;

import gama.core.runtime.IScope;

/**
 * The Interface ICategory.
 */
public interface ICategory extends IExperimentDisplayable {

	/**
	 * Checks if is expanded.
	 *
	 * @return true, if is expanded
	 */
	boolean isExpanded(IScope scope);

}

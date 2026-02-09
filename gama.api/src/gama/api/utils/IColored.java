/*******************************************************************************************************
 *
 * IColored.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils;

import java.util.List;


import gama.api.data.objects.IColor;
import gama.api.runtime.scope.IScope;

/**
 * The Interface IColored.
 */

public interface IColored {

	/**
	 * Gets the color.
	 *
	 * @param scope
	 *            the scope
	 * @return the color
	 */
	IColor getColor(IScope scope);

	/**
	 * Gets the colors.
	 *
	 * @param scope
	 *            the scope
	 * @return the colors
	 */
	default List<IColor> getColors(final IScope scope) {
		return List.of(getColor(scope));
	}

}

/*******************************************************************************************************
 *
 * IColored.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import java.util.List;

import gama.core.runtime.IScope;
import gama.core.util.IColor;
import gama.core.util.list.GamaListFactory;
import gama.gaml.types.Types;

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
		return GamaListFactory.wrap(Types.COLOR, getColor(scope));
	}

}

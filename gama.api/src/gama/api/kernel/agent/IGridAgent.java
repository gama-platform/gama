/*******************************************************************************************************
 *
 * IGridAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

import gama.api.data.objects.IColor;
import gama.api.data.objects.IList;
import gama.api.runtime.scope.IScope;

/**
 * The Interface IGridAgent.
 */
public interface IGridAgent extends IAgent {

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	IColor getColor();

	/**
	 * Sets the color.
	 *
	 * @param color
	 *            the new color
	 */
	void setColor(final IColor color);

	/**
	 * Gets the x.
	 *
	 * @return the x
	 */
	int getX();

	/**
	 * Gets the y.
	 *
	 * @return the y
	 */
	int getY();

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	double getValue();

	/**
	 * Gets the bands.
	 *
	 * @return the bands
	 */
	IList<Double> getBands();

	/**
	 * Gets the neighbors.
	 *
	 * @param scope
	 *            the scope
	 * @return the neighbors
	 */
	IList<IAgent> getNeighbors(IScope scope);

	/**
	 * Sets the value.
	 *
	 * @param d
	 *            the new value
	 */
	void setValue(final double d);
}
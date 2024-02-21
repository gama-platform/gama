/*******************************************************************************************************
 *
 * INeighborhood.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.metamodel.topology.grid;

import java.util.Set;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;

/**
 * Class INeighborhood.
 *
 * @author drogoul
 * @since 19 mai 2013
 *
 */
public interface INeighborhood {

	/**
	 * Gets the neighbors in.
	 *
	 * @param scope the scope
	 * @param placeIndex the place index
	 * @param radius the radius
	 * @return the neighbors in
	 */
	public abstract Set<IAgent> getNeighborsIn(IScope scope, final int placeIndex, final int radius);

	/**
	 * Checks if is vn.
	 *
	 * @return true, if is vn
	 */
	public abstract boolean isVN();

	/**
	 * @param placeIndex
	 * @param range
	 * @return
	 */
	public abstract int[] getRawNeighborsIncluding(IScope scope, int placeIndex, int range);

	/**
	 * @param placeIndex
	 * @param n
	 * @return
	 */
	public abstract int neighborsIndexOf(IScope scope, int placeIndex, int n);

	/**
	 *
	 */
	public abstract void clear();

}
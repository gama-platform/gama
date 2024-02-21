package gama.core.metamodel.topology.grid;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.core.util.IList;

/**
 * The Interface IGridAgent.
 */
public interface IGridAgent extends IAgent {

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	public GamaColor getColor();

	/**
	 * Sets the color.
	 *
	 * @param color the new color
	 */
	public void setColor(final GamaColor color);

	/**
	 * Gets the x.
	 *
	 * @return the x
	 */
	public int getX();

	/**
	 * Gets the y.
	 *
	 * @return the y
	 */
	public int getY();

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public double getValue();

	/**
	 * Gets the bands.
	 *
	 * @return the bands
	 */
	public IList<Double> getBands();

	/**
	 * Gets the neighbors.
	 *
	 * @param scope the scope
	 * @return the neighbors
	 */
	public IList<IAgent> getNeighbors(IScope scope);

	/**
	 * Sets the value.
	 *
	 * @param d the new value
	 */
	public void setValue(final double d);
}
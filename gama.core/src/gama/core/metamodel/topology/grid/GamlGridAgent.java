/*******************************************************************************************************
 *
 * GamlGridAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.topology.grid;

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.operators.Cast;
import gama.gaml.types.Types;

/**
 * The Class GamlGridAgent.
 */
public class GamlGridAgent extends GamlAgent implements IGridAgent {

	/**
	 * Instantiates a new gaml grid agent.
	 *
	 * @param index
	 *            the index
	 * @param gridPopulation
	 *            TODO
	 */
	public GamlGridAgent(final GridPopulation population, final int index) {
		super(population, index, population.grid.matrix[index].getGeometry());
	}

	@Override
	public GridPopulation getPopulation() { return (GridPopulation) super.getPopulation(); }

	@Override
	public GamaColor getColor() {
		if (getPopulation().grid.isHexagon) return (GamaColor) getAttribute(IKeyword.COLOR);
		return GamaColor.get(getPopulation().grid.supportImagePixels[getIndex()]);
	}

	@Override
	public void setColor(final GamaColor color) {
		if (getPopulation().grid.isHexagon) {
			setAttribute(IKeyword.COLOR, color);
		} else {
			getPopulation().grid.supportImagePixels[getIndex()] = color.getRGB();
		}
	}

	@Override
	public final int getX() {
		if (getPopulation().grid.isHexagon()) return getPopulation().grid.getX(getGeometry());
		return (int) (getLocation().getX() / getPopulation().grid.cellWidth);
	}

	@Override
	public final int getY() {
		if (getPopulation().grid.isHexagon()) return getPopulation().grid.getY(getGeometry());
		return (int) (getLocation().getY() / getPopulation().grid.cellHeight);
	}

	@Override
	public double getValue() {
		if (getPopulation().grid.gridValue != null) return getPopulation().grid.gridValue[getIndex()];
		return 0d;
	}

	@Override
	public IList<Double> getBands() {
		if (getPopulation().grid.nbBands == 1) {
			final IList<Double> bd = GamaListFactory.create(null, Types.FLOAT);
			bd.add(getValue());
			return bd;
		}
		return getPopulation().grid.bands.get(getIndex());
	}

	@Override
	public void setValue(final double d) {
		if (getPopulation().grid.gridValue != null) { getPopulation().grid.gridValue[getIndex()] = d; }
	}

	@Override
	public IList<IAgent> getNeighbors(final IScope scope) {
		return Cast.asList(scope, getPopulation().grid.getNeighborhood().getNeighborsIn(scope, getIndex(), 1));
	}

}
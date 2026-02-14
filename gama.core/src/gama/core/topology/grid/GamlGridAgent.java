/*******************************************************************************************************
 *
 * GamlGridAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.topology.grid;

import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaColorFactory;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IList;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IGridAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.color.GamaColor;
import gama.core.agent.GamlAgent;

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
		super(population, index, population.getGrid().matrix[index].getGeometry());
	}

	@Override
	public GridPopulation getPopulation() { return (GridPopulation) super.getPopulation(); }

	@Override
	public IColor getColor() {
		if (getPopulation().getGrid().isHexagon) return (GamaColor) getAttribute(IKeyword.COLOR);
		return GamaColorFactory.get(getPopulation().getGrid().supportImagePixels[getIndex()]);
	}

	@Override
	public void setColor(final IColor color) {
		if (getPopulation().getGrid().isHexagon) {
			setAttribute(IKeyword.COLOR, color);
		} else {
			getPopulation().getGrid().supportImagePixels[getIndex()] = color.getRGB();
		}
	}

	@Override
	public final int getX() {
		if (getPopulation().getGrid().isHexagon()) return getPopulation().getGrid().getX(getGeometry());
		return (int) (getLocation().getX() / getPopulation().getGrid().cellWidth);
	}

	@Override
	public final int getY() {
		if (getPopulation().getGrid().isHexagon()) return getPopulation().getGrid().getY(getGeometry());
		return (int) (getLocation().getY() / getPopulation().getGrid().cellHeight);
	}

	@Override
	public double getValue() {
		if (getPopulation().getGrid().gridValue != null) return getPopulation().getGrid().gridValue[getIndex()];
		return 0d;
	}

	@Override
	public IList<Double> getBands() {
		if (getPopulation().getGrid().nbBands == 1) {
			final IList<Double> bd = GamaListFactory.create(null, Types.FLOAT);
			bd.add(getValue());
			return bd;
		}
		return getPopulation().getGrid().bands.get(getIndex());
	}

	@Override
	public void setValue(final double d) {
		if (getPopulation().getGrid().gridValue != null) { getPopulation().getGrid().gridValue[getIndex()] = d; }
	}

	@Override
	public IList<IAgent> getNeighbors(final IScope scope) {
		return GamaListFactory.toList(scope,
				getPopulation().getGrid().getNeighborhood().getNeighborsIn(scope, getIndex(), 1));
	}

}
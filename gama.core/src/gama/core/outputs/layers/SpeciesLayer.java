/*******************************************************************************************************
 *
 * SpeciesLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.geom.Rectangle2D;

import gama.api.data.objects.IList;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.layers.ILayerStatement;
import gama.api.utils.list.GamaListFactory;
import one.util.streamex.StreamEx;

/**
 * Written by drogoul Modified on 23 août 2008
 */

public class SpeciesLayer extends AgentLayer {

	/** The has micro species layers. */
	final boolean hasMicroSpeciesLayers;

	/**
	 * Instantiates a new species layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public SpeciesLayer(final ILayerStatement layer) {
		super(layer);
		hasMicroSpeciesLayers = getDefinition().getMicroSpeciesLayers() != null;
	}

	@Override
	public SpeciesLayerStatement getDefinition() { return (SpeciesLayerStatement) super.getDefinition(); }

	@Override
	public IList<? extends IAgent> getAgentsForMenu(final IScope scope) {
		return GamaListFactory.createWithoutCasting(Types.AGENT, getDefinition().getSpecies().getPopulation(scope));
		// return
		// ImmutableSet.copyOf(scope.getSimulation().getMicroPopulation(getDefinition().getSpecies()).iterator());
	}

	@Override
	public String getType() { return "Species layer"; }

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics g) throws GamaRuntimeException {
		shapes.clear();
		final ISpecies species = getDefinition().getSpecies();
		final IMacroAgent world = scope.getSimulation();
		if (world != null && !world.dead()) {
			final IPopulation<? extends IAgent> microPop = world.getMicroPopulation(species);
			if (microPop != null) {
				IExecutable aspect = getDefinition().getAspect();
				if (aspect == null) { aspect = DEFAULT_ASPECT; }
				drawPopulation(scope, g, aspect, microPop);
			}
		}
	}

	/**
	 * Draw population.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param aspect
	 *            the aspect
	 * @param population
	 *            the population
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private void drawPopulation(final IScope scope, final IGraphics g, final IExecutable aspect,
			final IPopulation<? extends IAgent> population) throws GamaRuntimeException {

		// draw the population. A copy of the population is made to avoid
		// concurrent modification exceptions
		StreamEx<? extends IAgent> stream = population.stream(scope);
		// if (this.getData().getRefresh()) { stream = stream.parallel(); }
		stream.nonNull().filter(a -> !a.dead()).forEach(a -> {
			IExecutionResult result = null;
			if (a == scope.getGui().getHighlightedAgent()) {
				IExecutable hAspect = population.getSpecies().getAspect("highlighted");
				if (hAspect == null) { hAspect = aspect; }
				result = scope.execute(hAspect, a, null);
			} else {
				result = scope.execute(aspect, a, null);
			}
			if (result != IExecutionResult.FAILED) {
				if (result != null && result.getValue() instanceof Rectangle2D) {
					final Rectangle2D r = (Rectangle2D) result.getValue();
					shapes.put(a, r);
				}
				if (a instanceof IMacroAgent) {
					IPopulation<? extends IAgent> microPop;
					// then recursively draw the micro-populations
					if (hasMicroSpeciesLayers) {
						for (final SpeciesLayerStatement ml : getDefinition().getMicroSpeciesLayers()) {
							if (a.dead()) { continue; }
							microPop = ((IMacroAgent) a).getMicroPopulation(ml.getSpecies());
							if (microPop != null && microPop.size() > 0) {
								IExecutable microAspect = ml.getAspect();
								if (microAspect == null) { microAspect = DEFAULT_ASPECT; }
								drawPopulation(scope, g, microAspect, microPop);
							}
						}
					}
				}
			}
		});

	}

}

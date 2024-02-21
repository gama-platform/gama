/*******************************************************************************************************
 *
 * AgentLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Set;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IGraphics;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.ExecutionResult;
import gama.core.runtime.IScope;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.Collector;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.AspectStatement;
import gama.gaml.statements.IExecutable;
import gama.gaml.types.Types;

/**
 * Written by drogoul Modified on 23 août 2008
 *
 * @todo Description
 *
 */
public class AgentLayer extends AbstractLayer {

	/**
	 * Instantiates a new agent layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public AgentLayer(final ILayerStatement layer) {
		super(layer);
	}

	/** The shapes. */
	protected final IMap<IAgent, Rectangle2D> shapes = GamaMapFactory.createUnordered();

	/** The Constant DUMMY_RECT. */
	protected static final Rectangle2D DUMMY_RECT = new Rectangle2D.Double();

	/**
	 * Fill shapes.
	 *
	 * @param scope
	 *            the scope
	 */
	@SuppressWarnings ("unchecked")
	protected void fillShapes(final IScope scope) {
		shapes.clear();
		final Object o = ((AgentLayerStatement) definition).getAgentsExpr().value(scope);
		Iterable<? extends IAgent> agents = Collections.EMPTY_LIST;
		if (o instanceof ISpecies) {
			agents = ((ISpecies) o).iterable(scope);
		} else if (o instanceof IList) { agents = (IList) o; }
		for (final IAgent a : agents) { shapes.put(a, DUMMY_RECT); }
	}

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics g) throws GamaRuntimeException {
		if (scope == null || scope.interrupted()) return;
		fillShapes(scope);
		final String aspectName = ((AgentLayerStatement) definition).getAspectName();

		shapes.entrySet().forEach(entry -> {
			final IAgent a = entry.getKey();
			IExecutable aspect = null;
			if (a != null) {
				if (a == scope.getGui().getHighlightedAgent()) {
					aspect = a.getSpecies().getAspect("highlighted");
				} else {
					aspect = ((AgentLayerStatement) definition).getAspect();
					if (aspect == null) { aspect = a.getSpecies().getAspect(aspectName); }
				}
				if (aspect == null) { aspect = AspectStatement.DEFAULT_ASPECT; }

				final ExecutionResult result = scope.execute(aspect, a, null);
				final Object r = result.getValue();
				if (r instanceof Rectangle2D) { entry.setValue((Rectangle2D) r); }
			}
		});

	}

	@Override
	public IList<? extends IAgent> getAgentsForMenu(final IScope scope) {
		// if (shapes.isEmpty()) { return getAgentsToDisplay(); }
		// Avoid recalculating the agents
		return GamaListFactory.wrap(Types.AGENT, shapes.keySet());
	}

	// public Collection<IAgent> getAgentsToDisplay() {
	// return ((AgentLayerStatement) definition).getAgentsToDisplay();
	// }

	@Override
	public Set<IAgent> collectAgentsAt(final int x, final int y, final IDisplaySurface g) {
		try (final Collector.AsOrderedSet<IAgent> selectedAgents = Collector.getOrderedSet()) {
			final Rectangle2D selection = new Rectangle2D.Double();
			selection.setFrameFromCenter(x, y, x + IDisplaySurface.SELECTION_SIZE / 2,
					y + IDisplaySurface.SELECTION_SIZE / 2);
			shapes.forEachPair((a, b) -> {
				if (b.intersects(selection)) { selectedAgents.add(a); }
				return true;
			});

			return selectedAgents.items();
		}
	}

	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		if (geometry instanceof IAgent) {
			final Rectangle2D r = shapes.get(geometry);
			if (r != null) return r;
		}
		return super.focusOn(geometry, s);
	}

	@Override
	public String getType() { return "Agents layer"; }

}

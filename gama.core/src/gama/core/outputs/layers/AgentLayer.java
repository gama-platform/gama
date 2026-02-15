/*******************************************************************************************************
 *
 * AgentLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.ILayerStatement;
import gama.api.utils.collections.Collector;
import gama.api.utils.color.GamaColorFactory;
import gama.api.utils.list.GamaListFactory;
import gama.api.utils.prefs.GamaPreferences;
import gama.gaml.statements.draw.ShapeDrawingAttributes;
import one.util.streamex.StreamEx;

/**
 * Written by drogoul Modified on 23 août 2008
 *
 * @todo Description
 *
 */
public class AgentLayer extends AbstractLayer {

	/** The Constant SHAPES. */
	static final Map<String, Integer> SHAPES = new HashMap<>() {

		{
			put("circle", 1);
			put("square", 2);
			put("triangle", 3);
			put("sphere", 4);
			put("cube", 5);
			put("point", 6);
		}
	};

	/** The border color. */
	public static final IColor borderColor = GamaColorFactory.BLACK;

	/** The default aspect. */
	public static final IExecutable DEFAULT_ASPECT = sc -> {
		if (!sc.isGraphics()) return null;
		IGraphicsScope scope = (IGraphicsScope) sc;
		final IAgent agent = scope.getAgent();
		if (agent != null && !agent.dead()) {
			final IGraphics g = scope.getGraphics();
			if (g == null) return null;
			try {
				if (agent == scope.getGui().getHighlightedAgent()) { g.beginHighlight(); }
				final boolean hasColor = agent.getSpecies().hasVar(IKeyword.COLOR);
				IColor color;
				if (hasColor) {
					final Object value = agent.getDirectVarValue(scope, IKeyword.COLOR);
					color = GamaColorFactory.createFrom(scope, value);
				} else {
					color = GamaColorFactory.get(GamaPreferences.Displays.CORE_COLOR.getValue().getRGB());
				}
				final String defaultShape = GamaPreferences.Displays.CORE_SHAPE.getValue();
				final Integer index = SHAPES.get(defaultShape);
				IShape ag;

				if (index != null) {
					final Double defaultSize = GamaPreferences.Displays.CORE_SIZE.getValue();
					final IPoint point = agent.getLocation();

					ag = switch (SHAPES.get(defaultShape)) {
						case 1 -> GamaShapeFactory.buildCircle(defaultSize, point);
						case 2 -> GamaShapeFactory.buildSquare(defaultSize, point);
						case 3 -> GamaShapeFactory.buildTriangle(defaultSize, point);
						case 4 -> GamaShapeFactory.buildSphere(defaultSize, point);
						case 5 -> GamaShapeFactory.buildCube(defaultSize, point);
						case 6 -> GamaShapeFactory.buildPoint(point);
						default -> agent.getGeometry();
					};
				} else {
					ag = agent.getGeometry();
				}

				final IShape ag2 = ag.copy(scope);
				final IDrawingAttributes attributes = new ShapeDrawingAttributes(ag2, agent, color, borderColor);
				return g.drawShape(ag2.getInnerGeometry(), attributes);
			} catch (final GamaRuntimeException e) {
				// cf. Issue 1052: exceptions are not thrown, just displayed
				e.printStackTrace();
			} finally {
				g.endHighlight();
			}
		}
		return null;
	};

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

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics g) throws GamaRuntimeException {
		if (scope == null || scope.interrupted()) return;
		shapes.clear();
		AgentLayerStatement def = (AgentLayerStatement) definition;
		final Object o = def.getAgentsExpr().value(scope);
		IContainer<?, ? extends IAgent> agents = o instanceof ISpecies is ? is : o instanceof IList il ? il : null;
		if (agents == null) return;
		final String aspectName = def.getAspectName();
		StreamEx<? extends IAgent> stream = agents.stream(scope);
		// if (this.getData().getRefresh()) { stream = stream.parallel(); }
		stream.nonNull().forEach(a -> {
			IExecutable aspect = null;
			if (a == scope.getGui().getHighlightedAgent()) {
				aspect = a.getSpecies().getAspect("highlighted");
			} else {
				aspect = def.getAspect();
				if (aspect == null) { aspect = a.getSpecies().getAspect(aspectName); }
			}
			if (aspect == null) { aspect = DEFAULT_ASPECT; }
			final IExecutionResult result = scope.execute(aspect, a, null);
			final Object r = result.getValue();
			if (r instanceof Rectangle2D r2d) { shapes.put(a, r2d); }
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

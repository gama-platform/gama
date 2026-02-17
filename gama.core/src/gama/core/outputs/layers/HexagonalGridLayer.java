/*******************************************************************************************************
 *
 * HexagonalGridLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.geom.Rectangle2D;

import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.IShape;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.layers.ILayerStatement;
import gama.gaml.statements.draw.DrawingAttributes;
import gama.gaml.statements.draw.ShapeDrawingAttributes;

/**
 * The Class HexagonalGridLayer.
 */
public class HexagonalGridLayer extends AgentLayer {

	/**
	 * Instantiates a new grid agent layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public HexagonalGridLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public GridLayerData createData() {
		return new GridLayerData(definition);
	}

	@Override
	public GridLayerData getData() { return (GridLayerData) super.getData(); }

	@Override
	public void privateDraw(final IGraphicsScope s, final IGraphics gr) throws GamaRuntimeException {
		final IColor borderColor = getData().drawLines() ? getData().getLineColor() : null;
		final IExecutable aspect = scope -> {
			IGraphicsScope sc = (IGraphicsScope) scope;
			final IAgent agent = sc.getAgent();
			final IGraphics g = sc.getGraphics();
			try {
				if (agent == sc.getGui().getHighlightedAgent()) { g.beginHighlight(); }
				final IColor color = GamaColorFactory.castToColor(sc, agent.getDirectVarValue(sc, IKeyword.COLOR));
				final IShape ag = agent.getGeometry();
				final IShape ag2 = ag.copy(sc);
				final DrawingAttributes attributes = new ShapeDrawingAttributes(ag2, agent, color, borderColor);
				return g.drawShape(ag2.getInnerGeometry(), attributes);
			} catch (final GamaRuntimeException e) {
				// cf. Issue 1052: exceptions are not thrown, just displayed
				e.printStackTrace();
			} finally {
				g.endHighlight();
			}
			return null;
		};

		/*
		 * 20/2/2014 - PT: change getData().getAgentsToDisplay() by getData().getGrid().getAgents() + as
		 * getData().getAgentsToDisplay() returns a list of null elements for hexagonal grid (casting issue) + It can be
		 * optimized.
		 */
		getData().getGrid().stream(s).nonNull().forEach(a -> {
			final IExecutionResult result = s.execute(aspect, (IAgent) a, null);
			final Object r = result.getValue();
			if (r instanceof Rectangle2D r2d) { shapes.put((IAgent) a, r2d); }
		});

	}

	@Override
	public String getType() { return "Grid layer"; }
}

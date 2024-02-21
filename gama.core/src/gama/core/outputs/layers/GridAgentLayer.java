/*******************************************************************************************************
 *
 * GridAgentLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.geom.Rectangle2D;

import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.ILayer.IGridLayer;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.ExecutionResult;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.gaml.operators.Cast;
import gama.gaml.statements.IExecutable;
import gama.gaml.statements.draw.DrawingAttributes;
import gama.gaml.statements.draw.ShapeDrawingAttributes;

/**
 * The Class GridAgentLayer.
 */
public class GridAgentLayer extends AgentLayer implements IGridLayer {

	/**
	 * Instantiates a new grid agent layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public GridAgentLayer(final ILayerStatement layer) {
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
		final GamaColor borderColor = getData().drawLines() ? getData().getLineColor() : null;
		final IExecutable aspect = scope -> {
			IGraphicsScope sc = (IGraphicsScope) scope;
			final IAgent agent = sc.getAgent();
			final IGraphics g = sc.getGraphics();
			try {
				if (agent == sc.getGui().getHighlightedAgent()) { g.beginHighlight(); }
				final GamaColor color = Cast.asColor(sc, agent.getDirectVarValue(sc, IKeyword.COLOR));
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

		/*20/2/2014 - PT: change getData().getAgentsToDisplay() by getData().getGrid().getAgents()
		+           as getData().getAgentsToDisplay() returns a list of null elements for hexagonal grid (casting issue)
		+           It can be optimized.
		*/
		for (final IAgent a : getData().getGrid().getAgents()){
			if (a != null) {
				final ExecutionResult result = s.execute(aspect, a, null);
				final Object r = result.getValue();
				if (r instanceof Rectangle2D) { shapes.put(a, (Rectangle2D) r); }
			}
		}

	}

	@Override
	public String getType() { return "Grid layer"; }
}

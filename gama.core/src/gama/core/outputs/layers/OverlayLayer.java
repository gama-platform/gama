/*******************************************************************************************************
 *
 * OverlayLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.geom.Rectangle2D;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;

/**
 * Class OverlayLayer.
 *
 * @author drogoul
 * @since 23 févr. 2016
 *
 */
public class OverlayLayer extends GraphicLayer {

	/**
	 * Instantiates a new overlay layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public OverlayLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public boolean isOverlay() { return true; }

	@Override
	protected OverlayLayerData createData() {
		return new OverlayLayerData(definition);
	}

	@Override
	public OverlayLayerData getData() { return (OverlayLayerData) super.getData(); }

	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		return null;
	}

	@Override
	public String getType() { return IKeyword.OVERLAY; }

	@Override
	protected void privateDraw(final IGraphicsScope scope, final IGraphics g) throws GamaRuntimeException {
		g.beginOverlay(this);
		final IAgent agent = scope.getAgent();
		scope.execute(((OverlayStatement) definition).getAspect(), agent, null);
		g.endOverlay();
	}

	@Override
	public boolean isProvidingCoordinates() {
		return false; // by default
	}

	@Override
	public boolean stayProportional() {
		return false;
	}

	@Override
	public boolean isProvidingWorldCoordinates() {
		return false; // by default
	}

}

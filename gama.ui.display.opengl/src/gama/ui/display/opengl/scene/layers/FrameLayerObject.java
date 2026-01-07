/*******************************************************************************************************
 *
 * FrameLayerObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.layers;

import java.util.List;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPointFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.util.GamaColorFactory;
import gama.core.util.IColor;
import gama.gaml.statements.draw.DrawingAttributes;
import gama.gaml.statements.draw.ShapeDrawingAttributes;
import gama.gaml.types.GamaGeometryType;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;
import gama.ui.display.opengl.scene.AbstractObject;
import gama.ui.display.opengl.scene.geometry.GeometryObject;

/**
 * The Class FrameLayerObject.
 */
public class FrameLayerObject extends StaticLayerObject.World {

	/** The Constant FRAME. */
	private static final IColor FRAME = GamaColorFactory.get(150, 150, 150, 255);

	/**
	 * Instantiates a new frame layer object.
	 *
	 * @param renderer
	 *            the renderer
	 */
	public FrameLayerObject(final IOpenGLRenderer renderer) {
		super(renderer);
	}

	@Override
	public void fillWithObjects(final List<AbstractObject<?, ?>> list) {
		final double w = renderer.getData().getEnvWidth();
		final double h = renderer.getData().getEnvHeight();
		final IShape g = GamaGeometryType.buildRectangle(w, h, GamaPointFactory.create(w / 2, h / 2));
		final DrawingAttributes drawingAttr = new ShapeDrawingAttributes(g, (IAgent) null, null, FRAME);
		// drawingAttr.setLighting(false);
		final GeometryObject geomObj = new GeometryObject(g.getInnerGeometry(), drawingAttr);
		list.add(geomObj);
	}

	/**
	 * Compute rotation.
	 *
	 * @param trace
	 *            the trace
	 */
	@Override
	public void computeRotation(final Trace trace) {}
}
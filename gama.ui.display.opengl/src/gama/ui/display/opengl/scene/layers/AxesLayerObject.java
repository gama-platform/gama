/*******************************************************************************************************
 *
 * AxesLayerObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.layers;

import static gama.core.common.geometry.Rotation3D.MINUS_I;
import static gama.core.common.geometry.Rotation3D.PLUS_J;
import static gama.core.common.geometry.Scaling3D.of;
import static gama.gaml.constants.GamlCoreConstants.bottom_center;
import static gama.gaml.constants.GamlCoreConstants.left_center;
import static gama.gaml.constants.GamlCoreConstants.top_center;
import static gama.gaml.types.GamaGeometryType.buildCone3D;
import static gama.gaml.types.GamaGeometryType.buildLineCylinder;

import java.util.List;

import gama.core.common.geometry.AxisAngle;
import gama.core.common.preferences.GamaPreferences;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.util.GamaColor;
import gama.core.util.GamaFont;
import gama.gaml.statements.draw.DrawingAttributes;
import gama.gaml.statements.draw.ShapeDrawingAttributes;
import gama.gaml.statements.draw.TextDrawingAttributes;
import gama.ui.display.opengl.OpenGL;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;
import gama.ui.display.opengl.scene.AbstractObject;
import gama.ui.display.opengl.scene.geometry.GeometryObject;
import gama.ui.display.opengl.scene.text.StringObject;

/**
 * The Class AxesLayerObject.
 */
public class AxesLayerObject extends StaticLayerObject.World {

	/** The Constant LABELS. */
	public final static String[] LABELS = { "X", "Y", "Z" };

	/** The Constant ANCHORS. */
	public final static GamaPoint[] ANCHORS = { left_center, top_center, bottom_center };

	/** The Constant ROTATIONS. */
	public final static AxisAngle[] ROTATIONS = { new AxisAngle(PLUS_J, 90), new AxisAngle(MINUS_I, 90), null };

	/** The Constant COLORS. */
	public final static GamaColor[] COLORS =
			{ GamaColor.get("gamared"), GamaColor.get("gamaorange"), GamaColor.get("gamablue") };

	/** The Constant DEFAULT_SCALE. */
	protected final static GamaPoint DEFAULT_SCALE = new GamaPoint(.15, .15, .15);

	/** The Constant ORIGIN. */
	protected final static GamaPoint ORIGIN = new GamaPoint(0, 0, 0);

	/** The Constant AXES_FONT. */
	protected final static GamaFont AXES_FONT = new GamaFont("Helvetica", 0, 18);

	/** The arrow. */
	final GamaShape arrow;

	/** The dirs. */
	final GamaPoint[] dirs;

	/** The axes. */
	final GamaShape[] axes = new GamaShape[3];

	/**
	 * Instantiates a new axes layer object.
	 *
	 * @param renderer
	 *            the renderer
	 */
	public AxesLayerObject(final IOpenGLRenderer renderer) {
		super(renderer);
		// Addition to fix #2227
		currentList.scale.setLocation(DEFAULT_SCALE);
		final double max = renderer.getMaxEnvDim();
		arrow = (GamaShape) buildCone3D(max / 20, max / 8, ORIGIN);
		dirs = new GamaPoint[] { new GamaPoint(max / 2, 0, 0), new GamaPoint(0, max / 2, 0),
				new GamaPoint(0, 0, max / 2) };
		for (int i = 0; i < 3; i++) { axes[i] = (GamaShape) buildLineCylinder(ORIGIN, dirs[i], max / 60); }
	}

	@Override
	public void setScale(final GamaPoint s) {
		if (s == null) {
			currentList.scale.setLocation(DEFAULT_SCALE);
		} else {
			super.setScale(s);
		}
	}

	@Override
	public void draw(final OpenGL gl) {
		boolean previous = gl.setObjectWireframe(false);
		if (gl.isInRotationMode()) {
			final GamaPoint pivotPoint = renderer.getCameraTarget();
			setOffset(pivotPoint.yNegated());
			final double size = gl.sizeOfRotationElements();
			final double ratio = size / gl.getMaxEnvDim();
			setScale(new GamaPoint(ratio, ratio, ratio));
		} else {
			setOffset(null);
			setScale(null);
		}
		super.draw(gl);
		gl.setObjectWireframe(previous);
	}

	@Override
	public void fillWithObjects(final List<AbstractObject<?, ?>> list) {
		for (int i = 0; i < 3; i++) {
			final GamaPoint p = dirs[i];
			// build axis
			addSyntheticObject(list, axes[i], COLORS[i], IShape.Type.LINECYLINDER);
			// build labels
			final TextDrawingAttributes text =
					new TextDrawingAttributes(of(1), null, p.times(1.3).yNegated(), COLORS[i]);
			text.setAnchor(ANCHORS[i]);
			text.setFont(AXES_FONT);
			text.setPerspective(false);
			list.add(new StringObject(LABELS[i], text));
			// build arrows
			final GamaShape s =
					GamaShapeFactory.createFrom(arrow).withRotation(ROTATIONS[i]).withLocation(p.times(0.98));
			addSyntheticObject(list, s, COLORS[i], IShape.Type.CONE);
		}
	}

	/**
	 * Adds the synthetic object.
	 *
	 * @param list
	 *            the list
	 * @param shape
	 *            the shape
	 * @param color
	 *            the color
	 * @param type
	 *            the type
	 * @param empty
	 *            the empty
	 */
	protected void addSyntheticObject(final List<AbstractObject<?, ?>> list, final IShape shape, final GamaColor color,
			final IShape.Type type) {
		final DrawingAttributes att = new ShapeDrawingAttributes(shape, (IAgent) null, color, color, type,
				GamaPreferences.Displays.CORE_LINE_WIDTH.getValue(), null);
		att.setEmpty(false);
		att.setHeight(shape.getDepth());
		att.setLighting(false);
		list.add(new GeometryObject(shape.getInnerGeometry(), att));
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
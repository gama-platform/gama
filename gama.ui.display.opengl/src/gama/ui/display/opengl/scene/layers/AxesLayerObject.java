/*******************************************************************************************************
 *
 * AxesLayerObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.layers;

import static gama.api.gaml.constants.GamlCoreConstants.bottom_center;
import static gama.api.gaml.constants.GamlCoreConstants.left_center;
import static gama.api.gaml.constants.GamlCoreConstants.top_center;
import static gama.api.utils.geometry.Rotation3D.MINUS_I;
import static gama.api.utils.geometry.Rotation3D.PLUS_J;
import static gama.api.utils.geometry.Scaling3D.of;

import java.util.List;

import gama.api.kernel.agent.IAgent;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.font.GamaFontFactory;
import gama.api.types.font.IFont;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.prefs.GamaPreferences;
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
	public final static IPoint[] ANCHORS = { left_center, top_center, bottom_center };

	/** The Constant ROTATIONS. */
	public final static AxisAngle[] ROTATIONS = { new AxisAngle(PLUS_J, 90), new AxisAngle(MINUS_I, 90), null };

	/** The Constant NAME_REGISTRY. */
	public final static IColor[] COLORS =
			{ GamaColorFactory.get("gamared"), GamaColorFactory.get("gamaorange"), GamaColorFactory.get("gamablue") };

	/** The Constant DEFAULT_SCALE. */
	protected final static IPoint DEFAULT_SCALE = GamaPointFactory.create(.15, .15, .15);

	/** The Constant ORIGIN. */
	protected final static IPoint ORIGIN = GamaPointFactory.create(0, 0, 0);

	/** The Constant AXES_FONT. */
	protected final static IFont AXES_FONT = GamaFontFactory.createFont("Helvetica", 0, 18);

	/** The arrow. */
	final IShape arrow;

	/** The dirs. */
	final IPoint[] dirs;

	/** The axes. */
	final IShape[] axes = new IShape[3];

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
		arrow = GamaShapeFactory.buildCone3D(max / 20, max / 8, ORIGIN);
		dirs = new IPoint[] { GamaPointFactory.create(max / 2, 0, 0), GamaPointFactory.create(0, max / 2, 0),
				GamaPointFactory.create(0, 0, max / 2) };
		for (int i = 0; i < 3; i++) { axes[i] = GamaShapeFactory.buildLineCylinder(ORIGIN, dirs[i], max / 60); }
	}

	@Override
	public void setScale(final IPoint s) {
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
			final IPoint pivotPoint = renderer.getCameraTarget();
			setOffset(pivotPoint.yNegated());
			final double size = gl.sizeOfRotationElements();
			final double ratio = size / gl.getMaxEnvDim();
			setScale(GamaPointFactory.create(ratio, ratio, ratio));
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
			final IPoint p = dirs[i];
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
			final IShape s = GamaShapeFactory.createFrom(arrow).withRotation(ROTATIONS[i]).withLocation(p.times(0.98));
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
	protected void addSyntheticObject(final List<AbstractObject<?, ?>> list, final IShape shape, final IColor color,
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
/*******************************************************************************************************
 *
 * LayerObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.layers;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;

import gama.annotations.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.matrix.IField;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.ILayer;
import gama.api.ui.layers.ILayerData;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.Rotation3D;
import gama.api.utils.geometry.Scaling3D;
import gama.core.outputs.layers.FramedLayerData;
import gama.core.util.file.GamaGeometryFile;
import gama.ui.display.opengl.OpenGL;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;
import gama.ui.display.opengl.scene.AbstractObject;
import gama.ui.display.opengl.scene.geometry.GeometryObject;
import gama.ui.display.opengl.scene.mesh.MeshObject;
import gama.ui.display.opengl.scene.resources.ResourceObject;
import gama.ui.display.opengl.scene.text.StringObject;

/**
 * Class LayerObject.
 *
 * @author drogoul
 * @since 3 mars 2014
 *
 */
@SuppressWarnings ({ "rawtypes" })
public class LayerObject {

	/** The Constant NULL_OFFSET. */
	final static IPoint NULL_OFFSET = GamaPointFactory.createImmutable(0, 0, 0);

	/** The Constant NULL_SCALE. */
	final static IPoint NULL_SCALE = GamaPointFactory.createImmutable(1, 1, 1);

	/** The Constant NULL_ROTATION. */
	final static AxisAngle NULL_ROTATION = new AxisAngle(0d);

	/**
	 * The Class Trace.
	 */
	class Trace extends ArrayList<AbstractObject<?, ?>> {
		/** The offset. */
		final IPoint offset = GamaPointFactory.create(NULL_OFFSET);
		/** The scale. */
		final IPoint scale = GamaPointFactory.create(NULL_SCALE);

		/** The rotation. */
		AxisAngle rotation = NULL_ROTATION;

		/**
		 * Instantiates a new trace.
		 */
		Trace() {
			computeOffset();
			computeScale();
			computeRotation();
		}

		/**
		 * Compute scale.
		 *
		 * @return the gama point
		 */
		public void computeScale() {
			LayerObject.this.computeScale(this);
		}

		/**
		 * Compute offset.
		 *
		 * @return the gama point
		 */
		public void computeOffset() {
			LayerObject.this.computeOffset(this);
		}

		/**
		 * Compute rotation.
		 */
		public void computeRotation() {
			LayerObject.this.computeRotation(this);
		}

		/**
		 * Gets the offset.
		 *
		 * @return the offset
		 */
		public IPoint getOffset() { return offset; }

		/**
		 * Gets the scale.
		 *
		 * @return the scale
		 */
		public IPoint getScale() { return scale; }

		/**
		 * Gets the rotation.
		 *
		 * @return the rotation
		 */
		public AxisAngle getRotation() { return rotation; }

		/**
		 * As array.
		 *
		 * @return the abstract object[]
		 */
		/** Cached backing array — reallocated only when the list size changes. */
		private AbstractObject[] cachedArray = new AbstractObject[0];

		public AbstractObject[] asArray() {
			final int n = size();
			if (cachedArray.length != n) { cachedArray = toArray(new AbstractObject[n]); }
			return cachedArray;
		}
	}

	/** The alpha. */
	protected volatile Double alpha = 1d;

	/** The layer. */
	public final ILayer layer;

	/** The is animated. */
	volatile boolean isAnimated;

	/** The renderer. */
	protected final IOpenGLRenderer renderer;

	/** The current list. */
	protected Trace currentList;

	/** The open GL list index. */
	protected volatile Integer openGLListIndex;

	/** The is fading. */
	protected volatile boolean isFading;

	/**
	 * Instantiates a new layer object.
	 *
	 * @param renderer2
	 *            the renderer 2
	 * @param layer
	 *            the layer
	 */
	public LayerObject(final IOpenGLRenderer renderer2, final ILayer layer) {
		this.renderer = renderer2;
		this.layer = layer;
		currentList = new Trace();
	}

	/**
	 * Compute rotation.
	 *
	 * @param trace
	 *            the trace
	 */
	public void computeRotation(final Trace trace) {
		AxisAngle oldRotation = trace.rotation;
		// if vectors are different... (not computed for the moment)
		// if angles are different
		Double newAngleInDeg = layer.getData().getRotation();
		if (newAngleInDeg.equals(oldRotation.getAngle())) return;
		trace.rotation = new AxisAngle(Rotation3D.PLUS_K, newAngleInDeg);
	}

	/**
	 * Compute scale.
	 */
	public void computeScale(final Trace list) {
		double zScale = layer.getData().getSize().getZ();
		if (zScale <= 0) { zScale = 1; }
		list.scale.setLocation(renderer.getLayerWidth() / renderer.getWidth(),
				renderer.getLayerHeight() / renderer.getHeight(), zScale);

	}

	/**
	 * Compute offset.
	 */
	public void computeOffset(final Trace list) {
		final IScope scope = renderer.getSurface().getScope();
		final IExpression expr = layer.getDefinition().getFacet(IKeyword.POSITION);

		if (expr != null) {
			final boolean containsPixels = expr.containsPixels();
			IPoint offset = list.offset;
			offset.setLocation(GamaPointFactory.castToPoint(scope, expr.value(scope)));
			if (Math.abs(offset.getX()) <= 1 && !containsPixels) {
				offset.setX(offset.getX() * renderer.getEnvWidth());
			}
			if (Math.abs(offset.getY()) <= 1 && !containsPixels) {
				offset.setY(offset.getY() * renderer.getEnvHeight());
			}

			// REMOVE TO FIX #3342
			// if (offset.x < 0) { offset.x = renderer.getEnvWidth() - offset.x; }
			// if (offset.y < 0) { offset.y = renderer.getEnvHeight() - offset.y; }

		}
		computeZ(list);
	}

	/**
	 * Increase Z.
	 */
	protected void computeZ(final Trace list) {
		double currentZLayer = renderer.getMaxEnvDim() * layer.getData().getPosition().getZ();
		list.offset.setZ(currentZLayer);
	}

	/**
	 * Checks if is light interaction.
	 *
	 * @return true, if is light interaction
	 */
	public boolean isLightInteraction() { return true; }

	/**
	 * Checks if is pickable.
	 *
	 * @return true, if is pickable
	 */
	public boolean isPickable() { return layer != null && layer.getData().isSelectable(); }

	/**
	 * Draw.
	 *
	 * @param gl
	 *            the gl
	 */
	public void draw(final OpenGL gl) {
		if (hasDepth()) {
			gl.getGL().glEnable(GL.GL_DEPTH_TEST);
		} else {
			// Addition to fix #2228 and #2222
			gl.suspendZTranslation();
			gl.getGL().glDisable(GL.GL_DEPTH_TEST);
		}
		gl.push(GLMatrixFunc.GL_MODELVIEW);
		try {
			doDrawing(gl);
		} finally {
			stopDrawing(gl);
		}
	}

	/**
	 * Do drawing.
	 *
	 * @param gl
	 *            the gl
	 * @param picking
	 *            the picking
	 */
	protected void doDrawing(final OpenGL gl) {
		if (renderer.getPickingHelper().isPicking()) {
			gl.runWithNames(() -> drawAllObjects(gl, true));
		} else if (isAnimated) {
			drawAllObjects(gl, false);
		} else {
			if (openGLListIndex == null) { openGLListIndex = gl.compileAsList(() -> drawAllObjects(gl, false)); }
			gl.drawList(openGLListIndex);
		}
	}

	/**
	 * Prepare drawing.
	 *
	 * @param gl
	 *            the gl
	 */
	protected void prepareDrawing(final OpenGL gl, final Trace list) {
		final IPoint nonNullOffset = list.getOffset();
		gl.translateBy(nonNullOffset.getX(), -nonNullOffset.getY(), hasDepth() ? nonNullOffset.getZ() : 0);
		final IPoint nonNullScale = list.getScale();
		gl.scaleBy(nonNullScale.getX(), nonNullScale.getY(), nonNullScale.getZ());
		final AxisAngle nonNullRotation = list.getRotation();

		// Rotation
		double x = nonNullOffset.getX() + renderer.getEnvWidth() * nonNullScale.getX() / 2;
		double y = nonNullOffset.getY() + renderer.getEnvHeight() * nonNullScale.getY() / 2;

		gl.translateBy(x, -y, 0d);
		IPoint p = nonNullRotation.getAxis();
		gl.rotateBy(nonNullRotation.getAngle(), p.getX(), p.getY(), p.getZ());
		gl.translateBy(-x, y, 0d);
		addFrame(gl);
	}

	/**
	 * Adds the frame.
	 *
	 * @param gl
	 *            the gl
	 */
	protected void addFrame(final OpenGL gl) {
		if (layer == null) return;
		ILayerData d = layer.getData();
		if (d instanceof FramedLayerData data) {
			IPoint size = GamaPointFactory.create(renderer.getEnvWidth(), renderer.getEnvHeight());
			final IScope scope = renderer.getSurface().getScope();
			final IExpression expr = layer.getDefinition().getFacet(IKeyword.SIZE);
			if (expr != null) { size = GamaPointFactory.castToPoint(scope, expr.value(scope)); }
			double sx = size.getX();
			double sy = size.getY();
			// Only treat values in [0,1] as proportional (percentage of env size) when the
			// expression does not already contain pixel units. Fixes #656.
			final boolean sizeContainsPixels = expr != null && expr.containsPixels();
			if (!sizeContainsPixels && sx <= 1) { sx *= renderer.getEnvWidth(); }
			if (!sizeContainsPixels && sy <= 1) { sy *= renderer.getEnvHeight(); }
			gl.pushMatrix();
			boolean previous = gl.setObjectWireframe(false);
			try {
				gl.translateBy(sx / 2, -sy / 2, 0);
				gl.scaleBy(sx, sy, 1);
				if (data.getBackgroundColor(scope) != null) {
					gl.setCurrentColor(data.getBackgroundColor(scope), 1 - data.getTransparency(scope));
					gl.drawCachedGeometry(IShape.Type.SQUARE, null);
				}
				if (data.getBorderColor() != null) {
					gl.setObjectWireframe(true);
					gl.drawCachedGeometry(IShape.Type.SQUARE, data.getBorderColor());
				}

			} finally {
				gl.setObjectWireframe(previous);
				gl.popMatrix();
			}
		}

	}

	/**
	 * Enable depth test.
	 *
	 * @return true, if successful
	 */
	protected boolean hasDepth() {
		return true;
	}

	/**
	 * Stop drawing.
	 *
	 * @param gl
	 *            the gl
	 */
	protected void stopDrawing(final OpenGL gl) {
		gl.pop(GLMatrixFunc.GL_MODELVIEW);
	}

	/**
	 * Draw all objects.
	 *
	 * @param gl
	 *            the gl
	 * @param picking
	 *            the picking
	 */
	protected void drawAllObjects(final OpenGL gl, final boolean picking) {
		drawObjects(gl, currentList, alpha, picking);
	}

	/**
	 * Draw objects.
	 *
	 * @param gl
	 *            the gl
	 * @param list
	 *            the list
	 * @param alpha
	 *            the alpha
	 * @param picking
	 *            the picking
	 */
	protected final void drawObjects(final OpenGL gl, final Trace list, final double alpha, final boolean picking) {
		prepareDrawing(gl, list);
		gl.setCurrentObjectAlpha(alpha);
		for (final AbstractObject object : list.asArray()) { gl.getDrawerFor(object).draw(object, picking); }
	}

	/**
	 * Checks if is static.
	 *
	 * @return true, if is static
	 */
	public boolean isStatic() { return layer == null || !layer.getData().isDynamic(); }

	/**
	 * Sets the alpha.
	 *
	 * @param a
	 *            the new alpha
	 */
	public void setAlpha(final Double a) { alpha = a; }

	/**
	 * Sets the offset.
	 *
	 * @param offset
	 *            the new offset
	 */
	public void setOffset(final IPoint offset) {
		if (offset != null) {
			currentList.offset.setLocation(offset);
		} else {
			currentList.offset.setLocation(NULL_OFFSET);
		}
	}

	/**
	 * Sets the scale.
	 *
	 * @param scale
	 *            the new scale
	 */
	public void setScale(final IPoint scale) {
		currentList.scale.setLocation(scale);
	}

	/**
	 * Adds the string.
	 *
	 * @param string
	 *            the string
	 * @param attributes
	 *            the attributes
	 */
	public void addString(final String string, final IDrawingAttributes attributes) {
		currentList.add(new StringObject(string, attributes));
	}

	/**
	 * Adds the file.
	 *
	 * @param file
	 *            the file
	 * @param attributes
	 *            the attributes
	 */
	public void addFile(final GamaGeometryFile file, final IDrawingAttributes attributes) {
		currentList.add(new ResourceObject(file, attributes));
	}

	/**
	 * Adds the image.
	 *
	 * @param o
	 *            the o
	 * @param attributes
	 *            the attributes
	 */
	public void addImage(final Object o, final IDrawingAttributes attributes) {
		// If no dimensions have been defined, then the image is considered as wide and tall as the environment
		Scaling3D size = attributes.getSize();
		if (size == null) {
			size = Scaling3D.of(renderer.getEnvWidth(), renderer.getEnvHeight(), 0);
			attributes.setSize(size);
		}
		final IPoint loc = attributes.getLocation();
		final IPoint newLoc = loc == null ? size.toGamaPoint().dividedBy(2) : loc;
		// We build a rectangle that will serve as a "support" for the image (which will become its texture)
		final Geometry geometry =
				GamaShapeFactory.buildRectangle(size.getX(), size.getY(), GamaPointFactory.create()).getInnerGeometry();
		attributes.setLocation(newLoc);
		attributes.setTexture(o);
		attributes.setSynthetic(true);
		addGeometry(geometry, attributes);
	}

	/**
	 * Adds the field.
	 *
	 * @param fieldValues
	 *            the field values
	 * @param attributes
	 *            the attributes
	 */
	public void addField(final IField fieldValues, final IDrawingAttributes attributes) {
		currentList.add(new MeshObject(fieldValues, attributes));
	}

	/**
	 * Adds the geometry.
	 *
	 * @param geometry
	 *            the geometry
	 * @param attributes
	 *            the attributes
	 */
	public void addGeometry(final Geometry geometry, final IDrawingAttributes attributes) {
		isAnimated = /* isAnimated || ?? */attributes.isAnimated();
		currentList.add(new GeometryObject(geometry, attributes));
	}

	/**
	 * Gets the trace.
	 *
	 * @return the trace
	 */
	protected int getTrace() { return 0; }

	/**
	 * Gets the fading.
	 *
	 * @return the fading
	 */
	protected boolean getFading() {
		if (layer == null) return false;
		final Boolean fading = layer.getData().getFading();
		return fading == null ? false : fading;
	}

	/**
	 * Clear.
	 *
	 * @param gl
	 *            the gl
	 */
	public void clear(final OpenGL gl) {
		currentList.clear();
		final Integer index = openGLListIndex;
		if (index != null) {
			gl.deleteList(index);
			openGLListIndex = null;
		}

	}

	/**
	 * Checks for trace.
	 *
	 * @return true, if successful
	 */
	public boolean hasTrace() {
		return false;
	}

	/**
	 * Checks if is overlay.
	 *
	 * @return true, if is overlay
	 */
	public boolean isOverlay() { return false; }

	/**
	 * Number of traces.
	 *
	 * @return the int
	 */
	public int numberOfActualTraces() {
		return 1;
	}

	/**
	 * Can split.
	 *
	 * @return true, if successful
	 */
	public boolean canSplit() {
		return true;
	}

	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	public boolean isVisible() { return layer == null || layer.getData().isVisible(); }

}

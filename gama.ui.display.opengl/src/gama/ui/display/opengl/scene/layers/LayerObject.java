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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;

import gama.api.runtime.scope.IScope;
import gama.api.types.color.AdaptiveColor;
import gama.api.types.color.IColor;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.matrix.IField;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.ILayer;
import gama.api.ui.layers.ILayerData;
import gama.api.ui.layers.LayerCoordinates;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.Rotation3D;
import gama.api.utils.geometry.Scaling3D;
import gama.core.outputs.layers.FramedLayerData;
import gama.core.outputs.layers.OverlayStatement;
import gama.core.util.file.GamaGeometryFile;
import gama.gaml.statements.draw.TextDrawingAttributes;
import gama.ui.display.opengl.IOpenGL;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;
import gama.ui.display.opengl.scene.AbstractObject;
import gama.ui.display.opengl.scene.geometry.GeometryObject;
import gama.ui.display.opengl.scene.geometry.PrimitiveObject;
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

	public record LayerState(LayerCoordinates coordinates, LayerCoordinates contentCoordinates,
			int x, int y, int width, int height, double contentRatio, IColor background, IColor border,
			boolean rounded, boolean selectable, boolean dynamic, boolean fading, int trace, boolean adaptive) {}

	private LayerState captureLayerState() {
		if (layer == null) return new LayerState(LayerCoordinates.WORLD, LayerCoordinates.WORLD,
				0, 0, 0, 0, 0, null, null, false, false, false, false, 0, false);
		final ILayerData data = layer.getData();
		final java.awt.Point position = data.getPositionInPixels(), size = data.getSizeInPixels();
		final FramedLayerData frame = data instanceof FramedLayerData f ? f : null;
		final gama.core.outputs.layers.OverlayLayerData overlay =
				data instanceof gama.core.outputs.layers.OverlayLayerData o ? o : null;
		return new LayerState(data.getCoordinates(), data.getContentCoordinates(), position.x, position.y,
				size.x, size.y, overlay == null ? 0 : overlay.getContentRatio(),
				frame == null ? null : frame.getBackgroundColor(), frame == null ? null : frame.getBorderColor(),
				overlay != null && overlay.isRounded(), Boolean.TRUE.equals(data.isSelectable()), data.isDynamic(),
				Boolean.TRUE.equals(data.getFading()), data.getTrace() == null ? 0 : data.getTrace(),
				layer.getDefinition() instanceof OverlayStatement o && o.mayUseAdaptiveText());
	}

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
		LayerState state;
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
			state = currentList == null ? captureLayerState() : currentList.state;
			if (currentList == null) {
				computeOffset();
				computeScale();
				computeRotation();
			} else {
				offset.setLocation(currentList.offset);
				scale.setLocation(currentList.scale);
				rotation = currentList.rotation;
			}
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

		/** Cached backing array; reallocated only when the list size changes. */
		private AbstractObject[] cachedArray = {};
		private int cachedRevision = -1;

		/**
		 * As array.
		 *
		 * @return the abstract object[]
		 */
		public AbstractObject[] asArray() {
			int n = 0;
			for (final AbstractObject object : this) {
				n += object instanceof StringObject text ? text.numberOfLines() : 1;
			}
			if (cachedArray.length != n) { cachedArray = new AbstractObject[n]; }
			if (cachedRevision != modCount) {
				int index = 0;
				for (final AbstractObject object : this) {
					if (object instanceof StringObject text) {
						for (final StringObject line : text.realizeLines(
								renderer.getAbsoluteRatioBetweenPixelsAndModelsUnits())) { cachedArray[index++] = line; }
					} else {
						cachedArray[index++] = object;
					}
				}
				cachedRevision = modCount;
			}
			return cachedArray;
		}

		@Override
		public void clear() {
			super.clear();
			java.util.Arrays.fill(cachedArray, null);
			cachedRevision = -1;
		}
	}

	/** The alpha. */
	protected volatile Double alpha = 1d;

	/** The layer. */
	public final ILayer layer;

	/** The is animated. */
	volatile boolean isAnimated;
	private boolean shared;

	public void markShared() { shared = true; }

	public boolean wasShared() { return shared; }

	/** The renderer. */
	protected final IOpenGLRenderer renderer;

	/** The current list. */
	protected Trace currentList;

	/** The open GL list index. */
	protected volatile Integer openGLListIndex;

	private double listStartZTranslation, listZIncrement, drawingStartZTranslation;
	private long drawingObjectIndex;

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
		if (isScreenLayer()) return;
		list.offset.setLocation(layer.getData().getPosition());
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
	public boolean isPickable() { return layer != null && currentList.state.selectable(); }

	/**
	 * Draw.
	 *
	 * @param gl
	 *            the gl
	 */
	public void draw(final IOpenGL gl) {
		final ILayer previousLayer = renderer.setRenderingLayer(layer);
		final LayerState previousState = renderer.setRenderingLayerState(currentList.state);
		final LayerCoordinates previous = renderer.setCoordinateSystem(
				currentList.state.contentCoordinates());
		try {
			final LayerState state = currentList.state;
			if (state.adaptive() && !renderer.getPickingHelper().isPicking()) {
				final List<TextDrawingAttributes> texts = new ArrayList<>();
				for (final AbstractObject object : currentList.asArray()) {
					if (object instanceof StringObject string
							&& string.getAttributes() instanceof TextDrawingAttributes text
							&& text.getAdaptiveTextBounds() != null) {
						texts.add(text);
					}
				}
				final IColor background = state.background();
				final AdaptiveColor.Background adaptiveBackground = background == null ? null
						: new AdaptiveColor.Background(new Rectangle2D.Double(0, 0,
								state.width(), state.height()), background, 1);
				renderer.getAdaptiveTextHelper().resolve(texts, state.x(), state.y(), adaptiveBackground, alpha);
			}
			drawInCoordinates(gl);
		} finally {
			renderer.setCoordinateSystem(previous);
			renderer.setRenderingLayerState(previousState);
			renderer.setRenderingLayer(previousLayer);
		}
	}

	private void drawInCoordinates(final IOpenGL gl) {
		final boolean depthTest = gl.getGL().glIsEnabled(GL.GL_DEPTH_TEST);
		final boolean mapOrder = hasDepth() && renderer.getData().isMapOrder();
		final byte[] depthWrite = new byte[1];
		if (mapOrder) {
			gl.getGL().glGetBooleanv(GL.GL_DEPTH_WRITEMASK, depthWrite, 0);
			gl.getGL().glDepthMask(false);
		}
		if (hasDepth() && !mapOrder) {
			gl.getGL().glEnable(GL.GL_DEPTH_TEST);
		} else {
			// Addition to fix #2228 and #2222
			if (!hasDepth()) { gl.suspendZTranslation(); }
			gl.getGL().glDisable(GL.GL_DEPTH_TEST);
		}
		if (isScreenLayer()) {
			gl.pushIdentity(GLMatrixFunc.GL_PROJECTION);
			if (renderer.getPickingHelper().isBeginningPicking()) {
				renderer.getPickingHelper().applyPickTransform();
			}
			gl.ortho(0, gl.getViewWidth(), -gl.getViewHeight(), 0, -1, 1);
			// Flatten clip-space depth, not object transforms: elevation must not clip screen contents.
			gl.scaleBy(1, 1, 0);
			// Keep object transforms off the projection stack and independent of the world camera.
			gl.pushIdentity(GLMatrixFunc.GL_MODELVIEW);
		} else {
			gl.push(GLMatrixFunc.GL_MODELVIEW);
		}
		try {
			doDrawing(gl);
		} finally {
			try {
				stopDrawing(gl);
			} finally {
				if (mapOrder) { gl.getGL().glDepthMask(depthWrite[0] != 0); }
				if (depthTest) {
					gl.getGL().glEnable(GL.GL_DEPTH_TEST);
				} else {
					gl.getGL().glDisable(GL.GL_DEPTH_TEST);
				}
			}
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
	protected void doDrawing(final IOpenGL gl) {
		drawingStartZTranslation = gl.getCurrentZTranslation();
		drawingObjectIndex = 0;
		final boolean picking = renderer.getPickingHelper().isPicking() && isPickable();
		if (isScreenLayer()) {
			if (picking) {
				gl.runWithNames(() -> drawAllObjects(gl, true));
			} else {
				drawAllObjects(gl, false);
			}
			return;
		}
		if (picking) {
			gl.runWithNames(() -> drawAllObjects(gl, true));
		} else if (isAnimated || hasAdaptiveDraws()) {
			drawAllObjects(gl, false);
		} else {
			final boolean stored = gl.isDrawingStored();
			if (stored && openGLListIndex != null
					&& (Double.compare(listStartZTranslation, gl.getCurrentZTranslation()) != 0
							|| Double.compare(listZIncrement, gl.getCurrentZIncrement()) != 0)) {
				gl.deleteList(openGLListIndex);
				openGLListIndex = null;
			}
			if (openGLListIndex == null) {
				if (stored) {
					for (final AbstractObject object : currentList.asArray()) {
						if (!object.areTexturesAvailable(gl)) {
							drawAllObjects(gl, false);
							return;
						}
					}
					listStartZTranslation = gl.getCurrentZTranslation();
					listZIncrement = gl.getCurrentZIncrement();
				}
				openGLListIndex = gl.compileAsList(() -> drawAllObjects(gl, false));
			}
			gl.drawList(openGLListIndex);
		}
	}

	protected boolean hasAdaptiveDraws() { return hasAdaptiveDraws(currentList); }

	protected final boolean hasAdaptiveDraws(final Trace list) {
		for (final AbstractObject object : list.asArray()) {
			if (object.getAttributes() instanceof TextDrawingAttributes text && text.isAdaptiveDraw()) return true;
		}
		return false;
	}

	/**
	 * Prepare drawing.
	 *
	 * @param gl
	 *            the gl
	 */
	protected void prepareDrawing(final IOpenGL gl, final Trace list) {
		if (isScreenLayer()) {
			final LayerState d = list.state;
			gl.translateBy(d.x(), -d.y(), 0);
			addScreenFrame(gl);
			if (d.contentCoordinates() != LayerCoordinates.SCREEN) {
				gl.scaleBy(d.width() / renderer.getEnvWidth(),
						d.height() / renderer.getEnvHeight(), list.getScale().getZ());
			}
			return;
		}
		final IPoint nonNullOffset = list.getOffset();
		gl.translateBy(nonNullOffset.getX(), -nonNullOffset.getY(), hasDepth() ? nonNullOffset.getZ() : 0);
		final IPoint nonNullScale = list.getScale();
		gl.scaleBy(nonNullScale.getX(), nonNullScale.getY(), nonNullScale.getZ());
		final AxisAngle nonNullRotation = list.getRotation();

		if (nonNullRotation.getAngle() != 0d) {
			final double x = nonNullOffset.getX() + renderer.getEnvWidth() * nonNullScale.getX() / 2;
			final double y = nonNullOffset.getY() + renderer.getEnvHeight() * nonNullScale.getY() / 2;
			gl.translateBy(x, -y, 0d);
			final IPoint p = nonNullRotation.getAxis();
			gl.rotateBy(nonNullRotation.getAngle(), p.getX(), p.getY(), p.getZ());
			gl.translateBy(-x, y, 0d);
		}
		addFrame(gl);
	}

	/**
	 * Adds the frame.
	 *
	 * @param gl
	 *            the gl
	 */
	protected void addFrame(final IOpenGL gl) {
		addFrame(gl, renderer.getEnvWidth(), renderer.getEnvHeight());
	}

	private void addFrame(final IOpenGL gl, final double width, final double height) {
		final LayerState data = currentList.state;
		final IColor background = data.background();
		final IColor border = data.border();
		if (background == null && border == null) return;
		final boolean previous = gl.setObjectWireframe(false);
		final double previousAlpha = gl.getCurrentObjectAlpha();
		gl.pushMatrix();
		try {
			gl.translateBy(width / 2, -height / 2, 0);
			gl.scaleBy(width, height, 1);
			if (background != null) {
				// FramedLayerData has already applied the layer's transparency to this colour.
				gl.setCurrentObjectAlpha(1);
				gl.setCurrentColor(background, 1);
				gl.drawCachedGeometry(IShape.Type.SQUARE, null);
			}
			gl.setCurrentObjectAlpha(previousAlpha);
			if (border != null) {
				gl.setObjectWireframe(true);
				gl.setCurrentColor(border);
				// Cached border drawing multiplies the current colour alpha again.
				gl.beginDrawing(GL.GL_LINE_LOOP);
				gl.outputNormal(0, 0, 1);
				gl.outputVertex(-0.5, -0.5, 0);
				gl.outputVertex(0.5, -0.5, 0);
				gl.outputVertex(0.5, 0.5, 0);
				gl.outputVertex(-0.5, 0.5, 0);
				gl.endDrawing();
			}
		} finally {
			gl.setCurrentObjectAlpha(previousAlpha);
			gl.setObjectWireframe(previous);
			gl.popMatrix();
		}
	}

	/**
	 * Enable depth test.
	 *
	 * @return true, if successful
	 */
	protected boolean hasDepth() {
		return !isScreenLayer();
	}

	/**
	 * Stop drawing.
	 *
	 * @param gl
	 *            the gl
	 */
	protected void stopDrawing(final IOpenGL gl) {
		if (isScreenLayer()) {
			gl.resumeZTranslation();
			gl.pop(GLMatrixFunc.GL_PROJECTION);
		}
		gl.pop(GLMatrixFunc.GL_MODELVIEW);
	}

	private boolean isScreenLayer() {
		return layer != null && (currentList == null ? layer.getData().getCoordinates()
				: currentList.state.coordinates()) == LayerCoordinates.SCREEN;
	}

	private void addScreenFrame(final IOpenGL gl) {
		final LayerState data = currentList.state;
		addFrame(gl, data.width(), data.height());
	}

	/**
	 * Draw all objects.
	 *
	 * @param gl
	 *            the gl
	 * @param picking
	 *            the picking
	 */
	protected void drawAllObjects(final IOpenGL gl, final boolean picking) {
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
	protected final void drawObjects(final IOpenGL gl, final Trace list, final double alpha, final boolean picking) {
		gl.setCurrentObjectAlpha(alpha);
		prepareDrawing(gl, list);
		final AbstractObject<?, ?>[] objects = list.asArray();
		final long firstObject = drawingObjectIndex;
		gl.drawLayerObjects(objects, isStatic(), () -> drawObjectList(gl, objects, picking));
		if (layer != null && hasDepth()) { drawingObjectIndex = firstObject + objects.length; }
	}

	private void drawObjectList(final IOpenGL gl, final AbstractObject<?, ?>[] objects, final boolean picking) {
		for (final AbstractObject object : objects) {
			if (layer != null && hasDepth()) {
				gl.setCurrentZTranslation(drawingStartZTranslation + drawingObjectIndex * gl.getCurrentZIncrement());
				drawingObjectIndex++;
			}
			if (picking && object instanceof StringObject string
					&& string.getAttributes() instanceof TextDrawingAttributes text
					&& text.getAdaptiveTextBounds() != null) {
				continue;
			}
			gl.getDrawerFor(object).draw(object, picking);
		}
	}

	/**
	 * Checks if is static.
	 *
	 * @return true, if is static
	 */
	public boolean isStatic() { return layer == null || !currentList.state.dynamic(); }

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
		currentList.add(StringObject.collected(string, attributes.snapshot()));
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
		currentList.add(new ResourceObject(file, attributes.snapshot()));
	}

	public void addFile(final GamaGeometryFile file, final IDrawingAttributes attributes, final String key) {
		currentList.add(new ResourceObject(file, attributes.snapshot(), key));
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
		attributes.setLocation(newLoc);
		attributes.setTexture(o);
		attributes.setSynthetic(true);
		isAnimated = isAnimated || attributes.isAnimated();
		currentList.add(new gama.ui.display.opengl.scene.geometry.ImageObject(attributes.snapshot()));
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
		isAnimated = isAnimated || attributes.isAnimated();
		currentList.add(new MeshObject(fieldValues, attributes.snapshot()));
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
		isAnimated = isAnimated || attributes.isAnimated();
		currentList.add(new GeometryObject(geometry, attributes.snapshot()));
	}

	public void addGeometry(final gama.api.ui.displays.GeometryDrawing command, final IDrawingAttributes attributes) {
		isAnimated = isAnimated || attributes.isAnimated();
		currentList.add(new GeometryObject(command, attributes.snapshot()));
	}

	public void addPrimitive(final PrimitiveObject object) {
		isAnimated = isAnimated || object.getAttributes().isAnimated();
		currentList.add(object);
	}

	public void addGeometryObject(final GeometryObject object) {
		isAnimated = isAnimated || object.getAttributes().isAnimated();
		currentList.add(object);
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
		return currentList.state.fading();
	}

	/**
	 * Clear.
	 *
	 * @param gl
	 *            the gl
	 */
	public void clear(final IOpenGL gl) {
		currentList.clear();
		final Integer index = openGLListIndex;
		if (index != null) {
			gl.deleteList(index);
			openGLListIndex = null;
		}

	}

	public void resetForReuse() {
		isAnimated = false;
		isFading = false;
		captureState();
	}

	/** Called only while the producer owns the empty trace. */
	public void captureState() {
		currentList.state = captureLayerState();
		currentList.computeOffset();
		currentList.computeScale();
		currentList.computeRotation();
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
	public boolean isOverlay() { return isScreenLayer(); }

	/**
	 * Number of traces.
	 *
	 * @return the int
	 */
	public int numberOfActualTraces() {
		return 1;
	}

	/** Number of objects that advance the world-space Z translation when this layer is drawn. */
	public long numberOfZTranslatedObjects() {
		if (layer == null || isScreenLayer()) return 0;
		return numberOfObjects(currentList);
	}

	protected long numberOfObjects(final Trace trace) {
		long count = 0;
		for (final AbstractObject object : trace) {
			count += object instanceof StringObject text ? text.numberOfLines() : 1;
		}
		return count;
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

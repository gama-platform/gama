/*******************************************************************************************************
 *
 * OpenGL.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4;

import java.awt.image.BufferedImage;
import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;

import gama.api.utils.geometry.ICoordinates.VertexVisitor;
import gama.ui.display.opengl.ITesselator;
import gama.ui.display.opengl.IOpenGL;
import gama.ui.display.opengl.renderer.helpers.PolygonOffset;
import jogamp.opengl.glu.tessellator.GLUtessellatorImpl;

import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.IDrawingAttributes.DrawerType;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.ICoordinates;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.geometry.Rotation3D;
import gama.api.utils.geometry.Scaling3D;
import gama.api.utils.geometry.UnboundedCoordinateSequence;
import gama.api.utils.interfaces.IImageProvider;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.util.file.GamaGeometryFile;
import gama.dev.DEBUG;
import gama.gaml.operators.Maths;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;
import gama.ui.display.opengl.renderer.caches.PolygonTessellationCache;
import gama.ui.display.opengl4.renderer.caches.GeometryCache;
import gama.ui.display.opengl4.renderer.caches.GeometryCache.BuiltInGeometry;
import gama.ui.display.opengl4.renderer.caches.TextureCache2;
import gama.ui.display.opengl4.renderer.helpers.AbstractRendererHelper;
import gama.ui.display.opengl4.renderer.shaders.BasicShader;
import gama.ui.display.opengl4.renderer.shaders.MatrixStack;
import gama.ui.display.opengl.scene.AbstractObject;
import gama.ui.display.opengl.scene.ObjectDrawer;
import gama.ui.display.opengl4.scene.geometry.GeometryDrawer;
import gama.ui.display.opengl4.scene.mesh.MeshDrawer;
import gama.ui.display.opengl.scene.mesh.MeshObject;
import gama.ui.display.opengl.scene.resources.ResourceDrawer;
import gama.ui.display.opengl4.scene.text.TextDrawer;
import gama.ui.shared.utils.DPIHelper;

/**
 * A class that represents an intermediate state between the rendering and the opengl state. Implements
 * {@link ITesselator} so that the GLU tessellator callbacks ({@code begin}, {@code vertex}, {@code end}) are wired
 * directly to the VBO-based drawing path used by this core-profile renderer.
 */
public class OpenGL extends AbstractRendererHelper implements IOpenGL, ITesselator {

	static {
		DEBUG.OFF();
		GamaPreferences.Displays.DRAW_ROTATE_HELPER.onChange(v -> SHOULD_DRAW_ROTATION_SPHERE = v);
	}

	/** The should draw rotation sphere. */
	private static boolean SHOULD_DRAW_ROTATION_SPHERE = GamaPreferences.Displays.DRAW_ROTATE_HELPER.getValue();

	/** The Constant NO_TEXTURE. */
	public static final int NO_TEXTURE = Integer.MAX_VALUE;

	/** The Constant PROFILE. */
	public static final GLProfile PROFILE = GLProfile.get(GLProfile.GL4);

	/** The drawers. */
	final Map<DrawerType, ObjectDrawer<?, ?>> drawers = new HashMap<>();

	private record MeshKey(MeshObject object) {
		@Override
		public int hashCode() { return System.identityHashCode(object.getObject()); }

		@Override
		public boolean equals(final Object other) {
			return other instanceof MeshKey key && object.getObject() == key.object.getObject();
		}
	}

	private final Cache<MeshKey, MeshDrawer> meshDrawers = CacheBuilder.newBuilder().maximumSize(8)
			.removalListener((RemovalNotification<MeshKey, MeshDrawer> notification) ->
					notification.getValue().dispose()).build();

	/** The viewport. */
	// Matrices of the display
	public final int[] viewport = new int[4];

	/** The mvmatrix. */
	public final double mvmatrix[] = new double[16];

	/** The projmatrix. */
	public final double projmatrix[] = new double[16];

	/** The gl. */
	// The real openGL context
	private GL4 gl;

	/** The basic shader. */
	private BasicShader basicShader;

	private int currentPickingId = -1;

	public void loadPicking(final BasicShader shader, final float alpha) {
		shader.loadPicking(renderer.getPickingHelper().isBeginningPicking(), currentPickingId, alpha);
	}

	/**
	 * Gets the basic shader. Used by {@link gama.ui.display.opengl4.renderer.helpers.LightHelper} to push
	 * per-frame light uniforms.
	 *
	 * @return the BasicShader program, or {@code null} if not yet initialised
	 */
	public BasicShader getBasicShader() { return basicShader; }

	/** The model view matrix. */
	private final MatrixStack modelViewMatrix = new MatrixStack();

	/** The projection matrix. */
	private final MatrixStack projectionMatrix = new MatrixStack();

	/** The current matrix mode. */
	private int currentMatrixMode = GLMatrixFunc.GL_MODELVIEW;

	/**
	 * Gets the current matrix stack.
	 *
	 * @return the current matrix stack
	 */
	public MatrixStack getCurrentMatrixStack() {
		return currentMatrixMode == GLMatrixFunc.GL_PROJECTION ? projectionMatrix : modelViewMatrix;
	}

	/**
	 * Returns the current model-view {@link org.joml.Matrix4f}. Used by
	 * {@link gama.ui.display.opengl4.scene.text.TextDrawer} to feed the scene matrices to the JOGL graph/curve
	 * {@code RegionRenderer}.
	 *
	 * @return the model-view matrix (live reference — do not mutate)
	 */
	public org.joml.Matrix4f getModelViewMatrix() { return modelViewMatrix.getCurrentMatrix(); }

	/**
	 * Returns the current projection {@link org.joml.Matrix4f}. Used by
	 * {@link gama.ui.display.opengl4.scene.text.TextDrawer} to feed the scene matrices to the JOGL graph/curve
	 * {@code RegionRenderer}.
	 *
	 * @return the projection matrix (live reference — do not mutate)
	 */
	public org.joml.Matrix4f getProjectionMatrix() { return projectionMatrix.getCurrentMatrix(); }

	@Override
	public boolean project(final double x, final double y, final double z, final double[] result) {
		final var point = new org.joml.Vector4f((float) x, (float) y, (float) z, 1);
		getModelViewMatrix().transform(point);
		getProjectionMatrix().transform(point);
		if (point.w <= 0) return false;
		result[0] = (point.x / point.w + 1) * getViewWidth() / 2d;
		result[1] = (1 - point.y / point.w) * getViewHeight() / 2d;
		result[2] = (point.z / point.w + 1) / 2d;
		return Double.isFinite(result[0]) && Double.isFinite(result[1]) && result[2] >= 0 && result[2] <= 1;
	}

	/**
	 * The glu. Used for GLU tessellator callbacks ({@code gluTessCallback} etc.) and for {@code gluProject} in
	 * {@link #reshape} and {@link #getPixelWidthAndHeightOfWorld}. Not used for any fixed-function rendering.
	 */
	private final GLU glu;

	/** The view height. */
	private int viewWidth, viewHeight;

	/** The current polygon mode. */
	private int currentPolygonMode = GL4.GL_FILL;

	/** The current color. */
	private IColor currentColor = GamaColorFactory.getWithDoubles(1, 1, 1, 1);

	/** The picking state. */
	// private final PickingHelper pickingState;

	/** The texture cache. */
	// Textures
	private final TextureCache2 textureCache = new TextureCache2(this);

	/** The texture envelope. */
	private final IEnvelope textureEnvelope = GamaEnvelopeFactory.create();

	/** The current texture rotation. */
	private final Rotation3D currentTextureRotation = Rotation3D.identity();

	/** The null texture rotation. */
	private final Rotation3D nullTextureRotation = new Rotation3D(0, 0, 0, 1, false);

	/** The textured. */
	private boolean textured;

	/** The primary texture. */
	private int primaryTexture = NO_TEXTURE;

	/** The alternate texture. */
	private int alternateTexture = NO_TEXTURE;

	/** The anisotropic level. */
	public static float ANISOTROPIC_LEVEL = 0;

	/** The current color. */
	// Colors

	/** The current object alpha. */
	private double currentObjectAlpha = 1d;

	/** The previous object lighting. */
	boolean previousObjectWireframe, previousObjectLighting;

	/** The previous object line width. */
	float currentObjectLineWidth = 1f,
			previousObjectLineWidth = GamaPreferences.Displays.CORE_LINE_WIDTH.getValue().floatValue();
	private float nativeLineWidth = 1f;
	private final float[] lineWidthRange = new float[2];
	private boolean lineWidthLimitsKnown, unsupportedLineWidthReported;

	/** The previous display lighting. */
	boolean previousDisplayWireframe, previousDisplayLighting;

	/** The lighted. */
	private boolean objectIsLighted;

	/** The display is lighted. */
	private boolean displayIsLighted;

	/** The geometry cache. */
	// Geometries
	protected final GeometryCache geometryCache;

	/** The display is wireframe. */
	protected volatile boolean displayIsWireframe;

	/** The object is wireframe. */
	protected volatile boolean objectIsWireframe;

	/**
	 * The GLU tessellator object used to triangulate concave polygons and polygons with holes. Wired to the
	 * {@link ITesselator} callbacks implemented by this class so that tessellated vertices are written directly into the
	 * VBO float buffers rather than via deprecated {@code glVertex} calls.
	 */
	final GLUtessellatorImpl tobj = (GLUtessellatorImpl) GLU.gluNewTess();

	/**
	 * Visitor lambda that feeds each coordinate from an {@link gama.api.utils.geometry.ICoordinates} ring into the GLU
	 * tessellator via {@code gluTessVertex}.
	 */
	final VertexVisitor glTesselatorDrawer;

	/** The ratios. */
	// World
	final IPoint ratios = GamaPointFactory.create();

	/** The rotation mode. */
	private boolean rotationMode;

	/** The current normal. */
	final IPoint currentNormal = GamaPointFactory.create();

	/** The texture coords. */
	final IPoint textureCoords = GamaPointFactory.create();

	/** The working vertices. */
	final UnboundedCoordinateSequence workingVertices = new UnboundedCoordinateSequence();

	/** The saved Z translation. */
	private double currentZIncrement, currentZTranslation, savedZTranslation;

	/** The end scene. */
	// private final boolean useJTSTriangulation = !GamaPreferences.Displays.OPENGL_TRIANGULATOR.getValue();
	private final Pass endScene = this::endScene;

	/**
	 * Instantiates a new open GL.
	 *
	 * @param renderer
	 *            the renderer
	 */
	public OpenGL(final IOpenGLRenderer renderer) {
		super(renderer);
		glu = new GLU();
		geometryCache = new GeometryCache(renderer, this);
		glTesselatorDrawer = (final double[] ordinates) -> { tobj.gluTessVertex(ordinates, 0, ordinates); };
		GLU.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, this);
		GLU.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, this);
		GLU.gluTessCallback(tobj, GLU.GLU_TESS_END, this);
		GLU.gluTessProperty(tobj, GLU.GLU_TESS_TOLERANCE, 0.1);
		TextDrawer td = new TextDrawer(this);
		var gd = new GeometryDrawer(this);
		var rd = new ResourceDrawer(this);
		drawers.put(DrawerType.STRING, td);
		drawers.put(DrawerType.GEOMETRY, gd);
		drawers.put(DrawerType.RESOURCE, rd);
	}

	/** The last bound texture ID for optimization. */
	private int lastBoundTexture = NO_TEXTURE;

	/** The last texture minification filter. */
	private int lastTextureMinFilter = GL.GL_NEAREST;

	private boolean currentTextureSmooth;

	/** The gl drawer. */
	private final ICoordinates.IndexedVisitor glDrawer = this::drawVertex;

	/**
	 * Gets the drawer for.
	 *
	 * @param type
	 *            the type
	 * @return the drawer for
	 */
	public ObjectDrawer<? extends AbstractObject<?, ?>, ?> getDrawerFor(final AbstractObject<?, ?> object) {
		// Only create signature and checking mesh drawers if the object is indeed a mesh.
		if (object instanceof MeshObject mo) {
			final MeshKey key = new MeshKey(mo);
			MeshDrawer drawer = meshDrawers.getIfPresent(key);
			if (drawer == null) {
				drawer = new MeshDrawer(this);
				meshDrawers.put(key, drawer);
			}
			return drawer;
		}
		return drawers.get(object.type);
	}

	/**
	 * Gets the geometry drawer.
	 *
	 * @return the geometry drawer
	 */
	public GeometryDrawer getGeometryDrawer() { return (GeometryDrawer) drawers.get(DrawerType.GEOMETRY); }

	/**
	 * Dispose.
	 */
	public void dispose() {
		if (reusableMapBatch != null) { reusableMapBatch.dispose(); }
		if (instanceBuffer != 0) {
			gl.glDeleteBuffers(1, new int[] { instanceBuffer }, 0);
			instanceBuffer = 0;
		}
		roadDrawings.invalidateAll();
		objectDrawings.invalidateAll();
		listCache.clear();
		for (ObjectDrawer<?, ?> o : drawers.values()) { o.dispose(); }
		meshDrawers.invalidateAll();
		geometryCache.dispose();
		retainedDrawings.invalidateAll();
		drawingCandidates.invalidateAll();
		polygonTessellations.dispose();
		polygonDrawings.invalidateAll();
		coordinateDrawings.invalidateAll();
		coordinateCandidates.invalidateAll();
		textureCache.dispose();
		if (neutralTexture != 0) {
			gl.glDeleteTextures(1, new int[] { neutralTexture }, 0);
			neutralTexture = 0;
		}
		gl = null;

	}

	@Override
	public GL4 getGL() { return gl; }

	/**
	 * Sets the gl2.
	 *
	 * @param gl2
	 *            the new gl2
	 */
	public void setGL4(final GL4 gl2) { this.gl = gl2; }

	@Override
	public GLProfile getProfile() { return PROFILE; }

	@Override
	public void setGL(final GL context) { setGL4(context.getGL4()); }

	@Override
	public void reshape(final GL context, final int width, final int height) {
		reshape(context.getGL4(), width, height);
	}

	@Override
	public int[] getViewport() { return viewport; }

	@Override
	public void lookAt(final IPoint position, final IPoint target, final IPoint up) {
		getCurrentMatrixStack().multMatrix(new org.joml.Matrix4f().lookAt(
				(float) position.getX(), (float) position.getY(), (float) position.getZ(),
				(float) target.getX(), (float) target.getY(), (float) target.getZ(),
				(float) up.getX(), (float) up.getY(), (float) up.getZ()));
	}

	private final org.joml.Vector3f unprojectResult = new org.joml.Vector3f();

	@Override
	public synchronized void unproject(final double x, final double y, final double depth, final double[] result) {
		final var modelView = new org.joml.Matrix4f(
				(float) mvmatrix[0], (float) mvmatrix[1], (float) mvmatrix[2], (float) mvmatrix[3],
				(float) mvmatrix[4], (float) mvmatrix[5], (float) mvmatrix[6], (float) mvmatrix[7],
				(float) mvmatrix[8], (float) mvmatrix[9], (float) mvmatrix[10], (float) mvmatrix[11],
				(float) mvmatrix[12], (float) mvmatrix[13], (float) mvmatrix[14], (float) mvmatrix[15]);
		final var projection = new org.joml.Matrix4f(
				(float) projmatrix[0], (float) projmatrix[1], (float) projmatrix[2], (float) projmatrix[3],
				(float) projmatrix[4], (float) projmatrix[5], (float) projmatrix[6], (float) projmatrix[7],
				(float) projmatrix[8], (float) projmatrix[9], (float) projmatrix[10], (float) projmatrix[11],
				(float) projmatrix[12], (float) projmatrix[13], (float) projmatrix[14], (float) projmatrix[15]);
		projection.mul(modelView).unproject((float) x, (float) y, (float) depth, viewport, unprojectResult);
		result[0] = unprojectResult.x;
		result[1] = unprojectResult.y;
		result[2] = unprojectResult.z;
	}

	@Override
	public void ortho(final double left, final double right, final double bottom, final double top,
			final double near, final double far) {
		getCurrentMatrixStack().ortho(left, right, bottom, top, near, far);
	}

	@Override
	public boolean isDrawingStored() { return false; }

	/**
	 * Reshapes the GL world to comply with a new view size and computes the resulting ratios between pixels and world
	 * coordinates
	 *
	 * @param newGL
	 *            the (possibly new) GL4 context
	 * @param width
	 *            the width of the view (in pixels)
	 * @param height
	 *            the height of the view (in pixels)
	 * @return
	 */
	public void reshape(final GL4 newGL, final int width, final int height) {
		setGL4(newGL);
		if (basicShader == null) {
			basicShader = new BasicShader(newGL);
			basicShader.start();

			int[] vao = new int[1];
			newGL.glGenVertexArrays(1, vao, 0);
			vaoId = vao[0];
			newGL.glBindVertexArray(vaoId);
			newGL.glGenBuffers(4, vboIds, 0);
		}
		// newGL.glViewport(0, 0, width, height);
		viewWidth = width;
		viewHeight = height;
		resetMatrix(GLMatrixFunc.GL_PROJECTION);
		updatePerspective();
		// Note: updatePerspective() calls animate() and then reads back mvmatrix/projmatrix from our MatrixStacks.
		// Reset model-view after perspective so gluProject below uses the correct projection.
		resetMatrix(GLMatrixFunc.GL_MODELVIEW);

		final double[] pixelSize = new double[4];
		glu.gluProject(getWorldWidth(), 0, 0, mvmatrix, 0, projmatrix, 0, viewport, 0, pixelSize, 0);
		final double initialEnvWidth = pixelSize[0];
		final double initialEnvHeight = pixelSize[1];
		final double envWidthInPixels = 2 * pixelSize[0] - width;
		final double envHeightInPixels = 2 * pixelSize[1] - height;
		final double windowWidthInModelUnits = getWorldWidth() * width / envWidthInPixels;
		final double windowHeightInModelUnits = getWorldHeight() * height / envHeightInPixels;
		final double xRatio = width / windowWidthInModelUnits / getData().getZoomLevel();
		final double yRatio = height / windowHeightInModelUnits / getData().getZoomLevel();
		if (DEBUG.IS_ON()) {
			debugSizes(width, height, initialEnvWidth, initialEnvHeight, envWidthInPixels, envHeightInPixels,
					getData().getZoomLevel(), xRatio, yRatio);
		}
		ratios.setLocation(xRatio, yRatio, 0d);
	}

	/**
	 * Debug sizes.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param initialEnvWidth
	 *            the initial env width
	 * @param initialEnvHeight
	 *            the initial env height
	 * @param envWidth
	 *            the env width
	 * @param envHeight
	 *            the env height
	 * @param zoomLevel
	 *            the zoom level
	 * @param xRatio
	 *            the x ratio
	 * @param yRatio
	 *            the y ratio
	 */
	private void debugSizes(final int width, final int height, final double initialEnvWidth,
			final double initialEnvHeight, final double envWidth, final double envHeight, final double zoomLevel,
			final double xRatio, final double yRatio) {

		DEBUG.SECTION("RESHAPING TO " + width + "x" + height);
		DEBUG.OUT("Camera zoom level ", 35, zoomLevel);
		DEBUG.OUT("Size of env in units ", 35, getWorldWidth() + " | " + getWorldHeight());
		DEBUG.OUT("Ratio width/height in units ", 35, getWorldWidth() / getWorldHeight());
		DEBUG.OUT("Initial Size of env in pixels ", 35, initialEnvWidth + " | " + initialEnvHeight);
		DEBUG.OUT("Size of env in pixels ", 35, envWidth + " | " + envHeight);
		DEBUG.OUT("Ratio width/height in pixels ", 35, envWidth / envHeight);
		DEBUG.OUT("Window pixels/env pixels ", 35, width / envWidth + " | " + height / envHeight);
		DEBUG.OUT("Current XRatio pixels/env in units ", 35, xRatio + " | " + yRatio);
		DEBUG.OUT("Device Zoom =  " + DPIHelper.getDeviceZoom(renderer.getCanvas().getMonitor()));
		DEBUG.OUT("AutoScale down = ", false);
		DEBUG.OUT(" " + DPIHelper.autoScaleDown(getCanvas().getMonitor(), width) + " "
				+ DPIHelper.autoScaleDown(getCanvas().getMonitor(), height));
		// DEBUG.OUT("Client area of window:" + getRenderer().getCanvas().getClientArea());
	}

	/** The maximum ratio between the far and near clipping planes preserving depth-buffer precision. */
	private static final double MAX_DEPTH_RANGE_RATIO = 1e5;

	/**
	 * Update perspective.
	 *
	 * @param gl
	 *            the gl
	 */
	public void updatePerspective() {
		final double height = getViewHeight();
		final double aspect = getViewWidth() / (height == 0d ? 1d : height);
		final double maxDim = getMaxEnvDim();
		final double resizeScaleFactor =
				renderer.getSurface().getResizeScaleFactor(getViewWidth(), getViewHeight());
		double zNear = getZNear();
		if (zNear < 0.0) {
			zNear = maxDim / 100d;
			data.setZNear(zNear);
		}
		double zFar = getZFar();
		if (zFar < 0.0) {
			zFar = maxDim * 100d;
			data.setZFar(zFar);
		}
		// A 24-bit depth buffer cannot resolve surfaces when zFar/zNear is too large: back faces then win the
		// depth test and objects appear lit from behind (#973).
		zNear = Math.max(zNear, zFar / MAX_DEPTH_RANGE_RATIO);

		if (!getData().isOrtho()) {
			try {
				double fW, fH;
				final double fovY = getData().getCameraLens();
				if (aspect > 1.0) {
					fH = Math.tan(fovY / 360 * Math.PI) * zNear;
					fW = fH * aspect;
				} else {
					fW = Math.tan(fovY / 360 * Math.PI) * zNear;
					fH = fW / aspect;
				}

				fW *= resizeScaleFactor;
				fH *= resizeScaleFactor;
				getCurrentMatrixStack().frustum(-fW, fW, -fH, fH, zNear, zFar);
			} catch (final BufferOverflowException e) {
				DEBUG.ERR("Buffer overflow exception");
			}
		} else if (aspect >= 1.0) {
			// Match the normal depth direction of the perspective projection and the legacy backend.
			getCurrentMatrixStack().ortho(-maxDim * aspect * resizeScaleFactor,
					maxDim * aspect * resizeScaleFactor, -maxDim * resizeScaleFactor, maxDim * resizeScaleFactor,
					-maxDim * 10, maxDim * 10);
		} else {
			getCurrentMatrixStack().ortho(-maxDim * resizeScaleFactor, maxDim * resizeScaleFactor,
					-maxDim / aspect * resizeScaleFactor, maxDim / aspect * resizeScaleFactor, -maxDim * 10,
					maxDim * 10);
		}

		// else {
		// gl.glOrtho(-maxDim, maxDim, -maxDim, maxDim, maxDim * 10, -maxDim * 10);
		// }
		//
		// translateBy(0d, 0d, maxDim * 0.2);
		getRenderer().getCameraHelper().animate();
		// Read matrices from our own MatrixStack, not from the deprecated GL state
		// (GL_MODELVIEW_MATRIX / GL_PROJECTION_MATRIX are removed in core GL4).
		float[] mv = new float[16];
		float[] pr = new float[16];
		modelViewMatrix.getCurrentMatrix().get(mv);
		projectionMatrix.getCurrentMatrix().get(pr);
		synchronized (this) {
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			for (int i = 0; i < 16; i++) {
				mvmatrix[i] = mv[i];
				projmatrix[i] = pr[i];
			}
		}
	}

	/**
	 * Gets the pixel width and height of world.
	 *
	 * @return the pixel width and height of world
	 */
	public synchronized double[] getPixelWidthAndHeightOfWorld() {
		final double[] coord = new double[4];
		glu.gluProject(getWorldWidth(), 0, 0, mvmatrix, 0, projmatrix, 0, viewport, 0, coord, 0);
		return coord;
	}

	/**
	 * Gets the view width.
	 *
	 * @return the view width
	 */
	public int getViewWidth() { return viewWidth; }

	/**
	 * Gets the view height.
	 *
	 * @return the view height
	 */
	public int getViewHeight() { return viewHeight; }

	/**
	 * Sets the z increment.
	 *
	 * @param z
	 *            the new z increment
	 */
	public void setZIncrement(final double z) {
		currentZTranslation = 0;
		currentZIncrement = z;
	}

	/**
	 * Suspend Z translation.
	 */
	public void suspendZTranslation() {
		savedZTranslation = currentZTranslation;
		currentZTranslation = 0;
	}

	/**
	 * Resume Z translation.
	 */
	public void resumeZTranslation() {
		currentZTranslation = savedZTranslation;
	}

	/**
	 * Gets the current Z translation.
	 *
	 * @return the current Z translation
	 */
	public double getCurrentZTranslation() { return currentZTranslation; }

	@Override
	public void setCurrentZTranslation(final double translation) { currentZTranslation = translation; }

	/**
	 * Gets the current Z increment.
	 *
	 * @return the current Z increment
	 */
	public double getCurrentZIncrement() { return currentZIncrement; }

	/**
	 * Returns the previous state
	 *
	 * @param lighted
	 * @return
	 */
	public boolean setObjectLighting(final boolean lighted) {
		if (objectCapture != null) { recordObjectCommand(() -> setObjectLighting(lighted)); }
		boolean previous = objectIsLighted;
		if (lighted != previous) {
			objectIsLighted = lighted;
			updateLightMode();
		}
		return previous;
	}

	/**
	 * Sets the display lighting.
	 *
	 * @param lighted
	 *            the lighted
	 * @return true, if successful
	 */
	public boolean setDisplayLighting(final boolean lighted) {
		boolean previous = displayIsLighted;
		if (lighted != previous) {
			displayIsLighted = lighted;
			updateLightMode();
		}
		return previous;
	}

	/**
	 * Update light mode. In core GL4, per-vertex fixed-function lighting (GL_LIGHTING) has been removed. Lighting is
	 * handled entirely in shaders; this method is intentionally empty.
	 */
	private void updateLightMode() {
		// GL_LIGHTING is not available in OpenGL 4 core profile.
		// Lighting state is passed to shaders via uniforms (e.g. in BasicShader).
	}

	/**
	 * Gets the lighting.
	 *
	 * @return the lighting
	 */
	public boolean getLighting() { return objectIsLighted && displayIsLighted; }

	/**
	 * Matrix mode.
	 *
	 * @param mode
	 *            the mode
	 */
	public void matrixMode(final int mode) {
		currentMatrixMode = mode;
	}

	/**
	 * Push matrix.
	 */
	public void pushMatrix() {
		if (objectCapture != null) { recordObjectCommand(this::pushMatrix); }
		getCurrentMatrixStack().pushMatrix();
	}

	/**
	 * Pop matrix.
	 */
	public void popMatrix() {
		if (objectCapture != null) { recordObjectCommand(this::popMatrix); }
		getCurrentMatrixStack().popMatrix();
	}

	/**
	 * Reset matrix.
	 *
	 * @param mode
	 *            the mode
	 */
	private void resetMatrix(final int mode) {
		matrixMode(mode);
		getCurrentMatrixStack().loadIdentity();
	}

	/**
	 * Push identity.
	 *
	 * @param mode
	 *            the mode
	 */
	public void pushIdentity(final int mode) {
		matrixMode(mode);
		pushMatrix();
		getCurrentMatrixStack().loadIdentity();
	}

	/**
	 * Pop.
	 *
	 * @param mode
	 *            the mode
	 */
	public void pop(final int mode) {
		matrixMode(mode);
		popMatrix();
	}

	/**
	 * Push.
	 *
	 * @param mode
	 *            the mode
	 */
	public void push(final int mode) {
		matrixMode(mode);
		pushMatrix();
	}

	/**
	 * Enable.
	 *
	 * @param state
	 *            the state
	 */
	public void enable(final int state) {
		// removed glEnableClientState
	}

	/**
	 * Disable.
	 *
	 * @param state
	 *            the state
	 */
	public void disable(final int state) {
		// removed glDisableClientState
	}

	/** The Constant INITIAL_BUFFER_SIZE. */
	private static final int INITIAL_BUFFER_SIZE = 1024 * 64; // 64k floats

	/** The current vertices. */
	private FloatBuffer currentVertices = Buffers.newDirectFloatBuffer(INITIAL_BUFFER_SIZE);

	/** The current colors. */
	private FloatBuffer currentColors = Buffers.newDirectFloatBuffer(INITIAL_BUFFER_SIZE);

	/** The current tex coords. */
	private FloatBuffer currentTexCoords = Buffers.newDirectFloatBuffer(INITIAL_BUFFER_SIZE);

	/** The current normals (one vec3 per vertex, replicated from the face normal set by {@link #outputNormal}). */
	private FloatBuffer currentNormals = Buffers.newDirectFloatBuffer(INITIAL_BUFFER_SIZE);

	/** The current draw style. */
	private int currentDrawStyle = GL4.GL_TRIANGLES;

	/** The current vertex count. */
	private int currentVertexCount = 0;

	private boolean currentDrawHasTransparency;
	private boolean currentDrawUsesColorBuffer;
	private float drawRed, drawGreen, drawBlue, drawAlpha;

	/** The vao id. */
	private int vaoId = -1;

	/** The vbo ids. Index 0 = positions, 1 = colours, 2 = tex-coords, 3 = normals. */
	private final int[] vboIds = new int[4];

	private final PolygonTessellationCache polygonTessellations = new PolygonTessellationCache();
	private record PolygonDrawingKey(PolygonTessellationCache.Tessellation mesh, double x, double y, double z,
			CoordinateDrawingKey textureMapping) {}
	private final Cache<PolygonDrawingKey, List<StoredDrawing>> polygonDrawings = CacheBuilder.newBuilder()
			.maximumWeight(32 * 1024 * 1024)
			.weigher((PolygonDrawingKey key, List<StoredDrawing> value) -> key.mesh().weight() * 4)
			.removalListener((RemovalNotification<PolygonDrawingKey, List<StoredDrawing>> notification) ->
					notification.getValue().forEach(StoredDrawing::dispose)).build();
	private final org.joml.Matrix4f identityViewMatrix = new org.joml.Matrix4f();
	private List<VertexData> drawingCapture;
	private boolean coordinateStreaming;
	private boolean suppressDynamicGeometryRetention;
	private MapBatch mapBatch;
	private MapBatch reusableMapBatch;

	private static final class CoordinateDrawingKey {
		final int style;
		final double[] values;
		final int hash;

		CoordinateDrawingKey(final int style, final double[] values) {
			this.style = style;
			this.values = values;
			hash = 31 * style + Arrays.hashCode(values);
		}

		int weight() { return 128 + values.length * Double.BYTES; }

		@Override
		public int hashCode() { return hash; }

		@Override
		public boolean equals(final Object other) {
			return other instanceof CoordinateDrawingKey key && style == key.style && Arrays.equals(values, key.values);
		}
	}

	private final Cache<CoordinateDrawingKey, List<StoredDrawing>> coordinateDrawings = CacheBuilder.newBuilder()
			.maximumWeight(32 * 1024 * 1024)
			.weigher((CoordinateDrawingKey key, List<StoredDrawing> value) -> key.weight() * 4)
			.removalListener((RemovalNotification<CoordinateDrawingKey, List<StoredDrawing>> notification) ->
					notification.getValue().forEach(StoredDrawing::dispose)).build();
	private final Cache<CoordinateDrawingKey, Boolean> coordinateCandidates = CacheBuilder.newBuilder()
			.maximumWeight(4 * 1024 * 1024)
			.weigher((CoordinateDrawingKey key, Boolean value) -> key.weight()).build();

	private static final class VertexData {
		final int style;
		final float[] positions, normals, textureCoordinates;
		final int hash;

		VertexData(final int style, final FloatBuffer positions, final FloatBuffer normals,
				final FloatBuffer textureCoordinates) {
			this.style = style;
			this.positions = snapshot(positions);
			this.normals = snapshot(normals);
			this.textureCoordinates = snapshot(textureCoordinates);
			hash = 31 * (31 * (31 * style + Arrays.hashCode(this.positions)) + Arrays.hashCode(this.normals))
					+ Arrays.hashCode(this.textureCoordinates);
		}

		private static float[] snapshot(final FloatBuffer source) {
			final FloatBuffer data = source.duplicate();
			data.flip();
			final float[] values = new float[data.remaining()];
			data.get(values);
			return values;
		}

		int weight() { return 128 + Float.BYTES * (positions.length + normals.length + textureCoordinates.length); }

		@Override
		public int hashCode() { return hash; }

		@Override
		public boolean equals(final Object other) {
			return other instanceof VertexData data && style == data.style
					&& Arrays.equals(positions, data.positions) && Arrays.equals(normals, data.normals)
					&& Arrays.equals(textureCoordinates, data.textureCoordinates);
		}
	}

	private static final int RETAINED_DRAWING_BYTES = 32 * 1024 * 1024;

	// Eviction and disposal run on the owning GL thread.
	private final Cache<VertexData, StoredDrawing> retainedDrawings = CacheBuilder.newBuilder()
			.maximumWeight(RETAINED_DRAWING_BYTES)
			.weigher((VertexData key, StoredDrawing value) -> key.weight() * 2)
			.removalListener((RemovalNotification<VertexData, StoredDrawing> notification) ->
					notification.getValue().dispose()).build();
	private final Cache<VertexData, Boolean> drawingCandidates = CacheBuilder.newBuilder()
			.maximumWeight(8 * 1024 * 1024)
			.weigher((VertexData key, Boolean value) -> key.weight()).build();

	/** Immutable vertex data with per-instance drawing state, owned by this GL context. */
	public final class StoredDrawing {
		private int references = 1;
		private final int weight;
		private final float[] linePositions, lineNormals;
		private final int[] capturedIndices;
		private final int[] vao = new int[1];
		private final int[] buffers;
		private final int count;
		private final int style;
		private final boolean hasTextureCoordinates;
		private final boolean indexed;
		private final boolean uniformNormal;
		private final float faceNormalX, faceNormalY, faceNormalZ;
		private final double normalX, normalY, normalZ;

		private StoredDrawing() {
			this(currentDrawStyle, currentVertices, currentNormals, currentTexCoords, null);
		}

		private StoredDrawing(final int drawStyle, final FloatBuffer positions, final FloatBuffer normals,
				final FloatBuffer textureCoordinates, final IntBuffer indices) {
			indexed = indices != null;
			weight = Math.toIntExact(128L + (long) Float.BYTES
					* (positions.position() + normals.position() + textureCoordinates.position())
					+ (indices == null ? 0L : (long) Integer.BYTES * indices.position())
					+ (isLineStyle(drawStyle) || roadCapture != null
							? (long) Float.BYTES * (positions.position() + normals.position()) : 0L)
					+ (roadCapture != null && indices != null ? (long) Integer.BYTES * indices.position() : 0L));
			count = indexed ? indices.position() : positions.position() / 3;
			style = drawStyle;
			linePositions = isLineStyle(style) || roadCapture != null ? VertexData.snapshot(positions) : null;
			lineNormals = isLineStyle(style) || roadCapture != null ? VertexData.snapshot(normals) : null;
			if (roadCapture != null && indices != null) {
				final IntBuffer elements = indices.duplicate();
				elements.flip();
				capturedIndices = new int[elements.remaining()];
				elements.get(capturedIndices);
			} else {
				capturedIndices = null;
			}
			hasTextureCoordinates = textureCoordinates.position() > 0;
			normalX = currentNormal.getX();
			normalY = currentNormal.getY();
			normalZ = currentNormal.getZ();
			uniformNormal = hasUniformNormal(normals);
			faceNormalX = uniformNormal ? normals.get(0) : 0;
			faceNormalY = uniformNormal ? normals.get(1) : 0;
			faceNormalZ = uniformNormal ? normals.get(2) : 1;
			final int attributeBuffers = 1 + (uniformNormal ? 0 : 1) + (hasTextureCoordinates ? 1 : 0);
			buffers = new int[attributeBuffers + (indexed ? 1 : 0)];
			gl.glGenVertexArrays(1, vao, 0);
			gl.glGenBuffers(buffers.length, buffers, 0);
			gl.glBindVertexArray(vao[0]);
			upload(0, 0, 3, positions);
			int nextBuffer = 1;
			if (uniformNormal) {
				gl.glDisableVertexAttribArray(3);
			} else {
				upload(nextBuffer++, 3, 3, normals);
			}
			if (hasTextureCoordinates) { upload(nextBuffer, 2, 2, textureCoordinates); }
			if (indexed) {
				final IntBuffer data = indices.duplicate();
				data.flip();
				gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, buffers[attributeBuffers]);
				gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, (long) data.remaining() * Integer.BYTES, data,
						GL.GL_STATIC_DRAW);
				renderer.getDiagnostics().genericUpload((long) data.remaining() * Integer.BYTES);
			}
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
			gl.glBindVertexArray(0);
		}

		private boolean hasUniformNormal(final FloatBuffer normals) {
			if (normals.position() < 3 || normals.position() % 3 != 0) return false;
			final float x = normals.get(0), y = normals.get(1), z = normals.get(2);
			for (int i = 3; i < normals.position(); i += 3) {
				if (normals.get(i) != x || normals.get(i + 1) != y || normals.get(i + 2) != z) return false;
			}
			return true;
		}

		private void upload(final int bufferIndex, final int attribute, final int components, final FloatBuffer source) {
			final FloatBuffer data = source.duplicate();
			data.flip();
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers[bufferIndex]);
			gl.glBufferData(GL.GL_ARRAY_BUFFER, (long) data.remaining() * Float.BYTES, data, GL.GL_STATIC_DRAW);
			renderer.getDiagnostics().genericUpload((long) data.remaining() * Float.BYTES);
			gl.glVertexAttribPointer(attribute, components, GL.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(attribute);
		}

		public void draw() {
			if (count == 0 || basicShader == null) return;
			if (mapBatch != null) { mapBatch.flush(); }
			if (instanceBatch != null) {
				if (instanceBatch.add(this)) {
					outputNormal(normalX, normalY, normalZ);
					return;
				}
				instanceBatch.flush();
			}
			if (roadCapture != null) { roadCapture.add(this); }
			if (objectCapture != null && objectCapture.supported) {
				if (!objectCapture.normalDefined) { objectCapture.inheritedNormal = true; }
				objectCapture.retain(this);
				recordObjectCommand(this::draw);
			}
			basicShader.start();
			gl.glBindVertexArray(vao[0]);
			if (uniformNormal) { gl.glVertexAttrib3f(3, faceNormalX, faceNormalY, faceNormalZ); }
			gl.glDisableVertexAttribArray(1);
			final float alpha = (float) (currentColor.alpha() / 255.0 * currentObjectAlpha);
			gl.glVertexAttrib4f(1, currentColor.red() / 255f, currentColor.green() / 255f,
					currentColor.blue() / 255f, alpha);
			basicShader.loadModelMatrix(modelViewMatrix.getCurrentMatrix());
			basicShader.loadViewMatrix(identityViewMatrix);
			basicShader.loadProjectionMatrix(projectionMatrix.getCurrentMatrix());
			basicShader.loadUseTexture(hasTextureCoordinates);
			basicShader.loadPremultiplyTexture(hasTextureCoordinates);
			loadPicking(basicShader, 1f);
			basicShader.loadUseLighting(getLighting());
			final boolean pickingPass = renderer.getPickingHelper().isBeginningPicking();
			if (!pickingPass) { gl.glEnable(GL.GL_BLEND); }
			gl.glBlendFunc(hasTextureCoordinates ? GL.GL_ONE : GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			final byte[] previousDepthMask = new byte[1];
			if (alpha < 1f && !pickingPass) {
				gl.glGetBooleanv(GL.GL_DEPTH_WRITEMASK, previousDepthMask, 0);
				gl.glDepthMask(false);
			}
			try {
				if (indexed) {
					gl.glDrawElements(style, count, GL.GL_UNSIGNED_INT, 0L);
				} else {
					gl.glDrawArrays(style, 0, count);
				}
				renderer.getDiagnostics().genericDraw(0);
			} finally {
				if (alpha < 1f && !pickingPass) { gl.glDepthMask(previousDepthMask[0] != 0); }
				if (hasTextureCoordinates) { gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); }
				gl.glBindVertexArray(0);
				basicShader.stop();
				outputNormal(normalX, normalY, normalZ);
			}
		}

		public void dispose() {
			if (--references > 0) return;
			if (references < 0) throw new IllegalStateException("Retained drawing released twice");
			gl.glDeleteBuffers(buffers.length, buffers, 0);
			gl.glDeleteVertexArrays(1, vao, 0);
		}
	}

	/** Retain the vertices accumulated since the last beginDrawing call. */
	public StoredDrawing storeDrawing() { return new StoredDrawing(); }

	/** Capture constant-color geometry only; matrices and other GL state must be applied by the caller. */
	public List<StoredDrawing> captureDrawing(final Runnable producer) {
		if (drawingCapture != null) throw new IllegalStateException("Nested geometry capture");
		final List<VertexData> data = new ArrayList<>();
		final List<StoredDrawing> result = new ArrayList<>();
		drawingCapture = data;
		try {
			producer.run();
			if (!data.isEmpty() && data.stream().allMatch(part -> (part.style == GL.GL_TRIANGLE_STRIP
					|| part.style == GL.GL_TRIANGLE_FAN || part.style == GL.GL_TRIANGLES)
					&& part.normals.length == part.positions.length
					&& (part.textureCoordinates.length == 0
							|| part.textureCoordinates.length == part.positions.length / 3 * 2)
					&& (part.textureCoordinates.length > 0) == (data.getFirst().textureCoordinates.length > 0))) {
				result.add(storeTriangles(data));
			} else {
				for (final VertexData part : data) { result.add(storeVertices(part)); }
			}
			return List.copyOf(result);
		} catch (RuntimeException | Error e) {
			result.forEach(StoredDrawing::dispose);
			throw e;
		} finally {
			drawingCapture = null;
		}
	}

	private StoredDrawing storeVertices(final VertexData data) {
		final FloatBuffer positions = Buffers.newDirectFloatBuffer(data.positions.length);
		final FloatBuffer normals = Buffers.newDirectFloatBuffer(data.normals.length);
		final FloatBuffer tex = Buffers.newDirectFloatBuffer(data.textureCoordinates.length);
		positions.put(data.positions);
		normals.put(data.normals);
		tex.put(data.textureCoordinates);
		return new StoredDrawing(data.style, positions, normals, tex, null);
	}

	private StoredDrawing storeTriangles(final List<VertexData> strips) {
		final int vertices = Math.toIntExact(strips.stream().mapToLong(part -> part.positions.length / 3).sum());
		final int indices = Math.toIntExact(strips.stream()
				.mapToLong(part -> part.style == GL.GL_TRIANGLES ? part.positions.length / 9 * 3L
						: Math.max(0, part.positions.length / 3 - 2) * 3L).sum());
		final FloatBuffer positions = Buffers.newDirectFloatBuffer(Math.multiplyExact(vertices, 3));
		final FloatBuffer normals = Buffers.newDirectFloatBuffer(Math.multiplyExact(vertices, 3));
		final FloatBuffer tex = Buffers.newDirectFloatBuffer(strips.getFirst().textureCoordinates.length == 0
				? 0 : Math.multiplyExact(vertices, 2));
		final IntBuffer elements = Buffers.newDirectIntBuffer(indices);
		int base = 0;
		for (final VertexData strip : strips) {
			positions.put(strip.positions);
			normals.put(strip.normals);
			tex.put(strip.textureCoordinates);
			final int count = strip.positions.length / 3;
			if (strip.style == GL.GL_TRIANGLES) {
				for (int i = 0; i < count - count % 3; i++) { elements.put(base + i); }
			} else {
				for (int i = 2; i < count; i++) {
					elements.put(strip.style == GL.GL_TRIANGLE_FAN ? base : base + i - 2 + (i & 1));
					elements.put(strip.style == GL.GL_TRIANGLE_FAN ? base + i - 1 : base + i - 1 - (i & 1));
					elements.put(base + i);
				}
			}
			base += count;
		}
		return new StoredDrawing(GL.GL_TRIANGLES, positions, normals, tex, elements);
	}

	/**
	 * Ensure capacity.
	 *
	 * @param buffer
	 *            the buffer
	 * @param elementsToAdd
	 *            the elements to add
	 */
	private FloatBuffer ensureCapacity(final FloatBuffer buffer, final int elementsToAdd) {
		if (buffer.remaining() >= elementsToAdd) return buffer;
		final int required = Math.addExact(buffer.position(), elementsToAdd);
		final int capacity = (int) Math.min(Integer.MAX_VALUE,
				Math.max((long) required, (long) buffer.capacity() * 2));
		final FloatBuffer expanded = Buffers.newDirectFloatBuffer(capacity);
		buffer.flip();
		expanded.put(buffer);
		return expanded;
	}

	/**
	 * Begin drawing.
	 *
	 * @param style
	 *            the style
	 */
	public void beginDrawing(final int style) {
		currentDrawStyle = style;
		currentVertices.clear();
		currentColors.clear();
		currentTexCoords.clear();
		currentNormals.clear();
		currentVertexCount = 0;
		currentDrawHasTransparency = false;
		currentDrawUsesColorBuffer = false;
	}

	/** Stream transient triangle batches without admitting them to the retained geometry cache. */
	public void streamTriangles(final Runnable drawing) {
		final boolean previous = coordinateStreaming;
		coordinateStreaming = true;
		try {
			drawing.run();
		} finally {
			coordinateStreaming = previous;
		}
	}

	private boolean hasUniformColor() {
		final float red = (float) (currentColor.red() / 255.0), green = (float) (currentColor.green() / 255.0),
				blue = (float) (currentColor.blue() / 255.0);
		final float alpha = (float) (currentColor.alpha() / 255.0 * currentObjectAlpha);
		return !currentDrawUsesColorBuffer && drawRed == red && drawGreen == green && drawBlue == blue
				&& drawAlpha == alpha;
	}

	/**
	 * End drawing.
	 */
	public void endDrawing() {
		if (currentVertices.position() == 0 || basicShader == null || vaoId < 0) return;
		if (drawingCapture != null) {
			drawingCapture.add(new VertexData(currentDrawStyle, currentVertices, currentNormals, currentTexCoords));
			return;
		}
		final boolean uniformColor = hasUniformColor();
		if (mapBatch != null) {
			if (uniformColor && currentTexCoords.position() == 0
					&& mapBatch.add(currentDrawStyle, currentVertices, currentNormals)) return;
			mapBatch.flush();
		}
		if (roadCapture != null && !uniformColor) { roadCapture.supported = false; }
		if (objectCapture != null && objectCapture.supported) {
			if (uniformColor && currentNormals.position() == currentVertices.position()
					&& (currentTexCoords.position() == 0 || currentTexCoords.position() == currentVertexCount * 2)) {
				final StoredDrawing drawing = storeDrawing();
				try {
					drawing.draw();
				} finally {
					drawing.dispose();
				}
				return;
			}
			objectCapture.supported = false;
		}
		if (mapBatch == null && !coordinateStreaming && uniformColor && (currentTexCoords.position() == 0
				|| currentTexCoords.position() == currentVertexCount * 2)) {
			final VertexData key = new VertexData(currentDrawStyle, currentVertices, currentNormals, currentTexCoords);
			if (key.weight() <= RETAINED_DRAWING_BYTES / 2) {
				StoredDrawing drawing = retainedDrawings.getIfPresent(key);
				if (drawing == null && drawingCandidates.getIfPresent(key) != null) {
					drawingCandidates.invalidate(key);
					drawing = storeDrawing();
					retainedDrawings.put(key, drawing);
				}
				if (drawing != null) {
					final double x = currentNormal.getX(), y = currentNormal.getY(), z = currentNormal.getZ();
					try {
						drawing.draw();
					} finally {
						outputNormal(x, y, z);
					}
					return;
				}
				// A changing mesh should stream through shared VBOs, not allocate new GPU objects.
				drawingCandidates.put(key, Boolean.TRUE);
			}
		}
		if (roadCapture != null) { roadCapture.supported = false; }
		basicShader.start();
		gl.glBindVertexArray(vaoId);

		// --- vertices (location 0) ---
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
		currentVertices.flip();
		gl.glBufferData(GL.GL_ARRAY_BUFFER, (long) currentVertices.limit() * 4, currentVertices, GL.GL_DYNAMIC_DRAW);
		gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// --- colors (location 1) ---
		if (uniformColor) {
			gl.glDisableVertexAttribArray(1);
			gl.glVertexAttrib4f(1, (float) (currentColor.red() / 255.0), (float) (currentColor.green() / 255.0),
					(float) (currentColor.blue() / 255.0),
					(float) (currentColor.alpha() / 255.0 * currentObjectAlpha));
		} else {
			if (!currentDrawUsesColorBuffer) { materializeDrawColors(currentVertexCount); }
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[1]);
			currentColors.flip();
			gl.glBufferData(GL.GL_ARRAY_BUFFER, (long) currentColors.limit() * Float.BYTES, currentColors,
					GL.GL_DYNAMIC_DRAW);
			gl.glVertexAttribPointer(1, 4, GL.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
		}

		// --- tex coords (location 2) ---
		boolean hasTex = currentTexCoords.position() > 0;
		if (hasTex) {
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[2]);
			currentTexCoords.flip();
			gl.glBufferData(GL.GL_ARRAY_BUFFER, (long) currentTexCoords.limit() * 4, currentTexCoords,
					GL.GL_DYNAMIC_DRAW);
			gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(2);
		} else {
			gl.glDisableVertexAttribArray(2);
		}

		// --- normals (location 3) ---
		boolean hasNormals = currentNormals.position() > 0 && getLighting();
		if (hasNormals) {
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[3]);
			currentNormals.flip();
			gl.glBufferData(GL.GL_ARRAY_BUFFER, (long) currentNormals.limit() * 4, currentNormals,
					GL.GL_DYNAMIC_DRAW);
			gl.glVertexAttribPointer(3, 3, GL.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(3);
		} else {
			gl.glDisableVertexAttribArray(3);
			// Provide a default up-facing normal so the shader doesn't read garbage
			gl.glVertexAttrib3f(3, 0.0f, 0.0f, 1.0f);
		}

		basicShader.loadModelMatrix(modelViewMatrix.getCurrentMatrix());
		// Pass the projection matrix; the model-view matrix already contains camera+model transforms.
		basicShader.loadViewMatrix(identityViewMatrix);
		basicShader.loadProjectionMatrix(projectionMatrix.getCurrentMatrix());
		basicShader.loadUseTexture(hasTex);
		basicShader.loadPremultiplyTexture(hasTex);
		loadPicking(basicShader, 1f);

		// --- lighting uniforms ---
		basicShader.loadUseLighting(hasNormals);

		// TextureCache2 uploads straight-alpha pixels. Like legacy OpenGL, textured draws use GL_ONE so that
		// object transparency reveals the background without dimming the texture; the shader premultiplies
		// the texel alpha to avoid fringes.
		final boolean pickingPass = renderer.getPickingHelper().isBeginningPicking();
		if (!pickingPass) { gl.glEnable(GL.GL_BLEND); }
		gl.glBlendFunc(hasTex ? GL.GL_ONE : GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		// For transparent draws, disable depth-buffer writes so that objects behind a
		// semi-transparent surface remain visible (depth *testing* is kept on).
		final byte[] previousDepthMask = new byte[1];
		if (currentDrawHasTransparency && !pickingPass) {
			gl.glGetBooleanv(GL.GL_DEPTH_WRITEMASK, previousDepthMask, 0);
			gl.glDepthMask(false);
		}
		try {
			gl.glDrawArrays(currentDrawStyle, 0, currentVertices.limit() / 3);
			renderer.getDiagnostics().genericDraw((long) Float.BYTES * (currentVertices.limit()
					+ (uniformColor ? 0 : currentColors.limit())
					+ (hasTex ? currentTexCoords.limit() : 0) + (hasNormals ? currentNormals.limit() : 0)));
		} finally {
			if (currentDrawHasTransparency && !pickingPass) { gl.glDepthMask(previousDepthMask[0] != 0); }
			if (hasTex) { gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); }
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
			gl.glBindVertexArray(0);
			basicShader.stop();
		}
	}

	/**
	 * Translate by Y negated.
	 *
	 * @param p
	 *            the p
	 */
	public void translateByYNegated(final IPoint p) {
		translateBy(p.getX(), -p.getY(), p.getZ());
	}

	/**
	 * Translate by.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public void translateBy(final double x, final double y, final double z) {
		if (objectCapture != null) { recordObjectCommand(() -> translateBy(x, y, z)); }
		getCurrentMatrixStack().translate(x, y, z);
	}

	/**
	 * Translate by.
	 *
	 * @param ordinates
	 *            the ordinates
	 */
	public void translateBy(final double... ordinates) {
		switch (ordinates.length) {
			case 0:
				return;
			case 1:
				translateBy(ordinates[0], 0, 0);
				break;
			case 2:
				translateBy(ordinates[0], ordinates[1], 0);
				break;
			default:
				translateBy(ordinates[0], ordinates[1], ordinates[2]);
		}
	}

	/**
	 * Translate by.
	 *
	 * @param p
	 *            the p
	 */
	public void translateBy(final IPoint p) {
		translateBy(p.getX(), p.getY(), p.getZ());
	}

	/**
	 * Rotate by.
	 *
	 * @param angle
	 *            the angle in degrees
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public void rotateBy(final double angle, final double x, final double y, final double z) {
		if (objectCapture != null) { recordObjectCommand(() -> rotateBy(angle, x, y, z)); }
		if (x == 0d && y == 0d && z == 0d) {
			getCurrentMatrixStack().rotate(angle, 0, 0, 1);
		} else {
			getCurrentMatrixStack().rotate(angle, x, y, z);
		}
	}

	/**
	 * Rotate by.
	 *
	 * @param rotation
	 *            the rotation
	 */
	public void rotateBy(final Rotation3D rotation) {
		final IPoint axis = rotation.getAxis();
		final double angle = rotation.getAngle() * Maths.toDeg;
		rotateBy(angle, axis.getX(), axis.getY(), axis.getZ());
	}

	/**
	 * Scale by.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public void scaleBy(final double x, final double y, final double z) {
		if (objectCapture != null) { recordObjectCommand(() -> scaleBy(x, y, z)); }
		// currentScale.setLocation(x, y, z);
		getCurrentMatrixStack().scale(x, y, z);
	}

	/**
	 * Scale by.
	 *
	 * @param scaling
	 *            the scaling
	 */
	public void scaleBy(final Scaling3D scaling) {
		scaleBy(scaling.getX(), scaling.getY(), scaling.getZ());
	}

	// DRAWING

	/**
	 * Draws an arbitrary shape using a set of vertices as input, computing the normal if necessary and drawing the
	 * contour if a border is present
	 *
	 * @param yNegatedVertices
	 *            the set of vertices to draw
	 * @param number
	 *            the number of vertices to draw. Either 3 (a triangle), 4 (a quad) or -1 (a polygon)
	 * @param solid
	 *            whether to draw the shape as a solid shape
	 * @param clockwise
	 *            whether to draw the shape in the clockwise direction (the vertices are always oriented clockwise)
	 * @param computeNormal
	 *            whether to compute the normal for this shape
	 * @param border
	 *            if not null, will be used to draw the contour
	 */
	public void drawSimpleShape(final ICoordinates yNegatedVertices, final int number, final boolean clockwise,
			final boolean computeNormal, final IColor border) {
		if (!isWireframe()) {
			if (computeNormal) { setNormal(yNegatedVertices, clockwise); }
			final int style = number == 4 ? GL.GL_TRIANGLE_FAN : number == -1 ? GL.GL_TRIANGLE_FAN : GL.GL_TRIANGLES;
			try (var offset = PolygonOffset.forOutline(this, border != null)) {
				drawVertices(style, yNegatedVertices, number, clockwise);
			}
		}
		if (border != null || isWireframe()) {
			final IColor colorToUse = border != null ? border : getCurrentColor();
			drawClosedLine(yNegatedVertices, colorToUse, -1);
		}
	}

	/**
	 * Triangulates an arbitrary {@link Polygon} (with optional holes) using the GLU tessellator and emits the resulting
	 * triangles via the {@link ITesselator} callbacks ({@link #beginDrawing}/{@link #drawVertex}/{@link #endDrawing}).
	 *
	 * <p>
	 * This mirrors the implementation in {@code gama.ui.display.opengl.OpenGL} and replaces the former
	 * {@link FastTriangulation} pure-Java ear-clipping path, which did not handle complex concave polygons reliably.
	 * The GLU tessellator is a robust, well-tested C-backed implementation that correctly handles concave contours,
	 * self-intersections, and multiple holes.
	 * </p>
	 *
	 * <p>
	 * The tessellator fires {@code begin}/{@code vertex}/{@code end} callbacks (defined by {@link ITesselator}) which
	 * are dispatched to {@link #beginDrawing(int)}, {@link #drawVertex(int, double, double, double)}, and
	 * {@link #endDrawing()} respectively — writing directly into the VBO float buffers used by this renderer.
	 * </p>
	 *
	 * @param p
	 *            the JTS {@link Polygon} (provides the hole rings via {@link Polygon#getInteriorRingN(int)})
	 * @param yNegatedVertices
	 *            the outer ring coordinates, already Y-negated and in clockwise order
	 * @param clockwise
	 *            whether {@code yNegatedVertices} is in clockwise order
	 */
	public void drawPolygon(final Polygon p, final ICoordinates yNegatedVertices, final boolean clockwise) {
		setNormal(yNegatedVertices, clockwise);
		final var mesh = polygonTessellations.get(p, yNegatedVertices, renderer.getDiagnostics());
		if (mapBatch != null || drawingCapture != null || !mesh.isCacheable() || mesh.weight() > 8 * 1024 * 1024) {
			mesh.draw(this);
			return;
		}
		final CoordinateDrawingKey mapping;
		if (isTextured()) {
			final double[] values = new double[10];
			snapshotTextureMapping(values, 0);
			mapping = new CoordinateDrawingKey(0, values);
		} else {
			mapping = null;
		}
		final PolygonDrawingKey key = new PolygonDrawingKey(mesh, currentNormal.getX(), currentNormal.getY(),
				currentNormal.getZ(), mapping);
		List<StoredDrawing> drawings = polygonDrawings.getIfPresent(key);
		if (drawings == null) {
			drawings = captureDrawing(() -> mesh.draw(this));
			polygonDrawings.put(key, drawings);
		}
		for (final StoredDrawing drawing : drawings) { drawing.draw(); }
	}

	/**
	 * Draw closed line.
	 *
	 * @param yNegatedVertices
	 *            the y negated vertices
	 * @param number
	 *            the number
	 */
	public void drawClosedLine(final ICoordinates yNegatedVertices, final int number) {
		drawVertices(GL.GL_LINE_LOOP, yNegatedVertices, number, true);
	}

	/**
	 * Draw closed line.
	 *
	 * @param yNegatedVertices
	 *            the y negated vertices
	 * @param color
	 *            the color
	 * @param number
	 *            the number
	 */
	public void drawClosedLine(final ICoordinates yNegatedVertices, final IColor color, final int number) {
		if (color == null) return;
		final IColor previous = swapCurrentColor(color);
		drawClosedLine(yNegatedVertices, number);
		setCurrentColor(previous);
	}

	/**
	 * Draw line.
	 *
	 * @param yNegatedVertices
	 *            the y negated vertices
	 * @param number
	 *            the number
	 */
	public void drawLine(final ICoordinates yNegatedVertices, final int number) {
		// final boolean previous = this.setLighting(false);
		drawVertices(GL.GL_LINE_STRIP, yNegatedVertices, number, true);
		// this.setLighting(previous);
	}

	/**
	 * Outputs a single vertex to OpenGL.
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public void outputVertex(final double x, final double y, final double z) {
		if (objectCapture != null && !objectCapture.normalDefined) { objectCapture.inheritedNormal = true; }
		currentVertices = ensureCapacity(currentVertices, 3);
		currentVertices.put((float) x);
		currentVertices.put((float) y);
		currentVertices.put((float) z);
		final float red = (float) (currentColor.red() / 255.0), green = (float) (currentColor.green() / 255.0),
				blue = (float) (currentColor.blue() / 255.0);
		// Multiply the color's own alpha by the current object (layer) alpha so that
		// both per-color transparency and layer-level transparency are honoured.
		final float alpha = (float) (currentColor.alpha() / 255.0 * currentObjectAlpha);
		if (currentVertexCount == 0) {
			drawRed = red;
			drawGreen = green;
			drawBlue = blue;
			drawAlpha = alpha;
		} else if (!currentDrawUsesColorBuffer
				&& (red != drawRed || green != drawGreen || blue != drawBlue || alpha != drawAlpha)) {
			materializeDrawColors(currentVertexCount);
			currentDrawUsesColorBuffer = true;
		}
		if (currentDrawUsesColorBuffer) {
			currentColors = ensureCapacity(currentColors, 4);
			currentColors.put(red).put(green).put(blue).put(alpha);
		}
		currentDrawHasTransparency |= alpha < 1.0f;
		// Replicate the current face normal for this vertex
		currentNormals = ensureCapacity(currentNormals, 3);
		currentNormals.put((float) currentNormal.getX());
		currentNormals.put((float) currentNormal.getY());
		currentNormals.put((float) currentNormal.getZ());
		currentVertexCount++;
	}

	private void materializeDrawColors(final int count) {
		currentColors = ensureCapacity(currentColors, Math.multiplyExact(count, 4));
		for (int i = 0; i < count; i++) {
			currentColors.put(drawRed).put(drawGreen).put(drawBlue).put(drawAlpha);
		}
	}

	/**
	 * Output tex coord.
	 *
	 * @param u
	 *            the u
	 * @param v
	 *            the v
	 */
	public void outputTexCoord(final double u, final double v) {
		currentTexCoords = ensureCapacity(currentTexCoords, 2);
		currentTexCoords.put((float) u);
		currentTexCoords.put((float) v);
	}

	/**
	 * Output normal.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public void outputNormal(final double x, final double y, final double z) {
		if (objectCapture != null) { objectCapture.normalDefined = true; }
		// Store the current face normal; it is replicated into currentNormals for every
		// subsequent outputVertex call until the next outputNormal.
		currentNormal.setLocation(x, y, z);
	}

	/**
	 * Draw vertex.
	 *
	 * @param coords
	 *            the coords
	 * @param normal
	 *            the normal
	 * @param tex
	 *            the tex
	 */
	public void drawVertex(final IPoint coords, final IPoint normal, final IPoint tex) {
		if (normal != null) { outputNormal(normal.getX(), normal.getY(), normal.getZ()); }
		if (tex != null) { outputTexCoord(tex.getX(), tex.getY()); }
		outputVertex(coords.getX(), coords.getY(), coords.getZ());
	}

	/**
	 * Draw vertex.
	 *
	 * @param i
	 *            the i
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public void drawVertex(final int i, final double x, final double y, final double z) {
		if (isTextured()) {
			textureCoords.setLocation(x, y, z);
			if (GamaPreferences.Displays.OPENGL_TEXTURE_ORIENTATION.getValue()) {
				currentTextureRotation.applyTo(textureCoords);
			} else {
				nullTextureRotation.applyTo(textureCoords);
			}
			final double u = 1 - (textureCoords.getX() - textureEnvelope.getMinX()) / textureEnvelope.getWidth();
			final double v = (textureCoords.getY() - textureEnvelope.getMinY()) / textureEnvelope.getHeight();
			outputTexCoord(u, v);
		}
		outputVertex(x, y, z);
	}

	/**
	 * Draw vertices.
	 *
	 * @param style
	 *            the style
	 * @param yNegatedVertices
	 *            the y negated vertices
	 * @param number
	 *            the number
	 * @param clockwise
	 *            the clockwise
	 */
	public void drawVertices(final int style, final ICoordinates yNegatedVertices, final int number,
			final boolean clockwise) {
		final int count = number < 0 ? yNegatedVertices.size() : Math.min(number, yNegatedVertices.size());
		if (mapBatch == null && !suppressDynamicGeometryRetention && drawingCapture == null
				&& count > 0 && count <= 65536) {
			final CoordinateDrawingKey key = coordinateDrawingKey(style, yNegatedVertices, number, clockwise, count);
			List<StoredDrawing> drawings = coordinateDrawings.getIfPresent(key);
			if (drawings == null && coordinateCandidates.getIfPresent(key) != null) {
				coordinateCandidates.invalidate(key);
				drawings = captureDrawing(() -> drawVertices(style, yNegatedVertices, number, clockwise));
				coordinateDrawings.put(key, drawings);
			}
			if (drawings != null) {
				for (final StoredDrawing drawing : drawings) { drawing.draw(); }
				return;
			}
			coordinateCandidates.put(key, Boolean.TRUE);
		}
		final boolean previous = coordinateStreaming;
		coordinateStreaming = true;
		try {
			beginDrawing(style);
			yNegatedVertices.visit(glDrawer, number, clockwise);
			endDrawing();
		} finally {
			coordinateStreaming = previous;
		}
	}

	private CoordinateDrawingKey coordinateDrawingKey(final int style, final ICoordinates coordinates,
			final int number, final boolean clockwise, final int count) {
		final boolean textured = isTextured();
		final double[] values = new double[count * 3 + (textured ? 13 : 3)];
		// Preserve visitor order, which differs between bounded and unbounded reversed rings.
		final int[] cursor = { 0 };
		coordinates.visit((i, x, y, z) -> {
			values[cursor[0]++] = x;
			values[cursor[0]++] = y;
			values[cursor[0]++] = z;
		}, number, clockwise);
		int offset = count * 3;
		values[offset++] = currentNormal.getX();
		values[offset++] = currentNormal.getY();
		values[offset++] = currentNormal.getZ();
		if (textured) { snapshotTextureMapping(values, offset); }
		return new CoordinateDrawingKey(style, values);
	}

	private void snapshotTextureMapping(final double[] values, int offset) {
		values[offset++] = textureEnvelope.getMinX();
		values[offset++] = textureEnvelope.getMinY();
		values[offset++] = textureEnvelope.getWidth();
		values[offset++] = textureEnvelope.getHeight();
		final Rotation3D rotation = GamaPreferences.Displays.OPENGL_TEXTURE_ORIENTATION.getValue()
				? currentTextureRotation : nullTextureRotation;
		for (int axis = 0; axis < 3; axis++) {
			textureCoords.setLocation(axis == 0 ? 1 : 0, axis == 1 ? 1 : 0, axis == 2 ? 1 : 0);
			rotation.applyTo(textureCoords);
			values[offset++] = textureCoords.getX();
			values[offset++] = textureCoords.getY();
		}
	}

	/**
	 * Replaces the current color by the parameter, sets the alpha of the parameter to be the one of the current color,
	 * and returns the ex-current color
	 *
	 * @param iColor
	 *            a Color
	 * @return the previous current color
	 */
	public IColor swapCurrentColor(final IColor iColor) {
		final IColor old = currentColor;
		setCurrentColor(iColor, old == null ? 1 : old.alpha() / 255d);
		return old;
	}

	/**
	 * Sets the normal.
	 *
	 * @param yNegatedVertices
	 *            the y negated vertices
	 * @param clockwise
	 *            the clockwise
	 * @return the gama point
	 */
	public IPoint setNormal(final ICoordinates yNegatedVertices, final boolean clockwise) {
		yNegatedVertices.getNormal(clockwise, 1, currentNormal);
		outputNormal(currentNormal.getX(), currentNormal.getY(), currentNormal.getZ());
		if (isTextured()) { computeTextureCoordinates(yNegatedVertices, clockwise); }
		return currentNormal;
	}

	/**
	 * Compute texture coordinates.
	 *
	 * @param yNegatedVertices
	 *            the y negated vertices
	 * @param clockwise
	 *            the clockwise
	 */
	private void computeTextureCoordinates(final ICoordinates yNegatedVertices, final boolean clockwise) {
		workingVertices.setTo(yNegatedVertices);
		if (GamaPreferences.Displays.OPENGL_TEXTURE_ORIENTATION.getValue()) {
			currentTextureRotation.rotateToHorizontal(currentNormal,
					workingVertices.directionBetweenLastPointAndOrigin(), clockwise);
			workingVertices.applyRotation(currentTextureRotation);
		} else {
			workingVertices.applyRotation(nullTextureRotation);
		}

		workingVertices.getEnvelopeInto(textureEnvelope);
	}

	/**
	 * Sets the current color.
	 *
	 * @param c
	 *            the c
	 * @param alpha
	 *            the alpha
	 */
	public void setCurrentColor(final IColor c, final double alpha) {
		if (c == null) return;
		// When an explicit alpha is supplied (e.g. from a layer's setAlpha call) store the
		// product of the colour's own alpha and the caller-supplied alpha directly.  The
		// resulting stored alpha is then further multiplied by currentObjectAlpha in
		// outputVertex, so currentObjectAlpha is always applied exactly once.
		currentColor = GamaColorFactory.getWithDoubles(c.red() / 255d, c.green() / 255d, c.blue() / 255d,
				c.alpha() / 255d * alpha);
		final IColor color = currentColor;
		if (objectCapture != null) { recordObjectCommand(() -> setCurrentColor(color)); }
	}

	/**
	 * Sets the current color.
	 *
	 * @param c
	 *            the new current color
	 */
	public void setCurrentColor(final IColor c) {
		// Store the colour's own alpha as-is; currentObjectAlpha is applied later in
		// outputVertex so it is never applied more than once.
		if (c == null) return;
		currentColor = GamaColorFactory.getWithDoubles(c.red() / 255d, c.green() / 255d, c.blue() / 255d,
				c.alpha() / 255d);
		final IColor color = currentColor;
		if (objectCapture != null) { recordObjectCommand(() -> setCurrentColor(color)); }
	}

	/**
	 * Sets the current color.
	 *
	 * @param red
	 *            the red
	 * @param green
	 *            the green
	 * @param blue
	 *            the blue
	 * @param alpha
	 *            the alpha
	 */
	public void setCurrentColor(final double red, final double green, final double blue, final double alpha) {
		// Store the colour as-is. currentObjectAlpha is applied at the point of use
		// (outputVertex) so that it is never applied more than once regardless of which
		// setCurrentColor overload is used.
		currentColor = GamaColorFactory.getWithDoubles(Math.max(red, 0), Math.max(green, 0), Math.max(blue, 0), alpha);
		final IColor color = currentColor;
		if (objectCapture != null) { recordObjectCommand(() -> setCurrentColor(color)); }
	}

	/**
	 * Gets the current color.
	 *
	 * @return the current color
	 */
	public IColor getCurrentColor() { return currentColor; }

	// LINE WIDTH

	/**
	 * Sets the line width.
	 *
	 * @param width
	 *            the new line width
	 */
	public float setLineWidth(final float width) {
		if (!Float.isFinite(width) || width <= 0f) {
			throw new IllegalArgumentException("OpenGL4 line width must be finite and positive: " + width);
		}
		if (objectCapture != null) { recordObjectCommand(() -> setLineWidth(width)); }
		float previous = currentObjectLineWidth;
		if (width != currentObjectLineWidth) {
			currentObjectLineWidth = width;
			if (!lineWidthLimitsKnown) {
				gl.glGetFloatv(gl.glIsEnabled(GL.GL_LINE_SMOOTH) ? GL2GL3.GL_SMOOTH_LINE_WIDTH_RANGE
						: GL.GL_ALIASED_LINE_WIDTH_RANGE, lineWidthRange, 0);
				lineWidthLimitsKnown = true;
			}
			final float supportedWidth = Math.max(lineWidthRange[0], Math.min(width, lineWidthRange[1]));
			if (supportedWidth != width && mapBatch == null && !unsupportedLineWidthReported) {
				unsupportedLineWidthReported = true;
				DEBUG.ERR("OpenGL4 driver supports native line widths " + lineWidthRange[0] + ".."
						+ lineWidthRange[1] + "; requested " + width + " is rendered as " + supportedWidth);
			}
			if (supportedWidth != nativeLineWidth) {
				gl.glLineWidth(supportedWidth);
				nativeLineWidth = supportedWidth;
			}
		}
		return previous;
	}

	// ALPHA
	/**
	 * Between 0d (transparent) to 1d (opaque)
	 *
	 * @param alpha
	 */
	public final void setCurrentObjectAlpha(final double alpha) { currentObjectAlpha = alpha; }

	/**
	 * Gets the current object alpha.
	 *
	 * @return the current object alpha
	 */
	public double getCurrentObjectAlpha() { return currentObjectAlpha; }

	// TEXTURES
	private int neutralTexture;

	/**
	 * Sets the id of the textures to enable. If the first is equal to NO_TEXTURE, all textures are disabled. If the
	 * second is equal to NO_TEXTURE, then the first one is also bound to the second unit.
	 *
	 * @param t
	 *            the id of the texture to enable. NO_TEXTURE means disabling textures
	 */
	public void setCurrentTextures(final int t0, final int t1) {
		primaryTexture = t0;
		alternateTexture = t1;
		textured = t0 != NO_TEXTURE;
		if (textured) {
			enablePrimaryTexture();
		} else {
			disableTextures();
		}
	}

	/**
	 * Bind texture.
	 *
	 * @param texture
	 *            the texture
	 */
	public void bindTexture(final int texture) {
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
		if (currentTextureSmooth) { textureCache.ensureMipmaps(texture); }
		final boolean isAntiAlias = getData().isAntialias() || currentTextureSmooth;
		final int minFilter = currentTextureSmooth ? GL.GL_LINEAR_MIPMAP_LINEAR
				: isAntiAlias ? GL.GL_LINEAR : GL.GL_NEAREST;
		if (texture == lastBoundTexture && minFilter == lastTextureMinFilter) return;

		lastBoundTexture = texture;
		lastTextureMinFilter = minFilter;
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, minFilter);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, isAntiAlias ? GL.GL_LINEAR : GL.GL_NEAREST);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT,
				isAntiAlias ? Math.max(1f, ANISOTROPIC_LEVEL) : 1f);
	}

	/**
	 * Enable primary texture. Binds the primary texture. In core GL4 there is no GL_TEXTURE_2D enable/disable;
	 * texturing is active whenever a sampler uniform is bound in the shader.
	 */
	public void enablePrimaryTexture() {
		if (objectCapture != null) { recordObjectCommand(this::enablePrimaryTexture); }
		if (primaryTexture == NO_TEXTURE) return;
		bindTexture(primaryTexture);
	}

	/**
	 * Enable alternate texture. Binds the alternate texture.
	 */
	public void enableAlternateTexture() {
		if (objectCapture != null) { recordObjectCommand(this::enableAlternateTexture); }
		if (alternateTexture == NO_TEXTURE) return;
		bindTexture(alternateTexture);
	}

	/**
	 * Disable texture sampling, keeping a complete neutral texture bound for core-profile sampler validation.
	 */
	public void disableTextures() {
		if (objectCapture != null) { recordObjectCommand(this::disableTextures); }
		gl.glActiveTexture(GL.GL_TEXTURE0);
		if (neutralTexture == 0) {
			final int[] texture = new int[1];
			gl.glGenTextures(1, texture, 0);
			neutralTexture = texture[0];
			gl.glBindTexture(GL.GL_TEXTURE_2D, neutralTexture);
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, 1, 1, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
					Buffers.newDirectByteBuffer(new byte[] { -1, -1, -1, -1 }));
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		} else {
			gl.glBindTexture(GL.GL_TEXTURE_2D, neutralTexture);
		}
		textured = false;
	}

	/**
	 * Invalidate cached texture state.
	 */
	public void invalidateTextureCache() {
		lastBoundTexture = NO_TEXTURE;
	}

	/**
	 * Delete volatile textures.
	 */
	public void deleteVolatileTextures() {
		lastBoundTexture = NO_TEXTURE;
		textureCache.deleteVolatileTextures();
	}

	/**
	 * Cache texture.
	 *
	 * @param file
	 *            the file
	 */
	public void cacheTexture(final IImageProvider file) {
		if (file == null) return;
		textureCache.processs(file);
	}

	/**
	 * Gets the texture id.
	 *
	 * @param file
	 *            the file
	 * @param useCache
	 *            the use cache
	 * @return the texture id
	 */
	public int getTextureId(final IImageProvider file, final boolean useCache) {
		final Texture r = textureCache.getTexture(file, file.isAnimated(), useCache);
		if (r == null) return NO_TEXTURE;
		return r.getTextureObject();
	}

	/**
	 * Gets the texture id.
	 *
	 * @param img
	 *            the img
	 * @return the texture id
	 */
	public int getTextureId(final BufferedImage img) {
		final Texture r = textureCache.getTexture(img, currentTextureSmooth);
		if (r == null) return NO_TEXTURE;
		return r.getTextureObject();
	}

	/**
	 * Gets the texture.
	 *
	 * @param file
	 *            the file
	 * @param isAnimated
	 *            the is animated
	 * @param useCache
	 *            the use cache
	 * @return the texture
	 */
	public Texture getTexture(final IImageProvider file, final boolean isAnimated, final boolean useCache) {
		return textureCache.getTexture(file, isAnimated, useCache);
	}

	// GEOMETRIES

	/**
	 * Cache geometry.
	 *
	 * @param object
	 *            the object
	 */
	public void cacheGeometry(final GamaGeometryFile object) {
		geometryCache.process(object);
	}

	@Override
	public void cacheGeometry(final GamaGeometryFile object, final String key) {
		geometryCache.process(object, key);
	}

	@Override
	public IEnvelope getEnvelopeFor(final GamaGeometryFile object, final String key) {
		return geometryCache.getEnvelope(object, key);
	}

	/**
	 * Gets the envelope for.
	 *
	 * @param obj
	 *            the obj
	 * @return the envelope for
	 */
	public IEnvelope getEnvelopeFor(final Object obj) {
		if (obj instanceof GamaGeometryFile) return geometryCache.getEnvelope((GamaGeometryFile) obj);
		if (obj instanceof Geometry) return GamaEnvelopeFactory.of((Geometry) obj);
		return null;
	}

	// TEXT

	/**
	 * Draws one string in raster at the given coords and with the given font.
	 *
	 * <p>
	 * <b>GL4 core profile limitation:</b> GLUT bitmap strings rely on {@code glBitmap()} and {@code glRasterPos3d()},
	 * both removed from the OpenGL 4 core profile. This method is a no-op in the current implementation. Text that must
	 * be visible should be drawn via {@link gama.ui.display.opengl4.scene.text.TextDrawer} with
	 * {@code isPerspective = true}.
	 * </p>
	 *
	 * @param s
	 *            the string
	 * @param font
	 *            the GLUT font constant
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param z
	 *            z coordinate
	 */
	public void rasterText(final String s, final int font, final double x, final double y, final double z) {
		// No-op in GL4 core: glBitmap / glRasterPos3d have been removed.
	}

	/**
	 * Gets the world width.
	 *
	 * @return the world width
	 */
	public double getWorldWidth() { return getData().getEnvWidth(); }

	/**
	 * Gets the world height.
	 *
	 * @return the world height
	 */
	public double getWorldHeight() { return getData().getEnvHeight(); }

	/**
	 * Sets the display wireframe.
	 *
	 * @param wireframe
	 *            the new display wireframe
	 */
	public boolean setDisplayWireframe(final boolean wireframe) {
		boolean old = displayIsWireframe;
		if (old != wireframe) {
			displayIsWireframe = wireframe;
			updatePolygonMode();
		}
		return old;
	}

	/**
	 * Sets the object wireframe. Returns the previous value.
	 *
	 * @param wireframe
	 *            the new object wireframe
	 */
	public boolean setObjectWireframe(final boolean wireframe) {
		if (objectCapture != null) { recordObjectCommand(() -> setObjectWireframe(wireframe)); }
		boolean old = objectIsWireframe;
		if (old != wireframe) {
			objectIsWireframe = wireframe;
			updatePolygonMode();
		}
		return old;
	}

	/**
	 * Sets the polygon mode.
	 *
	 * @param wireframe
	 *            the new polygon mode
	 */
	public void updatePolygonMode() {
		int newPolygonMode = isWireframe() ? GL2GL3.GL_LINE : GL2GL3.GL_FILL;
		if (newPolygonMode != currentPolygonMode) {
			currentPolygonMode = newPolygonMode;
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, currentPolygonMode);
		}
	}

	/**
	 * Checks if is wireframe.
	 *
	 * @return true, if is wireframe
	 */
	public boolean isWireframe() { return displayIsWireframe || objectIsWireframe; }

	// PICKING

	/**
	 * Run with names.
	 *
	 * @param r
	 *            the r
	 */
	public void runWithNames(final Runnable r) {
		// gl.glInitNames();
		// gl.glPushName(0);
		r.run();
		// gl.glPopName();
	}

	/**
	 * Registers the object index for the colour-buffer picking pass.
	 *
	 * @param index
	 *            the drawing attributes index
	 */
	public void registerForSelection(final int index) {
		currentPickingId = ((gama.ui.display.opengl4.renderer.helpers.PickingHelper) renderer.getPickingHelper())
				.idFor(index);
	}

	// LISTS

	private Integer activeList;
	private ObjectCommands objectCapture;
	private RoadCapture roadCapture;
	private InstanceBatch instanceBatch;
	private int instanceBuffer;
	private FloatBuffer instanceData;
	private boolean suppressObjectRetention;
	private static final int MAX_RETAINED_LAYER_OBJECTS = 4096;
	private static final int MAX_RETAINED_DYNAMIC_LAYER_OBJECTS = 256;
	private static final int ROAD_CACHE_BYTES = 64 * 1024 * 1024;

	/**
	 * Consecutive instances share one primitive and one lighting state. Matrices are captured after all
	 * object and layer transforms have been applied, so neither the model nor traversal order changes.
	 */
	private final class InstanceBatch {
		private static final int MAX_INSTANCES = 4096;
		private static final int FLOATS_PER_INSTANCE = 20;
		private final FloatBuffer data;
		private StoredDrawing mesh;
		private boolean lighting;
		private int count;

		InstanceBatch() {
			if (instanceData == null) {
				instanceData = Buffers.newDirectFloatBuffer(MAX_INSTANCES * FLOATS_PER_INSTANCE);
			}
			data = instanceData;
			data.clear();
		}

		boolean add(final StoredDrawing drawing) {
			final float determinant = getModelViewMatrix().determinant();
			if (drawing.hasTextureCoordinates || drawing.style != GL.GL_TRIANGLES
					|| isWireframe() || isTextured() || currentObjectAlpha != 1d || currentColor.alpha() != 255
					|| !Float.isFinite(determinant) || Math.abs(determinant) < 1e-12f) return false;
			final boolean lit = getLighting();
			if (mesh != null && (mesh != drawing || lighting != lit || count == MAX_INSTANCES)) { flush(); }
			if (mesh == null) {
				mesh = drawing;
				mesh.references++;
			}
			lighting = lit;
			getModelViewMatrix().get(data.position(), data);
			data.position(data.position() + 16);
			data.put(currentColor.red() / 255f).put(currentColor.green() / 255f)
					.put(currentColor.blue() / 255f).put(1f);
			count++;
			return true;
		}

		void flush() {
			if (count == 0) return;
			final StoredDrawing pending = mesh;
			final int[] frontFace = new int[1];
			boolean shaderStarted = false, frontChanged = false, vaoBound = false;
			try {
				if (instanceBuffer == 0) {
					final int[] id = new int[1];
					gl.glGenBuffers(1, id, 0);
					instanceBuffer = id[0];
				}
				gl.glGetIntegerv(GL.GL_FRONT_FACE, frontFace, 0);
				data.flip();
				basicShader.start();
				shaderStarted = true;
				basicShader.loadProjectionMatrix(getProjectionMatrix());
				basicShader.loadUseTexture(false);
				basicShader.loadPremultiplyTexture(false);
				loadPicking(basicShader, 1f);
				basicShader.loadUseLighting(lighting);
				basicShader.loadInstanced(true);
				gl.glFrontFace(GL.GL_CCW);
				frontChanged = true;
				gl.glBindVertexArray(mesh.vao[0]);
				vaoBound = true;
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, instanceBuffer);
				final long bytes = (long) data.remaining() * Float.BYTES;
				gl.glBufferData(GL.GL_ARRAY_BUFFER, bytes, data, GL2GL3.GL_STREAM_DRAW);
				for (int column = 0; column < 4; column++) {
					final int location = 4 + column;
					gl.glVertexAttribPointer(location, 4, GL.GL_FLOAT, false, FLOATS_PER_INSTANCE * Float.BYTES,
							(long) column * 4 * Float.BYTES);
					gl.glEnableVertexAttribArray(location);
					gl.glVertexAttribDivisor(location, 1);
				}
				gl.glVertexAttribPointer(8, 4, GL.GL_FLOAT, false, FLOATS_PER_INSTANCE * Float.BYTES, 64L);
				gl.glEnableVertexAttribArray(8);
				gl.glVertexAttribDivisor(8, 1);
				if (mesh.uniformNormal) {
					gl.glVertexAttrib3f(3, mesh.faceNormalX, mesh.faceNormalY, mesh.faceNormalZ);
				}
				if (mesh.indexed) {
					gl.glDrawElementsInstanced(mesh.style, mesh.count, GL.GL_UNSIGNED_INT, 0L, count);
				} else {
					gl.glDrawArraysInstanced(mesh.style, 0, mesh.count, count);
				}
				renderer.getDiagnostics().genericDraw(bytes);
				renderer.getDiagnostics().instancedDraw(count);
			} finally {
				try {
					if (vaoBound) {
						for (int location = 4; location <= 8; location++) {
							gl.glVertexAttribDivisor(location, 0);
							gl.glDisableVertexAttribArray(location);
						}
						gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
						gl.glBindVertexArray(0);
					}
					if (frontChanged) { gl.glFrontFace(frontFace[0]); }
					if (shaderStarted) {
						basicShader.loadInstanced(false);
						basicShader.stop();
					}
				} finally {
					data.clear();
					mesh = null;
					count = 0;
					pending.dispose();
				}
			}
		}
	}

	private boolean canInstanceLayer(final AbstractObject<?, ?>[] objects) {
		if (objects.length < 2 || basicShader == null || instanceBatch != null || isWireframe()
				|| currentObjectAlpha != 1d || renderer.getPickingHelper().isPicking()) return false;
		for (final var object : objects) {
			final var attributes = object.getAttributes();
			if (object.type != DrawerType.GEOMETRY || object.isTextured() || attributes.isEmpty()
					|| attributes.getBorder() != null || attributes.getColor() == null
					|| attributes.getColor().alpha() != 255
					|| attributes.getType() != IShape.Type.SPHERE && attributes.getType() != IShape.Type.POINT)
				return false;
		}
		return true;
	}
	private final Cache<Integer, RoadDrawing> roadDrawings = CacheBuilder.newBuilder()
			.maximumWeight(ROAD_CACHE_BYTES)
			.weigher((Integer key, RoadDrawing drawing) -> drawing.weight)
			.removalListener((RemovalNotification<Integer, RoadDrawing> notification) ->
					notification.getValue().dispose()).build();

	private static boolean isLineStyle(final int style) {
		return style == GL.GL_LINES || style == GL.GL_LINE_STRIP || style == GL.GL_LINE_LOOP;
	}

	/** Bounded, renderer-owned streaming buffers; unlike retained maps, refreshed maps rebuild their vertices. */
	private final class MapBatch {
		private static final int MAX_VERTICES = 12288, MAX_DRAWINGS = 1024, FLOATS_PER_VERTEX = 15;
		private static final int[] RIBBON_CORNERS = { 0, 2, 1, 0, 3, 2 };
		private final FloatBuffer data = Buffers.newDirectFloatBuffer(MAX_VERTICES * FLOATS_PER_VERTEX);
		private final IntBuffer first = Buffers.newDirectIntBuffer(MAX_DRAWINGS);
		private final IntBuffer counts = Buffers.newDirectIntBuffer(MAX_DRAWINGS);
		private final int[] vao = new int[1], buffer = new int[1];
		private final int[] framebuffer = new int[4], polygonMode = new int[2];
		private final org.joml.Matrix4f matrix = new org.joml.Matrix4f(), inverse = new org.joml.Matrix4f(),
				relative = new org.joml.Matrix4f(), projection = new org.joml.Matrix4f();
		private final org.joml.Matrix3f normalMatrix = new org.joml.Matrix3f();
		private final org.joml.Vector3f point = new org.joml.Vector3f(), normal = new org.joml.Vector3f(),
				end = new org.joml.Vector3f();
		private final org.joml.Vector4f clip = new org.joml.Vector4f();
		private int style;
		private boolean ribbons;
		private float lineWidth;

		void begin() {
			matrix.set(getModelViewMatrix());
			projection.set(getProjectionMatrix());
			inverse.set(matrix).invert();
			gl.glGetIntegerv(GL.GL_VIEWPORT, framebuffer, 0);
			data.clear();
			first.clear();
			counts.clear();
		}

		boolean add(final int drawStyle, final FloatBuffer positions, final FloatBuffer normals) {
			final int vertices = positions.position() / 3;
			if (normals.position() != positions.position() || currentObjectAlpha != 1d
					|| currentColor.alpha() != 255 || isTextured()) return false;
			final boolean lines = isLineStyle(drawStyle);
			if (!lines && drawStyle != GL.GL_TRIANGLES && drawStyle != GL.GL_TRIANGLE_STRIP
					&& drawStyle != GL.GL_TRIANGLE_FAN) return false;
			if (drawStyle == GL.GL_LINES && vertices % 2 != 0
					|| drawStyle == GL.GL_TRIANGLES && vertices % 3 != 0) return false;
			inverse.mul(getModelViewMatrix(), relative);
			final float determinant = relative.determinant();
			if (!Float.isFinite(determinant) || Math.abs(determinant) < 1e-12f) return false;
			final boolean lit = getLighting();
			final boolean wide = lines && currentObjectLineWidth > 1f;
			if (lit) { relative.normal(normalMatrix); }
			// Validate before deferring any vertices, so an unsupported draw can use the ordinary path once.
			for (int i = 0; i < positions.position(); i += 3) {
				point.set(positions.get(i), positions.get(i + 1), positions.get(i + 2));
				if (!relative.transformPosition(point).isFinite()) return false;
				if (wide) {
					clip.set(point.x, point.y, point.z, 1f);
					matrix.transform(clip);
					projection.transform(clip);
					if (!clip.isFinite() || clip.w <= 0f) return false;
				}
				if (lit) {
					normal.set(normals.get(i), normals.get(i + 1), normals.get(i + 2));
					if (!normalMatrix.transform(normal).isFinite()) return false;
				}
			}
			final int mode = lines && !wide ? GL.GL_LINES : GL.GL_TRIANGLES;
			if (data.position() > 0 && (style != mode || ribbons != wide
					|| mode == GL.GL_LINES && lineWidth != currentObjectLineWidth)) { flush(); }
			style = mode;
			ribbons = wide;
			lineWidth = currentObjectLineWidth;
			final int primitives = lines ? drawStyle == GL.GL_LINES ? vertices / 2
					: Math.max(0, vertices - 1) + (drawStyle == GL.GL_LINE_LOOP && vertices > 1 ? 1 : 0)
					: drawStyle == GL.GL_TRIANGLES ? vertices / 3 : Math.max(0, vertices - 2);
			final int size = lines ? wide ? 6 : 2 : 3;
			int start = data.position() / FLOATS_PER_VERTEX;
			for (int primitive = 0; primitive < primitives; primitive++) {
				if (data.remaining() < size * FLOATS_PER_VERTEX || !counts.hasRemaining()) {
					finishDrawing(start);
					flush();
					start = 0;
				}
				if (lines) {
					final int a = drawStyle == GL.GL_LINES ? primitive * 2 : primitive;
					final int b = (a + 1) % vertices;
					if (wide) {
						for (final int corner : RIBBON_CORNERS) {
							putVertex(positions, normals, corner < 2 ? a : b, corner < 2 ? b : a,
									corner == 0 || corner == 2 ? lineWidth : -lineWidth, lit);
						}
					} else {
						putVertex(positions, normals, a, a, 0f, lit);
						putVertex(positions, normals, b, b, 0f, lit);
					}
				} else if (drawStyle == GL.GL_TRIANGLES) {
					for (int corner = 0; corner < 3; corner++) {
						putVertex(positions, normals, primitive * 3 + corner, 0, 0f, lit);
					}
				} else {
					putVertex(positions, normals, drawStyle == GL.GL_TRIANGLE_FAN ? 0
							: primitive + (primitive & 1), 0, 0f, lit);
					putVertex(positions, normals, drawStyle == GL.GL_TRIANGLE_FAN ? primitive + 1
							: primitive + 1 - (primitive & 1), 0, 0f, lit);
					putVertex(positions, normals, primitive + 2, 0, 0f, lit);
				}
			}
			finishDrawing(start);
			return true;
		}

		private void finishDrawing(final int start) {
			final int count = data.position() / FLOATS_PER_VERTEX - start;
			if (count > 0) {
				first.put(start);
				counts.put(count);
			}
		}

		private void putVertex(final FloatBuffer positions, final FloatBuffer normals, final int vertex,
				final int endpoint, final float width, final boolean lit) {
			final int offset = vertex * 3;
			point.set(positions.get(offset), positions.get(offset + 1), positions.get(offset + 2));
			relative.transformPosition(point);
			normal.set(normals.get(offset), normals.get(offset + 1), normals.get(offset + 2));
			if (lit) { normalMatrix.transform(normal); }
			if (width != 0f) {
				final int other = endpoint * 3;
				end.set(positions.get(other), positions.get(other + 1), positions.get(other + 2));
				relative.transformPosition(end);
			} else {
				end.set(point);
			}
			data.put(point.x).put(point.y).put(point.z);
			data.put(currentColor.red() / 255f).put(currentColor.green() / 255f)
					.put(currentColor.blue() / 255f).put(1f);
			data.put(normal.x).put(normal.y).put(normal.z);
			data.put(end.x).put(end.y).put(end.z).put(width).put(lit ? 1f : 0f);
		}

		void flush() {
			if (data.position() == 0) return;
			final boolean culling = gl.glIsEnabled(GL.GL_CULL_FACE);
			final float previousWidth = currentObjectLineWidth;
			boolean shaderStarted = false;
			gl.glGetIntegerv(GL2GL3.GL_POLYGON_MODE, polygonMode, 0);
			try {
				if (vao[0] == 0) {
					try {
						gl.glGenVertexArrays(1, vao, 0);
						gl.glGenBuffers(1, buffer, 0);
						gl.glBindVertexArray(vao[0]);
						gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer[0]);
						final int[] locations = { 0, 1, 3, 9, 10, 11 };
						final int[] components = { 3, 4, 3, 3, 1, 1 };
						int offset = 0;
						for (int i = 0; i < locations.length; i++) {
							gl.glVertexAttribPointer(locations[i], components[i], GL.GL_FLOAT, false,
									FLOATS_PER_VERTEX * Float.BYTES, (long) offset * Float.BYTES);
							gl.glEnableVertexAttribArray(locations[i]);
							offset += components[i];
						}
					} catch (RuntimeException | Error e) {
						dispose();
						throw e;
					}
				}
				basicShader.start();
				shaderStarted = true;
				basicShader.loadModelMatrix(matrix);
				basicShader.loadViewMatrix(identityViewMatrix);
				basicShader.loadProjectionMatrix(projection);
				basicShader.loadInstanced(false);
				basicShader.loadMapBatch(true, framebuffer[2], framebuffer[3]);
				basicShader.loadUseTexture(false);
				basicShader.loadPremultiplyTexture(false);
				basicShader.loadUseLighting(true);
				loadPicking(basicShader, 1f);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
				if (ribbons && culling) { gl.glDisable(GL.GL_CULL_FACE); }
				if (style == GL.GL_LINES) { setLineWidth(lineWidth); }
				gl.glBindVertexArray(vao[0]);
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer[0]);
				data.flip();
				first.flip();
				counts.flip();
				final long bytes = (long) data.remaining() * Float.BYTES;
				gl.glBufferData(GL.GL_ARRAY_BUFFER, bytes, data, GL2GL3.GL_STREAM_DRAW);
				gl.glMultiDrawArrays(style, first, counts, counts.remaining());
				renderer.getDiagnostics().genericDraw(bytes);
				renderer.getDiagnostics().mapBatch(counts.remaining());
			} finally {
				data.clear();
				first.clear();
				counts.clear();
				setLineWidth(previousWidth);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, polygonMode[0]);
				if (ribbons && culling) { gl.glEnable(GL.GL_CULL_FACE); }
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
				gl.glBindVertexArray(0);
				if (shaderStarted) {
					basicShader.loadMapBatch(false, 0, 0);
					basicShader.stop();
				}
			}
		}

		void dispose() {
			gl.glDeleteBuffers(1, buffer, 0);
			gl.glDeleteVertexArrays(1, vao, 0);
			buffer[0] = vao[0] = 0;
		}
	}

	private record RoadAppearance(AbstractObject<?, ?> object, IColor color, float lineWidth, boolean empty,
			boolean lighting) {
		RoadAppearance(final AbstractObject<?, ?> object) {
			this(object, object.getAttributes().getColor(), object.getAttributes().getLineWidth().floatValue(),
					object.getAttributes().isEmpty(), object.getAttributes().isLighting());
		}

		boolean matches(final AbstractObject<?, ?> other) {
			final var attributes = other.getAttributes();
			return object == other && color.equals(attributes.getColor())
					&& lineWidth == attributes.getLineWidth().floatValue() && empty == attributes.isEmpty()
					&& lighting == attributes.isLighting();
		}
	}

	private static final class RoadRun {
		final int style;
		final float lineWidth;
		final boolean lighting;
		final List<float[]> vertices = new ArrayList<>();
		final List<float[]> normals = new ArrayList<>();
		final List<IColor> colors = new ArrayList<>();
		int vertexCount;

		RoadRun(final int style, final float lineWidth, final boolean lighting) {
			this.style = style;
			this.lineWidth = lineWidth;
			this.lighting = lighting;
		}
	}

	private final class RoadCapture {
		final org.joml.Matrix4f inverse = new org.joml.Matrix4f(getModelViewMatrix()).invert();
		final org.joml.Matrix4f relative = new org.joml.Matrix4f();
		final org.joml.Matrix3f normalMatrix = new org.joml.Matrix3f();
		final org.joml.Vector3f point = new org.joml.Vector3f();
		final List<RoadRun> runs = new ArrayList<>();
		long bytes;
		boolean supported = true;

		void add(final StoredDrawing drawing) {
			if (!supported) return;
			if (drawing.linePositions == null || drawing.indexed && drawing.capturedIndices == null
					|| drawing.hasTextureCoordinates
					|| currentColor.alpha() != 255 || currentObjectAlpha != 1d) {
				supported = false;
				return;
			}
			final int vertexCount = drawing.linePositions.length / 3;
			final int[] elements;
			if (drawing.indexed) {
				elements = drawing.capturedIndices;
			} else if (drawing.style == GL.GL_TRIANGLE_FAN || drawing.style == GL.GL_TRIANGLE_STRIP) {
				elements = new int[Math.max(0, vertexCount - 2) * 3];
				int offset = 0;
				for (int i = 2; i < vertexCount; i++) {
					elements[offset++] = drawing.style == GL.GL_TRIANGLE_FAN ? 0 : i - 2 + (i & 1);
					elements[offset++] = drawing.style == GL.GL_TRIANGLE_FAN ? i - 1 : i - 1 - (i & 1);
					elements[offset++] = i;
				}
			} else if (isLineStyle(drawing.style) || drawing.style == GL.GL_TRIANGLES) {
				elements = null;
			} else {
				supported = false;
				return;
			}
			final int count = elements == null ? vertexCount : elements.length;
			bytes += (long) count * 10 * Float.BYTES + 128;
			if (bytes > ROAD_CACHE_BYTES) {
				supported = false;
				return;
			}
			if (drawing.lineNormals.length != drawing.linePositions.length) {
				supported = false;
				return;
			}
			final float[] positions = new float[count * 3], normals = new float[count * 3];
			for (int i = 0; i < count; i++) {
				final int source = (elements == null ? i : elements[i]) * 3;
				System.arraycopy(drawing.linePositions, source, positions, i * 3, 3);
				System.arraycopy(drawing.lineNormals, source, normals, i * 3, 3);
			}
			inverse.mul(getModelViewMatrix(), relative);
			if (getLighting()) { relative.normal(normalMatrix); }
			for (int i = 0; i < positions.length; i += 3) {
				point.set(positions[i], positions[i + 1], positions[i + 2]);
				relative.transformPosition(point);
				if (!point.isFinite()) {
					supported = false;
					return;
				}
				positions[i] = point.x;
				positions[i + 1] = point.y;
				positions[i + 2] = point.z;
				if (getLighting()) {
					point.set(normals[i], normals[i + 1], normals[i + 2]);
					normalMatrix.transform(point);
					if (!point.isFinite()) {
						supported = false;
						return;
					}
					normals[i] = point.x;
					normals[i + 1] = point.y;
					normals[i + 2] = point.z;
				}
			}
			RoadRun run = runs.isEmpty() ? null : runs.getLast();
			final int style = isLineStyle(drawing.style) ? drawing.style : GL.GL_TRIANGLES;
			if (run == null || run.style != style || run.lineWidth != currentObjectLineWidth
					|| run.lighting != getLighting()
					|| run.vertices.size() == 1024) {
				if (runs.size() == 128) {
					supported = false;
					return;
				}
				run = new RoadRun(style, currentObjectLineWidth, getLighting());
				runs.add(run);
			}
			run.vertices.add(positions);
			run.normals.add(normals);
			run.colors.add(currentColor);
			run.vertexCount += count;
		}
	}

	private final class RoadBuffer {
		final int[] vao = new int[1], buffers = new int[3];
		final int style;
		final float lineWidth;
		final boolean lighting;
		final IntBuffer first, count;

		RoadBuffer(final RoadRun run) {
			style = run.style;
			lineWidth = run.lineWidth;
			lighting = run.lighting;
			first = Buffers.newDirectIntBuffer(run.vertices.size());
			count = Buffers.newDirectIntBuffer(run.vertices.size());
			final FloatBuffer positions = Buffers.newDirectFloatBuffer(run.vertexCount * 3);
			final FloatBuffer colors = Buffers.newDirectFloatBuffer(run.vertexCount * 4);
			final FloatBuffer normals = Buffers.newDirectFloatBuffer(run.vertexCount * 3);
			int start = 0;
			for (int i = 0; i < run.vertices.size(); i++) {
				final float[] vertices = run.vertices.get(i);
				final IColor color = run.colors.get(i);
				first.put(start);
				count.put(vertices.length / 3);
				positions.put(vertices);
				normals.put(run.normals.get(i));
				for (int vertex = 0; vertex < vertices.length / 3; vertex++) {
					colors.put(color.red() / 255f).put(color.green() / 255f).put(color.blue() / 255f).put(1f);
				}
				start += vertices.length / 3;
			}
			first.flip();
			count.flip();
			gl.glGenVertexArrays(1, vao, 0);
			gl.glGenBuffers(3, buffers, 0);
			try {
				gl.glBindVertexArray(vao[0]);
				final FloatBuffer[] data = { positions, colors, normals };
				for (int buffer = 0; buffer < 3; buffer++) {
					final int attribute = buffer == 2 ? 3 : buffer;
					data[buffer].flip();
					gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers[buffer]);
					final long bytes = (long) data[buffer].remaining() * Float.BYTES;
					gl.glBufferData(GL.GL_ARRAY_BUFFER, bytes, data[buffer], GL.GL_STATIC_DRAW);
					renderer.getDiagnostics().genericUpload(bytes);
					gl.glVertexAttribPointer(attribute, buffer == 1 ? 4 : 3, GL.GL_FLOAT, false, 0, 0L);
					gl.glEnableVertexAttribArray(attribute);
				}
			} catch (RuntimeException | Error e) {
				dispose();
				throw e;
			} finally {
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
				gl.glBindVertexArray(0);
			}
		}

		void draw() {
			setLineWidth(lineWidth);
			basicShader.loadUseLighting(lighting);
			gl.glBindVertexArray(vao[0]);
			gl.glMultiDrawArrays(style, first, count, first.remaining());
			renderer.getDiagnostics().genericDraw(0);
			renderer.getDiagnostics().mapBatch(first.remaining());
		}

		void dispose() {
			gl.glDeleteBuffers(3, buffers, 0);
			gl.glDeleteVertexArrays(1, vao, 0);
		}
	}

	private final class RoadDrawing {
		final List<RoadBuffer> buffers = new ArrayList<>();
		final RoadAppearance[] appearances;
		final double startZ = getCurrentZTranslation(), increment = getCurrentZIncrement();
		final double width = getWorldWidth(), height = getWorldHeight(), maxEnv = getMaxEnvDim();
		final double initialNormalX = currentNormal.getX(), initialNormalY = currentNormal.getY(),
				initialNormalZ = currentNormal.getZ();
		final boolean lighted = displayIsLighted;
		int weight;
		double finalZ, normalX, normalY, normalZ;
		IColor finalColor;

		RoadDrawing(final AbstractObject<?, ?>[] objects, final RoadCapture capture) {
			appearances = Arrays.stream(objects).map(RoadAppearance::new).toArray(RoadAppearance[]::new);
			weight = Math.toIntExact(capture.bytes + (long) objects.length * 64);
		}

		boolean matches(final AbstractObject<?, ?>[] objects) {
			if (objects.length != appearances.length || startZ != getCurrentZTranslation()
					|| increment != getCurrentZIncrement() || width != getWorldWidth() || height != getWorldHeight()
					|| maxEnv != getMaxEnvDim() || lighted != displayIsLighted || currentObjectAlpha != 1d
					|| initialNormalX != currentNormal.getX() || initialNormalY != currentNormal.getY()
					|| initialNormalZ != currentNormal.getZ()) return false;
			for (int i = 0; i < objects.length; i++) {
				if (!appearances[i].matches(objects[i])) return false;
			}
			return true;
		}

		void draw() {
			basicShader.start();
			final float previousWidth = currentObjectLineWidth;
			try {
				basicShader.loadModelMatrix(getModelViewMatrix());
				basicShader.loadViewMatrix(identityViewMatrix);
				basicShader.loadProjectionMatrix(getProjectionMatrix());
				basicShader.loadUseTexture(false);
				basicShader.loadPremultiplyTexture(false);
				basicShader.loadUseLighting(false);
				loadPicking(basicShader, 1f);
				gl.glEnable(GL.GL_BLEND);
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				for (final RoadBuffer buffer : buffers) { buffer.draw(); }
				setCurrentZTranslation(finalZ);
				outputNormal(normalX, normalY, normalZ);
				setCurrentColor(finalColor);
			} finally {
				setLineWidth(previousWidth);
				gl.glBindVertexArray(0);
				basicShader.stop();
			}
		}

		void dispose() { buffers.forEach(RoadBuffer::dispose); }
	}

	@Override
	public void drawLayerObjects(final AbstractObject<?, ?>[] objects, final boolean staticLayer,
			final Runnable drawing) {
		final boolean previousSuppression = suppressObjectRetention;
		final boolean previousDynamicSuppression = suppressDynamicGeometryRetention;
		suppressDynamicGeometryRetention |= !staticLayer && objects.length > MAX_RETAINED_DYNAMIC_LAYER_OBJECTS;
		suppressObjectRetention |= objects.length > MAX_RETAINED_LAYER_OBJECTS || suppressDynamicGeometryRetention;
		try {
			if (canInstanceLayer(objects)) {
				suppressObjectRetention = true;
				instanceBatch = new InstanceBatch();
				try {
					drawing.run();
				} finally {
					try {
						instanceBatch.flush();
					} finally {
						instanceBatch = null;
					}
				}
				return;
			}
			if (canBatchMapLayer(objects)
					&& (suppressDynamicGeometryRetention || activeList == null || hasWideMapLines(objects))) {
				if (reusableMapBatch == null) { reusableMapBatch = new MapBatch(); }
				reusableMapBatch.begin();
				mapBatch = reusableMapBatch;
				try {
					drawing.run();
				} finally {
					try {
						mapBatch.flush();
					} finally {
						mapBatch = null;
					}
				}
				return;
			}
			if (suppressDynamicGeometryRetention) {
				drawing.run();
			} else {
				drawLayerObjectsInternal(objects, drawing);
			}
		} finally {
			suppressObjectRetention = previousSuppression;
			suppressDynamicGeometryRetention = previousDynamicSuppression;
		}
	}

	public boolean canRetainDynamicGeometry() { return mapBatch == null && !suppressDynamicGeometryRetention; }

	private static boolean hasWideMapLines(final AbstractObject<?, ?>[] objects) {
		for (final var object : objects) {
			final var attributes = object.getAttributes();
			final boolean line = switch (attributes.getType()) {
				case LINESTRING, LINEARRING, PLAN, POLYPLAN -> true;
				default -> false;
			};
			if (line && attributes.getLineWidth().floatValue() > 1f) return true;
		}
		return false;
	}

	private boolean canBatchMapLayer(final AbstractObject<?, ?>[] objects) {
		if (objects.length < 2 || roadCapture != null || mapBatch != null || instanceBatch != null
				|| basicShader == null || isWireframe() || renderer.getPickingHelper().isPicking()
				|| currentObjectAlpha != 1d || viewport[2] <= 0 || viewport[3] <= 0) return false;
		final float determinant = getModelViewMatrix().determinant();
		if (!Float.isFinite(determinant) || Math.abs(determinant) < 1e-12f) return false;
		for (final var object : objects) {
			final var attributes = object.getAttributes();
			if (object.type != DrawerType.GEOMETRY || object.isTextured() || attributes.getColor() == null
					|| attributes.getColor().alpha() != 255 || attributes.getType() == null
					|| attributes.getBorder() != null) return false;
			final boolean line = switch (attributes.getType()) {
				case LINESTRING, LINEARRING, PLAN, POLYPLAN -> true;
				default -> false;
			};
			final boolean polygon = switch (attributes.getType()) {
				case POLYGON, SQUARE, CIRCLE -> !attributes.isEmpty() && attributes.getBorder() == null
						&& !isWireframe();
				default -> false;
			};
			if (!line && !polygon
					|| attributes.getDepth() != null && attributes.getDepth() != 0d) return false;
		}
		return true;
	}

	private void drawLayerObjectsInternal(final AbstractObject<?, ?>[] objects, final Runnable drawing) {
		if (activeList == null || !canBatchMapLayer(objects)) {
			drawing.run();
			return;
		}
		RoadDrawing retained = roadDrawings.getIfPresent(activeList);
		if (retained != null && retained.matches(objects)) {
			if (retained.buffers.isEmpty()) {
				drawing.run();
			} else {
				retained.draw();
			}
			return;
		}
		roadDrawings.invalidate(activeList);
		final RoadCapture capture = new RoadCapture();
		retained = new RoadDrawing(objects, capture);
		roadCapture = capture;
		boolean complete = false;
		boolean traversed = false;
		try {
			drawing.run();
			traversed = true;
			if (capture.supported && !capture.runs.isEmpty()
					&& capture.bytes + (long) objects.length * 64 <= ROAD_CACHE_BYTES) {
				for (final RoadRun run : capture.runs) { retained.buffers.add(new RoadBuffer(run)); }
				retained.finalZ = getCurrentZTranslation();
				retained.normalX = currentNormal.getX();
				retained.normalY = currentNormal.getY();
				retained.normalZ = currentNormal.getZ();
				retained.finalColor = currentColor;
				retained.weight = Math.toIntExact(capture.bytes + (long) objects.length * 64);
				complete = true;
			}
		} finally {
			roadCapture = null;
			if (complete) {
				roadDrawings.put(activeList, retained);
				for (final var object : objects) { objectDrawings.invalidate(object); }
			} else {
				retained.dispose();
				if (traversed) {
					retained.buffers.clear();
					roadDrawings.put(activeList, retained);
				}
			}
		}
	}
	private final java.util.Deque<gama.ui.display.opengl.renderer.helpers.AbstractRendererHelper.Pass> replayOffsets =
			new java.util.ArrayDeque<>();

	private final Cache<AbstractObject<?, ?>, ObjectCommands> objectDrawings = CacheBuilder.newBuilder().weakKeys()
			.maximumWeight(32 * 1024 * 1024)
			.weigher((AbstractObject<?, ?> key, ObjectCommands value) -> value.weight())
			.removalListener((RemovalNotification<AbstractObject<?, ?>, ObjectCommands> notification) ->
					notification.getValue().dispose()).build();

	private final class ObjectCommands {
		final int list;
		final boolean wireframe = isWireframe(), hasTexture = isTextured();
		final boolean objectWireframe = objectIsWireframe, objectLighting = objectIsLighted;
		final float lineWidth = currentObjectLineWidth;
		final IColor color = currentColor;
		final double normalX = currentNormal.getX(), normalY = currentNormal.getY(), normalZ = currentNormal.getZ();
		final boolean textureOrientation = GamaPreferences.Displays.OPENGL_TEXTURE_ORIENTATION.getValue();
		final double envWidth = getWorldWidth(), envHeight = getWorldHeight(), maxEnv = getMaxEnvDim();
		final List<Runnable> commands = new ArrayList<>();
		final List<StoredDrawing> drawings = new ArrayList<>();
		long bytes;
		boolean supported = true;
		boolean normalDefined, inheritedNormal;

		ObjectCommands(final int list) { this.list = list; }

		boolean matches() {
			return list == activeList && wireframe == isWireframe() && hasTexture == isTextured()
					&& objectWireframe == objectIsWireframe && objectLighting == objectIsLighted
					&& lineWidth == currentObjectLineWidth && color.equals(currentColor)
					&& (!inheritedNormal || normalX == currentNormal.getX() && normalY == currentNormal.getY()
							&& normalZ == currentNormal.getZ())
					&& textureOrientation == GamaPreferences.Displays.OPENGL_TEXTURE_ORIENTATION.getValue()
					&& envWidth == getWorldWidth() && envHeight == getWorldHeight() && maxEnv == getMaxEnvDim();
		}

		void retain(final StoredDrawing drawing) {
			if (bytes + drawing.weight > 8 * 1024 * 1024) {
				supported = false;
				return;
			}
			drawing.references++;
			drawings.add(drawing);
			bytes += drawing.weight;
		}

		int weight() { return Math.toIntExact(256L + bytes + commands.size() * 64L); }

		void draw() {
			try {
				for (final Runnable command : commands) { command.run(); }
			} finally {
				while (!replayOffsets.isEmpty()) { replayOffsets.pop().close(); }
			}
		}

		void dispose() { drawings.forEach(StoredDrawing::dispose); }
	}

	private void recordObjectCommand(final Runnable command) {
		if (objectCapture == null || !objectCapture.supported) return;
		if (objectCapture.commands.size() == 65536) {
			objectCapture.supported = false;
		} else {
			objectCapture.commands.add(command);
		}
	}

	@Override
	public boolean canRetainObjectGeometry(final AbstractObject<?, ?> object) {
		return mapBatch == null && instanceBatch == null && activeList != null
				&& (!suppressObjectRetention || roadCapture != null)
				&& object.type == DrawerType.GEOMETRY && objectCapture == null
				&& !renderer.getPickingHelper().isBeginningPicking();
	}

	@Override
	public void drawObjectGeometry(final AbstractObject<?, ?> object, final Runnable drawing) {
		// Only immutable list geometry is retained. Picking and foreign shader/resource paths stay live.
		if (!canRetainObjectGeometry(object)) {
			drawing.run();
			return;
		}
		ObjectCommands stored = objectDrawings.getIfPresent(object);
		if (stored != null && stored.matches()) {
			renderer.getDiagnostics().objectReplay();
			stored.draw();
			return;
		}
		objectDrawings.invalidate(object);
		stored = new ObjectCommands(activeList);
		objectCapture = stored;
		boolean complete = false;
		try {
			drawing.run();
			final double nx = currentNormal.getX(), ny = currentNormal.getY(), nz = currentNormal.getZ();
			recordObjectCommand(() -> outputNormal(nx, ny, nz));
			complete = stored.supported;
		} finally {
			objectCapture = null;
			if (complete) {
				objectDrawings.put(object, stored);
			} else {
				stored.dispose();
			}
		}
	}

	@Override
	public gama.ui.display.opengl.renderer.helpers.AbstractRendererHelper.Pass offsetPolygonsForOutline() {
		final var offset = PolygonOffset.apply(this);
		if (objectCapture == null) return offset;
		recordObjectCommand(() -> replayOffsets.push(PolygonOffset.apply(this)));
		return () -> {
			recordObjectCommand(() -> replayOffsets.pop().close());
			offset.close();
		};
	}

	/** Replacement for display lists: maps a list-index → Runnable to replay. */
	private final java.util.Map<Integer, Runnable> listCache = new java.util.HashMap<>();

	/** Counter for generating unique list IDs. */
	private int nextListId = 1;

	/**
	 * Store the traversal callback. Its geometry objects retain ordered commands on their first actual draw.
	 *
	 * @param r
	 *            the geometry-producing runnable
	 * @return the list index
	 */
	public int compileAsList(final Runnable r) {
		final int index = nextListId++;
		listCache.put(index, r);
		return index;
	}

	/**
	 * Draw list. Replays the {@link Runnable} stored under the given index.
	 *
	 * @param i
	 *            the list index returned by {@link #compileAsList(Runnable)}
	 */
	public void drawList(final int i) {
		final Runnable r = listCache.get(i);
		if (r == null) return;
		final Integer previous = activeList;
		if (previous == null) { activeList = i; }
		if (basicShader != null) { basicShader.beginGeometrySequence(); }
		try {
			r.run();
		} finally {
			if (basicShader != null) { basicShader.endGeometrySequence(); }
			activeList = previous;
		}
	}

	/**
	 * Delete list. Removes the stored {@link Runnable} for the given index.
	 *
	 * @param index
	 *            the list index to remove
	 */
	public void deleteList(final Integer index) {
		if (index != null) {
			roadDrawings.invalidate(index);
			listCache.remove(index);
			objectDrawings.asMap().entrySet().removeIf(entry -> entry.getValue().list == index);
		}
	}

	/**
	 * Draw cached geometry.
	 *
	 * @param file
	 *            the file
	 * @param border
	 *            the border
	 */
	public void drawCachedGeometry(final GamaGeometryFile file, final IColor border) {
		if (file == null) return;
		final Integer index = geometryCache.get(file);
		drawCachedFile(index, border);
	}

	@Override
	public void drawCachedGeometry(final GamaGeometryFile file, final String key, final IColor border) {
		if (file == null) return;
		drawCachedFile(geometryCache.get(key), border);
	}

	private void drawCachedFile(final Integer index, final IColor border) {
		if (index != null) {
			try (var offset = PolygonOffset.forOutline(this, border != null && !isWireframe())) {
				drawList(index);
			}
			if (border != null || isWireframe()) {
				final IColor colorToUse = border != null ? border : getCurrentColor();
				final IColor old = swapCurrentColor(colorToUse);
				boolean previous = setObjectWireframe(true);
				try {
					drawList(index);
				} finally {
					setCurrentColor(old);
					setObjectWireframe(previous);
				}
			}
		}
	}

	/**
	 * Draw cached geometry.
	 *
	 * @param id
	 *            the id
	 * @param border
	 *            the border
	 */
	public void drawCachedGeometry(final IShape.Type id, /* final boolean solid, */ final IColor border) {
		if (geometryCache == null || id == null) return;
		final BuiltInGeometry object = geometryCache.get(id);
		if (object == null) return;
		if (!isWireframe()) {
			try (var offset = PolygonOffset.forOutline(this, border != null)) {
				object.draw(this);
			}
		}
		if (border != null || isWireframe()) {
			final IColor colorToUse = border != null ? border : getCurrentColor();
			final IColor old = swapCurrentColor(colorToUse);
			boolean previous = setObjectWireframe(true);
			try {
				object.draw(this);
			} finally {
				setCurrentColor(old);
				setObjectWireframe(previous);
			}
		}
	}

	/**
	 * Initialize shape cache.
	 */
	public void initializeShapeCache() {
		textured = true;
		geometryCache.initialize(this);
		textured = false;
	}

	/**
	 * Checks if is textured.
	 *
	 * @return true, if is textured
	 */
	public boolean isTextured() { return textured && !isWireframe(); }

	/**
	 * Begin object.
	 *
	 * @param object
	 *            the object
	 */
	public void beginObject(final AbstractObject<?, ?> object, final boolean isPicking) {
		if (basicShader != null && object.type != DrawerType.GEOMETRY && object.type != DrawerType.RESOURCE) {
			basicShader.suspendGeometrySequence();
		}
		IDrawingAttributes att = object.getAttributes();
		currentTextureSmooth = att.isSmoothTexture();
		if (isPicking && renderer.getPickingHelper().isBeginningPicking()) {
			registerForSelection(att.getIndex());
		}
		boolean empty = att.isEmpty();
		previousObjectWireframe = setObjectWireframe(empty);
		previousObjectLighting = setObjectLighting(att.isLighting());
		previousObjectLineWidth = setLineWidth(att.getLineWidth().floatValue());
		setCurrentTextures(object.getPrimaryTexture(this), object.getAlternateTexture(this));
		if (!renderer.getPickingHelper().isBeginningPicking()) { gl.glEnable(GL.GL_BLEND); }
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		setCurrentColor(att.getColor());
		if (basicShader != null && (object.type == DrawerType.GEOMETRY || object.type == DrawerType.RESOURCE)) {
			basicShader.beginGeometrySequence();
		}
	}

	/**
	 * End object.
	 *
	 * @param object
	 *            the object
	 */
	public void endObject(final AbstractObject<?, ?> object, final boolean isPicking) {
		if (basicShader != null && (object.type == DrawerType.GEOMETRY || object.type == DrawerType.RESOURCE)) {
			basicShader.endGeometrySequence();
		}
		currentPickingId = -1;
		disableTextures();
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		if (object.isFilled() && !object.getAttributes().isSynthetic()) {
			// removed glTexEnvi
		}
		setObjectLighting(previousObjectLighting);
		setObjectWireframe(previousObjectWireframe);
		setLineWidth(previousObjectLineWidth);
		if (isPicking) { renderer.getPickingHelper().tryPick(object.getAttributes()); }
	}

	private int diagnosticFrames;

	/**
	 * Begin scene.
	 *
	 * @return the pass
	 */
	public Pass beginScene() {
		if (TextDrawer.DIAGNOSTICS && diagnosticFrames++ < 3) {
			DEBUG.ERR("[GL4-TEXT] frame=" + diagnosticFrames + " driver=" + gl.glGetString(GL.GL_VERSION)
					+ " renderer=" + gl.glGetString(GL.GL_RENDERER));
			TextDrawer.traceState(gl, "scene entry");
		}
		previousDisplayWireframe = setDisplayWireframe(getData().isWireframe());
		previousDisplayLighting = setDisplayLighting(getData().isLightOn());
		processUnloadedCacheObjects();
		final IColor backgroundColor = getData().getBackgroundColor();
		gl.glClearColor(backgroundColor.red() / 255.0f, backgroundColor.green() / 255.0f,
				backgroundColor.blue() / 255.0f, 1.0f);
		gl.glClearDepth(1.0f);
		// The depth write mask also controls depth clears.
		gl.glDepthMask(true);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
		// Individual draw calls may override blending and must restore the straight-alpha equation.
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		resetMatrix(GLMatrixFunc.GL_PROJECTION);
		updatePerspective();
		resetMatrix(GLMatrixFunc.GL_MODELVIEW);
		// AD removed from here and put in ModelScene.draw() so that it is inside the keystone drawing. See #3285
		// rotateModel();
		return endScene;
	}

	/**
	 * End scene.
	 */
	public void endScene() {
		boolean drawFPS = getData().isShowfps();
		boolean drawRotation = rotationMode && SHOULD_DRAW_ROTATION_SPHERE;
		boolean drawROI = renderer.getCameraHelper().getROIEnvelope() != null;
		if (drawFPS || drawRotation || drawROI) { disableTextures(); }
		drawFPS(drawFPS);
		drawROI(drawROI);
		drawRotation(drawRotation);
		setDisplayLighting(previousDisplayLighting);
		setDisplayWireframe(previousDisplayWireframe);
		// Buffer swapping and explicit readbacks provide their own ordering; do not drain the GPU each frame.
		gl.glFlush();
		if (TextDrawer.DIAGNOSTICS && diagnosticFrames <= 3) { TextDrawer.traceState(gl, "scene end"); }
	}

	/**
	 * Process unloaded cache objects.
	 */
	public void processUnloadedCacheObjects() {
		textureCache.processUnloaded();
		geometryCache.processUnloaded();
	}

	/**
	 * Rotate model.
	 */
	public void rotateModel() {
		if (getData().hasRotation()) {
			IPoint c = getData().getRotationCenter();
			double cx = c.getX();
			double cy = c.getY();
			double cz = c.getZ();
			translateBy(cx, cy, cz);
			IPoint p = getData().getRotationAxis();
			if (p == null) {
				rotateBy(getData().getRotationAngle(), 0, 0, 1);
			} else {
				rotateBy(getData().getRotationAngle(), p.getX(), p.getY(), p.getZ());
			}
			translateBy(-cx, -cy, -cz);
		}
	}

	/**
	 * Initialize GL states.
	 *
	 * @param bg
	 *            the bg
	 */
	public void initializeGLStates(final IColor bg) {
		final float[] lineWidth = new float[1];
		gl.glGetFloatv(GL.GL_LINE_WIDTH, lineWidth, 0);
		currentObjectLineWidth = lineWidth[0];
		nativeLineWidth = lineWidth[0];
		lineWidthLimitsKnown = false;
		unsupportedLineWidthReported = false;
		disableTextures();
		gl.glClearColor(bg.red() / 255.0f, bg.green() / 255.0f, bg.blue() / 255.0f, 1.0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

		// Putting the swap interval to 0 (instead of 1) seems to cure some of
		// the problems of resizing of views.
		gl.setSwapInterval(0);

		// Enable smooth shading, which blends colors nicely, and smoothes out
		// lighting.
		// gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
		// Enabling the depth buffer & the depth testing
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST); // enables depth testing
		gl.glDepthFunc(GL.GL_LEQUAL); // the type of depth test to do
		// Whether face culling is enabled or not
		if (GamaPreferences.Displays.ONLY_VISIBLE_FACES.getValue()) {
			gl.glEnable(GL.GL_CULL_FACE);
			gl.glCullFace(GL.GL_BACK);
		}
		// Turn on clockwise direction of vertices as an indication of "front" (important)
		gl.glFrontFace(GL.GL_CW);

		// Hints
		int hint = getData().isAntialias() ? GL.GL_NICEST : GL.GL_FASTEST;
		gl.glHint(GL.GL_LINE_SMOOTH_HINT, hint);
		// GL_TEXTURE_2D enable/disable is not valid in OpenGL 4 core profile; texturing is controlled by shaders.
		// Blending & alpha control
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL.GL_DEPTH_TEST);
		// Disabling line smoothing to only rely on FSAA
		gl.glEnable(GL.GL_LINE_SMOOTH);
		// GL_NORMALIZE is a fixed-function lighting constant removed in GL4 core profile; normals are normalized in
		// shaders.
		// Enabling multi-sampling
		gl.glEnable(GL.GL_MULTISAMPLE);
		// Setting the default polygon mode
		updatePolygonMode();
		initializeShapeCache();

	}

	/**
	 * Gets the ratios.
	 *
	 * @return the ratios
	 */
	public IPoint getRatios() { return ratios; }

	/**
	 *
	 * DECORATIONS: ROI, Rotation, FPS
	 *
	 */

	public void setRotationMode(final boolean b) { rotationMode = b; }

	/**
	 * Checks if is in rotation mode.
	 *
	 * @return true, if is in rotation mode
	 */
	public boolean isInRotationMode() { return rotationMode; }

	/**
	 * Draws a string in fixed screen-space (overlay) coordinates using the JOGL graph/curve renderer. The position
	 * {@code (x, y)} is in pixels measured from the bottom-left corner of the viewport. This replaces every former call
	 * to {@link #rasterText} which is a no-op in GL4 core.
	 *
	 * @param s
	 *            the string to draw
	 * @param font
	 *            the AWT font to use
	 * @param x
	 *            x position in pixels from the left edge of the viewport
	 * @param y
	 *            y position in pixels from the bottom edge of the viewport
	 */
	public void drawScreenText(final String s, final java.awt.Font font, final double x, final double y) {
		final TextDrawer td = (TextDrawer) drawers.get(DrawerType.STRING);
		if (td == null) return;
		// Build minimal IDrawingAttributes in overlay (screen-space) mode
		final gama.gaml.statements.draw.TextDrawingAttributes attr =
				new gama.gaml.statements.draw.TextDrawingAttributes(gama.api.utils.geometry.Scaling3D.of(1, 1, 1), null,
						gama.api.types.geometry.GamaPointFactory.create(x, y, 0), getCurrentColor());
		attr.setFont(gama.api.types.font.GamaFontFactory.createFontFrom(font));
		attr.setPerspective(false);
		td.drawWithGraphLibraryPublic(s, attr, true);
	}

	/**
	 * Draw FPS. Renders the current frames-per-second count in the top-left corner of the display using the JOGL
	 * graph/curve screen-space renderer.
	 *
	 * @param doIt
	 *            whether the FPS counter should be drawn
	 */
	public void drawFPS(final boolean doIt) {
		if (doIt) {
			setCurrentColor(GamaColorFactory.BLACK);
			final int nb = (int) getCanvas().getLastFPS();
			final String s = nb == 0 ? "(computing FPS...)" : nb + " FPS";
			drawScreenText(s, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12), -5, 5);
		}
	}

	/**
	 * Draw ROI.
	 *
	 * @param doIt
	 *            the do it
	 */
	public void drawROI(final boolean doIt) {
		if (doIt) { getGeometryDrawer().drawROIHelper(renderer.getCameraHelper().getROIEnvelope()); }
	}

	/**
	 * Size of rotation elements.
	 *
	 * @return the double
	 */
	public double sizeOfRotationElements() {
		return Math.min(getMaxEnvDim() / 4d, getData().getCameraPos().minus(getData().getCameraTarget()).norm() / 6d);
	}

	/**
	 * Draw rotation.
	 *
	 * @param doIt
	 *            the do it
	 */
	public void drawRotation(final boolean doIt) {
		if (doIt) {
			final IPoint target = getData().getCameraTarget();
			final double distance = getData().getCameraPos().minus(target).norm();
			getGeometryDrawer().drawRotationHelper(target, distance, Math.min(getMaxEnvDim() / 4d, distance / 8d));
		}
	}

	@Override
	public void initialize() {}

	/**
	 * Checks if is rendering keystone.
	 *
	 * @return true, if is rendering keystone
	 */
	public boolean isRenderingKeystone() {
		var k = getRenderer().getKeystoneHelper();
		return k.isActive() || getRenderer().getData().isKeystoneDefined();
	}

}

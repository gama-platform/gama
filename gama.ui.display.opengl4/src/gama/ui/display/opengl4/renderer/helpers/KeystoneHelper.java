/*******************************************************************************************************
 *
 * KeystoneHelper.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.renderer.helpers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;

import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.ICoordinates;
import gama.api.utils.geometry.Scaling3D;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl4.renderer.IOpenGLRenderer;
import gama.ui.display.opengl4.renderer.shaders.AbstractPostprocessingShader;
import gama.ui.display.opengl4.renderer.shaders.AbstractShader;
import gama.ui.display.opengl4.renderer.shaders.FrameBufferObject;
import gama.ui.display.opengl4.renderer.shaders.KeystoneShaderProgram;
import gama.ui.shared.utils.DPIHelper;

/**
 * The Class KeystoneHelper.
 */
public class KeystoneHelper extends AbstractRendererHelper {

	/** The finishing helper. */
	private final Pass finishingHelper = this::finishRenderToTexture;

	/** The fbo scene. */
	private FrameBufferObject fboScene;

	/** The draw keystone helper. */
	protected boolean drawKeystoneHelper = false;

	/** The corner hovered. */
	protected int cornerSelected = -1, cornerHovered = -1;

	/** The uv mapping buffer index. */
	private int uvMappingBufferIndex;

	/** The vertices buffer index. */
	private int verticesBufferIndex;

	/** The index buffer index. */
	private int indexBufferIndex;

	/** The shader. */
	private KeystoneShaderProgram shader;

	/** The world corners. */
	private boolean worldCorners = false;

	/** The Constant FILL_COLORS. */
	private static final IColor[] FILL_COLORS = { GamaColorFactory.get("gamared").withAlpha(0.3),
			GamaColorFactory.get("gamablue").withAlpha(0.3), GamaColorFactory.get("black").withAlpha(0.3) };

	/** The ib idx buff. */
	final IntBuffer ibIdxBuff = Buffers.newDirectIntBuffer(new int[] { 0, 1, 2, 0, 2, 3 });

	/**
	 * Instantiates a new keystone helper.
	 *
	 * @param r
	 *            the r
	 */
	public KeystoneHelper(final IOpenGLRenderer r) {
		super(r);
	}

	/**
	 * Gets the view width.
	 *
	 * @return the view width
	 */
	int getViewWidth() { return getRenderer().getViewWidth(); }

	/**
	 * Gets the view height.
	 *
	 * @return the view height
	 */
	int getViewHeight() { return getRenderer().getViewHeight(); }

	@Override
	public void initialize() {

	}

	/**
	 * Gets the corner selected.
	 *
	 * @return the corner selected
	 */
	public int getCornerSelected() { return cornerSelected; }

	/**
	 * Gets the coords.
	 *
	 * @return the coords
	 */
	public IPoint[] getCoords() { return getData().getKeystone().toPointsArray(); }

	/**
	 * Gets the keystone coordinates.
	 *
	 * @param corner
	 *            the corner
	 * @return the keystone coordinates
	 */
	public IPoint getKeystoneCoordinates(final int corner) {
		return getCoords()[corner];
	}

	/**
	 * Start draw helper.
	 */
	public void startDrawHelper() {
		drawKeystoneHelper = true;
		cornerSelected = -1;
	}

	/**
	 * Stop draw helper.
	 */
	public void stopDrawHelper() {
		drawKeystoneHelper = false;
	}

	/**
	 * Switch corners.
	 */
	public void switchCorners() {
		worldCorners = !GamaCoordinateSequenceFactory.getKeystoneIdentity().getEnvelope()
				.covers(getData().getKeystone().getEnvelope());
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		final GL4 gl = getGL();
		if (fboScene != null) { fboScene.cleanUp(); }
		if (gl != null) {
			gl.glDeleteBuffers(3, new int[] { indexBufferIndex, verticesBufferIndex, uvMappingBufferIndex }, 0);
		}
	}

	/**
	 * Begin render to texture.
	 */
	public void beginRenderToTexture() {
		final GL4 gl = getGL();
		gl.glClearColor(0, 0, 0, 1.0f);
		gl.glClear(GL.GL_STENCIL_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		if (fboScene == null) { fboScene = new FrameBufferObject(gl, getViewWidth(), getViewHeight()); }
		// redirect the rendering to the fbo_scene (will be rendered later, as a texture)
		fboScene.bindFrameBuffer();

	}

	/**
	 * Draw rectangle.
	 *
	 * @param openGL
	 *            the open GL
	 * @param centerX
	 *            the center X
	 * @param centerY
	 *            the center Y
	 * @param centerZ
	 *            the center Z
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param fill
	 *            the fill
	 */
	private void drawRectangle(final OpenGL openGL, final double centerX, final double centerY, final double centerZ,
			final double width, final double height, final IColor fill) {
		openGL.pushMatrix();
		openGL.translateBy(centerX, centerY, centerZ);
		openGL.setCurrentColor(fill);
		openGL.scaleBy(Scaling3D.of(width, height, 1));
		openGL.drawCachedGeometry(IShape.Type.SQUARE, null);
		openGL.popMatrix();
	}

	/**
	 * Font used for the keystone corner labels — SansSerif 18pt.
	 * Stored as a constant to avoid recreating it on every frame.
	 */
	private static final java.awt.Font KEYSTONE_FONT = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 18);

	/**
	 * Cached {@link java.awt.FontMetrics} for {@link #KEYSTONE_FONT}, obtained once from a scratch
	 * {@link java.awt.image.BufferedImage}. Avoids allocating a new image on every frame.
	 */
	private static final java.awt.FontMetrics KEYSTONE_METRICS = initMetrics();

	/** Label height in pixels: font size (18) + padding (20). */
	private static final int LABEL_HEIGHT_PX = 18 + 20;

	/**
	 * Creates the cached {@link java.awt.FontMetrics} for {@link #KEYSTONE_FONT}.
	 *
	 * @return the metrics, or a fallback that estimates 10 px per character if initialisation fails
	 */
	private static java.awt.FontMetrics initMetrics() {
		final java.awt.image.BufferedImage img =
				new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		final java.awt.Graphics2D g2 = img.createGraphics();
		try {
			return g2.getFontMetrics(KEYSTONE_FONT);
		} finally {
			g2.dispose();
		}
	}

	/**
	 * Estimates the pixel width of a label string rendered in {@link #KEYSTONE_FONT}.
	 * Uses the statically cached {@link #KEYSTONE_METRICS}; no allocation occurs per call.
	 *
	 * @param text the label text
	 * @return estimated pixel width
	 */
	private static int estimateLabelWidth(final String text) {
		return KEYSTONE_METRICS.stringWidth(text);
	}

	/**
	 * Draws the four corner labels for the keystone helper overlay.
	 *
	 * <p>All geometry (background rectangles and text) is expressed in <em>pixel</em> coordinates.
	 * The background rectangles are drawn via {@link OpenGL#drawCachedGeometry} under a pixel-space
	 * ortho projection ({@code [0, W] × [0, H]}), and the text is drawn via
	 * {@link OpenGL#drawScreenText} which uses the same coordinate space internally. This avoids
	 * the previous mismatch where the outer {@code [0,1]} normalised ortho was silently overwritten
	 * by {@code TextDrawer}'s own pixel-space overlay projection.</p>
	 */
	private void drawKeystoneMarks() {
		final OpenGL openGL = getOpenGL();
		final int W = getViewWidth();
		final int H = getViewHeight();

		// Compute keystone vertex positions in [0,W]×[0,H] pixel space
		final double[] worldCoords = openGL.getPixelWidthAndHeightOfWorld();
		final double widthRatio  = worldCoords[0] / W;
		final double heightRatio = worldCoords[1] / H;
		final double xOffsetPx   = (1 - widthRatio)  * W;
		final double yOffsetPx   = (1 - heightRatio) * H;

		ICoordinates vertices;
		if (!worldCorners) {
			// identity corners in pixel space: (0,0),(0,H),(W,H),(W,0)
			vertices = GamaCoordinateSequenceFactory.create(4, 3);
			vertices.at(0).setLocation(0,  0,  0);
			vertices.at(1).setLocation(0,  H,  0);
			vertices.at(2).setLocation(W,  H,  0);
			vertices.at(3).setLocation(W,  0,  0);
		} else {
			vertices = GamaCoordinateSequenceFactory.create(4, 3);
			vertices.at(0).setLocation(xOffsetPx,       yOffsetPx,       0);
			vertices.at(1).setLocation(xOffsetPx,       H - yOffsetPx,   0);
			vertices.at(2).setLocation(W - xOffsetPx,   H - yOffsetPx,   0);
			vertices.at(3).setLocation(W - xOffsetPx,   yOffsetPx,       0);
		}

		// Use pixel-space ortho so rectangles and text share the same coordinate system.
		// TextDrawer's overlay projection is also [0,W]×[0,H], so no conflict.
		openGL.pushIdentity(GLMatrixFunc.GL_PROJECTION);
		openGL.getCurrentMatrixStack().ortho(0, W, 0, H, 1, -1);
		final boolean previous = openGL.setObjectLighting(false);
		openGL.push(GLMatrixFunc.GL_MODELVIEW);

		vertices.visit((id, x, y, z) -> {
			final String text = floor4Digit(getCoords()[id].getX()) + "," + floor4Digit(getCoords()[id].getY());
			final int textWidthPx  = estimateLabelWidth(text);
			final int labelWidthPx = textWidthPx + 20;

			final int fill = id == cornerSelected ? 0 : id == cornerHovered ? 1 : 2;

			// Background rectangle — centred on the label position, in pixel space
			final double xLabelPx = x + (id == 0 || id == 1 ?  labelWidthPx / 2.0 : -labelWidthPx / 2.0);
			final double yLabelPx = y + (id == 0 || id == 3 ?  LABEL_HEIGHT_PX / 2.0 : -LABEL_HEIGHT_PX / 2.0);
			drawRectangle(openGL, xLabelPx, yLabelPx, z, labelWidthPx, LABEL_HEIGHT_PX, FILL_COLORS[fill]);

			// Text position — lower-left of the text, in pixel space
			final double xTextPx = id == 0 || id == 1
					? 10 + (worldCorners ? xOffsetPx : 0)
					: W - labelWidthPx + 10 - (worldCorners ? xOffsetPx : 0);
			final double yTextPx = id == 0 || id == 3
					? 12 + (worldCorners ? yOffsetPx : 0)
					: H - LABEL_HEIGHT_PX + 12 - (worldCorners ? yOffsetPx : 0);

			openGL.setCurrentColor(GamaColorFactory.WHITE);
			openGL.drawScreenText(text, KEYSTONE_FONT, xTextPx, yTextPx);
		}, 4, true);

		openGL.pop(GLMatrixFunc.GL_MODELVIEW);
		openGL.setObjectLighting(previous);
		openGL.pop(GLMatrixFunc.GL_PROJECTION);
	}

	/**
	 * Floor 4 digit.
	 *
	 * @param n
	 *            the n
	 * @return the double
	 */
	private double floor4Digit(final double n) {
		double number = n * 1000;
		number = Math.round(number);
		number /= 1000;
		return number;
	}

	/**
	 * Finish render to texture.
	 */
	public void finishRenderToTexture() {
		if (drawKeystoneHelper) { drawKeystoneMarks(); }
		// gl.glDisable(GL4.GL_DEPTH_TEST); // disables depth testing
		final AbstractPostprocessingShader theShader = getShader();
		// unbind the last fbo
		if (fboScene != null) {
			// We verify if it is not null
			fboScene.unbindCurrentFrameBuffer();
			// prepare shader
			theShader.start();
			prepareShader(theShader);
			// build the surface
			createScreenSurface();
			// draw
			final GL4 gl = getGL();
			gl.glDrawElements(GL.GL_TRIANGLES, 6, GL.GL_UNSIGNED_INT, 0);
			theShader.stop();
			gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
		}

	}

	/**
	 * Gets the shader.
	 *
	 * @return the shader
	 */
	public KeystoneShaderProgram getShader() {
		final GL4 gl = getGL();
		if (shader == null) {
			shader = new KeystoneShaderProgram(gl, "keystoneVertexShader2", "keystoneFragmentShader2");
			final int[] handles = new int[3];
			gl.glGenBuffers(3, handles, 0);
			uvMappingBufferIndex = handles[0];
			verticesBufferIndex = handles[1];
			indexBufferIndex = handles[2];
		}
		return shader;
	}

	/**
	 * Prepare shader.
	 *
	 * @param shaderProgram
	 *            the shader program
	 */
	private void prepareShader(final AbstractPostprocessingShader shaderProgram) {
		// shaderProgram.loadTexture(0);
		shaderProgram.storeTextureID(fboScene.getFBOTexture());
	}

	/**
	 * Creates the screen surface.
	 */
	public void createScreenSurface() {
		final GL4 gl = getGL();
		// Keystoning computation (cf
		// http://www.bitlush.com/posts/arbitrary-quadrilaterals-in-opengl-es-2-0)
		// transform the coordinates [0,1] --> [-1,+1]
		final ICoordinates coords = getData().getKeystone();
		final float[] p0 = { (float) coords.at(0).getX() * 2f - 1f, (float) (coords.at(0).getY() * 2f - 1f) }; // bottom-left
		final float[] p1 = { (float) coords.at(1).getX() * 2f - 1f, (float) coords.at(1).getY() * 2f - 1f }; // top-left
		final float[] p2 = { (float) coords.at(2).getX() * 2f - 1f, (float) coords.at(2).getY() * 2f - 1f }; // top-right
		final float[] p3 = { (float) coords.at(3).getX() * 2f - 1f, (float) coords.at(3).getY() * 2f - 1f }; // bottom-right

		final float ax = (p2[0] - p0[0]) / 2f;
		final float ay = (p2[1] - p0[1]) / 2f;
		final float bx = (p3[0] - p1[0]) / 2f;
		final float by = (p3[1] - p1[1]) / 2f;

		final float cross = ax * by - ay * bx;

		if (cross != 0) {
			final float cy = (p0[1] - p1[1]) / 2f;
			final float cx = (p0[0] - p1[0]) / 2f;

			final float s = (ax * cy - ay * cx) / cross;

			final float t = (bx * cy - by * cx) / cross;

			final float q0 = 1 / (1 - t);
			final float q1 = 1 / (1 - s);
			final float q2 = 1 / t;
			final float q3 = 1 / s;

			// I can now pass (u * q, v * q, q) to OpenGL
			final float[] listVertices = { p0[0], p0[1], 1f, p1[0], p1[1], 0f, p2[0], p2[1], 0f, p3[0], p3[1], 1f };
			final float[] listUvMapping =
					{ 0f, 1f * q0, 0f, q0, 0f, 0f, 0f, q1, 1f * q2, 0f, 0f, q2, 1f * q3, 1f * q3, 0f, q3 };
			// VERTICES POSITIONS BUFFER
			storeAttributes(AbstractShader.POSITION_ATTRIBUTE_IDX, verticesBufferIndex, 3, listVertices);
			// UV MAPPING (If a texture is defined)
			storeAttributes(AbstractShader.UVMAPPING_ATTRIBUTE_IDX, uvMappingBufferIndex, 4, listUvMapping);

		}

		getOpenGL().bindTexture(fboScene.getFBOTexture());
		// Select the VBO, GPU memory data, to use for colors
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBufferIndex);
		gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, 24, ibIdxBuff, GL.GL_STATIC_DRAW);
		ibIdxBuff.rewind();
	}

	/**
	 * Store attributes.
	 *
	 * @param shaderAttributeType
	 *            the shader attribute type
	 * @param bufferIndex
	 *            the buffer index
	 * @param size
	 *            the size
	 * @param data
	 *            the data
	 */
	private void storeAttributes(final int shaderAttributeType, final int bufferIndex, final int size,
			final float[] data) {
		final GL4 gl = getGL();
		// Select the VBO, GPU memory data, to use for data
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferIndex);
		// Associate Vertex attribute with the last bound VBO
		gl.glVertexAttribPointer(shaderAttributeType, size, GL.GL_FLOAT, false, 0, 0 /* offset */);
		// compute the total size of the buffer :
		final int numBytes = data.length * 4;
		gl.glBufferData(GL.GL_ARRAY_BUFFER, numBytes, null, GL.GL_STATIC_DRAW);
		final FloatBuffer fbData = Buffers.newDirectFloatBuffer(data);
		gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, numBytes, fbData);
		gl.glEnableVertexAttribArray(shaderAttributeType);
	}

	/**
	 * Sets the corner selected.
	 *
	 * @param cornerId
	 *            the new corner selected
	 */
	public void setCornerSelected(final int cornerId) { cornerSelected = cornerId; }

	/**
	 * Reset corner.
	 *
	 * @param cornerId
	 *            the corner id
	 */
	public void resetCorner(final int cornerId) {
		setKeystoneCoordinates(cornerId, GamaCoordinateSequenceFactory.getKeystoneIdentity().at(cornerId));
		cornerSelected = -1;
		cornerHovered = -1;
	}

	/**
	 * Corner selected.
	 *
	 * @param mouse
	 *            the mouse
	 * @return the int
	 */
	public int cornerSelected(final IPoint mouse) {
		if (mouse.getX() < getViewWidth() / 2d) {
			if (mouse.getY() < getViewHeight() / 2d) return 1;
			return 0;
		}
		if (mouse.getY() < getViewHeight() / 2d) return 2;
		return 3;
	}

	/**
	 * Corner hovered.
	 *
	 * @param mouse
	 *            the mouse
	 * @return the int
	 */
	public int cornerHovered(final IPoint mouse) {
		if (mouse.getX() < getViewWidth() / 2d) {
			if (mouse.getY() < getViewHeight() / 2d) return 1;
			return 0;
		}
		if (mouse.getY() < getViewHeight() / 2d) return 2;
		return 3;
	}

	/**
	 * Sets the corner hovered.
	 *
	 * @param cornerId
	 *            the new corner hovered
	 */
	public void setCornerHovered(final int cornerId) { cornerHovered = cornerId; }

	/**
	 * Sets the keystone coordinates.
	 *
	 * @param cornerId
	 *            the corner id
	 * @param p
	 *            the p
	 */
	public void setKeystoneCoordinates(final int cornerId, final IPoint p) {
		getData().getKeystone().replaceWith(cornerId, p.getX(), p.getY(), p.getZ());
		switchCorners();
		getData().setKeystone(getData().getKeystone());
	}

	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() { return drawKeystoneHelper; }

	/**
	 * Render.
	 *
	 * @return the pass
	 */
	public Pass render() {
		if (drawKeystoneHelper || getData().isKeystoneDefined()) {
			beginRenderToTexture();
			return finishingHelper;
		}
		return null;
	}

	/**
	 * Reshape.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public void reshape(final int width, final int height) {
		if (fboScene != null) {
			fboScene.setDisplayDimensions(DPIHelper.autoScaleUp(renderer.getCanvas().getMonitor(), width),
					DPIHelper.autoScaleUp(renderer.getCanvas().getMonitor(), height));
		}

	}

}

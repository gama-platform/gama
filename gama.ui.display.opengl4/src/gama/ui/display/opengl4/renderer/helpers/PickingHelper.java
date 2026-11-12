/*******************************************************************************************************
 *
 * PickingHelper.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.renderer.helpers;

import java.nio.IntBuffer;
import java.util.ArrayList;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;

import gama.api.ui.layers.IDrawingAttributes;
import gama.dev.DEBUG;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;
import gama.ui.display.opengl.renderer.helpers.CameraHelper;

/**
 * The Class PickingHelper.
 */
public class PickingHelper extends AbstractRendererHelper
		implements gama.ui.display.opengl.renderer.helpers.IPickingHelper {

	static {
		DEBUG.OFF();
	}

	/** The select buffer. */
	protected final IntBuffer selectBuffer = Buffers.newDirectIntBuffer(1024);

	/**
	 * Instantiates a new picking helper.
	 *
	 * @param r
	 *            the r
	 */
	public PickingHelper(final IOpenGLRenderer r) {
		super(r);
	}

	@Override
	public void initialize() {}

	/** The Constant NONE. */
	final static int NONE = -2;

	/** The Constant WORLD. */
	final static int WORLD = -1;

	/** The is picking. */
	volatile boolean isPicking;

	/** The is menu on. */
	volatile boolean isMenuOn;

	/** The picked index. */
	volatile int pickedIndex = NONE;

	/**
	 * Sets the picking.
	 *
	 * @param isPicking
	 *            the new picking
	 */
	public void setPicking(final boolean isPicking) {
		// DEBUG.OUT("Entering setPicking with " + isPicking);
		// This is set now here (instead of below because of Issue #10 (github.com/gama-platform/gama/issues/10)
		setPickedIndex(NONE);
		// if (this.isPicking && isPicking) {
		// this.isPicking = false;
		// } else {
		this.isPicking = isPicking;
		// }
		if (!isPicking) {
			// setPickedIndex(NONE);
			setMenuOn(false);
		}
	}

	/**
	 * Sets the menu on.
	 *
	 * @param isMenuOn
	 *            the new menu on
	 */
	public void setMenuOn(final boolean isMenuOn) { this.isMenuOn = isMenuOn; }

	/**
	 * Sets the picked index.
	 *
	 * @param pickedIndex
	 *            the new picked index
	 */
	public void setPickedIndex(final int pickedIndex) {
		// DEBUG.OUT("Entering setPickedIndex with " + pickedIndex);
		try {
			this.pickedIndex = pickedIndex;
			if (pickedIndex == WORLD && !isMenuOn) {
				// DEBUG.OUT("In setPickedIndex world has been selected ");
				// Selection occured, but no object have been selected
				setMenuOn(true);
				getSurface().selectAgent(null);
			}
		} catch (Throwable e) {
			DEBUG.ERR("in setPickedIndex", e);
		}
	}

	/**
	 * Try pick.
	 *
	 * @param attributes
	 *            the attributes
	 */
	public void tryPick(final IDrawingAttributes attributes) {
		// DEBUG.OUT("Entering tryPick");
		try {
			attributes.markSelected(pickedIndex);
			if (attributes.isSelected() && !isMenuOn) {
				setMenuOn(true);
				getSurface().selectAgent(attributes);
			}
		} catch (Throwable e) {
			DEBUG.ERR("in tryPick", e);
		}
	}

	/**
	 * Checks if is beginning picking.
	 *
	 * @return true, if is beginning picking
	 */
	public boolean isBeginningPicking() { return isPicking && pickedIndex == NONE; }

	/**
	 * Checks if is menu on.
	 *
	 * @return true, if is menu on
	 */
	public boolean isMenuOn() { return isMenuOn; }

	/**
	 * Checks if is picking.
	 *
	 * @return true, if is picking
	 */
	public boolean isPicking() { return isPicking; }

	// Picking (GL4 replacement for the removed GL_SELECT path)
	// =========================================================
	// We use colour-buffer picking: during the picking pass each object is
	// rendered with a unique flat colour in a separate single-sample framebuffer.
	// IDs are mapped back to drawing-attribute indices after reading the cursor pixel.

	/** GL-coordinate (y-flipped) of the pixel to read back during endPicking(). */
	private int pickX, pickY;
	private boolean pickInViewport;
	private final ArrayList<Integer> pickingIndices = new ArrayList<>();
	private final int[] framebuffer = new int[1], colorBuffer = new int[1], depthBuffer = new int[1];
	private final int[] previousFramebuffers = new int[2], previousRenderbuffer = new int[1];
	private final int[] previousBlendFactors = new int[4];
	private final byte[] previousDepthMask = new byte[1], previousColorMask = new byte[4];
	private final float[] previousClearColor = new float[4], previousBlendColor = new float[4];
	private final double[] previousClearDepth = new double[1];
	private boolean previousBlend, previousDither, previousMultisample, previousScissor, previousSrgb;

	public int idFor(final int index) {
		if (pickingIndices.size() >= 0xFFFFFF) {
			throw new IllegalStateException("Too many objects in the OpenGL4 picking pass");
		}
		pickingIndices.add(index);
		return pickingIndices.size() - 1;
	}

	@Override
	public boolean shouldRenderPicking() { return pickInViewport; }

	/**
	 * Begin picking with the normal scene projection. Unlike GL_SELECT, colour-buffer
	 * picking reads the pixel at the cursor and must not magnify a separate pick volume.
	 */
	public void beginPicking() {
		final OpenGL openGL = getOpenGL();
		final GL4 gl = getGL();
		pickInViewport = false;
		pickingIndices.clear();
		openGL.push(GLMatrixFunc.GL_PROJECTION);
		try {
			final CameraHelper camera = getRenderer().getCameraHelper();
			final int[] viewport = new int[4];
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			final double x = camera.getMousePosition().getX(), y = camera.getMousePosition().getY();
			final int width = getRenderer().getCanvas().getSurfaceWidth();
			final int height = getRenderer().getCanvas().getSurfaceHeight();
			// CameraHelper receives NEWT coordinates in framebuffer pixels, not logical view pixels.
			pickX = (int) x;
			pickY = height - 1 - (int) y;
			pickInViewport = width > 0 && height > 0 && viewport[0] >= 0 && viewport[1] >= 0
					&& viewport[2] > 0 && viewport[3] > 0
					&& x >= 0 && x < width && y >= 0 && y < height
					&& pickX >= viewport[0] && pickX < viewport[0] + viewport[2]
					&& pickY >= viewport[1] && pickY < viewport[1] + viewport[3];
			if (pickInViewport) {
				beginFramebuffer(width, height);
			}
		} catch (final RuntimeException e) {
			openGL.pop(GLMatrixFunc.GL_PROJECTION);
			throw e;
		} finally {
			openGL.matrixMode(GLMatrixFunc.GL_MODELVIEW);
		}
	}

	private void beginFramebuffer(final int width, final int height) {
		final GL4 gl = getGL();
		gl.glGetIntegerv(GL.GL_DRAW_FRAMEBUFFER_BINDING, previousFramebuffers, 0);
		gl.glGetIntegerv(GL.GL_READ_FRAMEBUFFER_BINDING, previousFramebuffers, 1);
		gl.glGetIntegerv(GL.GL_RENDERBUFFER_BINDING, previousRenderbuffer, 0);
		gl.glGetBooleanv(GL.GL_DEPTH_WRITEMASK, previousDepthMask, 0);
		gl.glGetBooleanv(GL.GL_COLOR_WRITEMASK, previousColorMask, 0);
		gl.glGetFloatv(GL.GL_COLOR_CLEAR_VALUE, previousClearColor, 0);
		gl.glGetFloatv(GL4.GL_BLEND_COLOR, previousBlendColor, 0);
		gl.glGetIntegerv(GL.GL_BLEND_SRC_RGB, previousBlendFactors, 0);
		gl.glGetIntegerv(GL.GL_BLEND_DST_RGB, previousBlendFactors, 1);
		gl.glGetIntegerv(GL.GL_BLEND_SRC_ALPHA, previousBlendFactors, 2);
		gl.glGetIntegerv(GL.GL_BLEND_DST_ALPHA, previousBlendFactors, 3);
		gl.glGetDoublev(GL.GL_DEPTH_CLEAR_VALUE, previousClearDepth, 0);
		previousBlend = gl.glIsEnabled(GL.GL_BLEND);
		previousDither = gl.glIsEnabled(GL.GL_DITHER);
		previousMultisample = gl.glIsEnabled(GL.GL_MULTISAMPLE);
		previousScissor = gl.glIsEnabled(GL.GL_SCISSOR_TEST);
		previousSrgb = gl.glIsEnabled(GL4.GL_FRAMEBUFFER_SRGB);
		try {
			gl.glGenFramebuffers(1, framebuffer, 0);
			gl.glGenRenderbuffers(1, colorBuffer, 0);
			gl.glGenRenderbuffers(1, depthBuffer, 0);
			gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, framebuffer[0]);
			gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, colorBuffer[0]);
			gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_RGBA8, width, height);
			gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_RENDERBUFFER,
					colorBuffer[0]);
			gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, depthBuffer[0]);
			gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL4.GL_DEPTH_COMPONENT24, width, height);
			gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER,
					depthBuffer[0]);
			gl.glDrawBuffer(GL.GL_COLOR_ATTACHMENT0);
			gl.glReadBuffer(GL.GL_COLOR_ATTACHMENT0);
			final int status = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
			if (status != GL.GL_FRAMEBUFFER_COMPLETE) {
				throw new IllegalStateException("Cannot allocate OpenGL4 picking framebuffer " + width + "x" + height
						+ " (status 0x" + Integer.toHexString(status) + ")");
			}
			gl.glDisable(GL.GL_BLEND);
			gl.glDisable(GL.GL_DITHER);
			gl.glDisable(GL.GL_MULTISAMPLE);
			gl.glDisable(GL.GL_SCISSOR_TEST);
			gl.glDisable(GL4.GL_FRAMEBUFFER_SRGB);
			gl.glColorMask(true, true, true, true);
			gl.glDepthMask(true);
			gl.glClearColor(1, 1, 1, 1);
			gl.glClearDepth(1);
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		} catch (final RuntimeException e) {
			restoreFramebuffer();
			throw e;
		}
	}

	private void restoreFramebuffer() {
		final GL4 gl = getGL();
		gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, previousFramebuffers[0]);
		gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, previousFramebuffers[1]);
		gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, previousRenderbuffer[0]);
		gl.glDeleteFramebuffers(1, framebuffer, 0);
		gl.glDeleteRenderbuffers(1, colorBuffer, 0);
		gl.glDeleteRenderbuffers(1, depthBuffer, 0);
		framebuffer[0] = colorBuffer[0] = depthBuffer[0] = 0;
		setEnabled(gl, GL.GL_BLEND, previousBlend);
		setEnabled(gl, GL.GL_DITHER, previousDither);
		setEnabled(gl, GL.GL_MULTISAMPLE, previousMultisample);
		setEnabled(gl, GL.GL_SCISSOR_TEST, previousScissor);
		setEnabled(gl, GL4.GL_FRAMEBUFFER_SRGB, previousSrgb);
		gl.glDepthMask(previousDepthMask[0] != 0);
		gl.glColorMask(previousColorMask[0] != 0, previousColorMask[1] != 0, previousColorMask[2] != 0,
				previousColorMask[3] != 0);
		gl.glClearColor(previousClearColor[0], previousClearColor[1], previousClearColor[2], previousClearColor[3]);
		gl.glClearDepth(previousClearDepth[0]);
		gl.glBlendColor(previousBlendColor[0], previousBlendColor[1], previousBlendColor[2], previousBlendColor[3]);
		gl.glBlendFuncSeparate(previousBlendFactors[0], previousBlendFactors[1], previousBlendFactors[2],
				previousBlendFactors[3]);
	}

	private static void setEnabled(final GL4 gl, final int capability, final boolean enabled) {
		if (enabled) { gl.glEnable(capability); } else { gl.glDisable(capability); }
	}

	/**
	 * End picking. Flushes the GPU pipeline, then reads the single pixel at the click position from the
	 * colour buffer. The RGB channels encode a per-pass ID; white signals the background.
	 *
	 * <p>{@link GL4#glFinish()} is called before {@link GL4#glReadPixels} to guarantee the picking-pass
	 * draw commands are complete before the readback, which is required on async/multi-threaded drivers.</p>
	 */
	public void endPicking() {
		final GL4 gl = getGL();
		final OpenGL openGL = getOpenGL();
		int selectedIndex = PickingHelper.NONE;
		try {
			if (!pickInViewport) return;
			// Ensure the picking pass is fully rendered before reading back
			gl.glFinish();
			final java.nio.ByteBuffer pixel = com.jogamp.common.nio.Buffers.newDirectByteBuffer(4);
			gl.glReadPixels(pickX, pickY, 1, 1, GL4.GL_RGBA, com.jogamp.opengl.GL.GL_UNSIGNED_BYTE, pixel);
			final int r = pixel.get(0) & 0xFF;
			final int g = pixel.get(1) & 0xFF;
			final int b = pixel.get(2) & 0xFF;
			final int id = r | g << 8 | b << 16;
			if (id == 0xFFFFFF) {
				selectedIndex = PickingHelper.WORLD;
			} else if (id < pickingIndices.size()) {
				selectedIndex = pickingIndices.get(id);
			} else {
				throw new IllegalStateException("Invalid OpenGL4 picking ID: " + id);
			}
		} finally {
			if (pickInViewport) { restoreFramebuffer(); }
			// Restore the projection matrix pushed by beginPicking()
			openGL.pop(GLMatrixFunc.GL_PROJECTION);
			openGL.matrixMode(GLMatrixFunc.GL_MODELVIEW);
			if (pickInViewport) {
				setPickedIndex(selectedIndex);
			} else {
				setPicking(false);
			}
		}
	}

}
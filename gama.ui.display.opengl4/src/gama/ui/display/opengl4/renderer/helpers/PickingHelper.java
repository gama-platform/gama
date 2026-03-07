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

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;

import gama.api.ui.layers.IDrawingAttributes;
import gama.dev.DEBUG;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl4.renderer.IOpenGLRenderer;

/**
 * The Class PickingHelper.
 */
public class PickingHelper extends AbstractRendererHelper {

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
	// rendered with a unique flat colour encoding its index via
	// OpenGL#registerForSelection(int). After the scene is drawn we read back
	// the pixel under the mouse and decode the index.

	/** GL-coordinate (y-flipped) of the pixel to read back during endPicking(). */
	private int pickX, pickY;

	/**
	 * Begin picking. Records the cursor position and restricts the projection to a 4×4-pixel window around
	 * the click (replicates the old {@code gluPickMatrix} behaviour without requiring {@code GL_SELECT} mode).
	 */
	public void beginPicking() {
		final OpenGL openGL = getOpenGL();
		try {
			final CameraHelper camera = getRenderer().getCameraHelper();
			final int[] viewport = new int[4];
			getGL().glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			pickX = (int) camera.getMousePosition().getX();
			pickY = viewport[3] - (int) camera.getMousePosition().getY(); // GL y is bottom-up
			openGL.pushIdentity(GLMatrixFunc.GL_PROJECTION);
			final GLU glu = GLU.createGLU();
			glu.gluPickMatrix(pickX, pickY, 4, 4, viewport, 0);
		} catch (Throwable e) {
			DEBUG.ERR("in beginPicking", e);
		} finally {
			openGL.updatePerspective();
			openGL.matrixMode(GLMatrixFunc.GL_MODELVIEW);
		}
	}

	/**
	 * End picking. Reads the single pixel at the click position from the colour buffer. The object index is
	 * encoded in the red (low byte) and green (high byte) channels by
	 * {@link OpenGL#registerForSelection(int)}. A blue value of 0xFF signals the background (no object hit).
	 */
	public void endPicking() {
		final GL4 gl = getGL();
		final OpenGL openGL = getOpenGL();
		int selectedIndex = PickingHelper.NONE;
		try {
			final java.nio.ByteBuffer pixel = com.jogamp.common.nio.Buffers.newDirectByteBuffer(4);
			gl.glReadPixels(pickX, pickY, 1, 1, GL4.GL_RGBA, GL.GL_UNSIGNED_BYTE, pixel);
			final int r = pixel.get(0) & 0xFF;
			final int g = pixel.get(1) & 0xFF;
			final int b = pixel.get(2) & 0xFF;
			// Background: blue channel == 0xFF (objects encode b=0 for index < 65536)
			if (b == 0xFF) {
				selectedIndex = PickingHelper.WORLD;
			} else {
				selectedIndex = r + (g << 8);
			}
		} catch (Throwable e) {
			DEBUG.ERR("in endPicking", e);
		} finally {
			openGL.pop(GLMatrixFunc.GL_PROJECTION);
			openGL.matrixMode(GLMatrixFunc.GL_MODELVIEW);
			setPickedIndex(selectedIndex);
		}
	}

}
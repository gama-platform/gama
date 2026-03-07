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
	 * Begin picking. Records the cursor position and restricts the projection matrix to a 4×4-pixel
	 * window around the click, replicating the effect of the old {@code gluPickMatrix} but writing
	 * directly into the {@link OpenGL} {@link gama.ui.display.opengl4.renderer.shaders.MatrixStack}
	 * so it is honoured by the shader pipeline.
	 *
	 * <p>The {@code gluPickMatrix(x, y, w, h, viewport)} formula is equivalent to:</p>
	 * <pre>
	 *   translate( (viewport[2] - 2*(x - viewport[0])) / w,
	 *              (viewport[3] - 2*(y - viewport[1])) / h,
	 *              0 )
	 *   scale( viewport[2]/w, viewport[3]/h, 1 )
	 * </pre>
	 * <p>We apply this as a pre-multiplication on the projection stack <em>after</em>
	 * {@link OpenGL#updatePerspective()} has set the full-scene frustum, so only fragments
	 * within 4×4 pixels of the click are rendered.</p>
	 */
	public void beginPicking() {
		final OpenGL openGL = getOpenGL();
		try {
			final CameraHelper camera = getRenderer().getCameraHelper();
			final int[] viewport = new int[4];
			getGL().glGetIntegerv(com.jogamp.opengl.GL.GL_VIEWPORT, viewport, 0);
			pickX = (int) camera.getMousePosition().getX();
			pickY = viewport[3] - (int) camera.getMousePosition().getY(); // GL y is bottom-up

			// Set the full perspective projection first
			openGL.pushIdentity(GLMatrixFunc.GL_PROJECTION);
			openGL.updatePerspective();

			// Now pre-multiply the projection stack with the pick-window transform.
			// This is the direct JOML equivalent of gluPickMatrix(px, py, 4, 4, viewport).
			final float pw = 4f, ph = 4f; // pick-window size in pixels
			final float sx = viewport[2] / pw;
			final float sy = viewport[3] / ph;
			final float tx = (viewport[2] - 2f * (pickX - viewport[0])) / pw;
			final float ty = (viewport[3] - 2f * (pickY - viewport[1])) / ph;
			// Pre-multiply: P' = T * S * P  (applied left of the existing projection)
			openGL.getCurrentMatrixStack().translate(tx, ty, 0f);
			openGL.getCurrentMatrixStack().scale(sx, sy, 1f);

		} catch (final Throwable e) {
			DEBUG.ERR("in beginPicking", e);
		} finally {
			openGL.matrixMode(GLMatrixFunc.GL_MODELVIEW);
		}
	}

	/**
	 * End picking. Flushes the GPU pipeline, then reads the single pixel at the click position from the
	 * colour buffer. The object index is encoded in the red (low byte) and green (high byte) channels by
	 * {@link OpenGL#registerForSelection(int)}. A blue value of {@code 0xFF} signals the background.
	 *
	 * <p>{@link GL4#glFinish()} is called before {@link GL4#glReadPixels} to guarantee the picking-pass
	 * draw commands are complete before the readback, which is required on async/multi-threaded drivers.</p>
	 */
	public void endPicking() {
		final GL4 gl = getGL();
		final OpenGL openGL = getOpenGL();
		int selectedIndex = PickingHelper.NONE;
		try {
			// Ensure the picking pass is fully rendered before reading back
			gl.glFinish();
			final java.nio.ByteBuffer pixel = com.jogamp.common.nio.Buffers.newDirectByteBuffer(4);
			gl.glReadPixels(pickX, pickY, 1, 1, GL4.GL_RGBA, com.jogamp.opengl.GL.GL_UNSIGNED_BYTE, pixel);
			final int r = pixel.get(0) & 0xFF;
			final int g = pixel.get(1) & 0xFF;
			final int b = pixel.get(2) & 0xFF;
			// Background: blue channel == 0xFF (objects encode b=0 for index < 65536)
			if (b == 0xFF) {
				selectedIndex = PickingHelper.WORLD;
			} else {
				selectedIndex = r + (g << 8);
			}
		} catch (final Throwable e) {
			DEBUG.ERR("in endPicking", e);
		} finally {
			// Restore the projection matrix pushed by beginPicking()
			openGL.pop(GLMatrixFunc.GL_PROJECTION);
			openGL.matrixMode(GLMatrixFunc.GL_MODELVIEW);
			setPickedIndex(selectedIndex);
		}
	}

}
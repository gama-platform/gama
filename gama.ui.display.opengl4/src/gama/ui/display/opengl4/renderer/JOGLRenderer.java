package gama.ui.display.opengl4.renderer;

import java.util.Arrays;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLAutoDrawable;

import gama.api.ui.displays.IDisplaySurface;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.ui.display.opengl.IOpenGL;
import gama.ui.display.opengl.renderer.helpers.IKeystoneHelper;
import gama.ui.display.opengl.renderer.helpers.ILightHelper;
import gama.ui.display.opengl.renderer.helpers.IPickingHelper;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl4.renderer.helpers.KeystoneHelper;
import gama.ui.display.opengl4.renderer.helpers.LightHelper;
import gama.ui.display.opengl4.renderer.helpers.PickingHelper;

/** Core-profile backend of the regular OpenGL renderer. */
public class JOGLRenderer extends gama.ui.display.opengl.renderer.JOGLRenderer {

	private int startupDiagnosticFrames;

	public JOGLRenderer(final IDisplaySurface surface) {
		super(surface);
	}

	@Override
	protected IOpenGL createOpenGL() { return new OpenGL(this); }

	@Override
	protected IKeystoneHelper createKeystoneHelper() { return new KeystoneHelper(this); }

	@Override
	protected IPickingHelper createPickingHelper() { return new PickingHelper(this); }

	@Override
	protected ILightHelper createLightHelper() { return new LightHelper(this); }

	@Override
	public void display(final GLAutoDrawable drawable) {
		super.display(drawable);
		if (!GamaPreferences.Displays.OPENGL_DIAGNOSTICS.getValue() || startupDiagnosticFrames >= 3) return;
		startupDiagnosticFrames++;
		final GL gl = drawable.getGL();
		final int[] viewport = new int[4], framebuffer = new int[1], linked = new int[1];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		gl.glGetIntegerv(GL.GL_FRAMEBUFFER_BINDING, framebuffer, 0);
		final var shader = ((OpenGL) openGL).getBasicShader();
		final var animator = getCanvas().getAnimator();
		if (shader != null) {
			gl.getGL2ES2().glGetProgramiv(shader.getProgramID(), GL2ES2.GL_LINK_STATUS, linked, 0);
		}
		DEBUG.ERR("[OpenGL4 startup] frame=" + startupDiagnosticFrames + " initialized=" + inited
				+ " sceneReady=" + getSceneHelper().isReady() + " visible=" + getCanvas().getVisibleStatus()
				+ " surface=" + drawable.getSurfaceWidth() + "x" + drawable.getSurfaceHeight()
				+ " viewport=" + Arrays.toString(viewport) + " framebuffer=" + framebuffer[0]
				+ " animator=" + (animator == null ? "absent" : animator.getThread().getState())
				+ " renderThread=" + Thread.currentThread().getName()
				+ " autoSwap=" + drawable.getAutoSwapBufferMode() + " shaderLinked=" + linked[0]
				+ " glError=0x" + Integer.toHexString(gl.glGetError()));
	}
}

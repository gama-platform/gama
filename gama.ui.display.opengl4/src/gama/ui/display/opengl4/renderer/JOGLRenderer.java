package gama.ui.display.opengl4.renderer;

import gama.api.ui.displays.IDisplaySurface;
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
}

package gama.ui.display.opengl4.view;

import gama.annotations.display;
import gama.annotations.doc;
import gama.api.ui.IOutput;
import gama.ui.display.opengl4.renderer.JOGLRenderer;

/** Display registration and renderer selection for the core-profile backend. */
@display (value = { "opengl4" }, is3D = true)
@doc ("Displays that uses the OpenGL technology to display their layers in 3D")
public class SWTOpenGLDisplaySurface extends gama.ui.display.opengl.view.SWTOpenGLDisplaySurface {

	public SWTOpenGLDisplaySurface(final IOutput.Display output, final Object parent) {
		super(output, parent);
	}

	@Override
	protected JOGLRenderer createRenderer() { return new JOGLRenderer(this); }
}

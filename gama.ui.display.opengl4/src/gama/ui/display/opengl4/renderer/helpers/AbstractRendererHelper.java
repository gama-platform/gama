package gama.ui.display.opengl4.renderer.helpers;

import com.jogamp.opengl.GL4;

import gama.ui.display.opengl.renderer.IOpenGLRenderer;
import gama.ui.display.opengl4.OpenGL;

/** Typed access for helpers that belong exclusively to the core-profile backend. */
public abstract class AbstractRendererHelper
		extends gama.ui.display.opengl.renderer.helpers.AbstractRendererHelper {

	public AbstractRendererHelper(final IOpenGLRenderer renderer) {
		super(renderer);
	}

	@Override
	protected GL4 getGL() { return getOpenGL().getGL(); }

	@Override
	protected OpenGL getOpenGL() { return (OpenGL) super.getOpenGL(); }
}

package gama.ui.display.opengl.renderer.helpers;

import com.jogamp.opengl.GL2;

import gama.ui.display.opengl.OpenGL;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;

/** Typed access for helpers that belong exclusively to the fixed-function backend. */
public abstract class GL2RendererHelper extends AbstractRendererHelper {

	protected GL2RendererHelper(final IOpenGLRenderer renderer) {
		super(renderer);
	}

	@Override
	protected GL2 getGL() { return getOpenGL().getGL(); }

	@Override
	protected OpenGL getOpenGL() { return (OpenGL) super.getOpenGL(); }
}

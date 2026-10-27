package gama.ui.display.opengl4.view;

import org.eclipse.swt.widgets.Control;

import gama.api.utils.interfaces.IDisposable;

/** Core-profile view retaining its own Eclipse ID and focus behaviour. */
public class OpenGLDisplayView extends gama.ui.display.opengl.view.OpenGLDisplayView {

	public static String ID = "gama.ui.application.view.OpenGLDisplayView2";

	@Override
	protected IDisposable createNativeMultiListener() {
		return new NEWTLayeredDisplayMultiListener(decorator, getDisplaySurface(), getGLCanvas().getNEWTWindow());
	}

	@Override
	public void setFocus() {
		final Control control = getSurfaceComposite();
		if (control != null && !control.isDisposed() && !control.isFocusControl()) { control.setFocus(); }
	}
}

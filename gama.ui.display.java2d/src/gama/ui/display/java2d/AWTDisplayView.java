/*******************************************************************************************************
 *
 * AWTDisplayView.java, in gama.ui.display.java2d, is part of the source code of the GAMA modeling and simulation
 * platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.java2d;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gama.core.common.interfaces.IDisposable;
import gama.core.runtime.PlatformHelper;
import gama.ui.display.java2d.swing.SwingControl;
import gama.ui.experiment.views.displays.LayeredDisplayView;
import gama.ui.experiment.views.displays.SWTLayeredDisplayMultiListener;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class AWTDisplayView.
 */
public class AWTDisplayView extends LayeredDisplayView {

	@Override
	protected Composite createSurfaceComposite(final Composite parent) {
		if (getOutput() == null) return null;
		surfaceComposite = SwingControl.create(parent, AWTDisplayView.this, getDisplaySurface(), SWT.NO_FOCUS);
		return surfaceComposite;
	}

	@Override
	public Java2DDisplaySurface getDisplaySurface() { return (Java2DDisplaySurface) super.getDisplaySurface(); }

	@Override
	public void ownCreatePartControl(final Composite c) {
		super.ownCreatePartControl(c);
		if (getOutput().getData().fullScreen() > -1) {
			new Thread(() -> { WorkbenchHelper.runInUI("FS", 1000, m -> toggleFullScreen()); }).start();
		}
	}

	@Override
	public void setFocus() {
		// Uncommenting this method seems to fix #3325. Should be tested !
		// DEBUG.OUT("Part " + getTitle() + " gaining focus");
		if (getParentComposite() != null && !getParentComposite().isDisposed()
				&& !getParentComposite().isFocusControl()) {
			getParentComposite().forceFocus(); // Necessary ?
		}
	}

	@Override
	public void focusCanvas() {
		WorkbenchHelper.asyncRun(() -> centralPanel.forceFocus());
	}

	@Override
	public IDisposable getMultiListener() {
		SWTLayeredDisplayMultiListener listener = (SWTLayeredDisplayMultiListener) super.getMultiListener();
		if (PlatformHelper.isMac() || PlatformHelper.isLinux()) {
			// See Issue #3426
			SwingControl control = (SwingControl) surfaceComposite;
			control.setKeyListener(listener.getKeyAdapterForAWT());
			if (PlatformHelper.isLinux()) { control.setMouseListener(listener.getMouseAdapterForAWT()); }
		}
		return listener;
	}

	@Override
	public boolean is2D() {
		return true;
	}

	@Override
	public boolean isLocked() { return getDisplaySurface().isLocked(); }

}
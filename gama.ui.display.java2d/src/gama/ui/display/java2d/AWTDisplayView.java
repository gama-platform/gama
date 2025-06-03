/*******************************************************************************************************
 *
 * AWTDisplayView.java, in gama.ui.display.java2d, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.java2d;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import gama.core.common.interfaces.IDisposable;
import gama.dev.DEBUG;
import gama.ui.display.java2d.swing.SwingControl;
import gama.ui.experiment.views.displays.LayeredDisplayView;
import gama.ui.experiment.views.displays.SWTLayeredDisplayMultiListener;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class AWTDisplayView.
 */
public class AWTDisplayView extends LayeredDisplayView {

	static {
		DEBUG.ON();
	}

	static IPartListener MyPartListener = new IPartListener() {
		@Override
		public void partActivated(IWorkbenchPart part) {
			DEBUG.OUT("Part activated: " + part.getTitle());
			// if (PlatformHelper.isWindows()) {
			// if (part instanceof AWTDisplayView av) {
			//
			// WorkbenchHelper.runInUI("", 20, m -> av.setFocus());
			//
			// }
			// }
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
			DEBUG.OUT("Part brought to top: " + part.getTitle());
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
			DEBUG.OUT("Part closed: " + part.getTitle());
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
			DEBUG.OUT("Part deactivated: " + part.getTitle());
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
			DEBUG.OUT("Part opened: " + part.getTitle());
		}
	};

	@Override
	protected Composite createSurfaceComposite(final Composite parent) {
		WorkbenchHelper.getPage().addPartListener(MyPartListener);
		if (getOutput() == null) { return null; }
		surfaceComposite = SwingControl.create(parent, AWTDisplayView.this, getDisplaySurface(), SWT.NO_FOCUS);
		return surfaceComposite;
	}

	@Override
	public Java2DDisplaySurface getDisplaySurface() { return (Java2DDisplaySurface) super.getDisplaySurface(); }

	@Override
	public void ownCreatePartControl(final Composite c) {
		super.ownCreatePartControl(c);
		if (getOutput().getData().fullScreen() > -1) {
			new Thread(() -> { WorkbenchHelper.runInUI("Expand " + this.getTitle(), 1000, m -> toggleFullScreen()); })
					.start();
		}
	}

	@Override
	public void setFocus() {
		// Uncommenting this method seems to fix #3325. Should be tested !
		// Getting the focus (through this method) seems to enable keyboard events on Windows.
		DEBUG.OUT("Part " + getTitle() + " gaining focus");
		// if (getParentComposite() != null && !getParentComposite().isDisposed()
		// && !getParentComposite().isFocusControl()) {
		// getParentComposite().forceFocus(); // Necessary ?
		// }

		// ViewsHelper.activate(this);
		if (centralPanel != null && !centralPanel.isDisposed() && !centralPanel.isFocusControl()) {
			centralPanel.forceFocus(); // Necessary ?
		}
	}

	@Override
	public void focusCanvas() {
		WorkbenchHelper.asyncRun(() -> centralPanel.forceFocus());
	}

	@Override
	public IDisposable getMultiListener() {
		SWTLayeredDisplayMultiListener listener = (SWTLayeredDisplayMultiListener) super.getMultiListener();
		centralPanel.addKeyListener(listener);
		// See Issue #3426
		SwingControl control = (SwingControl) surfaceComposite;
		control.addKeyListener(listener);
		control.setKeyListener(listener.getKeyAdapterForAWT());
		control.setMouseListener(listener.getMouseAdapterForAWT());
		return listener;
	}

	@Override
	public boolean is2D() {
		return true;
	}

	@Override
	public boolean isCameraLocked() { return getDisplaySurface().isCameraLocked(); }

	@Override
	public boolean isCameraDynamic() { return getDisplaySurface().isCameraDynamic(); }

	@Override
	public boolean largePauseIcon() {
		return true;
	}

}
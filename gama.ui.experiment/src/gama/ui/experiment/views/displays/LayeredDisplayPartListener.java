/**
 *
 */
package gama.ui.experiment.views.displays;

import java.util.Objects;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import gama.api.ui.displays.IDisplaySurface;
import gama.dev.DEBUG;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 *
 */
final class LayeredDisplayPartListener implements IPartListener2 {

	/**
	 *
	 */
	private final LayeredDisplayDecorator decorator;

	/**
	 * @param layeredDisplayDecorator
	 */
	LayeredDisplayPartListener(final LayeredDisplayDecorator layeredDisplayDecorator) {
		decorator = layeredDisplayDecorator;
	}

	/**
	 * Ok.
	 *
	 * @param partRef
	 *            the part ref
	 * @return true, if successful
	 */
	private boolean ok(final IWorkbenchPartReference partRef) {
		return Objects.equals(partRef.getTitle(), decorator.view.getTitle()) && decorator.view.surfaceComposite != null
				&& !decorator.view.surfaceComposite.isDisposed() && !decorator.isFullScreen();
	}

	@Override
	public void partActivated(final IWorkbenchPartReference partRef) {
		if (ok(partRef)) {
			DEBUG.OUT("partActivated: " + partRef.getTitle());
			WorkbenchHelper.asyncRun(() -> {
				decorator.view.showCanvas();
				if (decorator.overlay != null) { decorator.overlay.display(); }
			});
		}
	}

	@Override
	public void partClosed(final IWorkbenchPartReference partRef) {
		if (ok(partRef) && decorator.overlay != null) {
			DEBUG.OUT("partClosed: " + partRef.getTitle());
			decorator.overlay.close();
		}
	}

	@Override
	public void partDeactivated(final IWorkbenchPartReference partRef) {}

	@Override
	public void partHidden(final IWorkbenchPartReference partRef) {
		// On macOS, this event is wrongly sent when tabs are not displayed for the views and another display is
		// selected. After tests, the same happens on Linux and Windows -- so the test is generalized.
		if (!PerspectiveHelper.keepTabs()) return;
		if (ok(partRef)) {
			DEBUG.OUT("partHidden: " + partRef.getTitle());
			WorkbenchHelper.asyncRun(() -> {
				decorator.view.hideCanvas();
				if (decorator.overlay != null) { decorator.overlay.hide(); }
			});
		}
	}

	@Override
	public void partVisible(final IWorkbenchPartReference partRef) {
		if (ok(partRef)) {
			DEBUG.OUT("partVisible: " + partRef.getTitle());
			WorkbenchHelper.asyncRun(() -> {
				decorator.view.showCanvas();
				IDisplaySurface s = decorator.view.getDisplaySurface();
				if (s != null) { s.getOutput().update(); }
				if (decorator.overlay != null) { decorator.overlay.display(); }
			});
		}
	}
}
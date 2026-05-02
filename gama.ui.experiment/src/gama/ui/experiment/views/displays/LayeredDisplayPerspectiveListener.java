/**
 *
 */
package gama.ui.experiment.views.displays;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;

import gama.api.runtime.SystemInfo;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.utils.prefs.GamaPreferences;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 *
 */
final class LayeredDisplayPerspectiveListener implements IPerspectiveListener {

	/**
	 *
	 */
	private final LayeredDisplayDecorator decorator;

	/**
	 * @param layeredDisplayDecorator
	 */
	LayeredDisplayPerspectiveListener(final LayeredDisplayDecorator layeredDisplayDecorator) {
		decorator = layeredDisplayDecorator;
	}

	/** The previous state. */
	boolean previousState = false;

	@Override
	public void perspectiveChanged(final IWorkbenchPage page, final IPerspectiveDescriptor perspective,
			final String changeId) {}

	@Override
	public void perspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {

		if (PerspectiveHelper.PERSPECTIVE_MODELING_ID.equals(perspective.getId())) {
			if (decorator.view.getOutput() != null && decorator.view.getDisplaySurface() != null
					&& !GamaPreferences.Displays.CORE_DISPLAY_PERSPECTIVE.getValue()) {
				previousState = decorator.view.getOutput().isPaused();
				decorator.view.getOutput().setPaused(true);
			}
			// Seems necessary in addition to the IPartListener
			WorkbenchHelper.asyncRun(() -> {
				if (SystemInfo.isMac() && decorator.overlay != null) { decorator.overlay.hide(); }
				decorator.view.hideCanvas();
			});
		} else {
			// Issue #2639
			if (SystemInfo.isMac() && !decorator.view.isOpenGL()) {
				final IDisplaySurface ds = decorator.view.getDisplaySurface();
				if (ds != null) { ds.updateDisplay(true); }
			}
			if (!GamaPreferences.Displays.CORE_DISPLAY_PERSPECTIVE.getValue() && decorator.view.getOutput() != null
					&& decorator.view.getDisplaySurface() != null) {
				decorator.view.getOutput().setPaused(previousState);
			}
			// Necessary in addition to the IPartListener as there is no way to distinguish between the wrong
			// "hidden" event and the good one when there are no tabs.
			WorkbenchHelper.asyncRun(() -> {
				if (SystemInfo.isMac() && decorator.overlay != null) { decorator.overlay.display(); }
				decorator.view.showCanvas();
			});
		}

	}
}
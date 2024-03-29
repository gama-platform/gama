/*******************************************************************************************************
 *
 * OutputPartsManager.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import gama.core.common.interfaces.IGamaView;
import gama.core.outputs.IOutput;
import gama.dev.DEBUG;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * Class OutputPartsManager.
 *
 * @author drogoul
 * @since 28 déc. 2015
 *
 */
public class OutputPartsManager {

	static {
		DEBUG.OFF();
	}

	/**
	 * The listener interface for receiving part events. The class that is interested in processing a part event
	 * implements this interface, and the object created with that class is registered with a component using the
	 * component's <code>addPartListener<code> method. When the part event occurs, that object's appropriate method is
	 * invoked.
	 *
	 * @see PartEvent
	 */
	public static class PartListener implements IPartListener2 {

		@Override
		public void partActivated(final IWorkbenchPartReference partRef) {
			// final IWorkbenchPart part = partRef.getPart(false);
			// if (part instanceof IGamaView.Display) { DEBUG.LOG("Part Activated: " + part.getTitle()); }
		}

		@Override
		public void partClosed(final IWorkbenchPartReference partRef) {
			final IWorkbenchPart part = partRef.getPart(false);
			if (part instanceof IGamaView view) {
				// DEBUG.LOG("Part Closed:" + part.getTitle());
				final IOutput output = view.getOutput();
				if (output != null) {
					output.setPaused(true);
					output.close();
				}
			}
		}

		@Override
		public void partDeactivated(final IWorkbenchPartReference partRef) {}

		@Override
		public void partOpened(final IWorkbenchPartReference partRef) {
			final IWorkbenchPart part = partRef.getPart(false);
			if (part instanceof IGamaView view) {
				// DEBUG.LOG("Part Opened:" + part.getTitle());
				final IOutput output = view.getOutput();
				if (output != null && !output.isOpen()) {
					output.open();
					output.setPaused(false);
				}
			}

		}

		@Override
		public void partBroughtToTop(final IWorkbenchPartReference partRef) {
			// final IWorkbenchPart part = partRef.getPart(false);
			// if (part instanceof IGamaView.Display) { DEBUG.LOG("Part Brought to top: " + part.getTitle()); }
		}

		@Override
		public void partHidden(final IWorkbenchPartReference partRef) {
			// final IWorkbenchPart part = partRef.getPart(false);
			// if (part instanceof IGamaView.Display display) {
			// // See issue #3318
			// if (PlatformHelper.isMac() && display.hasTab()) {
			// WorkbenchHelper.asyncRun(() -> display.hideCanvas());
			// }
			// DEBUG.LOG("Part Hidden: " + part.getTitle());
			// }
		}

		@Override
		public void partVisible(final IWorkbenchPartReference partRef) {
			// final IWorkbenchPart part = partRef.getPart(false);
			// See issue #3318
			// if (part instanceof IGamaView.Display display) {
			// if (PlatformHelper.isMac() && display.hasTab()) {
			// WorkbenchHelper.asyncRun(() -> display.showCanvas());
			// }
			// DEBUG.LOG("Part Visible: " + part.getTitle());
			// }
		}

		@Override
		public void partInputChanged(final IWorkbenchPartReference partRef) {}
	}

	/** The Constant listener. */
	private final static PartListener listener = new PartListener();

	/**
	 * Install.
	 */
	public static void install() {
		WorkbenchHelper.getPage().addPartListener(listener);

	}

}

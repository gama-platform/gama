/*******************************************************************************************************
 *
 * ApplicationWorkbenchWindowAdvisor.java, in gama.ui.application, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.application.workbench;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchWindowAdvisor;

import gama.api.runtime.SystemInfo;
import gama.dev.DEBUG;

/**
 * The Class ApplicationWorkbenchWindowAdvisor.
 */
public class ApplicationWorkbenchWindowAdvisor extends IDEWorkbenchWindowAdvisor {

	static {
		DEBUG.OFF();
	}

	@Override
	public IStatus saveState(final IMemento memento) {
		return super.saveState(memento);
	}

	@Override
	public IStatus restoreState(final IMemento memento) {

		return super.restoreState(memento);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(final IActionBarConfigurer configurer) {
		return new GamaActionBarAdvisor(configurer);
	}

	/**
	 * Instantiates a new application workbench window advisor.
	 *
	 * @param adv
	 *            the adv
	 * @param configurer
	 *            the configurer
	 */
	public ApplicationWorkbenchWindowAdvisor(final ApplicationWorkbenchAdvisor adv,
			final IWorkbenchWindowConfigurer configurer) {
		super(adv, configurer);
	}

	@Override
	public void preWindowOpen() {
		super.preWindowOpen();
		final IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

		configurer.getWindow().addPerspectiveListener(new IPerspectiveListener() {

			@Override
			public void perspectiveChanged(final IWorkbenchPage page, final IPerspectiveDescriptor perspective,
					final String changeId) {}

			/**
			 * Running the perspective listener to automatically launch modeling at startup in case a simulation
			 * perspective is remembered.
			 *
			 * @param page
			 * @param perspective
			 */
			@Override
			public void perspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
				if (PerspectiveHelper.isSimulationPerspective()) {
					final IPerspectiveDescriptor desc = page.getPerspective();
					page.closePerspective(desc, false, false);
					PerspectiveHelper.openModelingPerspective(true, false);
				}
				configurer.getWindow().removePerspectiveListener(this);

			}
		});
		configurer.getWindow().addPageListener(new IPageListener() {

			/**
			 * Running the perspective listener to automatically launch modeling at startup.
			 *
			 * @param page
			 */
			@Override
			public void pageActivated(final IWorkbenchPage page) {
				configurer.getWindow().removePageListener(this);
				PerspectiveHelper.openModelingPerspective(true, false);
			}

			@Override
			public void pageClosed(final IWorkbenchPage page) {}

			@Override
			public void pageOpened(final IWorkbenchPage page) {}
		});
		configurer.setShowMenuBar(true);
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		configurer.setShowPerspectiveBar(false);
		configurer.setTitle(SystemInfo.VERSION);

		PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR, true);
		ThemeHelper.applyCSSWhenWindowOpens();
		Resource.setNonDisposeHandler(null);
	}

	@Override
	public void postWindowRestore() throws WorkbenchException {}

	@Override
	public void postWindowCreate() {
		final IWorkbenchWindow window = getWindowConfigurer().getWindow();
		PerspectiveHelper.showBottomTray((WorkbenchWindow) window, false);
	}

	@Override
	public void postWindowOpen() {
		PerspectiveHelper.cleanPerspectives();
	}

}

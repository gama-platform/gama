package gama.ui.export.launcher;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/** The idle workbench page of an exported playable model. */
public final class LauncherHomePerspective implements IPerspectiveFactory {

	public static final String ID = "gama.ui.export.perspectives.Home";

	@Override
	public void createInitialLayout(final IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.addStandaloneView(LauncherHomeView.ID, false, IPageLayout.LEFT, 1f, layout.getEditorArea());
		layout.getViewLayout(LauncherHomeView.ID).setCloseable(false);
		layout.getViewLayout(LauncherHomeView.ID).setMoveable(false);
	}
}

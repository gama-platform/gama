/*******************************************************************************************************
 *
 * PreviousVersionsView.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ui.history.GenericHistoryView;
import org.eclipse.team.internal.ui.history.LocalHistoryPageSource;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryPageSource;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.Page;

import gama.api.GAMA;
import gama.ui.shared.utils.WorkbenchHelper;

/** Local versions, always following the selected file or active editor. */
public class PreviousVersionsView extends GenericHistoryView {

	public static final String ID = "gama.ui.navigator.view.PreviousVersions";

	private static final IHistoryPageSource SOURCE = new LocalHistoryPageSource() {
		@Override
		public Page createPage(final Object object) {
			return new PreviousVersionsPage();
		}
	};

	public static void show(final IFile file) {
		try {
			final IWorkbenchPage page = WorkbenchHelper.getPage();
			final PreviousVersionsView view = (PreviousVersionsView) page.showView(ID);
			view.showHistoryFor(file, true);
		} catch (final PartInitException e) {
			GAMA.getGui().getDialogFactory().error(e.getMessage());
		}
	}

	@Override
	public void setContentDescription(final String description) {
		super.setContentDescription("");
	}

	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);
		setLinkingEnabled(true);
		removeToolbar();
		editorActivated(getSite().getPage().getActiveEditor());
	}

	@Override
	public IHistoryPage showHistoryPageFor(final Object object, final boolean refresh, final boolean force,
			final IHistoryPageSource source) {
		final IFile file = LocalHistoryPageSource.getFile(object);
		if (file == null) return null;
		return super.showHistoryPageFor(file, true, true, SOURCE);
	}

	@Override
	protected void editorActivated(final IEditorPart editor) {
		if (editor == null || !getSite().getPage().isPartVisible(this)) return;
		final IFile file = ResourceUtil.getFile(editor.getEditorInput());
		// Revision and compare editors must not replace the current workspace file.
		if (file != null) { showHistoryFor(file, true); }
	}

	@Override
	public void partBroughtToTop(final IWorkbenchPart part) {
		if (part instanceof IEditorPart editor) { editorActivated(editor); }
	}

	@Override
	public void partVisible(final IWorkbenchPart part) {
		super.partVisible(part);
		if (part == this) { editorActivated(getSite().getPage().getActiveEditor()); }
	}

	@Override
	protected void showPageRec(final PageRec pageRec) {
		super.showPageRec(pageRec);
		final var bars = getPageSite(pageRec.page).getActionBars();
		bars.getToolBarManager().removeAll();
		bars.getMenuManager().removeAll();
		bars.updateActionBars();
		removeToolbar();
	}

	private void removeToolbar() {
		final var bars = getViewSite().getActionBars();
		bars.getToolBarManager().removeAll();
		bars.getMenuManager().removeAll();
		bars.updateActionBars();
	}
}

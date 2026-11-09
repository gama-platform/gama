/*******************************************************************************************************
 *
 * PreviousVersionsPage.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.actions.CompareRevisionAction;
import org.eclipse.team.internal.ui.actions.OpenRevisionAction;
import org.eclipse.team.internal.ui.history.FileRevisionEditorInput;
import org.eclipse.team.internal.ui.history.LocalHistoryPage;
import org.eclipse.team.internal.ui.history.DateHistoryCategory;
import org.eclipse.ui.PlatformUI;

import gama.api.GAMA;
import gama.ui.shared.resources.GamaIcon;

/** Retains Eclipse's history retrieval, comparison and safe replacement actions. */
public class PreviousVersionsPage extends LocalHistoryPage {

	private Tree versions;

	private final IPropertyChangeListener themeListener = event -> refreshIcons();

	public PreviousVersionsPage() {
		super(ON);
	}

	@Override
	public void createControl(final Composite parent) {
		TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.PREF_GROUPBYDATE_MODE, true);
		super.createControl(parent, false);
		for (final var child : ((Composite) getControl()).getChildren()) {
			if (child instanceof Tree tree) {
				versions = tree;
				versions.setHeaderVisible(false);
				versions.addListener(SWT.Expand, event -> refreshIcons());
				break;
			}
		}
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(themeListener);
		getSite().getActionBars().getToolBarManager().removeAll();
		getSite().getActionBars().updateActionBars();
	}

	@Override
	protected void update(final IFileRevision[] revisions, final IProgressMonitor monitor) {
		super.update(revisions, monitor);
		// Eclipse queues the tree update; decorate its items after that update completes.
		refreshIcons();
	}

	private void refreshIcons() {
		final Tree tree = versions;
		if (tree == null || tree.isDisposed()) return;
		tree.getDisplay().asyncExec(() -> {
			if (!tree.isDisposed()) { updateIcons(tree.getItems()); }
		});
	}

	private void updateIcons(final TreeItem[] items) {
		for (final TreeItem item : items) {
			if (item.getData() instanceof DateHistoryCategory) {
				item.setImage(GamaIcon.named("navigator/history.date").image());
			} else if (item.getData() instanceof IFileRevision) {
				item.setImage(GamaIcon.named("navigator/history.version").image());
			}
			updateIcons(item.getItems());
		}
	}

	@Override
	public void dispose() {
		PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(themeListener);
		super.dispose();
	}

	@Override
	public void setClickAction(final boolean compare) {
		super.setClickAction(true);
	}

	@Override
	protected void fillTableMenu(final IMenuManager manager) {
		final MenuManager original = new MenuManager();
		super.fillTableMenu(original);
		for (final var item : original.getItems()) {
			if (!(item instanceof ActionContributionItem contribution)) continue;
			final var action = contribution.getAction();
			if (action instanceof OpenRevisionAction open) {
				final Action textEditor = new Action("Open in text editor") {
					@Override
					public void run() {
						if (!(open.getStructuredSelection().getFirstElement() instanceof IFileRevision revision))
							return;
						try {
							final var input = FileRevisionEditorInput.createEditorInputFor(revision,
									new NullProgressMonitor());
							getSite().getPage().openEditor(input, "org.eclipse.ui.DefaultTextEditor", true);
						} catch (final CoreException e) {
							GAMA.getGui().getDialogFactory().error(e.getMessage());
						}
					}
				};
				textEditor.setEnabled(open.isEnabled());
				manager.add(textEditor);
			} else if (action instanceof CompareRevisionAction) {
				action.setText("Compare with current");
				manager.add(action);
			} else {
				action.setText("Replace current");
				manager.add(action);
			}
		}
	}
}

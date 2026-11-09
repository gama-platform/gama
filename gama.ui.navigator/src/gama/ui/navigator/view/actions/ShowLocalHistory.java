/*******************************************************************************************************
 *
 * ShowLocalHistory.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.actions.WorkspaceAction;

import gama.ui.navigator.view.PreviousVersionsView;
import gama.ui.navigator.view.contents.ResourceManager;

/**
 * The Class ShowLocalHistory.
 */
public class ShowLocalHistory extends WorkspaceAction {

	/** The is file. */
	boolean isFile;

	/**
	 * Instantiates a new show local history.
	 *
	 * @param provider
	 *            the provider
	 */
	protected ShowLocalHistory(final IShellProvider provider) {
		super(provider, "Previous versions...");
	}

	/** The selection. */
	private IStructuredSelection fSelection;

	@Override
	public void run() {
		if (!isFile) return;
		final IFile file = ResourceManager.getFile(getSelection().getFirstElement());
		if (file != null) { PreviousVersionsView.show(file); }
	}

	@Override
	protected boolean updateSelection(final IStructuredSelection sel) {
		fSelection = sel;
		isFile = sel.size() == 1 && selectionIsOfType(IResource.FILE);
		return isFile;
	}

	/**
	 * Gets the selection.
	 *
	 * @return the selection
	 */
	public IStructuredSelection getSelection() { return fSelection; }

	/**
	 * Gets the prompt title.
	 *
	 * @return the prompt title
	 */
	protected String getPromptTitle() { return "Previous versions"; }

	@Override
	protected String getOperationMessage() { return "Previous versions"; }

}

/*******************************************************************************************************
 *
 * CompareWithEachOtherAction.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.view.actions;

import org.eclipse.compare.internal.CompareAction;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.actions.WorkspaceAction;

import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class CompareWithEachOtherAction.
 */
public class CompareWithEachOtherAction extends WorkspaceAction {

	/** The is enabled. */
	boolean isEnabled;
	
	/** The action. */
	protected CompareAction action;

	/**
	 * Instantiates a new compare with each other action.
	 *
	 * @param provider the provider
	 */
	protected CompareWithEachOtherAction(final IShellProvider provider) {
		super(provider, "Compare");
		action = new CompareAction();
		action.setActivePart(this, WorkbenchHelper.getActivePart());
	}

	/** The selection. */
	private IStructuredSelection fSelection;

	@Override
	public void run() {
		action.run(fSelection);
	}

	@Override
	protected boolean updateSelection(final IStructuredSelection sel) {
		fSelection = sel;
		isEnabled = sel.size() == 2 && selectionIsOfType(IResource.FILE);
		action.selectionChanged(this, sel);
		return isEnabled;
	}

	/**
	 * Gets the selection.
	 *
	 * @return the selection
	 */
	public IStructuredSelection getSelection() {
		return fSelection;
	}

	@Override
	protected String getOperationMessage() {
		return "Compare";
	}

}

/*******************************************************************************************************
 *
 * DelegateForAllElements.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class DelegateForAllElements.
 */
public class DelegateForAllElements implements IWorkbenchWindowActionDelegate {

	@Override
	public void run(final IAction action) {
		WorkbenchHelper.runCommand("org.eclipse.xtext.ui.shared.OpenXtextElementCommand");
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {}

	@Override
	public void dispose() {}

	@Override
	public void init(final IWorkbenchWindow window) {}

}

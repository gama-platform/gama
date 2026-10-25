/*******************************************************************************************************
 *
 * OpenNewWizardHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Opens one of the "new" wizards declared in plugin.xml, identified by the {@value #WIZARD} parameter.
 */
public class OpenNewWizardHandler extends AbstractHandler {

	/** The id of the command parameter. */
	public static final String WIZARD = "gama.ui.devtools.wizardId";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		String id = event.getParameter(WIZARD);
		IWizardDescriptor descriptor = PlatformUI.getWorkbench().getNewWizardRegistry().findWizard(id);
		if (descriptor == null) throw new ExecutionException("Unknown wizard: " + id);
		try {
			IWorkbenchWizard wizard = descriptor.createWizard();
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (wizard instanceof INewWizard) {
				wizard.init(PlatformUI.getWorkbench(), selection instanceof IStructuredSelection ss ? ss
						: StructuredSelection.EMPTY);
			}
			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			dialog.create();
			dialog.open();
		} catch (CoreException e) {
			throw new ExecutionException("Cannot open wizard " + id, e);
		}
		return null;
	}

}

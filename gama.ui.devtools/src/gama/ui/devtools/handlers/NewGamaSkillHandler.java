/*******************************************************************************************************
 *
 * NewGamaSkillHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.devtools.wizards.NewGamaSkillWizard;

/**
 * Command handler that opens the {@link NewGamaSkillWizard} dialog.
 *
 * @author GAMA Development Team
 * @since 2026
 */
public class NewGamaSkillHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 *
	 * @param event
	 *            the execution event
	 * @return {@code null} always
	 * @throws ExecutionException
	 *             if the active shell cannot be resolved
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShellChecked(event), new NewGamaSkillWizard());
		dialog.open();
		return null;
	}

}

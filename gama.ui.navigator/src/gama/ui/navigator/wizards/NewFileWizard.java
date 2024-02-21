/*******************************************************************************************************
 *
 * NewFileWizard.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.wizards;

import org.eclipse.jface.viewers.ISelection;

/**
 * The Class NewFileWizard.
 */
public class NewFileWizard extends AbstractNewModelWizard {

	@Override
	public AbstractNewModelWizardPage createPage(final ISelection selection) {
		return new NewFileWizardPage(selection);
	}

	@Override
	protected String getDefaultFolderForModels() {
		return "models";
	}

}
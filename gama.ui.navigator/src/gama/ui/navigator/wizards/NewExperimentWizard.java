/*******************************************************************************************************
 *
 * NewExperimentWizard.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.INewWizard;

import gama.gaml.operators.Strings;

/**
 * The Class NewExperimentWizard.
 */
public class NewExperimentWizard extends AbstractNewModelWizard implements INewWizard {

	@Override
	protected String getHeader(final IContainer folder, final String str, final String title, final String author,
			final String desc) {
		final IResource model =
				ResourcesPlugin.getWorkspace().getRoot().findMember(getPage().getExperimentedModelPath());
		final IPath pathToModel;
		if (model == null || model.getType() != IResource.FILE) {
			pathToModel = null;
		} else {
			pathToModel = model.getFullPath().makeRelativeTo(folder.getFullPath());
		}
		final String header = super.getHeader(folder, str, title, author, desc);
		final String result = pathToModel == null ? header.replace("model:$MODEL$", "")
				: header.replaceAll("\\$MODEL\\$", "'" + pathToModel + "'");
		return result.replaceAll("\\$TYPE\\$", Strings.toLowerCase(getPage().getType()));
	}

	@Override
	public AbstractNewModelWizardPage createPage(final ISelection selection) {
		return new NewExperimentWizardPage(selection);
	}

	@Override
	public NewExperimentWizardPage getPage() {
		return (NewExperimentWizardPage) page;
	}

	@Override
	protected String getDefaultFolderForModels() {
		return "models";
	}

}
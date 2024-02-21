/*******************************************************************************************************
 *
 * HideShowEditors.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class HideShowEditors.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 26 juin 2023
 */
public class HideShowEditors extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		WorkbenchHelper.getPage().setEditorAreaVisible(!WorkbenchHelper.getPage().isEditorAreaVisible());
		return null;
	}

}

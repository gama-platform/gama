/*******************************************************************************************************
 *
 * OpenGamaWebsiteHandler.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;

import gama.core.runtime.GAMA;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class OpenGamaWebsiteHandler.
 */
public class OpenGamaWebsiteHandler extends AbstractHandler {

	/**
	 * Method execute()
	 *
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		//GAMA.getGui().openWebDocumentationPage();
		try {
			WorkbenchHelper.getPage().showView("gama.ui.display.chart");
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}

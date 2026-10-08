/*******************************************************************************************************
 *
 * AbstractJsonEditorHandler.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import gama.ui.viewers.json.eclipse.JsonEditor;

/**
 * The Class AbstractJsonEditorHandler.
 */
public abstract class AbstractJsonEditorHandler extends AbstractHandler {

	/**
	 * Instantiates a new abstract highspeed JSON editor handler.
	 */
	public AbstractJsonEditorHandler() {
	}

	/**
	 * Execute something by using gradle editor instance
	 * 
	 * @param jsonEditor
	 *            - never <code>null</code>
	 */
	protected abstract void executeOnJsonEditor(JsonEditor jsonEditor);

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) return null;
		IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) return null;
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) return null;
		IEditorPart editor = activePage.getActiveEditor();

		if (editor instanceof JsonEditor) { executeOnJsonEditor((JsonEditor) editor); }
		return null;
	}

}
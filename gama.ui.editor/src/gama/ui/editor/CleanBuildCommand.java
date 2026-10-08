/*******************************************************************************************************
 *
 * CleanBuildCommand.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.editor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import gama.api.GAMA;
import gaml.compiler.indexer.GamlResourceIndexer;

/**
 * The Class CleanBuildCommand.
 */
public class CleanBuildCommand implements IHandler {

	@Override
	public void addHandlerListener(final IHandlerListener handlerListener) {}

	@Override
	public void dispose() {}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		final IWorkspace workspace = GAMA.getWorkspaceManager().getWorkspace();
		try {
			GamlResourceIndexer.eraseIndex();
			workspace.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
		} catch (final CoreException ex) {
			ex.printStackTrace();
		}
		return this;
	}

	@Override
	public boolean isEnabled() { return true; }

	@Override
	public boolean isHandled() { return true; }

	@Override
	public void removeHandlerListener(final IHandlerListener handlerListener) {}

}

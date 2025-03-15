/*******************************************************************************************************
 *
 * GamlSyncUtil.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.utils;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.xtext.ui.refactoring.ui.SyncUtil;

import com.google.inject.Inject;

/**
 * The class GamlSyncUtil.
 *
 * @author drogoul
 * @since 4 mars 2025
 *
 */
public class GamlSyncUtil extends SyncUtil {

	/** The workspace. */
	@Inject (
			optional = true) private IWorkspace workspace;

	/**
	 * Wait for build.
	 *
	 * @param monitor
	 *            the monitor
	 */
	@Override
	public void waitForBuild(final IProgressMonitor monitor) {
		try {
			// See
			// https://github.com/eclipse-xtext/xtext/commit/2f2fe5d60e523ec4b9c5d9a12e96d172833499ea#commitcomment-153274022
			// waitForJdtIndex(monitor);
			workspace.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		} catch (CoreException e) {
			throw new OperationCanceledException(e.getMessage());
		}
	}

}

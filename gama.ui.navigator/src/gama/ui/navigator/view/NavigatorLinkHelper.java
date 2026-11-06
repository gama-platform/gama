/*******************************************************************************************************
 *
 * NavigatorLinkHelper.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.navigator.resources.workbench.ResourceLinkHelper;

import gama.ui.navigator.view.contents.ResourceManager;
import gama.ui.navigator.view.contents.WrappedResource;

/**
 * The Class NavigatorLinkHelper.
 */
public class NavigatorLinkHelper extends ResourceLinkHelper {

	@Override
	public IStructuredSelection findSelection(final IEditorInput anInput) {
		final IFile file = ResourceUtil.getFile(anInput);
		if (file == null || ResourceManager.INSTANCE == null) return StructuredSelection.EMPTY;
		// The wrapper may not exist yet if its parent folder has never been expanded: resolve (and create) it.
		final WrappedResource<?, ?> wrapped = ResourceManager.INSTANCE.resolveWrappedInstanceOf(file);
		return wrapped == null ? StructuredSelection.EMPTY : new StructuredSelection(wrapped);
	}

	@Override
	public void activateEditor(final IWorkbenchPage aPage, final IStructuredSelection aSelection) {
		// Selection must not replace the target editor before a navigator drag starts.
	}

}

/*******************************************************************************************************
 *
 * RevealInWorkspace.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.navigator.view.GamaNavigator;
import gama.ui.navigator.view.contents.LinkedFile;
import gama.ui.navigator.view.contents.WrappedFile;

/**
 * The Class RevealInWorkspace.
 */
public class RevealInWorkspace extends AbstractHandler implements IHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IStructuredSelection sel = HandlerUtil.getCurrentStructuredSelection(event);
		if (sel.isEmpty()) { return null; }
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (!(part instanceof GamaNavigator)) { return null; }
		final GamaNavigator nav = (GamaNavigator) part;
		final List<Object> selection = sel.toList();
		final List<WrappedFile> newSelection = new ArrayList<>();
		for (final Object o : selection) {
			if (o instanceof LinkedFile) {
				newSelection.add(((LinkedFile) o).getTarget());
			}
		}
		if (newSelection.isEmpty()) { return null; }
		nav.selectReveal(new StructuredSelection(newSelection));
		return this;
	}

}

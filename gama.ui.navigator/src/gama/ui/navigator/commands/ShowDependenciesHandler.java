package gama.ui.navigator.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.api.GAMA;
import gama.ui.navigator.view.dependencies.DependenciesView;
import gama.ui.navigator.view.dependencies.ModelDependencyService;

public final class ShowDependenciesHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final var selection = HandlerUtil.getCurrentSelection(event);
		final Object element = selection instanceof IStructuredSelection selected && selected.size() == 1
				? selected.getFirstElement() : null;
		final IFile file = element == null ? null : Adapters.adapt(element, IFile.class);
		if (!ModelDependencyService.isModel(file)) {
			GAMA.getGui().getDialogFactory().warning("Select a single model or experiment file.");
			return null;
		}
		DependenciesView.show(file);
		return null;
	}
}

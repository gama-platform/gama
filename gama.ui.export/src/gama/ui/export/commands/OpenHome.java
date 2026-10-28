package gama.ui.export.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

import gama.api.GAMA;
import gama.ui.application.workbench.PerspectiveHelper;

/** Leave no experiment running behind the launcher's idle page. */
public final class OpenHome extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		if (GAMA.getExperiment() == null) {
			PerspectiveHelper.openModelingPerspective(true, false);
		} else {
			new Thread(() -> GAMA.closeAllExperiments(true, false), "Return to launcher home").start();
		}
		return null;
	}
}

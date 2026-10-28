package gama.ui.devtools.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.devtools.install.InstallModel.Kind;
import gama.ui.devtools.wizards.InstallSourcesWizard;

/** Opens the source installation wizard for one of the three repository families. */
public class InstallSourcesHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
			Kind kind = Kind.valueOf(event.getParameter("gama.ui.devtools.installKind"));
			new WizardDialog(HandlerUtil.getActiveShell(event), new InstallSourcesWizard(kind)).open();
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new ExecutionException("Unknown GAMA installation kind", e);
		}
		return null;
	}
}

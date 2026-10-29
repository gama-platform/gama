package gama.ui.export.commands;

import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/** Lets navigator Open/Open With use the same validated startup and error reporting as the File menu. */
public final class StandaloneModelLauncher implements IEditorLauncher {

	@Override
	public void open(final IPath file) {
		final var window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final Command command = window.getService(ICommandService.class).getCommand(OpenSimulationPackage.COMMAND);
		final var handlers = window.getService(IHandlerService.class);
		try {
			new OpenSimulationPackage().execute(new ExecutionEvent(command,
					Map.of(OpenSimulationPackage.PATH_PARAMETER, file.toOSString()), null, handlers.getCurrentState()));
		} catch (final ExecutionException e) {
			throw new IllegalStateException("Unable to open exported model " + file, e);
		}
	}
}

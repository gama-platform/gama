package gama.ui.export.launcher;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import gama.export.ExportModels;

public final class PlayerSplashHandlerTest {

	public static void main(final String[] args) {
		for (final String name : List.of("Ant Foraging.gaml", "Model.GAML", "Model.with.dots.gaml")) {
			final IFile file = (IFile) Proxy.newProxyInstance(IFile.class.getClassLoader(), new Class<?>[] { IFile.class },
					(proxy, method, values) -> {
						if (method.getName().equals("getName")) return name;
						throw new UnsupportedOperationException(method.getName());
					});
			final String title = new ExportModels(List.of(file), List.of(), List.of(), Set.of(), Map.of()).defaultTitle();
			if (!title.equals(name.substring(0, name.length() - 5)))
				throw new AssertionError("Remove only the model extension from default titles: " + title);
		}
		if (!StartupModelHelper.getInstance().label("Classic@Example/Ant Foraging.gaml").equals("Experiment Classic"))
			throw new AssertionError("Do not expose model paths in experiment labels");
		final String previous = System.getProperty("exported_runner");
		final Display display = new Display();
		try {
			System.setProperty("exported_runner", "true");
			final Shell shell = new Shell(display);
			final var handler = new PlayerSplashHandler();
			try {
				handler.init(shell);
				final var monitor = handler.getBundleProgressMonitor();
				if (monitor != null || shell.getChildren().length != 0)
					throw new AssertionError("Player splash must have no progress/message controls");
			} finally {
				handler.dispose();
				shell.dispose();
			}
		} finally {
			display.dispose();
			if (previous == null) System.clearProperty("exported_runner");
			else System.setProperty("exported_runner", previous);
		}
		System.out.println("Default model titles, experiment labels and player-only splash progress suppression passed");
	}
}

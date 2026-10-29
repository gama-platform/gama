package gama.ui.export.launcher;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

/** Injected only into a scratch installation by SingleProcessSmokeTest. */
public final class NormalStartupProbe implements IStartup {

	@Override
	public void earlyStartup() {
		final String marker = System.getProperty("gama.smoke.normal.marker");
		if (marker == null || System.getProperty("gama.package.content") != null) return;
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
			try {
				final var page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				if (page == null || page.getPerspective() == null
						|| page.getPerspective().getId().equals("gama.ui.export.perspectives.Home"))
					throw new AssertionError("Normal startup must retain the modeling perspective");
				Files.writeString(Path.of(marker), "Modeler ready; pid=" + ProcessHandle.current().pid());
			} catch (final Exception | AssertionError failure) {
				throw new IllegalStateException("Normal startup smoke failed", failure);
			}
		});
	}
}

package gama.export;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import gama.api.runtime.SystemInfo;

/** Validation, preparation and startup shared by cold document opening and the running modeler. */
public final class SimulationPackageOpening {

	public record Launch(SimulationPackageLauncher.Prepared prepared, Process process) {}

	private SimulationPackageOpening() {}

	/** Starts the player directly; never loads package code in the calling process. */
	public static Launch open(final Shell owner, final Path path) throws IOException, InterruptedException {
		final Path archive = path.toRealPath();
		final var manifest = SimulationPackage.inspect(archive);
		manifest.requireRuntime(SystemInfo.VERSION_NUMBER);
		final String executable = System.getProperty("eclipse.launcher");
		if (executable == null || executable.isBlank())
			throw new IOException("Playable model launching requires an installed GAMA native launcher; it is unavailable in this development launch");
		final var result = new AtomicReference<SimulationPackageLauncher.Prepared>();
		try {
			new ProgressMonitorDialog(owner).run(true, false, monitor -> {
				monitor.beginTask("Preparing playable model", IProgressMonitor.UNKNOWN);
				try {
					result.set(SimulationPackageLauncher.prepare(archive,
							SimulationPackageSessions.directory(),
							Path.of(Platform.getInstallLocation().getURL().toURI()), Path.of(executable), SystemInfo.VERSION_NUMBER));
				} catch (final IOException | java.net.URISyntaxException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			});
		} catch (final InvocationTargetException e) {
			throw new IOException("Unable to prepare playable model: " + e.getCause().getMessage(), e.getCause());
		}
		final var prepared = result.get();
		return new Launch(prepared, prepared.start());
	}

	public static void awaitStartup(final Launch launch) throws IOException, InterruptedException {
		for (int i = 0; i < 120; i++) {
			if (Files.isRegularFile(launch.prepared().content().resolve("player.ready"))) return;
			if (launch.process().waitFor(1, TimeUnit.SECONDS))
				throw new IOException("The simulation player exited before opening its workbench (exit "
						+ launch.process().exitValue() + ").\nSession and startup log: " + launch.prepared().content());
		}
		throw new IOException("The simulation player has not reported startup after two minutes. It has not been stopped."
				+ "\nSession and startup log: " + launch.prepared().content());
	}
}

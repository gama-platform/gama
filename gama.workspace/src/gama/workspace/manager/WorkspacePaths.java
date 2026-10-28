package gama.workspace.manager;

import java.net.URI;
import java.nio.file.Path;

import gama.api.runtime.SystemInfo;

/** Installation-relative paths, independent of any workspace or UI activation. */
public final class WorkspacePaths {

	private WorkspacePaths() {}

	public static Path installationDirectory() {
		final String location = System.getProperty("eclipse.home.location");
		if (location == null) throw new IllegalStateException("The Eclipse installation location is not set");
		return Path.of(URI.create(location));
	}

	public static Path applicationDirectory() {
		final Path installation = installationDirectory();
		return SystemInfo.isMac() ? installation.getParent().getParent() : installation;
	}

	public static Path resolveAgainstInstallation(final Path path) {
		return path.isAbsolute() ? path : installationDirectory().resolve(path);
	}

	public static String resolveAgainstInstallation(final String path) {
		return path == null || path.isBlank() ? path : resolveAgainstInstallation(Path.of(path)).toString();
	}
}

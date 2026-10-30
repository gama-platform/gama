package gama.export;

import java.io.IOException;
import java.net.URL;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.eclipse.core.runtime.URIUtil;

final class InstallationLocation {

	private InstallationLocation() {}

	static Path toPath(final URL location) throws IOException {
		try {
			try {
				return Path.of(location.toURI());
			} catch (final URISyntaxException e) {
				return Path.of(URIUtil.toURI(location));
			}
		} catch (final URISyntaxException e) {
			throw new IOException("The GAMA installation location is not a valid file URL: " + location, e);
		}
	}
}

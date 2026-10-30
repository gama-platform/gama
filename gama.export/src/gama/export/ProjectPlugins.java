package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;

/** Provisioning is deliberate and restart-based; archive inspection never activates these bundles. */
public final class ProjectPlugins {

	private ProjectPlugins() {}

	public static List<SimulationPackageManifest.BundleRequirement> missing(final ProjectArchive.Info info) {
		return info.plugins().stream().filter(p -> {
			final var installed = Platform.getBundle(p.symbolicName());
			return installed == null || installed.getVersion().compareTo(new Version(p.version())) < 0;
		}).toList();
	}

	public static void validate(final Path file, final SimulationPackageManifest.BundleRequirement requirement) throws IOException {
		try (var jar = new JarFile(file.toFile())) {
			final var manifest = jar.getManifest();
			if (manifest == null) throw new IOException("Missing plugin manifest: " + file);
			final var attributes = manifest.getMainAttributes();
			final String id = attributes.getValue("Bundle-SymbolicName");
			if (id == null || !id.split(";", 2)[0].trim().equals(requirement.symbolicName())
					|| !requirement.version().equals(attributes.getValue("Bundle-Version")))
				throw new IOException("Plugin identity/version does not match archive metadata: " + file);
		}
	}

	public static Path dropins() throws IOException {
		final String configured = System.getProperty("org.eclipse.equinox.p2.reconciler.dropins.directory");
		if (configured != null && !configured.isBlank()) return Path.of(configured).toAbsolutePath().normalize();
		final var install = Platform.getInstallLocation();
		if (install == null || !"file".equals(install.getURL().getProtocol()))
			throw new IOException("This GAMA installation has no local Dropins folder");
		return Path.of(java.net.URI.create(install.getURL().toExternalForm())).resolve("dropins");
	}

	public static final class Prepared implements AutoCloseable {
		private final Path staging, destination;
		private boolean published;

		private Prepared(final Path staging, final Path destination) {
			this.staging = staging;
			this.destination = destination;
		}

		public boolean publish() throws IOException {
			if (staging == null) return false;
			Files.move(staging, destination, StandardCopyOption.ATOMIC_MOVE);
			published = true;
			return true;
		}

		@Override public void close() throws IOException {
			if (staging != null && !published) ProjectArchive.delete(staging);
		}
	}

	/** Checks permissions and bundle conflicts, and stages plugins before any project is replaced. */
	public static Prepared prepare(final ProjectArchive.Extracted archive,
			final List<SimulationPackageManifest.BundleRequirement> requested) throws IOException {
		if (requested.isEmpty()) return new Prepared(null, null);
		for (final var plugin : requested) {
			if (Platform.getBundle(plugin.symbolicName()) != null)
				throw new IOException("An incompatible version of " + plugin.symbolicName()
						+ " is already installed. Update it through Install new Plugins before importing.");
			if (plugin.artifact().isEmpty()) throw new IOException("Required plugin is not included: " + plugin.symbolicName());
			validate(archive.directory().resolve(plugin.artifact()), plugin);
		}
		final Path dropins = dropins();
		Files.createDirectories(dropins);
		if (!Files.isWritable(dropins)) throw new IOException("GAMA's Dropins folder is not writable: " + dropins);
		try (var paths = Files.walk(dropins)) {
			for (final Path path : paths.filter(p -> p.toString().endsWith(".jar")).toList()) {
				try (var jar = new JarFile(path.toFile())) {
					final var manifest = jar.getManifest();
					if (manifest == null) continue;
					final String id = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
					if (id != null && requested.stream().anyMatch(p -> id.split(";", 2)[0].trim().equals(p.symbolicName())))
						throw new IOException("A contribution for " + id + " is already present in Dropins. Restart GAMA "
								+ "and review its installed plugins before importing again.");
				}
			}
		}
		final Path staging = Files.createTempDirectory(dropins.getParent(), ".gama-plugin-import-");
		try {
			final Path plugins = Files.createDirectories(staging.resolve("eclipse/plugins"));
			for (final var plugin : requested)
				Files.copy(archive.directory().resolve(plugin.artifact()),
						plugins.resolve(plugin.symbolicName() + "_" + plugin.version() + ".jar"));
			return new Prepared(staging, dropins.resolve("gama-project-" + java.util.UUID.randomUUID()));
		} catch (final IOException | RuntimeException e) {
			try { ProjectArchive.delete(staging); } catch (final IOException cleanup) { e.addSuppressed(cleanup); }
			throw e;
		}
	}
}

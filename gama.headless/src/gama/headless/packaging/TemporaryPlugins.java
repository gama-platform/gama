/*******************************************************************************************************
 *
 * TemporaryPlugins.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.packaging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.FrameworkWiring;

import gama.export.SimulationPackageLauncher;
import gama.export.SimulationPackageManifest.BundleRequirement;

/**
 * Plugins installed in the running framework for one headless command only. They must be installed before GAMA core
 * is initialized, so that their GAML additions are loaded with the standard ones. Each JAR is first copied into a
 * private directory, so the bundles never refer to user or archive files, and leftovers of an interrupted run can be
 * recognized and removed by {@link #removeLeftovers()}.
 */
public final class TemporaryPlugins implements AutoCloseable {

	/** The prefix of the private directories holding the installed copies. */
	static final String DIRECTORY_PREFIX = "gama-headless-plugins-";

	/** The bundle context. */
	private final BundleContext context;

	/** The private directory, created on the first installation. */
	private Path directory;

	/** The installed bundles, by symbolic name. */
	private final Map<String, Bundle> bundles = new LinkedHashMap<>();

	/** The installed copies, by symbolic name. */
	private final Map<String, Path> files = new LinkedHashMap<>();

	/** Whether the installed bundles have been resolved. */
	private boolean resolved;

	/** Creates an empty set of temporary plugins. */
	public TemporaryPlugins() {
		context = FrameworkUtil.getBundle(TemporaryPlugins.class).getBundleContext();
	}

	/**
	 * Uninstalls the bundles a previous headless command could not uninstall (e.g. because it was killed), as the
	 * framework may have persisted them.
	 */
	public static void removeLeftovers() {
		final var bundle = FrameworkUtil.getBundle(TemporaryPlugins.class);
		if (bundle == null || bundle.getBundleContext() == null) return;
		for (final Bundle candidate : bundle.getBundleContext().getBundles()) {
			if (!candidate.getLocation().contains("/" + DIRECTORY_PREFIX)) { continue; }
			try {
				candidate.uninstall();
			} catch (final BundleException | IllegalStateException e) {
				System.err.println("Unable to remove the leftover plugin " + candidate.getLocation() + ": " + e);
			}
		}
	}

	/**
	 * Installs a plugin JAR. Platform bundles, fragments, native code, nested JARs and plugins already installed are
	 * rejected.
	 *
	 * @param jar
	 *            the plugin JAR
	 * @param expected
	 *            the identity the plugin must have, or null to accept the identity of its manifest
	 * @return the installed copy, which is the file the framework reports for this bundle
	 */
	public Path install(final Path jar, final BundleRequirement expected) throws IOException {
		if (resolved) throw new IllegalStateException("Plugins must all be installed before being resolved");
		if (!Files.isRegularFile(jar)) throw new IOException("Plugin JAR not found: " + jar);
		final BundleRequirement requirement = expected != null ? expected : identity(jar);
		final String name = requirement.symbolicName();
		final Bundle existing = Platform.getBundle(name);
		if (existing != null || bundles.containsKey(name))
			throw new IOException("Plugin " + name + " is already installed"
					+ (existing == null ? "" : " (version " + existing.getVersion() + ")")
					+ "; an installed plugin cannot be replaced");
		if (directory == null) { directory = Files.createTempDirectory(DIRECTORY_PREFIX); }
		final Path copy = directory.resolve(name + "_" + requirement.version() + ".jar");
		Files.copy(jar, copy);
		// Same checks as the playable model player: identity, no platform bundle, no native code or nested JAR
		SimulationPackageLauncher.validatePlugin(copy, new BundleRequirement(name, requirement.version(),
				requirement.artifact().isEmpty() ? "plugins/" + copy.getFileName() : requirement.artifact()));
		try {
			bundles.put(name, context.installBundle("reference:" + copy.toUri().toASCIIString()));
		} catch (final BundleException e) {
			throw new IOException("Unable to install plugin " + name + ": " + e.getMessage(), e);
		}
		files.put(name, copy);
		return copy;
	}

	/** Reads the identity of a plugin JAR from its manifest. */
	static BundleRequirement identity(final Path jar) throws IOException {
		try (var file = new JarFile(jar.toFile())) {
			final var manifest = file.getManifest();
			if (manifest == null) throw new IOException("Not a plugin (no manifest): " + jar);
			final String id = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
			final String version = manifest.getMainAttributes().getValue("Bundle-Version");
			if (id == null || version == null) throw new IOException("Not a plugin (no OSGi identity): " + jar);
			try {
				return new BundleRequirement(id.split(";", 2)[0].trim(), version.trim(),
						"plugins/" + id.split(";", 2)[0].trim() + ".jar");
			} catch (final IllegalArgumentException e) {
				throw new IOException("Invalid plugin identity in " + jar + ": " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Resolves the installed plugins, which publishes their extensions (GAML additions included) to the registry. Must
	 * be called before GAMA core is initialized.
	 */
	public void resolve() throws IOException {
		resolved = true;
		if (bundles.isEmpty()) return;
		final Bundle core = Platform.getBundle("gama.core");
		if (core != null && core.getState() == Bundle.ACTIVE)
			throw new IOException("Plugins must be installed before GAMA is initialized");
		final var framework = context.getBundle(0).adapt(FrameworkWiring.class);
		if (!framework.resolveBundles(bundles.values())) {
			final List<String> unresolved = bundles.values().stream()
					.filter(bundle -> bundle.getState() == Bundle.INSTALLED).map(Bundle::getSymbolicName).toList();
			throw new IOException("Cannot resolve the dependencies of plugin(s) " + unresolved
					+ ": they require bundles that this GAMA does not provide");
		}
	}

	/** The installed copies, by symbolic name. */
	public Map<String, Path> files() {
		return java.util.Collections.unmodifiableMap(files);
	}

	/** Whether no plugin is installed. */
	public boolean isEmpty() { return bundles.isEmpty(); }

	/** Uninstalls the plugins, then deletes their copies. Idempotent: also called if the JVM is stopped. */
	@Override
	public synchronized void close() {
		final List<Bundle> installed = new ArrayList<>(bundles.values());
		java.util.Collections.reverse(installed);
		for (final Bundle bundle : installed) {
			try {
				bundle.uninstall();
			} catch (final BundleException | IllegalStateException e) {
				System.err.println("Unable to uninstall plugin " + bundle.getSymbolicName() + ": " + e);
			}
		}
		bundles.clear();
		// The JARs can still be open (e.g. on Windows): a failure is only reported; removeLeftovers() handles bundles
		if (directory != null) { PackageCommands.delete(directory); }
		directory = null;
	}
}

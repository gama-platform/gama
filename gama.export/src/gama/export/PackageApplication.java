package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.FrameworkWiring;

/** Resolves package language contributions before instantiating the UI application and activating gama.core. */
public final class PackageApplication implements IApplication {

	private volatile IApplication player;

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		final String content = System.getProperty(SimulationPackageLauncher.CONTENT_PROPERTY);
		if (content == null) throw new IOException("Playable model players require a prepared launch configuration");
		SimulationPackageSessions.Lease lease = null;
		for (int i = 0; i < 50 && lease == null; i++) {
			try { lease = SimulationPackageSessions.acquire(Path.of(content)); }
			catch (final IOException e) {
				if (i == 49) throw e;
				Thread.sleep(100);
			}
		}
		try (var held = lease) { return startPlayer(context); }
	}

	private Object startPlayer(final IApplicationContext context) throws Exception {
		final String location = System.getProperty(SimulationPackageLauncher.CONTENT_PROPERTY);
		if (location == null || !Boolean.getBoolean("exported_runner")
				|| !"false".equals(System.getProperty("use_global_preference_store")))
			throw new IOException("Playable model players require a private, prepared launch configuration");
		final var extracted = SimulationPackage.Extracted.open(Path.of(location));
		final boolean ignoreVersionRequirements =
				Boolean.getBoolean(SimulationPackageLauncher.IGNORE_VERSION_REQUIREMENTS_PROPERTY);
		if (!ignoreVersionRequirements)
			extracted.manifest().requireRuntime(System.getProperty("gama.package.runtime", ""));
		SimulationLaunchContext.fromPackage(extracted);
		final var instance = Platform.getInstanceLocation();
		final var configuration = Platform.getConfigurationLocation();
		if (instance == null || !instance.isSet()
				|| !Path.of(instance.getURL().toURI()).toRealPath().equals(extracted.workspace().toRealPath())
				|| !Path.of(configuration.getURL().toURI()).toRealPath()
						.equals(extracted.content().resolve("runtime-configuration").toRealPath()))
			throw new IOException("Playable model player workspace or configuration is not isolated");
		final var requiredBundles = new ArrayList<Bundle>();
		final var bundleContext = FrameworkUtil.getBundle(PackageApplication.class).getBundleContext();
		for (final var required : extracted.manifest().bundles()) {
			final var bundle = java.util.Arrays.stream(bundleContext.getBundles())
					.filter(candidate -> required.symbolicName().equals(candidate.getSymbolicName())
							&& (ignoreVersionRequirements && required.artifact().isEmpty()
									|| required.accepts(candidate.getVersion().toString())))
					.max(java.util.Comparator.comparing(Bundle::getVersion)).orElse(null);
			if (bundle == null)
				throw new IOException("Playable model bundle is missing or incompatible: " + required.symbolicName());
			requiredBundles.add(bundle);
		}
		final var core = Platform.getBundle("gama.core");
		if (core == null || core.getState() == Bundle.ACTIVE)
			throw new IOException("Playable model bootstrap must run before GAMA core initialization");
		final var framework = bundleContext.getBundle(0).adapt(FrameworkWiring.class);
		if (!framework.resolveBundles(requiredBundles))
			throw new IOException("Cannot resolve playable model dependencies: " + requiredBundles.stream()
					.filter(bundle -> bundle.getState() == Bundle.INSTALLED).map(Bundle::getSymbolicName).toList());
		final var extension = Platform.getExtensionRegistry()
				.getExtension("org.eclipse.core.runtime.applications", "gama.ui.export.SimulationApplication");
		if (extension == null) throw new IOException("The simulation player application is not installed");
		final String bootstrapPid = System.getProperty("gama.package.bootstrap.pid");
		if (bootstrapPid != null && !bootstrapPid.equals(Long.toString(ProcessHandle.current().pid())))
			throw new IOException("The pre-framework playable model bootstrap changed processes");
		Files.writeString(extracted.content().resolve("bootstrap.ready"), "Package dependencies resolved before GAMA initialization\n"
				+ "pid=" + ProcessHandle.current().pid() + "\n"
				+ "in-process=" + (bootstrapPid != null) + "\n");
		for (final var application : extension.getConfigurationElements()) {
			for (final var run : application.getChildren("run")) {
				player = (IApplication) run.createExecutableExtension("class");
				return player.start(context);
			}
		}
		throw new IOException("The simulation player application has no entry point");
	}

	@Override
	public void stop() {
		final var application = player;
		if (application != null) application.stop();
	}
}

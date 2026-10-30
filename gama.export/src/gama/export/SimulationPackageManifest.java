package gama.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/** Runtime-independent metadata: reading a package must not initialize GAMA or its plugins. */
public record SimulationPackageManifest(String id, String version, String runtimeVersion,
		List<Experiment> experiments, List<BundleRequirement> bundles) {

	public static final String FILE = "manifest.properties";
	public static final int FORMAT_VERSION = 1;
	private static final int MAX_ITEMS = 10_000;

	public record Experiment(String model, String name) {
		public Experiment {
			if (model == null || !model.contains("/") || model.contains("@") || model.contains("#")
					|| !model.endsWith(".gaml") && !model.endsWith(".experiment"))
				throw new IllegalArgumentException("Invalid experiment model: " + model);
			SimulationPackage.validatePath("workspace/" + model);
			if (name == null || name.isBlank() || name.contains("#") || name.contains("@"))
				throw new IllegalArgumentException("Invalid experiment name: " + name);
		}

		public String selection() { return name + "@" + model; }
	}

	/** Empty artifact means that this bundle version or newer must be supplied by the host. */
	public record BundleRequirement(String symbolicName, String version, String artifact) {
		public BundleRequirement {
			if (symbolicName == null || !symbolicName.matches("[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*"))
				throw new IllegalArgumentException("Invalid bundle symbolic name: " + symbolicName);
			if (version == null || !version.matches("\\d+\\.\\d+\\.\\d+(?:\\.[A-Za-z0-9_-]+)?"))
				throw new IllegalArgumentException("An exact OSGi bundle version is required: " + version);
			if (artifact == null) throw new IllegalArgumentException("Missing bundle artifact");
			if (!artifact.isEmpty()) {
				SimulationPackage.validatePath(artifact);
				if (!artifact.startsWith("plugins/") || !artifact.endsWith(".jar"))
					throw new IllegalArgumentException("Invalid bundle artifact: " + artifact);
			}

		}

		public boolean accepts(final String actualVersion) {
			return artifact.isEmpty()
					? new org.osgi.framework.Version(actualVersion).compareTo(new org.osgi.framework.Version(version)) >= 0
					: version.equals(actualVersion);
		}
	}

	public SimulationPackageManifest {
		if (id == null || !id.matches("[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*"))
			throw new IllegalArgumentException("Invalid playable model identifier: " + id);
		if (version == null || version.isBlank() || runtimeVersion == null || runtimeVersion.isBlank())
			throw new IllegalArgumentException("Playable model and runtime versions are required");
		experiments = List.copyOf(experiments);
		bundles = List.copyOf(bundles);
		if (experiments.isEmpty() || experiments.size() > MAX_ITEMS || bundles.size() > MAX_ITEMS)
			throw new IllegalArgumentException("Invalid number of experiments or bundles");
		final var selections = new HashSet<String>();
		for (final var experiment : experiments) {
			if (!selections.add(experiment.selection()))
				throw new IllegalArgumentException("Duplicate experiment: " + experiment.selection());
		}
		final var names = new HashSet<String>();
		final var artifacts = new HashSet<String>();
		for (final var bundle : bundles) {
			if (!names.add(bundle.symbolicName()) || !bundle.artifact().isEmpty() && !artifacts.add(bundle.artifact()))
				throw new IllegalArgumentException("Duplicate bundle: " + bundle.symbolicName());
		}
	}

	public void requireRuntime(final String actualVersion) throws IOException {
		if (!atLeast(actualVersion, runtimeVersion))
			throw new IOException("This playable model requires GAMA " + runtimeVersion + " or newer; the host is " + actualVersion);
	}

	private static boolean atLeast(final String actual, final String required) {
		if (required.equals(actual)) return true;
		if (actual == null) return false;
		final String actualRelease = actual.replaceFirst("-SNAPSHOT$", "");
		final String requiredRelease = required.replaceFirst("-SNAPSHOT$", "");
		if (!actualRelease.matches("\\d+(?:[.-]\\d+)*") || !requiredRelease.matches("\\d+(?:[.-]\\d+)*"))
			return false;
		final String[] left = actualRelease.split("[.-]");
		final String[] right = requiredRelease.split("[.-]");
		for (int i = 0; i < Math.max(left.length, right.length); i++) {
			final var a = i < left.length ? new java.math.BigInteger(left[i]) : java.math.BigInteger.ZERO;
			final var b = i < right.length ? new java.math.BigInteger(right[i]) : java.math.BigInteger.ZERO;
			final int comparison = a.compareTo(b);
			if (comparison != 0) return comparison > 0;
		}
		return true;
	}

	Properties properties() {
		final var properties = new Properties();
		properties.setProperty("format.version", Integer.toString(FORMAT_VERSION));
		properties.setProperty("package.id", id);
		properties.setProperty("package.version", version);
		properties.setProperty("runtime.version", runtimeVersion);
		properties.setProperty("experiments.count", Integer.toString(experiments.size()));
		for (int i = 0; i < experiments.size(); i++) {
			properties.setProperty("experiment." + i + ".model", experiments.get(i).model());
			properties.setProperty("experiment." + i + ".name", experiments.get(i).name());
		}
		properties.setProperty("bundles.count", Integer.toString(bundles.size()));
		for (int i = 0; i < bundles.size(); i++) {
			final var bundle = bundles.get(i);
			properties.setProperty("bundle." + i + ".id", bundle.symbolicName());
			properties.setProperty("bundle." + i + ".version", bundle.version());
			properties.setProperty("bundle." + i + ".artifact", bundle.artifact());
		}
		return properties;
	}

	static SimulationPackageManifest read(final Properties properties) throws IOException {
		try {
			if (!Integer.toString(FORMAT_VERSION).equals(required(properties, "format.version")))
				throw new IOException("Unsupported GAMA playable model format: " + properties.getProperty("format.version"));
			final var experiments = new ArrayList<Experiment>();
			for (int i = 0, count = count(properties, "experiments.count"); i < count; i++) {
				experiments.add(new Experiment(required(properties, "experiment." + i + ".model"),
						required(properties, "experiment." + i + ".name")));
			}
			final var bundles = new ArrayList<BundleRequirement>();
			for (int i = 0, count = count(properties, "bundles.count"); i < count; i++) {
				bundles.add(new BundleRequirement(required(properties, "bundle." + i + ".id"),
						required(properties, "bundle." + i + ".version"),
						required(properties, "bundle." + i + ".artifact")));
			}
			return new SimulationPackageManifest(required(properties, "package.id"),
					required(properties, "package.version"), required(properties, "runtime.version"), experiments, bundles);
		} catch (final IllegalArgumentException e) {
			throw new IOException("Invalid GAMA playable model manifest: " + e.getMessage(), e);
		}
	}

	static String required(final Properties properties, final String key) throws IOException {
		final String value = properties.getProperty(key);
		if (value == null) throw new IOException("Missing GAMA playable model field: " + key);
		return value;
	}

	private static int count(final Properties properties, final String key) throws IOException {
		final int count = Integer.parseInt(required(properties, key));
		if (count < 0 || count > MAX_ITEMS) throw new IOException("Invalid GAMA playable model count: " + key);
		return count;
	}
}

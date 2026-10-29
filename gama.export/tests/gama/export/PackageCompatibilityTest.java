package gama.export;

import java.io.IOException;
import java.util.List;

public final class PackageCompatibilityTest {

	private static void runtime(final String required, final String actual, final boolean accepted) throws Exception {
		final var manifest = new SimulationPackageManifest("example.model", "1", required,
				List.of(new SimulationPackageManifest.Experiment("Example/model.gaml", "Run")), List.of());
		try {
			manifest.requireRuntime(actual);
			if (!accepted) throw new AssertionError("Accepted older runtime " + actual + " for " + required);
		} catch (final IOException e) {
			if (accepted) throw e;
		}
	}

	public static void main(final String[] args) throws Exception {
		runtime("2026.1.0", "2026.1.0", true);
		runtime("2026.1.0", "2026.10.0", true);
		runtime("2026.1.0", "2027.0.0", true);
		runtime("2026.10.0", "2026.2.0", false);
		runtime("2025-03", "2026-01", true);
		runtime("0.0.0-SNAPSHOT", "0.0.0", true);
		runtime("test", "test", true);
		runtime("test", "unknown", false);
		final var host = new SimulationPackageManifest.BundleRequirement("gama.api", "0.0.0.202610290813", "");
		if (!host.accepts("0.0.0.202610290827") || host.accepts("0.0.0.202610280827"))
			throw new AssertionError("Host build qualifiers are minimum requirements");
		final var embedded = new SimulationPackageManifest.BundleRequirement("example.extra", "1.0.0", "plugins/extra.jar");
		if (!embedded.accepts("1.0.0") || embedded.accepts("1.1.0"))
			throw new AssertionError("Embedded payload identity remains exact");
		System.out.println("Minimum runtime/host versions and exact embedded payload checks passed");
	}
}

package gama.export;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/** Checks immutable product membership independently of installed optional plugins. */
public final class StandardDistributionTest {

	public static void main(final String[] args) throws Exception {
		final Map<String, String> resources = Map.of(
				"gama.product/gama.product", "<product><features><feature id='gama.feature.root'/></features></product>",
				"gama.feature.root/feature.xml", """
						<feature id='gama.feature.root'>
						<plugin id='gama.core'/><includes id='gama.feature.child'/>
						<requires><import plugin='standard.library'/><import feature='gama.feature.required'/></requires>
						</feature>
						""",
				"gama.feature.child/feature.xml", """
						<feature id='gama.feature.child'><plugin id='standard.child'/>
						<includes id='gama.feature.root'/></feature>
						""",
				"gama.feature.required/feature.xml",
				"<feature id='gama.feature.required'><plugin id='standard.required'/></feature>",
				"gama.feature.optional/feature.xml",
				"<feature id='gama.feature.optional'><plugin id='extra.optional'/></feature>");
		final var standard = StandardDistribution.bundles(path -> {
			final String xml = resources.get(path);
			return xml == null ? null : new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
		});
		if (!standard.equals(Set.of("gama.core", "standard.child", "standard.library", "standard.required")))
			throw new AssertionError("Follow standard product feature graph, not unrelated features: " + standard);
		try {
			StandardDistribution.bundles(path -> null);
			throw new AssertionError("Missing build metadata must not classify every plugin as an extra");
		} catch (final IOException expected) {}
		final var built = StandardDistribution.bundles();
		final Path repository = Path.of(args.length == 0 ? "." : args[0]);
		final var source = StandardDistribution.bundles(path -> Files.newInputStream(repository.resolve(path)));
		if (!built.equals(source) || !built.containsAll(Set.of("gama.core", "gama.api", "gama.extension.bdi", "org.eclipse.osgi"))
				|| built.contains("example.packageprobe"))
			throw new AssertionError("Bundle the current standard product metadata, not the extended host inventory");
		if (args.length > 1) {
			for (final String line : Files.readAllLines(Path.of(args[1]).resolve(
					"configuration/org.eclipse.equinox.simpleconfigurator/bundles.info"))) {
				if (!line.isBlank() && !line.startsWith("#") && !built.contains(line.split(",")[0]))
					throw new AssertionError("Stock product contains an undeclared transitive standard dependency: " + line);
			}
		}
		System.out.println("Standard distribution membership, feature traversal and generated metadata checks passed");
	}
}

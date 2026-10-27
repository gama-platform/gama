import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import gama.ui.devtools.launch.ProductLaunchSpec;

/** Standalone test; pass the repository root as the first argument. */
public final class ProductLaunchSpecTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) { throw new AssertionError(message); }
	}

	public static void main(final String[] args) throws Exception {
		Path root = Path.of(args.length == 0 ? "." : args[0]);
		for (String file : List.of("gama.product", "gama.headless.product")) {
			for (String os : List.of("macosx", "linux", "win32")) {
				try (var input = Files.newInputStream(root.resolve("gama.product").resolve(file))) {
					ProductLaunchSpec spec = ProductLaunchSpec.read(input, os);
					boolean gui = file.equals("gama.product");
					check(spec.application().equals(gui ? "gama.ui.application.GamaApplication" : "gama.headless.product"),
							"Application ID");
					check(spec.product().equals(gui ? "gama.ui.application.product" : "gama.headless.product"),
							"Product ID");
					check(spec.vmArguments().contains("--enable-preview"), "Product VM arguments");
					check(spec.vmArguments().contains("osgi.requiredJavaVersion=25"), "Java requirement");
					check(spec.vmArguments().contains("-XstartOnFirstThread") == os.equals("macosx"),
							"First-thread argument must be macOS-only");
					check(spec.features().contains("gama.feature.core"), "Core feature");
					check(spec.features().contains("gama.feature.ui") == gui, "Separate headless feature selection");
					check(spec.startups().stream().anyMatch(s -> s.id().equals("org.apache.felix.scr")
							&& s.level().equals("1") && s.autoStart()), "Startup configuration");
					check(spec.properties().get("eclipse.buildId").equals("development"), "Development build ID");
					check(spec.properties().get("gama.version").equals("SNAPSHOT"), "Product properties");
				}
			}
		}
		String sample = """
				<product id="example" application="example.app" type="features">
				<launcherArgs><vmArgs>-Xmx4G</vmArgs><vmArgsMac>-XstartOnFirstThread</vmArgsMac>
				<programArgs>common</programArgs><programArgsLin>linux</programArgsLin></launcherArgs>
				<features><feature id="example.feature"/></features>
				</product>
				""";
		var spec = ProductLaunchSpec.read(new ByteArrayInputStream(sample.getBytes(StandardCharsets.UTF_8)), "linux");
		check(spec.programArguments().equals("common\nlinux"), "Combine common and OS program arguments");
		check(spec.vmArguments().strip().equals("-Xmx4G"), "No accidental macOS argument on Linux");
		check(spec.startups().isEmpty() && spec.properties().isEmpty(), "Optional configuration section");
		try {
			ProductLaunchSpec.read(new ByteArrayInputStream("<product/>".getBytes(StandardCharsets.UTF_8)), "linux");
			throw new AssertionError("Invalid product accepted");
		} catch (IllegalArgumentException expected) {
			check(expected.getMessage().contains("feature-based"), "Explicit invalid-product error");
		}
		System.out.println("Product launch settings verified for GUI/headless on macOS, Windows and Linux.");
	}
}

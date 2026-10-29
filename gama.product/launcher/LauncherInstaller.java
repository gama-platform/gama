import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import javax.tools.ToolProvider;

/** Build-time installation only; never edits a user's installed/signed application. */
public final class LauncherInstaller {

	public static void main(final String[] args) throws Exception {
		final Path source = Path.of(args[0]).toRealPath();
		final Path products = Path.of(args[1]).toRealPath();
		final var version = Pattern.compile("VERSION_NUMBER\\s*=\\s*\"([^\"]+)\"")
				.matcher(Files.readString(source.getParent().resolve("gama.api/src/gama/api/runtime/SystemInfo.java")));
		if (!version.find()) throw new IOException("Unable to determine GAMA's runtime version");
		int installed = 0;
		try (var paths = Files.walk(products)) {
			for (final Path configuration : paths.filter(path -> path.endsWith("configuration/config.ini")).toList()) {
				final Path installation = configuration.getParent().getParent();
				if (!Files.isDirectory(installation.resolve("plugins"))) continue;
				install(source.resolve("launcher"), installation, version.group(1));
				installed++;
			}
		}
		if (installed == 0) throw new IOException("No materialized GUI products found in " + products);
	}

	private static Path bundle(final Path install, final String id) throws IOException {
		try (var files = Files.list(install.resolve("plugins"))) {
			final var candidates = files.filter(path -> path.getFileName().toString().startsWith(id + "_")).toList();
			if (candidates.size() != 1) throw new IOException("Expected one " + id + " bundle, found " + candidates);
			return candidates.getFirst();
		}
	}

	private static void install(final Path source, final Path install, final String runtime) throws Exception {
		final Path launcher = bundle(install, "org.eclipse.equinox.launcher");
		final Path exporter = bundle(install, "gama.export");
		final Path osgi = bundle(install, "org.eclipse.osgi");
		if (!Files.isRegularFile(exporter)) throw new IOException("Package exporter must be a JAR: " + exporter);
		try (var jar = new JarFile(exporter.toFile())) {
			if (jar.getEntry("gama/export/PackageBootstrap.class") == null)
				throw new IOException("Rebuild gama.export before assembling the product");
		}
		final Path classes = Files.createTempDirectory("gama-launcher-classes-");
		try {
			final int code = ToolProvider.getSystemJavaCompiler().run(null, null, null, "-proc:none",
					"-cp", launcher.toString(), "-d", classes.toString(),
					source.resolve("src/gama/launcher/PackageLauncher.java").toString(),
					source.resolve("src/gama/launcher/SplashImage.java").toString());
			if (code != 0) throw new IOException("Unable to compile the package launcher");
			final var manifest = new Manifest();
			final var attributes = manifest.getMainAttributes();
			attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
			attributes.put(Attributes.Name.MAIN_CLASS, "gama.launcher.PackageLauncher");
			attributes.put(Attributes.Name.CLASS_PATH, launcher.getFileName().toString());
			try (var jar = new JarOutputStream(Files.newOutputStream(install.resolve("plugins/gama-launcher.jar")), manifest);
					var paths = Files.walk(classes)) {
				for (final Path file : paths.filter(Files::isRegularFile).toList()) {
					jar.putNextEntry(new JarEntry(classes.relativize(file).toString().replace('\\', '/')));
					Files.copy(file, jar);
					jar.closeEntry();
				}
			}
		} finally {
			try (var paths = Files.walk(classes)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
			}
		}
		final var properties = new Properties();
		properties.setProperty("export", install.relativize(exporter).toString().replace('\\', '/'));
		properties.setProperty("osgi", install.relativize(osgi).toString().replace('\\', '/'));
		properties.setProperty("runtime", runtime);
		try (var writer = Files.newBufferedWriter(install.resolve("configuration/gama-bootstrap.properties"))) {
			properties.store(writer, "Pre-Equinox simulation package bootstrap");
		}
		final Path ini = install.resolve("Gama.ini");
		String text = Files.readString(ini, StandardCharsets.UTF_8);
		final var startup = Pattern.compile("(?m)(^-startup\\r?\\n)([^\\r\\n]+)").matcher(text);
		if (!startup.find()) throw new IOException("Native launcher startup JAR is missing in " + ini);
		final String previous = startup.group(2).replace('\\', '/');
		final int slash = previous.lastIndexOf('/');
		text = startup.replaceFirst(java.util.regex.Matcher.quoteReplacement(startup.group(1)
				+ (slash < 0 ? "" : previous.substring(0, slash + 1)) + "gama-launcher.jar"));
		Files.writeString(ini, text, StandardCharsets.UTF_8);
		if (install.toString().contains("Gama.app")) {
			final Path nativeSource = Files.createDirectories(install.resolve("launcher"));
			Files.copy(source.resolve("cocoa/PackageLauncher.m"), nativeSource.resolve("PackageLauncher.m"),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(source.resolve("build-macos.sh"), nativeSource.resolve("build-macos.sh"),
					StandardCopyOption.REPLACE_EXISTING);
			if (System.getProperty("os.name").startsWith("Mac")) {
				final int code = new ProcessBuilder("bash", nativeSource.resolve("build-macos.sh").toString())
						.inheritIO().start().waitFor();
				if (code != 0) throw new IOException("Unable to build the native package document bridge");
			}
		}
		System.out.println("Installed the pre-Equinox package launcher in " + install);
	}
}

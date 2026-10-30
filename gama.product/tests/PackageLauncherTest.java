package gama.launcher;

import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;

/** JDK-only tests; compile with the launcher's two sources and the Equinox launcher JAR. */
public final class PackageLauncherTest {

	public static void main(final String[] args) throws Exception {
		final var nativeArgs = List.of("-os", "macosx", "-ws", "cocoa", "-arch", "aarch64",
				"-showsplash", "-launcher", "/Gama.app/Contents/MacOS/Gama", "-name", "Gama",
				"--launcher.library", "/plugins/eclipse.so", "-startup", "/plugins/gama-launcher.jar",
				"--launcher.overrideVmargs", "-data", "@noDefault", "-vm", "/jdk/libjvm.dylib");
		check(PackageLauncher.documents(nativeArgs).isEmpty(), "Native launcher options are not document paths");
		final var command = new java.util.ArrayList<>(nativeArgs);
		command.addAll(List.of("--launcher.openFile", "/Models/Ants.gamx", "-vmargs", "-Dtitle=ignored.gamx"));
		check(PackageLauncher.documents(command).equals(List.of("/Models/Ants.gamx")), "Ignore VM flags after document paths");
		check(PackageLauncher.documents(List.of("-data", "/workspace", "/A model.gamx", "/model.gaml"))
				.equals(List.of("/A model.gamx", "/model.gaml")), "Preserve mixed and multiple documents for fallback routing");
		final var root = Files.createTempDirectory("package-splash-test-");
		try {
			final var image = new java.awt.image.BufferedImage(120, 80, java.awt.image.BufferedImage.TYPE_INT_RGB);
			final var paint = image.createGraphics();
			paint.setColor(java.awt.Color.RED);
			paint.fillRect(0, 0, 120, 80);
			paint.dispose();
			final var illustration = root.resolve("image.png");
			ImageIO.write(image, "png", illustration.toFile());
			for (final String path : List.of("", illustration.toString())) {
				final var output = root.resolve("splash.png");
				SplashImage.write(path, "A very long title ".repeat(20),
						"A description with spaces, accents \u00e9 and a second line.\n".repeat(10), output);
				final var splash = ImageIO.read(output.toFile());
				check(splash.getWidth() == 680 && splash.getHeight() == 520, "Stable presentation size");
				check(path.isEmpty() || splash.getRGB(340, 290) == java.awt.Color.RED.getRGB(),
						"Keep the author's illustration centered without changing its aspect ratio");
			}
			final var invalid = root.resolve("invalid");
			Files.writeString(invalid, "not an image");
			try {
				SplashImage.write(invalid.toString(), "Title", "", root.resolve("invalid.png"));
				throw new AssertionError("Corrupt images must not silently disappear");
			} catch (java.io.IOException expected) {}
		} finally {
			try (var paths = Files.walk(root)) {
				for (final var file : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.delete(file);
			}
		}
		System.out.println("Pre-framework document parsing and splash rendering checks passed");
	}

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}
}

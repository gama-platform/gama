package gama.launcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Runs before Equinox, inside the native launcher's original JVM and process. */
public final class PackageLauncher extends org.eclipse.equinox.launcher.Main {

	private static native String[] startupDocuments(boolean debug);
	private static native void composeSplash(String image, String title, String description, String output);
	private static native void showError(String message);

	public static void main(final String[] args) { System.exit(new PackageLauncher().run(args)); }

	@Override
	public int run(final String[] supplied) {
		try {
			final var arguments = new ArrayList<>(Arrays.asList(supplied));
			if (Boolean.getBoolean("gama.launcher.debug"))
				System.err.println("Pre-framework launcher arguments: " + arguments);
			final boolean alreadyPrepared = System.getProperty("gama.package.content") != null;
			if (hasOption(arguments, "-application") && !alreadyPrepared) return super.run(supplied);
			final Path install = Path.of(PackageLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getParent().getParent().toRealPath();
			final var settings = new Properties();
			try (var input = Files.newInputStream(install.resolve("configuration/gama-bootstrap.properties"))) {
				settings.load(input);
			}
			final boolean mac = System.getProperty("os.name").startsWith("Mac");
			if (mac) {
				System.load(install.resolve("gama-launcher.dylib").toString());
				if (!alreadyPrepared) {
					for (final String document : startupDocuments(Boolean.getBoolean("gama.launcher.debug"))) {
						final int vm = arguments.indexOf("-vmargs");
						if (!arguments.contains(document)) arguments.add(vm < 0 ? arguments.size() : vm, document);
					}
				}
			}
			final var documents = documents(arguments);
			if (Boolean.getBoolean("gama.launcher.debug"))
				System.err.println("Pre-framework startup documents: " + documents);
			if (!alreadyPrepared && (documents.size() != 1
					|| !documents.getFirst().toLowerCase(java.util.Locale.ROOT).endsWith(".gamx")))
				return super.run(arguments.toArray(String[]::new));
			final URL[] classpath = {
					install.resolve(settings.getProperty("export")).toUri().toURL(),
					install.resolve(settings.getProperty("osgi")).toUri().toURL()
			};
			final String[] prepared;
			try (var loader = new URLClassLoader(classpath, ClassLoader.getPlatformClassLoader())) {
				final var bootstrap = loader.loadClass("gama.export.PackageBootstrap");
				if (alreadyPrepared) {
					prepared = (String[]) bootstrap.getMethod("presentation", String.class)
							.invoke(null, System.getProperty("gama.package.content"));
				} else {
					final String executable = option(arguments, "-launcher");
					if (executable == null) throw new IOException("The native GAMA launcher path is missing");
					prepared = (String[]) bootstrap.getMethod("prepare", String.class, String.class, String.class, String.class)
							.invoke(null, documents.getFirst(), install.toString(), executable, settings.getProperty("runtime"));
				}
			}
			final var command = List.of(prepared);
			final int vm = command.indexOf("-vmargs");
			for (final String argument : command.subList(alreadyPrepared ? 0 : vm + 1, command.size())) {
				if (!argument.startsWith("-D")) throw new IOException("Unexpected prepared JVM argument: " + argument);
				final int equals = argument.indexOf('=');
				System.setProperty(argument.substring(2, equals), argument.substring(equals + 1));
			}
			final var playerArgs = new ArrayList<String>();
			// Preserve the native launcher handshake, VM and JNI bridge arguments.
			for (int i = 0; i < arguments.size(); i++) {
				final String value = arguments.get(i);
				if (value.equals("-vmargs")) break;
				if (value.equals("-showsplash") || value.equals("-name")
						|| !alreadyPrepared && List.of("-data", "-configuration", "-install", "-application").contains(value)) {
					if (i + 1 < arguments.size() && !arguments.get(i + 1).startsWith("-")) i++;
				} else if (!documents.contains(value) && !List.of("--", "-openFile", "--launcher.openFile", "-nosplash").contains(value)) {
					playerArgs.add(value);
				}
			}
			if (!alreadyPrepared) playerArgs.addAll(command.subList(1, vm));
			final String title = System.getProperty("gama.package.title", "").strip();
			final String description = System.getProperty("gama.package.description", "");
			final Path splash = Path.of(System.getProperty("gama.package.content")).resolve("player-splash.png");
			if (mac) {
				composeSplash(System.getProperty("gama.package.splash", ""), title, description, splash.toString());
			} else {
				SplashImage.write(System.getProperty("gama.package.splash", ""), title, description, splash);
			}
			playerArgs.addAll(List.of("-showsplash", splash.toString()));
			if (!title.isEmpty()) playerArgs.addAll(List.of("-name", title));
			if (alreadyPrepared) return super.run(playerArgs.toArray(String[]::new));
			final PrintStream out = System.out;
			final PrintStream err = System.err;
			try (var log = Files.newOutputStream(Path.of(System.getProperty("gama.package.content")).resolve("player.log"))) {
				System.setOut(tee(out, log));
				System.setErr(tee(err, log));
				try {
					return super.run(playerArgs.toArray(String[]::new));
				} finally {
					System.out.flush();
					System.err.flush();
					System.setOut(out);
					System.setErr(err);
				}
			}
		} catch (final Exception | LinkageError e) {
			final Throwable failure = e instanceof java.lang.reflect.InvocationTargetException invocation
					? invocation.getCause() : e;
			System.err.println("Unable to start GAMA: " + failure);
			failure.printStackTrace(System.err);
			if (!Boolean.getBoolean("gama.launcher.suppressErrors")) {
				try {
					final String message = "Unable to open the playable model:\n" + failure.getMessage();
					if (System.getProperty("os.name").startsWith("Mac")) showError(message);
					else if (!java.awt.GraphicsEnvironment.isHeadless())
						javax.swing.JOptionPane.showMessageDialog(null, message, "GAMA", javax.swing.JOptionPane.ERROR_MESSAGE);
				} catch (final RuntimeException | UnsatisfiedLinkError unavailable) {
					System.err.println("Startup error dialog unavailable: " + unavailable);
				}
			}
			return 1;
		}
	}

	private static PrintStream tee(final PrintStream console, final OutputStream log) {
		return new PrintStream(new OutputStream() {
			@Override public void write(final int value) throws IOException {
				synchronized (log) { console.write(value); log.write(value); }
			}
			@Override public void write(final byte[] bytes, final int offset, final int length) throws IOException {
				synchronized (log) { console.write(bytes, offset, length); log.write(bytes, offset, length); }
			}
			@Override public void flush() throws IOException {
				synchronized (log) { console.flush(); log.flush(); }
			}
		}, true, java.nio.charset.StandardCharsets.UTF_8);
	}

	private static boolean hasOption(final List<String> arguments, final String option) {
		final int vm = arguments.indexOf("-vmargs");
		final int index = arguments.indexOf(option);
		return index >= 0 && (vm < 0 || index < vm);
	}

	private static String option(final List<String> arguments, final String option) {
		final int index = arguments.indexOf(option);
		return index >= 0 && index + 1 < arguments.size() ? arguments.get(index + 1) : null;
	}

	static List<String> documents(final List<String> arguments) {
		final var result = new ArrayList<String>();
		final var values = List.of("-data", "-configuration", "-install", "-application", "-product", "-vm", "-name",
				"-startup", "--launcher.library", "--launcher.ini", "--launcher.defaultAction", "-launcher", "-exitdata",
				"-showsplash", "-os", "-ws", "-arch", "-nl");
		boolean documentsOnly = false;
		for (int i = 0; i < arguments.size(); i++) {
			final String argument = arguments.get(i);
			if (argument.equals("-vmargs")) break;
			if (List.of("--", "-openFile", "--launcher.openFile").contains(argument)) documentsOnly = true;
			else if (!documentsOnly && values.contains(argument)) {
				if (i + 1 < arguments.size() && !arguments.get(i + 1).startsWith("-")) i++;
			} else if (documentsOnly || !argument.startsWith("-")) result.add(argument);
		}
		return List.copyOf(result);
	}
}

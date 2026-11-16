/*******************************************************************************************************
 *
 * PackageCommandTest.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.packaging;

import java.util.List;
import java.util.Random;

import gama.headless.packaging.PackageCommand.Format;
import gama.headless.packaging.PackageCommand.Kind;

/** JDK-only checks of the command line of the archive commands; no GAMA, OSGi or workspace is needed. */
public final class PackageCommandTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	/** The command line must be rejected with a message naming {@code expected}. */
	private static void rejects(final List<String> args, final String expected) {
		try {
			PackageCommand.parse(args);
		} catch (final IllegalArgumentException e) {
			check(e.getMessage() != null && e.getMessage().contains(expected),
					"Unexpected message for " + args + ": " + e.getMessage());
			return;
		}
		throw new AssertionError("Accepted invalid command line " + args);
	}

	public static void main(final String[] args) {
		// Selection of the command
		check(PackageCommand.kindOf(List.of("-validate")).isEmpty(), "-validate is the library validator");
		check(PackageCommand.kindOf(List.of("-batch", "exp", "m.gaml")).isEmpty(), "Legacy commands are not ours");
		check(PackageCommand.kindOf(List.of("-v", "-package", "a", "b.gamx")).orElseThrow() == Kind.GENERATE,
				"-package selects generation");
		check(PackageCommand.kindOf(List.of("-validate-package", "a.gamx")).orElseThrow() == Kind.VALIDATE,
				"-validate-package selects validation");
		check(PackageCommand.kindOf(List.of("-run-package", "a.gamp")).orElseThrow() == Kind.RUN,
				"-run-package selects execution");
		try {
			PackageCommand.kindOf(List.of("-package", "-run-package"));
			throw new AssertionError("Two commands must be rejected");
		} catch (final IllegalArgumentException expected) {}

		// Generation
		final var gamx = PackageCommand.parse(List.of("-package", "/models/My Project", "out/My Model.GAMX",
				"-experiment", "e1", "-experiment", "e2@My Project/models/m.gaml", "-title", "A title with spaces",
				"-plugin", "a.jar", "-plugin", "b.jar", "-allow-missing-data", "-v"));
		check(gamx.positional().equals(List.of("/models/My Project", "out/My Model.GAMX")), "Positional lost");
		check(gamx.all("-experiment").equals(List.of("e1", "e2@My Project/models/m.gaml")), "Repeated option lost");
		check(gamx.all("-plugin").size() == 2, "Plugins lost");
		check("A title with spaces".equals(gamx.value("-title")), "Value with spaces lost");
		check(gamx.has("-allow-missing-data") && gamx.has("-v") && !gamx.has("-show-home"), "Flags lost");
		check(gamx.all("-project").isEmpty() && gamx.value("-id") == null, "Absent options must be empty");
		final var gamp = PackageCommand.parse(List.of("-package", "p", "p.gamp", "-include-plugins",
				"-resolve-links", "-plugin", "x.jar"));
		check(gamp.has("-include-plugins") && gamp.has("-resolve-links"), "gamp flags lost");
		rejects(List.of("-package", "p"), "expects");
		rejects(List.of("-package", "p", "out.gamx", "extra"), "expects");
		rejects(List.of("-package", "p", "out.zip"), ".gamx");
		rejects(List.of("-package", "p", "out.gamx", "-include-plugins"), "-include-plugins cannot be used");
		rejects(List.of("-package", "p", "out.gamp", "-experiment", "e"), "-experiment cannot be used");
		rejects(List.of("-package", "p", "out.gamx", "-title", "a", "-title", "b"), "only be given once");
		rejects(List.of("-package", "p", "out.gamx", "-title"), "requires a value");
		rejects(List.of("-package", "p", "out.gamx", "-title", ""), "requires a value");
		rejects(List.of("-package", "p", "out.gamx", "-until-cycle", "2"), "Unknown option");
		rejects(List.of("-package", "p", "out.gamx", "-resolve"), "Unknown option");

		// Validation
		check(PackageCommand.parse(List.of("-validate-package", "a.gamx")).positional().equals(List.of("a.gamx")),
				"Validation archive lost");
		rejects(List.of("-validate-package"), "expects");
		rejects(List.of("-validate-package", "a.gamx", "-until-cycle", "2"), "Unknown option");

		// Execution
		final var run = PackageCommand.parse(List.of("-run-package", "a.gamx", "-until-cycle", "2", "-experiment", "e",
				"-output", "out dir", "-hpc", "4", "-model", "P/models/m.gaml"));
		check(run.integer("-until-cycle") == 2 && run.integer("-hpc") == 4, "Integers lost");
		check("out dir".equals(run.value("-output")) && "P/models/m.gaml".equals(run.value("-model")),
				"Run values lost");
		check(!PackageCommand.parse(List.of("-run-package", "a.gamx")).has("-until-cycle"), "Forever by default");
		check(PackageCommand.parse(List.of("-run-package", "a.gamx", "-until-cycle", "0")).integer("-until-cycle") == 0,
				"Cycle 0 only initializes the experiment");
		rejects(List.of("-run-package", "a.gamx", "-until-cycle", "-1"), "positive or zero");
		rejects(List.of("-run-package", "a.gamx", "-until-cycle", "two"), "integer");
		rejects(List.of("-run-package", "a.gamx", "-steps", "2"), "Unknown option");
		rejects(List.of("-run-package", "a.gamx", "-hpc", "0"), "at least 1");
		rejects(List.of("-run-package", "a.gamx", "-experiment", "a", "-experiment", "b"), "only be given once");
		rejects(List.of("-run-package", "a.gamx", "b.gamx"), "expects");

		// Formats
		check(Format.of("a.gamx").orElseThrow() == Format.GAMX && Format.of("A.GAMP").orElseThrow() == Format.GAMP,
				"Formats are case-insensitive");
		check(Format.of("a.gamx.zip").isEmpty() && Format.of("gamx").isEmpty(), "Only the extension counts");

		// Robustness: random command lines are either parsed or rejected with an IllegalArgumentException
		final List<String> tokens = List.of("-package", "-validate-package", "-run-package", "-until-cycle", "-plugin",
				"-experiment", "-title", "-hpc", "-include-plugins", "-allow-missing-data", "-v", "a.gamx", "b.gamp",
				"dir", "3", "-1", "", "x y", "-", "--steps", "-output");
		final Random random = new Random(42);
		for (int i = 0; i < 20_000; i++) {
			final List<String> line = random.ints(random.nextInt(8), 0, tokens.size()).mapToObj(tokens::get).toList();
			try {
				if (PackageCommand.kindOf(line).isEmpty()) { continue; }
				final var parsed = PackageCommand.parse(line);
				check(parsed.positional().size() == parsed.kind().arity, "Wrong arity accepted: " + line);
				check(parsed.positional().stream().noneMatch(p -> p.startsWith("-") && p.length() > 1),
						"Option taken as positional: " + line);
			} catch (final IllegalArgumentException expected) {
				check(expected.getMessage() != null && !expected.getMessage().isBlank(),
						"Rejections must explain the problem: " + line);
			}
		}
		System.out.println("PackageCommandTest: all checks passed");
	}
}

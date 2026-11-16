/*******************************************************************************************************
 *
 * PackageCommand.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.packaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The parsed command line of the headless {@code .gamx}/{@code .gamp} commands. Parsing is pure: it does not touch
 * GAMA, OSGi or the workspace, so that malformed command lines are rejected before anything is initialized.
 */
public record PackageCommand(Kind kind, List<String> positional, Map<String, List<String>> values, Set<String> flags) {

	/** The three package commands, with the number of positional arguments they take. */
	public enum Kind {

		/** Writes a .gamx playable model or a .gamp project archive from a project or a model. */
		GENERATE("-package", 2, "<source> <output.gamx|output.gamp>"),

		/** Checks the layout, manifest, plugins, host compatibility and models of an archive. */
		VALIDATE("-validate-package", 1, "<archive.gamx|archive.gamp>"),

		/** Runs one experiment of an archive, forever or for a number of steps. */
		RUN("-run-package", 1, "<archive.gamx|archive.gamp>");

		/** The option that selects the command. */
		public final String option;

		/** The number of positional arguments. */
		final int arity;

		/** The usage of the positional arguments. */
		final String usage;

		Kind(final String option, final int arity, final String usage) {
			this.option = option;
			this.arity = arity;
			this.usage = usage;
		}
	}

	/** Installs a plugin JAR for the duration of the command (repeatable). */
	public static final String PLUGIN = "-plugin";

	/** Registers an additional project, e.g. one whose models are imported by the exported ones (repeatable). */
	public static final String PROJECT = "-project";

	/** Selects an experiment, as {@code name} or {@code name@Project/path/model.gaml} (repeatable for -package). */
	public static final String EXPERIMENT = "-experiment";

	/** Restricts the experiment selection to one model, as {@code Project/path/model.gaml}. */
	public static final String MODEL = "-model";

	/** The experiment a .gamx opens at startup. */
	public static final String DEFAULT_EXPERIMENT = "-default-experiment";

	/** The .gamx title. */
	public static final String TITLE = "-title";

	/** The .gamx description. */
	public static final String DESCRIPTION = "-description";

	/** The .gamx presentation image. */
	public static final String IMAGE = "-image";

	/** The .gamx identifier. */
	public static final String ID = "-id";

	/** The .gamx version. */
	public static final String VERSION = "-version";

	/** The cycle at which the run stops; the experiment runs until it stops by itself when absent. */
	public static final String UNTIL_CYCLE = "-until-cycle";

	/** The directory receiving the extracted archive and the simulation outputs. */
	public static final String OUTPUT = "-output";

	/** The number of threads available to the experiment. */
	public static final String THREADS = "-hpc";

	/** Exports a .gamx even though some statically referenced data files are missing. */
	public static final String ALLOW_MISSING_DATA = "-allow-missing-data";

	/** Shows the experiment selector of a .gamx at startup even when it has a default experiment. */
	public static final String SHOW_HOME = "-show-home";

	/** Materializes the linked resources of a .gamp project. */
	public static final String RESOLVE_LINKS = "-resolve-links";

	/** Embeds the required non-standard plugins in a .gamp. */
	public static final String INCLUDE_PLUGINS = "-include-plugins";

	/** Verbose mode, shared with the other headless commands. */
	public static final String VERBOSE = "-v";

	/** Options that can be given several times, per command. */
	private static final Map<Kind, Set<String>> REPEATABLE = Map.of(Kind.GENERATE, Set.of(PLUGIN, PROJECT, EXPERIMENT),
			Kind.VALIDATE, Set.of(PLUGIN), Kind.RUN, Set.of(PLUGIN));

	/** Options followed by a value, per command. */
	private static final Map<Kind, Set<String>> VALUE_OPTIONS = Map.of(Kind.GENERATE,
			Set.of(PLUGIN, PROJECT, EXPERIMENT, DEFAULT_EXPERIMENT, TITLE, DESCRIPTION, IMAGE, ID, VERSION),
			Kind.VALIDATE, Set.of(PLUGIN), Kind.RUN, Set.of(PLUGIN, EXPERIMENT, MODEL, UNTIL_CYCLE, OUTPUT, THREADS));

	/** Options without value, per command. */
	private static final Map<Kind, Set<String>> FLAG_OPTIONS =
			Map.of(Kind.GENERATE, Set.of(ALLOW_MISSING_DATA, SHOW_HOME, RESOLVE_LINKS, INCLUDE_PLUGINS, VERBOSE),
					Kind.VALIDATE, Set.of(VERBOSE), Kind.RUN, Set.of(VERBOSE));

	/** Options only meaningful when generating a .gamx. */
	static final Set<String> GAMX_ONLY = Set.of(PROJECT, EXPERIMENT, DEFAULT_EXPERIMENT, TITLE, DESCRIPTION, IMAGE,
			ID, VERSION, ALLOW_MISSING_DATA, SHOW_HOME);

	/** Options only meaningful when generating a .gamp. */
	static final Set<String> GAMP_ONLY = Set.of(RESOLVE_LINKS, INCLUDE_PLUGINS);

	public PackageCommand {
		positional = List.copyOf(positional);
		final var copy = new LinkedHashMap<String, List<String>>();
		values.forEach((k, v) -> copy.put(k, List.copyOf(v)));
		values = Collections.unmodifiableMap(copy);
		flags = Collections.unmodifiableSet(new LinkedHashSet<>(flags));
	}

	/**
	 * Returns the package command selected by the arguments, if any.
	 *
	 * @throws IllegalArgumentException
	 *             if several package commands are given
	 */
	public static Optional<Kind> kindOf(final List<String> args) {
		Kind result = null;
		for (final Kind kind : Kind.values()) {
			if (!args.contains(kind.option)) { continue; }
			if (result != null)
				throw new IllegalArgumentException(result.option + " and " + kind.option + " cannot be combined");
			result = kind;
		}
		return Optional.ofNullable(result);
	}

	/**
	 * Parses the arguments of a package command.
	 *
	 * @throws IllegalArgumentException
	 *             with a message meant for the user, if the command line is invalid
	 */
	public static PackageCommand parse(final List<String> args) {
		final Kind kind = kindOf(args)
				.orElseThrow(() -> new IllegalArgumentException("No package command in the arguments"));
		final Set<String> valueOptions = VALUE_OPTIONS.get(kind);
		final Set<String> flagOptions = FLAG_OPTIONS.get(kind);
		final List<String> positional = new ArrayList<>();
		final Map<String, List<String>> values = new LinkedHashMap<>();
		final Set<String> flags = new LinkedHashSet<>();
		for (int i = 0; i < args.size(); i++) {
			final String arg = args.get(i);
			if (arg.equals(kind.option)) { continue; }
			if (valueOptions.contains(arg)) {
				if (i + 1 >= args.size() || args.get(i + 1).isBlank())
					throw new IllegalArgumentException(arg + " requires a value");
				final String value = args.get(++i);
				final List<String> list = values.computeIfAbsent(arg, k -> new ArrayList<>());
				if (!list.isEmpty() && !REPEATABLE.get(kind).contains(arg))
					throw new IllegalArgumentException(arg + " can only be given once");
				list.add(value);
			} else if (flagOptions.contains(arg)) {
				flags.add(arg);
			} else if (arg.startsWith("-") && arg.length() > 1) {
				throw new IllegalArgumentException("Unknown option for " + kind.option + ": " + arg);
			} else {
				positional.add(arg);
			}
		}
		if (positional.size() != kind.arity)
			throw new IllegalArgumentException(kind.option + " expects " + kind.usage + ", got "
					+ (positional.isEmpty() ? "nothing" : String.join(" ", positional)));
		final PackageCommand command = new PackageCommand(kind, positional, values, flags);
		if (kind == Kind.GENERATE) { command.checkGenerateOptions(); }
		if (kind == Kind.RUN) {
			command.nonNegative(UNTIL_CYCLE);
			command.nonNegative(THREADS);
			if (command.has(THREADS) && command.integer(THREADS) == 0)
				throw new IllegalArgumentException(THREADS + " must be at least 1");
		}
		return command;
	}

	/** Rejects the options of the other archive format. */
	private void checkGenerateOptions() {
		final Format format = Format.of(positional.get(1))
				.orElseThrow(() -> new IllegalArgumentException("The output of " + kind.option
						+ " must end with .gamx (playable model) or .gamp (project archive): " + positional.get(1)));
		final Set<String> forbidden = format == Format.GAMX ? GAMP_ONLY : GAMX_ONLY;
		for (final String option : forbidden) {
			if (has(option))
				throw new IllegalArgumentException(option + " cannot be used to generate a " + format.extension);
		}
	}

	/** The two archive formats. */
	public enum Format {

		/** Playable model. */
		GAMX(".gamx"),

		/** Editable project archive. */
		GAMP(".gamp");

		/** The file extension, with its dot. */
		public final String extension;

		Format(final String extension) {
			this.extension = extension;
		}

		/** The format of a file name, from its extension (case-insensitive). */
		public static Optional<Format> of(final String fileName) {
			final String name = fileName.toLowerCase(Locale.ROOT);
			for (final Format format : values()) { if (name.endsWith(format.extension)) return Optional.of(format); }
			return Optional.empty();
		}
	}

	/** Whether the option or flag was given. */
	public boolean has(final String option) {
		return flags.contains(option) || values.containsKey(option);
	}

	/** The value of a single-valued option, or null. */
	public String value(final String option) {
		final List<String> list = values.get(option);
		return list == null ? null : list.getFirst();
	}

	/** All the values of an option, possibly empty. */
	public List<String> all(final String option) {
		return values.getOrDefault(option, List.of());
	}

	/** The integer value of an option. */
	public long integer(final String option) {
		try {
			return Long.parseLong(value(option));
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException(option + " expects an integer, got " + value(option));
		}
	}

	private void nonNegative(final String option) {
		if (has(option) && integer(option) < 0)
			throw new IllegalArgumentException(option + " must be positive or zero, got " + value(option));
	}

	/** The help of the package commands, appended to the headless help. */
	public static String help() {
		final String o = "\n\t\t";
		final String d = "\n\t\t                              -- ";
		return "\n\t=== GAMA archives (.gamx playable models, .gamp project archives) ===" //
				+ o + Kind.GENERATE.option + " [source] [output.gamx|output.gamp] [options]" //
				+ d + "generate an archive from a project directory or from a model file in a project;" //
				+ d + "the format follows the extension of the output" //
				+ o + "    " + PLUGIN + " [file.jar]        -- install a plugin for this command, declare/embed it (repeatable)" //
				+ o + "    .gamx: " + EXPERIMENT + " [name|name@Project/model.gaml] (repeatable; default: all)" //
				+ o + "           " + DEFAULT_EXPERIMENT + " [name|name@Project/model.gaml]  " + SHOW_HOME //
				+ o + "           " + TITLE + " [text]  " + DESCRIPTION + " [text]  " + IMAGE + " [file]  " + ID
				+ " [id]  " + VERSION + " [x.y.z]" //
				+ o + "           " + PROJECT + " [dir]    -- register another project used by the models (repeatable)" //
				+ o + "           " + ALLOW_MISSING_DATA + " -- export even if referenced data files are missing" //
				+ o + "    .gamp: " + RESOLVE_LINKS + "     -- include the targets of linked resources" //
				+ o + "           " + INCLUDE_PLUGINS + "   -- embed the required non-standard plugins" //
				+ o + Kind.VALIDATE.option + " [archive] [" + PLUGIN + " file.jar]" //
				+ d + "check the layout, manifest, plugins, host compatibility and models of an archive" //
				+ o + Kind.RUN.option + " [archive] [options]" //
				+ d + "run one experiment of an archive; its plugins are installed for the run only" //
				+ o + "    " + EXPERIMENT + " [name|name@Project/model.gaml]  " + MODEL + " [Project/model.gaml]" //
				+ o + "    " + UNTIL_CYCLE + " [N]          -- stop when the simulation reaches cycle N; default: until it stops" //
				+ o + "    " + OUTPUT + " [dir]             -- keep the extracted archive and outputs in this new or empty directory" //
				+ o + "    " + THREADS + " [threads]  " + PLUGIN + " [file.jar]";
	}
}

package gaml.grammar.transition;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gama.dev.DEBUG;

/**
 * Applies a configurable chain of {@link IFileTransformer} instances to every
 * GAML source file found recursively under a given root directory.
 *
 * <p>By default the processor is pre-loaded with all built-in transformers
 * (one for each Python migration script):</p>
	 * <ol>
	 *   <li>{@link FixDiffuseVar} – replaces {@code diffuse var:} with
	 *       {@code diffuse}</li>
	 *   <li>{@link FixTransitionTo} – replaces {@code transition to:} with
	 *       {@code transition}</li>
	 *   <li>{@link FixDisplayExperimentNames} – rewrites quoted display/experiment
	 *       names to identifier + {@code title:} syntax</li>
	 *   <li>{@link FixImageToPicture} – replaces the {@code image} layer keyword
	 *       with {@code picture} inside {@code display} blocks</li>
	 *   <li>{@link FixWithPairs} – transforms {@code with: [key::val]} to
	 *       {@code with: (key:val)}</li>
	 *   <li>{@link FixArrowBraces} – removes outer braces from
	 *       {@code -> { ... }} expressions</li>
	 *   <li>{@link FixActionParentheses} – adds missing empty parentheses to
	 *       parameter-less {@code action} and typed-action declarations
	 *       (e.g. {@code action foo \{} → {@code action foo() \{})</li>
	 *   <li>{@link FixLetStatement} – transforms {@code let var type:T value:V;}
	 *       into {@code T var <- V;}</li>
	 * </ol>
 *
 * <p>Additional transformers can be registered at any time via
 * {@link #addTransformer(IFileTransformer)}. The transformers are always
 * applied in registration order.</p>
 *
 * <h3>Typical usage</h3>
 * <pre>
 *   // Process all .gaml and .experiment files under a directory
 *   GamlFileProcessor processor = new GamlFileProcessor();
 *   int changed = processor.processDirectory("/path/to/workspace", false);
 *   System.out.println("Updated " + changed + " file(s).");
 *
 *   // Dry-run only
 *   processor.processDirectory("/path/to/workspace", true);
 *
 *   // Add a custom transformer
 *   processor.addTransformer(content -> content.replace("old", "new"));
 * </pre>
 *
 * <h3>Command-line usage</h3>
 * <pre>
 *   java gaml.grammar.transition.GamlFileProcessor &lt;root&gt; [--dry-run]
 * </pre>
 *
 * <p>All informational output is routed through {@link DEBUG#LOG(Object)} and all error
 * output through {@link DEBUG#ERR(Object)}. Debugging is automatically enabled for this
 * class via {@link DEBUG#ON()} in the static initialiser.</p>
 */
public class GamlFileProcessor {

	static {
		DEBUG.ON();
	}

	// -----------------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------------

	/**
	 * Default file extensions processed by {@link #processDirectory}.
	 * Files whose name ends with one of these suffixes are included.
	 */
	public static final List<String> DEFAULT_EXTENSIONS =
			Collections.unmodifiableList(Arrays.asList(".gaml", ".experiment"));

	// -----------------------------------------------------------------------
	// State
	// -----------------------------------------------------------------------

	/**
	 * Ordered list of transformers applied to each file's content.
	 * Transformers are applied in the order they appear in this list.
	 */
	private final List<IFileTransformer> transformers = new ArrayList<>();

	/**
	 * File extensions to include when walking the directory tree.
	 * Defaults to {@link #DEFAULT_EXTENSIONS}.
	 */
	private final List<String> extensions;

	// -----------------------------------------------------------------------
	// Constructors
	// -----------------------------------------------------------------------

	/**
	 * Create a processor pre-loaded with all built-in transformers and the
	 * {@link #DEFAULT_EXTENSIONS default file extensions}.
	 *
	 * <p>Output is routed through {@link DEBUG#LOG(Object)} and
	 * {@link DEBUG#ERR(Object)}.</p>
	 */
	public GamlFileProcessor() {
		this(DEFAULT_EXTENSIONS);
	}

	/**
	 * Create a processor with a custom list of file extensions.
	 *
	 * <p>The built-in transformers are registered in the order listed in the
	 * class-level documentation.</p>
	 *
	 * @param extensions
	 *            the file-name suffixes to include (e.g. {@code ".gaml"});
	 *            must not be {@code null}
	 */
	public GamlFileProcessor(final List<String> extensions) {
		this.extensions = new ArrayList<>(extensions);
		registerBuiltInTransformers();
	}

	// -----------------------------------------------------------------------
	// Configuration
	// -----------------------------------------------------------------------

	/**
	 * Register all built-in transformers in a fixed, deterministic order.
	 *
	 * <p>This method is called once by the constructor. It can be overridden in
	 * subclasses to change the default set of transformers.</p>
	 */
	protected void registerBuiltInTransformers() {
		transformers.add(new FixDiffuseVar());
		transformers.add(new FixTransitionTo());
		transformers.add(new FixDisplayExperimentNames());
		transformers.add(new FixImageToPicture());
		transformers.add(new FixWithPairs());
		transformers.add(new FixArrowBraces());
		transformers.add(new FixActionParentheses());
		transformers.add(new FixSetStatement());
		transformers.add(new FixLetStatement());
		transformers.add(new FixDoParentheses());
	}

	/**
	 * Append a transformer to the end of the transformation chain.
	 *
	 * @param transformer
	 *            the transformer to add; must not be {@code null}
	 */
	public void addTransformer(final IFileTransformer transformer) {
		transformers.add(transformer);
	}

	/**
	 * Return an unmodifiable view of the current list of transformers.
	 *
	 * @return an unmodifiable {@link List} of {@link IFileTransformer}s in
	 *         registration order; never {@code null}
	 */
	public List<IFileTransformer> getTransformers() {
		return Collections.unmodifiableList(transformers);
	}

	/**
	 * Return the list of file extensions that this processor will consider.
	 *
	 * @return an unmodifiable {@link List} of extension strings; never
	 *         {@code null}
	 */
	public List<String> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}

	// -----------------------------------------------------------------------
	// Processing
	// -----------------------------------------------------------------------

	/**
	 * Apply all registered transformers to a single file.
	 *
	 * <p>The file is read as UTF-8. Each transformer is applied in order to the
	 * current content; the output of one transformer becomes the input of the
	 * next. If the final content differs from the original the file is
	 * overwritten (unless {@code dryRun} is {@code true}).</p>
	 *
	 * <p>Progress and diff output is sent to {@link DEBUG#LOG(Object)};
	 * errors are sent to {@link DEBUG#ERR(Object)}.</p>
	 *
	 * @param path
	 *            the path of the file to process; must not be {@code null}
	 * @param dryRun
	 *            when {@code true} the file is not written; changes are only
	 *            logged via {@link DEBUG#LOG(Object)}
	 * @return {@code true} if at least one transformation changed the content
	 *         (or would have changed it in dry-run mode)
	 */
	public boolean processFile(final Path path, final boolean dryRun) {
		final String original;
		try {
			original = Files.readString(path, StandardCharsets.UTF_8);
		} catch (final MalformedInputException e) {
			DEBUG.ERR("  [SKIP] Not valid UTF-8: " + path);
			return false;
		} catch (final IOException e) {
			DEBUG.ERR("  [ERROR] Cannot read " + path + ": " + e.getMessage());
			return false;
		}

		// Apply every transformer in order
		String content = original;
		for (final IFileTransformer transformer : transformers) {
			content = transformer.transform(content);
		}

		if (content.equals(original)) { return false; }

		DEBUG.LOG("  " + (dryRun ? "[dry-run] " : "") + "Updating: " + path);
		printLineDiff(original, content);

		if (!dryRun) {
			try {
				Files.writeString(path, content, StandardCharsets.UTF_8);
			} catch (final IOException e) {
				DEBUG.ERR("  [ERROR] Cannot write " + path + ": " + e.getMessage());
				return false;
			}
		}
		return true;
	}

	/**
	 * Recursively walk {@code root} and process every file whose name ends with
	 * one of the configured {@link #extensions}.
	 *
	 * <p>Hidden directories (whose name starts with {@code '.'}) are skipped.</p>
	 *
	 * @param root
	 *            the root directory to search; must not be {@code null}
	 * @param dryRun
	 *            when {@code true} no files are written
	 * @return the total number of files that were (or would be) updated
	 * @throws IOException
	 *             if an I/O error occurs while walking the directory tree
	 */
	public int processDirectory(final Path root, final boolean dryRun) throws IOException {
		final int[] counts = { 0, 0 }; // [total, changed]

		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(final Path dir,
					final BasicFileAttributes attrs) {
				// Skip hidden directories
				if (dir.getFileName() != null
						&& dir.getFileName().toString().startsWith(".")) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file,
					final BasicFileAttributes attrs) {
				final String name = file.getFileName().toString();
				final boolean matches =
						extensions.stream().anyMatch(name::endsWith);
				if (matches) {
					counts[0]++;
					if (processFile(file, dryRun)) { counts[1]++; }
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(final Path file,
					final IOException exc) {
				DEBUG.ERR("  [ERROR] Cannot visit " + file + ": " + exc.getMessage());
				return FileVisitResult.CONTINUE;
			}
		});

		final String action = dryRun ? "Would update" : "Updated";
		DEBUG.LOG("\n" + action + " " + counts[1] + " / " + counts[0] + " file(s).");
		return counts[1];
	}

	/**
	 * Convenience overload that accepts a {@link String} path.
	 *
	 * @param rootPath
	 *            the root directory as a string; must not be {@code null}
	 * @param dryRun
	 *            when {@code true} no files are written
	 * @return the total number of files that were (or would be) updated
	 * @throws IOException
	 *             if an I/O error occurs while walking the directory tree
	 * @see #processDirectory(Path, boolean)
	 */
	public int processDirectory(final String rootPath, final boolean dryRun)
			throws IOException {
		return processDirectory(Paths.get(rootPath), dryRun);
	}

	// -----------------------------------------------------------------------
	// Helpers
	// -----------------------------------------------------------------------

	/**
	 * Log a compact line-by-line diff between {@code original} and
	 * {@code updated} via {@link DEBUG#LOG(Object)}.
	 *
	 * <p>Only lines that actually changed are shown. If the number of lines
	 * differs (e.g. because a multi-line block was collapsed) this is noted
	 * as well.</p>
	 *
	 * @param original
	 *            the original file content; never {@code null}
	 * @param updated
	 *            the transformed file content; never {@code null}
	 */
	private void printLineDiff(final String original, final String updated) {
		final String[] origLines = original.split("\n", -1);
		final String[] newLines = updated.split("\n", -1);
		final int limit = Math.min(origLines.length, newLines.length);
		for (int i = 0; i < limit; i++) {
			if (!origLines[i].equals(newLines[i])) {
				DEBUG.LOG("    line " + (i + 1) + ":");
				DEBUG.LOG("      - " + origLines[i]);
				DEBUG.LOG("      + " + newLines[i]);
			}
		}
		if (origLines.length != newLines.length) {
			DEBUG.LOG("    (line count changed: "
					+ origLines.length + " → " + newLines.length + ")");
		}
	}

	// -----------------------------------------------------------------------
	// main
	// -----------------------------------------------------------------------

	/**
	 * Command-line entry point.
	 *
	 * <p>Usage:</p>
	 * <pre>
	 *   java gaml.grammar.transition.GamlFileProcessor &lt;root&gt; [--dry-run]
	 * </pre>
	 *
	 * <p>Arguments:</p>
	 * <ul>
	 *   <li>{@code root} – the root directory to search recursively
	 *       (required)</li>
	 *   <li>{@code --dry-run} – optional flag; when present no files are
	 *       written and only a preview is printed</li>
	 * </ul>
	 *
	 * @param args
	 *            command-line arguments; never {@code null}
	 */
	public static void main(final String[] args) {
		if (args.length < 1) {
			DEBUG.ERR("Usage: GamlFileProcessor <root> [--dry-run]");
			System.exit(1);
		}
		final String root = args[0];
		boolean dryRun = false;
		for (int i = 1; i < args.length; i++) {
			if ("--dry-run".equalsIgnoreCase(args[i])) { dryRun = true; }
		}

		try {
			new GamlFileProcessor().processDirectory(root, dryRun);
		} catch (final IOException e) {
			DEBUG.ERR("[ERROR] " + e.getMessage());
			System.exit(1);
		}
	}
}

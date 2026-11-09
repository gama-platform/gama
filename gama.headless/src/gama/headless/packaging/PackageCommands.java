/*******************************************************************************************************
 *
 * PackageCommands.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.packaging;

import static gama.headless.packaging.PackageCommand.ALLOW_MISSING_DATA;
import static gama.headless.packaging.PackageCommand.DEFAULT_EXPERIMENT;
import static gama.headless.packaging.PackageCommand.DESCRIPTION;
import static gama.headless.packaging.PackageCommand.EXPERIMENT;
import static gama.headless.packaging.PackageCommand.ID;
import static gama.headless.packaging.PackageCommand.IMAGE;
import static gama.headless.packaging.PackageCommand.INCLUDE_PLUGINS;
import static gama.headless.packaging.PackageCommand.MODEL;
import static gama.headless.packaging.PackageCommand.OUTPUT;
import static gama.headless.packaging.PackageCommand.PLUGIN;
import static gama.headless.packaging.PackageCommand.PROJECT;
import static gama.headless.packaging.PackageCommand.RESOLVE_LINKS;
import static gama.headless.packaging.PackageCommand.SHOW_HOME;
import static gama.headless.packaging.PackageCommand.THREADS;
import static gama.headless.packaging.PackageCommand.TITLE;
import static gama.headless.packaging.PackageCommand.UNTIL_CYCLE;
import static gama.headless.packaging.PackageCommand.VERSION;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;

import gama.api.GAMA;
import gama.api.compilation.GamlCompilationError;
import gama.api.constants.GamlFileExtension;
import gama.api.exceptions.GamaCompilationFailedException;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.SystemInfo;
import gama.export.ExportModels;
import gama.export.LauncherConfiguration;
import gama.export.ProjectArchive;
import gama.export.ProjectArchiveExporter;
import gama.export.SimulationLaunchContext;
import gama.export.SimulationPackage;
import gama.export.SimulationPackageExporter;
import gama.export.SimulationPackageManifest;
import gama.export.SimulationPackageManifest.BundleRequirement;
import gama.headless.common.Globals;
import gama.headless.job.ExperimentJob;
import gama.headless.job.IExperimentJob;
import gama.headless.job.PackageExperimentJob;
import gama.headless.packaging.PackageCommand.Format;
import gama.headless.runtime.SimulationRuntime;
import gama.headless.script.ExperimentationPlanFactory;
import gama.headless.xml.XMLWriter;
import gaml.compiler.validation.GamlModelBuilder;

/**
 * The headless commands generating, validating and running {@code .gamx} playable models and {@code .gamp} project
 * archives. They reuse the codecs and exporters of the GUI (bundle {@code gama.export}), so that the archives are the
 * same whichever way they are produced.
 *
 * <p>
 * Plugins carried by an archive, or given with {@code -plugin}, are installed in the running framework before GAMA is
 * initialized, and uninstalled at the end of the command: they are never added to the installation.
 * </p>
 *
 * <p>
 * Exit codes: 0 on success, 1 when the archive is invalid or the command fails, 2 when the command line is invalid.
 * </p>
 */
public final class PackageCommands {

	/** Exit code of a successful command. */
	public static final int OK = 0;

	/** Exit code of a failed command, or of an invalid archive. */
	public static final int FAILED = 1;

	/** Exit code of an invalid command line. */
	public static final int USAGE = 2;

	/** The parsed command. */
	private final PackageCommand command;

	/** Initializes GAMA (compiler and GAML additions). */
	private final Runnable initializeGama;

	/** The plugins installed for this command. */
	private final TemporaryPlugins plugins;

	/** Temporary directories still to delete if the JVM is stopped (e.g. Ctrl+C on a run without -until-cycle). */
	private static final java.util.Set<Path> TEMPORARY = java.util.concurrent.ConcurrentHashMap.newKeySet();

	/** Stops the running experiment, if any, so that its directories can be deleted when the JVM stops. */
	private static volatile Runnable stopRunning = () -> {};

	/** The plugins of the command, uninstalled before their files are deleted when the JVM stops. */
	private static volatile TemporaryPlugins activePlugins;

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			stopRunning.run();
			final TemporaryPlugins plugins = activePlugins;
			if (plugins != null) { plugins.close(); }
			TEMPORARY.forEach(PackageCommands::delete);
		}, "Archive command cleanup"));
	}

	/** The problems found by the validation. */
	private final List<String> errors = new ArrayList<>();

	/** The warnings found by the validation. */
	private final List<String> warnings = new ArrayList<>();

	private PackageCommands(final PackageCommand command, final Runnable initializeGama,
			final TemporaryPlugins plugins) {
		this.command = command;
		this.initializeGama = initializeGama;
		this.plugins = plugins;
	}

	/**
	 * Executes the package command of the arguments.
	 *
	 * @param args
	 *            the application arguments
	 * @param initializeGama
	 *            initializes GAMA; called once the plugins to install are known and installed
	 * @return the exit code
	 */
	public static int execute(final List<String> args, final Runnable initializeGama) {
		final PackageCommand command;
		try {
			command = PackageCommand.parse(args);
		} catch (final IllegalArgumentException e) {
			error(e.getMessage());
			error("Run gama-headless -help for the usage of the archive commands.");
			return USAGE;
		}
		TemporaryPlugins.removeLeftovers();
		try (var plugins = new TemporaryPlugins()) {
			activePlugins = plugins;
			final var commands = new PackageCommands(command, initializeGama, plugins);
			return switch (command.kind()) {
				case GENERATE -> commands.generate();
				case VALIDATE -> commands.validate();
				case RUN -> commands.run();
			};
		} catch (final IOException | CoreException | OperationCanceledException | IllegalArgumentException e) {
			error(message(e));
			return FAILED;
		} catch (final RuntimeException | LinkageError e) {
			error("Unexpected failure: " + message(e));
			e.printStackTrace();
			return FAILED;
		}
	}

	// =====================================================================================================
	// Generation
	// =====================================================================================================

	/** Writes a .gamx or .gamp from a project directory or from a model file of a project. */
	private int generate() throws IOException, CoreException {
		final Path source = Path.of(command.positional().get(0)).toAbsolutePath().normalize();
		final Path output = Path.of(command.positional().get(1)).toAbsolutePath().normalize();
		final Format format = Format.of(output.getFileName().toString()).orElseThrow();
		if (!Files.exists(source)) throw new IOException("The source does not exist: " + source);
		final Path model = Files.isDirectory(source) ? null : source;
		if (model != null && !GamlFileExtension.isGaml(model.toString())
				&& !GamlFileExtension.isExperiment(model.toString()))
			throw new IOException("The source must be a project directory or a .gaml/.experiment model: " + source);
		final Path projectDirectory = model == null ? source : WorkspaceProjects.projectDirectoryOf(model);
		installRequestedPlugins();
		plugins.resolve();
		initializeGama.run();
		try (var projects = new WorkspaceProjects()) {
			final IProject project = projects.register(projectDirectory);
			for (final String other : command.all(PROJECT)) { projects.register(Path.of(other)); }
			if (format == Format.GAMX) {
				final List<IFile> roots = model == null ? ExportModels.modelFiles(project) : List.of(project
						.getFile(IPath.fromOSString(projectDirectory.relativize(model).toString()).makeRelative()));
				generateGamx(roots, output);
			} else {
				if (model != null) {
					info("The whole project " + project.getName() + " is archived, not only " + model.getFileName());
				}
				generateGamp(project, output);
			}
		}
		return OK;
	}

	/** Writes a playable model. */
	private void generateGamx(final List<IFile> roots, final Path output) throws IOException, CoreException {
		final ExportModels models;
		try {
			models = ExportModels.discover(roots, command.has(ALLOW_MISSING_DATA) ? missing -> {
				missing.forEach(PackageCommands::warning);
				return true;
			} : null);
		} catch (final IOException e) {
			if (e.getMessage() != null && e.getMessage().startsWith("Unable to find model data file"))
				throw new IOException(e.getMessage() + "\nUse " + ALLOW_MISSING_DATA
						+ " if these files are only used in branches that are never executed.", e);
			throw e;
		}
		final List<ExportModels.Experiment> selected = new ArrayList<>();
		if (command.all(EXPERIMENT).isEmpty()) {
			selected.addAll(models.experiments());
		} else {
			for (final String selector : command.all(EXPERIMENT)) {
				final var experiment = select(models.experiments(), selector, EXPERIMENT);
				if (!selected.contains(experiment)) { selected.add(experiment); }
			}
		}
		String startup = "";
		if (command.has(DEFAULT_EXPERIMENT)) {
			final var experiment = select(selected, command.value(DEFAULT_EXPERIMENT), DEFAULT_EXPERIMENT);
			startup = experiment.name() + "@" + experiment.modelPath().replaceFirst("^/", "");
		}
		String image = "";
		if (command.has(IMAGE)) {
			final Path file = Path.of(command.value(IMAGE)).toAbsolutePath().normalize();
			if (!Files.isRegularFile(file)) throw new IOException("The image does not exist: " + file);
			image = file.toString();
		}
		final var configuration = new LauncherConfiguration(
				command.has(TITLE) ? command.value(TITLE) : models.defaultTitle(),
				command.has(DESCRIPTION) ? command.value(DESCRIPTION) : models.defaultDescription(),
				command.has(SHOW_HOME), startup, image);
		final String id = command.has(ID) ? command.value(ID) : "simulation." + java.util.UUID.randomUUID();
		final String version = command.has(VERSION) ? command.value(VERSION) : "1.0.0";
		final List<Path> embedded = List.copyOf(plugins.files().values());
		SimulationPackageExporter.export(models, selected, configuration, id, version, embedded, output,
				new NullProgressMonitor());
		final var manifest = SimulationPackage.inspect(output);
		info("Playable model written to " + output + " (" + Files.size(output) + " bytes)");
		info("  identifier " + manifest.id() + ", version " + manifest.version() + ", requires GAMA "
				+ manifest.runtimeVersion() + " or newer");
		for (final var experiment : manifest.experiments()) {
			info("  experiment " + experiment.selection()
					+ (experiment.selection().equals(startup) ? " (default)" : ""));
		}
		printBundles(manifest.bundles());
	}

	/** Writes an editable project archive. */
	private void generateGamp(final IProject project, final Path output) throws IOException, CoreException {
		final var monitor = new NullProgressMonitor();
		final var plan = ProjectArchiveExporter.plan(project, plugins.files().keySet(), monitor);
		plan.warnings().forEach(PackageCommands::warning);
		if (!command.has(INCLUDE_PLUGINS) && plan.hasExtraPlugins()) {
			info("Required non-standard plugins are declared but not embedded; use " + INCLUDE_PLUGINS
					+ " to embed them");
		}
		ProjectArchiveExporter.export(plan, command.has(RESOLVE_LINKS), command.has(INCLUDE_PLUGINS), output,
				monitor);
		final var info = ProjectArchive.inspect(output);
		info("Project archive written to " + output + " (" + Files.size(output) + " bytes)");
		info("  " + info.description().replace("\n", "\n  "));
	}

	/**
	 * Selects one experiment with {@code name} or {@code name@Project/path/model.gaml}.
	 */
	private static ExportModels.Experiment select(final List<ExportModels.Experiment> experiments,
			final String selector, final String option) throws IOException {
		final List<ExportModels.Experiment> matching = experiments.stream().filter(experiment -> {
			final String model = experiment.modelPath().replaceFirst("^/", "");
			return selector.equals(experiment.name()) || selector.equals(experiment.name() + "@" + model)
					|| selector.equals(experiment.name() + "@/" + model);
		}).toList();
		if (matching.size() == 1) return matching.getFirst();
		final String available = experiments.stream()
				.map(experiment -> experiment.name() + "@" + experiment.modelPath().replaceFirst("^/", ""))
				.collect(Collectors.joining(", "));
		throw new IOException((matching.isEmpty() ? "No experiment matches " : "Several experiments match ") + option
				+ " " + selector + (matching.isEmpty() ? "" : "; use name@Project/path/model.gaml") + ". Available: "
				+ available);
	}

	// =====================================================================================================
	// Validation
	// =====================================================================================================

	/** Validates an archive, without modifying it or the installation. */
	private int validate() throws IOException {
		final Path archive = existingArchive();
		final Format format = format(archive);
		final Path temporary = temporary(Files.createTempDirectory("gama-package-validation-"));
		try {
			info("Validating " + archive);
			if (format == Format.GAMX) {
				validateGamx(archive, temporary);
			} else {
				validateGamp(archive, temporary);
			}
		} finally {
			delete(temporary);
		}
		warnings.forEach(PackageCommands::warning);
		errors.forEach(PackageCommands::error);
		if (!errors.isEmpty()) {
			info("INVALID: " + errors.size() + " problem(s) in " + archive.getFileName());
			return FAILED;
		}
		info("VALID: " + archive.getFileName() + (warnings.isEmpty() ? "" : " (" + warnings.size() + " warning(s))"));
		return OK;
	}

	/** Validates a playable model: layout, manifest, launcher settings, plugins, host and models. */
	private void validateGamx(final Path archive, final Path temporary) throws IOException {
		final SimulationPackageManifest manifest;
		try {
			manifest = SimulationPackage.inspect(archive);
		} catch (final IOException e) {
			errors.add("Invalid playable model: " + message(e) + oldPlayableHint(archive));
			return;
		}
		info("  format: GAMA playable model (.gamx), version " + SimulationPackageManifest.FORMAT_VERSION);
		info("  identifier " + manifest.id() + ", version " + manifest.version());
		info("  requires GAMA " + manifest.runtimeVersion() + " or newer (host: " + SystemInfo.VERSION_NUMBER + ")");
		try {
			manifest.requireRuntime(SystemInfo.VERSION_NUMBER);
		} catch (final IOException e) {
			errors.add(message(e));
		}
		final SimulationPackage.Extracted extracted;
		try {
			// Extraction checks the actual size of every payload against the declared limits
			extracted = SimulationPackage.extract(archive, temporary, manifest.runtimeVersion());
			final var context = SimulationLaunchContext.fromPackage(extracted);
			info("  title: " + context.configuration().title()
					+ (context.configuration().startupExperiment().isBlank() ? ""
							: ", default experiment: " + context.configuration().startupExperiment()));
		} catch (final IOException e) {
			errors.add("Invalid playable model content: " + message(e));
			return;
		}
		printBundles(manifest.bundles());
		installRequestedPlugins();
		installGamxPlugins(manifest, extracted.content());
		if (!resolvePlugins()) return;
		initializeGama.run();
		for (final var experiment : manifest.experiments()) {
			final Path model = extracted.workspace().resolve(experiment.model());
			final IModelSpecies species = compile(model, experiment.model(), errors);
			if (species == null) { continue; }
			try {
				final IExperimentSpecies found = species.getExperiment(experiment.name());
				if (found == null) {
					errors.add("Experiment " + experiment.name() + " is not defined in " + experiment.model());
				} else {
					info("  experiment " + experiment.selection() + ": compiles" + (found.isBatch() ? " (batch)" : ""));
				}
			} finally {
				species.dispose();
			}
		}
	}

	/** Validates a project archive: layout, manifest, plugins and models (which may legitimately not compile). */
	private void validateGamp(final Path archive, final Path temporary) throws IOException {
		final ProjectArchive.Info info;
		try {
			info = ProjectArchive.inspect(archive);
		} catch (final IOException e) {
			errors.add("Invalid project archive: " + message(e) + oldPlayableHint(archive));
			return;
		}
		info("  format: " + (info.legacy() ? "legacy project ZIP (no GAMA manifest)"
				: "GAMA editable project archive (.gamp), version 1"));
		info("  " + info.description().replace("\n", "\n  "));
		final ProjectArchive.Extracted extracted;
		try {
			extracted = ProjectArchive.extract(archive, temporary, () -> false);
		} catch (final IOException e) {
			errors.add("Invalid project archive content: " + message(e));
			return;
		}
		installRequestedPlugins();
		installGampPlugins(info, extracted.directory(), warnings);
		if (!resolvePlugins()) return;
		initializeGama.run();
		for (final var project : extracted.projects().entrySet()) {
			for (final Path model : modelFiles(project.getValue())) {
				final String name = project.getKey() + "/" + project.getValue().relativize(model).toString()
						.replace(File.separatorChar, '/');
				// An editable project may contain unfinished models: they are reported, not rejected
				final IModelSpecies species = compile(model, name, warnings);
				if (species != null) {
					info("  model " + name + ": compiles");
					species.dispose();
				}
			}
		}
	}

	// =====================================================================================================
	// Execution
	// =====================================================================================================

	/** Runs one experiment of an archive. */
	private int run() throws IOException {
		final Path archive = existingArchive();
		final Format format = format(archive);
		final boolean keep = command.has(OUTPUT);
		final Path output = keep ? outputDirectory(Path.of(command.value(OUTPUT)))
				: temporary(Files.createTempDirectory("gama-package-run-"));
		try {
			final Path content = output.resolve("package");
			final Path model;
			final String experiment;
			if (format == Format.GAMX) {
				final var extracted = SimulationPackage.extract(archive, output, SystemInfo.VERSION_NUMBER);
				Files.move(extracted.content(), content);
				final var context = SimulationLaunchContext.fromPackage(SimulationPackage.Extracted.open(content));
				final var selected = selectPackagedExperiment(extracted.manifest(), context.configuration());
				model = content.resolve("workspace").resolve(selected.model());
				experiment = selected.name();
				installRequestedPlugins();
				installGamxPlugins(extracted.manifest(), content);
			} else {
				final var info = ProjectArchive.inspect(archive);
				final var extracted = ProjectArchive.extract(archive, output, () -> false);
				Files.move(extracted.directory(), content);
				model = selectArchivedModel(content, extracted.projects().keySet());
				experiment = null;
				installRequestedPlugins();
				installGampPlugins(info, content, errors);
			}
			if (!resolvePlugins()) {
				errors.forEach(PackageCommands::error);
				return FAILED;
			}
			initializeGama.run();
			return runExperiment(model, experiment, output);
		} finally {
			if (keep) {
				info("Extracted archive and outputs kept in " + output);
			} else {
				delete(output);
			}
		}
	}

	/** Selects the experiment of a playable model from -model, -experiment and the default experiment. */
	private SimulationPackageManifest.Experiment selectPackagedExperiment(final SimulationPackageManifest manifest,
			final LauncherConfiguration configuration) throws IOException {
		List<SimulationPackageManifest.Experiment> candidates = manifest.experiments();
		if (command.has(MODEL)) {
			final String model = normalizedModel(command.value(MODEL));
			candidates = candidates.stream().filter(experiment -> experiment.model().equals(model)).toList();
		}
		if (command.has(EXPERIMENT)) {
			final String selector = command.value(EXPERIMENT);
			candidates = candidates.stream().filter(
					experiment -> experiment.name().equals(selector) || experiment.selection().equals(selector))
					.toList();
		}
		if (candidates.size() > 1 && !command.has(EXPERIMENT) && !configuration.startupExperiment().isBlank()) {
			candidates = candidates.stream()
					.filter(experiment -> experiment.selection().equals(configuration.startupExperiment())).toList();
		}
		if (candidates.size() == 1) return candidates.getFirst();
		final String available = manifest.experiments().stream().map(SimulationPackageManifest.Experiment::selection)
				.collect(Collectors.joining(", "));
		throw new IOException((candidates.isEmpty() ? "No experiment of the playable model matches the selection"
				: "The playable model has several experiments; choose one with " + EXPERIMENT
						+ " name or name@Project/path/model.gaml")
				+ ". Available: " + available);
	}

	/** Selects the model to run in an extracted project archive. */
	private Path selectArchivedModel(final Path content, final java.util.Set<String> projects) throws IOException {
		final Path workspace = content.resolve("workspace");
		if (command.has(MODEL)) {
			final String name = normalizedModel(command.value(MODEL));
			final Path model = workspace.resolve(name).normalize();
			if (!model.startsWith(workspace) || !Files.isRegularFile(model)
					|| !projects.contains(name.split("/", 2)[0]))
				throw new IOException("The project archive contains no model " + name);
			return model;
		}
		final List<Path> models = new ArrayList<>();
		for (final String project : projects) { models.addAll(modelFiles(workspace.resolve(project))); }
		if (models.size() == 1) return models.getFirst();
		throw new IOException((models.isEmpty() ? "The project archive contains no model"
				: "The project archive contains several models; choose one with " + MODEL + " Project/path/model.gaml")
				+ (models.isEmpty() ? "" : ". Available: " + models.stream()
						.map(path -> workspace.relativize(path).toString().replace(File.separatorChar, '/'))
						.collect(Collectors.joining(", "))));
	}

	/** Compiles a model and runs one of its experiments. */
	private int runExperiment(final Path model, final String name, final Path output) throws IOException {
		final String label = output.resolve("package").relativize(model).toString().replace(File.separatorChar, '/')
				.replaceFirst("^workspace/", "");
		final List<String> problems = new ArrayList<>();
		final IModelSpecies species = compile(model, label, problems);
		if (species == null) {
			problems.forEach(PackageCommands::error);
			return FAILED;
		}
		final String experimentName;
		boolean batch = false;
		final Integer threads = command.has(THREADS) ? (int) command.integer(THREADS) : null;
		try {
			final List<IExperimentSpecies> experiments = new ArrayList<>();
			species.getExperiments().forEach(experiments::add);
			final String requested = name != null ? name : command.value(EXPERIMENT);
			final IExperimentSpecies experiment;
			if (requested != null) {
				experiment = species.getExperiment(requested);
				if (experiment == null) throw new IOException("Experiment " + requested + " is not defined in "
						+ label + ". Available: " + names(experiments));
			} else if (experiments.size() == 1) {
				experiment = experiments.getFirst();
			} else
				throw new IOException(label + " defines " + (experiments.isEmpty() ? "no experiment"
						: "several experiments; choose one with " + EXPERIMENT + ". Available: " + names(experiments)));
			experimentName = experiment.getName();
			batch = experiment.isBatch();
			if (batch && command.has(UNTIL_CYCLE))
				throw new IOException(UNTIL_CYCLE + " cannot be used with the batch experiment " + experimentName
						+ ", which runs until its exploration is complete");
			if (batch) {
				info("Running batch experiment " + experimentName + " of " + label);
				if (threads != null) { GamaExecutorService.THREADS_NUMBER.set(threads); }
				final var firstError = new java.util.concurrent.atomic.AtomicReference<Throwable>();
				final var gui = new ErrorRecordingGui(e -> firstError.compareAndSet(null, e));
				GAMA.setHeadlessGui(gui);
				experiment.setHeadless(true);
				experiment.open();
				GAMA.getControllers().add(experiment.getController());
				experiment.getController().processStart(false);
				if (gui.errors() > 0) {
					error("Batch experiment " + experimentName + " completed with " + gui.errors()
							+ " runtime error(s); first: " + message(firstError.get()));
					return FAILED;
				}
				info("Batch experiment " + experimentName + " completed");
				return OK;
			}
		} finally {
			// A batch experiment uses its model until the end; the other experiments are recompiled by their job
			if (!batch) { species.dispose(); }
		}
		return runSimulation(model, label, experimentName, output, threads);
	}

	/** Runs a non-batch experiment until the requested cycle, or until it stops. */
	private int runSimulation(final Path model, final String label, final String experimentName, final Path output,
			final Integer threads) throws IOException {
		final long untilCycle = command.has(UNTIL_CYCLE) ? command.integer(UNTIL_CYCLE) : -1;
		Globals.OUTPUT_PATH = output.toString();
		Globals.IMAGES_PATH = Files.createDirectories(output.resolve("snapshot")).toString();
		final List<IExperimentJob> jobs;
		try {
			jobs = ExperimentationPlanFactory.buildExperiment(model.toString(), null, threads);
		} catch (final GamaCompilationFailedException e) {
			throw new IOException("Unable to compile " + label + ": " + message(e), e);
		}
		final ExperimentJob template = (ExperimentJob) jobs.stream()
				.filter(job -> job.getExperimentName().equals(experimentName)).findFirst()
				.orElseThrow(() -> new IOException("Unable to prepare experiment " + experimentName));
		final PackageExperimentJob job = new PackageExperimentJob(template, untilCycle);
		job.setBufferedWriter(new XMLWriter(
				output.resolve(Globals.OUTPUT_FILENAME + "-" + job.getExperimentID() + ".xml").toString()));
		info("Running experiment " + experimentName + " of " + label
				+ (untilCycle < 0 ? " until its until: condition is true (or forever; interrupt with Ctrl+C)"
						: " until cycle " + untilCycle));
		// Errors happen on simulation threads and are only reported to the GUI: record them to stop and fail the run
		GAMA.setHeadlessGui(new ErrorRecordingGui(job::fail));
		final SimulationRuntime runtime = new SimulationRuntime();
		runtime.setNumberOfThreads(threads == null ? 1 : threads);
		stopRunning = () -> {
			job.fail(new IOException("The run was interrupted"));
			try {
				runtime.awaitTermination(3, TimeUnit.SECONDS);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		};
		runtime.execute(job);
		runtime.shutdown();
		try {
			while (!runtime.awaitTermination(100, TimeUnit.MILLISECONDS)) {}
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while running " + experimentName, e);
		}
		stopRunning = () -> {};
		if (job.getFailure() != null) {
			error("Experiment " + experimentName + " failed after " + job.getExecutedSteps() + " step(s): "
					+ message(job.getFailure()));
			return FAILED;
		}
		info("Experiment " + experimentName + " stopped at cycle " + job.getCycle() + " after "
				+ job.getExecutedSteps() + " step(s), because " + job.getStopReason());
		return OK;
	}

	// =====================================================================================================
	// Plugins
	// =====================================================================================================

	/** Installs the plugins given with -plugin. */
	private void installRequestedPlugins() throws IOException {
		for (final String jar : command.all(PLUGIN)) {
			final var installed = plugins.install(Path.of(jar).toAbsolutePath().normalize(), null);
			info("  installed plugin " + installed.getFileName() + " for this command");
		}
	}

	/**
	 * Installs the plugins embedded in a playable model, with the rules of the GUI player: embedded plugins cannot
	 * replace installed ones, and host plugins must be present at the required version or newer.
	 */
	private void installGamxPlugins(final SimulationPackageManifest manifest, final Path content) throws IOException {
		for (final BundleRequirement required : manifest.bundles()) {
			final var installed = Platform.getBundle(required.symbolicName());
			if (installed != null && !plugins.files().containsKey(required.symbolicName())) {
				if (!required.artifact().isEmpty()) {
					errors.add("The playable model embeds " + required.symbolicName()
							+ ", which is already installed: an installed plugin cannot be replaced");
				} else if (!required.accepts(installed.getVersion().toString())) {
					errors.add("Plugin " + required.symbolicName() + " " + required.version()
							+ " or newer is required; this GAMA provides " + installed.getVersion());
				}
			} else if (installed == null) {
				if (required.artifact().isEmpty()) {
					errors.add("Plugin " + required.symbolicName() + " " + required.version()
							+ " is required but neither installed nor embedded (use -plugin to supply it)");
				} else {
					try {
						plugins.install(content.resolve(required.artifact()), required);
						info("  installed embedded plugin " + required.symbolicName() + " " + required.version());
					} catch (final IOException e) {
						errors.add(message(e));
					}
				}
			}
		}
	}

	/**
	 * Installs the plugins embedded in a project archive that this GAMA misses. Plugins neither installed nor embedded
	 * are reported in {@code problems}.
	 */
	private void installGampPlugins(final ProjectArchive.Info info, final Path content, final List<String> problems) {
		for (final BundleRequirement required : info.plugins()) {
			final var installed = Platform.getBundle(required.symbolicName());
			if (installed != null) {
				if (new org.osgi.framework.Version(required.version()).compareTo(installed.getVersion()) > 0) {
					problems.add("Plugin " + required.symbolicName() + " " + required.version()
							+ " or newer is required; this GAMA provides " + installed.getVersion());
				}
			} else if (required.artifact().isEmpty()) {
				problems.add("Plugin " + required.symbolicName() + " " + required.version()
						+ " is required but neither installed nor included (use -plugin to supply it)");
			} else {
				try {
					plugins.install(content.resolve(required.artifact()), required);
					info("  installed included plugin " + required.symbolicName() + " " + required.version());
				} catch (final IOException e) {
					errors.add(message(e));
				}
			}
		}
	}

	/** Resolves the installed plugins; false (with an error) if they cannot be resolved or errors were found. */
	private boolean resolvePlugins() {
		if (!errors.isEmpty()) return false;
		try {
			plugins.resolve();
			return true;
		} catch (final IOException e) {
			errors.add(message(e));
			return false;
		}
	}

	// =====================================================================================================
	// Utilities
	// =====================================================================================================

	/** Compiles a model file; returns null and adds its errors to {@code problems} if it does not compile. */
	private static IModelSpecies compile(final Path model, final String label, final List<? super String> problems) {
		final List<GamlCompilationError> found = new ArrayList<>();
		try {
			final IModelSpecies species = GamlModelBuilder.getInstance().compile(model.toFile(), found, null);
			if (species != null) return species;
		} catch (final IOException | GamaCompilationFailedException e) {
			// The errors are in the list
		}
		final List<String> messages = found.stream().filter(GamlCompilationError::isError)
				.map(error -> label + (error.location() == null || error.location().line() <= 0 ? ""
						: ":" + error.location().line()) + ": " + error.message())
				.toList();
		problems.add("Model " + label + " does not compile" + (messages.isEmpty() ? "" : ":\n    "
				+ String.join("\n    ", messages)));
		return null;
	}

	/** The GAML model files of a directory tree, sorted. */
	private static List<Path> modelFiles(final Path directory) throws IOException {
		try (var paths = Files.walk(directory)) {
			return paths.filter(Files::isRegularFile).filter(path -> {
				final String name = path.getFileName().toString();
				return GamlFileExtension.isGaml(name) || GamlFileExtension.isExperiment(name);
			}).sorted().toList();
		}
	}

	/** The archive positional argument, which must exist. */
	private Path existingArchive() throws IOException {
		final Path archive = Path.of(command.positional().getFirst()).toAbsolutePath().normalize();
		if (!Files.isRegularFile(archive)) throw new IOException("The archive does not exist: " + archive);
		return archive;
	}

	/** The format of an archive, from its extension or, failing that, from its manifest. */
	static Format format(final Path archive) throws IOException {
		final var format = Format.of(archive.getFileName().toString());
		if (format.isPresent()) return format.get();
		try (var zip = new ZipFile(archive.toFile())) {
			if (zip.getEntry(SimulationPackageManifest.FILE) != null) return Format.GAMX;
			if (zip.getEntry(ProjectArchive.MANIFEST) != null) return Format.GAMP;
		} catch (final IOException e) {
			throw new IOException("Not a GAMA archive (not a ZIP file): " + archive, e);
		}
		throw new IOException("Not a GAMA archive (no .gamx or .gamp manifest): " + archive);
	}

	/** A hint for playable models still named .gamp, as written by older GAMA versions. */
	private static String oldPlayableHint(final Path archive) {
		if (!ProjectArchive.isProjectArchive(archive)) return "";
		try (var zip = new ZipFile(archive.toFile())) {
			if (zip.getEntry(SimulationPackageManifest.FILE) != null && zip.getEntry(ProjectArchive.MANIFEST) == null)
				return "\nThis looks like a playable model written by an older GAMA: rename it to .gamx";
		} catch (final IOException e) {
			// Already reported
		}
		return "";
	}

	/** Creates the output directory, which must be new or empty. */
	private static Path outputDirectory(final Path directory) throws IOException {
		final Path output = directory.toAbsolutePath().normalize();
		if (Files.exists(output)) {
			if (!Files.isDirectory(output)) throw new IOException("The output is not a directory: " + output);
			try (var entries = Files.list(output)) {
				if (entries.findAny().isPresent())
					throw new IOException("The output directory must be new or empty: " + output);
			}
		}
		return Files.createDirectories(output);
	}

	/** Normalizes a model path given on the command line to Project/path/model.gaml. */
	private static String normalizedModel(final String model) {
		return model.replace('\\', '/').replaceFirst("^/", "").replaceFirst("^workspace/", "");
	}

	/** The names of experiments. */
	private static String names(final List<IExperimentSpecies> experiments) {
		return experiments.stream().map(IExperimentSpecies::getName).collect(Collectors.joining(", "));
	}

	/** Prints the plugin requirements of an archive. */
	private static void printBundles(final List<BundleRequirement> bundles) {
		final var embedded = bundles.stream().filter(bundle -> !bundle.artifact().isEmpty()).toList();
		info("  " + bundles.size() + " required plugin(s), " + embedded.size() + " embedded"
				+ (embedded.isEmpty() ? "" : ": " + embedded.stream()
						.map(bundle -> bundle.symbolicName() + " " + bundle.version())
						.collect(Collectors.joining(", "))));
	}

	/** Registers a temporary directory, deleted by {@link #delete(Path)} or when the JVM stops. */
	static Path temporary(final Path directory) {
		TEMPORARY.add(directory);
		return directory;
	}

	/**
	 * Deletes a directory tree created by this command. Synchronized and unregistered only once done, so that the
	 * shutdown hook waits for a deletion in progress instead of letting the JVM stop in the middle of it.
	 */
	static synchronized void delete(final Path directory) {
		if (Files.exists(directory)) {
			try (var paths = Files.walk(directory)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.deleteIfExists(path); }
			} catch (final IOException e) {
				warning("Unable to delete " + directory + ": " + message(e));
			}
		}
		TEMPORARY.remove(directory);
	}

	/** The message of an exception, or its class name. */
	private static String message(final Throwable e) {
		return e.getMessage() == null || e.getMessage().isBlank() ? e.getClass().getSimpleName() : e.getMessage();
	}

	/** Prints a progress or result line. Uses the standard streams: GAMA logs may be disabled in releases. */
	private static void info(final String message) {
		System.out.println(message);
	}

	/** Prints a warning. */
	private static void warning(final String message) {
		System.out.println("WARNING: " + message);
	}

	/** Prints an error. */
	private static void error(final String message) {
		System.err.println("ERROR: " + message);
	}
}

/*******************************************************************************************************
 *
 * WorkspaceModelsManager.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.manager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.osgi.framework.Bundle;

import com.google.common.base.Strings;

import gama.api.GAMA;
import gama.api.additions.GamaBundleLoader;
import gama.api.constants.GamlFileExtension;
import gama.api.utils.files.IGamlFileInfo;
import gama.dev.DEBUG;
import gama.dev.THREADS;
import gama.workspace.nature.GamaNatures;

/**
 * The WorkspaceModelsManager is responsible for managing GAMA model projects within the Eclipse workspace. It handles
 * project creation, model file loading, library linking, and workspace synchronization.
 *
 * This class provides functionality to: - Open and import model files from various sources - Create and manage
 * workspace projects with appropriate natures - Link built-in model libraries to the workspace - Handle project
 * discovery and creation from file system
 *
 * Performance considerations: - Uses concurrent operations for project discovery - Caches project information to avoid
 * repeated file system access - Implements async project loading for better responsiveness
 *
 * @author drogoul
 * @since 16 nov. 2013
 * @version 2025-03
 */
public class WorkspaceModelsManager {

	static {
		DEBUG.OFF();
	}

	/** The Constant BUILTIN_PROPERTY. */
	public static final QualifiedName BUILTIN_PROPERTY = new QualifiedName("gama.builtin", "models");

	/** Project name for unclassified models */
	public static final String UNCLASSIFIED_MODELS = "Unclassified Models";

	/** Filename filter for .project files */
	private static final FilenameFilter DOT_PROJECT_FILTER = (dir, name) -> ".project".equals(name);

	/** Pattern for filename matching in createUniqueFileFrom method */
	private static final Pattern FILENAME_PATTERN = Pattern.compile("(.*?)(\\d+)?(\\..*)?");

	/** The singleton instance */
	public final static WorkspaceModelsManager instance = new WorkspaceModelsManager();

	/** Cache for project existence checks to avoid repeated file system access */
	private final Map<String, Boolean> projectExistenceCache = new ConcurrentHashMap<>();

	/** Cache for project descriptions to improve performance */
	private final Map<File, Optional<IProjectDescription>> projectDescriptionCache = new ConcurrentHashMap<>();

	/**
	 * Opens a model passed as a command-line argument. This method handles both direct model files and experiment
	 * definitions specified with '#' notation.
	 *
	 * @param modelPath
	 *            the path to the model file, optionally followed by '#experimentName' for direct experiment execution
	 *
	 *            Examples: - "/path/to/model.gaml" - opens the model in editor, unless it has one experiment, in which
	 *            case it runs that experiment - "/path/to/model.gaml#myExperiment" - runs the specified experiment -
	 *            "/path/to/model.experiment" - runs experiment "0" by default
	 */
	public void openModelPassedAsArgument(final String modelPath) {
		String filePath = modelPath;
		String expName = null;
		if (filePath.contains("#")) {
			final String[] segments = filePath.split("#");
			if (segments.length != 2) {
				DEBUG.OUT("Wrong definition of model and experiment in argument '" + filePath + "'");
				return;
			}
			filePath = segments[0];
			expName = segments[1];
		}
		if (filePath.endsWith(".experiment") && expName == null) {
			expName = "0";
			// Verify that it works even if the included model defines experiments itself...

		}
		final IFile file = findAndLoadIFile(filePath);
		if (file != null) {
			String en = expName;
			// final Runnable run = () -> {
			try {
				// DEBUG.OUT(Thread.currentThread().getName() + ": Rebuilding the model " + fp);
				// Force the project to rebuild itself in order to load the various XText plugins.
				file.touch(null);
				file.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			} catch (final CoreException e1) {
				DEBUG.OUT(Thread.currentThread().getName() + ": File " + file.getFullPath() + " cannot be built");
				return;
			}
			if (en == null && GamlFileExtension.isGaml(filePath)) {
				final var metadata = GAMA.getMetadataProvider().getMetaData(file, true, true);
				if (metadata instanceof IGamlFileInfo info && info.getExperiments().size() == 1) {
					final String onlyExperiment = info.getExperiments().iterator().next();
					en = onlyExperiment.startsWith("***") ? onlyExperiment.substring(3) : onlyExperiment;
				}
			}
			while (GAMA.getRegularGui() == null) {
				THREADS.WAIT(100, Thread.currentThread().getName() + ": waiting for the GUI to become available");
			}
			if (en == null) {
				// System.out
				// .println(Thread.currentThread().getName() + ": Opening the model " + fp + " in the editor");
				GAMA.getGui().getModelsManager().editModel(file);
			} else {
				// DEBUG.OUT(Thread.currentThread().getName() + ": Trying to run experiment " + en);
				GAMA.getGui().getModelsManager().runModel(file, en);
			}

			// };
			// new Thread(run, "Automatic opening of " + filePath).start();

		}
	}

	/**
	 * Finds and loads an IFile from the given file path. This method first checks if the file exists in the workspace,
	 * then checks outside.
	 *
	 * @param filePath
	 *            the path to the file to load
	 * @return the IFile if found, null otherwise
	 */
	private IFile findAndLoadIFile(final String filePath) {
		// No error in case of an empty argument
		if (Strings.isNullOrEmpty(filePath)) {
			DEBUG.OUT("Empty file path provided to findAndLoadIFile");
			return null;
		}
		final IPath path = new Path(filePath);

		// 1st case: the path can be identified as a file residing in the workspace
		IFile result = findInWorkspace(path);
		if (result != null) {
			DEBUG.OUT("Found file in workspace: " + path);
			return result;
		}

		// Otherwise it belongs outside
		DEBUG.OUT("Searching file outside workspace: " + path);
		return findOutsideWorkspace(path);
	}

	/**
	 * Searches for a file within the workspace using relative path.
	 *
	 * @param originalPath
	 *            the original file path to search for
	 * @return the IFile if found in workspace, null otherwise
	 */
	private IFile findInWorkspace(final IPath originalPath) {
		final IPath filePath = originalPath.makeRelativeTo(GAMA.getWorkspaceManager().getWorkspacePath());
		IFile file = null;
		try {
			file = GAMA.getWorkspaceManager().getRoot().getFile(filePath);
		} catch (final Exception e) {
			return null;
		}
		if (!file.exists()) return null;
		return file;
	}

	/**
	 * Find outside workspace.
	 *
	 * @param originalPath
	 *            the original path
	 * @return the i file
	 */
	private IFile findOutsideWorkspace(final IPath originalPath) {
		final File modelFile = new File(originalPath.toOSString());
		// TODO If the file does not exist we return null (might be a good idea to check other locations)
		if (!modelFile.exists()) return null;

		// We try to find a folder containing the model file which can be considered as a project
		File projectFileBean = new File(modelFile.getPath());
		File dotFile = null;
		while (projectFileBean != null && dotFile == null) {
			projectFileBean = projectFileBean.getParentFile();
			if (projectFileBean != null) {
				/* parcours des fils pour trouver le dot file et creer le lien vers le projet */
				final File[] children = projectFileBean.listFiles();
				if (children != null) {
					for (final File element : children) {
						if (".project".equals(element.getName())) {
							dotFile = element;
							break;
						}
					}
				}
			}
		}

		if (dotFile == null || projectFileBean == null) {
			GAMA.getGui().getDialogFactory().inform("The model '" + modelFile.getAbsolutePath()
					+ "' does not seem to belong to an existing GAML project. You can import it in an existing project or in the 'Unclassified models' project.");
			return createUnclassifiedModelsProjectAndAdd(originalPath);
		}

		final IPath location = new Path(dotFile.getAbsolutePath());
		final String pathToProject = projectFileBean.getName();

		try {
			// We load the project description.
			final IProjectDescription description =
					GAMA.getWorkspaceManager().getWorkspace().loadProjectDescription(location);
			if (description != null) {
				final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

					@Override
					protected void execute(final IProgressMonitor monitor)
							throws CoreException, InvocationTargetException, InterruptedException {
						// We try to get the project in the workspace
						IProject proj = GAMA.getWorkspaceManager().getRoot().getProject(pathToProject);
						// If it does not exist, we create it
						if (!proj.exists()) {
							// If a project with the same name exists
							final IProject[] projects = GAMA.getWorkspaceManager().getRoot().getProjects();
							final String name = description.getName();
							for (final IProject p : projects) {
								if (p.getName().equals(name)) {
									GAMA.getGui().getDialogFactory().inform(
											"A project with the same name already exists in the workspace. The model '"
													+ modelFile.getAbsolutePath()
													+ " will be imported as part of the 'Unclassified models' project.");
									createUnclassifiedModelsProjectAndAdd(originalPath);
									return;
								}
							}

							proj.create(description, monitor);
						} else // project exists but is not accessible, so we delete it and recreate it
						if (!proj.isAccessible()) {
							proj.delete(true, null);
							proj = GAMA.getWorkspaceManager().getRoot().getProject(pathToProject);
							proj.create(description, monitor);
						}
						// We open the project
						proj.open(IResource.NONE, monitor);
						// And we set some properties to it
						setValuesProjectDescription(proj, null);
					}
				};
				operation.run(new NullProgressMonitor() {});
			}
		} catch (final InterruptedException | InvocationTargetException e) {
			return null;
		} catch (final CoreException e) {
			GAMA.getGui().getDialogFactory().warning("Error wien importing project: " + e.getMessage());
		}
		final IProject project = GAMA.getWorkspaceManager().getRoot().getProject(pathToProject);
		final String relativePathToModel =
				project.getName() + modelFile.getAbsolutePath().replace(projectFileBean.getPath(), "");
		return findInWorkspace(new Path(relativePathToModel));
	}

	/**
	 * Creates the unclassified models project.
	 *
	 * @param location
	 *            the location
	 * @return the i folder
	 * @throws CoreException
	 *             the core exception
	 */
	public IFolder createUnclassifiedModelsProject(final IPath location) throws CoreException {
		final IPath result = GAMA.getGui().getDialogFactory().openContainerSelectionDialog("Project selection",
				"Select a parent project or folder to import the model into:");
		IFolder modelFolder = null;
		if (result == null) {
			IProject project = createOrUpdateProject(UNCLASSIFIED_MODELS);
			modelFolder = project.getFolder(new Path("models"));
			if (!modelFolder.exists()) { modelFolder.create(true, true, null); }
		} else {
			IContainer resultContainer = (IContainer) GAMA.getWorkspaceManager().getRoot().findMember(result);
			if (resultContainer instanceof IProject project) {
				modelFolder = project.getFolder(new Path("models"));
				if (!modelFolder.exists()) { modelFolder.create(true, true, null); }
			} else if (resultContainer instanceof IFolder folder) { modelFolder = folder; }
		}
		return modelFolder;
	}

	/**
	 * Creates an unclassified models project and adds the given file as a copy or linked resource. Handles file
	 * conflicts by creating unique names when necessary.
	 *
	 * @param location
	 *            the location of the file to add
	 * @return the IFile representing the imported resource, or null if creation failed
	 */
	IFile createUnclassifiedModelsProjectAndAdd(final IPath location) {
		if (location == null) {
			DEBUG.ERR("Cannot add null location to unclassified models project");
			return null;
		}

		IFile targetFile = null;
		try {
			final boolean createLink = !GAMA.getGui().getDialogFactory().question("Import model",
					"Copy '" + location.lastSegment()
							+ "' into the project? Choose Yes to copy it, or No to keep it at its current location as a link.");
			final IFolder modelFolder = createUnclassifiedModelsProject(location);
			if (modelFolder == null) {
				DEBUG.ERR("Failed to create or get models folder");
				return null;
			}

			targetFile = modelFolder.getFile(location.lastSegment());

			// Handle existing files
			if (targetFile.exists()) {
				if (targetFile.isLinked()) {
					final IPath existingPath = targetFile.getLocation();
					if (location.equals(existingPath) && createLink) {
						// File already linked to the same location
						DEBUG.OUT("File already linked to same location: " + location);
						return targetFile;
					}
				}
				// Create unique filename to avoid conflicts
				targetFile = createUniqueFileFrom(targetFile, modelFolder);
			}

			if (createLink) {
				targetFile.createLink(location, IResource.NONE, null);
				DEBUG.OUT("Successfully created linked resource: " + targetFile.getFullPath());
			} else {
				try (InputStream input = Files.newInputStream(location.toFile().toPath())) {
					targetFile.create(input, IResource.NONE, null);
				}
				DEBUG.OUT("Successfully copied file into workspace: " + targetFile.getFullPath());
			}

			return targetFile;

		} catch (final CoreException | IOException e) {
			final String fileName =
					targetFile != null ? targetFile.getFullPath().lastSegment() : location.lastSegment();
			final String message = "Failed to create file " + fileName + ": " + e.getMessage();

			DEBUG.ERR(message, e);
			GAMA.getGui().getDialogFactory().inform("The file " + fileName
					+ " cannot be created because of the following exception: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Creates a unique file name by appending a number if the original file already exists. Uses the FILENAME_PATTERN
	 * to intelligently increment existing numeric suffixes.
	 *
	 * @param originalFile
	 *            the original file that may conflict
	 * @param modelFolder
	 *            the target folder for the unique file
	 * @return a unique IFile that doesn't conflict with existing files
	 */
	private IFile createUniqueFileFrom(final IFile originalFile, final IFolder modelFolder) {
		IFile file = originalFile;
		while (file.exists()) {
			final IPath path = file.getLocation();
			String fileName = path.lastSegment();
			final Matcher matcher = FILENAME_PATTERN.matcher(fileName);
			if (matcher.matches()) {
				// Group 1: prefix, Group 2: number (optional), Group 3: suffix (optional)
				final String prefix = matcher.group(1);
				final String numberPart = matcher.group(2);
				final String suffix = matcher.group(3);

				final int nextNumber = numberPart == null ? 1 : Integer.parseInt(numberPart) + 1;
				fileName = prefix + nextNumber + (suffix == null ? "" : suffix);
			}
			file = modelFolder.getFile(fileName);
		}
		return file;
	}

	/**
	 * Link sample models to workspace.
	 */
	public void linkSampleModelsToWorkspace() {

		final WorkspaceJob job = new WorkspaceJob("Updating the Built-in Models Library") {

			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				final LibraryUpdateReport report = loadModelsLibrary(monitor);
				GAMA.getGui().refreshNavigatorContents();
				report.save();
				return report.getStatus();
			}

		};
		job.setUser(true);
		job.setProperty(new org.eclipse.core.runtime.QualifiedName("org.eclipse.ui.workbench.progress",
				"delayErrorPrompt"), Boolean.TRUE);
		job.schedule();

	}

	/** The identifiers of the natures that were used to classify the projects before they were classified by location. */
	private static final String[] LEGACY_NATURES = { "gama.workspace.builtinNature", "gama.workspace.pluginNature",
			"gama.workspace.testNature", "gama.workspace.tutorialNature", "gama.workspace.recipeNature",
			"gama.workspace.dataNature", "gama.workspace.demoNature", "msi.gama.application.builtinNature",
			"msi.gama.application.pluginNature", "msi.gama.application.testNature",
			"msi.gama.application.tutorialNature", "msi.gama.application.recipeNature" };

	/**
	 * Load models library.
	 */
	public void loadModelsLibrary() {
		final LibraryUpdateReport report = loadModelsLibrary(new NullProgressMonitor());
		report.save();
	}

	/**
	 * Reconciles library links using workspace-owned provenance. The running installation is authoritative:
	 * old library links are replaced without prompting or creating user copies. Original files are not deleted.
	 */
	public LibraryUpdateReport loadModelsLibrary(final IProgressMonitor monitor) {
		final LibraryUpdateReport report = new LibraryUpdateReport();
		clearCaches();
		ProjectCategory.reset();
		final List<GamaBundleLoader.ModelFolder> folders = new ArrayList<>(GamaBundleLoader.getModelFolders());
		if (folders.isEmpty() || GamaBundleLoader.CORE_MODELS == null) {
			report.error("The running installation's model library could not be discovered. "
					+ "Check that the gama.library bundle is installed and enabled. Existing projects were left untouched.",
					null);
			return report;
		}
		removeLegacyNatures(report);
		// The core library has the priority when several plugins contribute projects with the same name
		folders.sort((f1, f2) -> Boolean.compare(!f1.bundle().equals(GamaBundleLoader.CORE_MODELS),
				!f2.bundle().equals(GamaBundleLoader.CORE_MODELS)));
		final Map<String, Bundle> claimedNames = new HashMap<>();
		final java.util.Set<String> importedNames = new java.util.HashSet<>();
		for (final GamaBundleLoader.ModelFolder folder : folders) {
			if (monitor.isCanceled()) throw new OperationCanceledException();
			linkModelsToWorkspace(folder, claimedNames, importedNames, report, monitor);
		}
		if (importedNames.isEmpty()) {
			report.error("Model folders were discovered, but no library projects could be linked. "
					+ "The workspace was not stamped as updated. Run Clean and update workspace to retry.", null);
			return report;
		}
		removeObsoleteProjects(importedNames, report);
		if (!report.needsAttention()) { stampWorkspaceFromModels(); }
		// If the directory is not empty, we should maybe try to recreate the projects (if they do not exist...)
		final java.nio.file.Path workspace = java.nio.file.Path.of(GAMA.getWorkspaceManager().getWorkspaceLocation());
		recreateProjects(workspace, false, 0, report);
		// The test projects of the user are in a dedicated sub-directory of the workspace
		final java.nio.file.Path tests = workspace.resolve(ProjectCategory.USER_TESTS_FOLDER);
		if (Files.isDirectory(tests)) { recreateProjects(tests, true, 0, report); }
		return report;
	}

	/**
	 * Recreates the projects found in a directory. Directories that are not projects are considered as user groups
	 * (see issue #678) and are explored, up to {@link #MAX_GROUP_DEPTH} levels, only in the workspace itself. The
	 * projects found in groups are always created with an explicit location.
	 */
	private void recreateProjects(final java.nio.file.Path directory, final boolean explicitLocation,
			final int depth, final LibraryUpdateReport report) {
		try (DirectoryStream<java.nio.file.Path> paths = Files.newDirectoryStream(directory, Files::isDirectory)) {
			for (final java.nio.file.Path r : paths) {
				final File folder = r.toFile();
				final String name = folder.getName();
				if (isGamaProject(folder)) {
					if (createOrUpdateProject(name, explicitLocation || depth > 0 ? folder : null) == null) {
						report.error("Could not restore workspace project " + folder, null);
					}
				} else if (!explicitLocation && depth < MAX_GROUP_DEPTH && !name.startsWith(".")
						&& !ProjectCategory.isReservedName(name)) {
					recreateProjects(r, false, depth + 1, report);
				}
			}
		} catch (final IOException e1) {
			report.error("Could not scan workspace directory " + directory, e1);
		}
	}

	/** The maximum number of nested group folders in the workspace. */
	private static final int MAX_GROUP_DEPTH = 3;

	/**
	 * Removes the natures that were formerly used to classify the projects (they are not defined anymore) and replaces
	 * the former identifiers of the GAMA nature by the current one. The
	 * projects that were contributed by a plugin in a previous layout, and whose location does not exist anymore, are
	 * removed from the workspace (without deleting their contents). User projects are never removed.
	 */
	private void removeLegacyNatures(final LibraryUpdateReport report) {
		for (final IProject p : GAMA.getWorkspaceManager().getRoot().getProjects()) {
			try {
				if (!p.isOpen()) { continue; }
				final IProjectDescription desc = p.getDescription();
				final List<String> natures = new ArrayList<>(List.of(desc.getNatureIds()));
				// Only the old built-in/plugin natures prove ownership; test natures were also used by user projects.
				if (natures.contains("gama.workspace.builtinNature") || natures.contains("gama.workspace.pluginNature")
						|| natures.contains("msi.gama.application.builtinNature")
						|| natures.contains("msi.gama.application.pluginNature")) {
					if (p.isOpen() && !LibraryProjectSupport.isBuiltIn(p)) {
						p.setPersistentProperty(BUILTIN_PROPERTY, "legacy");
					}
				}
				final boolean removed = natures.removeAll(List.of(LEGACY_NATURES));
				final String[] migrated = GamaNatures.migrate(natures.toArray(new String[0]));
				final boolean renamed = !List.of(migrated).equals(natures);
				if (!removed && !renamed) { continue; }
				natures.clear();
				natures.addAll(List.of(migrated));
				final IPath loc = p.getLocation();
				if (loc != null && !loc.toFile().exists() && LibraryProjectSupport.isBuiltIn(p)) {
					p.delete(false, true, null);
				} else if (p.isOpen()) {
					desc.setNatureIds(natures.toArray(new String[0]));
					p.setDescription(desc, IResource.FORCE, null);
				}
			} catch (final CoreException e) {
				report.error("Could not update the natures of project " + p.getName(), e);
			}
		}
	}

	/**
	 * Removes from the workspace (without deleting their contents) the projects that were contributed by a plugin and
	 * whose location does not exist anymore. User projects are never removed.
	 *
	 * @param importedNames
	 *            the names of the projects that have just been imported
	 */
	private void removeObsoleteProjects(final java.util.Set<String> importedNames, final LibraryUpdateReport report) {
		for (final IProject p : GAMA.getWorkspaceManager().getRoot().getProjects()) {
			if (importedNames.contains(p.getName())) { continue; }
			try {
				final IPath loc = p.getLocation();
				if (loc == null) { continue; }
				if (loc.toFile().exists()) {
					if (LibraryProjectSupport.isBuiltIn(p)) {
						report.warning("Library project '" + p.getName() + "' is not available from this installation. "
								+ "Its existing reference and files were kept at " + loc + ".");
					}
					continue;
				}
				if (LibraryProjectSupport.isBuiltIn(p)) {
					p.delete(false, true, null);
					report.changed("Removed missing library reference: " + p.getName() + " (" + loc + ").");
				}
			} catch (final CoreException e) {
				report.error("Could not check project " + p.getName(), e);
			}
		}
	}

	/**
	 * Links the projects found in a folder contributed by a plugin to the workspace.
	 *
	 * @param folder
	 *            the folder contributed by a plugin, with its category
	 * @param claimedNames
	 *            the names of the projects already imported, with the plugin that contributed them
	 * @param importedNames
	 *            the names of all the projects imported so far
	 */
	private void linkModelsToWorkspace(final GamaBundleLoader.ModelFolder folder, final Map<String, Bundle> claimedNames,
			final java.util.Set<String> importedNames, final LibraryUpdateReport report, final IProgressMonitor monitor) {
		final Bundle bundle = folder.bundle();
		final URL fileURL = bundle.getEntry(folder.path());
		if (fileURL == null) {
			report.error("Missing model folder in " + bundle.getSymbolicName() + ": " + folder.path(), null);
			return;
		}
		File modelsRep = null;
		try {
			final URL resolvedFileURL = FileLocator.toFileURL(fileURL);
			// We need to use the 3-arg constructor of URI in order to properly escape file system chars
			final URI resolvedURI = new URI(resolvedFileURL.getProtocol(), resolvedFileURL.getPath(), null).normalize();
			modelsRep = new File(resolvedURI);

		} catch (final URISyntaxException | IOException e1) {
			report.error("Could not resolve models from " + bundle.getSymbolicName() + ": " + folder.path(), e1);
			return;
		}

		final Map<File, IPath> foundProjects = new ConcurrentHashMap<>();
		try {
			findProjects(modelsRep, foundProjects);
		} catch (final IOException e) {
			report.error("Could not discover all model projects in " + modelsRep, e);
		}
		importBuiltInProjects(folder, modelsRep, foundProjects, claimedNames, importedNames, report, monitor);
	}

	/**
	 * Recursively finds all GAMA project directories by looking for .project files.
	 *
	 * @param folder
	 *            the root folder to search from
	 * @param found
	 *            the map to store found projects (folder -> .project file path)
	 */
	private void findProjects(final File folder, final Map<File, IPath> found) throws IOException {
		if (folder == null || !folder.isDirectory()) throw new IOException("Missing model directory: " + folder);
		if (Files.isSymbolicLink(folder.toPath())) throw new IOException("Cannot scan symbolic model directory: " + folder);

		// Check for .project file in current directory
		final File[] dotFiles = folder.listFiles(DOT_PROJECT_FILTER);
		if (dotFiles == null) throw new IOException("Cannot read model directory: " + folder);
		if (dotFiles.length > 0) {
			found.put(folder, new Path(dotFiles[0].getAbsolutePath()));
			return; // Found project, no need to search subdirectories
		}

		// Search subdirectories
		final File[] subdirectories = folder.listFiles(File::isDirectory);
		if (subdirectories == null) throw new IOException("Cannot read model directory: " + folder);
		for (final File subdir : subdirectories) { findProjects(subdir, found); }
	}

	/**
	 * @param plugin
	 * @param core
	 * @param workspace
	 * @param project
	 */
	private void importBuiltInProjects(final GamaBundleLoader.ModelFolder folder, final File modelsRoot,
			final Map<File, IPath> projects, final Map<String, Bundle> claimedNames,
			final java.util.Set<String> importedNames, final LibraryUpdateReport report, final IProgressMonitor monitor) {
		final Bundle plugin = folder.bundle();
		final Map<String, IProject> bySource;
		try {
			bySource = indexLibrarySources();
		} catch (final CoreException e) {
			report.error("Could not read workspace library provenance for " + folder.path(), e);
			return;
		}
		for (final Map.Entry<File, IPath> entry : projects.entrySet().stream()
				.sorted(Map.Entry.comparingByKey()).toList()) {
			if (monitor.isCanceled()) throw new OperationCanceledException();
			final File project = entry.getKey();
			final IPath location = entry.getValue();

			final IProjectDescription description;
			try {
				description = GAMA.getWorkspaceManager().getWorkspace().loadProjectDescription(location);
			} catch (final CoreException e) {
				report.error("Could not load the project description of: " + project.getAbsolutePath(), e);
				continue;
			}
			// Skip projects whose .project file declares no natures: they are
			// deprecated container directories that have been emptied of GAMA content
			if (description.getNatureIds().length == 0) {
				DEBUG.OUT("Skipping empty/deprecated container project: " + project.getName());
				continue;
			}

			// Two projects cannot have the same name in the workspace: the latest one is renamed
			String name = project.getName();
			if (claimedNames.putIfAbsent(name, plugin) != null) {
				name = name + " (" + lastSegment(plugin) + ")";
				for (int i = 2; claimedNames.putIfAbsent(name, plugin) != null; i++) {
					name = project.getName() + " (" + lastSegment(plugin) + " " + i + ")";
				}
			}
			final String source = plugin.getSymbolicName() + ":" + folder.path() + "/"
					+ modelsRoot.toPath().relativize(project.toPath()).toString().replace('\\', '/');
			final IPath projectLocation = new Path(project.getAbsolutePath());
			final IProject target;
			try {
				target = findLibraryTarget(name, source, projectLocation, report, bySource);
			} catch (final CoreException | IOException e) {
				report.error("Could not resolve the library project " + name, e);
				continue;
			}
			final String projectName = target.getName();
			description.setName(projectName);
			description.setLocation(projectLocation);

			final IPath oldLocation = target.exists() ? target.getLocation() : null;
			final boolean relinking;
			try {
				relinking = target.exists() && !sameLocation(oldLocation, projectLocation);
			} catch (final IOException e) {
				importedNames.add(projectName);
				report.error("Could not resolve the location of " + projectName + " at " + oldLocation
						+ ". The library link was left untouched.", e);
				continue;
			}

			final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

				@Override
				protected void execute(final IProgressMonitor monitor)
						throws CoreException, InvocationTargetException, InterruptedException {
					final IProject proj = GAMA.getWorkspaceManager().getRoot().getProject(projectName);
					final boolean created = !proj.exists() || relinking;
					if (relinking) { replaceLibraryLink(proj, description, monitor); }
					if (!proj.exists()) { proj.create(description, monitor); }
					proj.open(IResource.NONE, monitor);
					configureProjectDescription(proj, plugin);
					LibraryProjectSupport.remember(proj, source, ProjectCategory.ofFolder(folder.category()));
					importedNames.add(projectName);
					report.available();
					if (relinking) {
						report.changed("Relinked " + projectName + "\n  From: " + oldLocation + "\n  To: " + projectLocation);
					} else if (created) {
						report.changed("Restored " + projectName + " from " + projectLocation + ".");
					}
				}
			};
			try {
				operation.run(monitor);
				bySource.putIfAbsent(source, target);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OperationCanceledException();
			} catch (final InvocationTargetException e) {
				report.error("Could not link " + projectName + " to " + projectLocation, e.getCause());
			}
		}
	}

	private static boolean sameLocation(final IPath first, final IPath second) throws IOException {
		return first != null && second != null
				&& first.toFile().getCanonicalFile().equals(second.toFile().getCanonicalFile());
	}

	/** Prefer a stable identity; never evict a user project, even when its files have disappeared. */
	IProject findLibraryTarget(final String name, final String source, final IPath location,
			final LibraryUpdateReport report) throws CoreException, IOException {
		return findLibraryTarget(name, source, location, report, indexLibrarySources());
	}

	private Map<String, IProject> indexLibrarySources() throws CoreException {
		final Map<String, IProject> result = new HashMap<>();
		for (final IProject project : GAMA.getWorkspaceManager().getRoot().getProjects()) {
			final String source = LibraryProjectSupport.propertyOf(project, LibraryProjectSupport.SOURCE);
			if (source != null) result.putIfAbsent(source, project);
		}
		return result;
	}

	private IProject findLibraryTarget(final String name, final String source, final IPath location,
			final LibraryUpdateReport report, final Map<String, IProject> bySource) throws CoreException, IOException {
		final IProject known = bySource.get(source);
		if (known != null) return known;
		final IWorkspaceRoot root = GAMA.getWorkspaceManager().getRoot();
		final IProject candidate = root.getProject(name);
		if (!candidate.exists() || sameLocation(candidate.getLocation(), location)) return candidate;
		final boolean wasOpen = candidate.isOpen();
		try {
			// A legacy closed project has no root-level provenance yet. Open it only for migration,
			// and restore its closed state if it turns out to be a user project.
			if (!wasOpen) { candidate.open(new NullProgressMonitor()); }
			if (LibraryProjectSupport.isBuiltIn(candidate)
					&& LibraryProjectSupport.propertyOf(candidate, LibraryProjectSupport.SOURCE) == null) return candidate;
		} finally {
			if (!wasOpen) { candidate.close(new NullProgressMonitor()); }
		}
		for (int i = 1;; i++) {
			final IProject alternative = root.getProject(name + " (library" + (i == 1 ? "" : " " + i) + ")");
			if (!alternative.exists() || sameLocation(alternative.getLocation(), location)) {
				report.warning("Kept existing project '" + name + "' at " + candidate.getLocation()
						+ ". The bundled project is available as '" + alternative.getName() + "' at " + location + ".");
				return alternative;
			}
		}
	}

	/** Re-register only, retaining the old files and restoring the original registration if creation fails. */
	void replaceLibraryLink(final IProject project, final IProjectDescription replacement,
			final IProgressMonitor monitor) throws CoreException {
		final boolean wasOpen = project.isOpen();
		if (!wasOpen) { project.open(monitor); }
		final IProjectDescription previous = project.getDescription();
		final Map<QualifiedName, String> properties = project.getPersistentProperties();
		project.delete(false, true, monitor);
		try {
			project.create(replacement, monitor);
		} catch (final CoreException | OperationCanceledException e) {
			try {
				if (!project.exists()) {
					project.create(previous, new NullProgressMonitor());
					project.open(new NullProgressMonitor());
					for (final var property : properties.entrySet()) {
						project.setPersistentProperty(property.getKey(), property.getValue());
					}
					if (!wasOpen) { project.close(new NullProgressMonitor()); }
				}
			} catch (final CoreException restoreError) {
				e.addSuppressed(restoreError);
			}
			throw e;
		}
	}

	/**
	 * Returns the last segment of the symbolic name of a bundle.
	 *
	 * @param bundle
	 *            the bundle
	 * @return the last segment of its symbolic name
	 */
	private static String lastSegment(final Bundle bundle) {
		final String bundleName = bundle.getSymbolicName();
		return bundleName.substring(bundleName.lastIndexOf('.') + 1);
	}

	/**
	 * Creates the or update project.
	 *
	 * @param name
	 *            the name
	 * @return the i project
	 */
	public IProject createOrUpdateProject(final String name) {
		return createOrUpdateProject(name, null);
	}

	/**
	 * Creates or updates a project, which can be located outside of the default location in the workspace.
	 *
	 * @param name
	 *            the name
	 * @param location
	 *            the directory of the project, or null to use the default location
	 * @return the i project
	 */
	public IProject createOrUpdateProject(final String name, final File location) {
		final IProject[] projectHandle = { null };
		final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

			@Override
			protected void execute(final IProgressMonitor monitor) throws CoreException {
				final SubMonitor m = SubMonitor.convert(monitor, "Creating or updating " + name, 2000);
				final IProject project = GAMA.getWorkspaceManager().getRoot().getProject(name);
				final boolean created = !project.exists();
				if (created) {
					final IProjectDescription desc =
							GAMA.getWorkspaceManager().getWorkspace().newProjectDescription(name);
					if (location != null) { desc.setLocation(new Path(location.getAbsolutePath())); }
					project.create(desc, m.split(1000));
				}
				if (monitor.isCanceled()) throw new OperationCanceledException();
				project.open(IResource.BACKGROUND_REFRESH, m.split(1000));
				projectHandle[0] = project;
				if (created) { setValuesProjectDescription(project, null); }
			}
		};
		try {
			op.run(null);
		} catch (final InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		return projectHandle[0];
	}

	// static public String GET_BUILT_IN_GAMA_VERSION() {
	// if (BUILTIN_VERSION == null) {
	// BUILTIN_VERSION = Platform.getProduct().getDefiningBundle().getVersion().toString();
	// }
	// return BUILTIN_VERSION;
	// }

	/**
	 * Configures the project description of a project with the GAMA natures and properties.
	 *
	 * @param proj
	 *            the project to configure
	 * @param bundle
	 *            the plugin that contributed the project (can be null). Its symbolic name is stored in the comment of
	 *            the {@code .project} file
	 */
	public void setValuesProjectDescription(final IProject proj, final Bundle bundle) {
		try {
			configureProjectDescription(proj, bundle);
		} catch (final CoreException e) {
			DEBUG.ERR("Failed to set project description for: " + proj.getName(), e);
		}
	}

	private void configureProjectDescription(final IProject proj, final Bundle bundle) throws CoreException {
		final IProjectDescription desc = proj.getDescription();
		final String[] natures = { GamaNatures.XTEXT_NATURE, GamaNatures.GAMA_NATURE };
		final String comment = bundle == null ? "" : bundle.getSymbolicName();
		if (!java.util.Arrays.equals(desc.getNatureIds(), natures) || !comment.equals(desc.getComment())) {
			desc.setNatureIds(natures);
			desc.setComment(comment);
			proj.setDescription(desc, IResource.FORCE, null);
		}

		if (bundle != null) {
			LibraryProjectSupport.setIfChanged(proj, BUILTIN_PROPERTY, bundle.getVersion().toString());
		}
		DEBUG.OUT("Successfully configured project: " + proj.getName());
	}

	// static private IProjectDescription setProjectDescription(final File project) {
	// final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
	// final IPath location = new Path(project.getAbsolutePath());
	// description.setLocation(location);
	// return description;
	// }

	/**
	 * Creates a timestamp file in the workspace to track the current GAMA version. This helps identify which version of
	 * GAMA was used to create the workspace. Removes any existing timestamp files from previous versions.
	 */
	public void stampWorkspaceFromModels() {
		try {
			final String currentStamp = GAMA.getWorkspaceManager().getCurrentGamaStampString();
			final IWorkspaceRoot root = GAMA.getWorkspaceManager().getRoot();
			final String oldStamp = root.getPersistentProperty(BUILTIN_PROPERTY);
			final java.nio.file.Path newStampPath =
					java.nio.file.Paths.get(GAMA.getWorkspaceManager().getWorkspaceLocation(), currentStamp);
			if (currentStamp.equals(oldStamp) && Files.isRegularFile(newStampPath)) return;

			// Remove old stamp file if it exists
			if (oldStamp != null) {
				final java.nio.file.Path oldStampPath =
						java.nio.file.Paths.get(GAMA.getWorkspaceManager().getWorkspaceLocation(), oldStamp);
				try {
					Files.deleteIfExists(oldStampPath);
					DEBUG.OUT("Removed old workspace stamp: " + oldStamp);
				} catch (IOException e) {
					DEBUG.ERR("Failed to remove old stamp file: " + oldStampPath, e);
				}
			}

			// Set new stamp property
			LibraryProjectSupport.setIfChanged(root, BUILTIN_PROPERTY, currentStamp);

			// Create new stamp file
			try {
				Files.createFile(newStampPath);
				DEBUG.OUT("Created new workspace stamp: " + currentStamp);
			} catch (IOException e) {
				if (!Files.exists(newStampPath)) { DEBUG.ERR("Failed to create stamp file: " + newStampPath, e); }
			}

		} catch (final CoreException e) {
			DEBUG.ERR("Failed to stamp workspace from models", e);
		}
	}

	/**
	 * Checks if the given directory contains a GAMA project by looking for a .project file with GAMA nature. Uses
	 * caching to improve performance for repeated checks.
	 *
	 * @param f
	 *            the directory to check
	 * @return true if the directory contains a GAMA project, false otherwise
	 * @throws CoreException
	 *             if there's an error reading the project description
	 */
	public boolean isGamaProject(final File f) {
		if (f == null || !f.exists() || !f.isDirectory()) return false;

		// Check cache first
		final String cacheKey = f.getAbsolutePath();
		return projectExistenceCache.computeIfAbsent(cacheKey, key -> {
			try {
				final String[] list = f.list();
				if (list == null) return false;

				return Stream.of(list).filter(".project"::equals).findFirst().map(projectFile -> {
					try {
						final IPath projectPath = new Path(f.getAbsolutePath()).append(".project");
						final IProjectDescription pd =
								GAMA.getWorkspaceManager().getWorkspace().loadProjectDescription(projectPath);
						return GamaNatures.isGamaProject(pd);
					} catch (CoreException e) {
						DEBUG.ERR("Error checking GAMA nature for project: " + f.getAbsolutePath(), e);
						return false;
					}
				}).orElse(false);
			} catch (Exception e) {
				DEBUG.ERR("Error listing directory contents: " + f.getAbsolutePath(), e);
				return false;
			}
		});
	}

	/**
	 * Clears all internal caches. Should be called when workspace structure changes significantly or when memory usage
	 * needs to be reduced.
	 */
	public void clearCaches() {
		projectExistenceCache.clear();
		projectDescriptionCache.clear();
		DEBUG.OUT("Cleared all workspace model manager caches");
	}

	/**
	 * Gets cache statistics for monitoring and debugging purposes.
	 *
	 * @return a map containing cache names and their current sizes
	 */
	public Map<String, Integer> getCacheStatistics() {
		final Map<String, Integer> stats = new HashMap<>();
		stats.put("projectExistenceCache", projectExistenceCache.size());
		stats.put("projectDescriptionCache", projectDescriptionCache.size());
		return stats;
	}

	/**
	 * Utility method to safely get a project description with caching.
	 *
	 * @param projectDir
	 *            the project directory
	 * @return Optional containing the project description if available
	 */
	private Optional<IProjectDescription> getProjectDescription(final File projectDir) {
		return projectDescriptionCache.computeIfAbsent(projectDir, dir -> {
			try {
				final File projectFile = new File(dir, ".project");
				if (!projectFile.exists()) return Optional.empty();

				final IPath projectPath = new Path(projectFile.getAbsolutePath());
				final IProjectDescription desc =
						GAMA.getWorkspaceManager().getWorkspace().loadProjectDescription(projectPath);
				return Optional.of(desc);
			} catch (CoreException e) {
				DEBUG.ERR("Failed to load project description for: " + dir.getAbsolutePath(), e);
				return Optional.empty();
			}
		});
	}

}
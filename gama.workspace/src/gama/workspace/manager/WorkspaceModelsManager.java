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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.osgi.framework.Bundle;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;

import gama.api.GAMA;
import gama.api.additions.GamaBundleLoader;
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
	 *            Examples: - "/path/to/model.gaml" - opens the model in editor - "/path/to/model.gaml#myExperiment" -
	 *            runs the specified experiment - "/path/to/model.experiment" - runs experiment "0" by default
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
			final String en = expName;
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
						setValuesProjectDescription(proj, false, false, false, null);
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
	 * Creates an unclassified models project and adds the given file as a linked resource. Handles file conflicts by
	 * creating unique names when necessary.
	 *
	 * @param location
	 *            the location of the file to add
	 * @return the IFile representing the linked resource, or null if creation failed
	 */
	IFile createUnclassifiedModelsProjectAndAdd(final IPath location) {
		if (location == null) {
			DEBUG.ERR("Cannot add null location to unclassified models project");
			return null;
		}

		IFile targetFile = null;
		try {
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
					if (location.equals(existingPath)) {
						// File already linked to the same location
						DEBUG.OUT("File already linked to same location: " + location);
						return targetFile;
					}
				}
				// Create unique filename to avoid conflicts
				targetFile = createUniqueFileFrom(targetFile, modelFolder);
			}

			// Create the linked resource
			targetFile.createLink(location, IResource.NONE, null);
			DEBUG.OUT("Successfully created linked resource: " + targetFile.getFullPath());

			return targetFile;

		} catch (final CoreException e) {
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
				// DEBUG.OUT("Asynchronous link of models library...");
				GAMA.getGui().refreshNavigator();
				return GamaBundleLoader.ERRORED ? Status.CANCEL_STATUS : Status.OK_STATUS;
			}

		};
		job.setUser(true);
		job.schedule();

	}

	/**
	 * Load models library.
	 */
	public void loadModelsLibrary() {
		// while (!GAMA.__LOADED__ && !GamaBundleLoader.ERRORED) {
		// THREADS.WAIT(100, (String) null, "Impossible to load the Built-in Models Library");
		// }
		// DEBUG.OUT("Synchronous link of models library...");
		final Multimap<Bundle, String> pluginsWithModels = GamaBundleLoader.getPluginsWithModels();
		for (final Bundle plugin : pluginsWithModels.keySet()) {
			for (final String entry : pluginsWithModels.get(plugin)) { linkModelsToWorkspace(plugin, entry, false); }
		}
		final Multimap<Bundle, String> pluginsWithTests = GamaBundleLoader.getPluginsWithTests();
		for (final Bundle plugin : pluginsWithTests.keySet()) {
			for (final String entry : pluginsWithTests.get(plugin)) { linkModelsToWorkspace(plugin, entry, true); }
		}
		// If the directory is not empty, we should maybe try to recreate the projects (if they do not exist...)

		try (DirectoryStream<java.nio.file.Path> paths = Files.newDirectoryStream(
				java.nio.file.Path.of(GAMA.getWorkspaceManager().getWorkspaceLocation()), Files::isDirectory)) {
			for (java.nio.file.Path r : paths) {
				File folder = r.toFile();
				if (isGamaProject(folder)) { createOrUpdateProject(folder.getName()); }
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * @param plugin
	 */

	private void linkModelsToWorkspace(final Bundle bundle, final String path, final boolean tests) {
		// DEBUG.OUT("Linking library from bundle " + bundle.getSymbolicName() + " at path " + path);
		final boolean core = bundle.equals(GamaBundleLoader.CORE_MODELS);
		final URL fileURL = bundle.getEntry(path);
		File modelsRep = null;
		try {
			final URL resolvedFileURL = FileLocator.toFileURL(fileURL);
			// We need to use the 3-arg constructor of URI in order to properly escape file system chars
			final URI resolvedURI = new URI(resolvedFileURL.getProtocol(), resolvedFileURL.getPath(), null).normalize();
			modelsRep = new File(resolvedURI);

		} catch (final URISyntaxException | IOException e1) {
			e1.printStackTrace();
		}

		final Map<File, IPath> foundProjects = new HashMap<>();
		findProjects(modelsRep, foundProjects);
		importBuiltInProjects(bundle, core, tests, foundProjects);

		if (core) { stampWorkspaceFromModels(); }

	}

	/**
	 * Recursively finds all GAMA project directories by looking for .project files.
	 *
	 * @param folder
	 *            the root folder to search from
	 * @param found
	 *            the map to store found projects (folder -> .project file path)
	 */
	private void findProjects(final File folder, final Map<File, IPath> found) {
		if (folder == null || !folder.exists() || !folder.isDirectory()) return;

		// Check for .project file in current directory
		final File[] dotFiles = folder.listFiles(DOT_PROJECT_FILTER);
		if (dotFiles != null && dotFiles.length > 0) {
			found.put(folder, new Path(dotFiles[0].getAbsolutePath()));
			return; // Found project, no need to search subdirectories
		}

		// Search subdirectories
		final File[] subdirectories = folder.listFiles(File::isDirectory);
		if (subdirectories != null) {
			Stream.of(subdirectories).parallel().forEach(subdir -> findProjects(subdir, found));
		}
	}

	/**
	 * @param plugin
	 * @param core
	 * @param workspace
	 * @param project
	 */
	private void importBuiltInProjects(final Bundle plugin, final boolean core, final boolean tests,
			final Map<File, IPath> projects) {
		for (final Map.Entry<File, IPath> entry : projects.entrySet()) {
			final File project = entry.getKey();
			final IPath location = entry.getValue();
			final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

				@Override
				protected void execute(final IProgressMonitor monitor)
						throws CoreException, InvocationTargetException, InterruptedException {
					IProject proj = GAMA.getWorkspaceManager().getRoot().getProject(project.getName());
					if (!proj.exists()) {
						proj.create(GAMA.getWorkspaceManager().getWorkspace().loadProjectDescription(location),
								monitor);
					} else // project exists but is not accessible
					if (!proj.isAccessible()) {
						proj.delete(true, null);
						proj = GAMA.getWorkspaceManager().getRoot().getProject(project.getName());
						proj.create(GAMA.getWorkspaceManager().getWorkspace().loadProjectDescription(location),
								monitor);
					}
					proj.open(IResource.NONE, monitor);
					setValuesProjectDescription(proj, true, !core, tests, plugin);
				}
			};
			try {
				operation.run(null);
			} catch (final InterruptedException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Creates the or update project.
	 *
	 * @param name
	 *            the name
	 * @return the i project
	 */
	public IProject createOrUpdateProject(final String name) {
		final IProject[] projectHandle = { null };
		final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

			@Override
			protected void execute(final IProgressMonitor monitor) throws CoreException {
				final SubMonitor m = SubMonitor.convert(monitor, "Creating or updating " + name, 2000);
				final IProject project = GAMA.getWorkspaceManager().getRoot().getProject(name);
				if (!project.exists()) {
					final IProjectDescription desc =
							GAMA.getWorkspaceManager().getWorkspace().newProjectDescription(name);
					project.create(desc, m.split(1000));
				}
				if (monitor.isCanceled()) throw new OperationCanceledException();
				project.open(IResource.BACKGROUND_REFRESH, m.split(1000));
				projectHandle[0] = project;
				setValuesProjectDescription(project, false, false, false, null);
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
	 * Configures project description with appropriate GAMA natures and properties. Sets up the project based on its
	 * type (builtin, plugin, test, or regular model).
	 *
	 * @param proj
	 *            the project to configure
	 * @param builtin
	 *            true if this is a built-in GAMA project
	 * @param inPlugin
	 *            true if this project is part of a plugin
	 * @param inTests
	 *            true if this is a test project
	 * @param bundle
	 *            the OSGi bundle associated with this project (can be null)
	 */
	public void setValuesProjectDescription(final IProject proj, final boolean builtin, final boolean inPlugin,
			final boolean inTests, final Bundle bundle) {
		try {
			// Build nature IDs list based on project type
			final List<String> natures = new ArrayList<>();
			natures.add(GamaNatures.XTEXT_NATURE);
			natures.add(GamaNatures.GAMA_NATURE);

			// Add specific nature based on project type
			if (inTests) {
				natures.add(GamaNatures.TEST_NATURE);
			} else if (inPlugin) {
				natures.add(GamaNatures.PLUGIN_NATURE);
			} else if (builtin) { natures.add(GamaNatures.BUILTIN_NATURE); }

			// Update project description
			final IProjectDescription desc = proj.getDescription();
			desc.setNatureIds(natures.toArray(new String[0])); // Java 8+ array creation

			// Set project comment based on type
			setProjectComment(desc, inTests, inPlugin, bundle);

			proj.setDescription(desc, IResource.FORCE, null);

			// Set builtin property if applicable
			if (builtin) {
				final String version = Platform.getProduct().getDefiningBundle().getVersion().toString();
				proj.setPersistentProperty(BUILTIN_PROPERTY, version);
			}

			DEBUG.OUT("Successfully configured project: " + proj.getName() + " (builtin=" + builtin + ", plugin="
					+ inPlugin + ", tests=" + inTests + ")");

		} catch (final CoreException e) {
			DEBUG.ERR("Failed to set project description for: " + proj.getName(), e);
		}
	}

	/**
	 * Sets the appropriate comment on the project description based on project type.
	 *
	 * @param desc
	 *            the project description to update
	 * @param inTests
	 *            true if this is a test project
	 * @param inPlugin
	 *            true if this is a plugin project
	 * @param bundle
	 *            the associated bundle (can be null)
	 */
	private void setProjectComment(final IProjectDescription desc, final boolean inTests, final boolean inPlugin,
			final Bundle bundle) {
		if (inTests && bundle == null) {
			desc.setComment("user defined");
		} else if ((inPlugin || inTests) && bundle != null) {
			final String bundleName = bundle.getSymbolicName();
			final String[] segments = bundleName.split("\\.");
			final String projectName = segments[segments.length - 1] + " plugin";
			desc.setComment(projectName);
		} else {
			desc.setComment("");
		}
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
			root.setPersistentProperty(BUILTIN_PROPERTY, currentStamp);

			// Create new stamp file
			final java.nio.file.Path newStampPath =
					java.nio.file.Paths.get(GAMA.getWorkspaceManager().getWorkspaceLocation(), currentStamp);
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
						return pd.hasNature(GamaNatures.GAMA_NATURE);
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
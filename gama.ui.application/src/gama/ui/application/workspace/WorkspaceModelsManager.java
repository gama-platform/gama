/*******************************************************************************************************
 *
 * WorkspaceModelsManager.java, in gama.ui.application, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.application.workspace;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.osgi.framework.Bundle;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;

import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.dev.THREADS;
import gama.gaml.compilation.kernel.GamaBundleLoader;

/**
 * Class InitialModelOpener.
 *
 * @author drogoul
 * @since 16 nov. 2013
 *
 */
public class WorkspaceModelsManager {

	static {
		DEBUG.OFF();
	}

	/** The Constant GAMA_NATURE. */
	public final static String GAMA_NATURE = "gama.ui.application.gamaNature";

	/** The Constant XTEXT_NATURE. */
	public final static String XTEXT_NATURE = "org.eclipse.xtext.ui.shared.xtextNature";

	/** The Constant PLUGIN_NATURE. */
	public final static String PLUGIN_NATURE = "gama.ui.application.pluginNature";

	/** The Constant TEST_NATURE. */
	public final static String TEST_NATURE = "gama.ui.application.testNature";

	/** The Constant BUILTIN_NATURE. */
	public final static String BUILTIN_NATURE = "gama.ui.application.builtinNature";

	/** The Constant BUILTIN_PROPERTY. */
	public static final QualifiedName BUILTIN_PROPERTY = new QualifiedName("gama.builtin", "models");

	/** The Constant instance. */
	public final static WorkspaceModelsManager instance = new WorkspaceModelsManager();

	/** The workspace. */
	final IWorkspace workspace = ResourcesPlugin.getWorkspace();

	/** The location of the workspace */
	final IPath workspacePath = new Path(Platform.getInstanceLocation().getURL().getPath());

	/** The folder of the workspace */
	final String workspaceLocation = workspacePath.toOSString();

	/**
	 * Open model passed as argument.
	 *
	 * @param modelPath
	 *            the model path
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
				GAMA.getGui().editModel(file);
			} else {
				// DEBUG.OUT(Thread.currentThread().getName() + ": Trying to run experiment " + en);
				GAMA.getGui().runModel(file, en);
			}

			// };
			// new Thread(run, "Automatic opening of " + filePath).start();

		}
	}

	/**
	 * @param filePath
	 * @return
	 */
	private IFile findAndLoadIFile(final String filePath) {
		// No error in case of an empty argument
		if (Strings.isNullOrEmpty(filePath)) return null;
		final IPath path = new Path(filePath);

		// 1st case: the path can be identified as a file residing in the workspace
		IFile result = findInWorkspace(path);
		if (result != null) return result;
		// Otherwise it belongs outside
		return findOutsideWorkspace(path);
	}

	/**
	 * @param filePath
	 * @return
	 */
	private IFile findInWorkspace(final IPath originalPath) {
		final IPath filePath = originalPath.makeRelativeTo(workspacePath);
		IFile file = null;
		try {
			file = workspace.getRoot().getFile(filePath);
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
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "No project", "The model '"
					+ modelFile.getAbsolutePath()
					+ "' does not seem to belong to an existing GAML project. You can import it in an existing project or in the 'Unclassified models' project.");
			return createUnclassifiedModelsProjectAndAdd(originalPath);
		}

		final IPath location = new Path(dotFile.getAbsolutePath());
		final String pathToProject = projectFileBean.getName();

		try {
			// We load the project description.
			final IProjectDescription description = workspace.loadProjectDescription(location);
			if (description != null) {
				final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

					@Override
					protected void execute(final IProgressMonitor monitor)
							throws CoreException, InvocationTargetException, InterruptedException {
						// We try to get the project in the workspace
						IProject proj = workspace.getRoot().getProject(pathToProject);
						// If it does not exist, we create it
						if (!proj.exists()) {
							// If a project with the same name exists
							final IProject[] projects = workspace.getRoot().getProjects();
							final String name = description.getName();
							for (final IProject p : projects) {
								if (p.getName().equals(name)) {
									MessageDialog.openInformation(Display.getDefault().getActiveShell(),
											"Existing project",
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
							proj = workspace.getRoot().getProject(pathToProject);
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
			GAMA.getGui().error("Error wien importing project: " + e.getMessage());
		}
		final IProject project = workspace.getRoot().getProject(pathToProject);
		final String relativePathToModel =
				project.getName() + modelFile.getAbsolutePath().replace(projectFileBean.getPath(), "");
		return findInWorkspace(new Path(relativePathToModel));
	}

	/**
	 *
	 */

	public static final String UNCLASSIFIED_MODELS = "Unclassified Models";

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
		// First allow to select a parent folder
		final ContainerSelectionDialog dialog = new ContainerSelectionDialog(Display.getDefault().getActiveShell(),
				null, false, "Select a parent project or cancel to create a new project:");
		dialog.setTitle("Project selection");
		dialog.showClosedProjects(false);

		final int result = dialog.open();
		IProject project;
		IFolder modelFolder;

		if (result == Window.CANCEL) {
			project = createOrUpdateProject(UNCLASSIFIED_MODELS);
			modelFolder = project.getFolder(new Path("models"));
			if (!modelFolder.exists()) { modelFolder.create(true, true, null); }
		} else {
			final IContainer container =
					(IContainer) ResourcesPlugin.getWorkspace().getRoot().findMember((IPath) dialog.getResult()[0]);
			if (container instanceof IProject) {
				project = (IProject) container;
				modelFolder = project.getFolder(new Path("models"));
				if (!modelFolder.exists()) { modelFolder.create(true, true, null); }
			} else {
				modelFolder = (IFolder) container;
			}

		}

		return modelFolder;
	}

	/**
	 * Creates the unclassified models project and add.
	 *
	 * @param location
	 *            the location
	 * @return the i file
	 */
	IFile createUnclassifiedModelsProjectAndAdd(final IPath location) {
		IFile iFile = null;
		try {
			final IFolder modelFolder = createUnclassifiedModelsProject(location);
			iFile = modelFolder.getFile(location.lastSegment());
			if (iFile.exists()) {
				if (iFile.isLinked()) {
					final IPath path = iFile.getLocation();
					if (path.equals(location))
						// First case, this is a linked resource to the same location. In that case, we simply return
						// its name.
						return iFile;
				}
				// Second case, this resource is a link to another location. We create a filename that is
				// guaranteed not to exist and change iFile accordingly.
				iFile = createUniqueFileFrom(iFile, modelFolder);
			}
			iFile.createLink(location, IResource.NONE, null);
			// RefreshHandler.run();
			return iFile;
		} catch (final CoreException e) {
			e.printStackTrace();
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Error in creation",
					"The file " + (iFile == null ? location.lastSegment() : iFile.getFullPath().lastSegment())
							+ " cannot be created because of the following exception " + e.getMessage());
			return null;
		}
	}

	/**
	 * @param lastSegment
	 * @param modelFolder
	 * @return
	 */
	private IFile createUniqueFileFrom(final IFile originalFile, final IFolder modelFolder) {
		IFile file = originalFile;
		final Pattern p = Pattern.compile("(.*?)(\\d+)?(\\..*)?");
		while (file.exists()) {
			final IPath path = file.getLocation();
			String fName = path.lastSegment();
			final Matcher m = p.matcher(fName);
			if (m.matches()) {// group 1 is the prefix, group 2 is the number, group 3 is the suffix
				fName = m.group(1) + (m.group(2) == null ? 1 : Integer.parseInt(m.group(2)) + 1)
						+ (m.group(3) == null ? "" : m.group(3));
			}
			file = modelFolder.getFile(fName);
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
		while (!GamaBundleLoader.LOADED && !GamaBundleLoader.ERRORED) {
			THREADS.WAIT(100, (String) null, "Impossible to load the Built-in Models Library");
		}
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

		try (DirectoryStream<java.nio.file.Path> paths =
				Files.newDirectoryStream(java.nio.file.Path.of(workspaceLocation), Files::isDirectory)) {
			for (java.nio.file.Path r : paths) {
				File folder = r.toFile();
				if (isGamaProject(folder)) { createOrUpdateProject(folder.getName()); }
			}
		} catch (IOException | CoreException e1) {
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

	/** The Constant isDotFile. */
	private static final FilenameFilter isDotFile = (dir, name) -> ".project".equals(name);

	/**
	 * Find projects.
	 *
	 * @param folder
	 *            the folder
	 * @param found
	 *            the found
	 */
	private void findProjects(final File folder, final Map<File, IPath> found) {
		if (folder == null) return;
		final File[] dotFile = folder.listFiles(isDotFile);
		if (dotFile == null) return;
		if (dotFile.length == 0) { // no .project file
			final File[] files = folder.listFiles();
			if (files != null) { for (final File f : files) { findProjects(f, found); } }
			return;
		}
		found.put(folder, new Path(dotFile[0].getAbsolutePath()));

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
					IProject proj = workspace.getRoot().getProject(project.getName());
					if (!proj.exists()) {
						proj.create(workspace.loadProjectDescription(location), monitor);
					} else // project exists but is not accessible
					if (!proj.isAccessible()) {
						proj.delete(true, null);
						proj = workspace.getRoot().getProject(project.getName());
						proj.create(workspace.loadProjectDescription(location), monitor);
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
				final IProject project = workspace.getRoot().getProject(name);
				if (!project.exists()) {
					final IProjectDescription desc = workspace.newProjectDescription(name);
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
	 * Sets the values project description.
	 *
	 * @param proj
	 *            the proj
	 * @param builtin
	 *            the builtin
	 * @param inPlugin
	 *            the in plugin
	 * @param inTests
	 *            the in tests
	 * @param bundle
	 *            the bundle
	 */
	public void setValuesProjectDescription(final IProject proj, final boolean builtin, final boolean inPlugin,
			final boolean inTests, final Bundle bundle) {
		/* Modify the project description */
		IProjectDescription desc = null;
		try {

			final List<String> ids = new ArrayList<>();
			ids.add(XTEXT_NATURE);
			ids.add(GAMA_NATURE);
			if (inTests) {
				ids.add(TEST_NATURE);
			} else if (inPlugin) {
				ids.add(PLUGIN_NATURE);
			} else if (builtin) { ids.add(BUILTIN_NATURE); }
			desc = proj.getDescription();
			desc.setNatureIds(ids.toArray(new String[ids.size()]));
			// Addition of a special nature to the project.
			if (inTests && bundle == null) {
				desc.setComment("user defined");
			} else if ((inPlugin || inTests) && bundle != null) {
				String name = bundle.getSymbolicName();
				final String[] ss = name.split("\\.");
				name = ss[ss.length - 1] + " plugin";
				desc.setComment(name);
			} else {
				desc.setComment("");
			}
			proj.setDescription(desc, IResource.FORCE, null);
			// Addition of a special persistent property to indicate that the project is built-in
			if (builtin) {
				proj.setPersistentProperty(BUILTIN_PROPERTY,
						Platform.getProduct().getDefiningBundle().getVersion().toString());
			}
		} catch (final CoreException e) {
			e.printStackTrace();
		}
	}

	// static private IProjectDescription setProjectDescription(final File project) {
	// final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
	// final IPath location = new Path(project.getAbsolutePath());
	// description.setLocation(location);
	// return description;
	// }

	/**
	 * Stamp workspace from models.
	 */
	public void stampWorkspaceFromModels() {
		try {
			final String stamp = WorkspacePreferences.getCurrentGamaStampString();
			final IWorkspaceRoot root = workspace.getRoot();
			final String oldStamp = root.getPersistentProperty(BUILTIN_PROPERTY);
			if (oldStamp != null) {
				final File stampFile = new File(new Path(workspaceLocation + File.separator + oldStamp).toOSString());
				if (stampFile.exists()) { stampFile.delete(); }
			}
			root.setPersistentProperty(BUILTIN_PROPERTY, stamp);
			final File stampFile = new File(new Path(workspaceLocation + File.separator + stamp).toOSString());
			if (!stampFile.exists()) { stampFile.createNewFile(); }
		} catch (final CoreException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if is gama project.
	 *
	 * @param f
	 *            the f
	 * @return true, if is gama project
	 * @throws CoreException
	 *             the core exception
	 */
	public boolean isGamaProject(final File f) throws CoreException {
		final String[] list = f.list();
		if (list != null) {
			for (final String s : list) {
				if (".project".equals(s)) {
					IPath p = new Path(f.getAbsolutePath());
					p = p.append(".project");
					final IProjectDescription pd = workspace.loadProjectDescription(p);
					if (pd.hasNature(WorkspaceModelsManager.GAMA_NATURE)) return true;
				}
			}
		}
		return false;
	}

}

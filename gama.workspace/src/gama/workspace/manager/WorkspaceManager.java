/*******************************************************************************************************
 *
 * WorkspaceManager.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.util.tracker.ServiceTracker;

import gama.api.GAMA;
import gama.api.runtime.IWorkspaceManager;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gama.workspace.WorkspaceActivator;

/**
 * Singleton implementation of {@link IWorkspaceManager} that manages the GAMA workspace lifecycle.
 *
 * <p>
 * This class is responsible for:
 * </p>
 * <ul>
 * <li>Locating and validating the workspace directory on startup.</li>
 * <li>Persisting workspace-related preferences (remembered location, recent workspaces, rebuild flags).</li>
 * <li>Checking workspace sanity (stale lock files, corrupted snap files) and triggering a clean rebuild when
 * necessary.</li>
 * <li>Providing access to the underlying {@link IWorkspace} and its root via the OSGi service registry.</li>
 * <li>Exposing workspace location as a file-system path, an Eclipse {@link IPath}, and an EMF {@link URI}.</li>
 * </ul>
 *
 * <p>
 * The single instance is obtained via {@link #getInstance()}.
 * </p>
 *
 * @see IWorkspaceManager
 * @see IWorkspace
 * @see IWorkspaceRoot
 */
public class WorkspaceManager implements IWorkspaceManager {

	// -----------------------------------------------------------------------
	// Fields
	// -----------------------------------------------------------------------

	/**
	 * The cached {@link IWorkspace} service, lazily resolved via the OSGi {@link ServiceTracker} on first access.
	 */
	private IWorkspace workspace;

	// -----------------------------------------------------------------------
	// Singleton
	// -----------------------------------------------------------------------

	/**
	 * The unique instance of this class. Access it through {@link #getInstance()}.
	 */
	private static WorkspaceManager INSTANCE;

	/**
	 * Cached stamp string that identifies the current version of the built-in models library. Lazily initialised by
	 * {@link #getModelIdentifier()}.
	 */
	public static String MODEL_IDENTIFIER = null;

	/**
	 * Returns the singleton instance of {@link WorkspaceManager}, creating it on the first call.
	 *
	 * @return the unique {@link IWorkspaceManager} instance; never {@code null}
	 */
	public static IWorkspaceManager getInstance() {
		if (INSTANCE == null) { INSTANCE = new WorkspaceManager(); }
		return INSTANCE;
	}

	/**
	 * Private constructor – use {@link #getInstance()} to obtain the singleton.
	 */
	private WorkspaceManager() {}

	// -----------------------------------------------------------------------
	// Preference accessors
	// -----------------------------------------------------------------------

	/**
	 * Returns whether the user opted to remember the last workspace location so that the workspace-selection dialog is
	 * skipped on the next launch.
	 *
	 * @return {@code true} if the "remember workspace" preference is set; {@code false} otherwise
	 */
	@Override
	public boolean isRememberWorkspace() {
		return getBooleanPref(KEY_WORKSPACE_REMEMBER, false);
	}

	/**
	 * Persists the user's choice about whether to remember the workspace location across sessions.
	 *
	 * @param remember
	 *            {@code true} to remember the current workspace location; {@code false} to always show the dialog
	 */
	@Override
	public void isRememberWorkspace(final boolean remember) {
		GAMA.getPreferenceStore().putInStore(KEY_WORKSPACE_REMEMBER, remember);
	}

	/**
	 * Returns the semicolon-separated list of recently used workspace paths stored in the preference store.
	 *
	 * @return a (possibly empty) string containing the list of recently used workspace paths
	 */
	@Override
	public String getLastUsedWorkspaces() { return GAMA.getPreferenceStore().getInStore(KEY_WORKSPACE_LIST, ""); }

	/**
	 * Persists the semicolon-separated list of recently used workspace paths to the preference store.
	 *
	 * @param used
	 *            the new list of recently used workspace paths; must not be {@code null}
	 */
	@Override
	public void setLastUsedWorkspaces(final String used) {
		GAMA.getPreferenceStore().putInStore(KEY_WORKSPACE_LIST, used);
	}

	/**
	 * Returns the absolute path of the workspace directory that was selected during the last session. Falls back to
	 * {@code <user.home>/Gama_Workspace} when no value has been stored yet.
	 *
	 * @return the last explicitly set workspace path; never {@code null}
	 */
	@Override
	public String getLastSetWorkspaceDirectory() {
		return GAMA.getPreferenceStore().getInStore(KEY_WORSPACE_PATH,
				System.getProperty("user.home") + File.separator + "Gama_Workspace");
	}

	/**
	 * Persists the given workspace directory path to the preference store so that it can be restored on the next
	 * launch.
	 *
	 * @param last
	 *            the absolute path of the workspace directory to remember; must not be {@code null}
	 */
	@Override
	public void setLastSetWorkspaceDirectory(final String last) {
		GAMA.getPreferenceStore().putInStore(KEY_WORSPACE_PATH, last);
	}

	/**
	 * Flags the workspace for clearing on the next start-up. Delegates to the preference store using the
	 * {@link #CLEAR_WORKSPACE} key.
	 *
	 * @param clear
	 *            {@code true} to schedule a workspace clear; {@code false} to cancel a previously scheduled clear
	 */
	@Override
	public void clearWorkspace(final boolean clear) {
		GAMA.getPreferenceStore().putInStore(CLEAR_WORKSPACE, clear);
	}

	// -----------------------------------------------------------------------
	// Private helpers
	// -----------------------------------------------------------------------

	/**
	 * Reads a boolean preference from the store, converting the stored string value with
	 * {@link Boolean#parseBoolean(String)}.
	 *
	 * @param key
	 *            the preference key; must not be {@code null}
	 * @param defaultValue
	 *            the value to return when the key is absent from the store
	 * @return the boolean value of the preference
	 */
	private boolean getBooleanPref(final String key, final boolean defaultValue) {
		return Boolean.parseBoolean(GAMA.getPreferenceStore().getInStore(key, String.valueOf(defaultValue)));
	}

	/**
	 * Returns whether the user should be asked for confirmation before GAMA rebuilds a corrupted workspace.
	 *
	 * @return {@code true} if a confirmation dialog should be shown; {@code false} to rebuild silently
	 */
	private boolean askBeforeRebuildingWorkspace() {
		return getBooleanPref(KEY_ASK_REBUILD, true);
	}

	/**
	 * Returns whether the user should be asked for confirmation before GAMA opens a workspace that was created by a
	 * different version of the platform (i.e. whose model-library stamp does not match the current one).
	 *
	 * @return {@code true} if a confirmation dialog should be shown; {@code false} to proceed silently
	 */
	private boolean askBeforeUsingOutdatedWorkspace() {
		return getBooleanPref(KEY_ASK_OUTDATED, true);
	}

	/**
	 * Creates the two marker files that identify a directory as a valid GAMA workspace:
	 * <ol>
	 * <li>The generic {@link IWorkspaceManager#WORKSPACE_IDENTIFIER} sentinel file.</li>
	 * <li>A stamp file whose name encodes the current version of the built-in models library.</li>
	 * </ol>
	 * The parent directories are created if they do not yet exist.
	 *
	 * @param workspaceDirectoryPath
	 *            the workspace root directory; must not be {@code null}
	 * @param workspaceIdentifierFilePath
	 *            full path to the {@link IWorkspaceManager#WORKSPACE_IDENTIFIER} sentinel file; must not be
	 *            {@code null}
	 * @throws RuntimeException
	 *             if any file-system operation fails
	 * @throws IOException
	 *             if any file-system operation fails
	 */
	private void createWorkspaceMarkerFiles(final Path workspaceDirectoryPath, final Path workspaceIdentifierFilePath)
			throws IOException {
		Files.createDirectories(workspaceDirectoryPath);
		Files.createFile(workspaceIdentifierFilePath);
		Files.createFile(Paths.get(workspaceDirectoryPath.toString(), getModelIdentifier()));
	}

	/**
	 * Recursively deletes a directory and all its contents. Does nothing if {@code dir} is {@code null} or does not
	 * exist.
	 *
	 * @param dir
	 *            the root of the directory tree to delete; may be {@code null}
	 */
	private static void deleteDirectory(final File dir) {
		if (dir == null || !dir.exists()) return;
		File[] children = dir.listFiles();
		if (children != null) { for (File child : children) { deleteDirectory(child); } }
		dir.delete();
	}

	/**
	 * Checks the sanity of the workspace at {@code workspacePath} and triggers a clean rebuild when problems are
	 * detected.
	 *
	 * <p>
	 * The following situations are handled:
	 * </p>
	 * <ul>
	 * <li>A {@code .rebuild} sentinel file — triggers an unconditional rebuild (see issue #3445).</li>
	 * <li>A stale {@code .metadata/.lock} file — indicates that the previous session was force-killed or crashed, which
	 * is the root cause of the {@code "Workspace is closed"} exception at the next start-up. The
	 * {@code org.eclipse.core.resources} metadata folder is deleted proactively.</li>
	 * <li>{@code .snap} files inside {@code org.eclipse.core.resources} — indicate workspace corruption caused by a
	 * previous crash.</li>
	 * </ul>
	 * <p>
	 * When {@link #askBeforeRebuildingWorkspace()} returns {@code true}, the user is presented with a confirmation
	 * dialog before any destructive action is taken.
	 * </p>
	 *
	 * @param workspacePath
	 *            the path to the workspace root directory; must not be {@code null}
	 * @return {@code true} if the workspace appears sane and no rebuild was triggered; {@code false} if a rebuild was
	 *         scheduled
	 */
	private boolean testWorkspaceSanity(final Path workspacePath) {
		return DEBUG.TIMER(BANNER_CATEGORY.GAMA, "Workspace sanity", "checked in", () -> {
			File workspaceDir = workspacePath.toFile();

			// Issue #3445 – a manually placed .rebuild file forces an unconditional rebuild.
			File[] files = workspaceDir.listFiles((FileFilter) file -> ".rebuild".equals(file.getName()));
			boolean rebuild = false;
			if (files != null && files.length == 1) {
				if (files[0].exists()) { files[0].delete(); }
				rebuild = true;
			}

			if (!rebuild) {
				files = workspaceDir.listFiles((FileFilter) file -> ".metadata".equals(file.getName()));
				if (files == null || files.length == 0) return true;
				final File metadataDir = files[0];

				// Remove stale log files left by previous sessions.
				final File[] logs = metadataDir.listFiles((FileFilter) file -> file.getName().contains(".log"));
				if (logs != null) { for (final File log : logs) { log.delete(); } }

				// Detect a stale .lock file — a sign that the previous session was force-killed or crashed.
				// This is the root cause of the "Workspace is closed" exception at the next launch.
				// When detected, we proactively delete the org.eclipse.core.resources folder to prevent
				// Eclipse from failing to open the workspace on the next start.
				final File lockFile = new File(metadataDir, ".lock");
				if (lockFile.exists()) {
					boolean doClean = true;
					if (askBeforeRebuildingWorkspace()) {
						doClean = GAMA.getGui().getDialogFactory().question("Stale workspace lock detected", """
								GAMA detected that the previous session was not closed cleanly (a stale lock file \
								was found in the workspace). This can cause a 'Workspace is closed' error \
								at startup. Would you like GAMA to clean the workspace metadata now to \
								prevent this error ?""");
					}
					if (doClean) {
						lockFile.delete();
						final File pluginsDir = new File(metadataDir, ".plugins");
						if (pluginsDir.exists()) {
							deleteDirectory(new File(pluginsDir, "org.eclipse.core.resources"));
						}
						clearWorkspace(true);
						return false;
					}
				}

				// Check for .snap files that indicate workspace corruption.
				files = metadataDir.listFiles((FileFilter) file -> ".plugins".equals(file.getName()));
				if (files == null || files.length == 0) return files != null;
				files = files[0].listFiles((FileFilter) file -> "org.eclipse.core.resources".equals(file.getName()));
				if (files == null || files.length == 0) return files != null;
				files = files[0].listFiles((FileFilter) file -> file.getName().contains("snap"));
				if (files == null || files.length == 0) return files != null;

				if (askBeforeRebuildingWorkspace()) {
					rebuild = GAMA.getGui().getDialogFactory().question("Corrupted workspace",
							"The workspace appears to be corrupted (due to a previous crash) or "
									+ "it is currently used by another instance of the platform. Would you like GAMA to clean it ?");
				}
			}

			if (rebuild) {
				if (files != null) { for (final File file : files) { if (file.exists()) { file.delete(); } } }
				clearWorkspace(true);
				return false;
			}
			return true;
		});
	}

	// -----------------------------------------------------------------------
	// Workspace identity / stamp
	// -----------------------------------------------------------------------

	/**
	 * Computes a stamp string that encodes the last-modified timestamp of the built-in GAMA models library. The stamp
	 * has the form {@code .built_in_models_<timestamp>} and is used to detect whether the workspace was created with a
	 * different version of the models library.
	 *
	 * @return the stamp string, or {@code null} if the models library location cannot be resolved
	 */
	@Override
	public String getCurrentGamaStampString() {
		String gamaStamp = null;
		try {
			final URL tmpURL = new URL(GAMA_LIBRARY_MODELS);
			final URL resolvedFileURL = FileLocator.toFileURL(tmpURL);
			// Use the 3-argument URI constructor to properly escape file-system characters.
			final java.net.URI resolvedURI =
					new java.net.URI(resolvedFileURL.getProtocol(), resolvedFileURL.getPath(), null).normalize();
			final File modelsRep = new File(resolvedURI);
			gamaStamp = ".built_in_models_" + modelsRep.lastModified();
			LocalDateTime localDateTime = Files.getLastModifiedTime(modelsRep.toPath()).toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			String date = localDateTime.format(DateTimeFormatter.ofPattern("MMM dd,yyyy HH:mm:ss"));
			DEBUG.BANNER(BANNER_CATEGORY.GAMA, "Checking date of models library", "modified", "" + date);
		} catch (final IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return gamaStamp;
	}

	/**
	 * Returns the model identifier for the current GAMA models library, computing and caching it on first call via
	 * {@link #getCurrentGamaStampString()}.
	 *
	 * @return the model identifier string; may be {@code null} if the library location cannot be resolved
	 */
	@Override
	public String getModelIdentifier() {
		if (MODEL_IDENTIFIER == null) { MODEL_IDENTIFIER = getCurrentGamaStampString(); }
		return MODEL_IDENTIFIER;
	}

	// -----------------------------------------------------------------------
	// Workspace directory validation
	// -----------------------------------------------------------------------

	/**
	 * Validates a candidate workspace directory and ensures it is usable by GAMA.
	 *
	 * <p>
	 * The method performs the following checks in order:
	 * </p>
	 * <ol>
	 * <li>If the directory does not yet exist and {@code askCreate} is {@code true}, the user is asked whether to
	 * create it.</li>
	 * <li>The directory must be readable and must actually be a directory.</li>
	 * <li>The workspace sanity check ({@link #testWorkspaceSanity}) is executed.</li>
	 * <li>The GAMA workspace identifier file must be present (or the user must accept to create it when
	 * {@code fromDialog} is {@code true}).</li>
	 * <li>The model-library stamp file must match the current version (or the user must accept an update).</li>
	 * <li>When {@code cloning} is {@code true}, the user must confirm that the target workspace contents will be
	 * replaced.</li>
	 * </ol>
	 *
	 * @param workspaceLocation
	 *            absolute path of the directory to validate; must not be {@code null}
	 * @param askCreate
	 *            if {@code true} and the directory does not exist, ask the user whether to create it
	 * @param fromDialog
	 *            {@code true} when the call originates from the workspace-selection UI dialog; affects which dialogs
	 *            are shown
	 * @param cloning
	 *            {@code true} when the workspace is about to be cloned into the given location
	 * @return {@code null} if the directory is valid and ready to use; a human-readable error message otherwise
	 */
	@Override
	public String checkWorkspaceDirectory(final String workspaceLocation, final boolean askCreate,
			final boolean fromDialog, final boolean cloning) {
		final Path workspaceDirectoryPath = Paths.get(workspaceLocation);
		final Path workspaceIdentifierFilePath = Paths.get(workspaceLocation, WORKSPACE_IDENTIFIER);

		if (!Files.exists(workspaceDirectoryPath) && askCreate) {
			final boolean create = GAMA.getGui().getDialogFactory().question("New Directory",
					workspaceLocation + " does not exist. Would you like to create a new workspace here"
							+ (cloning ? ", copy the projects of your current workspace into it," : "")
							+ " and proceed ?");
			if (create) {
				try {
					createWorkspaceMarkerFiles(workspaceDirectoryPath, workspaceIdentifierFilePath);
					return null;
				} catch (final RuntimeException | IOException er) {
					er.printStackTrace();
					return "Error creating directories, please check folder permissions";
				}
			}
			if (!Files.notExists(workspaceDirectoryPath)) return "The selected directory does not exist";
			return null;
		}

		if (!Files.isReadable(workspaceDirectoryPath)) return "The selected directory is not readable";
		if (!Files.isDirectory(workspaceDirectoryPath)) return "The selected path is not a directory";

		testWorkspaceSanity(workspaceDirectoryPath);

		if (fromDialog) {
			if (Files.notExists(workspaceIdentifierFilePath)) {
				final boolean create = GAMA.getGui().getDialogFactory().question("New Workspace", "The directory '"
						+ workspaceIdentifierFilePath.toAbsolutePath()
						+ "' exists but is not identified as a GAMA workspace. \n\nWould you like to use it anyway ?");
				if (!create) return "Please select a directory for your workspace";
				try {
					Files.createDirectories(workspaceDirectoryPath);
					Files.createFile(workspaceIdentifierFilePath);
				} catch (final Exception err) {
					return "Error creating directories, please check folder permissions";
				}
				if (Files.notExists(workspaceIdentifierFilePath)) return "The selected directory does not exist";
				return null;
			}
		} else if (Files.notExists(workspaceIdentifierFilePath))
			return "The selected directory is not a workspace directory";

		final File dotFile = new File(workspaceLocation + File.separator + getModelIdentifier());
		if (!dotFile.exists()) {
			if (fromDialog) {
				boolean proceed = true;
				if (askBeforeUsingOutdatedWorkspace()) {
					proceed = GAMA.getGui().getDialogFactory().question("Different version of the models library",
							"The workspace contains a different version of the models library. Do you want GAMA to proceed and update it ?");
				}
				if (proceed) {
					try {
						dotFile.createNewFile();
						clearWorkspace(true);
					} catch (final IOException e) {
						return "Error updating the models library";
					}
					return null;
				}
			}
			return "models";
		}

		if (cloning) {
			final boolean b = GAMA.getGui().getDialogFactory().question("Existing workspace",
					"The path entered is a path to an existing workspace. Its contents will be erased and replaced by the current workspace contents. Proceed anyway ?");
			if (!b) return "";
		}
		return null;
	}

	/**
	 * Validates and sets the Eclipse instance location for the workspace on application startup.
	 *
	 * <p>
	 * The resolution order is:
	 * </p>
	 * <ol>
	 * <li>If the location is already set (e.g. via {@code -data} on the command line) it is validated and used
	 * directly.</li>
	 * <li>Otherwise the "remember workspace" preference is consulted. If set and the remembered location is still
	 * valid, it is used without prompting the user.</li>
	 * <li>If no valid remembered location exists the workspace-selection dialog is opened.</li>
	 * </ol>
	 *
	 * @return {@code null} on success, or {@link IApplication#EXIT_OK} if the application must abort
	 * @throws IOException
	 *             if the instance location URL cannot be set
	 */
	@Override
	public Object checkWorkspace() throws IOException {
		final Location instanceLoc = Platform.getInstanceLocation();
		if (instanceLoc == null) {
			// -data @none was specified but GAMA requires a workspace.
			GAMA.getGui().getDialogFactory().error("A workspace is required to run GAMA");
			return IApplication.EXIT_OK;
		}

		boolean remember = false;
		String lastUsedWs = null;

		if (instanceLoc.isSet()) {
			lastUsedWs = instanceLoc.getURL().getFile();
			final String ret = checkWorkspaceDirectory(lastUsedWs, false, false, false);
			if (ret != null) {
				GAMA.getGui().getDialogFactory().error("The workspace provided cannot be used. Please change it");
				GAMA.getGui().exit();
				return IApplication.EXIT_OK;
			}
		} else {
			remember = isRememberWorkspace();
			lastUsedWs = getLastSetWorkspaceDirectory();
			// A "remember" flag without a stored path is meaningless.
			if (remember && (lastUsedWs == null || lastUsedWs.isEmpty())) { remember = false; }
			if (remember) {
				final String ret = checkWorkspaceDirectory(lastUsedWs, false, false, false);
				if (ret != null) {
					remember = "models".equals(ret) && askBeforeUsingOutdatedWorkspace()
							&& GAMA.getGui().getDialogFactory().question("Different version of the models library",
									"The workspace contains a different version of the models library. Do you want GAMA to proceed and update it ?");
					if (remember) { clearWorkspace(true); }
				}
			}
		}

		if (!remember) {
			String wr = GAMA.getGui().getDialogFactory().openWorkspaceSelectionDialog(true);
			if (wr == null) {
				GAMA.getGui().getDialogFactory().error("GAMA can not start without a workspace and will now exit.");
				return IApplication.EXIT_OK;
			}
			instanceLoc.set(new URL("file", null, wr), false);
		} else if (!instanceLoc.isSet()) {
			instanceLoc.set(new URL("file", null, lastUsedWs), false);
		}

		return null;
	}

	/**
	 * Creates a {@code .rebuild} sentinel file inside the current workspace directory. On the next launch GAMA will
	 * detect this file, delete it, and perform a clean rebuild of the workspace metadata.
	 */
	@Override
	public void forceWorkspaceRebuild() {
		File f = new File(Platform.getInstanceLocation().getURL().getPath() + File.separator + ".rebuild");
		try {
			f.createNewFile();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	// -----------------------------------------------------------------------
	// Workspace resource accessors
	// -----------------------------------------------------------------------

	/**
	 * Returns the {@link IWorkspace} service obtained from the OSGi service registry. The service tracker is opened
	 * lazily on first call and the result is cached for subsequent calls.
	 *
	 * @return the {@link IWorkspace} instance, or {@code null} if the service is not yet available
	 */
	@Override
	public IWorkspace getWorkspace() {
		if (workspace == null) {
			ServiceTracker<IWorkspace, IWorkspace> workspaceTracker =
					new ServiceTracker<>(WorkspaceActivator.getContext(), IWorkspace.class, null);
			workspaceTracker.open();
			workspace = workspaceTracker.getService();
		}
		return workspace;
	}

	/**
	 * Returns the {@link IWorkspaceRoot} of the current workspace.
	 *
	 * <p>
	 * If the underlying {@link IWorkspace} service is unavailable (e.g. because the previous session crashed before
	 * the metadata could be flushed), this method attempts a recovery by deleting the corrupted
	 * {@code .metadata/.plugins/org.eclipse.core.resources/.root} tree and scheduling a workspace clear. It then
	 * returns {@code null} so that the caller can abort gracefully.
	 * </p>
	 *
	 * @return the {@link IWorkspaceRoot}, or {@code null} if the workspace service is not available
	 */
	@Override
	public IWorkspaceRoot getRoot() {
		IWorkspace ws = getWorkspace();
		if (ws == null) {
			DEBUG.ERR("The workspace service could not be obtained.");
			GAMA.getGui().getDialogFactory().inform(
					"GAMA detected that the previous session was not closed cleanly. "
							+ "The workspace metadata will be cleaned now and the platform will restart to prevent further errors.");
			File wsDir = new File(getWorkspaceLocation());
			File[] files = wsDir.listFiles((FileFilter) file -> ".metadata".equals(file.getName()));
			if (files == null || files.length == 0) return null;
			final File metadataDir = files[0];
			final File pluginsDir = new File(metadataDir, ".plugins");
			if (pluginsDir.exists()) {
				final File rootFile = new File(new File(pluginsDir, "org.eclipse.core.resources"), ".root");
				if (rootFile.exists()) { deleteDirectory(rootFile); }
			}
			clearWorkspace(true);
			return null;
		}
		return ws.getRoot();
	}

	/**
	 * Returns an EMF {@link URI} pointing to the root of the current workspace, derived from the workspace root's
	 * location URI.
	 *
	 * @return an EMF {@link URI} representing the workspace root location; never {@code null}
	 */
	@Override
	public URI getWorkspaceURI() { return URI.createURI(getRoot().getLocationURI().toString(), false); }

	/**
	 * Returns the absolute file-system path of the workspace as an Eclipse {@link IPath}, derived from the OSGi
	 * instance location.
	 *
	 * @return the workspace path; never {@code null}
	 */
	@Override
	public IPath getWorkspacePath() {
		return new org.eclipse.core.runtime.Path(Platform.getInstanceLocation().getURL().getPath());
	}

	/**
	 * Returns the absolute file-system path of the workspace as a platform-specific OS string (e.g.
	 * {@code /home/user/Gama_Workspace} on Unix).
	 *
	 * @return the workspace location string; never {@code null}
	 */
	@Override
	public String getWorkspaceLocation() { return getWorkspacePath().toOSString(); }

}
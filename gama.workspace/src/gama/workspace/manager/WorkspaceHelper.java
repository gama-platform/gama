/*******************************************************************************************************
 *
 * WorkspaceHelper.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.manager;

import static gama.core.common.preferences.GamaPreferenceStore.getStore;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.osgi.service.datalocation.Location;

import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.GAMA;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;

/**
 * The Class WorkspaceHelper.
 */
public class WorkspaceHelper {

	/** The Constant KEY_WORSPACE_PATH. */
	private static final String KEY_WORSPACE_PATH = "pref_workspace_path";

	/** The Constant KEY_WORKSPACE_REMEMBER. */
	private static final String KEY_WORKSPACE_REMEMBER = "pref_workspace_remember";

	/** The Constant KEY_WORKSPACE_LIST. */
	private static final String KEY_WORKSPACE_LIST = "pref_workspace_list";

	/** The Constant KEY_ASK_REBUILD. */
	private static final String KEY_ASK_REBUILD = "pref_ask_rebuild";

	/** The Constant KEY_ASK_OUTDATED. */
	private static final String KEY_ASK_OUTDATED = "pref_ask_outdated";

	/** The Constant WORKSPACE_IDENTIFIER. */
	public static final String WORKSPACE_IDENTIFIER = ".gama_application_workspace";

	/** The model identifier. */
	private static String MODEL_IDENTIFIER = null;

	/**
	 * Returns whether the user selected "remember workspace" in the preferences
	 */
	public static boolean isRememberWorkspace() { return getStore().getBoolean(KEY_WORKSPACE_REMEMBER, false); }

	/**
	 * Checks if is remember workspace.
	 *
	 * @param remember
	 *            the remember
	 */
	public static void isRememberWorkspace(final boolean remember) {
		getStore().putBoolean(KEY_WORKSPACE_REMEMBER, remember);
	}

	/**
	 * Gets the last used workspaces.
	 *
	 * @return the last used workspaces
	 */
	public static String getLastUsedWorkspaces() { return getStore().get(KEY_WORKSPACE_LIST, ""); }

	/**
	 * Sets the last used workspaces.
	 *
	 * @param used
	 *            the new last used workspaces
	 */
	public static void setLastUsedWorkspaces(final String used) {
		getStore().put(KEY_WORKSPACE_LIST, used);
	}

	/**
	 * Returns the last set workspace directory from the preferences
	 *
	 * @return null if none
	 */
	public static String getLastSetWorkspaceDirectory() {
		return getStore().get(KEY_WORSPACE_PATH, System.getProperty("user.home") + File.separator + "Gama_Workspace");
	}

	/**
	 * Sets the last set workspace directory.
	 *
	 * @param last
	 *            the new last set workspace directory
	 */
	public static void setLastSetWorkspaceDirectory(final String last) {
		getStore().put(KEY_WORSPACE_PATH, last);
	}

	/**
	 * Ask before rebuilding workspace.
	 *
	 * @return true, if successful
	 */
	public static boolean askBeforeRebuildingWorkspace() {
		// true by default
		return GamaPreferences.Interface.CORE_ASK_REBUILD.getValue();
		// return getStore().getBoolean(KEY_ASK_REBUILD, true);
	}

	/**
	 * Ask before using outdated workspace.
	 *
	 * @return true, if successful
	 */
	public static boolean askBeforeUsingOutdatedWorkspace() {
		// true by default
		return GamaPreferences.Interface.CORE_ASK_OUTDATED.getValue();
		// return getStore().getBoolean(KEY_ASK_OUTDATED, true);
	}

	/**
	 * Gets the current gama stamp string.
	 *
	 * @return the current gama stamp string
	 */
	public static String getCurrentGamaStampString() {
		String gamaStamp = null;
		try {
			final URL tmpURL = new URL("platform:/plugin/gama.library/models/");
			final URL resolvedFileURL = FileLocator.toFileURL(tmpURL);
			// We need to use the 3-arg constructor of URI in order to properly escape file system chars
			final URI resolvedURI = new URI(resolvedFileURL.getProtocol(), resolvedFileURL.getPath(), null).normalize();
			final File modelsRep = new File(resolvedURI);
			final long time = modelsRep.lastModified();
			gamaStamp = ".built_in_models_" + time;
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
	 * Ensures a workspace directory is OK in regards of reading/writing, etc. This method will get called externally as
	 * well.
	 *
	 * @param parentShell
	 *            Shell parent shell
	 * @param workspaceLocation
	 *            Directory the user wants to use
	 * @param askCreate
	 *            Whether to ask if to create the workspace or not in this location if it does not exist already
	 * @param fromDialog
	 *            Whether this method was called from our dialog or from somewhere else just to check a location
	 * @return null if everything is ok, or an error message if not
	 */
	public static String checkWorkspaceDirectory(final String workspaceLocation, final boolean askCreate,
			final boolean fromDialog, final boolean cloning) {
		final Path workspaceDirectoryPath = Paths.get(workspaceLocation);
		final Path workspaceIdentifierFilePath = Paths.get(workspaceLocation, WORKSPACE_IDENTIFIER);
		if (!Files.exists(workspaceDirectoryPath) && askCreate) {
			final boolean create = GAMA.getGui().getDialogFactory().question("New Directory",
					workspaceLocation + " does not exist. Would you like to create a new workspace here"
							+ (cloning ? ", copy the projects of your current workspace into it," : "")
							+ " and proceeed ?");
			if (create) {
				try {
					Files.createDirectories(workspaceDirectoryPath);
					Files.createFile(workspaceIdentifierFilePath);
					Path dotPath = Paths.get(workspaceLocation, getModelIdentifier());
					Files.createFile(dotPath);
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
				boolean create = true;
				if (askBeforeUsingOutdatedWorkspace()) {
					create = GAMA.getGui().getDialogFactory().question("Different version of the models library",
							"The workspace contains a different version of the models library. Do you want GAMA to proceed and update it ?");
				}
				if (create) {
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
	 * Test workspace sanity.
	 *
	 * @param workspace
	 *            the workspace
	 * @return true, if successful
	 * @throws IOException
	 */
	public static boolean testWorkspaceSanity(final Path workspacePath) {

		return DEBUG.TIMER(BANNER_CATEGORY.GAMA, "Workspace sanity", "checked in", () -> {
			File workspace = workspacePath.toFile();
			// In light of issue #3445, allows a .rebuild file to trigger the rebuild
			File[] files = workspace.listFiles((FileFilter) file -> ".rebuild".equals(file.getName()));
			boolean rebuild = false;
			if (files != null && files.length == 1) {
				if (files[0].exists()) { files[0].delete(); }
				rebuild = true;
			}
			if (!rebuild) {
				files = workspace.listFiles((FileFilter) file -> ".metadata".equals(file.getName()));
				if (files == null || files.length == 0) return true;
				final File[] logs = files[0].listFiles((FileFilter) file -> file.getName().contains(".log"));
				if (logs != null) { for (final File log : logs) { log.delete(); } }
				files = files[0].listFiles((FileFilter) file -> ".plugins".equals(file.getName()));
				if (files == null) return false;
				if (files.length == 0) return true;
				files = files[0].listFiles((FileFilter) file -> "org.eclipse.core.resources".equals(file.getName()));
				if (files == null) return false;
				if (files.length == 0) return true;
				files = files[0].listFiles((FileFilter) file -> file.getName().contains("snap"));
				if (files == null) return false;
				// DEBUG.OUT("[GAMA] Workspace appears to be " + (files.length == 0 ? "clean" : "corrupted"));>
				if (files.length == 0) return true;

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

	/**
	 * Gets the model identifier.
	 *
	 * @return the model identifier
	 */
	public static String getModelIdentifier() {
		if (MODEL_IDENTIFIER == null) { MODEL_IDENTIFIER = getCurrentGamaStampString(); }
		return MODEL_IDENTIFIER;
	}

	/**
	 * Force workspace rebuild.
	 */
	public static void forceWorkspaceRebuild() {
		File f = new File(Platform.getInstanceLocation().getURL().getPath() + File.separator + ".rebuild");
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Check workspace.
	 *
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws MalformedURLException
	 *             the malformed URL exception
	 */
	public static Object checkWorkspace() throws IOException {
		final Location instanceLoc = Platform.getInstanceLocation();
		if (instanceLoc == null) {
			// -data @none was specified but GAMA requires a workspace
			GAMA.getGui().getDialogFactory().error("A workspace is required to run GAMA");
			return IApplication.EXIT_OK;
		}
		boolean remember = false;
		String lastUsedWs = null;
		if (instanceLoc.isSet()) {
			lastUsedWs = instanceLoc.getURL().getFile();
			final String ret = WorkspaceHelper.checkWorkspaceDirectory(lastUsedWs, false, false, false);
			if (ret != null) {
				// if ( ret.equals("Restart") ) { return EXIT_RESTART; }
				/* If we don't or can't remember and the location is set, we can't do anything as we need a workspace */
				GAMA.getGui().getDialogFactory().error("The workspace provided cannot be used. Please change it");
				GAMA.getGui().exit();
				return IApplication.EXIT_OK;
			}
		} else {

			/* Get what the user last said about remembering the workspace location */
			remember = isRememberWorkspace();
			/* Get the last used workspace location */
			lastUsedWs = getLastSetWorkspaceDirectory();
			/* If we have a "remember" but no last used workspace, it's not much to remember */
			if (remember && (lastUsedWs == null || lastUsedWs.length() == 0)) { remember = false; }
			if (remember) {
				/*
				 * If there's any problem with the workspace, force a dialog
				 */
				final String ret = checkWorkspaceDirectory(lastUsedWs, false, false, false);
				// AD Added this check explicitly as the checkWorkspaceDirectory() was not supposed to return null at
				// this stage
				if (ret != null) {
					remember = "models".equals(ret) && WorkspaceHelper.askBeforeUsingOutdatedWorkspace()
							&& GAMA.getGui().getDialogFactory().question("Different version of the models library",
									"The workspace contains a different version of the models library. Do you want GAMA to proceed and update it ?");
					if (remember) { clearWorkspace(true); }
				}
			}
		}

		/* If we don't remember the workspace, show the dialog */
		if (!remember) {
			String wr = GAMA.getGui().getDialogFactory().openWorkspaceSelectionDialog(true);
			if (wr == null) {
				GAMA.getGui().getDialogFactory().error("GAMA can not start without a workspace and will now exit.");
				return IApplication.EXIT_OK;
			}
			/* Tell Eclipse what the selected location was and continue */
			instanceLoc.set(new URL("file", null, wr), false);
			// if ( applyPrefs() ) { applyEclipsePreferences(getSelectedWorkspaceRootLocation()); }
		} else if (!instanceLoc.isSet()) {
			/* Set the last used location and continue */
			instanceLoc.set(new URL("file", null, lastUsedWs), false);
		}

		return null;
	}

	/** The Constant CLEAR_WORKSPACE. */
	public static final String CLEAR_WORKSPACE = "clearWorkspace";

	/**
	 * Clear workspace.
	 *
	 * @param clear
	 *            the clear
	 */
	public static void clearWorkspace(final boolean clear) {
		getStore().putBoolean(CLEAR_WORKSPACE, clear);
	}

}

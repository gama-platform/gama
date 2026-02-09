/*******************************************************************************************************
 *
 * IWorkspaceManager.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import java.io.IOException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;

/**
 *
 */
public interface IWorkspaceManager {

	/** The Constant CLEAR_WORKSPACE. */
	String CLEAR_WORKSPACE = "clearWorkspace";
	/**
	 *
	 */
	String GAMA_LIBRARY_MODELS = "platform:/plugin/gama.library/models/";
	/** The Constant KEY_WORSPACE_PATH. */
	String KEY_WORSPACE_PATH = "pref_workspace_path";
	/** The Constant KEY_WORKSPACE_REMEMBER. */
	String KEY_WORKSPACE_REMEMBER = "pref_workspace_remember";
	/** The Constant KEY_WORKSPACE_LIST. */
	String KEY_WORKSPACE_LIST = "pref_workspace_list";
	/** The Constant KEY_ASK_REBUILD. */
	String KEY_ASK_REBUILD = "pref_ask_rebuild";
	/** The Constant KEY_ASK_OUTDATED. */
	String KEY_ASK_OUTDATED = "pref_ask_outdated";
	/** The Constant WORKSPACE_IDENTIFIER. */
	String WORKSPACE_IDENTIFIER = ".gama_application_workspace";

	/**
	 * Gets the workspace.
	 *
	 * @return the workspace
	 */
	IWorkspace getWorkspace();

	/**
	 * Gets the workspace root.
	 *
	 * @return the workspace root
	 */
	IWorkspaceRoot getRoot();

	/** The get workspace URI. */
	URI getWorkspaceURI();

	/**
	 * Gets the workspace location.
	 *
	 * @return the workspace location
	 */
	String getWorkspaceLocation();

	/**
	 * Gets the workspace path.
	 *
	 * @return the workspace path
	 */
	IPath getWorkspacePath();

	/**
	 * @param str
	 * @param b
	 * @param c
	 * @param cloning
	 * @return
	 */
	String checkWorkspaceDirectory(String str, boolean b, boolean c, boolean cloning);

	/**
	 * @return
	 */
	String getModelIdentifier();

	/**
	 * @param str
	 */
	void setLastSetWorkspaceDirectory(String str);

	/**
	 * @return
	 */
	String getLastSetWorkspaceDirectory();

	/**
	 * @param selection
	 */
	void isRememberWorkspace(boolean selection);

	/**
	 * @param string
	 */
	void setLastUsedWorkspaces(String string);

	/**
	 * @return
	 */
	boolean isRememberWorkspace();

	/**
	 * @return
	 */
	String getLastUsedWorkspaces();

	/**
	 * @return
	 * @throws IOException
	 */
	Object checkWorkspace() throws IOException;

	/**
	 *
	 */
	void forceWorkspaceRebuild();

	/**
	 * @param b
	 */
	void clearWorkspace(boolean b);

	/**
	 * @return
	 */
	String getCurrentGamaStampString();

}
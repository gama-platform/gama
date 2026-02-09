/*******************************************************************************************************
 *
 * WorkspaceActivator.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace;

import org.osgi.framework.BundleContext;

import gama.api.GAMA;
import gama.dependencies.GamaBundleActivator;
import gama.dev.DEBUG;
import gama.workspace.manager.WorkspaceManager;
import gama.workspace.metadata.FileMetaDataProvider;

/**
 * The GAMA Workspace bundle activator that initializes and configures workspace management
 * components within the GAMA modeling platform.
 * 
 * <p>This activator is responsible for bootstrapping the workspace infrastructure when the
 * bundle starts, including setting up the workspace manager and file metadata provider
 * that enable GAMA to manage model files, projects, and workspace resources.</p>
 * 
 * <p>The class extends {@link GamaBundleActivator} to participate in the GAMA bundle
 * lifecycle and provides access to the OSGi bundle context for other workspace components.</p>
 * 
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Initializing the workspace manager for project and file management</li>
 *   <li>Configuring the file metadata provider for resource indexing</li>
 *   <li>Providing access to the bundle context for other workspace services</li>
 * </ul>
 * 
 * @author GAMA Development Team
 * @since 2.0
 */
public class WorkspaceActivator extends GamaBundleActivator {

	/**
	 * Static initialization block that disables debug output during class loading.
	 * This ensures that the workspace initialization process doesn't generate
	 * unnecessary debug messages during bundle startup.
	 */
	static {
		DEBUG.OFF();
	}

	/**
	 * The OSGi bundle context for this workspace bundle.
	 * 
	 * <p>This context is stored during bundle initialization and provides access
	 * to OSGi framework services for other workspace components. It is used by
	 * workspace services to interact with the OSGi environment.</p>
	 * 
	 * @see #getContext()
	 */
	static BundleContext CONTEXT;

	/**
	 * Retrieves the OSGi bundle context for this workspace bundle.
	 * 
	 * <p>This method provides access to the bundle context that was stored during
	 * bundle initialization. The context can be used by other workspace components
	 * to access OSGi framework services and interact with other bundles.</p>
	 *
	 * @return the OSGi bundle context for this workspace bundle, or {@code null}
	 *         if the bundle has not been initialized yet
	 */
	public static BundleContext getContext() { return CONTEXT; }

	/**
	 * Initializes the GAMA workspace bundle components.
	 * 
	 * <p>This method is called during bundle startup to initialize core workspace
	 * functionality. It performs the following initialization tasks:</p>
	 * <ul>
	 *   <li>Stores the bundle context for later use by workspace services</li>
	 *   <li>Registers the workspace manager with the GAMA framework for project management</li>
	 *   <li>Registers the file metadata provider for resource indexing and tracking</li>
	 * </ul>
	 * 
	 * <p>These components are essential for GAMA's workspace management capabilities,
	 * including project creation, file organization, and metadata tracking.</p>
	 * 
	 * @param bundleContext the OSGi bundle context provided by the framework
	 */
	@Override
	public void initialize(final BundleContext bundleContext) {
		DEBUG.OUT("Starting GAMA Workspace bundle");
		// Store bundle context for use by other workspace components
		CONTEXT = bundleContext;
		// Register workspace manager for project and file management
		GAMA.setWorkspaceManager(WorkspaceManager.getInstance());
		// Register metadata provider for resource indexing
		GAMA.setMetadataProvider(FileMetaDataProvider.getInstance());
	}

	/**
	 * Utility method for manually loading the GAMA workspace bundle.
	 * 
	 * <p>This method provides a way to explicitly trigger loading of the workspace
	 * bundle, typically used during development or testing scenarios where manual
	 * bundle initialization is required.</p>
	 * 
	 * <p>The method simply outputs a debug message indicating that the workspace
	 * bundle is being loaded. The actual initialization logic is handled by the
	 * {@link #initialize(BundleContext)} method.</p>
	 */
	public static void load() {
		DEBUG.OUT("Loading GAMA Workspace Bundle");
	}

}

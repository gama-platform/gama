/*******************************************************************************************************
 *
 * AbstractModelLibraryRunner.java, in gama.headless, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.batch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IFile;

import com.google.inject.Injector;

import gama.api.GAMA;
import gama.api.constants.GamlFileExtension;
import gama.dev.DEBUG;
import gama.workspace.manager.WorkspaceModelsManager;
import gaml.compiler.validation.GamlModelBuilder;

/**
 * The Class AbstractModelLibraryRunner.
 */
public abstract class AbstractModelLibraryRunner {

	/**
	 * Creates the builder.
	 *
	 * @param injector
	 *            the injector
	 * @return the gaml model builder
	 */
	protected GamlModelBuilder createBuilder(final Injector injector) {
		return new GamlModelBuilder(injector);
	}

	/**
	 * Links the models library to the workspace exactly as the GUI does, so that the models are compiled in the same
	 * context: in particular, an import can then designate a project contributed by another plugin.
	 */
	protected void linkModelsLibraryToWorkspace() {
		WorkspaceModelsManager.instance.loadModelsLibrary();
	}

	/**
	 * Returns the URL under which the GUI sees a model of the library, i.e. the URL of the model in its project of the
	 * workspace, or the URL passed if the model does not belong to any project.
	 *
	 * @param fileURL
	 *            the file URL of the model
	 * @return the URL of the model in the workspace
	 */
	protected URL toWorkspaceURL(final URL fileURL) {
		try {
			final IFile[] files = GAMA.getWorkspaceManager().getRoot()
					.findFilesForLocationURI(new URI(fileURL.getProtocol(), fileURL.getPath(), null));
			for (final IFile file : files) {
				if (file.getProject().isOpen()) return new URL("platform:/resource" + file.getFullPath());
			}
		} catch (final URISyntaxException | MalformedURLException e) {
			DEBUG.ERR("Cannot find " + fileURL + " in the workspace", e);
		}
		return fileURL;
	}

	/**
	 * Checks if is model.
	 *
	 * @param url
	 *            the url
	 * @return true, if is model
	 */
	protected boolean isModel(final URL url) {
		return isModel(url.getFile());
	}

	/**
	 * Checks if is model.
	 *
	 * @param file
	 *            the file
	 * @return true, if is model
	 */
	protected boolean isModel(final String file) {
		return GamlFileExtension.isGaml(file) || GamlFileExtension.isExperiment(file);
	}

	/**
	 * Checks if is test.
	 *
	 * @param url
	 *            the url
	 * @return true, if is test
	 */
	protected boolean isTest(final URL url) {
		final String file = url.getFile();
		return isModel(file) && (file.contains("test") || file.contains("Test"));
	}

	/**
	 * Start.
	 *
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract int start() throws IOException;
}

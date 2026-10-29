/*******************************************************************************************************
 *
 * EmbeddedWorkspace.java, in gama.export, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import com.google.inject.Injector;
import gama.api.compilation.GamlCompilationError;
import gama.api.kernel.species.IModelSpecies;
import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.validation.GamlModelBuilder;

/** Registers and compiles models in a packaged workspace without requiring an editor. */
public final class EmbeddedWorkspace {

	/** The builder. */
	private GamlModelBuilder builder;

	/**
	 * Register projects.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws CoreException
	 *             the core exception
	 */
	public void registerProjects() throws IOException, CoreException {
		registerProjects(Path.of(ExportHelper.toAbsoluteFromEmbeddedWorkspacePath("")));
	}

	public void registerProjects(final Path workspacePath) throws IOException, CoreException {
		final var workspace = ResourcesPlugin.getWorkspace();
		try (Stream<Path> paths = Files.list(workspacePath)) {
			for (final Path path : (Iterable<Path>) paths::iterator) {
				final Path metadata = path.resolve(".project");
				if (!Files.isRegularFile(metadata)) { continue; }
				final var description = workspace
						.loadProjectDescription(org.eclipse.core.runtime.Path.fromOSString(metadata.toString()));
				final var project = workspace.getRoot().getProject(description.getName());
				final Path location = path.toRealPath();
				if (project.exists()) {
					if (project.getLocation() == null
							|| !project.getLocation().toFile().toPath().toRealPath().equals(location))
						throw new IOException("A different project is already registered as " + description.getName());
				} else {
					final Path defaultLocation = workspace.getRoot().getLocation().toFile().toPath()
							.toRealPath().resolve(description.getName());
					description.setLocation(defaultLocation.equals(location) ? null
							: org.eclipse.core.runtime.Path.fromOSString(location.toString()));
					project.create(description, null);
				}
				if (!project.isOpen()) { project.open(null); }
			}
		}
	}

	/**
	 * Compile.
	 *
	 * @param modelPath
	 *            the model path
	 * @return the i model species
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public IModelSpecies compile(final String modelPath) throws IOException {
		if (builder == null) {
			builder = Platform.getBundle("gama.ui.editor") == null ? new GamlModelBuilder(GamlStandaloneSetup.doSetup())
					: GamlModelBuilder.getInstance();
		}
		final var errors = new ArrayList<GamlCompilationError>();
		final IModelSpecies model = builder.compile(URI.createPlatformResourceURI(modelPath, true), errors);
		if (model == null) throw new IOException("Unable to compile the startup model: " + errors);
		return model;
	}
}

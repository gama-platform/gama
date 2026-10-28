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

import gama.api.compilation.GamlCompilationError;
import gama.api.kernel.species.IModelSpecies;
import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.validation.GamlModelBuilder;

/** Registers and compiles models in a packaged workspace without requiring an editor. */
public final class EmbeddedWorkspace {

	private GamlModelBuilder builder;

	public void registerProjects() throws IOException, CoreException {
		final var workspace = ResourcesPlugin.getWorkspace();
		try (Stream<Path> paths = Files.list(Path.of(ExportHelper.toAbsoluteFromEmbeddedWorkspacePath("")))) {
			for (final Path path : (Iterable<Path>) paths::iterator) {
				final Path metadata = path.resolve(".project");
				if (!Files.isRegularFile(metadata)) continue;
				final var description = workspace.loadProjectDescription(
						org.eclipse.core.runtime.Path.fromOSString(metadata.toString()));
				final var project = workspace.getRoot().getProject(description.getName());
				if (!project.exists()) project.create(description, null);
				if (!project.isOpen()) project.open(null);
			}
		}
	}

	public IModelSpecies compile(final String modelPath) throws IOException {
		if (builder == null) {
			builder = Platform.getBundle("gama.ui.editor") == null
					? new GamlModelBuilder(GamlStandaloneSetup.doSetup()) : GamlModelBuilder.getInstance();
		}
		final var errors = new ArrayList<GamlCompilationError>();
		final IModelSpecies model = builder.compile(URI.createPlatformResourceURI(modelPath, true), errors);
		if (model == null) throw new IOException("Unable to compile the startup model: " + errors);
		return model;
	}
}

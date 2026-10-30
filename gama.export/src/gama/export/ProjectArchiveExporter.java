package gama.export;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;

import gama.api.runtime.SystemInfo;
import gama.api.utils.GamlProperties;
import gaml.compiler.validation.GamlModelBuilder;

/** Exports a complete editable project, including unfinished models with explicit discovery warnings. */
public final class ProjectArchiveExporter {

	public record Plan(IProject project, List<SimulationPackageExporter.Dependency> dependencies, List<String> warnings) {
		public boolean hasExtraPlugins() { return dependencies.stream().anyMatch(d -> !d.standard()); }
	}

	private ProjectArchiveExporter() {}

	public static Plan plan(final IProject project, final IProgressMonitor monitor) throws CoreException, IOException {
		if (!project.isAccessible()) throw new IOException("Open the project before exporting it");
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		final Set<String> plugins = new HashSet<>();
		final var warnings = new ArrayList<String>();
		discover(project, plugins, warnings, new HashSet<>(), monitor);
		for (final var reference : project.getReferencedProjects())
			warnings.add("Referenced project must be imported separately: " + reference.getName());
		final var dependencies = new ArrayList<SimulationPackageExporter.Dependency>();
		for (final var d : SimulationPackageExporter.dependencies(plugins, monitor)) {
			if (!d.standard() && d.file() != null && Files.isRegularFile(d.file())) {
				ProjectPlugins.validate(d.file(), new SimulationPackageManifest.BundleRequirement(d.id(), d.version(),
						"plugins/" + d.id() + "_" + d.version() + ".jar"));
				dependencies.add(new SimulationPackageExporter.Dependency(d.id(), d.version(), d.file(), true, false, ""));
			} else {
				dependencies.add(d);
			}
		}
		return new Plan(project, List.copyOf(dependencies), List.copyOf(warnings));
	}

	private static void discover(final IContainer container, final Set<String> plugins, final List<String> warnings,
			final Set<Path> ancestors, final IProgressMonitor monitor) throws CoreException, IOException {
		if (container.getLocation() == null) throw new IOException("Non-local project container");
		final Path location = container.getLocation().toFile().toPath();
		if (!Files.exists(location) || !ancestors.add(location.toRealPath())) {
			warnings.add("Plugin discovery incomplete for linked directory " + container.getProjectRelativePath());
			return;
		}
		try {
			for (final IResource resource : container.members()) {
				checkCanceled(monitor);
				if (resource instanceof IContainer child) {
					discover(child, plugins, warnings, ancestors, monitor);
				} else if (resource instanceof IFile file && ("gaml".equalsIgnoreCase(file.getFileExtension())
						|| "experiment".equalsIgnoreCase(file.getFileExtension()))) {
					final var model = GamlModelBuilder.getInstance()
							.compile(URI.createPlatformResourceURI(file.getFullPath().toString(), true), null);
					if (model == null) {
						warnings.add("Plugin discovery incomplete for " + file.getProjectRelativePath()
								+ ": the model does not compile. Project files can still be exported.");
					} else {
						try {
							final var properties = new GamlProperties();
							model.getDescription().collectMetaInformation(properties);
							final var required = properties.get(GamlProperties.PLUGINS);
							if (required != null) plugins.addAll(required);
						} finally { model.dispose(); }
					}
				}
			}
		} finally { ancestors.remove(location.toRealPath()); }
	}

	public static void export(final Plan plan, final boolean resolveLinks, final boolean includePlugins,
			final Path destination, final IProgressMonitor monitor) throws IOException, CoreException {
		final IProject project = plan.project();
		if (project.getLocation() == null) throw new IOException("Project has no local location");
		final Path source = project.getLocation().toFile().toPath().toRealPath();
		final Path output = destination.toAbsolutePath().normalize();
		Path ancestor = output.getParent();
		if (ancestor == null) throw new IOException("Choose a destination directory");
		while (!Files.exists(ancestor)) ancestor = ancestor.getParent();
		final Path realOutput = ancestor.toRealPath().resolve(ancestor.relativize(output));
		if (realOutput.startsWith(source)) throw new IOException("Save the archive outside the exported project");
		if (includePlugins && plan.warnings().stream().anyMatch(w -> w.startsWith("Plugin discovery incomplete")))
			throw new IOException("Correct model compilation errors before exporting required plugins");
		final Path staging = Files.createTempDirectory("gama-project-export-");
		monitor.beginTask("Exporting GAMA project", IProgressMonitor.UNKNOWN);
		try {
			final var links = new ArrayList<String>();
			collect(project, staging.resolve("workspace").resolve(project.getName()), resolveLinks, links,
					new HashSet<>(), realOutput, monitor);
			final var description = ProjectArchive.description(Files.newInputStream(source.resolve(".project")));
			// External projects can be registered under a different name from their on-disk description.
			final var children = description.getDocumentElement().getChildNodes();
			ProjectArchive.projectName(description);
			for (int i = 0; i < children.getLength(); i++)
				if ("name".equals(children.item(i).getNodeName())) children.item(i).setTextContent(project.getName());
			if (resolveLinks) {
				for (final String tag : List.of("linkedResources", "variableList", "filteredResources")) {
					final var nodes = description.getElementsByTagName(tag);
					while (nodes.getLength() > 0) nodes.item(0).getParentNode().removeChild(nodes.item(0));
				}
			}
			writeDescription(description, staging.resolve("workspace").resolve(project.getName()).resolve(".project"));
			final var requirements = new ArrayList<SimulationPackageManifest.BundleRequirement>();
			for (final var dependency : plan.dependencies()) {
				checkCanceled(monitor);
				if (dependency.standard()) continue;
				String artifact = "";
				if (includePlugins) {
					if (!dependency.embeddable()) throw new IOException("Cannot export plugin " + dependency.id() + ": " + dependency.reason());
					artifact = "plugins/" + dependency.id() + "_" + dependency.version() + ".jar";
					final Path target = staging.resolve(artifact);
					Files.createDirectories(target.getParent());
					PackageProjectStager.copy(dependency.file(), target, monitor::isCanceled);
				}
				requirements.add(new SimulationPackageManifest.BundleRequirement(dependency.id(), dependency.version(), artifact));
			}
			ProjectArchive.write(staging, output, new ProjectArchive.Info(List.of(project.getName()), SystemInfo.VERSION_NUMBER,
					resolveLinks || links.isEmpty(), links, requirements, plan.warnings(), 0, false), monitor::isCanceled);
		} finally {
			try { ProjectArchive.delete(staging); } finally { monitor.done(); }
		}
	}

	private static void collect(final IContainer container, final Path target, final boolean resolveLinks,
			final List<String> links, final Set<Path> ancestors, final Path output, final IProgressMonitor monitor)
			throws CoreException, IOException {
		checkCanceled(monitor);
		if (container.getLocation() == null) throw new IOException("Non-local resource: " + container.getFullPath());
		final Path location = container.getLocation().toFile().toPath();
		if (Files.isSymbolicLink(location) || !ancestors.add(location.toRealPath()))
			throw new IOException("Symbolic link or linked directory cycle: " + container.getFullPath());
		if (output.startsWith(location.toRealPath())) throw new IOException("The destination is inside an exported resource");
		try {
			Files.createDirectories(target);
			for (final IResource resource : container.members()) {
				checkCanceled(monitor);
				if (resource.getName().equals(".metadata")) continue;
				if (resource.isLinked()) {
					links.add(resource.getProjectRelativePath().toPortableString());
					if (!resolveLinks) continue;
				}
				final Path destination = target.resolve(resource.getName());
				if (resource instanceof IContainer child) {
					collect(child, destination, resolveLinks, links, ancestors, output, monitor);
				} else if (resource instanceof IFile file) {
					if (file.getLocation() == null) throw new IOException("Non-local file: " + file.getFullPath());
					final Path input = file.getLocation().toFile().toPath();
					if (!Files.isRegularFile(input) || Files.isSymbolicLink(input))
						throw new IOException("Missing or symbolic project file: " + input);
					if (input.toRealPath().equals(output)) throw new IOException("The archive would overwrite a project resource");
					PackageProjectStager.copy(input, destination, monitor::isCanceled);
				}
			}
		} finally { ancestors.remove(location.toRealPath()); }
	}

	public static void writeDescription(final org.w3c.dom.Document document, final Path destination) throws IOException {
		try {
			final var factory = TransformerFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			final var writer = new StringWriter();
			factory.newTransformer().transform(new DOMSource(document), new StreamResult(writer));
			Files.writeString(destination, writer.toString());
		} catch (final javax.xml.transform.TransformerException e) { throw new IOException("Cannot write project description", e); }
	}

	private static void checkCanceled(final IProgressMonitor monitor) {
		if (monitor.isCanceled()) throw new OperationCanceledException();
	}
}

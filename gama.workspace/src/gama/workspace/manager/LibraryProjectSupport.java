package gama.workspace.manager;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import gama.api.GAMA;

/** Workspace-owned provenance, also readable while a project is closed. */
final class LibraryProjectSupport {

	static final QualifiedName SOURCE = new QualifiedName("gama.builtin", "source");
	static final QualifiedName CATEGORY = new QualifiedName("gama.builtin", "category");
	private static final QualifiedName LOCATION = new QualifiedName("gama.builtin", "location");

	private LibraryProjectSupport() {}

	private static QualifiedName workspaceKey(final IProject project, final QualifiedName property) {
		return new QualifiedName("gama.builtin." + project.getName(), property.getLocalName());
	}

	static String propertyOf(final IProject project, final QualifiedName property) throws CoreException {
		if (!project.exists()) return null;
		final IWorkspaceRoot root = GAMA.getWorkspaceManager().getRoot();
		final String location = root.getPersistentProperty(workspaceKey(project, LOCATION));
		if (project.getLocation() != null && project.getLocation().toPortableString().equals(location)) {
			final String value = root.getPersistentProperty(workspaceKey(project, property));
			if (value != null) return value;
		}
		// Eclipse rejects project persistent-property reads when the project is closed.
		if (!project.isOpen()) return null;
		try {
			return project.getPersistentProperty(property);
		} catch (final CoreException e) {
			if (!project.isOpen()) return null;
			throw e;
		}
	}

	static boolean isBuiltIn(final IProject project) throws CoreException {
		if (propertyOf(project, SOURCE) != null
				|| propertyOf(project, WorkspaceModelsManager.BUILTIN_PROPERTY) != null) return true;
		if (!project.isOpen()) return false;
		for (final String nature : project.getDescription().getNatureIds()) {
			if ("gama.workspace.builtinNature".equals(nature) || "gama.workspace.pluginNature".equals(nature)
					|| "msi.gama.application.builtinNature".equals(nature)
					|| "msi.gama.application.pluginNature".equals(nature)) return true;
		}
		return false;
	}

	static ProjectCategory categoryOf(final IProject project) throws CoreException {
		final String category = propertyOf(project, CATEGORY);
		if (category != null) {
			for (final ProjectCategory c : ProjectCategory.values()) {
				if (c != ProjectCategory.USER && c.name().equals(category)) return c;
			}
		}
		return isBuiltIn(project) ? ProjectCategory.LIBRARY : ProjectCategory.USER;
	}

	static void remember(final IProject project, final String source, final ProjectCategory category)
			throws CoreException {
		project.setPersistentProperty(SOURCE, source);
		project.setPersistentProperty(CATEGORY, category.name());
		final IWorkspaceRoot root = GAMA.getWorkspaceManager().getRoot();
		root.setPersistentProperty(workspaceKey(project, SOURCE), source);
		root.setPersistentProperty(workspaceKey(project, CATEGORY), category.name());
		root.setPersistentProperty(workspaceKey(project, LOCATION), project.getLocation().toPortableString());
	}

	static void forget(final IProject project) throws CoreException {
		final IWorkspaceRoot root = GAMA.getWorkspaceManager().getRoot();
		for (final QualifiedName property : new QualifiedName[] { SOURCE, CATEGORY, LOCATION,
				WorkspaceModelsManager.BUILTIN_PROPERTY }) {
			root.setPersistentProperty(workspaceKey(project, property), null);
			if (project.isOpen()) { project.setPersistentProperty(property, null); }
		}
	}
}

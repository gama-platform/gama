package gama.workspace.manager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;

import gama.api.GAMA;
import gama.api.additions.GamaBundleLoader;
import gama.api.additions.GamaBundleLoader.ModelFolder;

/**
 * The categories under which the projects of the workspace are classified in the top-level folders of the navigator.
 *
 * <p>
 * The category of a project is derived from its <em>physical location</em> only:
 * </p>
 * <ul>
 * <li>a project located in {@code <workspace>/Tests/<project>} is a user test project ({@link #TESTS}). This
 * directory is the counterpart, in the workspace, of the {@code models/Tests} folder of the plugins. It only exists on
 * disk: it must not be mistaken with the virtual "Tests" folder of the navigator, which displays both kinds of tests.
 * For this reason, no user project can be named {@code Tests} (see {@link #isReservedName(String)});</li>
 * <li>a project located in the {@code models/<Category>} folder of a plugin is classified in this category, or in
 * {@link #LIBRARY} if the sub-folder is not the name of a category;</li>
 * <li>any other project is a {@link #USER} project.</li>
 * </ul>
 */
public enum ProjectCategory {

	/** Models related to data management. */
	DATA("Data"),
	/** Full models, toy or serious. */
	DEMOS("Demos"),
	/** All the models that are not otherwise classified. */
	LIBRARY("Library"),
	/** How to do and write things. */
	RECIPES("Recipes"),
	/** Step by step learning of GAMA. */
	TUTORIALS("Tutorials"),
	/** Unit and regression tests, in plugins or in the workspace. */
	TESTS("Tests"),
	/** The projects created by the user. */
	USER("User");

	/** The name of the directory of the workspace that contains the test projects of the user. */
	public static final String USER_TESTS_FOLDER = "Tests";

	/** A root folder contributed by a plugin, with its category. */
	private record Root(String path, ProjectCategory category) {}

	/** The folders contributed by the plugins, computed lazily. */
	private static volatile List<Root> roots;

	/** The name of the category. */
	private final String label;

	ProjectCategory(final String label) {
		this.label = label;
	}

	/**
	 * Returns the name of the category, as displayed in the navigator.
	 *
	 * @return the name
	 */
	public String getLabel() { return label; }

	/**
	 * Returns the category corresponding to a sub-folder name of {@code models}.
	 *
	 * @param name
	 *            the name of the sub-folder
	 * @return the category, {@link #LIBRARY} if the name is not the one of a category
	 */
	public static ProjectCategory ofFolder(final String name) {
		if (name != null) {
			for (final ProjectCategory c : values()) {
				if (c != USER && c != LIBRARY && c.label.equals(name)) return c;
			}
		}
		return LIBRARY;
	}

	/**
	 * Checks whether a name cannot be used for a user project, because it is the name of a top-level folder of the
	 * navigator or of the directory of the workspace that contains the user test projects.
	 *
	 * @param name
	 *            the name of the project
	 * @return true if the name is reserved
	 */
	public static boolean isReservedName(final String name) {
		if (name == null) return false;
		for (final ProjectCategory c : values()) { if (c.label.equalsIgnoreCase(name.trim())) return true; }
		return false;
	}

	/**
	 * Forgets the folders contributed by the plugins. To be called when they have changed.
	 */
	public static void reset() {
		roots = null;
	}

	/**
	 * Returns the category of a project.
	 *
	 * @param project
	 *            the project
	 * @return its category, {@link #USER} if it cannot be determined
	 */
	public static ProjectCategory of(final IProject project) {
		return project == null ? USER : of(project.getLocation());
	}

	/**
	 * Returns the category of a project located at a given place.
	 *
	 * @param location
	 *            the location of the project
	 * @return its category, {@link #USER} if it cannot be determined
	 */
	public static ProjectCategory of(final IPath location) {
		if (location == null) return USER;
		final String path = normalize(location.toFile());
		final String workspace = workspacePath();
		if (workspace != null && isIn(path, workspace + "/" + USER_TESTS_FOLDER)) return TESTS;
		for (final Root root : getRoots()) { if (path.equals(root.path()) || isIn(path, root.path())) return root.category(); }
		return USER;
	}

	/**
	 * Returns the group of a project, i.e. the path of the directory containing it relative to the root of its
	 * category (the {@code models/<Category>} folder of a plugin, the {@code Tests} directory of the workspace, or the
	 * workspace itself for user projects). The path is empty when the project is not in a group.
	 *
	 * @param location
	 *            the location of the project
	 * @return the relative path of the group, never {@code null}
	 */
	public static IPath groupOf(final IPath location) {
		if (location == null) return org.eclipse.core.runtime.Path.EMPTY;
		final String path = normalize(location.toFile());
		String base = null;
		final String workspace = workspacePath();
		if (workspace != null && isIn(path, workspace + "/" + USER_TESTS_FOLDER)) {
			base = workspace + "/" + USER_TESTS_FOLDER;
		} else {
			for (final Root root : getRoots()) {
				if (path.equals(root.path()) || isIn(path, root.path())) {
					base = root.path();
					break;
				}
			}
			if (base == null) { base = workspace; }
		}
		if (base == null || !isIn(path, base)) return org.eclipse.core.runtime.Path.EMPTY;
		return new org.eclipse.core.runtime.Path(path.substring(base.length() + 1)).removeLastSegments(1);
	}

	/**
	 * Checks if a path is strictly inside a directory.
	 */
	private static boolean isIn(final String path, final String directory) {
		return path.length() > directory.length() && path.startsWith(directory)
				&& path.charAt(directory.length()) == '/';
	}

	/**
	 * Returns the normalized path of the workspace.
	 */
	private static String workspacePath() {
		try {
			final IPath p = GAMA.getWorkspaceManager().getRoot().getLocation();
			return p == null ? null : normalize(p.toFile());
		} catch (final RuntimeException e) {
			return null;
		}
	}

	/**
	 * Normalizes a file to a path with forward slashes and without trailing separator.
	 */
	private static String normalize(final File file) {
		String s = file.getAbsolutePath().replace('\\', '/');
		while (s.length() > 1 && s.endsWith("/")) { s = s.substring(0, s.length() - 1); }
		return s;
	}

	/**
	 * Gets the folders contributed by the plugins, with their category.
	 */
	private static List<Root> getRoots() {
		List<Root> result = roots;
		if (result != null) return result;
		result = new ArrayList<>();
		for (final ModelFolder folder : GamaBundleLoader.getModelFolders()) {
			final URL url = folder.bundle().getEntry(folder.path());
			if (url == null) { continue; }
			try {
				final URL resolved = FileLocator.toFileURL(url);
				final URI uri = new URI(resolved.getProtocol(), resolved.getPath(), null).normalize();
				result.add(new Root(normalize(new File(uri)), ofFolder(folder.category())));
			} catch (final URISyntaxException | IOException e) {
				// The folder cannot be resolved: its projects are considered as user projects
			}
		}
		// Only cache a non-empty result: the plugins may not have been scanned yet
		if (!result.isEmpty()) { roots = result; }
		return result;
	}

}

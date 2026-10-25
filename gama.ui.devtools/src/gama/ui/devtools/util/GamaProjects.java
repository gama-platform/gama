/*******************************************************************************************************
 *
 * GamaProjects.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Knowledge about the layout of the GAMA workspace: which projects are GAMA plugins (i.e. run the GAML annotation
 * processor), where the processor lives, etc.
 */
public final class GamaProjects {

	/** The name of the project holding the annotation processor. */
	public static final String PROCESSOR_PROJECT = "gama.processor";

	/** The name of the annotations project. */
	public static final String ANNOTATIONS_PROJECT = "gama.annotations";

	/** The name of the processor jar, relative to the processor project. */
	public static final String PROCESSOR_JAR = "gama.processor.jar";

	/** The workspace path used in {@code .factorypath} files. */
	public static final String PROCESSOR_WKSP_PATH = "/" + PROCESSOR_PROJECT + "/" + PROCESSOR_JAR;

	/** The folder where the annotation processor generates its sources. */
	public static final String GEN_SRC_DIR = "gaml";

	/** The parent Maven project. */
	public static final String PARENT_PROJECT = "gama.parent";

	/** The Maven nature, used to detect Maven-only projects. */
	public static final String MAVEN_NATURE = "org.eclipse.m2e.core.maven2Nature";

	private GamaProjects() {}

	/**
	 * Returns true if the project is open and runs (or should run) the GAML annotation processor.
	 */
	public static boolean isGamaPlugin(final IProject p) {
		if (p == null || !p.isOpen()) return false;
		try {
			if (!p.hasNature(JavaCore.NATURE_ID)) return false;
			String fp = ResourceUtils.read(p, ".factorypath");
			if (fp != null && fp.contains(PROCESSOR_PROJECT)) return true;
			String px = ResourceUtils.read(p, "plugin.xml");
			return px != null && px.contains("\"gaml.extension\"");
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * All the open GAMA plugins of the workspace, sorted by name.
	 */
	public static List<IProject> all() {
		List<IProject> result = new ArrayList<>();
		for (IProject p : root().getProjects()) { if (isGamaPlugin(p)) { result.add(p); } }
		result.sort((a, b) -> a.getName().compareTo(b.getName()));
		return result;
	}

	/**
	 * The GAMA plugins designated by the selection, or all of them if the selection does not contain any.
	 */
	public static List<IProject> fromSelectionOrAll(final ISelection selection) {
		List<IProject> selected = fromSelection(selection);
		return selected.isEmpty() ? all() : selected;
	}

	/**
	 * The GAMA plugins designated by the selection (possibly empty).
	 */
	public static List<IProject> fromSelection(final ISelection selection) {
		Set<IProject> result = new LinkedHashSet<>();
		if (selection instanceof IStructuredSelection ss) {
			for (Object o : ss.toList()) {
				IResource r = Adapters.adapt(o, IResource.class);
				if (r != null && isGamaPlugin(r.getProject())) { result.add(r.getProject()); }
			}
		}
		return new ArrayList<>(result);
	}

	/**
	 * The first project (GAMA or not) designated by the selection, or null.
	 */
	public static IProject firstProject(final ISelection selection) {
		if (selection instanceof IStructuredSelection ss) {
			for (Object o : ss.toList()) {
				IResource r = Adapters.adapt(o, IResource.class);
				if (r != null) return r.getProject();
			}
		}
		return null;
	}

	/**
	 * The processor project, or null if it is not in the workspace.
	 */
	public static IProject processor() {
		IProject p = root().getProject(PROCESSOR_PROJECT);
		return p.exists() ? p : null;
	}

	/**
	 * Returns true if the project is only a Maven aggregator (like gama.parent) and must not be built by Eclipse.
	 */
	public static boolean isMavenOnly(final IProject p) {
		try {
			String[] natures = p.getDescription().getNatureIds();
			return natures.length == 0 || Arrays.stream(natures).allMatch(MAVEN_NATURE::equals);
		} catch (CoreException e) {
			return true;
		}
	}

	/**
	 * The "shortcut" of a plugin, i.e. the last segment of its name, used by the processor to name the generated
	 * package ({@code gaml.additions.<shortcut>}).
	 */
	public static String shortcut(final String pluginName) {
		int i = pluginName.lastIndexOf('.');
		return i < 0 ? pluginName : pluginName.substring(i + 1);
	}

	/**
	 * The project-relative path of the {@code GamlAdditions} class generated for this plugin.
	 */
	public static String generatedAdditionsPath(final IProject p) {
		return GEN_SRC_DIR + "/gaml/additions/" + shortcut(p.getName()) + "/GamlAdditions.java";
	}

	/**
	 * Returns the Java project corresponding to this project.
	 */
	public static IJavaProject java(final IProject p) {
		return JavaCore.create(p);
	}

	/**
	 * The workspace root.
	 */
	public static IWorkspaceRoot root() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

}

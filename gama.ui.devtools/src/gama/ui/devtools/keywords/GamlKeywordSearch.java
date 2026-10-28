/*******************************************************************************************************
 *
 * GamlKeywordSearch.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.keywords;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import gama.ui.devtools.keywords.GamlKeywordIndex.Entry;
import gama.ui.devtools.keywords.GamlKeywordIndex.Source;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Maintains a {@link GamlKeywordIndex} of the Java sources of the open Java projects of the workspace. The index is
 * refreshed incrementally (only the modified files are parsed again) each time it is queried.
 */
public final class GamlKeywordSearch {

	private static final GamlKeywordIndex INDEX = new GamlKeywordIndex();

	private GamlKeywordSearch() {}

	/**
	 * Refreshes the index with the current state of the workspace and returns its entries.
	 */
	public static List<Entry> refresh(final IProgressMonitor monitor) {
		SubMonitor sub = SubMonitor.convert(monitor, "Indexing the GAML keywords", 2);
		List<Source> sources = collect(sub.split(1));
		sub.subTask("Parsing the annotated Java files");
		INDEX.update(sources);
		sub.worked(1);
		return INDEX.entries();
	}

	/** The file corresponding to the path of an entry, or null. */
	public static IFile file(final Entry entry) {
		IResource r = GamaProjects.root().findMember(IPath.fromPortableString(entry.path()));
		return r instanceof IFile f ? f : null;
	}

	private static List<Source> collect(final IProgressMonitor monitor) {
		List<Source> sources = new ArrayList<>();
		IProject[] projects = GamaProjects.root().getProjects();
		SubMonitor sub = SubMonitor.convert(monitor, "Collecting the Java sources", projects.length);
		for (IProject p : projects) {
			sub.split(1);
			try {
				if (!p.isOpen() || !p.hasNature(JavaCore.NATURE_ID)) { continue; }
				IJavaProject jp = JavaCore.create(p);
				IPath generated = p.getFullPath().append(GamaProjects.GEN_SRC_DIR);
				for (IClasspathEntry e : jp.getRawClasspath()) {
					if (e.getEntryKind() != IClasspathEntry.CPE_SOURCE || generated.isPrefixOf(e.getPath())) {
						continue;
					}
					IResource folder = GamaProjects.root().findMember(e.getPath());
					if (folder instanceof IContainer c) { visit(c, p.getName(), sources); }
				}
			} catch (CoreException e) {
				ResourceUtils.log(e);
			}
		}
		return sources;
	}

	private static void visit(final IContainer container, final String plugin, final List<Source> sources)
			throws CoreException {
		container.accept(proxy -> {
			if (proxy.getType() == IResource.FILE) {
				if (proxy.getName().endsWith(".java")) {
					IFile file = (IFile) proxy.requestResource();
					sources.add(new Source(file.getFullPath().toPortableString(), plugin, file.getModificationStamp(),
							() -> {
								try {
									return ResourceUtils.read(file);
								} catch (CoreException e) {
									return null;
								}
							}));
				}
				return false;
			}
			return true;
		}, IResource.NONE);
	}

}

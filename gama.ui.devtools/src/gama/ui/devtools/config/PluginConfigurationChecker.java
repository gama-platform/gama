/*******************************************************************************************************
 *
 * PluginConfigurationChecker.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.config;

import static gama.ui.devtools.config.ConfigurationIssue.Kind.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import gama.ui.devtools.config.ConfigurationIssue.Kind;
import gama.ui.devtools.templates.CodeTemplates;
import gama.ui.devtools.util.BuildPropertiesText;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.ManifestText;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Checks (and repairs) the configuration of GAMA plugins: annotation processing, generated source folder, factory
 * path, classpath, build.properties, plugin.xml, libraries, etc. The reference layout is the one produced by the
 * "New GAMA Plugin" wizard (see {@link CodeTemplates}).
 */
public final class PluginConfigurationChecker {

	/** The type of the markers created for configuration issues (declared in plugin.xml). */
	public static final String MARKER = "gama.ui.devtools.configurationProblem";

	/** The marker attribute holding the {@link Kind} name. */
	public static final String KIND_ATTRIBUTE = "gamaIssueKind";

	private PluginConfigurationChecker() {}

	// ------------------------------------------------------------------------------------------------------------
	// Checks
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Checks the given projects, as well as the global configuration (processor, unique shortcuts).
	 */
	public static List<ConfigurationIssue> check(final Collection<IProject> projects, final IProgressMonitor monitor) {
		SubMonitor sub = SubMonitor.convert(monitor, "Checking GAMA plugins", projects.size() + 1);
		List<ConfigurationIssue> issues = new ArrayList<>();
		checkProcessor(issues);
		checkShortcuts(projects, issues);
		sub.worked(1);
		for (IProject p : projects) {
			sub.subTask(p.getName());
			issues.addAll(check(p));
			sub.worked(1);
		}
		return issues;
	}

	/**
	 * Checks a single project.
	 */
	public static List<ConfigurationIssue> check(final IProject p) {
		List<ConfigurationIssue> issues = new ArrayList<>();
		try {
			IJavaProject jp = GamaProjects.java(p);
			if (!AptConfig.isEnabled(jp)) { add(issues, p, APT_DISABLED, "Annotation processing is disabled"); }
			if (!GamaProjects.GEN_SRC_DIR.equals(AptConfig.getGenSrcDir(jp))) {
				add(issues, p, APT_GEN_DIR, "Generated source directory is '" + AptConfig.getGenSrcDir(jp)
						+ "' instead of '" + GamaProjects.GEN_SRC_DIR + "'");
			}
			if (AptConfig.shouldProcessDuringReconcile(jp)) {
				add(issues, p, APT_RECONCILE, "Annotation processing during reconcile (editing) is enabled");
			}
			String fp = ResourceUtils.read(p, ".factorypath");
			if (fp == null || !fp.contains(GamaProjects.PROCESSOR_WKSP_PATH)) {
				add(issues, p, FACTORY_PATH, ".factorypath does not reference " + GamaProjects.PROCESSOR_WKSP_PATH);
			}
			if (!hasSourceEntry(jp, GamaProjects.GEN_SRC_DIR)) {
				add(issues, p, CLASSPATH_GAML, "'gaml' is not declared as a source folder in .classpath");
			}
			String bp = ResourceUtils.read(p, "build.properties");
			if (bp != null) {
				List<String> sources = BuildPropertiesText.getList(bp, "source..");
				if (!BuildPropertiesText.containsPath(sources, "gaml")
						|| p.getFolder("src").exists() && !BuildPropertiesText.containsPath(sources, "src")) {
					add(issues, p, BUILD_SOURCES, "build.properties 'source..' should contain 'src/' and 'gaml/'");
				}
				if (p.getFolder("models").exists()
						&& !BuildPropertiesText.containsPath(BuildPropertiesText.getList(bp, "bin.includes"), "models")) {
					add(issues, p, MODELS_NOT_EXPORTED, "'models/' is not exported in build.properties 'bin.includes'");
				}
			}
			String px = ResourceUtils.read(p, "plugin.xml");
			if (px == null || !px.contains("\"gaml.extension\"")) {
				add(issues, p, PLUGIN_XML, "plugin.xml does not declare the 'gaml.extension' extension");
			}
			String mf = ResourceUtils.read(p, "META-INF/MANIFEST.MF");
			if (mf != null) {
				String bsn = ManifestText.get(mf, "Bundle-SymbolicName");
				if (bsn != null) {
					String name = bsn.split(";", 2)[0].strip();
					if (!name.equals(p.getName())) {
						add(issues, p, NAME_MISMATCH, "The project name differs from the Bundle-SymbolicName '" + name
								+ "': the processor derives the plugin name from the project folder");
					}
					if (!bsn.replace(" ", "").contains("singleton:=true")) {
						add(issues, p, SINGLETON, "The bundle should be declared with 'singleton:=true'");
					}
				}
				for (String jar : missingLibs(p, mf, bp, jp)) {
					add(issues, p, LIB_NOT_DECLARED,
							"'" + jar + "' is not declared in Bundle-ClassPath, build.properties and/or .classpath");
				}
			}
			if (p.getDefaultCharset(false) == null) {
				add(issues, p, ENCODING, "The project has no explicit text encoding (should be UTF-8)");
			}
			if (!p.getFile(GamaProjects.generatedAdditionsPath(p)).exists()) {
				add(issues, p, ADDITIONS_MISSING, "'" + GamaProjects.generatedAdditionsPath(p)
						+ "' has not been generated. Run 'Clean and Rebuild GAMA Plugins'");
			}
		} catch (CoreException e) {
			ResourceUtils.log(e);
		}
		return issues;
	}

	private static void checkProcessor(final List<ConfigurationIssue> issues) {
		IProject proc = GamaProjects.processor();
		if (proc == null || !proc.isOpen()) {
			issues.add(new ConfigurationIssue(GamaProjects.root().getProject(GamaProjects.PROCESSOR_PROJECT),
					PROCESSOR_MISSING, "The project '" + GamaProjects.PROCESSOR_PROJECT
							+ "' is not open in the workspace: the GAML annotation processor cannot run"));
			return;
		}
		IFile jar = proc.getFile(GamaProjects.PROCESSOR_JAR);
		if (!jar.exists()) {
			issues.add(new ConfigurationIssue(proc, PROCESSOR_STALE,
					GamaProjects.PROCESSOR_JAR + " is missing. Use 'Rebuild Annotation Processor Jar'"));
			return;
		}
		long newest = Math.max(newestSource(proc.getFolder("src")),
				newestSource(GamaProjects.root().getProject(GamaProjects.ANNOTATIONS_PROJECT).getFolder("src")));
		if (newest > jar.getLocalTimeStamp()) {
			issues.add(new ConfigurationIssue(proc, PROCESSOR_STALE, GamaProjects.PROCESSOR_JAR
					+ " is older than the sources of the processor or of the annotations. Consider using 'Rebuild Annotation Processor Jar'"));
		}
	}

	private static long newestSource(final IFolder folder) {
		long[] max = { 0 };
		if (!folder.exists()) return 0;
		try {
			folder.accept(r -> {
				if (r.getType() == IResource.FILE && "java".equals(r.getFileExtension())) {
					max[0] = Math.max(max[0], r.getLocalTimeStamp());
				}
				return true;
			});
		} catch (CoreException e) {
			ResourceUtils.log(e);
		}
		return max[0];
	}

	private static void checkShortcuts(final Collection<IProject> projects, final List<ConfigurationIssue> issues) {
		Map<String, List<IProject>> byShortcut = new HashMap<>();
		for (IProject p : GamaProjects.all()) {
			byShortcut.computeIfAbsent(GamaProjects.shortcut(p.getName()), k -> new ArrayList<>()).add(p);
		}
		for (IProject p : projects) {
			List<IProject> same = byShortcut.getOrDefault(GamaProjects.shortcut(p.getName()), List.of());
			if (same.size() > 1) {
				issues.add(new ConfigurationIssue(p, DUPLICATE_SHORTCUT,
						"Several plugins end with '." + GamaProjects.shortcut(p.getName()) + "' " + names(same)
								+ ": their generated additions (gaml.additions." + GamaProjects.shortcut(p.getName())
								+ ") collide"));
			}
		}
	}

	private static String names(final List<IProject> ps) {
		return ps.stream().map(IProject::getName).toList().toString();
	}

	private static boolean hasSourceEntry(final IJavaProject jp, final String folder) throws CoreException {
		IPath path = jp.getProject().getFullPath().append(folder);
		for (IClasspathEntry e : jp.getRawClasspath()) {
			if (e.getEntryKind() == IClasspathEntry.CPE_SOURCE && e.getPath().equals(path)) return true;
		}
		return false;
	}

	/** The project-relative paths of the jars found under 'libs/'. */
	static List<String> libJars(final IProject p) {
		List<String> jars = new ArrayList<>();
		IFolder libs = p.getFolder("libs");
		if (!libs.exists()) return jars;
		try {
			libs.accept(r -> {
				if (r.getType() == IResource.FILE && "jar".equalsIgnoreCase(r.getFileExtension())) {
					jars.add(r.getProjectRelativePath().toString());
				}
				return true;
			});
		} catch (CoreException e) {
			ResourceUtils.log(e);
		}
		jars.sort(String::compareTo);
		return jars;
	}

	private static List<String> missingLibs(final IProject p, final String mf, final String bp, final IJavaProject jp)
			throws CoreException {
		List<String> missing = new ArrayList<>();
		List<String> jars = libJars(p);
		if (jars.isEmpty()) return missing;
		List<String> bundleCp = ManifestText.getNames(mf, "Bundle-ClassPath");
		List<String> bin = bp == null ? List.of() : BuildPropertiesText.getList(bp, "bin.includes");
		Set<IPath> cpLibs = new LinkedHashSet<>();
		for (IClasspathEntry e : jp.getRawClasspath()) {
			if (e.getEntryKind() == IClasspathEntry.CPE_LIBRARY) { cpLibs.add(e.getPath()); }
		}
		for (String jar : jars) {
			boolean inBin = BuildPropertiesText.containsPath(bin, jar) || BuildPropertiesText.containsPath(bin, "libs");
			if (!bundleCp.contains(jar) || !inBin || !cpLibs.contains(p.getFullPath().append(jar))) {
				missing.add(jar);
			}
		}
		return missing;
	}

	private static void add(final List<ConfigurationIssue> issues, final IProject p, final Kind kind,
			final String message) {
		issues.add(new ConfigurationIssue(p, kind, message));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Repair
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Repairs the fixable issues. Must be called from a workspace operation.
	 *
	 * @return the number of issues repaired
	 */
	public static int repair(final Collection<ConfigurationIssue> issues, final IProgressMonitor monitor)
			throws CoreException {
		Map<IProject, Set<Kind>> byProject = new HashMap<>();
		for (ConfigurationIssue i : issues) {
			if (i.kind().fixable) {
				byProject.computeIfAbsent(i.project(), k -> EnumSet.noneOf(Kind.class)).add(i.kind());
			}
		}
		SubMonitor sub = SubMonitor.convert(monitor, "Repairing GAMA plugins", byProject.size());
		int count = 0;
		for (Map.Entry<IProject, Set<Kind>> e : byProject.entrySet()) {
			sub.subTask(e.getKey().getName());
			count += repair(e.getKey(), e.getValue(), sub.split(1));
		}
		return count;
	}

	private static int repair(final IProject p, final Set<Kind> kinds, final IProgressMonitor monitor)
			throws CoreException {
		IJavaProject jp = GamaProjects.java(p);
		int count = 0;
		if (kinds.contains(APT_DISABLED)) {
			AptConfig.setEnabled(jp, true);
			count++;
		}
		if (kinds.contains(APT_GEN_DIR)) {
			AptConfig.setGenSrcDir(jp, GamaProjects.GEN_SRC_DIR);
			AptConfig.setGenTestSrcDir(jp, "-");
			count++;
		}
		if (kinds.contains(APT_RECONCILE)) {
			AptConfig.setProcessDuringReconcile(jp, false);
			count++;
		}
		if (kinds.contains(FACTORY_PATH)) {
			if (p.getFile(".factorypath").exists()) {
				IFactoryPath fp = AptConfig.getFactoryPath(jp);
				fp.addWkspJar(IPath.fromPortableString(GamaProjects.PROCESSOR_WKSP_PATH));
				AptConfig.setFactoryPath(jp, fp);
			} else {
				ResourceUtils.write(p, ".factorypath", CodeTemplates.factoryPath(), monitor);
			}
			count++;
		}
		if (kinds.contains(CLASSPATH_GAML)) {
			addGamlSourceEntry(jp, monitor);
			count++;
		}
		if (kinds.contains(BUILD_SOURCES) || kinds.contains(MODELS_NOT_EXPORTED)) {
			String bp = ResourceUtils.read(p, "build.properties");
			if (bp != null) {
				if (kinds.contains(BUILD_SOURCES)) {
					List<String> src = new ArrayList<>();
					if (p.getFolder("src").exists()) { src.add("src/"); }
					src.add("gaml/");
					bp = BuildPropertiesText.addAll(bp, "source..", src);
					count++;
				}
				if (kinds.contains(MODELS_NOT_EXPORTED)) {
					bp = BuildPropertiesText.addAll(bp, "bin.includes", List.of("models/"));
					count++;
				}
				ResourceUtils.write(p, "build.properties", bp, monitor);
			}
		}
		if (kinds.contains(PLUGIN_XML)) {
			String px = ResourceUtils.read(p, "plugin.xml");
			if (px == null) {
				px = CodeTemplates.pluginXml();
			} else {
				int i = px.lastIndexOf("</plugin>");
				String ext = "   <extension\n         point=\"gaml.extension\">\n   </extension>\n";
				px = i < 0 ? CodeTemplates.pluginXml() : px.substring(0, i) + ext + px.substring(i);
			}
			ResourceUtils.write(p, "plugin.xml", px, monitor);
			count++;
		}
		if (kinds.contains(SINGLETON)) {
			String mf = ResourceUtils.read(p, "META-INF/MANIFEST.MF");
			String bsn = ManifestText.get(mf, "Bundle-SymbolicName");
			String fixedBsn = bsn.strip();
			Matcher singleton = Pattern.compile("(?i)(;\\s*singleton\\s*:=\\s*)[^;]*").matcher(fixedBsn);
			fixedBsn = singleton.find() ? singleton.replaceFirst("$1true") : fixedBsn + ";singleton:=true";
			mf = ManifestText.setList(mf, "Bundle-SymbolicName", List.of(fixedBsn));
			ResourceUtils.write(p, "META-INF/MANIFEST.MF", mf, monitor);
			count++;
		}
		if (kinds.contains(LIB_NOT_DECLARED)) {
			declareLibs(p, jp, monitor);
			count++;
		}
		if (kinds.contains(ENCODING)) {
			p.setDefaultCharset("UTF-8", monitor);
			count++;
		}
		return count;
	}

	private static void addGamlSourceEntry(final IJavaProject jp, final IProgressMonitor monitor)
			throws CoreException {
		IFolder gaml = jp.getProject().getFolder(GamaProjects.GEN_SRC_DIR);
		IClasspathEntry entry = JavaCore.newSourceEntry(gaml.getFullPath(), new IPath[0], new IPath[0], null,
				new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.OPTIONAL, "true") });
		List<IClasspathEntry> entries = new ArrayList<>(List.of(jp.getRawClasspath()));
		int index = entries.size();
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getEntryKind() == IClasspathEntry.CPE_SOURCE) { index = i + 1; }
		}
		entries.add(index, entry);
		jp.setRawClasspath(entries.toArray(IClasspathEntry[]::new), monitor);
	}

	private static void declareLibs(final IProject p, final IJavaProject jp, final IProgressMonitor monitor)
			throws CoreException {
		List<String> jars = libJars(p);
		// MANIFEST.MF: Bundle-ClassPath, with '.' first
		String mf = ResourceUtils.read(p, "META-INF/MANIFEST.MF");
		List<String> cp = new ArrayList<>(ManifestText.getList(mf, "Bundle-ClassPath"));
		if (cp.stream().noneMatch(s -> ".".equals(s.split(";", 2)[0].strip()))) { cp.add(0, "."); }
		List<String> names = cp.stream().map(s -> s.split(";", 2)[0].strip()).toList();
		boolean changed = false;
		for (String jar : jars) {
			if (!names.contains(jar)) {
				cp.add(jar);
				changed = true;
			}
		}
		if (changed) { ResourceUtils.write(p, "META-INF/MANIFEST.MF", ManifestText.setList(mf, "Bundle-ClassPath", cp), monitor); }
		// build.properties: bin.includes
		String bp = ResourceUtils.read(p, "build.properties");
		if (bp != null) {
			List<String> bin = BuildPropertiesText.getList(bp, "bin.includes");
			if (!BuildPropertiesText.containsPath(bin, "libs")) {
				String nbp = BuildPropertiesText.addAll(bp, "bin.includes", jars);
				if (!nbp.equals(bp)) { ResourceUtils.write(p, "build.properties", nbp, monitor); }
			}
		}
		// .classpath: exported libraries, before the containers
		List<IClasspathEntry> entries = new ArrayList<>(List.of(jp.getRawClasspath()));
		Set<IPath> existing = new LinkedHashSet<>();
		for (IClasspathEntry e : entries) { if (e.getEntryKind() == IClasspathEntry.CPE_LIBRARY) { existing.add(e.getPath()); } }
		int index = 0;
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getEntryKind() == IClasspathEntry.CPE_LIBRARY) { index = i + 1; }
		}
		boolean cpChanged = false;
		for (String jar : jars) {
			IPath path = p.getFullPath().append(jar);
			if (!existing.contains(path)) {
				entries.add(index++, JavaCore.newLibraryEntry(path, null, null, true));
				cpChanged = true;
			}
		}
		if (cpChanged) { jp.setRawClasspath(entries.toArray(IClasspathEntry[]::new), monitor); }
	}

	// ------------------------------------------------------------------------------------------------------------
	// Markers
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Replaces the configuration markers of the given projects by markers describing the issues.
	 *
	 * @param global
	 *            whether the markers of the processor project (global issues) are refreshed too
	 */
	public static void updateMarkers(final Collection<IProject> projects, final boolean global,
			final Collection<ConfigurationIssue> issues) throws CoreException {
		Set<IProject> all = new LinkedHashSet<>(projects);
		IProject proc = GamaProjects.processor();
		if (global && proc != null && proc.isOpen()) { all.add(proc); }
		for (IProject p : all) { if (p.isOpen()) { p.deleteMarkers(MARKER, false, IResource.DEPTH_ZERO); } }
		for (ConfigurationIssue i : issues) {
			if (!i.project().isOpen() || !all.contains(i.project())) { continue; }
			IMarker m = i.project().createMarker(MARKER);
			m.setAttribute(IMarker.MESSAGE, "[GAMA] " + i.message());
			m.setAttribute(IMarker.SEVERITY, i.kind().severity);
			m.setAttribute(IMarker.LOCATION, i.project().getName());
			m.setAttribute(KIND_ATTRIBUTE, i.kind().name());
		}
	}

	/**
	 * Rebuilds the issue corresponding to a marker, or null if the marker is not a configuration marker.
	 */
	public static ConfigurationIssue fromMarker(final IMarker marker) {
		String kind = marker.getAttribute(KIND_ATTRIBUTE, null);
		if (kind == null || !(marker.getResource() instanceof IProject p)) return null;
		try {
			return new ConfigurationIssue(p, Kind.valueOf(kind), marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}

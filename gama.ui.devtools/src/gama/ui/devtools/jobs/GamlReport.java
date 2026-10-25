/*******************************************************************************************************
 *
 * GamlReport.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.jobs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Builds a Markdown report from the warnings emitted by the GAML annotation processor (which are prefixed with
 * "GAML: "): undocumented elements, untested operators and other warnings or errors.
 */
public final class GamlReport {

	private static final Pattern UNDOCUMENTED = Pattern.compile("documentation missing for (.*)");

	private static final Pattern UNTESTED = Pattern.compile("operator '(.*)' is not tested");

	private GamlReport() {}

	/** An entry of the report. */
	record Entry(String plugin, String element, String location) {}

	/**
	 * Builds the report for the given projects (which should have been built with annotation processing enabled).
	 */
	public static String build(final List<IProject> projects) throws CoreException {
		Map<String, List<Entry>> undocumented = new TreeMap<>();
		Map<String, List<Entry>> untested = new TreeMap<>();
		Map<String, List<Entry>> others = new TreeMap<>();
		for (IProject p : projects) {
			for (IMarker m : p.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
				String msg = m.getAttribute(IMarker.MESSAGE, "");
				int i = msg.indexOf("GAML: ");
				if (i < 0) { continue; }
				msg = msg.substring(i + 6).strip();
				String location = location(m);
				Matcher d = UNDOCUMENTED.matcher(msg);
				Matcher t = UNTESTED.matcher(msg);
				if (d.matches()) {
					add(undocumented, new Entry(p.getName(), d.group(1), location));
				} else if (t.matches()) {
					add(untested, new Entry(p.getName(), t.group(1), location));
				} else {
					String severity = m.getAttribute(IMarker.SEVERITY, 0) == IMarker.SEVERITY_ERROR ? "**error** "
							: "";
					add(others, new Entry(p.getName(), severity + msg, location));
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("# GAML documentation and tests report\n\n");
		sb.append("Generated on ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
				.append(" from the warnings of the GAML annotation processor on ").append(projects.size())
				.append(" plugin(s).\n\n");
		sb.append("> If this report is empty or incomplete, run **GAMA > Clean and Rebuild GAMA Plugins** first: the ")
				.append("warnings are only produced when the annotation processor runs.\n\n");
		sb.append("| Plugin | Undocumented | Untested operators | Other |\n|---|---:|---:|---:|\n");
		for (IProject p : projects) {
			String n = p.getName();
			int a = size(undocumented, n), b = size(untested, n), c = size(others, n);
			if (a + b + c > 0) { sb.append("| ").append(n).append(" | ").append(a).append(" | ").append(b)
					.append(" | ").append(c).append(" |\n"); }
		}
		sb.append("| **Total** | ").append(total(undocumented)).append(" | ").append(total(untested)).append(" | ")
				.append(total(others)).append(" |\n\n");
		section(sb, "Undocumented elements", undocumented);
		section(sb, "Untested operators",
				untested);
		sb.append("_An operator is considered tested if it has executable examples (`@example(value=..., equals=...)`), ")
				.append("`@test` / `@tests` annotations, or is explicitly marked `@no_test`._\n\n");
		section(sb, "Other processor messages", others);
		return sb.toString();
	}

	private static void section(final StringBuilder sb, final String title, final Map<String, List<Entry>> map) {
		sb.append("## ").append(title).append(" (").append(total(map)).append(")\n\n");
		if (map.isEmpty()) {
			sb.append("None.\n\n");
			return;
		}
		for (Map.Entry<String, List<Entry>> e : map.entrySet()) {
			sb.append("### ").append(e.getKey()).append(" (").append(e.getValue().size()).append(")\n\n");
			e.getValue().stream().sorted(Comparator.comparing(Entry::element)).forEach(
					x -> sb.append("- ").append(x.element()).append(" — `").append(x.location()).append("`\n"));
			sb.append("\n");
		}
	}

	private static void add(final Map<String, List<Entry>> map, final Entry e) {
		map.computeIfAbsent(e.plugin(), k -> new ArrayList<>()).add(e);
	}

	private static int size(final Map<String, List<Entry>> map, final String plugin) {
		return map.getOrDefault(plugin, List.of()).size();
	}

	private static int total(final Map<String, List<Entry>> map) {
		return map.values().stream().mapToInt(List::size).sum();
	}

	private static String location(final IMarker m) {
		String path = m.getResource().getProjectRelativePath().toString();
		int line = m.getAttribute(IMarker.LINE_NUMBER, -1);
		return line > 0 ? path + ":" + line : path;
	}

}

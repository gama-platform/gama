/*******************************************************************************************************
 *
 * WorkspaceIntegration.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.templates;

/**
 * Pure text transformations used to register a new plugin in the GAMA build: modules of {@code gama.parent/pom.xml},
 * plugins of {@code gama.feature.extensions/feature.xml} and features of {@code gama.p2site/category.xml}. All
 * methods return the text unchanged if the element is already present.
 */
public final class WorkspaceIntegration {

	private WorkspaceIntegration() {}

	/**
	 * Adds {@code <module>../name</module>} after the last module whose name starts with {@code afterPrefix} (or
	 * before {@code </modules>} if there is none).
	 */
	public static String addModule(final String pom, final String name, final String afterPrefix) {
		String module = "<module>../" + name + "</module>";
		if (pom.contains(module)) return pom;
		int anchor = pom.lastIndexOf("<module>../" + afterPrefix);
		if (anchor >= 0) {
			int eol = pom.indexOf('\n', anchor);
			int bol = pom.lastIndexOf('\n', anchor) + 1;
			String indent = pom.substring(bol, anchor);
			return pom.substring(0, eol + 1) + indent + module + "\n" + pom.substring(eol + 1);
		}
		int end = pom.indexOf("</modules>");
		if (end < 0) return pom;
		int bol = pom.lastIndexOf('\n', end) + 1;
		return pom.substring(0, bol) + "\t\t" + module + "\n" + pom.substring(bol);
	}

	/**
	 * Adds a {@code <plugin>} element at the end of a feature.
	 */
	public static String addPluginToFeature(final String feature, final String pluginId) {
		if (feature.contains("id=\"" + pluginId + "\"")) return feature;
		int end = feature.lastIndexOf("</feature>");
		if (end < 0) return feature;
		String plugin = "   <plugin\n         id=\"" + pluginId + "\"\n         version=\"0.0.0\"/>\n\n";
		return feature.substring(0, end) + plugin + feature.substring(end);
	}

	/**
	 * Adds a feature to the "gama.ext" category of the update site, after the last extension feature.
	 */
	public static String addFeatureToCategory(final String category, final String featureId) {
		if (category.contains("id=\"" + featureId + "\"")) return category;
		String entry = "   <feature url=\"features/" + featureId + "_0.0.0.jar\" id=\"" + featureId
				+ "\" version=\"0.0.0\">\n      <category name=\"gama.ext\"/>\n   </feature>\n";
		int last = category.lastIndexOf("<category name=\"gama.ext\"/>");
		if (last >= 0) {
			int close = category.indexOf("</feature>", last);
			int eol = category.indexOf('\n', close);
			return category.substring(0, eol + 1) + entry + category.substring(eol + 1);
		}
		int end = category.lastIndexOf("</site>");
		return end < 0 ? category : category.substring(0, end) + entry + category.substring(end);
	}

}

/*******************************************************************************************************
 *
 * PluginSpec.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.templates;

import java.util.List;

/**
 * The description of a new GAMA plugin, as entered in the wizard.
 *
 * @param id
 *            the bundle symbolic name (also the project name), e.g. {@code gama.extension.foo}
 * @param name
 *            the human-readable bundle name
 * @param vendor
 *            the bundle vendor
 * @param basePackage
 *            the base Java package
 * @param description
 *            a short description (used in the feature)
 * @param libs
 *            whether a {@code libs/} folder for third-party jars is created
 * @param modelFolders
 *            the sub-folders of {@code models/} to create (may be empty)
 * @param exampleCode
 *            whether to generate an example operator (and the corresponding GAML test model)
 * @param standaloneFeature
 *            whether a dedicated {@code gama.feature.extension.<shortcut>} feature is created (otherwise the plugin
 *            is added to {@code gama.feature.extensions})
 */
public record PluginSpec(String id, String name, String vendor, String basePackage, String description, boolean libs,
		List<String> modelFolders, boolean exampleCode, boolean standaloneFeature) {

	/** The last segment of the id, used by the processor to name the generated package. */
	public String shortcut() {
		int i = id.lastIndexOf('.');
		return i < 0 ? id : id.substring(i + 1);
	}

	/** The id of the dedicated feature. */
	public String featureId() {
		return "gama.feature.extension." + shortcut();
	}

	/** Whether a models folder is created. */
	public boolean hasModels() {
		return !modelFolders.isEmpty() || exampleCode;
	}

}

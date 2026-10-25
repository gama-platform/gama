/*******************************************************************************************************
 *
 * NewGamaTypeWizard.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.wizards;

import java.util.Map;

import gama.ui.devtools.templates.CodeTemplates;

/**
 * Creates a new GAML type and the Java class of its values.
 */
public class NewGamaTypeWizard extends AbstractGamaClassWizard {

	@Override
	protected String title() { return "New GAMA Type"; }

	@Override
	protected String description() { return "Creates a new GAML type and the Java class of its values."; }

	@Override
	protected String defaultClassName() { return "MyThingType"; }

	@Override
	protected String suffix() { return "Type"; }

	@Override
	protected String gamlNameLabel() { return "Type name"; }

	@Override
	protected Map<String, String> generate(final String pkg, final String className, final String gamlName) {
		String valueClass = className.endsWith("Type") && className.length() > 4
				? className.substring(0, className.length() - 4) : className + "Value";
		Map<String, String> files = new java.util.LinkedHashMap<>();
		files.put(className + ".java", CodeTemplates.type(pkg, className, valueClass, gamlName));
		files.put(valueClass + ".java", CodeTemplates.typeValue(pkg, valueClass, className));
		return files;
	}

}

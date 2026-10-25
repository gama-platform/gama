/*******************************************************************************************************
 *
 * NewGamaSkillWizard.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
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
 * Creates a skill (attributes and actions that species can use with 'skills: [...]').
 */
public class NewGamaSkillWizard extends AbstractGamaClassWizard {

	@Override
	protected String title() { return "New GAMA Skill"; }

	@Override
	protected String description() { return "Creates a skill (attributes and actions that species can use with 'skills: [...]')."; }

	@Override
	protected String defaultClassName() { return "MySkill"; }

	@Override
	protected String suffix() { return "Skill"; }

	@Override
	protected String gamlNameLabel() { return "Skill name"; }

	@Override
	protected Map<String, String> generate(final String pkg, final String className, final String gamlName) {
		return Map.of(className + ".java", CodeTemplates.skill(pkg, className, gamlName));
	}

}

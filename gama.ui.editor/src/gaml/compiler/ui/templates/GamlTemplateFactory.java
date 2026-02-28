/*******************************************************************************************************
 *
 * GamlTemplateFactory.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.templates;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.templates.Template;
// import org.eclipse.text.templates.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import gama.annotations.example;
import gama.annotations.usage;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.gaml.GAML;
import gama.api.gaml.types.Types;
import gama.api.utils.StringUtils;
import gama.api.utils.interfaces.INamed;
import gaml.compiler.gaml.descriptions.StatementDescription;

/**
 * The class GamlTemplateFactory.
 *
 * @author drogoul
 * @since 12 déc. 2014
 *
 */

@SuppressWarnings ("deprecation")
public class GamlTemplateFactory {

	/**
	 * Gets the context name.
	 *
	 * @return the context name
	 */
	public static String getContextName() { return "Model"; }

	/**
	 * Gets the context id.
	 *
	 * @return the context id
	 */
	public static String getContextId() { return "gaml.compiler.gaml.Gaml.Model"; }

	/**
	 * From.
	 *
	 * @param u
	 *            the u
	 * @param sp
	 *            the sp
	 * @return the template persistence data
	 */
	public static TemplatePersistenceData from(final usage u, final IArtefact sp) {
		boolean isExample = false;
		String name = u.name();
		boolean emptyName = name.isEmpty();
		String pattern = u.pattern();
		if (pattern.isEmpty()) {
			for (final example e : u.examples()) {
				if (emptyName) {
					name = e.value();
					emptyName = false;
				}
				if (!e.isPattern()) { isExample = true; }
				// if ( e.isPattern() ) {
				pattern += StringUtils.LN + e.value();
				// }
			}
		}
		if (pattern.isEmpty()) return null;
		pattern += StringUtils.LN;
		String[] path = u.path();
		if (path.length == 0) { path = new String[] { org.apache.commons.lang3.StringUtils.capitalize(sp.getName()) }; }
		String menuPath = "";
		for (final String p : path) { menuPath += p + "."; }
		String menu = u.menu();
		if (usage.NULL.equals(menu)) { menu = ISymbolKind.TEMPLATE_MENU[sp.getKind().code()]; }
		String desc = u.value();
		if (usage.NULL.equals(desc)) {
			// Trying to build something that makes sense..
			desc = menu + " " + name;
			desc += StringUtils.LN;
			final String doc = sp.getDocumentation().toString();
			int index = doc.indexOf(". ");
			if (index == -1) { index = doc.length(); }
			desc += doc.substring(0, Math.min(index, 150)) + " [...]";
		}
		menuPath = menu + "." + menuPath.substring(0, menuPath.length() - 1);
		if (isExample) { menuPath = "Examples." + menuPath; }
		final Template template = new Template(name, desc, getContextId(), pattern, true);
		return new TemplatePersistenceData(template, true, menuPath);

	}

	/** The begin comment. */
	static String beginComment = "/**" + StringUtils.LN;

	/** The end comment. */
	static String endComment = "*/" + StringUtils.LN;

	/** The comment line. */
	static String commentLine = StringUtils.LN + "* " + StringUtils.TAB + StringUtils.TAB;

	/** The inherited attributes. */
	static String inheritedAttributes = "* Inherited attributes:";

	/** The inherited actions. */
	static String inheritedActions = "* Inherited actions:";

	/** The available behaviors. */
	static String availableBehaviors = "* Available behaviors:";

	/**
	 * Body.
	 *
	 * @param body
	 *            the body
	 * @return the string
	 */
	private static String body(final String body) {
		final StringBuilder sb = new StringBuilder(200);
		sb.append(" {").append(StringUtils.LN);
		sb.append(body);
		sb.append(StringUtils.LN).append("${cursor}");
		sb.append(StringUtils.LN).append("}").append(StringUtils.LN);
		return sb.toString();
	}

	/**
	 * Dump.
	 *
	 * @param title
	 *            the title
	 * @param descs
	 *            the descs
	 * @param sb
	 *            the sb
	 */
	private static void dump(final String title, final Iterable<? extends INamed> descs, final StringBuilder sb) {
		if (!Iterables.isEmpty(descs)) {
			final List<INamed> named = Lists.newArrayList(descs);
			Collections.sort(named, INamed.COMPARATOR);
			sb.append(title);
			for (final INamed sd : named) { sb.append(commentLine).append(sd.serializeToGaml(true)); }
			sb.append(StringUtils.LN);
		}
	}

	/**
	 * Species with skill.
	 *
	 * @param skill
	 *            the skill
	 * @return the template
	 */
	public static Template speciesWithSkill(final String skill) {
		final StringBuilder comment = new StringBuilder(200);
		comment.append(beginComment);
		dump(inheritedAttributes, GamaSkillRegistry.INSTANCE.getVariablesForSkill(skill), comment);
		dump(inheritedActions, GamaSkillRegistry.INSTANCE.getActionsForSkill(skill), comment);
		comment.append(endComment);
		return new Template("A species with the skill " + skill,
				"Defines a species that implements the skill named " + skill, getContextId(),
				"species ${species_name} skills: [" + skill + "]" + body(comment.toString()), true);
	}

	/**
	 * Attribute with type.
	 *
	 * @param type
	 *            the type
	 * @return the template
	 */
	public static Template attributeWithType(final String type) {
		return new Template("An attribute of type " + type, "Defines an attribute of type " + type, getContextId(),
				type + " " + Types.get(type).asPattern() + " <- ${initial_value};", true);
	}

	/**
	 * Species with control.
	 *
	 * @param skill
	 *            the skill
	 * @return the template
	 */
	public static Template speciesWithControl(final String skill) {
		// Collection<SymbolArtefact> controls =
		// AbstractGamlAdditions.getStatementsForSkill(skill);
		final StringBuilder comment = new StringBuilder(200);
		comment.append(beginComment);
		dump(inheritedAttributes, GamaSkillRegistry.INSTANCE.getVariablesForSkill(skill), comment);
		dump(inheritedActions, GamaSkillRegistry.INSTANCE.getActionsForSkill(skill), comment);
		dump(availableBehaviors, GAML.getStatementsForSkill(skill), comment);
		comment.append(endComment);
		return new Template("A species with the control " + skill,
				"Defines a species that implements the control named " + skill, getContextId(),
				"species ${species_name} control: " + skill + body(comment.toString()), true);
	}

	/**
	 * Species with parent.
	 *
	 * @param species
	 *            the species
	 * @return the template
	 */
	public static Template speciesWithParent(final ITypeDescription species) {
		final String name = species.getName();
		final StringBuilder comment = new StringBuilder(200);
		comment.append(beginComment);
		dump(inheritedAttributes, species.getAttributes(), comment);
		dump(inheritedActions, species.getActions(), comment);
		comment.append(endComment);
		return new Template("A species with the parent " + name,
				"Defines a species that implements the control named " + name, getContextId(),
				"species ${species_name} parent: " + name + body(comment.toString()), true);
	}

	/**
	 * Call to action.
	 *
	 * @param sd
	 *            the sd
	 * @return the template
	 */
	public static Template callToAction(final StatementDescription sd) {
		final String name = sd.getName();
		final Iterable<IDescription> args = sd.getFormalArgs();
		final StringBuilder sb = new StringBuilder(100);
		sb.append("(");
		for (final IDescription arg : args) {
			sb.append(arg.getName()).append(": ").append("${the_").append(arg.getName()).append("}, ");
		}
		final int length = sb.length();
		if (length > 0) { sb.setLength(length - 2); }
		sb.append(")");
		return new Template("A call to action " + name, "A call to action " + name + " will all its arguments",
				getContextId(), "do " + name + sb.toString() + ";" + StringUtils.LN, true);
	}

	/**
	 * @param proto
	 * @return
	 */
	public static Template from(final IArtefact.Operator proto) {
		String description = proto.getMainDoc();
		if (description == null) { description = "Template for using operator " + proto.getName(); }
		return new Template("Operator " + proto.getName(), description, getContextId(), proto.getPattern(true), true);
	}

}

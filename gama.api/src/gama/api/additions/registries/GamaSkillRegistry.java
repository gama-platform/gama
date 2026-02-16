/*******************************************************************************************************
 *
 * GamaSkillRegistry.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions.registries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;

import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription.DescriptionVisitor;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.kernel.skill.ISkill;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;

/**
 * The Class GamaSkillRegistry.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaSkillRegistry {

	/** The Constant INSTANCE. */
	public final static GamaSkillRegistry INSTANCE = new GamaSkillRegistry();

	/** The skills. */
	private final IMap<String, ISkillDescription> skills = GamaMapFactory.createUnordered();

	/** The class skill names. */
	private final Map<Class, String> classSkillNames = new HashMap<>();

	/** The architecture names. */
	private List<String> architectureNames = null;

	/** The skill names. */
	private List<String> skillNames = null;

	/**
	 * Instantiates a new gama skill registry.
	 */
	private GamaSkillRegistry() {}

	/**
	 * Register.
	 *
	 * @param name
	 *            the name
	 * @param support
	 *            the support
	 * @param plugin
	 *            the plugin
	 * @param children
	 *            the children
	 * @param species
	 *            the species
	 * @return the skill description
	 */
	public ISkillDescription register(final ISkillDescription sd, final Class<? extends ISkill> support) {
		String name = sd.getName();
		classSkillNames.put(support, name);
		skills.put(name, sd);
		return sd;

	}

	/**
	 * Gets the.
	 *
	 * @param name
	 *            the name
	 * @return the skill description
	 */
	public ISkillDescription get(final String name) {
		return skills.get(name);
	}

	/**
	 * Gets the.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the skill description
	 */
	public ISkillDescription get(final Class clazz) {
		final String name = classSkillNames.get(clazz);
		if (name == null) return null;
		return skills.get(name);
	}

	/**
	 * Gets the skill instance for.
	 *
	 * @param skillName
	 *            the skill name
	 * @return the skill instance for
	 */
	public ISkill getSkillInstanceFor(final String skillName) {
		final ISkillDescription sd = skills.get(skillName);
		return sd == null ? null : sd.getInstance();
	}

	/**
	 * Gets the skill class for.
	 *
	 * @param skillName
	 *            the skill name
	 * @return the skill class for
	 */
	public Class<? extends ISkill> getSkillClassFor(final String skillName) {
		final ISkillDescription sd = skills.get(skillName);
		return sd == null ? null : sd.getJavaBase();
	}

	/**
	 * Gets the skill name for.
	 *
	 * @param skillClass
	 *            the skill class
	 * @return the skill name for
	 */
	public String getSkillNameFor(final Class skillClass) {
		return classSkillNames.get(skillClass);
	}

	/**
	 * Checks for skill.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean hasSkill(final String name) {
		return skills.containsKey(name);
	}

	/**
	 * Gets the all skill names.
	 *
	 * @return the all skill names
	 */
	public Collection<String> getAllSkillNames() { return skills.keySet(); }

	/**
	 * Gets the skill names.
	 *
	 * @return the skill names
	 */
	public Collection<String> getSkillNames() {
		if (skillNames != null) return skillNames;
		final Set<String> result = new LinkedHashSet();
		for (final String s : getAllSkillNames()) {
			final ISkillDescription c = skills.get(s);
			if (!c.isControl()) { result.add(s); }
		}
		skillNames = new ArrayList(result);
		return result;
	}

	/**
	 * Gets the architecture names.
	 *
	 * @return the architecture names
	 */
	public Collection<String> getArchitectureNames() {
		if (architectureNames != null) return architectureNames;
		final Set<String> result = new LinkedHashSet();
		for (final String s : getAllSkillNames()) {
			final ISkillDescription c = skills.get(s);
			if (c.isControl()) { result.add(s); }
		}
		architectureNames = new ArrayList(result);
		return result;

	}

	/**
	 * Gets the variables for skill.
	 *
	 * @param s
	 *            the s
	 * @return the variables for skill
	 */
	public Iterable<? extends IVariableDescription> getVariablesForSkill(final String s) {
		final ISkillDescription sd = skills.get(s);
		if (sd == null) return Collections.EMPTY_LIST;
		return sd.getOwnAttributes().values();
	}

	/**
	 * Gets the actions for skill.
	 *
	 * @param s
	 *            the s
	 * @return the actions for skill
	 */
	public Iterable<? extends IActionDescription> getActionsForSkill(final String s) {
		final ISkillDescription sd = skills.get(s);
		if (sd == null) return Collections.EMPTY_LIST;
		return sd.getOwnActions().values();
	}

	/**
	 * Visit skills.
	 *
	 * @param visitor
	 *            the visitor
	 */
	public void visitSkills(final DescriptionVisitor visitor) {
		skills.forEachValue(visitor);
	}

	/**
	 * Gets the registered skills.
	 *
	 * @return the registered skills
	 */
	public Iterable<ISkillDescription> getRegisteredSkills() { return skills.values(); }

	/**
	 * Gets the registered skills attributes.
	 *
	 * @return the registered skills attributes
	 */
	public Iterable<? extends IVariableDescription> getRegisteredSkillsAttributes() {
		return Iterables.concat(Iterables.transform(getRegisteredSkills(), s -> s.getOwnAttributes().values()));
	}

	/**
	 * Gets the registered skills actions.
	 *
	 * @return the registered skills actions
	 */
	public Iterable<? extends IActionDescription> getRegisteredSkillsActions() {
		return Iterables.concat(Iterables.transform(getRegisteredSkills(), s -> s.getOwnActions().values()));
	}

}

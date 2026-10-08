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
 * Registry for agent skills and control architectures in GAMA.
 * 
 * <p>This singleton registry maintains all skill and control architecture definitions available
 * in the GAMA platform. Skills are collections of attributes and actions that can be added to
 * agent species to extend their capabilities. Control architectures are special skills that
 * define how agents schedule and execute their actions.</p>
 * 
 * <h2>Skills vs. Control Architectures</h2>
 * <ul>
 *   <li><b>Skills</b> - Reusable capabilities (e.g., moving, communicating) added via the 'skills:' facet</li>
 *   <li><b>Control Architectures</b> - Execution strategies (e.g., fsm, reflex, user_only) specified via 'control:'</li>
 * </ul>
 * 
 * <p>The distinction is made via {@link ISkillDescription#isControl()}.</p>
 * 
 * <h2>Skill Registration</h2>
 * <p>Skills are registered during platform initialization by scanning for classes annotated with
 * {@code @skill}. Each skill is associated with:</p>
 * <ul>
 *   <li>A unique name used in GAML models</li>
 *   <li>A Java class implementing {@link ISkill}</li>
 *   <li>Attributes (variables) provided by the skill</li>
 *   <li>Actions (methods) provided by the skill</li>
 * </ul>
 * 
 * <h2>Skill Lookup</h2>
 * <p>Skills can be retrieved by:</p>
 * <ul>
 *   <li>Name - as used in GAML models</li>
 *   <li>Java class - for implementation-level access</li>
 * </ul>
 * 
 * <h2>Singleton Pattern</h2>
 * <p>The registry uses a singleton pattern accessible via {@link #INSTANCE}. All methods
 * are instance methods called on this singleton.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Get a skill by name
 * ISkillDescription moving = GamaSkillRegistry.INSTANCE.get("moving");
 * 
 * // Get all skill names
 * Collection<String> skillNames = GamaSkillRegistry.INSTANCE.getSkillNames();
 * 
 * // Get all architecture names
 * Collection<String> archNames = GamaSkillRegistry.INSTANCE.getArchitectureNames();
 * 
 * // Get attributes provided by a skill
 * Iterable<? extends IVariableDescription> vars = 
 *     GamaSkillRegistry.INSTANCE.getVariablesForSkill("moving");
 * 
 * // Get actions provided by a skill
 * Iterable<? extends IActionDescription> actions = 
 *     GamaSkillRegistry.INSTANCE.getActionsForSkill("moving");
 * }</pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * 
 * @see ISkillDescription
 * @see ISkill
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaSkillRegistry {

	/** The singleton instance. */
	public final static GamaSkillRegistry INSTANCE = new GamaSkillRegistry();

	/** Map of skill names to their descriptions. */
	private final IMap<String, ISkillDescription> skills = GamaMapFactory.createUnordered();

	/** Map of skill implementation classes to their GAML names. */
	private final Map<Class, String> classSkillNames = new HashMap<>();

	/** Cached list of architecture (control) names. */
	private List<String> architectureNames = null;

	/** Cached list of regular skill names. */
	private List<String> skillNames = null;

	/**
	 * Private constructor for singleton pattern.
	 */
	private GamaSkillRegistry() {}

	/**
	 * Registers a skill description in the registry.
	 * 
	 * <p>This method is called during platform initialization to register skills
	 * discovered through annotation processing.</p>
	 *
	 * @param sd the skill description to register
	 * @param support the Java class implementing the skill
	 * @return the registered skill description (same as input)
	 */
	public ISkillDescription register(final ISkillDescription sd, final Class<? extends ISkill> support) {
		String name = sd.getName();
		classSkillNames.put(support, name);
		skills.put(name, sd);
		return sd;

	}

	/**
	 * Retrieves a skill description by its GAML name.
	 *
	 * @param name the skill name as used in GAML models
	 * @return the skill description, or null if not found
	 */
	public ISkillDescription get(final String name) {
		return skills.get(name);
	}

	/**
	 * Retrieves a skill description by its implementation class.
	 *
	 * @param clazz the skill implementation class
	 * @return the skill description, or null if not found
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
	 * Returns all skill names (both regular skills and control architectures).
	 *
	 * @return collection of all registered skill names
	 */
	public Collection<String> getAllSkillNames() { return skills.keySet(); }

	/**
	 * Returns the names of all regular skills (excluding control architectures).
	 * 
	 * <p>This method caches the result for performance. Regular skills are those
	 * where {@link ISkillDescription#isControl()} returns false.</p>
	 *
	 * @return collection of skill names suitable for use in 'skills:' facets
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
	 * Returns the names of all control architectures.
	 * 
	 * <p>This method caches the result for performance. Control architectures are
	 * skills where {@link ISkillDescription#isControl()} returns true.</p>
	 *
	 * @return collection of architecture names suitable for use in 'control:' facets
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

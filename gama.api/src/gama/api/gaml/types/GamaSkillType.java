/*******************************************************************************************************
 *
 * GamaSkillType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.skill.ISkill;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.IMap;

/**
 * Meta-type representing skills in GAML - reusable behavioral components for agents.
 * <p>
 * Skills are modular collections of attributes and actions that can be attached to agents to extend their
 * capabilities. They provide a composition-based approach to agent design, allowing agents to acquire behaviors
 * without inheritance. Skills can be dynamically assigned to species and provide specialized functionality like
 * movement, communication, or spatial awareness.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Modular behavioral components</li>
 * <li>Composition-based agent extension</li>
 * <li>Reusable across multiple species</li>
 * <li>Provide additional attributes and actions</li>
 * <li>Can be built-in or defined by plugins</li>
 * <li>Runtime access to skill instances</li>
 * </ul>
 * 
 * <h2>Built-in Skills:</h2>
 * <ul>
 * <li><b>moving</b> - basic movement capabilities (move, goto, wander)</li>
 * <li><b>advanced_moving</b> - advanced pathfinding and movement</li>
 * <li><b>grid</b> - grid-based agent behaviors</li>
 * <li><b>messaging</b> - inter-agent communication (FIPA)</li>
 * <li><b>driving</b> - road network navigation</li>
 * <li><b>pedestrian</b> - pedestrian movement simulation</li>
 * <li><b>physics</b> - physical simulation integration</li>
 * <li>And many more from plugins...</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {@code
 * // Assign skills to a species
 * species person skills: [moving] {
 *     reflex move {
 *         do wander;  // Action from moving skill
 *     }
 * }
 * 
 * // Multiple skills
 * species communicating_agent skills: [moving, messaging] {
 *     reflex send_message {
 *         do start_conversation to: [other_agent] protocol: 'fipa-request';
 *     }
 * }
 * 
 * // Access skill programmatically
 * skill moving_skill <- skill("moving");
 * 
 * // Check if agent has skill
 * bool has_moving <- agent1 has_skill moving;
 * 
 * // Add skill dynamically (advanced usage)
 * do add_skill(moving_skill);
 * }
 * </pre>
 * 
 * <h2>Skill Registration:</h2>
 * <p>
 * Skills are registered through the {@link gama.api.additions.registries.GamaSkillRegistry} by plugins during
 * initialization. Each skill defines its attributes (variables) and actions that become available to agents.
 * </p>
 * 
 * @author GAMA Development Team
 * @see GamaType
 * @see gama.api.kernel.skill.ISkill
 * @see gama.api.additions.registries.GamaSkillRegistry
 * @since GAMA 1.0
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.SKILL,
		id = IType.SKILL,
		wraps = { ISkill.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.SKILL },
		doc = @doc ("Meta-type of the skills present in the GAML language"))
public class GamaSkillType extends GamaType<ISkill> {

	/**
	 * Constructs a new skill type.
	 * 
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaSkillType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a skill.
	 * <p>
	 * This method supports casting from:
	 * <ul>
	 * <li>ISkill - returns the skill itself</li>
	 * <li>String - looks up and returns the skill registered with that name</li>
	 * <li>Other types - returns null</li>
	 * </ul>
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a skill
	 * @param param
	 *            optional parameter (not used for skill casting)
	 * @param copy
	 *            whether to create a copy (not applicable for skills)
	 * @return the skill instance if found, null otherwise
	 * @throws GamaRuntimeException
	 *             if the casting operation encounters an error
	 */
	@Override
	@doc ("Tries to convert the parameter to a skill. If it is a skill already, returns it. If it is a string, returns it if it is registered in GAMA. Otherwise return null")
	public ISkill cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof ISkill) return (ISkill) obj;
		if (obj instanceof String) return GamaSkillRegistry.INSTANCE.getSkillInstanceFor((String) obj);
		return null;
	}

	/**
	 * Returns the default value for skill type.
	 * <p>
	 * The default skill is null, as there is no meaningful default skill.
	 * </p>
	 * 
	 * @return null
	 */
	@Override
	public ISkill getDefault() { return null; }

	/**
	 * Indicates whether skills can be cast to constant values.
	 * <p>
	 * Skills can be constant as they are stateless singletons.
	 * </p>
	 * 
	 * @return true, skills can be constant
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Deserializes a skill from a JSON representation.
	 * <p>
	 * The JSON map should contain a "name" field with the skill name.
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param map2
	 *            the JSON map containing skill data
	 * @return the deserialized skill
	 */
	@Override
	public ISkill deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return cast(scope, map2.get("name"), null, false);
	}

}

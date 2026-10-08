/*******************************************************************************************************
 *
 * ISkill.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.skill;

import gama.api.compilation.IVarAndActionSupport;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.ISkillDescription;

/**
 * Interface for objects that provide reusable behaviors (skills) to agents.
 * 
 * <p>Skills in GAMA are modular components that can be attached to species to provide
 * additional attributes and actions. They enable code reuse and composition of behaviors
 * across different agent types without requiring inheritance.</p>
 * 
 * <h2>Core Concepts</h2>
 * <ul>
 *   <li><b>Reusability:</b> A skill can be used by multiple species</li>
 *   <li><b>Composition:</b> An agent can have multiple skills simultaneously</li>
 *   <li><b>Variables & Actions:</b> Skills can declare both attributes and behaviors</li>
 *   <li><b>Description-based:</b> Skills are described and compiled like other GAML elements</li>
 * </ul>
 * 
 * <h2>Built-in Skills</h2>
 * <p>GAMA provides many built-in skills including:
 * <ul>
 *   <li><b>moving:</b> Movement capabilities (move, goto, etc.)</li>
 *   <li><b>advanced_driving:</b> Traffic simulation behaviors</li>
 *   <li><b>messaging:</b> Agent communication</li>
 *   <li><b>SQLDB:</b> Database interactions</li>
 *   <li><b>network:</b> Network communication</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Using Skills in GAML</h3>
 * <pre>{@code
 * species Animal skills: [moving] {
 *     reflex wander {
 *         do wander;  // action from 'moving' skill
 *     }
 * }
 * 
 * species Car skills: [moving, advanced_driving] {
 *     reflex drive {
 *         do drive;  // action from 'advanced_driving' skill
 *     }
 * }
 * }</pre>
 * 
 * <h3>Implementing a Custom Skill in Java</h3>
 * <pre>{@code
 * @skill(name = "my_skill")
 * @vars({
 *     @variable(name = "energy", type = IType.FLOAT, init = "100.0")
 * })
 * public class MySkill extends Skill {
 * 
 *     @action(name = "consume_energy", args = {
 *         @arg(name = "amount", type = IType.FLOAT)
 *     })
 *     public void consumeEnergy(IScope scope) {
 *         IAgent agent = scope.getAgent();
 *         double amount = Cast.asFloat(scope, scope.getArg("amount"));
 *         double energy = Cast.asFloat(scope, agent.getAttribute("energy"));
 *         agent.setAttribute("energy", energy - amount);
 *     }
 * }
 * }</pre>
 * 
 * <h3>Accessing Skill Attributes</h3>
 * <pre>{@code
 * species MySpecies skills: [moving] {
 *     init {
 *         speed <- 5.0;  // 'speed' is defined by the 'moving' skill
 *     }
 * }
 * }</pre>
 * 
 * @see Skill for the base implementation
 * @see IArchitecture for agent control architectures
 * @see ISkillDescription for skill metadata
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 */
public interface ISkill extends IGamlDescription, IVarAndActionSupport {

	/**
	 * Gets the description of this skill.
	 * 
	 * <p>The description contains metadata about the skill including its name,
	 * variables, actions, and documentation.</p>
	 *
	 * @return the skill description
	 * 
	 * @see ISkillDescription
	 */
	ISkillDescription getDescription();

	/**
	 * Sets the description for this skill.
	 * 
	 * <p>This method is typically called during skill initialization by the
	 * GAML compiler infrastructure.</p>
	 *
	 * @param skillDescription the skill description to set
	 */
	void setDescription(ISkillDescription skillDescription);
}

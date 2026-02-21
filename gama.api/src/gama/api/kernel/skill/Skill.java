/*******************************************************************************************************
 *
 * Skill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.skill;

import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.topology.ITopology;

/**
 * The Class Skill.
 * 
 * <p>
 * Abstract base class for all skills in GAMA. A skill is a reusable package of attributes (variables) and behaviors
 * (actions) that can be added to species. Skills promote code reuse by allowing common functionality to be defined
 * once and shared across multiple species.
 * </p>
 * 
 * <h3>Purpose</h3>
 * <p>
 * Skills provide:
 * </p>
 * <ul>
 * <li><b>Code Reuse:</b> Share attributes and actions across species</li>
 * <li><b>Modularity:</b> Package related functionality together</li>
 * <li><b>Extensibility:</b> Extend GAMA's capabilities with custom skills</li>
 * <li><b>Composition:</b> Species can combine multiple skills</li>
 * </ul>
 * 
 * <h3>Built-in Skills</h3>
 * <ul>
 * <li><b>moving:</b> Navigation and path-following (do goto, do wander, etc.)</li>
 * <li><b>grid:</b> Grid-specific operations for grid agents</li>
 * <li><b>3d:</b> 3D geometric operations</li>
 * <li><b>driving:</b> Road network navigation</li>
 * <li><b>graph:</b> Graph-based operations</li>
 * <li><b>messaging:</b> Inter-agent communication (FIPA)</li>
 * </ul>
 * 
 * <h3>Extension Skills</h3>
 * <ul>
 * <li><b>advanced_driving:</b> Advanced road navigation</li>
 * <li><b>pedestrian:</b> Pedestrian-specific movement</li>
 * <li><b>physics:</b> Physical simulation</li>
 * <li><b>bdi:</b> Belief-Desire-Intention cognitive modeling</li>
 * <li><b>skill_road:</b> Road network skills</li>
 * </ul>
 * 
 * <h3>Usage in GAML</h3>
 * 
 * <h4>1. Using Built-in Skills</h4>
 * 
 * <pre>
 * <code>
 * species animal skills: [moving] {
 *     // Gets attributes: speed, heading, destination
 *     // Gets actions: goto, wander, follow, etc.
 *     
 *     reflex move {
 *         do wander amplitude: 120;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Combining Multiple Skills</h4>
 * 
 * <pre>
 * <code>
 * species communicating_agent skills: [moving, messaging] {
 *     // Has both movement and communication capabilities
 *     
 *     reflex move {
 *         do goto target: target_location;
 *     }
 *     
 *     reflex communicate {
 *         do start_conversation with: other_agents 
 *            protocol: 'fipa-request' 
 *            contents: ['Give me your position'];
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>3. Skill-Specific Attributes</h4>
 * 
 * <pre>
 * <code>
 * species vehicle skills: [moving] {
 *     init {
 *         // Skill provides 'speed' attribute
 *         speed <- 50 #km/#h;
 *     }
 *     
 *     reflex adjust_speed {
 *         speed <- speed * 0.9;  // Skill attribute
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Java Usage - Creating Custom Skill</h3>
 * 
 * <pre>
 * <code>
 * {@literal @}skill(name = "energy_management")
 * {@literal @}vars({
 *     {@literal @}variable(name = "energy", type = IType.FLOAT, init = "100.0"),
 *     {@literal @}variable(name = "max_energy", type = IType.FLOAT, init = "100.0")
 * })
 * {@literal @}doc("Provides energy management capabilities to agents")
 * public class EnergySkill extends Skill {
 *     
 *     {@literal @}action(name = "consume_energy",
 *           args = {@literal @}arg(name = "amount", type = IType.FLOAT))
 *     {@literal @}doc("Consumes the specified amount of energy")
 *     public void consumeEnergy(IScope scope) {
 *         IAgent agent = getCurrentAgent(scope);
 *         Double amount = scope.getFloatArg("amount");
 *         Double current = (Double) agent.getAttribute("energy");
 *         agent.setAttribute("energy", Math.max(0, current - amount));
 *     }
 *     
 *     {@literal @}action(name = "recharge",
 *           args = {@literal @}arg(name = "amount", type = IType.FLOAT))
 *     public void recharge(IScope scope) {
 *         IAgent agent = getCurrentAgent(scope);
 *         Double amount = scope.getFloatArg("amount");
 *         Double current = (Double) agent.getAttribute("energy");
 *         Double max = (Double) agent.getAttribute("max_energy");
 *         agent.setAttribute("energy", Math.min(max, current + amount));
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>Using Custom Skill</h4>
 * 
 * <pre>
 * <code>
 * species robot skills: [energy_management] {
 *     // Gets energy and max_energy attributes
 *     // Gets consume_energy and recharge actions
 *     
 *     reflex work {
 *         do consume_energy amount: 5.0;
 *     }
 *     
 *     reflex rest when: energy < 20 {
 *         do recharge amount: 10.0;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Skill Composition</h3>
 * <p>
 * Species can combine multiple skills:
 * </p>
 * 
 * <pre>
 * <code>
 * species advanced_robot skills: [moving, messaging, energy_management, physics] {
 *     // Inherits all attributes and actions from all four skills
 *     // Can move, communicate, manage energy, and use physics
 * }
 * </code>
 * </pre>
 * 
 * <h3>Helper Methods</h3>
 * <p>
 * The Skill base class provides utility methods for subclasses:
 * </p>
 * <ul>
 * <li><b>getCurrentAgent(scope):</b> Get the agent executing the current action</li>
 * <li><b>getTopology(agent):</b> Get the agent's spatial topology</li>
 * </ul>
 * 
 * <h3>Skill Description</h3>
 * <p>
 * Each skill has an associated ISkillDescription that provides:
 * </p>
 * <ul>
 * <li>Name and documentation</li>
 * <li>Variables defined by the skill</li>
 * <li>Actions provided by the skill</li>
 * <li>Defining plugin information</li>
 * </ul>
 * 
 * <h3>Implementation Requirements</h3>
 * <p>
 * Custom skills must:
 * </p>
 * <ol>
 * <li>Extend the Skill class</li>
 * <li>Be annotated with @skill(name = "skill_name")</li>
 * <li>Use @vars to declare skill-specific attributes (optional)</li>
 * <li>Use @action to declare skill-specific actions (optional)</li>
 * <li>Provide @doc documentation</li>
 * </ol>
 * 
 * <h3>Design Patterns</h3>
 * <ul>
 * <li><b>Mixin Pattern:</b> Skills add functionality to species without inheritance</li>
 * <li><b>Strategy Pattern:</b> Different skills = different strategies for behavior</li>
 * <li><b>Decorator Pattern:</b> Skills decorate agents with additional capabilities</li>
 * </ul>
 * 
 * @see ISkill
 * @see IAgent
 * @see ISkillDescription
 * @author drogoul
 * @since GAMA 1.0
 */
public abstract class Skill implements ISkill {

	/** The description. */
	protected ISkillDescription description;

	/**
	 * Instantiates a new skill.
	 */
	protected Skill() {}

	@Override
	public void setName(final String newName) {}

	/**
	 * Sets the description.
	 *
	 * @param desc
	 *            the new description
	 */
	@Override
	public void setDescription(final ISkillDescription desc) { description = desc; }

	@Override
	public IGamlDocumentation getDocumentation() { return description.getDocumentation(); }

	@Override
	public ISkillDescription getDescription() { return description; }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	/**
	 * Gets the current agent.
	 *
	 * @param scope
	 *            the scope
	 * @return the current agent
	 */
	protected IAgent getCurrentAgent(final IScope scope) {
		return scope.getAgent();
	}

	/**
	 * Gets the topology.
	 *
	 * @param agent
	 *            the agent
	 * @return the topology
	 */
	protected ITopology getTopology(final IAgent agent) {
		return agent.getTopology();
	}

	@Override
	public String getTitle() { return description.getTitle(); }

	@Override
	public String getDefiningPlugin() { return description.getDefiningPlugin(); }

	@Override
	public String getName() { return description.getName(); }

}

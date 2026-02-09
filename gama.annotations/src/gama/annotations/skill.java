/**
 *
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @skill} annotation defines reusable behavioral components that can be attached to agent species. Skills
 * provide modular functionality by grouping related variables and actions that agents can use.
 *
 * <p>
 * <strong>Architecture:</strong>
 * </p>
 * Skills are a fundamental architectural pattern in GAMA that promotes code reuse and modularity:
 * <ul>
 * <li><strong>Composition over Inheritance:</strong> Agents acquire behaviors through skill attachment</li>
 * <li><strong>Modularity:</strong> Related functionality is grouped into cohesive units</li>
 * <li><strong>Reusability:</strong> Skills can be shared across multiple species</li>
 * <li><strong>Separation of Concerns:</strong> Different aspects of behavior are isolated</li>
 * </ul>
 *
 * <p>
 * <strong>Skill Design Principles:</strong>
 * </p>
 * <ul>
 * <li><strong>Single Responsibility:</strong> Each skill should focus on one behavioral aspect</li>
 * <li><strong>Loose Coupling:</strong> Skills should minimize dependencies on other skills</li>
 * <li><strong>High Cohesion:</strong> Variables and actions within a skill should be closely related</li>
 * <li><strong>Clear Interface:</strong> Public actions and variables should form a coherent API</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 *
 * <pre>
 * {
 * 	&#64;code
 * 	// Basic movement skill
 * 	&#64;skill (
 * 			name = "moving",
 * 			concept = { IConcept.AGENT_MOVEMENT, IConcept.SKILL },
 * 			doc = @doc ("Provides basic movement capabilities for agents"))
 * 	public class MovingSkill extends Skill {
 * 
 * 		&#64;action (
 * 				name = "move",
 * 				doc = @doc ("Moves the agent forward"))
 * 		public Object move(final IScope scope) throws GamaRuntimeException {
 * 			// Movement implementation
 * 			return null;
 * 		}
 * 
 * 		&#64;action (
 * 				name = "wander",
 * 				doc = @doc ("Random movement behavior"))
 * 		public Object wander(final IScope scope) throws GamaRuntimeException {
 * 			// Random movement implementation
 * 			return null;
 * 		}
 * 	}
 *
 * 	// Communication skill with automatic attachment
 * 	&#64;skill (
 * 			name = "communicating",
 * 			attach_to = { "agent", "social_agent" },
 * 			concept = { IConcept.COMMUNICATION },
 * 			doc = @doc ("Enables agents to send and receive messages"))
 * 	public class CommunicationSkill extends Skill {
 * 
 * 		&#64;action (
 * 				name = "send_message",
 * 				doc = @doc ("Sends a message to another agent"))
 * 		public Object sendMessage(final IScope scope) throws GamaRuntimeException {
 * 			// Message sending implementation
 * 			return null;
 * 		}
 * 	}
 *
 * 	// Specialized skill for specific agent types
 * 	&#64;skill (
 * 			name = "predator_hunting",
 * 			concept = { IConcept.AGENT_MOVEMENT, IConcept.ALGORITHM },
 * 			category = { "Predation", "AI" },
 * 			doc = @doc ("Specialized hunting behaviors for predator agents"))
 * 	public class PredatorSkill extends Skill {
 * 
 * 		@action (
 * 				name = "hunt",
 * 				doc = @doc ("Actively hunts for prey agents"))
 * 		public Object hunt(final IScope scope) throws GamaRuntimeException {
 * 			// Hunting implementation
 * 			return null;
 * 		}
 * 	}
 * }
 * </pre>
 *
 * <p>
 * <strong>Skill Attachment:</strong>
 * </p>
 * Skills can be attached to species in multiple ways:
 * <ul>
 * <li><strong>Explicit:</strong> {@code species my_agent skills: [moving, communicating]}</li>
 * <li><strong>Automatic:</strong> Using the {@code attach_to} property</li>
 * <li><strong>Inheritance:</strong> Inherited from parent species</li>
 * </ul>
 *
 * <p>
 * <strong>Performance Considerations:</strong>
 * </p>
 * <ul>
 * <li>Skills add minimal runtime overhead once loaded</li>
 * <li>Skill variables are stored in agent attribute maps</li>
 * <li>Action dispatch is optimized through pre-compilation</li>
 * <li>Unused skills don't impact performance</li>
 * </ul>
 *
 * <p>
 * <strong>Common Skill Categories:</strong>
 * </p>
 * <ul>
 * <li><strong>Movement:</strong> Navigation, pathfinding, spatial behaviors</li>
 * <li><strong>Communication:</strong> Message passing, networking</li>
 * <li><strong>Perception:</strong> Sensing, observation, information gathering</li>
 * <li><strong>Decision Making:</strong> AI algorithms, behavior trees</li>
 * <li><strong>Lifecycle:</strong> Birth, death, reproduction</li>
 * <li><strong>Resource Management:</strong> Energy, inventory, economics</li>
 * </ul>
 *
 * @see species For agent type definitions
 * @see action For skill behavior definitions
 * @see vars For skill variable definitions
 * @see doc For documentation metadata
 *
 * @author drogoul
 * @author GAMA Development Team
 * @since GAMA 1.0 (June 2, 2012)
 */
@Retention (RetentionPolicy.CLASS)
@Target (ElementType.TYPE)
public @interface skill {

	/**
	 * The unique name of this skill as it appears in GAML.
	 *
	 * <p>
	 * Skill names must be unique across the entire GAMA platform and should be descriptive of the skill's primary
	 * purpose. Names should follow GAML naming conventions: lowercase with underscores for separation.
	 * </p>
	 *
	 * <p>
	 * <strong>Naming Guidelines:</strong>
	 * </p>
	 * <ul>
	 * <li>Use descriptive names that clearly indicate the skill's purpose</li>
	 * <li>Avoid generic names like "behavior" or "utils"</li>
	 * <li>Consider including the domain area for specialized skills</li>
	 * <li>Keep names concise but meaningful</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Examples:</strong>
	 * </p>
	 * <ul>
	 * <li>{@code "moving"} - Basic movement capabilities</li>
	 * <li>{@code "perception"} - Sensing and observation</li>
	 * <li>{@code "communication"} - Message passing and networking</li>
	 * <li>{@code "pathfinding"} - Advanced navigation algorithms</li>
	 * <li>{@code "resource_management"} - Resource handling and inventory</li>
	 * </ul>
	 *
	 * @return the unique skill name in GAML
	 */
	String name();

	/**
	 * Keywords associated with this skill for documentation and search purposes.
	 *
	 * <p>
	 * Concepts help users discover skills through the documentation search system and provide semantic categorization.
	 * Use predefined constants from {@code IConcept} whenever possible to maintain consistency across the platform.
	 * </p>
	 *
	 * <p>
	 * <strong>Common Concept Categories:</strong>
	 * </p>
	 * <ul>
	 * <li><strong>Functional:</strong> {@code IConcept.AGENT_MOVEMENT}, {@code IConcept.COMMUNICATION}</li>
	 * <li><strong>Architectural:</strong> {@code IConcept.SKILL}, {@code IConcept.ARCHITECTURE}</li>
	 * <li><strong>Domain:</strong> {@code IConcept.ALGORITHM}, {@code IConcept.NETWORK}</li>
	 * <li><strong>Temporal:</strong> {@code IConcept.SCHEDULE}, {@code IConcept.TIME}</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Usage Examples:</strong>
	 * </p>
	 * <ul>
	 * <li>Movement skill: {@code { IConcept.AGENT_MOVEMENT, IConcept.SKILL }}</li>
	 * <li>AI skill: {@code { IConcept.ALGORITHM, IConcept.DECISION_MAKING }}</li>
	 * <li>Network skill: {@code { IConcept.COMMUNICATION, IConcept.NETWORK }}</li>
	 * </ul>
	 *
	 * @return array of concept keywords for documentation search
	 * @see gama.annotations.support.IConcept For predefined concept constants
	 */
	String[] concept() default {};

	/**
	 * Species names that will automatically receive this skill.
	 *
	 * <p>
	 * This provides a convenient way to automatically attach skills to specific agent types without requiring explicit
	 * declaration in the species definition. It's particularly useful for:
	 * </p>
	 *
	 * <ul>
	 * <li><strong>Essential Skills:</strong> Skills that all agents need</li>
	 * <li><strong>Default Behaviors:</strong> Standard functionality for agent types</li>
	 * <li><strong>Platform Integration:</strong> System-level skills</li>
	 * <li><strong>Backward Compatibility:</strong> Maintaining existing model compatibility</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Attachment Strategy:</strong>
	 * </p>
	 * <ul>
	 * <li>Use sparingly to avoid unexpected behavior</li>
	 * <li>Prefer explicit skill attachment in most cases</li>
	 * <li>Document automatic attachments clearly</li>
	 * <li>Consider inheritance implications</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Examples:</strong>
	 * </p>
	 * <ul>
	 * <li>{@code {"agent"}} - Attach to base agent type</li>
	 * <li>{@code {"mobile_agent", "vehicle"}} - Multiple specific types</li>
	 * <li>{@code {"social_agent"}} - Domain-specific agent category</li>
	 * </ul>
	 *
	 * @return array of species names that automatically receive this skill
	 * @see species For species definition details
	 */
	String[] attach_to() default {};

	/**
	 * Indicates whether this skill is for internal framework use only.
	 *
	 * <p>
	 * Internal skills are not intended for direct use by modelers and typically provide system-level functionality.
	 * They may have unstable APIs and are not included in standard documentation.
	 * </p>
	 *
	 * <p>
	 * <strong>Internal Skill Characteristics:</strong>
	 * </p>
	 * <ul>
	 * <li>Hidden from standard GAML documentation</li>
	 * <li>Used for framework implementation details</li>
	 * <li>May have unstable APIs across versions</li>
	 * <li>Not recommended for general modeling use</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Examples of Internal Skills:</strong>
	 * </p>
	 * <ul>
	 * <li>Debugging and profiling utilities</li>
	 * <li>Low-level system integration</li>
	 * <li>Framework infrastructure components</li>
	 * <li>Experimental or unstable features</li>
	 * </ul>
	 *
	 * @return {@code true} if this skill is for internal use only, {@code false} otherwise
	 */
	boolean internal() default false;

	/**
	 * Documentation categories for organizing skills in reference materials.
	 *
	 * <p>
	 * Categories provide hierarchical organization for documentation and help users navigate large collections of
	 * skills. They complement the concept system by providing more specific classification.
	 * </p>
	 *
	 * <p>
	 * <strong>Category Guidelines:</strong>
	 * </p>
	 * <ul>
	 * <li>Use established category names when possible</li>
	 * <li>Keep category names concise and descriptive</li>
	 * <li>Consider the target audience's perspective</li>
	 * <li>Maintain consistency across related skills</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Common Categories:</strong>
	 * </p>
	 * <ul>
	 * <li><strong>Behavioral:</strong> "Movement", "Communication", "Decision Making"</li>
	 * <li><strong>Domain-Specific:</strong> "Ecology", "Economics", "Social", "Physics"</li>
	 * <li><strong>Technical:</strong> "Data Analysis", "Visualization", "Import/Export"</li>
	 * <li><strong>Advanced:</strong> "AI", "Machine Learning", "Optimization"</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Examples:</strong>
	 * </p>
	 * <ul>
	 * <li>{@code {"Movement", "Spatial"}} - Movement-related skill</li>
	 * <li>{@code {"AI", "Decision Making"}} - Artificial intelligence skill</li>
	 * <li>{@code {"Network", "Communication"}} - Networking skill</li>
	 * </ul>
	 *
	 * @return array of category names for documentation organization
	 */
	String[] category() default {};

	/**
	 * Comprehensive documentation for this skill.
	 *
	 * <p>
	 * Skill documentation should provide a clear overview of the skill's purpose, the behaviors it provides, and
	 * guidelines for effective use. Include examples showing how to attach and use the skill in typical scenarios.
	 * </p>
	 *
	 * <p>
	 * <strong>Documentation Content:</strong>
	 * </p>
	 * <ul>
	 * <li><strong>Purpose:</strong> What the skill does and why it's useful</li>
	 * <li><strong>Actions:</strong> Key behaviors provided by the skill</li>
	 * <li><strong>Variables:</strong> Important attributes managed by the skill</li>
	 * <li><strong>Usage:</strong> How to attach and configure the skill</li>
	 * <li><strong>Examples:</strong> Practical demonstration of skill usage</li>
	 * <li><strong>Integration:</strong> How the skill works with other components</li>
	 * </ul>
	 *
	 * @return the documentation for this skill
	 * @see doc For documentation specification details
	 */
	doc[] doc() default {};

}
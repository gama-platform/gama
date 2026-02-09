/**
 *
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @species} annotation defines agent types that can be instantiated and simulated in GAMA. Species serve as
 * templates for creating agents with specific behaviors, attributes, and capabilities.
 *
 * <p>
 * <strong>Architecture:</strong>
 * </p>
 * Species are the fundamental building blocks of agent-based models in GAMA:
 * <ul>
 * <li><strong>Agent Templates:</strong> Define the structure and behavior of agent instances</li>
 * <li><strong>Type System:</strong> Provide strong typing for agent collections and references</li>
 * <li><strong>Inheritance:</strong> Support hierarchical relationships between agent types</li>
 * <li><strong>Skill Integration:</strong> Compose behaviors through skill attachment</li>
 * <li><strong>Lifecycle Management:</strong> Handle agent creation, execution, and disposal</li>
 * </ul>
 *
 * <p>
 * <strong>Species Design Patterns:</strong>
 * </p>
 * <ul>
 * <li><strong>Base Types:</strong> Fundamental agent categories (e.g., "agent", "person")</li>
 * <li><strong>Domain Entities:</strong> Model-specific agents (e.g., "predator", "vehicle")</li>
 * <li><strong>Specialized Roles:</strong> Behavior-focused agents (e.g., "leader", "follower")</li>
 * <li><strong>Abstract Templates:</strong> Common interfaces for agent hierarchies</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 *
 * <pre>
 * {
 * 	&#64;code
 * 	// Basic agent species with movement capability
 * 	&#64;species (
 * 			name = "animal",
 * 			skills = { "moving" },
 * 			concept = { IConcept.SPECIES, IConcept.AGENT_MOVEMENT },
 * 			doc = @doc ("Basic animal agent with movement capabilities"))
 * 	public class Animal extends AbstractAgent {
 * 		// Species-specific variables and actions
 * 	}
 *
 * 	// Predator species with specialized hunting behavior
 * 	&#64;species (
 * 			name = "predator",
 * 			skills = { "moving", "hunting", "perception" },
 * 			concept = { IConcept.SPECIES, IConcept.PREDATION },
 * 			category = { "Ecology", "Behavior" },
 * 			doc = @doc (
 * 					value = "Predator agent that hunts prey in the ecosystem",
 * 					examples = @example ("create predator number: 10;")))
 * 	public class Predator extends Animal {
 *
 * 		&#64;action (
 * 				name = "hunt",
 * 				doc = @doc ("Hunt for nearby prey"))
 * 		public Object hunt(final IScope scope) throws GamaRuntimeException {
 * 			// Hunting behavior implementation
 * 			return null;
 * 		}
 * 	}
 *
 * 	// Vehicle species for transportation models
 * 	&#64;species (
 * 			name = "vehicle",
 * 			skills = { "moving", "traffic_following" },
 * 			concept = { IConcept.TRANSPORT, IConcept.MOBILITY },
 * 			category = { "Transportation", "Urban Modeling" },
 * 			doc = @doc (
 * 					value = "Vehicle agent for traffic simulation",
 * 					examples = { @example ("create vehicle number: 50;"),
 * 							&#64;example ("ask vehicle { do move speed: 10 heading: 90; }") }))
 * 	public class Vehicle extends AbstractAgent {
 *
 * 		@variable (
 * 				name = "max_speed",
 * 				type = IType.FLOAT,
 * 				init = "50.0")
 * 		&#64;getter ("max_speed")
 * 		public Double getMaxSpeed(final IAgent agent) {
 * 			return (Double) agent.getAttribute("max_speed");
 * 		}
 * 	}
 *
 * 	// Abstract base species for inheritance hierarchies
 * 	&#64;species (
 * 			name = "social_agent",
 * 			skills = { "moving", "communicating" },
 * 			concept = { IConcept.SPECIES, IConcept.SOCIAL },
 * 			internal = true, // Base class, not directly instantiated
 * 			doc = @doc ("Abstract base for agents with social capabilities"))
 * 	public abstract class SocialAgent extends AbstractAgent {
 * 		// Common social behavior definitions
 * 	}
 * }
 * </pre>
 *
 * <p>
 * <strong>Species Lifecycle:</strong>
 * </p>
 * <ol>
 * <li><strong>Registration:</strong> Species are registered with GAMA's type system during compilation</li>
 * <li><strong>Instantiation:</strong> Agents are created using the {@code create} statement</li>
 * <li><strong>Initialization:</strong> Agent attributes are set and skills are attached</li>
 * <li><strong>Execution:</strong> Agents execute scheduled behaviors during simulation steps</li>
 * <li><strong>Disposal:</strong> Agents are removed when they die or the simulation ends</li>
 * </ol>
 *
 * <p>
 * <strong>Skill Integration:</strong>
 * </p>
 * Species can acquire behaviors through multiple mechanisms:
 * <ul>
 * <li><strong>Explicit Skills:</strong> Listed in the {@code skills} property</li>
 * <li><strong>Auto-attached Skills:</strong> Skills that automatically attach to certain species</li>
 * <li><strong>Inherited Skills:</strong> Skills inherited from parent species</li>
 * <li><strong>Runtime Skills:</strong> Skills added dynamically during simulation</li>
 * </ul>
 *
 * <p>
 * <strong>Performance Considerations:</strong>
 * </p>
 * <ul>
 * <li>Species definition overhead is minimal and occurs at compilation time</li>
 * <li>Agent creation performance depends on the number and complexity of attached skills</li>
 * <li>Inheritance hierarchies should be kept reasonably shallow</li>
 * <li>Use composition (skills) over deep inheritance for better maintainability</li>
 * </ul>
 *
 * @see skill For behavioral component definitions
 * @see action For species behavior definitions
 * @see vars For species attribute definitions
 * @see doc For documentation metadata
 *
 * @author drogoul
 * @author GAMA Development Team
 * @since GAMA 1.0 (June 2, 2012)
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
public @interface species {

	/**
	 * The unique name of this species as it appears in GAML.
	 *
	 * <p>
	 * Species names must be unique across the entire GAMA platform and should be descriptive of the agent type they
	 * represent. Names are used for agent creation, type checking, and model documentation.
	 * </p>
	 *
	 * <p>
	 * <strong>Naming Guidelines:</strong>
	 * </p>
	 * <ul>
	 * <li>Use descriptive names that clearly identify the agent type</li>
	 * <li>Follow domain terminology when appropriate (e.g., "predator", "vehicle")</li>
	 * <li>Use lowercase with underscores for multi-word names</li>
	 * <li>Avoid overly generic names like "object" or "thing"</li>
	 * <li>Consider the model context and user expectations</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Usage in GAML:</strong>
	 * </p>
	 * The species name is used throughout GAML for:
	 * <ul>
	 * <li><strong>Creation:</strong> {@code create my_species number: 10;}</li>
	 * <li><strong>Queries:</strong> {@code list<my_species> agents <- my_species at_distance 50;}</li>
	 * <li><strong>Type Checking:</strong> {@code my_species target_agent;}</li>
	 * <li><strong>Collections:</strong> {@code list<my_species> species_list;}</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Examples:</strong>
	 * </p>
	 * <ul>
	 * <li>{@code "animal"} - Generic biological agent</li>
	 * <li>{@code "predator"} - Hunting agent in ecological models</li>
	 * <li>{@code "vehicle"} - Transportation agent</li>
	 * <li>{@code "household"} - Social/economic agent</li>
	 * <li>{@code "fire_cell"} - Environmental/cellular agent</li>
	 * </ul>
	 *
	 * @return the unique species name in GAML
	 */
	String name();

	/**
	 * Keywords associated with this species for documentation and search purposes.
	 *
	 * <p>
	 * Concepts help users discover species through the documentation search system and provide semantic categorization.
	 * They should reflect the species' role, domain, and behavioral characteristics.
	 * </p>
	 *
	 * <p>
	 * <strong>Concept Categories:</strong>
	 * </p>
	 * <ul>
	 * <li><strong>Structural:</strong> {@code IConcept.SPECIES}, {@code IConcept.AGENT}</li>
	 * <li><strong>Behavioral:</strong> {@code IConcept.AGENT_MOVEMENT}, {@code IConcept.SOCIAL}</li>
	 * <li><strong>Domain:</strong> {@code IConcept.ECOLOGY}, {@code IConcept.TRANSPORT}</li>
	 * <li><strong>Functional:</strong> {@code IConcept.PREDATION}, {@code IConcept.COMMUNICATION}</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Usage Examples:</strong>
	 * </p>
	 * <ul>
	 * <li>Ecological agent: {@code { IConcept.SPECIES, IConcept.ECOLOGY, IConcept.PREDATION }}</li>
	 * <li>Social agent: {@code { IConcept.SPECIES, IConcept.SOCIAL, IConcept.COMMUNICATION }}</li>
	 * <li>Mobile agent: {@code { IConcept.SPECIES, IConcept.AGENT_MOVEMENT }}</li>
	 * </ul>
	 *
	 * @return array of concept keywords for documentation search
	 * @see gama.annotations.support.IConcept For predefined concept constants
	 */
	String[] concept() default {};

	/**
	 * Skills that are automatically attached to agents of this species.
	 *
	 * <p>
	 * Skills provide modular behaviors that agents can use. By listing skills here, all agents created from this
	 * species will automatically have access to the actions and variables defined in those skills.
	 * </p>
	 *
	 * <p>
	 * <strong>Skill Selection Strategy:</strong>
	 * </p>
	 * <ul>
	 * <li><strong>Essential Behaviors:</strong> Include skills that define core agent capabilities</li>
	 * <li><strong>Domain Requirements:</strong> Add skills specific to the model domain</li>
	 * <li><strong>Minimal Set:</strong> Avoid over-loading with unnecessary skills</li>
	 * <li><strong>Dependency Awareness:</strong> Consider skill interactions and dependencies</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Common Skill Combinations:</strong>
	 * </p>
	 * <ul>
	 * <li><strong>Mobile Agents:</strong> {@code {"moving", "perception"}}</li>
	 * <li><strong>Social Agents:</strong> {@code {"moving", "communicating", "social"}}</li>
	 * <li><strong>Predators:</strong> {@code {"moving", "hunting", "perception", "energy"}}</li>
	 * <li><strong>Vehicles:</strong> {@code {"moving", "traffic", "pathfinding"}}</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Usage Example:</strong>
	 * </p>
	 *
	 * <pre>{@code
	 * &#64;species(
	 *     name = "animal",
	 *     skills = { "moving", "perception", "energy_management" }
	 * )
	 * }</pre>
	 *
	 * @return array of skill names that will be attached to this species
	 * @see skill For skill definition details
	 */
	String[] skills() default {};

	/**
	 * Indicates whether this species is for internal framework use only.
	 *
	 * <p>
	 * Internal species are typically abstract base classes or system-level agent types that are not intended for direct
	 * instantiation by modelers. They provide common functionality for inheritance hierarchies.
	 * </p>
	 *
	 * <p>
	 * <strong>Internal Species Characteristics:</strong>
	 * </p>
	 * <ul>
	 * <li>Often abstract classes that cannot be instantiated directly</li>
	 * <li>Provide common behaviors for inheritance hierarchies</li>
	 * <li>Hidden from standard GAML documentation</li>
	 * <li>Used for framework infrastructure and base types</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Examples of Internal Species:</strong>
	 * </p>
	 * <ul>
	 * <li>Abstract base classes for agent hierarchies</li>
	 * <li>System-level agent types for infrastructure</li>
	 * <li>Template species for code generation</li>
	 * <li>Debugging and profiling agents</li>
	 * </ul>
	 *
	 * @return {@code true} if this species is for internal use only, {@code false} otherwise
	 */
	boolean internal() default false;

	/**
	 * Documentation categories for organizing species in reference materials.
	 *
	 * <p>
	 * Categories provide hierarchical organization for documentation and help users navigate collections of species
	 * based on domain, functionality, or application area.
	 * </p>
	 *
	 * <p>
	 * <strong>Category Guidelines:</strong>
	 * </p>
	 * <ul>
	 * <li>Use established domain terminology when possible</li>
	 * <li>Consider the target audience's perspective</li>
	 * <li>Maintain consistency across related species</li>
	 * <li>Keep category names descriptive but concise</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Common Categories:</strong>
	 * </p>
	 * <ul>
	 * <li><strong>Domain:</strong> "Ecology", "Transportation", "Social", "Economics"</li>
	 * <li><strong>Behavioral:</strong> "Mobile", "Communicating", "Predator-Prey"</li>
	 * <li><strong>Spatial:</strong> "Grid-based", "Continuous Space", "Network"</li>
	 * <li><strong>Temporal:</strong> "Scheduled", "Event-driven", "Reactive"</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Examples:</strong>
	 * </p>
	 * <ul>
	 * <li>{@code {"Ecology", "Animal Behavior"}} - Ecological agent</li>
	 * <li>{@code {"Transportation", "Urban Modeling"}} - Vehicle agent</li>
	 * <li>{@code {"Social", "Economics"}} - Human/household agent</li>
	 * </ul>
	 *
	 * @return array of category names for documentation organization
	 */
	String[] category() default {};

	/**
	 * Comprehensive documentation for this species.
	 *
	 * <p>
	 * Species documentation should provide a clear overview of the agent type, its role in models, the behaviors it
	 * provides, and guidelines for effective use. Include examples showing typical creation and usage patterns.
	 * </p>
	 *
	 * <p>
	 * <strong>Documentation Content:</strong>
	 * </p>
	 * <ul>
	 * <li><strong>Purpose:</strong> What role this agent type plays in models</li>
	 * <li><strong>Behaviors:</strong> Key actions and capabilities provided</li>
	 * <li><strong>Attributes:</strong> Important variables and their meanings</li>
	 * <li><strong>Usage:</strong> How to create and configure agents of this type</li>
	 * <li><strong>Examples:</strong> Practical demonstrations of species usage</li>
	 * <li><strong>Relationships:</strong> How this species interacts with others</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Example Documentation:</strong>
	 * </p>
	 *
	 * <pre>{@code
	 * &#64;doc(
	 *     value = "Predator agent that hunts prey in ecological simulations",
	 *     examples = {
	 *         &#64;example("create predator number: 10;"),
	 *         &#64;example("ask predator { do hunt; }")
	 *     },
	 *     see = { "prey", "moving", "hunting" }
	 * )
	 * }</pre>
	 *
	 * @return the documentation for this species
	 * @see doc For documentation specification details
	 */
	doc[] doc() default {};
}
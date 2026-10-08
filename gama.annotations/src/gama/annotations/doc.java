/**
 * 
 */
package gama.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The {@code @doc} annotation provides a comprehensive documentation framework for GAML elements.
 * It serves as the foundation for generating user-facing documentation, IDE assistance, and
 * runtime help systems throughout the GAMA platform.
 *
 * <p><strong>Architecture:</strong></p>
 * The documentation system operates at multiple levels:
 * <ul>
 *   <li><strong>Compile-time:</strong> Annotation processors extract documentation to generate help files</li>
 *   <li><strong>Runtime:</strong> Documentation is available for dynamic help and validation</li>
 *   <li><strong>IDE Integration:</strong> Provides content for autocompletion and hover information</li>
 *   <li><strong>Web Documentation:</strong> Generates online reference materials</li>
 * </ul>
 *
 * <p><strong>Usage Patterns:</strong></p>
 * <pre>{@code
 * // Basic documentation for an action
 * @action(
 *     name = "move_to",
 *     args = { @arg(name = "target", type = IType.POINT) },
 *     doc = @doc(
 *         value = "Moves the agent to the specified target location",
 *         returns = "true if movement was successful, false otherwise",
 *         examples = {
 *             @example("do move_to target: {10, 20};"),
 *             @example("bool success <- move_to({50, 30});")
 *         }
 *     )
 * )
 * 
 * // Documentation with special cases and side effects
 * @action(
 *     name = "reproduce",
 *     doc = @doc(
 *         value = "Creates a new offspring agent with inherited characteristics",
 *         side_effects = "Creates a new agent in the population; modifies parent's energy",
 *         special_cases = {
 *             "If energy < 50, reproduction fails",
 *             "Offspring inherits 50% of parent's energy"
 *         },
 *         see = {"die", "energy", "offspring"}
 *     )
 * )
 * 
 * // Deprecated element documentation
 * @action(
 *     name = "old_method", 
 *     doc = @doc(
 *         value = "Legacy method for backward compatibility",
 *         deprecated = "Use new_method instead. This method will be removed in GAMA 2.0",
 *         see = "new_method"
 *     )
 * )
 * 
 * // Complex documentation with multiple usage patterns
 * @action(
 *     name = "pathfind",
 *     doc = @doc(
 *         value = "Finds optimal path between two points using A* algorithm",
 *         returns = "Path object containing sequence of waypoints, or null if no path found",
 *         usages = {
 *             @usage(
 *                 value = "Basic pathfinding between two points",
 *                 examples = @example("path my_path <- pathfind(source: location, target: food_location);")
 *             ),
 *             @usage(
 *                 value = "Pathfinding with obstacles",
 *                 examples = @example("path my_path <- pathfind(source: location, target: goal, obstacles: walls);")
 *             )
 *         },
 *         see = {"goto", "path", "topology"}
 *     )
 * )
 * }</pre>
 *
 * <p><strong>Documentation Quality Guidelines:</strong></p>
 * <ul>
 *   <li><strong>Clarity:</strong> Write clear, concise descriptions accessible to modelers</li>
 *   <li><strong>Completeness:</strong> Document all parameters, return values, and side effects</li>
 *   <li><strong>Examples:</strong> Provide practical, runnable examples</li>
 *   <li><strong>Cross-references:</strong> Link to related concepts and elements</li>
 *   <li><strong>Consistency:</strong> Follow established documentation patterns</li>
 * </ul>
 *
 * <p><strong>Master Documentation:</strong></p>
 * When multiple instances of the same element exist (e.g., overloaded operators),
 * one can be marked as the master using {@code masterDoc = true}. The master
 * documentation will be used as the primary reference, with other instances
 * contributing supplementary information.
 *
 * @see example For code example specifications
 * @see usage For usage pattern documentation  
 * @see action For action documentation
 * @see variable For variable documentation
 * 
 * @author Benoit Gaudou
 * @author GAMA Development Team
 * @since GAMA 1.0 (June 2, 2012)
 */

@Retention (RetentionPolicy.RUNTIME)
// @Target({ ElementType.TYPE, ElementType.METHOD })
// @Inherited
public @interface doc {

	/**
	 * Documents any side effects that the annotated element might have on its operands or the simulation state.
	 * 
	 * <p>Side effects are important for users to understand the full impact of using an element.
	 * They should be clearly documented to help modelers make informed decisions about when
	 * and how to use specific actions or operators.</p>
	 * 
	 * <p><strong>Types of Side Effects to Document:</strong></p>
	 * <ul>
	 *   <li><strong>State Changes:</strong> Modifications to agent attributes or global variables</li>
	 *   <li><strong>Population Changes:</strong> Creation or removal of agents</li>
	 *   <li><strong>Spatial Changes:</strong> Updates to spatial structures or topologies</li>
	 *   <li><strong>Resource Consumption:</strong> Energy usage, memory allocation</li>
	 *   <li><strong>Network Effects:</strong> Communications or relationship changes</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "Decreases agent's energy by movement cost"}</li>
	 *   <li>{@code "Creates a new agent in the current species"}</li>
	 *   <li>{@code "Modifies the spatial index; affects neighbor queries"}</li>
	 *   <li>{@code "Sends message to all agents in communication range"}</li>
	 * </ul>
	 *
	 * @return description of side effects, or empty string if none
	 */
	String side_effects() default "";

	/**
	 * The primary documentation text describing the purpose and behavior of the annotated element.
	 * 
	 * <p>This is the main description that appears in documentation and help systems.
	 * It should be clear, concise, and provide enough detail for users to understand
	 * what the element does and when to use it.</p>
	 * 
	 * <p><strong>Writing Guidelines:</strong></p>
	 * <ul>
	 *   <li>Start with a clear, action-oriented description</li>
	 *   <li>Explain the purpose and expected behavior</li>
	 *   <li>Mention any important constraints or requirements</li>
	 *   <li>Use present tense and active voice</li>
	 *   <li>Keep it concise but comprehensive</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "Moves the agent to the specified target location"}</li>
	 *   <li>{@code "Returns the distance between two geometric objects"}</li>
	 *   <li>{@code "Creates a new population of agents with given characteristics"}</li>
	 * </ul>
	 *
	 * @return the main documentation text
	 */
	String value() default "";

	/**
	 * Indicates whether this documentation instance is the master documentation for the element.
	 * 
	 * <p>In cases where multiple instances of the same element exist (such as overloaded
	 * operators or actions), one instance should be designated as the master. The master
	 * documentation serves as the primary reference and its content takes precedence
	 * in documentation generation.</p>
	 * 
	 * <p><strong>Use Cases for Master Documentation:</strong></p>
	 * <ul>
	 *   <li>Operator overloads with different parameter types</li>
	 *   <li>Actions available in multiple skills or species</li>
	 *   <li>Variables with different contexts but same name</li>
	 * </ul>
	 * 
	 * <p>When multiple elements have {@code masterDoc = true}, the documentation
	 * system will merge their information, with later declarations potentially
	 * overriding earlier ones.</p>
	 *
	 * @return {@code true} if this is the master documentation, {@code false} otherwise
	 */
	boolean masterDoc() default false;

	/**
	 * Indicates that the documented element is deprecated and may be removed in future versions.
	 * 
	 * <p>Deprecated elements should include clear guidance about alternatives to help
	 * users migrate their code. The deprecation notice appears prominently in
	 * documentation and may generate warnings in development tools.</p>
	 * 
	 * <p><strong>Deprecation Best Practices:</strong></p>
	 * <ul>
	 *   <li>Clearly state that the element is deprecated</li>
	 *   <li>Provide specific alternatives or migration paths</li>
	 *   <li>Include version information when possible</li>
	 *   <li>Explain the reason for deprecation if relevant</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "Use move_to action instead. Will be removed in GAMA 2.0"}</li>
	 *   <li>{@code "Replaced by improved_algorithm. The old algorithm is less efficient"}</li>
	 *   <li>{@code "Use the new parameter syntax: action(param: value)"}</li>
	 * </ul>
	 *
	 * @return deprecation notice with suggested alternatives, or empty string if not deprecated
	 */
	String deprecated() default "";

	/**
	 * Documents the return value(s) of the annotated element.
	 * 
	 * <p>For actions, operators, and getters, this describes what value is returned
	 * and under what conditions. Include type information, possible value ranges,
	 * and any special return conditions.</p>
	 * 
	 * <p><strong>Return Documentation Guidelines:</strong></p>
	 * <ul>
	 *   <li>Specify the return type and its meaning</li>
	 *   <li>Describe possible return values and their significance</li>
	 *   <li>Document error conditions and their return values</li>
	 *   <li>Explain null returns and empty collections</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "Returns true if movement was successful, false otherwise"}</li>
	 *   <li>{@code "Returns the calculated distance as a float, or 0.0 if points are identical"}</li>
	 *   <li>{@code "Returns a list of nearby agents, or empty list if none found"}</li>
	 *   <li>{@code "Returns null if the target is unreachable"}</li>
	 * </ul>
	 *
	 * @return documentation of the return value(s)
	 */
	String returns() default "";

	/**
	 * An optional comment that provides additional context or implementation details.
	 * 
	 * <p>Comments appear separately from the main documentation and are typically
	 * used for technical notes, implementation details, or additional context
	 * that doesn't fit in the primary description.</p>
	 * 
	 * <p><strong>Appropriate Comment Content:</strong></p>
	 * <ul>
	 *   <li>Implementation algorithms or performance characteristics</li>
	 *   <li>Historical context or design rationale</li>
	 *   <li>Technical limitations or platform-specific behavior</li>
	 *   <li>Advanced usage notes for expert users</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "Uses A* algorithm for optimal pathfinding"}</li>
	 *   <li>{@code "Performance degrades with large populations (>10000 agents)"}</li>
	 *   <li>{@code "Thread-safe implementation suitable for parallel execution"}</li>
	 * </ul>
	 *
	 * @return additional technical or contextual information
	 */
	String comment() default "";

	/**
	 * Documents special cases or edge conditions where the element behaves differently.
	 * 
	 * <p>Special cases help users understand exceptional behavior and avoid common
	 * pitfalls. They should cover boundary conditions, error cases, and any
	 * non-obvious behaviors.</p>
	 * 
	 * <p><strong>Types of Special Cases:</strong></p>
	 * <ul>
	 *   <li><strong>Boundary Conditions:</strong> Behavior at limits or extremes</li>
	 *   <li><strong>Error Handling:</strong> What happens with invalid inputs</li>
	 *   <li><strong>Performance:</strong> Conditions affecting execution speed</li>
	 *   <li><strong>Platform Differences:</strong> OS or system-specific behavior</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "Returns null if the target is outside the world bounds"}</li>
	 *   <li>{@code "Throws exception if energy is negative"}</li>
	 *   <li>{@code "Performance decreases significantly with distance > 1000"}</li>
	 * </ul>
	 *
	 * @return array of special case descriptions
	 */
	String[] special_cases() default {};

	/**
	 * Provides practical examples showing how to use the annotated element.
	 * 
	 * <p>Examples are crucial for user understanding and should demonstrate
	 * realistic usage patterns. They should be syntactically correct and
	 * executable in appropriate contexts.</p>
	 * 
	 * <p><strong>Example Guidelines:</strong></p>
	 * <ul>
	 *   <li>Provide multiple examples showing different use cases</li>
	 *   <li>Include both simple and complex usage patterns</li>
	 *   <li>Use realistic variable names and values</li>
	 *   <li>Show common parameter combinations</li>
	 *   <li>Demonstrate integration with other GAML features</li>
	 * </ul>
	 *
	 * @return array of usage examples
	 * @see example For detailed example specifications
	 */
	example[] examples() default {};

	/**
	 * Cross-references to related elements in the GAML ecosystem.
	 * 
	 * <p>Cross-references help users discover related functionality and understand
	 * how different elements work together. They create a web of connections
	 * that enhances documentation navigation.</p>
	 * 
	 * <p><strong>Types of Cross-References:</strong></p>
	 * <ul>
	 *   <li><strong>Related Actions:</strong> Actions that perform similar or complementary functions</li>
	 *   <li><strong>Associated Variables:</strong> Variables that are commonly used together</li>
	 *   <li><strong>Required Skills:</strong> Skills needed to use certain actions</li>
	 *   <li><strong>Alternative Approaches:</strong> Different ways to achieve similar results</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>For a movement action: {@code {"goto", "wander", "follow", "location", "speed"}}</li>
	 *   <li>For an energy variable: {@code {"die", "reproduce", "metabolism", "feed"}}</li>
	 *   <li>For a species: {@code {"skill_name", "related_species", "common_actions"}}</li>
	 * </ul>
	 *
	 * @return array of cross-references to related GAML elements
	 */
	String[] see() default {};

	/**
	 * Defines different usage patterns or contexts for the annotated element.
	 * 
	 * <p>Usages provide structured documentation for elements that can be used
	 * in multiple ways or contexts. Each usage describes a specific pattern
	 * with its own examples and explanations.</p>
	 * 
	 * <p><strong>When to Use Multiple Usages:</strong></p>
	 * <ul>
	 *   <li>Elements with significantly different parameter patterns</li>
	 *   <li>Actions that serve multiple distinct purposes</li>
	 *   <li>Operators with context-dependent behavior</li>
	 *   <li>Complex elements requiring step-by-step explanation</li>
	 * </ul>
	 * 
	 * <p>Each usage should have a clear description and relevant examples
	 * that demonstrate the specific pattern being documented.</p>
	 *
	 * @return array of usage patterns for this element
	 * @see usage For individual usage pattern specifications
	 */
	usage[] usages() default {};

}
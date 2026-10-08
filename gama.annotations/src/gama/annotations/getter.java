/**
 * 
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @getter} annotation marks methods that serve as accessors for GAML variables.
 * This annotation establishes the connection between Java methods and GAML variable access,
 * enabling transparent property access from the modeling language.
 * 
 * <p><strong>Requirements:</strong></p>
 * <ul>
 *   <li>The annotated method must be public and non-static</li>
 *   <li>The method should follow getter conventions (no parameters, appropriate return type)</li>
 *   <li>The referenced variable must be declared in the same class's {@code @vars} annotation</li>
 *   <li>The method must be in a class annotated with {@code @species} or {@code @skill}</li>
 * </ul>
 * 
 * <p><strong>Architecture:</strong></p>
 * Getters provide the bridge between GAML's declarative variable system and Java's
 * object-oriented property access. The annotation processor generates the necessary
 * metadata to enable seamless variable access from GAML code, including type checking
 * and runtime optimization.
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * // Basic getter for a simple variable
 * @vars({ @variable(name = "energy", type = IType.FLOAT, init = "100.0") })
 * public class MySpecies {
 *     
 *     @getter("energy")
 *     public Double getEnergy(final IAgent agent) {
 *         return (Double) agent.getAttribute("energy");
 *     }
 * }
 * 
 * // Getter with computation
 * @getter("total_score")
 * public Double getTotalScore(final IAgent agent) {
 *     Double base = (Double) agent.getAttribute("base_score");
 *     Double bonus = (Double) agent.getAttribute("bonus_score");
 *     return base + bonus;
 * }
 * 
 * // Getter that also serves as initializer
 * @getter(value = "location", initializer = true)
 * public IPoint getLocation(final IAgent agent) {
 *     IPoint loc = agent.getLocation();
 *     if (loc == null) {
 *         loc = agent.getScope().getRandom().getRandomPointInBounds();
 *         agent.setLocation(loc);
 *     }
 *     return loc;
 * }
 * }</pre>
 * 
 * <p><strong>Initializer Behavior:</strong></p>
 * When {@code initializer = true}, the getter method will be called during agent
 * initialization to establish the initial value of the variable. This is particularly
 * useful for computed properties or complex initialization logic.
 * 
 * <p><strong>Performance Considerations:</strong></p>
 * <ul>
 *   <li>Getters are called frequently during simulation - keep them lightweight</li>
 *   <li>Consider caching computed values if calculation is expensive</li>
 *   <li>Use primitive types where possible to avoid boxing overhead</li>
 * </ul>
 * 
 * @see vars For variable declarations
 * @see setter For variable modification methods
 * @see variable For individual variable definitions
 * 
 * @author GAMA Development Team
 * @since GAMA 1.0
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface getter {

	/**
	 * Specifies the name of the GAML variable for which this method serves as a getter.
	 * 
	 * <p>The variable name must exactly match a variable declared in the {@code @vars}
	 * annotation of the same class. This creates a bidirectional binding between the
	 * GAML variable declaration and the Java accessor method.</p>
	 * 
	 * <p><strong>Naming Conventions:</strong></p>
	 * <ul>
	 *   <li>Use lowercase with underscores for multi-word names</li>
	 *   <li>Choose descriptive, meaningful names</li>
	 *   <li>Maintain consistency with GAML naming standards</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "energy"} - Simple property</li>
	 *   <li>{@code "current_target"} - Compound name</li>
	 *   <li>{@code "is_active"} - Boolean property</li>
	 * </ul>
	 *
	 * @return the name of the variable for which the annotated method serves as a getter
	 */
	String value();

	/**
	 * Indicates whether this getter should also function as an initializer for the variable.
	 * 
	 * <p>When set to {@code true}, this method will be called during agent initialization
	 * to establish the initial value of the associated variable. This is particularly
	 * useful for:</p>
	 * 
	 * <ul>
	 *   <li><strong>Computed Initial Values:</strong> Variables whose initial value depends on other properties</li>
	 *   <li><strong>Complex Initialization:</strong> Properties requiring non-trivial setup logic</li>
	 *   <li><strong>Dynamic Defaults:</strong> Initial values that vary based on simulation context</li>
	 *   <li><strong>Lazy Initialization:</strong> Properties initialized only when first accessed</li>
	 * </ul>
	 * 
	 * <p><strong>Execution Context:</strong></p>
	 * Initializer getters are called after the agent's basic construction but before
	 * the agent becomes active in the simulation. They have access to the agent's
	 * scope and other initialized properties.
	 * 
	 * <p><strong>Example Use Cases:</strong></p>
	 * <pre>{@code
	 * // Location initialized based on species habitat
	 * @getter(value = "location", initializer = true)
	 * public IPoint getLocation(final IAgent agent) {
	 *     String habitat = (String) agent.getAttribute("preferred_habitat");
	 *     return findLocationInHabitat(habitat);
	 * }
	 * 
	 * // ID generated during initialization
	 * @getter(value = "unique_id", initializer = true)
	 * public String getUniqueId(final IAgent agent) {
	 *     return generateUniqueId(agent.getSpecies().getName());
	 * }
	 * }</pre>
	 *
	 * @return {@code true} if this getter should also be used as an initializer, {@code false} otherwise
	 */
	boolean initializer() default false;

}
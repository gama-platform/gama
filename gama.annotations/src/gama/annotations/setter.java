/**
 * 
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @setter} annotation marks methods that handle variable assignment in GAML.
 * This annotation enables custom logic to be executed when GAML variables are modified,
 * providing control over validation, side effects, and state consistency.
 * 
 * <p><strong>Requirements:</strong></p>
 * <ul>
 *   <li>The annotated method must be public and non-static</li>
 *   <li>The method should accept appropriate parameters (typically agent and new value)</li>
 *   <li>The referenced variable must be declared in the same class's {@code @vars} annotation</li>
 *   <li>The method must be in a class annotated with {@code @species} or {@code @skill}</li>
 * </ul>
 * 
 * <p><strong>Architecture:</strong></p>
 * Setters provide programmatic control over variable assignment in GAML. They enable
 * validation, transformation, notification, and side-effect management when variables
 * are modified. The annotation processor ensures proper integration with GAML's
 * assignment operations.
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * // Basic setter with validation
 * @setter("energy")
 * public void setEnergy(final IAgent agent, final Double value) {
 *     if (value < 0) {
 *         throw new GamaRuntimeException("Energy cannot be negative");
 *     }
 *     agent.setAttribute("energy", Math.min(value, 100.0)); // Cap at maximum
 * }
 * 
 * // Setter with side effects
 * @setter("location") 
 * public void setLocation(final IAgent agent, final IPoint newLocation) {
 *     IPoint oldLocation = agent.getLocation();
 *     agent.setLocation(newLocation);
 *     
 *     // Trigger movement-related updates
 *     updateSpatialIndex(agent, oldLocation, newLocation);
 *     notifyNeighbors(agent, newLocation);
 *     
 *     // Update heading based on movement
 *     if (oldLocation != null) {
 *         Double heading = computeHeading(oldLocation, newLocation);
 *         agent.setAttribute("heading", heading);
 *     }
 * }
 * 
 * // Setter with transformation
 * @setter("status")
 * public void setStatus(final IAgent agent, final String status) {
 *     String normalizedStatus = status.toLowerCase().trim();
 *     agent.setAttribute("status", normalizedStatus);
 *     
 *     // Update derived properties based on status
 *     updateBehaviorState(agent, normalizedStatus);
 * }
 * 
 * // Setter for complex objects with deep validation
 * @setter("target")
 * public void setTarget(final IAgent agent, final IAgent target) {
 *     if (target != null && target.isDead()) {
 *         // Clear invalid target
 *         agent.setAttribute("target", null);
 *         return;
 *     }
 *     
 *     IAgent oldTarget = (IAgent) agent.getAttribute("target");
 *     agent.setAttribute("target", target);
 *     
 *     // Manage target relationships
 *     if (oldTarget != null) {
 *         removeFromTargetList(oldTarget, agent);
 *     }
 *     if (target != null) {
 *         addToTargetList(target, agent);
 *     }
 * }
 * }</pre>
 * 
 * <p><strong>Common Patterns:</strong></p>
 * <ul>
 *   <li><strong>Validation:</strong> Check value bounds, types, and business rules</li>
 *   <li><strong>Transformation:</strong> Normalize or convert values before storage</li>
 *   <li><strong>Notification:</strong> Inform other components of state changes</li>
 *   <li><strong>Side Effects:</strong> Update dependent properties or trigger actions</li>
 *   <li><strong>Consistency:</strong> Maintain invariants across related variables</li>
 * </ul>
 * 
 * <p><strong>Performance Considerations:</strong></p>
 * <ul>
 *   <li>Setters may be called frequently - keep them efficient</li>
 *   <li>Avoid expensive operations in setters unless necessary</li>
 *   <li>Consider lazy evaluation for complex side effects</li>
 *   <li>Be mindful of circular dependencies in multi-variable updates</li>
 * </ul>
 * 
 * @see vars For variable declarations
 * @see getter For variable access methods  
 * @see variable For individual variable definitions
 * 
 * @author GAMA Development Team
 * @since GAMA 1.0
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface setter {

	/**
	 * Specifies the name of the GAML variable for which this method serves as a setter.
	 * 
	 * <p>The variable name must exactly match a variable declared in the {@code @vars}
	 * annotation of the same class. This creates a bidirectional binding between the
	 * GAML variable declaration and the Java setter method.</p>
	 * 
	 * <p><strong>Naming Conventions:</strong></p>
	 * <ul>
	 *   <li>Use lowercase with underscores for multi-word names</li>
	 *   <li>Choose descriptive, meaningful names</li>
	 *   <li>Maintain consistency with corresponding getter names</li>
	 *   <li>Follow GAML naming standards</li>
	 * </ul>
	 * 
	 * <p><strong>Integration with Assignment:</strong></p>
	 * When GAML code executes an assignment like {@code my_variable <- new_value;},
	 * the runtime system will:
	 * <ol>
	 *   <li>Resolve the variable name to find the associated setter</li>
	 *   <li>Perform type checking and conversion if necessary</li>
	 *   <li>Call the setter method with the agent and new value</li>
	 *   <li>Handle any exceptions or validation failures</li>
	 * </ol>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "energy"} - Simple property</li>
	 *   <li>{@code "current_target"} - Compound name</li>
	 *   <li>{@code "is_active"} - Boolean property</li>
	 * </ul>
	 *
	 * @return the name of the variable for which the annotated method serves as a setter
	 */
	String value();
}
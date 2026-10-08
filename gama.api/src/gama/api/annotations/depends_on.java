/*******************************************************************************************************
 *
 * depends_on.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention (RUNTIME)
@Target (METHOD)
/**
 * Specifies implicit dependencies for operators on model attributes that must be initialized before the operator can
 * be executed.
 * 
 * <p>
 * This annotation is used to declare that an operator relies on certain model attributes being initialized before it
 * can function correctly. This is particularly important for operators that access global simulation properties or
 * geometric data that may not be available during early initialization phases.
 * </p>
 * 
 * <h2>Purpose</h2>
 * <p>
 * The annotation helps the GAMA platform understand the initialization order requirements and can provide better error
 * messages when operators are used before their dependencies are ready.
 * </p>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Example 1: Operator depending on world shape</h3>
 * <pre>{@code
 * @operator(value = "cone", ...)
 * @depends_on(value = {"shape"})
 * public static IShape cone(IScope scope, ...) {
 *     // This operator needs the world shape to be initialized
 *     // before it can compute cone geometries
 *     return ...;
 * }
 * }</pre>
 * 
 * <h3>Example 2: Operator depending on multiple attributes</h3>
 * <pre>{@code
 * @operator(value = "custom_grid_operation", ...)
 * @depends_on(value = {"shape", "cell_width", "cell_height"})
 * public static Object customGridOp(IScope scope, ...) {
 *     // This operator requires the world shape and grid parameters
 *     // to be initialized first
 *     return ...;
 * }
 * }</pre>
 * 
 * <h3>Example 3: Operator without dependencies</h3>
 * <pre>{@code
 * @operator(value = "simple_math", ...)
 * @depends_on // No dependencies - can be used anytime
 * public static Double simpleMath(IScope scope, Double a, Double b) {
 *     return a + b;
 * }
 * }</pre>
 * 
 * <h2>When to Use</h2>
 * <ul>
 *   <li>When an operator accesses global simulation properties (e.g., world shape, environment bounds)</li>
 *   <li>When an operator relies on specific model attributes being initialized</li>
 *   <li>When an operator performs geometric operations that depend on the simulation space being defined</li>
 *   <li>When initialization order matters for correct operator behavior</li>
 * </ul>
 * 
 * <h2>Common Attributes to Depend On</h2>
 * <ul>
 *   <li>{@code shape} - The world/environment shape</li>
 *   <li>{@code cell_width}, {@code cell_height} - Grid cell dimensions</li>
 *   <li>{@code environment} - The simulation environment</li>
 *   <li>Any custom global attributes defined in the model</li>
 * </ul>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see gama.gaml.operators.IOperator
 */
public @interface depends_on {

	/**
	 * The names of attributes that must be initialized before this operator can be used.
	 * 
	 * <p>
	 * Specifies an array of attribute names (as strings) that this operator depends on. These attributes should be
	 * initialized in the model before the operator is invoked. If left empty (default), the operator has no implicit
	 * dependencies.
	 * </p>
	 * 
	 * @return an array of attribute names that this operator depends on, or an empty array if there are no
	 *         dependencies
	 */
	String[] value() default {};
}

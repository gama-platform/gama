/**
 *
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @action} annotation is used to mark methods that will be exposed as actions (primitives) in the GAML
 * modeling language. Actions represent behaviors that agents can perform during simulation.
 *
 * <p>
 * <strong>Requirements:</strong>
 * </p>
 * <ul>
 * <li>The annotated method must have the signature: {@code Object methodName(IScope) throws GamaRuntimeException}</li>
 * <li>The method must be contained in a class annotated with {@code @species} or {@code @skill}</li>
 * <li>The method should be public for proper accessibility</li>
 * </ul>
 *
 * <p>
 * <strong>Architecture:</strong>
 * </p>
 * Actions are fundamental building blocks in GAMA's agent-based modeling framework. They provide the behavioral
 * capabilities that agents can execute during simulation steps. The annotation processing system automatically
 * registers these methods as available GAML primitives.
 *
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 *
 * <pre>
 * {@code
 * // Simple action without parameters
 * &#64;action(name = "move_randomly")
 * public Object moveRandomly(final IScope scope) throws GamaRuntimeException {
 *     // Implementation here
 *     return null;
 * }
 *
 * // Action with parameters and documentation
 * &#64;action(
 *     name = "move_to_target",
 *     args = {
 *         &#64;arg(name = "target", type = IType.POINT, doc = @doc("The target location")),
 *         &#64;arg(name = "speed", type = IType.FLOAT, optional = true, doc = @doc("Movement speed"))
 *     },
 *     doc = @doc(
 *         value = "Moves the agent to the specified target location",
 *         examples = @example("do move_to_target target: {10, 20} speed: 5.0;")
 *     )
 * )
 * public Object moveToTarget(final IScope scope) throws GamaRuntimeException {
 *     // Implementation here
 *     return null;
 * }
 *
 * // Virtual (abstract) action for inheritance
 * @action(name = "abstract_behavior", virtual = true)
 * public abstract Object abstractBehavior(final IScope scope) throws GamaRuntimeException;
 * }
 * </pre>
 *
 * <p>
 * <strong>Processing:</strong>
 * </p>
 * During compilation, the annotation processor:
 * <ol>
 * <li>Validates method signatures and class annotations</li>
 * <li>Generates metadata for GAML runtime registration</li>
 * <li>Creates documentation entries for IDE support</li>
 * <li>Performs type checking for arguments and return values</li>
 * </ol>
 *
 * @see species For agent type definitions
 * @see skill For reusable behavior groupings
 * @see arg For action parameter specifications
 * @see doc For documentation metadata
 *
 * @author GAMA Development Team
 * @since GAMA 1.0
 */
@Retention (RetentionPolicy.CLASS)
@Target (ElementType.METHOD)
public @interface action {

	/**
	 * Specifies the name of the action as it will appear in GAML code.
	 *
	 * <p>
	 * The name must be unique within the scope where it's defined (species or skill). It should follow GAML naming
	 * conventions: lowercase with underscores for separation.
	 * </p>
	 *
	 * <p>
	 * <strong>Examples:</strong>
	 * </p>
	 * <ul>
	 * <li>{@code "move"} - Simple action name</li>
	 * <li>{@code "move_randomly"} - Descriptive action name</li>
	 * <li>{@code "update_energy_level"} - Complex action name</li>
	 * </ul>
	 *
	 * @return the name of the action as it can be used in GAML
	 */
	String name();

	/**
	 * Indicates whether this action is virtual (abstract).
	 *
	 * <p>
	 * Virtual actions are designed to be overridden by subclasses or implementing species. They define a behavioral
	 * contract without providing implementation. This is useful for creating action hierarchies and ensuring consistent
	 * interfaces across different agent types.
	 * </p>
	 *
	 * <p>
	 * <strong>Use Cases:</strong>
	 * </p>
	 * <ul>
	 * <li>Abstract base skills that define common action signatures</li>
	 * <li>Template methods in inheritance hierarchies</li>
	 * <li>Interface definitions for polymorphic behavior</li>
	 * </ul>
	 *
	 * @return {@code true} if this action is virtual/abstract, {@code false} otherwise
	 */
	boolean virtual() default false;

	/**
	 * Defines the list of arguments (parameters) that this action accepts.
	 *
	 * <p>
	 * Each argument is specified using the {@code @arg} annotation, which defines the parameter's name, type,
	 * optionality, and documentation. The order of arguments in this array determines their order in GAML method calls.
	 * </p>
	 *
	 * <p>
	 * <strong>Argument Processing:</strong>
	 * </p>
	 * <ul>
	 * <li>Required arguments must be provided when calling the action</li>
	 * <li>Optional arguments have default values or can be omitted</li>
	 * <li>Type checking is performed at compilation and runtime</li>
	 * <li>Argument names are used for named parameter syntax in GAML</li>
	 * </ul>
	 *
	 * @return the list of arguments passed to this action
	 * @see arg For individual argument specification
	 */
	arg[] args() default {};

	/**
	 * Marks this action for internal use only within the GAMA framework.
	 *
	 * <p>
	 * Internal actions are not intended for direct use by modelers and are typically used for framework-level
	 * operations, debugging, or advanced system features. They may not appear in standard GAML documentation or IDE
	 * autocompletion.
	 * </p>
	 *
	 * <p>
	 * <strong>Internal Action Characteristics:</strong>
	 * </p>
	 * <ul>
	 * <li>Hidden from standard GAML documentation</li>
	 * <li>May have unstable APIs across GAMA versions</li>
	 * <li>Used for system-level operations</li>
	 * <li>Not recommended for general modeling use</li>
	 * </ul>
	 *
	 * @return {@code true} if this action is for internal use only, {@code false} otherwise
	 */
	boolean internal() default false;

	/**
	 * Provides comprehensive documentation for this action.
	 *
	 * <p>
	 * The documentation is automatically processed to generate help content for GAMA's IDE, online documentation, and
	 * runtime assistance. It should include a clear description of the action's purpose, behavior, and usage patterns.
	 * </p>
	 *
	 * <p>
	 * <strong>Documentation Best Practices:</strong>
	 * </p>
	 * <ul>
	 * <li>Provide clear, concise descriptions of action behavior</li>
	 * <li>Include practical usage examples</li>
	 * <li>Document any side effects or state changes</li>
	 * <li>Explain return values and their meaning</li>
	 * <li>Note any dependencies or prerequisites</li>
	 * </ul>
	 *
	 * @return the documentation attached to this action
	 * @see doc For documentation specification details
	 */
	doc[] doc() default {};
}
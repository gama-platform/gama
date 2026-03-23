/*******************************************************************************************************
 *
 * IActionDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.lang.reflect.AccessibleObject;
import java.util.List;

import gama.api.additions.IGamaHelper;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.symbols.Arguments;

/**
 * Description interface for GAML actions (agent behaviors).
 *
 * <p>
 * This interface extends {@link IStatementDescription} to provide additional capabilities specific to actions - named,
 * reusable behaviors that can be invoked on agents. Actions are a fundamental building block of agent behavior in GAMA,
 * supporting both user-defined and built-in (primitive) implementations.
 * </p>
 *
 * <h2>Purpose</h2>
 *
 * <p>
 * IActionDescription enables:
 * </p>
 * <ul>
 * <li><strong>Action Metadata:</strong> Access to action name, arguments, return type, and documentation</li>
 * <li><strong>Argument Management:</strong> Defining and validating action parameters</li>
 * <li><strong>Primitive Actions:</strong> Associating Java implementations with GAML actions</li>
 * <li><strong>Documentation:</strong> Providing help text and examples for the action</li>
 * <li><strong>Invocation Validation:</strong> Checking call sites for correct argument usage</li>
 * </ul>
 *
 * <h2>Action Types</h2>
 *
 * <h3>User-Defined Actions:</h3>
 *
 * <pre>{@code
 * action move(float speed, point target) {
 *     location <- location + (target - location) * speed;
 * }
 * }</pre>
 *
 * <h3>Primitive (Built-in) Actions:</h3>
 *
 * <pre>{@code
 * // Defined in Java and registered with GAMA
 * &#64;action(name = "write", args = {@arg(name = "message", type = IType.STRING)})
 * public void primitiveWrite(IScope scope) { ... }
 * }</pre>
 *
 * <h2>Action Arguments</h2>
 *
 * <p>
 * Actions can define formal arguments with types and optional default values:
 * </p>
 * <ul>
 * <li>Access argument names via {@link #getArgNames()}</li>
 * <li>Check if an argument exists via {@link #containsArg(String)}</li>
 * <li>Validate arguments at call sites via {@link #verifyArgs(IDescription, Arguments)}</li>
 * </ul>
 *
 * <h2>Primitive Actions</h2>
 *
 * <p>
 * Primitive actions are implemented in Java and exposed to GAML:
 * </p>
 * <ul>
 * <li>Associate a Java method via {@link #setHelper(IGamaHelper, AccessibleObject)}</li>
 * <li>Retrieve the helper via {@link #getHelper()}</li>
 * <li>The helper executes the Java implementation when the action is called</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * IActionDescription actionDesc = ...;
 *
 * // Get action metadata
 * String name = actionDesc.getName(); // "move"
 * List<String> argNames = actionDesc.getArgNames(); // ["speed", "target"]
 *
 * // Check if argument exists
 * if (actionDesc.containsArg("speed")) {
 *     // Action has a 'speed' parameter
 * }
 *
 * // Validate a call site
 * Arguments callArgs = ...; // Arguments from "do move(speed: 5.0)"
 * boolean valid = actionDesc.verifyArgs(callerContext, callArgs);
 *
 * // Get documentation
 * IGamlDocumentation doc = actionDesc.getShortDocumentation(true);
 *
 * // For primitive actions
 * IGamaHelper helper = actionDesc.getHelper();
 * if (helper != null) {
 *     // This is a primitive action with Java implementation
 * }
 * }</pre>
 *
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 *
 * @see IStatementDescription
 * @see gama.api.additions.IGamaHelper
 * @see gama.api.gaml.symbols.Arguments
 */
public interface IActionDescription extends IStatementDescription {

	/**
	 * Returns the names of all arguments (parameters) defined by this action.
	 *
	 * <p>
	 * The list includes all formal parameters in the order they are declared.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>{@code
	 * // For: action move(float speed, point target) { ... }
	 * List<String> names = actionDesc.getArgNames();
	 * // Returns: ["speed", "target"]
	 * }</pre>
	 *
	 * @return a list of argument names (never null, but may be empty)
	 */
	List<String> getArgNames();

	/**
	 * Checks if this action has an argument with the given name.
	 *
	 * <p>
	 * This is useful for validating call sites and checking parameter availability.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>{@code
	 * if (actionDesc.containsArg("speed")) {
	 * 	// Action has a 'speed' parameter
	 * }
	 * }</pre>
	 *
	 * @param arg
	 *            the argument name to check for
	 * @return true if the action has an argument with this name, false otherwise
	 */
	boolean containsArg(String arg);

	/**
	 * Returns short documentation for this action.
	 *
	 * <p>
	 * Short documentation is a concise description of the action suitable for tooltips, quick help, and outline views.
	 * It typically includes the action signature and a brief description.
	 * </p>
	 *
	 * @param includeMetaData
	 *            whether to include metadata (defining plugin, etc.) in the documentation
	 * @return short documentation object (never null, but may be empty)
	 */
	IGamlDocumentation getShortDocumentation(boolean includeMetaData);

	/**
	 * Verifies that the arguments passed at a call site match this action's formal parameters.
	 *
	 * <p>
	 * This method validates:
	 * </p>
	 * <ul>
	 * <li>All required arguments are provided</li>
	 * <li>No unknown arguments are passed</li>
	 * <li>Argument types are compatible</li>
	 * <li>Argument expressions are valid in the caller context</li>
	 * </ul>
	 *
	 * <p>
	 * If validation fails, errors are attached to the caller's description.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>{@code
	 * // Validating: do move(speed: 5.0, target: {10, 20});
	 * Arguments passedArgs = ...; // speed: 5.0, target: {10, 20}
	 * boolean valid = actionDesc.verifyArgs(doStatementDesc, passedArgs);
	 * if (!valid) {
	 *     // Errors have been reported to doStatementDesc
	 * }
	 * }</pre>
	 *
	 * @param callerContext
	 *            the description of the statement invoking this action
	 * @param arguments
	 *            the arguments passed at the call site
	 * @return true if all arguments are valid, false if validation errors occurred
	 */
	boolean verifyArgs(IDescription callerContext, Arguments arguments);

	/**
	 * Associates a Java helper and method with this primitive action.
	 *
	 * <p>
	 * This method is used only for primitive (built-in) actions to link the GAML action description with its Java
	 * implementation. The helper will be invoked when the action is executed.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>{@code
	 * // In Java code defining a primitive action
	 * IGamaHelper helper = new MyActionHelper();
	 * Method method = MyClass.class.getMethod("primitiveWrite", IScope.class);
	 * actionDesc.setHelper(helper, method);
	 * }</pre>
	 *
	 * <p>
	 * <b>Default Implementation:</b> Does nothing. Override for primitive actions.
	 * </p>
	 *
	 * @param helper
	 *            the helper object that implements the action logic
	 * @param method
	 *            the Java method that implements the action
	 */
	default void setHelper(final IGamaHelper helper, final AccessibleObject method) {}

	/**
	 * Returns the helper object that implements this primitive action.
	 *
	 * <p>
	 * For primitive (built-in) actions, this returns the Java helper that executes the action logic. For user-defined
	 * actions, this returns null.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>{@code
	 * IGamaHelper helper = actionDesc.getHelper();
	 * if (helper != null) {
	 * 	// This is a primitive action
	 * 	Object result = helper.run(scope, agent, arguments);
	 * }
	 * }</pre>
	 *
	 * <p>
	 * <b>Default Implementation:</b> Returns {@code null}.
	 * </p>
	 *
	 * @return the helper object for primitive actions, or null for user-defined actions
	 */
	default IGamaHelper getHelper() { return null; }

}

/*******************************************************************************************************
 *
 * IStatementDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import gama.api.gaml.symbols.Arguments;

/**
 * Description interface for GAML statements and executable blocks.
 *
 * <p>
 * This interface extends {@link IDescription} to provide additional capabilities specific to executable statements in
 * GAML models. Statements represent behavioral elements such as actions, reflexes, control structures (if, loop), and
 * other executable blocks.
 * </p>
 *
 * <h2>Purpose</h2>
 *
 * <p>
 * IStatementDescription enables:
 * </p>
 * <ul>
 * <li><strong>Formal Arguments:</strong> Access to action/statement parameters</li>
 * <li><strong>Argument Passing:</strong> Tracking of passed arguments at call sites</li>
 * <li><strong>Control Flow:</strong> Identification of super invocations and flow control statements</li>
 * <li><strong>Statement Properties:</strong> Query breakable/continuable properties for loops</li>
 * </ul>
 *
 * <h2>Statement Types</h2>
 *
 * <p>
 * This interface represents various statement categories:
 * </p>
 * <ul>
 * <li><strong>Actions:</strong> Named, reusable behaviors with parameters ({@link IActionDescription})</li>
 * <li><strong>Reflexes:</strong> Reactive behaviors triggered by conditions</li>
 * <li><strong>Control Structures:</strong> if, loop, switch, etc.</li>
 * <li><strong>Commands:</strong> create, ask, do, write, etc.</li>
 * <li><strong>Blocks:</strong> Compound statements grouping multiple statements</li>
 * </ul>
 *
 * <h2>Formal Arguments</h2>
 *
 * <p>
 * Statements can define formal arguments (parameters), particularly for actions:
 * </p>
 *
 * <pre>{@code
 * action move(float speed, point target) {
 * 	// speed and target are formal arguments
 * }
 * }</pre>
 *
 * <p>
 * Access formal arguments via {@link #getFormalArgs()}, which returns descriptions of each parameter.
 * </p>
 *
 * <h2>Passed Arguments</h2>
 *
 * <p>
 * When a statement/action is invoked, the actual arguments passed at the call site are tracked:
 * </p>
 *
 * <pre>{@code
 * do move(speed: 5.0, target: {10, 20});
 * // Passed arguments: speed=5.0, target={10,20}
 * }</pre>
 *
 * <p>
 * Access passed arguments via {@link #getPassedArgs()} to validate and process them.
 * </p>
 *
 * <h2>Control Flow Properties</h2>
 *
 * <ul>
 * <li><strong>Super Invocation:</strong> {@link #isSuperInvocation()} - Is this invoking a parent's action?</li>
 * <li><strong>Breakable:</strong> {@link #isBreakable()} - Can this statement contain 'break'?</li>
 * <li><strong>Continuable:</strong> {@link #isContinuable()} - Can this statement contain 'continue'?</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * IStatementDescription stmtDesc = ...;
 *
 * // Check if this is an action with parameters
 * Iterable<IDescription> params = stmtDesc.getFormalArgs();
 * for (IDescription param : params) {
 *     System.out.println("Parameter: " + param.getName());
 * }
 *
 * // For a call site, get passed arguments
 * Arguments passedArgs = stmtDesc.getPassedArgs();
 * if (passedArgs != null) {
 *     // Validate arguments match formal parameters
 * }
 *
 * // Check control flow properties
 * if (stmtDesc.isBreakable()) {
 *     // This is a loop or switch that can contain break statements
 * }
 * }</pre>
 *
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 *
 * @see IDescription
 * @see IActionDescription
 * @see gama.api.gaml.symbols.Arguments
 */
public interface IStatementDescription extends IDescription {

	/**
	 * Returns the formal arguments (parameters) defined by this statement.
	 *
	 * <p>
	 * Formal arguments are the parameter declarations in action signatures and other parametrized statements. Each
	 * argument is described by an {@link IDescription} (typically a variable description) that includes name, type, and
	 * optional default value.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>{@code
	 * // For: action move(float speed, point target) { ... }
	 * Iterable<IDescription> args = actionDesc.getFormalArgs();
	 * // Returns descriptions for 'speed' and 'target' parameters
	 * }</pre>
	 *
	 * @return an iterable of formal argument descriptions, or an empty iterable if none
	 */
	Iterable<IDescription> getFormalArgs();

	/**
	 * Gets the arguments passed to this statement at its call site.
	 *
	 * <p>
	 * For statement descriptions that represent invocations (do, call, etc.), this method returns the actual arguments
	 * provided by the caller. The Arguments object maps argument names to their expression values.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>{@code
	 * // For: do move(speed: 5.0, target: {10, 20});
	 * Arguments args = doStmtDesc.getPassedArgs();
	 * IExpression speedExpr = args.get("speed"); // 5.0
	 * IExpression targetExpr = args.get("target"); // {10, 20}
	 * }</pre>
	 *
	 * <p>
	 * <b>Note:</b> This is only meaningful for statement descriptions representing invocations, not for declarations.
	 * </p>
	 *
	 * @return the passed arguments object, or null if this is not an invocation or has no arguments
	 */
	Arguments getPassedArgs();

	/**
	 * Checks if this statement is a super invocation.
	 *
	 * <p>
	 * A super invocation calls the parent species' implementation of an action, typically using the {@code invoke}
	 * statement with the {@code super} keyword.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>{@code
	 * species child parent: parent_species {
	 *     action move {
	 *         invoke move; // super invocation
	 *         // Additional child-specific behavior
	 *     }
	 * }
	 * }</pre>
	 *
	 * <p>
	 * <b>Default Implementation:</b> Returns {@code false}.
	 * </p>
	 *
	 * @return true if this statement invokes a parent's action, false otherwise
	 */
	default boolean isSuperInvocation() { return false; }

	/**
	 * Checks if this statement can contain {@code continue} statements.
	 *
	 * <p>
	 * Continuable statements are loops where {@code continue} can be used to skip to the next iteration. This includes
	 * {@code loop}, {@code ask} over collections, etc.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>{@code
	 * loop i from: 0 to: 10 {
	 *     if (i = 5) { continue; } // Valid - loop is continuable
	 *     // ...
	 * }
	 * }</pre>
	 *
	 * <p>
	 * <b>Default Implementation:</b> Returns {@code false}.
	 * </p>
	 *
	 * @return true if continue statements are allowed within this statement, false otherwise
	 */
	default boolean isContinuable() { return false; }

	/**
	 * Checks if this statement can contain {@code break} statements.
	 *
	 * <p>
	 * Breakable statements are loops and switches where {@code break} can be used to exit early. This includes
	 * {@code loop}, {@code switch}, {@code ask} over collections, etc.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>{@code
	 * loop i from: 0 to: 10 {
	 *     if (i = 5) { break; } // Valid - loop is breakable
	 *     // ...
	 * }
	 *
	 * switch value {
	 *     match 1 { do_something(); break; } // Valid - switch is breakable
	 * }
	 * }</pre>
	 *
	 * <p>
	 * <b>Default Implementation:</b> Returns {@code false}.
	 * </p>
	 *
	 * @return true if break statements are allowed within this statement, false otherwise
	 */
	default boolean isBreakable() { return false; }

}

/*******************************************************************************************************
 *
 * IStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

import java.util.Map;

import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.ISymbol;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IExperimentDisplayable;
import gama.api.utils.tests.IndividualTestSummary;
import gama.api.utils.tests.WithTestSummary;

/**
 * Base interface for all GAML statements.
 * 
 * <p>
 * A statement represents an executable unit in GAML that can perform actions, modify state, or control execution flow.
 * Statements are the building blocks of agent behaviors, actions, and control structures in GAMA simulations.
 * </p>
 * 
 * <h2>Statement Categories</h2>
 * 
 * <h3>Control Flow</h3>
 * <ul>
 * <li>{@code if/else} - Conditional execution</li>
 * <li>{@code loop} - Iteration</li>
 * <li>{@code switch/match} - Multi-way branching</li>
 * <li>{@code return} - Exit from actions</li>
 * </ul>
 * 
 * <h3>Agent Operations</h3>
 * <ul>
 * <li>{@code create} - Instantiate new agents</li>
 * <li>{@code ask} - Execute code in other agents' context</li>
 * <li>{@code do} - Invoke actions</li>
 * </ul>
 * 
 * <h3>Data Operations</h3>
 * <ul>
 * <li>{@code add/remove/put} - Container manipulation</li>
 * <li>{@code save} - Data persistence</li>
 * <li>{@code write} - Console output</li>
 * </ul>
 * 
 * <h2>Execution Model</h2>
 * <p>
 * Statements are executed within a scope that provides context including the current agent, global variables, and
 * temporary variables. The execution can return a value and may throw runtime exceptions.
 * </p>
 * 
 * <h2>Example Usage</h2>
 * <pre>
 * {@code
 * reflex move {
 *     // Each line is a statement
 *     point target <- any_location_in(world);
 *     do goto target: target;
 *     if (location = target) {
 *         write "Arrived at destination";
 *     }
 * }
 * }
 * </pre>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see IExecutable
 * @see ISymbol
 * @see AbstractStatement
 */
public interface IStatement extends ISymbol, IExecutable {

	/**
	 * Marker interface for remote statements that can be executed across network connections.
	 */
	public interface Remote extends IStatement {

	}

	/**
	 * Interface for user-accessible commands in experiments.
	 * 
	 * <p>
	 * User commands are actions that can be triggered from the UI, typically appearing as buttons or menu items in
	 * experiment interfaces.
	 * </p>
	 */
	public interface UserCommand extends IStatement.WithArgs, IExperimentDisplayable {

	}

	/**
	 * Interface for aspect statements that define visual representations.
	 * 
	 * <p>
	 * Aspects contain drawing statements that specify how agents should be rendered in displays.
	 * </p>
	 */
	public interface Aspect extends IStatement {

	}

	/**
	 * Interface for action statements that define reusable behaviors.
	 * 
	 * <p>
	 * Actions are named sequences of statements that can accept parameters and return values, similar to methods in
	 * object-oriented programming.
	 * </p>
	 */
	public interface Action extends IStatement.WithArgs {

	}

	/**
	 * Interface for statements that accept arguments.
	 * 
	 * <p>
	 * This interface is implemented by actions and other parameterized statements. It provides methods for managing
	 * both formal arguments (parameter declarations) and runtime arguments (actual values passed during execution).
	 * </p>
	 */
	public interface WithArgs extends IStatement {

		/**
		 * Sets the formal argument declarations for this statement.
		 * 
		 * <p>
		 * Formal arguments define the parameters that this statement accepts, including their names, types, and
		 * default values.
		 * </p>
		 *
		 * @param args
		 *            the formal argument definitions
		 */
		void setFormalArgs(Arguments args);

		/**
		 * Sets the runtime argument values for execution.
		 * 
		 * <p>
		 * This method is called before execution to provide the actual values for the statement's parameters.
		 * </p>
		 *
		 * @param scope
		 *            the execution scope
		 * @param args
		 *            the runtime argument values
		 */
		@Override
		default void setRuntimeArgs(final IScope scope, final Arguments args) {}

	}

	/**
	 * Marker interface for statements that support break/continue.
	 * 
	 * <p>
	 * Breakable statements are control structures (like loops) that can be interrupted by {@code break} or
	 * {@code continue} statements.
	 * </p>
	 */
	public interface Breakable extends IStatement {
		// Unused tagging interface (for the moment)
	}

	/**
	 * Interface for test statements used in model validation.
	 * 
	 * <p>
	 * Test statements allow automated testing of model behavior. They execute assertions and collect results into a
	 * test summary.
	 * </p>
	 */
	public interface Test extends IStatement, WithTestSummary<IndividualTestSummary> {

	}

	/**
	 * Interface for create statements that instantiate agents.
	 * 
	 * <p>
	 * Create statements generate new agents and can initialize their attributes using expressions or user-provided
	 * maps.
	 * </p>
	 */
	public interface Create extends IStatement.WithArgs {

		/**
		 * Fills a map with user initialization values for created agents.
		 *
		 * @param scope
		 *            the execution scope
		 * @param map
		 *            the map to fill with initialization data
		 */
		void fillWithUserInit(IScope scope, Map map);

		/**
		 * Gets the header expression that determines how many agents to create.
		 *
		 * @return the header expression
		 */
		IExpression getHeader();

	}

	/**
	 * Marker interface for save statements that persist data.
	 * 
	 * <p>
	 * Save statements write data to files in various formats (CSV, shapefile, image, etc.).
	 * </p>
	 */
	public interface Save extends IStatement {

	}

	/**
	 * Marker interface for draw statements used in aspects.
	 * 
	 * <p>
	 * Draw statements render graphical elements (shapes, text, images) in displays.
	 * </p>
	 */
	public interface Draw extends IStatement {

	}

	/**
	 * Marker interface for event handling statements.
	 * 
	 * <p>
	 * Event statements respond to user interactions (mouse clicks, key presses) in displays.
	 * </p>
	 */
	public interface Event extends ISymbol {

	}

	/**
	 * Checks if this statement is empty (contains no executable code).
	 * 
	 * <p>
	 * Empty statements can be optimized away or used as placeholders.
	 * </p>
	 *
	 * @return true if the statement is empty, false otherwise
	 */
	default boolean isEmpty() { return true; }

}

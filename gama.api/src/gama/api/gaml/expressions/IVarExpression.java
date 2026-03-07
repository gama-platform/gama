/*******************************************************************************************************
 *
 * IVarExpression.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.expressions;

import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * Interface representing variable expressions in GAML. Variable expressions reference and manipulate variables,
 * attributes, and other named storage locations in the GAML runtime environment.
 *
 * <p>
 * IVarExpression extends {@link IExpression} to provide both read access (via {@link #value(IScope)}) and write access
 * (via {@link #setVal(IScope, Object, boolean)}) to variables and attributes.
 * </p>
 *
 * <h3>Variable Types:</h3>
 * <p>
 * GAML supports several categories of variables, identified by enum constants:
 * </p>
 * <ul>
 * <li><b>{@link #GLOBAL} (0):</b> Global variables declared at the model or experiment level</li>
 * <li><b>{@link #AGENT} (1):</b> Agent attributes and variables</li>
 * <li><b>{@link #TEMP} (2):</b> Temporary variables (loop variables, let-bindings)</li>
 * <li><b>{@link #EACH} (3):</b> The special 'each' variable in collection iterations</li>
 * <li><b>{@link #SELF} (4):</b> The 'self' pseudo-variable referring to the current agent</li>
 * <li><b>{@link #SUPER} (5):</b> The 'super' pseudo-variable for parent species access</li>
 * <li><b>{@link #MYSELF} (6):</b> The 'myself' pseudo-variable referring to the calling agent</li>
 * </ul>
 *
 * <h3>Variable Access Patterns:</h3>
 * <p>
 * Variables can be accessed in several ways:
 * </p>
 * <ul>
 * <li><b>Simple:</b> {@code my_var} - Direct variable reference</li>
 * <li><b>Qualified:</b> {@code agent1.location} - Attribute access on another agent</li>
 * <li><b>Nested:</b> {@code self.host.population} - Chain of attribute accesses</li>
 * </ul>
 *
 * <h3>Read and Write Operations:</h3>
 *
 * <pre>
 * // Reading a variable value
 * IVarExpression varExpr = ...; // Represents "my_var"
 * Object value = varExpr.value(scope);
 *
 * // Writing a variable value
 * varExpr.setVal(scope, newValue, false);
 *
 * // Creating a new variable if it doesn't exist
 * varExpr.setVal(scope, initialValue, true);
 * </pre>
 *
 * <h3>Qualified Variables:</h3>
 * <p>
 * Variable expressions can be qualified with an owner expression (e.g., {@code agent.attribute}):
 * </p>
 * <ul>
 * <li>{@link #getOwner()} returns the expression for the owning agent (e.g., "agent")</li>
 * <li>{@link #getVar()} returns the expression for the variable name (e.g., "attribute")</li>
 * </ul>
 *
 * <h3>Modifiability:</h3>
 * <p>
 * Some variables are read-only and cannot be assigned to:
 * </p>
 * <ul>
 * <li>Pseudo-variables like {@code self}, {@code myself}, {@code super}</li>
 * <li>Constants and final variables</li>
 * <li>Read-only attributes</li>
 * </ul>
 * <p>
 * Use {@link #isNotModifiable()} to check if a variable can be assigned to.
 * </p>
 *
 * <h3>Agent Variables:</h3>
 * <p>
 * The {@link Agent} sub-interface represents variables that reference agent attributes. These provide additional
 * metadata about the attribute definition via {@link Agent#getDefinitionDescription()}.
 * </p>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>
 * // Simple variable assignment
 * // GAML: let x <- 10;
 * IVarExpression xVar = ...; // Variable expression for "x"
 * xVar.setVal(scope, 10, true);
 *
 * // Attribute access
 * // GAML: let loc <- self.location;
 * IVarExpression locVar = ...; // Variable expression for "location"
 * IExpression owner = locVar.getOwner(); // Expression for "self"
 * Object location = locVar.value(scope);
 *
 * // Checking modifiability
 * if (!varExpr.isNotModifiable()) {
 * 	varExpr.setVal(scope, newValue, false);
 * } else {
 * 	// Cannot assign to this variable
 * }
 * </pre>
 *
 * <h3>Scope Context:</h3>
 * <p>
 * Variable expressions require a scope for both reading and writing. The scope provides:
 * </p>
 * <ul>
 * <li>Access to the current agent (for agent variables)</li>
 * <li>Temporary variable storage (for temp variables)</li>
 * <li>Global variable access (for global variables)</li>
 * <li>Agent hierarchy for qualified accesses</li>
 * </ul>
 *
 * <h3>Thread Safety:</h3>
 * <p>
 * Variable expressions themselves are thread-safe (immutable structure), but variable access is only safe within a
 * single scope/thread. Different scopes can evaluate the same variable expression concurrently on different agents.
 * </p>
 *
 * @author drogoul
 * @since 4 sept. 2007
 * @see IExpression
 * @see IScope
 */
public interface IVarExpression extends IExpression {

	/**
	 * Specialized interface for variable expressions that reference agent attributes. Agent variable expressions
	 * provide access to attribute metadata and definition information.
	 *
	 * <p>
	 * This interface is used for variables that are defined as agent attributes (species variables) rather than
	 * temporary or global variables.
	 * </p>
	 *
	 * @see IVarExpression
	 */
	public interface Agent extends IVarExpression {

		/**
		 * Returns the description of the attribute definition in the species or model where this variable is declared.
		 * This provides access to metadata such as type, initial value, update policy, etc.
		 *
		 * @return the description of the variable's definition, never null
		 */
		IDescription getDefinitionDescription();
	}

	/**
	 * The Enum Category.
	 */
	public enum Category {

		/** Variable category constant for global variables (model-level or experiment-level). */
		GLOBAL(0),

		/** Variable category constant for agent attributes and variables. */
		AGENT(1),

		/** Variable category constant for temporary variables (loop variables, let-bindings). */
		TEMP(2),

		/** Variable category constant for the special 'each' loop variable in collection iterations. */
		EACH(3),

		/** Variable category constant for the 'self' pseudo-variable referring to the current agent. */
		SELF(4),

		/** Variable category constant for the 'super' pseudo-variable for accessing parent species members. */
		SUPER(5),

		/** Variable category constant for the 'myself' pseudo-variable referring to the calling agent in actions. */
		MYSELF(6);
		// public static final int WORLD = 5;

		/** The value. */
		int value;

		/**
		 * Instantiates a new category.
		 *
		 * @param index
		 *            the index
		 */
		Category(final int index) {
			value = index;
		}

	}

	/**
	 * Sets the value of this variable in the given scope. This method provides write access to variables and
	 * attributes.
	 *
	 * <p>
	 * The behavior depends on the variable category and the {@code create} parameter:
	 * </p>
	 * <ul>
	 * <li>If {@code create} is true and the variable doesn't exist, it may be created (for temp variables)</li>
	 * <li>If {@code create} is false and the variable doesn't exist, an error may be thrown</li>
	 * <li>For agent attributes, the value is set on the appropriate agent from the scope</li>
	 * <li>For temp variables, the value is stored in the scope's temporary variable map</li>
	 * </ul>
	 *
	 * <p>
	 * Type conversion is performed automatically if the value type doesn't match the variable's declared type.
	 * </p>
	 *
	 * <h3>Examples:</h3>
	 *
	 * <pre>
	 * // Set existing variable
	 * varExpr.setVal(scope, 42, false);
	 *
	 * // Create and set temporary variable
	 * tempVarExpr.setVal(scope, "initial", true);
	 *
	 * // Set agent attribute
	 * locationVar.setVal(scope, new GamaPoint(10, 20), false);
	 * </pre>
	 *
	 * @param scope
	 *            the execution scope providing context for the assignment
	 * @param v
	 *            the value to assign to the variable
	 * @param create
	 *            whether to create the variable if it doesn't exist (mainly for temp variables)
	 * @throws GamaRuntimeException
	 *             if the variable is not modifiable or doesn't exist (when create is false)
	 * @see #isNotModifiable()
	 */
	void setVal(IScope scope, Object v, boolean create);

	/**
	 * Indicates whether this variable is modifiable (can be assigned to). Some variables are read-only and cannot be
	 * modified:
	 *
	 * <p>
	 * Read-only variables include:
	 * </p>
	 * <ul>
	 * <li>Pseudo-variables: {@code self}, {@code myself}, {@code super}, {@code each}</li>
	 * <li>Constants declared with the {@code const} keyword</li>
	 * <li>Built-in read-only attributes (e.g., {@code shape}, {@code host})</li>
	 * <li>Attributes marked as non-modifiable in their definition</li>
	 * </ul>
	 *
	 * <p>
	 * This check is performed during validation to prevent invalid assignments at compile time.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>
	 * // Attempting to assign to 'self' would fail
	 * IVarExpression selfVar = ...; // Represents "self"
	 * assert selfVar.isNotModifiable() == true;
	 *
	 * // Regular variable can be assigned
	 * IVarExpression normalVar = ...; // Represents "my_var"
	 * assert normalVar.isNotModifiable() == false;
	 * </pre>
	 *
	 * @return true if this variable cannot be modified (read-only), false if it can be assigned to
	 * @see #setVal(IScope, Object, boolean)
	 */
	@Override
	boolean isNotModifiable();

	/**
	 * Returns the owner expression for qualified variable access. For expressions like {@code agent.attribute}, this
	 * returns the expression for "agent".
	 *
	 * <p>
	 * For simple unqualified variables, this typically returns null or a reference to the implicit owner (like
	 * {@code self}).
	 * </p>
	 *
	 * <h3>Examples:</h3>
	 *
	 * <pre>
	 * // For "agent1.location":
	 * IExpression owner = locationVar.getOwner(); // Expression for "agent1"
	 *
	 * // For "self.speed":
	 * IExpression owner = speedVar.getOwner(); // Expression for "self"
	 *
	 * // For simple "my_var":
	 * IExpression owner = varExpr.getOwner(); // Might be null or implicit self
	 * </pre>
	 *
	 * @return the expression representing the variable's owner/qualifier, or null if unqualified
	 * @see #getVar()
	 */
	IExpression getOwner();

	/**
	 * Returns the variable name expression for this variable reference. For qualified access like
	 * {@code owner.attribute}, this returns the expression for the attribute name part.
	 *
	 * <p>
	 * In most cases, this is a constant string expression representing the variable name.
	 * </p>
	 *
	 * <h3>Examples:</h3>
	 *
	 * <pre>
	 * // For "location":
	 * IExpression var = locVar.getVar(); // Constant string "location"
	 *
	 * // For "agent.speed":
	 * IExpression var = speedVar.getVar(); // Constant string "speed"
	 * </pre>
	 *
	 * @return the expression representing the variable name
	 * @see #getOwner()
	 */
	IExpression getVar();

}
/*******************************************************************************************************
 *
 * IExecutionResult.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

/**
 * Represents the result of executing a statement, expression, or action in GAMA.
 *
 * <p>
 * IExecutionResult encapsulates both the success/failure status of an execution and any value produced by that
 * execution. This interface provides a clean way to return execution outcomes without relying solely on exceptions for
 * error handling.
 * </p>
 *
 * <p>
 * The interface uses a functional design with factory methods to create different types of results. It distinguishes
 * between:
 * </p>
 * <ul>
 * <li>Successful execution with no value ({@link #PASSED})</li>
 * <li>Successful execution with a value ({@link #withValue(Object)})</li>
 * <li>Failed execution ({@link #FAILED})</li>
 * </ul>
 *
 * <h2>Predefined Constants</h2>
 * <ul>
 * <li>{@link #PASSED} - Successful execution with boolean true as value</li>
 * <li>{@link #FAILED} - Failed execution with boolean false as value</li>
 * <li>{@link #PASSED_WITH_NULL} - Successful execution with null value</li>
 * <li>{@link #PASSED_WITH_FALSE} - Successful execution with boolean false as value</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Checking Execution Success</h3>
 *
 * <pre>{@code
 * IExecutionResult result = scope.execute(statement, agent, null);
 *
 * if (result.passed()) {
 * 	// Execution was successful
 * 	Object value = result.getValue();
 * 	// Process the result value...
 * } else {
 * 	// Execution failed
 * 	System.err.println("Statement execution failed");
 * }
 * }</pre>
 *
 * <h3>Creating Results</h3>
 *
 * <pre>{@code
 * // Return success with a computed value
 * public IExecutionResult computeDistance() {
 * 	double distance = calculateDistance();
 * 	return IExecutionResult.withValue(distance);
 * }
 *
 * // Return success with null
 * public IExecutionResult initialize() {
 * 	// Perform initialization...
 * 	return IExecutionResult.PASSED;
 * }
 *
 * // Return failure
 * public IExecutionResult validateData() {
 * 	if (!isValid()) { return IExecutionResult.FAILED; }
 * 	return IExecutionResult.PASSED;
 * }
 * }</pre>
 *
 * <h3>Extracting Values</h3>
 *
 * <pre>{@code
 * IExecutionResult result = scope.evaluate(expression, agent);
 *
 * if (result.passed()) {
 * 	// For results with values, getValue() returns the actual value
 * 	Object value = result.getValue();
 * 
 * 	// Safe casting
 * 	if (value instanceof Double) { double numericValue = (Double) value; }
 * }
 * }</pre>
 *
 * <h3>Boolean Results</h3>
 *
 * <pre>{@code
 * // Creating boolean results
 * boolean condition = checkCondition();
 * IExecutionResult result = IExecutionResult.withValue(condition);
 *
 * // Note: withValue(true) returns PASSED
 * // withValue(false) returns PASSED_WITH_FALSE (still passed!)
 *
 * if (result.passed()) {
 * 	boolean boolValue = (Boolean) result.getValue();
 * 	if (boolValue) {
 * 		// Condition was true
 * 	} else {
 * 		// Condition was false (but execution succeeded)
 * 	}
 * }
 * }</pre>
 *
 * <h3>Statement Implementation Pattern</h3>
 *
 * <pre>{@code
 * public class CustomStatement implements IStatement {
 * 
 * 	@Override
 * 	public Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
 * 		// Execute statement logic
 * 		if (error) { throw GamaRuntimeException.error("Something went wrong", scope); }
 * 
 * 		// Return the computed value
 * 		return computedValue;
 * 	}
 * }
 *
 * // The framework wraps the result automatically
 * IExecutionResult result = scope.execute(statement);
 * // If privateExecuteIn returns normally: result = IExecutionResult.withValue(returnValue)
 * // If privateExecuteIn throws: result = IExecutionResult.FAILED
 * }</pre>
 *
 * <h2>Design Notes</h2>
 *
 * <p>
 * The interface is marked as {@code @FunctionalInterface}, though it has a nested interface. The functional method is
 * {@link #passed()}, which allows for simple success/failure checks.
 * </p>
 *
 * <p>
 * The {@link WithValue} sub-interface provides type safety for results that carry values, ensuring that
 * {@link #getValue()} returns the actual value rather than just the passed() status.
 * </p>
 *
 * @see IScope#execute(gama.api.runtime.IExecutable)
 * @see IScope#evaluate(gama.api.gaml.expressions.IExpression, gama.api.kernel.agent.IAgent)
 * @see IScope#step(gama.api.runtime.IStepable)
 * @see IScope#init(gama.api.runtime.IStepable)
 *
 * @author drogoul
 */
@FunctionalInterface
public interface IExecutionResult {

	/**
	 * Sub-interface for execution results that carry a value.
	 *
	 * <p>
	 * WithValue guarantees that the execution passed (succeeded) and that {@link #getValue()} returns the actual result
	 * value, not just the success status.
	 * </p>
	 *
	 * <h3>Example</h3>
	 *
	 * <pre>{@code
	 * IExecutionResult result = scope.evaluate(expression, agent);
	 * if (result instanceof IExecutionResult.WithValue) {
	 * 	// We know it passed and has a value
	 * 	Object value = result.getValue();
	 * }
	 * }</pre>
	 */
	@FunctionalInterface
	interface WithValue extends IExecutionResult {

		/**
		 * Returns the value produced by the execution.
		 *
		 * <p>
		 * For WithValue results, this method returns the actual computed value, which may be:
		 * </p>
		 * <ul>
		 * <li>A primitive wrapper (Integer, Double, Boolean, etc.)</li>
		 * <li>A GAMA type (IAgent, IList, IMap, GamaPoint, etc.)</li>
		 * <li>Any Java object produced by the execution</li>
		 * <li>null (for successful executions that don't produce a value)</li>
		 * </ul>
		 *
		 * @return the value produced by the execution
		 */
		@Override
		Object getValue();

		/**
		 * Indicates whether the execution succeeded.
		 *
		 * <p>
		 * For WithValue results, this always returns true since only successful executions can have values.
		 * </p>
		 *
		 * @return always true for WithValue results
		 */
		@Override
		default boolean passed() {
			return true;
		}
	}

	/**
	 * Creates an execution result with the specified value.
	 *
	 * <p>
	 * This factory method creates a successful execution result carrying the provided value. If the value is null,
	 * returns {@link #PASSED_WITH_NULL}.
	 * </p>
	 *
	 * <h3>Examples</h3>
	 *
	 * <pre>{@code
	 * // With a numeric value
	 * IExecutionResult result1 = IExecutionResult.withValue(42);
	 *
	 * // With an agent
	 * IExecutionResult result2 = IExecutionResult.withValue(agent);
	 *
	 * // With null (returns PASSED_WITH_NULL)
	 * IExecutionResult result3 = IExecutionResult.withValue(null);
	 *
	 * // With a list
	 * IExecutionResult result4 = IExecutionResult.withValue(listOfAgents);
	 * }</pre>
	 *
	 * @param value
	 *            the value to wrap in the result
	 * @return a WithValue result containing the value, or PASSED_WITH_NULL if value is null
	 */
	static IExecutionResult withValue(final Object value) {
		if (value == null) return PASSED_WITH_NULL;
		return (WithValue) () -> value;
	}

	/**
	 * Creates an execution result with a boolean value.
	 *
	 * <p>
	 * This factory method creates a successful execution result with a boolean value. Note:
	 * </p>
	 * <ul>
	 * <li>If value is true, returns {@link #PASSED}</li>
	 * <li>If value is false, returns {@link #PASSED_WITH_FALSE}</li>
	 * </ul>
	 *
	 * <p>
	 * Both results indicate successful execution; the difference is in the returned value.
	 * </p>
	 *
	 * <h3>Examples</h3>
	 *
	 * <pre>{@code
	 * // Returns PASSED (getValue() returns true)
	 * IExecutionResult result1 = IExecutionResult.withValue(true);
	 *
	 * // Returns PASSED_WITH_FALSE (getValue() returns false)
	 * IExecutionResult result2 = IExecutionResult.withValue(false);
	 *
	 * // Both passed
	 * assert result1.passed() && result2.passed();
	 * }</pre>
	 *
	 * @param value
	 *            the boolean value
	 * @return PASSED if value is true, PASSED_WITH_FALSE if value is false
	 */
	static IExecutionResult withValue(final boolean value) {
		return value ? PASSED : PASSED_WITH_FALSE;
	}

	/**
	 * Successful execution result with boolean true value.
	 *
	 * <p>
	 * Use this constant for executions that succeeded and should return true. This is the most common success result.
	 * </p>
	 *
	 * <p>
	 * getValue() returns true.
	 * </p>
	 */
	IExecutionResult PASSED = () -> true;

	/**
	 * Successful execution result with null value.
	 *
	 * <p>
	 * Use this constant for executions that succeeded but don't produce a meaningful value (e.g., initialization, void
	 * actions).
	 * </p>
	 *
	 * <p>
	 * getValue() returns null.
	 * </p>
	 */
	IExecutionResult PASSED_WITH_NULL = (WithValue) () -> null;

	/**
	 * Successful execution result with boolean false value.
	 *
	 * <p>
	 * Use this constant for executions that succeeded but produced a false boolean result. Note that this is NOT a
	 * failure - passed() returns true.
	 * </p>
	 *
	 * <p>
	 * getValue() returns false.
	 * </p>
	 *
	 * <h3>Example</h3>
	 *
	 * <pre>{@code
	 * // Evaluating a condition
	 * boolean conditionMet = evaluateCondition();
	 * IExecutionResult result = IExecutionResult.withValue(conditionMet);
	 *
	 * // Even if conditionMet is false, execution succeeded
	 * assert result.passed() == true;
	 * assert result.getValue().equals(conditionMet);
	 * }</pre>
	 */
	IExecutionResult PASSED_WITH_FALSE = (WithValue) () -> false;

	/**
	 * Failed execution result.
	 *
	 * <p>
	 * Use this constant for executions that failed. This indicates an error or exceptional condition that prevented
	 * successful execution.
	 * </p>
	 *
	 * <p>
	 * passed() returns false, getValue() returns false.
	 * </p>
	 */
	IExecutionResult FAILED = () -> false;

	/**
	 * Indicates whether the execution succeeded.
	 *
	 * <p>
	 * This is the primary method for checking execution outcomes. Returns true if the execution completed successfully,
	 * false if it failed.
	 * </p>
	 *
	 * <p>
	 * Note: A successful execution (passed() == true) can still have a null or false value. Success refers to whether
	 * the execution completed without errors, not to the semantic meaning of the result value.
	 * </p>
	 *
	 * @return true if execution succeeded, false if it failed
	 */
	boolean passed();

	/**
	 * Returns the value produced by the execution.
	 *
	 * <p>
	 * For basic IExecutionResult implementations (non-WithValue), this returns the same as passed() converted to an
	 * Object (Boolean).
	 * </p>
	 *
	 * <p>
	 * For WithValue implementations, this returns the actual value produced by the execution.
	 * </p>
	 *
	 * @return the execution result value; for non-WithValue results, returns Boolean of passed() status
	 */
	default Object getValue() { return passed(); }

}
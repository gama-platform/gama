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
package gama.core.runtime;

import gama.annotations.precompiler.OkForAPI;

/**
 * The result of executions. 'passed' represents the success or failure of the computation, value its result
 *
 * @author drogoul
 *
 */
@FunctionalInterface
@OkForAPI (OkForAPI.Location.UTILS)
public interface IExecutionResult {

	/**
	 * The Interface WithValue.
	 */
	@FunctionalInterface
	interface WithValue extends IExecutionResult {

		/**
		 * Gets the value.
		 *
		 * @return the value
		 */
		@Override
		Object getValue();

		/**
		 * Passed.
		 *
		 * @return true, if successful
		 */
		@Override
		default boolean passed() {
			return true;
		}

	}

	/**
	 * With value.
	 *
	 * @param value
	 *            the value
	 * @return the execution result
	 */
	// FACTORY METHODS
	static IExecutionResult withValue(final Object value) {
		if (value == null) return PASSED_WITH_NULL;
		return (WithValue) () -> value;
	}

	/**
	 * With value.
	 *
	 * @param value
	 *            the value
	 * @return the execution result
	 */
	static IExecutionResult withValue(final boolean value) {
		return value ? PASSED : PASSED_WITH_FALSE;
	}

	/** The Constant PASSED. */
	IExecutionResult PASSED = () -> true;

	/** The passed with null. */
	IExecutionResult PASSED_WITH_NULL = (WithValue) () -> null;

	/** The passed with false. */
	IExecutionResult PASSED_WITH_FALSE = (WithValue) () -> false;

	/** The Constant FAILED. */
	IExecutionResult FAILED = () -> false;

	/**
	 * Passed.
	 *
	 * @return true, if successful
	 */
	boolean passed();

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	default Object getValue() { return passed(); }

}
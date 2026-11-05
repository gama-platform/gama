/*******************************************************************************************************
 *
 * IGamaGetter.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.additions;

import gama.api.runtime.scope.IScope;

/**
 * A functional interface for retrieving values within the GAMA framework. This interface defines
 * a general-purpose getter mechanism that can access values based on a scope and optional arguments.
 * 
 * <p>As a functional interface, IGamaGetter can be implemented using lambda expressions or method references,
 * making it ideal for defining dynamic value retrieval logic.</p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>This interface is commonly used for:</p>
 * <ul>
 *   <li><strong>Attribute Access:</strong> Retrieving agent or species attributes</li>
 *   <li><strong>Variable Evaluation:</strong> Computing variable values on demand</li>
 *   <li><strong>Dynamic Getters:</strong> Implementing context-sensitive value retrieval</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>{@code
 * // Simple getter that returns a constant
 * IGamaGetter<Integer> constantGetter = (scope, args) -> 42;
 * 
 * // Getter that uses scope to retrieve a value
 * IGamaGetter<String> nameGetter = (scope, args) -> 
 *     scope.getAgent().getName();
 * 
 * // Getter with arguments
 * IGamaGetter<Double> calculatorGetter = (scope, args) -> {
 *     if (args.length > 0) {
 *         return ((Number) args[0]).doubleValue() * 2;
 *     }
 *     return 0.0;
 * };
 * }</pre>
 *
 * @param <T> the type of value returned by this getter
 * 
 * @author GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 */
@FunctionalInterface
public interface IGamaGetter<T> {
	
	/**
	 * Retrieves a value based on the provided scope and arguments. This is the functional method
	 * of the IGamaGetter interface that performs the actual value retrieval.
	 * 
	 * <p>The implementation can use the scope to access the current execution context (agent, simulation state, etc.)
	 * and the arguments to customize the retrieval logic. The behavior is entirely implementation-dependent.</p>
	 *
	 * @param scope
	 *            the execution scope providing access to the current agent, simulation, and runtime context
	 * @param arguments
	 *            variable arguments that can be used to parameterize the retrieval. The number and types
	 *            of arguments depend on the specific implementation
	 * @return the retrieved value of type T, or null if the value cannot be retrieved or does not exist
	 */
	T get(IScope scope, Object... arguments);

	/**
	 * Invokes a unary getter, using the array-based entry point for legacy implementations.
	 */
	default T getUnary(final IScope scope, final Object operand) {
		return get(scope, operand);
	}

	/**
	 * Invokes a binary getter, using the array-based entry point for legacy implementations.
	 */
	default T getBinary(final IScope scope, final Object left, final Object right) {
		return get(scope, left, right);
	}

	/**
	 * A unary operator helper that does not allocate an operand array on direct invocation.
	 *
	 * @param <T> the result type
	 */
	@FunctionalInterface
	interface Unary<T> extends IGamaGetter<T> {

		@Override
		T getUnary(IScope scope, Object operand);

		@Override
		default T get(final IScope scope, final Object... arguments) {
			return getUnary(scope, arguments[0]);
		}
	}

	/**
	 * A binary operator helper that does not allocate an operand array on direct invocation.
	 *
	 * @param <T> the result type
	 */
	@FunctionalInterface
	interface Binary<T> extends IGamaGetter<T> {

		@Override
		T getBinary(IScope scope, Object left, Object right);

		@Override
		default T get(final IScope scope, final Object... arguments) {
			return getBinary(scope, arguments[0], arguments[1]);
		}
	}

}

/*******************************************************************************************************
 *
 * IOperator.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.expressions;

import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.utils.benchmark.IBenchmarkable;

/**
 * Interface representing operator expressions in GAML. Operators are expressions that apply operations (unary, binary,
 * or n-ary) to argument expressions and return computed results.
 * 
 * <p>
 * IOperator extends both {@link IExpression} (making operators evaluable) and {@link IBenchmarkable} (enabling
 * performance profiling of operator execution).
 * </p>
 * 
 * <h3>Operator Types:</h3>
 * <p>
 * GAML supports various categories of operators:
 * </p>
 * <ul>
 * <li><b>Arithmetic:</b> {@code +}, {@code -}, {@code *}, {@code /}, {@code ^}, etc.</li>
 * <li><b>Comparison:</b> {@code =}, {@code !=}, {@code <}, {@code >}, {@code <=}, {@code >=}</li>
 * <li><b>Logical:</b> {@code and}, {@code or}, {@code not}</li>
 * <li><b>Collection:</b> {@code contains}, {@code in}, {@code at}, {@code collect}, {@code where}, etc.</li>
 * <li><b>Spatial:</b> {@code distance_to}, {@code overlaps}, {@code inside}, {@code at_distance}, etc.</li>
 * <li><b>Casting:</b> {@code as}, {@code is}, {@code is_error}</li>
 * <li><b>Special:</b> {@code ?:} (ternary), {@code ::} (pair creation), etc.</li>
 * </ul>
 * 
 * <h3>Operator Structure:</h3>
 * <p>
 * Each operator has:
 * </p>
 * <ul>
 * <li>A name (the operator symbol or keyword)</li>
 * <li>Zero or more argument expressions accessible via {@link #arg(int)}</li>
 * <li>A prototype ({@link IArtefactProto}) defining signature, return type, and implementation</li>
 * <li>Evaluation logic that processes argument values</li>
 * </ul>
 * 
 * <h3>Evaluation Process:</h3>
 * <p>
 * When an operator is evaluated via {@link #value(gama.api.runtime.scope.IScope)}:
 * </p>
 * <ol>
 * <li>Each argument expression is evaluated in order</li>
 * <li>Argument values are passed to the operator's implementation</li>
 * <li>The operator performs its computation</li>
 * <li>The result is returned, possibly after type conversion</li>
 * </ol>
 * 
 * <h3>Visitor Pattern:</h3>
 * <p>
 * Operators support traversal via {@link #visitSuboperators(IOperatorVisitor)}, which applies a visitor to this
 * operator and recursively to any sub-operators in the argument tree. This enables:
 * </p>
 * <ul>
 * <li>Expression tree analysis</li>
 * <li>Dependency discovery</li>
 * <li>Optimization passes</li>
 * <li>Performance profiling</li>
 * </ul>
 * 
 * <h3>Benchmarking:</h3>
 * <p>
 * As {@link IBenchmarkable}, operators can be profiled to measure execution time and frequency. This is useful for
 * identifying performance bottlenecks in models. The benchmark name is the operator's GAML serialization via
 * {@link #getNameForBenchmarks()}.
 * </p>
 * 
 * <h3>Usage Examples:</h3>
 * 
 * <pre>
 * // Binary operator: 2 + 3
 * IOperator plusOp = ...;
 * IExpression leftArg = plusOp.arg(0);  // Expression for "2"
 * IExpression rightArg = plusOp.arg(1); // Expression for "3"
 * Object result = plusOp.value(scope);  // Returns 5
 * 
 * // Unary operator: not true
 * IOperator notOp = ...;
 * IExpression arg = notOp.arg(0);       // Expression for "true"
 * Object result = notOp.value(scope);   // Returns false
 * 
	 * // Visiting sub-operators for analysis
	 * operator.visitSuboperators(op -&gt; {
	 * 	System.out.println("Found operator: " + op.getName());
	 * });
	 * 
	 * // Getting operator metadata
	 * IArtefactProto proto = operator.getPrototype();
	 * String opName = proto.getName();
	 * IType&lt;?&gt; returnType = proto.getReturnType();
	 * &lt;/pre&gt;
 * 
 * <h3>Custom Operators:</h3>
 * <p>
 * GAML allows definition of custom operators via the {@code @operator} annotation in Java plugins. These are
 * discovered at startup and made available in GAML models alongside built-in operators.
 * </p>
 * 
 * <h3>Thread Safety:</h3>
 * <p>
 * Operator instances are generally not thread-safe. Each simulation thread should evaluate operators in its own scope.
 * However, the operator structure (argument tree, prototype) is immutable after compilation.
 * </p>
 * 
 * @author drogoul
 * @see IExpression
 * @see IArtefactProto
 * @see IBenchmarkable
 * @see IOperatorVisitor
 */
public interface IOperator extends IExpression, IBenchmarkable {

	/**
	 * Functional interface for visiting operators in an expression tree. Visitors are used to traverse operator
	 * hierarchies and perform operations on each operator encountered.
	 * 
	 * <p>
	 * This is a {@link FunctionalInterface}, allowing lambda expressions:
	 * </p>
	 * 
	 * <pre>
	 * operator.visitSuboperators(op -&gt; System.out.println(op.getName()));
	 * &lt;/pre&gt;
	 * 
	 * <p>
	 * Common use cases include:
	 * </p>
	 * <ul>
	 * <li>Collecting all operators of a specific type</li>
	 * <li>Analyzing operator usage patterns</li>
	 * <li>Applying transformations to operator trees</li>
	 * <li>Gathering performance metrics</li>
	 * </ul>
	 * 
	 * @see IOperator#visitSuboperators(IOperatorVisitor)
	 */
	@FunctionalInterface
	public interface IOperatorVisitor {

		/**
		 * Called for each operator visited during tree traversal. Implementations should process the operator as
		 * needed but should not modify the operator tree structure during traversal.
		 * 
		 * @param operator
		 *            the operator being visited, never null
		 */
		void visit(IOperator operator);
	}

	/**
	 * Recursively visits this operator and all sub-operators in the argument tree. The visitor is applied to this
	 * operator first, then recursively to any operators found in the arguments.
	 * 
	 * <p>
	 * This enables depth-first traversal of the operator tree structure. Only operator expressions are visited;
	 * non-operator expressions in the tree are skipped.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <p>
	 * To count all operators in an expression tree, use: {@code operator.visitSuboperators(op -> counter.increment())}
	 * </p>
	 * 
	 * <p>
	 * To collect operators by type, accumulate them in a collection during traversal.
	 * </p>
	 * 
	 * @param visitor
	 *            the visitor to apply to each operator in the tree, must not be null
	 */
	void visitSuboperators(IOperatorVisitor visitor);

	/**
	 * Returns the expression for the argument at the specified index. Operator arguments are indexed starting from 0.
	 * 
	 * <p>
	 * For unary operators, only index 0 is valid. For binary operators, indices 0 and 1 are valid (left and right
	 * operands). N-ary operators may have more arguments.
	 * </p>
	 * 
	 * <h3>Examples:</h3>
	 * 
	 * <pre>
	 * // Binary operator: a + b
	 * IExpression left = operator.arg(0);  // Expression for "a"
	 * IExpression right = operator.arg(1); // Expression for "b"
	 * 
	 * // Unary operator: not x
	 * IExpression operand = operator.arg(0); // Expression for "x"
	 * 
	 * // Ternary operator: condition ? value1 : value2
	 * IExpression condition = operator.arg(0);
	 * IExpression trueValue = operator.arg(1);
	 * IExpression falseValue = operator.arg(2);
	 * </pre>
	 * 
	 * @param i
	 *            the zero-based argument index
	 * @return the expression for the argument at the given index
	 * @throws IndexOutOfBoundsException
	 *             if the index is invalid for this operator's arity
	 */
	IExpression arg(int i);

	/**
	 * Returns the prototype (metadata and implementation) for this operator. The prototype defines the operator's
	 * signature, return type, documentation, and evaluation logic.
	 * 
	 * <p>
	 * The prototype provides access to:
	 * </p>
	 * <ul>
	 * <li>Operator name and alternative names</li>
	 * <li>Expected argument types</li>
	 * <li>Return type</li>
	 * <li>Documentation and examples</li>
	 * <li>Implementation method</li>
	 * </ul>
	 * 
	 * @return the operator's prototype, never null
	 */
	IArtefactProto getPrototype();

	/**
	 * Returns the name to use when benchmarking this operator. By default, this is the operator's GAML serialization,
	 * which provides a readable representation of the operation being performed.
	 * 
	 * <p>
	 * This is used by the GAMA profiling system to identify operators in performance reports.
	 * </p>
	 * 
	 * @return the benchmark name, typically the serialized GAML representation of this operator
	 */
	@Override
	default String getNameForBenchmarks() { return serializeToGaml(true); }

}
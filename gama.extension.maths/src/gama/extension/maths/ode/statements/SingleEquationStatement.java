/*******************************************************************************************************
 *
 * SingleEquationStatement.java, in gama.extension.maths, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.maths.ode.statements;

import static gama.api.constants.IKeyword.DIF2;
import static gama.api.constants.IKeyword.DIFF;
import static gama.api.constants.IKeyword.EQUALS;
import static gama.api.constants.IKeyword.EQUATION;
import static gama.api.constants.IKeyword.EQUATION_LEFT;
import static gama.api.constants.IKeyword.EQUATION_RIGHT;
import static gama.api.constants.IKeyword.SOLVE;
import static gama.api.constants.IKeyword.ZERO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.annotations.support.Reason;
import gama.api.annotations.serializer;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.statements.AbstractStatement;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.GamaMapFactory;
import gama.extension.maths.ode.statements.SingleEquationStatement.SIngleEquationSerializer;
import gama.extension.maths.ode.statements.SingleEquationStatement.SingleEquationValidator;
import gaml.compiler.gaml.expression.AbstractNAryOperator;

/**
 *
 * The class SingleEquationStatement. Implements an Equation in the form function(n, t) = expression; The left function
 * is only here as a placeholder for enabling a simpler syntax and grabbing the variable as its left member.
 *
 * @comment later, the order will be used as it will require a different integrator to solve the equation. For the
 *          moment, it is just here to show how to compute it from the function used
 *
 * @author Alexis Drogoul, Huynh Quang Nghi
 * @since 26 janv. 2013
 *
 */

@symbol (
		name = { EQUALS },
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.EQUATION, IConcept.MATH })
@facets (
		value = { @facet (
				name = EQUATION_LEFT,
				type = IType.NONE,
				optional = false,
				doc = @doc ("the left part of the equation (it should be a variable or a call to the diff() or diff2() operators) ")),
				@facet (
						name = EQUATION_RIGHT,
						type = IType.FLOAT,
						optional = false,
						doc = @doc ("the right part of the equation (it is mandatory that it can be evaluated as a float")) },
		omissible = EQUATION_RIGHT)
@inside (
		symbols = EQUATION)
@validator (SingleEquationValidator.class)
@serializer (SIngleEquationSerializer.class)
@doc (
		value = "Allows to implement an equation in the form function(n, t) = expression. The left function is only here as a placeholder for enabling a simpler syntax and grabbing the variable as its left member.",
		usages = { @usage (
				value = "The syntax of the = statement is a bit different from the other statements. It has to be used as follows (in an equation):",
				examples = { @example (
						value = "float t;",
						isExecutable = false),
						@example (
								value = "float S;",
								isExecutable = false),
						@example (
								value = "float I;",
								isExecutable = false),
						@example (
								value = "equation SI { ",
								isExecutable = false),
						@example (
								value = "   diff(S,t) = (- 0.3 * S * I / 100);",
								isExecutable = false),
						@example (
								value = "   diff(I,t) = (0.3 * S * I / 100);",
								isExecutable = false),
						@example (
								value = "} ",
								isExecutable = false) }) },
		see = { EQUATION, SOLVE })
@SuppressWarnings ({ "rawtypes" })
public class SingleEquationStatement extends AbstractStatement {

	/** The Constant orderNames. */
	public static final Map<String, Integer> orderNames = GamaMapFactory.create();
	static {
		orderNames.put(ZERO, 0);
		orderNames.put(DIFF, 1);
		orderNames.put(DIF2, 2);
	}

	/**
	 * The Class SIngleEquationSerializer.
	 */
	public static class SIngleEquationSerializer implements ISymbolSerializer {

		@Override
		public void serialize(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
			sb.append(desc.getFacet(EQUATION_LEFT).serializeToGaml(includingBuiltIn)).append(" = ")
					.append(desc.getFacet(EQUATION_RIGHT).serializeToGaml(includingBuiltIn)).append(";");
		}
	}

	/**
	 * The Class SingleEquationValidator.
	 */
	public static class SingleEquationValidator implements IDescriptionValidator<IDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription d) {

			final IExpressionDescription fDesc = d.getFacet(EQUATION_LEFT);
			final IExpression func = fDesc.getExpression();
			final IExpressionDescription eDesc = d.getFacet(EQUATION_RIGHT);
			final IExpression expr = eDesc.getExpression();
			final boolean isFunction = func instanceof IOperator && orderNames.containsKey(func.getName());
			if (!isFunction) {
				d.error("The left-hand member of an equation should be a variable or a call to the diff() or diff2() operators",
						IGamlIssue.UNKNOWN_OPERATOR, fDesc.getTarget());
				return;
			}

			final IType<?> type = ((IOperator) func).arg(0).getGamlType();
			if (!type.isTranslatableInto(Types.FLOAT)) {
				d.error("The variable of the left-hand member of an equation is expected to be of type float",
						IGamlIssue.WRONG_TYPE, fDesc.getTarget());
				return;
			}

			if (expr == null || !expr.getGamlType().isTranslatableInto(Types.FLOAT)) {
				d.error("The right-hand member of an equation is expected to be of type float", IGamlIssue.WRONG_TYPE,
						eDesc.getTarget());
			}
		}
	}

	/** The expression. */
	private IExpression function, expression;

	/** The var. */
	private final List<IExpression> var = new ArrayList<>();

	/** The var t. */
	private IExpression var_t;

	/**
	 * Gets the function.
	 *
	 * @return the function
	 */
	public IExpression getFunction() { return function; }

	/**
	 * Sets the function.
	 *
	 * @param function
	 *            the new function
	 */
	public void setFunction(final IExpression function) { this.function = function; }

	/**
	 * Gets the expression.
	 *
	 * @return the expression
	 */
	public IExpression getExpression() { return expression; }

	/**
	 * Sets the expression.
	 *
	 * @param expression
	 *            the new expression
	 */
	public void setExpression(final IExpression expression) { this.expression = expression; }

	/**
	 * Gets the vars.
	 *
	 * @return the vars
	 */
	public List<IExpression> getVars() { return var; }

	/**
	 * Gets the var time.
	 *
	 * @return the var time
	 */
	public IExpression getVarTime() { return var_t; }

	/**
	 * Gets the var.
	 *
	 * @param index
	 *            the index
	 * @return the var
	 */
	public IExpression getVar(final int index) {
		return var.get(index);
	}

	/**
	 * Sets the var t.
	 *
	 * @param vt
	 *            the new var t
	 */
	public void setVar_t(final IVarExpression vt) { this.var_t = vt; }

	/**
	 * Instantiates a new single equation statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public SingleEquationStatement(final IDescription desc) {
		super(desc);
		function = getFacet(EQUATION_LEFT);
		expression = getFacet(EQUATION_RIGHT);
	}

	/**
	 * Establish var.
	 */
	public void establishVar() {
		if (getOrder() == 0) return;
		int i = 0;
		for (i = 0; i < ((AbstractNAryOperator) function).numArg() - 1; i++) {
			final IExpression tmp = ((AbstractNAryOperator) function).arg(i);
			var.add(i, tmp);
		}
		var_t = ((AbstractNAryOperator) function).arg(i);
	}

	/**
	 * This method is normally called by the system of equations to which this equation belongs. It simply computes the
	 * expression that represents the equation and returns it. The storage of the new values is realized in
	 * SystemOfEquationsStatement, in order not to generate side effects (e.g. the value of a shared variable changing
	 * between the integrations of two equations)
	 *
	 * @see gama.api.gaml.statements.AbstractStatement#privateExecuteIn(gama.api.runtime.scope.IScope)
	 */
	@Override
	protected Double privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final Double result = (Double) expression.value(scope);

		return result;
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		return expression.value(scope);// super.executeOn(scope);
	}

	@Override
	public long getOrder() { return orderNames.get(function.getName()); }

	// Placeholders operators that are (normally) never called.
	// FIXME Can probably be replaced by actions, so that they do not pollute
	// the whole scope of
	// GAMA operators.
	// TODO And maybe they can do something useful, like gathering the order, or
	// the var or var_t,
	// whenever they are called.

	/**
	 * Diff.
	 *
	 * @param scope
	 *            the scope
	 * @param var
	 *            the var
	 * @param time
	 *            the time
	 * @return the double
	 */
	@operator (
			value = DIFF,
			concept = { IConcept.EQUATION, IConcept.MATH })
	@doc (
			value = "A placeholder function for expressing equations")
	@no_test (Reason.IMPOSSIBLE_TO_TEST)

	public static Double diff(final IScope scope, final Double var, final Double time) {
		return Double.NaN;
	}

	/**
	 * Diff 2.
	 *
	 * @param scope
	 *            the scope
	 * @param var
	 *            the var
	 * @param time
	 *            the time
	 * @return the double
	 */
	@operator (
			value = DIF2,
			concept = { IConcept.EQUATION, IConcept.MATH })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	@doc (
			value = "A placeholder function for expressing equations")
	public static Double diff2(final IScope scope, final Double var, final Double time) {
		return Double.NaN;
	}

	/**
	 * Placeholder for zero-order equations. The expression on the right allows to pass the variable directly (maybe
	 * useful one day).
	 *
	 * @param scope
	 * @param var
	 * @param time
	 * @return
	 */
	@operator (
			value = ZERO,
			concept = { IConcept.EQUATION, IConcept.MATH },
			internal = true)
	@doc (
			value = "An internal placeholder function")
	@no_test (Reason.IMPOSSIBLE_TO_TEST)

	public static Double f(final IScope scope, final IExpression var) {
		return Double.NaN;
	}

	@Override
	public String toString() {
		return function.toString() + " = " + expression.toString();
	}

}

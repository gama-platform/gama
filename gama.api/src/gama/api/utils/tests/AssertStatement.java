/*******************************************************************************************************
 *
 * AssertStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import java.util.Collection;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaAssertException;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.statements.AbstractStatement;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;

/**
 * The Class AssertStatement.
 */
@symbol (
		name = "assert",
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.TEST })
@facets (
		value = { @facet (
				name = IKeyword.VALUE,
				type = IType.BOOL,
				optional = false,
				doc = @doc ("a boolean expression. If its evaluation is true, the assertion is successful. Otherwise, an error (or a warning) is raised.")),
				@facet (
						name = "label",
						type = IType.STRING,
						optional = true,
						doc = @doc ("a string displayed instead of the failed expression in order to customize the error or warning if the assertion is false")),
				@facet (
						name = IKeyword.WARNING,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("if set to true, makes the assertion emit a warning instead of an error")) },
		omissible = IKeyword.VALUE)
@inside (
		symbols = { "test", "action" },
		kinds = { ISymbolKind.ACTION, ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT })
@doc (
		value = "Allows to check if the evaluation of a given expression returns true. If not, an error (or a warning) is raised. If the statement is used inside a test, the error is not propagagated but invalidates the test (in case of a warning, it partially invalidates it). Otherwise, it is normally propagated",
		usages = { @usage (
				value = "Any boolean expression can be used",
				examples = { @example ("assert (2+2) = 4;"), @example ("assert self != nil;"),
						@example ("int t <- 0; assert is_error(3/t);"), @example ("(1 / 2) is float") }),

				@usage (
						value = "if the 'warn:' facet is set to true, the statement emits a warning (instead of an error) in case the expression is false",
						examples = { @example ("assert 'abc' is string warning: true") }) },
		see = { "test", "setup", "is_error", "is_warning" })
public class AssertStatement extends AbstractStatement implements WithTestSummary<AssertionSummary> {

	/** The warn. */
	final IExpression value, warn, label;

	/** The summary. */
	final AssertionSummary summary;

	/**
	 * Instantiates a new assert statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public AssertStatement(final IDescription desc) {
		super(desc);
		value = getFacet(IKeyword.VALUE);
		warn = getFacet(IKeyword.WARNING);
		label = getFacet("label");
		summary = new AssertionSummary(this);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		summary.reset();
		boolean result;
		try {
			result = Cast.asBool(scope, value.value(scope));
		} catch (final GamaRuntimeException e) {
			summary.setErrorWithTrace(e);
			summary.setState(TestState.ABORTED);
			throw e;
		}
		if (!result) {
			final boolean warning = isWarning(scope);
			final TestState s = warning ? TestState.WARNING : TestState.FAILED;
			summary.setState(s);
			if (!warning) {
				try {
					summary.setEvaluatedExpression(resolveExpression(value, scope));
				} catch (final GamaRuntimeException e) {
					summary.setEvaluatedExpression(value.serializeToGaml(true) + " [value expansion failed: "
							+ e.getMessage() + "]");
				}
			}
			final var failure = new GamaAssertException(scope,
					"Assert failed: "
							+ (label == null ? getTitleForSummary() : Cast.asString(scope, label.value(scope))),
					warning);
			summary.setErrorWithTrace(failure);
			throw failure;
		}
		summary.setState(TestState.PASSED);
		return result;
	}

	private String resolveExpression(final IExpression expression, final IScope scope) {
		if (expression == null) return "nil";
		if (expression instanceof IVarExpression variable) {
			return StringUtils.toGaml(variable.value(scope), true);
		}
		if (expression instanceof IExpression.List list) {
			final StringBuilder result = new StringBuilder("[");
			for (final IExpression element : list.getElements()) {
				if (result.length() > 1) { result.append(", "); }
				result.append(resolveExpression(element, scope));
			}
			return result.append(']').toString();
		}
		if (expression instanceof IExpression.Map map) {
			final StringBuilder result = new StringBuilder("[");
			for (final java.util.Map.Entry<IExpression, IExpression> entry : map.getElements().entrySet()) {
				if (result.length() > 1) { result.append(", "); }
				result.append(resolveExpression(entry.getKey(), scope)).append("::")
						.append(resolveExpression(entry.getValue(), scope));
			}
			return result.append(']').toString();
		}
		if (expression instanceof IOperator operator
				&& operator.getPrototype() instanceof IArtefact.Operator prototype) {
			final int arity = prototype.getSignature().list().length;
			final String[] arguments = new String[arity];
			for (int i = 0; i < arity; i++) {
				arguments[i] = resolveChild(operator.arg(i), scope);
			}
			final String name = expression.getName();
			if (arity == 1 && ArtefactRegistry.ARTEFACTS_WITHOUT_PARENTHESES.contains(name)) {
				return name + arguments[0];
			}
			if (arity == 2 && IKeyword.AS.equals(name)) {
				return operator.arg(1).serializeToGaml(true) + "(" + resolveExpression(operator.arg(0), scope) + ")";
			}
			if (arity == 2 && "internal_at".equals(name)) { return arguments[0] + arguments[1]; }
			if (arity == 2 && ArtefactRegistry.BINARY_ARTEFACTS_NAMES.contains(name)) {
				return arguments[0] + " " + name + " " + arguments[1];
			}
			return name + "(" + String.join(", ", arguments) + ")";
		}
		if (expression.isConst()) { return StringUtils.toGaml(expression.getConstValue(), true); }
		return expression.serializeToGaml(true);
	}

	private String resolveChild(final IExpression expression, final IScope scope) {
		final String resolved = resolveExpression(expression, scope);
		if (expression instanceof IOperator && !(expression instanceof IVarExpression)
				&& expression.shouldBeParenthesized()) return "(" + resolved + ")";
		return resolved;
	}

	/**
	 * Checks if is warning.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is warning
	 */
	public boolean isWarning(final IScope scope) {
		return warn != null && Cast.asBool(scope, warn.value(scope));
	}

	@Override
	public AssertionSummary getSummary() { return summary; }

	@Override
	public String getTitleForSummary() { return value.serializeToGaml(true); }

	@Override
	public Collection<? extends WithTestSummary<?>> getSubElements() { return null; }

}

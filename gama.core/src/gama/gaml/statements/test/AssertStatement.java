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
package gama.gaml.statements.test;

import java.util.Collection;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaAssertException;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;

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
						name = "warning",
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
		warn = getFacet("warning");
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
			summary.setError(e.getMessage());
			summary.setState(TestState.ABORTED);
			throw e;
		}
		if (!result) {
			final TestState s = isWarning(scope) ? TestState.WARNING : TestState.FAILED;
			summary.setState(s);
			throw new GamaAssertException(scope,
					"Assert failed: "
							+ (label == null ? getTitleForSummary() : Cast.asString(scope, label.value(scope))),
					isWarning(scope));
		}
		summary.setState(TestState.PASSED);
		return result;
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

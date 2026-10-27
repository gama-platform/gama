/*******************************************************************************************************
 *
 * TestStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaAssertException;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import one.util.streamex.StreamEx;

/**
 * The Class TestStatement.
 */
@symbol (
		name = { "test" },
		kind = ISymbolKind.BEHAVIOR,
		with_sequence = true,
		unique_name = true,
		concept = { IConcept.TEST })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = true,
				doc = @doc ("identifier of the test")) },
		omissible = IKeyword.NAME)
@doc (
		value = "The test statement allows modeler to define a set of assertions that will be tested. Assertions nested in loops, conditional branches and other statement blocks are included in the test summary. Before the execution of the embedded set of instructions, if a setup is defined in the species, model or experiment, it is executed. In a test, if one assertion fails, the evaluation of other assertions continue.",
		usages = { @usage (
				value = "An example of use:",
				examples = { @example (
						value = "species Tester {",
						isExecutable = false),
						@example (
								value = "    // set of attributes that will be used in test",
								isExecutable = false),
						@example (
								value = "",
								isExecutable = false),
						@example (
								value = "    setup {",
								isExecutable = false),
						@example (
								value = "        // [set of instructions... in particular initializations]",
								isExecutable = false),
						@example (
								value = "    }",
								isExecutable = false),
						@example (
								value = "",
								isExecutable = false),
						@example (
								value = "    test t1 {",
								isExecutable = false),
						@example (
								value = "       // [set of instructions, including asserts]",
								isExecutable = false),
						@example (
								value = "    }",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }) },
		see = { "setup", "assert" })
public class TestStatement extends AbstractStatementSequence implements IStatement.Test {

	/** The setup. */
	SetUpStatement setup = null;

	/** The assertions. */
	// Assertions contained in the test.
	List<AssertStatement> assertions = new ArrayList<>();

	/** The summary. */
	IndividualTestSummary summary;

	private record SelectionMatch(TestSelection selection, boolean selected) {}

	/** Last selection checked, cached because tests of species are executed by every agent at every step. */
	private volatile SelectionMatch lastMatch;

	/**
	 * Instantiates a new test statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public TestStatement(final IDescription desc) {
		super(desc);
		if (hasFacet(IKeyword.NAME)) { setName(getLiteral(IKeyword.NAME)); }
	}

	@Override
	public IndividualTestSummary getSummary() {
		if (summary == null) { summary = new IndividualTestSummary(this); }
		return summary;
	}

	@Override
	public void setEnclosing(final ISymbol enclosing) {
		super.setEnclosing(enclosing);
		if (enclosing instanceof ISpecies spec) {
			setup = (SetUpStatement) StreamEx.of(spec.getBehaviors()).findFirst(SetUpStatement.class::isInstance)
					.orElse(null);
		}
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		super.setChildren(commands);
		assertions.clear();
		collectAssertions(getSubStatements());
		summary = null;
	}

	private void collectAssertions(final Iterable<? extends IStatement> statements) {
		for (final IStatement statement : statements) {
			if (statement instanceof AssertStatement assertion) {
				assertions.add(assertion);
			} else if (statement instanceof AbstractStatementSequence sequence) {
				collectAssertions(sequence.getSubStatements());
			}
		}
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final var gui = GAMA.getGui();
		if (gui.isTestStopRequested() || scope.interrupted() || !isSelected(TestSelection.active(scope))) return null;
		getSummary().reset();
		TestExperimentRunner.entering(scope, getSummary());
		gui.displayRunningTest(scope, getName());
		gui.displayTestSummary(scope, getSummary(), false);
		Object lastResult = null;
		try {
			if (setup != null && !gui.isTestStopRequested()) { setup.setup(scope); }
			scope.enableTryMode();
			for (final IStatement statement : commands) {
				if (gui.isTestStopRequested() || scope.interrupted()) break;
				try {
					// TODO Verify this call (wrt IScope.execute())
					lastResult = statement.executeOn(scope);
				} catch (final GamaAssertException e) {} catch (final GamaRuntimeException e) {
					if (!(statement instanceof AssertStatement)) {
						getSummary().setState(TestState.ABORTED);
						getSummary().setErrorWithTrace(e);
						break;
					}

				} catch (final RuntimeException e) {
					// A statement that throws a Java exception instead of a GAML error only aborts this test:
					// uncaught, it would end the whole experiment and its other tests would not be reported
					getSummary().setState(TestState.ABORTED);
					getSummary().setErrorWithTrace(GamaRuntimeException.create(e, scope));
					break;
				}
				gui.displayTestSummary(scope, getSummary(), false);
			}
		} finally {
			scope.disableTryMode();
			if (gui.isTestStopRequested()) {
				getSummary().setState(TestState.INTERRUPTED);
				getSummary().setError("Interrupted by user");
			}
			TestExperimentRunner.leaving(getSummary());
			gui.displayTestSummary(scope, getSummary(), true);
		}
		return lastResult;

	}

	private boolean isSelected(final TestSelection selection) {
		if (selection == null) return true;
		SelectionMatch match = lastMatch;
		if (match == null || match.selection() != selection) {
			match = new SelectionMatch(selection, selection.matches(this));
			lastMatch = match;
		}
		return match.selected();
	}

	@Override
	public String getTitleForSummary() { return getName(); }

	@Override
	public Collection<? extends WithTestSummary<?>> getSubElements() { return assertions; }

}

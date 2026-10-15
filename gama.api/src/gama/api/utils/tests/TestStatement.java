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
 * GAML {@code test} statement that executes a sequence of statements while collecting the summaries of every nested
 * {@link AssertStatement}.
 *
 * <p>
 * When executed, a test resets its {@link #summary}, registers itself in {@link TestExperimentRunner}, optionally runs
 * the enclosing {@link SetUpStatement}, enables the scope try mode, and then executes each child statement in order.
 * The first failed assertion stops the test, while any other
 * {@link GamaRuntimeException} or unexpected {@link RuntimeException} aborts the test and records the corresponding
 * error in the summary.
 * </p>
 *
 * <p>
 * The list of {@link #assertions} is rebuilt from the statement tree whenever the children are set, so nested
 * assertions appearing in loops, conditionals, or other statement sequences contribute to the reported summary.
 * </p>
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
		value = "The test statement executes a sequence of instructions intended for model validation and builds a summary from all nested assertions, including those located inside loops, conditionals, and other statement blocks. Before executing the body of the test, it runs the enclosing setup if one is available. The first failed assertion stops the test and marks it as failed, whereas any non-assert runtime error aborts the test and is stored in its summary.",
		usages = { @usage (
				value = "A test usually relies on a setup block for initialization and may contain assertions directly or inside nested statement sequences:",
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

	/**
	 * Setup sequence resolved from the enclosing species-like symbol and executed before the test body when available.
	 */
	SetUpStatement setup = null;

	/**
	 * Flat list of all assertions contained in this test, including assertions nested inside child statement sequences.
	 */
	List<AssertStatement> assertions = new ArrayList<>();

	/**
	 * Lazily created summary that aggregates the state of this test and the summaries of its nested assertions.
	 */
	IndividualTestSummary summary;

	/**
	 * Creates a test statement from its compiled description and uses the optional {@code name:} facet as the summary
	 * title.
	 *
	 * @param desc
	 *            compiled description of the statement
	 */
	public TestStatement(final IDescription desc) {
		super(desc);
		if (hasFacet(IKeyword.NAME)) { setName(getLiteral(IKeyword.NAME)); }
	}

	/**
	 * Returns the summary associated with this test, creating it on first access.
	 *
	 * @return the summary used to report this test execution
	 */
	@Override
	public IndividualTestSummary getSummary() {
		if (summary == null) { summary = new IndividualTestSummary(this); }
		return summary;
	}

	/**
	 * Sets the enclosing symbol and resolves the first {@link SetUpStatement} declared among its behaviors, if any.
	 *
	 * @param enclosing
	 *            symbol that contains this test
	 */
	@Override
	public void setEnclosing(final ISymbol enclosing) {
		super.setEnclosing(enclosing);
		if (enclosing instanceof ISpecies spec) {
			setup = (SetUpStatement) StreamEx.of(spec.getBehaviors()).findFirst(SetUpStatement.class::isInstance)
					.orElse(null);
		}
	}

	/**
	 * Stores the child statements, rebuilds the flattened assertion list, and invalidates the cached summary so it can
	 * be recreated from the new structure.
	 *
	 * @param commands
	 *            child symbols that form the body of the test
	 */
	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		super.setChildren(commands);
		assertions.clear();
		collectAssertions(getSubStatements());
		summary = null;
	}

	/**
	 * Recursively traverses nested statement sequences to collect every assertion contained in this test.
	 *
	 * @param statements
	 *            statements to inspect
	 */
	private void collectAssertions(final Iterable<? extends IStatement> statements) {
		for (final IStatement statement : statements) {
			if (statement instanceof AssertStatement assertion) {
				assertions.add(assertion);
			} else if (statement instanceof AbstractStatementSequence sequence) {
				collectAssertions(sequence.getSubStatements());
			}
		}
	}

	/**
	 * Executes this test.
	 *
	 * <p>
	 * The method resets the summary, notifies the test runner and GUI, optionally executes the resolved setup,
	 * activates try mode on the scope, and then executes each child statement in order. A failed assertion stops further
	 * execution, whereas other GAML or Java runtime failures abort the test and mark its summary
	 * accordingly. The GUI is refreshed after each executed statement and once again when leaving the test.
	 * </p>
	 *
	 * @param scope
	 *            execution scope of the current test
	 * @return the value returned by the last executed child statement, or {@code null} when nothing ran
	 * @throws GamaRuntimeException
	 *             if the surrounding execution infrastructure raises one before it is handled here
	 */
	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final var gui = GAMA.getGui();
		if (gui.isTestStopRequested() || scope.interrupted()) return null;
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
				} catch (final GamaAssertException e) {
					if (!e.isWarning()) break;
				} catch (final GamaRuntimeException e) {
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

	/**
	 * Returns the title displayed in test summaries.
	 *
	 * @return the name of this test
	 */
	@Override
	public String getTitleForSummary() { return getName(); }

	/**
	 * Returns the nested summary-bearing elements of this test.
	 *
	 * @return the collected assertions contained in this test
	 */
	@Override
	public Collection<? extends WithTestSummary<?>> getSubElements() { return assertions; }

}

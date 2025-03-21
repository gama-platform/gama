/*******************************************************************************************************
 *
 * TestStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaAssertException;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.species.GamlSpecies;
import gama.gaml.statements.AbstractStatementSequence;
import gama.gaml.statements.IStatement;
import gama.gaml.types.IType;

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
		value = "The test statement allows modeler to define a set of assertions that will be tested. Before the execution of the embedded set of instructions, if a setup is defined in the species, model or experiment, it is executed. In a test, if one assertion fails, the evaluation of other assertions continue.",
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
public class TestStatement extends AbstractStatementSequence implements WithTestSummary<IndividualTestSummary> {

	/** The setup. */
	SetUpStatement setup = null;

	/** The assertions. */
	// Assertions contained in the test.
	List<AssertStatement> assertions = new ArrayList<>();

	/** The summary. */
	IndividualTestSummary summary;

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
		setup = (SetUpStatement) ((GamlSpecies) enclosing).getBehaviors().stream()
				.filter(SetUpStatement.class::isInstance).findAny().orElse(null);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		super.setChildren(commands);
		commands.forEach(s -> { if (s instanceof AssertStatement) { assertions.add((AssertStatement) s); } });
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		getSummary().reset();
		if (setup != null) { setup.setup(scope); }
		Object lastResult = null;
		try {
			scope.enableTryMode();
			for (final IStatement statement : commands) {
				try {
					// TODO Verify this call (wrt IScope.execute())
					lastResult = statement.executeOn(scope);
				} catch (final GamaAssertException e) {} catch (final GamaRuntimeException e) {
					if (!(statement instanceof AssertStatement)) {
						getSummary().setState(TestState.ABORTED);
						getSummary().setError(e.getMessage());
						break;
					}

				}
			}
		} finally {
			scope.disableTryMode();
		}
		return lastResult;

	}

	@Override
	public String getTitleForSummary() { return getName(); }

	@Override
	public Collection<? extends WithTestSummary<?>> getSubElements() { return assertions; }

}

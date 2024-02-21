/*******************************************************************************************************
 *
 * SetUpStatement.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.statements.test;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.statements.AbstractStatementSequence;

/**
 * The Class SetUpStatement.
 */
@symbol (
		name = { "setup" },
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		concept = { IConcept.TEST })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL })
@doc (
		value = "The setup statement is used to define the set of instructions that will be executed before every [#test test].",
		usages = { @usage (
				value = "As every test should be independent from the others, the setup will mainly contain initialization of variables that will be used in each test.",
				examples = { @example (
						value = "species Tester {",
						isExecutable = false),
						@example (
								value = "    int val_to_test;",
								isExecutable = false),
						@example (
								value = "",
								isExecutable = false),
						@example (
								value = "    setup {",
								isExecutable = false),
						@example (
								value = "        val_to_test <- 0;",
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
		see = { "test", "assert" })
public class SetUpStatement extends AbstractStatementSequence {
	
	/**
	 * Instantiates a new sets the up statement.
	 *
	 * @param desc the desc
	 */
	public SetUpStatement(final IDescription desc) {
		super(desc);
		setName("setup");
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		// does nothing when called « normally »
		return null;
	}

	/**
	 * Setup.
	 *
	 * @param scope the scope
	 * @return the object
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	public Object setup(final IScope scope) throws GamaRuntimeException {
		// calls the « normal » execution defined in the superclass
		return super.executeOn(scope);
	}

}

/*******************************************************************************************************
 *
 * ArgStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.validation.ValidNameValidator;
import gama.api.constants.IKeyword;
import gama.api.gaml.types.IType;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;

/**
 * The Class ArgStatement.
 */
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.NEW_TEMP_ID,
				optional = false,
				doc = @doc ("the name of the action argument ")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("the type of the action argument (only in an action statement)")),
				@facet (
						name = IKeyword.OF,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("the type of the content of the argument, if its type is a container (only in an action statement)")),
				@facet (
						name = IKeyword.INDEX,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("the type of the key of the argument, if its type is a map (only in an action statement)")),
				@facet (
						name = IKeyword.OPTIONAL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("a boolean specifying if the argument is optional (false by default) (only in an action statement)")),
				@facet (
						name = IKeyword.VALUE,
						type = IType.NONE,
						optional = true,
						doc = @doc ("the value of the argument (only in a do statement)")),
				@facet (
						name = IKeyword.ID,
						type = IType.BOOL,
						internal = true,
						optional = true,
						doc = @doc ("whether the argument is to be treated as an id or as a value")),
				@facet (
						name = IKeyword.DEFAULT,
						type = { IType.NONE },
						optional = true,
						doc = @doc ("the default value of the argument (only in an action statement)")) },
		omissible = IKeyword.NAME)
@symbol (
		name = IKeyword.ARG,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		unique_name = true,
		internal = true,
		concept = { IConcept.ACTION })
@inside (
		symbols = { IKeyword.ACTION, IKeyword.DO, IKeyword.INVOKE })
@validator (ValidNameValidator.class)
@doc (
		value = "Argument ",
		usages = { @usage (
				value = "In an action, it is used to define the paramaters of the action. Facets type:, optional: and default: can be used in this case to characterize the arguments.",
				examples = { @example (
						value = "action swap {",
						isExecutable = false),
						@example (
								value = "		arg arg1 type: int default: 3;",
								isExecutable = false),
						@example (
								value = "	arg arg2 type: int optional: true;",
								isExecutable = false),
						@example (
								value = "	// ....",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "In the call of an action, i.e. in a do statement, it is used to explicit the values given to arguments. Facets value: cna be used in this case.",
						examples = { @example (
								value = "int val1 <- 5;",
								isExecutable = false),
								@example (
										value = "do swap {",
										isExecutable = false),
								@example (
										value = "		arg arg1 value: 7;",
										isExecutable = false),
								@example (
										value = "	arg arg2 value: val1 ;",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) },
		see = { "action", "do" })
public class ArgStatement extends AbstractPlaceHolderStatement {

	/**
	 * Instantiates a new arg statement.
	 *
	 * @param desc
	 *            the desc
	 */
	// A placeholder for arguments of actions
	public ArgStatement(final IDescription desc) {
		super(desc);
	}

}
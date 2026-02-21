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
 * Placeholder statement for argument declarations in actions.
 * 
 * <p>
 * ArgStatement represents a parameter declaration in an action definition. It doesn't execute any runtime code but
 * provides metadata about arguments including their name, type, default value, and whether they're optional.
 * </p>
 * 
 * <h2>Usage Contexts</h2>
 * 
 * <h3>In Action Declarations</h3>
 * <p>
 * Defines formal parameters for actions:
 * </p>
 * <pre>
 * {@code
 * action move_to {
 *     arg target type: point;
 *     arg speed type: float default: 1.0;
 *     arg ignore_obstacles type: bool optional: true;
 *     // Action body...
 * }
 * }
 * </pre>
 * 
 * <h3>In Action Invocations (do statements)</h3>
 * <p>
 * Provides actual values for parameters:
 * </p>
 * <pre>
 * {@code
 * do move_to {
 *     arg target value: {100, 100};
 *     arg speed value: 2.0;
 * }
 * }
 * </pre>
 * 
 * <h2>Facets</h2>
 * <ul>
 * <li><b>name:</b> The parameter name</li>
 * <li><b>type:</b> The parameter type (in declarations)</li>
 * <li><b>of:</b> Content type for container parameters</li>
 * <li><b>index:</b> Key type for map parameters</li>
 * <li><b>optional:</b> Whether the parameter is optional (default: false)</li>
 * <li><b>default:</b> Default value if not provided</li>
 * <li><b>value:</b> Actual value (in invocations)</li>
 * </ul>
 * 
 * <h2>Modern Syntax</h2>
 * <p>
 * Note that modern GAML syntax uses the shorter form without explicit 'arg' keywords:
 * </p>
 * <pre>
 * {@code
 * // Declaration
 * action move_to(point target, float speed <- 1.0) { ... }
 * 
 * // Invocation
 * do move_to(target: {100, 100}, speed: 2.0);
 * }
 * </pre>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see ActionStatement
 * @see AbstractPlaceHolderStatement
 * @see Arguments
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
	 * Constructs a new argument declaration statement.
	 * 
	 * <p>
	 * This is a placeholder statement that provides metadata but executes no runtime code.
	 * </p>
	 *
	 * @param desc
	 *            the argument description containing type and default value information
	 */
	public ArgStatement(final IDescription desc) {
		super(desc);
	}

}
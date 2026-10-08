/*******************************************************************************************************
 *
 * ReflexStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.architecture.reflex;

import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.validation.ValidNameValidator;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
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

/**
 * The Class ReflexStatement.
 */
@symbol (
		name = { IKeyword.REFLEX, IKeyword.INIT, IKeyword.ABORT },
		kind = ISymbolKind.BEHAVIOR,
		with_sequence = true,
		unique_name = true,
		concept = { IConcept.BEHAVIOR, IConcept.SCHEDULER })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = IKeyword.WHEN,
				type = IType.BOOL,
				optional = true,
				doc = @doc ("an expression that evaluates a boolean, the condition to fulfill in order to execute the statements embedded in the reflex.")),
				@facet (
						name = IKeyword.NAME,
						type = IType.ID,
						optional = true,
						doc = @doc ("the identifier of the reflex")) },
		omissible = IKeyword.NAME)
@validator (ValidNameValidator.class)
@doc (
		value = "Reflexes are sequences of statements that can be executed by the agent. Reflexes prefixed by the 'reflex' keyword are executed continuously. Reflexes prefixed by 'init' are executed only immediately after the agent has been created. Reflexes prefixed by 'abort' just before the agent is killed. If a facet when: is defined, a reflex is executed only if the boolean expression evaluates to true.",
		usages = { @usage (
				value = "Reflexes are sequences of statements that are executed by the agent at each step, when scheduled and if the when: condition is true.",
				examples = { @example (
						value = "reflex my_reflex when: flip (0.5){ 		//Only executed when flip returns true",
						isExecutable = false),
						@example (
								value = "    write \"Executing the unconditional reflex\";",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = " 'init' reflexes are executed only immediately after the agent has been created.",
						examples = { @example (
								value = "init { write \"I am created\"; }",
								isExecutable = false) }),
				@usage (
						value = " 'abort' reflexes are executed just before the agent is killed.",
						examples = { @example (
								value = "abort { write \"Last actions before being removed \"; }",
								isExecutable = false) }) })
public class ReflexStatement extends AbstractStatementSequence {

	/** The when. */
	private final IExpression when;

	/**
	 * Instantiates a new reflex statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public ReflexStatement(final IDescription desc) {
		super(desc);
		when = getFacet(IKeyword.WHEN);
		if (hasFacet(IKeyword.NAME)) { setName(getLiteral(IKeyword.NAME)); }
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		return when == null || Cast.asBool(scope, when.value(scope)) ? super.privateExecuteIn(scope) : null;
	}

}

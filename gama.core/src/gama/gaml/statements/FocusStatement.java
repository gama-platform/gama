/*******************************************************************************************************
 *
 * FocusStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatement;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IShape;

/**
 * Written by drogoul Modified on 6 févr. 2010
 *
 * @todo Description
 *
 */

@symbol (
		name = IKeyword.FOCUS_ON,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.DISPLAY, IConcept.GEOMETRY })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@facets (
		value = { @facet (
				name = IKeyword.VALUE,
				type = IType.NONE,
				optional = false,
				doc = @doc ("The agent, list of agents, geometry to focus on")) },
		omissible = IKeyword.VALUE)
@doc (
		value = "Allows to focus on the passed parameter in all available displays. Passing 'nil' for the parameter will make all screens return to their normal zoom",
		usages = { @usage (
				value = "Focuses on an agent, a geometry, a set of agents, etc...",
				examples = { @example ("focus_on my_species(0);") }) })
public class FocusStatement extends AbstractStatement {

	@Override
	public String getTrace(final IScope scope) {
		// We dont trace focus statements
		return "";
	}

	/** The value. */
	final IExpression value;

	/**
	 * Instantiates a new focus statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public FocusStatement(final IDescription desc) {
		super(desc);
		value = getFacet(IKeyword.VALUE);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		if (agent != null && !agent.dead()) {
			final IShape o = GamaShapeFactory.castToShape(scope, value.value(scope), false);
			GAMA.getGui().setFocusOn(o);
		}
		return value.value(scope);
	}
}

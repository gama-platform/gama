/*******************************************************************************************************
 *
 * FocusStatement.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.statements;

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
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;

/**
 * Written by drogoul Modified on 6 févr. 2010
 * 
 * @todo Description
 * 
 */

@symbol(name = IKeyword.FOCUS_ON, kind = ISymbolKind.SINGLE_STATEMENT, with_sequence = false, concept = {
		IConcept.DISPLAY, IConcept.GEOMETRY })
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@facets(value = {
		@facet(name = IKeyword.VALUE, type = IType.NONE, optional = false, doc = @doc("The agent, list of agents, geometry to focus on")) }, omissible = IKeyword.VALUE)
@doc(value = "Allows to focus on the passed parameter in all available displays. Passing 'nil' for the parameter will make all screens return to their normal zoom", usages = {
		@usage(value = "Focuses on an agent, a geometry, a set of agents, etc...", examples = {
				@example("focus_on my_species(0);") }) })
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
	 * @param desc the desc
	 */
	public FocusStatement(final IDescription desc) {
		super(desc);
		value = getFacet(IKeyword.VALUE);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		if (agent != null && !agent.dead()) {
			final IShape o = Cast.asGeometry(scope, value.value(scope));
			GAMA.getGui().setFocusOn(o);
		}
		return value.value(scope);
	}
}

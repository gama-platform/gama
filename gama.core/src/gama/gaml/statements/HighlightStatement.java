/*******************************************************************************************************
 *
 * HighlightStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.utils.prefs.GamaPreferences;

/**
 * Written by drogoul Modified on 6 févr. 2010
 *
 * @todo Description
 *
 */

@symbol (
		name = IKeyword.HIGHLIGHT,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.DISPLAY, IConcept.COLOR })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@facets (
		value = { @facet (
				name = IKeyword.COLOR,
				type = IType.COLOR,
				doc = @doc ("An optional color to highlight the agent. Note that this color will become the default color for further higlight operations"),
				optional = true),
				@facet (
						name = IKeyword.VALUE,
						type = IType.AGENT,
						optional = false,
						doc = @doc ("The agent to hightlight")) },
		omissible = IKeyword.VALUE)
@doc (
		value = "Allows to highlight the agent passed in parameter in all available displays, optionaly setting a color. Passing 'nil' for the agent will remove the current highlight",
		usages = { @usage (
				value = "Highlighting an agent",
				examples = { @example ("highlight my_species(0) color: #blue;") }) })
public class HighlightStatement extends AbstractStatement {

	@Override
	public String getTrace(final IScope scope) {
		// We dont trace highlight statements
		return "";
	}

	/** The value. */
	final IExpression value;

	/** The color. */
	final IExpression color;

	/**
	 * Instantiates a new highlight statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public HighlightStatement(final IDescription desc) {
		super(desc);
		value = getFacet(IKeyword.VALUE);
		color = getFacet(IKeyword.COLOR);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		if (agent != null && !agent.dead()) {
			final IAgent o = Cast.asAgent(scope, value.value(scope));
			if (color != null) {
				final IColor c = GamaColorFactory.castToColor(scope, color.value(scope));
				if (c != null) { GamaPreferences.Displays.CORE_HIGHLIGHT.set(c); }
			}
			GAMA.getGui().setHighlightedAgent(o);
		}
		return value.value(scope);
	}
}

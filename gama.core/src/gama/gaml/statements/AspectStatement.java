/*******************************************************************************************************
 *
 * AspectStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.awt.geom.Rectangle2D;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;

/**
 * The Class AspectStatement.
 */
@symbol (
		name = IKeyword.ASPECT,
		kind = ISymbolKind.BEHAVIOR,
		with_sequence = true,
		unique_name = true,
		concept = { IConcept.DISPLAY })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = true,
				doc = @doc ("identifier of the aspect (it can be used in a display to identify which aspect should be used for the given species). Two special names can also be used: 'default' will allow this aspect to be used as a replacement for the default aspect defined in preferences; 'highlighted' will allow the aspect to be used when the agent is highlighted as a replacement for the default (application of a color)")) },
		omissible = IKeyword.NAME)
@doc (
		value = "Aspect statement is used to define a way to draw the current agent. Several aspects can be defined in one species. It can use attributes to customize each agent's aspect. The aspect is evaluate for each agent each time it has to be displayed.",
		usages = { @usage (
				value = "An example of use of the aspect statement:",
				examples = { @example (
						value = "species one_species {",
						isExecutable = false),
						@example (
								value = "	int a <- rnd(10);",
								isExecutable = false),
						@example (
								value = "	aspect aspect1 {",
								isExecutable = false),
						@example (
								value = "		if(a mod 2 = 0) { draw circle(a);}",
								isExecutable = false),
						@example (
								value = "		else {draw square(a);}",
								isExecutable = false),
						@example (
								value = "		draw text: \"a= \" + a color: #black size: 5;",
								isExecutable = false),
						@example (
								value = "	}",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }) })
public class AspectStatement extends AbstractStatementSequence {

	/** The is highlight aspect. */
	boolean isHighlightAspect;

	/**
	 * Instantiates a new aspect statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public AspectStatement(final IDescription desc) {
		super(desc);
		setName(getLiteral(IKeyword.NAME, IKeyword.DEFAULT));
		isHighlightAspect = "highlighted".equals(getName());
	}

	@Override
	public Rectangle2D executeOn(final IScope sc) {
		if (!sc.isGraphics()) return null;
		IGraphicsScope scope = (IGraphicsScope) sc;
		final IAgent agent = scope.getAgent();
		final boolean shouldHighlight = agent == scope.getGui().getHighlightedAgent() && !isHighlightAspect;
		if (agent != null && !agent.dead()) {
			IGraphics g = scope.getGraphics();
			// hqnghi: try to find scope from experiment
			// AD: removed as it should not be necessary... Or else we create a ISimulationAgent.getGraphicsScope()
			// method ??
			// if (g == null) { g = GAMA.getExperiment().getAgent().getSimulation().getScope().getGraphics(); }
			// end-hqnghi
			if (g == null) return null;
			try {
				if (scope.interrupted()) return null;
				if (shouldHighlight) { g.beginHighlight(); }
				return (Rectangle2D) super.executeOn(scope);
			} catch (final GamaRuntimeException e) {
				// cf. Issue 1052: exceptions are not thrown, just displayed
				e.printStackTrace();
			} finally {
				if (shouldHighlight) { g.endHighlight(); }
				// agent.releaseLock();
			}

		}
		return null;

	}

	@Override
	public Rectangle2D privateExecuteIn(final IScope sc) throws GamaRuntimeException {
		if (!sc.isGraphics()) return null;
		IGraphicsScope scope = (IGraphicsScope) sc;
		final IGraphics g = scope.getGraphics();
		super.privateExecuteIn(scope);
		return g.getAndWipeTemporaryEnvelope();
	}
}

/*******************************************************************************************************
 *
 * FsmTransitionStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.architecture.fsm;

import java.util.Arrays;
import java.util.List;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.serializer;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.gaml.architecture.fsm.FsmTransitionStatement.TransitionSerializer;
import gama.gaml.architecture.fsm.FsmTransitionStatement.TransitionValidator;

/**
 * The Class FsmTransitionStatement.
 */
@symbol (
		name = FsmTransitionStatement.TRANSITION,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true)
@inside (
		kinds = { ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.BEHAVIOR })
@facets (
		value = { @facet (
				name = IKeyword.WHEN,
				type = IType.BOOL,
				optional = true,
				doc = @doc ("a condition to be fulfilled to have a transition to another given state")),
				@facet (
						name = FsmTransitionStatement.TO,
						type = IType.ID,
						optional = false,
						doc = @doc ("the identifier of the next state")) },
		omissible = IKeyword.WHEN)
@validator (TransitionValidator.class)
@serializer (TransitionSerializer.class)
@doc (
		value = "In an FSM architecture, `" + FsmTransitionStatement.TRANSITION
				+ "` specifies the next state of the life cycle. The transition occurs when the condition is fulfilled. The embedded statements are executed when the transition is triggered.",
		usages = { @usage (
				value = "In the following example, the transition is executed when after 2 steps:",
				examples = { @example (
						value = "state s_init initial: true {",
						isExecutable = false),
						@example (
								value = "	write state;",
								isExecutable = false),
						@example (
								value = "	transition to: s1 when: (cycle > 2) {",
								isExecutable = false),
						@example (
								value = "		write \"transition s_init -> s1\";",
								isExecutable = false),
						@example (
								value = "	}",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }) },
		see = { FsmStateStatement.ENTER, FsmStateStatement.STATE, FsmStateStatement.EXIT })
public class FsmTransitionStatement extends AbstractStatementSequence {

	/** The Constant states. */
	static final List<String> states = Arrays.asList(FsmStateStatement.STATE, IKeyword.USER_PANEL);

	/**
	 * The Class TransitionSerializer.
	 */
	public static class TransitionSerializer implements ISymbolSerializer {

		/** The my facets. */
		static String[] MY_FACETS = { TO, WHEN };

		@Override
		public void serializeFacets(final IDescription s, final StringBuilder sb, final boolean includingBuiltIn) {
			for (final String key : MY_FACETS) {

				final String expr = serializeFacetValue(s, key, includingBuiltIn);
				if (expr != null) { sb.append(serializeFacetKey(s, key, includingBuiltIn)).append(expr).append(" "); }
			}

		}
	}

	/**
	 * The Class TransitionValidator.
	 */
	public static class TransitionValidator implements IDescriptionValidator<IDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription desc) {
			final IDescription sup = desc.getEnclosingDescription();
			final String keyword = sup.getKeyword();
			if (!states.contains(keyword)) {
				desc.error("Transitions cannot be declared inside  " + keyword, IGamlIssue.WRONG_PARENT);
				return;
			}
			final IExpression expr = sup.getFacetExpr(FsmStateStatement.FINAL);
			if (GAML.getExpressionFactory().getTrue().equals(expr)) {
				desc.error("Transitions are not accepted in final states", IGamlIssue.WRONG_PARENT);
				return;
			}
			final String behavior = desc.getLitteral(TO);
			final ISpeciesDescription sd = desc.getSpeciesContext();
			if (!sd.hasBehavior(behavior)) {
				desc.error("Behavior " + behavior + " does not exist in " + sd.getName(), IGamlIssue.UNKNOWN_BEHAVIOR,
						TO, behavior, sd.getName());
			}

		}

	}

	/** The when. */
	final IExpression when;

	/** Constant field TRANSITION. */
	public static final String TRANSITION = "transition";

	/** The Constant TO. */
	protected static final String TO = "to";

	/**
	 * Instantiates a new fsm transition statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public FsmTransitionStatement(final IDescription desc) {
		super(desc);
		final String stateName = getLiteral(TO);
		setName(stateName);
		if (getFacet(IKeyword.WHEN) != null) {
			when = getFacet(IKeyword.WHEN);
		} else {
			when = GAML.getExpressionFactory().getTrue();
		}
	}

	/**
	 * Evaluates true on.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public boolean evaluatesTrueOn(final IScope scope) throws GamaRuntimeException {
		return Cast.asBool(scope, when.value(scope));
		// Normally, the agent is still in the "currentState" scope.
	}

}

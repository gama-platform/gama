/*******************************************************************************************************
 *
 * FsmTransitionStatement.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.architecture.finite_state_machine;

import java.util.Arrays;
import java.util.List;

import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.architecture.finite_state_machine.FsmTransitionStatement.TransitionSerializer;
import gama.gaml.architecture.finite_state_machine.FsmTransitionStatement.TransitionValidator;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolSerializer;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IExpressionFactory;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatementSequence;
import gama.gaml.types.IType;

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
	public static class TransitionSerializer extends SymbolSerializer<SymbolDescription> {

		/** The my facets. */
		static String[] MY_FACETS = new String[] { TO, WHEN };

		@Override
		protected void serializeFacets(final SymbolDescription s, final StringBuilder sb,
				final boolean includingBuiltIn) {
			for (final String key : MY_FACETS) {

				final String expr = serializeFacetValue(s, key, includingBuiltIn);
				if (expr != null) {
					sb.append(serializeFacetKey(s, key, includingBuiltIn)).append(expr).append(" ");
				}
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
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
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
			if (IExpressionFactory.TRUE_EXPR.equals(expr)) {
				desc.error("Transitions are not accepted in final states", IGamlIssue.WRONG_PARENT);
				return;
			}
			final String behavior = desc.getLitteral(TO);
			final SpeciesDescription sd = desc.getSpeciesContext();
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
	 * @param desc the desc
	 */
	public FsmTransitionStatement(final IDescription desc) {
		super(desc);
		final String stateName = getLiteral(TO);
		setName(stateName);
		if (getFacet(IKeyword.WHEN) != null) {
			when = getFacet(IKeyword.WHEN);
		} else {
			when = IExpressionFactory.TRUE_EXPR;
		}
	}

	/**
	 * Evaluates true on.
	 *
	 * @param scope the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	public boolean evaluatesTrueOn(final IScope scope) throws GamaRuntimeException {
		return Cast.asBool(scope, when.value(scope));
		// Normally, the agent is still in the "currentState" scope.
	}

}

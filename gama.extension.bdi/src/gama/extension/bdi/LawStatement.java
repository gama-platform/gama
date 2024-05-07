/*******************************************************************************************************
 *
 * LawStatement.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.bdi;

import java.util.ArrayList;
import java.util.List;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.operators.System;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;

// Définition des lois pour créer des obligations sur le modèle des rêgles d'inférences avec en supplément un seuil
// d'obéissance

/**
 * The Class LawStatement.
 */
@symbol (
		name = LawStatement.LAW,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.BDI })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = LawStatement.BELIEF,
				type = PredicateType.id,
				optional = true,
				doc = @doc ("The mandatory belief")),
				@facet (
						name = LawStatement.BELIEFS,
						type = IType.LIST,
						of = PredicateType.id,
						optional = true,
						doc = @doc ("The mandatory beliefs")),
				@facet (
						name = LawStatement.NEW_OBLIGATION,
						type = PredicateType.id,
						optional = true,
						doc = @doc ("The predicate that will be added as an obligation")),
				@facet (
						name = LawStatement.NEW_OBLIGATIONS,
						type = IType.LIST,
						of = PredicateType.id,
						optional = true,
						doc = @doc ("The list of predicates that will be added as obligations")),
				@facet (
						name = IKeyword.WHEN,
						type = IType.BOOL,
						optional = true,
						doc = @doc (" ")),

				@facet (
						name = IKeyword.PARALLEL,
						type = { IType.BOOL, IType.INT },
						optional = true,
						doc = @doc ("setting this facet to 'true' will allow 'perceive' to use concurrency with a parallel_bdi architecture; setting it to an integer will set the threshold under which they will be run sequentially (the default is initially 20, but can be fixed in the preferences). This facet is true by default.")),
				@facet (
						name = LawStatement.STRENGTH,
						type = { IType.FLOAT, IType.INT },
						optional = true,
						doc = @doc ("The stregth of the mental state created")),
				@facet (
						name = LawStatement.LIFETIME,
						type = IType.INT,
						optional = true,
						doc = @doc ("the lifetime value of the mental state created")),
				@facet (
						name = RuleStatement.THRESHOLD,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("Threshold linked to the obedience value.")),
				@facet (
						name = LawStatement.ALL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("add an obligation for each belief")),
				@facet (
						name = IKeyword.NAME,
						type = IType.ID,
						optional = true,
						doc = @doc ("The name of the law")) },
		omissible = IKeyword.NAME)
@doc (
		value = "enables to add a desire or a belief or to remove a belief, a desire or an intention if the agent gets the belief or/and desire or/and condition mentioned.",
		examples = {
				@example ("rule belief: new_predicate(\"test\") when: flip(0.5) new_desire: new_predicate(\"test\");") })

public class LawStatement extends AbstractStatement {

	/** The Constant LAW. */
	public static final String LAW = "law";

	/** The Constant BELIEF. */
	public static final String BELIEF = "belief";

	/** The Constant BELIEFS. */
	public static final String BELIEFS = "beliefs";

	/** The Constant NEW_OBLIGATION. */
	public static final String NEW_OBLIGATION = "new_obligation";

	/** The Constant NEW_OBLIGATIONS. */
	public static final String NEW_OBLIGATIONS = "new_obligations";

	/** The Constant STRENGTH. */
	public static final String STRENGTH = "strength";

	/** The Constant LIFETIME. */
	public static final String LIFETIME = "lifetime";

	/** The Constant THRESHOLD. */
	public static final String THRESHOLD = "threshold";

	/** The Constant ALL. */
	public static final String ALL = "all";

	/** The when. */
	final IExpression when;

	/** The parallel. */
	final IExpression parallel;

	/** The belief. */
	final IExpression belief;

	/** The beliefs. */
	final IExpression beliefs;

	/** The new obligation. */
	final IExpression newObligation;

	/** The new obligations. */
	final IExpression newObligations;

	/** The strength. */
	final IExpression strength;

	/** The lifetime. */
	final IExpression lifetime;

	/** The threshold. */
	final IExpression threshold;

	/** The all. */
	final IExpression all;

	/**
	 * Gets the context expression.
	 *
	 * @return the context expression
	 */
	public IExpression getContextExpression() { return when; }

	/**
	 * Gets the belief expression.
	 *
	 * @return the belief expression
	 */
	public IExpression getBeliefExpression() { return belief; }

	/**
	 * Gets the obligation expression.
	 *
	 * @return the obligation expression
	 */
	public IExpression getObligationExpression() { return newObligation; }

	/**
	 * Gets the parallel.
	 *
	 * @return the parallel
	 */
	public IExpression getParallel() { return parallel; }

	/**
	 * Gets the threshold.
	 *
	 * @return the threshold
	 */
	public IExpression getThreshold() { return threshold; }

	/**
	 * Instantiates a new law statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public LawStatement(final IDescription desc) {
		super(desc);
		when = getFacet(IKeyword.WHEN);
		belief = getFacet(LawStatement.BELIEF);
		beliefs = getFacet(LawStatement.BELIEFS);
		newObligation = getFacet(LawStatement.NEW_OBLIGATION);
		newObligations = getFacet(LawStatement.NEW_OBLIGATIONS);
		strength = getFacet(LawStatement.STRENGTH);
		lifetime = getFacet("lifetime");
		threshold = getFacet(LawStatement.THRESHOLD);
		parallel = getFacet(IKeyword.PARALLEL);
		all = getFacet(IKeyword.ALL);
		setName(desc.getName());
	}

	@SuppressWarnings ("unchecked")
	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		
		if (newObligation == null && newObligations == null) {
			return null;
		}
		
		if (when != null && !Cast.asBool(scope, when.value(scope))) {
			return null;
		}
		
		List<Predicate> predBeliefList = new ArrayList<>();
		
		final MentalState tempBelief = new MentalState("Belief");
		if (belief != null) {
			tempBelief.setPredicate((Predicate) belief.value(scope));
			if (Utils.hasBelief(scope, tempBelief)) {
				for (final MentalState mental : Utils.getBase(scope, SimpleBdiArchitecture.BELIEF_BASE)) {
					if (tempBelief.getPredicate().equals(mental.getPredicate())) {
						predBeliefList.add(mental.getPredicate());
					}
				}
			}
		}

		if (	(belief == null || Utils.hasBelief(scope, tempBelief))
			&& 	(beliefs == null || hasBeliefs(scope, (List<Predicate>) beliefs.value(scope)))
			&&	(threshold == null || (double) scope.getAgent().getAttribute("obedience") >= (double) threshold.value(scope))) {
			
			if (newObligation != null) {								
				final Predicate newObl = (Predicate) newObligation.value(scope);
				if (all != null && Cast.asBool(scope, all.value(scope))) {
					for (Predicate p : predBeliefList) {
						addNewObligation(scope, newObl, (IMap<String, Object>) System.opCopy(scope, p.getValues()));
					}
				} else {
					addNewObligation(scope, newObl, null);
				}
			}
			if (newObligations != null) {
				final List<Predicate> newObls =	(List<Predicate>) newObligations.value(scope);
				for (final Predicate newObl : newObls) {
					addNewObligation(scope, newObl, null);
				}
			}
		}
		return null;
	}

	private void addNewObligation(IScope scope, Predicate initialObligation, IMap<String, Object> initialValues) {

		final MentalState tempNewObligation = new MentalState("Obligation", initialObligation);
		
		if (initialValues != null) {
			tempNewObligation.getPredicate().setValues(initialValues);			
		}
		
		if (strength != null) {
			tempNewObligation.setStrength(Cast.asFloat(scope, strength.value(scope)));
		}
		if (lifetime != null) {
			tempNewObligation.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
		}
		// Only do it if there's not already an obligation
		if (!Utils.hasObligation(scope, tempNewObligation)) {
			Utils.addObligation(scope, tempNewObligation);
			Utils.clearIntention(scope);
			final IAgent agent = scope.getAgent();
			agent.setAttribute(SimpleBdiArchitecture.CURRENT_PLAN, null);
			agent.setAttribute(SimpleBdiArchitecture.CURRENT_NORM, null);
		}
	}

	/**
	 * Checks for beliefs.
	 *
	 * @param scope
	 *            the scope
	 * @param predicates
	 *            the predicates
	 * @return true, if successful
	 */
	private boolean hasBeliefs(final IScope scope, final List<Predicate> predicates) {
		for (final Predicate p : predicates) {
			final MentalState temp = new MentalState("Belief", p);
			if (!Utils.hasBelief(scope, temp)) return false;
		}
		return true;
	}

}

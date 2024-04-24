/*******************************************************************************************************
 *
 * SimpleBdiArchitecture.java, in gama.extension.bdi, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.bdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.architecture.reflex.ReflexArchitecture;
import gama.gaml.compilation.ISymbol;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Maths;
import gama.gaml.operators.Random;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.IStatement;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class SimpleBdiArchitecture.
 */
@vars ({ @variable (
		name = SimpleBdiArchitecture.PERSISTENCE_COEFFICIENT_PLANS,
		type = IType.FLOAT,
		init = "1.0",
		doc = @doc ("plan persistence")),
		@variable (
				name = SimpleBdiArchitecture.PERSISTENCE_COEFFICIENT_INTENTIONS,
				type = IType.FLOAT,
				init = "1.0",
				doc = @doc ("intention persistence")),
		@variable (
				name = SimpleBdiArchitecture.PROBABILISTIC_CHOICE,
				type = IType.BOOL,
				init = "false",
				doc = @doc ("indicates if the choice is deterministic or probabilistic")),
		@variable (
				name = SimpleBdiArchitecture.USE_EMOTIONS_ARCHITECTURE,
				type = IType.BOOL,
				init = "false",
				doc = @doc ("indicates if emotions are automaticaly computed")),
		@variable (
				name = SimpleBdiArchitecture.USE_SOCIAL_ARCHITECTURE,
				type = IType.BOOL,
				init = "false",
				doc = @doc ("indicates if social relations are automaticaly computed")),
		@variable (
				name = SimpleBdiArchitecture.USE_PERSONALITY,
				type = IType.BOOL,
				init = "false",
				doc = @doc ("indicates if the personnality is used")),
		@variable (
				name = SimpleBdiArchitecture.USE_PERSISTENCE,
				type = IType.BOOL,
				init = "false",
				doc = @doc ("indicates if the persistence coefficient is computed with personality (false) or with the value given by the modeler")),
		@variable (
				name = SimpleBdiArchitecture.OBEDIENCE,
				type = IType.FLOAT,
				init = "1.0",
				doc = @doc ("an obedience value. By default, it is computed with personality")),
		@variable (
				name = SimpleBdiArchitecture.CHARISMA,
				type = IType.FLOAT,
				init = "1.0",
				doc = @doc ("a charisma value. By default, it is computed with personality")),
		@variable (
				name = SimpleBdiArchitecture.RECEPTIVITY,
				type = IType.FLOAT,
				init = "1.0",
				doc = @doc ("a receptivity value. By default, it is computed with personality")),
		@variable (
				name = SimpleBdiArchitecture.OPENNESS,
				type = IType.FLOAT,
				init = "0.5",
				doc = @doc ("an openness value for the personality")),
		@variable (
				name = SimpleBdiArchitecture.CONSCIENTIOUSNESS,
				type = IType.FLOAT,
				init = "0.5",
				doc = @doc ("a conscientiousness value for the personality")),
		@variable (
				name = SimpleBdiArchitecture.EXTRAVERSION,
				type = IType.FLOAT,
				init = "0.5",
				doc = @doc ("an extraversion value for the personality")),
		@variable (
				name = SimpleBdiArchitecture.AGREEABLENESS,
				type = IType.FLOAT,
				init = "0.5",
				doc = @doc ("an agreeableness value for the personality")),
		@variable (
				name = SimpleBdiArchitecture.NEUROTISM,
				type = IType.FLOAT,
				init = "0.5",
				doc = @doc ("a neurotism value for the personality")),
		@variable (
				name = SimpleBdiArchitecture.BELIEF_BASE,
				type = IType.LIST,
				of = MentalStateType.id,
				init = "[]",
				doc = @doc ("the belief base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.LAST_THOUGHTS,
				type = IType.LIST,
				init = "[]",
				doc = @doc ("the list of the last thoughts of the agent")),
		@variable (
				name = SimpleBdiArchitecture.INTENTION_BASE,
				type = IType.LIST,
				of = MentalStateType.id,
				init = "[]",
				doc = @doc ("the intention base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.EMOTION_BASE,
				type = IType.LIST,
				of = EmotionType.EMOTIONTYPE_ID,
				init = "[]",
				doc = @doc ("the emotion base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.DESIRE_BASE,
				type = IType.LIST,
				of = MentalStateType.id,
				init = "[]",
				doc = @doc ("the desire base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.OBLIGATION_BASE,
				type = IType.LIST,
				of = MentalStateType.id,
				init = "[]",
				doc = @doc ("the obligation base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.UNCERTAINTY_BASE,
				type = IType.LIST,
				of = MentalStateType.id,
				init = "[]",
				doc = @doc ("the uncertainty base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.IDEAL_BASE,
				type = IType.LIST,
				of = MentalStateType.id,
				init = "[]",
				doc = @doc ("the ideal base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.LAW_BASE,
				type = IType.LIST,
				of = IType.NONE,
				init = "[]",
				doc = @doc ("the law base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.PLAN_BASE,
				type = IType.LIST,
				of = BDIPlanType.TYPE_ID,
				init = "[]",
				doc = @doc ("the plan base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.NORM_BASE,
				type = IType.LIST,
				of = NormType.id,
				init = "[]",
				doc = @doc ("the norm base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.SANCTION_BASE,
				type = IType.LIST,
				of = SanctionType.id,
				init = "[]",
				doc = @doc ("the sanction base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.SOCIALLINK_BASE,
				type = IType.LIST,
				of = SocialLinkType.id,
				init = "[]",
				doc = @doc ("the social link base of the agent")),
		@variable (
				name = SimpleBdiArchitecture.CURRENT_PLAN,
				type = IType.NONE/* BDIPlanType.id */,
				doc = @doc ("thecurrent plan of the agent")),
		@variable (
				name = SimpleBdiArchitecture.CURRENT_NORM,
				type = IType.NONE/* NormType.id */,
				doc = @doc ("the current norm of the agent")) })
@skill (
		name = SimpleBdiArchitecture.SIMPLE_BDI,
		concept = { IConcept.BDI, IConcept.ARCHITECTURE })
@doc ("this architecture enables to define a behaviour using BDI. It is an implementation of the BEN architecture (Behaviour with Emotions and Norms)")
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class SimpleBdiArchitecture extends ReflexArchitecture {

	/** The Constant SIMPLE_BDI. */
	public static final String SIMPLE_BDI = "simple_bdi";
	
	/** The Constant PLAN. */
	public static final String PLAN = "plan";
	
	/** The Constant PRIORITY. */
	public static final String PRIORITY = "priority";
	
	/** The Constant FINISHEDWHEN. */
	public static final String FINISHEDWHEN = "finished_when";
	
	/** The Constant PERSISTENCE_COEFFICIENT_PLANS. */
	public static final String PERSISTENCE_COEFFICIENT_PLANS = "plan_persistence";
	
	/** The Constant PERSISTENCE_COEFFICIENT_INTENTIONS. */
	public static final String PERSISTENCE_COEFFICIENT_INTENTIONS = "intention_persistence";
	
	/** The Constant USE_EMOTIONS_ARCHITECTURE. */
	public static final String USE_EMOTIONS_ARCHITECTURE = "use_emotions_architecture";
	
	/** The Constant USE_SOCIAL_ARCHITECTURE. */
	public static final String USE_SOCIAL_ARCHITECTURE = "use_social_architecture";
	
	/** The Constant USE_PERSONALITY. */
	public static final String USE_PERSONALITY = "use_personality";
	
	/** The Constant USE_PERSISTENCE. */
	public static final String USE_PERSISTENCE = "use_persistence";
	
	
	/** The Constant OBEDIENCE. */
	public static final String OBEDIENCE = "obedience";
	
	/** The Constant CHARISMA. */
	public static final String CHARISMA = "charisma";
	
	/** The Constant RECEPTIVITY. */
	public static final String RECEPTIVITY = "receptivity";
	
	/** The Constant OPENNESS. */
	public static final String OPENNESS = "openness";
	
	/** The Constant CONSCIENTIOUSNESS. */
	public static final String CONSCIENTIOUSNESS = "conscientiousness";
	
	/** The Constant EXTRAVERSION. */
	public static final String EXTRAVERSION = "extroversion";
	
	/** The Constant AGREEABLENESS. */
	public static final String AGREEABLENESS = "agreeableness";
	
	/** The Constant NEUROTISM. */
	public static final String NEUROTISM = "neurotism";

	/** The Constant PROBABILISTIC_CHOICE. */
	public static final String PROBABILISTIC_CHOICE = "probabilistic_choice";
	
	/** The Constant INSTANTANEAOUS. */
	public static final String INSTANTANEOUS = "instantaneous";

	/** The Constant LAST_THOUGHTS. */
	// INFORMATION THAT CAN BE DISPLAYED
	public static final String LAST_THOUGHTS = "thinking";
	
	/** The Constant LAST_THOUGHTS_SIZE. */
	public static final Integer LAST_THOUGHTS_SIZE = 5;

	/** The Constant EMOTION. */
	public static final String EMOTION = "emotion";
	
	/** The Constant SOCIALLINK. */
	public static final String SOCIALLINK = "social_link";
	
	/** The Constant PREDICATE. */
	public static final String PREDICATE = "predicate";
	
	/** The Constant PREDICATE_NAME. */
	public static final String PREDICATE_NAME = "name";
	
	/** The Constant PREDICATE_VALUE. */
	public static final String PREDICATE_VALUE = "value";
	
	/** The Constant PREDICATE_PRIORITY. */
	public static final String PREDICATE_PRIORITY = "priority";
	
	/** The Constant PREDICATE_PARAMETERS. */
	public static final String PREDICATE_PARAMETERS = "parameters";
	
	/** The Constant PREDICATE_ONHOLD. */
	public static final String ON_HOLD_UNTIL = "on_hold_until";
	
	/** The Constant SIMPLE_BDI. */
	public static final String AGENT_CAUSE = "cause";

	/** The Constant AGREEABLENESS. */
	public static final String MENTAL_STATE = "mental_state";
	
	/** The Constant PREDICATE_TODO. */
	public static final String PREDICATE_TODO = "todo";
	
	/** The Constant PREDICATE_SUBINTENTIONS. */
	public static final String PREDICATE_SUBINTENTION = "subintention";
	
	/** The Constant PREDICATE_SUBINTENTIONS. */
	public static final String SUBINTENTIONS = "subintentions";

	/** The Constant PREDICATE_SUBINTENTIONS. */
	public static final String SUPERINTENTION = "super_intention";
	
	/** The Constant PREDICATE_DATE. */
	public static final String PREDICATE_DATE = "date";
	
	/** The Constant BELIEF_BASE. */
	public static final String BELIEF_BASE = "belief_base";
	
	/** The Constant IDEAL_BASE. */
	public static final String IDEAL_BASE = "ideal_base";
	
	/** The Constant REMOVE_DESIRE_AND_INTENTION. */
	public static final String REMOVE_DESIRE_AND_INTENTION = "desire_also";
	
	/** The Constant DESIRE_BASE. */
	public static final String DESIRE_BASE = "desire_base";
	
	/** The Constant OBLIGATION_BASE. */
	public static final String OBLIGATION_BASE = "obligation_base";
	
	/** The Constant INTENTION_BASE. */
	public static final String INTENTION_BASE = "intention_base";
	
	/** The Constant EMOTION_BASE. */
	public static final String EMOTION_BASE = "emotion_base";
	
	/** The Constant SOCIALLINK_BASE. */
	public static final String SOCIALLINK_BASE = "social_link_base";
	
	/** The Constant EVERY_VALUE. */
	public static final String EVERY_VALUE = "every_possible_value";
	
	/** The Constant PLAN_BASE. */
	public static final String PLAN_BASE = "plan_base";
	
	/** The Constant NORM_BASE. */
	public static final String NORM_BASE = "norm_base";
	
	/** The Constant SANCTION_BASE. */
	public static final String SANCTION_BASE = "sanction_base";
	
	/** The Constant CURRENT_PLAN. */
	public static final String CURRENT_PLAN = "current_plan";
	
	/** The Constant CURRENT_NORM. */
	public static final String CURRENT_NORM = "current_norm";
	
	/** The Constant UNCERTAINTY_BASE. */
	public static final String UNCERTAINTY_BASE = "uncertainty_base";
	
	/** The Constant LAW_BASE. */
	public static final String LAW_BASE = "law_base";

	public static final String OWNER = "owner";
	

	// WARNING
	// AD: These values depend on the scope (i.e. the agent)
	// An architecture should be stateless and stock the scope dependent values
	// in the agent(s).
	/** The plans. */
	protected final List<BDIPlan> _plans = new ArrayList<>();
	
	/** The perceptions. */
	protected final List<PerceiveStatement> _perceptions = new ArrayList<>();
	
	/** The rules. */
	protected final List<RuleStatement> _rules = new ArrayList<>();
	
	/** The coping. */
	protected final List<CopingStatement> _coping = new ArrayList<>();
	
	/** The laws. */
	protected final List<LawStatement> _laws = new ArrayList<>();
	
	/** The norms. */
	protected final List<Norm> _norms = new ArrayList<>();
	
	/** The sanctions. */
	protected final List<Sanction> _sanctions = new ArrayList<>();
	
	/** The plans number. */
	protected int _plansNumber = 0;
	
	/** The perception number. */
	protected int _perceptionNumber = 0;
	
	/** The iscurrentplaninstantaneous. */
	protected boolean iscurrentplaninstantaneous = false;
	
	/** The laws number. */
	protected int _lawsNumber = 0;
	
	/** The rules number. */
	protected int _rulesNumber = 0;
	
	/** The coping number. */
	protected int _copingNumber = 0;
	
	/** The norm number. */
	protected int _normNumber = 0;
	
	/** The sanction number. */
	protected int _sanctionNumber = 0;

	@Override
	protected void clearBehaviors() {
		super.clearBehaviors();
		_plans.clear();
		_rules.clear();
		_coping.clear();
		_perceptions.clear();
		_laws.clear();
		_norms.clear();
		_sanctions.clear();
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		clearBehaviors();
		for (final ISymbol c : children) {
			addBehavior((IStatement) c);
		}
	}

	@Override
	public void addBehavior(final IStatement c) {
		if (c instanceof SimpleBdiPlanStatement) {
			// final String statementKeyword = c.getDescription().getKeyword();
			_plans.add(new BDIPlan((SimpleBdiPlanStatement) c));
			_plansNumber++;
		} else if (c instanceof PerceiveStatement) {
			// final String statementKeyword = c.getDescription().getKeyword();
			_perceptions.add((PerceiveStatement) c);
			_perceptionNumber++;
		} else if (c instanceof RuleStatement) {
			// final String statementKeyword = c.getDescription().getKeyword();
			_rules.add((RuleStatement) c);
			_rulesNumber++;
		} else if (c instanceof CopingStatement) {
			// final String statementKeyword = c.getDescription().getKeyword();
			_coping.add((CopingStatement) c);
			_copingNumber++;
		} else if (c instanceof LawStatement) {
			// final String statementKeyword = c.getDescription().getKeyword();
			_laws.add((LawStatement) c);
			_lawsNumber++;
		} else if (c instanceof NormStatement) {
			// final String statementKeyword = c.getDescription().getKeyword();
			_norms.add(new Norm((NormStatement) c));
			_normNumber++;
		} else if (c instanceof SanctionStatement) {
			// final String statementKeyword = c.getDescription().getKeyword();
			_sanctions.add(new Sanction((SanctionStatement) c));
			_sanctionNumber++;
		} else {
			super.addBehavior(c);
		}
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		super.executeOn(scope);
		final IAgent agent = scope.getAgent();
		if (agent.dead()) { return null; }
		final Boolean use_personality = scope.hasArg(USE_PERSONALITY) ? scope.getBoolArg(USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY);
		if (use_personality) {
			final Double expressivity = (Double) scope.getAgent().getAttribute(EXTRAVERSION);
			final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
			final Double conscience = (Double) scope.getAgent().getAttribute(CONSCIENTIOUSNESS);
			final Double agreeableness = (Double) scope.getAgent().getAttribute(AGREEABLENESS);
			scope.getAgent().setAttribute(CHARISMA, expressivity);
			scope.getAgent().setAttribute(RECEPTIVITY, 1 - neurotisme);
			scope.getAgent().setAttribute(PERSISTENCE_COEFFICIENT_PLANS, Maths.sqrt(scope, conscience));
			scope.getAgent().setAttribute(PERSISTENCE_COEFFICIENT_INTENTIONS, Maths.sqrt(scope, conscience));
			scope.getAgent().setAttribute(OBEDIENCE, Maths.sqrt(scope, (conscience + agreeableness) * 0.5));
		}
		if (_sanctionNumber > 0) {
			scope.getAgent().setAttribute(SANCTION_BASE, _sanctions);
		}
		if (_perceptionNumber > 0) {
			for (int i = 0; i < _perceptionNumber; i++) {
				_perceptions.get(i).executeOn(scope);
				if (agent.dead()) { return null; }
			}
		}
		if (_rulesNumber > 0) {
			for (int i = 0; i < _rulesNumber; i++) {
				_rules.get(i).executeOn(scope);
				if (agent.dead()) { return null; }
			}
		}
		// cleanObligation(scope);
		if (_lawsNumber > 0) {
			for (int i = 0; i < _lawsNumber; i++) {
				_laws.get(i).executeOn(scope);
				if (agent.dead()) { return null; }
			}
		}
		// computeEmotions(scope);
		updateSocialLinks(scope);
		if (_copingNumber > 0) {
			for (int i = 0; i < _copingNumber; i++) {
				_coping.get(i).executeOn(scope);
				if (agent.dead()) { return null; }
			}
		}
		final Object result = executePlans(scope);
		if (!scope.getAgent().dead()) {
			// Activer la violation des normes
			updateNormViolation(scope);
			// Mettre à jour le temps de vie des normes
			updateNormLifetime(scope);

			// if (!agent.dead()) {
			// Part that manage the lifetime of predicates
			// if (result != null) {
			updateLifeTimePredicates(scope);
			updateEmotionsIntensity(scope);
			// }
		}
		return result;
	}

	/**
	 * Execute plans.
	 *
	 * @param scope the scope
	 * @return the object
	 */
	protected final Object executePlans(final IScope scope) {
		Object result = null;
		if (_plansNumber <= 0 && _normNumber <= 0) {
			return result;	
		}
		boolean loop_instantaneous_plans = true;
		while (loop_instantaneous_plans) {
			loop_instantaneous_plans = false;
			final IAgent agent = getCurrentAgent(scope);
			if (agent.dead()) { return null; }
			agent.setAttribute(LAW_BASE, _laws);
			agent.setAttribute(PLAN_BASE, _plans);
			agent.setAttribute(NORM_BASE, _norms);
			agent.setAttribute(SANCTION_BASE, _sanctions);
			final Boolean usingPersistence = (Boolean) agent.getAttribute(USE_PERSISTENCE);
			final IList<MentalState> intentionBase = scope.hasArg(INTENTION_BASE) ? scope.getListArg(INTENTION_BASE)
					: (IList<MentalState>) agent.getAttribute(INTENTION_BASE);
			Double persistenceCoefficientPlans = 1.0;
			Double persistenceCoefficientintention = 1.0;
			if (usingPersistence) {
				persistenceCoefficientPlans = scope.hasArg(PERSISTENCE_COEFFICIENT_PLANS)
						? scope.getFloatArg(PERSISTENCE_COEFFICIENT_PLANS)
						: (Double) agent.getAttribute(PERSISTENCE_COEFFICIENT_PLANS);
				persistenceCoefficientintention = scope.hasArg(PERSISTENCE_COEFFICIENT_INTENTIONS)
						? scope.getFloatArg(PERSISTENCE_COEFFICIENT_INTENTIONS)
						: (Double) agent.getAttribute(PERSISTENCE_COEFFICIENT_INTENTIONS);

			}
			BDIPlan _persistentTask = (BDIPlan) agent.getAttribute(CURRENT_PLAN);
			Norm _persistentNorm = (Norm) agent.getAttribute(CURRENT_NORM);
			// RANDOMLY REMOVE (last)INTENTION
			Boolean flipResultintention = gama.gaml.operators.Random.opFlip(scope, persistenceCoefficientintention);
			while (!flipResultintention && intentionBase.size() > 0) {
				flipResultintention = gama.gaml.operators.Random.opFlip(scope, persistenceCoefficientintention);
				if (intentionBase.size() > 0) {
					final int toremove = intentionBase.size() - 1;
					final Predicate previousint = intentionBase.get(toremove).getPredicate();
					intentionBase.remove(toremove);
					final String think = "check what happens if I remove: " + previousint;
					addThoughts(scope, think);
					_persistentTask = null;
					agent.setAttribute(CURRENT_PLAN, _persistentTask);
					_persistentNorm = null;
					agent.setAttribute(CURRENT_NORM, _persistentNorm);
				}
			}
			// If current intention has no plan/norm or is on hold, choose a new
			// Desire/Obligation
			MentalState intentionTemp;
			if (currentIntention(scope) != null) {
				intentionTemp = currentIntention(scope);
			} else {
				intentionTemp = new MentalState("Intention", currentIntention(scope));
			}
			if (testOnHold(scope, intentionTemp) || currentIntention(scope) == null
					|| currentIntention(scope).getPredicate() == null
					|| listExecutablePlans(scope).isEmpty() && listExecutableNorms(scope).isEmpty()) {
				if (!selectObligationWithHighestPriority(scope)) {
					selectDesireWithHighestPriority(scope);
				}
				_persistentTask = null;
				agent.setAttribute(CURRENT_PLAN, _persistentTask);
				_persistentNorm = null;
				agent.setAttribute(CURRENT_NORM, _persistentNorm);
			}

			_persistentTask = (BDIPlan) agent.getAttribute(CURRENT_PLAN);
			_persistentNorm = (Norm) agent.getAttribute(CURRENT_NORM);
			final Boolean flipResult = gama.gaml.operators.Random.opFlip(scope, persistenceCoefficientPlans);

			if (!flipResult) {
				if (_persistentTask != null) {
					addThoughts(scope, "check what happens if I stop: " + _persistentTask.getName());
				}
				_persistentTask = selectExecutablePlanWithHighestPriority(scope);
				agent.setAttribute(CURRENT_PLAN, _persistentTask);

				if (_persistentTask != null) {
					addThoughts(scope, "lets do instead " + _persistentTask.getName());
				}

			}
			if (currentIntention(scope) == null) {
				addThoughts(scope, "I want nothing...");
				// update the lifetime of beliefs
				// updateLifeTimePredicates(scope);
				// updateEmotionsIntensity(scope);
				return null;
			}
			// check and choose a norm to apply to the current intention
			if (_persistentNorm == null && currentIntention(scope) != null
					&& currentIntention(scope).getPredicate() == null) {
				if (!selectObligationWithHighestPriority(scope)) {
					selectDesireWithHighestPriority(scope);
				}
				if (currentIntention(scope) != null && currentIntention(scope).getPredicate() == null) {
					addThoughts(scope, "I want nothing...");
					// update the lifetime of beliefs
					// updateLifeTimePredicates(scope);
					// updateEmotionsIntensity(scope);
					return null;
				}
				_persistentNorm = selectExecutableNormWithHighestPriority(scope);
				agent.setAttribute(CURRENT_NORM, _persistentNorm);
				if (currentIntention(scope) != null && _persistentTask != null) {
					addThoughts(scope, "ok, new intention: " + currentIntention(scope).getPredicate()
							+ " with norm " + _persistentNorm.getName());
				}
			}
			// choose a plan for the current intention
			if (_persistentNorm == null && _persistentTask == null && currentIntention(scope) != null
					&& currentIntention(scope).getPredicate() == null) {
				selectDesireWithHighestPriority(scope);
				if (currentIntention(scope) != null && currentIntention(scope).getPredicate() == null) {
					addThoughts(scope, "I want nothing...");
					// update the lifetime of beliefs
					// updateLifeTimePredicates(scope);
					// updateEmotionsIntensity(scope);
					return null;
				}
				_persistentTask = selectExecutablePlanWithHighestPriority(scope);
				agent.setAttribute(CURRENT_PLAN, _persistentTask);
				if (currentIntention(scope) != null && _persistentTask != null) {
					addThoughts(scope, "ok, new intention: " + currentIntention(scope).getPredicate()
							+ " with plan " + _persistentTask.getName());
				}
			}
			if (currentIntention(scope) != null && _persistentTask == null
					&& currentIntention(scope).getPredicate() != null) {
				_persistentNorm = selectExecutableNormWithHighestPriority(scope);
				agent.setAttribute(CURRENT_NORM, _persistentNorm);
				if (_persistentNorm == null) {
					_persistentTask = selectExecutablePlanWithHighestPriority(scope);
					agent.setAttribute(CURRENT_PLAN, _persistentTask);
				} else {
					agent.setAttribute(CURRENT_PLAN, _persistentTask);
				}
				if (_persistentNorm != null) {
					addThoughts(scope, "use norm : " + _persistentNorm.getName());
				}
				if (_persistentTask != null) {
					addThoughts(scope, "use plan : " + _persistentTask.getName());
				}
			}
			if (_persistentNorm != null) {
				if (!agent.dead()) {
					result = _persistentNorm.getNormStatement().executeOn(scope);
					boolean isExecuted = false;
					if (_persistentNorm.getNormStatement().getExecutedExpression() != null) {
						isExecuted = Cast.asBool(scope,
								_persistentNorm.getNormStatement().getExecutedExpression().value(scope));
					}
					if (this.iscurrentplaninstantaneous) {
						loop_instantaneous_plans = true;
					}
					if (isExecuted) {
						_persistentNorm = null;
						agent.setAttribute(CURRENT_NORM, _persistentNorm);

					}
				}
			}
			if (_persistentTask != null) {
				if (!agent.dead()) {
					result = _persistentTask.getPlanStatement().executeOn(scope);
					boolean isExecuted = false;
					if (_persistentTask.getPlanStatement().getExecutedExpression() != null) {
						isExecuted = Cast.asBool(scope,
								_persistentTask.getPlanStatement().getExecutedExpression().value(scope));
					}
					if (this.iscurrentplaninstantaneous) {
						loop_instantaneous_plans = true;
					}
					if (isExecuted) {
						_persistentTask = null;
						agent.setAttribute(CURRENT_PLAN, _persistentTask);

					}
				}
			}
		}
		return result;	

	}

	

	/**
	 * Select desire with highest priority.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	protected final boolean selectMentalStateWithHighestPriority(final IScope scope, final String base, boolean testListPlans, Function<NormStatement, Predicate> getExpression) {
		final IAgent agent = getCurrentAgent(scope);
		final Boolean is_probabilistic_choice = scope.getBoolArgIfExists(PROBABILISTIC_CHOICE, (Boolean) agent.getAttribute(PROBABILISTIC_CHOICE));
		
		final List<BDIPlan> listPlans = getPlans(scope) == null ? Collections.emptyList() : getPlans(scope);
		final List<Norm> listNorm = getNorms(scope);

		if (listNorm == null) {
			return false;
		}
		
		final List<MentalState> listMentalStatesTest = GamaListFactory.create();
		final IList<MentalState> mentalStateBase = getBase(scope, base);
		final IList<MentalState> intentionBase = getBase(scope, INTENTION_BASE);

		if (is_probabilistic_choice) {
			
			if (mentalStateBase.isEmpty()) {
				return false;
			}

			for (final MentalState tempMentalState : mentalStateBase) {
				final Predicate predicate = tempMentalState.getPredicate();
				//TODO: this code looks a lot like addMentalStatesWithMatchingIntentions but a bit less strict, probably an error somewhere
				//TODO: This code adds tempMentalState as many times as there are matching plans and norms, which seems weird but that's how it was written initialy
				if (testListPlans) {
					listPlans.stream().filter(p -> ((Predicate)p.getPlanStatement().getIntentionExpression().value(scope)).equalsIntentionPlan(predicate))
							.forEach(i -> listMentalStatesTest.add(tempMentalState)); 
				}
				listNorm.stream().filter(p -> getExpression.apply(p.getNormStatement()).equalsIntentionPlan(predicate)) 
						.forEach(i -> listMentalStatesTest.add(tempMentalState));					
			}
			MentalState newIntention = mentalStateBase.getFirst();
			double newIntStrength;
			
			final double priority_list[] = listMentalStatesTest.stream().mapToDouble(s -> s.getStrength()).toArray();

			final IList priorities = GamaListFactory.create(scope, Types.FLOAT, priority_list);
			final int index_choice = Random.opRndChoice(scope, priorities);
			newIntention = listMentalStatesTest.get(index_choice);
			newIntStrength = listMentalStatesTest.get(index_choice).getStrength();
			if (listMentalStatesTest.size() > intentionBase.size()) {
				while (intentionBase.contains(newIntention)) {
					final int index_choice2 = Random.opRndChoice(scope, priorities);
					newIntention = listMentalStatesTest.get(index_choice2);
					newIntStrength = listMentalStatesTest.get(index_choice2).getStrength();
				}
			}
			return addInMentalStateBase(scope, newIntention, newIntStrength, base);
		} else {

			scope.getRandom().shuffleInPlace(mentalStateBase);//TODO:why ?
			addMentalStatesWithMatchingIntentions(scope, mentalStateBase, listMentalStatesTest, testListPlans);

			double maxpriority = Double.MIN_VALUE;
			if (listMentalStatesTest.isEmpty() || intentionBase == null) {
				return false;
			}

			MentalState newIntention = null;
			for (final MentalState state : listMentalStatesTest) {

				if (state.getStrength() > maxpriority && !intentionBase.contains(state)) {
					maxpriority = state.getStrength();
					newIntention = state;
				}
			}
			if (newIntention == null) {
				return false;
			}
			
			return addInMentalStateBase(scope, newIntention, maxpriority, base);
		}

	}

	
	private void addMentalStatesWithMatchingIntentions(final IScope scope, final IList<MentalState> mentalStateBase, List<MentalState> listMentalStatesTest, boolean testListPlans) {
		
		final List<BDIPlan> listPlans = getPlans(scope) == null ? Collections.emptyList() : getPlans(scope);
		final List<Norm> listNorm = getNorms(scope);
		for (final MentalState tempMentalState : mentalStateBase) {
			//TODO: this is just rewriting the old method but it feels like this should be refactored and it was probably partially wrong
			//TODO: could an item in the lists really be null ? probably useless conditions
			//TODO: This code adds tempMentalState as many times as there are matching plans and norms, which seems weird but that's how it was written initialy
			if (testListPlans) {
				listPlans.stream().filter(p -> p != null && p.getPlanStatement() != null)
								.map(p -> p.getPlanStatement().getIntentionExpression())
						.filter(i -> i == null || i.value(scope) == null || ((Predicate)i.value(scope)).equalsIntentionPlan(tempMentalState.getPredicate()))
						.forEach(i -> listMentalStatesTest.add(tempMentalState));		
			}
			listNorm.stream().filter(n -> n != null && n.getNormStatement() != null)
				.map(n -> n.getNormStatement())
				.filter(s ->  	s.getIntentionExpression() == null || s.getIntentionExpression().value(scope) == null
							|| (s.getObligationExpression() != null && ((Predicate)s.getObligationExpression().value(scope)).equalsIntentionPlan(tempMentalState)))
				.forEach(s -> listMentalStatesTest.add(tempMentalState));						
		}
	}

	private boolean addInMentalStateBase(final IScope scope, MentalState newIntention, double maxpriority, String base) {
		MentalState newIntentionState = null;
		var intentionBase = getBase(scope, INTENTION_BASE);
		var mentalStateBase = getBase(scope, base);
		final Predicate predicate = newIntention.getPredicate();
		int lifetime = newIntention.getLifeTime();
		if (predicate != null) {
			newIntentionState = new MentalState("Intention", predicate, maxpriority, lifetime, scope.getAgent());
		}
		if (newIntention.getMentalState() != null) {
			newIntentionState = new MentalState("Intention", newIntention.getMentalState(), maxpriority, lifetime, scope.getAgent());
		}
		if (predicate != null) {
			if (predicate.getSubintentions() == null && !intentionBase.contains(newIntentionState)) {
				intentionBase.addValue(scope, newIntentionState);
				return true;
			} else {
				//add into the base all the mentalstates that were subintentions of the predicate but not in the base yet
				predicate.getSubintentions().stream().filter(s -> !mentalStateBase.contains(s)).forEach(i -> mentalStateBase.addValue(scope, i));

				predicate.setOnHoldUntil(predicate.getSubintentions());
				if (!intentionBase.contains(newIntentionState)) {
					intentionBase.addValue(scope, newIntentionState);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Select desire with highest priority.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	protected final boolean selectDesireWithHighestPriority(final IScope scope) {
		// Réduire la liste des désires potentiellement intentionable en fonction des valeurs des plans
		return selectMentalStateWithHighestPriority(scope, DESIRE_BASE, true, n -> (Predicate) n.getIntentionExpression().value(scope));
	}
	/**
	 * Select obligation with highest priority.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	protected final boolean selectObligationWithHighestPriority(final IScope scope) {
		return selectMentalStateWithHighestPriority(scope, DESIRE_BASE, false, n -> (Predicate) n.getObligationExpression().value(scope));
	}
	/**
	 * Select executable plan with highest priority.
	 *
	 * @param scope the scope
	 * @return the BDI plan
	 */
	// Faire la même chose pour choisir la norme à appliquer, en l'appelant avant.
	protected final BDIPlan selectExecutablePlanWithHighestPriority(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		final Boolean is_probabilistic_choice = scope.hasArg(PROBABILISTIC_CHOICE)
				? scope.getBoolArg(PROBABILISTIC_CHOICE) : (Boolean) agent.getAttribute(PROBABILISTIC_CHOICE);

		BDIPlan resultStatement = null;

		double highestPriority = Double.MIN_VALUE;
		final List<BDIPlan> temp_plan = new ArrayList<>();
		final IList priorities = GamaListFactory.create(Types.FLOAT);
		final List<BDIPlan> plansCopy = new ArrayList(_plans);
		scope.getRandom().shuffleInPlace(plansCopy);
		for (final BDIPlan BDIPlanstatement : plansCopy) {
			final SimpleBdiPlanStatement statement = BDIPlanstatement.getPlanStatement();
			final boolean isContextConditionSatisfied = statement.getContextExpression() == null
					|| Cast.asBool(scope, statement.getContextExpression().value(scope));
			final boolean isIntentionConditionSatisfied = statement.getIntentionExpression() == null
					|| statement.getIntentionExpression().value(scope) == null
					|| ((Predicate) statement.getIntentionExpression().value(scope))
							.equalsIntentionPlan(currentIntention(scope).getPredicate());
			final boolean isEmotionConditionSatisfied = statement.getEmotionExpression() == null
					|| getEmotionBase(scope, EMOTION_BASE).contains(statement.getEmotionExpression().value(scope));
			final boolean thresholdSatisfied = statement.getThreshold() == null
					|| statement.getEmotionExpression() != null && SimpleBdiArchitecture.getEmotion(scope,
							(Emotion) statement.getEmotionExpression().value(scope)).intensity >= (double) statement
									.getThreshold().value(scope);
			if (isContextConditionSatisfied && isIntentionConditionSatisfied && isEmotionConditionSatisfied
					&& thresholdSatisfied) {
				if (is_probabilistic_choice) {
					temp_plan.add(BDIPlanstatement);
				} else {
					double currentPriority = 1.0;
					if (statement.getFacet(SimpleBdiArchitecture.PRIORITY) != null) {
						currentPriority =
								Cast.asFloat(scope, statement.getPriorityExpression().value(scope));
					}

					if (highestPriority < currentPriority) {
						highestPriority = currentPriority;
						resultStatement = BDIPlanstatement;
					}
				}
			}
		}
		if (is_probabilistic_choice && !temp_plan.isEmpty()) {
			for (final BDIPlan statement : temp_plan) {
				if (statement.getPlanStatement().hasFacet(PRIORITY)) {
					priorities.add(Cast.asFloat(scope, statement.getPlanStatement().getPriorityExpression().value(scope)));
				} else {
					priorities.add(1.0);
				}
			}
			final int index_plan = gama.gaml.operators.Random.opRndChoice(scope, priorities);
			resultStatement = temp_plan.get(index_plan);
		}

		iscurrentplaninstantaneous = false;
		if (resultStatement != null && resultStatement.getPlanStatement().getFacet(SimpleBdiArchitecture.INSTANTANEOUS) != null) {
			iscurrentplaninstantaneous = Cast.asBool(scope,resultStatement.getPlanStatement().getInstantaneousExpression().value(scope));
		}

		return resultStatement;
	}

	/**
	 * Select executable norm with highest priority.
	 *
	 * @param scope the scope
	 * @return the norm
	 */
	protected final Norm selectExecutableNormWithHighestPriority(final IScope scope) {
		// Doit sélectionner une norme sociale ou une norme obligatoire
		final IAgent agent = getCurrentAgent(scope);
		final Double obedienceValue = (Double) scope.getAgent().getAttribute("obedience");
		final Boolean is_probabilistic_choice = scope.getTypedArgIfExists(PROBABILISTIC_CHOICE, IType.BOOL, (Boolean)agent.getAttribute(PROBABILISTIC_CHOICE));

		Norm resultStatement = null;

		double highestPriority = Double.MIN_VALUE;
		final List<Norm> temp_norm = new ArrayList<>();
		final IList priorities = GamaListFactory.create(Types.FLOAT);
		for (final Norm tempNorm : getNorms(scope)){
			tempNorm.setSanctioned(false);
		}
		final List<Norm> normsCopy = new ArrayList(_norms);
		scope.getRandom().shuffleInPlace(normsCopy);
		for (final Norm norm : normsCopy) {
			final NormStatement statement = norm.getNormStatement();
			final boolean isContextConditionSatisfied = statement.getContextExpression() == null
					|| Cast.asBool(scope, statement.getContextExpression().value(scope));
			boolean isIntentionConditionSatisfied = false;
			if (statement.getIntentionExpression() != null && statement.getIntentionExpression().value(scope) != null) {
				isIntentionConditionSatisfied = ((Predicate) statement.getIntentionExpression().value(scope))
						.equalsIntentionPlan(currentIntention(scope).getPredicate());
			}
			boolean isObligationConditionSatisfied = false;
			if (	statement.getObligationExpression() != null && statement.getObligationExpression().value(scope) != null
				&& 	hasObligation(scope, new MentalState("Obligation", (Predicate) statement.getObligationExpression().value(scope)))) {
				isObligationConditionSatisfied = ((Predicate) statement.getObligationExpression().value(scope))
						.equalsIntentionPlan(currentIntention(scope).getPredicate());
			}
			final boolean thresholdSatisfied = statement.getThreshold() == null
					|| obedienceValue >= (Double) statement.getThreshold().value(scope);

			if (isContextConditionSatisfied && isObligationConditionSatisfied && thresholdSatisfied) {
				if (is_probabilistic_choice) {
					temp_norm.add((Norm) norm);
				} else {
					double currentPriority = 1.0;
					if (statement.getFacet(SimpleBdiArchitecture.PRIORITY) != null) {
						currentPriority = Cast.asFloat(scope, statement.getPriorityExpression().value(scope));
					}

					if (highestPriority < currentPriority) {
						highestPriority = currentPriority;
						resultStatement = norm;
					}
				}
			}

			if (isContextConditionSatisfied && isIntentionConditionSatisfied && thresholdSatisfied) {
				if (is_probabilistic_choice) {
					temp_norm.add(norm);
				} else {
					double currentPriority = 1.0;
					if (statement.getFacet(SimpleBdiArchitecture.PRIORITY) != null) {
						currentPriority = Cast.asFloat(scope, statement.getPriorityExpression().value(scope));
					}

					if (highestPriority < currentPriority) {
						highestPriority = currentPriority;
						resultStatement = norm;
					}
				}
			}
		}
		if (is_probabilistic_choice && !temp_norm.isEmpty()) {
			for (final Norm statement : temp_norm) {
				if (statement.getNormStatement().hasFacet(PRIORITY)) {
					priorities.add(Cast.asFloat(scope, statement.getNormStatement().getPriorityExpression().value(scope)));
				} else {
					priorities.add(1.0);
				}
			}
			final int index_plan = Random.opRndChoice(scope, priorities);
			resultStatement = temp_norm.get(index_plan);
		}

		iscurrentplaninstantaneous = false;
		if (resultStatement != null && resultStatement.getNormStatement().getFacet(SimpleBdiArchitecture.INSTANTANEOUS) != null) {
			iscurrentplaninstantaneous = Cast.asBool(scope,resultStatement.getNormStatement().getInstantaneousExpression().value(scope));
		}

		return resultStatement;
	}

	/**
	 * Update life time predicates.
	 *
	 * @param scope the scope
	 */
	protected void updateLifeTimePredicates(final IScope scope) {
		for (final MentalState mental : getBase(scope, BELIEF_BASE)) {
			mental.isUpdated = false;
		}
		for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
			mental.isUpdated = false;
		}
		for (final MentalState mental : getBase(scope, INTENTION_BASE)) {
			mental.isUpdated = false;
		}
		for (final MentalState mental : getBase(scope, UNCERTAINTY_BASE)) {
			mental.isUpdated = false;
		}
		for (final MentalState mental : getBase(scope, OBLIGATION_BASE)) {
			mental.isUpdated = false;
		}
		for (final MentalState mental : getBase(scope, BELIEF_BASE)) {
			mental.updateLifetime();
		}
		for (final MentalState mental : listBeliefsLifeTimeNull(scope)) {
			removeBelief(scope, mental);
		}
		for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
			mental.updateLifetime();
		}
		for (final MentalState mental : listDesiresLifeTimeNull(scope)) {
			removeDesire(scope, mental);
		}
		for (final MentalState mental : getBase(scope, INTENTION_BASE)) {
			mental.updateLifetime();
		}
		for (final MentalState mental : listIntentionsLifeTimeNull(scope)) {
			removeIntention(scope, mental);
		}
		for (final MentalState mental : getBase(scope, UNCERTAINTY_BASE)) {
			mental.updateLifetime();
		}
		for (final MentalState mental : listUncertaintyLifeTimeNull(scope)) {
			removeUncertainty(scope, mental);
		}
		for (final MentalState mental : getBase(scope, OBLIGATION_BASE)) {
			mental.updateLifetime();
		}
		for (final MentalState mental : listObligationLifeTimeNull(scope)) {
			removeObligation(scope, mental);
		}
	}

	/**
	 * List beliefs life time null.
	 *
	 * @param scope the scope
	 * @return the list
	 */
	private List<MentalState> listBeliefsLifeTimeNull(final IScope scope) {
		final List<MentalState> tempPred = new ArrayList<>();
		for (final MentalState mental : getBase(scope, BELIEF_BASE)) {
			if (mental.getLifeTime() == 0) {
				tempPred.add(mental);
			}
		}
		return tempPred;
	}

	/**
	 * List desires life time null.
	 *
	 * @param scope the scope
	 * @return the list
	 */
	private List<MentalState> listDesiresLifeTimeNull(final IScope scope) {
		final List<MentalState> tempPred = new ArrayList<>();
		for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
			if (mental.getLifeTime() == 0) {
				tempPred.add(mental);
			}
		}
		return tempPred;
	}

	/**
	 * List intentions life time null.
	 *
	 * @param scope the scope
	 * @return the list
	 */
	private List<MentalState> listIntentionsLifeTimeNull(final IScope scope) {
		final List<MentalState> tempPred = new ArrayList<>();
		for (final MentalState mental : getBase(scope, INTENTION_BASE)) {
			if (mental.getLifeTime() == 0) {
				tempPred.add(mental);
			}
		}
		return tempPred;
	}

	/**
	 * List uncertainty life time null.
	 *
	 * @param scope the scope
	 * @return the list
	 */
	private List<MentalState> listUncertaintyLifeTimeNull(final IScope scope) {
		final List<MentalState> tempPred = new ArrayList<>();
		for (final MentalState mental : getBase(scope, UNCERTAINTY_BASE)) {
			if (mental.getLifeTime() == 0) {
				tempPred.add(mental);
			}
		}
		return tempPred;
	}

	/**
	 * List obligation life time null.
	 *
	 * @param scope the scope
	 * @return the list
	 */
	private List<MentalState> listObligationLifeTimeNull(final IScope scope) {
		final List<MentalState> tempPred = new ArrayList<>();
		for (final MentalState mental : getBase(scope, OBLIGATION_BASE)) {
			if (mental.getLifeTime() == 0) {
				tempPred.add(mental);
			}
		}
		return tempPred;
	}

	/**
	 * List executable plans.
	 *
	 * @param scope the scope
	 * @return the list
	 */
	protected final List<SimpleBdiPlanStatement> listExecutablePlans(final IScope scope) {
		final List<SimpleBdiPlanStatement> plans = new ArrayList<>();
		final List<BDIPlan> plansCopy = new ArrayList(_plans);
		scope.getRandom().shuffleInPlace(plansCopy);
		for (final BDIPlan BDIPlanstatement : plansCopy) {
			final SimpleBdiPlanStatement statement = BDIPlanstatement.getPlanStatement();

			if (statement.getContextExpression() != null
					&& !Cast.asBool(scope, statement.getContextExpression().value(scope))) {
				continue;
			}
			if (currentIntention(scope) != null) {
				if (statement.getIntentionExpression() == null
						|| (Predicate) statement.getIntentionExpression().value(scope) == null
						|| ((Predicate) statement.getIntentionExpression().value(scope))
								.equalsIntentionPlan(currentIntention(scope).getPredicate())) {
					plans.add(statement);
				}
			}
		}
		return plans;
	}

	/**
	 * List executable norms.
	 *
	 * @param scope the scope
	 * @return the list
	 */
	protected final List<NormStatement> listExecutableNorms(final IScope scope) {
		final List<NormStatement> norms = new ArrayList<>();
		final List<Norm> normsCopy = new ArrayList(_norms);
		scope.getRandom().shuffleInPlace(normsCopy);
		for (final Norm Normstatement : normsCopy) {
			final NormStatement statement = Normstatement.getNormStatement();

			if (statement.getContextExpression() != null && ! Cast.asBool(scope, statement.getContextExpression().value(scope))) {
				continue;
			}
			if (currentIntention(scope) != null) {
				if (	statement.getIntentionExpression() != null && statement.getIntentionExpression().value(scope) != null
					&& ((Predicate) statement.getIntentionExpression().value(scope))
								.equalsIntentionPlan(currentIntention(scope).getPredicate())) {
					norms.add(statement);
				}
				if (statement.getObligationExpression() != null && statement.getObligationExpression().value(scope) != null
					&& ((Predicate) statement.getObligationExpression().value(scope))
								.equalsIntentionPlan(currentIntention(scope).getPredicate())) {
					norms.add(statement);
				}
			}
		}

		return norms;
	}


	/**
	 * Gets the thoughts.
	 *
	 * @param scope the scope
	 * @return the thoughts
	 */
	public IList<String> getThoughts(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		final IList<String> thoughts = (IList<String>) agent.getAttribute(LAST_THOUGHTS);
		return thoughts;
	}

	/**
	 * Adds the thoughts.
	 *
	 * @param scope the scope
	 * @param think the think
	 * @return the i list
	 */
	public IList<String> addThoughts(final IScope scope, final String think) {
		final IAgent agent = getCurrentAgent(scope);
		final IList<String> thoughts = (IList<String>) agent.getAttribute(LAST_THOUGHTS);
		final IList newthoughts = GamaListFactory.create(Types.STRING);
		newthoughts.add(think);
		if (thoughts != null && thoughts.size() > 0) {
			newthoughts.addAll(thoughts.subList(0, Math.min(LAST_THOUGHTS_SIZE - 1, thoughts.size())));
		}
		agent.setAttribute(LAST_THOUGHTS, newthoughts);
		return newthoughts;
	}

	
	private void removeIntention(final IScope scope, MentalState intention, boolean shouldRemoveFromIntentionBase) {
		final IList desbase = getBase(scope, DESIRE_BASE);
		final IList intentionbase = getBase(scope, INTENTION_BASE);
		desbase.remove(intention);
		intentionbase.remove(intention);
		if (shouldRemoveFromIntentionBase) {
			for (final MentalState statement : getBase(scope, SimpleBdiArchitecture.INTENTION_BASE)) {
				final List<MentalState> statementSubintention = statement.getPredicate().getSubintentions();
				if (statementSubintention != null && statementSubintention.contains(intention)) {
					statementSubintention.remove(intention);
				}
				final List<MentalState> statementOnHoldUntil = statement.getPredicate().getOnHoldUntil();
				if (statementOnHoldUntil != null && statementOnHoldUntil.contains(intention)) {
					statementOnHoldUntil.remove(intention);
				}
			}
		}
	}
	
	/**
	 * Test on hold.
	 *
	 * @param scope the scope
	 * @param intention the intention
	 * @return true, if successful
	 */
	public boolean testOnHold(final IScope scope, final MentalState intention) {
		
		if (intention == null) { return false; }
		if (intention.getPredicate() == null) { return false; }
		if (intention.onHoldUntil == null) { return false; }
		
		final IList desbase = getBase(scope, DESIRE_BASE);
		
		final List<MentalState> onHoldPredicate = intention.getPredicate().onHoldUntil;
		if (intention.getPredicate().getValues() != null && onHoldPredicate != null) {
			if (intention.getPredicate().getValues().containsKey("and")) {
				if (onHoldPredicate.size() == 0) {
					removeIntention(scope, intention, true);
					return false;
				} else {
					return true;
				}
			}
			if (intention.getPredicate().getValues().containsKey("or")) {
				if (onHoldPredicate.size() <= 1) {
					if (desbase.contains(onHoldPredicate.get(0))) {
						desbase.remove(onHoldPredicate.get(0));
					}
					removeIntention(scope, intention, onHoldPredicate.size() == 1);
					return false;
				} else {
					return true;
				}
			}
		}
		final List<MentalState> onHoldIntention = intention.onHoldUntil;
		if (!(onHoldIntention instanceof ArrayList)) {
			return false;
		}
		if (desbase.isEmpty()) { return false; }
		for (final MentalState subintention : onHoldIntention) {
			if (desbase.contains(subintention)) { 
				return true; 
			}
		}
		addThoughts(scope, "no more subintention for" + intention);
		/* Must remove the current plan to change for a new one */
		final IAgent agent = getCurrentAgent(scope);
		BDIPlan _persistentTask = (BDIPlan) agent.getAttribute(CURRENT_PLAN);
		_persistentTask = null;
		agent.setAttribute(CURRENT_PLAN, _persistentTask);
		return false;

	}

	/**
	 * Gets the plans.
	 *
	 * @param scope the scope
	 * @return the plans
	 */
	@action (
			name = "get_plans",
			doc = @doc (
					value = "get the list of plans.",
					returns = "the list of BDI plans.",
					examples = { @example ("get_plans()") }))
	public List<BDIPlan> getPlans(final IScope scope) {
		return _plans;
	}

	/**
	 * Gets the plan.
	 *
	 * @param scope the scope
	 * @return the plan
	 */
	@action (
			name = "get_plan",
			args = { @arg (
					name = "plan_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("the name of the plan to get")) },
			doc = @doc (
					value = "get the first plan with the given name",
					returns = "a BDIPlan",
					examples = { @example ("get_plan(name)") }))
	public BDIPlan getPlan(final IScope scope) {
		final String namePlan = scope.getTypedArgIfExists("plan_name", IType.STRING);
		for (final BDIPlan tempPlan : _plans) {
			if (tempPlan.getPlanStatement().getName().equals(namePlan)) { return tempPlan; }
		}
		return null;
	}

	/**
	 * Checks if is current plan.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	@action (
			name = "is_current_plan",
			args = { @arg (
					name = "plan_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("the name of the plan to test")) },
			doc = @doc (
					value = "tell if the current plan has the same name as tested",
					returns = "true if the current plan has the same name",
					examples = { @example ("is_current_plan(name)") }))
	public Boolean isCurrentPlan(final IScope scope) {
		final String namePlan = scope.getTypedArgIfExists("plan_name", IType.STRING);
		for (final BDIPlan tempPlan : _plans) {
			if (tempPlan.getPlanStatement().getName().equals(namePlan)) { return true; }
		}
		return false;
	}

	/**
	 * Gets the current plans.
	 *
	 * @param scope the scope
	 * @return the current plans
	 */
	@action (
			name = "get_current_plan",
			doc = @doc (
					value = "get the current plan.",
					returns = "the current plans.",
					examples = { @example ("get_current_plan()") }))
	public BDIPlan getCurrentPlans(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		final BDIPlan result = (BDIPlan) agent.getAttribute(CURRENT_PLAN);
		return result;
	}

	/**
	 * Gets the laws.
	 *
	 * @param scope the scope
	 * @return the laws
	 */
	public static List<LawStatement> getLaws(final IScope scope) {
		final IAgent agent = scope.getAgent();
		return scope.hasArg(LAW_BASE) ? scope.getListArg(LAW_BASE) : (List<LawStatement>) agent.getAttribute(LAW_BASE);
	}

	/**
	 * Gets the norms.
	 *
	 * @param scope the scope
	 * @return the norms
	 */
	public static List<Norm> getNorms(final IScope scope) {
		final IAgent agent = scope.getAgent();
		return scope.hasArg(NORM_BASE) ? scope.getListArg(NORM_BASE) : (List<Norm>) agent.getAttribute(NORM_BASE);
	}

	/**
	 * Gets the sanctions.
	 *
	 * @param scope the scope
	 * @return the sanctions
	 */
	public static List<Sanction> getSanctions(final IScope scope) {
		final IAgent agent = scope.getAgent();
		return scope.hasArg(SANCTION_BASE) ? scope.getListArg(SANCTION_BASE)
				: (List<Sanction>) agent.getAttribute(SANCTION_BASE);
	}

	/**
	 * Gets the base.
	 *
	 * @param scope the scope
	 * @param basename the basename
	 * @return the base
	 */
	public static IList<MentalState> getBase(final IScope scope, final String basename) {
		final IAgent agent = scope.getAgent();
		return scope.hasArg(basename) ? scope.getListArg(basename) : (IList<MentalState>) agent.getAttribute(basename);
	}

	/**
	 * Gets the emotion base.
	 *
	 * @param scope the scope
	 * @param basename the basename
	 * @return the emotion base
	 */
	public static IList<Emotion> getEmotionBase(final IScope scope, final String basename) {
		final IAgent agent = scope.getAgent();
		return scope.hasArg(basename) ? scope.getListArg(basename) : (IList<Emotion>) agent.getAttribute(basename);
	}

	/**
	 * Gets the social base.
	 *
	 * @param scope the scope
	 * @param basename the basename
	 * @return the social base
	 */
	public static IList<SocialLink> getSocialBase(final IScope scope, final String basename) {
		final IAgent agent = scope.getAgent();
		return scope.hasArg(basename) ? scope.getListArg(basename) : (IList<SocialLink>) agent.getAttribute(basename);
	}

	/**
	 * Removes the from base.
	 *
	 * @param scope the scope
	 * @param predicateItem the predicate item
	 * @param factBaseName the fact base name
	 * @return true, if successful
	 */
	public static boolean removeFromBase(final IScope scope, final MentalState predicateItem,
			final String factBaseName) {
		final IList<MentalState> factBase = getBase(scope, factBaseName);
		return factBase.remove(predicateItem);
	}

	/**
	 * Removes the from base.
	 *
	 * @param scope the scope
	 * @param emotionItem the emotion item
	 * @param factBaseName the fact base name
	 * @return true, if successful
	 */
	public static boolean removeFromBase(final IScope scope, final Emotion emotionItem, final String factBaseName) {
		final IList<Emotion> factBase = getEmotionBase(scope, factBaseName);
		return factBase.remove(emotionItem);
	}

	/**
	 * Removes the from base.
	 *
	 * @param scope the scope
	 * @param socialItem the social item
	 * @param factBaseName the fact base name
	 * @return true, if successful
	 */
	public static boolean removeFromBase(final IScope scope, final SocialLink socialItem, final String factBaseName) {
		final IList<SocialLink> factBase = getSocialBase(scope, factBaseName);
		return factBase.remove(socialItem);
	}

	/**
	 * Removes the from base.
	 *
	 * @param scope the scope
	 * @param normItem the norm item
	 * @return true, if successful
	 */
	public static boolean removeFromBase(final IScope scope, final Norm normItem) {
		final List<Norm> factBase = getNorms(scope);
		return factBase.remove(normItem);
	}

	/**
	 * Adds the to base.
	 *
	 * @param scope the scope
	 * @param mentalItem the mental item
	 * @param factBaseName the fact base name
	 * @return true, if successful
	 */
	public static boolean addToBase(final IScope scope, final MentalState mentalItem, final String factBaseName) {
		return addToBase(scope, mentalItem, getBase(scope, factBaseName));
	}

	/**
	 * Adds the to base.
	 *
	 * @param scope the scope
	 * @param emotionItem the emotion item
	 * @param factBaseName the fact base name
	 * @return true, if successful
	 */
	public static boolean addToBase(final IScope scope, final Emotion emotionItem, final String factBaseName) {
		return addToBase(scope, emotionItem, getEmotionBase(scope, factBaseName));
	}

	/**
	 * Adds the to base.
	 *
	 * @param scope the scope
	 * @param socialItem the social item
	 * @param factBaseName the fact base name
	 * @return true, if successful
	 */
	public static boolean addToBase(final IScope scope, final SocialLink socialItem, final String factBaseName) {
		return addToBase(scope, socialItem, getSocialBase(scope, factBaseName));
	}

	/**
	 * Adds the to base.
	 *
	 * @param scope the scope
	 * @param normItem the norm item
	 * @return true, if successful
	 */
	public static boolean addToBase(final IScope scope, final Norm normItem) {
		final List<Norm> factBase = getNorms(scope);
		return factBase.add(normItem);
	}

	/**
	 * Adds the to base.
	 *
	 * @param scope the scope
	 * @param mentalItem the mental item
	 * @param factBase the fact base
	 * @return true, if successful
	 */
	public static boolean addToBase(final IScope scope, final MentalState mentalItem,
			final IList<MentalState> factBase) {
		if (!factBase.contains(mentalItem)) {
			return factBase.add(mentalItem);
		}
		return false;
	}

	/**
	 * Adds the to base.
	 *
	 * @param scope the scope
	 * @param predicateItem the predicate item
	 * @param factBase the fact base
	 * @return true, if successful
	 */
	public static boolean addToBase(final IScope scope, final Emotion predicateItem, final IList<Emotion> factBase) {
		factBase.remove(predicateItem);
		return factBase.add(predicateItem);
	}

	/**
	 * Adds the to base.
	 *
	 * @param scope the scope
	 * @param socialItem the social item
	 * @param factBase the fact base
	 * @return true, if successful
	 */
	public static boolean addToBase(final IScope scope, final SocialLink socialItem, final IList<SocialLink> factBase) {
		factBase.remove(socialItem);
		return factBase.add(socialItem);
	}

	// le add belief crée les émotion joie, sadness, satisfaction, disapointment, relief, fear_confirmed, pride, shame,
	/**
	 * Adds the belief.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	// admiration, reproach
	public static Boolean addBelief(final IScope scope, final MentalState predicateDirect) {
		final Boolean use_emotion_architecture =
				scope.hasArg(USE_EMOTIONS_ARCHITECTURE) ? scope.getBoolArg(USE_EMOTIONS_ARCHITECTURE)
						: (Boolean) scope.getAgent().getAttribute(USE_EMOTIONS_ARCHITECTURE);
		MentalState predTemp = null;
		if (predicateDirect == null) {
			return false;
		}
		if (use_emotion_architecture) {
			createJoyFromPredicate(scope, predicateDirect);
			createSatisfactionFromMentalState(scope, predicateDirect); // satisfaction, disapointment, relief,
																		// fear_confirmed
			createPrideFromMentalState(scope, predicateDirect); // pride, shame, admiration, reproach
			createHappyForFromMentalState(scope, predicateDirect); // (seulement si le prédicat est sur une	
																	// émotion).
		}
		for (final MentalState predTest : getBase(scope, SimpleBdiArchitecture.BELIEF_BASE)) {
			if (   predTest.getPredicate() != null && predicateDirect.getPredicate() != null
				&& predTest.getPredicate().equalsButNotTruth(predicateDirect.getPredicate())) {
				predTemp = predTest;
			}
		}
		if (predTemp != null) {
			removeFromBase(scope, predTemp, BELIEF_BASE);
		}
		if (getBase(scope, SimpleBdiArchitecture.INTENTION_BASE).contains(predicateDirect)) {
			removeFromBase(scope, predicateDirect, DESIRE_BASE);
			removeFromBase(scope, predicateDirect, INTENTION_BASE);
			scope.getAgent().setAttribute(CURRENT_PLAN, null);
			scope.getAgent().setAttribute(CURRENT_NORM, null);
		}
		if (getBase(scope, SimpleBdiArchitecture.UNCERTAINTY_BASE).contains(predicateDirect)) {
			removeFromBase(scope, predicateDirect, UNCERTAINTY_BASE);
		}
		if (getBase(scope, SimpleBdiArchitecture.OBLIGATION_BASE).contains(predicateDirect)) {
			removeFromBase(scope, predicateDirect, OBLIGATION_BASE);
		}
		for (final MentalState predTest : getBase(scope, SimpleBdiArchitecture.UNCERTAINTY_BASE)) {
			if (	predTest.getPredicate() != null && predicateDirect.getPredicate() != null
				&& 	predTest.getPredicate().equalsButNotTruth(predicateDirect.getPredicate())) {
				predTemp = predTest;
			}
		}
		if (predTemp != null) {
			removeFromBase(scope, predTemp, UNCERTAINTY_BASE);
		}
		for (final MentalState statement : getBase(scope, SimpleBdiArchitecture.INTENTION_BASE)) {
			List<MentalState> statementSubintention = null;
			List<MentalState> statementOnHoldUntil = null;
			if (statement.getPredicate() != null) {
				statementSubintention = statement.getSubintentions();
				statementOnHoldUntil = statement.getOnHoldUntil();
			}
			if (statementSubintention != null && statementSubintention.contains(predicateDirect)) {
				statementSubintention.remove(predicateDirect);
			}
			if (statementOnHoldUntil != null && statementOnHoldUntil.contains(predicateDirect)) {
				statementOnHoldUntil.remove(predicateDirect);
			}
		}
		predicateDirect.setOwner(scope.getAgent());
		return addToBase(scope, predicateDirect, BELIEF_BASE);
	}

	/**
	 * Prim add belief.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_belief",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to add as a belief")),
					@arg (
							name = MentalState.STRENGTH,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the belief")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the belief")) },
			doc = @doc (
					value = "add the predicate in the belief base.",
					returns = "true if it is added in the base.",
					examples = { @example ("") }))
	public Boolean primAddBelief(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect = scope.getTypedArgIfExists(PREDICATE, PredicateType.id);
		final Double stre = scope.getTypedArgIfExists(MentalState.STRENGTH, IType.FLOAT);
		final int life = scope.getTypedArgIfExists("lifetime", IType.INT, -1);
		MentalState tempState;
		if (predicateDirect != null) {
			tempState = new MentalState("Belief", predicateDirect);
		} else {
			tempState = new MentalState("Belief");
		}
		if (life > 0) {
			tempState.setLifeTime(life);
		}
		if (stre != null) {
			tempState.setStrength(stre);
		}
		tempState.setOwner(scope.getAgent());
		return addBelief(scope, tempState);

	}

	/**
	 * Prim add directly belief.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_directly_belief",
			args = { @arg (
					name = "belief",
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("belief to add in th belief base")) },
			doc = @doc (
					value = "add the belief in the belief base.",
					returns = "true if it is added in the base.",
					examples = { @example ("") }))
	public Boolean primAddDirectlyBelief(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists("belief", MentalStateType.id);
		if (predicateDirect != null && "Belief".equals(predicateDirect.getModality())) {
			predicateDirect.setOwner(scope.getAgent());
			return addBelief(scope, predicateDirect);
		}
		return false;

	}

	/**
	 * Prim add belief mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_belief_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("predicate to add as a belief")),
					@arg (
							name = MentalState.STRENGTH,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the belief")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the belief")) },
			doc = @doc (
					value = "add the predicate in the belief base.",
					returns = "true if it is added in the base.",
					examples = { @example ("") }))
	public Boolean primAddBeliefMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState stateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final Double stre = scope.getTypedArgIfExists(MentalState.STRENGTH, IType.FLOAT);
		final Integer life = scope.getTypedArgIfExists("lifetime", IType.INT);
		
		MentalState tempState = new MentalState("Belief", stateDirect);

		if (stre != null) {
			tempState.setStrength(stre);
		} 
		if (life != null) {
			tempState.setLifeTime(life);
		}
		tempState.setOwner(scope.getAgent());
		return addBelief(scope, tempState);

	}

	/**
	 * Prim add belief emotion.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	// va déclencher les émotions happy_for, sorry_for, resentment et gloating
	@action (
			name = "add_belief_emotion",
			args = { @arg (
					name = "emotion",
					type = EmotionType.EMOTIONTYPE_ID,
					optional = true,
					doc = @doc ("emotion to add as a belief")),
					@arg (
							name = "strength",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the belief")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the belief")) },
			doc = @doc (
					value = "add the belief about an emotion in the belief base.",
					returns = "true if it is added in the base.",
					examples = { @example ("") }))
	public Boolean primAddBeliefEmotion(final IScope scope) throws GamaRuntimeException {
		final Emotion stateDirect =
				(Emotion) (scope.hasArg("emotion") ? scope.getArg("emotion", EmotionType.EMOTIONTYPE_ID) : null);
		final Double stre = (Double) (scope.hasArg("strength") ? scope.getArg("strength", IType.FLOAT) : null);
		final int life = (int) (scope.hasArg("lifetime") ? scope.getArg("lifetime", IType.INT) : -1);
		MentalState tempState;
		if (stateDirect != null) {
			tempState = new MentalState("Belief", stateDirect);
		} else {
			tempState = new MentalState("Belief");
		}
		if (life > 0) {
			tempState.setLifeTime(life);
		}
		if (stre != null) {
			tempState.setStrength(stre);
		} 
		tempState.setOwner(scope.getAgent());
		return addBelief(scope, tempState);

	}

	/**
	 * Checks for belief.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	public static Boolean hasBelief(final IScope scope, final MentalState predicateDirect) {
		return getBase(scope, BELIEF_BASE).contains(predicateDirect);

	}

	/**
	 * Checks for desire.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	public static Boolean hasDesire(final IScope scope, final MentalState predicateDirect) {
		return getBase(scope, DESIRE_BASE).contains(predicateDirect);
	}

	/**
	 * Prim test belief.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_belief",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to check")) },
			doc = @doc (
					value = "check if the predicates is in the belief base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestBelief(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final MentalState tempState = new MentalState("Belief", predicateDirect);
		if (predicateDirect != null) { return hasBelief(scope, tempState); }
		return false;
	}

	/**
	 * Checks for belief name.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_belief_with_name",
			args = { @arg (
					name = "belief_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the predicate to check")) },
			doc = @doc (
					value = "check if the predicate is in the belief base.",
					returns = "true if it is in the base.",
					examples = { @example ("has_belief_with_name(\"has_water\")") }))
	public Boolean hasBeliefName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.getTypedArgIfExists("name", IType.STRING);
		if (predicateName != null) {
			final MentalState tempState = new MentalState("Belief", new Predicate(predicateName));
			return hasBelief(scope, tempState);
		}
		return null;
	}

	/**
	 * Prim test belief mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_belief_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("mental state to check")) },
			doc = @doc (
					value = "check if the mental state is in the belief base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestBeliefMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.<MentalState>getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final MentalState tempState = new MentalState("Belief", predicateDirect);
		if (predicateDirect != null) { 
			return hasBelief(scope, tempState); 
		}
		return false;
	}

	/**
	 * Gets the belief.
	 *
	 * @param scope the scope
	 * @return the belief
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_belief",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("predicate to get")) },
			doc = @doc (
					value = "return the belief about the predicate in the belief base (if several, returns the first one).",
					returns = "the belief about the predicate if it is in the base.",
					examples = { @example ("get_belief(new_predicate(\"has_water\", true))") }))
	public MentalState getBelief(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect = scope.getTypedArgIfExists(PREDICATE, PredicateType.id);
		if (predicateDirect == null) {
			return null;
		}
		for (final MentalState mental : getBase(scope, BELIEF_BASE)) {
			if (mental.getPredicate() != null) {
				if (predicateDirect.equals(mental.getPredicate())) { return mental; }
				if (predicateDirect.equalsButNotTruth(mental.getPredicate())) { return mental; }
			}
		}
		return null;

	}

	/**
	 * Gets the belief mental state.
	 *
	 * @param scope the scope
	 * @return the belief mental state
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_belief_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = false,
					doc = @doc ("mental state to get")) },
			doc = @doc (
					value = "return the belief about the mental state in the belief base (if several, returns the first one).",
					returns = "the belief about the mental state if it is in the base.",
					examples = { @example ("get_belief(new_mental_state(\"Desire\", predicate1))") }))
	public MentalState getBeliefMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		return getBeliefMentalStateFromBase(scope, predicateDirect);
	}
	
	public static MentalState getBeliefMentalStateFromBase(final IScope scope, MentalState predicateDirect) {
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, BELIEF_BASE)) {
				if (mental.getMentalState() != null && predicateDirect.equals(mental.getMentalState())) { 
					return mental; 
				}
			}
		}
		return null;
	}
	

	/**
	 * Gets the belief emotion.
	 *
	 * @param scope the scope
	 * @return the belief emotion
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_belief_emotion",
			args = { @arg (
					name = SimpleBdiArchitecture.EMOTION,
					type = EmotionType.EMOTIONTYPE_ID,
					optional = false,
					doc = @doc ("emotion about which the belief to get is")) },
			doc = @doc (
					value = "return the belief about the emotion in the belief base (if several, returns the first one).",
					returns = "the belief about the emotion if it is in the base.",
					examples = { @example ("get_belief(new_mental_state(\"Desire\", predicate1))") }))
	public MentalState getBeliefEmotion(final IScope scope) throws GamaRuntimeException {
		final Emotion predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.EMOTION, EmotionType.EMOTIONTYPE_ID);
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, BELIEF_BASE)) {
				if (mental.getMentalState() != null) {
					if (predicateDirect.equals(mental.getEmotion())) { return mental; }
				}
			}

		}
		return null;

	}

	/**
	 * Gets the belief name.
	 *
	 * @param scope the scope
	 * @return the belief name
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_belief_with_name",
			args = { @arg (
					name = "belief_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the predicate to check")) },
			doc = @doc (
					value = "get the predicate in the belief base (if several, returns the first one).",
					returns = "the predicate if it is in the base.",
					examples = { @example ("get_belief_with_name(\"has_water\")") }))
	public MentalState getBeliefName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.<String>getTypedArg("belief_name", IType.STRING);
		if (predicateName != null) {
			for (final MentalState mental : getBase(scope, BELIEF_BASE)) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					return mental;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the beliefs name.
	 *
	 * @param scope the scope
	 * @return the beliefs name
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_beliefs_with_name",
			args = { @arg (
					name = "belief_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the predicates to check")) },
			doc = @doc (
					value = "get the list of predicates in the belief base with the given name.",
					returns = "the list of beliefs (mental state).",
					examples = { @example ("get_belief(\"has_water\")") }))
	public IList<MentalState> getBeliefsName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.<String>getTypedArgIfExists("belief_name", IType.STRING);
		final IList<MentalState> predicates = GamaListFactory.create();
		if (predicateName != null) {
			for (final MentalState mental : getBase(scope, BELIEF_BASE)) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the beliefs.
	 *
	 * @param scope the scope
	 * @return the beliefs
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_beliefs",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("predicate to check")) },
			doc = @doc (
					value = "get the list of predicates in the belief base",
					returns = "the list of beliefs (mental state).",
					examples = { @example ("get_beliefs(\"has_water\")") }))
	public IList<MentalState> getBeliefs(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect = scope.<Predicate>getTypedArgIfExists(PREDICATE, PredicateType.id);
		final IList<MentalState> predicates = GamaListFactory.create();
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, BELIEF_BASE)) {
				if (mental.getPredicate() != null) {
					if (predicateDirect.equals(mental.getPredicate())) {
						predicates.add(mental);
					}
					if (predicateDirect.equalsButNotTruth(mental.getPredicate())) {
						predicates.add(mental);
					}
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the beliefs mental state.
	 *
	 * @param scope the scope
	 * @return the beliefs mental state
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_beliefs_metal_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = false,
					doc = @doc ("mental state to check")) },
			doc = @doc (
					value = "get the list of bliefs in the belief base containing the mental state",
					returns = "the list of beliefs (mental state).",
					examples = { @example ("get_beliefs_mental_state(\"has_water\")") }))
	public IList<MentalState> getBeliefsMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.<MentalState>getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final IList<MentalState> predicates = GamaListFactory.create();
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, BELIEF_BASE)) {
				if (mental.getMentalState() != null) {
					if (predicateDirect.equals(mental.getMentalState())) {
						predicates.add(mental);
					}
				}
			}
		}
		return predicates;
	}

	/**
	 * Iscurrent intention.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "is_current_intention",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("predicate to check")) },
			doc = @doc (
					value = "check if the predicates is the current intention (last entry of intention base).",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean iscurrentIntention(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect = scope.<Predicate>getTypedArgIfExists(PREDICATE, PredicateType.id);
		final Predicate currentIntention;
		if (currentIntention(scope) != null) {
			currentIntention = currentIntention(scope).getPredicate();
		} else {
			currentIntention = null;
		}

		if (predicateDirect != null && currentIntention != null) { return predicateDirect.equals(currentIntention); }

		return false;
	}

	/**
	 * Iscurrent intention mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "is_current_intention_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = false,
					doc = @doc ("mental state to check")) },
			doc = @doc (
					value = "check if the mental state is the current intention (last entry of intention base).",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean iscurrentIntentionMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.<MentalState>getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final MentalState currentIntention = currentIntention(scope).getMentalState();

		if (predicateDirect != null && currentIntention != null) { return predicateDirect.equals(currentIntention); }

		return false;
	}

	/**
	 * Current intention.
	 *
	 * @param scope the scope
	 * @return the mental state
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_current_intention",
			doc = @doc (
					value = "returns the current intention (last entry of intention base).",
					returns = "the current intention",
					examples = { @example ("") }))
	public MentalState currentIntention(final IScope scope) throws GamaRuntimeException {
		final IList<MentalState> intentionBase = getBase(scope, INTENTION_BASE);
		if (intentionBase == null) { return null; }
		if (!intentionBase.isEmpty()) { return intentionBase.lastValue(scope); }
		return null;
	}

	/**
	 * Prim test desire.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_desire",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to check")) },
			doc = @doc (
					value = "check if the predicates is in the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestDesire(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect = scope.<Predicate>getTypedArgIfExists(PREDICATE, PredicateType.id);
		if (predicateDirect != null) {
			final MentalState temp = new MentalState("Desire", predicateDirect);
			return getBase(scope, DESIRE_BASE).contains(temp);
		}
		return false;
	}

	/**
	 * Checks for desire name.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_desire_with_name",
			args = { @arg (
					name = "desire_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the predicate to check")) },
			doc = @doc (
					value = "check if the prediate is in the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("has_desire_with_name(\"has_water\")") }))
	public Boolean hasDesireName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.<String>getTypedArgIfExists("desire_name", IType.STRING);
		if (predicateName != null) {
			final MentalState tempState = new MentalState("Desire", new Predicate(predicateName));
			return hasDesire(scope, tempState);
		}
		return null;
	}

	/**
	 * Prim test desire mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_desire_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("mental state to check")) },
			doc = @doc (
					value = "check if the mental state is in the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestDesireMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.<MentalState>getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		if (predicateDirect != null) {
			final MentalState temp = new MentalState("Desire", predicateDirect);
			return getBase(scope, DESIRE_BASE).contains(temp);
		}
		return false;
	}

	/**
	 * Prim on hold intention.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "current_intention_on_hold",
			args = { @arg (
					name = "until",
					type = IType.NONE,
					optional = true,
					doc = @doc ("the current intention is put on hold (fited plan are not considered) until specific condition is reached. Can be an expression (which will be tested), a list (of subintentions), or nil (by default the condition will be the current list of subintentions of the intention)")) },

			doc = @doc (
					value = "puts the current intention on hold until the specified condition is reached or all subintentions are reached (not in desire base anymore).",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primOnHoldIntention(final IScope scope) throws GamaRuntimeException {
		MentalState predicate = null;
		if (currentIntention(scope) != null) {
			predicate = currentIntention(scope);
		}
		if (predicate != null) {
			final Predicate until = scope.getTypedArgIfExists("until", PredicateType.id);
			if (until == null) {
				final List<MentalState> subintention = predicate.subintentions;
				if (subintention != null && !subintention.isEmpty()) {
					predicate.onHoldUntil = subintention;
				}
			} else {
				if (predicate.onHoldUntil == null) {
					predicate.onHoldUntil = GamaListFactory.create(Types.get(MentalStateType.id));
				}
				if (predicate.getSubintentions() == null) {
					predicate.subintentions = GamaListFactory.create(Types.get(MentalStateType.id));
				}
				final MentalState tempState = new MentalState("Intention", predicate.getPredicate());
				final MentalState tempUntil = new MentalState("Desire", until);
				tempUntil.setSuperIntention(tempState);
				predicate.onHoldUntil.add(tempUntil);
				predicate.getSubintentions().add(tempUntil);
				addToBase(scope, tempUntil, DESIRE_BASE);
			}
		}
		return true;
	}

	/**
	 * Adds the sub intention.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_subintention",
			args = { @arg (
					name = PREDICATE,
					type = MentalStateType.id,
					optional = false,
					doc = @doc ("the intention that receives the sub_intention")),
					@arg (
							name = PREDICATE_SUBINTENTION,
							type = PredicateType.id,
							optional = false,
							doc = @doc ("the predicate to add as a subintention to the intention")),
					@arg (
							name = "add_as_desire",
							type = IType.BOOL,
							optional = true,
							doc = @doc ("add the subintention as a desire as well (by default, false) ")) },
			doc = @doc (
					value = "adds the predicates in the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean addSubIntention(final IScope scope) throws GamaRuntimeException {
		final MentalState predicate = scope.<MentalState>getTypedArgIfExists(PREDICATE, MentalStateType.id);
		final Predicate subpredicate = scope.<Predicate>getTypedArgIfExists(PREDICATE_SUBINTENTION, PredicateType.id);

		if (predicate == null || subpredicate == null) { return false; }
		final boolean addAsDesire = scope.<Boolean>getTypedArgIfExists("add_as_desire", IType.BOOL, false);
		MentalState superState = null;
		for (final MentalState mental : getBase(scope, INTENTION_BASE)) {
			if (mental != null && predicate.equals(mental)) {
				superState = mental;
				break;
			}
		}
		if (superState == null) { return false; }

		if (predicate.getSubintentions() == null) {
			predicate.subintentions = GamaListFactory.create(Types.get(MentalStateType.id));
		}
		final MentalState subState = new MentalState("Desire", subpredicate);
		subpredicate.setSuperIntention(superState);
		predicate.getSubintentions().add(subState);
		subState.strength = superState.strength;
		if (addAsDesire) {
			addToBase(scope, subState, DESIRE_BASE);
		}
		return true;
	}

	/**
	 * Adds the desire.
	 *
	 * @param scope the scope
	 * @param superPredicate the super predicate
	 * @param predicate the predicate
	 * @return the boolean
	 */
	public static Boolean addDesire(final IScope scope, final MentalState superPredicate, final MentalState predicate) {
		if (superPredicate != null && superPredicate.getPredicate() != null) {
			if (superPredicate.getPredicate().getSubintentions() == null) {
				superPredicate.getPredicate().subintentions = GamaListFactory.create(Types.get(PredicateType.id));
			}
			if (predicate.getPredicate() != null) {
				predicate.getPredicate().setSuperIntention(superPredicate);
			}
			superPredicate.getPredicate().getSubintentions().add(predicate);
		}
		predicate.setOwner(scope.getAgent());
		addToBase(scope, predicate, DESIRE_BASE);

		return false;
	}

	/**
	 * Prim add desire.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_desire",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to add as a desire")),
					@arg (
							name = MentalState.STRENGTH,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the belief")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the belief")),
					@arg (
							name = PREDICATE_TODO,
							type = PredicateType.id,
							optional = true,
							doc = @doc ("add the desire as a subintention of this parameter")), },
			doc = @doc (
					value = "adds the predicates is in the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primAddDesire(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect = scope.<Predicate>getTypedArgIfExists(PREDICATE, PredicateType.id);
		final Double stre = scope.<Double>getTypedArgIfExists(MentalState.STRENGTH, IType.FLOAT);
		final int life = scope.<Integer>getTypedArgIfExists("lifetime", IType.INT, -1);
		if (predicateDirect != null) {
			final Predicate superpredicate = scope.<Predicate>getTypedArgIfExists(PREDICATE_TODO, PredicateType.id);
			final MentalState tempPred = new MentalState("Desire", predicateDirect);
			final MentalState tempSuper = new MentalState("Intention", superpredicate);
			if (stre != null) {
				tempPred.setStrength(stre);
			}
			if (life > 0) {
				tempPred.setLifeTime(life);
			}
			tempPred.setOwner(scope.getAgent());
			return addDesire(scope, tempSuper, tempPred);
		}
		return false;
	}

	/**
	 * Prim add directly desire.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_directly_desire",
			args = { @arg (
					name = "desire",
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("desire to add in th belief base")) },
			doc = @doc (
					value = "add the desire in the desire base.",
					returns = "true if it is added in the base.",
					examples = { @example ("") }))
	public Boolean primAddDirectlyDesire(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.<MentalState>getTypedArgIfExists("desire", MentalStateType.id);
		if (predicateDirect != null && "Desire".equals(predicateDirect.getModality())) {
			predicateDirect.setOwner(scope.getAgent());
			return addDesire(scope, null, predicateDirect);
		}
		return false;

	}

	/**
	 * Prim add desire mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_desire_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("mental_state to add as a desire")),
					@arg (
							name = MentalState.STRENGTH,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the desire")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the desire")),
					@arg (
							name = PREDICATE_TODO,
							type = PredicateType.id,
							optional = true,
							doc = @doc ("add the desire as a subintention of this parameter")), },
			doc = @doc (
					value = "adds the mental state is in the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primAddDesireMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState stateDirect = scope.<MentalState>getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final Double stre = scope.<Double>getTypedArgIfExists(MentalState.STRENGTH, IType.FLOAT);
		final int life = scope.<Integer>getTypedArgIfExists("lifetime", IType.INT, -1);
		if (stateDirect != null) {
			final Predicate superpredicate = scope.<Predicate>getTypedArgIfExists(PREDICATE_TODO, PredicateType.id);
			final MentalState tempPred = new MentalState("Desire", stateDirect);
			final MentalState tempSuper = new MentalState("Intention", superpredicate);
			if (stre != null) {
				tempPred.setStrength(stre);
			}
			if (life > 0) {
				tempPred.setLifeTime(life);
			}
			tempPred.setOwner(scope.getAgent());
			return addDesire(scope, tempSuper, tempPred);
		}

		return false;
	}

	/**
	 * Prim add desire emotion.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_desire_emotion",
			args = { @arg (
					name = SimpleBdiArchitecture.EMOTION,
					type = EmotionType.EMOTIONTYPE_ID,
					optional = true,
					doc = @doc ("emotion to add as a desire")),
					@arg (
							name = MentalState.STRENGTH,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the desire")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the desire")),
					@arg (
							name = PREDICATE_TODO,
							type = PredicateType.id,
							optional = true,
							doc = @doc ("add the desire as a subintention of this parameter")), },
			doc = @doc (
					value = "adds the emotion in the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primAddDesireEmotion(final IScope scope) throws GamaRuntimeException {
		final Emotion stateDirect = scope.getTypedArgIfExists("emotion", EmotionType.EMOTIONTYPE_ID);
		final Double stre = scope.getTypedArgIfExists(MentalState.STRENGTH, IType.FLOAT);
		final int life = scope.<Integer>getTypedArgIfExists("lifetime", IType.INT, -1);
		if (stateDirect != null) {
			final Predicate superpredicate = scope.getTypedArgIfExists(PREDICATE_TODO, PredicateType.id);
			final MentalState tempPred = new MentalState("Desire", stateDirect);
			final MentalState tempSuper = new MentalState("Intention", superpredicate);
			if (stre != null) {
				tempPred.setStrength(stre);
			}
			if (life > 0) {
				tempPred.setLifeTime(life);
			}
			tempPred.setOwner(scope.getAgent());
			return addDesire(scope, tempSuper, tempPred);
		}

		return false;
	}

	/**
	 * Gets the desire.
	 *
	 * @param scope the scope
	 * @return the desire
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_desire",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("predicate to check")) },
			doc = @doc (
					value = "get the predicates is in the desire base (if several, returns the first one).",
					returns = "the predicate if it is in the base.",
					examples = { @example ("get_desire(new_predicate(\"has_water\", true))") }))
	public MentalState getDesire(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect = scope.getTypedArgIfExists(PREDICATE, PredicateType.id);
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
				if (mental.getPredicate() != null && predicateDirect.equals(mental.getPredicate())) { return mental; }
			}
		}
		return null;
	}

	/**
	 * Gets the desire mental state.
	 *
	 * @param scope the scope
	 * @return the desire mental state
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_desire_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = false,
					doc = @doc ("mental state to check")) },
			doc = @doc (
					value = "get the mental state is in the desire base (if several, returns the first one).",
					returns = "the predicate if it is in the base.",
					examples = { @example ("get_desire(new_predicate(\"has_water\", true))") }))
	public MentalState getDesireMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
				if (mental.getMentalState() != null && predicateDirect.equals(mental.getMentalState())) {
					return mental;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the desires.
	 *
	 * @param scope the scope
	 * @return the desires
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_desires",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("name of the predicates to check")) },
			doc = @doc (
					value = "get the list of predicates is in the desire base",
					returns = "the list of deires.",
					examples = { @example ("get_desires(\"has_water\")") }))
	public IList<MentalState> getDesires(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect = scope.getTypedArgIfExists(PREDICATE, PredicateType.id);
		final IList<MentalState> predicates = GamaListFactory.create();
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
				if (mental.getPredicate() != null && predicateDirect.equals(mental.getPredicate())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the desires mental state.
	 *
	 * @param scope the scope
	 * @return the desires mental state
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_desires_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = false,
					doc = @doc ("name of the mental states to check")) },
			doc = @doc (
					value = "get the list of mental states is in the desire base",
					returns = "the list of mental states.",
					examples = { @example ("get_desires_mental_state(\"Belief\",predicte1)") }))
	public IList<MentalState> getDesiresMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final IList<MentalState> predicates = GamaListFactory.create();
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
				if (mental.getMentalState() != null && predicateDirect.equals(mental.getMentalState())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the desire name.
	 *
	 * @param scope the scope
	 * @return the desire name
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_desire_with_name",
			args = { @arg (
					name = "desire_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the predicate to check")) },
			doc = @doc (
					value = "get the predicates is in the belief base (if several, returns the first one).",
					returns = "the predicate if it is in the base.",
					examples = { @example ("get_desire_with_name(\"has_water\")") }))
	public MentalState getDesireName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.getTypedArgIfExists("desire_name", IType.STRING);
		if (predicateName != null) {
			for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					return mental;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the desires name.
	 *
	 * @param scope the scope
	 * @return the desires name
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_desires_with_name",
			args = { @arg (
					name = "desire_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the predicates to check")) },
			doc = @doc (
					value = "get the list of predicates is in the belief base with the given name.",
					returns = "the list of predicates.",
					examples = { @example ("get_belief(\"has_water\")") }))
	public List<MentalState> getDesiresName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.getTypedArgIfExists("desire_name", IType.STRING);
		final List<MentalState> predicates = GamaListFactory.create();
		if (predicateName != null) {
			for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Removes the belief.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeBelief(final IScope scope, final MentalState pred) {
		return getBase(scope, BELIEF_BASE).remove(pred);
	}

	/**
	 * Prim remove belief.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_belief",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to remove")) },
			doc = @doc (
					value = "removes the predicate from the belief base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveBelief(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		if (predicateDirect != null) {
			final MentalState temp = new MentalState("Belief", predicateDirect);
			return removeBelief(scope, temp);
		}
		return false;
	}

	/**
	 * Prim remove belief mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_belief_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("mental state to remove")) },
			doc = @doc (
					value = "removes the mental state from the belief base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveBeliefMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		if (predicateDirect != null) {
			final MentalState temp = new MentalState("Belief", predicateDirect);
			return removeBelief(scope, temp);
		}
		return false;
	}

	/**
	 * Prim place belief.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "replace_belief",
			args = { @arg (
					name = "old_predicate",
					type = PredicateType.id,
					optional = false,
					doc = @doc ("predicate to remove")),
					@arg (
							name = PREDICATE,
							type = PredicateType.id,
							optional = false,
							doc = @doc ("predicate to add")) },
			doc = @doc (
					value = "replace the old predicate by the new one.",
					returns = "true if the old predicate is in the base.",
					examples = { @example ("") }))
	public Boolean primPlaceBelief(final IScope scope) throws GamaRuntimeException {
		final Predicate oldPredicate = scope.getTypedArgIfExists("old_predicate", PredicateType.id);
		boolean ok = true;
		if (oldPredicate != null) {
			ok = getBase(scope, BELIEF_BASE).remove(new MentalState("Belief", oldPredicate));
		} else {
			ok = false;
		}
		final Predicate newPredicate = scope.getTypedArgIfExists(PREDICATE, PredicateType.id);
		if (newPredicate == null) {
			return ok;
		}
		
		final MentalState temp = new MentalState("Belief", newPredicate);
		// Predicate current_intention = currentIntention(scope);
		if (getBase(scope, SimpleBdiArchitecture.INTENTION_BASE)
				.contains(new MentalState("Intention", newPredicate))) {
			removeFromBase(scope, temp, DESIRE_BASE);
			removeFromBase(scope, temp, INTENTION_BASE);
		}
		if (getBase(scope, SimpleBdiArchitecture.DESIRE_BASE).contains(new MentalState("Desire", newPredicate))) {
			removeFromBase(scope, temp, DESIRE_BASE);
		}
		for (final MentalState statement : getBase(scope, SimpleBdiArchitecture.INTENTION_BASE)) {
			if (statement.getPredicate() != null) {
				final List<MentalState> statementSubintention = statement.getPredicate().getSubintentions();
				if (statementSubintention != null) {
					if (statementSubintention.contains(temp)) {
						statementSubintention.remove(temp);
					}
				}
				final List<MentalState> statementOnHoldUntil = statement.getPredicate().getOnHoldUntil();
				if (statementOnHoldUntil != null) {
					if (statementOnHoldUntil.contains(temp)) {
						statementOnHoldUntil.remove(temp);
					}
				}
			}
		}
		return addToBase(scope, temp, BELIEF_BASE);
	}

	/**
	 * Removes the desire.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeDesire(final IScope scope, final MentalState pred) {
		getBase(scope, DESIRE_BASE).remove(pred);
		getBase(scope, INTENTION_BASE).remove(pred);
		for (final MentalState statement : getBase(scope, SimpleBdiArchitecture.INTENTION_BASE)) {
			if (statement.getPredicate() != null) {
				final List<MentalState> statementSubintention = statement.getPredicate().getSubintentions();
				if (statementSubintention != null) {
					if (statementSubintention.contains(pred)) {
						statementSubintention.remove(pred);
					}
				}
				final List<MentalState> statementOnHoldUntil = statement.getPredicate().getOnHoldUntil();
				if (statementOnHoldUntil != null) {
					if (statementOnHoldUntil.contains(pred)) {
						statementOnHoldUntil.remove(pred);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Prim remove desire.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_desire",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to remove from desire base")) },
			doc = @doc (
					value = "removes the predicates from the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveDesire(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		if (predicateDirect != null) {
			final MentalState temp = new MentalState("Desire", predicateDirect);
			return removeDesire(scope, temp);
		}
		return false;
	}

	/**
	 * Prim remove desire mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_desire_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("mental state to remove from desire base")) },
			doc = @doc (
					value = "removes the mental state from the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveDesireMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		if (predicateDirect != null) {
			final MentalState temp = new MentalState("Desire", predicateDirect);
			return removeDesire(scope, temp);
		}
		return false;
	}

	/**
	 * Prim add intention.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_intention",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to check")),
					@arg (
							name = "strength",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the belief")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the belief")) },
			doc = @doc (
					value = "check if the predicates is in the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primAddIntention(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final Double stre = (Double) (scope.hasArg("strength") ? scope.getArg("strength", IType.FLOAT) : null);
		final int life = (int) (scope.hasArg("lifetime") ? scope.getArg("lifetime", IType.INT) : -1);
		MentalState temp;
		if (predicateDirect != null) {
			temp = new MentalState("Intention", predicateDirect);
		} else {
			temp = new MentalState("Intention");
		}
		if (stre != null) {
			temp.setStrength(stre);
		}
		if (life > 0) {
			temp.setLifeTime(life);
		}
		temp.setOwner(scope.getAgent());
		return addToBase(scope, temp, INTENTION_BASE);

	}

	/**
	 * Prim add intention mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_intention_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("predicate to add as an intention")),
					@arg (
							name = "strength",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the belief")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the belief")) },
			doc = @doc (
					value = "check if the predicates is in the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primAddIntentionMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState stateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final Double stre = scope.getTypedArgIfExists(MentalState.STRENGTH, IType.FLOAT);
		final int life = scope.getTypedArgIfExists("lifetime", IType.INT, -1);
		MentalState temp;
		if (stateDirect != null) {
			temp = new MentalState("Intention", stateDirect);
		} else {
			temp = new MentalState("Intention");
		}
		if (stre != null) {
			temp.setStrength(stre);
		}
		if (life > 0) {
			temp.setLifeTime(life);
		}
		temp.setOwner(scope.getAgent());
		return addToBase(scope, temp, INTENTION_BASE);

	}

	/**
	 * Prim add intention emotion.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_intention_emotion",
			args = { @arg (
					name = SimpleBdiArchitecture.EMOTION,
					type = EmotionType.EMOTIONTYPE_ID,
					optional = true,
					doc = @doc ("emotion to add as an intention")),
					@arg (
							name = MentalState.STRENGTH,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the belief")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the belief")) },
			doc = @doc (
					value = "check if the predicates is in the desire base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primAddIntentionEmotion(final IScope scope) throws GamaRuntimeException {
		final Emotion stateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.EMOTION, EmotionType.EMOTIONTYPE_ID);
		final Double stre = scope.getTypedArgIfExists(MentalState.STRENGTH, IType.FLOAT);
		final int life = scope.getTypedArgIfExists("lifetime", IType.INT, -1);
		MentalState temp;
		if (stateDirect != null) {
			temp = new MentalState("Intention", stateDirect);
		} else {
			temp = new MentalState("Intention");
		}
		if (stre != null) {
			temp.setStrength(stre);
		}
		if (life > 0) {
			temp.setLifeTime(life);
		}
		temp.setOwner(scope.getAgent());
		return addToBase(scope, temp, INTENTION_BASE);

	}

	/**
	 * Gets the intention.
	 *
	 * @param scope the scope
	 * @return the intention
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_intention",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("predicate to check")) },
			doc = @doc (
					value = "get the predicates in the intention base (if several, returns the first one).",
					returns = "the mental state if it is in the base.",
					examples = { @example ("get_intention(new_predicate(\"has_water\", true))") }))
	public MentalState getIntention(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, INTENTION_BASE)) {
				if (mental.getPredicate() != null && predicateDirect.equals(mental.getPredicate())) { return mental; }
			}
		}
		return null;
	}

	/**
	 * Gets the intention mental state.
	 *
	 * @param scope the scope
	 * @return the intention mental state
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_intention_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = false,
					doc = @doc ("mental state to check")) },
			doc = @doc (
					value = "get the mental state is in the intention base (if several, returns the first one).",
					returns = "the mental state if it is in the base.",
					examples = { @example ("get_belief(new_predicate(\"has_water\", true))") }))
	public MentalState getIntentionMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, INTENTION_BASE)) {
				if (mental.getMentalState() != null && predicateDirect.equals(mental.getMentalState())) {
					return mental;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the intentions.
	 *
	 * @param scope the scope
	 * @return the intentions
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_intentions",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("name of the predicates to check")) },
			doc = @doc (
					value = "get the list of predicates is in the intention base",
					returns = "the list of intentions.",
					examples = { @example ("get_intentions(\"has_water\")") }))
	public IList<MentalState> getIntentions(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final IList<MentalState> predicates = GamaListFactory.create();
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, INTENTION_BASE)) {
				if (mental.getPredicate() != null && predicateDirect.equals(mental.getPredicate())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the intentions mental state.
	 *
	 * @param scope the scope
	 * @return the intentions mental state
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_intentions_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = false,
					doc = @doc ("mental state to check")) },
			doc = @doc (
					value = "get the list of mental state is in the intention base",
					returns = "the list of intentions.",
					examples = { @example ("get_intentions_mental_state(\"Desire\",predicate1)") }))
	public IList<MentalState> getIntentionsMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final IList<MentalState> predicates = GamaListFactory.create();
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, INTENTION_BASE)) {
				if (mental.getMentalState() != null && predicateDirect.equals(mental.getMentalState())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the intention name.
	 *
	 * @param scope the scope
	 * @return the intention name
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_intention_with_name",
			args = { @arg (
					name = "intention_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the predicate to check")) },
			doc = @doc (
					value = "get the predicates is in the belief base (if several, returns the first one).",
					returns = "the predicate if it is in the base.",
					examples = { @example ("get_intention_with_name(\"has_water\")") }))
	public MentalState getIntentionName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.getTypedArgIfExists("intention_name", IType.STRING);
		if (predicateName != null) {
			for (final MentalState mental : getBase(scope, INTENTION_BASE)) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					return mental;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the intentions name.
	 *
	 * @param scope the scope
	 * @return the intentions name
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_intentions_with_name",
			args = { @arg (
					name = "intention_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the predicates to check")) },
			doc = @doc (
					value = "get the list of predicates is in the belief base with the given name.",
					returns = "the list of predicates.",
					examples = { @example ("get_belief(\"has_water\")") }))
	public List<MentalState> getIntentionsName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.getTypedArgIfExists("intention_name", IType.STRING);
		final List<MentalState> predicates = GamaListFactory.create();
		if (predicateName != null) {
			for (final MentalState mental : getBase(scope, INTENTION_BASE)) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Removes the intention.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeIntention(final IScope scope, final MentalState pred) {
		getBase(scope, INTENTION_BASE).remove(pred);
		for (final MentalState statement : getBase(scope, SimpleBdiArchitecture.INTENTION_BASE)) {
			if (statement.getPredicate() != null) {
				final List<MentalState> statementSubintention = statement.getSubintentions();
				if (statementSubintention != null) {
					if (statementSubintention.contains(pred)) {
						statementSubintention.remove(pred);
					}
				}
				final List<MentalState> statementOnHoldUntil = statement.getOnHoldUntil();
				if (statementOnHoldUntil != null) {
					if (statementOnHoldUntil.contains(pred)) {
						statementOnHoldUntil.remove(pred);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Prim remove intention.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_intention",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("intention's predicate to remove")),
					@arg (
							name = REMOVE_DESIRE_AND_INTENTION,
							type = IType.BOOL,
							optional = true,
							doc = @doc ("removes also desire")) },
			doc = @doc (
					value = "removes the predicates from the intention base.",
					returns = "true if it is removed from the base.",
					examples = { @example ("") }))
	public Boolean primRemoveIntention(final IScope scope) throws GamaRuntimeException {

		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final MentalState temp = new MentalState("Intention", predicateDirect);
		if (predicateDirect == null) {
			return false;
		}
		final Boolean dodesire = scope.getBoolArgIfExists(REMOVE_DESIRE_AND_INTENTION, false);
		if (dodesire) {
			getBase(scope, DESIRE_BASE).remove(temp);
			getBase(scope, OBLIGATION_BASE).remove(temp);
		}
		if (currentIntention(scope) != null && predicateDirect.equals(currentIntention(scope).getPredicate())) {
			scope.getAgent().setAttribute(CURRENT_PLAN, null);
			scope.getAgent().setAttribute(CURRENT_NORM, null);
		}
		for (final MentalState statement : getBase(scope, SimpleBdiArchitecture.INTENTION_BASE)) {
			if (statement.getPredicate() != null) {
				final List<MentalState> statementSubintention = statement.getPredicate().getSubintentions();
				if (statementSubintention != null) {
					if (statementSubintention.contains(temp)) {
						statementSubintention.remove(temp);
					}
				}
				final List<MentalState> statementOnHoldUntil = statement.getPredicate().getOnHoldUntil();
				if (statementOnHoldUntil != null) {
					if (statementOnHoldUntil.contains(temp)) {
						statementOnHoldUntil.remove(temp);
					}
				}
			}
		}
		getBase(scope, INTENTION_BASE).remove(temp);

		return true;
	}

	/**
	 * Prim remove intention mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_intention_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("intention's mental state to remove")),
					@arg (
							name = REMOVE_DESIRE_AND_INTENTION,
							type = IType.BOOL,
							optional = false,
							doc = @doc ("removes also desire")) },
			doc = @doc (
					value = "removes the mental state from the intention base.",
					returns = "true if it is removed from the base.",
					examples = { @example ("") }))
	public Boolean primRemoveIntentionMentalState(final IScope scope) throws GamaRuntimeException {

		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final MentalState temp = new MentalState("Intention", predicateDirect);
		if (predicateDirect != null) {
			final Boolean dodesire =
					scope.hasArg(REMOVE_DESIRE_AND_INTENTION) ? scope.getBoolArg(REMOVE_DESIRE_AND_INTENTION) : false;
			getBase(scope, INTENTION_BASE).remove(temp);
			if (dodesire) {
				getBase(scope, DESIRE_BASE).remove(temp);
			}

			return true;
		}

		return false;
	}

	/**
	 * Prim clear belief.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	@action (
			name = "clear_beliefs",
			doc = @doc (
					value = "clear the belief base",
					returns = "true if the base is cleared correctly",
					examples = { @example ("") }))
	public Boolean primClearBelief(final IScope scope) {
		getBase(scope, BELIEF_BASE).clear();
		return true;
	}

	/**
	 * Prim clear desire.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	@action (
			name = "clear_desires",
			doc = @doc (
					value = "clear the desire base",
					returns = "true if the base is cleared correctly",
					examples = { @example ("") }))
	public Boolean primClearDesire(final IScope scope) {
		getBase(scope, DESIRE_BASE).clear();
		return true;
	}

	/**
	 * Prim clear intention.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	@action (
			name = "clear_intentions",
			doc = @doc (
					value = "clear the intention base",
					returns = "true if the base is cleared correctly",
					examples = { @example ("") }))
	public Boolean primClearIntention(final IScope scope) {
		getBase(scope, INTENTION_BASE).clear();
		scope.getAgent().setAttribute(CURRENT_PLAN, null);
		return true;
	}

	/**
	 * Clear intention.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	public static Boolean clearIntention(final IScope scope) {
		getBase(scope, INTENTION_BASE).clear();
		scope.getAgent().setAttribute(CURRENT_PLAN, null);
		return true;
	}

	/**
	 * Prim remove all belief.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_all_beliefs",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to remove")) },
			doc = @doc (
					value = "removes the predicates from the belief base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveAllBelief(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		if (predicateDirect != null) {
			final MentalState temp = new MentalState("Belief", predicateDirect);
			getBase(scope, BELIEF_BASE).removeAllOccurrencesOfValue(scope, temp);
			return true;
		}
		return false;
	}

	/**
	 * Prim clear emotion.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	@action (
			name = "clear_emotions",
			doc = @doc (
					value = "clear the emotion base",
					returns = "true if the base is cleared correctly",
					examples = { @example ("") }))
	public Boolean primClearEmotion(final IScope scope) {
		getEmotionBase(scope, EMOTION_BASE).clear();
		return true;
	}

	/**
	 * Update emotions intensity.
	 *
	 * @param scope the scope
	 */
	protected void updateEmotionsIntensity(final IScope scope) {
		for (final Emotion emo : getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE)) {
			emo.decayIntensity();
		}
		for (final Emotion emo : listEmotionsNull(scope)) {
			removeFromBase(scope, emo, SimpleBdiArchitecture.EMOTION_BASE);
		}
	}

	/**
	 * Compute emotions.
	 *
	 * @param scope the scope
	 */
	protected void computeEmotions(final IScope scope) {
		// Etape 0, demander à l'utilisateur s'il veut ou non utiliser cette
		// architecture
		// Etape 1, créer les émotions par rapport à la cognition (modèle thèse
		// de Carole). Cette étape va être dissociée d'ici.
		final IAgent agent = getCurrentAgent(scope);
		final Boolean use_emotion_architecture = scope.hasArg(USE_EMOTIONS_ARCHITECTURE)
				? scope.getBoolArg(USE_EMOTIONS_ARCHITECTURE) : (Boolean) agent.getAttribute(USE_EMOTIONS_ARCHITECTURE);
		if (use_emotion_architecture) {
			createEmotionsRelatedToOthers(scope);
		}
	}

	
	/**
	 * Creates the joy from predicate.
	 *
	 * @param scope the scope
	 * @param predTest the pred test
	 */
	// va démarrer le calcul de gratification , remorse, anger et gratitude
	private static void createJoyFromPredicate(final IScope scope, final MentalState predTest) {
		final Boolean use_personality = scope.hasArg(USE_PERSONALITY) ? scope.getBoolArg(USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY);
		if (predTest.getPredicate() != null) {
			if (getBase(scope, SimpleBdiArchitecture.DESIRE_BASE).contains(predTest)) {
				final Emotion joy = new Emotion("joy", predTest.getPredicate());
				final IAgent agentTest = predTest.getPredicate().getAgentCause();
				if (agentTest != null) {
					joy.setAgentCause(agentTest);
				}
				// ajout de l'intensité
				Double intensity = 1.0;
				Double decay = 0.0;
				if (use_personality) {
					final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
					MentalState desire = null;
					for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
						if (mental.getPredicate() != null && predTest.getPredicate().equals(mental.getPredicate())) {
							desire = mental;
						}
					}
					// Faire ce calcul seulement si le désire à une force (vérifier le no value)
					if (desire != null && desire.getStrength() >= 0.0 && predTest.getStrength() >= 0.0) {
						intensity = predTest.getStrength() * desire.getStrength() * (1 + (0.5 - neurotisme));
						if (intensity > 1.0) {
							intensity = 1.0;
						}
						if (intensity < 0) {
							intensity = 0.0;
						}
					}
					// 0.00028=1/3600
					decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * intensity;
				}
				joy.setIntensity(intensity);
				joy.setDecay(decay);
				addEmotion(scope, joy);
				createGratificationGratitudeFromJoy(scope, joy);

			} else {
				for (final MentalState pred : getBase(scope, DESIRE_BASE)) {
					if (pred.getPredicate() != null) {
						if (predTest.getPredicate().equalsButNotTruth(pred.getPredicate())) {
							final Emotion sadness = new Emotion("sadness", predTest.getPredicate());
							final IAgent agentTest = predTest.getPredicate().getAgentCause();
							if (agentTest != null) {
								sadness.setAgentCause(agentTest);
							}
							// ajout de l'intensité
							Double intensity = 1.0;
							Double decay = 0.0;
							if (use_personality) {
								final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
								final MentalState desire = pred;
								// Faire ce calcul seulement si le désire à une force (vérifier le no value)
								if (desire.getStrength() >= 0.0 && predTest.getStrength() >= 0.0) {
									intensity =
											predTest.getStrength() * desire.getStrength() * (1 + (0.5 - neurotisme));
									if (intensity > 1.0) {
										intensity = 1.0;
									}
									if (intensity < 0) {
										intensity = 0.0;
									}
								}
								// 0.00028=1/3600
								decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * intensity;

							}
							sadness.setIntensity(intensity);
							sadness.setDecay(decay);
							addEmotion(scope, sadness);
							createRemorseAngerFromSadness(scope, sadness);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates the hope from mental state.
	 *
	 * @param scope the scope
	 * @param predTest the pred test
	 */
	private static void createHopeFromMentalState(final IScope scope, final MentalState predTest) {
		final Boolean use_personality = scope.hasArg(USE_PERSONALITY) ? scope.getBoolArg(USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY);
		if (predTest.getPredicate() != null) {
			if (getBase(scope, SimpleBdiArchitecture.DESIRE_BASE).contains(predTest)) {
				final Emotion hope = new Emotion("hope", predTest.getPredicate());
				final IAgent agentTest = predTest.getPredicate().getAgentCause();
				if (agentTest != null) {
					hope.setAgentCause(agentTest);
				}
				// ajout de l'intensité
				Double intensity = 1.0;
				Double decay = 0.0;
				if (use_personality) {
					final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
					MentalState desire = null;
					for (final MentalState mental : getBase(scope, DESIRE_BASE)) {
						if (mental.getPredicate() != null && predTest.getPredicate().equals(mental.getPredicate())) {
							desire = mental;
						}
					}
					if (desire != null && desire.getStrength() >= 0.0 && predTest.getStrength() >= 0.0) {
						intensity = predTest.getStrength() * desire.getStrength() * (1 + (0.5 - neurotisme));
						if (intensity > 1.0) {
							intensity = 1.0;
						}
						if (intensity < 0) {
							intensity = 0.0;
						}
					}
					// 0.00028=1/3600
					decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * intensity;
				}
				hope.setIntensity(intensity);
				hope.setDecay(decay);
				addEmotion(scope, hope);

			} else {
				for (final MentalState pred : getBase(scope, DESIRE_BASE)) {
					if (pred.getPredicate() != null) {
						if (predTest.getPredicate().equalsButNotTruth(pred.getPredicate())) {
							final Emotion fear = new Emotion("fear", predTest.getPredicate());
							final IAgent agentTest = predTest.getPredicate().getAgentCause();
							if (agentTest != null) {
								fear.setAgentCause(agentTest);
							}
							// ajout de l'intensité
							Double intensity = 1.0;
							Double decay = 0.0;
							if (use_personality) {
								final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
								final MentalState desire = pred;
								// Faire ce calcul seulement si le désire à une force (vérifier le no value)
								if (desire.getStrength() >= 0.0 && predTest.getStrength() >= 0.0) {
									intensity =
											predTest.getStrength() * desire.getStrength() * (1 + (0.5 - neurotisme));
									if (intensity > 1.0) {
										intensity = 1.0;
									}
									if (intensity < 0) {
										intensity = 0.0;
									}
								}
								// 0.00028=1/3600
								decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * intensity;

							}
							fear.setIntensity(intensity);
							fear.setDecay(decay);
							addEmotion(scope, fear);
						}
					}
				}
			}
		}

	}
	
	/**
	 * Creates the satisfaction from mental state.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 */
	//
	private static void createSatisfactionFromMentalState(final IScope scope, final MentalState predicateDirect) {
		final Boolean use_personality = scope.hasArg(USE_PERSONALITY) ? scope.getBoolArg(USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY);
		if (predicateDirect.getPredicate() != null) {
			final IList<Emotion> emoTemps = getEmotionBase(scope, EMOTION_BASE).copy(scope);
			for (final Emotion emo : emoTemps) {
				if ("hope".equals(emo.getName())) {
					if (emo.getAbout() != null && emo.getAbout().equalsEmotions(predicateDirect.getPredicate())) {
						Emotion satisfaction = null;
						Emotion joy = null;
						final IAgent agentTest = emo.getAgentCause();
						if (!emo.hasIntensity()) {
							satisfaction = new Emotion("satisfaction", emo.getAbout());
							if (agentTest != null) {
								satisfaction.setAgentCause(agentTest);
							}
							joy = new Emotion("joy", emo.getAbout());
							if (agentTest != null) {
								joy.setAgentCause(agentTest);
							}
						} else {
							// On décide de transmettre l'intensité de l'émotion
							// précédente.
							satisfaction = new Emotion("satisfaction", emo.getIntensity(), emo.getAbout());
							if (agentTest != null) {
								satisfaction.setAgentCause(agentTest);
							}
							joy = new Emotion("joy", emo.getIntensity(), emo.getAbout());
							if (agentTest != null) {
								joy.setAgentCause(agentTest);
							}
						}
						Double decay = 0.0;
						if (use_personality) {
							final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
							decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme
									* satisfaction.getIntensity();
						}
						satisfaction.setDecay(decay);
						joy.setDecay(decay);
						addEmotion(scope, satisfaction);
						addEmotion(scope, joy);
						removeEmotion(scope, emo);
					}
					if (emo.getAbout() != null && emo.getAbout().equalsButNotTruth(predicateDirect.getPredicate())) {
						Emotion disappointment = null;
						Emotion sadness = null;
						final IAgent agentTest = emo.getAgentCause();
						if (!emo.hasIntensity()) {
							disappointment = new Emotion("disappointment", emo.getAbout());
							if (agentTest != null) {
								disappointment.setAgentCause(agentTest);
							}
							sadness = new Emotion("sadness", emo.getAbout());
							if (agentTest != null) {
								sadness.setAgentCause(agentTest);
							}
						} else {
							// On décide de transmettre l'intensité de
							// l'émotion précédente.
							disappointment = new Emotion("disappointment", emo.getIntensity(), emo.getAbout());
							if (agentTest != null) {
								disappointment.setAgentCause(agentTest);
							}
							sadness = new Emotion("sadness", emo.getIntensity(), emo.getAbout());
							if (agentTest != null) {
								sadness.setAgentCause(agentTest);
							}
						}
						Double decay = 0.0;
						if (use_personality) {
							final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
							decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme
									* disappointment.getIntensity();

						}
						disappointment.setDecay(decay);
						sadness.setDecay(decay);
						addEmotion(scope, disappointment);
						addEmotion(scope, sadness);
						removeEmotion(scope, emo);
					}
				}
				if ("fear".equals(emo.getName())) {
					if (emo.getAbout() != null && emo.getAbout().equalsEmotions(predicateDirect.getPredicate())) {
						Emotion fearConfirmed = null;
						Emotion sadness = null;
						final IAgent agentTest = emo.getAgentCause();
						if (!emo.hasIntensity()) {
							fearConfirmed = new Emotion("fear_confirmed", emo.getAbout());
							if (agentTest != null) {
								fearConfirmed.setAgentCause(agentTest);
							}
							sadness = new Emotion("sadness", emo.getAbout());
							if (agentTest != null) {
								sadness.setAgentCause(agentTest);
							}
						} else {
							// On décide de transmettre l'intensité de l'émotion
							// précédente.
							fearConfirmed = new Emotion("fear_confirmed", emo.getIntensity(), emo.getAbout());
							if (agentTest != null) {
								fearConfirmed.setAgentCause(agentTest);
							}
							sadness = new Emotion("sadness", emo.getIntensity(), emo.getAbout());
							if (agentTest != null) {
								sadness.setAgentCause(agentTest);
							}
						}
						Double decay = 0.0;
						if (use_personality) {
							final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
							//TODO: decay will be negative, is it normal ?
							decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * fearConfirmed.getIntensity();

						}
						fearConfirmed.setDecay(decay);
						sadness.setDecay(decay);
						addEmotion(scope, fearConfirmed);
						addEmotion(scope, sadness);
						removeEmotion(scope, emo);
					}
					if (emo.getAbout() != null && emo.getAbout().equalsButNotTruth(predicateDirect.getPredicate())) {
						Emotion relief = null;
						Emotion joy = null;
						final IAgent agentTest = emo.getAgentCause();
						if (!emo.hasIntensity()) {
							relief = new Emotion("relief", emo.getAbout());
							if (agentTest != null) {
								relief.setAgentCause(agentTest);
							}
							joy = new Emotion("joy", emo.getAbout());
							if (agentTest != null) {
								joy.setAgentCause(agentTest);
							}
						} else {
							// On décide de transmettre l'intensité de
							// l'émotion précédente.
							relief = new Emotion("relief", emo.getIntensity(), emo.getAbout());
							if (agentTest != null) {
								relief.setAgentCause(agentTest);
							}
							joy = new Emotion("joy", emo.getIntensity(), emo.getAbout());
							if (agentTest != null) {
								joy.setAgentCause(agentTest);
							}
						}
						Double decay = 0.0;
						if (use_personality) {
							final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
							//TODO: decay will be negative, is it normal ?
							decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * relief.getIntensity();

						}
						relief.setDecay(decay);
						joy.setDecay(decay);
						addEmotion(scope, relief);
						addEmotion(scope, joy);
						removeEmotion(scope, emo);
					}
				}
			}
		}
	}

	
	private static String getNewEmotionNameToCreateHappyFor(String emoName, boolean positiveLiking) {
		//TODO: When we switch to jdk 21, use a switch and don't forget to handle null case
		if ("joy".equals(emoName)) {
			return positiveLiking ? "happy_for" : "resentment";
		}
		else if ("sadness".equals(emoName)) {
			return positiveLiking ? "sorry_for" : "gloating";
		}
		return null;
	}

	/**
	 * Creates the happy for from mental state.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 */
	private static void createHappyForFromMentalState(final IScope scope, final MentalState predicateDirect) {
		final boolean use_personality = scope.getBoolArgIfExists(USE_PERSONALITY, (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY));
		if (predicateDirect.getEmotion() == null) {
			return;
		}
		
		final Emotion emo = predicateDirect.getEmotion();
		if ( ! "joy".equals(emo.getName()) && ! "sadness".equals(emo.getName()) ) {
			return;
		}
		
		final IAgent agentTemp = emo.getOwner();
		if (agentTemp == null) {
			return;
		}
		

		for (final SocialLink temp : getSocialBase(scope, SOCIALLINK_BASE)) {
			if (agentTemp.equals(temp.getAgent())) {
				double absLiking = Math.abs(temp.getLiking());
				double signLiking = Math.signum(temp.getLiking());
				String emoName = getNewEmotionNameToCreateHappyFor(emo.getName(), signLiking >0);
				final Emotion emotion = new Emotion(emoName, emo.getAbout(), agentTemp);
				Double intensity = 1.0;
				Double decay = 0.0;
				if (use_personality) {
					final double neurotisme = (double) scope.getAgent().getAttribute(NEUROTISM);
					final double amicability = (double) scope.getAgent().getAttribute(AGREEABLENESS);
					intensity = Math.clamp(emo.getIntensity() * absLiking * (1 - signLiking * (0.5 - amicability)), 0, 1);
					decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * emotion.getIntensity();//TODO: decay will be negative, is it normal ?
				}
				emotion.setIntensity(intensity);
				emotion.setDecay(decay);
				addEmotion(scope, emotion);
			}
		}
	}

	/**
	 * Creates the emotions related to others.
	 *
	 * @param scope the scope
	 */
	private void createEmotionsRelatedToOthers(final IScope scope) {
		// Regroupe le happy_for, sorry_for, resentment et gloating.
		
		
		for (final SocialLink temp : getSocialBase(scope, SOCIALLINK_BASE)) {
			final IAgent agentTemp = temp.getAgent();
			IScope scopeAgentTemp = null;
			if (agentTemp != null) {
				scopeAgentTemp = agentTemp.getScope().copy("in SimpleBdiArchitecture");
				scopeAgentTemp.push(agentTemp);
			}
			double signLiking = Math.signum(temp.getLiking());
			for (final Emotion emo : getEmotionBase(scopeAgentTemp, EMOTION_BASE)) {
				if ( "joy".equals(emo.getName()) || "sadness".equals(emo.getName()) ) {
					final Emotion newEmo = new Emotion(getNewEmotionNameToCreateHappyFor(emo.getName(), signLiking > 0),
							emo.getIntensity() * signLiking * temp.getLiking(), emo.getAbout(),
							//TODO: the previous comments said to change the formula but no indication
							// maybe use the one used in createHappyForFromMentalState (above)?
							agentTemp);
					newEmo.setDecay(0);
					addEmotion(scope, newEmo);
				}
			}
			GAMA.releaseScope(scopeAgentTemp);
		}
	}
	
	
	/**
	 * Creates the pride from mental state.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 */
	private static void createPrideFromMentalState(final IScope scope, final MentalState predicateDirect) {
		final Boolean use_personality = scope.getBoolArgIfExists(USE_PERSONALITY, (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY));
		
		Predicate pred = predicateDirect.getPredicate();
		if (pred == null) {
			return;
		}
		
		for (final MentalState temp : getBase(scope, SimpleBdiArchitecture.IDEAL_BASE).stream().filter(m -> m != null && m.getPredicate().equals(pred)).toList()) {
			double absStrength = Math.abs(temp.getStrength());
			double intensity = 1.0;
			double decay = 0.0;
			if (use_personality) {
				final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
				final Double openness = (Double) scope.getAgent().getAttribute(OPENNESS);
				intensity = Math.clamp(predicateDirect.getStrength() * absStrength * (1 + (0.5 - openness)), 0, 1);
				decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * -1;//TODO: formula obtained while refactoring and unchanged: decay will be negative, is it normal ?
			}
			
			if (temp.getStrength() > 0.0) {
				if (	predicateDirect.getPredicate().getAgentCause() != null
					&& predicateDirect.getPredicate().getAgentCause().equals(scope.getAgent())) {
					addEmotionForCreatingPride(scope, "pride", intensity, decay, predicateDirect.getPredicate(), scope.getAgent());	
				}
				//TODO: if we created a pride, we will also create an admiration, is it something normal ?(should we switch to else if, or keep as is and combine both into nested ifs ?)
				if (predicateDirect.getPredicate().getAgentCause() != null) {
					addEmotionForCreatingPride(scope, "admiration", intensity, decay, predicateDirect.getPredicate(),  predicateDirect.getPredicate().getAgentCause());	
				}
			}
			if (temp.getStrength() < 1.0) {
				if (	predicateDirect.getPredicate().getAgentCause() != null
					&& predicateDirect.getPredicate().getAgentCause().equals(scope.getAgent())) {
					addEmotionForCreatingPride(scope, "shame", intensity, decay, predicateDirect.getPredicate(),  scope.getAgent());
				}
				//TODO: if we created a shame, we will also create a reproach, is it something normal ? (should we switch to else if, or keep as is and combine both into nested ifs ?)
				if (predicateDirect.getPredicate().getAgentCause() != null) {
					addEmotionForCreatingPride(scope, "reproach", intensity, decay, predicateDirect.getPredicate(),  predicateDirect.getPredicate().getAgentCause());				
				}
			}
		}
	}


	private static void addEmotionForCreatingPride(final IScope scope, String name, double intensity, double decay, Predicate about,IAgent cause) {
		final Emotion pride = new Emotion(name, about);
		pride.setAgentCause(cause);
		pride.setIntensity(intensity);
		pride.setDecay(decay);
		addEmotion(scope, pride);
	}

	/**
	 * Creates the gratification gratitude from joy.
	 *
	 * @param scope the scope
	 * @param emo the emo
	 */
	private static void createGratificationGratitudeFromJoy(final IScope scope, final Emotion emo) {
		//TODO: refactor like previous methods
		final Boolean use_personality = scope.getBoolArgIfExists(USE_PERSONALITY, (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY));
		final IList<Emotion> emoTemps = getEmotionBase(scope, EMOTION_BASE).copy(scope);
		for (final Emotion emoTemp : emoTemps) {
			if ("pride".equals(emoTemp.getName())) {
				if (emoTemp.getAbout() != null && emo.getAbout() != null && emo.getAbout().getAgentCause() != null) {
					if (emoTemp.getAbout().equals(emo.getAbout())
							&& emo.getAbout().getAgentCause().equals(scope.getAgent())) {
						final Emotion gratification = new Emotion("gratification", emoTemp.getAbout());
						gratification.setAgentCause(emo.getAgentCause());
						// adding intensity
						Double intensity = 1.0;
						Double decay = 0.0;
						if (use_personality) {
							// update intensity and decay
							final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
							if (emo.hasIntensity() && emoTemp.hasIntensity()) {
								intensity = emo.getIntensity() * emoTemp.getIntensity();
							}
							decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * intensity;
						}
						gratification.setIntensity(intensity);
						gratification.setDecay(decay);
						addEmotion(scope, gratification);
					}
				}
			}
			if ("admiration".equals(emoTemp.getName())) {
				if (emoTemp.getAbout() != null && emo.getAbout() != null && emo.getAbout().getAgentCause() != null
						&& emoTemp.getAbout().getAgentCause() != null) {
					if (emoTemp.getAbout().equals(emo.getAbout())
							&& emo.getAbout().getAgentCause().equals(emoTemp.getAbout().getAgentCause())) {
						final Emotion gratitude = new Emotion("gratitude", emoTemp.getAbout());
						gratitude.setAgentCause(emo.getAgentCause());
						// ajout de l'intensité
						Double intensity = 1.0;
						Double decay = 0.0;
						if (use_personality) {
							final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
							if (emo.hasIntensity() && emoTemp.hasIntensity()) {
								intensity = emo.getIntensity() * emoTemp.getIntensity();
							}
							decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * intensity;
						}
						gratitude.setIntensity(intensity);
						gratitude.setDecay(decay);
						addEmotion(scope, gratitude);
					}
				}
			}
		}
	}

	/**
	 * Creates the remorse anger from sadness.
	 *
	 * @param scope the scope
	 * @param emo the emo
	 */
	private static void createRemorseAngerFromSadness(final IScope scope, final Emotion emo) {
		final Boolean use_personality = scope.hasArg(USE_PERSONALITY) ? scope.getBoolArg(USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY);
		final IList<Emotion> emoTemps = getEmotionBase(scope, EMOTION_BASE).copy(scope);
		for (final Emotion emoTemp : emoTemps) {
			if ("shame".equals(emoTemp.getName())) {
				if (emoTemp.getAbout() != null && emo.getAbout() != null && emo.getAbout().getAgentCause() != null) {
					if (emoTemp.getAbout().equals(emo.getAbout())
							&& emo.getAbout().getAgentCause().equals(scope.getAgent())) {
						final Emotion remorse = new Emotion("remorse", emoTemp.getAbout());
						remorse.setAgentCause(emo.getAgentCause());
						// ajout de l'intensité
						Double intensity = 1.0;
						Double decay = 0.0;
						if (use_personality) {
							final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
							if (emo.hasIntensity() && emoTemp.hasIntensity()) {
								intensity = emo.getIntensity() * emoTemp.getIntensity();
							}
							decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * intensity;
						}
						remorse.setIntensity(intensity);
						remorse.setDecay(decay);
						addEmotion(scope, remorse);
					}
				}
			}
			if ("reproach".equals(emoTemp.getName())) {
				if (emoTemp.getAbout() != null && emo.getAbout() != null && emo.getAbout().getAgentCause() != null
						&& emoTemp.getAbout().getAgentCause() != null) {
					if (emoTemp.getAbout().equals(emo.getAbout())
							&& emo.getAbout().getAgentCause().equals(emoTemp.getAbout().getAgentCause())) {
						final Emotion anger = new Emotion("anger", emoTemp.getAbout());
						anger.setAgentCause(emo.getAgentCause());
						// ajout de l'intensité
						Double intensity = 1.0;
						Double decay = 0.0;
						if (use_personality) {
							final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
							if (emo.hasIntensity() && emoTemp.hasIntensity()) {
								intensity = emo.getIntensity() * emoTemp.getIntensity();
							}
							decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * intensity;
						}
						anger.setIntensity(intensity);
						anger.setDecay(decay);
						addEmotion(scope, anger);
					}
				}
			}
		}
	}


	/**
	 * List emotions null.
	 *
	 * @param scope the scope
	 * @return the list
	 */
	private List<Emotion> listEmotionsNull(final IScope scope) {
		final List<Emotion> tempPred = new ArrayList<>();
		for (final Emotion pred : getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE)) {
			if (pred.getIntensity() <= 0 && pred.getIntensity() != -1.0) {
				tempPred.add(pred);
			}
		}
		return tempPred;
	}

	/**
	 * Prim add emotion.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_emotion",
			args = { @arg (
					name = EMOTION,
					type = EmotionType.EMOTIONTYPE_ID,
					optional = true,
					doc = @doc ("emotion to add to the base")) },
			doc = @doc (
					value = "add the emotion to the emotion base.",
					returns = "true if it is added in the base.",
					examples = { @example ("") }))
	public Boolean primAddEmotion(final IScope scope) throws GamaRuntimeException {
		final Emotion emotionDirect = (Emotion) (scope.hasArg(EMOTION) ? scope.getArg(EMOTION, EmotionType.EMOTIONTYPE_ID) : null);
		return addEmotion(scope, emotionDirect);
	}

	/**
	 * Adds the emotion.
	 *
	 * @param scope the scope
	 * @param emo the emo
	 * @return true, if successful
	 */
	public static boolean addEmotion(final IScope scope, final Emotion emo) {
		Emotion newEmo = emo;
		if (emo.hasIntensity() && hasEmotion(scope, emo)) {
			final Emotion oldEmo = getEmotion(scope, emo);
			if (oldEmo.hasIntensity()) {
				newEmo = new Emotion(emo.getName(), Math.min(1.0, emo.getIntensity() + oldEmo.getIntensity()), emo.getAbout(),
						/* Math.min(emo.getDecay(), oldEmo.getDecay()), */ emo.getAgentCause());
				if (oldEmo.getIntensity() >= emo.getIntensity()) {
					newEmo.setDecay(oldEmo.getDecay());
				} else {
					newEmo.setDecay(emo.getDecay());
				}
			}
		}
		newEmo.setOwner(scope.getAgent());
		return addToBase(scope, newEmo, EMOTION_BASE);
	}

	/**
	 * Prim test emotion.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_emotion",
			args = { @arg (
					name = EMOTION,
					type = EmotionType.EMOTIONTYPE_ID,
					optional = true,
					doc = @doc ("emotion to check")) },
			doc = @doc (
					value = "check if the emotion is in the belief base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestEmotion(final IScope scope) throws GamaRuntimeException {
		final Emotion emotionDirect = (Emotion) (scope.hasArg(EMOTION) ? scope.getArg(EMOTION, EmotionType.EMOTIONTYPE_ID) : null);
		if (emotionDirect != null) { return hasEmotion(scope, emotionDirect); }
		return false;
	}

	/**
	 * Checks for emotion name.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_emotion_with_name",
			args = { @arg (
					name = "emotion_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the emotion to check")) },
			doc = @doc (
					value = "check if the emotion is in the emotion base.",
					returns = "true if it is in the base.",
					examples = { @example ("has_belief_with_name(\"has_water\")") }))
	public Boolean hasEmotionName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.getTypedArgIfExists("emotion_name", IType.STRING);
		if (predicateName != null) {
			final Emotion tempEmo = new Emotion(predicateName);
			return hasEmotion(scope, tempEmo);
		}
		return null;
	}

	/**
	 * Checks for emotion.
	 *
	 * @param scope the scope
	 * @param emo the emo
	 * @return the boolean
	 */
	public static Boolean hasEmotion(final IScope scope, final Emotion emo) {
		return getEmotionBase(scope, EMOTION_BASE).contains(emo);
	}

	/**
	 * Gets the emotion.
	 *
	 * @param scope the scope
	 * @return the emotion
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_emotion",
			args = { @arg (
					name = EMOTION,
					type = EmotionType.EMOTIONTYPE_ID,
					optional = false,
					doc = @doc ("emotion to get")) },
			doc = @doc (
					value = "get the emotion in the emotion base (if several, returns the first one).",
					returns = "the emotion if it is in the base.",
					examples = { @example ("get_belief(new_predicate(\"has_water\", true))") }))
	public Emotion getEmotion(final IScope scope) throws GamaRuntimeException {
		final Emotion emotionDirect = (Emotion) (scope.hasArg(EMOTION) ? scope.getArg(EMOTION, EmotionType.EMOTIONTYPE_ID) : null);
		if (emotionDirect != null) {
			for (final Emotion emo : getEmotionBase(scope, EMOTION_BASE)) {
				if (emotionDirect.equals(emo)) { return emo; }
			}
		}
		return null;
	}

	/**
	 * Gets the emotion name.
	 *
	 * @param scope the scope
	 * @return the emotion name
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_emotion_with_name",
			args = { @arg (
					name = "emotion_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the emotion to check")) },
			doc = @doc (
					value = "get the emotion is in the emotion base (if several, returns the first one).",
					returns = "the emotion if it is in the base.",
					examples = { @example ("get_emotion_with_name(\"fear\")") }))
	public Emotion getEmotionName(final IScope scope) throws GamaRuntimeException {
		final String emotionName = scope.getTypedArgIfExists("emotion_name", IType.STRING);
		if (emotionName != null) {
			for (final Emotion emo : getEmotionBase(scope, EMOTION_BASE)) {
				if (emotionName.equals(emo.getName())) { return emo; }
			}
		}
		return null;
	}

	/**
	 * Gets the emotion from the emotion base that is equal to the one passed as an argument
	 *
	 * @param scope the scope
	 * @param emotionDirect the emotion direct
	 * @return the emotion
	 */
	public static Emotion getEmotion(final IScope scope, final Emotion emotionDirect) {
		for (final Emotion emo : getEmotionBase(scope, EMOTION_BASE)) {
			if (emotionDirect.equals(emo)) { return emo; }
		}
		return null;
	}

	/**
	 * Removes the emotion.
	 *
	 * @param scope the scope
	 * @param emo the emo
	 * @return the boolean
	 */
	public static Boolean removeEmotion(final IScope scope, final Emotion emo) {
		return getEmotionBase(scope, EMOTION_BASE).remove(emo);
	}

	/**
	 * Prim remove emotion.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_emotion",
			args = { @arg (
					name = EMOTION,
					type = EmotionType.EMOTIONTYPE_ID,
					optional = true,
					doc = @doc ("emotion to remove")) },
			doc = @doc (
					value = "removes the emotion from the emotion base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveEmotion(final IScope scope) throws GamaRuntimeException {
		final Emotion emotionDirect = (Emotion) (scope.hasArg(EMOTION) ? scope.getArg(EMOTION, EmotionType.EMOTIONTYPE_ID) : null);
		if (emotionDirect != null) { return removeEmotion(scope, emotionDirect); }
		return false;
	}

	// Peut-être mettre un replace emotion.

	/**
	 * Adds the uncertainty.
	 *
	 * @param scope the scope
	 * @param predicate the predicate
	 * @return the boolean
	 */
	// Déclencher la création des émotions peur et espoir
	public static Boolean addUncertainty(final IScope scope, final MentalState predicate) {
		final Boolean use_emotion_architecture =
				scope.hasArg(USE_EMOTIONS_ARCHITECTURE) ? scope.getBoolArg(USE_EMOTIONS_ARCHITECTURE)
						: (Boolean) scope.getAgent().getAttribute(USE_EMOTIONS_ARCHITECTURE);
		MentalState predTemp = null;
		for (final MentalState predTest : getBase(scope, SimpleBdiArchitecture.BELIEF_BASE)) {
			if (predTest.getPredicate() != null && predicate.getPredicate() != null
					&& predTest.getPredicate().equalsButNotTruth(predicate.getPredicate())) {
				predTemp = predTest;
			}
		}
		if (predTemp != null) {
			removeFromBase(scope, predTemp, BELIEF_BASE);
		}
		for (final MentalState predTest : getBase(scope, SimpleBdiArchitecture.UNCERTAINTY_BASE)) {
			if (predTest.getPredicate() != null && predicate.getPredicate() != null
					&& predTest.getPredicate().equalsButNotTruth(predicate.getPredicate())) {
				predTemp = predTest;
			}
		}
		if (predTemp != null) {
			removeFromBase(scope, predTemp, UNCERTAINTY_BASE);
		}
		if (use_emotion_architecture) {
			createHopeFromMentalState(scope, predicate);
		}
		predicate.setOwner(scope.getAgent());
		return addToBase(scope, predicate, UNCERTAINTY_BASE);
	}

	/**
	 * Prim add uncertainty.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_uncertainty",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to add")),
					@arg (
							name = "strength",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the belief")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the belief")) },
			doc = @doc (
					value = "add a predicate in the uncertainty base.",
					returns = "true it works.",
					examples = { @example ("") }))
	public Boolean primAddUncertainty(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final Double stre = (Double) (scope.hasArg("strength") ? scope.getArg("strength", IType.FLOAT) : null);
		final int life = (int) (scope.hasArg("lifetime") ? scope.getArg("lifetime", IType.INT) : -1);
		MentalState temp;
		if (predicateDirect != null) {
			temp = new MentalState("Uncertainty", predicateDirect);
		} else {
			temp = new MentalState("Uncertainty");
		}
		if (stre != null) {
			temp.setStrength(stre);
		}
		if (life > 0) {
			temp.setLifeTime(life);
		}
		temp.setOwner(scope.getAgent());
		return addUncertainty(scope, temp);

	}

	/**
	 * Prim add directly uncertainty.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_directly_uncertainty",
			args = { @arg (
					name = "uncertainty",
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("uncertainty to add in the uncertainty base")) },
			doc = @doc (
					value = "add the uncertainty in the uncertainty base.",
					returns = "true if it is added in the base.",
					examples = { @example ("") }))
	public Boolean primAddDirectlyUncertainty(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists("uncertainty", MentalStateType.id);
		if (predicateDirect != null && "Uncertainty".equals(predicateDirect.getModality())) {
			predicateDirect.setOwner(scope.getAgent());
			return addUncertainty(scope, predicateDirect);
		}
		return false;

	}

	/**
	 * Prim add uncertainty mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_uncertainty_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("mental state to add as an uncertainty")),
					@arg (
							name = MentalState.STRENGTH,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the belief")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the belief")) },
			doc = @doc (
					value = "add a predicate in the uncertainty base.",
					returns = "true it works.",
					examples = { @example ("") }))
	public Boolean primAddUncertaintyMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState stateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final Double stre = scope.getTypedArgIfExists(MentalState.STRENGTH, IType.FLOAT);
		final int life = scope.getTypedArgIfExists("lifetime", IType.INT, -1);
		MentalState temp;
		if (stateDirect != null) {
			temp = new MentalState("Uncertainty", stateDirect);
		} else {
			temp = new MentalState("Uncertainty");
		}
		if (stre != null) {
			temp.setStrength(stre);
		}
		if (life > 0) {
			temp.setLifeTime(life);
		}
		temp.setOwner(scope.getAgent());
		return addUncertainty(scope, temp);

	}

	/**
	 * Prim add uncertainty emotion.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_uncertainty_emotion",
			args = { @arg (
					name = SimpleBdiArchitecture.EMOTION,
					type = EmotionType.EMOTIONTYPE_ID,
					optional = true,
					doc = @doc ("emotion to add as an uncertainty")),
					@arg (
							name = MentalState.STRENGTH,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the stregth of the belief")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the belief")) },
			doc = @doc (
					value = "add a predicate in the uncertainty base.",
					returns = "true it works.",
					examples = { @example ("") }))
	public Boolean primAddUncertaintyEmotion(final IScope scope) throws GamaRuntimeException {
		final Emotion stateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.EMOTION, EmotionType.EMOTIONTYPE_ID);
		final Double stre = scope.getTypedArgIfExists(MentalState.STRENGTH, IType.FLOAT);
		final int life = scope.getTypedArgIfExists("lifetime", IType.INT, -1);
		MentalState temp;
		if (stateDirect != null) {
			temp = new MentalState("Uncertainty", stateDirect);
		} else {
			temp = new MentalState("Uncertainty");
		}
		if (stre != null) {
			temp.setStrength(stre);
		}
		if (life > 0) {
			temp.setLifeTime(life);
		}
		temp.setOwner(scope.getAgent());
		return addUncertainty(scope, temp);

	}

	/**
	 * Gets the uncertainty.
	 *
	 * @param scope the scope
	 * @return the uncertainty
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_uncertainty",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("predicate to return")) },
			doc = @doc (
					value = "get the predicates is in the uncertainty base (if several, returns the first one).",
					returns = "the uncertainty (mental state) if it is in the base.",
					examples = { @example ("get_uncertainty(new_predicate(\"has_water\", true))") }))
	public MentalState getUncertainty(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		if (predicateDirect != null) {
			for (final MentalState pred : getBase(scope, UNCERTAINTY_BASE)) {
				if (pred.getPredicate() != null && predicateDirect.equals(pred.getPredicate())) { return pred; }
			}
		}
		return null;
	}

	/**
	 * Gets the uncertainty mental state.
	 *
	 * @param scope the scope
	 * @return the uncertainty mental state
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_uncertainty_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = false,
					doc = @doc ("mental state to return")) },
			doc = @doc (
					value = "get the mental state is in the uncertainty base (if several, returns the first one).",
					returns = "the mental state if it is in the base.",
					examples = { @example ("get_uncertainty(new_predicate(\"has_water\", true))") }))
	public MentalState getUncertaintyMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		if (predicateDirect != null) {
			for (final MentalState pred : getBase(scope, UNCERTAINTY_BASE)) {
				if (pred.getMentalState() != null && predicateDirect.equals(pred.getMentalState())) { return pred; }
			}
		}
		return null;
	}

	/**
	 * Checks for uncertainty.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	public static Boolean hasUncertainty(final IScope scope, final MentalState predicateDirect) {
		return getBase(scope, UNCERTAINTY_BASE).contains(predicateDirect);
	}

	/**
	 * Prim test uncertainty.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_uncertainty",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to check")) },
			doc = @doc (
					value = "check if the predicates is in the uncertainty base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestUncertainty(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final MentalState temp = new MentalState("Uncertainty", predicateDirect);
		if (predicateDirect != null) { return hasUncertainty(scope, temp); }
		return false;
	}

	/**
	 * Checks for uncertainty name.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_uncertainty_with_name",
			args = { @arg (
					name = "uncertainty_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the uncertainty to check")) },
			doc = @doc (
					value = "check if the predicate is in the uncertainty base.",
					returns = "true if it is in the base.",
					examples = { @example ("has_uncertainty_with_name(\"has_water\")") }))
	public Boolean hasUncertaintyName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.getTypedArgIfExists("uncertainty_name", IType.STRING);
		if (predicateName != null) {
			final MentalState tempState = new MentalState("Uncertainty", new Predicate(predicateName));
			return hasUncertainty(scope, tempState);
		}
		return null;
	}

	/**
	 * Prim test uncertainty mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_uncertainty_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("mental state to check")) },
			doc = @doc (
					value = "check if the mental state is in the uncertainty base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestUncertaintyMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final MentalState temp = new MentalState("Uncertainty", predicateDirect);
		if (predicateDirect != null) { return hasUncertainty(scope, temp); }
		return false;
	}

	/**
	 * Removes the uncertainty.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeUncertainty(final IScope scope, final MentalState pred) {
		return getBase(scope, UNCERTAINTY_BASE).remove(pred);
	}

	/**
	 * Prim remove uncertainty.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_uncertainty",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to remove")) },
			doc = @doc (
					value = "removes the predicates from the uncertainty base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveUncertainty(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final MentalState temp = new MentalState("Uncertainty", predicateDirect);
		if (predicateDirect != null) { return removeUncertainty(scope, temp); }
		return false;
	}

	/**
	 * Prim remove uncertainty mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_uncertainty_mental_state",
			args = { @arg (
					name = MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("mental state to remove")) },
			doc = @doc (
					value = "removes the mental state from the uncertainty base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveUncertaintyMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.<MentalState>getTypedArgIfExists(MENTAL_STATE, MentalStateType.id);
		final MentalState temp = new MentalState("Uncertainty", predicateDirect);
		if (predicateDirect != null) { return removeUncertainty(scope, temp); }
		return false;
	}

	// Peut-être mettre plus tard un replace Uncertainty

	/**
	 * Prim clear uncertainty.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	@action (
			name = "clear_uncertainties",
			doc = @doc (
					value = "clear the uncertainty base",
					returns = "true if the base is cleared correctly",
					examples = { @example ("") }))
	public Boolean primClearUncertainty(final IScope scope) {
		getBase(scope, UNCERTAINTY_BASE).clear();
		return true;
	}

	/**
	 * Adds the ideal.
	 *
	 * @param scope the scope
	 * @param predicate the predicate
	 * @return the boolean
	 */
	public static Boolean addIdeal(final IScope scope, final MentalState predicate) {
		predicate.setOwner(scope.getAgent());
		return addToBase(scope, predicate, IDEAL_BASE);
	}

	/**
	 * Prim add ideal.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_ideal",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to add as an ideal")),
					@arg (
							name = "praiseworthiness",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the praiseworthiness value of the ideal")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the ideal")) },
			doc = @doc (
					value = "add a predicate in the ideal base.",
					returns = "true it works.",
					examples = { @example ("") }))
	public Boolean primAddIdeal(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final Double stre =
				(Double) (scope.hasArg("praiseworthiness") ? scope.getArg("praiseworthiness", IType.FLOAT) : null);
		final int life = (int) (scope.hasArg("lifetime") ? scope.getArg("lifetime", IType.INT) : -1);
		MentalState temp;
		if (predicateDirect != null) {
			temp = new MentalState("Ideal", predicateDirect);
		} else {
			temp = new MentalState();
		}
		if (stre != null) {
			temp.setStrength(stre);
		}
		if (life > 0) {
			temp.setLifeTime(life);
		}
		temp.setOwner(scope.getAgent());
		return addIdeal(scope, temp);
	}

	/**
	 * Prim add directly ideal.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_directly_ideal",
			args = { @arg (
					name = "ideal",
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("ideal to add in the ideal base")) },
			doc = @doc (
					value = "add the ideal in the ideal base.",
					returns = "true if it is added in the base.",
					examples = { @example ("") }))
	public Boolean primAddDirectlyIdeal(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists("ideal", MentalStateType.id);
		if (predicateDirect != null && "Ideal".equals(predicateDirect.getModality())) {
			predicateDirect.setOwner(scope.getAgent());
			return addIdeal(scope, predicateDirect);
		}
		return false;

	}

	/**
	 * Prim add ideal mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_ideal_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("mental state to add as an ideal")),
					@arg (
							name = "praiseworthiness",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the praiseworthiness value of the ideal")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the ideal")) },
			doc = @doc (
					value = "add a predicate in the ideal base.",
					returns = "true it works.",
					examples = { @example ("") }))
	public Boolean primAddIdealMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState stateDirect = scope.getTypedArgIfExists(MENTAL_STATE, MentalStateType.id);
		final Double stre = scope.getTypedArgIfExists("praiseworthiness", IType.FLOAT);
		final int life = scope.getTypedArgIfExists("lifetime", IType.INT, -1);
		MentalState temp;
		if (stateDirect != null) {
			temp = new MentalState("Ideal", stateDirect);
		} else {
			temp = new MentalState();
		}
		if (stre != null) {
			temp.setStrength(stre);
		}
		if (life > 0) {
			temp.setLifeTime(life);
		}
		temp.setOwner(scope.getAgent());
		return addIdeal(scope, temp);
	}

	/**
	 * Prim add ideal emotion.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_ideal_emotion",
			args = { @arg (
					name = SimpleBdiArchitecture.EMOTION,
					type = EmotionType.EMOTIONTYPE_ID,
					optional = true,
					doc = @doc ("emotion to add as an ideal")),
					@arg (
							name = "praiseworthiness",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the praiseworthiness value of the ideal")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the ideal")) },
			doc = @doc (
					value = "add a predicate in the ideal base.",
					returns = "true it works.",
					examples = { @example ("") }))
	public Boolean primAddIdealEmotion(final IScope scope) throws GamaRuntimeException {
		final Emotion stateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.EMOTION, EmotionType.EMOTIONTYPE_ID);
		final Double stre = scope.getTypedArgIfExists("praiseworthiness", IType.FLOAT);
		final int life = scope.getTypedArgIfExists("lifetime", IType.INT, -1);
		MentalState temp;
		if (stateDirect != null) {
			temp = new MentalState("Ideal", stateDirect);
		} else {
			temp = new MentalState();
		}
		if (stre != null) {
			temp.setStrength(stre);
		}
		if (life > 0) {
			temp.setLifeTime(life);
		}
		temp.setOwner(scope.getAgent());
		return addIdeal(scope, temp);
	}

	/**
	 * Gets the ideal.
	 *
	 * @param scope the scope
	 * @return the ideal
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_ideal",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("predicate to check ad an ideal")) },
			doc = @doc (
					value = "get the ideal about the predicate in the ideal base (if several, returns the first one).",
					returns = "the ideal if it is in the base.",
					examples = { @example ("get_ideal(new_predicate(\"has_water\", true))") }))
	public MentalState getIdeal(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		if (predicateDirect != null) {
			for (final MentalState pred : getBase(scope, IDEAL_BASE)) {
				if (pred.getPredicate() != null && predicateDirect.equals(pred.getPredicate())) { return pred; }
			}
		}
		return null;
	}

	/**
	 * Gets the ideal mental state.
	 *
	 * @param scope the scope
	 * @return the ideal mental state
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_ideal_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = false,
					doc = @doc ("mental state to return")) },
			doc = @doc (
					value = "get the mental state in the ideal base (if several, returns the first one).",
					returns = "the ideal (mental state) if it is in the base.",
					examples = { @example ("get_ideal(new_predicate(\"has_water\", true))") }))
	public MentalState getIdealMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		if (predicateDirect != null) {
			for (final MentalState pred : getBase(scope, IDEAL_BASE)) {
				if (pred.getMentalState() != null && predicateDirect.equals(pred.getMentalState())) { return pred; }
			}
		}
		return null;
	}

	/**
	 * Checks for ideal.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	public static Boolean hasIdeal(final IScope scope, final MentalState predicateDirect) {
		return getBase(scope, IDEAL_BASE).contains(predicateDirect);
	}

	/**
	 * Prim test ideal.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_ideal",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to check")) },
			doc = @doc (
					value = "check if the predicates is in the ideal base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestIdeal(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final MentalState temp = new MentalState("Ideal", predicateDirect);
		if (predicateDirect != null) { return hasIdeal(scope, temp); }
		return false;
	}

	/**
	 * Checks for ideal name.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_ideal_with_name",
			args = { @arg (
					name = "ideal_name",
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the predicate to check")) },
			doc = @doc (
					value = "check if the predicate is in the ideal base.",
					returns = "true if it is in the base.",
					examples = { @example ("has_belief_with_name(\"has_water\")") }))
	public Boolean hasIdealName(final IScope scope) throws GamaRuntimeException {
		final String predicateName = scope.getTypedArgIfExists("ideal_name", IType.STRING);
		if (predicateName != null) {
			final MentalState tempState = new MentalState("Ideal", new Predicate(predicateName));
			return hasIdeal(scope, tempState);
		}
		return null;
	}

	/**
	 * Prim test ideal mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_ideal_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("mental state to check")) },
			doc = @doc (
					value = "check if the mental state is in the ideal base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestIdealMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final MentalState temp = new MentalState("Ideal", predicateDirect);
		if (predicateDirect != null) { return hasIdeal(scope, temp); }
		return false;
	}

	/**
	 * Removes the ideal.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeIdeal(final IScope scope, final MentalState pred) {
		return getBase(scope, IDEAL_BASE).remove(pred);
	}

	/**
	 * Prim remove ideal.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_ideal",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to remove")) },
			doc = @doc (
					value = "removes the predicates from the ideal base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveIdeal(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final MentalState temp = new MentalState("Ideal", predicateDirect);
		if (predicateDirect != null) { return removeIdeal(scope, temp); }
		return false;
	}

	/**
	 * Prim remove ideal mental state.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_ideal_mental_state",
			args = { @arg (
					name = SimpleBdiArchitecture.MENTAL_STATE,
					type = MentalStateType.id,
					optional = true,
					doc = @doc ("metal state to remove")) },
			doc = @doc (
					value = "removes the mental state from the ideal base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveIdealMentalState(final IScope scope) throws GamaRuntimeException {
		final MentalState predicateDirect = scope.getTypedArgIfExists(SimpleBdiArchitecture.MENTAL_STATE, MentalStateType.id);
		final MentalState temp = new MentalState("Ideal", predicateDirect);
		if (predicateDirect != null) { return removeIdeal(scope, temp); }
		return false;
	}

	/**
	 * Prim clear ideal.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	@action (
			name = "clear_ideals",
			doc = @doc (
					value = "clear the ideal base",
					returns = "true if the base is cleared correctly",
					examples = { @example ("") }))
	public Boolean primClearIdeal(final IScope scope) {
		getBase(scope, IDEAL_BASE).clear();
		return true;
	}

	/**
	 * Adds the obligation.
	 *
	 * @param scope the scope
	 * @param predicate the predicate
	 * @return the boolean
	 */
	public static Boolean addObligation(final IScope scope, final MentalState predicate) {
		predicate.setOwner(scope.getAgent());
		clearIntention(scope);
		final IAgent agent = scope.getAgent();
		agent.setAttribute(SimpleBdiArchitecture.CURRENT_PLAN, null);
		return addToBase(scope, predicate, OBLIGATION_BASE);
	}

	/**
	 * Prim add obligation.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_obligation",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to add as an obligation")),
					@arg (
							name = "strength",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the strength value of the obligation")),
					@arg (
							name = "lifetime",
							type = IType.INT,
							optional = true,
							doc = @doc ("the lifetime of the obligation")) },
			doc = @doc (
					value = "add a predicate in the ideal base.",
					returns = "true it works.",
					examples = { @example ("") }))
	public Boolean primAddObligation(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final Double stre = (Double) (scope.hasArg("strength") ? scope.getArg("strength", IType.FLOAT) : null);
		final int life = (int) (scope.hasArg("lifetime") ? scope.getArg("lifetime", IType.INT) : -1);
		MentalState temp;
		if (predicateDirect != null) {
			temp = new MentalState("Obligation", predicateDirect);
		} else {
			temp = new MentalState();
		}
		if (stre != null) {
			temp.setStrength(stre);
		}
		if (life > 0) {
			temp.setLifeTime(life);
		}
		temp.setOwner(scope.getAgent());
		return addObligation(scope, temp);
	}

	/**
	 * Gets the obligation.
	 *
	 * @param scope the scope
	 * @return the obligation
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_obligation",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = false,
					doc = @doc ("predicate to return")) },
			doc = @doc (
					value = "get the predicates in the obligation base (if several, returns the first one).",
					returns = "the obligation (mental state) if it is in the base.",
					examples = { @example ("get_obligation(new_predicate(\"has_water\", true))") }))
	public MentalState getObligation(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		if (predicateDirect != null) {
			for (final MentalState pred : getBase(scope, OBLIGATION_BASE)) {
				if (pred.getPredicate() != null && predicateDirect.equals(pred.getPredicate())) { return pred; }
			}
		}
		return null;
	}

	/**
	 * Checks for obligation.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	public static Boolean hasObligation(final IScope scope, final MentalState predicateDirect) {
		return getBase(scope, OBLIGATION_BASE).contains(predicateDirect);
	}

	/**
	 * Prim test obligation.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_obligation",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to check")) },
			doc = @doc (
					value = "check if the predicates is in the obligation base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestObligation(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final MentalState temp = new MentalState("Obligation", predicateDirect);
		if (predicateDirect != null) { return hasObligation(scope, temp); }
		return false;
	}

	/**
	 * Removes the obligation.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeObligation(final IScope scope, final MentalState pred) {
		return getBase(scope, OBLIGATION_BASE).remove(pred);
	}

	/**
	 * Prim remove obligation.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_obligation",
			args = { @arg (
					name = PREDICATE,
					type = PredicateType.id,
					optional = true,
					doc = @doc ("predicate to remove")) },
			doc = @doc (
					value = "removes the predicates from the obligation base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveObligation(final IScope scope) throws GamaRuntimeException {
		final Predicate predicateDirect =
				(Predicate) (scope.hasArg(PREDICATE) ? scope.getArg(PREDICATE, PredicateType.id) : null);
		final MentalState temp = new MentalState("Obligation", predicateDirect);
		if (predicateDirect != null) { return removeObligation(scope, temp); }
		return false;
	}

	/**
	 * Prim clear obligation.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	@action (
			name = "clear_obligations",
			doc = @doc (
					value = "clear the obligation base",
					returns = "true if the base is cleared correctly",
					examples = { @example ("") }))
	public Boolean primClearObligation(final IScope scope) {
		getBase(scope, OBLIGATION_BASE).clear();
		return true;
	}

	/**
	 * Prim add social link.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "add_social_link",
			args = { @arg (
					name = SOCIALLINK,
					type = SocialLinkType.id,
					optional = true,
					doc = @doc ("social link to add to the base")) },
			doc = @doc (
					value = "add the social link to the social link base.",
					returns = "true if it is added in the base.",
					examples = { @example ("") }))
	public Boolean primAddSocialLink(final IScope scope) throws GamaRuntimeException {
		final SocialLink social =
				(SocialLink) (scope.hasArg(SOCIALLINK) ? scope.getArg(SOCIALLINK, SocialLinkType.id) : null);
		return addSocialLink(scope, social);
	}

	/**
	 * Adds the social link.
	 *
	 * @param scope the scope
	 * @param social the social
	 * @return true, if successful
	 */
	public static boolean addSocialLink(final IScope scope, final SocialLink social) {
		if (social.getLiking() >= -1.0 && social.getLiking() <= 1.0) {
			if (social.getDominance() >= -1.0 && social.getDominance() <= 1.0) {
				if (social.getSolidarity() >= 0.0 && social.getSolidarity() <= 1.0) {
					if (social.getFamiliarity() >= 0.0 && social.getFamiliarity() <= 1.0) {
						if (getSocialLink(scope, social) == null) { return addToBase(scope, social, SOCIALLINK_BASE); }
					}
				}
			}
		}
		return false;
	}

	/**
	 * Gets the social link.
	 *
	 * @param scope the scope
	 * @return the social link
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_social_link",
			args = { @arg (
					name = SOCIALLINK,
					type = SocialLinkType.id,
					optional = false,
					doc = @doc ("social link to check")) },
			doc = @doc (
					value = "get the social link (if several, returns the first one).",
					returns = "the social link if it is in the base.",
					examples = { @example ("get_social_link(new_social_link(agentA))") }))
	public SocialLink getSocialLink(final IScope scope) throws GamaRuntimeException {
		final SocialLink socialDirect =
				(SocialLink) (scope.hasArg(SOCIALLINK) ? scope.getArg(SOCIALLINK, SocialLinkType.id) : null);
		if (socialDirect != null) { return getSocialLink(scope, socialDirect); }
		return null;
	}

	/**
	 * Gets the social link with agent.
	 *
	 * @param scope the scope
	 * @return the social link with agent
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "get_social_link_with_agent",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("an agent with who I get a social link")) },
			doc = @doc (
					value = "get the social link with the agent concerned (if several, returns the first one).",
					returns = "the social link if it is in the base.",
					examples = { @example ("get_social_link_with_agent(agentA)") }))
	public SocialLink getSocialLinkWithAgent(final IScope scope) throws GamaRuntimeException {
		final IAgent agentDirect = (IAgent) (scope.hasArg("agent") ? scope.getArg("agent", IType.AGENT) : null);
		if (agentDirect != null) { return getSocialLink(scope, new SocialLink(agentDirect)); }
		return null;
	}

	/**
	 * Gets the social link.
	 *
	 * @param scope the scope
	 * @param social the social
	 * @return the social link
	 */
	public static SocialLink getSocialLink(final IScope scope, final SocialLink social) {
		for (final SocialLink socialLink : getSocialBase(scope, SOCIALLINK_BASE)) {
			if (socialLink.equals(social)) { return socialLink; }
			if (socialLink.equalsInAgent(social)) { return socialLink; }
		}
		return null;
	}

	/**
	 * Checks for social link.
	 *
	 * @param scope the scope
	 * @param socialDirect the social direct
	 * @return the boolean
	 */
	public static Boolean hasSocialLink(final IScope scope, final SocialLink socialDirect) {
		return getSocialBase(scope, SOCIALLINK_BASE).contains(socialDirect);
	}

	/**
	 * Prim test social with agent.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_social_link",
			args = { @arg (
					name = SOCIALLINK,
					type = SocialLinkType.id,
					optional = true,
					doc = @doc ("social link to check")) },
			doc = @doc (
					value = "check if the social link base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestSocialWithAgent(final IScope scope) throws GamaRuntimeException {
		final SocialLink socialDirect =
				(SocialLink) (scope.hasArg(SOCIALLINK) ? scope.getArg(SOCIALLINK, SocialLinkType.id) : null);
		if (socialDirect != null) { return hasSocialLink(scope, socialDirect); }
		return false;
	}

	/**
	 * Prim test social.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "has_social_link_with_agent",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = true,
					doc = @doc ("an agent with who I want to check if I have a social link")) },
			doc = @doc (
					value = "check if the social link base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primTestSocial(final IScope scope) throws GamaRuntimeException {
		final IAgent agentDirect = (IAgent) (scope.hasArg("agent") ? scope.getArg("agent", IType.AGENT) : null);
		if (agentDirect != null) { return hasSocialLink(scope, new SocialLink(agentDirect)); }
		return false;
	}

	/**
	 * Removes the social link.
	 *
	 * @param scope the scope
	 * @param socialDirect the social direct
	 * @return the boolean
	 */
	public static Boolean removeSocialLink(final IScope scope, final SocialLink socialDirect) {
		return getSocialBase(scope, SOCIALLINK_BASE).remove(socialDirect);
	}

	/**
	 * Prim remove social link with agent.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_social_link",
			args = { @arg (
					name = SOCIALLINK,
					type = SocialLinkType.id,
					optional = true,
					doc = @doc ("social link to remove")) },
			doc = @doc (
					value = "removes the social link from the social relation base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveSocialLinkWithAgent(final IScope scope) throws GamaRuntimeException {
		final SocialLink socialDirect =
				(SocialLink) (scope.hasArg(SOCIALLINK) ? scope.getArg(SOCIALLINK, SocialLinkType.id) : null);
		if (socialDirect != null) { return removeSocialLink(scope, socialDirect); }
		return false;
	}

	/**
	 * Prim remove social link.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "remove_social_link_with_agent",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = true,
					doc = @doc ("an agent with who I get the social link to remove")) },
			doc = @doc (
					value = "removes the social link from the social relation base.",
					returns = "true if it is in the base.",
					examples = { @example ("") }))
	public Boolean primRemoveSocialLink(final IScope scope) throws GamaRuntimeException {
		final IAgent agentDirect = (IAgent) (scope.hasArg("agent") ? scope.getArg("agent", IType.AGENT) : null);
		if (agentDirect != null) { return removeSocialLink(scope, new SocialLink(agentDirect)); }
		return false;
	}

	/**
	 * Prim clear social links.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	@action (
			name = "clear_social_links",
			doc = @doc (
					value = "clear the intention base",
					returns = "true if the base is cleared correctly",
					examples = { @example ("") }))
	public Boolean primClearSocialLinks(final IScope scope) {
		getSocialBase(scope, SOCIALLINK_BASE).clear();
		return true;
	}

	/**
	 * Prim change liking.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "change_liking",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = true,
					doc = @doc ("an agent with who I get a social link")),
					@arg (
							name = "liking",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("a value to change the liking value")) },
			doc = @doc (
					value = "changes the liking value of the social relation with the agent specified.",
					returns = "true if it worked.",
					examples = { @example ("") }))
	public Boolean primChangeLiking(final IScope scope) throws GamaRuntimeException {
		final IAgent agentDirect = (IAgent) (scope.hasArg("agent") ? scope.getArg("agent", IType.AGENT) : null);
		final Double likingDirect = (Double) (scope.hasArg("liking") ? scope.getArg("liking", IType.FLOAT) : 0.0);
		if (agentDirect != null) {
			final SocialLink tempSocial = getSocialLink(scope, new SocialLink(agentDirect));
			if (tempSocial != null) {
				tempSocial.setLiking(tempSocial.getLiking() + likingDirect);
				if (tempSocial.getLiking() > 1.0) {
					tempSocial.setLiking(1.0);
				}
				if (tempSocial.getLiking() < -1.0) {
					tempSocial.setLiking(-1.0);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Prim change dominance.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "change_dominance",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = true,
					doc = @doc ("an agent with who I get a social link")),
					@arg (
							name = "dominance",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("a value to change the dominance value")) },
			doc = @doc (
					value = "changes the dominance value of the social relation with the agent specified.",
					returns = "true if it worked.",
					examples = { @example ("") }))
	public Boolean primChangeDominance(final IScope scope) throws GamaRuntimeException {
		final IAgent agentDirect = (IAgent) (scope.hasArg("agent") ? scope.getArg("agent", IType.AGENT) : null);
		final Double dominanceDirect =
				(Double) (scope.hasArg("dominance") ? scope.getArg("dominance", IType.FLOAT) : 0.0);
		if (agentDirect != null) {
			final SocialLink tempSocial = getSocialLink(scope, new SocialLink(agentDirect));
			if (tempSocial != null) {
				tempSocial.setDominance(tempSocial.getDominance() + dominanceDirect);
				if (tempSocial.getDominance() > 1.0) {
					tempSocial.setDominance(1.0);
				}
				if (tempSocial.getDominance() < -1.0) {
					tempSocial.setDominance(-1.0);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Prim change solidarity.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "change_solidarity",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = true,
					doc = @doc ("an agent with who I get a social link")),
					@arg (
							name = "solidarity",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("a value to change the solidarity value")) },
			doc = @doc (
					value = "changes the solidarity value of the social relation with the agent specified.",
					returns = "true if it worked.",
					examples = { @example ("") }))
	public Boolean primChangeSolidarity(final IScope scope) throws GamaRuntimeException {
		final IAgent agentDirect = (IAgent) (scope.hasArg("agent") ? scope.getArg("agent", IType.AGENT) : null);
		final Double solidarityDirect =
				(Double) (scope.hasArg("solidarity") ? scope.getArg("solidarity", IType.FLOAT) : 0.0);
		if (agentDirect != null) {
			final SocialLink tempSocial = getSocialLink(scope, new SocialLink(agentDirect));
			if (tempSocial != null) {
				tempSocial.setSolidarity(tempSocial.getSolidarity() + solidarityDirect);
				if (tempSocial.getSolidarity() > 1.0) {
					tempSocial.setSolidarity(1.0);
				}
				if (tempSocial.getSolidarity() < 0.0) {
					tempSocial.setSolidarity(-1.0);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Prim change familiarity.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "change_familiarity",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = true,
					doc = @doc ("an agent with who I get a social link")),
					@arg (
							name = "familiarity",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("a value to change the familiarity value")) },
			doc = @doc (
					value = "changes the familiarity value of the social relation with the agent specified.",
					returns = "true if it worked.",
					examples = { @example ("") }))
	public Boolean primChangeFamiliarity(final IScope scope) throws GamaRuntimeException {
		final IAgent agentDirect = (IAgent) (scope.hasArg("agent") ? scope.getArg("agent", IType.AGENT) : null);
		final Double familiarityDirect =
				(Double) (scope.hasArg("familiarity") ? scope.getArg("familiarity", IType.FLOAT) : 0.0);
		if (agentDirect != null) {
			final SocialLink tempSocial = getSocialLink(scope, new SocialLink(agentDirect));
			if (tempSocial != null) {
				tempSocial.setFamiliarity(tempSocial.getFamiliarity() + familiarityDirect);
				if (tempSocial.getFamiliarity() > 1.0) {
					tempSocial.setFamiliarity(1.0);
				}
				if (tempSocial.getFamiliarity() < 0.0) {
					tempSocial.setFamiliarity(0.0);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Prim change trust.
	 *
	 * @param scope the scope
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@action (
			name = "change_trust",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = true,
					doc = @doc ("an agent with who I get a social link")),
					@arg (
							name = "trust",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("a value to change the trust value")) },
			doc = @doc (
					value = "changes the trust value of the social relation with the agent specified.",
					returns = "true if it worked.",
					examples = { @example ("") }))
	public Boolean primChangeTrust(final IScope scope) throws GamaRuntimeException {
		final IAgent agentDirect = (IAgent) (scope.hasArg("agent") ? scope.getArg("agent", IType.AGENT) : null);
		final Double trustDirect = (Double) (scope.hasArg("trust") ? scope.getArg("trust", IType.FLOAT) : 0.0);
		if (agentDirect != null) {
			final SocialLink tempSocial = getSocialLink(scope, new SocialLink(agentDirect));
			if (tempSocial != null) {
				double trust = Math.min(1.0, Math.max(-1.0, tempSocial.getTrust() + trustDirect));
				tempSocial.setTrust(trust);
				return true;
			}
		}
		return false;
	}

	/**
	 * List social agent dead.
	 *
	 * @param scope the scope
	 * @return the list
	 */
	private List<SocialLink> listSocialAgentDead(final IScope scope) {
		final List<SocialLink> tempPred = new ArrayList<>();
		for (final SocialLink pred : getSocialBase(scope, SimpleBdiArchitecture.SOCIALLINK_BASE)) {
			if (pred.getAgent().dead()) {
				tempPred.add(pred);
			}
		}
		return tempPred;
	}

	/**
	 * Update social links.
	 *
	 * @param scope the scope
	 */
	protected void updateSocialLinks(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		final Boolean use_social_architecture = scope.hasArg(USE_SOCIAL_ARCHITECTURE)
				? scope.getBoolArg(USE_SOCIAL_ARCHITECTURE) : (Boolean) agent.getAttribute(USE_SOCIAL_ARCHITECTURE);
		if (use_social_architecture) {
			for (final SocialLink tempLink : listSocialAgentDead(scope)) {
				removeFromBase(scope, tempLink, SimpleBdiArchitecture.SOCIALLINK_BASE);
			}
			// for (final SocialLink tempLink : getSocialBase(scope, SOCIALLINK_BASE)) {
			// updateSocialLink(scope, tempLink);
			// }
		}
	}

	/**
	 * Update social link.
	 *
	 * @param scope the scope
	 * @param social the social
	 */
	static public void updateSocialLink(final IScope scope, final SocialLink social) {
		updateAppreciation(scope, social);
		updateDominance(scope, social);
		updateSolidarity(scope, social);
		updateFamiliarity(scope, social);
	}

	// Lier les coeffiscient à la personnalité

	/**
	 * Update appreciation.
	 *
	 * @param scope the scope
	 * @param social the social
	 */
	static private void updateAppreciation(final IScope scope, final SocialLink social) {
		final Boolean use_personality = scope.getBoolArgIfExists(USE_PERSONALITY, (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY));
		final IAgent agentCause = social.getAgent();
		Double tempPositif = 0.0;
		Double moyPositif = 0.0;
		Double tempNegatif = 0.0;
		Double moyNegatif = 0.0;
		Double coefModification = 0.1;
		if (use_personality) {
			final Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
			coefModification = 1 - neurotisme;
		}
		Double appreciationModif = social.getLiking();
		for (final Emotion emo : getEmotionBase(scope, EMOTION_BASE)) {
			if (emo.getAgentCause() != null && emo.getAgentCause().equals(agentCause)) {
				if ("joy".equals(emo.getName()) || "hope".equals(emo.getName())) {
					tempPositif = tempPositif + 1.0;
					moyPositif = moyPositif + emo.getIntensity();
				}
				if ("sadness".equals(emo.getName()) || "fear".equals(emo.getName())) {
					tempNegatif = tempNegatif + 1.0;
					moyNegatif = moyNegatif + emo.getIntensity();
				}
			}
		}
		moyPositif = tempPositif != 0.0 ? moyPositif / tempPositif : 0;
		moyNegatif = tempNegatif != 0.0 ? moyNegatif / tempNegatif : 0;

		appreciationModif = Math.clamp(appreciationModif 
							+ Maths.abs(appreciationModif) * (1 - Maths.abs(appreciationModif)) * social.getSolidarity()
							+ coefModification * (1 - Maths.abs(appreciationModif)) * (moyPositif - moyNegatif),
							-1,
							1);
		social.setLiking(appreciationModif);
	}

	/**
	 * Update dominance.
	 *
	 * @param scope the scope
	 * @param social the social
	 */
	static private void updateDominance(final IScope scope, final SocialLink social) {
		final Boolean use_personality = scope.getBoolArgIfExists(USE_PERSONALITY, (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY));
		final IAgent agentCause = social.getAgent();
		IScope scopeAgentCause = null;
		if (agentCause != null) {
			scopeAgentCause = agentCause.getScope().copy("in SimpleBdiArchitecture");
			scopeAgentCause.push(agentCause);
		}
		final IAgent currentAgent = scope.getAgent();
		double tempPositif = 0.0;
		double moyPositif = 0.0;
		double tempNegatif = 0.0;
		double moyNegatif = 0.0;
		double coefModification = 0.1;
		if (use_personality) {
			final double neurotisme = (double) scope.getAgent().getAttribute(NEUROTISM);
			coefModification = 1 - neurotisme;
		}
		double dominanceModif = social.getDominance();
		for (final Emotion emo : getEmotionBase(scope, EMOTION_BASE)) {
			if (emo.getAgentCause() != null && emo.getAgentCause().equals(agentCause)) {
				if ("sadness".equals(emo.getName()) || "fear".equals(emo.getName())) {
					tempNegatif = tempNegatif + 1.0;
					moyNegatif = moyNegatif + emo.getIntensity();
				}
			}
		}
		for (final Emotion emo : getEmotionBase(scopeAgentCause, EMOTION_BASE)) {
			if (emo.getAgentCause() != null && emo.getAgentCause().equals(currentAgent)) {
				if ("sadness".equals(emo.getName()) || "fear".equals(emo.getName())) {
					tempPositif = tempPositif + 1.0;
					moyPositif = moyPositif + emo.getIntensity();
				}
			}
		}

		moyPositif = tempPositif != 0.0 ? moyPositif / tempPositif : 0;
		moyNegatif = tempNegatif != 0.0 ? moyNegatif / tempNegatif : 0;

		dominanceModif = Math.clamp(dominanceModif + coefModification * Maths.abs(dominanceModif) * (moyPositif - moyNegatif), 1, 1);
		social.setDominance(dominanceModif);
		GAMA.releaseScope(scopeAgentCause);
	}

	/**
	 * Update solidarity.
	 *
	 * @param scope the scope
	 * @param social the social
	 */
	static private void updateSolidarity(final IScope scope, final SocialLink social) {
		final Boolean use_personality = scope.hasArg(USE_PERSONALITY) ? scope.getBoolArg(USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY);
		final IAgent agentCause = social.getAgent();
		double tempPositif = 0.0;
		double moySolid = 0.0;
		double tempNegatif = 0.0;
		double nbMentalState = 0.0;
		double tempEmoNeg = 0.0;
		double moyEmoNeg = 0.0;
		double coefModification = 0.1;
		if (use_personality) {
			final double openness = (double) scope.getAgent().getAttribute(OPENNESS);
			coefModification = 1 - openness;
		}
		double coefModifEmo = 0.1;
		if (use_personality) {
			final double neurotisme = (double) scope.getAgent().getAttribute(NEUROTISM);
			coefModifEmo = 1 - neurotisme;
		}
		double solidarityModif = social.getSolidarity();
		for (final Emotion emo : getEmotionBase(scope, EMOTION_BASE)) {
			if (emo.getAgentCause() != null && emo.getAgentCause().equals(agentCause)) {
				if ("sadness".equals(emo.getName()) || "fear".equals(emo.getName())) {
					tempEmoNeg = tempEmoNeg + 1.0;
					moyEmoNeg = moyEmoNeg + emo.getIntensity();
				}
			}
		}
		// Modifier pour ne prendre que ses propres croyances
		for (final MentalState predTest1 : getBase(scope, SimpleBdiArchitecture.BELIEF_BASE)) {
			if (predTest1.getMentalState() != null && predTest1.getMentalState().getOwner() != null
					&& predTest1.getMentalState().getOwner().equals(agentCause)
					&& "Belief".equals(predTest1.getMentalState().getModality())) {
				for (final MentalState predTest2 : getBase(scope, SimpleBdiArchitecture.BELIEF_BASE)) {
					if (predTest2.getPredicate() != null && predTest1.getMentalState().getPredicate() != null
							&& predTest2.getPredicate().equals(predTest1.getMentalState().getPredicate())) {
						tempPositif = tempPositif + 1.0;
						nbMentalState = nbMentalState + 1.0;
					}
					if (predTest2.getPredicate() != null && predTest1.getMentalState().getPredicate() != null
							&& predTest2.getPredicate().equalsButNotTruth(predTest1.getMentalState().getPredicate())) {
						tempNegatif = tempNegatif + 1.0;
						nbMentalState = nbMentalState + 1.0;
					}
				}
			}
			if (predTest1.getMentalState() != null && predTest1.getMentalState().getOwner() != null
					&& predTest1.getMentalState().getOwner().equals(agentCause)
					&& "Desire".equals(predTest1.getMentalState().getModality())) {
				for (final MentalState predTest2 : getBase(scope, SimpleBdiArchitecture.DESIRE_BASE)) {
					if (predTest2.getPredicate() != null && predTest1.getMentalState().getPredicate() != null
							&& predTest2.getPredicate().equals(predTest1.getMentalState().getPredicate())) {
						tempPositif = tempPositif + 1.0;
						nbMentalState = nbMentalState + 1.0;
					}
					if (predTest2.getPredicate() != null && predTest1.getMentalState().getPredicate() != null
							&& predTest2.getPredicate().equalsButNotTruth(predTest1.getMentalState().getPredicate())) {
						tempNegatif = tempNegatif + 1.0;
						nbMentalState = nbMentalState + 1.0;
					}
				}
			}
			if (predTest1.getMentalState() != null && predTest1.getMentalState().getOwner() != null
					&& predTest1.getMentalState().getOwner().equals(agentCause)
					&& "Uncertainty".equals(predTest1.getMentalState().getModality())) {
				for (final MentalState predTest2 : getBase(scope, SimpleBdiArchitecture.UNCERTAINTY_BASE)) {
					if (predTest2.getPredicate() != null && predTest1.getMentalState().getPredicate() != null
							&& predTest2.getPredicate().equals(predTest1.getMentalState().getPredicate())) {
						tempPositif = tempPositif + 1.0;
						nbMentalState = nbMentalState + 1.0;
					}
					if (predTest2.getPredicate() != null && predTest1.getMentalState().getPredicate() != null
							&& predTest2.getPredicate().equalsButNotTruth(predTest1.getMentalState().getPredicate())) {
						tempNegatif = tempNegatif + 1.0;
						nbMentalState = nbMentalState + 1.0;
					}
				}
			}
			if (predTest1.getMentalState() != null && predTest1.getMentalState().getOwner() != null
					&& predTest1.getMentalState().getOwner().equals(agentCause)
					&& "Ideal".equals(predTest1.getMentalState().getModality())) {
				for (final MentalState predTest2 : getBase(scope, SimpleBdiArchitecture.IDEAL_BASE)) {
					if (predTest2.getPredicate() != null && predTest1.getMentalState().getPredicate() != null
							&& predTest2.getPredicate().equals(predTest1.getMentalState().getPredicate())) {
						tempPositif = tempPositif + 1.0;
						nbMentalState = nbMentalState + 1.0;
					}
					if (predTest2.getPredicate() != null && predTest1.getMentalState().getPredicate() != null
							&& predTest2.getPredicate().equalsButNotTruth(predTest1.getMentalState().getPredicate())) {
						tempNegatif = tempNegatif + 1.0;
						nbMentalState = nbMentalState + 1.0;
					}
				}
			}
		}
		if (tempEmoNeg != 0.0) {
			moyEmoNeg = moyEmoNeg / tempEmoNeg;
		} else {
			moyEmoNeg = 0.0;
		}
		if (nbMentalState != 0.0) {
			moySolid = (tempPositif - tempNegatif) / nbMentalState;
		}
		solidarityModif = solidarityModif
				+ solidarityModif * (1 - solidarityModif) * (coefModification * moySolid - coefModifEmo * moyEmoNeg);
		if (solidarityModif > 1.0) {
			solidarityModif = 1.0;
		}
		if (solidarityModif < 0.0) {
			solidarityModif = 0.0;
		}
		social.setSolidarity(solidarityModif);
		// GAMA.releaseScope(scopeAgentCause);
	}

	/**
	 * Update familiarity.
	 *
	 * @param scope the scope
	 * @param social the social
	 */
	static private void updateFamiliarity(final IScope scope, final SocialLink social) {
		Double familiarityModif = social.getFamiliarity();
		familiarityModif = familiarityModif * (1 + social.getLiking());
		if (familiarityModif > 1.0) {
			familiarityModif = 1.0;
		}
		if (familiarityModif < 0.0) {
			familiarityModif = 0.0;
		}
		if (social.getFamiliarity() == 0.0) {//TODO: why not clamp at 0.1 then ?
			familiarityModif = 0.1;
		}
		social.setFamiliarity(familiarityModif);
	}

	/**
	 * Update norm violation.
	 *
	 * @param scope the scope
	 */
	public void updateNormViolation(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		final Norm _persistentNorm = (Norm) agent.getAttribute(CURRENT_NORM);
		// Double obedienceValue = (Double) scope.getAgent().getAttribute("obedience");

		for (final Norm tempNorm : getNorms(scope)) {
			// si la norme est activable mais n'est pas activée, alors elle est violée (non prise en compte des
			// obligations)
			final NormStatement statement = tempNorm.getNormStatement();
			if (statement != null) {
				final boolean isContextConditionSatisfied = statement.getContextExpression() == null
						|| Cast.asBool(scope, statement.getContextExpression().value(scope));
				boolean isIntentionConditionSatisfied = false;
				if (currentIntention(scope) == null || statement.getIntentionExpression() == null
						|| statement.getIntentionExpression().value(scope) == null
						|| ((Predicate) statement.getIntentionExpression().value(scope))
								.equalsIntentionPlan(currentIntention(scope).getPredicate())) {
					isIntentionConditionSatisfied = true;
				}
				boolean isObligationConditionSatisfied = false;
				if (currentIntention(scope) != null && statement.getObligationExpression() != null
						&& statement.getObligationExpression().value(scope) != null) {
					isObligationConditionSatisfied = ((Predicate) statement.getObligationExpression().value(scope))
							.equalsIntentionPlan(currentIntention(scope).getPredicate());
				}
				// final boolean thresholdSatisfied = statement.getThreshold() == null
				// || obedienceValue >= (Double) statement.getThreshold().value(scope);
				if (isContextConditionSatisfied && isIntentionConditionSatisfied
						|| isContextConditionSatisfied && isObligationConditionSatisfied /* && thresholdSatisfied */) {
					if (_persistentNorm == null || !statement.equals(_persistentNorm.getNormStatement())) {
						tempNorm.violated(scope);
					} else if (statement.equals(_persistentNorm.getNormStatement())) {
						tempNorm.applied(scope);
					}
				}
			}
		}
	}

	/**
	 * Update norm lifetime.
	 *
	 * @param scope the scope
	 */
	public void updateNormLifetime(final IScope scope) {
		for (final Norm tempNorm : getNorms(scope)) {
			if (tempNorm != null) {
				tempNorm.updateLifeime();
			}
		}
	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		super.init(scope);
		// _consideringScope = scope;
		return true;
	}

	@Override
	public void verifyBehaviors(final ISpecies context) {}

}

/*******************************************************************************************************
 *
 * FocusStatement.java, in gama.extension.bdi, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/

package gama.extension.bdi;

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
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class FocusStatement.
 */
@symbol (
		name = FocusStatement.FOCUS,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.BDI })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
				name = IKeyword.ID,
				type = IType.STRING,
				optional = true,
				doc = @doc ("the identifier of the focus")),
				@facet (
						name = FocusStatement.VAR,
						type = { IType.NONE, IType.LIST, IType.CONTAINER },
						optional = true,
						doc = @doc ("the variable of the perceived agent you want to add to your beliefs")),
				@facet (
						name = FocusStatement.EXPRESSION,
						type = IType.NONE,
						optional = true,
						doc = @doc ("an expression that will be the value kept in the belief")),
				@facet (
						name = IKeyword.WHEN,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("A boolean value to focus only with a certain condition")),
				@facet (
						name = FocusStatement.LIFETIME,
						type = IType.INT,
						optional = true,
						doc = @doc ("the lifetime value of the created belief")),
				@facet (
						name = FocusStatement.TRUTH,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("the truth value of the created belief")),
				@facet (
						name = FocusStatement.AGENTCAUSE,
						type = IType.AGENT,
						optional = true,
						doc = @doc ("the value of the agent causing the created belief (can be nil")),
				@facet (
						name = FocusStatement.BELIEF,
						type = PredicateType.id,
						optional = true,
						doc = @doc ("The predicate to focus on the beliefs of the other agent")),
				@facet (
						name = FocusStatement.DESIRE,
						type = PredicateType.id,
						optional = true,
						doc = @doc ("The predicate to focus on the desires of the other agent")),
				@facet (
						name = FocusStatement.EMOTION,
						type = EmotionType.EMOTIONTYPE_ID,
						optional = true,
						doc = @doc ("The emotion to focus on the emotions of the other agent")),
				@facet (
						name = FocusStatement.UNCERTAINTY,
						type = PredicateType.id,
						optional = true,
						doc = @doc ("The predicate to focus on the uncertainties of the other agent")),
				@facet (
						name = FocusStatement.IDEAL,
						type = PredicateType.id,
						optional = true,
						doc = @doc ("The predicate to focus on the ideals of the other agent")),
				@facet (
						name = FocusStatement.ISUNCERTAIN,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("a boolean to indicate if the mental state created is an uncertainty")),
				@facet (
						name = FocusStatement.STRENGTH,
						type = { IType.FLOAT, IType.INT },
						optional = true,
						doc = @doc ("The priority of the created predicate")) })
@doc (
		value = "enables to directly add a belief from the variable of a perceived species.",
		examples = {
				@example ("focus var:speed /*where speed is a variable from a species that is being perceived*/") })
public class FocusStatement extends AbstractStatement {

	/** The Constant FOCUS. */
	public static final String FOCUS = "focus";
	
	/** The Constant STRENGTH. */
	public static final String STRENGTH = "strength";
	
	/** The Constant EXPRESSION. */
	public static final String EXPRESSION = "expression";
	
	/** The Constant VAR. */
	public static final String VAR = "var";
	
	/** The Constant BELIEF. */
	public static final String BELIEF = "belief";
	
	/** The Constant DESIRE. */
	public static final String DESIRE = "desire";
	
	/** The Constant UNCERTAINTY. */
	public static final String UNCERTAINTY = "uncertainty";
	
	/** The Constant IDEAL. */
	public static final String IDEAL = "ideal";
	
	/** The Constant EMOTION. */
	public static final String EMOTION = "emotion";
	
	/** The Constant LIFETIME. */
	public static final String LIFETIME = "lifetime";
	
	/** The Constant TRUTH. */
	public static final String TRUTH = "truth";
	
	/** The Constant AGENTCAUSE. */
	public static final String AGENTCAUSE = "agent_cause";
	
	/** The Constant ISUNCERTAIN. */
	public static final String ISUNCERTAIN = "is_uncertain";

	/** The name expression. */
	final IExpression nameExpression;
	
	/** The variable. */
	final IExpression variable;
	
	/** The expression. */
	final IExpression expression;
	
	/** The belief. */
	final IExpression belief;
	
	/** The desire. */
	final IExpression desire;
	
	/** The uncertainty. */
	final IExpression uncertainty;
	
	/** The ideal. */
	final IExpression ideal;
	
	/** The emotion. */
	final IExpression emotion;
	
	/** The when. */
	final IExpression when;
	
	/** The strength. */
	final IExpression strength;
	
	/** The lifetime. */
	final IExpression lifetime;
	
	/** The truth. */
	final IExpression truth;
	
	/** The agent cause. */
	final IExpression agentCause;
	
	/** The is uncertain. */
	final IExpression isUncertain;

	/**
	 * Instantiates a new focus statement.
	 *
	 * @param desc the desc
	 */
	public FocusStatement(final IDescription desc) {
		super(desc);
		nameExpression = getFacet(IKeyword.ID);
		variable = getFacet(FocusStatement.VAR);
		expression = getFacet(FocusStatement.EXPRESSION);
		belief = getFacet(FocusStatement.BELIEF);
		desire = getFacet(FocusStatement.DESIRE);
		uncertainty = getFacet(FocusStatement.UNCERTAINTY);
		ideal = getFacet(FocusStatement.IDEAL);
		emotion = getFacet(FocusStatement.EMOTION);
		when = getFacet(IKeyword.WHEN);
		strength = getFacet(FocusStatement.STRENGTH);
		lifetime = getFacet(FocusStatement.LIFETIME);
		truth = getFacet(FocusStatement.TRUTH);
		agentCause = getFacet(FocusStatement.AGENTCAUSE);
		isUncertain = getFacet(FocusStatement.ISUNCERTAIN);
	}

	@SuppressWarnings ("rawtypes")
	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		if (when == null || Cast.asBool(scope, when.value(scope))) {
			final IAgent[] stack = scope.getAgentsStack();
			final IAgent mySelfAgent = stack[stack.length > 2 ? 1 : 0];
			IScope scopeMySelf = null;
			if (mySelfAgent != null) {
				scopeMySelf = mySelfAgent.getScope().copy("in FocusStatement");
				scopeMySelf.push(mySelfAgent);
			}
			Predicate tempPred;
			if (variable != null) {
				// Pour la liste, faire un truc générique dans un premier temps
				// avec un nom des variables du genre test_i, sans chercher à
				// récupérer le nom précis des variables.
				if (variable.value(scope) instanceof IContainer) {
					String namePred;
					if (nameExpression != null) {
						namePred = (String) nameExpression.value(scope);
					} else {
						namePred = variable.getName() + "_" + scope.getAgent().getSpeciesName();
					}
					String nameVarTemp;
					final IMap<String, Object> tempValues = GamaMapFactory.create(Types.STRING, Types.NO_TYPE, 1);
					final IList<?> variablesTemp =
							((IContainer<?, ?>) variable.value(scope)).listValue(scope, null, true);
					for (int temp = 0; temp < variablesTemp.length(scope); temp++) {
						final Object temp2 = variablesTemp.get(temp);
						nameVarTemp = "test" + temp;
						tempValues.put(nameVarTemp + "_value", Cast.asInt(scope, temp2));
					}
					tempPred = new Predicate(namePred, tempValues.copy(scope));
					if (truth != null) {
						tempPred.setIs_True(Cast.asBool(scope, truth.value(scope)));
					}
					if (agentCause != null) {
						tempPred.setAgentCause((IAgent) agentCause.value(scope));
					} else {
						tempPred.setAgentCause(scope.getAgent());
					}
					MentalState tempBelief;
					if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
						if (strength != null) {
							tempBelief = new MentalState("Uncertainty", tempPred,
									Cast.asFloat(scope, strength.value(scope)));
						} else {
							tempBelief = new MentalState("Uncertainty", tempPred);
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (!SimpleBdiArchitecture.hasUncertainty(scopeMySelf, tempBelief)) {
							SimpleBdiArchitecture.addUncertainty(scopeMySelf, tempBelief);
						}
					} else {
						if (strength != null) {
							tempBelief =
									new MentalState("Belief", tempPred, Cast.asFloat(scope, strength.value(scope)));
						} else {
							tempBelief = new MentalState("Belief", tempPred);
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (!SimpleBdiArchitecture.hasBelief(scopeMySelf, tempBelief)) {
							SimpleBdiArchitecture.addBelief(scopeMySelf, tempBelief);
						}
					}
				} else {
					String namePred;
					if (nameExpression != null) {
						namePred = (String) nameExpression.value(scope);
					} else {
						namePred = variable.getName() + "_" + scope.getAgent().getSpeciesName();
					}
					final String nameVar = variable.getName();
					final IMap<String, Object> tempValues = GamaMapFactory.create(Types.STRING, Types.NO_TYPE, 1);
					if (expression != null) {
						tempValues.put(nameVar + "_value", expression.value(scope));
					} else {
						tempValues.put(nameVar + "_value", variable.value(scope));
					}
					tempPred = new Predicate(namePred, tempValues);
					if (truth != null) {
						tempPred.setIs_True(Cast.asBool(scope, truth.value(scope)));
					}
					if (agentCause != null) {
						tempPred.setAgentCause((IAgent) agentCause.value(scope));
					} else {
						tempPred.setAgentCause(scope.getAgent());
					}
					MentalState tempBelief;
					if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
						if (strength != null) {
							tempBelief = new MentalState("Uncertainty", tempPred,
									Cast.asFloat(scope, strength.value(scope)));
						} else {
							tempBelief = new MentalState("Uncertainty", tempPred);
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scopeMySelf, lifetime.value(scope)));
						}
						if (!SimpleBdiArchitecture.hasUncertainty(scopeMySelf, tempBelief)) {
							SimpleBdiArchitecture.addUncertainty(scopeMySelf, tempBelief);
						}
					} else {
						if (strength != null) {
							tempBelief =
									new MentalState("Belief", tempPred, Cast.asFloat(scope, strength.value(scope)));
						} else {
							tempBelief = new MentalState("Belief", tempPred);
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (!SimpleBdiArchitecture.hasBelief(scopeMySelf, tempBelief)) {
							SimpleBdiArchitecture.addBelief(scopeMySelf, tempBelief);
						}
					}
				}
			} else {
				if (belief != null) {
					MentalState temporaryBelief = new MentalState("Belief");
					temporaryBelief.setPredicate((Predicate) belief.value(scope));
					if (SimpleBdiArchitecture.hasBelief(scope, temporaryBelief)) {
						MentalState tempBelief = null;
						if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
							tempBelief = new MentalState("Uncertainty");
						} else {
							tempBelief = new MentalState("Belief");
						}
						for (final MentalState temp : SimpleBdiArchitecture.getBase(scope, "belief_base")) {
							if (temp.equals(temporaryBelief)) {
								temporaryBelief = temp;
							}
						}
						tempBelief.setMentalState(temporaryBelief);
						if (strength != null) {
							tempBelief.setStrength(Cast.asFloat(scope, strength.value(scope)));
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
							if (!SimpleBdiArchitecture.hasUncertainty(scopeMySelf, tempBelief)) {
								SimpleBdiArchitecture.addUncertainty(scopeMySelf, tempBelief);
							}
						} else {
							if (!SimpleBdiArchitecture.hasBelief(scopeMySelf, tempBelief)) {
								SimpleBdiArchitecture.addBelief(scopeMySelf, tempBelief);
							}
						}
					}
				}
				if (desire != null) {
					MentalState temporaryBelief = new MentalState("Desire");
					temporaryBelief.setPredicate((Predicate) desire.value(scope));
					if (SimpleBdiArchitecture.hasDesire(scope, temporaryBelief)) {
						MentalState tempBelief = null;
						if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
							tempBelief = new MentalState("Uncertainty");
						} else {
							tempBelief = new MentalState("Belief");
						}
						for (final MentalState temp : SimpleBdiArchitecture.getBase(scope, "desire_base")) {
							if (temp.equals(temporaryBelief)) {
								temporaryBelief = temp;
							}
						}
						tempBelief.setMentalState(temporaryBelief);
						if (strength != null) {
							tempBelief.setStrength(Cast.asFloat(scope, strength.value(scope)));
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
							if (!SimpleBdiArchitecture.hasUncertainty(scopeMySelf, tempBelief)) {
								SimpleBdiArchitecture.addUncertainty(scopeMySelf, tempBelief);
							}
						} else {
							if (!SimpleBdiArchitecture.hasBelief(scopeMySelf, tempBelief)) {
								SimpleBdiArchitecture.addBelief(scopeMySelf, tempBelief);
							}
						}
					}
				}
				if (uncertainty != null) {
					MentalState temporaryBelief = new MentalState("Uncertainty");
					temporaryBelief.setPredicate((Predicate) uncertainty.value(scope));
					if (SimpleBdiArchitecture.hasUncertainty(scope, temporaryBelief)) {
						MentalState tempBelief = null;
						if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
							tempBelief = new MentalState("Uncertainty");
						} else {
							tempBelief = new MentalState("Belief");
						}
						for (final MentalState temp : SimpleBdiArchitecture.getBase(scope, "uncertainty_base")) {
							if (temp.equals(temporaryBelief)) {
								temporaryBelief = temp;
							}
						}
						tempBelief.setMentalState(temporaryBelief);
						if (strength != null) {
							tempBelief.setStrength(Cast.asFloat(scope, strength.value(scope)));
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
							if (!SimpleBdiArchitecture.hasUncertainty(scopeMySelf, tempBelief)) {
								SimpleBdiArchitecture.addUncertainty(scopeMySelf, tempBelief);
							}
						} else {
							if (!SimpleBdiArchitecture.hasBelief(scopeMySelf, tempBelief)) {
								SimpleBdiArchitecture.addBelief(scopeMySelf, tempBelief);
							}
						}
					}
				}
				if (ideal != null) {
					MentalState temporaryBelief = new MentalState("Ideal");
					temporaryBelief.setPredicate((Predicate) ideal.value(scope));
					if (SimpleBdiArchitecture.hasIdeal(scope, temporaryBelief)) {
						MentalState tempBelief = null;
						if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
							tempBelief = new MentalState("Uncertainty");
						} else {
							tempBelief = new MentalState("Belief");
						}
						for (final MentalState temp : SimpleBdiArchitecture.getBase(scope, "ideal_base")) {
							if (temp.equals(temporaryBelief)) {
								temporaryBelief = temp;
							}
						}
						tempBelief.setMentalState(temporaryBelief);
						if (strength != null) {
							tempBelief.setStrength(Cast.asFloat(scope, strength.value(scope)));
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
							if (!SimpleBdiArchitecture.hasUncertainty(scopeMySelf, tempBelief)) {
								SimpleBdiArchitecture.addUncertainty(scopeMySelf, tempBelief);
							}
						} else {
							if (!SimpleBdiArchitecture.hasBelief(scopeMySelf, tempBelief)) {
								SimpleBdiArchitecture.addBelief(scopeMySelf, tempBelief);
							}
						}
					}
				}
				if (emotion != null) {
					Emotion temporaryEmotion = (Emotion) emotion.value(scope);
					if (SimpleBdiArchitecture.hasEmotion(scope, temporaryEmotion)) {
						MentalState tempBelief = null;
						if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
							tempBelief = new MentalState("Uncertainty");
						} else {
							tempBelief = new MentalState("Belief");
						}
						for (final Emotion temp : SimpleBdiArchitecture.getEmotionBase(scope, "emotion_base")) {
							if (temp.equals(temporaryEmotion)) {
								temporaryEmotion = temp;
							}
						}
						tempBelief.setEmotion(temporaryEmotion);
						if (strength != null) {
							tempBelief.setStrength(Cast.asFloat(scope, strength.value(scope)));
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
							if (!SimpleBdiArchitecture.hasUncertainty(scopeMySelf, tempBelief)) {
								SimpleBdiArchitecture.addUncertainty(scopeMySelf, tempBelief);
							}
						} else {
							if (!SimpleBdiArchitecture.hasBelief(scopeMySelf, tempBelief)) {
								SimpleBdiArchitecture.addBelief(scopeMySelf, tempBelief);
							}
						}
					}
				}
				if (expression != null) {
					String namePred;
					if (nameExpression != null) {
						namePred = (String) nameExpression.value(scope);
					} else {
						namePred = "expression" + "_" + scope.getAgent().getSpeciesName();
					}
					final String nameVar = "expression";
					final IMap<String, Object> tempValues = GamaMapFactory.create(Types.NO_TYPE, Types.NO_TYPE, 1);
					tempValues.put(nameVar + "_value", expression.value(scope));
					tempPred = new Predicate(namePred, tempValues);
					if (truth != null) {
						tempPred.setIs_True(Cast.asBool(scope, truth.value(scope)));
					}
					if (agentCause != null) {
						tempPred.setAgentCause((IAgent) agentCause.value(scope));
					} else {
						tempPred.setAgentCause(scope.getAgent());
					}
					MentalState tempBelief;
					if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
						if (strength != null) {
							tempBelief = new MentalState("Uncertainty", tempPred,
									Cast.asFloat(scope, strength.value(scope)));
						} else {
							tempBelief = new MentalState("Uncertainty", tempPred);
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (!SimpleBdiArchitecture.hasUncertainty(scopeMySelf, tempBelief)) {
							SimpleBdiArchitecture.addUncertainty(scopeMySelf, tempBelief);
						}
					} else {
						if (strength != null) {
							tempBelief =
									new MentalState("Belief", tempPred, Cast.asFloat(scope, strength.value(scope)));
						} else {
							tempBelief = new MentalState("Belief", tempPred);
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (!SimpleBdiArchitecture.hasBelief(scopeMySelf, tempBelief)) {
							SimpleBdiArchitecture.addBelief(scopeMySelf, tempBelief);
						}
					}
				}
				if (variable == null && belief == null && desire == null && uncertainty == null && ideal == null
						&& emotion == null && expression == null) {
					String namePred = null;
					if (nameExpression != null) {
						namePred = (String) nameExpression.value(scope);
					}
					// final Map<String, Object> tempValues = new IMap<String, Object>(1, null, null);
					tempPred = new Predicate(namePred/* , tempValues */);
					if (truth != null) {
						tempPred.setIs_True(Cast.asBool(scope, truth.value(scope)));
					}
					if (agentCause != null) {
						tempPred.setAgentCause((IAgent) agentCause.value(scope));
					} else {
						tempPred.setAgentCause(scope.getAgent());
					}
					MentalState tempBelief;
					if (isUncertain != null && (Boolean) isUncertain.value(scopeMySelf)) {
						if (strength != null) {
							tempBelief = new MentalState("Uncertainty", tempPred,
									Cast.asFloat(scope, strength.value(scope)));
						} else {
							tempBelief = new MentalState("Uncertainty", tempPred);
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (!SimpleBdiArchitecture.hasUncertainty(scopeMySelf, tempBelief)) {
							SimpleBdiArchitecture.addUncertainty(scopeMySelf, tempBelief);
						}
					} else {
						if (strength != null) {
							tempBelief =
									new MentalState("Belief", tempPred, Cast.asFloat(scope, strength.value(scope)));
						} else {
							tempBelief = new MentalState("Belief", tempPred);
						}
						if (lifetime != null) {
							tempBelief.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
						}
						if (!SimpleBdiArchitecture.hasBelief(scopeMySelf, tempBelief)) {
							SimpleBdiArchitecture.addBelief(scopeMySelf, tempBelief);
						}
					}
				}
			}
			GAMA.releaseScope(scopeMySelf);
		}
		return null;
	}

}
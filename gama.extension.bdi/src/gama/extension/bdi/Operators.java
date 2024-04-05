/*******************************************************************************************************
 *
 * Operators.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
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
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.types.IType;

/**
 * The Class Operators.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class Operators {

	/**
	 * New predicate.
	 *
	 * @param name
	 *            the name
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_predicate",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "creates a new predicate with a given name and adidtional properties (values, agent causing the predicate, whether it is true...)",
			masterDoc = true,
			examples = @example (
					value = "new_predicate(\"people to meet\")",
					isExecutable = false))
	@no_test
	public static Predicate newPredicate(final String name) throws GamaRuntimeException {
		return new Predicate(name);
	}

	/**
	 * New predicate.
	 *
	 * @param name
	 *            the name
	 * @param values
	 *            the values
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_predicate",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new predicate with the given properties (name, values)",
			examples = @example (
					value = "new_predicate(\"people to meet\", map([\"val1\"::23]) )",
					isExecutable = false))
	@no_test
	public static Predicate newPredicate(final String name, final IMap values) throws GamaRuntimeException {
		return new Predicate(name, values);
	}

	/**
	 * New predicate.
	 *
	 * @param name
	 *            the name
	 * @param ist
	 *            the ist
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_predicate",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new predicate with the given is_true (name, is_true)",
			examples = @example (
					value = "new_predicate(\"hasWater\", true)",
					isExecutable = false))
	@no_test
	public static Predicate newPredicate(final String name, final Boolean ist) throws GamaRuntimeException {
		return new Predicate(name, ist);
	}

	/**
	 * New predicate.
	 *
	 * @param name
	 *            the name
	 * @param agent
	 *            the agent
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_predicate",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new predicate with the given properties (name, cause agent)",
			examples = @example (
					value = "new_predicate(\"people to meet\", agent1)",
					isExecutable = false))
	@no_test
	public static Predicate newPredicate(final String name, final IAgent agent) throws GamaRuntimeException {
		return new Predicate(name, agent);
	}

	/**
	 * New predicate.
	 *
	 * @param name
	 *            the name
	 * @param values
	 *            the values
	 * @param truth
	 *            the truth
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_predicate",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new predicate with the given properties (name, values, is_true)",
			examples = @example (
					value = "new_predicate(\"people to meet\", [\"time\"::10], true)",
					isExecutable = false))
	@no_test
	public static Predicate newPredicate(final String name, final IMap values, final Boolean truth)
			throws GamaRuntimeException {
		return new Predicate(name, values, truth);
	}


	/**
	 * New predicate.
	 *
	 * @param name
	 *            the name
	 * @param values
	 *            the values
	 * @param agent
	 *            the agent
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_predicate",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new predicate with the given properties (name, values, agent_cause)",
			examples = @example (
					value = "new_predicate(\"people to meet\", [\"time\"::10], agentA)",
					isExecutable = false))
	@no_test
	public static Predicate newPredicate(final String name, final IMap values, final IAgent agent)
			throws GamaRuntimeException {
		return new Predicate(name, values, agent);
	}

	/**
	 * New predicate.
	 *
	 * @param name
	 *            the name
	 * @param values
	 *            the values
	 * @param truth
	 *            the truth
	 * @param agent
	 *            the agent
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_predicate",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new predicate with the given properties (name, values, is_true, agent_cause)",
			examples = @example (
					value = "new_predicate(\"people to meet\", [\"time\"::10], true, agentA)",
					isExecutable = false))
	@no_test
	public static Predicate newPredicate(final String name, final IMap values, final Boolean truth, final IAgent agent)
			throws GamaRuntimeException {
		return new Predicate(name, values, truth, agent);
	}

	/**
	 * With truth.
	 *
	 * @param predicate
	 *            the predicate
	 * @param truth
	 *            the truth
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_truth",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the is_true value of the given predicate",
			examples = @example (
					value = "predicate with_truth false",
					isExecutable = false))
	@no_test
	public static Predicate withTruth(final Predicate predicate, final Boolean truth) throws GamaRuntimeException {
		predicate.is_true = truth;
		return predicate;
	}

	/**
	 * With values.
	 *
	 * @param predicate
	 *            the predicate
	 * @param values
	 *            the values
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_values",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the parameters of the given predicate",
			examples = @example (
					value = "predicate with_values [\"time\"::10]",
					isExecutable = false))
	@no_test
	public static Predicate withValues(final Predicate predicate, final IMap values) throws GamaRuntimeException {
		predicate.setValues(values);
		return predicate;
	}

	/**
	 * Adds the values.
	 *
	 * @param predicate
	 *            the predicate
	 * @param values
	 *            the values
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "add_values",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "add a new value to the map of the given predicate",
			examples = @example (
					value = "predicate add_values [\"time\"::10];",
					isExecutable = false))
	@no_test
	public static Predicate addValues(final Predicate predicate, final IMap values) throws GamaRuntimeException {
		if (values != null && predicate != null) { predicate.getValues().putAll(values); }
		return predicate;
	}

	/**
	 * Not.
	 *
	 * @param pred1
	 *            the pred 1
	 * @return the predicate
	 */
	@operator (
			value = "not",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "create a new predicate with the inverse truth value",
			examples = @example (
					value = "not predicate1",
					isExecutable = false))
	@no_test
	public static Predicate not(final Predicate pred1) {
		pred1.setIs_True(!pred1.getIs_True());
		return pred1;
	}

	/**
	 * And.
	 *
	 * @param pred1
	 *            the pred 1
	 * @param pred2
	 *            the pred 2
	 * @return the predicate
	 */
	@operator (
			value = "and",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "create a new predicate from two others by including them as subintentions",
			examples = @example (
					value = "predicate1 and predicate2",
					isExecutable = false))
	@no_test
	public static Predicate and(final Predicate pred1, final Predicate pred2) {
		final Predicate tempPred = new Predicate(pred1.getName() + "_and_" + pred2.getName());
		final List<MentalState> tempList = new ArrayList<>();
		final MentalState tempPred1 = new MentalState("Intention", pred1);
		final MentalState tempPred2 = new MentalState("Intention", pred2);
		tempList.add(tempPred1);
		tempList.add(tempPred2);
		tempPred.setSubintentions(tempList);
		final IMap<String, Object> tempMap = GamaMapFactory.create();
		tempMap.put("and", true);
		tempPred.setValues(tempMap);
		return tempPred;
	}

	/**
	 * Or.
	 *
	 * @param pred1
	 *            the pred 1
	 * @param pred2
	 *            the pred 2
	 * @return the predicate
	 */
	@operator (
			value = "or",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "create a new predicate from two others by including them as subintentions. It's an exclusive \"or\" ",
			examples = @example (
					value = "predicate1 or predicate2",
					isExecutable = false))
	@no_test
	public static Predicate or(final Predicate pred1, final Predicate pred2) {
		final Predicate tempPred = new Predicate(pred1.getName() + "_or_" + pred2.getName());
		final List<MentalState> tempList = new ArrayList<>();
		final MentalState tempPred1 = new MentalState("Intention", pred1);
		final MentalState tempPred2 = new MentalState("Intention", pred2);
		tempList.add(tempPred1);
		tempList.add(tempPred2);
		tempPred.setSubintentions(tempList);
		final IMap<String, Object> tempMap = GamaMapFactory.create();
		tempMap.put("or", true);
		tempPred.setValues(tempMap);
		return tempPred;
	}

	/**
	 * Eval when.
	 *
	 * @param scope
	 *            the scope
	 * @param plan
	 *            the plan
	 * @return the boolean
	 */
	@operator (
			value = "eval_when",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "evaluate the facet when of a given plan",
			examples = @example (
					value = "eval_when(plan1)",
					isExecutable = false))
	@no_test
	public static Boolean evalWhen(final IScope scope, final BDIPlan plan) {
		return plan.getPlanStatement().getContextExpression() == null
				|| gama.gaml.operators.Cast.asBool(scope, plan.getPlanStatement().getContextExpression().value(scope));
	}


	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (at least its name, and eventually intensity, parameters...)",
			masterDoc = true,
			examples = @example (
					value = "new_emotion(\"joy\")",
					isExecutable = false))
	@test ("emotion e <- new_emotion(\"joy\"); assert e.name = \"joy\"; assert e.intensity  = -1.0;")
	public static Emotion newEmotion(final String name) throws GamaRuntimeException {
		return new Emotion(name);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param intensity
	 *            the intensity
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name, intensity)",
			usages = { @usage (
					value = "a new emotion with a name and an initial intensity: ",
					examples = { @example (
							value = "new_emotion(\"joy\",12.3)",
							isExecutable = false) }) })
	@test ("get_intensity(new_emotion('joy',12.3)) = 12.3")
	public static Emotion newEmotion(final String name, final Double intensity) throws GamaRuntimeException {
		return new Emotion(name, intensity);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param about
	 *            the about
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name, about)",
			usages = { @usage (
					value = "a new emotion with a given name and the predicate it is about ",
					examples = { @example (
							value = "new_emotion(\"joy\",estFood)",
							isExecutable = false),
							@example (
									value = "new_emotion(\"joy\",agent1)",
									isExecutable = false) }) })
	@no_test
	public static Emotion newEmotion(final String name, final Predicate about) throws GamaRuntimeException {
		return new Emotion(name, about);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param agent
	 *            the agent
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name and cause agent)",
			usages = { @usage (
					value = "a new emotion with a given name and the agent which has caused this emotion ",
					examples = { @example (
							value = "new_emotion(\"joy\",agent1)",
							isExecutable = false) }) })
	@no_test
	public static Emotion newEmotion(final String name, final IAgent agent) throws GamaRuntimeException {
		return new Emotion(name, agent);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param intensity
	 *            the intensity
	 * @param about
	 *            the about
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name,intensity,about)",
			usages = { @usage (
					value = "Various combinations are possible to create the emotion: (name,intensity,about), (name,about,cause), (name,intensity,cause)... ",
					examples = { @example (
							value = "new_emotion(\"joy\",12.3,eatFood)",
							isExecutable = false),
							@example (
									value = "new_emotion(\"joy\",eatFood,agent1)",
									isExecutable = false),
							@example (
									value = "new_emotion(\"joy\",12.3,agent1)",
									isExecutable = false) }) })
	@no_test
	public static Emotion newEmotion(final String name, final Double intensity, final Predicate about)
			throws GamaRuntimeException {
		return new Emotion(name, intensity, about);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param intensity
	 *            the intensity
	 * @param decay
	 *            the decay
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name,intensity,decay)",
			usages = { @usage (
					value = "A decay value value can be added to define a new emotion.",
					examples = { @example (
							value = "new_emotion(\"joy\",12.3,4.0)",
							isExecutable = false) }) })
	@no_test
	public static Emotion newEmotion(final String name, final Double intensity, final Double decay)
			throws GamaRuntimeException {
		return new Emotion(name, intensity, decay);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param about
	 *            the about
	 * @param agent
	 *            the agent
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name, about, cause agent)")
	@no_test
	public static Emotion newEmotion(final String name, final Predicate about, final IAgent agent)
			throws GamaRuntimeException {
		return new Emotion(name, about, agent);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param intensity
	 *            the intensity
	 * @param agent
	 *            the agent
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name,intensity,cause agent)")
	@no_test
	public static Emotion newEmotion(final String name, final Double intensity, final IAgent agent)
			throws GamaRuntimeException {
		return new Emotion(name, intensity, agent);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param intensity
	 *            the intensity
	 * @param about
	 *            the about
	 * @param decay
	 *            the decay
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name, intensity, about,  decay)",
			examples = @example (
					value = "new_emotion(\"joy\",12.3,eatFood,4.0)",
					isExecutable = false))
	@no_test
	public static Emotion newEmotion(final String name, final Double intensity, final Predicate about,
			final Double decay) throws GamaRuntimeException {
		return new Emotion(name, intensity, about, decay);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param intensity
	 *            the intensity
	 * @param decay
	 *            the decay
	 * @param agent
	 *            the agent
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name,intensity, decay, cause agent)",
			examples = @example (
					value = "emotion(\"joy\", 12.3, 4, agent1)",
					isExecutable = false))
	@no_test
	public static Emotion newEmotion(final String name, final Double intensity, final Double decay, final IAgent agent)
			throws GamaRuntimeException {
		return new Emotion(name, intensity, decay, agent);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param intensity
	 *            the intensity
	 * @param about
	 *            the about
	 * @param agent
	 *            the agent
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name, intensity, about, cause  agent)",
			examples = @example (
					value = "new_emotion(\"joy\",12.3,eatFood,agent1)",
					isExecutable = false))
	@no_test
	public static Emotion newEmotion(final String name, final Double intensity, final Predicate about,
			final IAgent agent) throws GamaRuntimeException {
		return new Emotion(name, intensity, about, agent);
	}

	/**
	 * New emotion.
	 *
	 * @param name
	 *            the name
	 * @param intensity
	 *            the intensity
	 * @param about
	 *            the about
	 * @param decay
	 *            the decay
	 * @param agent
	 *            the agent
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_emotion",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new emotion with the given properties (name, intensity, about, decay, cause  agent)",
			examples = @example (
					value = "emotion(\"joy\",12.3,eatFood,4,agent1)",
					isExecutable = false))
	@no_test
	public static Emotion newEmotion(final String name, final Double intensity, final Predicate about,
			final Double decay, final IAgent agent) throws GamaRuntimeException {
		return new Emotion(name, intensity, about, decay, agent);
	}

	/**
	 * With agent cause.
	 *
	 * @param emotion
	 *            the emotion
	 * @param agent
	 *            the agent
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_agent_cause",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the agentCause value of the given emotion",
			examples = @example (
					value = "new_emotion with_agent_cause agentA",
					isExecutable = false))
	@no_test
	public static Emotion withAgentCause(final Emotion emotion, final IAgent agent) throws GamaRuntimeException {
		emotion.agentCause = agent;
		return emotion;
	}

	/**
	 * Sets the intensity.
	 *
	 * @param emotion
	 *            the emotion
	 * @param intensity
	 *            the intensity
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_intensity",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the intensity value of the given emotion",
			examples = @example (
					value = "emotion with_intensity 12",
					isExecutable = false))
	@no_test
	public static Emotion withIntensity(final Emotion emotion, final double intensity) throws GamaRuntimeException {
		emotion.intensity = intensity;
		return emotion;
	}

	/**
	 * Sets the decay.
	 *
	 * @param emotion
	 *            the emotion
	 * @param decay
	 *            the decay
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_decay",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the decay value of the given emotion",
			examples = @example (
					value = "emotion with_decay 12",
					isExecutable = false))
	@no_test
	public static Emotion withDecay(final Emotion emotion, final double decay) throws GamaRuntimeException {
		emotion.decay = decay;
		return emotion;
	}

	/**
	 * Sets the about.
	 *
	 * @param emotion
	 *            the emotion
	 * @param about
	 *            the about
	 * @return the emotion
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_about",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the about value of the given emotion",
			examples = @example (
					value = "emotion with_about predicate1",
					isExecutable = false))
	@no_test
	public static Emotion withAbout(final Emotion emotion, final Predicate about) throws GamaRuntimeException {
		emotion.about = about;
		return emotion;
	}
	

	/**
	 * New social link.
	 *
	 * @param agent
	 *            the agent
	 * @return the social link
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_social_link",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "creates a new social link with another agent (eventually given additional parameters such as the appreciation, dominance, solidarity, and familiarity values).",
			masterDoc = true,
			examples = @example (
					value = "new_social_link(agentA)",
					isExecutable = false))
	@no_test
	public static SocialLink newSocialLink(final IAgent agent) throws GamaRuntimeException {
		return new SocialLink(agent);
	}

	/**
	 * New social link.
	 *
	 * @param agent
	 *            the agent
	 * @param appreciation
	 *            the appreciation
	 * @param dominance
	 *            the dominance
	 * @param solidarity
	 *            the solidarity
	 * @param familiarity
	 *            the familiarity
	 * @return the social link
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_social_link",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "creates a new social link toward another agent with given appreciation, dominance, solidarity, and familiarity values",
			examples = @example (
					value = "new_social_link(agentA,0.0,-0.1,0.2,0.1)",
					isExecutable = false))
	@no_test
	public static SocialLink newSocialLink(final IAgent agent, final double appreciation, final double dominance,
			final double solidarity, final double familiarity) throws GamaRuntimeException {
		return new SocialLink(agent, appreciation, dominance, solidarity, familiarity);
	}

	/**
	 * Sets the agent.
	 *
	 * @param social
	 *            the social
	 * @param agent
	 *            the agent
	 * @return the social link
	 */
	@operator (
			value = "with_agent",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the agent value of the given social link",
			examples = @example (
					value = "social_link with_agent agentA",
					isExecutable = false))
	@no_test
	public static SocialLink withAgent(final SocialLink social, final IAgent agent) {
		social.setAgent(agent);
		return social;
	}

	/**
	 * Sets the liking.
	 *
	 * @param social
	 *            the social
	 * @param appreciation
	 *            the appreciation
	 * @return the social link
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_liking",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the liking value of the given social link",
			examples = @example (
					value = "social_link with_liking 0.4",
					isExecutable = false))
	@no_test
	public static SocialLink withLiking(final SocialLink social, final double appreciation) throws GamaRuntimeException {
		if (appreciation >= -1.0 && appreciation <= 1.0) { social.setLiking(appreciation); }
		return social;
	}

	/**
	 * Sets the dominance.
	 *
	 * @param social
	 *            the social
	 * @param dominance
	 *            the dominance
	 * @return the social link
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_dominance",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the dominance value of the given social link",
			examples = @example (
					value = "social_link with_dominance 0.4",
					isExecutable = false))
	@no_test
	public static SocialLink withDominance(final SocialLink social, final double dominance) throws GamaRuntimeException {
		if (dominance >= -1.0 && dominance < 1.0) { social.setDominance(dominance); }
		return social;
	}

	/**
	 * Sets the solidarity.
	 *
	 * @param social
	 *            the social
	 * @param solidarity
	 *            the solidarity
	 * @return the social link
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_solidarity",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the solidarity value of the given social link",
			examples = @example (
					value = "social_link with_solidarity 0.4",
					isExecutable = false))
	@no_test
	public static SocialLink withSolidarity(final SocialLink social, final double solidarity)
			throws GamaRuntimeException {
		if (solidarity >= 0.0 && solidarity <= 1.0) { social.setSolidarity(solidarity); }
		return social;
	}

	/**
	 * Sets the familiarity.
	 *
	 * @param social
	 *            the social
	 * @param familiarity
	 *            the familiarity
	 * @return the social link
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_familiarity",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the familiarity value of the given social link",
			examples = @example (
					value = "social_link with_familiarity 0.4",
					isExecutable = false))
	@no_test
	public static SocialLink withFamiliarity(final SocialLink social, final double familiarity)
			throws GamaRuntimeException {
		if (familiarity >= 0.0 && familiarity <= 1.0) { social.setFamiliarity(familiarity); }
		return social;
	}

	/**
	 * Sets the trust.
	 *
	 * @param social
	 *            the social
	 * @param trust
	 *            the trust
	 * @return the social link
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "with_trust",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the trust value of the given social link",
			examples = @example (
					value = "social_link with_trust 0.4",
					isExecutable = false))
	@no_test
	public static SocialLink withTrust(final SocialLink social, final double trust) throws GamaRuntimeException {
		if (trust >= -1.0 && trust <= 1.0) { social.setTrust(trust); }
		return social;
	}

	/**
	 * Gets the agent.
	 *
	 * @param social
	 *            the social
	 * @return the agent
	 */
	@operator (
			value = "get_agent",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the agent value of the given social link",
			examples = @example (
					value = "get_agent(social_link1)",
					isExecutable = false))
	@no_test
	public static IAgent getAgent(final SocialLink social) {
		if (social != null) return social.getAgent();
		return null;
	}

	// Faire en sorte que l'on puisse utiliser les opérateurs seulement avec le
	// nom de l'agent ?

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	// Faire des opérateurs pour créer des états mentaux (en précisant ou non l'agent propriétaire)
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "creates a new mental state with a given modality (e.g. belief or desire) and various properties (a predicate it is about, a strength, a lifetime, an ower agent  and an emotion it is about",
			masterDoc = true,
			examples = @example (
					value = "new_mental_state(\"belief\")",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality) throws GamaRuntimeException {
		return new MentalState(modality);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional predicate",
			examples = @example (
					value = "new_mental_state(\"belief\", raining)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Predicate pred) throws GamaRuntimeException {
		return new MentalState(modality, pred);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional predicate and strength",
			examples = @example (
					value = "new_mental_state(\"belief\", raining, 0.5)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Predicate pred, final Double strength)
			throws GamaRuntimeException {
		return new MentalState(modality, pred, strength);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional predicate and owner agent",
			examples = @example (
					value = "new_mental_state(\"belief\", raining, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Predicate pred, final IAgent ag)
			throws GamaRuntimeException {
		return new MentalState(modality, pred, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param life
	 *            the life
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional predicate and lifetime",
			examples = @example (
					value = "new_mental_state(\"belief\", raining, 10)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Predicate pred, final Integer life)
			throws GamaRuntimeException {
		return new MentalState(modality, pred, life);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional predicate, strength, and owner agent.",
			examples = @example (
					value = "new_mental_state(\"belief\", raining, 12.3, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Predicate pred, final Double strength,
			final IAgent ag) throws GamaRuntimeException {
		return new MentalState(modality, pred, strength, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @param life
	 *            the life
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional predicate, strength andd lifetime",
			examples = @example (
					value = "new_mental_state(\"belief\", raining, 12.4, 10)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Predicate pred, final Double strength,
			final Integer life) throws GamaRuntimeException {
		return new MentalState(modality, pred, strength, life);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param life
	 *            the life
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional predicate, lifetime, and owner agent.",
			examples = @example (
					value = "new_mental_state(\"belief\", raining, 10, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Predicate pred, final Integer life,
			final IAgent ag) throws GamaRuntimeException {
		return new MentalState(modality, pred, life, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @param life
	 *            the life
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional predicate, strength, lifetime and owner agent.",
			examples = @example (
					value = "new_mental_state(\"belief\",raining, 12.3, 10, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Predicate pred, final Double strength,
			final Integer life, final IAgent ag) throws GamaRuntimeException {
		return new MentalState(modality, pred, strength, life, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional mental state it is about",
			examples = @example (
					value = "new_mental_state(\"belief\", mental_state1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final MentalState pred)
			throws GamaRuntimeException {
		return new MentalState(modality, pred);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state  with an additional mental state it is about and a strength",
			examples = @example (
					value = "new_mental_state(\"belief\", mental_state1, 12.3)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final MentalState pred, final Double strength)
			throws GamaRuntimeException {
		return new MentalState(modality, pred, strength);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional mental state it is about and the owner  agent",
			examples = @example (
					value = "new_mental_state(\"belief\", mental_state1, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final MentalState pred, final IAgent ag)
			throws GamaRuntimeException {
		return new MentalState(modality, pred, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param life
	 *            the life
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional mental state it is about and a lifetime",
			examples = @example (
					value = "new_mental_state(\"belief\", mental_state1, 10)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final MentalState pred, final Integer life)
			throws GamaRuntimeException {
		return new MentalState(modality, pred, life);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional mental state it is about, a stength, and an owner agent",
			examples = @example (
					value = "new_mental_state(\"belief\", mental_state1, 12.2, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final MentalState pred, final Double strength,
			final IAgent ag) throws GamaRuntimeException {
		return new MentalState(modality, pred, strength, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @param life
	 *            the life
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional mental state it is about, a stength, and a lifetime.",
			examples = @example (
					value = "new_mental_state(\"belief\", mental_state1, 12.3, 10)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final MentalState pred, final Double strength,
			final Integer life) throws GamaRuntimeException {
		return new MentalState(modality, pred, strength, life);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param life
	 *            the life
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional mental state it is about, a stength, and an owner agent.",
			examples = @example (
					value = "new_mental_state(\"belief\", mental_state1, 10, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final MentalState pred, final Integer life,
			final IAgent ag) throws GamaRuntimeException {
		return new MentalState(modality, pred, life, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @param life
	 *            the life
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional mental state it is about, a stength, lifetime, and an owner agent",
			examples = @example (
					value = "new_mental_state(\"belief\", mental_state1, 12.3, 10, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final MentalState pred, final Double strength,
			final Integer life, final IAgent ag) throws GamaRuntimeException {
		return new MentalState(modality, pred, strength, life, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	// Remplacer avec les émotions
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional emotion it is about",
			examples = @example (
					value = "new_mental_state(\"belief\", my_joy)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Emotion pred) throws GamaRuntimeException {
		return new MentalState(modality, pred);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional emotion it is about and a strength.",
			examples = @example (
					value = "new_mental_state(\"belief\", my_joy, 12.3)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Emotion pred, final Double strength)
			throws GamaRuntimeException {
		return new MentalState(modality, pred, strength);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional emotion it is about and an owner agent.",
			examples = @example (
					value = "new_mental_state(\"belief\", my_joy, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Emotion pred, final IAgent ag)
			throws GamaRuntimeException {
		return new MentalState(modality, pred, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param life
	 *            the life
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional emotion it is about and a lifetime.",
			examples = @example (
					value = "new_mental_state(\"belief\",  my_joy, 10)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Emotion pred, final Integer life)
			throws GamaRuntimeException {
		return new MentalState(modality, pred, life);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional emotion it is about, a stength, and an owner agent.",
			examples = @example (
					value = "new_mental_state(\"belief\", my_joy, 12.3, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Emotion pred, final Double strength,
			final IAgent ag) throws GamaRuntimeException {
		return new MentalState(modality, pred, strength, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @param life
	 *            the life
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional emotion it is about, a strength, and a lifetime.",
			examples = @example (
					value = "new_mental_state(\"belief\", my_joy, 12.3, 10)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Emotion pred, final Double strength,
			final Integer life) throws GamaRuntimeException {
		return new MentalState(modality, pred, strength, life);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param life
	 *            the life
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional emotion it is about, a lifetime, and an owner agent.",
			examples = @example (
					value = "new_mental_state(\"belief\", my_joy, 10, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Emotion pred, final Integer life,
			final IAgent ag) throws GamaRuntimeException {
		return new MentalState(modality, pred, life, ag);
	}

	/**
	 * New mental state.
	 *
	 * @param modality
	 *            the modality
	 * @param pred
	 *            the pred
	 * @param strength
	 *            the strength
	 * @param life
	 *            the life
	 * @param ag
	 *            the ag
	 * @return the mental state
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "new_mental_state",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "a new mental state with an additional emotion it is about, a strength, a lifetime, and an owner agent.",
			examples = @example (
					value = "new_mental_state(\"belief\", my_joy, 12.3, 10, agent1)",
					isExecutable = false))
	@no_test
	public static MentalState newMentalState(final String modality, final Emotion pred, final Double strength,
			final Integer life, final IAgent ag) throws GamaRuntimeException {
		return new MentalState(modality, pred, strength, life, ag);
	}

	/**
	 * Sets the modalitity.
	 *
	 * @param mental
	 *            the mental
	 * @param modality
	 *            the modality
	 * @return the mental state
	 */
	@operator (
			value = "set_modality",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the modality value of the given mental state",
			examples = @example (
					value = "mental state set_modality belief",
					isExecutable = false))
	@no_test
	public static MentalState setModalitity(final MentalState mental, final String modality) {
		mental.setModality(modality);
		return mental;
	}

	/**
	 * Sets the predicate.
	 *
	 * @param mental
	 *            the mental
	 * @param predicate
	 *            the predicate
	 * @return the mental state
	 */
	@operator (
			value = "set_predicate",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the predicate value of the given mental state",
			examples = @example (
					value = "mental state set_predicate pred1",
					isExecutable = false))
	@no_test
	public static MentalState setPredicate(final MentalState mental, final Predicate predicate) {
		mental.setPredicate(predicate);
		return mental;
	}

	/**
	 * Sets the strength.
	 *
	 * @param mental
	 *            the mental
	 * @param strength
	 *            the strength
	 * @return the mental state
	 */
	@operator (
			value = "set_strength",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the strength value of the given mental state",
			examples = @example (
					value = "mental state set_strength 1.0",
					isExecutable = false))
	@no_test
	public static MentalState setStrength(final MentalState mental, final Double strength) {
		mental.setStrength(strength);
		return mental;
	}

	/**
	 * Sets the lifetime.
	 *
	 * @param mental
	 *            the mental
	 * @param life
	 *            the life
	 * @return the mental state
	 */
	@operator (
			value = "set_lifetime",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "change the lifetime value of the given mental state",
			examples = @example (
					value = "mental state set_lifetime 1",
					isExecutable = false))
	@no_test
	public static MentalState setLifetime(final MentalState mental, final int life) {
		mental.setLifeTime(life);
		return mental;
	}

	/**
	 * Gets the modality.
	 *
	 * @param mental
	 *            the mental
	 * @return the modality
	 */
	@operator (
			value = "get_modality",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the modality value of the given mental state",
			examples = @example (
					value = "get_modality(mental_state1)",
					isExecutable = false))
	@test ("get_modality(new_mental_state('Belief',new_predicate('test1')))='Belief'")
	public static String getModality(final MentalState mental) {
		if (mental != null) return mental.getModality();
		return null;
	}

	/**
	 * Gets the predicate.
	 *
	 * @param mental
	 *            the mental
	 * @return the predicate
	 */
	@operator (
			value = "get_predicate",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the predicate value of the given mental state",
			examples = @example (
					value = "get_predicate(mental_state1)",
					isExecutable = false))
	@no_test
	public static Predicate getPredicate(final MentalState mental) {
		if (mental != null) return mental.getPredicate();
		return null;
	}

	/**
	 * Gets the strength.
	 *
	 * @param mental
	 *            the mental
	 * @return the strength
	 */
	@operator (
			value = "get_strength",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the strength value of the given mental state",
			examples = @example (
					value = "get_strength(mental_state1)",
					isExecutable = false))
	@test ("get_strength(new_mental_state('Belief',new_predicate('test1')))=1.0")
	public static Double getStrength(final MentalState mental) {
		if (mental != null) return mental.getStrength();
		return null;
	}

	/**
	 * Gets the lifetime.
	 *
	 * @param mental
	 *            the mental
	 * @return the lifetime
	 */
	@operator (
			value = "get_lifetime",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the lifetime value of the given mental state",
			examples = @example (
					value = "get_lifetime(mental_state1)",
					isExecutable = false))
	@test ("get_lifetime(new_mental_state('Belief',new_predicate('test1'),4))=4")
	public static int getLifetime(final MentalState mental) {
		if (mental != null) return mental.getLifeTime();
		return -1;
	}

	/**
	 * Gets the plan name.
	 *
	 * @param plan
	 *            the plan
	 * @return the plan name
	 */
	@operator (
			value = "get_plan_name",
			can_be_const = true,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the name of a given plan",
			examples = @example (
					value = "get_plan_name(agent.current_plan)",
					isExecutable = false))
	@no_test
	public static String getPlanName(final BDIPlan plan) {
		if (plan != null && plan.getPlanStatement() != null) return plan.getPlanStatement().getName();
		return null;
	}

	/**
	 * Gets the beliefs name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the beliefs name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	// example of transformation of actions to operator
	@operator (
			value = "get_beliefs_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the list of beliefs in the belief base which predicate has the given name.",
			returns = "the list of beliefs (mental state).",
			examples = { @example (
					value = "get_beliefs_with_name_op(self,\"has_water\")",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getBeliefsName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("belief_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the belief name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the belief name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_belief_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the belief in the belief base with the given name.",
			returns = "the belief (mental state).",
			examples = { @example (
					value = "get_belief_with_name_op(self,\"has_water\")",
					equals = "nil") })
	public static MentalState getBeliefName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("belief_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName()))
					return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the belief.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the belief
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_belief_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the belief in the belief base with the given predicate.",
			returns = "the belief (mental state).",
			examples = { @example (
					value = "get_belief_op(self,predicate(\"has_water\"))",
					equals = "nil") })
	public static MentalState getBelief(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("belief_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the beliefs.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the beliefs
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_beliefs_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the beliefs in the belief base with the given predicate.",
			returns = "the list of belief (mental state).",
			examples = { @example (
					value = "get_beliefs_op(self,predicate(\"has_water\"))",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getBeliefs(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("belief_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { predicates.add(mental); }
			}
		}
		return predicates;
	}

	/**
	 * Gets the desires name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the desires name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_desires_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the list of desires in the desire base which predicate has the given name.",
			returns = "the list of desires (mental state).",
			examples = { @example (
					value = "get_desires_with_name_op(self,\"has_water\")",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getDesiresName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("desire_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the desire name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the desire name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_desire_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the desire in the desire base with the given name.",
			returns = "the desire (mental state).",
			examples = { @example (
					value = "get_desire_with_name_op(self,\"has_water\")",
					equals = "nil") })
	public static MentalState getDesireName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("desire_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName()))
					return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the desire.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the desire
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_desire_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the desire in the desire base with the given predicate.",
			returns = "the belief (mental state).",
			examples = { @example (
					value = "get_belief_op(self,predicate(\"has_water\"))",
					equals = "nil") })
	public static MentalState getDesire(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("desire_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the desires.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the desires
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_desires_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the desires in the desire base with the given predicate.",
			returns = "the list of desire (mental state).",
			examples = { @example (
					value = "get_desires_op(self,predicate(\"has_water\"))",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getDesires(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("desire_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { predicates.add(mental); }
			}
		}
		return predicates;
	}

	/**
	 * Gets the uncertainties name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the uncertainties name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_uncertainties_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the list of uncertainties in the uncertainty base which predicate has the given name.",
			returns = "the list of uncertainties (mental state).",
			examples = { @example (
					value = "get_uncertainties_with_name_op(self,\"has_water\")",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getUncertaintiesName(final IScope scope, final IAgent ag,
			final String predicateName) throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("uncertainty_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the uncertainty name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the uncertainty name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_uncertainty_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the uncertainty in the uncertainty base with the given name.",
			returns = "the unertainty (mental state).",
			examples = { @example (
					value = "get_uncertainty_with_name_op(self,\"has_water\")",
					equals = "nil") })
	public static MentalState getUncertaintyName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("uncertainty_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName()))
					return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the uncertainty.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the uncertainty
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_uncertainty_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the uncertainty in the uncertainty base with the given predicate.",
			returns = "the uncertainty (mental state).",
			examples = { @example (
					value = "get_uncertainty_op(self,predicate(\"has_water\"))",
					equals = "nil") })
	public static MentalState getUncertainty(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("uncertainty_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the uncertainties.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the uncertainties
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_uncertainties_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the uncertainties in the uncertainty base with the given predicate.",
			returns = "the list of uncertainties (mental state).",
			examples = { @example (
					value = "get_uncertainties_op(self,predicate(\"has_water\"))",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getUncertainties(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("uncertainty_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { predicates.add(mental); }
			}
		}
		return predicates;
	}

	/**
	 * Gets the ideals name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the ideals name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_ideals_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the list of ideals in the ideal base which predicate has the given name.",
			returns = "the list of ideals (mental state).",
			examples = { @example (
					value = "get_ideals_with_name_op(self,\"has_water\")",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getIdealsName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("ideal_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the ideal name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the ideal name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_ideal_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the ideal in the ideal base with the given name.",
			returns = "the ideal (mental state).",
			examples = { @example (
					value = "get_ideal_with_name_op(self,\"has_water\")",
					equals = "nil") })
	public static MentalState getIdealName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("ideal_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName()))
					return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the ideal.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the ideal
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_ideal_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the ideal in the ideal base with the given name.",
			returns = "the ideal (mental state).",
			examples = { @example (
					value = "get_ideal_op(self,predicate(\"has_water\"))",
					equals = "nil") })
	public static MentalState getIdeal(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("ideal_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the ideals.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the ideals
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_ideals_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the ideal in the ideal base with the given name.",
			returns = "the list of ideals (mental state).",
			examples = { @example (
					value = "get_ideals_op(self,predicate(\"has_water\"))",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getIdeals(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("ideal_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { predicates.add(mental); }
			}
		}
		return predicates;
	}

	/**
	 * Gets the obligations name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the obligations name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_obligations_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the list of obligations in the obligation base which predicate has the given name.",
			returns = "the list of obligations (mental state).",
			examples = { @example (
					value = "get_obligations_with_name_op(self,\"has_water\")",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getObligationsName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("obligation_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the obligation name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the obligation name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_obligation_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the obligation in the obligation base with the given name.",
			returns = "the obligation (mental state).",
			examples = { @example (
					value = "get_obligation_with_name_op(self,\"has_water\")",
					equals = "nil") })
	public static MentalState getObligationName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("obligation_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName()))
					return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the obligation.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the obligation
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_obligation_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the obligation in the obligation base with the given predicate.",
			returns = "the obligation (mental state).",
			examples = { @example (
					value = "get_obligation_op(self,predicate(\"has_water\"))",
					equals = "nil") })
	public static MentalState getObligation(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("obligation_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the obligations.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the obligations
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_obligations_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the obligations in the obligation base with the given predicate.",
			returns = "the list of obligations (mental state).",
			examples = { @example (
					value = "get_obligations_op(self,predicate(\"has_water\"))",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getObligations(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("obligation_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { predicates.add(mental); }
			}
		}
		return predicates;
	}

	/**
	 * Gets the intentions name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the intentions name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_intentions_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the list of intentions in the intention base which predicate has the given name.",
			returns = "the list of intentions (mental state).",
			examples = { @example (
					value = "get_intentions_with_name_op(self,\"has_water\")",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getIntentionsName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("intention_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName())) {
					predicates.add(mental);
				}
			}
		}
		return predicates;
	}

	/**
	 * Gets the intention name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predicateName
	 *            the predicate name
	 * @return the intention name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_intention_with_name_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the intention in the intention base with the given name.",
			returns = "the intention (mental state).",
			examples = { @example (
					value = "get_intention_with_name_op(self,\"has_water\")",
					isExecutable = false) })
	@no_test
	public static MentalState getIntentionName(final IScope scope, final IAgent ag, final String predicateName)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (predicateName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("intention_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predicateName.equals(mental.getPredicate().getName()))
					return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the intention.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the intention
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_intention_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the intention in the intention base with the given predicate.",
			returns = "the intention (mental state).",
			examples = { @example (
					value = "get_intention_op(self,predicate(\"has_water\"))",
					isExecutable = false) })
	@no_test
	public static MentalState getIntention(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("intention_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) return mental;
			}
		}
		return null;
	}

	/**
	 * Gets the intentions.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the intentions
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_intentions_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the intentions in the intention base with the given predicate.",
			returns = "the list of intentions (mental state).",
			examples = { @example (
					value = "get_intentions_op(self,predicate(\"has_water\"))",
					isExecutable = false) })
	@no_test
	public static IList<MentalState> getIntentions(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		final IList<MentalState> predicates = GamaListFactory.create();
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return predicates;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("intention_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { predicates.add(mental); }
			}
		}
		return predicates;
	}

	/**
	 * Gets the current intention.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @return the current intention
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "get_current_intention_op",
			can_be_const = true,
			content_type = MentalStateType.id,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "get the current intention.",
			returns = "the current intention (mental state).",
			examples = { @example (
					value = "get_current_intention_op(self)",
					equals = "nil") })
	public static MentalState getCurrentIntention(final IScope scope, final IAgent ag) throws GamaRuntimeException {
		// final MentalState predicate = new MentalState("Belief");
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return null;
		final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("intention_base");
		// if (beliefs == null) { return null; }
		if (!beliefs.isEmpty()) return beliefs.lastValue(scope);
		return null;
	}

	/**
	 * Checks for belief.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_belief_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is a belief about the given predicate.",
			returns = "true if a belief already exists.",
			examples = { @example (
					value = "has_belief_op(self,predicate(\"has_water\"))",
					equals = "false") })
	public static Boolean hasBelief(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("belief_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { result = true; }
			}
		}
		return result;
	}

	/**
	 * Checks for belief name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predName
	 *            the pred name
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_belief_with_name_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is a belief about the given name.",
			returns = "true if a belief already exists.",
			examples = { @example (
					value = "has_belief_with_name_op(self,\"has_water\")",
					equals = "false") })
	public static Boolean hasBeliefName(final IScope scope, final IAgent ag, final String predName)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (predName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("belief_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predName.equals(mental.getPredicate().getName())) {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * Checks for desire.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_desire_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is a desire about the given predicate.",
			returns = "true if a desire already exists.",
			examples = { @example (
					value = "has_desire_op(self,predicate(\"has_water\"))",
					equals = "false") })
	public static Boolean hasDesire(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("desire_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { result = true; }
			}
		}
		return result;
	}

	/**
	 * Checks for desire name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predName
	 *            the pred name
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_desire_with_name_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is a desire about the given name.",
			returns = "true if a desire already exists.",
			examples = { @example (
					value = "has_desire_with_name_op(self,\"has_water\")",
					equals = "false") })
	public static Boolean hasDesireName(final IScope scope, final IAgent ag, final String predName)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (predName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("desire_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predName.equals(mental.getPredicate().getName())) {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * Checks for uncertainty.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_uncertainty_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is an uncertainty about the given predicate.",
			returns = "true if an uncertainty already exists.",
			examples = { @example (
					value = "has_uncertainty_op(self,predicate(\"has_water\"))",
					equals = "false") })
	public static Boolean hasUncertainty(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("uncertainty_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { result = true; }
			}
		}
		return result;
	}

	/**
	 * Checks for uncertainty name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predName
	 *            the pred name
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_uncertainty_with_name_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is an uncertainty about the given name.",
			returns = "true if an uncertainty already exists.",
			examples = { @example (
					value = "has_uncertainty_with_name_op(self,\"has_water\")",
					equals = "false") })
	public static Boolean hasUncertaintyName(final IScope scope, final IAgent ag, final String predName)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (predName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("uncertainty_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predName.equals(mental.getPredicate().getName())) {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * Checks for ideal.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_ideal_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is an ideal about the given predicate.",
			returns = "true if an ideal already exists.",
			examples = { @example (
					value = "has_ideal_op(self,predicate(\"has_water\"))",
					equals = "false") })
	public static Boolean hasIdeal(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("ideal_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { result = true; }
			}
		}
		return result;
	}

	/**
	 * Checks for ideal name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predName
	 *            the pred name
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_ideal_with_name_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is an ideal about the given name.",
			returns = "true if an ideal already exists.",
			examples = { @example (
					value = "has_ideal_with_name_op(self,\"has_water\")",
					equals = "false") })
	public static Boolean hasIdealName(final IScope scope, final IAgent ag, final String predName)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (predName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("ideal_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predName.equals(mental.getPredicate().getName())) {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * Checks for intention.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_intention_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is an intention about the given predicate.",
			returns = "true if an intention already exists.",
			examples = { @example (
					value = "has_intention_op(self,predicate(\"has_water\"))",
					equals = "false") })
	public static Boolean hasIntention(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("intention_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { result = true; }
			}
		}
		return result;
	}

	/**
	 * Checks for intention name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predName
	 *            the pred name
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_intention_with_name_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is an intention about the given name.",
			returns = "true if an intention already exists.",
			examples = { @example (
					value = "has_intention_with_name_op(self,\"has_water\")",
					equals = "false") })
	public static Boolean hasIntentionName(final IScope scope, final IAgent ag, final String predName)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (predName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("intention_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predName.equals(mental.getPredicate().getName())) {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * Checks for obligation.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param pred
	 *            the pred
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_obligation_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is an obligation about the given predicate.",
			returns = "true if an obligation already exists.",
			examples = { @example (
					value = "has_obligation_op(self,predicate(\"has_water\"))",
					equals = "false") })
	public static Boolean hasObligation(final IScope scope, final IAgent ag, final Predicate pred)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (pred != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("obligation_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && pred.equals(mental.getPredicate())) { result = true; }
			}
		}
		return result;
	}

	/**
	 * Checks for obligation name.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 * @param predName
	 *            the pred name
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "has_obligation_with_name_op",
			can_be_const = true,
			content_type = IType.BOOL,
			category = { "BDI" },
			concept = { IConcept.BDI })
	@doc (
			value = "indicates if there already is an obligation about the given name.",
			returns = "true if an obligation already exists.",
			examples = { @example (
					value = "has_obligation_with_name_op(self,\"has_water\")",
					equals = "false") })
	public static Boolean hasObligationName(final IScope scope, final IAgent ag, final String predName)
			throws GamaRuntimeException {
		boolean result = false;
		if (!(ag.getSpecies().getArchitecture() instanceof SimpleBdiArchitecture)) return result;
		if (predName != null) {
			final IList<MentalState> beliefs = (IList<MentalState>) ag.getAttribute("obligation_base");
			for (final MentalState mental : beliefs) {
				if (mental.getPredicate() != null && predName.equals(mental.getPredicate().getName())) {
					result = true;
				}
			}
		}
		return result;
	}

}

package gama.extension.bdi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.api.gaml.GAML;
import gama.api.gaml.types.GamaIntegerType;
import gama.api.gaml.types.GamaListType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gaml.compiler.expressions.GamlExpressionFactory;

/** Standalone numeric compatibility checks for #161. Run with the BDI bundle's runtime classpath. */
public final class BdiNumericContractTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static void same(final double actual, final double expected, final String message) {
		check(Double.doubleToLongBits(actual) == Double.doubleToLongBits(expected),
				message + ": expected " + expected + ", got " + actual);
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private static final class Context {
		final Map<String, Object> attributes = new HashMap<>();
		double timeStep = 2.5;
		final IAgent agent = proxy(IAgent.class, (p, method, args) -> switch (method.getName()) {
			case "getAttribute" -> attributes.get(args[0]);
			case "setAttribute" -> attributes.put((String) args[0], args[1]);
			case "equals" -> p == args[0];
			case "hashCode" -> System.identityHashCode(p);
			case "toString" -> "numeric-test-agent";
			default -> throw new AssertionError("Unexpected agent call: " + method);
		});
		final ISimulationAgent simulation = proxy(ISimulationAgent.class, (p, method, args) -> {
			check(method.getName().equals("getTimeStep"), "Unexpected simulation call: " + method);
			return timeStep;
		});
		final IScope scope = proxy(IScope.class, (p, method, args) -> switch (method.getName()) {
			case "getAgent" -> agent;
			case "getSimulation" -> simulation;
			case "hasArg" -> false;
			case "getBoolArgIfExists" -> args[1];
			default -> throw new AssertionError("Unexpected scope call: " + method);
		});

		Context(final boolean personality) {
			for (final MentalStateKind kind : MentalStateKind.values()) {
				attributes.put(kind.base(), GamaListFactory.<MentalState>create());
			}
			attributes.put(SimpleBdiArchitecture.EMOTION_BASE, GamaListFactory.<Emotion>create());
			attributes.put(SimpleBdiArchitecture.SOCIALLINK_BASE, GamaListFactory.<SocialLink>create());
			attributes.put(SimpleBdiArchitecture.USE_PERSONALITY, personality);
			attributes.put(SimpleBdiArchitecture.NEUROTISM, 0.4);
			attributes.put(SimpleBdiArchitecture.OPENNESS, 0.3);
			attributes.put(SimpleBdiArchitecture.AGREEABLENESS, 0.6);
		}

		IList<Emotion> emotions() { return BdiUtils.getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE); }

		Emotion emotion(final String name) {
			return emotions().stream().filter(e -> name.equals(e.getName())).findFirst().orElseThrow();
		}
	}

	public static void main(final String[] args) throws Exception {
		if (GAML.getExpressionFactory() == null) GAML.registerExpressionFactory(GamlExpressionFactory.getInstance());
		if (Types.INT == null) Types.cache(new GamaIntegerType(Types.getBuiltInTypeManager()));
		if (Types.LIST == null) Types.cache(new GamaListType(Types.getBuiltInTypeManager()));
		formulas();
		numericValuesAndPresence();
		desireAppraisals();
		confirmedEmotions();
		socialAndIdealAppraisals();
		compoundEmotions();
		System.out.println("BDI numeric compatibility checks passed");
	}

	private static void formulas() {
		final var context = new Context(true);
		final double[] inputs = { -1, -0.0, 0, 0.1, 0.5, 1, 12.3, Double.MIN_VALUE, Double.MAX_VALUE,
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN };
		for (final double first : inputs) {
			context.timeStep = first;
			for (final double second : inputs) {
				for (final double third : inputs) {
					same(BdiUtils.emotionDecay(context.scope, second, third), first * 0.00028 * second * third,
							"Legacy decay coefficient and arithmetic order");
					double intensity = first * second * (1 + (0.5 - third));
					if (intensity > 1.0) intensity = 1.0;
					if (intensity < 0) intensity = 0.0;
					same(BdiUtils.desireEmotionIntensity(first, second, third), intensity, "Desire intensity");
				}
				same(BdiUtils.compoundEmotionIntensity(new Emotion("joy", first), new Emotion("pride", second)),
						first * second, "Compound intensities are not clamped");
			}
		}
		same(BdiUtils.compoundEmotionIntensity(new Emotion("joy"), new Emotion("pride", 0.2)), 1,
				"Unspecified first intensity");
		same(BdiUtils.compoundEmotionIntensity(new Emotion("joy", 0.2), new Emotion("pride")), 1,
				"Unspecified second intensity");
		same(BdiUtils.compoundEmotionIntensity(new Emotion("joy", -1), new Emotion("pride", 0.2)), -0.2,
				"Explicit -1 is not an omitted intensity");
	}

	private static void numericValuesAndPresence() {
		final Emotion unspecified = new Emotion("joy");
		same(unspecified.getIntensity(), -1, "Unspecified intensity");
		same(unspecified.getDecay(), 0, "Default decay");
		check(!unspecified.hasIntensity(), "Default presence flag");
		BdiOperators.withIntensity(unspecified, 12.3);
		same(unspecified.getIntensity(), 12.3, "with_intensity retains values above 1");
		check(!unspecified.hasIntensity(), "with_intensity retains the historical presence flag");
		unspecified.setIntensity(-1);
		check(unspecified.hasIntensity(), "Setter marks explicit -1 as supplied");
		for (final double value : new double[] { -2, -1, -0.0, 0, 1, 12.3, Double.NaN, Double.POSITIVE_INFINITY }) {
			final Emotion emotion = BdiOperators.newEmotion("joy", value, value);
			same(emotion.getIntensity(), value, "Constructor intensity is not normalised");
			same(emotion.getDecay(), value, "Constructor decay is not normalised");
			check(emotion.hasIntensity(), "Constructor intensity is explicit");
			emotion.setIntensity(value);
			emotion.setDecay(value);
			same(emotion.getIntensity(), value, "Setter intensity");
			same(emotion.getDecay(), value, "Setter decay");
			BdiOperators.withDecay(emotion, value);
			same(emotion.getDecay(), value, "Decay operator");
			final SocialLink social = new SocialLink(null, value, value, value, value, value);
			same(social.getLiking(), value, "Social constructor range policy");
			same(social.getTrust(), value, "Social trust constructor range policy");
			social.setDominance(value);
			social.setSolidarity(value);
			social.setFamiliarity(value);
			check(social.hasDominance() && social.hasSolidarity() && social.hasFamiliarity(), "Social presence flags");
			same(social.getDominance(), value, "Dominance setter range policy");
			same(social.getSolidarity(), value, "Solidarity setter range policy");
			same(social.getFamiliarity(), value, "Familiarity setter range policy");
		}
		final var social = new SocialLink();
		BdiOperators.withDominance(social, 1);
		check(!social.hasDominance(), "with_dominance still excludes 1");
		BdiOperators.withDominance(social, -1);
		same(social.getDominance(), -1, "with_dominance includes -1");
		BdiOperators.withLiking(social, 1);
		BdiOperators.withSolidarity(social, 1);
		BdiOperators.withFamiliarity(social, 1);
		BdiOperators.withTrust(social, -1);
		same(social.getLiking(), 1, "Liking upper endpoint");
		same(social.getSolidarity(), 1, "Solidarity upper endpoint");
		same(social.getFamiliarity(), 1, "Familiarity upper endpoint");
		same(social.getTrust(), -1, "Trust lower endpoint");
		BdiOperators.withLiking(social, 12.3);
		BdiOperators.withSolidarity(social, -1);
		BdiOperators.withFamiliarity(social, Double.NaN);
		BdiOperators.withTrust(social, 2);
		same(social.getLiking(), 1, "Rejected liking leaves value unchanged");
		same(social.getSolidarity(), 1, "Rejected solidarity leaves value unchanged");
		same(social.getFamiliarity(), 1, "Rejected familiarity leaves value unchanged");
		same(social.getTrust(), -1, "Rejected trust leaves value unchanged");
		final Emotion growing = new Emotion("joy", 0.2, -0.1);
		growing.decayIntensity();
		same(growing.getIntensity(), 0.2 - -0.1, "Negative decay increases intensity without clamping");
		final var context = new Context(false);
		context.emotions().add(new Emotion("joy", 0.1, 0.2));
		context.emotions().add(new Emotion("hope"));
		new SimpleBdiArchitecture().updateEmotionsIntensity(context.scope);
		check(context.emotions().size() == 1 && context.emotion("hope").getIntensity() == -1,
				"Emotion update removes depleted values but retains the -1 sentinel");
	}

	private static void generate(final String method, final Context context, final MentalState state) throws Exception {
		final var generation = BdiUtils.class.getDeclaredMethod(method, IScope.class, MentalState.class);
		generation.setAccessible(true);
		generation.invoke(null, context.scope, state);
	}

	private static void desireAppraisals() throws Exception {
		for (final boolean personality : List.of(false, true)) {
			for (final boolean opposite : List.of(false, true)) {
				for (final boolean uncertain : List.of(false, true)) {
					for (final double strength : new double[] { -1, 0, 0.7, 5 }) {
						final var context = new Context(personality);
						BdiUtils.getBase(context.scope, MentalStateKind.DESIRE)
								.add(new MentalState("Desire", new Predicate("fact", !opposite), 0.6));
						generate(uncertain ? "createHopeFromMentalState" : "createJoyFromPredicate", context,
								new MentalState("Belief", new Predicate("fact"), strength));
						final Emotion emotion = context.emotion(uncertain ? opposite ? "fear" : "hope"
								: opposite ? "sadness" : "joy");
						final double intensity = personality && strength >= 0
								? Math.min(1, strength * 0.6 * (1 + (0.5 - 0.4))) : 1;
						same(emotion.getIntensity(), intensity, "Appraisal intensity");
						same(emotion.getDecay(), personality ? 2.5 * 0.00028 * 0.4 * intensity : 0, "Appraisal decay");
					}
				}
			}
		}
	}

	private static void confirmedEmotions() throws Exception {
		for (final boolean personality : List.of(false, true)) {
			for (final String source : List.of("hope", "fear")) {
				for (final boolean opposite : List.of(false, true)) {
					for (final boolean explicit : List.of(false, true)) {
						final var context = new Context(personality);
						final Predicate about = new Predicate("fact");
						context.emotions().add(explicit ? new Emotion(source, 0.6, about) : new Emotion(source, about));
						generate("createSatisfactionFromMentalState", context,
								new MentalState("Belief", new Predicate("fact", !opposite)));
						final String target = source.equals("hope") ? opposite ? "disappointment" : "satisfaction"
								: opposite ? "relief" : "fear_confirmed";
						final Emotion emotion = context.emotion(target);
						same(emotion.getIntensity(), explicit ? 0.6 : -1, "Transferred intensity");
						check(emotion.hasIntensity() == explicit, "Transferred presence");
						same(emotion.getDecay(), personality ? 2.5 * 0.00028 * 0.4 * (explicit ? 0.6 : -1) : 0,
								"Transferred decay, including negative unspecified-intensity decay");
					}
				}
			}
		}
	}

	private static void socialAndIdealAppraisals() throws Exception {
		for (final boolean personality : List.of(false, true)) {
			final var context = new Context(personality);
			final var other = new Context(false);
			final Emotion perceived = new Emotion("joy", 0.6, new Predicate("fact"));
			perceived.setOwner(other.agent);
			BdiUtils.getSocialBase(context.scope, SimpleBdiArchitecture.SOCIALLINK_BASE)
					.add(new SocialLink(other.agent, 0.5, 0, 0, 0));
			generate("createHappyForFromMentalState", context, new MentalState("Belief", perceived));
			final Emotion happy = context.emotion("happy_for");
			same(happy.getIntensity(), personality ? 0.6 * 0.5 * (1 - (0.5 - 0.6)) : 1, "Social intensity");
			same(happy.getDecay(), personality ? 2.5 * 0.00028 * 0.4 * -1 : 0,
					"Social decay retains the pre-assignment unspecified intensity");
			final Predicate about = new Predicate("ideal", context.agent);
			BdiUtils.getBase(context.scope, MentalStateKind.IDEAL).add(new MentalState("Ideal", about, 0.7));
			generate("createPrideFromMentalState", context, new MentalState("Belief", about, 0.6));
			for (final String name : List.of("pride", "admiration", "shame", "reproach")) {
				same(context.emotion(name).getIntensity(), personality ? 0.6 * Math.abs(0.7) * (1 + (0.5 - 0.3)) : 1,
						"Ideal appraisal intensity");
				same(context.emotion(name).getDecay(), personality ? 2.5 * 0.00028 * 0.4 * -1 : 0,
						"Ideal appraisal retains negative decay");
			}
		}
	}

	private static void compoundEmotions() throws Exception {
		for (final boolean personality : List.of(false, true)) {
			for (final boolean explicit : List.of(false, true)) {
				for (final String target : List.of("gratification", "gratitude", "remorse", "anger")) {
					final var context = new Context(personality);
					final Predicate about = new Predicate("fact", context.agent);
					final boolean positive = target.equals("gratification") || target.equals("gratitude");
					final String other = switch (target) {
						case "gratification" -> "pride";
						case "gratitude" -> "admiration";
						case "remorse" -> "shame";
						default -> "reproach";
					};
					context.emotions().add(explicit ? new Emotion(other, 0.7, about) : new Emotion(other, about));
					final var generation = BdiUtils.class.getDeclaredMethod(
							positive ? "createGratificationGratitudeFromJoy" : "createRemorseAngerFromSadness",
							IScope.class, Emotion.class);
					generation.setAccessible(true);
					generation.invoke(null, context.scope, new Emotion(positive ? "joy" : "sadness", 0.6, about));
					final Emotion emotion = context.emotion(target);
					final double intensity = personality && explicit ? 0.6 * 0.7 : 1;
					same(emotion.getIntensity(), intensity, "Compound generated intensity");
					same(emotion.getDecay(), personality ? 2.5 * 0.00028 * 0.4 * intensity : 0, "Compound decay");
				}
			}
		}
	}
}

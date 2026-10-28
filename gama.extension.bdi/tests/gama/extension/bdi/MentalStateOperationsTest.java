package gama.extension.bdi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.GamaIntegerType;
import gama.api.gaml.types.GamaListType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gaml.compiler.expressions.GamlExpressionFactory;

/** Standalone compatibility checks for #161. Run with the BDI bundle's runtime classpath. */
public final class MentalStateOperationsTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private static final class Context {
		final Map<String, Object> attributes = new HashMap<>();
		final Map<String, Object> arguments = new HashMap<>();
		final IAgent agent = proxy(IAgent.class, (p, method, args) -> switch (method.getName()) {
			case "getAttribute" -> attributes.get(args[0]);
			case "setAttribute" -> attributes.put((String) args[0], args[1]);
			case "equals" -> p == args[0];
			case "hashCode" -> System.identityHashCode(p);
			case "toString" -> "bdi-test-agent";
			default -> throw new AssertionError("Unexpected agent call: " + method);
		});
		final IScope scope = proxy(IScope.class, (p, method, args) -> switch (method.getName()) {
			case "getAgent" -> agent;
			case "hasArg" -> arguments.containsKey(args[0]);
			case "getListArg", "getBoolArg", "getArg", "getTypedArg", "getTypedArgIfExists" ->
				arguments.get(args[0]);
			default -> throw new AssertionError("Unexpected scope call: " + method);
		});

		Context() {
			for (final MentalStateKind kind : MentalStateKind.values()) {
				attributes.put(kind.base(), GamaListFactory.<MentalState>create());
			}
			attributes.put(SimpleBdiArchitecture.EMOTION_BASE, GamaListFactory.<Emotion>create());
			attributes.put(SimpleBdiArchitecture.USE_EMOTIONS_ARCHITECTURE, false);
		}

		IList<MentalState> base(final MentalStateKind kind) { return BdiUtils.getBase(scope, kind); }
	}

	public static void main(final String[] args) throws Exception {
		if (GAML.getExpressionFactory() == null) GAML.registerExpressionFactory(GamlExpressionFactory.getInstance());
		if (Types.INT == null) Types.cache(new GamaIntegerType(Types.getBuiltInTypeManager()));
		if (Types.LIST == null) Types.cache(new GamaListType(Types.getBuiltInTypeManager()));
		constructors();
		builtInKinds();
		lookups();
		specialisedUpdates();
		focusedStates();
		System.out.println("BDI mental-state compatibility checks passed");
	}

	private static void constructors() throws Exception {
		final var context = new Context();
		final var predicate = new Predicate("fact");
		final var nested = new MentalState("custom");
		final var emotion = new Emotion("joy");
		for (final var constructor : MentalState.class.getConstructors()) {
			final Class<?>[] types = constructor.getParameterTypes();
			final Object[] args = new Object[types.length];
			Object payload = null;
			for (int i = 0; i < types.length; i++) {
				args[i] = switch (types[i].getSimpleName()) {
					case "String" -> "CustomModality";
					case "Predicate" -> predicate;
					case "MentalState" -> nested;
					case "Emotion" -> emotion;
					case "Double" -> 0.7;
					case "int" -> 12;
					case "IAgent" -> context.agent;
					default -> throw new AssertionError("Unexpected constructor: " + constructor);
				};
				if (i == 1) payload = args[i];
			}
			final MentalState state = MentalState.class.cast(constructor.newInstance(args));
			check(state.getModality().equals(types.length == 0 ? "" : "CustomModality"),
					"Constructors must preserve custom modality strings");
			check(state.getPredicate() == (payload instanceof Predicate ? payload : null), "Predicate payload");
			check(state.getMentalState() == (payload instanceof MentalState ? payload : null), "Nested payload");
			check(state.getEmotion() == (payload instanceof Emotion ? payload : null), "Emotion payload");
			check(state.getStrength() == (List.of(types).contains(Double.class) ? 0.7 : 1.0), "Strength and default");
			check(state.getLifeTime() == (List.of(types).contains(int.class) ? 12 : -1), "Lifetime and default");
			check(state.getOwner() == (List.of(types).contains(IAgent.class) ? context.agent : null), "Owner and default");
			check(state.getSubintentions() == null && state.getOnHoldUntil() == null
					&& state.getSuperIntention() == null && !state.isUpdated, "Intention metadata defaults");

			for (int i = 0; i < types.length; i++) {
				if (!types[i].isPrimitive()) args[i] = null;
			}
			final MentalState nullable = MentalState.class.cast(constructor.newInstance(args));
			check(nullable.getPredicate() == null && nullable.getMentalState() == null && nullable.getEmotion() == null,
					"Null payloads remain accepted");
			check(nullable.getModality() == null || types.length == 0, "Null modality remains accepted");
			if (List.of(types).contains(Double.class)) check(nullable.getStrength() == null, "Null strength remains accepted");
		}
		final var custom = new MentalState("belief");
		check(custom.getModality().equals("belief"), "Public construction must not canonicalise case");
		custom.setModality("ApplicationSpecific");
		check(custom.getModality().equals("ApplicationSpecific"), "Public setter must accept custom modalities");
	}

	private static void builtInKinds() {
		final Map<MentalStateKind, String> expected = Map.of(
				MentalStateKind.BELIEF, "Belief", MentalStateKind.DESIRE, "Desire",
				MentalStateKind.INTENTION, "Intention", MentalStateKind.UNCERTAINTY, "Uncertainty",
				MentalStateKind.IDEAL, "Ideal", MentalStateKind.OBLIGATION, "Obligation");
		final var predicate = new Predicate("fact");
		final var nested = new MentalState("custom");
		final var emotion = new Emotion("joy");
		for (final var entry : expected.entrySet()) {
			final MentalStateKind kind = entry.getKey();
			check(kind.modality().equals(entry.getValue()), "Canonical modality mapping");
			check(kind.base().equals(entry.getValue().toLowerCase() + "_base"), "Base mapping");
			check(kind.matches(kind.create()), "Built-in construction and recognition");
			check(!kind.matches(null) && !kind.matches(new MentalState((String) null))
					&& !kind.matches(new MentalState(entry.getValue().toLowerCase())), "Case-sensitive recognition");
			check(kind.create(predicate).getPredicate() == predicate, "Predicate factory preserves identity");
			check(kind.create(nested).getMentalState() == nested, "Nested factory preserves identity");
			check(kind.create(emotion).getEmotion() == emotion, "Emotion factory preserves identity");
		}
	}

	private static void lookups() {
		final var context = new Context();
		final var facade = new SimpleBdiArchitecture();
		final var predicate = new Predicate("fact");
		for (final MentalStateKind kind : MentalStateKind.values()) {
			final MentalState first = kind.create(predicate);
			final MentalState second = kind.create(new Predicate("fact"));
			context.base(kind).add(kind.create(new Emotion("joy")));
			context.base(kind).add(first);
			context.base(kind).add(second);
			check(BdiUtils.findByPredicate(context.scope, kind, new Predicate("fact")) == first, "First predicate match");
			check(BdiUtils.findByName(context.scope, kind, "fact") == first, "First named match");
			check(BdiUtils.findByPredicate(context.scope, kind, new Predicate("fact", false)) == null,
					"Ordinary lookup must respect truth");
			check(BdiUtils.findByPredicate(context.scope, kind, null) == null
					&& BdiUtils.findByName(context.scope, kind, null) == null, "Null lookup remains absent");
			check(BdiUtils.hasPredicates(context.scope, kind, List.of(predicate)), "Predicate-list membership");
			check(!BdiUtils.hasPredicates(context.scope, kind, List.of(predicate, new Predicate("missing"))),
					"All requested predicates are required");
			check(BdiUtils.hasPredicates(null, kind, List.of()), "Empty requirements need no base");
			check(BdiUtils.getBase(context.scope, kind.base()) == context.base(kind), "String API retains base identity");
			final IList<MentalState> override = GamaListFactory.create();
			context.arguments.put(kind.base(), override);
			check(BdiUtils.getBase(context.scope, kind) == override, "Action base argument takes precedence");
			check(BdiUtils.findByPredicate(context.scope, kind, predicate) == null, "Lookup uses overridden base");
			context.arguments.remove(kind.base());
		}
		context.arguments.put(SimpleBdiArchitecture.PREDICATE, new Predicate("fact", false));
		check(facade.getBelief(context.scope) == context.base(MentalStateKind.BELIEF).get(1),
				"Belief lookup retains its opposite-truth fallback");
		check(facade.getDesire(context.scope) == null, "Desire lookup does not use the belief fallback");
		context.arguments.put(SimpleBdiArchitecture.PREDICATE, predicate);
		check(facade.getDesire(context.scope) == context.base(MentalStateKind.DESIRE).get(1), "Desire facade");
		check(facade.getIntention(context.scope) == context.base(MentalStateKind.INTENTION).get(1), "Intention facade");
		check(facade.getUncertainty(context.scope) == context.base(MentalStateKind.UNCERTAINTY).get(1), "Uncertainty facade");
		check(facade.getIdeal(context.scope) == context.base(MentalStateKind.IDEAL).get(1), "Ideal facade");
		check(facade.getObligation(context.scope) == context.base(MentalStateKind.OBLIGATION).get(1), "Obligation facade");
		context.arguments.put("belief_name", "fact");
		context.arguments.put("desire_name", "fact");
		context.arguments.put("intention_name", "fact");
		check(facade.getBeliefName(context.scope) == context.base(MentalStateKind.BELIEF).get(1), "Named belief facade");
		check(facade.getDesireName(context.scope) == context.base(MentalStateKind.DESIRE).get(1), "Named desire facade");
		check(facade.getIntentionName(context.scope) == context.base(MentalStateKind.INTENTION).get(1), "Named intention facade");
	}

	private static void specialisedUpdates() {
		final var context = new Context();
		final var predicate = new Predicate("fact");
		final MentalState desire = MentalStateKind.DESIRE.create(predicate);
		check(!BdiUtils.addDesire(context.scope, null, desire), "Preserve the existing addDesire return value");
		check(desire.getOwner() == context.agent && context.base(MentalStateKind.DESIRE).get(0) == desire,
				"Desire insertion assigns ownership");
		BdiUtils.addDesire(context.scope, null, MentalStateKind.DESIRE.create(new Predicate("fact")));
		check(context.base(MentalStateKind.DESIRE).size() == 1
				&& context.base(MentalStateKind.DESIRE).get(0) == desire, "Mental-state duplicates retain the original");

		context.base(MentalStateKind.INTENTION).add(MentalStateKind.INTENTION.create(predicate));
		BdiUtils.addUncertainty(context.scope, MentalStateKind.UNCERTAINTY.create(predicate));
		BdiUtils.addObligation(context.scope, MentalStateKind.OBLIGATION.create(predicate));
		check(context.base(MentalStateKind.INTENTION).isEmpty(), "Obligation insertion clears intentions");
		context.base(MentalStateKind.INTENTION).add(MentalStateKind.INTENTION.create(predicate));
		context.attributes.put(SimpleBdiArchitecture.CURRENT_PLAN, new Object());
		context.attributes.put(SimpleBdiArchitecture.CURRENT_NORM, new Object());
		final MentalState belief = MentalStateKind.BELIEF.create(predicate);
		check(BdiUtils.addBelief(context.scope, belief) && belief.getOwner() == context.agent, "Belief ownership");
		check(context.base(MentalStateKind.DESIRE).isEmpty() && context.base(MentalStateKind.INTENTION).isEmpty()
				&& context.base(MentalStateKind.UNCERTAINTY).isEmpty()
				&& context.base(MentalStateKind.OBLIGATION).isEmpty(), "Belief insertion resolves related states");
		check(context.attributes.get(SimpleBdiArchitecture.CURRENT_PLAN) == null
				&& context.attributes.get(SimpleBdiArchitecture.CURRENT_NORM) == null, "Belief invalidates plan and norm");
		check(!BdiUtils.addBelief(context.scope, MentalStateKind.BELIEF.create(predicate)), "Duplicate belief return value");

		final Emotion oldEmotion = new Emotion("joy", 0.3);
		oldEmotion.setOwner(context.agent);
		oldEmotion.setDecay(0.1);
		final Emotion newEmotion = new Emotion("joy", 0.2);
		newEmotion.setOwner(context.agent);
		newEmotion.setDecay(0.2);
		BdiUtils.addEmotion(context.scope, oldEmotion);
		BdiUtils.addEmotion(context.scope, newEmotion);
		final IList<Emotion> emotions = BdiUtils.getEmotionBase(context.scope, SimpleBdiArchitecture.EMOTION_BASE);
		check(emotions.size() == 1 && emotions.get(0) != oldEmotion && emotions.get(0).getIntensity() == 0.5
				&& emotions.get(0).getDecay() == 0.1, "Emotion replacement retains its accumulation and decay policy");
	}

	private static void focusedStates() throws Exception {
		final var addBelief = FocusStatement.class.getDeclaredMethod("addBelief", IScope.class, IScope.class,
				IScope.class, Predicate.class);
		addBelief.setAccessible(true);
		for (final boolean uncertain : List.of(false, true)) {
			for (final boolean explicitParameters : List.of(false, true)) {
				final var observed = new Context();
				final var observer = new Context();
				final var lifetimeContext = new Context();
				final var evaluations = new ArrayList<String>();
				final Map<String, IExpression> facets = new HashMap<>();
				facets.put(FocusStatement.ISUNCERTAIN, proxy(IExpression.class, (p, method, args) -> {
					check(method.getName().equals("value") && args[0] == observer.scope, "Uncertainty uses observer scope");
					evaluations.add("uncertain");
					return uncertain;
				}));
				if (explicitParameters) {
					facets.put(FocusStatement.STRENGTH, proxy(IExpression.class, (p, method, args) -> {
						check(method.getName().equals("value") && args[0] == observed.scope, "Strength uses observed scope");
						evaluations.add("strength");
						return 0.4;
					}));
					facets.put(FocusStatement.LIFETIME, proxy(IExpression.class, (p, method, args) -> {
						check(method.getName().equals("value") && args[0] == observed.scope, "Lifetime uses observed scope");
						evaluations.add("lifetime");
						return 8;
					}));
				}
				final IDescription description = proxy(IDescription.class, (p, method, args) -> switch (method.getName()) {
					case "getKeyword" -> "focus";
					case "getName" -> "test";
					case "getFacetExpr" -> facets.get(((String[]) args[0])[0]);
					default -> throw new AssertionError("Unexpected description call: " + method);
				});
				final var focus = new FocusStatement(description);
				final var predicate = new Predicate("observed");
				addBelief.invoke(focus, observed.scope, observer.scope, lifetimeContext.scope, predicate);
				final MentalStateKind kind = uncertain ? MentalStateKind.UNCERTAINTY : MentalStateKind.BELIEF;
				final MentalState state = observer.base(kind).get(0);
				check(state.getPredicate() == predicate && state.getOwner() == observer.agent, "Focus payload and owner");
				check(state.getStrength() == (explicitParameters ? 0.4 : 1.0)
						&& state.getLifeTime() == (explicitParameters ? 8 : -1), "Focus parameters and defaults");
				check(evaluations.equals(explicitParameters ? List.of("uncertain", "strength", "lifetime")
						: List.of("uncertain")), "Focus expression evaluation order");
				check(observed.base(kind).isEmpty(), "Focused state belongs to observer, not observed agent");
			}
		}
	}
}

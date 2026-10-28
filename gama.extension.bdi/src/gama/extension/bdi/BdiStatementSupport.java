package gama.extension.bdi;

import java.util.List;
import java.util.function.Consumer;

import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.IMap;

/** Shared operations; statement classes retain control of guards, ordering and all-match expansion. */
final class BdiStatementSupport {

	private final IExpression strength;
	private final IExpression lifetime;

	BdiStatementSupport(final IExpression strength, final IExpression lifetime) {
		this.strength = strength;
		this.lifetime = lifetime;
	}

	MentalState prepare(final IScope scope, final MentalStateKind kind, final Predicate predicate,
			final IMap<String, Object> values) {
		final MentalState state = kind.create(predicate);
		if (values != null) state.getPredicate().setValues(values);
		if (strength != null) state.setStrength(Cast.asFloat(scope, strength.value(scope)));
		if (lifetime != null) state.setLifeTime(Cast.asInt(scope, lifetime.value(scope)));
		return state;
	}

	@SuppressWarnings ("unchecked")
	static boolean hasPredicates(final IScope scope, final MentalStateKind kind, final IExpression expression) {
		return expression == null || BdiUtils.hasPredicates(scope, kind, (List<Predicate>) expression.value(scope));
	}

	static boolean hasPredicate(final IScope scope, final MentalStateKind kind, final IExpression expression) {
		if (expression == null) return true;
		final MentalState state = kind.create((Predicate) expression.value(scope));
		return BdiUtils.getBase(scope, kind).contains(state);
	}

	static boolean hasEmotions(final IScope scope, final List<Emotion> emotions) {
		for (final Emotion emotion : emotions) {
			if (!BdiUtils.hasEmotion(scope, emotion)) return false;
		}
		return true;
	}

	void add(final IScope scope, final MentalStateKind kind, final IExpression expression) {
		if (expression != null) add(scope, kind, prepare(scope, kind, (Predicate) expression.value(scope), null));
	}

	private static void add(final IScope scope, final MentalStateKind kind, final MentalState state) {
		switch (kind) {
			case BELIEF -> BdiUtils.addBelief(scope, state);
			case DESIRE -> BdiUtils.addDesire(scope, null, state);
			case UNCERTAINTY -> BdiUtils.addUncertainty(scope, state);
			case IDEAL -> BdiUtils.addIdeal(scope, state);
			case OBLIGATION -> BdiUtils.addObligation(scope, state);
			case INTENTION -> throw new IllegalArgumentException("Statement additions do not support intentions");
		}
	}

	@SuppressWarnings ("unchecked")
	void addList(final IScope scope, final MentalStateKind kind, final IExpression expression,
			final boolean indexedParameters) {
		if (expression == null) return;
		int index = 0;
		for (final Predicate predicate : (List<Predicate>) expression.value(scope)) {
			final MentalState state;
			if (indexedParameters) {
				state = kind.create(predicate);
				// Rules historically re-evaluate a list-valued strength and a scalar lifetime for each element.
				if (strength != null) {
					state.setStrength(Cast.asFloat(scope, strength.value(scope) instanceof Number number ? number
							: ((List<Float>) strength.value(scope)).get(index)));
				}
				if (lifetime != null) {
					state.setLifeTime(Cast.asInt(scope, lifetime.value(scope) instanceof List<?> list
							? list.get(index) : lifetime.value(scope)));
				}
			} else {
				state = prepare(scope, kind, predicate, null);
			}
			add(scope, kind, state);
			index++;
		}
	}

	static void remove(final IScope scope, final MentalStateKind kind, final IExpression expression) {
		if (expression != null) remove(scope, kind, (Predicate) expression.value(scope));
	}

	private static void remove(final IScope scope, final MentalStateKind kind, final Predicate predicate) {
		final MentalState state = kind.create(predicate);
		switch (kind) {
			case BELIEF -> BdiUtils.removeBelief(scope, state);
			case DESIRE -> BdiUtils.removeDesire(scope, state);
			case INTENTION -> BdiUtils.removeIntention(scope, state);
			case UNCERTAINTY -> BdiUtils.removeUncertainty(scope, state);
			case IDEAL -> BdiUtils.removeIdeal(scope, state);
			case OBLIGATION -> BdiUtils.removeObligation(scope, state);
		}
	}

	@SuppressWarnings ("unchecked")
	static void removeList(final IScope scope, final MentalStateKind kind, final IExpression expression) {
		if (expression == null) return;
		removeList(scope, kind, (List<Predicate>) expression.value(scope));
	}

	static void removeList(final IScope scope, final MentalStateKind kind, final List<Predicate> predicates) {
		for (final Predicate predicate : predicates) {
			remove(scope, kind, predicate);
		}
	}

	static void addEmotions(final IScope scope, final IExpression expression) {
		forEachEmotion(scope, expression, emotion -> BdiUtils.addEmotion(scope, emotion));
	}

	static void removeEmotions(final IScope scope, final IExpression expression) {
		forEachEmotion(scope, expression, emotion -> BdiUtils.removeEmotion(scope, emotion));
	}

	@SuppressWarnings ("unchecked")
	private static void forEachEmotion(final IScope scope, final IExpression expression, final Consumer<Emotion> action) {
		if (expression == null) return;
		for (final Emotion emotion : (List<Emotion>) expression.value(scope)) {
			action.accept(emotion);
		}
	}
}

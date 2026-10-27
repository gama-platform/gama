package gama.extension.bdi;

/**
 * Built-in modalities and their bases. Public mental states still accept arbitrary, case-sensitive modality strings.
 */
enum MentalStateKind {

	BELIEF("Belief", SimpleBdiArchitecture.BELIEF_BASE),
	DESIRE("Desire", SimpleBdiArchitecture.DESIRE_BASE),
	INTENTION("Intention", SimpleBdiArchitecture.INTENTION_BASE),
	UNCERTAINTY("Uncertainty", SimpleBdiArchitecture.UNCERTAINTY_BASE),
	IDEAL("Ideal", SimpleBdiArchitecture.IDEAL_BASE),
	OBLIGATION("Obligation", SimpleBdiArchitecture.OBLIGATION_BASE);

	private final String modality;
	private final String base;

	MentalStateKind(final String modality, final String base) {
		this.modality = modality;
		this.base = base;
	}

	String modality() { return modality; }

	String base() { return base; }

	boolean matches(final MentalState state) {
		return state != null && modality.equals(state.getModality());
	}

	MentalState create() { return new MentalState(modality); }

	MentalState create(final Predicate predicate) { return new MentalState(modality, predicate); }

	MentalState create(final MentalState state) { return new MentalState(modality, state); }

	MentalState create(final Emotion emotion) { return new MentalState(modality, emotion); }
}

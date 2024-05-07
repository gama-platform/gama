package gama.extension.bdi;

import java.util.List;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.operators.Maths;
import gama.gaml.types.Types;

public class Utils {

	//TODO: replace this by Math.clamp calls once we switch in java21
	public static int clamp(long value, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException(min + " > " + max);
        }
        return (int) Math.min(max, Math.max(value, min));
    }
	
    public static long clamp(long value, long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException(min + " > " + max);
        }
        return Math.min(max, Math.max(value, min));
    }

    public static double clamp(double value, double min, double max) {
        // This unusual condition allows keeping only one branch
        // on common path when min < max and neither of them is NaN.
        // If min == max, we should additionally check for +0.0/-0.0 case,
        // so we're still visiting the if statement.
        if (!(min < max)) { // min greater than, equal to, or unordered with respect to max; NaN values are unordered
            if (Double.isNaN(min)) {
                throw new IllegalArgumentException("min is NaN");
            }
            if (Double.isNaN(max)) {
                throw new IllegalArgumentException("max is NaN");
            }
            if (Double.compare(min, max) > 0) {
                throw new IllegalArgumentException(min + " > " + max);
            }
            // Fall-through if min and max are exactly equal (or min = -0.0 and max = +0.0)
            // and none of them is NaN
        }
        return Math.min(max, Math.max(value, min));
    }

    public static float clamp(float value, float min, float max) {
        // This unusual condition allows keeping only one branch
        // on common path when min < max and neither of them is NaN.
        // If min == max, we should additionally check for +0.0/-0.0 case,
        // so we're still visiting the if statement.
        if (!(min < max)) { // min greater than, equal to, or unordered with respect to max; NaN values are unordered
            if (Float.isNaN(min)) {
                throw new IllegalArgumentException("min is NaN");
            }
            if (Float.isNaN(max)) {
                throw new IllegalArgumentException("max is NaN");
            }
            if (Float.compare(min, max) > 0) {
                throw new IllegalArgumentException(min + " > " + max);
            }
            // Fall-through if min and max are exactly equal (or min = -0.0 and max = +0.0)
            // and none of them is NaN
        }
        return Math.min(max, Math.max(value, min));
    }
    
    
    ///////////////////////////// real utils part
    

	/**
	 * Gets the laws.
	 *
	 * @param scope the scope
	 * @return the laws
	 */
	public static List<LawStatement> getLaws(final IScope scope) {
		final IAgent agent = scope.getAgent();
		return scope.hasArg(SimpleBdiArchitecture.LAW_BASE) ? scope.getListArg(SimpleBdiArchitecture.LAW_BASE) : (List<LawStatement>) agent.getAttribute(SimpleBdiArchitecture.LAW_BASE);
	}

	/**
	 * Gets the norms.
	 *
	 * @param scope the scope
	 * @return the norms
	 */
	public static List<Norm> getNorms(final IScope scope) {
		final IAgent agent = scope.getAgent();
		return scope.hasArg(SimpleBdiArchitecture.NORM_BASE) ? scope.getListArg(SimpleBdiArchitecture.NORM_BASE) : (List<Norm>) agent.getAttribute(SimpleBdiArchitecture.NORM_BASE);
	}

	/**
	 * Gets the sanctions.
	 *
	 * @param scope the scope
	 * @return the sanctions
	 */
	public static List<Sanction> getSanctions(final IScope scope) {
		final IAgent agent = scope.getAgent();
		return scope.hasArg(SimpleBdiArchitecture.SANCTION_BASE) ? scope.getListArg(SimpleBdiArchitecture.SANCTION_BASE)
				: (List<Sanction>) agent.getAttribute(SimpleBdiArchitecture.SANCTION_BASE);
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
				scope.hasArg(SimpleBdiArchitecture.USE_EMOTIONS_ARCHITECTURE) ? scope.getBoolArg(SimpleBdiArchitecture.USE_EMOTIONS_ARCHITECTURE)
						: (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_EMOTIONS_ARCHITECTURE);
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
			removeFromBase(scope, predTemp, SimpleBdiArchitecture.BELIEF_BASE);
		}
		if (getBase(scope, SimpleBdiArchitecture.INTENTION_BASE).contains(predicateDirect)) {
			removeFromBase(scope, predicateDirect, SimpleBdiArchitecture.DESIRE_BASE);
			removeFromBase(scope, predicateDirect, SimpleBdiArchitecture.INTENTION_BASE);
			scope.getAgent().setAttribute(SimpleBdiArchitecture.CURRENT_PLAN, null);
			scope.getAgent().setAttribute(SimpleBdiArchitecture.CURRENT_NORM, null);
		}
		if (getBase(scope, SimpleBdiArchitecture.UNCERTAINTY_BASE).contains(predicateDirect)) {
			removeFromBase(scope, predicateDirect, SimpleBdiArchitecture.UNCERTAINTY_BASE);
		}
		if (getBase(scope, SimpleBdiArchitecture.OBLIGATION_BASE).contains(predicateDirect)) {
			removeFromBase(scope, predicateDirect, SimpleBdiArchitecture.OBLIGATION_BASE);
		}
		for (final MentalState predTest : getBase(scope, SimpleBdiArchitecture.UNCERTAINTY_BASE)) {
			if (	predTest.getPredicate() != null && predicateDirect.getPredicate() != null
				&& 	predTest.getPredicate().equalsButNotTruth(predicateDirect.getPredicate())) {
				predTemp = predTest;
			}
		}
		if (predTemp != null) {
			removeFromBase(scope, predTemp, SimpleBdiArchitecture.UNCERTAINTY_BASE);
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
		return addToBase(scope, predicateDirect, SimpleBdiArchitecture.BELIEF_BASE);
	}
    
	public static MentalState getBeliefMentalStateFromBase(final IScope scope, MentalState predicateDirect) {
		if (predicateDirect != null) {
			for (final MentalState mental : getBase(scope, SimpleBdiArchitecture.BELIEF_BASE)) {
				if (mental.getMentalState() != null && predicateDirect.equals(mental.getMentalState())) { 
					return mental; 
				}
			}
		}
		return null;
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
		addToBase(scope, predicate, SimpleBdiArchitecture.DESIRE_BASE);

		return false;
	}


	/**
	 * Removes the belief.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeBelief(final IScope scope, final MentalState pred) {
		return getBase(scope, SimpleBdiArchitecture.BELIEF_BASE).remove(pred);
	}
	

	/**
	 * Removes the intention.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeIntention(final IScope scope, final MentalState pred) {
		getBase(scope, SimpleBdiArchitecture.INTENTION_BASE).remove(pred);
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
	 * Clear intention.
	 *
	 * @param scope the scope
	 * @return the boolean
	 */
	public static Boolean clearIntention(final IScope scope) {
		getBase(scope, SimpleBdiArchitecture.INTENTION_BASE).clear();
		scope.getAgent().setAttribute(SimpleBdiArchitecture.CURRENT_PLAN, null);
		return true;
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
		return addToBase(scope, newEmo, SimpleBdiArchitecture.EMOTION_BASE);
	}

	/**
	 * Checks for emotion.
	 *
	 * @param scope the scope
	 * @param emo the emo
	 * @return the boolean
	 */
	public static Boolean hasEmotion(final IScope scope, final Emotion emo) {
		return getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE).contains(emo);
	}
	

	/**
	 * Gets the emotion from the emotion base that is equal to the one passed as an argument
	 *
	 * @param scope the scope
	 * @param emotionDirect the emotion direct
	 * @return the emotion
	 */
	public static Emotion getEmotion(final IScope scope, final Emotion emotionDirect) {
		for (final Emotion emo : getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE)) {
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
		return getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE).remove(emo);
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
				scope.hasArg(SimpleBdiArchitecture.USE_EMOTIONS_ARCHITECTURE) ? scope.getBoolArg(SimpleBdiArchitecture.USE_EMOTIONS_ARCHITECTURE)
						: (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_EMOTIONS_ARCHITECTURE);
		MentalState predTemp = null;
		for (final MentalState predTest : getBase(scope, SimpleBdiArchitecture.BELIEF_BASE)) {
			if (predTest.getPredicate() != null && predicate.getPredicate() != null
					&& predTest.getPredicate().equalsButNotTruth(predicate.getPredicate())) {
				predTemp = predTest;
			}
		}
		if (predTemp != null) {
			removeFromBase(scope, predTemp, SimpleBdiArchitecture.BELIEF_BASE);
		}
		for (final MentalState predTest : getBase(scope, SimpleBdiArchitecture.UNCERTAINTY_BASE)) {
			if (predTest.getPredicate() != null && predicate.getPredicate() != null
					&& predTest.getPredicate().equalsButNotTruth(predicate.getPredicate())) {
				predTemp = predTest;
			}
		}
		if (predTemp != null) {
			removeFromBase(scope, predTemp, SimpleBdiArchitecture.UNCERTAINTY_BASE);
		}
		if (use_emotion_architecture) {
			createHopeFromMentalState(scope, predicate);
		}
		predicate.setOwner(scope.getAgent());
		return addToBase(scope, predicate, SimpleBdiArchitecture.UNCERTAINTY_BASE);
	}
	
	/**
	 * Checks for uncertainty.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	public static Boolean hasUncertainty(final IScope scope, final MentalState predicateDirect) {
		return getBase(scope, SimpleBdiArchitecture.UNCERTAINTY_BASE).contains(predicateDirect);
	}

	/**
	 * Removes the uncertainty.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeUncertainty(final IScope scope, final MentalState pred) {
		return getBase(scope, SimpleBdiArchitecture.UNCERTAINTY_BASE).remove(pred);
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
		return addToBase(scope, predicate, SimpleBdiArchitecture.IDEAL_BASE);
	}
	
	/**
	 * Checks for ideal.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	public static Boolean hasIdeal(final IScope scope, final MentalState predicateDirect) {
		return getBase(scope, SimpleBdiArchitecture.IDEAL_BASE).contains(predicateDirect);
	}

	/**
	 * Removes the ideal.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeIdeal(final IScope scope, final MentalState pred) {
		return getBase(scope, SimpleBdiArchitecture.IDEAL_BASE).remove(pred);
	}

	/**
	 * Checks for obligation.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	public static Boolean hasObligation(final IScope scope, final MentalState predicateDirect) {
		return getBase(scope, SimpleBdiArchitecture.OBLIGATION_BASE).contains(predicateDirect);
	}

	/**
	 * Removes the obligation.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeObligation(final IScope scope, final MentalState pred) {
		return getBase(scope, SimpleBdiArchitecture.OBLIGATION_BASE).remove(pred);
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
						if (getSocialLink(scope, social) == null) { return addToBase(scope, social, SimpleBdiArchitecture.SOCIALLINK_BASE); }
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
	 * @param social the social
	 * @return the social link
	 */
	public static SocialLink getSocialLink(final IScope scope, final SocialLink social) {
		for (final SocialLink socialLink : getSocialBase(scope, SimpleBdiArchitecture.SOCIALLINK_BASE)) {
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
		return getSocialBase(scope, SimpleBdiArchitecture.SOCIALLINK_BASE).contains(socialDirect);
	}

	/**
	 * Removes the social link.
	 *
	 * @param scope the scope
	 * @param socialDirect the social direct
	 * @return the boolean
	 */
	public static Boolean removeSocialLink(final IScope scope, final SocialLink socialDirect) {
		return getSocialBase(scope, SimpleBdiArchitecture.SOCIALLINK_BASE).remove(socialDirect);
	}

	/**
	 * Checks for belief.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	public static Boolean hasBelief(final IScope scope, final MentalState predicateDirect) {
		return getBase(scope, SimpleBdiArchitecture.BELIEF_BASE).contains(predicateDirect);

	}

	/**
	 * Checks for desire.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 * @return the boolean
	 */
	public static Boolean hasDesire(final IScope scope, final MentalState predicateDirect) {
		return getBase(scope, SimpleBdiArchitecture.DESIRE_BASE).contains(predicateDirect);
	}

	/**
	 * Removes the desire.
	 *
	 * @param scope the scope
	 * @param pred the pred
	 * @return the boolean
	 */
	public static Boolean removeDesire(final IScope scope, final MentalState pred) {
		Utils.getBase(scope, SimpleBdiArchitecture.DESIRE_BASE).remove(pred);
		Utils.getBase(scope, SimpleBdiArchitecture.INTENTION_BASE).remove(pred);
		for (final MentalState statement : Utils.getBase(scope, SimpleBdiArchitecture.INTENTION_BASE)) {
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
	 * Update social link.
	 *
	 * @param scope the scope
	 * @param social the social
	 */
	public static void updateSocialLink(final IScope scope, final SocialLink social) {
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
	private static void updateAppreciation(final IScope scope, final SocialLink social) {
		final Boolean use_personality = scope.getBoolArgIfExists(SimpleBdiArchitecture.USE_PERSONALITY, (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_PERSONALITY));
		final IAgent agentCause = social.getAgent();
		Double tempPositif = 0.0;
		Double moyPositif = 0.0;
		Double tempNegatif = 0.0;
		Double moyNegatif = 0.0;
		Double coefModification = 0.1;
		if (use_personality) {
			final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
			coefModification = 1 - neurotisme;
		}
		Double appreciationModif = social.getLiking();
		for (final Emotion emo : getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE)) {
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

		appreciationModif = Utils.clamp(appreciationModif 
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
	private static void updateDominance(final IScope scope, final SocialLink social) {
		final Boolean use_personality = scope.getBoolArgIfExists(SimpleBdiArchitecture.USE_PERSONALITY, (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_PERSONALITY));
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
			final double neurotisme = (double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
			coefModification = 1 - neurotisme;
		}
		double dominanceModif = social.getDominance();
		for (final Emotion emo : getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE)) {
			if (emo.getAgentCause() != null && emo.getAgentCause().equals(agentCause)) {
				if ("sadness".equals(emo.getName()) || "fear".equals(emo.getName())) {
					tempNegatif = tempNegatif + 1.0;
					moyNegatif = moyNegatif + emo.getIntensity();
				}
			}
		}
		for (final Emotion emo : getEmotionBase(scopeAgentCause, SimpleBdiArchitecture.EMOTION_BASE)) {
			if (emo.getAgentCause() != null && emo.getAgentCause().equals(currentAgent)) {
				if ("sadness".equals(emo.getName()) || "fear".equals(emo.getName())) {
					tempPositif = tempPositif + 1.0;
					moyPositif = moyPositif + emo.getIntensity();
				}
			}
		}

		moyPositif = tempPositif != 0.0 ? moyPositif / tempPositif : 0;
		moyNegatif = tempNegatif != 0.0 ? moyNegatif / tempNegatif : 0;

		dominanceModif = Utils.clamp(dominanceModif + coefModification * Maths.abs(dominanceModif) * (moyPositif - moyNegatif), 1, 1);
		social.setDominance(dominanceModif);
		GAMA.releaseScope(scopeAgentCause);
	}

	/**
	 * Update solidarity.
	 *
	 * @param scope the scope
	 * @param social the social
	 */
	private static void updateSolidarity(final IScope scope, final SocialLink social) {
		final Boolean use_personality = scope.hasArg(SimpleBdiArchitecture.USE_PERSONALITY) ? scope.getBoolArg(SimpleBdiArchitecture.USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_PERSONALITY);
		final IAgent agentCause = social.getAgent();
		double tempPositif = 0.0;
		double moySolid = 0.0;
		double tempNegatif = 0.0;
		double nbMentalState = 0.0;
		double tempEmoNeg = 0.0;
		double moyEmoNeg = 0.0;
		double coefModification = 0.1;
		if (use_personality) {
			final double openness = (double) scope.getAgent().getAttribute(SimpleBdiArchitecture.OPENNESS);
			coefModification = 1 - openness;
		}
		double coefModifEmo = 0.1;
		if (use_personality) {
			final double neurotisme = (double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
			coefModifEmo = 1 - neurotisme;
		}
		double solidarityModif = social.getSolidarity();
		for (final Emotion emo : getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE)) {
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
	private static void updateFamiliarity(final IScope scope, final SocialLink social) {
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
	 * Creates the joy from predicate.
	 *
	 * @param scope the scope
	 * @param predTest the pred test
	 */
	// va démarrer le calcul de gratification , remorse, anger et gratitude
	private static void createJoyFromPredicate(final IScope scope, final MentalState predTest) {
		final Boolean use_personality = scope.hasArg(SimpleBdiArchitecture.USE_PERSONALITY) ? scope.getBoolArg(SimpleBdiArchitecture.USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_PERSONALITY);
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
					final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
					MentalState desire = null;
					for (final MentalState mental : getBase(scope, SimpleBdiArchitecture.DESIRE_BASE)) {
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
				for (final MentalState pred : getBase(scope, SimpleBdiArchitecture.DESIRE_BASE)) {
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
								final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
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
		final Boolean use_personality = scope.hasArg(SimpleBdiArchitecture.USE_PERSONALITY) ? scope.getBoolArg(SimpleBdiArchitecture.USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_PERSONALITY);
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
					final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
					MentalState desire = null;
					for (final MentalState mental : getBase(scope, SimpleBdiArchitecture.DESIRE_BASE)) {
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
				for (final MentalState pred : getBase(scope, SimpleBdiArchitecture.DESIRE_BASE)) {
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
								final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
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
		final Boolean use_personality = scope.hasArg(SimpleBdiArchitecture.USE_PERSONALITY) ? scope.getBoolArg(SimpleBdiArchitecture.USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_PERSONALITY);
		if (predicateDirect.getPredicate() != null) {
			final IList<Emotion> emoTemps = getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE).copy(scope);
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
							final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
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
							final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
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
							final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
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
							final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
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
		return addToBase(scope, predicate, SimpleBdiArchitecture.OBLIGATION_BASE);
	}
	/**
	 * Creates the emotions related to others.
	 *
	 * @param scope the scope
	 */
	public static void createEmotionsRelatedToOthers(final IScope scope) {
		// Regroupe le happy_for, sorry_for, resentment et gloating.
		
		
		for (final SocialLink temp : Utils.getSocialBase(scope, SimpleBdiArchitecture.SOCIALLINK_BASE)) {
			final IAgent agentTemp = temp.getAgent();
			IScope scopeAgentTemp = null;
			if (agentTemp != null) {
				scopeAgentTemp = agentTemp.getScope().copy("in SimpleBdiArchitecture");
				scopeAgentTemp.push(agentTemp);
			}
			double signLiking = Math.signum(temp.getLiking());
			for (final Emotion emo : Utils.getEmotionBase(scopeAgentTemp, SimpleBdiArchitecture.EMOTION_BASE)) {
				if ( "joy".equals(emo.getName()) || "sadness".equals(emo.getName()) ) {
					final Emotion newEmo = new Emotion(Utils.getNewEmotionNameToCreateHappyFor(emo.getName(), signLiking > 0),
							emo.getIntensity() * signLiking * temp.getLiking(), emo.getAbout(),
							//TODO: the previous comments said to change the formula but no indication
							// maybe use the one used in createHappyForFromMentalState (above)?
							agentTemp);
					newEmo.setDecay(0);
					Utils.addEmotion(scope, newEmo);
				}
			}
			GAMA.releaseScope(scopeAgentTemp);
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
		final boolean use_personality = scope.getBoolArgIfExists(SimpleBdiArchitecture.USE_PERSONALITY, (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_PERSONALITY));
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
		

		for (final SocialLink temp : getSocialBase(scope, SimpleBdiArchitecture.SOCIALLINK_BASE)) {
			if (agentTemp.equals(temp.getAgent())) {
				double absLiking = Math.abs(temp.getLiking());
				double signLiking = Math.signum(temp.getLiking());
				String emoName = getNewEmotionNameToCreateHappyFor(emo.getName(), signLiking >0);
				final Emotion emotion = new Emotion(emoName, emo.getAbout(), agentTemp);
				Double intensity = 1.0;
				Double decay = 0.0;
				if (use_personality) {
					final double neurotisme = (double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
					final double amicability = (double) scope.getAgent().getAttribute(SimpleBdiArchitecture.AGREEABLENESS);
					intensity = Utils.clamp(emo.getIntensity() * absLiking * (1 - signLiking * (0.5 - amicability)), 0, 1);
					decay = scope.getSimulation().getTimeStep(scope) * 0.00028 * neurotisme * emotion.getIntensity();//TODO: decay will be negative, is it normal ?
				}
				emotion.setIntensity(intensity);
				emotion.setDecay(decay);
				addEmotion(scope, emotion);
			}
		}
	}

	/**
	 * Creates the pride from mental state.
	 *
	 * @param scope the scope
	 * @param predicateDirect the predicate direct
	 */
	private static void createPrideFromMentalState(final IScope scope, final MentalState predicateDirect) {
		final Boolean use_personality = scope.getBoolArgIfExists(SimpleBdiArchitecture.USE_PERSONALITY, (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_PERSONALITY));
		
		Predicate pred = predicateDirect.getPredicate();
		if (pred == null) {
			return;
		}
		
		for (final MentalState temp : getBase(scope, SimpleBdiArchitecture.IDEAL_BASE).stream().filter(m -> m != null && m.getPredicate().equals(pred)).toList()) {
			double absStrength = Math.abs(temp.getStrength());
			double intensity = 1.0;
			double decay = 0.0;
			if (use_personality) {
				final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
				final Double openness = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.OPENNESS);
				intensity = Utils.clamp(predicateDirect.getStrength() * absStrength * (1 + (0.5 - openness)), 0, 1);
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
		final Boolean use_personality = scope.getBoolArgIfExists(SimpleBdiArchitecture.USE_PERSONALITY, (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_PERSONALITY));
		final IList<Emotion> emoTemps = getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE).copy(scope);
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
							final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
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
							final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
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
		final Boolean use_personality = scope.hasArg(SimpleBdiArchitecture.USE_PERSONALITY) ? scope.getBoolArg(SimpleBdiArchitecture.USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(SimpleBdiArchitecture.USE_PERSONALITY);
		final IList<Emotion> emoTemps = getEmotionBase(scope, SimpleBdiArchitecture.EMOTION_BASE).copy(scope);
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
							final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
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
							final Double neurotisme = (Double) scope.getAgent().getAttribute(SimpleBdiArchitecture.NEUROTISM);
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
}

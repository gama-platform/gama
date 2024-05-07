/*******************************************************************************************************
 *
 * EmotionalContagion.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and
 * simulation platform .
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
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;

/**
 * The Class EmotionalContagion.
 */
@symbol (
		name = EmotionalContagion.EMOTIONALCONTAGION,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.BDI })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = true,
				doc = @doc ("the identifier of the emotional contagion")),
				@facet (
						name = EmotionalContagion.EMOTIONDETECTED,
						type = EmotionType.EMOTIONTYPE_ID,
						optional = false,
						doc = @doc ("the emotion that will start the contagion")),
				@facet (
						name = EmotionalContagion.EMOTIONCREATED,
						type = EmotionType.EMOTIONTYPE_ID,
						optional = true,
						doc = @doc ("the emotion that will be created with the contagion")),
				@facet (
						name = EmotionalContagion.CHARISMA,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("The charisma value of the perceived agent (between 0 and 1)")),
				@facet (
						name = IKeyword.WHEN,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("A boolean value to get the emotion only with a certain condition")),
				@facet (
						name = EmotionalContagion.THRESHOLD,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("The threshold value to make the contagion")),
				@facet (
						name = EmotionalContagion.DECAY,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("The decay value of the emotion added to the agent")),
				@facet (
						name = EmotionalContagion.INTENSITY,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("The intensity value of the emotion created to the agent")),
				@facet (
						name = EmotionalContagion.RECEPTIVITY,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("The receptivity value of the current agent (between 0 and 1)")) },
		omissible = IKeyword.NAME)
@doc (
		value = "enables to make conscious or unconscious emotional contagion",
		examples = { @example ("emotional_contagion emotion_detected:fearConfirmed;"),
				@example ("emotional_contagion emotion_detected:fear emotion_created:fearConfirmed;"),
				@example ("emotional_contagion emotion_detected:fear emotion_created:fearConfirmed charisma: 0.5 receptivity: 0.5;") })

public class EmotionalContagion extends AbstractStatement {

	/** The Constant EMOTIONALCONTAGION. */
	public static final String EMOTIONALCONTAGION = "emotional_contagion";

	/** The Constant EMOTIONDETECTED. */
	public static final String EMOTIONDETECTED = "emotion_detected";

	/** The Constant EMOTIONCREATED. */
	public static final String EMOTIONCREATED = "emotion_created";

	/** The Constant CHARISMA. */
	public static final String CHARISMA = "charisma";

	/** The Constant RECEPTIVITY. */
	public static final String RECEPTIVITY = "receptivity";

	/** The Constant THRESHOLD. */
	public static final String THRESHOLD = "threshold";

	/** The Constant DECAY. */
	public static final String DECAY = "decay";

	/** The Constant INTENSITY. */
	public static final String INTENSITY = "intensity";

	/** The name expr. */
	final IExpression nameExpr;

	/** The emotion detected. */
	final IExpression emotionDetected;

	/** The emotion created. */
	final IExpression emotionCreated;

	/** The charisma. */
	final IExpression charisma;

	/** The when. */
	final IExpression when;

	/** The receptivity. */
	final IExpression receptivity;

	/** The threshold. */
	final IExpression threshold;

	/** The decay. */
	final IExpression decay;

	/** The intensity. */
	final IExpression intensity;

	/**
	 * Instantiates a new emotional contagion.
	 *
	 * @param desc
	 *            the desc
	 */
	public EmotionalContagion(final IDescription desc) {
		super(desc);
		nameExpr = getFacet(IKeyword.NAME);
		emotionDetected = getFacet(EmotionalContagion.EMOTIONDETECTED);
		emotionCreated = getFacet(EmotionalContagion.EMOTIONCREATED);
		charisma = getFacet(EmotionalContagion.CHARISMA);
		when = getFacet(IKeyword.WHEN);
		receptivity = getFacet(EmotionalContagion.RECEPTIVITY);
		threshold = getFacet(EmotionalContagion.THRESHOLD);
		decay = getFacet(EmotionalContagion.DECAY);
		intensity = getFacet(EmotionalContagion.INTENSITY);
	}

	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent[] stack = scope.getAgentsStack();
		final IAgent mySelfAgent = stack[stack.length - 2];
		double charismaValue = 1.0;
		double receptivityValue = 1.0;
		double thresholdValue = 0.25;
		IScope scopeMySelf = null;
		double decayValue = 0.0;
		double intensityValue = 0.0;
		if (mySelfAgent != null) {
			scopeMySelf = mySelfAgent.getScope().copy("of EmotionalContagion");
			scopeMySelf.push(mySelfAgent);
		}
		if (	   (when == null || Cast.asBool(scopeMySelf, when.value(scopeMySelf))) 
				&& emotionDetected != null
				&& Utils.hasEmotion(scope, (Emotion) emotionDetected.value(scope))) {

			final Emotion detectedEmo = Utils.getEmotion(scope, (Emotion) emotionDetected.value(scope));
			
			//getting variables to calculate the contagious power
			if (charisma != null) {
				charismaValue = (double) charisma.value(scope);
			} else {
				charismaValue = (double) scope.getAgent().getAttribute(CHARISMA);
			}
			if (receptivity != null) {
				receptivityValue = (double) receptivity.value(scopeMySelf);
			} else if (mySelfAgent != null) { 
				receptivityValue = (double) mySelfAgent.getAttribute(RECEPTIVITY); 
			}
			if (threshold != null) { 
				thresholdValue = (double) threshold.value(scopeMySelf); 
			}
			
			// if contagious power is above the threshold a new emotion is created and added
			if (charismaValue * receptivityValue >= thresholdValue) {
				
				final Emotion emo;
				
				if (decay != null) {
					decayValue = (double) decay.value(scopeMySelf);
				}
				
				if (emotionCreated != null) {
					emo = (Emotion) emotionCreated.value(scope);
					if (decay == null) {
						decayValue = detectedEmo.getDecay();
					}
					if (intensity != null) {
						intensityValue = Utils.clamp((double) intensity.value(scopeMySelf), 0, 1);
					}
					emo.setIntensity(intensityValue);
				} else {
					if (detectedEmo.hasIntensity()) {
						emo = new Emotion(detectedEmo.getName(), detectedEmo.getIntensity() * charismaValue * receptivityValue,
											detectedEmo.getAbout(), detectedEmo.getDecay());
					} else {
						emo = (Emotion) detectedEmo.copy(scope);
					}
				}
				emo.setAgentCause(scope.getAgent());
				emo.setDecay(Utils.clamp(decayValue, 0, 1));
				Utils.addEmotion(scopeMySelf, emo);
			}
		}
		GAMA.releaseScope(scopeMySelf);
		return null;
	}

}

/*******************************************************************************************************
 *
 * ConsciousContagionStatement.java, in gama.extension.bdi, is part of the source code of the
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
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;

/**
 * The Class ConsciousContagionStatement.
 */
@symbol(name = ConsciousContagionStatement.CONSCIOUSCONTAGION, kind = ISymbolKind.SINGLE_STATEMENT, with_sequence = false, concept = {
		IConcept.BDI })
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT })
@facets(value = {
		@facet(name = IKeyword.NAME, type = IType.ID, optional = true, doc = @doc("the identifier of the unconscious contagion")),
		@facet(name = ConsciousContagionStatement.EMOTIONDETECTED, type = EmotionType.EMOTIONTYPE_ID, optional = false, doc = @doc("the emotion that will start the contagion")),
		@facet(name = ConsciousContagionStatement.EMOTIONCREATED, type = EmotionType.EMOTIONTYPE_ID, optional = false, doc = @doc("the emotion that will be created with the contagion")),
		@facet(name = ConsciousContagionStatement.CHARISMA, type = IType.FLOAT, optional = true, doc = @doc("The charisma value of the perceived agent (between 0 and 1)")),
		@facet(name = IKeyword.WHEN, type = IType.BOOL, optional = true, doc = @doc("A boolean value to get the emotion only with a certain condition")),
		@facet(name = ConsciousContagionStatement.THRESHOLD, type = IType.FLOAT, optional = true, doc = @doc("The threshold value to make the contagion")),
		@facet(name = ConsciousContagionStatement.DECAY, type = IType.FLOAT, optional = true, doc = @doc("The decay value of the emotion added to the agent")),
		@facet(name = ConsciousContagionStatement.INTENSITY, type = IType.FLOAT, optional = true, doc = @doc("The intensity value of the emotion added to the agent")),
		@facet(name = ConsciousContagionStatement.RECEPTIVITY, type = IType.FLOAT, optional = true, doc = @doc("The receptivity value of the current agent (between 0 and 1)")) }, omissible = IKeyword.NAME)
@doc(value = "enables to directly add an emotion of a perceived species if the perceived agent gets a particular emotion.", examples = {
		@example("conscious_contagion emotion_detected:fear emotion_created:fearConfirmed;"),
		@example("conscious_contagion emotion_detected:fear emotion_created:fearConfirmed charisma: 0.5 receptivity: 0.5;") })

public class ConsciousContagionStatement extends AbstractStatement {

	/** The Constant CONSCIOUSCONTAGION. */
	public static final String CONSCIOUSCONTAGION = "conscious_contagion";
	
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
	 * Instantiates a new conscious contagion statement.
	 *
	 * @param desc the desc
	 */
	public ConsciousContagionStatement(final IDescription desc) {
		super(desc);
		nameExpr = getFacet(IKeyword.NAME);
		emotionDetected = getFacet(ConsciousContagionStatement.EMOTIONDETECTED);
		emotionCreated = getFacet(ConsciousContagionStatement.EMOTIONCREATED);
		charisma = getFacet(ConsciousContagionStatement.CHARISMA);
		when = getFacet(IKeyword.WHEN);
		receptivity = getFacet(ConsciousContagionStatement.RECEPTIVITY);
		threshold = getFacet(ConsciousContagionStatement.THRESHOLD);
		decay = getFacet(ConsciousContagionStatement.DECAY);
		intensity = getFacet(ConsciousContagionStatement.INTENSITY);
	}

	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent[] stack = scope.getAgentsStack();
		final IAgent mySelfAgent = stack[stack.length - 2];
		Double charismaValue = 1.0;
		Double receptivityValue = 1.0;
		Double thresholdValue = 0.25;
		Double decayValue = 0.0;
		Double intensityValue = 0.0;
		IScope scopeMySelf = null;
		if (mySelfAgent != null) {
			scopeMySelf = mySelfAgent.getScope().copy("of ConsciousContagionStatement");
			scopeMySelf.push(mySelfAgent);
		} else
			return null;
		if (when == null || Cast.asBool(scopeMySelf, when.value(scopeMySelf))) {
			if (emotionDetected != null && emotionCreated != null) {
				if (SimpleBdiArchitecture.hasEmotion(scope, (Emotion) emotionDetected.value(scope))) {
					if (charisma != null) {
						charismaValue = (Double) charisma.value(scope);
					} else {
						charismaValue = (Double) scope.getAgent().getAttribute(CHARISMA);
					}
					if (receptivity != null) {
						receptivityValue = (Double) receptivity.value(scopeMySelf);
					} else {
						receptivityValue = (Double) mySelfAgent.getAttribute(RECEPTIVITY);
					}
					if (threshold != null) {
						thresholdValue = (Double) threshold.value(scopeMySelf);
					}
					if (charismaValue * receptivityValue >= thresholdValue) {
						final Emotion tempEmo = (Emotion) emotionCreated.value(scope);
						tempEmo.setAgentCause(scope.getAgent());
						if(decay!=null){
							decayValue = (Double) decay.value(scopeMySelf);
							if(decayValue>1.0){
								decayValue = 1.0;
							}
							if(decayValue<0.0){
								decayValue = 0.0;
							}
						}
						tempEmo.setDecay(decayValue);
						if(intensity!=null){
							intensityValue = (Double) intensity.value(scopeMySelf);
							if(intensityValue>1.0){
								intensityValue = 1.0;
							}
							if(intensityValue<0.0){
								intensityValue = 0.0;
							}
						}
						tempEmo.setIntensity(intensityValue);
						SimpleBdiArchitecture.addEmotion(scopeMySelf, tempEmo);
					}
				}
			}
		}
		GAMA.releaseScope(scopeMySelf);
		return null;
	}
}

/*******************************************************************************************************
 *
 * SimpleBdiPlanStatement.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.bdi;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.extension.bdi.SimpleBdiPlanStatement.SimpleBdiPlanValidator;

/**
 * The Class SimpleBdiPlanStatement.
 */
@symbol (
		name = { SimpleBdiArchitecture.PLAN },
		kind = ISymbolKind.BEHAVIOR,
		with_sequence = true,
		concept = { IConcept.BDI })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = IKeyword.WHEN,
				type = IType.BOOL,
				optional = true),
				@facet (
						name = SimpleBdiArchitecture.FINISHEDWHEN,
						type = IType.BOOL,
						optional = true),
				@facet (
						name = SimpleBdiArchitecture.PRIORITY,
						type = IType.FLOAT,
						optional = true),
				@facet (
						name = IKeyword.NAME,
						type = IType.ID,
						optional = true),
				@facet (
						name = SimpleBdiPlanStatement.INTENTION,
						type = PredicateType.id,
						optional = true),
				@facet (
						name = SimpleBdiPlanStatement.EMOTION,
						type = EmotionType.EMOTIONTYPE_ID,
						optional = true),
				@facet (
						name = SimpleBdiPlanStatement.THRESHOLD,
						type = IType.FLOAT,
						optional = true),
				@facet (
						name = SimpleBdiArchitecture.INSTANTANEOUS,
						type = IType.BOOL,
						optional = true) },
		omissible = IKeyword.NAME)
@validator (SimpleBdiPlanValidator.class)
@doc ("define an action plan performed by an agent using the BDI engine")
public class SimpleBdiPlanStatement extends AbstractStatementSequence {

	/**
	 * The Class SimpleBdiPlanValidator.
	 */
	public static class SimpleBdiPlanValidator implements IDescriptionValidator<IStatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IStatementDescription description) {
			// Verify that the state is inside a species with fsm control
			final ISpeciesDescription species = description.getSpeciesContext();
			final ISkillDescription control = species.getControl();
			if (!SimpleBdiArchitecture.class.isAssignableFrom(control.getJavaBase())) {
				description.error("A plan can only be defined in a simple_bdi architecture species",
						IGamlIssue.WRONG_CONTEXT);
			}
		}
	}

	/** The Constant INTENTION. */
	public static final String INTENTION = "intention";

	/** The Constant EMOTION. */
	public static final String EMOTION = "emotion";

	/** The Constant THRESHOLD. */
	public static final String THRESHOLD = "threshold";

	/** The when. */
	final IExpression _when;

	/** The priority. */
	final IExpression _priority;

	/** The executedwhen. */
	final IExpression _executedwhen;

	/** The instantaneous. */
	final IExpression _instantaneous;

	/** The intention. */
	final IExpression _intention;

	/** The emotion. */
	final IExpression _emotion;

	/** The threshold. */
	final IExpression _threshold;

	/**
	 * Gets the priority expression.
	 *
	 * @return the priority expression
	 */
	public IExpression getPriorityExpression() { return _priority; }

	/**
	 * Gets the context expression.
	 *
	 * @return the context expression
	 */
	public IExpression getContextExpression() { return _when; }

	/**
	 * Gets the executed expression.
	 *
	 * @return the executed expression
	 */
	public IExpression getExecutedExpression() { return _executedwhen; }

	/**
	 * Gets the instantaneous expression.
	 *
	 * @return the instantaneous expression
	 */
	public IExpression getInstantaneousExpression() { return _instantaneous; }

	/**
	 * Gets the intention expression.
	 *
	 * @return the intention expression
	 */
	public IExpression getIntentionExpression() { return _intention; }

	/**
	 * Gets the emotion expression.
	 *
	 * @return the emotion expression
	 */
	public IExpression getEmotionExpression() { return _emotion; }

	/**
	 * Gets the threshold.
	 *
	 * @return the threshold
	 */
	public IExpression getThreshold() { return _threshold; }

	/**
	 * Instantiates a new simple bdi plan statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public SimpleBdiPlanStatement(final IDescription desc) {
		super(desc);
		_when = getFacet(IKeyword.WHEN);
		_priority = getFacet(SimpleBdiArchitecture.PRIORITY);
		_executedwhen = getFacet(SimpleBdiArchitecture.FINISHEDWHEN);
		_instantaneous = getFacet(SimpleBdiArchitecture.INSTANTANEOUS);
		_intention = getFacet(SimpleBdiPlanStatement.INTENTION);
		_emotion = getFacet(SimpleBdiPlanStatement.EMOTION);
		_threshold = getFacet(SimpleBdiPlanStatement.THRESHOLD);
		setName(desc.getName());
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		if (_when == null || Cast.asBool(scope, _when.value(scope))) return super.privateExecuteIn(scope);
		return null;
	}

	/**
	 * Compute priority.
	 *
	 * @param scope
	 *            the scope
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public Double computePriority(final IScope scope) throws GamaRuntimeException {
		return Cast.asFloat(scope, _priority.value(scope));
	}
}

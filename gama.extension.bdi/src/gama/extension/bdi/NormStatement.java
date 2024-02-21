/*******************************************************************************************************
 *
 * NormStatement.java, in gama.extension.bdi, is part of the source code of the
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
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatementSequence;
import gama.gaml.types.IType;

/**
 * The Class NormStatement.
 */
@symbol(name = { NormStatement.NORM }, kind = ISymbolKind.BEHAVIOR, with_sequence = true, concept = {
		IConcept.BDI })
@inside(kinds = { ISymbolKind.SPECIES, ISymbolKind.MODEL })
@facets(value = { @facet(name = IKeyword.WHEN, type = IType.BOOL, optional = true, doc = @doc("the boolean condition when the norm is active")),
		@facet(name = SimpleBdiArchitecture.FINISHEDWHEN, type = IType.BOOL, optional = true, doc = @doc("the boolean condition when the norm is finished")),
		@facet(name = SimpleBdiArchitecture.PRIORITY, type = IType.FLOAT, optional = true, doc = @doc("the priority value of the norm")),
		@facet(name = IKeyword.NAME, type = IType.ID, optional = true, doc = @doc("the name of the norm")),
		@facet(name = NormStatement.INTENTION, type = PredicateType.id, optional = true, doc = @doc("the intention triggering the norm")),
		@facet(name = NormStatement.OBLIGATION, type = PredicateType.id, optional = true, doc = @doc("the obligation triggering of the norm")),
		@facet(name = NormStatement.THRESHOLD, type = IType.FLOAT, optional = true, doc = @doc("the threshold to trigger the norm")),
		@facet(name = NormStatement.LIFETIME, type = IType.INT, optional = true, doc = @doc("the lifetime of the norm")),
		@facet(name = SimpleBdiArchitecture.INSTANTANEAOUS, type = IType.BOOL, optional = true, doc = @doc("indicates if the norm is instananeous")) }, omissible = IKeyword.NAME)
@doc("a norm indicates what action the agent has to do in a certain context and with and obedience value higher than the threshold")

public class NormStatement extends AbstractStatementSequence{

	/** The Constant NORM. */
	public static final String NORM = "norm";
	
	/** The Constant INTENTION. */
	public static final String INTENTION = "intention";
	
	/** The Constant OBLIGATION. */
	public static final String OBLIGATION = "obligation";
	
	/** The Constant THRESHOLD. */
	public static final String THRESHOLD = "threshold";
	
	/** The Constant LIFETIME. */
	public static final String LIFETIME = "lifetime";
	
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
	
	/** The obligation. */
	final IExpression _obligation;
	
	/** The threshold. */
	final IExpression _threshold;
	
	/** The lifetime. */
	final IExpression _lifetime;
	
	/**
	 * Gets the context expression.
	 *
	 * @return the context expression
	 */
	public IExpression getContextExpression() {
		return _when;
	}
	
	/**
	 * Gets the executed expression.
	 *
	 * @return the executed expression
	 */
	public IExpression getExecutedExpression() {
		return _executedwhen;
	}
	
	/**
	 * Gets the intention expression.
	 *
	 * @return the intention expression
	 */
	public IExpression getIntentionExpression() {
		return _intention;
	}
	
	/**
	 * Gets the obligation expression.
	 *
	 * @return the obligation expression
	 */
	public IExpression getObligationExpression() {
		return _obligation;
	}
	
	/**
	 * Gets the instantaneous expression.
	 *
	 * @return the instantaneous expression
	 */
	public IExpression getInstantaneousExpression() {
		return _instantaneous;
	}
	
	/**
	 * Gets the threshold.
	 *
	 * @return the threshold
	 */
	public IExpression getThreshold() {
		return _threshold;
	}
	
	/**
	 * Gets the priority expression.
	 *
	 * @return the priority expression
	 */
	public IExpression getPriorityExpression() {
		return _priority;
	}
	
	/**
	 * Instantiates a new norm statement.
	 *
	 * @param desc the desc
	 */
	public NormStatement(IDescription desc) {
		super(desc);
		_when = getFacet(IKeyword.WHEN);
		_priority = getFacet(SimpleBdiArchitecture.PRIORITY);
		_executedwhen = getFacet(SimpleBdiArchitecture.FINISHEDWHEN);
		_instantaneous = getFacet(SimpleBdiArchitecture.INSTANTANEAOUS);
		_intention = getFacet(NormStatement.INTENTION);
		_obligation = getFacet(NormStatement.OBLIGATION);
		_threshold = getFacet(NormStatement.THRESHOLD);
		_lifetime = getFacet(NormStatement.LIFETIME);
		setName(desc.getName());
	}

	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		if (_when == null || Cast.asBool(scope, _when.value(scope))) {
			return super.privateExecuteIn(scope);
		}
		return null;
	}
	
}

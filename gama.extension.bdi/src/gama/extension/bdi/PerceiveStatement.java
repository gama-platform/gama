/*******************************************************************************************************
 *
 * PerceiveStatement.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.extension.bdi;

import java.util.Iterator;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IList;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.api.utils.list.GamaListFactory;
import gama.gaml.operators.spatial.SpatialQueries;
import gama.gaml.statements.RemoteSequence;

/**
 * The Class PerceiveStatement.
 */
@symbol (
		name = { PerceiveStatement.PERCEIVE },
		kind = ISymbolKind.BEHAVIOR,
		with_sequence = true,
		breakable = true, // ?//TODO:really ?
		remote_context = true,
		concept = { IConcept.BDI })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = true,
				doc = @doc ("the name of the perception")),
				@facet (
						name = IKeyword.AS,
						type = IType.SPECIES,
						optional = true,
						doc = @doc ("an expression that evaluates to a species")),
				@facet (
						name = IKeyword.WHEN,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("a boolean to tell when does the perceive is active")),
				@facet (
						name = IKeyword.PARALLEL,
						type = { IType.BOOL, IType.INT },
						optional = true,
						doc = @doc ("setting this facet to 'true' will allow 'perceive' to use concurrency with a parallel_bdi architecture; setting it to an integer will set the threshold under which they will be run sequentially (the default is initially 20, but can be fixed in the preferences). This facet is true by default.")),

				@facet (
						name = IKeyword.IN,
						type = { IType.FLOAT, IType.GEOMETRY },
						optional = true,
						doc = @doc ("a float or a geometry. If it is a float, it's a radius of a detection area. If it is a geometry, it is the area of detection of others species.")),
				@facet (
						name = PerceiveStatement.EMOTION,
						type = EmotionType.EMOTIONTYPE_ID,
						optional = true,
						doc = @doc ("The emotion needed to do the perception")),
				@facet (
						name = PerceiveStatement.THRESHOLD,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("Threshold linked to the emotion.")),
				@facet (
						name = IKeyword.TARGET,
						type = { IType.CONTAINER, /* IType.POINT, */
								IType.AGENT },
						of = IType.AGENT,
						optional = false,
						doc = @doc ("the list of the agent you want to perceive")) },
		omissible = IKeyword.NAME)
@doc (
		value = "Allow the agent, with a bdi architecture, to perceive others agents",
		usages = { @usage (
				value = "the basic syntax to perceive agents inside a circle of perception",
				examples = { @example (
						value = "perceive name_of_perception target: the_agents_you_want_to_perceive in: distance when: condition {",
						isExecutable = false),
						@example (
								value = "	//Here you are in the context of the perceived agents. To refer to the agent who does the perception, use myself.",
								isExecutable = false),
						@example (
								value = "	//If you want to make an action (such as adding a belief for example), use ask myself{ do the_action}",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }) })

@SuppressWarnings ({ "rawtypes", "unchecked" })
public class PerceiveStatement extends AbstractStatementSequence {

	/** The Constant PERCEIVE. */
	public static final String PERCEIVE = "perceive";

	/** The Constant EMOTION. */
	public static final String EMOTION = "emotion";

	/** The Constant THRESHOLD. */
	public static final String THRESHOLD = "threshold";

	/** The sequence. */
	private RemoteSequence sequence = null;

	/** The when. */
	final IExpression _when;

	/** The in. */
	final IExpression _in;

	/** The emotion. */
	final IExpression emotion;

	/** The threshold. */
	final IExpression threshold;

	/** The parallel. */
	final IExpression parallel;

	/** The target. */
	private final IExpression target = getFacet(IKeyword.TARGET);
	// AD Dangerous as it may still contain a value after the execution. Better
	// to use temp arrays
	// private final Object[] result = new Object[1];

	/**
	 * Gets the when.
	 *
	 * @return the when
	 */
	public IExpression getWhen() { return _when; }

	/**
	 * Gets the in.
	 *
	 * @return the in
	 */
	public IExpression getIn() { return _in; }

	@Override
	public void setChildren(final Iterable<? extends ISymbol> com) {
		sequence = new RemoteSequence(description);
		sequence.setName("commands of " + getName());
		sequence.setChildren(com);
	}

	@Override
	public void leaveScope(final IScope scope) {
		// scope.popLoop();
		super.leaveScope(scope);
	}

	/**
	 * Instantiates a new perceive statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public PerceiveStatement(final IDescription desc) {
		super(desc);
		_when = getFacet(IKeyword.WHEN);
		if (hasFacet(IKeyword.IN)) {
			_in = getFacet(IKeyword.IN);
		} else {
			_in = null;
		}
		if (hasFacet(IKeyword.NAME)) { setName(getLiteral(IKeyword.NAME)); }
		emotion = getFacet(PerceiveStatement.EMOTION);
		threshold = getFacet(PerceiveStatement.THRESHOLD);
		parallel = getFacet(IKeyword.PARALLEL);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {

		if (_when != null && !Cast.asBool(scope, _when.value(scope))
				|| emotion != null && !BdiUtils.hasEmotion(scope, (Emotion) emotion.value(scope)))
			return null;
		if (threshold != null && (emotion == null || BdiUtils.getEmotion(scope,
				(Emotion) emotion.value(scope)).intensity < (double) threshold.value(scope)))
			return null;

		final Object obj = target.value(scope);
		Object inArg = null;
		final IAgent ag = scope.getAgent();
		if (_in != null) { inArg = _in.value(scope); }

		if (inArg instanceof Number n) {
			IList temp = GamaListFactory.create();
			final double dist = Cast.asFloat(scope, n);
			if (obj instanceof IContainer container) {
				temp = SpatialQueries.at_distance(scope, container, Cast.asFloat(scope, inArg));
			} else if (obj instanceof IAgent agent && ag.euclidianDistanceTo(agent) <= dist) { temp.add(obj); }
			GamaExecutorService.execute(scope, sequence, temp.listValue(scope, Types.AGENT, false), null);
			return this;

		}
		if (inArg instanceof gama.api.gaml.types.GamaGeometryType || inArg instanceof IShape) {
			IList temp = GamaListFactory.create();
			final IShape geom = GamaShapeFactory.createFrom(scope, inArg, false);
			if (obj instanceof IContainer container) {
				temp = SpatialQueries.overlapping(scope, container, geom);
			} else if (obj instanceof IAgent agent && geom.intersects(agent)) { temp.add(obj); }
			GamaExecutorService.execute(scope, sequence, temp.listValue(scope, Types.AGENT, false), null);
			return this;
		}
		IExecutionResult result = null;
		final Iterator<? extends IAgent> runners = obj instanceof IContainer c ? c.iterable(scope).iterator()
				: obj instanceof IAgent agent ? transformAgentToList(agent, scope) : null;
		if (runners != null) {
			while (runners.hasNext() && (result = scope.execute(sequence, runners.next(), null)).passed()) {

			}
		}
		if (result != null) return result.getValue();

		return null;

	}

	/**
	 * Transform agent to list.
	 *
	 * @param temp
	 *            the temp
	 * @param scope
	 *            the scope
	 * @return the iterator
	 */
	Iterator<? extends IAgent> transformAgentToList(final IAgent temp, final IScope scope) {
		final IList<IAgent> tempList = GamaListFactory.create();
		tempList.add(temp);
		return tempList.iterable(scope).iterator();
	}

	/**
	 * Gets the parallel.
	 *
	 * @return the parallel
	 */
	public IExpression getParallel() { return parallel; }

}

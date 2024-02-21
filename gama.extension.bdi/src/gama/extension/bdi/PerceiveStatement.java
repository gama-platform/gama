/*******************************************************************************************************
 *
 * PerceiveStatement.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.extension.bdi;

import java.util.Iterator;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.ExecutionResult;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatementSequence;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class PerceiveStatement.
 */
@symbol (
		name = { PerceiveStatement.PERCEIVE },
		kind = ISymbolKind.BEHAVIOR,
		with_sequence = true,
		breakable = true, // ?
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

		if (_when == null || Cast.asBool(scope, _when.value(scope))) {
			final Object obj = target.value(scope);
			Object inArg = null;
			final IAgent ag = scope.getAgent();
			if (_in != null) { inArg = _in.value(scope); }
			if (emotion == null || SimpleBdiArchitecture.hasEmotion(scope, (Emotion) emotion.value(scope))) {
				if (threshold == null || emotion != null && threshold != null && SimpleBdiArchitecture.getEmotion(scope,
						(Emotion) emotion.value(scope)).intensity >= (Double) threshold.value(scope)) {
					if (inArg instanceof Float || inArg instanceof Integer || inArg instanceof Double) {
						IList temp = GamaListFactory.create();
						final double dist = Cast.asFloat(scope, inArg);
						if (obj instanceof IContainer) {
							temp = gama.gaml.operators.Spatial.Queries.at_distance(scope, (IContainer) obj,
									Cast.asFloat(scope, inArg));
						} else if (obj instanceof IAgent && ag.euclidianDistanceTo((IAgent) obj) <= dist) {
							temp.add(obj);
						}
						GamaExecutorService.execute(scope, sequence, temp.listValue(scope, Types.AGENT, false), null);
						return this;

					}
					if (inArg instanceof gama.gaml.types.GamaGeometryType || inArg instanceof GamaShape) {
						IList temp = GamaListFactory.create();
						final IShape geom = Cast.asGeometry(scope, inArg);
						if (obj instanceof IContainer) {
							temp = gama.gaml.operators.Spatial.Queries.overlapping(scope, (IContainer) obj,
									Cast.asGeometry(scope, inArg));
						} else if (obj instanceof IAgent && geom.intersects((IShape) obj)) { temp.add(obj); }
						GamaExecutorService.execute(scope, sequence, temp.listValue(scope, Types.AGENT, false), null);
						return this;
					}
					ExecutionResult result = null;
					final Iterator<IAgent> runners =
							obj instanceof IContainer ? ((IContainer) obj).iterable(scope).iterator()
									: obj instanceof IAgent ? transformAgentToList((IAgent) obj, scope) : null;
					if (runners != null) {
						while (runners.hasNext()
								&& (result = scope.execute(sequence, runners.next(), null)).passed()) {}
					}
					if (result != null) return result.getValue();
				}
			}
		}

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
	Iterator<IAgent> transformAgentToList(final IAgent temp, final IScope scope) {
		final IList<IAgent> tempList = GamaListFactory.create();
		tempList.add(temp);
		return ((IContainer) tempList).iterable(scope).iterator();
	}

	/**
	 * Gets the parallel.
	 *
	 * @return the parallel
	 */
	public IExpression getParallel() { return parallel; }

}

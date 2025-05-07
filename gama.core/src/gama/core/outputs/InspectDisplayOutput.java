/*******************************************************************************************************
 *
 * InspectDisplayOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.util.StringUtils;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.simulation.SimulationPopulation;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.dev.COUNTER;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.SymbolTracer;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.IStatement;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class AbstractInspectOutput.
 *
 * @author drogoul
 */
@symbol (
		name = { IKeyword.INSPECT, IKeyword.BROWSE },
		kind = ISymbolKind.OUTPUT,
		with_sequence = false,
		concept = { IConcept.INSPECTOR })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT },
		symbols = { IKeyword.OUTPUT, IKeyword.PERMANENT })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.NONE,
				optional = false,
				doc = @doc ("the identifier of the inspector")),
				@facet (
						name = IKeyword.REFRESH,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Indicates the condition under which this output should be refreshed (default is true)")),
				@facet (
						name = IKeyword.VALUE,
						type = IType.NONE,
						optional = true,
						doc = @doc ("the set of agents to inspect, could be a species, a list of agents or an agent")),
				@facet (
						name = IKeyword.ATTRIBUTES,
						type = { IType.LIST },
						optional = true,
						doc = @doc ("the list of attributes to inspect. A list that can contain strings or pair<string,type>, or a mix of them. These can be variables of the species, but also attributes present in the attributes table of the agent. The type is necessary in that case")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.ID,
						values = { IKeyword.AGENT, IKeyword.TABLE },
						optional = true,
						doc = @doc ("the way to inspect agents: in a table, or a set of inspectors")) },
		omissible = IKeyword.NAME)
@doc (
		value = "`" + IKeyword.INSPECT + "` (and `" + IKeyword.BROWSE
				+ "`) statements allows modeler to inspect a set of agents, in a table with agents and all their attributes or an agent inspector per agent, depending on the type: chosen. Modeler can choose which attributes to display. When `"
				+ IKeyword.BROWSE + "` is used, type: default value is table, whereas when`" + IKeyword.INSPECT
				+ "` is used, type: default value is agent.",
		usages = { @usage (
				value = "An example of syntax is:",
				examples = { @example (
						value = "inspect \"my_inspector\" value: ant attributes: [\"name\", \"location\"];",
						isExecutable = false) }) })
@SuppressWarnings ({ "rawtypes" })
public class InspectDisplayOutput extends AbstractValuedDisplayOutput implements IStatement {

	/** The Constant INSPECT_AGENT. */
	public static final short INSPECT_AGENT = 0;

	/** The Constant INSPECT_TABLE. */
	public static final short INSPECT_TABLE = 3;

	/** The Constant types. */
	static final List<String> types = Arrays.asList(IKeyword.AGENT, IKeyword.DYNAMIC, IKeyword.SPECIES, IKeyword.TABLE);

	/** The type. */
	String type;

	/** The attributes. */
	IExpression attributes;

	/** The list of attributes. */
	private Map<String, String> listOfAttributes;

	/** The root agent. */
	IMacroAgent rootAgent;

	/**
	 * Instantiates a new inspect display output.
	 *
	 * @param desc
	 *            the desc
	 */
	public InspectDisplayOutput(final IDescription desc) {
		super(desc);
		if (getValue() == null) {
			value = getFacet(IKeyword.NAME);
			expressionText = getValue() == null ? "" : getValue().serializeToGaml(false);
		}
		type = getLiteral(IKeyword.TYPE);
		if (type == null) {
			if (IKeyword.BROWSE.equals(getKeyword())) {
				type = IKeyword.TABLE;
			} else {
				type = IKeyword.AGENT;
			}
		}
		attributes = getFacet(IKeyword.ATTRIBUTES);
	}

	@Override
	public boolean init(final IScope scope) {
		super.init(scope);
		if (IKeyword.AGENT.equals(type) && getValue() != null) { lastValue = getValue().value(getScope()); }
		if (attributes != null) {
			listOfAttributes = (Map<String, String>) Types.MAP.of(Types.STRING, Types.STRING).cast(getScope(),
					attributes.value(getScope()), null, true);
		}
		if (rootAgent == null || rootAgent.dead()) { rootAgent = getScope().getRoot(); }
		return true;
	}

	/**
	 * Inspect.
	 *
	 * @param a
	 *            the a
	 * @return the inspect display output
	 */
	public static InspectDisplayOutput inspect(final IAgent a, final IExpression attributes) {

		IDescription desc = DescriptionFactory.create(IKeyword.INSPECT, IKeyword.NAME,
				StringUtils.toGamlString("Inspect: "), IKeyword.TYPE, types.get(INSPECT_AGENT)).validate();
		desc.setFacet(IKeyword.VALUE, GAML.getExpressionFactory().createConst(a, a.getGamlType()));
		desc.validate();
		if (attributes != null) { desc.setFacet(IKeyword.ATTRIBUTES, attributes); }
		// Opens directly an inspector
		InspectDisplayOutput result = new InspectDisplayOutput(desc);
		// result.setValue(GAML.getExpressionFactory().createConst(a, a.getGamlType()));
		result.lastValue = a;
		return result;
	}

	/**
	 * Instantiates a new inspect display output.
	 *
	 * @param a
	 *            the a
	 */
	public static InspectDisplayOutput inspect(final IExperimentAgent a, final IExpression attributes) {
		// Opens directly an inspector
		IDescription desc = DescriptionFactory.create(IKeyword.INSPECT, IKeyword.NAME,
				StringUtils.toGamlString("Inspect: "), IKeyword.TYPE, types.get(INSPECT_TABLE)).validate();
		final SimulationPopulation sp = a.getSimulationPopulation();
		desc.setFacet(IKeyword.VALUE, GAML.getExpressionFactory().createConst(sp, sp.getGamlType()));
		if (attributes != null) { desc.setFacet(IKeyword.ATTRIBUTES, attributes); }
		InspectDisplayOutput result = new InspectDisplayOutput(desc);
		result.lastValue = sp;
		result.rootAgent = a;
		return result;
	}

	/**
	 * Instantiates a new inspect display output.
	 *
	 * @param rootAgent
	 *            the root agent
	 * @param species
	 *            the species
	 */
	static public InspectDisplayOutput browse(final IMacroAgent rootAgent, final ISpecies species,
			final IExpression attributes) {
		// Opens a table inspector on the agents of this species
		IDescription desc = DescriptionFactory
				.create(IKeyword.INSPECT, GAML.getExperimentContext(rootAgent), IKeyword.NAME,
						StringUtils.toGamlString("Browse(" + COUNTER.COUNT() + ")"), IKeyword.VALUE,
						species == null ? "nil" : species.getName(), IKeyword.TYPE, types.get(INSPECT_TABLE))
				.validate();
		if (attributes != null) { desc.setFacet(IKeyword.ATTRIBUTES, attributes); }
		InspectDisplayOutput result = new InspectDisplayOutput(desc);
		result.rootAgent = rootAgent;
		return result;
	}

	/**
	 * Instantiates a new inspect display output.
	 *
	 * @param agent
	 *            the agent
	 * @param agents
	 *            the agents
	 */
	static public InspectDisplayOutput browse(final IMacroAgent agent, final Collection<? extends IAgent> agents,
			final IExpression attributes) {
		// Opens a table inspector on the agents of this container
		IDescription desc = DescriptionFactory.create(IKeyword.INSPECT, GAML.getExperimentContext(agent), IKeyword.NAME,
				StringUtils.toGamlString("Browse(" + COUNTER.COUNT() + ")"), IKeyword.VALUE,
				StringUtils.toGaml(agents, false), IKeyword.TYPE, types.get(INSPECT_TABLE)).validate();
		if (attributes != null) { desc.setFacet(IKeyword.ATTRIBUTES, attributes); }
		InspectDisplayOutput result = new InspectDisplayOutput(desc);
		result.lastValue = agents;
		result.rootAgent = agent;
		return result;
	}

	/**
	 * Instantiates a new inspect display output.
	 *
	 * @param agent
	 *            the agent
	 * @param agents
	 *            the agents
	 */
	static public InspectDisplayOutput browse(final IMacroAgent agent, final IExpression agents,
			final IExpression attributes) {
		// Opens a table inspector on the agents of this container
		IDescription desc = DescriptionFactory.create(IKeyword.INSPECT, GAML.getExperimentContext(agent), IKeyword.NAME,
				StringUtils.toGamlString("Browse(" + COUNTER.COUNT() + ")"), IKeyword.TYPE, types.get(INSPECT_TABLE))
				.validate();
		desc.setFacet(IKeyword.VALUE, agents);
		if (attributes != null) { desc.setFacet(IKeyword.ATTRIBUTES, attributes); }
		InspectDisplayOutput result = new InspectDisplayOutput(desc);

		// lastValue = agents;
		result.rootAgent = agent;
		return result;
	}

	/**
	 * Launch.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public void launch(final IScope scope) throws GamaRuntimeException {
		if (!scope.init(InspectDisplayOutput.this).passed()) return;
		// TODO What to do in case of multi-simulations ???
		if (scope.getSimulation() != null) {
			scope.getSimulation().addOutput(InspectDisplayOutput.this);
		} else if (scope.getExperiment() != null) {
			scope.getExperiment().getSpecies().getExperimentOutputs().add(InspectDisplayOutput.this);
		}
		setPaused(false);
		open();
		step(scope);
		update();
	}

	@Override
	public boolean step(final IScope scope) {
		// ((AbstractScope) getScope()).traceAgents = true;
		if (IKeyword.TABLE.equals(type)) {
			if (rootAgent == null || rootAgent.dead()) return false;
			if (getValue() == null) return true;
			if (getScope().interrupted()) return false;
			try {
				getScope().setCurrentSymbol(this);
				lastValue = getScope().evaluate(getValue(), rootAgent).getValue();
			} finally {
				scope.setCurrentSymbol(null);
			}
		}
		return true;
	}

	@Override
	public boolean isUnique() { return !IKeyword.TABLE.equals(type); }

	@Override
	public String getId() { return isUnique() ? getViewId() : getViewId() + getName(); }

	@Override
	public String getViewId() {
		if (IKeyword.TABLE.equals(type)) return IGui.TABLE_VIEW_ID;
		return IGui.AGENT_VIEW_ID;

	}

	/** The Constant EMPTY. */
	final static IAgent[] EMPTY = {};

	@Override
	public IAgent[] getLastValue() {
		if (IKeyword.TABLE.equals(type) && (rootAgent == null || rootAgent.dead())) return EMPTY;
		// DEBUG.LOG("Last value :" + lastValue);
		if (lastValue instanceof IAgent a) return new IAgent[] { a };
		if (lastValue instanceof ISpecies s && rootAgent != null) {
			final IPopulation pop = rootAgent.getMicroPopulation(s);
			return pop.toArray();
		}
		if (lastValue instanceof IContainer)
			return ((IContainer<?, ?>) lastValue).listValue(getScope(), Types.NO_TYPE, false).toArray(new IAgent[0]);
		return EMPTY;
	}

	// /**
	// * Gets the species.
	// *
	// * @return the species
	// */
	// public ISpecies getSpecies() {
	// final IExpression valueExpr = getValue();
	// if (valueExpr == null) return null;
	// final IType theType = valueExpr.getGamlType().getContentType();
	// if (theType == Types.get(IKeyword.MODEL)) return getScope().getModel().getSpecies();
	// final SpeciesDescription sd = theType.getSpecies();
	// if (sd == null) return getScope().getModel().getSpecies(IKeyword.AGENT);
	// if (sd.equals(getScope().getModel().getDescription())) return getScope().getModel().getSpecies();
	// String speciesName = sd.getName();
	// if (speciesName == null) { speciesName = IKeyword.AGENT; }
	// return rootAgent.getSpecies().getMicroSpecies(speciesName);
	// }

	/**
	 * Gets the species description.
	 *
	 * @return the species description
	 */
	public SpeciesDescription getSpeciesDescription() {
		final IExpression valueExpr = getValue();
		if (valueExpr == null) return null;
		final IType theType = valueExpr.getGamlType().getContentType();
		if (theType == Types.get(IKeyword.MODEL)) return getScope().getModel().getDescription();
		final SpeciesDescription sd = theType.getSpecies();
		if (sd instanceof ModelDescription) return sd;
		if (sd == null) return Types.AGENT.getDenotedSpecies();
		String speciesName = sd.getName();
		if (speciesName == null) return Types.AGENT.getDenotedSpecies();
		return rootAgent.getSpecies().getDescription().getMicroSpecies(speciesName);
	}

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() { return listOfAttributes; }

	/**
	 * Gets the root agent.
	 *
	 * @return the root agent
	 */
	public IMacroAgent getRootAgent() { return rootAgent; }

	@Override
	public void dispose() {
		super.dispose();
		rootAgent = null;
		attributes = null;
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		final IType theType = value.getGamlType();
		if (theType.isAgentType()) {
			GAMA.getGui().setSelectedAgent((IAgent) value.value(scope));
		} else if (theType.isContainer()) { ValuedDisplayOutputFactory.browse(scope.getRoot(), value, attributes); }
		return value.value(scope);
	}

	@Override
	public String getTrace(final IScope scope) {
		return new SymbolTracer().trace(scope, this);
	}

}

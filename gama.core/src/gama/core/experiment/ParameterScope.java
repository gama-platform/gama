/*******************************************************************************************************
 *
 * ParameterScope.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.experiment;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.symbols.IVariable;
import gama.api.kernel.object.IObject;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.scope.ExecutionScope;
import gama.api.runtime.scope.IScope;

/**
 * Short-lived simulation-parameter configuration, owned by an experiment but independent of its live simulations.
 * Configured values take precedence over global initializers; initializers are evaluated once per context, including
 * declared {@code nil} values. There is no simulation agent to expose as {@code self} or {@code world}.
 */
public final class ParameterScope extends ExecutionScope {

	private final IExperimentSpecies experiment;
	private final IExperimentAgent owner;
	private final Set<String> resolving = new LinkedHashSet<>();
	private final Map<String, Object> initialValues = new HashMap<>();

	public ParameterScope(final IExperimentSpecies experiment) {
		this(experiment, experiment.getAgent());
	}

	public ParameterScope(final IExperimentSpecies experiment, final IExperimentAgent owner) {
		super(owner, "in parameter configuration");
		this.experiment = experiment;
		this.owner = owner;
	}

	private ParameterScope(final ParameterScope source, final String name, final Sharing sharing) {
		super(source, name, sharing);
		experiment = source.experiment;
		owner = source.owner;
		resolving.addAll(source.resolving);
		initialValues.putAll(source.initialValues);
	}

	@Override
	public IScope copy(final String name) { return new ParameterScope(this, name, Sharing.IF_OWNED); }

	@Override
	public IScope fork(final String name) { return new ParameterScope(this, name, Sharing.ALWAYS); }

	@Override
	public IScope detachedCopy(final String name) { return new ParameterScope(this, name, Sharing.NEVER); }

	@Override
	public IModelSpecies getModel() { return experiment.getModel(); }

	@Override
	public IExperimentAgent getExperiment() { return owner; }

	@Override
	public ISimulationAgent getSimulation() { return null; }

	@Override
	public IObject getCurrentObjectOrAgent() {
		throw GamaRuntimeException.error("self is not available in parameter configuration: no simulation is selected.",
				this);
	}

	@Override
	public boolean hasAccessToGlobalVar(final String name) {
		final IParameter parameter = getParameter(name);
		return !"world".equals(name) && (getModel().hasVar(name)
				|| parameter != null && !parameter.isDefinedInExperiment()
				|| getExperiment() instanceof ExperimentAgent agent && agent.extraParametersMap.containsKey(name));
	}

	private IParameter getParameter(final String name) {
		final IParameter parameter = experiment.getParameters().get(name);
		return parameter == null ? experiment.getExplorableParameters().get(name) : parameter;
	}

	@Override
	public Object getGlobalVarValue(final String name) {
		if ("world".equals(name))
			throw GamaRuntimeException.error("world is not available in parameter configuration.", this);
		if (!resolving.add(name))
			throw GamaRuntimeException.error("Cyclic parameter initialization: " + String.join(" -> ", resolving)
					+ " -> " + name, this);
		try {
			if (getExperiment() instanceof ExperimentAgent agent && agent.extraParametersMap.containsKey(name))
				return agent.extraParametersMap.get(name);
			final IParameter parameter = getParameter(name);
			if (parameter != null && !parameter.isDefinedInExperiment()) return parameter.value(this);
			final IVariable variable = getModel().getVar(name);
			if (variable == null)
				throw GamaRuntimeException.error("No configured parameter or global variable named " + name, this);
			if (initialValues.containsKey(name)) return initialValues.get(name);
			final IExpression initial = variable.hasFacet(IKeyword.INIT) ? variable.getFacet(IKeyword.INIT)
					: variable.getFacet(IKeyword.FUNCTION);
			final Object value = initial == null ? variable.getType().createDefaultValue(this) : initial.value(this);
			initialValues.put(name, value);
			return value;
		} finally {
			resolving.remove(name);
		}
	}

	@Override
	public void setGlobalVarValue(final String name, final Object value) {
		final IParameter parameter = getParameter(name);
		if (parameter != null && !parameter.isDefinedInExperiment()) {
			parameter.setValue(this, value);
		} else if (getModel().hasVar(name) && getExperiment() instanceof ExperimentAgent agent) {
			agent.extraParametersMap.put(name, value);
		} else {
			throw GamaRuntimeException.error("No configurable global variable named " + name, this);
		}
	}
}

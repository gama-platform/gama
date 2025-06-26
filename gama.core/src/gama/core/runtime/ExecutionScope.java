/*******************************************************************************************************
 *
 * ExecutionScope.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime;

import static gama.core.runtime.ExecutionResult.FAILED;
import static gama.core.runtime.ExecutionResult.PASSED;
import static gama.core.runtime.ExecutionResult.withValue;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import gama.core.common.interfaces.IStepable;
import gama.core.common.util.RandomUtils;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.kernel.model.IModel;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.kernel.simulation.SimulationClock;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulationFactory;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.benchmark.StopWatch;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.Collector;
import gama.dev.COUNTER;
import gama.dev.DEBUG;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Strings;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.IExecutable;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.types.IType;
import gama.gaml.types.ITypesManager;
import gama.gaml.types.Types;

/**
 * Class AbstractScope.
 *
 * @author drogoul
 * @since 23 mai 2013
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class ExecutionScope implements IScope {

	static {
		DEBUG.OFF();
	}

	/** The scope name. */
	private final String scopeName;

	/** The execution context. */
	protected IExecutionContext executionContext;

	/** The agent context. */
	protected AgentExecutionContext agentContext;

	/** The errors disabled. */
	private volatile boolean _trace, _in_try_mode, _errors_disabled;

	/** The flow status. */
	protected volatile AtomicReference<FlowStatus> flowStatus = new AtomicReference(FlowStatus.NORMAL);

	/** The root agent. */
	private ITopLevelAgent rootAgent;

	/** The topology. */
	private ITopology topology;

	/** The types. */
	private ITypesManager types;

	/** The lock. */
	private final Object lock = new Object();

	/**
	 * Instantiates a new execution scope.
	 *
	 * @param root
	 *            the root
	 */
	public ExecutionScope(final ITopLevelAgent root) {
		this(root, null);
	}

	/**
	 * Instantiates a new execution scope.
	 *
	 * @param root
	 *            the root
	 * @param otherName
	 *            the other name
	 * @param context
	 *            the context
	 * @param agentContext
	 *            the agent context
	 * @param specialContext
	 *            the special context
	 */
	public ExecutionScope(final ITopLevelAgent root, final String otherName) {
		StringBuilder name = new StringBuilder("Scope #").append(COUNTER.COUNT());
		setRoot(root);
		if (root != null) { name.append(" of ").append(root.stringValue(root.getScope())); }
		name.append(otherName == null || otherName.isEmpty() ? "" : " (" + otherName + ")");
		this.scopeName = name.toString();
		this.setExecutionContext(ExecutionContext.create(this, null));
		this.agentContext = AgentExecutionContext.create(root, null);
	}

	/**
	 * Creates the child context.
	 *
	 * @param agent
	 *            the agent
	 * @return the agent execution context
	 */
	public AgentExecutionContext createChildContext(final IAgent agent) {
		return AgentExecutionContext.create(agent, agentContext);
	}

	/**
	 * Method clear()
	 *
	 * @see gama.core.runtime.IScope#clear()
	 */
	@Override
	public void clear() {
		if (executionContext != null) { executionContext.dispose(); }
		setExecutionContext(null);
		if (agentContext != null) { agentContext.dispose(); }
		agentContext = null;
		setFlowStatus(FlowStatus.DISPOSE);
	}

	@Override
	public void disableErrorReporting() {
		_errors_disabled = true;
	}

	@Override
	public void enableErrorReporting() {
		_errors_disabled = false;
	}

	@Override
	public boolean reportErrors() {
		return !_errors_disabled;
	}

	/**
	 * In 'try' mode, the errors are thrown even if _errors_disabled is true
	 */
	@Override
	public void enableTryMode() {
		_in_try_mode = true;
	}

	@Override
	public void disableTryMode() {
		_in_try_mode = false;
	}

	@Override
	public boolean isInTryMode() { return _in_try_mode; }

	@Override
	public void setTrace(final boolean t) { _trace = t; }

	/**
	 * Gets the topology.
	 *
	 * @return the topology
	 */
	@Override
	public ITopology getTopology() {
		if (topology != null) return topology;
		final IAgent a = getAgent();
		return a == null ? null : a.getTopology();
	}

	/**
	 * Sets the topology.
	 *
	 * @param topo
	 *            the new topology
	 */
	@Override
	public void setTopology(final ITopology topo) { topology = topo; }

	/**
	 *
	 * Method interrupted(). Returns true if the scope is currently marked as interrupted.
	 *
	 * @see gama.core.runtime.IScope#interrupted()
	 */
	@Override
	public final boolean interrupted() {
		return INTERRUPTING_STATUSES.contains(flowStatus.get());
	}

	@Override
	public final boolean isClosed() { return flowStatus.get() == FlowStatus.DISPOSE; }

	@Override
	public boolean isOnUserHold() {
		final ITopLevelAgent root = getRoot();
		if (root == null) return false;
		return root.isOnUserHold();
	}

	@Override
	public void setOnUserHold(final boolean state) {
		final ITopLevelAgent root = getRoot();
		if (root == null) return;
		root.setOnUserHold(state);
	}

	@Override
	public boolean push(final IAgent agent) {
		synchronized (lock) {
			final IAgent a = agentContext == null ? null : agentContext.getAgent();
			if (a == null) {
				if (agent instanceof ITopLevelAgent tla) {
					// Previous context didnt have a root.
					setRoot(tla);
				}
				// get rid of the previous context **important**
				agentContext = null;
			} else if (a == agent) return false;
			agentContext = createChildContext(agent);
			return true;
		}
	}

	/**
	 * Sets the root.
	 *
	 * @param agent
	 *            the new root
	 */
	protected void setRoot(final ITopLevelAgent agent) { rootAgent = agent; }

	/**
	 * Method pop()
	 *
	 * @see gama.core.runtime.IScope#pop(gama.core.metamodel.agent.IAgent)
	 */
	@Override
	public synchronized void pop(final IAgent agent) {
		synchronized (lock) {
			if (agentContext == null) // DEBUG.OUT("Agents stack is empty");
				return;
			final AgentExecutionContext previous = agentContext;
			agentContext = agentContext.getOuterContext();
			previous.dispose();
			getAndClearDeathStatus();
		}
	}

	/**
	 * Method push()
	 *
	 * @see gama.core.runtime.IScope#push(gama.gaml.statements.IStatement)
	 */
	@Override
	public void push(final ISymbol statement) {
		setCurrentSymbol(statement);
		if (executionContext != null) {
			setExecutionContext(executionContext.createChildContext(statement));
		} else {
			setExecutionContext(ExecutionContext.create(this, statement));
		}
	}

	@Override
	public void setCurrentSymbol(final ISymbol statement) {
		if (executionContext != null) { executionContext.setCurrentSymbol(statement); }
		if (statement != null && _trace) { writeTrace(); }
	}

	/**
	 *
	 */
	private void writeTrace() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < executionContext.depth(); i++) { sb.append(Strings.TAB); }
		sb.append(getCurrentSymbol().getTrace(this));
		this.getGui().getConsole().informConsole(sb.toString(), getRoot());
	}

	/**
	 * Method pop()
	 *
	 * @see gama.core.runtime.IScope#pop(gama.gaml.statements.IStatement)
	 */
	@Override
	public void pop(final ISymbol symbol) {
		if (executionContext != null) {
			final IExecutionContext previous = executionContext;
			setExecutionContext(executionContext.getOuterContext());
			previous.dispose();
		}
	}

	@Override
	public ISymbol getCurrentSymbol() { return executionContext == null ? null : executionContext.getCurrentSymbol(); }

	/**
	 * Method execute(). Asks the scope to manage the execution of a statement on an agent, taking care of pushing the
	 * agent on the stack, verifying the runtime state, etc. This method accepts optional arguments (which can be null)
	 *
	 * @see gama.core.runtime.IScope#execute(gama.gaml.statements.IStatement, gama.core.metamodel.agent.IAgent)
	 */
	@Override
	public ExecutionResult execute(final IExecutable statement, final IAgent target,
			final boolean useTargetScopeForExecution, final Arguments args) {
		if (statement == null || target == null || interrupted() || target.dead()) return FAILED;
		// We keep the current pushed agent (context of this execution)
		final IAgent caller = this.getAgent();
		// We then try to push the agent on the stack
		final boolean pushed = push(target);
		try (StopWatch w = GAMA.benchmark(this, statement)) {
			// Otherwise we compute the result of the statement, pushing the
			// arguments if the statement expects them
			if (args != null) { args.setCaller(caller); }
			// See issue #2815: we also push args even if they are null
			statement.setRuntimeArgs(this, args);
			// #3407 a specific case when create micro experiment, the myself (agentcontext) is as same as target
			// fixed by change myself to outer agentcontext
			if (statement instanceof RemoteSequence
					&& "create".equals(((RemoteSequence) statement).getDescription().getKeyword())
					&& caller.equals(target)) {

				statement.setMyself(this.agentContext.outer.getAgent());
			} else {
				statement.setMyself(caller);
			}
			// We push the caller to the remote sequence (will be cleaned when the remote
			// sequence leaves its scope)
			return withValue(statement.executeOn(useTargetScopeForExecution ? target.getScope() : ExecutionScope.this));
		} catch (final GamaRuntimeException g) {
			GAMA.reportAndThrowIfNeeded(this, g, true);
			return ExecutionResult.FAILED;
		} finally {
			// We clean the caller that may have been set previously so as to keep the
			// arguments clean
			if (args != null) { args.setCaller(null); }
			// Whatever the outcome, we pop the agent from the stack if it has
			// been previously pushed
			if (pushed) { pop(target); }
		}

	}

	@Override
	public void stackArguments(final Arguments actualArgs) {
		if (actualArgs == null) return;
		boolean callerPushed = false;
		final IAgent caller = actualArgs.getCaller();
		if (caller != null) { callerPushed = push(caller); }
		try {
			actualArgs.forEachFacet((a, b) -> {
				final IExpression e = b.getExpression();
				if (e != null) { addVarWithValue(a, e.value(ExecutionScope.this)); }
				return true;
			});

		} finally {
			if (callerPushed) { pop(caller); }
		}
	}

	@Override
	public ExecutionResult step(final IStepable agent) {
		if (agent == null || interrupted()) return FAILED;
		try (StopWatch w = GAMA.benchmark(this, agent)) {
			return withValue(agent.step(this));
		} catch (final Throwable ex) {
			if (ex instanceof OutOfMemoryError) {
				GamaExecutorService.EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), ex);
				return FAILED;
			}
			final GamaRuntimeException g = GamaRuntimeException.create(ex, this);
			GAMA.reportAndThrowIfNeeded(this, g, true);
			return FAILED;
		}
	}

	@Override
	public ExecutionResult init(final IStepable agent) {
		if (agent == null || interrupted()) return FAILED;
		try (StopWatch w = GAMA.benchmark(this, agent)) {
			return withValue(agent.init(this));
		} catch (final Throwable ex) {
			if (ex instanceof OutOfMemoryError) {
				GamaExecutorService.EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), ex);
				return FAILED;
			}
			final GamaRuntimeException g = GamaRuntimeException.create(ex, this);
			GAMA.reportAndThrowIfNeeded(this, g, true);
			return FAILED;
		}
	}

	@Override
	public ExecutionResult step(final IAgent agent) {
		if (agent == null || agent.dead() || interrupted()) return FAILED;
		final boolean pushed = push(agent);
		try {
			try (StopWatch w = GAMA.benchmark(this, agent)) {
				return withValue(agent.step(this));
			} catch (final Throwable ex) {
				if (ex instanceof OutOfMemoryError) {
					GamaExecutorService.EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), ex);
					return FAILED;
				}
				final GamaRuntimeException g = GamaRuntimeException.create(ex, this);
				GAMA.reportAndThrowIfNeeded(this, g, true);
				return FAILED;
			}
		} finally {
			if (pushed) { pop(agent); }
		}
	}

	@Override
	public ExecutionResult init(final IAgent agent) {
		if (agent == null || agent.dead() || interrupted()) return FAILED;
		final boolean pushed = push(agent);
		try {
			try (StopWatch w = GAMA.benchmark(this, agent)) {
				return withValue(agent.init(this));
			} catch (final Throwable ex) {
				if (ex instanceof OutOfMemoryError) {
					GamaExecutorService.EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), ex);
					return FAILED;
				}
				final GamaRuntimeException g = GamaRuntimeException.create(ex, this);
				GAMA.reportAndThrowIfNeeded(this, g, true);
				return FAILED;
			}
		} finally {
			if (pushed) { pop(agent); }
		}
	}

	@Override
	public ExecutionResult evaluate(final IExpression expr, final IAgent agent) throws GamaRuntimeException {
		if (agent == null || agent.dead() || interrupted()) return FAILED;
		final boolean pushed = push(agent);
		try {
			try (StopWatch w = GAMA.benchmark(this, agent)) {
				return withValue(expr.value(this));
			} catch (final Throwable ex) {
				if (ex instanceof OutOfMemoryError) {
					GamaExecutorService.EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), ex);
					return FAILED;
				}
				final GamaRuntimeException g = GamaRuntimeException.create(ex, this);
				GAMA.reportAndThrowIfNeeded(this, g, true);
				return FAILED;
			}
		} finally {
			if (pushed) { pop(agent); }
		}
	}

	/**
	 * Method getVarValue()
	 *
	 * @see gama.core.runtime.IScope#getVarValue(java.lang.String)
	 */
	@Override
	public final Object getVarValue(final String varName) {
		if (executionContext != null) return executionContext.getTempVar(varName);
		return null;
	}

	/**
	 * Method setVarValue(). Sets the value of varName in either the current context or its outer context
	 *
	 * @see gama.core.runtime.IScope#setVarValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public final void setVarValue(final String varName, final Object val) {
		if (executionContext != null) { executionContext.setTempVar(varName, val); }
	}

	/**
	 * Method saveAllVarValuesIn()
	 *
	 * @see gama.core.runtime.IScope#saveAllVarValuesIn(java.util.Map)
	 */
	@Override
	public void saveAllVarValuesIn(final Map<String, Object> varsToSave) {
		if (executionContext != null && varsToSave != null) { varsToSave.putAll(executionContext.getLocalVars()); }
	}

	/**
	 * Method addVarWithValue(). Adds a local var to the current scope
	 *
	 * @see gama.core.runtime.IScope#addVarWithValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void addVarWithValue(final String varName, final Object val) {
		if (executionContext != null) { executionContext.putLocalVar(varName, val); }
	}

	/**
	 * Method getArg()
	 *
	 * @see gama.core.runtime.IScope#getArg(java.lang.String, int)
	 */
	@Override
	public Object getArg(final String string, final int type) throws GamaRuntimeException {
		if (executionContext != null)
			return Types.get(type).cast(this, executionContext.getLocalVar(string), null, false);
		return null;
	}

	/**
	 * Method hasArg()
	 *
	 * @see gama.core.runtime.IScope#hasArg(java.lang.String)
	 */
	@Override
	public boolean hasArg(final String name) {
		if (executionContext != null) return executionContext.hasLocalVar(name);
		return false;
	}

	/**
	 * Method getAgentVarValue()
	 *
	 * @see gama.core.runtime.IScope#getAgentVarValue(gama.core.metamodel.agent.IAgent, java.lang.String)
	 */
	@Override
	public Object getAgentVarValue(final IAgent agent, final String name) throws GamaRuntimeException {
		if (agent == null || agent.dead() || interrupted()) return null;
		final boolean pushed = push(agent);
		try {
			return agent.getDirectVarValue(ExecutionScope.this, name);
		} finally {
			if (pushed) { pop(agent); }
		}
	}

	/**
	 * Method setAgentVarValue()
	 *
	 * @see gama.core.runtime.IScope#setAgentVarValue(gama.core.metamodel.agent.IAgent, java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public void setAgentVarValue(final IAgent agent, final String name, final Object v) {
		if (agent == null || agent.dead() || interrupted()) return;
		final boolean pushed = push(agent);
		try {
			agent.setDirectVarValue(ExecutionScope.this, name, v);
		} finally {
			if (pushed) { pop(agent); }
		}
	}

	@Override
	public ExecutionResult update(final IAgent a) {
		if (a == null || a.dead() || interrupted()) return FAILED;
		final boolean pushed = push(a);
		try {
			a.getPopulation().updateVariables(this, a);
			return PASSED;
		} catch (final GamaRuntimeException g) {
			GAMA.reportAndThrowIfNeeded(this, g, true);
			return FAILED;
		} finally {
			if (pushed) { pop(a); }
		}
	}

	/**
	 * Method getGlobalVarValue()
	 *
	 * @see gama.core.runtime.IScope#getGlobalVarValue(java.lang.String)
	 */
	@Override
	public Object getGlobalVarValue(final String name) throws GamaRuntimeException {
		final ITopLevelAgent root = getRoot();
		if (root == null) return null;
		return root.getDirectVarValue(this, name);
	}

	@Override
	public boolean hasAccessToGlobalVar(final String name) {
		final ITopLevelAgent root = getRoot();
		if (root == null) return false;
		return root.hasAttribute(name);
	}

	/**
	 * Method setGlobalVarValue()
	 *
	 * @see gama.core.runtime.IScope#setGlobalVarValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setGlobalVarValue(final String name, final Object v) throws GamaRuntimeException {
		final ITopLevelAgent root = getRoot();
		if (root == null) return;
		root.setDirectVarValue(this, name, v);
	}

	/**
	 * Method getName()
	 *
	 * @see gama.core.runtime.IScope#getName()
	 */

	@Override
	public String getName() { return scopeName; }

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Method getAgentScope()
	 *
	 * @see gama.core.runtime.IScope#getAgent()
	 */
	@Override
	public IAgent getAgent() {
		if (agentContext == null) return null;
		return agentContext.getAgent();
	}

	/**
	 * Method getSimulationScope()
	 *
	 * @see gama.core.runtime.IScope#getSimulation()
	 */
	@Override
	public SimulationAgent getSimulation() {
		if (agentContext != null) return agentContext.getSimulation();
		final ITopLevelAgent root = getRoot();
		if (root == null) return null;
		return root.getSimulation();
	}

	@Override
	public IExperimentAgent getExperiment() {
		final ITopLevelAgent root = getRoot();
		if (root == null) return null;
		return root.getExperiment();
	}

	@Override
	public IPopulationFactory getPopulationFactory() {
		IExperimentAgent exp = getExperiment();
		if (exp == null) return null;
		return exp.getPopulationFactory();
	}

	/**
	 * Method getModel()
	 *
	 * @see gama.core.runtime.IScope#getModel()
	 */
	@Override
	public IModel getModel() {
		final ITopLevelAgent root = getRoot();
		if (root == null) return null;
		return root.getModel();
	}

	@Override
	public IType getType(final String name) {
		if (types == null) {
			IModel model = getModel();
			if (model == null) {
				types = Types.builtInTypes;
			} else {
				ModelDescription desc = model.getDescription();
				types = desc.getTypesManager();
			}
		}
		return types.get(name);
	}

	/**
	 * Method getClock()
	 *
	 * @see gama.core.runtime.IScope#getClock()
	 */
	@Override
	public SimulationClock getClock() {
		final ITopLevelAgent root = getRoot();
		if (root == null) return null;
		return root.getClock();
	}

	@Override
	public IAgent[] getAgentsStack() {
		try (final Collector.AsOrderedSet<IAgent> agents = Collector.getOrderedSet()) {
			AgentExecutionContext current = agentContext;
			if (current == null) return new IAgent[0];
			while (current != null) {
				agents.add(current.getAgent());
				current = current.getOuterContext();
			}
			return agents.items().stream().toArray(IAgent[]::new);
		}
	}

	/**
	 * Method popReadAttributes()
	 *
	 * @see gama.core.runtime.IScope#popReadAttributes()
	 */
	@Override
	public Map popReadAttributes() {
		if (executionContext != null) {
			final Map value = (Map) this.getVarValue(KEY_ATTRIBUTES);
			executionContext.removeLocalVar(KEY_ATTRIBUTES);
			return value;
		}
		return Collections.EMPTY_MAP;
	}
	//
	// @Override
	// public IGui getGui() {
	// if (gui != null) return gui;
	// final IExperimentAgent experiment = getExperiment();
	// if (experiment == null) {
	// gui = GAMA.getGui();
	// } else if (experiment.getSpecies().isHeadless()) {
	// gui = GAMA.getHeadlessGui();
	// } else {
	// gui = GAMA.getRegularGui();
	// }
	// return gui;
	// }

	@Override
	public ITopLevelAgent getRoot() { return rootAgent; }

	@Override
	public boolean isPaused() { return GAMA.isPaused() || isOnUserHold(); }

	/**
	 * Method getRandom()
	 *
	 * @see gama.core.runtime.IScope#getRandom()
	 */
	@Override
	public RandomUtils getRandom() {
		final ITopLevelAgent root = getRoot();
		if (root == null) return new RandomUtils();
		return root.getRandomGenerator();
	}

	@Override
	public IScope copy(final String additionalName) {
		return duplicateInto(new ExecutionScope(getRoot(), additionalName));
	}

	/**
	 * Copy as graphics.
	 *
	 * @param additionalName
	 *            the additional name
	 * @return the i graphics scope
	 */
	@Override
	public IGraphicsScope copyForGraphics(final String additionalName) {
		return (IGraphicsScope) duplicateInto(new GraphicsScope(this, additionalName));
	}

	/**
	 * Duplicate into.
	 *
	 * @param scope
	 *            the scope
	 * @return the i scope
	 */
	private IScope duplicateInto(final ExecutionScope scope) {
		scope.setFlowStatus(flowStatus.get());
		scope.setExecutionContext(executionContext == null ? null : executionContext.createCopy(null));
		scope.agentContext = agentContext == null ? null : agentContext.createCopy();
		// Fix for Issue #725. Different scopes sharing the same "each map" would pollute each other
		// each = specialContext.each == null ? null : new HashMap<>(specialContext.each);
		// data = scope.data;
		return scope;
	}

	@Override
	public IExecutionContext getExecutionContext() { return executionContext; }

	@Override
	public void setFlowStatus(final FlowStatus status) {
		flowStatus.set(status);
	}

	/**
	 * Gets the and clear flow status.
	 *
	 * @return the and clear flow status
	 */
	@Override
	public FlowStatus getAndClearFlowStatus(final FlowStatus expectedValue) {
		try {
			return flowStatus.get();
		} finally {
			flowStatus.compareAndSet(expectedValue, FlowStatus.NORMAL);
		}
	}

	/**
	 * Sets the execution context.
	 *
	 * @param executionContext
	 *            the new execution context
	 */
	protected void setExecutionContext(final IExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

}

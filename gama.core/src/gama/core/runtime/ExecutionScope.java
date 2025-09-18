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
import static gama.core.runtime.exceptions.GamaRuntimeException.create;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IStepable;
import gama.core.common.util.RandomUtils;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.IExperimentController;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.kernel.model.IModel;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.kernel.simulation.SimulationClock;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IObject;
import gama.core.metamodel.population.IPopulationFactory;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.benchmark.StopWatch;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.Collector;
import gama.core.util.IList;
import gama.dev.COUNTER;
import gama.dev.DEBUG;
import gama.gaml.compilation.ISymbol;
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

	/** The Constant ATTRIBUTES. */
	private static final String ATTRIBUTES = "%_attributes_%";

	/** The scope name. */
	private final String scopeName;

	/** The execution context. */
	protected IExecutionContext executionContext;

	/** The agent context. */
	protected AgentExecutionContext agentContext;

	/** The additional context. */
	protected final SpecialContext additionalContext = new SpecialContext();

	/** The errors disabled. */
	private volatile boolean _trace, _in_try_mode, _errors_disabled;

	/** The flow status. */
	private volatile FlowStatus flowStatus = FlowStatus.NORMAL;

	/** The current symbol. */
	// private ISymbol currentSymbol;

	/**
	 * The Class SpecialContext.
	 */
	static class SpecialContext {

		/** The data map. A structure that can store arbitrary data */
		Map<String, Object> data;

		/** The each. */
		Map<String, Object> each;

		/** The topology. */
		public ITopology topology;

		/** The root agent. */
		ITopLevelAgent rootAgent;

		/** The gui. */
		IGui gui;

		/** The types. */
		ITypesManager types;

		/** The current error. */
		GamaRuntimeException currentError;

		/**
		 * Clear.
		 */
		void clear() {
			each = null;
			data = null;
			topology = null;
			rootAgent = null;
			gui = null;
			types = null;
			currentError = null;
		}

		/**
		 * Copy from.
		 *
		 * @param specialContext
		 *            the special context
		 */
		public void copyFrom(final SpecialContext specialContext) {
			if (specialContext == null) return;
			// Addresses #725 by avoiding the pollution of a scope by another
			// each = specialContext.each;
			each = null;

			data = specialContext.data;
			topology = specialContext.topology;
			rootAgent = specialContext.rootAgent;
			gui = specialContext.gui;
			types = specialContext.types;
			currentError = specialContext.currentError;
		}

		/**
		 * Gets the data.
		 *
		 * @param key
		 *            the key
		 * @return the data
		 */
		Object getData(final String key) {
			return data == null ? null : data.get(key);
		}

		/**
		 * Sets the data.
		 *
		 * @param key
		 *            the key
		 * @param value
		 *            the value
		 */
		void setData(final String key, final Object value) {
			if (value == null) {
				if (data == null) return;
				data.remove(key);
			}
			if (data == null) { data = new HashMap<>(); }
			data.put(key, value);
		}

		/**
		 * Gets the each.
		 *
		 * @param name
		 *            the name
		 * @return the each
		 */
		Object getEach(final String name) {
			return each == null ? null : each.get(name);
		}

		/**
		 * Sets the each.
		 *
		 * @param key
		 *            the key
		 * @param value
		 *            the value
		 */
		void setEach(final String key, final Object value) {
			if (each == null) { each = new HashMap<>(); }
			each.put(key, value);
		}

	}

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
	 */
	public ExecutionScope(final ITopLevelAgent root, final String otherName) {
		this(root, otherName, null);
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
	 */
	public ExecutionScope(final ITopLevelAgent root, final String otherName, final IExecutionContext context) {
		this(root, otherName, context, null, null);
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
	public ExecutionScope(final ITopLevelAgent root, final String otherName, final IExecutionContext context,
			final AgentExecutionContext agentContext, final SpecialContext specialContext) {
		StringBuilder name = new StringBuilder("Scope #").append(COUNTER.COUNT());
		setRoot(root);
		if (root != null) { name.append(" of ").append(root.stringValue(root.getScope())); }
		name.append(otherName == null || otherName.isEmpty() ? "" : " (" + otherName + ")");
		this.scopeName = name.toString();
		this.setExecutionContext(context == null ? ExecutionContext.create(this, null) : context.createCopy(null));
		this.agentContext = agentContext == null ? AgentExecutionContext.create(root, null) : agentContext;
		this.additionalContext.copyFrom(specialContext);
	}

	/**
	 * Creates the child context.
	 *
	 * @param agent
	 *            the agent
	 * @return the agent execution context
	 */
	public AgentExecutionContext createChildContext(final IObject agent) {
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
		additionalContext.clear();
		// currentSymbol = null;
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
	 *
	 * Method interrupted(). Returns true if the scope is currently marked as interrupted.
	 *
	 * @see gama.core.runtime.IScope#interrupted()
	 */
	@Override
	public final boolean interrupted() {
		return INTERRUPTING_STATUSES.contains(flowStatus);

		/*
		 * _root_interrupted() ||
		 */
		/* _action_halted || _loop_halted || _agent_halted */
		// flowStatus == FlowStatus.RETURN || flowStatus == FlowStatus.BREAK || flowStatus == FlowStatus.CONTINUE
		// || flowStatus == FlowStatus.DEATH;
	}

	@Override
	public final boolean isClosed() { return flowStatus == FlowStatus.DISPOSE; }

	// @Override
	// public void setInterrupted() {
	// this._interrupted = true;
	// }

	/**
	 * @return true if the root agent of the scope is marked as interrupted (i.e. dead)
	 */

	// public boolean _root_interrupted() {
	// // return ROOT_INTERRUPTING
	//
	// return /* _interrupted */ flowStatus == FlowStatus.CLOSE /* || getRoot() == null || getRoot().dead(); */;
	// }

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

	// @Override
	// public final void interruptAction() {
	// _action_halted = true;
	// }

	/**
	 * Interrupt loop.
	 */
	// @Override
	// public final void interruptLoop() {
	// _loop_halted = true;
	// }
	//
	// @Override
	// public final void interruptAgent() {
	// _agent_halted = true;
	// }

	/**
	 * Method push()
	 *
	 * @see gama.core.runtime.IScope#push(gama.core.metamodel.agent.IAgent)
	 */
	// @Override

	private final Object lock = new Object();

	@Override
	public boolean push(final IObject agent) {
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
	protected void setRoot(final ITopLevelAgent agent) { additionalContext.rootAgent = agent; }

	/**
	 * Method pop()
	 *
	 * @see gama.core.runtime.IScope#pop(gama.core.metamodel.agent.IAgent)
	 */
	// @Override
	@Override
	public synchronized void pop(final IObject agent) {
		synchronized (lock) {
			if (agentContext == null) {
				DEBUG.OUT("Agents stack is empty");
				return;
			}
			final AgentExecutionContext previous = agentContext;
			agentContext = agentContext.getOuterContext();
			previous.dispose();
			getAndClearDeathStatus();
			// _agent_halted = false;
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
	 * Pop loop.
	 */
	// @Override
	// public void popLoop() {
	// // _loop_halted = false;
	// }

	// @Override
	// public void popAction() {
	// _action_halted = false;
	// }

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
		} catch (final Exception g) {
			GAMA.reportAndThrowIfNeeded(this, g instanceof GamaRuntimeException e ? e : create(g, this), true);
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

	@Override
	public ExecutionResult evaluate(final IExpression expr, final IObject object) throws GamaRuntimeException {
		if (object == null || interrupted()) return FAILED;
		final boolean pushed = push(object);
		try {
			try (StopWatch w = GAMA.benchmark(this, object)) {
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
			if (pushed) { pop(object); }
		}
	}

	/**
	 * Method getVarValue()
	 *
	 * @see gama.core.runtime.IScope#getVarValue(java.lang.String)
	 */
	@Override
	public Object getVarValue(final String varName) {
		if (executionContext != null) return executionContext.getTempVar(varName);
		return null;
	}

	/**
	 * Method setVarValue()
	 *
	 * @see gama.core.runtime.IScope#setVarValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setVarValue(final String varName, final Object val) {
		if (executionContext != null) { executionContext.setTempVar(varName, val); }
	}

	/**
	 * Method setVarValue()
	 *
	 * @see gama.core.runtime.IScope#setVarValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setVarValue(final String varName, final Object val, final boolean localScopeOnly) {
		if (executionContext != null) {
			if (localScopeOnly) {
				executionContext.putLocalVar(varName, val);
			} else {
				executionContext.setTempVar(varName, val);
			}
		}
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
	 * Method removeAllVars()
	 *
	 * @see gama.core.runtime.IScope#removeAllVars()
	 */
	@Override
	public void removeAllVars() {
		if (executionContext != null) { executionContext.clearLocalVars(); }
	}

	/**
	 * Method addVarWithValue()
	 *
	 * @see gama.core.runtime.IScope#addVarWithValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void addVarWithValue(final String varName, final Object val) {
		if (executionContext != null) { executionContext.putLocalVar(varName, val); }
	}

	/**
	 * Method setEach()
	 *
	 * @see gama.core.runtime.IScope#setEach(java.lang.Object)
	 */
	@Override
	public void setEach(final String name, final Object value) {
		additionalContext.setEach(name, value);
	}

	/**
	 * Method getEach()
	 *
	 * @see gama.core.runtime.IScope#getEach()
	 */
	@Override
	public Object getEach(final String name) {
		return additionalContext.getEach(name);
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

	@Override
	public final Integer getIntArg(final String name) throws GamaRuntimeException {
		return (Integer) getArg(name, IType.INT);
	}

	@Override
	public final Double getFloatArg(final String name) throws GamaRuntimeException {
		return (Double) getArg(name, IType.FLOAT);
	}

	@Override
	public final IList getListArg(final String name) throws GamaRuntimeException {
		return (IList) getArg(name, IType.LIST);
	}

	@Override
	public final Boolean getBoolArg(final String name) throws GamaRuntimeException {
		return (Boolean) getArg(name, IType.BOOL);
	}

	@Override
	public final String getStringArg(final String name) throws GamaRuntimeException {
		return (String) getArg(name, IType.STRING);
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
	 * Method getTopology()
	 *
	 * @see gama.core.runtime.IScope#getTopology()
	 */
	@Override
	public ITopology getTopology() {
		final ITopology topology = additionalContext.topology;
		if (topology != null) return topology;
		final IAgent a = getAgent();
		return a == null ? null : a.getTopology();
	}

	/**
	 * Method setTopology()
	 *
	 * @see gama.core.runtime.IScope#setTopology(gama.core.metamodel.topology.ITopology)
	 */
	@Override
	public ITopology setTopology(final ITopology topo) {
		final ITopology previous = getTopology();
		additionalContext.topology = topo;
		return previous;
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
		if (additionalContext.types == null) {
			additionalContext.types = getExperiment().getSpecies().getModel().getDescription().getTypesManager();
		}
		return additionalContext.types.get(name);
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
	 * Method pushReadAttributes()
	 *
	 * @see gama.core.runtime.IScope#pushReadAttributes(java.util.Map)
	 */
	@Override
	public void pushReadAttributes(final Map values) {
		addVarWithValue(ATTRIBUTES, values);
	}

	/**
	 * Method popReadAttributes()
	 *
	 * @see gama.core.runtime.IScope#popReadAttributes()
	 */
	@Override
	public Map popReadAttributes() {
		if (executionContext != null) {
			final Map value = (Map) this.getVarValue(ATTRIBUTES);
			executionContext.removeLocalVar(ATTRIBUTES);
			return value;
		}
		return Collections.EMPTY_MAP;
	}

	@Override
	public Map peekReadAttributes() {
		return (Map) this.getVarValue(ATTRIBUTES);
	}

	@Override
	public IGui getGui() {
		if (additionalContext.gui != null) return additionalContext.gui;
		final IExperimentAgent experiment = getExperiment();
		if (experiment == null) {
			additionalContext.gui = GAMA.getGui();
		} else if (experiment.getSpecies().isHeadless()) {
			additionalContext.gui = GAMA.getHeadlessGui();
		} else {
			additionalContext.gui = GAMA.getRegularGui();
		}
		return additionalContext.gui;
	}

	@Override
	public ITopLevelAgent getRoot() { return additionalContext.rootAgent; }

	@Override
	public boolean isPaused() {
		final IExperimentAgent exp = getExperiment();
		if (exp != null) {
			final IExperimentPlan plan = exp.getSpecies();
			if (plan != null) {
				final IExperimentController controller = plan.getController();
				if (controller != null) return controller.isPaused() || isOnUserHold();
			}
		}
		return isOnUserHold();
	}

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
		final ExecutionScope scope = new ExecutionScope(getRoot(), additionalName);
		scope.setExecutionContext(executionContext == null ? null : executionContext.createCopy(null));
		scope.agentContext = agentContext == null ? null : agentContext.createCopy();
		scope.additionalContext.copyFrom(additionalContext);
		return scope;
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
		final GraphicsScope scope = new GraphicsScope(this, additionalName);
		scope.setExecutionContext(executionContext == null ? null : executionContext.createCopy(null));
		scope.agentContext = agentContext == null ? null : agentContext.createCopy();
		scope.additionalContext.copyFrom(additionalContext);
		return scope;
	}

	@Override
	public IExecutionContext getExecutionContext() { return executionContext; }

	@Override
	public void setCurrentError(final GamaRuntimeException g) { additionalContext.currentError = g; }

	@Override
	public GamaRuntimeException getCurrentError() { return additionalContext.currentError; }

	@Override
	public void setFlowStatus(final FlowStatus status) { flowStatus = status; }

	/**
	 * Gets the and clear flow status.
	 *
	 * @return the and clear flow status
	 */
	@Override
	public FlowStatus getAndClearFlowStatus(final FlowStatus comparison) {
		try {
			return flowStatus;
		} finally {
			if (flowStatus == comparison) { flowStatus = FlowStatus.NORMAL; }
		}
	}

	/**
	 * Gets the data.
	 *
	 * @param key
	 *            the key
	 * @return the data
	 */
	@Override
	public Object getData(final String key) {
		return additionalContext.getData(key);
	}

	/**
	 * Sets the data.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	@Override
	public void setData(final String key, final Object value) {
		additionalContext.setData(key, value);
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

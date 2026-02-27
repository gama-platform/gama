/*******************************************************************************************************
 *
 * ExecutionScope.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

import static gama.api.exceptions.GamaRuntimeException.create;
import static gama.api.runtime.scope.IExecutionResult.FAILED;
import static gama.api.runtime.scope.IExecutionResult.PASSED;
import static gama.api.runtime.scope.IExecutionResult.withValue;

import java.util.Collections;
import java.util.Map;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulationFactory;
import gama.api.kernel.simulation.IClock;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentController;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.IExecutable;
import gama.api.runtime.IStepable;
import gama.api.types.list.IList;
import gama.api.types.topology.ITopology;
import gama.api.ui.IGui;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.utils.StringUtils;
import gama.api.utils.benchmark.StopWatch;
import gama.api.utils.collections.Collector;
import gama.api.utils.random.IRandom;
import gama.api.utils.random.RandomUtils;
import gama.api.utils.server.IServerConfiguration;
import gama.dev.COUNTER;
import gama.dev.DEBUG;

/**
 * Standard implementation of {@link IScope} providing the complete execution context for GAML code.
 *
 * <p>
 * ExecutionScope is the primary scope implementation in GAMA, managing the entire execution environment for statements,
 * expressions, and actions. It maintains multiple context stacks (agents, execution contexts, symbols) and provides
 * access to all runtime services needed during execution.
 * </p>
 *
 * <h2>Core Responsibilities</h2>
 * <ul>
 * <li><b>Context Management:</b> Maintains agent stack, execution context stack, and symbol stack</li>
 * <li><b>Variable Access:</b> Provides unified access to local, agent, and global variables</li>
 * <li><b>Execution Control:</b> Manages statement execution, expression evaluation, and flow control</li>
 * <li><b>Runtime Services:</b> Provides access to GUI, random generators, clock, topology, and other services</li>
 * <li><b>Error Handling:</b> Manages exception reporting, try-mode, and error state</li>
 * <li><b>Lifecycle Management:</b> Handles agent initialization, stepping, and disposal</li>
 * </ul>
 *
 * <h2>Internal Structure</h2>
 *
 * <p>
 * ExecutionScope maintains three key context structures:
 * </p>
 *
 * <pre>
 * ExecutionScope {
 *   - executionContext: IExecutionContext      // Variable scoping and local vars
 *   - agentContext: AgentExecutionContext      // Agent stack
 *   - additionalContext: SpecialContext        // Services and special data
 *   - flowStatus: FlowStatus                   // Execution flow control
 *   - scopeName: String                        // Scope identifier
 * }
 * </pre>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Creating a Scope</h3>
 *
 * <pre>{@code
 * // Create scope for an experiment agent
 * ITopLevelAgent experimentAgent = ...;
 * ExecutionScope scope = new ExecutionScope(experimentAgent);
 *
 * // Create scope with a custom name
 * ExecutionScope namedScope = new ExecutionScope(experimentAgent, "MyScope");
 *
 * // Scope is now ready for execution
 * IExecutionResult result = scope.execute(statement, agent, null);
 * }</pre>
 *
 * <h3>Executing Statements</h3>
 *
 * <pre>{@code
 * ExecutionScope scope = new ExecutionScope(root);
 * IAgent agent = scope.getSimulation().getAgent("agent_0");
 * IStatement statement = ...; // e.g., a reflex or action
 *
 * // Execute in agent's context
 * IExecutionResult result = scope.execute(statement, agent, null);
 *
 * if (result.passed()) {
 *     Object returnValue = result.getValue();
 *     // Process result...
 * }
 * }</pre>
 *
 * <h3>Managing Agent Context</h3>
 *
 * <pre>{@code
 * ExecutionScope scope = new ExecutionScope(root);
 *
 * // Push an agent onto the stack
 * boolean pushed = scope.push(agent);
 * try {
 * 	// Agent is now the current context
 * 	assert scope.getAgent() == agent;
 * 
 * 	// Access agent's variables
 * 	Object value = scope.getVarValue("my_attribute");
 * 
 * 	// Execute code in agent's context
 * 	scope.execute(statement);
 * } finally {
 * 	// Always pop to restore previous context
 * 	if (pushed) { scope.pop(agent); }
 * }
 * }</pre>
 *
 * <h3>Variable Access</h3>
 *
 * <pre>{@code
 * ExecutionScope scope = new ExecutionScope(root);
 * scope.push(agent);
 *
 * // Access agent attributes
 * String name = (String) scope.getAgentVarValue(agent, "name");
 *
 * // Access global variables
 * int worldSize = (Integer) scope.getGlobalVarValue("world_size");
 *
 * // Access local/temporary variables
 * Object localValue = scope.getVarValue("temp_var");
 *
 * // Set variables
 * scope.setVarValue("my_var", 42);
 * scope.setAgentVarValue(agent, "energy", 100.0);
 * scope.setGlobalVarValue("total_count", 1000);
 * }</pre>
 *
 * <h3>Flow Control</h3>
 *
 * <pre>{@code
 * ExecutionScope scope = new ExecutionScope(root);
 *
 * // Execute loop body
 * for (int i = 0; i < 10; i++) {
 * 	scope.execute(bodyStatement);
 * 
 * 	// Check for break
 * 	if (scope.getAndClearBreakStatus() != null) {
 * 		break; // Exit loop
 * 	}
 * 
 * 	// Check for continue
 * 	if (scope.getAndClearContinueStatus() != null) {
 * 		continue; // Skip to next iteration
 * 	}
 * 
 * 	// Check for return
 * 	if (scope.getAndClearReturnStatus() != null) { return scope.getVarValue("result"); }
 * }
 * }</pre>
 *
 * <h3>Error Handling</h3>
 *
 * <pre>{@code
 * ExecutionScope scope = new ExecutionScope(root);
 *
 * // Disable error reporting for experimental code
 * scope.disableErrorReporting();
 * try {
 * 	// Errors won't be reported to user
 * 	riskyOperation(scope);
 * } finally {
 * 	scope.enableErrorReporting();
 * }
 *
 * // Use try-mode for exception handling
 * scope.enableTryMode();
 * try {
 * 	scope.execute(statement);
 * } catch (GamaRuntimeException e) {
 * 	// Handle error
 * 	scope.setCurrentError(e);
 * } finally {
 * 	scope.disableTryMode();
 * }
 * }</pre>
 *
 * <h3>Accessing Runtime Services</h3>
 *
 * <pre>{@code
 * ExecutionScope scope = new ExecutionScope(root);
 *
 * // Access random generator
 * IRandom random = scope.getRandom();
 * double randomValue = random.next();
 *
 * // Access GUI
 * IGui gui = scope.getGui();
 * gui.inform("Simulation started");
 *
 * // Access clock
 * IClock clock = scope.getClock();
 * double currentTime = clock.getTime();
 *
 * // Access topology
 * ITopology topology = scope.getTopology();
 * List<IAgent> neighbors = topology.getNeighborsOf(agent, 10.0, scope);
 *
 * // Access types
 * IType intType = scope.getType("int");
 * }</pre>
 *
 * <h3>Copying Scopes</h3>
 *
 * <pre>{@code
 * ExecutionScope originalScope = new ExecutionScope(root);
 *
 * // Create a copy for nested execution
 * IScope copyScope = originalScope.copy("NestedExecution");
 *
 * // Create a graphics scope for display operations
 * IGraphicsScope graphicsScope = originalScope.copyForGraphics("Display");
 * graphicsScope.setGraphics(graphics);
 * }</pre>
 *
 * <h3>Agent Lifecycle</h3>
 *
 * <pre>{@code
 * ExecutionScope scope = new ExecutionScope(root);
 * IAgent newAgent = ...; // Newly created agent
 *
 * // Initialize the agent
 * IExecutionResult initResult = scope.init(newAgent);
 *
 * if (initResult.passed()) {
 *     // Step the agent each cycle
 *     IExecutionResult stepResult = scope.step(newAgent);
 *
 *     if (!stepResult.passed()) {
 *         // Handle step failure
 *     }
 * }
 * }</pre>
 *
 * <h2>Scope Lifecycle</h2>
 *
 * <p>
 * A typical scope lifecycle:
 * </p>
 *
 * <pre>
 * 1. Creation:     new ExecutionScope(root)
 * 2. Usage:        scope.execute(), scope.push(), scope.getVarValue(), etc.
 * 3. Disposal:     scope.clear() or scope.close()
 * 4. After clear:  scope.isClosed() returns true, scope is unusable
 * </pre>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>
 * ExecutionScope is NOT thread-safe. Each thread should have its own scope instance. The use of volatile fields for
 * flow control provides some thread visibility guarantees but does not make the class fully thread-safe.
 * </p>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 * <li><b>Context Stacks:</b> Push/pop operations are optimized but should still be minimized in tight loops</li>
 * <li><b>Variable Access:</b> Local variables are faster to access than agent attributes or global variables</li>
 * <li><b>Benchmarking:</b> Scope implements IBenchmarkable and tracks execution time when benchmarking is enabled</li>
 * <li><b>Object Pooling:</b> AgentExecutionContext uses object pooling to reduce allocation overhead</li>
 * </ul>
 *
 * <h2>Integration with GAMA Framework</h2>
 *
 * <p>
 * ExecutionScope is used throughout GAMA:
 * </p>
 * <ul>
 * <li>Agents maintain their own scope for execution</li>
 * <li>Statements receive a scope for execution</li>
 * <li>Expressions evaluate within a scope</li>
 * <li>Actions execute within a caller's scope</li>
 * <li>Displays use GraphicsScope (subclass) for rendering</li>
 * </ul>
 *
 * @see IScope
 * @see GraphicsScope
 * @see ExecutionContext
 * @see AgentExecutionContext
 * @see SpecialContext
 *
 * @author drogoul
 * @since 23 mai 2013
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
		this(root, otherName, null, null, null);
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
	public AgentExecutionContext createChildContext(final IAgent agent) {
		return AgentExecutionContext.create(agent, agentContext);
	}

	/**
	 * Method clear()
	 *
	 * @see gama.api.runtime.scope.IScope#clear()
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
	 * @see gama.api.runtime.scope.IScope#interrupted()
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
	 * @see gama.api.runtime.scope.IScope#push(gama.api.kernel.agent.IAgent)
	 */
	// @Override

	private final Object lock = new Object();

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
	protected void setRoot(final ITopLevelAgent agent) { additionalContext.rootAgent = agent; }

	/**
	 * Method pop()
	 *
	 * @see gama.api.runtime.scope.IScope#pop(gama.api.kernel.agent.IAgent)
	 */
	// @Override
	@Override
	public synchronized void pop(final IAgent agent) {
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
	 * @see gama.api.runtime.scope.IScope#push(gama.gaml.statements.IStatement)
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
		for (int i = 0; i < executionContext.depth(); i++) { sb.append(StringUtils.TAB); }
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
	 * @see gama.api.runtime.scope.IScope#pop(gama.gaml.statements.IStatement)
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
	 * @see gama.api.runtime.scope.IScope#execute(gama.gaml.statements.IStatement, gama.api.kernel.agent.IAgent)
	 */
	@Override
	public IExecutionResult execute(final IExecutable statement, final IAgent target,
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
			if (statement instanceof IStatement.Remote remote && "create".equals(remote.getDescription().getKeyword())
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
			return IExecutionResult.FAILED;
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
			actualArgs.forEachArgument((a, b) -> {
				final IExpression e = b.getExpression();
				if (e != null) { addVarWithValue(a, e.value(ExecutionScope.this)); }
				return true;
			});

		} finally {
			if (callerPushed) { pop(caller); }
		}
	}

	@Override
	public IExecutionResult step(final IStepable agent) {
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
	public IExecutionResult init(final IStepable agent) {
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
	public IExecutionResult step(final IAgent agent) {
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
	public IExecutionResult init(final IAgent agent) {
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
	public IExecutionResult evaluate(final IExpression expr, final IAgent agent) throws GamaRuntimeException {
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
	 * @see gama.api.runtime.scope.IScope#getVarValue(java.lang.String)
	 */
	@Override
	public Object getVarValue(final String varName) {
		if (executionContext != null) return executionContext.getTempVar(varName);
		return null;
	}

	/**
	 * Method setVarValue()
	 *
	 * @see gama.api.runtime.scope.IScope#setVarValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setVarValue(final String varName, final Object val) {
		if (executionContext != null) { executionContext.setTempVar(varName, val); }
	}

	/**
	 * Method setVarValue()
	 *
	 * @see gama.api.runtime.scope.IScope#setVarValue(java.lang.String, java.lang.Object)
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
	 * @see gama.api.runtime.scope.IScope#saveAllVarValuesIn(java.util.Map)
	 */
	@Override
	public void saveAllVarValuesIn(final Map<String, Object> varsToSave) {
		if (executionContext != null && varsToSave != null) { varsToSave.putAll(executionContext.getLocalVars()); }
	}

	/**
	 * Method removeAllVars()
	 *
	 * @see gama.api.runtime.scope.IScope#removeAllVars()
	 */
	@Override
	public void removeAllVars() {
		if (executionContext != null) { executionContext.clearLocalVars(); }
	}

	/**
	 * Method addVarWithValue()
	 *
	 * @see gama.api.runtime.scope.IScope#addVarWithValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void addVarWithValue(final String varName, final Object val) {
		if (executionContext != null) { executionContext.putLocalVar(varName, val); }
	}

	/**
	 * Method setEach()
	 *
	 * @see gama.api.runtime.scope.IScope#setEach(java.lang.Object)
	 */
	@Override
	public void setEach(final String name, final Object value) {
		additionalContext.setEach(name, value);
	}

	/**
	 * Method getEach()
	 *
	 * @see gama.api.runtime.scope.IScope#getEach()
	 */
	@Override
	public Object getEach(final String name) {
		return additionalContext.getEach(name);
	}

	/**
	 * Method getArg()
	 *
	 * @see gama.api.runtime.scope.IScope#getArg(java.lang.String, int)
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
	 * @see gama.api.runtime.scope.IScope#hasArg(java.lang.String)
	 */
	@Override
	public boolean hasArg(final String name) {
		if (executionContext != null) return executionContext.hasLocalVar(name);
		return false;
	}

	/**
	 * Method getAgentVarValue()
	 *
	 * @see gama.api.runtime.scope.IScope#getAgentVarValue(gama.api.kernel.agent.IAgent, java.lang.String)
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
	 * @see gama.api.runtime.scope.IScope#setAgentVarValue(gama.api.kernel.agent.IAgent, java.lang.String,
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
	public IExecutionResult update(final IAgent a) {
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
	 * @see gama.api.runtime.scope.IScope#getGlobalVarValue(java.lang.String)
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
	 * @see gama.api.runtime.scope.IScope#setGlobalVarValue(java.lang.String, java.lang.Object)
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
	 * @see gama.api.runtime.scope.IScope#getName()
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
	 * @see gama.api.runtime.scope.IScope#getTopology()
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
	 * @see gama.api.runtime.scope.IScope#setTopology(gama.api.types.topology.ITopology)
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
	 * @see gama.api.runtime.scope.IScope#getAgent()
	 */
	@Override
	public IAgent getAgent() {
		if (agentContext == null) return null;
		return agentContext.getAgent();
	}

	/**
	 * Method getSimulationScope()
	 *
	 * @see gama.api.runtime.scope.IScope#getSimulation()
	 */
	@Override
	public ISimulationAgent getSimulation() {
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
	 * @see gama.api.runtime.scope.IScope#getModel()
	 */
	@Override
	public IModelSpecies getModel() {
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
	 * @see gama.api.runtime.scope.IScope#getClock()
	 */
	@Override
	public IClock getClock() {
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
	 * @see gama.api.runtime.scope.IScope#pushReadAttributes(java.util.Map)
	 */
	@Override
	public void pushReadAttributes(final Map values) {
		addVarWithValue(ATTRIBUTES, values);
	}

	/**
	 * Method popReadAttributes()
	 *
	 * @see gama.api.runtime.scope.IScope#popReadAttributes()
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
			final IExperimentSpecies plan = exp.getSpecies();
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
	 * @see gama.api.runtime.scope.IScope#getRandom()
	 */
	@Override
	public IRandom getRandom() {
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

	@Override
	public IServerConfiguration getServerConfiguration() {
		IExperimentAgent agent = getExperiment();
		if (agent == null) return IServerConfiguration.NULL;
		return agent.getScope().getServerConfiguration();
	}

	@Override
	public ITypesManager getTypes() { return Types.findTypesManager(this); }

}

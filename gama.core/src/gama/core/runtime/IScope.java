/*******************************************************************************************************
 *
 * IScope.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime;

import java.io.Closeable;
import java.util.EnumSet;
import java.util.Map;

import gama.core.common.interfaces.IBenchmarkable;
import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IKeyword;
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
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.runtime.server.GamaServerExperimentConfiguration;
import gama.core.util.IList;
import gama.gaml.compilation.ISymbol;
import gama.gaml.expressions.IExpression;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.IExecutable;
import gama.gaml.types.IType;
import gama.gaml.types.ITypesManager;
import gama.gaml.types.Types;

// TODO: Auto-generated Javadoc
/**
 * The IScope interface represents the execution context in the GAMA modeling and simulation platform.
 * It provides a complete environment for the execution of statements, expressions, and actions by agents
 * within a simulation. An IScope instance maintains the state of execution, manages access to agents and variables,
 * handles control flow, and provides utilities for simulation operations.
 *
 * <p>A scope is hierarchical in nature and typically associated with an agent. It maintains various stacks
 * (agents, symbols, attributes) that define the execution context. Scopes can be copied to create nested contexts
 * while preserving the parent context.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Execution state management: Controls the pause, interruption, tracing, and error reporting states</li>
 *   <li>Agent context: Maintains the current agent stack and provides access to the agent hierarchy</li>
 *   <li>Variable access: Provides a mechanism to access and modify agent, local, and global variables</li>
 *   <li>Execution flow: Manages execution of statements and evaluation of expressions</li>
 *   <li>Flow control: Handles special flow control statuses like BREAK, CONTINUE, RETURN, DIE</li>
 *   <li>Runtime utilities: Provides access to random generators, GUI, clock, and other platform services</li>
 * </ul>
 *
 * <p>IScope is a central element in GAMA's architecture, acting as the bridge between the model specification
 * (in GAML) and its execution in the platform. Every statement execution, expression evaluation, or agent action
 * occurs within a scope that provides the complete context needed for execution.</p>
 */
@SuppressWarnings({ "rawtypes" })
public interface IScope extends Closeable, IBenchmarkable {

    /**
     * Predefined set of flow statuses that interrupt the normal sequential execution flow.
     * These statuses signal special execution paths like loop breaks, returns from actions,
     * or agent termination.
     */
    EnumSet<FlowStatus> INTERRUPTING_STATUSES =
            EnumSet.of(FlowStatus.BREAK, FlowStatus.RETURN, FlowStatus.CONTINUE, FlowStatus.DIE, FlowStatus.DISPOSE);

    /**
     * The IGraphicsScope interface extends IScope for contexts related to graphical display.
     * It provides additional capabilities for working with graphical representations of the simulation.
     */
    public interface IGraphicsScope extends IScope {
        /**
         * Creates a copy of this graphical scope with an additional name identifier.
         * @param additionalName Name to append to the current scope name
         * @return A new IGraphicsScope instance that shares context with this scope
         */
        @Override
        IGraphicsScope copy(String additionalName);

        /**
         * Sets the graphics context for rendering in this scope.
         * @param val The graphics object to use for rendering
         */
        void setGraphics(IGraphics val);

        /**
         * Returns the graphics context associated with this scope.
         * @return The graphics object used for rendering
         */
        IGraphics getGraphics();

        /**
         * Indicates that this scope supports graphical operations.
         * @return Always returns true for IGraphicsScope implementations
         */
        @Override
        default boolean isGraphics() { return true; }
    }

    /**
     * Removes all contextual information from the scope, effectively resetting its state.
     * This includes clearing agent stacks, variables, symbols, and other temporary data.
     */
    void clear();

    /**
     * Closes this scope, releasing any resources it holds. The default implementation
     * simply calls {@link #clear()}.
     */
    @Override
    default void close() {
        clear();
    }

    /**
     * Suspends execution when user input or interaction is required.
     * @param b true to put the scope on hold, false to release it
     */
    void setOnUserHold(boolean b);

    /**
     * Checks if the scope is waiting for user input or interaction.
     * @return true if the scope is on hold for user input, false otherwise
     */
    boolean isOnUserHold();

    /**
     * Checks if execution in this scope is paused.
     * @return true if execution is paused, false otherwise
     */
    boolean isPaused();

    /**
     * Disables error reporting during execution, causing exceptions to be suppressed.
     * This is useful for operations that may generate controlled or expected errors.
     */
    void disableErrorReporting();

    /**
     * Enables error reporting during execution, allowing exceptions to be raised and handled.
     * This is the default behavior for normal execution.
     */
    void enableErrorReporting();

    /**
     * Checks if error reporting is currently enabled for this scope.
     * @return true if errors will be reported, false if they will be suppressed
     */
    boolean reportErrors();

    /**
     * Enables or disables execution tracing. When tracing is enabled, the scope
     * will output information about each execution step for debugging purposes.
     * @param trace true to enable tracing, false to disable it
     */
    void setTrace(boolean trace);

    /**
     * Returns the name of this scope, which is typically used for identification
     * and debugging purposes.
     * @return The scope's name
     */
    String getName();

    /**
     * Returns the name to use for benchmarking purposes. The default implementation
     * returns the same value as {@link #getName()}.
     * @return The name to use in benchmark reports
     */
    @Override
    default String getNameForBenchmarks() { return getName(); }

    /**
     * Creates a new scope that is a copy of this one, with an additional name component.
     * The copy shares the same context but can have its own local variables and state.
     * @param additionalName Name to append to the current scope name
     * @return A new IScope instance that shares context with this scope
     */
    IScope copy(String additionalName);

    /**
     * Creates a new graphics-enabled scope that is a copy of this one, with an additional name.
     * The resulting scope supports graphical operations through the IGraphicsScope interface.
     * @param additionalName Name to append to the current scope name
     * @return A new IGraphicsScope instance that shares context with this scope
     */
    IGraphicsScope copyForGraphics(String additionalName);

    /**
     * Checks if execution in this scope has been interrupted.
     * @return true if execution has been interrupted, false otherwise
     */
    boolean interrupted();

    /**
     * Pushes a symbol onto the execution context stack, creating a new execution context.
     * Symbols represent GAML elements like statements, variables, or outputs.
     * @param symbol The symbol to push onto the context stack
     */
    void push(ISymbol symbol);

    /**
     * Removes a symbol from the execution context stack, reverting to the previous context.
     * Should be called after execution of the symbol's code is complete.
     * @param symbol The symbol to remove from the context stack
     */
    void pop(ISymbol symbol);

    /**
     * Sets the current executing symbol without pushing it onto the stack.
     * This is used for temporary symbol changes that don't require a new context.
     * @param symbol The symbol to set as the current executing symbol
     */
    void setCurrentSymbol(ISymbol symbol);

    /**
     * Returns the symbol that is currently being executed in this scope.
     * @return The current executing symbol, or null if no symbol is executing
     */
    ISymbol getCurrentSymbol();

    /**
     * Pushes a map of attributes onto the attribute stack. This is typically used
     * when reading data from external sources like files or databases.
     * @param values The map of attribute names to values
     */
    void pushReadAttributes(Map values);

    /**
     * Retrieves and removes the top map of attributes from the attribute stack.
     * @return The map that was at the top of the stack
     */
    Map popReadAttributes();

    /**
     * Retrieves the top map of attributes from the stack without removing it.
     * @return The map at the top of the stack
     */
    Map peekReadAttributes();

    /**
     * Sets the value of a temporary variable used within iterators, such as the 'each' variable.
     * These variables provide access to the current element in an iteration.
     * @param name The name of the temporary variable
     * @param value The value to assign to the variable
     */
    void setEach(String name, Object value);

    /**
     * Returns the value of a temporary variable used within iterators.
     * @param each The name of the temporary variable
     * @return The current value of the temporary variable
     */
    Object getEach(String each);

    /**
     * Returns the root or top-level agent in the agent hierarchy for this scope.
     * This is typically the experiment agent that manages the simulation.
     * @return The top-level agent
     */
    ITopLevelAgent getRoot();

    /**
     * Returns the simulation agent associated with this scope.
     * The simulation agent manages the world and all the agents within it.
     * @return The simulation agent
     */
    SimulationAgent getSimulation();

    /**
     * Returns the experiment agent associated with this scope.
     * The experiment agent manages the simulation and provides access to experiment parameters.
     * @return The experiment agent
     */
    IExperimentAgent getExperiment();

    /**
     * Removes an agent from the agent stack, reverting to the previous agent context.
     * Should be called after execution in the agent's context is complete.
     * @param iAgent The agent to remove from the stack
     */
    void pop(IAgent iAgent);

    /**
     * Pushes an agent onto the agent stack, making it the current agent context.
     * Subsequent operations will be executed in the context of this agent.
     * @param iAgent The agent to push onto the stack
     * @return true if the agent was successfully pushed, false otherwise
     */
    boolean push(IAgent iAgent);

    /**
     * Returns the agent that is currently at the top of the agent stack,
     * representing the immediate execution context.
     * @return The current agent
     */
    IAgent getAgent();

    /**
     * Returns a copy of the entire agent stack, representing the chain of agent contexts.
     * The first element is the root agent, and the last element is the current agent.
     * @return Array of agents in the stack, from root to current
     */
    IAgent[] getAgentsStack();

    /**
     * Returns the random number generator associated with this scope.
     * This generator is used for all stochastic operations within the scope.
     * @return The random number generator
     */
    RandomUtils getRandom();

    /**
     * Returns the GUI interface associated with this scope.
     * This provides access to user interface components and user interaction.
     * @return The GUI interface
     */
    IGui getGui();

    /**
     * Returns the simulation clock associated with this scope.
     * The clock manages time progression and scheduling in the simulation.
     * @return The simulation clock
     */
    SimulationClock getClock();

    /**
     * Returns the current topology associated with this scope.
     * The topology defines the spatial relationships between agents.
     * @return The current topology
     */
    ITopology getTopology();

    /**
     * Sets the current topology for this scope.
     * @param topology The topology to set
     * @return The topology that was set
     */
    ITopology setTopology(ITopology topology);

    /**
     * Executes a statement in the context of the current agent.
     * @param executable The statement to execute
     * @return The result of the execution
     */
    default ExecutionResult execute(final IExecutable executable) {
        return execute(executable, getAgent(), null);
    }

    /**
     * Executes a statement in the context of the current agent with the specified arguments.
     * @param executable The statement to execute
     * @param args The arguments to pass to the statement
     * @return The result of the execution
     */
    default ExecutionResult execute(final IExecutable executable, final Arguments args) {
        return execute(executable, getAgent(), args);
    }

    /**
     * Executes a statement in the context of the specified agent with the specified arguments.
     * @param executable The statement to execute
     * @param agent The agent in whose context to execute the statement
     * @param args The arguments to pass to the statement
     * @return The result of the execution
     */
    default ExecutionResult execute(final IExecutable executable, final IAgent agent, final Arguments args) {
        return execute(executable, agent, false, args);
    }

    /**
     * Evaluates an expression in the context of the specified agent.
     * @param expr The expression to evaluate
     * @param agent The agent in whose context to evaluate the expression
     * @return The result of the evaluation
     * @throws GamaRuntimeException If an error occurs during evaluation
     */
    ExecutionResult evaluate(IExpression expr, IAgent agent) throws GamaRuntimeException;

    /**
     * Returns the value of a variable in the current execution context.
     * This may be a local variable, an agent attribute, or a global variable.
     * @param varName The name of the variable
     * @return The value of the variable, or null if it doesn't exist
     */
    Object getVarValue(String varName);

    /**
     * Returns the value of a variable of a specific agent.
     * @param agent The agent whose variable to access
     * @param name The name of the variable
     * @return The value of the agent's variable
     * @throws GamaRuntimeException If the variable doesn't exist or cannot be accessed
     */
    Object getAgentVarValue(IAgent agent, String name) throws GamaRuntimeException;

    /**
     * Sets the value of a variable of a specific agent.
     * @param agent The agent whose variable to modify
     * @param name The name of the variable
     * @param v The new value for the variable
     * @throws GamaRuntimeException If the variable doesn't exist or cannot be modified
     */
    void setAgentVarValue(IAgent agent, String name, Object v) throws GamaRuntimeException;

    /**
     * Returns the value of a global variable defined at the simulation level.
     * @param name The name of the global variable
     * @return The value of the global variable
     * @throws GamaRuntimeException If the variable doesn't exist or cannot be accessed
     */
    Object getGlobalVarValue(String name) throws GamaRuntimeException;

    /**
     * Checks if this scope has access to a global variable with the specified name.
     * @param name The name of the global variable
     * @return true if the scope has access to the variable, false otherwise
     */
    boolean hasAccessToGlobalVar(String name);

    /**
     * Sets the value of a global variable defined at the simulation level.
     * @param name The name of the global variable
     * @param v The new value for the variable
     * @throws GamaRuntimeException If the variable doesn't exist or cannot be modified
     */
    void setGlobalVarValue(String name, Object v) throws GamaRuntimeException;

    /**
     * Sets the value of a variable in the current execution context.
     * This may be a local variable, an agent attribute, or a global variable.
     * By default, variables defined in outer scopes can be modified.
     * @param varName The name of the variable
     * @param val The new value for the variable
     */
    default void setVarValue(final String varName, final Object val) {
        setVarValue(varName, val, false);
    }

    /**
     * Sets the value of a variable in the current execution context with control over scope.
     * @param varName The name of the variable
     * @param val The new value for the variable
     * @param localScopeOnly If true, only variables defined in this scope will be modified;
     *                       if false, variables in outer scopes can also be modified
     */
    void setVarValue(String varName, Object val, boolean localScopeOnly);

    /**
     * Saves all variable values from this scope into the provided map.
     * This is typically used to capture the state of the scope for later restoration.
     * @param varsToSave The map in which to save the variable values
     */
    void saveAllVarValuesIn(Map<String, Object> varsToSave);

    /**
     * Removes all variables defined in this scope.
     * This effectively clears the local variable context.
     */
    void removeAllVars();

    /**
     * Adds a new variable with the specified value to this scope.
     * @param varName The name of the variable to add
     * @param val The value to assign to the variable
     */
    void addVarWithValue(String varName, Object val);

    /**
     * Returns the value of an argument, casting it to the specified type.
     * Arguments are typically passed to actions or statements during execution.
     * @param string The name of the argument
     * @param type The GAML type ID to cast the value to
     * @return The value of the argument cast to the specified type
     * @throws GamaRuntimeException If the argument doesn't exist or cannot be cast
     */
    Object getArg(String string, int type) throws GamaRuntimeException;

    /**
     * Returns the value of an argument if it exists, or null if it doesn't.
     * @param string The name of the argument
     * @param type The GAML type ID to cast the value to
     * @return The value of the argument cast to the specified type, or null
     */
    default Object getArgIfExists(final String string, final int type) {
        if (hasArg(string)) return getArg(string, type);
        return null;
    }

    /**
     * Returns the value of an argument cast to the specified Java type.
     * @param <T> The Java type to cast the value to
     * @param string The name of the argument
     * @param type The GAML type ID to cast the value to
     * @return The value of the argument cast to the specified Java type
     */
    @SuppressWarnings("unchecked")
    default <T> T getTypedArg(final String string, final int type) {
        return (T) getArg(string, type);
    }

    /**
     * Returns the value of an argument cast to the specified Java type if it exists,
     * or null if it doesn't.
     * @param <T> The Java type to cast the value to
     * @param string The name of the argument
     * @param type The GAML type ID to cast the value to
     * @return The value of the argument cast to the specified Java type, or null
     */
    default <T> T getTypedArgIfExists(final String string, final int type) {
        return getTypedArgIfExists(string, type, null);
    }

    /**
     * Returns the value of an argument cast to the specified Java type if it exists,
     * or a default value if it doesn't.
     * @param <T> The Java type to cast the value to
     * @param string The name of the argument
     * @param type The GAML type ID to cast the value to
     * @param defaultValue The value to return if the argument doesn't exist
     * @return The value of the argument cast to the specified Java type, or the default value
     */
    @SuppressWarnings("unchecked")
    default <T> T getTypedArgIfExists(final String string, final int type, final T defaultValue) {
        if (hasArg(string)) return (T) getArg(string, type);
        return defaultValue;
    }

    /**
     * Returns the value of an argument as an Integer.
     * @param string The name of the argument
     * @return The value of the argument as an Integer
     * @throws GamaRuntimeException If the argument doesn't exist or cannot be cast
     */
    Integer getIntArg(String string) throws GamaRuntimeException;

    /**
     * Returns the value of an argument as a Double.
     * @param string The name of the argument
     * @return The value of the argument as a Double
     * @throws GamaRuntimeException If the argument doesn't exist or cannot be cast
     */
    Double getFloatArg(String string) throws GamaRuntimeException;

    /**
     * Returns the value of an argument as a list of the specified type.
     * @param <T> The Java type of the list elements
     * @param string The name of the argument
     * @return The value of the argument as a list
     * @throws GamaRuntimeException If the argument doesn't exist or cannot be cast
     */
    <T> IList<T> getListArg(String string) throws GamaRuntimeException;

    /**
     * Returns the value of an argument as a String.
     * @param string The name of the argument
     * @return The value of the argument as a String
     * @throws GamaRuntimeException If the argument doesn't exist or cannot be cast
     */
    String getStringArg(String string) throws GamaRuntimeException;

    /**
     * Returns the value of an argument as a Boolean.
     * @param string The name of the argument
     * @return The value of the argument as a Boolean
     * @throws GamaRuntimeException If the argument doesn't exist or cannot be cast
     */
    Boolean getBoolArg(String string) throws GamaRuntimeException;

    /**
     * Returns the value of an argument as a boolean if it exists,
     * or a default value if it doesn't.
     * @param string The name of the argument
     * @param ifNotExist The value to return if the argument doesn't exist
     * @return The value of the argument as a boolean, or the default value
     */
    default boolean getBoolArgIfExists(final String string, final boolean ifNotExist) {
        if (hasArg(string)) return getBoolArg(string);
        return ifNotExist;
    }

    /**
     * Checks if an argument with the specified name exists.
     * @param string The name of the argument
     * @return true if the argument exists, false otherwise
     */
    boolean hasArg(String string);

    /**
     * Returns the GAML type with the specified name.
     * @param name The name of the type
     * @return The GAML type, or null if no type with that name exists
     */
    IType getType(final String name);

    /**
     * Returns the model associated with this scope.
     * The model represents the entire GAML model being executed.
     * @return The model
     */
    IModel getModel();

    /**
     * Sets the current flow status for this scope.
     * The flow status controls how execution proceeds, including special
     * flow control operations like breaks, returns, and agent death.
     * @param status The flow status to set
     */
    void setFlowStatus(FlowStatus status);

    /**
     * Initializes an agent that implements the IStepable interface.
     * This is typically called when the agent is created.
     * @param agent The agent to initialize
     * @return The result of the initialization
     */
    ExecutionResult init(final IStepable agent);

    /**
     * Executes one simulation step for an agent that implements the IStepable interface.
     * @param agent The agent to step
     * @return The result of the step execution
     */
    ExecutionResult step(final IStepable agent);

    /**
     * Initializes an agent. This is typically called when the agent is created.
     * @param agent The agent to initialize
     * @return The result of the initialization
     */
    ExecutionResult init(final IAgent agent);

    /**
     * Executes one simulation step for an agent.
     * @param agent The agent to step
     * @return The result of the step execution
     */
    ExecutionResult step(final IAgent agent);

    /**
     * Pushes a set of arguments onto the argument stack.
     * These arguments can later be accessed using the getArg methods.
     * @param actualArgs The arguments to push
     */
    void stackArguments(Arguments actualArgs);

    /**
     * Updates an agent. This is typically called during the simulation update phase.
     * @param agent The agent to update
     * @return The result of the update
     */
    ExecutionResult update(IAgent agent);

    /**
     * Returns the current execution context, which contains information about
     * the execution environment, including variables and their values.
     * @return The execution context
     */
    IExecutionContext getExecutionContext();

    /**
     * Checks if this scope is in try mode, which affects how errors are handled.
     * @return true if the scope is in try mode, false otherwise
     */
    boolean isInTryMode();

    /**
     * Enables try mode for this scope, changing how errors are handled.
     * In try mode, errors may be caught and handled rather than propagating.
     */
    void enableTryMode();

    /**
     * Disables try mode for this scope, reverting to normal error handling.
     */
    void disableTryMode();

    /**
     * Sets the current error that occurred during execution in this scope.
     * @param g The error that occurred
     */
    void setCurrentError(GamaRuntimeException g);

    /**
     * Returns the current error that occurred during execution in this scope.
     * @return The current error, or null if no error has occurred
     */
    GamaRuntimeException getCurrentError();

    /**
     * Checks if this scope supports graphical operations.
     * @return true if the scope is graphical, false otherwise
     */
    default boolean isGraphics() { return false; }

    /**
     * Executes a statement on an agent with control over scope usage and arguments.
     * This is the most general form of execution, used by other execute methods.
     * @param statement The statement to execute
     * @param target The agent in whose context to execute the statement
     * @param useTargetScopeForExecution If true, use the target agent's scope;
     *                                  if false, use this scope
     * @param args The arguments to pass to the statement
     * @return The result of the execution
     */
    ExecutionResult execute(IExecutable statement, IAgent target, boolean useTargetScopeForExecution, Arguments args);

    /**
     * Retrieves and clears the BREAK flow status.
     * @return The BREAK flow status if it was set, null otherwise
     */
    default FlowStatus getAndClearBreakStatus() { return getAndClearFlowStatus(FlowStatus.BREAK); }

    /**
     * Retrieves and clears the CONTINUE flow status.
     * @return The CONTINUE flow status if it was set, null otherwise
     */
    default FlowStatus getAndClearContinueStatus() { return getAndClearFlowStatus(FlowStatus.CONTINUE); }

    /**
     * Retrieves and clears the RETURN flow status.
     * @return The RETURN flow status if it was set, null otherwise
     */
    default FlowStatus getAndClearReturnStatus() { return getAndClearFlowStatus(FlowStatus.RETURN); }

    /**
     * Retrieves and clears the DIE flow status.
     * @return The DIE flow status if it was set, null otherwise
     */
    default FlowStatus getAndClearDeathStatus() { return getAndClearFlowStatus(FlowStatus.DIE); }

    /**
     * Retrieves and clears a specific flow status.
     * @param comparison The flow status to check for and clear
     * @return The flow status if it was set, null otherwise
     */
    FlowStatus getAndClearFlowStatus(final FlowStatus comparison);

    /**
     * Sets the flow status to BREAK, which signals to break out of a loop.
     */
    default void setBreakStatus() {
        setFlowStatus(FlowStatus.BREAK);
    }

    /**
     * Sets the flow status to CONTINUE, which signals to skip to the next
     * iteration of a loop.
     */
    default void setContinueStatus() {
        setFlowStatus(FlowStatus.CONTINUE);
    }

    /**
     * Sets the flow status to RETURN, which signals to return from an action.
     */
    default void setReturnStatus() {
        setFlowStatus(FlowStatus.RETURN);
    }

    /**
     * Sets the flow status to DIE, which signals that the agent should die.
     */
    default void setDeathStatus() {
        setFlowStatus(FlowStatus.DIE);
    }

    /**
     * Sets the flow status to DISPOSE, which signals that resources should be released.
     */
    default void setDisposeStatus() {
        setFlowStatus(FlowStatus.DISPOSE);
    }

    /**
     * Checks if this scope is closed and no longer usable.
     * @return true if the scope is closed, false otherwise
     */
    boolean isClosed();

    /**
     * Returns a piece of data associated with this scope by key.
     * The default implementation returns null.
     * @param key The key for the data
     * @return The data associated with the key, or null if none exists
     */
    default Object getData(final String key) {
        return null;
    }

    /**
     * Associates a piece of data with this scope by key.
     * The default implementation does nothing.
     * @param key The key for the data
     * @param value The data to associate with the key
     */
    default void setData(final String key, final Object value) {}

    /**
     * Returns the population factory associated with this scope.
     * The population factory is used to create new agents and populations.
     * @return The population factory
     */
    IPopulationFactory getPopulationFactory();

    /**
     * Returns the server configuration associated with this scope.
     * The default implementation delegates to the experiment scope.
     * @return The server configuration
     */
    default GamaServerExperimentConfiguration getServerConfiguration() {
        IExperimentAgent agent = getExperiment();
        if (agent == null) return GamaServerExperimentConfiguration.NULL;
        return agent.getScope().getServerConfiguration();
    }

    /**
     * Sets the server configuration associated with this scope.
     * The default implementation delegates to the experiment scope.
     * @param config The server configuration to set
     */
    default void setServerConfiguration(final GamaServerExperimentConfiguration config) {
        IExperimentAgent agent = getExperiment();
        if (agent == null) return;
        agent.getScope().setServerConfiguration(config);
    }

    /**
     * Returns the types manager associated with this scope.
     * The types manager provides access to GAML types defined in the model.
     * @return The types manager, or the built-in types if no model is available
     */
    default ITypesManager getTypes() {
        IModel m = getModel();
        if (m == null) return Types.builtInTypes;
        return m.getDescription().getTypesManager();
    }
}

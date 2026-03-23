/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * Provides runtime scope management for GAML execution in the GAMA platform.
 * 
 * <p>
 * This package contains interfaces and implementations for managing execution scopes, which provide the complete
 * runtime context for evaluating expressions and executing statements in GAML models. Scopes are central to GAMA's
 * execution model, acting as the bridge between model specification and runtime execution.
 * </p>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Main Interfaces</h3>
 * <ul>
 * <li>{@link gama.api.runtime.scope.IScope} - The main scope interface providing comprehensive runtime context,
 * including agent stacks, variable access, flow control, and execution utilities</li>
 * <li>{@link gama.api.runtime.scope.IExecutionContext} - Manages hierarchical execution contexts for variable scoping
 * and local/temporary variable storage</li>
 * <li>{@link gama.api.runtime.scope.IExecutionResult} - Represents the result of statement or expression execution,
 * encapsulating success/failure status and return values</li>
 * <li>{@link gama.api.runtime.scope.InScope} - Functional interface for operations that execute within a scope</li>
 * <li>{@link gama.api.runtime.scope.IScoped} - Marker interface for objects that have an associated scope</li>
 * <li>{@link gama.api.runtime.scope.IScopedStepable} - Interface for stepable objects that manage their own scope</li>
 * </ul>
 * 
 * <h3>Implementations</h3>
 * <ul>
 * <li>{@link gama.api.runtime.scope.ExecutionScope} - Standard implementation of IScope for general execution</li>
 * <li>{@link gama.api.runtime.scope.GraphicsScope} - Specialized scope for graphical operations with display support</li>
 * <li>{@link gama.api.runtime.scope.ExecutionContext} - Implementation of IExecutionContext for hierarchical variable
 * management</li>
 * <li>{@link gama.api.runtime.scope.AgentExecutionContext} - Manages the agent context stack during execution</li>
 * </ul>
 * 
 * <h3>Supporting Classes</h3>
 * <ul>
 * <li>{@link gama.api.runtime.scope.FlowStatus} - Enumeration of execution flow control states (BREAK, CONTINUE,
 * RETURN, DIE, DISPOSE, NORMAL)</li>
 * <li>{@link gama.api.runtime.scope.SpecialContext} - Internal context storage for topology, GUI, types, and other
 * special scope data</li>
 * </ul>
 * 
 * <h2>Scope Functionality</h2>
 * 
 * <p>
 * Scopes provide comprehensive access to the execution environment:
 * </p>
 * 
 * <h3>Agent Management</h3>
 * <ul>
 * <li><strong>Agent Stack:</strong> Hierarchical stack of agents representing nested execution contexts</li>
 * <li><strong>Current Agent:</strong> The agent in whose context code is currently executing</li>
 * <li><strong>Simulation:</strong> Access to the simulation instance managing the world</li>
 * <li><strong>Experiment:</strong> Access to the experiment agent controlling the simulation</li>
 * <li><strong>Root Agent:</strong> Access to the top-level agent in the hierarchy</li>
 * </ul>
 * 
 * <h3>Variable Management</h3>
 * <ul>
 * <li><strong>Local Variables:</strong> Variables defined in the current execution context (e.g., loop variables,
 * action parameters)</li>
 * <li><strong>Temporary Variables:</strong> Variables accessible recursively through the context chain</li>
 * <li><strong>Agent Attributes:</strong> Access to agent-specific variables and attributes</li>
 * <li><strong>Global Variables:</strong> Access to simulation-level global variables</li>
 * <li><strong>Each Variables:</strong> Special iterator variables for accessing current elements in loops</li>
 * </ul>
 * 
 * <h3>Execution Control</h3>
 * <ul>
 * <li><strong>Flow Status:</strong> Control execution flow with BREAK, CONTINUE, RETURN, DIE, and DISPOSE statuses</li>
 * <li><strong>Interruption Management:</strong> Detection and handling of execution interruptions</li>
 * <li><strong>Error Handling:</strong> Exception management with try-mode support and error reporting control</li>
 * <li><strong>Tracing:</strong> Debug tracing of execution steps</li>
 * </ul>
 * 
 * <h3>Runtime Services</h3>
 * <ul>
 * <li><strong>Random Generator:</strong> Access to deterministic random number generation</li>
 * <li><strong>Clock:</strong> Simulation time and scheduling services</li>
 * <li><strong>GUI:</strong> User interface and display services</li>
 * <li><strong>Topology:</strong> Spatial topology and agent positioning</li>
 * <li><strong>Types Manager:</strong> Access to GAML type system</li>
 * <li><strong>Population Factory:</strong> Agent creation services</li>
 * </ul>
 * 
 * <h2>Scope Hierarchy and Context</h2>
 * 
 * <p>
 * Scopes maintain hierarchical contexts through multiple stacks:
 * </p>
 * 
 * <pre>
 * Execution Context Chain:
 * Experiment Context (depth 0)
 *   └─> Simulation Context (depth 1)
 *       └─> Agent Action Context (depth 2)
 *           └─> Loop Statement Context (depth 3)
 * 
 * Agent Context Chain:
 * Experiment Agent
 *   └─> Simulation Agent
 *       └─> Regular Agent
 *           └─> Nested Agent
 * </pre>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Variable Access</h3>
 * <pre>{@code
 * public Object execute(IScope scope) {
 *     // Access current agent
 *     IAgent agent = scope.getAgent();
 *     
 *     // Access simulation
 *     ISimulationAgent simulation = scope.getSimulation();
 *     
 *     // Get and set variable values
 *     Object value = scope.getVarValue("my_variable");
 *     scope.setVarValue("result", computedValue);
 *     
 *     return value;
 * }
 * }</pre>
 * 
 * <h3>Execution with Context Management</h3>
 * <pre>{@code
 * public void executeOnAgent(IScope scope, IAgent target, IExecutable statement) {
 *     // Push agent to establish execution context
 *     boolean pushed = scope.push(target);
 *     try {
 *         // Execute statement in target agent's context
 *         IExecutionResult result = scope.execute(statement, target, null);
 *         if (result.passed()) {
 *             Object returnValue = result.getValue();
 *             // Process result...
 *         }
 *     } finally {
 *         // Always pop the agent to restore previous context
 *         if (pushed) {
 *             scope.pop(target);
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h3>Flow Control</h3>
 * <pre>{@code
 * public Object loopIteration(IScope scope) {
 *     // Execute loop body
 *     scope.execute(statement);
 *     
 *     // Check for flow control
 *     if (scope.getAndClearBreakStatus() != null) {
 *         // Break out of loop
 *         return null;
 *     }
 *     if (scope.getAndClearContinueStatus() != null) {
 *         // Skip to next iteration
 *         return null;
 *     }
 *     
 *     return result;
 * }
 * }</pre>
 * 
 * <h3>Error Handling</h3>
 * <pre>{@code
 * public Object safeExecute(IScope scope, IExpression expression) {
 *     // Disable error reporting for experimental evaluation
 *     scope.disableErrorReporting();
 *     try {
 *         return expression.value(scope);
 *     } catch (Exception e) {
 *         return null; // Errors suppressed
 *     } finally {
 *         scope.enableErrorReporting();
 *     }
 * }
 * }</pre>
 * 
 * <h3>Using Execution Context</h3>
 * <pre>{@code
 * public void executeWithLocalVars(IScope scope) {
 *     IExecutionContext context = scope.getExecutionContext();
 *     
 *     // Set local variables (only in this context)
 *     context.putLocalVar("i", 0);
 *     context.putLocalVar("sum", 0.0);
 *     
 *     // Set temporary variables (accessible to child contexts)
 *     context.setTempVar("result", computedValue);
 *     
 *     // Access variables
 *     Integer i = (Integer) context.getLocalVar("i");
 *     Object result = context.getTempVar("result");
 * }
 * }</pre>
 * 
 * <h3>Graphics Scope</h3>
 * <pre>{@code
 * public void draw(IScope scope) {
 *     // Create graphics scope for display operations
 *     IGraphicsScope graphicsScope = scope.copyForGraphics("display");
 *     
 *     // Access graphics context
 *     IGraphics graphics = graphicsScope.getGraphics();
 *     
 *     // Use graphics-specific random generator
 *     IRandom random = graphicsScope.getRandom();
 *     
 *     // Perform drawing operations...
 * }
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 * <li><strong>Always pop what you push:</strong> Use try-finally blocks to ensure agents and symbols are properly
 * popped from stacks</li>
 * <li><strong>Check interruption status:</strong> Before executing operations, check if the scope is interrupted</li>
 * <li><strong>Use appropriate scope types:</strong> Use GraphicsScope for display operations, ExecutionScope for
 * general execution</li>
 * <li><strong>Respect variable scope:</strong> Use local variables for temporary data, temporary variables for data
 * shared with child contexts</li>
 * <li><strong>Handle flow control:</strong> Check and clear flow statuses (BREAK, CONTINUE, RETURN) appropriately</li>
 * <li><strong>Manage errors properly:</strong> Use try-mode and error reporting flags appropriately for different
 * execution scenarios</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>
 * Scopes are generally not thread-safe and should be used within a single thread. Each thread executing GAML code
 * should have its own scope instance. The framework manages scope lifecycle and ensures proper isolation between
 * concurrent executions.
 * </p>
 * 
 * @see gama.api.runtime.scope.IScope
 * @see gama.api.runtime.scope.ExecutionScope
 * @see gama.api.runtime.scope.IScope
 * @see gama.api.runtime.scope.ExecutionScope
 * @see gama.api.runtime.scope.IExecutionContext
 */
package gama.api.runtime.scope;

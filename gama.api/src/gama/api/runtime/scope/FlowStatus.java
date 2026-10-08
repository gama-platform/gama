/*******************************************************************************************************
 *
 * FlowStatus.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

/**
 * Enumeration of execution flow control statuses in GAMA.
 * 
 * <p>
 * FlowStatus represents special execution states that interrupt or modify the normal sequential flow of statement
 * execution. These statuses are used to implement control flow constructs like loops, returns, and agent lifecycle
 * management.
 * </p>
 * 
 * <p>
 * Flow statuses are managed by the {@link IScope} and checked during execution to determine whether to continue normal
 * sequential execution or take special action. Most flow statuses (BREAK, CONTINUE, RETURN, DIE, DISPOSE) cause
 * execution to be interrupted, while NORMAL indicates standard sequential execution.
 * </p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // In a loop statement
 * for (Object element : collection) {
 *     scope.execute(bodyStatement);
 *     
 *     // Check for break
 *     if (scope.getAndClearBreakStatus() != null) {
 *         break; // Exit loop
 *     }
 *     
 *     // Check for continue
 *     if (scope.getAndClearContinueStatus() != null) {
 *         continue; // Skip to next iteration
 *     }
 * }
 * 
 * // In an action
 * public Object executeAction(IScope scope) {
 *     scope.execute(statement);
 *     
 *     // Check for return
 *     if (scope.getAndClearReturnStatus() != null) {
 *         return scope.getVarValue("result");
 *     }
 *     
 *     return null;
 * }
 * }</pre>
 * 
 * @see IScope#setFlowStatus(FlowStatus)
 * @see IScope#getAndClearFlowStatus(FlowStatus)
 * @see IScope#INTERRUPTING_STATUSES
 */
public enum FlowStatus {

	/**
	 * Break status - signals to exit from a loop early.
	 * 
	 * <p>
	 * When a BREAK status is set (typically by executing a "break" statement in GAML), the scope signals that the
	 * current loop should be exited immediately, skipping any remaining iterations.
	 * </p>
	 * 
	 * <p>
	 * Usage in GAML:
	 * </p>
	 * 
	 * <pre>
	 * loop i from: 0 to: 10 {
	 *     if (i = 5) {
	 *         break;  // Sets FlowStatus.BREAK
	 *     }
	 * }
	 * </pre>
	 */
	BREAK,

	/**
	 * Return status - signals to return from an action or statement block.
	 * 
	 * <p>
	 * When a RETURN status is set (typically by executing a "return" statement in GAML), the scope signals that
	 * execution should exit from the current action, method, or reflex, returning control to the caller. The return
	 * value is typically stored in a scope variable.
	 * </p>
	 * 
	 * <p>
	 * Usage in GAML:
	 * </p>
	 * 
	 * <pre>
	 * action computeValue {
	 *     if (condition) {
	 *         return 42;  // Sets FlowStatus.RETURN
	 *     }
	 *     return 0;
	 * }
	 * </pre>
	 */
	RETURN,

	/**
	 * Continue status - signals to skip to the next iteration of a loop.
	 * 
	 * <p>
	 * When a CONTINUE status is set (typically by executing a "continue" statement in GAML), the scope signals that
	 * the current loop iteration should be terminated early, and execution should proceed to the next iteration.
	 * </p>
	 * 
	 * <p>
	 * Usage in GAML:
	 * </p>
	 * 
	 * <pre>
	 * loop agent over: agents {
	 *     if (not agent.active) {
	 *         continue;  // Sets FlowStatus.CONTINUE
	 *     }
	 *     // Process active agent
	 * }
	 * </pre>
	 */
	CONTINUE,

	/**
	 * Die status - signals that the agent running in the scope is dead.
	 * 
	 * <p>
	 * When a DIE status is set (typically by executing a "die" or "do die" statement in GAML), the scope signals that
	 * the current agent should be terminated. This interrupts all ongoing execution for that agent and triggers
	 * cleanup operations.
	 * </p>
	 * 
	 * <p>
	 * Usage in GAML:
	 * </p>
	 * 
	 * <pre>
	 * reflex check_health {
	 *     if (health <= 0) {
	 *         do die;  // Sets FlowStatus.DIE
	 *     }
	 * }
	 * </pre>
	 */
	DIE,

	/**
	 * Dispose status - signals that simulations or experiments are closing.
	 * 
	 * <p>
	 * When a DISPOSE status is set, the scope signals that the simulation or experiment is being shut down. This
	 * triggers cleanup of resources and termination of all ongoing operations. The scope becomes closed and unusable
	 * after this status is set.
	 * </p>
	 * 
	 * <p>
	 * This status is typically set internally by the GAMA framework during shutdown operations and is not directly
	 * triggered by GAML code.
	 * </p>
	 */
	DISPOSE,

	/**
	 * Normal status - indicates standard sequential execution.
	 * 
	 * <p>
	 * NORMAL is the default flow status and indicates that execution should proceed normally without any special flow
	 * control. When the flow status is NORMAL, statements execute sequentially without interruption.
	 * </p>
	 */
	NORMAL;
}
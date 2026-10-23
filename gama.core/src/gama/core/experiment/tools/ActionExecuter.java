/*******************************************************************************************************
 *
 * ActionExecuter.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.experiment.tools;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;

/**
 * Holds and executes the actions posted to a top-level agent (simulation or experiment):
 * <ul>
 * <li>end actions, executed at the end of every step of the agent, until it is disposed of;</li>
 * <li>one-shot actions, executed once, at the end of the next step of the agent;</li>
 * <li>dispose actions, executed once, when the agent is disposed of.</li>
 * </ul>
 * Actions can be posted from any thread.
 */
public class ActionExecuter {

	/** The actions executed at the end of every step. */
	private final Collection<IExecutable> endActions = new CopyOnWriteArrayList<>();

	/** The actions executed when the agent is disposed of. */
	private final Collection<IExecutable> disposeActions = new CopyOnWriteArrayList<>();

	/** The actions executed once, at the end of the next step. */
	private final Queue<IExecutable> oneShotActions = new ConcurrentLinkedQueue<>();

	/** The scope. */
	protected final IScope scope;

	/**
	 * Instantiates a new action executer.
	 *
	 * @param scope
	 *            the scope
	 */
	public ActionExecuter(final IScope scope) {
		this.scope = scope.copy("of ActionExecuter");
	}

	/**
	 * Inserts an action to execute when the agent is disposed of.
	 *
	 * @param action
	 *            the action
	 * @return the action
	 */
	public IExecutable insertDisposeAction(final IExecutable action) {
		disposeActions.add(action);
		return action;
	}

	/**
	 * Inserts an action to execute at the end of every step.
	 *
	 * @param action
	 *            the action
	 * @return the action
	 */
	public IExecutable insertEndAction(final IExecutable action) {
		endActions.add(action);
		return action;
	}

	/**
	 * Inserts an action to execute once, at the end of the next step.
	 *
	 * @param action
	 *            the action
	 * @return the action
	 */
	public IExecutable insertOneShotAction(final IExecutable action) {
		oneShotActions.add(action);
		return action;
	}

	/**
	 * Executes the end actions.
	 */
	public void executeEndActions() {
		if (scope.interrupted()) return;
		executeActions(endActions);
	}

	/**
	 * Executes the dispose actions.
	 */
	public void executeDisposeActions() {
		executeActions(disposeActions);
	}

	/**
	 * Executes the one-shot actions posted so far, and removes them. Actions posted while executing them (including by
	 * themselves) are kept for the next time.
	 */
	public void executeOneShotActions() {
		for (int n = oneShotActions.size(); n > 0 && !scope.interrupted(); n--) {
			final IExecutable action = oneShotActions.poll();
			if (action == null) { break; }
			action.executeOn(scope);
		}
	}

	/**
	 * Executes the actions.
	 *
	 * @param actions
	 *            the actions
	 */
	private void executeActions(final Collection<IExecutable> actions) {
		for (final IExecutable action : actions) { if (!scope.interrupted()) { action.executeOn(scope); } }
	}

	/**
	 * Executes an action immediately, in the current thread. The caller is responsible for making sure that the agent
	 * is not stepping meanwhile.
	 *
	 * @param action
	 *            the action
	 */
	public void executeNow(final IExecutable action) {
		action.executeOn(scope);
	}

	/**
	 * Executes an action immediately if the agent is paused, otherwise at the end of its next step. As the agent may be
	 * resumed (or paused) meanwhile, this does not guarantee that the action is not executed while it steps: prefer
	 * running the action in the thread of the agent, or excluding its step.
	 *
	 * @param action
	 *            the action
	 */
	public void executeOneAction(final IExecutable action) {
		if (scope.isPaused()) {
			action.executeOn(scope);
		} else {
			insertOneShotAction(action);
		}
	}

}

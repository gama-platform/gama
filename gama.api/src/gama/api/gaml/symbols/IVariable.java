/*******************************************************************************************************
 *
 * IVariable.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.symbols;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import gama.api.additions.IGamaHelper;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * The interface for GAML variable symbols, representing attributes and state of agents.
 * 
 * <p>
 * Variables in GAML represent the attributes, properties, and state of agents. They can be simple variables, parameters
 * (modifiable from experiments), functions (computed values), or micro-populations (collections of agents). Variables
 * extend both {@link ISymbol} (compilation structure) and {@link IParameter} (runtime behavior).
 * </p>
 * 
 * <h2>Variable Types</h2>
 * <ul>
 * <li><strong>Regular variables</strong> - Standard attributes with init/update expressions</li>
 * <li><strong>Parameters</strong> - Variables that can be set from experiment interfaces</li>
 * <li><strong>Functions</strong> - Read-only computed values (no storage)</li>
 * <li><strong>Micro-populations</strong> - Collections of child agents within a host agent</li>
 * </ul>
 * 
 * <h2>Listener System</h2>
 * <p>
 * Variables support a listener mechanism for observing value changes. Listeners can be registered globally by class or
 * by variable name, allowing plugins and extensions to react to state changes. The listener maps are thread-safe using
 * {@link ConcurrentHashMap}.
 * </p>
 * 
 * <h2>Update Semantics</h2>
 * <ul>
 * <li><strong>Updatable</strong> - Variables with an 'update' facet are automatically recomputed each cycle</li>
 * <li><strong>Not modifiable</strong> - Variables declared with 'const' cannot be changed after initialization</li>
 * <li><strong>Notification</strong> - Changes trigger listener callbacks and on_change actions</li>
 * </ul>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see ISymbol
 * @see IParameter
 */
public interface IVariable extends ISymbol, IParameter {

	/**
	 * The Constant LISTENERS_BY_CLASS. Thread-safe multimap using ConcurrentHashMap with concurrent sets. Maps classes
	 * to their associated helpers with lock-free concurrent access.
	 */
	Map<Class, Set<IGamaHelper>> LISTENERS_BY_CLASS = new ConcurrentHashMap<>();

	/**
	 * The Constant LISTENERS_BY_NAME. Thread-safe multimap using ConcurrentHashMap with concurrent sets. Maps listener
	 * names to classes with efficient concurrent operations.
	 */
	Map<String, Set<Class>> LISTENERS_BY_NAME = new ConcurrentHashMap<>();

	/**
	 * Gets all listeners for a class. Returns an empty set if none exist.
	 *
	 * @param key
	 *            the class key
	 * @return the set of helpers, never null
	 */
	static Set<IGamaHelper> getListenersByClass(final Class key) {
		return LISTENERS_BY_CLASS.getOrDefault(key, Collections.emptySet());
	}

	/**
	 * Gets all classes for a listener name. Returns an empty set if none exist.
	 *
	 * @param key
	 *            the listener name
	 * @return the set of classes, never null
	 */
	static Set<Class> getListenersByName(final String key) {
		return LISTENERS_BY_NAME.getOrDefault(key, Collections.emptySet());
	}

	/**
	 * Adds a listener to the LISTENERS_BY_CLASS multimap in a thread-safe manner. Creates the set if it doesn't exist
	 * using computeIfAbsent.
	 *
	 * @param key
	 *            the class key
	 * @param value
	 *            the helper to add
	 * @return true if the listener was added, false if it was already present
	 */
	static boolean addListenerByClass(final Class key, final IGamaHelper value) {
		return LISTENERS_BY_CLASS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
	}

	/**
	 * Adds a class to the LISTENERS_BY_NAME multimap in a thread-safe manner. Creates the set if it doesn't exist using
	 * computeIfAbsent.
	 *
	 * @param key
	 *            the listener name
	 * @param value
	 *            the class to add
	 * @return true if the class was added, false if it was already present
	 */
	static boolean addListenerByName(final String key, final Class value) {
		return LISTENERS_BY_NAME.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
	}

	/**
	 * Checks if this variable is updatable (has an 'update' facet).
	 * 
	 * <p>
	 * Updatable variables are automatically recomputed each simulation cycle by evaluating their update expression.
	 * This is typically used for variables whose values depend on changing conditions.
	 * </p>
	 *
	 * @return true if the variable has an update expression, false otherwise
	 */
	boolean isUpdatable();

	/**
	 * Checks if this variable is a parameter (can be modified from experiment UI).
	 * 
	 * <p>
	 * Parameters are special variables that can be set and modified from the experiment interface, allowing users to
	 * explore different model configurations without changing the code.
	 * </p>
	 *
	 * @return true if this is a parameter, false otherwise
	 */
	boolean isParameter();

	/**
	 * Checks if this variable is a function (computed value with no storage).
	 * 
	 * <p>
	 * Function variables don't store values; they compute and return a value each time they're accessed. They are
	 * declared using the 'function' keyword instead of 'var'.
	 * </p>
	 *
	 * @return true if this is a function, false otherwise
	 */
	boolean isFunction();

	/**
	 * Checks if this variable represents a micro-population.
	 * 
	 * <p>
	 * Micro-populations are collections of agents contained within a host agent. For example, a city agent might have a
	 * micro-population of building agents. They are typically declared implicitly by species containment.
	 * </p>
	 *
	 * @return true if this is a micro-population, false otherwise
	 */
	boolean isMicroPopulation();

	/**
	 * Initializes this variable for a specific agent with a given value.
	 * 
	 * <p>
	 * This method is called during agent creation to set the initial value of the variable. The value is typically the
	 * result of evaluating the 'init' facet expression, but can also come from parameter settings or default values.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param gamaObject
	 *            the agent being initialized
	 * @param object
	 *            the initial value to set
	 * @throws GamaRuntimeException
	 *             if initialization fails
	 */
	void initializeWith(IScope scope, IAgent gamaObject, Object object) throws GamaRuntimeException;

	/**
	 * Notifies that the value of this variable has changed externally.
	 * 
	 * <p>
	 * This method should be called when the variable's value is changed outside the normal assignment mechanism. For
	 * instance, if {@code agent.setLocation(...)} is invoked directly, this method ensures that listeners are notified
	 * and the 'on_change' facet action is executed.
	 * </p>
	 * 
	 * <p>
	 * Variables that use this notification mechanism automatically block internal notifications to avoid double
	 * notifications (one from the direct manipulation and one from the variable assignment).
	 * </p>
	 * 
	 * <p>
	 * This is particularly important for variables like location, shape, and name that can be modified by both model
	 * code and internal engine operations. Plugins may have additional variables requiring this mechanism.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param agent
	 *            the agent whose variable changed
	 * @param oldValue
	 *            the previous value (currently not used but reserved for future use)
	 * @param newValue
	 *            the new value after the change
	 */
	void notifyOfValueChange(final IScope scope, final IAgent agent, final Object oldValue, final Object newValue);

	/**
	 * Sets the value of this variable for a specific agent.
	 * 
	 * <p>
	 * This is the primary method for changing variable values. It handles type checking, validation, notification of
	 * listeners, and execution of on_change actions.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param agent
	 *            the agent whose variable to set
	 * @param v
	 *            the new value
	 * @throws GamaRuntimeException
	 *             if the assignment fails (e.g., type mismatch, not modifiable)
	 */
	void setVal(IScope scope, IAgent agent, Object v) throws GamaRuntimeException;

	/**
	 * Gets the current value of this variable for a specific agent.
	 * 
	 * <p>
	 * For regular variables, this returns the stored value. For functions, this evaluates the function expression. For
	 * updatable variables, this may trigger an update if needed.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param agent
	 *            the agent whose variable to read
	 * @return the current value
	 * @throws GamaRuntimeException
	 *             if reading the value fails
	 */
	Object value(IScope scope, IAgent agent) throws GamaRuntimeException;

	/**
	 * Gets the updated value of this variable in the current scope.
	 * 
	 * <p>
	 * For updatable variables, this evaluates the update expression. This is called during the update phase of the
	 * simulation cycle.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the updated value
	 */
	Object getUpdatedValue(final IScope scope);

	/**
	 * Checks if this variable is not modifiable (declared as 'const' or read-only).
	 * 
	 * <p>
	 * Non-modifiable variables can only be set during initialization and cannot be changed afterwards. Attempts to
	 * modify them will result in an error.
	 * </p>
	 *
	 * @return true if the variable cannot be modified, false otherwise
	 */
	boolean isNotModifiable();

}
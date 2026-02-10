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
 * @author drogoul
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
	 * Checks if is updatable.
	 *
	 * @return true, if is updatable
	 */
	boolean isUpdatable();

	/**
	 * Checks if is parameter.
	 *
	 * @return true, if is parameter
	 */
	boolean isParameter();

	/**
	 * Checks if is function.
	 *
	 * @return true, if is function
	 */
	boolean isFunction();

	/**
	 * Checks if is micro population.
	 *
	 * @return true, if is micro population
	 */
	boolean isMicroPopulation();

	/**
	 * Initialize with.
	 *
	 * @param scope
	 *            the scope
	 * @param gamaObject
	 *            the gama object
	 * @param object
	 *            the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void initializeWith(IScope scope, IAgent gamaObject, Object object) throws GamaRuntimeException;

	/**
	 * Can be called on this variable to indicate that the value it represents has been changed outside. For instance,
	 * if agent.setLocation(...) has been invoked, the value of location will change, but no listeners (see
	 * GamaAnnotations.listener.class) will be notified and the action attached to the on_change: facet will not be run
	 * as well. In the core, this represents a small set of variables (location, shape, name...) that can be modified
	 * outside of the models. Plugins may have more variables, although they are expected to produce listeners instead
	 * (e.g. listening to the changes of the location of an agent can be important for some). As soon as a variable is
	 * asked to produce notifications this way, it automatically blocks internal notifications (so as to avoid double
	 * notifications, one from the agent whose location is manipulated and one from the variable itself if it is
	 * modified in a model).
	 *
	 * @param scope
	 *            the current scope
	 * @param agent
	 *            the agent concerned by this change
	 * @param oldValue
	 *            previous value of the variable. Not used for the moment
	 * @param newValue
	 *            new value, once it has been set
	 */
	void notifyOfValueChange(final IScope scope, final IAgent agent, final Object oldValue, final Object newValue);

	/**
	 * Sets the val.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param v
	 *            the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void setVal(IScope scope, IAgent agent, Object v) throws GamaRuntimeException;

	/**
	 * Value.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	Object value(IScope scope, IAgent agent) throws GamaRuntimeException;

	/**
	 * Gets the updated value.
	 *
	 * @param scope
	 *            the scope
	 * @return the updated value
	 */
	Object getUpdatedValue(final IScope scope);

	/**
	 * Checks if is not modifiable.
	 *
	 * @return true, if is not modifiable
	 */
	boolean isNotModifiable();

}
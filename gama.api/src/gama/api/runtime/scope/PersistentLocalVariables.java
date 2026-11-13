/*******************************************************************************************************
 *
 * PersistentLocalVariables.java, in gama.api, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

import java.util.HashMap;
import java.util.Map;

import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;

/** Shared lexical storage carried by scope data, independently of transient execution frames and remote targets. */
public final class PersistentLocalVariables {

	private static final String KEY = PersistentLocalVariables.class.getName();

	private final IDescription owner;
	private final PersistentLocalVariables outer;
	private final Map<String, Object> values = new HashMap<>();

	private PersistentLocalVariables(final IDescription owner, final PersistentLocalVariables outer) {
		this.owner = owner;
		this.outer = outer;
	}

	public static PersistentLocalVariables current(final IScope scope) {
		return scope.getData(KEY) instanceof PersistentLocalVariables variables ? variables : null;
	}

	public static void install(final IScope scope, final PersistentLocalVariables variables) {
		scope.setData(KEY, variables);
	}

	public static void push(final IScope scope, final IDescription owner) {
		install(scope, new PersistentLocalVariables(owner, current(scope)));
	}

	private static PersistentLocalVariables find(final IScope scope, final IDescription owner) {
		for (PersistentLocalVariables variables = current(scope); variables != null; variables = variables.outer) {
			if (variables.owner == owner) return variables;
		}
		throw GamaRuntimeException.error("The local variables of " + owner.getKeyword() + " " + owner.getName()
				+ " are not available in this scope.", scope);
	}

	public static boolean isInitialized(final IScope scope, final IDescription owner, final String name) {
		final PersistentLocalVariables variables = find(scope, owner);
		synchronized (variables.values) {
			return variables.values.containsKey(name);
		}
	}

	public static Object get(final IScope scope, final IDescription owner, final String name) {
		final PersistentLocalVariables variables = find(scope, owner);
		synchronized (variables.values) {
			if (!variables.values.containsKey(name))
				throw GamaRuntimeException.error("Output/display local variable '" + name + "' is not initialized.", scope);
			return variables.values.get(name);
		}
	}

	public static void set(final IScope scope, final IDescription owner, final String name, final Object value,
			final boolean create) {
		final PersistentLocalVariables variables = find(scope, owner);
		synchronized (variables.values) {
			if (!create && !variables.values.containsKey(name))
				throw GamaRuntimeException.error("Output/display local variable '" + name + "' is not initialized.", scope);
			variables.values.put(name, value);
		}
	}
}

/*******************************************************************************************************
 *
 * KeyboardEventLayerDelegate.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.util.Objects;
import java.util.Set;

import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.constants.IKeyword;
import gama.api.gaml.statements.IStatement;
import gama.api.runtime.scope.IScope;

/**
 * The Class MouseEventLayerDelegate.
 */
public class KeyboardEventLayerDelegate implements IEventLayerDelegate {

	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		return Objects.equals(source, IKeyword.DEFAULT);
	}

	@Override
	public boolean createFrom(final IScope scope, final Object source, final IStatement.Event statement) {
		return true;
	}

	@Override
	public Set<String> getEvents() { return KEYBOARD_EVENTS; }

}
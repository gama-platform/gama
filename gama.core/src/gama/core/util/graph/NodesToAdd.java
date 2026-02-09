/*******************************************************************************************************
 *
 * NodesToAdd.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.graph;

import gama.api.data.objects.IContainer;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.collections.GraphObjectToAdd;
import gama.core.util.list.GamaList;

/**
 * The Class NodesToAdd.
 */
public class NodesToAdd extends GamaList<GraphObjectToAdd> implements GraphObjectToAdd {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new nodes to add.
	 */
	public NodesToAdd() {
		super(0, Types.NO_TYPE);
	}

	/**
	 * From.
	 *
	 * @param scope
	 *            the scope
	 * @param object
	 *            the object
	 * @return the nodes to add
	 */
	public static NodesToAdd from(final IScope scope, final IContainer object) {
		final NodesToAdd n = new NodesToAdd();
		for (final Object o : object.iterable(scope)) { n.add((GraphObjectToAdd) o); }
		return n;
	}

	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	@Override
	public Object getObject() { return this; }

}
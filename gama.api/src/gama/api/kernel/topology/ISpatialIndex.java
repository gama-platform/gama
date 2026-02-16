/*******************************************************************************************************
 *
 * ISpatialIndex.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.topology;

import java.util.Collection;
import java.util.Collections;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.interfaces.IAgentFilter;

/**
 * Written by drogoul Modified on 23 f�vr. 2011
 *
 * @todo Description
 *
 */
public interface ISpatialIndex {

	/** The null index. */
	ISpatialIndex NULL_INDEX = new ISpatialIndex() {};

	/**
	 * Insert.
	 *
	 * @param agent
	 *            the agent
	 */
	default void insert(final IAgent agent) {}

	/**
	 * Removes the.
	 *
	 * @param previous
	 *            the previous
	 * @param agent
	 *            the agent
	 */
	default void remove(final IEnvelope previous, final IAgent agent) {}

	/**
	 * First at distance.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param dist
	 *            the dist
	 * @param f
	 *            the f
	 * @return the i agent
	 */
	default IAgent firstAtDistance(final IScope scope, final IShape source, final double dist, final IAgentFilter f) {
		return null;
	}

	/**
	 * First at distance.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param dist
	 *            the dist
	 * @param f
	 *            the f
	 * @param number
	 *            the number
	 * @param alreadyChosen
	 *            the already chosen
	 * @return the collection
	 */
	default Collection<IAgent> firstAtDistance(final IScope scope, final IShape source, final double dist,
			final IAgentFilter f, final int number, final Collection<IAgent> alreadyChosen) {
		return Collections.EMPTY_LIST;
	}

	/**
	 * All in envelope.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param envelope
	 *            the envelope
	 * @param f
	 *            the f
	 * @param contained
	 *            the contained
	 * @return the collection
	 */
	default Collection<IAgent> allInEnvelope(final IScope scope, final IShape source, final IEnvelope envelope,
			final IAgentFilter f, final boolean contained) {
		return Collections.EMPTY_LIST;
	}

	/**
	 * All at distance.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param dist
	 *            the dist
	 * @param f
	 *            the f
	 * @return the collection
	 */
	default Collection<IAgent> allAtDistance(final IScope scope, final IShape source, final double dist,
			final IAgentFilter f) {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Dispose.
	 */
	default void dispose() {}

	/**
	 * The Interface Compound.
	 */
	public interface Compound extends ISpatialIndex {

		/**
		 * Removes the.
		 *
		 * @param species
		 *            the species
		 */
		void remove(final ISpecies species);

		/**
		 * Update.
		 *
		 * @param scope
		 *            the scope
		 * @param envelope
		 *            the envelope
		 * @param parallel
		 *            the parallel
		 */
		void update(IScope scope, IEnvelope envelope, boolean parallel);

		/**
		 * Merge with.
		 *
		 * @param spatialIndex
		 *            the spatial index
		 */
		void mergeWith(Compound spatialIndex);

	}

}
/*******************************************************************************************************
 *
 * RootTopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.topology.continuous;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.topology.ISpatialIndex;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.topology.ITopology;
import gama.api.utils.geometry.IEnvelope;
import gama.core.topology.CompoundSpatialIndex;

/**
 * The Class RootTopology.
 */
public class RootTopology extends ContinuousTopology {

	/**
	 * Instantiates a new root topology.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param isTorus
	 *            the is torus
	 * @param hasParallelism
	 *            the has parallelism
	 */
	public RootTopology(final IScope scope, final IShape geom, final boolean isTorus, final boolean hasParallelism) {
		super(scope, geom);
		final IEnvelope bounds = geom.getEnvelope();
		spatialIndex = new CompoundSpatialIndex(bounds, hasParallelism);
		this.isTorus = isTorus;
		root = this;
	}

	/** The spatial index. */
	private final ISpatialIndex.Compound spatialIndex;

	/** The is torus. */
	private final boolean isTorus;

	@Override
	public ISpatialIndex getSpatialIndex() { return spatialIndex; }

	/**
	 * Update environment.
	 *
	 * @param scope
	 *            the scope
	 * @param newEnv
	 *            the new env
	 * @param hasParallelism
	 *            the has parallelism
	 */
	public void updateEnvironment(final IScope scope, final IShape newEnv, final boolean hasParallelism) {
		spatialIndex.update(scope, newEnv.getEnvelope(), hasParallelism);
	}

	@Override
	public boolean isTorus() { return isTorus; }

	@Override
	public void setRoot(final IScope scope, final ITopology root) {}

	/**
	 * Merge with.
	 *
	 * @param other
	 *            the other
	 */
	@Override
	public void mergeWith(final ITopology other) {
		if (other instanceof RootTopology rt) { spatialIndex.mergeWith(rt.spatialIndex); }
	}

	@Override
	public void dispose() {
		super.dispose();
		if (spatialIndex != null) { spatialIndex.dispose(); }
	}

	/**
	 * Removes the.
	 *
	 * @param pop
	 *            the pop
	 */
	public void remove(final IPopulation<? extends IAgent> pop) {
		if (spatialIndex != null) { spatialIndex.remove(pop.getSpecies()); }

	}

}
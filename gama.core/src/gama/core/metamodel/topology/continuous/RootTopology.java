/*******************************************************************************************************
 *
 * RootTopology.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.metamodel.topology.continuous;

import org.locationtech.jts.geom.Envelope;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.CompoundSpatialIndex;
import gama.core.metamodel.topology.ISpatialIndex;
import gama.core.runtime.IScope;

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
		final Envelope bounds = geom.getEnvelope();
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
	public void setRoot(final IScope scope, final RootTopology root) {}

	/**
	 * Merge with.
	 *
	 * @param other
	 *            the other
	 */
	public void mergeWith(final RootTopology other) {
		spatialIndex.mergeWith(other.spatialIndex);
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
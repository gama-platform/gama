/*******************************************************************************************************
 *
 * NoCacheNeighborhood.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.topology.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.topology.INeighborhood;
import gama.api.runtime.scope.IScope;
import gama.api.utils.collections.Collector;

/**
 * The Class NoCacheNeighborhood.
 */
public class NoCacheNeighborhood implements INeighborhood {

	/**
	 *
	 */
	private final GamaSpatialMatrix matrix;

	/**
	 * Instantiates a new no cache neighborhood.
	 *
	 * @param gamaSpatialMatrix the gama spatial matrix
	 */
	public NoCacheNeighborhood(final GamaSpatialMatrix gamaSpatialMatrix) {
		matrix = gamaSpatialMatrix;
	}

	@Override
	public void clear() {}

	/**
	 * Method getNeighborsIn()
	 *
	 * @see gama.api.kernel.topology.INeighborhood#getNeighborsIn(int, int)
	 */
	@Override
	public Set<IAgent> getNeighborsIn(final IScope scope, final int placeIndex, final int radius) {
		return computeNeighborsFrom(scope, placeIndex, 1, radius);
	}

	/**
	 * Compute neighbors from.
	 *
	 * @param scope the scope
	 * @param placeIndex the place index
	 * @param begin the begin
	 * @param end the end
	 * @return the sets the
	 */
	private Set<IAgent> computeNeighborsFrom(final IScope scope, final int placeIndex, final int begin, final int end) {
		try (final Collector.AsOrderedSet<IAgent> result = Collector.getOrderedSet()) {
			final IntConsumer addNeighbor = index -> result.add(matrix.matrix[index].getAgent());
			for (int i = begin; i <= end; i++) {
				if (matrix.usesVN) {
					forEach4NeighborAtRadius(placeIndex, i, addNeighbor);
				} else {
					forEach8NeighborAtRadius(placeIndex, i, addNeighbor);
				}
			}
			// Addresses Issue 1071 by explicitly shuffling the result
			result.shuffleInPlaceWith(scope.getRandom());
			return result.items();
		}
	}

	/**
	 * Gets the 8 neighbors at radius.
	 *
	 * @param placeIndex the place index
	 * @param radius the radius
	 * @return the 8 neighbors at radius
	 */
	protected List<Integer> get8NeighborsAtRadius(final int placeIndex, final int radius) {
		final List<Integer> neighbors = new ArrayList<>(radius << 3);
		forEach8NeighborAtRadius(placeIndex, radius, neighbors::add);
		return neighbors;
	}

	private void forEach8NeighborAtRadius(final int placeIndex, final int radius, final IntConsumer consumer) {
		final int y = placeIndex / matrix.numCols;
		final int x = placeIndex - y * matrix.numCols;
		int p;
		for (int i = 1 - radius; i < radius; i++) {
			p = matrix.getPlaceIndexAt(x + i, y - radius);
			if (p != -1) {
				consumer.accept(p);
			}
			p = matrix.getPlaceIndexAt(x - i, y + radius);
			if (p != -1) {
				consumer.accept(p);
			}
		}
		for (int i = -radius; i < radius + 1; i++) {
			p = matrix.getPlaceIndexAt(x - radius, y - i);
			if (p != -1) {
				consumer.accept(p);
			}
			p = matrix.getPlaceIndexAt(x + radius, y + i);
			if (p != -1) {
				consumer.accept(p);
			}
		}
	}

	/**
	 * Gets the 4 neighbors at radius.
	 *
	 * @param placeIndex the place index
	 * @param radius the radius
	 * @return the 4 neighbors at radius
	 */
	protected List<Integer> get4NeighborsAtRadius(final int placeIndex, final int radius) {
		final List<Integer> neighbors = new ArrayList<>(radius << 2);
		forEach4NeighborAtRadius(placeIndex, radius, neighbors::add);
		return neighbors;
	}

	private void forEach4NeighborAtRadius(final int placeIndex, final int radius, final IntConsumer consumer) {
		final int y = placeIndex / matrix.numCols;
		final int x = placeIndex - y * matrix.numCols;

		int p;
		for (int i = -radius; i < radius; i++) {
			p = matrix.getPlaceIndexAt(x - i, y - Math.abs(i) + radius);
			if (p != -1) {
				consumer.accept(p);
			}
			p = matrix.getPlaceIndexAt(x + i, y + Math.abs(i) - radius);
			if (p != -1) {
				consumer.accept(p);
			}
		}
	}

	/**
	 * Method isVN()
	 *
	 * @see gama.api.kernel.topology.INeighborhood#isVN()
	 */
	@Override
	public boolean isVN() {
		return false;
	}

	/**
	 * Method getRawNeighborsIncluding()
	 *
	 * @see gama.api.kernel.topology.INeighborhood#getRawNeighborsIncluding(int, int)
	 */
	@Override
	public int[] getRawNeighborsIncluding(final IScope scope, final int placeIndex, final int range) {
		throw GamaRuntimeException.warning("The diffusion of signals must rely on a neighbors cache in the grid",
				scope);
	}

	/**
	 * Method neighborsIndexOf()
	 *
	 * @see gama.api.kernel.topology.INeighborhood#neighborsIndexOf(int, int)
	 */
	@Override
	public int neighborsIndexOf(final IScope scope, final int placeIndex, final int n) {
		throw GamaRuntimeException.warning("The diffusion of signals must rely on a neighbors cache in the grid",
				scope);
	}

}
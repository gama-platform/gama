/*******************************************************************************************************
 *
 * GridNeighborhood.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.topology.grid;

import java.util.Arrays;
import java.util.Set;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.topology.INeighborhood;
import gama.api.runtime.scope.IScope;
import gama.api.utils.collections.Collector;

/**
 * Written by drogoul Modified on 8 mars 2011
 *
 * @todo Description
 *
 */
public abstract class GridNeighborhood implements INeighborhood {

	/** The matrix. */
	protected final GamaSpatialMatrix matrix;
	
	/** The neighbors. */
	// i : index of agents; j : index of neighbors
	protected int[][] neighbors;
	
	/** The neighbors indexes. */
	// i : index of agents; j : index of the neighbors by distance
	protected int[][] neighborsIndexes;

	/**
	 * Instantiates a new grid neighborhood.
	 *
	 * @param matrix the matrix
	 */
	public GridNeighborhood(final GamaSpatialMatrix matrix) {
		this.matrix = matrix;
		neighbors = new int[matrix.matrix.length][0];
		// neighborsIndexes = new ArrayList[agents.length];
		neighborsIndexes = new int[matrix.matrix.length][];
	}

	@Override
	public int[] getRawNeighborsIncluding(final IScope scope, final int placeIndex, final int radius) {
		// List<Integer> n = neighborsIndexes[placeIndex];
		int[] n = neighborsIndexes[placeIndex];
		if (n == null) {
			// n = new ArrayList<Integer>();
			n = new int[0];
			neighborsIndexes[placeIndex] = n;
		}
		// final int size = n.size();
		final int size = n.length;
		if (radius > size) {
			computeNeighborsFrom(placeIndex, size + 1, radius);
		}
		return neighbors[placeIndex];
	}

	/**
	 * Gets the neighbors at radius.
	 *
	 * @param placeIndex the place index
	 * @param radius the radius
	 * @return the neighbors at radius
	 */
	protected abstract Set<Integer> getNeighborsAtRadius(final int placeIndex, final int radius);

	/**
	 * Compute neighbors from.
	 *
	 * @param placeIndex the place index
	 * @param begin the begin
	 * @param end the end
	 */
	private void computeNeighborsFrom(final int placeIndex, final int begin, final int end) {
		int[] buffer = neighbors[placeIndex];
		int size = buffer.length;
		final int[] indexes = Arrays.copyOf(neighborsIndexes[placeIndex], end);
		int completed = begin - 1;
		try {
			for (int i = begin; i <= end; i++) {
				final Set<Integer> list = getNeighborsAtRadius(placeIndex, i);
				final int required = size + list.size();
				if (required > buffer.length) {
					buffer = Arrays.copyOf(buffer, Math.max(required, buffer.length + (buffer.length >> 1) + 1));
				}
				int index = size;
				for (final Integer neighbor : list) { buffer[index++] = neighbor.intValue(); }
				size = required;
				indexes[i - 1] = size;
				completed = i;
			}
		} finally {
			// Keep only completed rings if computing a later ring fails.
			neighbors[placeIndex] = buffer.length == size ? buffer : Arrays.copyOf(buffer, size);
			neighborsIndexes[placeIndex] = completed == end ? indexes : Arrays.copyOf(indexes, completed);
		}
	}

	@Override
	public int neighborsIndexOf(final IScope scope, final int placeIndex, final int n) {
		if (n == 1) { return 0; }
		final int size = neighborsIndexes[placeIndex].length;
		if (n > size) { return neighbors[placeIndex].length - 1; }
		return neighborsIndexes[placeIndex][n - 2];
	}

	@Override
	public Set<IAgent> getNeighborsIn(final IScope scope, final int placeIndex, final int radius) {
		int[] n = neighborsIndexes[placeIndex];
		if (n == null) {
			n = new int[0];
			neighborsIndexes[placeIndex] = n;
		}
		final int size = n.length;
		if (radius > size) {
			computeNeighborsFrom(placeIndex, size + 1, radius);
		}
		final int[] nn = neighbors[placeIndex];
		final int nnSize = neighborsIndexes[placeIndex][radius - 1];
		try (final Collector.AsOrderedSet<IAgent> result = Collector.getOrderedSet()) {
			for (int i = 0; i < nnSize; i++) {
				result.add(matrix.matrix[nn[i]].getAgent());
			}
			result.shuffleInPlaceWith(scope.getRandom());
			return result.items();
		}
	}

	@Override
	public void clear() {
		neighbors = null;
		neighborsIndexes = null;
	}

}
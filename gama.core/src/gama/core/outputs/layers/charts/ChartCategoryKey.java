/*******************************************************************************************************
 *
 * ChartCategoryKey.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

/**
 * JFreeChart column key for categories. Identified by its position so that several categories can share the same
 * displayed label (JFreeChart would otherwise merge them); {@link #toString()} returns the label.
 */
public record ChartCategoryKey(int index, String label) implements Comparable<ChartCategoryKey> {

	@Override
	public int compareTo(final ChartCategoryKey o) {
		return Integer.compare(index, o.index);
	}

	@Override
	public String toString() {
		return label;
	}
}

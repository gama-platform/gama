/*******************************************************************************************************
 *
 * DoubleList.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.util.Arrays;

/**
 * A high-performance unboxed primitive double array list for chart data collection.
 * Eliminates Double object autoboxing, GC overhead on clear, and reduces memory usage by 75%.
 */
public class DoubleList implements Cloneable {

	private double[] elementData;
	private int size;

	public DoubleList() {
		this(16);
	}

	public DoubleList(final int initialCapacity) {
		this.elementData = new double[Math.max(16, initialCapacity)];
		this.size = 0;
	}

	public void add(final double element) {
		ensureCapacity(size + 1);
		elementData[size++] = element;
	}

	public double get(final int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
		return elementData[index];
	}

	public void set(final int index, final double element) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
		elementData[index] = element;
	}

		public int indexOf(final double value) {
		for (int i = 0; i < size; i++) {
			if (elementData[i] == value) return i;
		}
		return -1;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public void clear() {
		size = 0;
	}

	public void addAll(final DoubleList other) {
		if (other == null || other.isEmpty()) return;
		ensureCapacity(size + other.size);
		System.arraycopy(other.elementData, 0, elementData, size, other.size);
		size += other.size;
	}

	public double last() {
		if (size == 0) return 0.0;
		return elementData[size - 1];
	}

	public double removeLast() {
		if (size == 0) return 0.0;
		return elementData[--size];
	}

	public double min() {
		if (size == 0) return 0.0;
		double m = elementData[0];
		for (int i = 1; i < size; i++) {
			if (elementData[i] < m) m = elementData[i];
		}
		return m;
	}

	public double max() {
		if (size == 0) return 0.0;
		double m = elementData[0];
		for (int i = 1; i < size; i++) {
			if (elementData[i] > m) m = elementData[i];
		}
		return m;
	}

	public double[] toArray() {
		return Arrays.copyOf(elementData, size);
	}

	@Override
	public DoubleList clone() {
		DoubleList copy = new DoubleList(this.size);
		copy.size = this.size;
		System.arraycopy(this.elementData, 0, copy.elementData, 0, this.size);
		return copy;
	}

	private void ensureCapacity(final int minCapacity) {
		if (minCapacity > elementData.length) {
			int newCapacity = Math.max(elementData.length * 2, minCapacity);
			elementData = Arrays.copyOf(elementData, newCapacity);
		}
	}
}

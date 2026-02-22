/*******************************************************************************************************
 *
 * GamaListArrayWrapper.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.types.list;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.google.common.collect.Iterators;

import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;

/**
 * A highly efficient zero-copy wrapper that adapts a Java array into a GAMA {@link IList}.
 * 
 * <p>
 * {@code GamaListArrayWrapper} provides a read-optimized {@link List} view over an array without copying the
 * underlying data. It implements {@link RandomAccess} for optimal performance with algorithms that check for this
 * marker interface.
 * </p>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Zero-Copy Design</b>: Wraps arrays directly without data duplication</li>
 * <li><b>RandomAccess</b>: Marked for optimal performance in random-access algorithms</li>
 * <li><b>Bidirectional Updates</b>: Changes through the wrapper modify the original array</li>
 * <li><b>Minimal Overhead</b>: Direct array access for maximum performance</li>
 * <li><b>Type Tracking</b>: Maintains {@link IContainerType} for GAML integration</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <p>
 * <b>Do not instantiate directly</b>. Use {@link GamaListFactory#wrap} instead:
 * </p>
 * 
 * <pre>
 * // Wrap an existing array
 * String[] array = { "a", "b", "c" };
 * IList&lt;String&gt; list = GamaListFactory.wrap(Types.STRING, array);
 * 
 * // Changes affect the original array
 * list.set(0, "A");
 * assert array[0].equals("A"); // true
 * 
 * // Direct array modification visible through wrapper
 * array[1] = "B";
 * assert list.get(1).equals("B"); // true
 * </pre>
 * 
 * <h2>When to Use</h2>
 * <p>
 * Use {@code GamaListArrayWrapper} when:
 * </p>
 * <ul>
 * <li>You have an existing array that needs List/IList interface</li>
 * <li>Random access performance is critical</li>
 * <li>Memory efficiency is important (avoid copying)</li>
 * <li>The array size is fixed (arrays cannot grow/shrink)</li>
 * </ul>
 * 
 * <p>
 * <b>Do not use</b> when:
 * </p>
 * <ul>
 * <li>You need to add/remove elements (arrays have fixed size)</li>
 * <li>You need an independent copy</li>
 * <li>Thread safety is required without external synchronization</li>
 * </ul>
 * 
 * <h2>Performance Characteristics</h2>
 * <ul>
 * <li><b>get(index)</b>: O(1) - direct array access</li>
 * <li><b>set(index, element)</b>: O(1) - direct array access</li>
 * <li><b>size()</b>: O(1) - array length field</li>
 * <li><b>indexOf/contains</b>: O(n) - linear search</li>
 * <li><b>add/remove</b>: Not supported (UnsupportedOperationException) - arrays are fixed-size</li>
 * </ul>
 * 
 * <h2>Supported Operations</h2>
 * <p>
 * <b>Fully Supported (from AbstractList):</b>
 * </p>
 * <ul>
 * <li>get, set - random access with O(1) performance</li>
 * <li>size, isEmpty - size queries</li>
 * <li>indexOf, lastIndexOf, contains - search operations</li>
 * <li>iterator, listIterator - iteration</li>
 * <li>toArray - efficient array conversion (uses clone)</li>
 * </ul>
 * 
 * <p>
 * <b>Partially Supported (with limitations):</b>
 * </p>
 * <ul>
 * <li>replaceAll, sort - modify elements in-place</li>
 * <li>subList - creates view but structural changes not allowed</li>
 * </ul>
 * 
 * <p>
 * <b>Not Supported (throw UnsupportedOperationException):</b>
 * </p>
 * <ul>
 * <li>add, addAll - arrays cannot grow</li>
 * <li>remove, removeAll, clear - arrays cannot shrink</li>
 * </ul>
 * 
 * <h2>Equality</h2>
 * <p>
 * Overrides {@link #equals(Object)} to use {@link GamaListFactory#equals}, comparing elements but not array identity.
 * Two wrappers are equal if they have the same elements in the same order, even if wrapping different arrays.
 * </p>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * Not thread-safe. Concurrent access requires external synchronization or use of thread-safe alternatives.
 * </p>
 * 
 * <h2>Implementation Notes</h2>
 * <ul>
 * <li>Package-private constructor ensures creation only through {@link GamaListFactory}</li>
 * <li>Uses final array reference (wrapper structure is immutable)</li>
 * <li>Implements custom {@link #toArray()} using {@link Arrays#clone} for efficiency</li>
 * <li>Provides custom {@link #spliterator()} implementation for stream support</li>
 * </ul>
 * 
 * @param <E>
 *            the element type
 * 
 * @see GamaListFactory#wrap(IType, Object[])
 * @see IList
 * @see RandomAccess
 * @see Arrays#asList(Object...)
 * 
 * @author drogoul
 */
public class GamaListArrayWrapper<E> extends AbstractList<E> implements IList<E>, RandomAccess {

	/** The a. */
	private final E[] a;
	
	/** The type. */
	private final IContainerType type;

	/**
	 * Instantiates a new gama list array wrapper.
	 *
	 * @param array the array
	 * @param contents the contents
	 */
	GamaListArrayWrapper(final E[] array, final IType contents) {
		a = Objects.requireNonNull(array);
		type = Types.LIST.of(contents);
	}

	@Override
	public int size() {
		return a.length;
	}

	@Override
	public Object[] toArray() {
		return a.clone();
	}

	@Override
	public boolean equals(final Object other) {
		if (other == this) { return true; }
		if (!(other instanceof IList)) { return false; }
		return GamaListFactory.equals(this, (IList) other);
	}

	@Override
	@SuppressWarnings ("unchecked")
	public <T> T[] toArray(final T[] a) {
		final int size = size();
		if (a.length < size) { return Arrays.copyOf(this.a, size, (Class<? extends T[]>) a.getClass()); }
		System.arraycopy(this.a, 0, a, 0, size);
		if (a.length > size) {
			a[size] = null;
		}
		return a;
	}

	@Override
	public E get(final int index) {
		return a[index];
	}

	@Override
	public E set(final int index, final E element) {
		final E oldValue = a[index];
		a[index] = element;
		return oldValue;
	}

	@Override
	public int indexOf(final Object o) {
		final E[] a = this.a;
		if (o == null) {
			for (int i = 0; i < a.length; i++) {
				if (a[i] == null) { return i; }
			}
		} else {
			for (int i = 0; i < a.length; i++) {
				if (o.equals(a[i])) { return i; }
			}
		}
		return -1;
	}

	@Override
	public boolean contains(final Object o) {
		return indexOf(o) != -1;
	}

	@Override
	public Spliterator<E> spliterator() {
		return Spliterators.spliterator(a, Spliterator.ORDERED);
	}

	@Override
	public void forEach(final Consumer<? super E> action) {
		for (final E e : a) {
			action.accept(e);
		}
	}

	@Override
	public void replaceAll(final UnaryOperator<E> operator) {
		final E[] a = this.a;
		for (int i = 0; i < a.length; i++) {
			a[i] = operator.apply(a[i]);
		}
	}

	@Override
	public void sort(final Comparator<? super E> c) {
		Arrays.sort(a, c);
	}

	@Override
	public IContainerType<?> getGamlType() {
		return type;
	}

	@Override
	public Iterator<E> iterator() {
		return Iterators.forArray(a);
	}

}

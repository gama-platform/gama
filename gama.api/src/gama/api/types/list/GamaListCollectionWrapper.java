/*******************************************************************************************************
 *
 * GamaListCollectionWrapper.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;

import com.google.common.base.Objects;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.Iterables;

import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;

/**
 * A wrapper that adapts a non-List {@link Collection} (Set, Queue, etc.) into a GAMA {@link IList}.
 * 
 * <p>
 * {@code GamaListCollectionWrapper} provides a {@link List} interface over collections that don't natively support
 * indexed access. This is useful for integrating arbitrary Java collections into GAMA's type system, though with
 * performance trade-offs for index-based operations.
 * </p>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Collection Adaptation</b>: Converts any Collection to IList interface</li>
 * <li><b>Zero-Copy Design</b>: Wraps existing collections without data duplication</li>
 * <li><b>Bidirectional Updates</b>: Non-indexed operations (add, remove by value) affect the wrapped collection</li>
 * <li><b>Type Tracking</b>: Maintains {@link IContainerType} for GAML integration</li>
 * <li><b>ForwardingCollection-based</b>: Inherits delegation pattern from Guava</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <p>
 * <b>Do not instantiate directly</b>. Used internally by {@link GamaListFactory} when wrapping non-List collections:
 * </p>
 * 
 * <pre>
 * // Wrap a Set (order may vary)
 * Set&lt;String&gt; set = new HashSet&lt;&gt;(Arrays.asList("a", "b", "c"));
 * IList&lt;String&gt; list = GamaListFactory.wrap(Types.STRING, set);
 * 
 * // Indexed access works but may be slow
 * String first = list.get(0); // O(n) - iterates to index
 * 
 * // Non-indexed operations are efficient
 * list.add("d"); // O(1) for HashSet
 * list.remove("a"); // O(1) for HashSet
 * </pre>
 * 
 * <h2>When to Use</h2>
 * <p>
 * Use {@code GamaListCollectionWrapper} when:
 * </p>
 * <ul>
 * <li>You have a Set, Queue, or other non-List Collection</li>
 * <li>You need IList interface for GAMA integration</li>
 * <li>Indexed access is rare or performance is not critical</li>
 * <li>The collection's iteration order is acceptable as the "list order"</li>
 * </ul>
 * 
 * <p>
 * <b>Do not use</b> when:
 * </p>
 * <ul>
 * <li>Frequent indexed access is required (convert to ArrayList instead)</li>
 * <li>Stable ordering by index is critical (use ordered collections)</li>
 * <li>Performance is critical for get/set operations</li>
 * </ul>
 * 
 * <h2>Performance Characteristics</h2>
 * <p>
 * Performance depends heavily on the wrapped collection type:
 * </p>
 * 
 * <table border="1">
 * <tr>
 * <th>Operation</th>
 * <th>Complexity</th>
 * <th>Notes</th>
 * </tr>
 * <tr>
 * <td>get(index)</td>
 * <td>O(n)</td>
 * <td>Uses Guava's Iterables.get - iterates to index</td>
 * </tr>
 * <tr>
 * <td>set(index, element)</td>
 * <td>O(n)</td>
 * <td>Gets element at index, adds new element (order not guaranteed)</td>
 * </tr>
 * <tr>
 * <td>add(element)</td>
 * <td>O(1) or O(log n)</td>
 * <td>Delegates to collection's add (HashSet: O(1), TreeSet: O(log n))</td>
 * </tr>
 * <tr>
 * <td>remove(element)</td>
 * <td>O(1) or O(n)</td>
 * <td>Delegates to collection's remove</td>
 * </tr>
 * <tr>
 * <td>size()</td>
 * <td>O(1)</td>
 * <td>Direct delegation</td>
 * </tr>
 * <tr>
 * <td>contains(element)</td>
 * <td>O(1) or O(n)</td>
 * <td>Delegates to collection's contains</td>
 * </tr>
 * </table>
 * 
 * <h2>Index-Based Operations</h2>
 * <p>
 * <b>Warning:</b> Index-based operations are <b>not meaningful</b> for most non-List collections:
 * </p>
 * <ul>
 * <li><b>Unordered Sets</b>: Index order is arbitrary and may change</li>
 * <li><b>Queues</b>: Index order may not reflect queue ordering</li>
 * <li><b>set(index, element)</b>: Doesn't replace at index; adds element and removes old one</li>
 * <li><b>add(index, element)</b>: Ignores index parameter; just adds to collection</li>
 * </ul>
 * 
 * <h2>Supported Operations</h2>
 * <p>
 * <b>Fully Supported (delegated to Collection):</b>
 * </p>
 * <ul>
 * <li>add(element), addAll - collection operations</li>
 * <li>remove(element), removeAll, retainAll - collection operations</li>
 * <li>contains, containsAll - membership tests</li>
 * <li>size, isEmpty, clear - size operations</li>
 * <li>iterator - iteration</li>
 * </ul>
 * 
 * <p>
 * <b>Supported with Performance Penalty:</b>
 * </p>
 * <ul>
 * <li>get(index) - O(n) iteration to index</li>
 * <li>indexOf, lastIndexOf - O(n) search</li>
 * </ul>
 * 
 * <p>
 * <b>Supported but Semantics Differ:</b>
 * </p>
 * <ul>
 * <li>set(index, element) - doesn't truly replace at index</li>
 * <li>add(index, element) - ignores index</li>
 * <li>remove(index) - gets element at index then removes it</li>
 * </ul>
 * 
 * <p>
 * <b>Not Properly Supported:</b>
 * </p>
 * <ul>
 * <li>listIterator - costly to implement correctly</li>
 * <li>subList - would require maintaining index consistency</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * Not thread-safe unless the wrapped collection is thread-safe.
 * </p>
 * 
 * <h2>Equality</h2>
 * <p>
 * Uses {@link GamaListFactory#equals} for comparison, which compares elements in iteration order.
 * </p>
 * 
 * <h2>Best Practices</h2>
 * <ul>
 * <li>If you frequently need indexed access, convert to a proper List first</li>
 * <li>Use only for temporary GAMA integration of collections</li>
 * <li>Prefer iteration over indexed access</li>
 * <li>Be aware that "list order" is just iteration order</li>
 * </ul>
 * 
 * @param <E>
 *            the element type
 * 
 * @see GamaListFactory
 * @see IList
 * @see ForwardingCollection
 * 
 * @author drogoul
 */
public class GamaListCollectionWrapper<E> extends ForwardingCollection<E> implements IList<E> {

	/** The wrapped. */
	final Collection<E> wrapped;

	/** The type. */
	final IContainerType type;

	/**
	 * Instantiates a new gama list collection wrapper.
	 *
	 * @param wrapped
	 *            the wrapped
	 * @param contents
	 *            the contents
	 */
	GamaListCollectionWrapper(final Collection<E> wrapped, final IType contents) {
		this.type = Types.LIST.of(contents);
		this.wrapped = wrapped;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == this) return true;
		if (!(other instanceof IList)) return false;
		return GamaListFactory.equals(this, (IList) other);
	}

	@Override
	public IContainerType<?> getGamlType() { return type; }

	@Override
	protected Collection<E> delegate() {
		return wrapped;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		return addAll(c);
	}

	@Override
	public E get(final int index) {
		if (index > size() - 1) return null;
		return Iterables.get(wrapped, index);
	}

	@Override
	public E set(final int index, final E element) {
		final E old = get(index);
		add(element);
		return old;
	}

	@Override
	public void add(final int index, final E element) {
		add(element);
	}

	@Override
	public E remove(final int index) {
		final E element = get(index);
		remove(element);
		return element;
	}

	@Override
	public int indexOf(final Object o) {
		return Iterables.indexOf(wrapped, o1 -> Objects.equal(o, o1));
	}

	@Override
	public int lastIndexOf(final Object o) {
		// Same as indexOf for collections
		return new ArrayList<>(wrapped).lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return new ArrayList<>(wrapped).listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		return new ArrayList<>(wrapped).listIterator(index);
	}

	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		return new ArrayList<>(wrapped).subList(fromIndex, toIndex);
	}

	@Override
	public Spliterator<E> spliterator() {
		return wrapped.spliterator();
	}

	@Override
	public int hashCode() {
		return wrapped.hashCode();
	}

}

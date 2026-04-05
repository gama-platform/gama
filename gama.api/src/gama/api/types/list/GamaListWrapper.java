/*******************************************************************************************************
 *
 * GamaListWrapper.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.types.list;

import java.util.List;

import com.google.common.collect.ForwardingList;

import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;

/**
 * A zero-copy wrapper that adapts a standard Java {@link List} into a GAMA {@link IList}.
 * 
 * <p>
 * {@code GamaListWrapper} uses Guava's {@link ForwardingList} to delegate all {@link List} operations to an existing
 * Java list while adding GAMA-specific type tracking and container behaviors. This allows seamless integration of
 * Java collections into the GAMA type system without copying data.
 * </p>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Zero-Copy Design</b>: Wraps existing lists without data duplication</li>
 * <li><b>Bidirectional Updates</b>: Changes to the wrapper affect the wrapped list and vice versa</li>
 * <li><b>Type Tracking</b>: Maintains an {@link IContainerType} for GAML type system integration</li>
 * <li><b>ForwardingList-based</b>: Inherits reliable delegation pattern from Guava</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <p>
 * <b>Do not instantiate directly</b>. Use {@link GamaListFactory#wrap} instead:
 * </p>
 * 
 * <pre>
 * // Wrap an existing Java list
 * List&lt;String&gt; javaList = new ArrayList&lt;&gt;(Arrays.asList("a", "b", "c"));
 * IList&lt;String&gt; gamaList = GamaListFactory.wrap(Types.STRING, javaList);
 * 
 * // Both references point to the same data
 * javaList.add("d");
 * assert gamaList.size() == 4; // true
 * 
 * gamaList.add("e");
 * assert javaList.size() == 5; // true
 * </pre>
 * 
 * <h2>When to Use</h2>
 * <p>
 * Use {@code GamaListWrapper} when:
 * </p>
 * <ul>
 * <li>You have an existing Java list that needs GAMA integration</li>
 * <li>You want to avoid copying large lists</li>
 * <li>Bidirectional updates are desired or acceptable</li>
 * <li>The wrapped list is already of the correct type (no casting needed)</li>
 * </ul>
 * 
 * <p>
 * <b>Do not use</b> when:
 * </p>
 * <ul>
 * <li>You need an independent copy (use {@link GamaListFactory#create} instead)</li>
 * <li>The original list should remain immutable</li>
 * <li>Thread safety is required and the wrapped list is not thread-safe</li>
 * </ul>
 * 
 * <h2>Performance Characteristics</h2>
 * <p>
 * Inherits all performance characteristics of the wrapped list:
 * </p>
 * <ul>
 * <li><b>ArrayList</b>: O(1) random access, O(1) amortized append, O(n) insert/remove</li>
 * <li><b>LinkedList</b>: O(n) random access, O(1) insert/remove at ends</li>
 * <li><b>No overhead</b>: Direct delegation without additional processing</li>
 * </ul>
 * 
 * <h2>Type Safety</h2>
 * <p>
 * The wrapper tracks the content type but does <b>not enforce</b> it on the wrapped list. If the wrapped list is
 * modified directly with incompatible types, the wrapper will not prevent this:
 * </p>
 * 
 * <pre>
 * List rawList = new ArrayList();
 * IList&lt;String&gt; wrapped = GamaListFactory.wrap(Types.STRING, rawList);
 * 
 * // Direct modification bypasses type safety
 * rawList.add(123); // No error, but violates contract
 * </pre>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * Not thread-safe unless the wrapped list is thread-safe (e.g., {@link Collections#synchronizedList}).
 * </p>
 * 
 * <h2>Implementation Notes</h2>
 * <ul>
 * <li>Package-private constructor ensures creation only through {@link GamaListFactory}</li>
 * <li>Uses final fields for type and wrapped list (immutable wrapper structure)</li>
 * <li>Minimal memory overhead: just the type reference + ForwardingList overhead</li>
 * </ul>
 * 
 * @param <E>
 *            the element type
 * 
 * @see GamaListFactory#wrap(IType, List)
 * @see IList
 * @see ForwardingList
 * 
 * @author drogoul
 */
public class GamaListWrapper<E> extends ForwardingList<E> implements IList<E> {

	/** The wrapped. */
	final List<E> wrapped;
	
	/** The type. */
	final IContainerType type;

	/**
	 * Instantiates a new gama list wrapper.
	 *
	 * @param wrapped the wrapped
	 * @param contents the contents
	 */
	GamaListWrapper(final List<E> wrapped, final IType contents) {
		this.type = Types.LIST.of(contents);
		this.wrapped = wrapped;
	}

	@Override
	public IContainerType<?> getGamlType() {
		return type;
	}

	@Override
	protected List<E> delegate() {
		return wrapped;
	}

}

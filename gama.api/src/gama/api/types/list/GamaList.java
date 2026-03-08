/*******************************************************************************************************
 *
 * GamaList.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import one.util.streamex.StreamEx;

/**
 * The primary concrete implementation of {@link IList} for the GAMA platform.
 * 
 * <p>
 * {@code GamaList} extends {@link ArrayList} to provide a type-safe, GAML-integrated list implementation. It tracks
 * its content type through an {@link IContainerType} and ensures proper type handling for all operations.
 * </p>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>ArrayList-based</b>: Inherits all performance characteristics of ArrayList (fast random access, dynamic
 * resizing)</li>
 * <li><b>Type Tracking</b>: Maintains an {@link IContainerType} describing the element type</li>
 * <li><b>Efficient Cloning</b>: Supports shallow copying while preserving or changing content type</li>
 * <li><b>Custom Equality</b>: Uses {@link GamaListFactory#equals} for GAMA-aware equality checks</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <p>
 * <b>Do not instantiate directly</b>. Use {@link GamaListFactory} instead:
 * </p>
 * 
 * <pre>
 * // Create an empty list
 * IList&lt;Integer&gt; numbers = GamaListFactory.create(Types.INT);
 * 
 * // Create from elements
 * IList&lt;String&gt; strings = GamaListFactory.create(scope, Types.STRING, "a", "b", "c");
 * 
 * // Create with initial capacity
 * IList&lt;Double&gt; values = GamaListFactory.create(Types.FLOAT, 100);
 * </pre>
 * 
 * <h2>Type Handling</h2>
 * <p>
 * The list maintains a {@link IContainerType} that defines its element type. When the content type is changed (via
 * {@link #listValue} or {@link #cloneWithContentType}), a new type is created:
 * </p>
 * 
 * <pre>
 * IList&lt;Object&gt; original = GamaListFactory.create(Types.INT);
 * // Change content type to FLOAT
 * IList&lt;Double&gt; converted = original.listValue(scope, Types.FLOAT, true);
 * </pre>
 * 
 * <h2>Performance Characteristics</h2>
 * <ul>
 * <li><b>Random Access</b>: O(1) - inherited from ArrayList</li>
 * <li><b>Add/Remove at end</b>: O(1) amortized - inherited from ArrayList</li>
 * <li><b>Add/Remove at index</b>: O(n) - inherited from ArrayList</li>
 * <li><b>Type Casting</b>: Additional overhead when {@code FLAGS.CAST_CONTAINER_CONTENTS} is enabled</li>
 * </ul>
 * 
 * <h2>Equality and Hashing</h2>
 * <p>
 * Overrides {@link #equals(Object)} to use {@link GamaListFactory#equals}, which compares:
 * </p>
 * <ul>
 * <li>List sizes</li>
 * <li>Element equality (element by element)</li>
 * <li>Does NOT compare content types</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * Like ArrayList, {@code GamaList} is <b>not thread-safe</b>. For concurrent access, wrap with
 * {@link Collections#synchronizedList} or use concurrent collections.
 * </p>
 * 
 * <h2>Implementation Notes</h2>
 * <ul>
 * <li><b>Protected Constructor</b>: Ensures creation only through GamaListFactory</li>
 * <li><b>Mutable Type</b>: The type field is mutable to support efficient type changes during cloning</li>
 * <li><b>Streaming</b>: Provides {@link #stream(IScope)} for StreamEx integration</li>
 * </ul>
 * 
 * @param <E>
 *            the element type
 * 
 * @see GamaListFactory for creation methods
 * @see IList for the interface contract
 * @see ArrayList for underlying implementation details
 * 
 * @author drogoul
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaList<E> extends ArrayList<E> implements IList<E> {

	/** The type. */
	private IContainerType type;

	@Override
	public IContainerType<?> getGamlType() { return type; }

	@Override
	public StreamEx<E> stream(final IScope scope) {
		return StreamEx.<E> of(this);
	}

	@Override
	public boolean equals(final Object other) {
		if (other == this) return true;
		if (!(other instanceof IList)) return false;
		return GamaListFactory.equals(this, (IList) other);
	}

	/**
	 * Instantiates a new gama list.
	 */
	protected GamaList() {
		this(0, Types.NO_TYPE);
	}

	/**
	 * Instantiates a new gama list.
	 *
	 * @param capacity
	 *            the capacity
	 * @param contentType
	 *            the content type
	 */
	protected GamaList(final int capacity, final IType contentType) {
		super(capacity);
		this.type = Types.LIST.of(contentType);
	}

	@Override
	public IList<E> listValue(final IScope scope, final IType contentsType, final boolean copy) {
		IType myContentsType = getGamlType().getContentType();
		if (!GamaType.requiresCasting(contentsType, myContentsType)) {
			if (copy) return this.cloneWithContentType(contentsType);
			// See #385 : if we do not copy, but the contents types are different, we create a wrapper in order to not
			// duplicate the collection
			if (!contentsType.equals(myContentsType)) return GamaListFactory.wrap(contentsType, this);
			return this;
		}
		final GamaList clone = this.cloneWithContentType(contentsType);
		final int n = size();
		for (int i = 0; i < n; i++) { clone.setValueAtIndex(scope, i, get(i)); }
		return clone;
	}

	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		for (final Iterator iterator = iterator(); iterator.hasNext();) {
			if (Objects.equals(iterator.next(), value)) { iterator.remove(); }
		}
	}

	/**
	 * Clone with content type.
	 *
	 * @param contentType
	 *            the content type
	 * @return the gama list
	 */
	private GamaList cloneWithContentType(final IType contentType) {
		final GamaList clone = (GamaList) super.clone();
		clone.type = Types.LIST.of(contentType);
		return clone;
	}

	@Override
	public IList<E> copy(final IScope scope) {
		return cloneWithContentType(type.getContentType());
	}

	@Override
	public E getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, Cast.asInt(scope, indices.get(0)));
		// We do not consider the case where multiple indices are used. Maybe
		// could be used in the future to return a list of values ?
	}

}

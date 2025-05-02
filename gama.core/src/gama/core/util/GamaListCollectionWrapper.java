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
package gama.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;

import com.google.common.base.Objects;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.Iterables;

import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * A wrapper that tries to wrap a Collection into an IList. Not all operations are meaningful (those with indices in
 * particular) and some are really costly (listIterators). Wrapped can not be a List.
 *
 * @author drogoul
 *
 * @param <E>
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

/*******************************************************************************************************
 *
 * GamaDateInterval.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.date;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.google.common.collect.Iterators;

import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IDate;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IMatrix;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaDateType;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;

/**
 * An immutable interval of time between two instants.
 * <p>
 * An interval represents the time on the time-line between two {@link IDate} s. The class stores the start and end
 * dates, with the start inclusive and the end exclusive. The end date is always greater than or equal to the start
 * instant.
 * <p>
 * The {@link Duration} of an interval can be obtained, but is a separate concept. An interval is connected to the
 * time-line, whereas a duration is not.
 * <p>
 * Intervals are not comparable. To compare the length of two intervals, it is generally recommended to compare their
 * durations.
 */
public final class GamaDateInterval implements IList<IDate> {

	/**
	 * The start instant (inclusive).
	 */
	final IDate start;
	/**
	 * The end instant (exclusive).
	 */
	final IDate end;

	/** The step. */
	final Duration step;

	/** The size. */
	final Integer size;

	/**
	 * @param startInclusive
	 *            the start instant, inclusive, MIN_DATE treated as unbounded, not null
	 * @param endExclusive
	 *            the end instant, exclusive, MAX_DATE treated as unbounded, not null
	 * @return the half-open interval, not null
	 * @throws DateTimeException
	 *             if the end is before the start
	 */
	public static GamaDateInterval of(final IDate startInclusive, final IDate endExclusive) {
		return new GamaDateInterval(startInclusive, endExclusive);
	}

	/**
	 * Instantiates a new gama date interval.
	 *
	 * @param startInclusive
	 *            the start inclusive
	 * @param endExclusive
	 *            the end exclusive
	 */
	private GamaDateInterval(final IDate startInclusive, final IDate endExclusive) {
		this(startInclusive, endExclusive,
				Duration.of(GamaDateType.DATES_TIME_STEP.getValue().longValue(), ChronoUnit.SECONDS));
	}

	/**
	 * Instantiates a new gama date interval.
	 *
	 * @param startInclusive
	 *            the start inclusive
	 * @param endExclusive
	 *            the end exclusive
	 * @param step
	 *            the step
	 */
	public GamaDateInterval(final IDate startInclusive, final IDate endExclusive, final Duration step) {
		this.start = startInclusive;
		this.end = endExclusive;
		if (start.isAfter(end)) {
			this.step = step.abs().negated();
		} else {
			this.step = step;
		}
		size = size();
	}

	/**
	 * Gets the start instant (inclusive).
	 *
	 * @return the start instant (inclusive)
	 */
	public IDate getStart() { return start; }

	/**
	 * Gets the end instant (exclusive).
	 *
	 * @return the end instant (exclusive)
	 */
	public IDate getEnd() { return end; }

	@Override
	public boolean isEmpty() { return start.equals(end); }

	/**
	 * Contains.
	 *
	 * @param instant
	 *            the instant
	 * @return true, if successful
	 */
	public boolean contains(final IDate instant) {
		return start.compareTo(instant) <= 0 && instant.compareTo(end) < 0;
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains the duration of this interval.
	 * <p>
	 * An {@code Interval} is associated with two specific instants on the time-line. A {@code Duration} is simply an
	 * amount of time, separate from the time-line.
	 *
	 * @return the duration of the time interval
	 * @throws ArithmeticException
	 *             if the calculation exceeds the capacity of {@code Duration}
	 */
	public Duration toDuration() {
		return Duration.between(start, end);
	}

	// -----------------------------------------------------------------------
	/**
	 * Checks if this interval is equal to another interval.
	 * <p>
	 * Compares this {@code Interval} with another ensuring that the two instants are the same. Only objects of type
	 * {@code Interval} are compared, other types return false.
	 *
	 * @param obj
	 *            the object to check, null returns false
	 * @return true if this is equal to the other interval
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj instanceof final GamaDateInterval other) return start.equals(other.start) && end.equals(other.end);
		return false;
	}

	/**
	 * A hash code for this interval.
	 *
	 * @return a suitable hash code
	 */
	@Override
	public int hashCode() {
		return start.hashCode() ^ end.hashCode();
	}

	// -----------------------------------------------------------------------
	/**
	 * Outputs this interval as a {@code String}, such as {@code 2007-12-03T10:15:30/2007-12-04T10:15:30}.
	 * <p>
	 * The output will be the ISO-8601 format formed by combining the {@code toString()} methods of the two instants,
	 * separated by a forward slash.
	 *
	 * @return a string representation of this instant, not null
	 */
	@Override
	public String toString() {
		return start.toString() + '/' + end.toString();
	}

	@Override
	public IContainerType<?> getGamlType() { return Types.LIST.of(Types.DATE); }

	@Override
	public IList<IDate> listValue(final IScope scope, final IType contentType, final boolean copy) {
		if (copy) return GamaListFactory.createWithoutCasting(Types.DATE, this);
		return this;
	}

	@Override
	public Iterable<? extends IDate> iterable(final IScope scope) {
		return this;
	}

	@Override
	public boolean contains(final IScope scope, final Object o) throws GamaRuntimeException {
		return contains(o);
	}

	@Override
	public IDate firstValue(final IScope scope) throws GamaRuntimeException {
		return start;
	}

	@Override
	public IDate lastValue(final IScope scope) throws GamaRuntimeException {
		return end;
	}

	@Override
	public int length(final IScope scope) {
		return size();
	}

	@Override
	public boolean isEmpty(final IScope scope) {
		return this.isEmpty();
	}

	@Override
	public IDate anyValue(final IScope scope) {
		final int i = scope.getRandom().between(0, size());
		return get(i);
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return toString();
	}

	@Override
	public GamaDateInterval copy(final IScope scope) throws GamaRuntimeException {
		return new GamaDateInterval(start, end, step);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "(" + start.serializeToGaml(includingBuiltIn) + " to " + end.serializeToGaml(includingBuiltIn)
				+ ") every (" + (double) step.get(ChronoUnit.SECONDS) + ")";
	}
	//
	// @Override
	// public boolean checkBounds(final IScope scope, final Object index, final boolean forAdding) {
	// return false;
	// }

	/**
	 * Adds the value.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	@Override
	public void addValue(final IScope scope, final IDate value) {}

	/**
	 * Adds the value at index.
	 *
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @param value
	 *            the value
	 */
	@Override
	public void addValueAtIndex(final IScope scope, final Object index, final IDate value) {}

	/**
	 * Sets the value at index.
	 *
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @param value
	 *            the value
	 */
	@Override
	public void setValueAtIndex(final IScope scope, final Object index, final IDate value) {}

	@Override
	public void addValues(final IScope scope, final IContainer values) {}

	/**
	 * Sets the all values.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	@Override
	public void setAllValues(final IScope scope, final IDate value) {}

	@Override
	public void removeValue(final IScope scope, final Object value) {}

	@Override
	public void removeIndex(final IScope scope, final Object index) {}

	@Override
	public void removeIndexes(final IScope scope, final IContainer<?, ?> index) {}

	@Override
	public void removeValues(final IScope scope, final IContainer values) {}

	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {}

	@Override
	public IDate get(final IScope scope, final Integer index) throws GamaRuntimeException {
		return get(index);
	}

	@Override
	public IDate getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		return get(scope, (Integer) indices.get(0));
	}

	@Override
	public int size() {
		if (size != null) return size;
		return Iterators.size(iterator());
	}

	@Override
	public boolean contains(final Object o) {
		if (!(o instanceof IDate)) return false;
		return this.contains((IDate) o);
	}

	@Override
	public Iterator<IDate> iterator() {
		return new Iterator<>() {

			IDate current = null;

			@Override
			public boolean hasNext() {
				if (current == null) return !isEmpty();
				return current.plus(step).isBefore(end);
			}

			@Override
			public IDate next() {
				if (current == null) {
					current = start;
				} else {

					current = current.plus(step);
					if (current.isGreaterThan(end, false)) throw new NoSuchElementException();

				}
				return current;
			}
		};
	}

	@Override
	public IDate[] toArray() {
		return Iterators.toArray(iterator(), IDate.class);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <T> T[] toArray(final T[] a) {
		return (T[]) Iterators.toArray(iterator(), Object.class);
	}

	/**
	 * Adds the.
	 *
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	@Override
	public boolean add(final IDate e) {
		return false;
	}

	@Override
	public boolean remove(final Object o) {
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c) { if (!contains(o)) return false; }
		return true;
	}

	/**
	 * Adds the all.
	 *
	 * @param c
	 *            the c
	 * @return true, if successful
	 */
	@Override
	public boolean addAll(final Collection<? extends IDate> c) {
		return false;
	}

	/**
	 * Adds the all.
	 *
	 * @param index
	 *            the index
	 * @param c
	 *            the c
	 * @return true, if successful
	 */
	@Override
	public boolean addAll(final int index, final Collection<? extends IDate> c) {
		return false;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {}

	@Override
	public IDate get(final int index) {
		return start.plus(step.get(ChronoUnit.SECONDS), index, ChronoUnit.SECONDS);
	}

	/**
	 * Sets the.
	 *
	 * @param index
	 *            the index
	 * @param element
	 *            the element
	 * @return the gama date
	 */
	@Override
	public IDate set(final int index, final IDate element) {
		return null;
	}

	/**
	 * Adds the.
	 *
	 * @param index
	 *            the index
	 * @param element
	 *            the element
	 */
	@Override
	public void add(final int index, final IDate element) {}

	@Override
	public IDate remove(final int index) {
		return null;
	}

	@Override
	public int indexOf(final Object o) {
		int i = 0;
		for (final IDate d : this) {

			if (d.equals(o)) return i;
			i++;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(final Object o) {
		return indexOf(o);
	}

	@Override
	public ListIterator<IDate> listIterator() {
		return new ArrayList<>(this).listIterator();
	}

	@Override
	public ListIterator<IDate> listIterator(final int index) {
		return new ArrayList<>(this).listIterator(index);
	}

	@Override
	public GamaDateInterval subList(final int fromIndex, final int toIndex) {
		return new GamaDateInterval(get(fromIndex), get(toIndex), step);
	}

	@Override
	public GamaDateInterval reverse(final IScope scope) {
		return new GamaDateInterval(end, start, step);
	}

	@Override
	public IMatrix<IDate> matrixValue(final IScope scope, final IType contentType, final IPoint size,
			final boolean copy) {
		return GamaListFactory.wrap(Types.DATE, this).matrixValue(scope, contentType, copy);
	}

	@Override
	public IMatrix<IDate> matrixValue(final IScope scope, final IType contentType, final boolean copy) {
		return GamaListFactory.wrap(Types.DATE, this).matrixValue(scope, contentType, copy);
	}

	/**
	 * Step.
	 *
	 * @param step
	 *            the step
	 * @return the i list
	 */
	public IList<IDate> step(final Double step) {
		return new GamaDateInterval(start, end, Duration.of(step.longValue(), ChronoUnit.SECONDS));
	}

	@SuppressWarnings ("unchecked")
	@Override
	public IMap mapValue(final IScope scope, final IType keyType, final IType contentType, final boolean copy) {
		final IMap<IDate, IDate> map = GamaMapFactory.create(Types.DATE, Types.DATE, this.size());
		for (final IDate date : this) { map.put(date, date); }
		return map;
	}

}
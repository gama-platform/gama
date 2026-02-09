/*******************************************************************************************************
 *
 * IDateFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import java.time.temporal.Temporal;

import gama.api.data.objects.IContainer;
import gama.api.data.objects.IDate;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public interface IDateFactory extends IFactory<IDate> {

	/**
	 * Creates an IDate from an ISO 8601 string representation.
	 *
	 * @param isoString
	 *            the date string in ISO format
	 * @return the corresponding IDate
	 */
	IDate createFromISOString(String isoString);

	/**
	 * Creates an IDate from a Java Temporal object (e.g. Instant, LocalDateTime).
	 *
	 * @param temporal
	 *            the temporal object source
	 * @return the corresponding IDate
	 */
	IDate createFromTemporal(Temporal temporal);

	/**
	 * Creates a new IDate by copying parameters from another IDate.
	 * 
	 * @param scope
	 *            the current scope
	 * @param date
	 *            the source IDate
	 * @return a new IDate, or the same instance if immutable/reusable
	 */
	IDate createFromIDate(IScope scope, IDate date);

	/**
	 * Creates an IDate from a container (e.g. list of values [year, month, day...]).
	 *
	 * @param scope
	 *            the current scope
	 * @param container
	 *            the container holding date components
	 * @return the created IDate
	 */
	IDate createFromContainer(IScope scope, IContainer<?, ?> container);

	/**
	 * Creates an IDate from a generic string, using default parsing logic.
	 *
	 * @param scope
	 *            the current scope
	 * @param str
	 *            the date string
	 * @return the created IDate
	 */
	IDate createFromString(IScope scope, String str);

	/**
	 * Creates an IDate from a double value (representing seconds since epoch or similar).
	 *
	 * @param scope
	 *            the current scope
	 * @param value
	 *            the double value
	 * @return the created IDate
	 */
	IDate createFromDouble(IScope scope, Double value);

	/**
	 * Creates an IDate from a string using a specific pattern and locale.
	 *
	 * @param scope
	 *            the current scope
	 * @param value
	 *            the date string
	 * @param pattern
	 *            the format pattern (e.g. "yyyy-MM-dd")
	 * @param locale
	 *            the locale for parsing (e.g. "en_US")
	 * @return the created IDate
	 */
	IDate createWith(final IScope scope, final String value, final String pattern, final String locale);

	/**
	 * Creates an IDate from a string using a specific pattern.
	 *
	 * @param scope
	 *            the current scope
	 * @param value
	 *            the date string
	 * @param pattern
	 *            the format pattern
	 * @return the created IDate
	 */
	IDate createWith(IScope scope, String value, String pattern);

}

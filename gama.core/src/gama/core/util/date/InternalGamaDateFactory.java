/*******************************************************************************************************
 *
 * InternalGamaDateFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.date;

import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;

import gama.api.data.factories.IDateFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IDate;
import gama.api.gaml.constants.GamlCoreUnits;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.gaml.operators.Dates;

/**
 *
 */
public class InternalGamaDateFactory implements IDateFactory {

	static {
		// Only here to load the class and its preferences
		Dates.initialize();
	}

	@Override
	public IDate createFromISOString(final String s) {
		try {
			final TemporalAccessor t = Dates.getFormatter(GamlCoreUnits.ISO_OFFSET_KEY, null).parse(s);
			if (t instanceof Temporal tmp) return createFromTemporal(tmp);
		} catch (final DateTimeParseException e) {
			//
		}
		return new GamaDate(null, s);
	}

	/**
	 * Creates the from I date.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @return the i date
	 */
	@Override
	public IDate createFromIDate(final IScope scope, final IDate date) {
		return new GamaDate(scope, date);
	}

	@Override
	public IDate createFromTemporal(final Temporal temporal) {
		return new GamaDate(temporal);
	}

	@Override
	public IDate createFromContainer(final IScope scope, final IContainer<?, ?> container) {
		return new GamaDate(scope, container.listValue(scope, Types.INT, false));
	}

	@Override
	public IDate createFromString(final IScope scope, final String str) {
		return new GamaDate(scope, str);
	}

	@Override
	public IDate createFromDouble(final IScope scope, final Double value) {
		return new GamaDate(scope, value);
	}

	@Override
	public IDate createWith(final IScope scope, final String value, final String pattern, final String locale) {
		return new GamaDate(scope, value, pattern, locale);
	}

	@Override
	public IDate createWith(final IScope scope, final String value, final String pattern) {
		return new GamaDate(scope, value, pattern);
	}

}

/*******************************************************************************************************
 *
 * GamaDateType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaDate;
import gama.core.util.IContainer;
import gama.core.util.IDate;
import gama.core.util.map.IMap;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Dates;

/**
 * Written by Patrick Tallandier
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
@type (
		name = "date",
		id = IType.DATE,
		wraps = { IDate.class },
		kind = ISymbolKind.Variable.NUMBER,
		concept = { IConcept.TYPE, IConcept.DATE, IConcept.TIME },
		doc = { @doc ("GAML objects that represent a date") })
public class GamaDateType extends GamaType<IDate> {

	/** The Constant DEFAULT_ZONE. */
	public static final ZoneId DEFAULT_ZONE = Clock.systemDefaultZone().getZone();

	/** The Constant DEFAULT_OFFSET_IN_SECONDS. */
	public static final ZoneOffset DEFAULT_OFFSET_IN_SECONDS =
			Clock.systemDefaultZone().getZone().getRules().getOffset(Instant.now(Clock.systemDefaultZone()));

	/** The Constant EPOCH. */
	public static final IDate EPOCH = fromTemporal(LocalDateTime.ofEpochSecond(0, 0, DEFAULT_OFFSET_IN_SECONDS));

	// public static final ZoneOffset DEFAULT_OFFSET_IN_SECONDS = ZoneOffset.UTC; // ofTotalSeconds(0);
	// public static final ZoneId DEFAULT_ZONE =
	// ZoneId.ofOffset("UTC", DEFAULT_OFFSET_IN_SECONDS);/* ZoneId.ofOffset("", DEFAULT_OFFSET_IN_SECONDS); */
	// // Clock.systemDefaultZone().getZone().getRules().getOffset(Instant.now(Clock.systemDefaultZone()));
	// public static final GamaDate EPOCH = /** GamaDate.of(LocalDateTime.of(1970, 1, 1, 0, 0)); **/
	// GamaDate.of(LocalDateTime.ofEpochSecond(0, 0, DEFAULT_OFFSET_IN_SECONDS));
	@doc ("Cast the argument into a date. If the argument is a date already, returns it, otherwise: if it is a container, casts its contents to integer numbers and tries to build a date from it (following the order 'year, month, day, hour, minute, second'); if it is a string, tries to decode it into a date using the format described in the preferences; otherwise cast the argument into a float number and interprets it as the number of milliseconds since the start of the simulation")
	@Override
	public IDate cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param copy
	 *            the copy
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IDate staticCast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null) return null;
		if (obj instanceof IDate d) {
			if (copy) return new GamaDate(scope, d);
			return d;
		}
		if (obj instanceof IContainer)
			return new GamaDate(scope, ((IContainer<?, ?>) obj).listValue(scope, Types.INT, false));
		if (obj instanceof String) return new GamaDate(scope, (String) obj);
		// If everything fails, we assume it is a duration in seconds since the starting date of the model
		final Double d = Cast.asFloat(scope, obj);
		return new GamaDate(scope, d);
	}

	@Override
	public IDate getDefault() { return null; }

	@Override
	public IType<?> getContentType() { return Types.get(FLOAT); }

	@Override
	public boolean canCastToConst() {
		return false;
	}

	@Override
	public boolean isCompoundType() { return true; }

	@Override
	public IDate deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return fromISOString(Cast.asString(scope, map2.get("iso")));
	}

	/**
	 * From ISO string.
	 *
	 * @param s
	 *            the s
	 * @return the gama date
	 */
	public static GamaDate fromISOString(final String s) {
		try {
			final TemporalAccessor t = Dates.getFormatter(Dates.ISO_OFFSET_KEY, null).parse(s);
			if (t instanceof Temporal tmp) return fromTemporal(tmp);
		} catch (final DateTimeParseException e) {
			//
		}
		return new GamaDate(null, s);
	}

	/**
	 * Of.
	 *
	 * @param t
	 *            the t
	 * @return the gama date
	 */
	public static GamaDate fromTemporal(final Temporal t) {
		return new GamaDate(t);
	}

}

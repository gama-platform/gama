/*******************************************************************************************************
 *
 * GeoJSONUtil.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.time.FastDateFormat;
import org.geotools.util.Converters;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Coordinate;

/**
 * The Class GeoJSONUtil.
 */
public class GeoJSONUtil {

	/** Date format (ISO 8601) */
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	/** The Constant TIME_ZONE. */
	public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");

	/** The Constant dateFormatter. */
	public static final FastDateFormat dateFormatter = FastDateFormat.getInstance(DATE_FORMAT, TIME_ZONE);

	//
	// io
	//

	/**
	 * Converts an object to a {@link Reader} instance.
	 *
	 * <p>
	 * The <tt>input</tt> parameter may be one of the following types:
	 *
	 * <ul>
	 * <li>{@link Reader}
	 * <li>{@link InputStream}
	 * <li>{@link File}
	 * <li>{@link String} *
	 * </ul>
	 *
	 * * A string parameter is considered a file path.
	 *
	 * @param input
	 *            The input object.
	 * @return A reader.
	 */
	public static Reader toReader(final Object input) throws IOException {
		if (input instanceof BufferedReader) return (BufferedReader) input;

		if (input instanceof Reader) return new BufferedReader((Reader) input);

		if (input instanceof InputStream) return new BufferedReader(new InputStreamReader((InputStream) input));

		if (input instanceof File) return new BufferedReader(new FileReader((File) input));

		if (input instanceof String) return new StringReader((String) input);

		throw new IllegalArgumentException("Unable to turn " + input + " into a reader");
	}

	/**
	 * Converts an object to {@link Writer} instance.
	 *
	 * <p>
	 * The <tt>output</tt> parameter may be one of the following types:
	 *
	 * <ul>
	 * <li>{@link Writer}
	 * <li>{@link OutputStream}
	 * <li>{@link File}
	 * <li>{@link String} *
	 * </ul>
	 *
	 * * A string parameter is considered a file path.
	 *
	 * @param output
	 *            The output object.
	 * @return A writer.
	 */
	public static Writer toWriter(final Object output) throws IOException {
		// If the user passed in an OutputStreamWriter, we'll trust them to close it themselves.
		if (output instanceof OutputStreamWriter) return new Writer() {
			Writer writer = new BufferedWriter((Writer) output);

			@Override
			public void write(final char[] cbuf, final int off, final int len) throws IOException {
				writer.write(cbuf, off, len);
			}

			@Override
			public void flush() throws IOException {
				writer.flush();
			}

			@Override
			public void close() throws IOException {}
		};

		if (output instanceof BufferedWriter) return (BufferedWriter) output;

		if (output instanceof Writer) return new BufferedWriter((Writer) output);

		if (output instanceof OutputStream) return new BufferedWriter(new OutputStreamWriter((OutputStream) output));

		if (output instanceof File) return new BufferedWriter(new FileWriter((File) output));

		if (output instanceof String) return new BufferedWriter(new FileWriter((String) output));

		throw new IllegalArgumentException("Unable to turn " + output + " into a writer");
	}

	//
	// encoding
	/**
	 * String.
	 *
	 * @param string
	 *            the string
	 * @param sb
	 *            the sb
	 * @return the string builder
	 */
	//
	public static StringBuilder string(final String string, final StringBuilder sb) {
		sb.append("\"").append(JSONObject.escape(string)).append("\"");
		return sb;
	}

	/**
	 * Entry.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param sb
	 *            the sb
	 * @return the string builder
	 */
	public static StringBuilder entry(final String key, final Object value, final StringBuilder sb) {

		string(key, sb).append(":");

		value(value, sb);
		return sb;
	}

	/**
	 * Value.
	 *
	 * @param value
	 *            the value
	 * @param sb
	 *            the sb
	 */
	private static void value(final Object value, final StringBuilder sb) {
		if (value == null) {
			nul(sb);
		} else if (value.getClass().isArray()) {
			array(value, sb);
		} else if (value instanceof Number || value instanceof Boolean || value instanceof Date) {
			literal(value, sb);
		} else {
			String str = Converters.convert(value, String.class);
			if (str == null) { str = value.toString(); }
			string(str, sb);
		}
	}

	/**
	 * Array.
	 *
	 * @param array
	 *            the array
	 * @param sb
	 *            the sb
	 */
	private static void array(final Object array, final StringBuilder sb) {
		sb.append("[");
		int length = Array.getLength(array);
		for (int i = 0; i < length; i++) {
			Object value = Array.get(array, i);
			value(value, sb);
			if (i < length - 1) { sb.append(", "); }
		}
		sb.append("]");
	}

	/**
	 * Literal.
	 *
	 * @param value
	 *            the value
	 * @param sb
	 *            the sb
	 * @return the string builder
	 */
	static StringBuilder literal(final Object value, final StringBuilder sb) {
		// handle date as special case special case
		if (value instanceof Date) return string(dateFormatter.format((Date) value), sb);

		return sb.append(value);
	}

	/**
	 * Array.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param sb
	 *            the sb
	 * @return the string builder
	 */
	public static StringBuilder array(final String key, final Object value, final StringBuilder sb) {
		return string(key, sb).append(":").append(value);
	}

	/**
	 * Nul.
	 *
	 * @param sb
	 *            the sb
	 * @return the string builder
	 */
	public static StringBuilder nul(final StringBuilder sb) {
		sb.append("null");
		return sb;
	}

	//
	// parsing
	/**
	 * Trace.
	 *
	 * @param <T>
	 *            the generic type
	 * @param handler
	 *            the handler
	 * @param clazz
	 *            the clazz
	 * @return the t
	 */
	//
	@SuppressWarnings ("unchecked")
	public static <T> T trace(final T handler, final Class<T> clazz) {
		return (T) Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[] { clazz },
				new TracingHandler(handler));
	}

	/**
	 * Adds the ordinate.
	 *
	 * @param ordinates
	 *            the ordinates
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	public static boolean addOrdinate(final List<Object> ordinates, final Object value) {
		if (ordinates != null) { ordinates.add(value); }

		return true;
	}

	/**
	 * Creates the coordinate.
	 *
	 * @param ordinates
	 *            the ordinates
	 * @return the coordinate
	 * @throws ParseException
	 *             the parse exception
	 */
	public static Coordinate createCoordinate(final List ordinates) throws ParseException {
		Coordinate c = new Coordinate();
		if (ordinates.size() <= 1) throw new ParseException(ParseException.ERROR_UNEXPECTED_EXCEPTION,
				"Too few ordinates to create coordinate");
		if (ordinates.size() > 1) {
			c.x = ((Number) ordinates.get(0)).doubleValue();
			c.y = ((Number) ordinates.get(1)).doubleValue();
		}
		if (ordinates.size() > 2) { c.setZ(((Number) ordinates.get(2)).doubleValue()); }
		return c;
	}

	/**
	 * Creates the coordinates.
	 *
	 * @param coordinates
	 *            the coordinates
	 * @return the coordinate[]
	 */
	public static Coordinate[] createCoordinates(final List<Coordinate> coordinates) {
		return coordinates.toArray(new Coordinate[coordinates.size()]);
	}

	/**
	 * Parses the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param handler
	 *            the handler
	 * @param input
	 *            the input
	 * @param trace
	 *            the trace
	 * @return the t
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings ("unchecked")
	public static <T> T parse(IContentHandler<T> handler, final Object input, final boolean trace) throws IOException {
		try (Reader reader = toReader(input)) {
			if (trace) {
				handler = (IContentHandler<T>) Proxy.newProxyInstance(handler.getClass().getClassLoader(),
						new Class[] { IContentHandler.class }, new TracingHandler(handler));
			}

			JSONParser parser = new JSONParser();
			try {
				parser.parse(reader, handler);
				return handler.getValue();
			} catch (ParseException e) {
				throw (IOException) new IOException().initCause(e);
			}
		}
	}

	/**
	 * Encode.
	 *
	 * @param json
	 *            the json
	 * @param output
	 *            the output
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void encode(final String json, final Object output) throws IOException {
		try (Writer w = toWriter(output)) {
			w.write(json);
			w.flush();
		}
	}

	/**
	 * Encode.
	 *
	 * @param obj
	 *            the obj
	 * @param output
	 *            the output
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void encode(final Map<String, Object> obj, final Object output) throws IOException {
		try (Writer w = toWriter(output)) {
			JSONObject.writeJSONString(obj, w);
			w.flush();
		}
	}
}

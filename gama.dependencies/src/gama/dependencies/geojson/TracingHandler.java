/*******************************************************************************************************
 *
 * TracingHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.json.simple.parser.ParseException;

/**
 * The Class TracingHandler.
 */
public class TracingHandler implements InvocationHandler {

	/** The indent. */
	int indent = 0;

	/** The delegate. */
	Object delegate;

	/**
	 * Instantiates a new tracing handler.
	 *
	 * @param delegate
	 *            the delegate
	 */
	public TracingHandler(final Object delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if ("startObject".equals(method.getName())) {
			startObject();
		} else if ("endObject".equals(method.getName())) {
			endObject();
		} else if ("startObjectEntry".equals(method.getName())) {
			startObjectEntry((String) args[0]);
		} else if ("endObjectEntry".equals(method.getName())) {
			endObjectEntry();
		} else if ("startArray".equals(method.getName())) {
			startArray();
		} else if ("endArray".equals(method.getName())) {
			endArray();
		} else if ("primitive".equals(method.getName())) { primitive(args[0]); }

		StringBuilder sb = new StringBuilder();
		sb.append(method.getName());
		if (args != null && args.length > 0) {
			sb.append("[");
			for (Object arg : args) { sb.append(arg).append(","); }
			sb.setLength(sb.length() - 1);
			sb.append("]");
		}
		// System.out.println(sb.toString());
		return method.invoke(delegate, args);
	}

	/**
	 * Start JSON.
	 *
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void startJSON() {}

	/**
	 * End JSON.
	 *
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void endJSON() {}

	/**
	 * Start object.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	boolean startObject() {
		// indent();
		System.out.println("{");
		indent++;
		return true;
	}

	/**
	 * End object.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	boolean endObject() {
		indent--;
		indent();
		System.out.print("}");
		return true;
	}

	/**
	 * Start object entry.
	 *
	 * @param key
	 *            the key
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	boolean startObjectEntry(final String key) {
		indent();
		System.out.print(key + ": ");
		return true;
	}

	/**
	 * End object entry.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	boolean endObjectEntry() {
		System.out.println(",");
		return true;
	}

	/**
	 * Start array.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	boolean startArray() {
		// indent();
		System.out.print("[");
		return true;
	}

	/**
	 * End array.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	boolean endArray() {
		// indent();
		System.out.print("]");
		return true;
	}

	/**
	 * Primitive.
	 *
	 * @param value
	 *            the value
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	boolean primitive(final Object value) {
		System.out.print(value);
		return true;
	}

	/**
	 * Indent.
	 */
	void indent() {
		for (int i = 0; i < indent; i++) { System.out.print("  "); }
	}
}

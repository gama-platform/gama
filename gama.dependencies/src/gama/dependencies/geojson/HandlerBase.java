/*******************************************************************************************************
 *
 * HandlerBase.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson;

import java.io.IOException;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

/**
 * The Class HandlerBase.
 */
public class HandlerBase implements ContentHandler {

	@Override
	public void startJSON() throws ParseException, IOException {}

	@Override
	public void endJSON() throws ParseException, IOException {}

	@Override
	public boolean startObject() throws ParseException, IOException {
		return true;
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		return true;
	}

	@Override
	public boolean startObjectEntry(final String key) throws ParseException, IOException {
		return true;
	}

	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		return true;
	}

	@Override
	public boolean startArray() throws ParseException, IOException {
		return true;
	}

	@Override
	public boolean endArray() throws ParseException, IOException {
		return true;
	}

	@Override
	public boolean primitive(final Object value) throws ParseException, IOException {
		return true;
	}
}

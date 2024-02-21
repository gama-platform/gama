/*******************************************************************************************************
 *
 * FilterExample.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.kabeja.parser.dxf.filter;

import java.util.HashMap;
import java.util.Map;

import gama.dependencies.kabeja.parser.DXFParser;
import gama.dependencies.kabeja.parser.ParserBuilder;

/**
 * The Class FilterExample.
 */
public class FilterExample {
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			DXFParser parser = (DXFParser) ParserBuilder.createDefaultParser();

			// test
			DXFStreamFilter filter = new DXFStreamLayerFilter();
			Map<String, String> p = new HashMap<>();
			p.put("layers.include", args[0]);
			filter.setProperties(p);
			parser.addDXFStreamFilter(filter);
			parser.parse(args[1]);

			// DXFDocument doc = parser.getDocument();

			// do something with the doc
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

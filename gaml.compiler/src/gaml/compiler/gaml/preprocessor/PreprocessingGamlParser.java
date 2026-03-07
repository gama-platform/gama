/*******************************************************************************************************
 *
 * PreprocessingGamlParser.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.preprocessor;

import java.io.Reader;

import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.ParseException;

import gama.dev.DEBUG;
import gaml.compiler.parser.antlr.GamlParser;

/**
 * The Class PreprocessingGamlParser.
 */
public class PreprocessingGamlParser extends GamlParser {

	static {
		DEBUG.ON();
	}

	@Override
	public IParseResult doParse(final Reader reader) throws ParseException {
		return super.doParse(reader);
		// try {
		// GamlResourceOffsetMap offsetMap = null;
		// URI uri = null;
		// // 1. Read the original file
		// if (reader instanceof GamlResourceReader gamlReader) {
		// offsetMap = gamlReader.getOffsetMap();
		// uri = gamlReader.getURI();
		// }
		//
		// DEBUG.OUT("Preprocessor called !");
		// String result = CharStreams.toString(reader);
		//
		// // 2. Run the tagging preprocessor
		// GamlPreprocessor preprocessor = new GamlPreprocessor(offsetMap);
		//
		// result = preprocessor.process(result);
		//
		// // 3. Parse the TAGGED text
		// Reader taggedReader = new StringReader(result);
		//
		// IParseResult parseResult = super.doParse(taggedReader);
		//
		// return parseResult;
		//
		// } catch (Exception e) {
		// throw new ParseException(e.getMessage(), e);
		// }
	}
}
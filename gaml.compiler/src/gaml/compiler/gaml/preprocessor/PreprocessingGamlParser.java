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
import java.io.StringReader;

import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.ParseException;

import com.google.common.io.CharStreams;

import gama.dev.DEBUG;
import gaml.compiler.gaml.resource.GamlResourceReader;
import gaml.compiler.parser.antlr.GamlParser;

/**
 * The Class PreprocessingGamlParser.
 */
public class PreprocessingGamlParser extends GamlParser {

	static {
		DEBUG.OFF();
	}

	@Override
	public IParseResult doParse(final Reader reader) throws ParseException {
		try {
			GamlResourceOffsetMap offsetMap = null;
			// 1. Read the original file
			String result = CharStreams.toString(reader);
			if (reader instanceof GamlResourceReader gamlReader) { offsetMap = gamlReader.getOffsetMap(); }

			DEBUG.OUT("Preprocessor called !");

			// 2. Run the tagging preprocessor
			GamlPreprocessor preprocessor = new GamlPreprocessor(offsetMap);
			// String result = preprocessor.process(rawText);

			// 3. Parse the TAGGED text
			Reader taggedReader = new StringReader(result);
			IParseResult parseResult = super.doParse(taggedReader);

			return parseResult;

		} catch (Exception e) {
			throw new ParseException(e.getMessage(), e);
		}
	}
}
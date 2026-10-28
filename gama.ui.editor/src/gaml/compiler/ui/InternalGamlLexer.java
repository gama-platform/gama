/*******************************************************************************************************
 *
 * InternalGamlLexer.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.Lexer;

/** Adapts the generated IDE lexer to the UI lexer API, sharing the content-assist parser's token numbers. */
public class InternalGamlLexer extends Lexer {

	private gaml.compiler.ide.contentassist.antlr.internal.InternalGamlLexer delegate;

	public InternalGamlLexer() {}

	public InternalGamlLexer(final CharStream input) {
		this(input, new RecognizerSharedState());
	}

	public InternalGamlLexer(final CharStream input, final RecognizerSharedState state) {
		super(input, state);
		delegate = new gaml.compiler.ide.contentassist.antlr.internal.InternalGamlLexer(input, state);
	}

	@Override
	public void setCharStream(final CharStream input) {
		super.setCharStream(input);
		delegate = new gaml.compiler.ide.contentassist.antlr.internal.InternalGamlLexer(input, state);
	}

	@Override
	public void mTokens() throws RecognitionException {
		if (delegate == null) {
			delegate = new gaml.compiler.ide.contentassist.antlr.internal.InternalGamlLexer(input, state);
		}
		delegate.mTokens();
	}
}

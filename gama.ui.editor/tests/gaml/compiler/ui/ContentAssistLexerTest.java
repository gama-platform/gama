package gaml.compiler.ui;

import java.util.Objects;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;

/** Standalone regression check that UI completion uses the generated parser's token vocabulary. */
public final class ContentAssistLexerTest {

	public static void main(final String[] args) {
		final var ui = new InternalGamlLexer();
		for (final String source : new String[] {
				"@no_warning model folding global { int acc <- [1,2] reduce (0, @ + each); }",
				"[[1,2],[3,4]] reduce (0, @ + (each reduce (@ * each)))",
				"[1,2,3] scan (0, @ + each)",
				"[[1,2],[3,4]] scan (0, @ + (each reduce (@ + each)))",
				"1 #m 5 % each self nil true 1.5 // comment\n/* block */ 'text'",
				"[1,2] collect (x: x + 1)" }) {
			ui.setCharStream(new ANTLRStringStream(source));
			final var generated = new gaml.compiler.ide.contentassist.antlr.internal.InternalGamlLexer(
					new ANTLRStringStream(source));
			while (true) {
				final Token actual = ui.nextToken();
				final Token expected = generated.nextToken();
				if (actual.getType() != expected.getType() || !Objects.equals(actual.getText(), expected.getText())
						|| actual.getChannel() != expected.getChannel() || actual.getLine() != expected.getLine()
						|| actual.getCharPositionInLine() != expected.getCharPositionInLine()) {
					throw new AssertionError("Mismatched UI completion token in: " + source);
				}
				if (actual.getType() == Token.EOF) break;
			}
		}
		System.out.println("ContentAssistLexerTest: all token streams match");
	}
}

/*******************************************************************************************************
 *
 * GamlSyntaxErrorMessageProvider.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.parsing;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.diagnostics.Diagnostic;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.SyntaxErrorMessage;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.antlr.SyntaxErrorMessageProvider;

import gama.annotations.constants.IKeyword;
import gaml.compiler.EGaml;

/**
 * The class GamlSyntaxErrorMessageProvider.
 *
 * @author drogoul
 * @since 15 sept. 2013
 *
 */
public class GamlSyntaxErrorMessageProvider extends SyntaxErrorMessageProvider {

	// Eclipse marker attributes cannot exceed 65,535 UTF-8 bytes.
	private static final int MAX_MESSAGE_LENGTH = 1000;
	private static final int MAX_SOURCE_LENGTH = 120;
	private static final String INCOMPLETE_TOKEN_PREFIX =
			"required (...)+ loop did not match anything at character ";

	@Override
	public SyntaxErrorMessage getSyntaxErrorMessage(final IParserErrorContext context) {
		final EObject contextobj = context.getCurrentContext();
		final RecognitionException ex = context.getRecognitionException();

		StringBuilder msg = new StringBuilder();
		final INode node = context.getCurrentNode();
		if (ex == null) {
			msg.append(translateMessage(contextobj, context.getDefaultMessage(), node));
		} else {
			final Token t = ex.token;
			if (t != null) {
				final String token = t.getText();
				if (token != null) {
					if (token.length() == 1) {
						process1CharToken(msg, token);
					} else {
						String text;
						if (contextobj != null) {
							text = EGaml.getInstance().getKeyOf(contextobj);
							if (text == null) { text = token; }
							msg.append("Symbol '").append(abbreviate(text, MAX_SOURCE_LENGTH))
									.append("' seems to be incomplete or misplaced");
						} else {
							switch (token) {
								case IKeyword.ENVIRONMENT -> msg.append(
										"'environment' cannot be declared anymore.Its bounds should be declared in the global section as the value of the 'shape' attribute (since GAMA 1.7)");
								case IKeyword.ENTITIES -> msg.append(
										"'entities' cannot be declared anymore. The species it contains should be declared in the model or in the global section (since GAMA 1.6)");
								default -> msg.append("Unexpected symbol '").append(abbreviate(token, MAX_SOURCE_LENGTH))
										.append("'");
							}

						}
					}
				} else {
					// cf. Issue #3003. Some errors do not have token text.
					msg.append(translateMessage(contextobj, context.getDefaultMessage(), node));
				}
			}
		}
		if (msg.isEmpty()) { msg.append(context.getDefaultMessage()); }
		return new SyntaxErrorMessage(abbreviate(msg.toString(), MAX_MESSAGE_LENGTH), Diagnostic.SYNTAX_DIAGNOSTIC);
	}

	/**
	 * Process 1 char token.
	 *
	 * @param msg
	 *            the msg
	 * @param token
	 *            the token
	 */
	private void process1CharToken(final StringBuilder msg, final String token) {
		final char c = token.charAt(0);
		switch (c) {
			case ';' -> msg.append("Unexpected line termination character ' ").append(token).append(" '");
			case '{', '}' -> msg.append("Block definition does not begin or end correctly");
			case '[', ']' -> msg.append("List definition does not begin or end correctly");
			case '(', ')' -> msg.append("Parenthesized expression do not begin or end correctly");
			default -> msg.append("Unwanted or misplaced character ' ").append(token).append(" '");
		}

	}

	/**
	 * Translate message.
	 *
	 * @param contextobj
	 *            the contextobj
	 * @param message
	 *            the message
	 * @param node
	 *            the node
	 * @return the string
	 */
	private String translateMessage(final EObject contextobj, final String message, final INode node) {
		String msg = abbreviate(message, MAX_MESSAGE_LENGTH);
		if (msg.startsWith("mismatched ch")) {
			final String ch = msg.substring(msg.lastIndexOf(' '));
			msg = "Character expected " + ch;
		} else if (msg.startsWith("mismatched in")) {
			msg = msg.replace("mismatched input", "Found");
		} else if (msg.startsWith(INCOMPLETE_TOKEN_PREFIX)) {
			msg = "Incomplete or invalid token before " + msg.substring(INCOMPLETE_TOKEN_PREFIX.length());
		} else {
			final INode contextNode = contextobj == null ? node : NodeModelUtils.getNode(contextobj);
			// A large context is often an entire block, not the source of the error.
			if (contextNode != null && contextNode.getTotalLength() <= MAX_SOURCE_LENGTH) {
				final String text = NodeModelUtils.getTokenText(contextNode);
				if (!text.isBlank()) { msg += " (near '" + text + "')"; }
			}
		}
		return msg;
	}

	private static String abbreviate(final String text, final int maxLength) {
		if (text.length() <= maxLength) return text;
		int end = maxLength - 3;
		if (Character.isHighSurrogate(text.charAt(end - 1)) && Character.isLowSurrogate(text.charAt(end))) { end--; }
		return text.substring(0, end) + "...";
	}

}
/*******************************************************************************************************
 *
 * JsonDocumentPartitionScanner.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.document;

import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.BOOLEAN;
import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.COMMENT;
import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.KEY;
import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.NULL;
import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.STRING;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

/**
 * The Class JsonDocumentPartitionScanner.
 */
public class JsonDocumentPartitionScanner extends RuleBasedPartitionScanner {

	/** The only letters key word detector. */
	private final OnlyLettersKeyWordDetector onlyLettersKeyWordDetector = new OnlyLettersKeyWordDetector();

	/**
	 * Gets the offset.
	 *
	 * @return the offset
	 */
	public int getOffset() { return fOffset; }

	/**
	 * Instantiates a new highspeed JSON document partition scanner.
	 */
	public JsonDocumentPartitionScanner() {
		IToken comment = createToken(COMMENT);
		IToken doubleString = createToken(STRING);
		IToken nullValue = createToken(NULL);
		IToken key = createToken(KEY);
		IToken bool = createToken(BOOLEAN);

		List<IPredicateRule> rules = new ArrayList<>();
		rules.add(new JSONKeyRule(key));
		rules.add(new SingleLineRule("\"", "\"", doubleString, '\\', true));
		rules.add(new SingleLineRule("//", "", comment, (char) -1, true));
		rules.add(new MultiLineRule("/*", "*/", comment, (char) -1, true));
		rules.add(new WordPatternRule(onlyLettersKeyWordDetector, "false", "", bool));
		rules.add(new WordPatternRule(onlyLettersKeyWordDetector, "true", "", bool));
		rules.add(new WordPatternRule(onlyLettersKeyWordDetector, "null", "", nullValue));

		setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
	}

	/**
	 * Creates the token.
	 *
	 * @param identifier
	 *            the identifier
	 * @return the i token
	 */
	private IToken createToken(final JsonDocumentIdentifier identifier) {
		return new Token(identifier.getId());
	}
}

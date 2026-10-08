/*
 * Copyright 2021 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */
package gama.ui.viewers.json.eclipse.document;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 *
 * When we have <code>
 * {
 *  "key1" : "value1"
 *  }
 *  </code> This rule shall accept "key1" but not "value1". So
 *
 * @author albert
 *
 */
public class JSONKeyRule implements IPredicateRule {

	/** The token. */
	private final IToken token;

	/** The trace. */
	boolean trace;

	/**
	 * Instantiates a new JSON key rule.
	 *
	 * @param token
	 *            the token
	 */
	public JSONKeyRule(final IToken token) {
		this.token = token;
	}

	@Override
	public IToken evaluate(final ICharacterScanner scanner) {
		return evaluate(scanner, false);
	}

	@Override
	public IToken getSuccessToken() { return token; }

	@Override
	public IToken evaluate(final ICharacterScanner scanner, final boolean resume) {
		ICharacterScannerCodePosSupport support = new ICharacterScannerCodePosSupport(scanner);
		int pos = support.getInitialStartPos();

		// we scan for two ", followed by whitespace (optional) and then a colon :
		Character c = null;
		int countExlamationMarks = 0;
		boolean foundColon = false;
		boolean cancelSearch = false;
		while (!cancelSearch) {
			c = support.getCharacterAtPosOrNull(pos++);
			if (c == null) { break; }
			char cv = c.charValue();
			if (countExlamationMarks == 0) {
				if (cv != '"') {
					// first char is not " so not a key - fast fail
					break;
				}
				countExlamationMarks++;
			} else if (countExlamationMarks == 1) {
				if (cv == '"') { countExlamationMarks++; }
			} else if (countExlamationMarks == 2) {
				if (Character.isWhitespace(cv)) {
					/* whitespaces are accepted */
					continue;
				}
				cancelSearch = true;
				if (cv == ':') { foundColon = true; }
			} else {
				cancelSearch = true;
			}
		}
		if (foundColon) return token;
		support.resetToStartPos();
		return Token.UNDEFINED;

	}

}
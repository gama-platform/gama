/*******************************************************************************************************
 *
 * ExtractActionHandler.java, in gama.ui.editor, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor.refactoring;

import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import gaml.compiler.ui.editor.GamlEditor;

/**
 * Handles the "Extract Action" refactoring for GAML code. When the user selects a block of statements
 * inside a species, global, or experiment block and invokes this handler, it:
 * <ol>
 * <li>Asks for a name (and optional return type) via {@link ExtractActionDialog}.</li>
 * <li>Locates the enclosing species / global / experiment block.</li>
 * <li>Inserts a new action declaration containing the selected code just before the closing
 * {@code }} of that block.</li>
 * <li>Replaces the original selection with a {@code do <name>;} call.</li>
 * </ol>
 *
 * <p>
 * Variable analysis (automatic parameter detection) is not yet performed. The generated action will
 * have an empty parameter list; the user can add parameters manually if required.
 * </p>
 */
public class ExtractActionHandler {

	/** Keywords that introduce a top-level GAML block capable of containing action declarations. */
	private static final Set<String> ENCLOSING_KEYWORDS =
			Set.of("species", "global", "experiment", "grid", "class", "skill");

	/**
	 * Entry point called from the editor context menu. Performs the full extract-action workflow.
	 *
	 * @param editor
	 *            the active GAML editor
	 */
	public static void extractAction(final GamlEditor editor) {
		final ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		if (selection == null || selection.getLength() == 0) {
			MessageDialog.openInformation(editor.getEditorSite().getShell(), "Extract Action",
					"Please select the code you want to extract into an action.");
			return;
		}

		final Shell shell = editor.getEditorSite().getShell();
		final ExtractActionDialog dialog = new ExtractActionDialog(shell);
		if (dialog.open() != Window.OK) return;

		final String actionName = dialog.getActionName();
		final String returnType = dialog.getReturnType();

		final IDocument doc = editor.getInternalSourceViewer().getDocument();
		final int selOffset = selection.getOffset();
		final int selLength = selection.getLength();

		String fullText;
		try {
			fullText = doc.get();
		} catch (final Exception e) {
			showError(shell, "Could not read the document content.");
			return;
		}

		final String selectedText;
		try {
			selectedText = doc.get(selOffset, selLength);
		} catch (final BadLocationException e) {
			showError(shell, "Could not read the selected text.");
			return;
		}

		// Find the enclosing species / global / experiment block.
		final int[] enclosingBlock = findEnclosingBlock(fullText, selOffset);
		if (enclosingBlock == null) {
			MessageDialog.openWarning(shell, "Extract Action",
					"Cannot extract: the selected code is not inside a species, global, or experiment block.");
			return;
		}

		final int blockOpenBrace = enclosingBlock[0]; // offset of the opening '{'
		final int blockCloseBrace = enclosingBlock[1]; // offset of the closing '}'

		// Determine indentation for the action header (same level as other statements in the block).
		final String headerIndent = detectHeaderIndent(fullText, blockOpenBrace);

		// Generate the new action declaration.
		final String actionDecl = generateActionDeclaration(actionName, returnType, selectedText, headerIndent);

		// Determine the indentation for the replacement call (`do name;`).
		final String callIndent = detectCallIndent(fullText, selOffset);
		final String actionCall = callIndent + "do " + actionName + ";";

		// Apply both document changes.
		// Edits are applied in *reverse* document order (highest offset first) so that the first
		// edit (at blockCloseBrace) does not shift the offset of the second edit (at selOffset).
		// This is safe because blockCloseBrace > selOffset + selLength in the normal case,
		// i.e. positions below blockCloseBrace are unaffected by the insertion.
		try {
			if (blockCloseBrace > selOffset + selLength) {
				// Normal case: block end is after the selection — insert action first (higher
				// offset), then replace the selection (lower offset, unaffected by first edit).
				doc.replace(blockCloseBrace, 0, actionDecl);
				doc.replace(selOffset, selLength, actionCall);
			} else {
				// Fallback: replace selection first, then adjust blockCloseBrace for any length
				// change caused by that replacement, and insert action at the adjusted position.
				final int lengthDelta = actionCall.length() - selLength;
				doc.replace(selOffset, selLength, actionCall);
				doc.replace(blockCloseBrace + lengthDelta, 0, actionDecl);
			}
		} catch (final BadLocationException e) {
			showError(shell, "An error occurred while modifying the document: " + e.getMessage());
		}
	}

	// -------------------------------------------------------------------------
	// Block-finding logic (text-based)
	// -------------------------------------------------------------------------

	/**
	 * Scans backward from {@code fromOffset} to find the innermost enclosing block that belongs to a
	 * {@code species}, {@code global}, {@code experiment}, {@code grid}, {@code class}, or
	 * {@code skill} declaration.
	 *
	 * @param text
	 *            the full document text
	 * @param fromOffset
	 *            the start of the selection
	 * @return a two-element array {@code {openBraceOffset, closeBraceOffset}}, or {@code null} if no
	 *         suitable enclosing block was found
	 */
	static int[] findEnclosingBlock(final String text, final int fromOffset) {
		int depth = 0;
		int i = fromOffset - 1;

		while (i >= 0) {
			final char c = text.charAt(i);

			// Skip double-quoted string literals when scanning backward to avoid false
			// brace matches inside strings such as write "hello {world}";
			if (c == '"') {
				i = skipStringBackward(text, i - 1);
				continue;
			}

			// Skip single-line comments when scanning backward.
			// A '//' comment occupies the text from '//' to the preceding newline.
			// When scanning backward, the rightmost '/' of '//' is encountered first (at i);
			// its left neighbour text.charAt(i-1) is the leftmost '/' -- together they form '//'.
			if (c == '/' && i > 0 && text.charAt(i - 1) == '/') {
				// Walk back past both '/' characters and then to the preceding newline.
				i -= 2;
				while (i >= 0 && text.charAt(i) != '\n') { i--; }
				continue;
			}

			if (c == '}') {
				depth++;
			} else if (c == '{') {
				depth--;
				if (depth < 0) {
					// This '{' is an enclosing brace.  Inspect the keyword that precedes it.
					final String keyword = findKeywordBefore(text, i);
					if (ENCLOSING_KEYWORDS.contains(keyword)) {
						// Found the target block.
						final int closeBrace = findMatchingCloseBrace(text, i);
						if (closeBrace >= 0) return new int[] { i, closeBrace };
					}
					// Not the right kind of block — treat this '{' as transparent and
					// continue the scan one level further out.
					depth = 0;
				}
			}

			i--;
		}

		return null; // no suitable enclosing block found
	}

	/**
	 * Scans backward from {@code quotePos} (the position just before a closing {@code "}) to find the
	 * opening {@code "} of the string literal and returns the position just before it.
	 */
	private static int skipStringBackward(final String text, int pos) {
		while (pos >= 0) {
			final char c = text.charAt(pos);
			if (c == '"' && isUnescapedAt(text, pos)) {
				return pos - 1; // position just before the opening '"'
			}
			pos--;
		}
		return -1;
	}

	/**
	 * Returns {@code true} if the character at {@code pos} is not preceded by an odd number of
	 * consecutive backslashes (i.e. it is not escaped).
	 */
	private static boolean isUnescapedAt(final String text, final int pos) {
		int backslashes = 0;
		int k = pos - 1;
		while (k >= 0 && text.charAt(k) == '\\') {
			backslashes++;
			k--;
		}
		return (backslashes % 2) == 0;
	}

	/**
	 * Extracts the GAML keyword that begins the declaration containing the opening {@code '{'} at
	 * {@code braceOffset}. In standard GAML style the keyword ({@code species}, {@code global},
	 * {@code experiment}, etc.) is always the <em>first identifier</em> on its line. This method
	 * therefore finds the line that contains the {@code '{'}, extracts the first identifier on it, and
	 * returns that. If the {@code '{'} is on a line by itself (an unusual but legal style), the method
	 * walks backward to the nearest preceding line that starts with an identifier.
	 *
	 * @param text
	 *            the full document text
	 * @param braceOffset
	 *            the offset of the opening {@code \{}
	 * @return the first identifier on the declaration line, or an empty string if none could be found
	 */
	private static String findKeywordBefore(final String text, final int braceOffset) {
		// Locate the start of the line containing '{'.
		int lineStart = braceOffset;
		while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') { lineStart--; }

		// Walk backward through lines until we find one whose first non-whitespace character is
		// an identifier (i.e. the declaration line).
		while (true) {
			int i = lineStart;
			// Skip leading whitespace on this line.
			while (i < braceOffset && Character.isWhitespace(text.charAt(i))) { i++; }

			if (i < braceOffset && isIdentChar(text.charAt(i))) {
				// First non-whitespace char before '{' on this line is an identifier — extract it.
				// The upper bound uses braceOffset (the '{' position) so we never scan past the
				// opening brace. Since '{' is not an identifier character the loop would stop
				// there anyway, but the explicit bound makes the intent clear.
				final int identStart = i;
				while (i < braceOffset && isIdentChar(text.charAt(i))) { i++; }
				return text.substring(identStart, i);
			}

			// This line has nothing useful before the '{'; step to the preceding line.
			if (lineStart == 0) break;
			int prevLineEnd = lineStart - 1; // the '\n' terminating the preceding line
			lineStart = prevLineEnd;
			while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') { lineStart--; }
		}

		return "";
	}

	/** Returns {@code true} if {@code c} can appear in a GAML identifier. */
	private static boolean isIdentChar(final char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

	/**
	 * Finds the closing {@code \}} that matches the opening {@code \{} at {@code openBraceOffset},
	 * skipping nested braces and string literals.
	 */
	private static int findMatchingCloseBrace(final String text, final int openBraceOffset) {
		int depth = 1;
		int i = openBraceOffset + 1;
		while (i < text.length()) {
			final char c = text.charAt(i);
			if (c == '"') {
				// Skip string literal forward, respecting escaped quotes.
				i++;
				while (i < text.length()) {
					final char sc = text.charAt(i);
					if (sc == '"' && isUnescapedAt(text, i)) break;
					i++;
				}
			} else if (c == '{') {
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0) return i;
			}
			i++;
		}
		return -1;
	}

	// -------------------------------------------------------------------------
	// Indentation helpers
	// -------------------------------------------------------------------------

	/**
	 * Detects the indentation string used for top-level statements inside the enclosing block. It does
	 * this by looking at the first non-blank, non-closing-brace line after the opening {@code \{}.
	 *
	 * @param text
	 *            the full document text
	 * @param blockOpenBrace
	 *            the offset of the block's opening {@code \{}
	 * @return the leading whitespace of the first statement inside the block, or {@code "    "} (4
	 *         spaces) as a fallback
	 */
	private static String detectHeaderIndent(final String text, final int blockOpenBrace) {
		int i = blockOpenBrace + 1;
		while (i < text.length()) {
			if (text.charAt(i) == '\n') {
				final int lineStart = i + 1;
				int j = lineStart;
				// Scan to the end of leading whitespace on this line.
				while (j < text.length() && text.charAt(j) != '\n' && Character.isWhitespace(text.charAt(j))) {
					j++;
				}
				// Accept this line only if it contains non-whitespace content and is not just '}'.
				if (j < text.length() && text.charAt(j) != '\n' && text.charAt(j) != '}') {
					return text.substring(lineStart, j);
				}
			}
			i++;
		}
		return "    "; // fallback
	}

	/**
	 * Detects the leading whitespace of the line that contains the selection start, to use as the
	 * indentation prefix for the {@code do name;} replacement call.
	 *
	 * @param text
	 *            the full document text
	 * @param selOffset
	 *            the start offset of the selection
	 * @return the leading whitespace of that line
	 */
	private static String detectCallIndent(final String text, final int selOffset) {
		// Walk back to the start of the line.
		int lineStart = selOffset;
		while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') { lineStart--; }
		// Collect leading whitespace.
		int j = lineStart;
		while (j < text.length() && j < selOffset && Character.isWhitespace(text.charAt(j))) { j++; }
		return text.substring(lineStart, j);
	}

	// -------------------------------------------------------------------------
	// Code generation
	// -------------------------------------------------------------------------

	/**
	 * Generates the action declaration text to be inserted before the enclosing block's closing
	 * {@code }}.
	 *
	 * <p>
	 * The generated code follows the GAML syntax:
	 *
	 * <pre>
	 * {@code
	 * // for a void action (returnType == "action"):
	 * action my_action () {
	 *     <selected code>
	 * }
	 *
	 * // for a typed action (returnType == "int", "float", etc.):
	 * int my_action () {
	 *     <selected code>
	 * }
	 * }
	 * </pre>
	 *
	 * @param actionName
	 *            the name of the new action
	 * @param returnType
	 *            the return type ("action" for void, or a GAML type name)
	 * @param body
	 *            the selected code that will form the action body
	 * @param headerIndent
	 *            the indentation string for the action header and footer
	 * @return the full action declaration text, starting with a newline
	 */
	static String generateActionDeclaration(final String actionName, final String returnType, final String body,
			final String headerIndent) {
		final boolean isVoid = "action".equals(returnType);
		final String keyword = isVoid ? "action " : returnType + " ";
		final String header = headerIndent + keyword + actionName + " () {";
		final String footer = headerIndent + "}";

		// Ensure the body ends with a newline so the closing brace is on its own line.
		final String normalizedBody = body.endsWith("\n") ? body : body + "\n";

		return "\n" + header + "\n" + normalizedBody + footer + "\n";
	}

	// -------------------------------------------------------------------------
	// Error helper
	// -------------------------------------------------------------------------

	private static void showError(final Shell shell, final String message) {
		MessageDialog.openError(shell, "Extract Action", message);
	}
}

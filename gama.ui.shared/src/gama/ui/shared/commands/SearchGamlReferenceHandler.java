/*******************************************************************************************************
 *
 * SearchGamlReferenceHandler.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.shared.access.GamlAccessContents2;

/**
 * Handles the "Search in GAML Reference" command. Retrieves the selected word or word under the caret from the
 * active text editor or console (StyledText) and opens the GAML Reference popup dialog.
 */
public class SearchGamlReferenceHandler extends AbstractHandler {

	/** The Constant COMMAND_ID. */
	public static final String COMMAND_ID = "gama.ui.commands.searchGamlReference";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		String query = "";
		Display display = Display.getCurrent();
		if (display == null) { display = Display.getDefault(); }
		Control focus = display != null ? display.getFocusControl() : null;
		if (focus instanceof StyledText st && !st.isDisposed()) {
			query = st.getSelectionText();
			if (query == null || query.isBlank()) {
				query = getWordAtCaret(st);
			}
		}
		if ((query == null || query.isBlank()) && event != null) {
			ISelection sel = HandlerUtil.getCurrentSelection(event);
			if (sel instanceof ITextSelection textSelection && textSelection.getLength() > 0) {
				query = textSelection.getText();
			}
		}
		GamlAccessContents2.open(query);
		return null;
	}

	/**
	 * Extracts the word surrounding the current caret position in the given StyledText widget.
	 *
	 * @param st
	 *            the StyledText widget
	 * @return the word at caret, or an empty string
	 */
	public static String getWordAtCaret(final StyledText st) {
		if (st == null || st.isDisposed()) return "";
		int offset = st.getCaretOffset();
		String text = st.getText();
		if (text == null || text.isEmpty()) return "";
		int len = text.length();
		if (offset < 0 || offset > len) return "";
		int start = offset;
		int end = offset;
		while (start > 0 && isWordChar(text.charAt(start - 1))) {
			start--;
		}
		while (end < len && isWordChar(text.charAt(end))) {
			end++;
		}
		return start < end ? text.substring(start, end).trim() : "";
	}

	/**
	 * Checks whether the given character is a valid word character in GAML.
	 *
	 * @param c
	 *            the character
	 * @return true if letter, digit, or underscore
	 */
	public static boolean isWordChar(final char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

}

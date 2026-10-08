/*******************************************************************************************************
 *
 * JsonBracketInsertionCompleter.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import static gama.ui.viewers.json.eclipse.JsonEditorUtil.getPreferences;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_AUTO_CREATE_END_BRACKETSY;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

/**
 * The Class JsonBracketInsertionCompleter.
 */
class JsonBracketInsertionCompleter extends KeyAdapter {

	/** The json editor. */
	private final JsonEditor jsonEditor;

	/**
	 * @param jsonEditor
	 */
	JsonBracketInsertionCompleter(final JsonEditor jsonEditor) {
		this.jsonEditor = jsonEditor;
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		InsertClosingBracketsSupport insertClosingBracketsSupport = getInsertionSupport(e);
		if (insertClosingBracketsSupport == null) return;
		/*
		 * do not use last caret start - because the listener ordering could be different
		 */
		ISelectionProvider selectionProvider = this.jsonEditor.getSelectionProvider();
		if (selectionProvider == null) return;
		ISelection selection = selectionProvider.getSelection();
		if (!(selection instanceof ITextSelection)) return;
		boolean enabled = getPreferences().getBooleanPreference(P_EDITOR_AUTO_CREATE_END_BRACKETSY);
		if (!enabled) return;
		ITextSelection textSelection = (ITextSelection) selection;
		int offset = textSelection.getOffset();

		try {
			IDocument document = this.jsonEditor.getDocument();
			if (document == null) return;
			insertClosingBracketsSupport.insertClosingBrackets(document, selectionProvider, offset);
		} catch (BadLocationException e1) {
			/* ignore */
			return;
		}

	}

	/**
	 * Gets the insertion support.
	 *
	 * @param e
	 *            the e
	 * @return the insertion support
	 */
	protected InsertClosingBracketsSupport getInsertionSupport(final KeyEvent e) {
		if (e.character == '[') return new EdgeBracketInsertionSupport();
		if (e.character == '{') return new CurlyBracketInsertionSupport();
		return null;
	}

	/**
	 * The Class InsertClosingBracketsSupport.
	 */
	private abstract static class InsertClosingBracketsSupport {

		/**
		 * Insert closing brackets.
		 *
		 * @param document
		 *            the document
		 * @param selectionProvider
		 *            the selection provider
		 * @param offset
		 *            the offset
		 * @throws BadLocationException
		 *             the bad location exception
		 */
		protected abstract void insertClosingBrackets(IDocument document, ISelectionProvider selectionProvider,
				int offset) throws BadLocationException;
	}

	/**
	 * The Class EdgeBracketInsertionSupport.
	 */
	private class EdgeBracketInsertionSupport extends InsertClosingBracketsSupport {

		@Override
		protected void insertClosingBrackets(final IDocument document, final ISelectionProvider selectionProvider, final int offset)
				throws BadLocationException {
			document.replace(offset - 1, 1, "[ ]");
			selectionProvider.setSelection(new TextSelection(offset + 1, 0));

		}

	}

	/**
	 * The Class CurlyBracketInsertionSupport.
	 */
	private class CurlyBracketInsertionSupport extends InsertClosingBracketsSupport {

		@Override
		protected void insertClosingBrackets(final IDocument document, final ISelectionProvider selectionProvider, final int offset)
				throws BadLocationException {
			document.replace(offset - 1, 1, "{ }");
			selectionProvider.setSelection(new TextSelection(offset + 1, 0));

		}

	}
}
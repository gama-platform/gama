/*******************************************************************************************************
 *
 * FindGamlKeywordHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import gama.ui.devtools.keywords.GamlKeywordDialog;
import gama.ui.devtools.keywords.GamlKeywordIndex.Entry;
import gama.ui.devtools.keywords.GamlKeywordSearch;
import gama.ui.devtools.util.Notifier;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Opens the {@link GamlKeywordDialog}, initialized with the selected text or the word under the caret (in any text
 * editor, including the GAML editor), and opens the Java implementations chosen by the user.
 */
public class FindGamlKeywordHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) { return null; }
		GamlKeywordDialog dialog = new GamlKeywordDialog(window.getShell());
		String initial = initialPattern(event);
		if (initial != null && !initial.isEmpty()) { dialog.setInitialPattern(initial); }
		if (dialog.open() != Window.OK) { return null; }
		IWorkbenchPage page = window.getActivePage();
		for (Object o : dialog.getResult()) { if (o instanceof Entry e) { open(page, e); } }
		return null;
	}

	private static void open(final IWorkbenchPage page, final Entry entry) {
		IFile file = GamlKeywordSearch.file(entry);
		if (file == null || !file.exists()) {
			Notifier.show("GAML keyword", "The file " + entry.path() + " does not exist anymore.");
			return;
		}
		try {
			IEditorPart part = IDE.openEditor(page, file, true);
			ITextEditor editor = Adapters.adapt(part, ITextEditor.class);
			if (editor != null) { editor.selectAndReveal(entry.offset(), entry.length()); }
		} catch (Exception ex) {
			ResourceUtils.log(ex);
		}
	}

	/** The selected text (if it fits on one line) or the GAML word (or operator) under the caret. */
	private static String initialPattern(final ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (!(selection instanceof ITextSelection ts)) { return null; }
		String text = ts.getText();
		if (text != null && !text.isBlank()) { return text.contains("\n") ? null : text.strip(); }
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		ITextEditor editor = Adapters.adapt(part, ITextEditor.class);
		if (editor == null) { return null; }
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		return doc == null ? null : wordAt(doc, ts.getOffset());
	}

	private static String wordAt(final IDocument doc, final int offset) {
		try {
			int start = offset;
			int end = offset;
			int length = doc.getLength();
			while (start > 0 && isWordChar(doc.getChar(start - 1))) { start--; }
			while (end < length && isWordChar(doc.getChar(end))) { end++; }
			return start == end ? null : doc.get(start, end - start);
		} catch (BadLocationException e) {
			return null;
		}
	}

	private static boolean isWordChar(final char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

}

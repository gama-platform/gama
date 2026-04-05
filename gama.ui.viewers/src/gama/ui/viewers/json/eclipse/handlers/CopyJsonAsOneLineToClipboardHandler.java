/*******************************************************************************************************
 *
 * CopyJsonAsOneLineToClipboardHandler.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.handlers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import gama.ui.viewers.json.eclipse.JsonEditor;

/**
 * The Class CopyJsonAsOneLineToClipboardHandler.
 */
public class CopyJsonAsOneLineToClipboardHandler extends AbstractJsonEditorHandler {

	@Override
	protected void executeOnJsonEditor(final JsonEditor jsonEditor) {
		String reducedJson = jsonEditor.createJsonAsOneLine();

		StringSelection selection = new StringSelection(reducedJson);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}

}

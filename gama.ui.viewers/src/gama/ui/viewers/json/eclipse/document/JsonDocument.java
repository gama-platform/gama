/*******************************************************************************************************
 *
 * JsonDocument.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.document;

import org.eclipse.jface.text.Document;

import gama.ui.viewers.json.document.JSONFormatSupport;
import gama.ui.viewers.json.document.JSONFormatSupport.FormatterResult;
import gama.ui.viewers.json.eclipse.JsonEditor;

/**
 * The Class JsonDocument.
 */
public class JsonDocument extends Document {

	/** The editor. */
	private final JsonEditor editor;

	/**
	 * Instantiates a new highspeed JSON document.
	 *
	 * @param editor
	 *            the editor
	 */
	public JsonDocument(final JsonEditor editor) {
		this.editor = editor;
	}

	@Override
	public void set(String text, final long modificationStamp) {
		FormatterResult result = JSONFormatSupport.DEFAULT.formatJSONIfNotHavingMinAmountOfNewLines(text);
		if (result.state.hasContentChanged()) { text = result.getFormatted(); }
		super.set(text, modificationStamp);
		if (result.state.hasContentChanged()) { editor.markDirtyBecauseFormatWasNecessaryForOneLinerHandling(); }
	}

	/**
	 * Sets the formatted.
	 *
	 * @param text
	 *            the new formatted
	 */
	public void setFormatted(final String text) {
		super.set(text, getModificationStamp());
	}

}

/*******************************************************************************************************
 *
 * JsonTextFileDocumentProvider.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.document;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import gama.ui.viewers.json.eclipse.JsonEditor;

/**
 * Document provider for files outside of workspace
 *
 * @author albert
 *
 */
public class JsonTextFileDocumentProvider extends TextFileDocumentProvider {

	/** The editor. */
	private final JsonEditor editor;

	/**
	 * Instantiates a new highspeed JSON text file document provider.
	 *
	 * @param editor
	 *            the editor
	 */
	public JsonTextFileDocumentProvider(final JsonEditor editor) {
		this.editor = editor;
	}

	@Override
	public IDocument getDocument(final Object element) {
		IDocument document = super.getDocument(element);
		if (document == null) return null;
		IDocumentPartitioner formerPartitioner = document.getDocumentPartitioner();
		if (formerPartitioner instanceof JsonPartitioner
				|| formerPartitioner instanceof FallbackJsonPartitioner)
			return document;

		/* installation necessary */
		JsonPartitionerAndAutoFormatSupport.DEFAULT.setPartitionerAndFormatIfNecessary(editor, document);

		return document;
	}

}
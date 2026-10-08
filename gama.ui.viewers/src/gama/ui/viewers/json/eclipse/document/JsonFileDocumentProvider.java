/*******************************************************************************************************
 *
 * JsonFileDocumentProvider.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.document;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import gama.ui.viewers.json.eclipse.JsonEditor;

/**
 * Document provider for files inside workspace
 *
 * @author albert
 *
 */
public class JsonFileDocumentProvider extends FileDocumentProvider {

	/** The editor. */
	private final JsonEditor editor;

	/**
	 * Instantiates a new highspeed JSON file document provider.
	 *
	 * @param editor
	 *            the editor
	 */
	public JsonFileDocumentProvider(final JsonEditor editor) {
		this.editor = editor;
	}

	@Override
	protected IDocument createDocument(final Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		JsonPartitionerAndAutoFormatSupport.DEFAULT.setPartitionerAndFormatIfNecessary(editor, document);
		return document;
	}

	@Override
	protected IDocument createEmptyDocument() {
		return new JsonDocument(editor);
	}

}
/*******************************************************************************************************
 *
 * JsonPartitionerAndAutoFormatSupport.java, in gama.ui.viewers, is part of the source code of the GAMA
 * modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.document;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;

import gama.ui.viewers.json.document.JSONFormatSupport;
import gama.ui.viewers.json.document.JSONFormatSupport.FormatterResult;
import gama.ui.viewers.json.eclipse.JsonEditor;

/**
 * The Class JsonPartitionerAndAutoFormatSupport.
 */
public class JsonPartitionerAndAutoFormatSupport {

	/** The Constant DEFAULT. */
	public static final JsonPartitionerAndAutoFormatSupport DEFAULT =
			new JsonPartitionerAndAutoFormatSupport();

	/**
	 * Sets the partitioner and format if necessary.
	 *
	 * @param editor
	 *            the editor
	 * @param document
	 *            the document
	 */
	public void setPartitionerAndFormatIfNecessary(final JsonEditor editor, final IDocument document) {
		FormatterResult result = JSONFormatSupport.DEFAULT.formatJSONIfNotHavingMinAmountOfNewLines(document.get());
		/* installation necessary */

		IDocumentPartitioner partitioner = null;

		switch (result.state) {
			case FORMAT_DONE:
				document.set(result.getFormatted());
				editor.markDirtyBecauseFormatWasNecessaryForOneLinerHandling();
				break;
			case KEPT_AS_IS:
				/* no changes necessary */
				break;
			case NOT_VALID_JSON_BUT_FALLBACK_DONE:
				document.set(result.getFormatted());
				editor.markDirtyBecauseFormatWasNecessaryForOneLinerHandling();
				break;
			case NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS:
				// we cannot handle this correctly - so we turn off syntax highlighting by using
				// fallback partitioner
				partitioner = FallbackJsonPartionerFactory.create();
				break;
			default:
				break;

		}
		if (partitioner == null) { partitioner = JsonPartionerFactory.create(); }

		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
	}

}

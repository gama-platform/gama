/*******************************************************************************************************
 *
 * GamlEditorIssueProcessor.java, in gama.ui.editor, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionProvider;
import org.eclipse.xtext.ui.editor.validation.AnnotationIssueProcessor;
import org.eclipse.xtext.ui.editor.validation.IValidationIssueProcessor;
import org.eclipse.xtext.validation.Issue;

/** Publishes one-shot validation to both workspace markers and the live editor, including unsaved content. */
final class GamlEditorIssueProcessor implements IValidationIssueProcessor {

	private final IXtextDocument document;
	private final IAnnotationModel annotationModel;
	private final IssueResolutionProvider resolutions;
	private final IValidationIssueProcessor markers;

	GamlEditorIssueProcessor(final IXtextDocument document, final IAnnotationModel annotationModel,
			final IssueResolutionProvider resolutions, final IValidationIssueProcessor markers) {
		this.document = document;
		this.annotationModel = annotationModel;
		this.resolutions = resolutions;
		this.markers = markers;
	}

	@Override
	public void processIssues(final List<Issue> issues, final IProgressMonitor monitor) {
		if (monitor.isCanceled()) return;
		markers.processIssues(issues, monitor);
		if (monitor.isCanceled()) return;
		// Marker publication removes old Xtext annotations; restore live annotations afterwards.
		var annotations = new AnnotationIssueProcessor(document, annotationModel, resolutions);
		try {
			annotations.processIssues(issues, monitor);
		} finally {
			annotationModel.removeAnnotationModelListener(annotations);
		}
	}
}

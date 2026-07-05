/*******************************************************************************************************
 *
 * GamlAnnotationCodeMiningProvider.java, in gama.ui.editor, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.ui.internal.editors.text.codemining.annotation.AnnotationCodeMiningProvider;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.validation.XtextAnnotation;

import gama.api.utils.prefs.GamaPreferences;

/**
 * Provides GAMA-specific annotation code minings for {@link GamlEditor}.
 *
 * <p>
 * This provider replaces the default Eclipse {@link AnnotationCodeMiningProvider} for GAML editors so that inline
 * annotation messages can be customized without affecting third-party code-mining providers. Its responsibilities are:
 * </p>
 * <ul>
 * <li>respecting the {@link GamaPreferences.Modeling#EDITOR_MINING} preference;</li>
 * <li>restricting code minings to GAML error, warning, and information annotations;</li>
 * <li>placing header minings at the indentation of the decorated line rather than at the exact annotation column;</li>
 * <li>truncating long messages so they stay within the visible editor width.</li>
 * </ul>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 16 sept. 2023
 */
public class GamlAnnotationCodeMiningProvider extends AnnotationCodeMiningProvider implements ICodeMiningProvider {

	/** Separator used to merge several messages displayed in a single line header mining. */
	private static final String INLINE_MINING_SEPARATOR = " | ";

	/**
	 * Collects all distinct annotation messages that should be rendered as a single mining on one line.
	 */
	private static final class LineMiningGroup {

		/** The anchored document position shared by all merged messages. */
		private final Position position;

		/** Distinct messages accumulated for that rendered line, in encounter order. */
		private final LinkedHashSet<String> labels = new LinkedHashSet<>();

		/**
		 * Instantiates a new line mining group.
		 *
		 * @param position
		 *            the anchored document position shared by the group
		 */
		private LineMiningGroup(final Position position) {
			this.position = position;
		}

		/**
		 * Adds a label to the group.
		 *
		 * @param label
		 *            the label to add
		 */
		private void addLabel(final String label) {
			if (label != null && !label.isBlank()) { labels.add(label); }
		}

		/**
		 * Builds the combined full message for this line.
		 *
		 * @return the merged label text
		 */
		private String getCombinedLabel() { return String.join(INLINE_MINING_SEPARATOR, labels); }
	}

	/** Ellipsis appended to truncated mining labels. */
	private static final String INLINE_MINING_ELLIPSIS = "…";

	/** Additional right-side padding, in pixels, reserved when measuring visible mining width. */
	private static final int INLINE_MINING_PADDING = 24;

	/** Standard info annotation types that should still be mirrored as code minings. */
	private static final java.util.Set<String> ANNOTATION_INFO_TYPES =
			java.util.Set.of("org.eclipse.ui.workbench.texteditor.info", "org.eclipse.xtext.ui.editor.info");

	/** Empty mining result reused whenever code minings are disabled or unavailable. */
	private static final CompletableFuture<List<? extends ICodeMining>> EMPTY =
			CompletableFuture.completedFuture(Collections.emptyList());

	/** The editor owning this provider and supplying the annotation/rendering context. */
	private final GamlEditor editor;

	/**
	 * A line-header code mining whose label has already been resolved.
	 */
	private class GamlResolvedHeaderCodeMining extends LineHeaderCodeMining {

		/** The already-computed text label to display. */
		private final String label;

		/**
		 * Instantiates a new resolved header code mining.
		 *
		 * @param position
		 *            the anchored document position of the mining
		 * @param label
		 *            the label to display once resolved
		 * @throws BadLocationException
		 *             if the line header position cannot be mapped to a valid document line
		 */
		GamlResolvedHeaderCodeMining(final Position position, final String label) throws BadLocationException {
			super(position, GamlAnnotationCodeMiningProvider.this, null);
			this.label = label;
		}

		@Override
		protected CompletableFuture<Void> doResolve(final ITextViewer viewer, final IProgressMonitor monitor) {
			setLabel(label);
			return CompletableFuture.completedFuture(null);
		}
	}

	/**
	 * Instantiates a new GAML annotation code-mining provider.
	 *
	 * @param editor
	 *            the GAML editor that owns this provider; must not be {@code null}
	 */
	public GamlAnnotationCodeMiningProvider(final GamlEditor editor) {
		this.editor = editor;
		setContext(editor);
	}

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(final ITextViewer viewer,
			final IProgressMonitor monitor) {
		if (!GamaPreferences.Modeling.EDITOR_MINING.getValue() || !(viewer instanceof ISourceViewer sourceViewer)) return EMPTY;
		final IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
		final IDocument document = viewer.getDocument();
		if (annotationModel == null || document == null) return EMPTY;
		final Map<Integer, LineMiningGroup> groups = new LinkedHashMap<>();
		final List<ICodeMining> minings = new ArrayList<>();
		final Iterator<Annotation> annotations = annotationModel.getAnnotationIterator();
		while (annotations.hasNext()) {
			if (monitor != null && monitor.isCanceled()) return EMPTY;
			final Annotation annotation = annotations.next();
			if (!isInlineMiningAnnotation(annotation)) { continue; }
			final Position annotationPosition = annotationModel.getPosition(annotation);
			if (annotationPosition == null || annotationPosition.isDeleted()) { continue; }
			final Position miningPosition = computeMiningPosition(document, annotationPosition);
			final String label = normalizeMiningLabel(annotation.getText());
			if (label.isEmpty()) { continue; }
			groups.computeIfAbsent(miningPosition.getOffset(), o -> new LineMiningGroup(miningPosition))
					.addLabel(label);
		}
		for (final LineMiningGroup group : groups.values()) {
			final String label = truncateMiningLabel(viewer, group.position.getOffset(), group.getCombinedLabel());
			if (label.isEmpty()) { continue; }
			try {
				minings.add(new GamlResolvedHeaderCodeMining(group.position, label));
			} catch (final BadLocationException e) {}
		}
		return CompletableFuture.completedFuture(minings);
	}

	/**
	 * Normalizes an annotation message before it is aggregated with other messages rendered on the same line.
	 *
	 * @param label
	 *            the raw annotation text
	 * @return the normalized single-line label, or an empty string if no visible text remains
	 */
	private String normalizeMiningLabel(final String label) {
		return label == null ? "" : label.replace('\r', ' ').replace('\n', ' ').trim();
	}

	/**
	 * Returns whether the given annotation should be mirrored as an inline GAML mining.
	 *
	 * @param annotation
	 *            the annotation to inspect
	 * @return {@code true} if the annotation represents a visible error, warning, or info message with text;
	 *         {@code false} otherwise
	 */
	private boolean isInlineMiningAnnotation(final Annotation annotation) {
		if (annotation == null || annotation.isMarkedDeleted() || (!(annotation instanceof XtextAnnotation) && annotation.getText() == null)) return false;
		final String type = annotation.getType();
		return XtextEditor.ERROR_ANNOTATION_TYPE.equals(type) || XtextEditor.WARNING_ANNOTATION_TYPE.equals(type)
				|| XtextEditor.INFO_ANNOTATION_TYPE.equals(type) || ANNOTATION_INFO_TYPES.contains(type);
	}

	/**
	 * Computes the document position at which the header mining should be anchored.
	 *
	 * <p>
	 * The returned position preserves the original annotation length while moving the offset to the first
	 * non-whitespace character of the decorated line. This keeps the mining aligned with indentation while still
	 * referring to the same logical annotation range.
	 * </p>
	 *
	 * @param document
	 *            the editor document containing the decorated line
	 * @param annotationPosition
	 *            the original annotation position
	 * @return a new position anchored at the indentation of the annotated line
	 */
	private Position computeMiningPosition(final IDocument document, final Position annotationPosition) {
		final int fallbackOffset = annotationPosition == null ? 0 : annotationPosition.getOffset();
		final int fallbackLength = annotationPosition == null ? 1 : Math.max(1, annotationPosition.getLength());
		if (document == null || document.getLength() == 0 || annotationPosition == null)
			return new Position(Math.max(0, fallbackOffset), fallbackLength);
		try {
			final int safeOffset = Math.max(0, Math.min(annotationPosition.getOffset(), document.getLength() - 1));
			final IRegion line = document.getLineInformationOfOffset(safeOffset);
			final int lineOffset = line.getOffset();
			final int lineEnd = lineOffset + line.getLength();
			int anchor = lineOffset;
			while (anchor < lineEnd && Character.isWhitespace(document.getChar(anchor))) { anchor++; }
			return new Position(anchor, Math.max(1, annotationPosition.getLength()));
		} catch (final BadLocationException e) {
			return new Position(Math.max(0, Math.min(fallbackOffset, Math.max(0, document.getLength() - 1))),
					fallbackLength);
		}
	}

	/**
	 * Truncates a mining label so it fits in the visible horizontal space available from the given anchor offset.
	 *
	 * @param viewer
	 *            the text viewer displaying the code mining
	 * @param offset
	 *            the document offset at which the mining is anchored
	 * @param label
	 *            the full annotation message
	 * @return the original label when it fits, a shortened label followed by an ellipsis when it does not fit, or an
	 *         empty string when no visible label can be displayed
	 */
	private String truncateMiningLabel(final ITextViewer viewer, final int offset, final String label) {
		final String normalized = normalizeMiningLabel(label);
		if (normalized.isEmpty()) return "";
		final StyledText styledText = viewer.getTextWidget();
		if (styledText == null || styledText.isDisposed()) return normalized;
		final int availableWidth = computeAvailableMiningWidth(viewer, offset);
		if (availableWidth <= 0) return INLINE_MINING_ELLIPSIS;
		final GC gc = new GC(styledText);
		try {
			gc.setFont(styledText.getFont());
			if (gc.textExtent(normalized).x <= availableWidth) return normalized;
			if (gc.textExtent(INLINE_MINING_ELLIPSIS).x > availableWidth) return "";
			int low = 0;
			int high = normalized.length();
			while (low < high) {
				final int mid = low + high + 1 >>> 1;
				final String candidate = normalized.substring(0, mid).stripTrailing() + INLINE_MINING_ELLIPSIS;
				if (gc.textExtent(candidate).x <= availableWidth) {
					low = mid;
				} else {
					high = mid - 1;
				}
			}
			return normalized.substring(0, low).stripTrailing() + INLINE_MINING_ELLIPSIS;
		} finally {
			gc.dispose();
		}
	}

	/**
	 * Computes the visible width available to display a mining label from the given anchor offset.
	 *
	 * @param viewer
	 *            the text viewer containing the mining
	 * @param offset
	 *            the mining anchor offset in the document
	 * @return the available width in pixels, never negative
	 */
	private int computeAvailableMiningWidth(final ITextViewer viewer, final int offset) {
		final StyledText styledText = viewer.getTextWidget();
		final IDocument document = viewer.getDocument();
		if (styledText == null || styledText.isDisposed() || document == null || document.getLength() == 0)
			return Integer.MAX_VALUE;
		try {
			final int safeOffset = Math.max(0, Math.min(offset, document.getLength() - 1));
			final int anchorX = styledText.getLocationAtOffset(safeOffset).x;
			return Math.max(0, styledText.getClientArea().width - anchorX - INLINE_MINING_PADDING);
		} catch (final IllegalArgumentException e) {
			return Math.max(0, styledText.getClientArea().width - INLINE_MINING_PADDING);
		}
	}

	/**
	 * Returns the editor associated with this provider.
	 *
	 * @return the owning GAML editor
	 */
	protected GamlEditor getEditor() { return editor; }
}

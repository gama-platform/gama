package gaml.compiler.ui.wizards.behavior;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.xtext.parser.IParser;

import com.google.inject.Inject;

import gaml.compiler.ui.editor.GamlEditor;
import gaml.compiler.ui.wizards.species.SpeciesDocumentEdit;

/**
 * Opens the shared Reflex Editor on a GAML editor: creation in the caret's species, class, grid, global or experiment,
 * or edition of a reflex/init/abort header. Other behavior editors are diagram-only. The result is applied as one
 * undoable document change, refused when the document changed meanwhile.
 */
public final class BehaviorWizardHandler {

	@Inject private IParser parser;

	/** True when the selection is on a reflex/init/abort header (not inside its code). */
	public boolean canEdit(final GamlEditor editor) {
		if (!editor.isEditable() || !(editor.getSelectionProvider().getSelection() instanceof ITextSelection selection))
			return false;
		return editor.getDocument().readOnly(resource -> {
			if (resource.getParseResult() == null || resource.getParseResult().hasSyntaxErrors()) return false;
			try {
				return canEdit(new BehaviorSource(editor.getDocument().get(), selection.getOffset(), parser, false),
						selection.getOffset(), selection.getLength());
			} catch (IllegalArgumentException error) {
				return false;
			}
		});
	}

	static boolean canEdit(final BehaviorSource source, final int offset, final int length) {
		return BehaviorKind.LIFECYCLE.contains(source.draft().kind()) && source.isBehaviorSelection(offset, length);
	}

	public void open(final GamlEditor editor, final boolean create) {
		var shell = editor.getEditorSite().getShell();
		if (!editor.isEditable()) {
			MessageDialog.openError(shell, "Reflex Editor", "This editor is read-only.");
			return;
		}
		if (!create && !canEdit(editor)) return;
		try {
			var transaction = new SpeciesDocumentEdit(editor.getDocument());
			int offset = editor.getSelectionProvider().getSelection() instanceof ITextSelection selection
					? selection.getOffset() : -1;
			var source = new BehaviorSource(transaction.snapshot(), offset, parser, create);
			URI uri = isolatedUri(editor);
			var dialog = new BehaviorWizard(shell, source.draft(),
					BehaviorKind.LIFECYCLE, draft -> {
						if (!transaction.isCurrent()) return "The document changed. Cancel and reopen the Reflex Editor.";
						return source.validationError(draft);
					}, draft -> source.bodyContext(draft, uri));
			if (dialog.open() == Window.OK && dialog.getResult() != null)
				transaction.apply(source.render(dialog.getResult()));
		} catch (IllegalArgumentException | IllegalStateException | BadLocationException error) {
			MessageDialog.openError(shell, "Reflex Editor", error.getMessage());
		}
	}

	/** A sibling URI of the edited file, so relative imports resolve, but never the file itself. */
	private static URI isolatedUri(final GamlEditor editor) {
		URI uri = editor.getDocument().readOnly(resource -> resource.getURI());
		if (uri == null || uri.segmentCount() == 0)
			return URI.createURI("synthetic:/behavior-wizard/.behavior-wizard.gaml");
		return uri.trimSegments(1).appendSegment(".behavior-wizard.gaml");
	}
}

package gaml.compiler.ui.wizards.behavior;

import java.util.EnumSet;

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
 * Opens the shared behaviour form on a GAML editor: creation (kind selectable) in the caret's species, class, grid,
 * global or experiment, or edition of the behaviour whose header is selected. The result is applied as one undoable
 * document change, refused when the document changed meanwhile.
 */
public final class BehaviorWizardHandler {

	@Inject private IParser parser;

	/** True when the selection is on a behaviour header (not inside its code). */
	public boolean canEdit(final GamlEditor editor) {
		if (!editor.isEditable() || !(editor.getSelectionProvider().getSelection() instanceof ITextSelection selection))
			return false;
		return editor.getDocument().readOnly(resource -> {
			if (resource.getParseResult() == null || resource.getParseResult().hasSyntaxErrors()) return false;
			try {
				return new BehaviorSource(editor.getDocument().get(), selection.getOffset(), parser, false)
						.isBehaviorSelection(selection.getOffset(), selection.getLength());
			} catch (IllegalArgumentException error) {
				return false;
			}
		});
	}

	public void open(final GamlEditor editor, final boolean create) {
		var shell = editor.getEditorSite().getShell();
		if (!editor.isEditable()) {
			MessageDialog.openError(shell, "Behavior", "This editor is read-only.");
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
					create ? EnumSet.allOf(BehaviorKind.class) : source.draft().kind().convertibleKinds(), draft -> {
						if (!transaction.isCurrent()) return "The document changed. Cancel and reopen the behavior form.";
						return source.validationError(draft);
					}, draft -> source.bodyContext(draft, uri));
			if (dialog.open() == Window.OK && dialog.getResult() != null)
				transaction.apply(source.render(dialog.getResult()));
		} catch (IllegalArgumentException | IllegalStateException | BadLocationException error) {
			MessageDialog.openError(shell, "Behavior", error.getMessage());
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

package gaml.compiler.ui.wizards.display;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.xtext.parser.IParser;

import com.google.inject.Inject;

import gaml.compiler.ui.editor.GamlEditor;
import gaml.compiler.ui.wizards.species.SpeciesDocumentEdit;

/** Text-editor entry points; detached previews and one undoable apply against a checked document snapshot. */
public final class DisplayWizardHandler {
	@Inject private IParser parser;

	/** Add display is available only inside an editable, syntactically valid experiment. */
	public boolean canCreate(final GamlEditor editor) {
		if (!editor.isEditable() || !(editor.getSelectionProvider().getSelection() instanceof ITextSelection selection))
			return false;
		return editor.getDocument().readOnly(resource -> resource.getParseResult() != null
				&& !resource.getParseResult().hasSyntaxErrors()
				&& DisplaySource.canCreate(resource.getParseResult().getRootASTElement(), selection.getOffset()));
	}

	public boolean canEdit(final GamlEditor editor) {
		if (!editor.isEditable() || !(editor.getSelectionProvider().getSelection() instanceof ITextSelection selection))
			return false;
		return editor.getDocument().readOnly(resource -> {
			if (resource.getParseResult() == null || resource.getParseResult().hasSyntaxErrors()) return false;
			try {
				var source = new DisplaySource(editor.getDocument().get(), selection.getOffset(), parser, false);
				return source.isDisplaySelection(selection.getOffset(), selection.getLength());
			} catch (IllegalArgumentException error) { return false; }
		});
	}

	public void open(final GamlEditor editor, final boolean create) {
		var shell = editor.getEditorSite().getShell();
		if (!editor.isEditable()) { MessageDialog.openError(shell, "Display", "This editor is read-only."); return; }
		if (!create && !canEdit(editor)) return;
		try {
			var transaction = new SpeciesDocumentEdit(editor.getDocument());
			int offset = editor.getSelectionProvider().getSelection() instanceof ITextSelection selection ? selection.getOffset() : -1;
			var source = new DisplaySource(transaction.snapshot(), offset, parser, create);
			URI uri = editor.getDocument().readOnly(resource -> resource.getURI());
			URI isolated = uri == null || uri.segmentCount() == 0 ? URI.createURI("synthetic:/display-wizard/.display-wizard.gaml")
					: uri.trimSegments(1).appendSegment(".display-wizard.gaml");
			var dialog = new DisplayWizard(shell, source.draft(), draft -> {
				if (!transaction.isCurrent()) return "The document changed. Cancel and reopen the display form.";
				return source.validationError(draft);
			}, source::speciesNames, source::gridNames, draft -> source.bodyContext(draft, isolated), source::aspectNames);
			dialog.setCreationMode(create);
			if (dialog.open() == Window.OK && dialog.getResult() != null) transaction.apply(source.render(dialog.getResult()));
		} catch (IllegalArgumentException | IllegalStateException | BadLocationException error) {
			MessageDialog.openError(shell, "Display", error.getMessage());
		}
	}
}

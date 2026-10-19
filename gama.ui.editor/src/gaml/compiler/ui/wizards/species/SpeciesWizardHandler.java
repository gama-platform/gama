package gaml.compiler.ui.wizards.species;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.parser.IParser;

import com.google.inject.Inject;

import gaml.compiler.ui.editor.GamlEditor;

/** Shared command entry point for the editor's context and declaration menus. */
public final class SpeciesWizardHandler extends AbstractHandler {

	@Inject private IParser parser;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (!(HandlerUtil.getActiveEditor(event) instanceof GamlEditor editor)) return null;
		String command = event.getCommand().getId();
		open(editor, command.endsWith(".createSpecies") || command.endsWith(".createGrid") || command.endsWith(".createClass"),
				command.endsWith(".createGrid") ? SpeciesWizard.Kind.GRID
						: command.endsWith(".createClass") ? SpeciesWizard.Kind.CLASS : SpeciesWizard.Kind.SPECIES);
		return null;
	}

	public boolean canEdit(final GamlEditor editor) {
		if (!editor.isEditable()
				|| !(editor.getSelectionProvider().getSelection() instanceof ITextSelection selection)) return false;
		return editor.getDocument().readOnly(resource -> {
			if (resource.getParseResult() == null || resource.getParseResult().hasSyntaxErrors()) return false;
			try {
				return globalSelection(editor.getDocument().get(), selection) != null
						|| new SpeciesSource(editor.getDocument().get(), selection.getOffset(), parser)
								.isSpeciesSelection(selection.getOffset(), selection.getLength());
			} catch (IllegalArgumentException error) {
				return false;
			}
		});
	}

	private ModelSource globalSelection(final String text, final ITextSelection selection) {
		try {
			var source = new ModelSource(text, selection.getOffset(), parser);
			return source.isGlobalSelection(selection.getOffset(), selection.getLength()) ? source : null;
		} catch (IllegalArgumentException error) {
			return null;
		}
	}

	public void open(final GamlEditor editor, final boolean create) {
		open(editor, create, SpeciesWizard.Kind.SPECIES);
	}

	public void open(final GamlEditor editor, final boolean create, final SpeciesWizard.Kind kind) {
		if (!create && !canEdit(editor)) return;
		var shell = editor.getEditorSite().getShell();
		if (!editor.isEditable()) {
			MessageDialog.openError(shell, "Species", "This editor is read-only.");
			return;
		}
		try {
			var transaction = new SpeciesDocumentEdit(editor.getDocument());
			if (!create) {
				var model = globalSelection(transaction.snapshot(),
						(ITextSelection) editor.getSelectionProvider().getSelection());
				if (model != null) {
					openModel(editor, transaction, model);
					return;
				}
			}
			int offset = create ? -1 : ((ITextSelection) editor.getSelectionProvider().getSelection()).getOffset();
			var source = new SpeciesSource(transaction.snapshot(), offset, parser, kind);
			var dialog = new SpeciesWizard(shell, source.draft(), draft -> {
				if (!transaction.isCurrent()) return "The document changed. Cancel and reopen the species form.";
				return source.validationError(draft);
			}, create, source.parentNames(), source.context(), source.kind());
			dialog.setCreationMode(create);
			if (dialog.open() == Window.OK) transaction.apply(source.render(dialog.getResult()));
		} catch (IllegalArgumentException | IllegalStateException | BadLocationException error) {
			MessageDialog.openError(shell, "Species", error.getMessage());
		}
	}

	/** Header-only global edit: model name, skills, control, scheduling and the documentation comment. */
	private void openModel(final GamlEditor editor, final SpeciesDocumentEdit transaction, final ModelSource source)
			throws BadLocationException {
		var file = editor.getEditorInput().getAdapter(org.eclipse.core.resources.IFile.class);
		var location = file == null ? null
				: new ModelLocation(file.getParent().getFullPath().toString(), file.getName());
		var dialog = new SpeciesWizard(editor.getEditorSite().getShell(), source.draft(), draft -> {
			if (!transaction.isCurrent()) return "The document changed. Cancel and reopen the model form.";
			return source.validationError(draft);
		}, false, source.context(), location, false, source.documentation());
		dialog.setCreationMode(false);
		if (dialog.open() == Window.OK) transaction.apply(source.render(dialog.getResult(), dialog.getDocumentation()));
	}
}

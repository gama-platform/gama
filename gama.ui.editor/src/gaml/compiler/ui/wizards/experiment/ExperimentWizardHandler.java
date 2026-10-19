package gaml.compiler.ui.wizards.experiment;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.xtext.parser.IParser;

import com.google.inject.Inject;

import gama.api.gaml.types.Types;
import gaml.compiler.ui.editor.GamlEditor;
import gaml.compiler.ui.wizards.species.SpeciesDocumentEdit;

/**
 * Opens the shared experiment form on a GAML editor: creation (gui, batch or test, appended to the model) or edition
 * of the experiment whose header is selected. The result is applied as one undoable document change, refused when the
 * document changed meanwhile.
 */
public final class ExperimentWizardHandler {

	@Inject private IParser parser;

	/** True when the selection is on an experiment header (not inside its body). */
	public boolean canEdit(final GamlEditor editor) {
		if (!editor.isEditable() || !(editor.getSelectionProvider().getSelection() instanceof ITextSelection selection))
			return false;
		return editor.getDocument().readOnly(resource -> {
			if (resource.getParseResult() == null || resource.getParseResult().hasSyntaxErrors()) return false;
			try {
				return new ExperimentSource(editor.getDocument().get(), selection.getOffset(), parser, false,
						ExperimentDraft.GUI).isExperimentSelection(selection.getOffset(), selection.getLength());
			} catch (IllegalArgumentException error) {
				return false;
			}
		});
	}

	public void open(final GamlEditor editor, final boolean create) {
		var shell = editor.getEditorSite().getShell();
		if (!editor.isEditable()) {
			MessageDialog.openError(shell, "Experiment", "This editor is read-only.");
			return;
		}
		if (!create && !canEdit(editor)) return;
		try {
			var transaction = new SpeciesDocumentEdit(editor.getDocument());
			int offset = editor.getSelectionProvider().getSelection() instanceof ITextSelection selection
					? selection.getOffset() : -1;
			var source = new ExperimentSource(transaction.snapshot(), offset, parser, create, ExperimentDraft.GUI);
			URI uri = isolatedUri(editor);
			var dialog = new ExperimentWizard(shell, source.draft(), draft -> {
				if (!transaction.isCurrent()) return "The document changed. Cancel and reopen the experiment form.";
				return source.validationError(draft);
			}, typeNames(), source::globalNames, draft -> source.bodyContext(draft, uri), source::parentNames);
			dialog.setCreationMode(create);
			if (dialog.open() == Window.OK && dialog.getResult() != null)
				transaction.apply(source.render(dialog.getResult()));
		} catch (IllegalArgumentException | IllegalStateException | BadLocationException error) {
			MessageDialog.openError(shell, "Experiment", error.getMessage());
		}
	}

	private static List<String> typeNames() {
		List<String> names = new ArrayList<>();
		try {
			Types.getTypeNames().forEach(names::add);
		} catch (RuntimeException | LinkageError unavailable) {
			names.addAll(List.of("int", "float", "bool", "string", "point", "list", "map", "rgb"));
		}
		return names.stream().distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
	}

	/** A sibling URI of the edited file, so relative imports resolve, but never the file itself. */
	private static URI isolatedUri(final GamlEditor editor) {
		URI uri = editor.getDocument().readOnly(resource -> resource.getURI());
		if (uri == null || uri.segmentCount() == 0)
			return URI.createURI("synthetic:/experiment-wizard/.experiment-wizard.gaml");
		return uri.trimSegments(1).appendSegment(".experiment-wizard.gaml");
	}
}

package gaml.compiler.ui.wizards.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

/** Opens the shared action form on a GAML editor: creation at the caret's owner, or edition of the caret's action. */
public final class ActionWizardHandler {

	@Inject private IParser parser;

	/** True when the selection is on an action header (not inside its code). */
	public boolean canEdit(final GamlEditor editor) {
		if (!editor.isEditable() || !(editor.getSelectionProvider().getSelection() instanceof ITextSelection selection))
			return false;
		return editor.getDocument().readOnly(resource -> {
			if (resource.getParseResult() == null || resource.getParseResult().hasSyntaxErrors()) return false;
			try {
				return new ActionSource(editor.getDocument().get(), selection.getOffset(), parser, false)
						.isActionSelection(selection.getOffset(), selection.getLength());
			} catch (IllegalArgumentException error) {
				return false;
			}
		});
	}

	public void open(final GamlEditor editor, final boolean create) {
		var shell = editor.getEditorSite().getShell();
		if (!editor.isEditable()) {
			MessageDialog.openError(shell, "Action", "This editor is read-only.");
			return;
		}
		if (!create && !canEdit(editor)) return;
		try {
			var transaction = new SpeciesDocumentEdit(editor.getDocument());
			int offset = editor.getSelectionProvider().getSelection() instanceof ITextSelection selection
					? selection.getOffset() : -1;
			var source = new ActionSource(transaction.snapshot(), offset, parser, create);
			URI uri = isolatedUri(editor);
			Set<String>[] baseline = new Set[1];
			var dialog = new ActionWizard(shell, source.draft(), draft -> {
				if (!transaction.isCurrent()) return "The document changed. Cancel and reopen the action form.";
				String error = source.validationError(draft);
				if (error != null || !draft.virtual() || draft == source.draft()) return error;
				// Virtual code is not compiled by the form; the rendered document is checked for new errors instead.
				if (baseline[0] == null) baseline[0] = messages(documentErrors(uri, transaction.snapshot()));
				for (var problem : documentErrors(uri, source.render(draft)))
					if (!baseline[0].contains(problem.message())) return problem.message();
				return null;
			}, typeNames(source), draft -> source.bodyContext(draft, uri)) {
				@Override
				protected String signature(final ActionDraft draft) {
					return draft.virtual() && source.keepsBlockWhenVirtual() ? draft.header() + " virtual: true {"
							: super.signature(draft);
				}
			};
			dialog.setCreationMode(create);
			if (dialog.open() == Window.OK && dialog.getResult() != null)
				transaction.apply(source.render(dialog.getResult()));
		} catch (IllegalArgumentException | IllegalStateException | BadLocationException error) {
			MessageDialog.openError(shell, "Action", error.getMessage());
		}
	}

	private static List<ActionVerifier.Problem> documentErrors(final URI uri, final String text) {
		return ActionVerifier.verify(new ActionBodyContext(uri, text, ""), "");
	}

	private static Set<String> messages(final List<ActionVerifier.Problem> problems) {
		Set<String> result = new HashSet<>();
		for (var problem : problems) result.add(problem.message());
		return result;
	}

	/** A sibling URI of the edited file, so relative imports resolve, but never the file itself. */
	private static URI isolatedUri(final GamlEditor editor) {
		URI uri = editor.getDocument().readOnly(resource -> resource.getURI());
		if (uri == null || uri.segmentCount() == 0) return URI.createURI("synthetic:/action-wizard/.action-wizard.gaml");
		return uri.trimSegments(1).appendSegment(".action-wizard.gaml");
	}

	private static List<String> typeNames(final ActionSource source) {
		List<String> result = new ArrayList<>();
		Types.getTypeNames().forEach(result::add);
		result.addAll(source.localTypeNames());
		return result.stream().distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
	}
}

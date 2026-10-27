package gaml.compiler.ui.wizards.species;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;

/** Optimistic document transaction. A single replace is a single editor undo step. */
public final class SpeciesDocumentEdit {

	private final IDocument document;
	private final String snapshot;
	private final long stamp;

	public SpeciesDocumentEdit(final IDocument document) {
		this.document = document;
		snapshot = document.get();
		stamp = document instanceof IDocumentExtension4 extension ? extension.getModificationStamp()
				: IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
	}

	public String snapshot() { return snapshot; }

	public boolean isCurrent() {
		return snapshot.equals(document.get()) && (!(document instanceof IDocumentExtension4 extension)
				|| stamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP || stamp == extension.getModificationStamp());
	}

	/** Returns false for a no-op. Stale positions are never rebased or applied silently. */
	public boolean apply(final String replacement) throws BadLocationException {
		if (!isCurrent()) throw new IllegalStateException("The document changed while the species form was open. Reopen it.");
		if (snapshot.equals(replacement)) return false;
		int start = 0;
		while (start < snapshot.length() && start < replacement.length()
				&& snapshot.charAt(start) == replacement.charAt(start)) start++;
		int end = snapshot.length();
		int replacementEnd = replacement.length();
		while (end > start && replacementEnd > start && snapshot.charAt(end - 1) == replacement.charAt(replacementEnd - 1)) {
			end--;
			replacementEnd--;
		}
		var undo = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
		if (undo != null) undo.beginCompoundChange();
		try {
			document.replace(start, end - start, replacement.substring(start, replacementEnd));
		} finally {
			if (undo != null) undo.endCompoundChange();
		}
		return true;
	}
}

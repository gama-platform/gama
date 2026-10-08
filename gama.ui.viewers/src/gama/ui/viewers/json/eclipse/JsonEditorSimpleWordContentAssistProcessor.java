/*******************************************************************************************************
 *
 * JsonEditorSimpleWordContentAssistProcessor.java, in gama.ui.viewers, is part of the source code of the GAMA
 * modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CODE_ASSIST_ADD_KEYWORDS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CODE_ASSIST_ADD_SIMPLEWORDS;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.BoldStylerProvider;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension7;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import gama.ui.viewers.json.SimpleWordCodeCompletion;
import gama.ui.viewers.json.SimpleWordListBuilder;
import gama.ui.viewers.json.WordListBuilder;
import gama.ui.viewers.json.eclipse.document.keywords.DocumentKeyWord;
import gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferences;

/**
 * The Class JsonEditorSimpleWordContentAssistProcessor.
 */
public class JsonEditorSimpleWordContentAssistProcessor
		implements IContentAssistProcessor, ICompletionListener {

	/** The Constant WORD_LIST_BUILDER. */
	private static final SimpleWordListBuilder WORD_LIST_BUILDER = new SimpleWordListBuilder();

	/** The Constant NO_WORD_BUILDER. */
	private static final NoWordListBuilder NO_WORD_BUILDER = new NoWordListBuilder();

	/** The error message. */
	private String errorMessage;

	/** The simple word completion. */
	private final SimpleWordCodeCompletion simpleWordCompletion = new SimpleWordCodeCompletion();

	@Override
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
		IDocument document = viewer.getDocument();
		if (document == null) return null;
		String source = document.get();

		Set<String> words = simpleWordCompletion.calculate(source, offset);

		ICompletionProposal[] result = new ICompletionProposal[words.size()];
		int i = 0;
		for (String word : words) { result[i++] = new SimpleWordProposal(document, offset, word); }

		return result;
	}

	@Override
	public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int offset) {
		return null;
	}

	/**
	 * The Class SimpleWordProposal.
	 */
	private class SimpleWordProposal implements ICompletionProposal, ICompletionProposalExtension7 {

		/** The offset. */
		private final int offset;

		/** The word. */
		private final String word;

		/** The next selection. */
		private int nextSelection;

		/** The styled string. */
		private StyledString styledString;

		/** The text before. */
		private final String textBefore;

		/**
		 * Instantiates a new simple word proposal.
		 *
		 * @param document
		 *            the document
		 * @param offset
		 *            the offset
		 * @param word
		 *            the word
		 */
		SimpleWordProposal(final IDocument document, final int offset, final String word) {
			this.offset = offset;
			this.word = word;

			String source = document.get();
			textBefore = simpleWordCompletion.getTextbefore(source, offset);
		}

		@Override
		public void apply(final IDocument document) {
			// the proposal shall enter always a space after applyment...
			String proposal = word;
			if (isAddingSpaceAtEnd()) { proposal += " "; }
			int zeroOffset = offset - textBefore.length();
			try {
				document.replace(zeroOffset, textBefore.length(), proposal);
				nextSelection = zeroOffset + proposal.length();
			} catch (BadLocationException e) {
				JsonEditorUtil.logError("Not able to replace by proposal:" + word + ", zero offset:"
						+ zeroOffset + ", textBefore:" + textBefore, e);
			}

		}

		@Override
		public Point getSelection(final IDocument document) {
			Point point = new Point(nextSelection, 0);
			return point;
		}

		@Override
		public String getAdditionalProposalInfo() { return null; }

		@Override
		public String getDisplayString() { return word; }

		@Override
		public Image getImage() { return null; }

		@Override
		public IContextInformation getContextInformation() { return null; }

		@Override
		public StyledString getStyledDisplayString(final IDocument document, final int offset,
				final BoldStylerProvider boldStylerProvider) {
			if (styledString != null) return styledString;
			styledString = new StyledString();
			styledString.append(word);
			try {

				int enteredTextLength = textBefore.length();
				int indexOfTextBefore = word.toLowerCase().indexOf(textBefore.toLowerCase());

				if (indexOfTextBefore != -1) {
					styledString.setStyle(indexOfTextBefore, enteredTextLength, boldStylerProvider.getBoldStyler());
				}
			} catch (RuntimeException e) {
				JsonEditorUtil.logError("Not able to set styles for proposal:" + word, e);
			}
			return styledString;
		}

	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() { return null; }

	/**
	 * Checks if is adding space at end.
	 *
	 * @return true, if is adding space at end
	 */
	public boolean isAddingSpaceAtEnd() { return true; }

	@Override
	public char[] getContextInformationAutoActivationCharacters() { return null; }

	@Override
	public String getErrorMessage() { return errorMessage; }

	@Override
	public IContextInformationValidator getContextInformationValidator() { return null; }

	/**
	 * Gets the completion listener.
	 *
	 * @return the completion listener
	 */
	public ICompletionListener getCompletionListener() { return this; }

	/* completion listener parts: */

	@Override
	public void assistSessionStarted(final ContentAssistEvent event) {
		simpleWordCompletion.reset();

		JsonEditorPreferences preferences = JsonEditorPreferences.getInstance();
		boolean addKeyWords = preferences.getBooleanPreference(P_CODE_ASSIST_ADD_KEYWORDS);
		boolean addSimpleWords = preferences.getBooleanPreference(P_CODE_ASSIST_ADD_SIMPLEWORDS);

		if (addSimpleWords) {
			simpleWordCompletion.setWordListBuilder(WORD_LIST_BUILDER);
		} else {
			simpleWordCompletion.setWordListBuilder(NO_WORD_BUILDER);
		}
		if (addKeyWords) { addAllJsonKeyWords(); }
	}

	/**
	 * Adds the all highspeed JSON key words.
	 */
	protected void addAllJsonKeyWords() {}

	/**
	 * Adds the key word.
	 *
	 * @param keyword
	 *            the keyword
	 */
	protected void addKeyWord(final DocumentKeyWord keyword) {
		simpleWordCompletion.add(keyword.getText());
	}

	@Override
	public void assistSessionEnded(final ContentAssistEvent event) {
		simpleWordCompletion.reset();// clean up...
	}

	@Override
	public void selectionChanged(final ICompletionProposal proposal, final boolean smartToggle) {

	}

	/**
	 * The Class NoWordListBuilder.
	 */
	private static class NoWordListBuilder implements WordListBuilder {

		/**
		 * Instantiates a new no word list builder.
		 */
		private NoWordListBuilder() {

		}

		/** The list. */
		private final List<String> list = new ArrayList<>(0);

		/**
		 * Builds the.
		 *
		 * @param source
		 *            the source
		 * @return the list
		 */
		@Override
		public List<String> build(final String source) {
			return list;
		}

	}
}

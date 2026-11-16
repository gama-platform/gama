/*******************************************************************************************************
 *
 * GamlContentAssistProcessor.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.contentassist;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.contentassist.CompletionProposalComputer;
import org.eclipse.xtext.ui.editor.contentassist.XtextContentAssistProcessor;

import gaml.compiler.ui.contentassist.GamlProposalProvider.GamlCompletionProposal;

public class GamlContentAssistProcessor extends XtextContentAssistProcessor {

	@Override
	protected CompletionProposalComputer createCompletionProposalComputer(final ITextViewer viewer, final int offset) {
		return new CompletionProposalComputer(this, viewer, offset) {
			// Group across parser contexts before Xtext deduplicates proposals by their display text.
			private final Map<String, List<GamlCompletionProposal>> groups = new LinkedHashMap<>();

			@Override
			public void accept(final ICompletionProposal proposal) {
				if (proposal instanceof GamlCompletionProposal completion) {
					groups.computeIfAbsent(completion.getReplacementString(), name -> new ArrayList<>()).add(completion);
				} else {
					super.accept(proposal);
				}
			}

			@Override
			public ICompletionProposal[] exec(final XtextResource resource) throws Exception {
				final List<ICompletionProposal> result = new ArrayList<>(List.of(super.exec(resource)));
				for (final List<GamlCompletionProposal> group : groups.values()) {
					result.add(GamlCompletionProposal.group(group));
				}
				return result.toArray(ICompletionProposal[]::new);
			}
		};
	}

}

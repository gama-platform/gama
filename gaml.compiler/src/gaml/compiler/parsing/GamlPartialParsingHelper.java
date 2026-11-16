/*******************************************************************************************************
 *
 * GamlPartialParsingHelper.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.parsing;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.parser.impl.PartialParsingHelper;

/**
 * Keeps incremental replacement roots consistent with the semantic objects they represent.
 */
public class GamlPartialParsingHelper extends PartialParsingHelper {

	@Override
	protected void filterInvalidRootNodes(final ICompositeNode rootNode, final List<ICompositeNode> candidates) {
		super.filterInvalidRootNodes(rootNode, candidates);
		// Filter every fallback, not just the deepest: lookahead can force selection of an outer node.
		candidates.removeIf(candidate -> !candidate.hasDirectSemanticElement()
				&& candidate.getGrammarElement() instanceof RuleCall call
				&& call.getRule() instanceof ParserRule rule && rule.getType().getClassifier() instanceof EClass type
				&& !type.isInstance(candidate.getSemanticElement()));
	}
}

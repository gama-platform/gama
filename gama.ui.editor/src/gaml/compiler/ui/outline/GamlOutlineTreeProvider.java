/*******************************************************************************************************
 *
 * GamlOutlineTreeProvider.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.outline;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
import org.eclipse.xtext.ui.editor.outline.impl.AbstractOutlineNode;
import org.eclipse.xtext.ui.editor.outline.impl.BackgroundOutlineTreeProvider;

import com.google.inject.Inject;

import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.factories.DescriptionFactory;
import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.ExperimentFileStructure;
import gaml.compiler.gaml.HeadlessExperiment;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.S_Action;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Global;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.util.GamlSwitch;
import gaml.compiler.ui.labeling.GamlLabelProvider;

/**
 * customization of the default outline structure
 *
 */
public class GamlOutlineTreeProvider extends BackgroundOutlineTreeProvider {

	/** The provider. */
	@Inject private GamlLabelProvider provider;

	/** The Constant FOUND. */
	final static Object FOUND = new Object();

	@Override
	public void createChildren(final IOutlineNode parentNode, final EObject stm) {
		if (stm != null && parentNode.hasChildren()) {
			new GamlSwitch<>() {

				@Override
				public Object caseModel(final Model stm) {

					Block block = stm.getBlock();
					if (block != null) {
						for (final Statement s : EGaml.getInstance().getStatementsOf(block)) {
							if (s instanceof S_Global) {
								block = s.getBlock();
								if (block != null) { ownCreateChildren(parentNode, block, true); }
							} else {
								createNode(parentNode, s);
							}
						}
					}
					return FOUND;
				}

				@Override
				public Object caseExperimentFileStructure(final ExperimentFileStructure stm) {
					return caseHeadlessExperiment(stm.getExp());
				}

				@Override
				public Object caseS_Experiment(final S_Experiment stm) {
					ownCreateChildren(parentNode, stm.getBlock(), true);
					return FOUND;
				}

				@Override
				public Object caseHeadlessExperiment(final HeadlessExperiment stm) {
					ownCreateChildren(parentNode, stm.getBlock(), true);
					return FOUND;
				}

				@Override
				public Object caseS_Species(final S_Species stm) {
					ownCreateChildren(parentNode, stm.getBlock(), true);
					return FOUND;
				}

				public Object defaultCase(final EObject e) {
					final String key = EGaml.getInstance().getKeyOf(e);
					if (IKeyword.OUTPUT.equals(key)) {
						Statement stm = (Statement) e;
						ownCreateChildren(parentNode, stm.getBlock(), false);
						return FOUND;
					}
					return null;
				}

			}.doSwitch(stm);
		}
	}

	/**
	 * Own create children.
	 *
	 * @param parentNode
	 *            the parent node
	 * @param block
	 *            the block
	 */
	protected void ownCreateChildren(final IOutlineNode parentNode, final Block block,
			final boolean categorizeChildren) {

		IOutlineNode attributesNode = null;
		IOutlineNode parametersNode = null;
		IOutlineNode actionsNode = null;
		if (block != null && categorizeChildren) {
			for (final Statement s : EGaml.getInstance().getStatementsOf(block)) {
				if (isParameter(s)) {
					if (parametersNode == null) {
						parametersNode = new AbstractOutlineNode(parentNode,
								provider.convertToImageDescriptor("_parameter.png"), "Parameters", false) {};
					}
					createNode(parametersNode, s);
				} else if (isAttribute(s)) {
					if (attributesNode == null) {
						attributesNode = new AbstractOutlineNode(parentNode,
								provider.convertToImageDescriptor("_attributes.png"), "Attributes", false) {};
					}
					createNode(attributesNode, s);
				} else if (isAction(s)) {
					if (actionsNode == null) {
						actionsNode = new AbstractOutlineNode(parentNode,
								provider.convertToImageDescriptor("_action.png"), "Actions", false) {};
					}
					createNode(actionsNode, s);

				}

				else {
					createNode(parentNode, s);
				}
			}
		} else {
			EGaml.getInstance().getStatementsOf(block).forEach(s -> createNode(parentNode, s));
		}
	}

	/**
	 * Checks if is parameter.
	 *
	 * @param s
	 *            the s
	 * @return true, if is parameter
	 */
	private boolean isParameter(final Statement s) {
		final String key = EGaml.getInstance().getKeyOf(s);
		return IKeyword.PARAMETER.equals(key) || IKeyword.TEXT.equals(key) || IKeyword.USER_COMMAND.equals(key)
				|| IKeyword.CATEGORY.equals(key);
	}

	/**
	 * @param s
	 * @return
	 */
	public static boolean isAttribute(final Statement s) {
		if (!(s instanceof S_Definition)) return false;
		final String key = EGaml.getInstance().getKeyOf(s);
		if (IKeyword.ACTION.equals(key)) return false;
		// if (s.getBlock() != null && s.getBlock().getFunction() == null) { return false; }
		final SymbolProto p = DescriptionFactory.getStatementProto(key);
		if (p != null && p.getKind() == ISymbolKind.BATCH_METHOD) return false;
		return true;
	}

	/**
	 * Checks if is action.
	 *
	 * @param s
	 *            the s
	 * @return true, if is action
	 */
	public static boolean isAction(final Statement s) {
		if (!(s instanceof S_Definition)) return false;
		if (s instanceof S_Action) return true;
		final String key = EGaml.getInstance().getKeyOf(s);
		final SymbolProto p = DescriptionFactory.getStatementProto(key);
		if (p != null && p.isTopLevel()) return false;
		if (s.getKey() == null) return true;
		return false;
	}

	@Override
	protected Object getText(final Object modelElement) {
		if (modelElement instanceof S_Global) return null;
		return super.getText(modelElement);
	}

	@Override
	protected boolean isLeaf(final EObject s) {
		if (s instanceof S_Experiment)
			return ((S_Experiment) s).getBlock() == null || ((S_Experiment) s).getBlock().getStatements().isEmpty();
		if (s instanceof S_Species)
			return ((S_Species) s).getBlock() == null || ((S_Species) s).getBlock().getStatements().isEmpty();
		if (s instanceof Model) return ((Model) s).getBlock() == null;
		final String key = EGaml.getInstance().getKeyOf(s);
		if (IKeyword.OUTPUT.equals(key)) return false;
		return true;
	}
}

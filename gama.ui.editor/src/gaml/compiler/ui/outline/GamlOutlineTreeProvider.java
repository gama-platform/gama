/*******************************************************************************************************
 *
 * GamlOutlineTreeProvider.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.outline;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
import org.eclipse.xtext.ui.editor.outline.impl.AbstractOutlineNode;
import org.eclipse.xtext.ui.editor.outline.impl.BackgroundOutlineTreeProvider;

import com.google.inject.Inject;

import gama.annotations.constants.IKeyword;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.ast.IGamlLabelProvider.OutlineGroup;
import gaml.compiler.EGaml;
import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.S_Action;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Global;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.StandaloneExperiment;
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
				public Object caseModel(final Model stm1) {

					Block block = stm1.getBlock();
					if (block != null) {
						for (final Statement s : EGaml.getInstance().getStatementsOf(block)) {
							if (s instanceof S_Global) {
								block = s.getBlock();
								if (block != null) { ownCreateChildren(parentNode, block, true, false); }
							} else {
								createNode(parentNode, s);
							}
						}
					}
					return FOUND;
				}

				@Override
				public Object caseS_Experiment(final S_Experiment stm1) {
					ownCreateChildren(parentNode, stm1.getBlock(), true, true);
					return FOUND;
				}

				@Override
				public Object caseStandaloneExperiment(final StandaloneExperiment stm1) {
					ownCreateChildren(parentNode, stm1.getBlock(), true, true);
					return FOUND;
				}

				@Override
				public Object caseS_Species(final S_Species stm1) {
					ownCreateChildren(parentNode, stm1.getBlock(), true, false);
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
	 * @param categorizeChildren
	 *            whether to group the children in folders
	 */
	protected void ownCreateChildren(final IOutlineNode parentNode, final Block block,
			final boolean categorizeChildren) {
		ownCreateChildren(parentNode, block, categorizeChildren, false);
	}

	/**
	 * Own create children. Grouped children are placed in folders created in the order of {@link OutlineGroup},
	 * followed by the other children.
	 *
	 * @param parentNode
	 *            the parent node
	 * @param block
	 *            the block
	 * @param categorizeChildren
	 *            whether to group the children in folders
	 * @param inExperiment
	 *            whether the block is the one of an experiment (behaviors and aspects are then not grouped)
	 */
	protected void ownCreateChildren(final IOutlineNode parentNode, final Block block,
			final boolean categorizeChildren, final boolean inExperiment) {
		if (block == null) return;
		final List<Statement> statements = EGaml.getInstance().getStatementsOf(block);
		if (!categorizeChildren) {
			statements.forEach(s -> createNode(parentNode, s));
			return;
		}
		final Map<OutlineGroup, List<Statement>> groups = new EnumMap<>(OutlineGroup.class);
		final List<Statement> others = new ArrayList<>();
		for (final Statement s : statements) {
			final OutlineGroup g = groupOf(s, inExperiment);
			if (g == null) {
				others.add(s);
			} else {
				groups.computeIfAbsent(g, k -> new ArrayList<>()).add(s);
			}
		}
		groups.forEach((g, list) -> {
			final IOutlineNode node = new AbstractOutlineNode(parentNode,
					provider.convertToImageDescriptor(g.icon + ".png"), g.label, false) {};
			list.forEach(s -> createNode(node, s));
		});
		others.forEach(s -> createNode(parentNode, s));
	}

	/**
	 * Returns the group in which a statement is displayed in outlines.
	 *
	 * @param s
	 *            the statement
	 * @param inExperiment
	 *            whether the statement belongs to an experiment
	 * @return the group or null
	 */
	public static OutlineGroup groupOf(final Statement s, final boolean inExperiment) {
		if (s == null) return null;
		if (isParameter(s)) return OutlineGroup.PARAMETERS;
		if (isAttribute(s)) return OutlineGroup.ATTRIBUTES;
		if (isAction(s)) return OutlineGroup.ACTIONS;
		if (isTest(s)) return OutlineGroup.TESTS;
		if (inExperiment) return null;
		if (isAspect(s)) return OutlineGroup.ASPECTS;
		if (isBehavior(s)) return OutlineGroup.BEHAVIORS;
		return null;
	}

	/**
	 * Checks if is parameter.
	 *
	 * @param s
	 *            the s
	 * @return true, if is parameter
	 */
	public static boolean isParameter(final Statement s) {
		final String key = EGaml.getInstance().getKeyOf(s);
		return IKeyword.PARAMETER.equals(key) || IKeyword.TEXT.equals(key) || IKeyword.USER_COMMAND.equals(key)
				|| IKeyword.CATEGORY.equals(key);
	}

	/**
	 * @param s
	 * @return
	 */
	public static boolean isAttribute(final Statement s) {
		if (!(s instanceof S_Definition) || s instanceof S_Action) return false;
		final String key = EGaml.getInstance().getKeyOf(s);
		if (IKeyword.ACTION.equals(key)) return false;
		// if (s.getBlock() != null && s.getBlock().getFunction() == null) { return false; }
		final IArtefact.Symbol p = ArtefactRegistry.getStatementArtefact(key);
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
		return s instanceof S_Action
				|| s instanceof S_Definition && IKeyword.ACTION.equals(EGaml.getInstance().getKeyOf(s));
	}

	/**
	 * Checks if is test.
	 *
	 * @param s
	 *            the s
	 * @return true, if is test
	 */
	public static boolean isTest(final Statement s) {
		return IKeyword.TEST.equals(EGaml.getInstance().getKeyOf(s));
	}

	/**
	 * Checks if is aspect.
	 *
	 * @param s
	 *            the s
	 * @return true, if is aspect
	 */
	public static boolean isAspect(final Statement s) {
		return IKeyword.ASPECT.equals(EGaml.getInstance().getKeyOf(s));
	}

	/**
	 * Checks if is a behavior (reflex, init, state, perceive, rule, plan...).
	 *
	 * @param s
	 *            the s
	 * @return true, if is behavior
	 */
	public static boolean isBehavior(final Statement s) {
		final String key = EGaml.getInstance().getKeyOf(s);
		if (key == null || IKeyword.ASPECT.equals(key) || IKeyword.ACTION.equals(key) || IKeyword.TEST.equals(key))
			return false;
		final IArtefact.Symbol p = ArtefactRegistry.getStatementArtefact(key);
		return p != null && p.getKind() == ISymbolKind.BEHAVIOR;
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

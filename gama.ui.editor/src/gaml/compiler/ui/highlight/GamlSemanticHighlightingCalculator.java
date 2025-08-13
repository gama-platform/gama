/*******************************************************************************************************
 *
 * GamlSemanticHighlightingCalculator.java, in gama.ui.shared.modeling, is part of the source code of the GAMA modeling
 * and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.highlight;

import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.ASSIGN_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.FACET_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.KEYWORD_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.NUMBER_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.OPERATOR_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.PRAGMA_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.RESERVED_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.TASK_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.TYPE_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.UNIT_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.VARDEF_ID;
import static gaml.compiler.ui.highlight.DelegateHighlightingConfiguration.VARIABLE_ID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor;
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.tasks.ITaskFinder;
import org.eclipse.xtext.tasks.Task;
import org.eclipse.xtext.util.CancelIndicator;

import com.google.inject.Inject;

import gama.core.common.util.StringUtils;
import gama.dev.DEBUG;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.ArgumentDefinition;
import gaml.compiler.gaml.ArgumentPair;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.GamlDefinition;
import gaml.compiler.gaml.GamlDefinitionUtils;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.HeadlessExperiment;
import gaml.compiler.gaml.Parameter;
import gaml.compiler.gaml.Pragma;
import gaml.compiler.gaml.S_Assignment;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Display;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.StringLiteral;

/**
 *
 * @author Pierrick cf. http://www.eclipse.org/Xtext/documentation/latest/xtext.html# highlighting
 *
 */
public class GamlSemanticHighlightingCalculator implements ISemanticHighlightingCalculator {

	/** The task finder. */
	@Inject private ITaskFinder taskFinder;

	/** The assignments. */
	private static Set<String> ASSIGNMENTS =
			new HashSet<>(Arrays.asList("<-", "<<", ">>", "->", "<+", ">-", "<<+", ">>-", "+<-"));

	/** The acceptor. */
	private IHighlightedPositionAcceptor acceptor;

	/** The done. */
	Set<INode> done = new HashSet<>();

	@Override
	public void provideHighlightingFor(final XtextResource resource, final IHighlightedPositionAcceptor arg1,
			final CancelIndicator arg2) {
		if (resource == null) return;
		acceptor = arg1;
		final var root = resource.getAllContents();
		while (root.hasNext()) { process(root.next()); }
		done.clear();
		highlightTasks(resource);
	}

	/**
	 * Highlight tasks.
	 *
	 * @param resource
	 *            the resource
	 * @param acceptor
	 *            the acceptor
	 */
	protected void highlightTasks(final XtextResource resource) {
		final var tasks = taskFinder.findTasks(resource);
		for (final Task task : tasks) { acceptor.addPosition(task.getOffset(), task.getTagLength(), TASK_ID); }
	}

	/**
	 * Process.
	 *
	 * @param object
	 *            the object
	 */
	void process(final EObject object) {
		if (object == null) return;
		process(object, object.eClass());
	}

	/**
	 * Process.
	 *
	 * @param object
	 *            the object
	 * @param clazz
	 *            the clazz
	 */
	void process(final EObject object, final EClass clazz) {
		final var id = clazz.getClassifierID();
		if ("envelope".equals(EGaml.getInstance().getKeyOf(object))) {

			DEBUG.OUT("");

		}
		switch (id) {
			case GamlPackage.PRAGMA:
				setStyle(object, PRAGMA_ID, ((Pragma) object).getName(), false);
				break;
			case GamlPackage.SASSIGNMENT:
				final var s = ((S_Assignment) object).getKey();
				setStyle(object, ASSIGN_ID, s, false);
				break;
			case GamlPackage.FACET:
				final var f = (Facet) object;
				final var key = f.getKey();
				if (ASSIGNMENTS.contains(key)) {
					setStyle(object, ASSIGN_ID, 0);
				} else {
					setStyle(object, FACET_ID, 0);
					if (key.startsWith("type")) {
						setStyle(TYPE_ID, NodeModelUtils.getNode(f.getExpr()));
					} else if (f.getName() != null) { setStyle(object, VARDEF_ID, 1); }
				}
				break;
			case GamlPackage.TERMINAL_EXPRESSION:
				if (!(object instanceof StringLiteral)) { setStyle(object, NUMBER_ID, 0); }
				break;
			case GamlPackage.RESERVED_LITERAL:
				setStyle(object, RESERVED_ID, 0);
				break;
			case GamlPackage.BINARY_OPERATOR:
			case GamlPackage.FUNCTION:
				setStyle(object, OPERATOR_ID, EGaml.getInstance().getKeyOf(object), true);
				break;
			case GamlPackage.ARGUMENT_PAIR:
				setStyle(object, VARIABLE_ID, ((ArgumentPair) object).getOp(), false);
				break;
			case GamlPackage.VARIABLE_REF:
				setStyle(VARIABLE_ID, NodeModelUtils.getNode(object));
				break;
			case GamlPackage.UNIT_NAME:
				setStyle(object, UNIT_ID, 0);
				break;
			case GamlPackage.TYPE_REF:
				final var st = EGaml.getInstance().getStatement(object);
				if (st instanceof S_Definition && ((S_Definition) st).getTkey() == object) {
					setStyle(KEYWORD_ID, NodeModelUtils.findActualNodeFor(object));
				} else {
					setStyle(TYPE_ID, NodeModelUtils.getNode(object));
				}
				break;
			case GamlPackage.PARAMETER:
				setStyle(object, VARIABLE_ID, ((Parameter) object).getBuiltInFacetKey(), false);
				break;
			case GamlPackage.ARGUMENT_DEFINITION:
				setStyle(object, VARDEF_ID, ((ArgumentDefinition) object).getName(), false);
				break;
			case GamlPackage.STATEMENT:
				Statement stat = (Statement) object;
				String name = findNameOf(stat);
				if (name != null) { setStyle(stat, VARDEF_ID, name, false); }
				setStyle(stat, KEYWORD_ID, stat.getKey(), false);
				break;
			default:
				final List<EClass> eSuperTypes = clazz.getESuperTypes();
				if (!eSuperTypes.isEmpty()) { process(object, eSuperTypes.get(0)); }
		}
	}

	/**
	 * Find name of.
	 *
	 * @param o
	 *            the o
	 * @return the string
	 */
	private String findNameOf(final EObject o) {
		if (o instanceof GamlDefinition gd) return GamlDefinitionUtils.getName(gd);
		if (o instanceof S_Display sd) return sd.getName();
		if (o instanceof HeadlessExperiment he) return he.getName();

		return null;
	}

	/**
	 * Sets the style.
	 *
	 * @param obj
	 *            the obj
	 * @param s
	 *            the s
	 * @param position
	 *            the position
	 * @return true, if successful
	 */
	private final boolean setStyle(final EObject obj, final String s, final int position) {
		// position = -1 for all the node; 0 for the first leaf node, 1 for the
		// second one, etc.
		if (obj != null && s != null) {
			INode n = NodeModelUtils.getNode(obj);
			if (n == null) return false;
			if (position > -1) {
				var i = 0;
				for (final ILeafNode node : n.getLeafNodes()) {
					if (!node.isHidden()) {
						if (position == i) {
							n = node;
							break;
						}
						i++;
					}
				}
			}
			return setStyle(s, n);
		}
		return false;
	}

	/**
	 * Sets the style.
	 *
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return true, if successful
	 */
	private final boolean setStyle(final String s, final INode n) {
		if (!done.contains(n) && n != null) {
			done.add(n);
			acceptor.addPosition(n.getOffset(), n.getLength(), s);
			return true;
		}
		return false;
	}

	/**
	 * Sets the style.
	 *
	 * @param obj
	 *            the obj
	 * @param s
	 *            the s
	 * @param text
	 *            the text
	 * @param all
	 *            the all
	 * @return true, if successful
	 */
	private final boolean setStyle(final EObject obj, final String s, final String text, final boolean all) {
		if (text == null) return false;
		if (obj != null && s != null) {
			boolean b = true;
			INode n = NodeModelUtils.getNode(obj);
			if (n == null) return false;
			for (final ILeafNode node : n.getLeafNodes()) {
				if (!node.isHidden()) {
					final var sNode = StringUtils.toJavaString(NodeModelUtils.getTokenText(node));
					if (equalsFacetOrString(text, sNode)) {
						n = node;
						b = setStyle(s, n);
						if (!all) { break; }
					}
				}
			}
			return b;
		}
		return false;
	}

	/**
	 * Equals facet or string.
	 *
	 * @param text
	 *            the text
	 * @param s
	 *            the s
	 * @return true, if successful
	 */
	boolean equalsFacetOrString(final String text, final String s) {
		if (s == null || text == null) return false;
		if (s.equals(text)) return true;
		final var length = s.length();
		if (length == 0) return false;
		final var last = s.charAt(length - 1);
		switch (last) {
			case ':':
				return text.equals(s.substring(0, length - 1));
			case '\"':
			case '\'':
				return s.charAt(0) == last && length > 1 && text.equals(s.substring(1, length - 1));

		}
		return false;
	}

}

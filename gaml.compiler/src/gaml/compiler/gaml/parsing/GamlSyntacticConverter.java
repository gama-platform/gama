/*******************************************************************************************************
 *
 * GamlSyntacticConverter.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.parsing;

import static gama.api.constants.IKeyword.ACTION;
import static gama.api.constants.IKeyword.ADD;
import static gama.api.constants.IKeyword.ALL;
import static gama.api.constants.IKeyword.ARG;
import static gama.api.constants.IKeyword.AT;
import static gama.api.constants.IKeyword.DEFAULT;
import static gama.api.constants.IKeyword.DISPLAY;
import static gama.api.constants.IKeyword.ELSE;
import static gama.api.constants.IKeyword.EQUALS;
import static gama.api.constants.IKeyword.EQUATION;
import static gama.api.constants.IKeyword.EQUATION_LEFT;
import static gama.api.constants.IKeyword.EQUATION_RIGHT;
import static gama.api.constants.IKeyword.EXPERIMENT;
import static gama.api.constants.IKeyword.FROM;
import static gama.api.constants.IKeyword.FUNCTION;
import static gama.api.constants.IKeyword.GRID;
import static gama.api.constants.IKeyword.GRID_LAYER;
import static gama.api.constants.IKeyword.GUI_;
import static gama.api.constants.IKeyword.IMAGE;
import static gama.api.constants.IKeyword.IMAGE_LAYER;
import static gama.api.constants.IKeyword.IN;
import static gama.api.constants.IKeyword.INDEX;
import static gama.api.constants.IKeyword.INIT;
import static gama.api.constants.IKeyword.INTERNAL_FUNCTION;
import static gama.api.constants.IKeyword.ITEM;
import static gama.api.constants.IKeyword.LET;
import static gama.api.constants.IKeyword.METHOD;
import static gama.api.constants.IKeyword.MODEL;
import static gama.api.constants.IKeyword.NAME;
import static gama.api.constants.IKeyword.PUT;
import static gama.api.constants.IKeyword.REMOVE;
import static gama.api.constants.IKeyword.SET;
import static gama.api.constants.IKeyword.SPECIES;
import static gama.api.constants.IKeyword.SPECIES_LAYER;
import static gama.api.constants.IKeyword.SYNTHETIC;
import static gama.api.constants.IKeyword.TITLE;
import static gama.api.constants.IKeyword.TO;
import static gama.api.constants.IKeyword.TYPE;
import static gama.api.constants.IKeyword.VALUE;
import static gama.api.constants.IKeyword.WHEN;
import static gama.api.constants.IKeyword.WITH;
import static gama.api.constants.IKeyword.ZERO;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import gama.annotations.support.ISymbolKind;
import gama.api.GAMA;
import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Facets;
import gama.api.types.list.GamaListFactory;
import gama.api.types.map.GamaMapFactory;
import gama.dev.DEBUG;
import gaml.compiler.gaml.Access;
import gaml.compiler.gaml.ArgumentDefinition;
import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.Expression;
import gaml.compiler.gaml.ExpressionList;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.Function;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.Pragma;
import gaml.compiler.gaml.S_Action;
import gaml.compiler.gaml.S_Assignment;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Do;
import gaml.compiler.gaml.S_Equations;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_If;
import gaml.compiler.gaml.S_Reflex;
import gaml.compiler.gaml.S_Solve;
import gaml.compiler.gaml.S_Try;
import gaml.compiler.gaml.StandaloneBlock;
import gaml.compiler.gaml.StandaloneExperiment;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.TypeRef;
import gaml.compiler.gaml.VariableRef;
import gaml.compiler.gaml.ast.SyntacticExperimentModelElement;
import gaml.compiler.gaml.ast.SyntacticFactory;
import gaml.compiler.gaml.ast.SyntacticModelElement;
import gaml.compiler.gaml.descriptions.OperatorExpressionDescription;
import gaml.compiler.gaml.impl.ModelImpl;
import gaml.compiler.gaml.resource.GamlResourceServices;

/**
 * The GamlSyntacticConverter class performs critical transformations between the Eclipse EMF-based EObject
 * representation of GAML models and the GAMA-specific SyntacticElement representation.
 *
 * <p>
 * This converter serves as the bridge between the parsing phase (which produces an EMF model) and the compilation phase
 * (which works with SyntacticElements). It handles:
 * </p>
 *
 * <ul>
 * <li>Model structure conversion (species, experiments, statements, etc.)</li>
 * <li>Expression and facet transformation</li>
 * <li>Keyword mapping and normalization</li>
 * <li>Assignment operator translation</li>
 * <li>Synthetic element generation</li>
 * <li>Block and statement hierarchy preservation</li>
 * </ul>
 *
 * <p>
 * <strong>Key responsibilities:</strong>
 * </p>
 * <ul>
 * <li>Convert parsed GAML models into compilable syntactic structures</li>
 * <li>Transform various assignment operations into standard GAMA statements</li>
 * <li>Handle special cases like synthetic models, experiment files, and pragmas</li>
 * <li>Maintain proper parent-child relationships in the syntactic tree</li>
 * </ul>
 *
 * <p>
 * <strong>Thread Safety:</strong> This class is designed to be stateless and thread-safe. The static builder instance
 * is thread-safe and the conversion process maintains no mutable state between calls.
 * </p>
 *
 * @author drogoul
 * @since 16 mars 2013
 * @see ISyntacticElement
 * @see SyntacticFactory
 * @see ExpressionDescriptionBuilder
 */
public class GamlSyntacticConverter {

	static {
		DEBUG.OFF();
	}

	/**
	 * Counter for generating unique synthetic action names. Used when creating synthetic actions for blocks that need
	 * to be converted to expressions.
	 */
	private static int syntheticActionCounter = 0;

	// =========================================================================
	// MAIN CONVERSION METHODS
	// =========================================================================

	/**
	 * Gets the absolute folder path of the resource passed in arguments. Used to get the path to the model files
	 *
	 * @param r
	 *            the r
	 * @return the absolute container folder path of
	 */
	/**
	 * Extracts the absolute folder path of the container directory for the given resource. This is used to determine
	 * the path to model files for proper resource resolution.
	 *
	 * @param resource
	 *            the EMF resource whose container path should be extracted - must not be null
	 * @return the absolute path to the folder containing the resource as a string
	 * @throws IllegalArgumentException
	 *             if the resource is null or has no valid URI
	 */
	public static String getAbsoluteContainerFolderPathOf(final Resource resource) {
		if (resource == null) throw new IllegalArgumentException("Resource cannot be null");

		URI uri = resource.getURI();
		if (uri == null) throw new IllegalArgumentException("Resource must have a valid URI");

		if (uri.isFile()) {
			uri = uri.trimSegments(1);
			return uri.toFileString();
		}
		if (uri.isPlatform()) {
			final IPath path = GamlResourceServices.getPathOf(resource);
			final IFile file = GAMA.getWorkspaceManager().getRoot().getFile(path);
			final IContainer folder = file.getParent();
			return folder.getLocation().toString();
		}
		return URI.decode(uri.toString());
	}

	/**
	 * Builds the syntactic contents of the root object passed to it. This is the main entry point for converting parsed
	 * GAML structures into the GAMA compilation-ready syntactic representation.
	 *
	 * <p>
	 * Handles three main types of root objects:
	 * </p>
	 * <ul>
	 * <li><strong>StandaloneBlock:</strong> Converted to a synthetic model containing the block's statements</li>
	 * <li><strong>ExperimentFileStructure:</strong> Converted to an experiment model with proper metadata</li>
	 * <li><strong>Model:</strong> Converted to a full model element with pragma handling and statement processing</li>
	 * </ul>
	 *
	 * <p>
	 * The resulting syntactic element preserves the original EMF structure while providing the necessary metadata and
	 * relationships for the GAMA compilation process.
	 * </p>
	 *
	 * @param root
	 *            the root EObject to convert - must be one of StandaloneBlock, ExperimentFileStructure, or Model
	 * @return the converted ISyntacticElement representing the root structure, or null if the root type is unsupported
	 * @throws IllegalArgumentException
	 *             if the root object type is not supported
	 */
	public ISyntacticElement buildSyntacticContents(final EObject root) {
		// Input validation
		if (root == null) throw new IllegalArgumentException("Root object cannot be null");

		if (root instanceof StandaloneBlock) {
			final SyntacticModelElement elt = SyntacticFactory.getInstance().createSyntheticModel(root);
			convertBlock(elt, ((StandaloneBlock) root).getBlock());
			return elt;
		}
		if (root instanceof StandaloneExperiment he) {
			final String path = getAbsoluteContainerFolderPathOf(root.eResource());
			final SyntacticExperimentModelElement exp =
					SyntacticFactory.getInstance().createExperimentModel(root, he, path);
			convertFacets(he, exp.getExperiment());
			exp.setFacet(NAME, GAML.getExpressionDescriptionFactory().createLabel(exp.getExperiment().getName()));
			convStatements(exp.getExperiment(), EGaml.getInstance().getStatementsOf(he.getBlock()));
			return exp;
		}
		if (!(root instanceof Model))
			throw new IllegalArgumentException("Unsupported root object type: " + root.getClass().getName());

		final ModelImpl m = (ModelImpl) root;
		final String path = getAbsoluteContainerFolderPathOf(root.eResource());
		final SyntacticModelElement model = (SyntacticModelElement) SyntacticFactory.getInstance().create(MODEL, m,
				EGaml.getInstance().hasChildren(m), path);
		final Map<String, List<String>> prgm = collectPragmas(m);
		if (prgm != null) {
			model.setFacet(IKeyword.PRAGMA, GAML.getExpressionDescriptionFactory().createConstant(prgm));
		}
		model.setFacet(NAME, convertToLabel(null, m.getName()));
		convStatements(model, EGaml.getInstance().getStatementsOf(m));
		model.compactModel();
		return model;
	}

	/**
	 * Collects pragma declarations from the model and transforms them into a map structure.
	 *
	 * <p>
	 * Pragmas in GAML provide metadata about the model, including plugin dependencies and configuration information.
	 * This method extracts these declarations and converts them into a format suitable for the compilation process.
	 * </p>
	 *
	 * @param m
	 *            the ModelImpl containing the pragma declarations to collect
	 * @return a Map where keys are pragma names and values are lists of associated plugin names, or null if no pragmas
	 *         are defined in the model
	 */
	private Map<String, List<String>> collectPragmas(final ModelImpl m) {
		if (!m.eIsSet(GamlPackage.MODEL__PRAGMAS)) return null;
		final List<Pragma> pragmas = m.getPragmas();
		if (pragmas.isEmpty()) return null;
		Map<String, List<String>> result = GamaMapFactory.create();
		for (final Pragma p : pragmas) {
			ExpressionList plugins = p.getPlugins();
			if (plugins != null) {
				List<String> list = GamaListFactory.create();
				for (Expression exp : plugins.getExprs()) { list.add(EGaml.getInstance().toString(exp)); }
				result.put(p.getName(), list);
			} else {
				result.put(p.getName(), null);
			}
		}
		return result;
	}

	/**
	 * Does not define attributes.
	 *
	 * @param keyword
	 *            the keyword
	 * @return true, if successful
	 */
	private boolean doesNotDefineAttributes(final String keyword) {
		final IArtefactProto.Symbol p = ArtefactProtoRegistry.getProto(keyword, null);
		if (p == null) return true;
		final ISymbolKind kind = p.getKind();
		return !ISymbolKind.STATEMENTS_CONTAINING_ATTRIBUTES.contains(kind);
	}

	/**
	 * Converts a single statement from the parsed EMF representation to a syntactic element.
	 *
	 * <p>
	 * This method is responsible for the core conversion logic that handles different types of statements including:
	 * </p>
	 * <ul>
	 * <li>Variable and attribute definitions (S_Definition)</li>
	 * <li>Assignment operations (S_Assignment)</li>
	 * <li>Action calls (S_Do)</li>
	 * <li>Conditional statements (S_If)</li>
	 * <li>Method and action declarations (S_Action)</li>
	 * <li>Reflex statements with conditions (S_Reflex)</li>
	 * <li>Equation solving (S_Solve)</li>
	 * <li>Exception handling (S_Try)</li>
	 * <li>Experiments and parameters</li>
	 * </ul>
	 *
	 * <p>
	 * The conversion process includes keyword normalization, facet processing, and proper handling of parent-child
	 * relationships in the syntactic tree.
	 * </p>
	 *
	 * @param upper
	 *            the parent syntactic element that will contain this statement
	 * @param stm
	 *            the statement EObject to convert from the parsed model
	 * @return the converted ISyntacticElement representing the statement
	 * @throws NullPointerException
	 *             if the statement has a null keyword
	 */
	private final ISyntacticElement convStatement(final ISyntacticElement upper, final Statement stm) {
		// We catch its keyword
		String keyword = EGaml.getInstance().getKeyOf(stm);
		if (keyword == null) throw new NullPointerException(
				"Trying to convert a statement with a null keyword. Please debug to understand the cause.");
		keyword = convertKeyword(keyword, upper.getKeyword());

		final boolean upperContainsAttributes = !doesNotDefineAttributes(upper.getKeyword());
		final boolean isVar = stm instanceof S_Definition && !ArtefactProtoRegistry.isStatementProto(keyword)
				&& upperContainsAttributes && !EGaml.getInstance().hasChildren(stm);

		final ISyntacticElement elt =
				isVar ? SyntacticFactory.getInstance().createVar(keyword, ((S_Definition) stm).getName(), stm)
						: SyntacticFactory.getInstance().create(keyword, stm, EGaml.getInstance().hasChildren(stm));

		if (stm instanceof S_Assignment sa) {
			keyword = convertAssignment(sa, keyword, elt, stm.getExpr());
		} else if (stm instanceof S_Definition def && !ArtefactProtoRegistry.isStatementProto(keyword)) {
			// If we define a variable with this statement
			final TypeRef t = (TypeRef) def.getTkey();
			if (t != null) {
				addFacet(elt, TYPE, convExpr(t));
				// If the type should not be inferred, we add a facet to specify it (see #385)
				elt.setFacet(IKeyword.NO_TYPE_INFERENCE,
						(IExpressionDescription) GAML.getExpressionFactory().getTrue());
			}
			if (t != null && !upperContainsAttributes) {
				// Translation of "type var ..." to "let var type: type ..." if
				// we are not in a
				// top-level statement (i.e. not in the declaration of a species
				// or an experiment)
				elt.setKeyword(LET);
				keyword = LET;
			} else {
				// Translation of "type1 ID1 (type2 ID2, type3 ID3) {...}" to
				// "action ID1 type: type1 { arg ID2 type: type2; arg ID3 type:
				// type3; ...}"
				if (EGaml.getInstance().hasChildren(def)) {
					elt.setKeyword(ACTION);
					keyword = ACTION;
				}
				convertArgs(def.getArgs(), elt);
			}
		} else if (stm instanceof S_Do) {
			processDo(stm, elt);
		} else if (stm instanceof S_If) {
			// If the statement is "if", we convert its potential "else" part
			// and put it as a child
			// of the syntactic element (as GAML expects it)
			convElse((S_If) stm, elt);
		} else if (stm instanceof S_Action sa) {
			// Conversion of "action ID (type1 ID1 <- V1, type2 ID2)" to
			// "action ID {arg ID1 type: type1 default: V1; arg ID2 type:
			// type2}"
			convertArgs(sa.getArgs(), elt);
		} else if (stm instanceof S_Reflex ref) {
			if (ref.getExpr() != null) { addFacet(elt, WHEN, convExpr(ref.getExpr())); }
		} else if (stm instanceof S_Solve) {
			final Expression e = stm.getExpr();
			addFacet(elt, EQUATION, convertToLabel(e, EGaml.getInstance().getKeyOf(e)));
		} else if (stm instanceof S_Try) {
			convCatch((S_Try) stm, elt);
		} else if (IKeyword.PARAMETER.equals(keyword)) { processParameter(stm, elt); }

		// We apply some conversions to the facets expressed in the statement
		convertFacets(stm, keyword, elt);

		if (stm instanceof S_Experiment) {
			processExperiment(elt);
		} else if (METHOD.equals(keyword)) {
			// We apply some conversion for methods (to get the name instead of
			// the "method" keyword)
			final String type = elt.getName();
			if (type != null) { elt.setKeyword(type); }
		} else if (stm instanceof S_Equations) { convStatements(elt, EGaml.getInstance().getEquationsOf(stm)); }
		// We convert the block of statements (if any)
		if (!IKeyword.PARAMETER.equals(keyword)) { convertBlock(elt, stm.getBlock()); }
		return elt;
	}

	/**
	 * Process experiment.
	 *
	 * @param elt
	 *            the elt
	 */
	private void processExperiment(final ISyntacticElement elt) {
		// We do it also for experiments, and change their name
		final IExpressionDescription type = elt.getExpressionAt(TYPE);
		if (type == null) { elt.setFacet(TYPE, GAML.getExpressionDescriptionFactory().createConstant(GUI_)); }
		// We modify the names of experiments so as not to confuse them with
		// species
		final String name = elt.getName();
		elt.setFacet(TITLE, convertToLabel(null, "Experiment " + name));
		elt.setFacet(NAME, convertToLabel(null, name));
	}

	/**
	 * Process parameter.
	 *
	 * @param stm
	 *            the stm
	 * @param elt
	 *            the elt
	 */
	private void processParameter(final Statement stm, final ISyntacticElement elt) {
		// As the description of parameters does not accept children, we move the block to the
		// 'on_change' facet.
		Block b = stm.getBlock();
		if (b != null) {
			final ISyntacticElement blockElt = SyntacticFactory.getInstance().create(ACTION,
					new Facets(NAME, SYNTHETIC + syntheticActionCounter++), true);
			convertBlock(blockElt, b);
			IExpressionDescription fexpr = createBlockExpr(blockElt);
			addFacet(elt, IKeyword.ON_CHANGE, fexpr);
		}
	}

	/**
	 * Process do.
	 *
	 * @param stm
	 *            the stm
	 * @param elt
	 *            the elt
	 */
	private void processDo(final Statement stm, final ISyntacticElement elt) {
		// Translation of "stm ID (ID1: V1, ID2:V2)" to "stm ID with:(ID1:
		// V1, ID2:V2)"
		final Expression e = stm.getExpr();
		addFacet(elt, ACTION, convertToLabel(e, EGaml.getInstance().getKeyOf(e)));
		// Systematically adds the internal function (see #2915) in order to have the right documentation
		// TODO AD: verify that 'ACTION' is still necessary in that case
		addFacet(elt, INTERNAL_FUNCTION, convExpr(e));
		if (e instanceof Function f) {
			final ExpressionList list = f.getRight();
			if (list != null) { addFacet(elt, WITH, convExpr(list)); }

		}
	}

	/**
	 * Convert block.
	 *
	 * @param elt
	 *            the elt
	 * @param block
	 *            the block
	 */
	public void convertBlock(final ISyntacticElement elt, final Block block) {
		if (block != null) { convStatements(elt, EGaml.getInstance().getStatementsOf(block)); }
	}

	/**
	 * Adds the facet.
	 *
	 * @param e
	 *            the e
	 * @param key
	 *            the key
	 * @param expr
	 *            the expr
	 */
	private void addFacet(final ISyntacticElement e, final String key, final IExpressionDescription expr) {
		if (e.hasFacet(key)) {
			e.setFacet(IGamlIssue.DOUBLED_CODE + key, expr);
		} else {
			e.setFacet(key, expr);
		}
	}

	/**
	 * Conv else.
	 *
	 * @param stm
	 *            the stm
	 * @param elt
	 *            the elt
	 */
	private void convElse(final S_If stm, final ISyntacticElement elt) {
		final EObject elseBlock = stm.getElse();
		if (elseBlock != null) {
			final ISyntacticElement elseElt =
					SyntacticFactory.getInstance().create(ELSE, elseBlock, EGaml.getInstance().hasChildren(elseBlock));
			if (elseBlock instanceof Statement) {
				elseElt.addChild(convStatement(elt, (Statement) elseBlock));
			} else {
				convStatements(elseElt, EGaml.getInstance().getStatementsOf(elseBlock));
			}
			elt.addChild(elseElt);
		}
	}

	/**
	 * Conv catch.
	 *
	 * @param stm
	 *            the stm
	 * @param elt
	 *            the elt
	 */
	private void convCatch(final S_Try stm, final ISyntacticElement elt) {
		final EObject catchBlock = stm.getCatch();
		if (catchBlock != null) {
			final ISyntacticElement catchElt = SyntacticFactory.getInstance().create(IKeyword.CATCH, catchBlock,
					EGaml.getInstance().hasChildren(catchBlock));
			convStatements(catchElt, EGaml.getInstance().getStatementsOf(catchBlock));
			elt.addChild(catchElt);
		}
	}

	/**
	 * Convert args.
	 *
	 * @param eList
	 *            the args
	 * @param elt
	 *            the elt
	 */
	private void convertArgs(final EList<ArgumentDefinition> eList, final ISyntacticElement elt) {
		if (eList != null) {
			for (final ArgumentDefinition def : eList) {
				final ISyntacticElement arg = SyntacticFactory.getInstance().create(ARG, def, false);
				addFacet(arg, NAME, convertToLabel(null, def.getName()));
				final EObject type = def.getType();
				addFacet(arg, TYPE, convExpr(type));
				final Expression e = def.getDefault();
				if (e != null) { addFacet(arg, DEFAULT, convExpr(e)); }
				elt.addChild(arg);
			}
		}
	}

	// =========================================================================
	// ASSIGNMENT CONVERSION METHODS
	// =========================================================================

	/**
	 * Converts different types of assignment statements into their corresponding GAMA statements.
	 *
	 * <p>
	 * This method handles various assignment operators including:
	 * </p>
	 * <ul>
	 * <li><strong>&lt;- and set:</strong> Basic assignment operations</li>
	 * <li><strong>&lt;&lt; and &lt;+:</strong> Addition operations to containers</li>
	 * <li><strong>&gt;&gt; and &gt;-:</strong> Removal operations from containers</li>
	 * <li><strong>equation operators:</strong> Mathematical equation assignments</li>
	 * </ul>
	 *
	 * @param stm
	 *            the assignment statement to convert
	 * @param originalKeyword
	 *            the original keyword before conversion
	 * @param elt
	 *            the syntactic element being built
	 * @param expr
	 *            the expression being assigned
	 * @return the converted keyword after processing the assignment
	 * @throws IllegalArgumentException
	 *             if the assignment type is not supported
	 */
	private String convertAssignment(final S_Assignment stm, final String originalKeyword, final ISyntacticElement elt,
			final Expression expr) {
		if (stm == null || elt == null)
			throw new IllegalArgumentException("Assignment statement and syntactic element cannot be null");

		final IExpressionDescription value = convExpr(stm.getValue());
		String keyword = originalKeyword;

		if (keyword.endsWith("<-") || SET.equals(keyword)) {
			keyword = processBasicAssignment(elt, expr, value, keyword);
		} else if (keyword.startsWith("<<") || "<+".equals(keyword)) {
			// Translation of "container <+ item" or "container << item" to "add item: item to: container"
			// 08/01/14: Addition of the "<<+" (add all)
			keyword = processAdditiveAssignment(elt, expr, value, keyword);
		} else if (keyword.startsWith(">>") || ">-".equals(keyword)) {
			keyword = processRemovalAssignment(elt, expr, value, keyword);
		} else if (EQUALS.equals(keyword)) { processEquationAssignment(elt, expr, value); }
		return keyword;
	}

	/**
	 * Processes equation assignment operations by converting differential equations into proper GAMA equation format.
	 *
	 * <p>
	 * Handles conversion of left member (variable or function) and right member of the equation into the appropriate
	 * facet structure.
	 * </p>
	 *
	 * @param elt
	 *            the syntactic element to modify
	 * @param expr
	 *            the left-hand side expression of the equation
	 * @param value
	 *            the right-hand side value expression description
	 */
	private void processEquationAssignment(final ISyntacticElement elt, final Expression expr,
			final IExpressionDescription value) {
		// conversion of left member (either a var or a function)
		IExpressionDescription left = null;
		if (expr instanceof VariableRef) {
			left = new OperatorExpressionDescription(ZERO, convExpr(expr));
		} else {
			left = convExpr(expr);
		}
		addFacet(elt, EQUATION_LEFT, left);
		// Translation of right member
		addFacet(elt, EQUATION_RIGHT, value);
	}

	/**
	 * Process removal assignment.
	 *
	 * @param elt
	 *            the elt
	 * @param expr
	 *            the expr
	 * @param value
	 *            the value
	 * @param keyword
	 *            the keyword
	 * @return the string
	 */
	private String processRemovalAssignment(final ISyntacticElement elt, final Expression expr,
			final IExpressionDescription value, final String keyword) {
		// Translation of "container >> item" or "container >- item" to
		// "remove item: item from: container"
		// 08/01/14: Addition of the ">>-" keyword (remove all)
		elt.setKeyword(REMOVE);
		// 20/01/14: Addition of the access [] to remove from the index
		if (expr instanceof Access && "[".equals(((Access) expr).getOp())
				&& EGaml.getInstance().getExprsOf(((Access) expr).getRight()).size() == 0) {
			addFacet(elt, FROM, convExpr(((Access) expr).getLeft()));
			addFacet(elt, INDEX, value);
		} else {
			addFacet(elt, FROM, convExpr(expr));
			addFacet(elt, ITEM, value);
		}
		if (">>-".equals(keyword)) { addFacet(elt, ALL, GAML.getExpressionDescriptionFactory().createConstant(true)); }
		return REMOVE;
	}

	/**
	 * Process additive assignment.
	 *
	 * @param elt
	 *            the elt
	 * @param expr
	 *            the expr
	 * @param value
	 *            the value
	 * @param keyword
	 *            the keyword
	 * @return the string
	 */
	private String processAdditiveAssignment(final ISyntacticElement elt, final Expression expr,
			final IExpressionDescription value, final String keyword) {
		elt.setKeyword(ADD);
		addFacet(elt, TO, convExpr(expr));
		addFacet(elt, ITEM, value);
		if ("<<+".equals(keyword)) { addFacet(elt, ALL, GAML.getExpressionDescriptionFactory().createConstant(true)); }
		return ADD;
	}

	/**
	 * Process basic assignment.
	 *
	 * @param elt
	 *            the elt
	 * @param expr
	 *            the expr
	 * @param value
	 *            the value
	 * @param keyword
	 *            the keyword
	 * @return the string
	 */
	private String processBasicAssignment(final ISyntacticElement elt, final Expression expr,
			final IExpressionDescription value, String keyword) {
		// Translation of "container[index] <- value" to
		// "put item: value in: container at: index"
		// 20/1/14: Translation of container[index] +<- value" to
		// "add item: value in: container at: index"
		if (expr instanceof Access && "[".equals(((Access) expr).getOp())) {
			boolean isAdd = "+<-".equals(keyword);
			final String kw = isAdd ? ADD : PUT;
			final String to = isAdd ? TO : IN;
			elt.setKeyword(kw);
			addFacet(elt, ITEM, value);
			addFacet(elt, to, convExpr(((Access) expr).getLeft()));
			final List<Expression> args = EGaml.getInstance().getExprsOf(((Access) expr).getRight());
			if (args.size() == 0) {
				// Add facet all: true when no index is provided
				addFacet(elt, ALL, GAML.getExpressionDescriptionFactory().createConstant(true));
			} else if (args.size() == 1) { // Integer index -- or pair index see #3099
				addFacet(elt, AT, convExpr(args.get(0)));
			} else { // Point index
				final IExpressionDescription p = new OperatorExpressionDescription("internal_list",
						convExpr(args.get(0)), convExpr(args.get(1)));
				addFacet(elt, AT, p);
			}
			keyword = kw;
		} else {
			// Translation of "var <- value" to "set var value: value"
			elt.setKeyword(SET);
			addFacet(elt, VALUE, value);
			keyword = SET;
		}
		return keyword;
	}

	/**
	 * Convert facets.
	 *
	 * @param stm
	 *            the stm
	 * @param keyword
	 *            the keyword
	 * @param elt
	 *            the elt
	 */
	private void convertFacets(final Statement stm, final String keyword, final ISyntacticElement elt) {
		final IArtefactProto.Symbol p = ArtefactProtoRegistry.getProto(keyword, null);
		for (final Facet f : EGaml.getInstance().getFacetsOf(stm)) {
			String fname = replaceAssignments(keyword, EGaml.getInstance().getKeyOf(f));
			// We compute (and convert) the expression attached to the facet
			final boolean label = p == null ? false : p.isLabel(fname);
			final IExpressionDescription fexpr = convExpr(f, label);
			addFacet(elt, fname, fexpr);
		}

		// We add the "default" (or omissible) facet to the syntactic element
		String def = getDefaultFacet(stm, keyword);
		if (def != null && !def.isEmpty() && !elt.hasFacet(def)) {
			final IExpressionDescription ed = findExpr(stm);
			if (ed != null) { elt.setFacet(def, ed); }
		}
		if (LET.equals(keyword) && elt.hasFacet(TYPE)) {
			elt.setFacet(IKeyword.NO_TYPE_INFERENCE, GAML.getExpressionDescriptionFactory().createConstant(true));
		}
	}

	/**
	 * Gets the default facet.
	 *
	 * @param stm
	 *            the stm
	 * @param keyword
	 *            the keyword
	 * @return the default facet
	 */
	private String getDefaultFacet(final Statement stm, final String keyword) {
		return ArtefactProtoRegistry.getOmissibleFacetForSymbol(keyword);
	}

	/**
	 * Replace assignments.
	 *
	 * @param keyword
	 *            the keyword
	 * @param fname
	 *            the fname
	 * @return the string
	 */
	private String replaceAssignments(final String keyword, String fname) {
		// We change the "<-" and "->" symbols into full names
		if ("<-".equals(fname)) {
			fname = LET.equals(keyword) || SET.equals(keyword) ? VALUE : INIT;
		} else if ("->".equals(fname)) { fname = FUNCTION; }
		return fname;
	}

	/**
	 * Convert facets.
	 *
	 * @param stm
	 *            the stm
	 * @param elt
	 *            the elt
	 */
	private void convertFacets(final StandaloneExperiment stm, final ISyntacticElement elt) {
		final IArtefactProto.Symbol p = ArtefactProtoRegistry.getProto(EXPERIMENT, null);
		for (final Facet f : EGaml.getInstance().getFacetsOf(stm)) {
			final String fname = EGaml.getInstance().getKeyOf(f);

			// We compute (and convert) the expression attached to the facet
			final boolean label = p == null ? false : p.isLabel(fname);
			final IExpressionDescription fexpr = convExpr(f, label);
			addFacet(elt, fname, fexpr);
		}
		final IExpressionDescription ed = findExpr(stm);
		addFacet(elt, NAME, ed);
		addFacet(elt, TITLE, ed);
		// if (!elt.hasFacet(TYPE)) { addFacet(elt, TYPE, convertToLabel(null, HEADLESS_UI)); }
	}

	/**
	 * Convert keyword.
	 *
	 * @param k
	 *            the k
	 * @param upper
	 *            the upper
	 * @return the string
	 */
	private String convertKeyword(final String keyword, final String upper) {
		// if ((BATCH.equals(upper) || EXPERIMENT.equals(upper)) && SAVE.equals(keyword)) {
		// keyword = SAVE_BATCH;
		// } else
		if (!DISPLAY.equals(upper) && !SPECIES_LAYER.equals(upper)) return keyword;
		return switch (keyword) {
			case SPECIES -> SPECIES_LAYER;
			case GRID -> GRID_LAYER;
			case IMAGE -> IMAGE_LAYER;
			default -> keyword;
		};

	}

	/**
	 * Conv expr.
	 *
	 * @param expr
	 *            the expr
	 * @return the i expression description
	 */
	private final IExpressionDescription convExpr(final EObject expr) {
		if (expr == null) return null;
		return GAML.getExpressionDescriptionFactory().createFromEObject(expr);
	}

	/**
	 * Conv expr.
	 *
	 * @param expr
	 *            the expr
	 * @param errors
	 *            the errors
	 * @return the i expression description
	 */
	private final IExpressionDescription createBlockExpr(final ISyntacticElement expr) {
		if (expr == null) return null;
		return GAML.getExpressionDescriptionFactory().createBlock(expr);
	}

	/**
	 * Conv expr.
	 *
	 * @param facet
	 *            the facet
	 * @param label
	 *            the label
	 * @return the i expression description
	 */
	private final IExpressionDescription convExpr(final Facet facet, final boolean label) {
		if (facet != null) {
			final Expression expr = facet.getExpr();
			if (expr == null && facet.getBlock() != null) {
				final Block b = facet.getBlock();
				final ISyntacticElement elt = SyntacticFactory.getInstance().create(ACTION,
						new Facets(NAME, SYNTHETIC + syntheticActionCounter++), true);
				convertBlock(elt, b);
				return createBlockExpr(elt);
			}
			if (expr != null) return label ? convertToLabel(expr, EGaml.getInstance().getKeyOf(expr)) : convExpr(expr);
			final String name = facet.getName();
			// TODO Verify the use of "facet"
			if (name != null) return convertToLabel(null, name);
		}
		return null;
	}

	/**
	 * Convert to label.
	 *
	 * @param target
	 *            the target
	 * @param string
	 *            the string
	 * @return the i expression description
	 */
	final IExpressionDescription convertToLabel(final EObject target, final String string) {

		final IExpressionDescription ed = GAML.getExpressionDescriptionFactory().createLabel(string);
		ed.setTarget(target);
		if (target != null) {
			// GamlResourceServices.getResourceDocumenter().setGamlDocumentation(target, ed.getExpression(), true);
		}
		return ed;
	}

	/**
	 * Conv statements.
	 *
	 * @param elt
	 *            the elt
	 * @param ss
	 *            the ss
	 */
	final void convStatements(final ISyntacticElement elt, final List<? extends Statement> ss) {
		for (final Statement stm : ss) {
			if (IKeyword.GLOBAL.equals(EGaml.getInstance().getKeyOf(stm))) {
				convStatements(elt, EGaml.getInstance().getStatementsOf(stm.getBlock()));
				convertFacets(stm, IKeyword.GLOBAL, elt);
			} else {
				final ISyntacticElement child = convStatement(elt, stm);
				if (child != null) { elt.addChild(child); }
			}
		}
	}

	/**
	 * Find expr.
	 *
	 * @param stm
	 *            the stm
	 * @return the i expression description
	 */
	private final IExpressionDescription findExpr(final Statement stm) {
		if (stm == null) return null;
		// The order below should be important
		final String name = EGaml.getInstance().getNameOf(stm);
		if (name != null) return convertToLabel(stm, name);
		final Expression expr = stm.getExpr();
		if (expr != null) return convExpr(expr);
		return null;
	}

	/**
	 * Find expr.
	 *
	 * @param stm
	 *            the stm
	 * @return the i expression description
	 */
	private final IExpressionDescription findExpr(final StandaloneExperiment stm) {
		if (stm == null) return null;
		return convertToLabel(stm, EGaml.getInstance().getNameOf(stm));

	}

}

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

import static gama.annotations.constants.IKeyword.ACTION;
import static gama.annotations.constants.IKeyword.ADD;
import static gama.annotations.constants.IKeyword.ALL;
import static gama.annotations.constants.IKeyword.ARG;
import static gama.annotations.constants.IKeyword.AT;
import static gama.annotations.constants.IKeyword.CATCH;
import static gama.annotations.constants.IKeyword.DEFAULT;
import static gama.annotations.constants.IKeyword.DISPLAY;
import static gama.annotations.constants.IKeyword.ELSE;
import static gama.annotations.constants.IKeyword.EQUALS;
import static gama.annotations.constants.IKeyword.EQUATION_LEFT;
import static gama.annotations.constants.IKeyword.EQUATION_RIGHT;
import static gama.annotations.constants.IKeyword.EXPERIMENT;
import static gama.annotations.constants.IKeyword.FROM;
import static gama.annotations.constants.IKeyword.FUNCTION;
import static gama.annotations.constants.IKeyword.GRID;
import static gama.annotations.constants.IKeyword.GRID_LAYER;
import static gama.annotations.constants.IKeyword.GUI_;
import static gama.annotations.constants.IKeyword.IMAGE;
import static gama.annotations.constants.IKeyword.IMAGE_LAYER;
import static gama.annotations.constants.IKeyword.IN;
import static gama.annotations.constants.IKeyword.INDEX;
import static gama.annotations.constants.IKeyword.INIT;
import static gama.annotations.constants.IKeyword.INTERNAL_FUNCTION;
import static gama.annotations.constants.IKeyword.ITEM;
import static gama.annotations.constants.IKeyword.LET;
import static gama.annotations.constants.IKeyword.MODEL;
import static gama.annotations.constants.IKeyword.NAME;
import static gama.annotations.constants.IKeyword.PUT;
import static gama.annotations.constants.IKeyword.REMOVE;
import static gama.annotations.constants.IKeyword.SET;
import static gama.annotations.constants.IKeyword.SPECIES;
import static gama.annotations.constants.IKeyword.SPECIES_LAYER;
import static gama.annotations.constants.IKeyword.SYNTHETIC;
import static gama.annotations.constants.IKeyword.TITLE;
import static gama.annotations.constants.IKeyword.TO;
import static gama.annotations.constants.IKeyword.TYPE;
import static gama.annotations.constants.IKeyword.VALUE;
import static gama.annotations.constants.IKeyword.WITH;
import static gama.annotations.constants.IKeyword.ZERO;
import static gama.annotations.support.ISymbolKind.isDefiningAttributes;
import static gama.api.additions.registries.ArtefactRegistry.getStatementArtefact;
import static gama.api.gaml.GAML.getExpressionDescriptionFactory;
import static gaml.compiler.gaml.IInternalFacets.NO_TYPE_INFERENCE;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import gama.annotations.constants.IKeyword;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.ast.ISyntacticFactory;
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
import gaml.compiler.gaml.IInternalFacets;
import gaml.compiler.gaml.Pragma;
import gaml.compiler.gaml.S_Assignment;
import gaml.compiler.gaml.S_Callable;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Do;
import gaml.compiler.gaml.S_Equations;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_If;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.S_Try;
import gaml.compiler.gaml.StandaloneBlock;
import gaml.compiler.gaml.StandaloneExperiment;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.VarDefinition;
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
 * (which works with SyntacticElements). Its goal is to process the EMF model into a syntactic "AST" form suitable for
 * compilation. It handles:
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
 * <li>A new feature allows this converter to pass errors and warnings to the descriptions, through specialized facets
 * available in {@code IInternalFacets}</li>
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
 * @see IInternalFacets
 */
public class GamlSyntacticConverter {

	static {
		DEBUG.ON();
	}

	/**
	 * Counter for generating unique synthetic action names. Used when creating synthetic actions for blocks that need
	 * to be converted to expressions. Thread-safe via atomic operations.
	 */
	private static final AtomicInteger syntheticActionCounter = new AtomicInteger();

	/** The factory. */
	private static SyntacticFactory FACTORY = SyntacticFactory.getInstance();

	/** The egaml. */
	private static EGaml EGAML = EGaml.getInstance();

	// =========================================================================
	// MAIN CONVERSION SAMPLING
	// =========================================================================

	/**
	 * Extracts the absolute folder path of the container directory for the given resource. Delegates to
	 * {@link GamlResourceServices#getAbsoluteContainerFolderPathOf(Resource)} which owns this responsibility.
	 *
	 * @param resource
	 *            the EMF resource whose container path should be extracted; must not be null
	 * @return the absolute path to the folder containing the resource as a string
	 */
	private static String getAbsoluteContainerFolderPathOf(final Resource resource) {
		return GamlResourceServices.getAbsoluteContainerFolderPathOf(resource);
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
		switch (root) {
			case StandaloneBlock block -> {
				final SyntacticModelElement elt = FACTORY.createSyntheticModel(root);
				convertBlock(ISyntacticFactory.SYNTHETIC_MODEL, elt, block.getBlock());
				return elt;
			}
			case StandaloneExperiment he -> {
				final String path = getAbsoluteContainerFolderPathOf(root.eResource());
				final SyntacticExperimentModelElement exp = FACTORY.createExperimentModel(root, he, path);
				convertFacets(he, exp.getExperiment());
				exp.setFacet(NAME, GAML.getExpressionDescriptionFactory().createLabel(exp.getExperiment().getName()));
				convStatements(exp.getExperiment(), EGAML.getStatementsOf(he.getBlock()));
				return exp;
			}
			case ModelImpl m -> {
				final String path = getAbsoluteContainerFolderPathOf(root.eResource());
				final SyntacticModelElement model =
						(SyntacticModelElement) FACTORY.create(MODEL, m, EGAML.hasChildren(m), path);
				final Map<String, List<String>> prgm = collectPragmas(m);
				if (prgm != null) {
					model.setFacet(IKeyword.PRAGMA, GAML.getExpressionDescriptionFactory().createConstant(prgm));
				}
				model.setFacet(NAME, convertToLabel(null, m.getName()));
				convStatements(model, EGAML.getStatementsOf(m));
				model.compactModel();
				return model;
			}
			case null, default -> throw new IllegalArgumentException("Root object not supported: " + root);
		}
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
				for (Expression exp : plugins.getExprs()) { list.add(EGAML.toString(exp)); }
				result.put(p.getName(), list);
			} else {
				result.put(p.getName(), null);
			}
		}
		return result;
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
		String keyword = computeKeyword(upper, stm);

		final IArtefact.Symbol upperArtefact = getStatementArtefact(upper.getKeyword());

		final boolean upperContainsAttributes = upperArtefact != null && isDefiningAttributes(upperArtefact.getKind());
		final boolean isWronglyClassifiedInStatements = !(stm instanceof S_Definition) && GAML.isADeclaration(keyword);
		final boolean isWronglyClassifiedInDefinitions = stm instanceof S_Definition && !GAML.isADeclaration(keyword);

		// if (isWronglyClassifiedInDefinitions) { DEBUG.LOG("Wrongly classified in definitions: " + stm); }
		// if (isWronglyClassifiedInStatements) { DEBUG.LOG("Wrongly classified in statements: " + stm); }

		ISyntacticElement elt = null;
		final String finalKeyword = keyword;
		switch (stm) {
			case S_Species ss when "species_layer".equals(finalKeyword) -> {
				elt = FACTORY.create(keyword, stm, true);
				// Create a VarDefinition with the species name, wrap it in a VariableRef,
				// and assign it as the expression of the statement so that the species_layer
				// facet resolution can find the referenced species by name. It is a very bad fix...
				final String speciesName = ss.getName();
				if (speciesName != null) {
					final VarDefinition varDef = EGAML.getFactory().createVarDefinition();
					varDef.setName(speciesName);
					final VariableRef varRef = EGAML.getFactory().createVariableRef();
					varRef.setRef(varDef);
					ss.setExpr(varRef);
					ss.setName(null);
				}
			}
			case S_Callable call when upperContainsAttributes -> {
				// If we define an action with this statement
				final Expression t = call.getTkey();
				keyword = ACTION;
				elt = FACTORY.create(ACTION, stm, true);
				processTypeKey(elt, t);
				convertArgs(call.getArgs(), elt);
				break;
			}
			case S_Definition def when !isWronglyClassifiedInDefinitions -> {
				// If we define a variable with this statement
				final Expression t = def.getTkey();
				keyword = !upperContainsAttributes ? LET : keyword;
				elt = !upperContainsAttributes ? FACTORY.create(LET, stm, true)
						: FACTORY.createVar(keyword, ((S_Definition) stm).getName(), stm);
				processTypeKey(elt, t);
			}
			case S_Assignment sa -> {
				elt = FACTORY.create(keyword, stm, false);
				keyword = convertAssignment(sa, keyword, elt, stm.getExpr());
			}
			case S_Do sdo -> {
				elt = FACTORY.create(keyword, stm, false);
				processDo(sdo, elt);
			}
			// If the statement is "if", we convert its "else" part and put it as a child (as GAML expects it)
			case S_If sif -> {
				elt = FACTORY.create(keyword, stm, true);
				convElse(sif, elt);
			}
			case S_Try st -> {
				elt = FACTORY.create(keyword, stm, true);
				convCatch(st, elt);
			}
			case null, default -> {
				switch (keyword) {
					case IKeyword.PARAMETER -> {
						elt = FACTORY.create(keyword, stm, false);
						processParameter(stm, elt);
					}
					case IKeyword.METHOD -> {// We apply conversion to get the name instead of the "method" keyword
						keyword = EGAML.getNameOf(stm);
						elt = FACTORY.create(keyword, stm, true);
					}
					default -> {
						elt = FACTORY.create(keyword, stm, EGaml.getInstance().hasChildren(stm));
					}
				}
			}
		}

		// We apply some conversions to the facets expressed in the statement
		convertFacets(stm, keyword, elt);
		if (isWronglyClassifiedInStatements) {
			// We mark the element with a GAML warning facet
			// Will mark for instance: int my_action {...}
			// This is a temporary feature to ease the transitioning towards the full functional syntax
			elt.setFacet(IInternalFacets.GAML_WARNING,
					GAML.getExpressionDescriptionFactory().createConstant("Action declaration is missing parentheses"));
			String type = elt.getKeyword();
			elt.setKeyword(ACTION);
			elt.setFacet(TYPE, GAML.getExpressionDescriptionFactory().createStringBased(type));
			elt.setFacet(NAME, GAML.getExpressionDescriptionFactory().createFromEObject(stm.getExpr()));
		}

		if (stm instanceof S_Experiment) {
			processExperiment(elt);
		} else if (stm instanceof S_Equations seq) { convStatements(elt, EGAML.getEquationsOf(seq)); }
		// We convert the block of statements (if any)

		if (!IKeyword.PARAMETER.equals(keyword)) { convertBlock(keyword, elt, stm.getBlock()); }
		return elt;
	}

	/**
	 * Process type key.
	 *
	 * @param elt
	 *            the elt
	 * @param t
	 *            the t
	 */
	private void processTypeKey(final ISyntacticElement elt, final Expression t) {
		if (t != null) {
			addFacet(elt, TYPE, convExpr(t));
			// If the type should not be inferred, we add a facet to specify it (see #385)
			elt.setFacet(NO_TYPE_INFERENCE, getExpressionDescriptionFactory().getTrue());
		}
	}

	/** The Constant DISPLAY_UPPER. */
	private static final Set<String> DISPLAY_UPPER = Set.of(DISPLAY, SPECIES_LAYER);

	/** The Constant KEYWORD_CONVERSIONS. */
	private static final Map<String, String> KEYWORD_CONVERSIONS =
			Map.of(SPECIES, SPECIES_LAYER, GRID, GRID_LAYER, IMAGE, IMAGE_LAYER);

	/**
	 * Extracts and normalises the keyword for the given statement.
	 *
	 * <p>
	 * Retrieves the raw keyword from the EMF statement via {@link EGaml#getKeyOf(EObject)}, then applies
	 * context-sensitive conversions (e.g. {@code species} → {@code species_layer} when the parent is a
	 * {@code display}).
	 * </p>
	 *
	 * @param upper
	 *            the parent syntactic element whose keyword provides the conversion context
	 * @param stm
	 *            the statement from which the keyword should be extracted
	 * @return the normalised keyword; never null
	 * @throws NullPointerException
	 *             if the statement's raw keyword is null
	 */

	private String computeKeyword(final ISyntacticElement upper, final Statement stm) {
		String key = EGAML.getKeyOf(stm);
		if (DISPLAY_UPPER.contains(upper.getKeyword())) {
			String conversion = KEYWORD_CONVERSIONS.get(key);
			return conversion != null ? conversion : key;
		}
		return key;
	}

	/**
	 * Process experiment.
	 *
	 * @param elt
	 *            the elt
	 */
	private void processExperiment(final ISyntacticElement elt) {
		// We do it also for experiments, and change their name
		if (!elt.hasFacet(TYPE)) { elt.setFacet(TYPE, GAML.getExpressionDescriptionFactory().createConstant(GUI_)); }
		// We modify the names of experiments so as not to confuse them with species
		final String name = elt.getName();
		if (!elt.hasFacet(TITLE)) { elt.setFacet(TITLE, convertToLabel(null, "Experiment " + name)); }
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
			final ISyntacticElement blockElt = FACTORY.create(ACTION,
					new Facets(NAME, SYNTHETIC + syntheticActionCounter.getAndIncrement()), true);
			convertBlock(ACTION, blockElt, b);
			IExpressionDescription fexpr = createBlockExpression(blockElt);
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
	private void processDo(final S_Do stm, final ISyntacticElement elt) {
		// Translation of "stm ID (ID1: V1, ID2:V2)" to "stm ID with:(ID1:
		// V1, ID2:V2)"
		final Expression e = stm.getExpr();
		addFacet(elt, ACTION, convertToLabel(e, EGAML.getKeyOf(e)));
		if (stm.getTarget() != null) { addFacet(elt, IKeyword.SYNTHETIC_DO_TARGET, convExpr(stm.getTarget())); }
		// Systematically adds the internal function (see #2915) in order to have the right documentation
		// TODO AD: verify that 'ACTION' is still necessary in that case
		addFacet(elt, INTERNAL_FUNCTION, convExpr(e));
		if (e instanceof Function f) {
			final ExpressionList list = f.getRight();
			if (list != null) { addFacet(elt, WITH, convExpr(list)); }
		}
		// Not yet implemented but maybe we should in the near future.
		// if (EGaml.getInstance().hasFacets(stm)) {
		// addFacet(elt, IInternalFacets.GAML_ERROR, GAML.getExpressionDescriptionFactory()
		// .createConstant("Deprecated facets used. Use the functional form instead."));
		// }
	}

	/**
	 * Convert block.
	 *
	 * @param elt
	 *            the elt
	 * @param block
	 *            the block
	 */
	public void convertBlock(final String keyword, final ISyntacticElement elt, final Block block) {
		if (ACTION.equals(keyword) && block == null) {
			elt.setFacet(IKeyword.VIRTUAL, GAML.getExpressionDescriptionFactory().getTrue());
		}
		if (block != null) { convStatements(elt, EGAML.getStatementsOf(block)); }
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
			e.setFacet(IInternalFacets.DUPLICATE_FACET, GAML.getExpressionDescriptionFactory().createConstant(key));
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
			final ISyntacticElement elseElt = FACTORY.create(ELSE, elseBlock, EGAML.hasChildren(elseBlock));
			if (elseBlock instanceof Statement) {
				elseElt.addChild(convStatement(elt, (Statement) elseBlock));
			} else {
				convStatements(elseElt, EGAML.getStatementsOf(elseBlock));
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
			final ISyntacticElement catchElt = FACTORY.create(CATCH, catchBlock, EGAML.hasChildren(catchBlock));
			convStatements(catchElt, EGAML.getStatementsOf(catchBlock));
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
				final ISyntacticElement arg = FACTORY.create(ARG, def, false);
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
	// ASSIGNMENT CONVERSION SAMPLING
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
				&& EGAML.getExprsOf(((Access) expr).getRight()).size() == 0) {
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
			final List<Expression> args = EGAML.getExprsOf(((Access) expr).getRight());
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
	 * Applies a list of facets to the given syntactic element, converting each facet's expression and honouring the
	 * {@code isLabel} contract of the artefact descriptor.
	 *
	 * <p>
	 * This is the single shared implementation used by both
	 * {@link #convertFacets(Statement, String, ISyntacticElement)} and
	 * {@link #convertFacets(StandaloneExperiment, ISyntacticElement)}.
	 * </p>
	 *
	 * @param facets
	 *            the list of {@link Facet} objects to process; must not be null
	 * @param artefact
	 *            the artefact descriptor used to determine whether a facet value should be treated as a label; may be
	 *            null (all facets are then treated as non-label expressions)
	 * @param elt
	 *            the syntactic element to populate; must not be null
	 */
	private void applyFacets(final List<Facet> facets, final IArtefact.Symbol artefact, final ISyntacticElement elt) {
		for (final Facet f : facets) {
			final String fname = EGAML.getKeyOf(f);
			final boolean label = artefact != null && artefact.isLabel(fname);
			final IExpressionDescription fexpr = convExpr(f, label);
			addFacet(elt, fname, fexpr);
		}
	}

	/**
	 * Converts the facets of a {@link Statement} into the given syntactic element.
	 *
	 * <p>
	 * Facet names are normalised (assignments replaced by full names), and the statement's omissible (default) facet is
	 * added when it has not already been set. If the resulting keyword is {@code let} and a {@code type} facet is
	 * present, a {@code no_type_inference} flag is also set.
	 * </p>
	 *
	 * @param stm
	 *            the statement whose facets are to be converted
	 * @param keyword
	 *            the (possibly already normalised) keyword of the statement
	 * @param elt
	 *            the syntactic element to populate
	 */
	private void convertFacets(final Statement stm, final String keyword, final ISyntacticElement elt) {
		final IArtefact.Symbol p = ArtefactRegistry.getArtefact(keyword, null);
		final List<Facet> raw = EGAML.getFacetsOf(stm);
		// Normalise facet names before delegating to the shared helper
		if (!raw.isEmpty()) {
			for (final Facet f : raw) {
				String fname = replaceAssignments(keyword, EGAML.getKeyOf(f));
				final boolean label = p != null && p.isLabel(fname);
				final IExpressionDescription fexpr = convExpr(f, label);
				addFacet(elt, fname, fexpr);
			}
		}

		// Add the omissible (default) facet when it has not been supplied explicitly
		final String def = ArtefactRegistry.getOmissibleFacetForSymbol(keyword);
		if (def != null && !def.isEmpty() && !elt.hasFacet(def)) {
			final IExpressionDescription ed = findExpr(stm);
			if (ed != null) { elt.setFacet(def, ed); }
		}
		if (LET.equals(keyword) && elt.hasFacet(TYPE)) {
			elt.setFacet(NO_TYPE_INFERENCE, GAML.getExpressionDescriptionFactory().createConstant(true));
		}
	}

	/**
	 * Converts the facets of a {@link StandaloneExperiment} into the given syntactic element and also populates the
	 * {@code name} and {@code title} facets from the experiment's name.
	 *
	 * @param stm
	 *            the standalone experiment whose facets are to be converted
	 * @param elt
	 *            the syntactic element to populate
	 */
	private void convertFacets(final StandaloneExperiment stm, final ISyntacticElement elt) {
		final IArtefact.Symbol p = ArtefactRegistry.getArtefact(EXPERIMENT, null);
		applyFacets(EGAML.getFacetsOf(stm), p, elt);
		final IExpressionDescription ed = findExpr(stm);
		addFacet(elt, NAME, ed);
		addFacet(elt, TITLE, ed);
	}

	/**
	 * Replaces symbolic assignment facet names with their canonical GAML keyword equivalents.
	 *
	 * <p>
	 * The two substitutions performed are:
	 * </p>
	 * <ul>
	 * <li>{@code "<-"} → {@code "value"} (for {@code let} and {@code set} statements) or {@code "init"} otherwise</li>
	 * <li>{@code "->"} → {@code "function"}</li>
	 * </ul>
	 *
	 * @param keyword
	 *            the normalised keyword of the enclosing statement; used to decide between {@code value} and
	 *            {@code init}
	 * @param fname
	 *            the raw facet name extracted from the parsed model
	 * @return the canonical facet name, or {@code fname} unchanged if no substitution applies
	 */
	private String replaceAssignments(final String keyword, String fname) {
		// We change the "<-" and "->" symbols into full names
		if ("<-".equals(fname)) {
			fname = LET.equals(keyword) || SET.equals(keyword) ? VALUE : INIT;
		} else if ("->".equals(fname)) { fname = FUNCTION; }
		return fname;
	}

	/**
	 * Conv expr.
	 *
	 * @param expr
	 *            the expr
	 * @return the i expression description
	 */
	private final IExpressionDescription convExpr(final EObject expr) {
		return expr == null ? null : getExpressionDescriptionFactory().createFromEObject(expr);
	}

	/**
	 * Creates an {@link IExpressionDescription} that wraps a syntactic element as a block expression. Used to represent
	 * a block (e.g. an {@code on_change} handler) as a first-class expression value.
	 *
	 * @param expr
	 *            the syntactic element representing the block; may be null
	 * @return a block-based expression description, or {@code null} if {@code expr} is null
	 */
	private IExpressionDescription createBlockExpression(final ISyntacticElement expr) {
		return expr == null ? null : getExpressionDescriptionFactory().createBlock(expr);
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
				final ISyntacticElement elt = FACTORY.create(ACTION,
						new Facets(NAME, SYNTHETIC + syntheticActionCounter.getAndIncrement()), true);
				convertBlock(ACTION, elt, b);
				return createBlockExpression(elt);
			}
			if (expr != null) return label ? convertToLabel(expr, EGAML.getKeyOf(expr)) : convExpr(expr);
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
			if (IKeyword.GLOBAL.equals(EGAML.getKeyOf(stm))) {
				convStatements(elt, EGAML.getStatementsOf(stm.getBlock()));
				convertFacets(stm, IKeyword.GLOBAL, elt);
			} else {
				final ISyntacticElement child = convStatement(elt, stm);
				if (child != null) { elt.addChild(child); }
			}
		}
	}

	/**
	 * Resolves the primary name-or-expression description for a statement or standalone experiment EObject.
	 *
	 * <p>
	 * When {@code nameOnly} is {@code true} only a label derived from the element's name is returned (used for
	 * {@link StandaloneExperiment} where the expression is irrelevant). Otherwise the resolution order is:
	 * </p>
	 * <ol>
	 * <li>The element's name, returned as a label anchored to the element</li>
	 * <li>The element's embedded expression, converted directly</li>
	 * </ol>
	 *
	 * @param stm
	 *            the EObject to resolve – either a {@link Statement} or a {@link StandaloneExperiment}; may be null
	 * @param nameOnly
	 *            when {@code true} only the element's name is considered (no fall-through to expression)
	 * @return the resolved {@link IExpressionDescription}, or {@code null} if {@code stm} is null or nothing can be
	 *         resolved
	 */
	private IExpressionDescription findExpr(final EObject stm, final boolean nameOnly, final boolean nameAsLabel) {
		if (stm == null) return null;
		final String name = EGAML.getNameOf(stm);
		if (name != null) return nameAsLabel ? convertToLabel(stm, name) : convExpr(stm);
		if (nameOnly) return null;
		// Only reachable for Statement objects that carry an expression
		final Expression expr = stm instanceof Statement s ? s.getExpr() : null;
		return expr != null ? convExpr(expr) : null;
	}

	/**
	 * Convenience overload for {@link Statement} objects that may carry either a name or an embedded expression.
	 *
	 * @param stm
	 *            the {@link Statement} to resolve; may be null
	 * @return the resolved {@link IExpressionDescription}, or {@code null}
	 */
	private IExpressionDescription findExpr(final Statement stm) {
		return findExpr(stm, false, false);
	}

	/**
	 * Convenience overload for {@link StandaloneExperiment} objects where only the name is relevant.
	 *
	 * @param stm
	 *            the {@link StandaloneExperiment} to resolve; may be null
	 * @return the resolved {@link IExpressionDescription}, or {@code null}
	 */
	private IExpressionDescription findExpr(final StandaloneExperiment stm) {
		return findExpr(stm, true, true);
	}

}

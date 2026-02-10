/*******************************************************************************************************
 *
 * EGaml.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import gama.api.compilation.ast.ISyntacticFactory;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.constants.IKeyword;
import gama.dev.COUNTER;
import gaml.compiler.gaml.impl.ActionArgumentsImpl;
import gaml.compiler.gaml.impl.BlockImpl;
import gaml.compiler.gaml.impl.ExpressionListImpl;
import gaml.compiler.gaml.impl.HeadlessExperimentImpl;
import gaml.compiler.gaml.impl.ModelImpl;
import gaml.compiler.gaml.impl.S_ActionImpl;
import gaml.compiler.gaml.impl.S_EquationsImpl;
import gaml.compiler.gaml.impl.S_IfImpl;
import gaml.compiler.gaml.impl.StatementImpl;

/**
 * The class EGaml. A stateless utility class providing helper methods to work with various GAML statements and
 * expressions. This class acts as a facade over the EMF-generated GAML model classes, providing convenient access
 * methods with proper type safety and null handling.
 *
 * <p>
 * Performance considerations:
 * <ul>
 * <li>Uses singleton pattern to avoid repeated instantiation</li>
 * <li>Caches expensive lookups where appropriate</li>
 * <li>Returns immutable empty collections instead of creating new ones</li>
 * </ul>
 *
 * @author drogoul
 * @since 2012
 *
 */
public class EGaml {

	/** The singleton instance. */
	private static final EGaml instance = new EGaml();

	/** Cache for empty argument lists to avoid repeated allocations. */
	private static final List<ArgumentDefinition> EMPTY_ARG_LIST = Collections.emptyList();

	/** Cache for empty facet lists to avoid repeated allocations. */
	private static final List<Facet> EMPTY_FACET_LIST = Collections.emptyList();

	/** Cache for empty statement lists to avoid repeated allocations. */
	private static final List<Statement> EMPTY_STATEMENT_LIST = Collections.emptyList();

	/** Cache for empty expression lists to avoid repeated allocations. */
	private static final List<Expression> EMPTY_EXPRESSION_LIST = Collections.emptyList();

	/** Cache for empty assignment lists to avoid repeated allocations. */
	private static final List<S_Assignment> EMPTY_ASSIGNMENT_LIST = Collections.emptyList();

	/** Commonly used string constants for performance. */
	private static final String VALUE = "value";

	/** The Constant INIT. */
	private static final String INIT = "init";

	/** The Constant ITEM. */
	private static final String ITEM = "item";

	/** The Constant SPECIES. */
	private static final String SPECIES = "species";

	/** The Constant COLON. */
	private static final String COLON = ":";

	/** The Constant LESS_THAN. */
	private static final String LESS_THAN = "<";

	/** The Constant GREATER_THAN. */
	private static final String GREATER_THAN = ">";

	/** The Constant ARROW. */
	private static final String ARROW = "<-";

	/** The Constant LEFT_PAREN. */
	private static final String LEFT_PAREN = "(";

	/** The Constant RIGHT_PAREN. */
	private static final String RIGHT_PAREN = ")";

	/** The Constant LEFT_BRACE. */
	private static final String LEFT_BRACE = "{";

	/** The Constant RIGHT_BRACE. */
	private static final String RIGHT_BRACE = "}";

	/** The Constant COMMA. */
	private static final String COMMA = ",";

	/** The Constant DOT. */
	private static final String DOT = ".";

	/** The Constant QUESTION_MARK. */
	private static final String QUESTION_MARK = "?";

	/**
	 * Gets the singleton instance of EGaml.
	 *
	 * @return the singleton instance of EGaml
	 */
	public static EGaml getInstance() { return instance; }

	/**
	 * Gets the name of an EObject. Extracts the name from various GAML statement types including reflexes, definitions,
	 * displays, and experiments. For reflexes without explicit names, generates an internal name based on the keyword
	 * and a counter.
	 *
	 * @param o
	 *            the EObject to extract the name from
	 * @return the name of the object, or null if the object type doesn't have a name
	 */
	public String getNameOf(final EObject o) {
		return switch (o) {
			case S_Reflex r -> {
				String s = r.getName();
				yield s == null ? IKeyword.INTERNAL + getKeyOf(r) + COUNTER.COUNT() : s;
			}
			case GamlDefinition g -> g.getName();
			case S_Display d -> d.getName();
			case HeadlessExperiment he -> he.getName();
			case null, default -> null;
		};
	}

	/**
	 * Gets the expressions from an expression list. Safely extracts the list of expressions if the container is
	 * properly set.
	 *
	 * @param o
	 *            the EObject that may contain an expression list
	 * @return the list of expressions, or an empty list if not available
	 */
	public List<Expression> getExprsOf(final EObject o) {
		if (o instanceof ExpressionListImpl eli && eli.eIsSet(GamlPackage.EXPRESSION_LIST__EXPRS))
			return eli.getExprs();
		return EMPTY_EXPRESSION_LIST;
	}

	/**
	 * Gets the argument definitions from an action's arguments. Safely extracts the list of argument definitions if
	 * properly set.
	 *
	 * @param args
	 *            the action arguments object
	 * @return the list of argument definitions, or an empty list if not available
	 */
	public List<ArgumentDefinition> getArgsOf(final EObject args) {
		if (args instanceof ActionArgumentsImpl
				&& ((ActionArgumentsImpl) args).eIsSet(GamlPackage.ACTION_ARGUMENTS__ARGS))
			return ((ActionArgumentsImpl) args).getArgs();
		return EMPTY_ARG_LIST;
	}

	/**
	 * Gets the facets of a statement. Extracts the list of facets from statements or headless experiments.
	 *
	 * @param s
	 *            the statement or experiment object
	 * @return the list of facets, or an empty list if not available
	 */
	public List<Facet> getFacetsOf(final EObject s) {
		if (s instanceof StatementImpl) {
			if (((StatementImpl) s).eIsSet(GamlPackage.STATEMENT__FACETS)) return ((StatementImpl) s).getFacets();
		} else if (s instanceof HeadlessExperimentImpl
				&& ((HeadlessExperimentImpl) s).eIsSet(GamlPackage.HEADLESS_EXPERIMENT__FACETS))
			return ((HeadlessExperimentImpl) s).getFacets();
		return EMPTY_FACET_LIST;
	}

	/**
	 * Gets the facets map of a statement. Converts the facet list into a map keyed by facet name for efficient lookup
	 * operations.
	 *
	 * @param s
	 *            the statement or experiment object
	 * @return a map of facet names to Facet objects, or an empty map if no facets exist
	 */
	public Map<String, Facet> getFacetsMapOf(final EObject s) {
		final List<? extends EObject> list = getFacetsOf(s);
		if (list.isEmpty()) return Collections.emptyMap();
		final Map<String, Facet> map = new HashMap<>();
		for (final EObject f : list) { if (f instanceof Facet) { map.put(getKeyOf(f), (Facet) f); } }
		return map;
	}

	/**
	 * Tells if a facet is present in a statement. This is more efficient than getting the full facet map when you only
	 * need to check for existence.
	 *
	 * @param s
	 *            the statement or experiment object
	 * @param facet
	 *            the name of the facet to check for
	 * @return true if the facet exists, false otherwise
	 */
	public boolean hasFacet(final EObject s, final String facet) {
		final List<? extends EObject> list = getFacetsOf(s);
		if (list.isEmpty()) return false;
		for (final EObject f : list) {
			if (f instanceof Facet) {
				final String name = getKeyOf(f);
				if (facet.equals(name)) return true;
			}
		}
		return false;
	}

	/**
	 * Gets one particular facet expression of a statement by key name. This method handles special cases for value
	 * initialization and assignments.
	 *
	 * @param s
	 *            the statement or object containing facets
	 * @param name
	 *            the name of the facet key to retrieve
	 * @return the expression associated with the key, or null if not found
	 */
	public Expression getExpressionAtKey(final EObject s, final String name) {
		if (s == null || name == null) return null;
		if (VALUE.equals(name) && s instanceof S_DirectAssignment) return ((S_DirectAssignment) s).getValue();
		final List<Facet> list = getFacetsOf(s);

		for (final Facet f : list) {
			final String key = getKeyOf(f);
			if (s instanceof Statement && (VALUE.equals(name) || INIT.equals(name))) {
				if (ARROW.equals(key)) return f.getExpr();
			} else if (s instanceof S_Assignment && ITEM.equals(name)
					&& (key.contains(LESS_THAN) || key.contains(GREATER_THAN)))
				return f.getExpr();
			if (name.equals(key)) return f.getExpr();
		}
		return null;
	}

	/**
	 * Gets the expression from an EObject. Handles both direct Expression objects and Statements that contain an
	 * expression.
	 *
	 * @param s
	 *            the EObject to extract the expression from
	 * @return the expression, or null if the object doesn't contain an expression
	 */
	public Expression getExprOf(final EObject s) {
		if (s instanceof Expression) return (Expression) s;
		if (s instanceof Statement) return ((Statement) s).getExpr();
		return null;
	}

	/**
	 * Checks if the given EObject has children. Uses an optimized type dispatch mechanism to determine whether the
	 * object contains nested elements.
	 *
	 * @param obj
	 *            the EObject to check for children
	 * @return true if the object has children, false otherwise
	 */

	public boolean hasChildren(final EObject obj) {
		return switch (obj) {
			case ModelImpl mi -> mi.eIsSet(GamlPackage.MODEL__BLOCK);
			case S_EquationsImpl si -> si.eIsSet(GamlPackage.SEQUATIONS__EQUATIONS);
			case S_ActionImpl si -> si.eIsSet(GamlPackage.SACTION__ARGS) || si.eIsSet(GamlPackage.STATEMENT__BLOCK)
					|| hasFacet(si, IKeyword.VIRTUAL);
			case BlockImpl bi -> bi.eIsSet(GamlPackage.BLOCK__STATEMENTS);
			case HeadlessExperimentImpl hi -> hi.eIsSet(GamlPackage.HEADLESS_EXPERIMENT__BLOCK);
			case ExperimentFileStructure efs -> true;
			case S_IfImpl si -> si.eIsSet(GamlPackage.STATEMENT__BLOCK) || hasFacet(si, IKeyword.VIRTUAL)
					|| si.eIsSet(GamlPackage.SIF__ELSE);
			case StatementImpl si -> si.eIsSet(GamlPackage.STATEMENT__BLOCK) || hasFacet(si, IKeyword.VIRTUAL);
			default -> false;
		};
	}

	/**
	 * Gets the statements of a block.
	 *
	 * @param block
	 *            the block object to extract statements from
	 * @return the list of statements, or an empty list if not available
	 */
	public List<Statement> getStatementsOf(final EObject block) {
		if (block instanceof BlockImpl bi) {
			if (bi.eIsSet(GamlPackage.BLOCK__STATEMENTS)) return bi.getStatements();
		} else if (block instanceof Model m) return getStatementsOf(m.getBlock());
		return EMPTY_STATEMENT_LIST;
	}

	/**
	 * Gets the equations of a system of equations.
	 *
	 * @param stm
	 *            the statement containing equations
	 * @return the list of equation assignments, or an empty list if not available
	 */
	public List<S_Assignment> getEquationsOf(final EObject stm) {
		if (stm instanceof S_EquationsImpl e && e.eIsSet(GamlPackage.SEQUATIONS__EQUATIONS)) return e.getEquations();
		return EMPTY_ASSIGNMENT_LIST;
	}

	/**
	 * Gets the key of an eObject
	 *
	 * @param f
	 *            the f
	 * @return the key of
	 */

	public String getKeyOf(final EObject f) {
		if (f == null) return null;
		return getKeyOf(f, f.eClass());
	}

	/**
	 * Gets the key of an eObject in a given eClass
	 *
	 * @param object
	 *            the object
	 * @param clazz
	 *            the clazz
	 * @return the key of
	 */

	public String getKeyOf(final EObject object, final EClass clazz) {
		final int id = clazz.getClassifierID();
		return switch (id) {
			case GamlPackage.UNARY -> ((Unary) object).getOp();
			case GamlPackage.BINARY_OPERATOR -> ((BinaryOperator) object).getOp();
			case GamlPackage.ARGUMENT_PAIR -> getKeyOfArgumentPair((ArgumentPair) object);
			case GamlPackage.PARAMETER -> getKeyOfParameter((Parameter) object);
			case GamlPackage.MODEL -> IKeyword.MODEL;
			case GamlPackage.STATEMENT -> getKeyOfStatement((Statement) object);
			case GamlPackage.FACET -> getKeyOfFacet((Facet) object);
			case GamlPackage.FUNCTION -> getKeyOf(((Function) object).getLeft());
			case GamlPackage.TYPE_REF -> getKeyOfTypeRef((TypeRef) object);
			case GamlPackage.IF -> QUESTION_MARK;
			case GamlPackage.VARIABLE_REF, GamlPackage.UNIT_NAME, GamlPackage.ACTION_REF, GamlPackage.SKILL_REF, GamlPackage.EQUATION_REF -> getNameOfRef(
					object, id);
			case GamlPackage.INT_LITERAL, GamlPackage.STRING_LITERAL, GamlPackage.DOUBLE_LITERAL, GamlPackage.RESERVED_LITERAL, GamlPackage.BOOLEAN_LITERAL, GamlPackage.TERMINAL_EXPRESSION -> ((TerminalExpression) object)
					.getOp();
			default -> {
				final List<EClass> eSuperTypes = clazz.getESuperTypes();
				yield eSuperTypes.isEmpty() ? null : getKeyOf(object, eSuperTypes.get(0));
			}
		};
	}

	/**
	 * Gets the key of argument pair, removing trailing colon if present.
	 *
	 * @param object
	 *            the argument pair object
	 * @return the key of argument pair
	 */
	public String getKeyOfArgumentPair(final ArgumentPair object) {
		String s = object.getOp();
		if (s == null) return null;
		return s.endsWith(COLON) ? s.substring(0, s.length() - 1) : s;
	}

	/**
	 * Gets the key of facet, removing trailing colon if present.
	 *
	 * @param object
	 *            the facet object
	 * @return the key of facet
	 */
	private String getKeyOfFacet(final Facet object) {
		String s = object.getKey();
		return s.endsWith(COLON) ? s.substring(0, s.length() - 1) : s;
	}

	/**
	 * Gets the key of type ref, handling special cases like species types.
	 *
	 * @param object
	 *            the type reference object
	 * @return the key of type ref
	 */
	private String getKeyOfTypeRef(final TypeRef object) {
		String s = getNameOfRef(object, GamlPackage.TYPE_REF);
		if (s.contains(LESS_THAN)) {
			s = s.split(LESS_THAN)[0];
			// Special case for the 'species<xxx>' case
			if (SPECIES.equals(s)) { s = ISyntacticFactory.SPECIES_VAR; }
		}
		return s;
	}

	/**
	 * Gets the key of parameter, handling both left expressions and built-in facet keys.
	 *
	 * @param p
	 *            the parameter object
	 * @return the key of parameter
	 */
	public String getKeyOfParameter(final Parameter p) {
		String s = getKeyOf(p.getLeft());
		if (s == null) { s = p.getBuiltInFacetKey(); }
		return s.endsWith(COLON) ? s.substring(0, s.length() - 1) : s;
	}

	/**
	 * Gets the key of statement.
	 *
	 * @param object
	 *            the object
	 * @return the key of statement
	 */
	private String getKeyOfStatement(final Statement object) {
		String s = object.getKey();
		if (s == null && object instanceof S_Definition sd) {
			final TypeRef type = (TypeRef) sd.getTkey();
			if (type != null) return getKeyOfTypeRef(type);
		}
		return s;
	}

	/**
	 * Gets the name of the ref represented by this eObject. This method handles various types of references including
	 * units, variables, actions, skills, equations, and types.
	 *
	 * @param o
	 *            the EObject representing a reference
	 * @return the name of ref
	 */

	public String getNameOfRef(final EObject o) {
		return getNameOfRef(o, o.eClass().getClassifierID());
	}

	/**
	 * Gets the name of ref for a specific classifier type. This is an internal helper method that extracts names based
	 * on the EMF classifier ID for performance optimization.
	 *
	 * @param o
	 *            the EObject
	 * @param id
	 *            the classifier ID
	 * @return the name of ref
	 */
	private String getNameOfRef(final EObject o, final int id) {
		String result = "";

		switch (id) {
			case GamlPackage.UNIT_NAME: {
				UnitFakeDefinition ref = ((UnitName) o).getRef();
				if (ref != null) { result = ref.getName(); }
				break;
			}
			case GamlPackage.VARIABLE_REF: {
				VarDefinition ref = ((VariableRef) o).getRef();
				if (ref != null) {
					if (ref instanceof ModelImpl mi) {
						result = mi.getName() + IModelDescription.MODEL_SUFFIX;
					} else {
						result = ref.getName();
					}
				}
				break;
			}
			case GamlPackage.ACTION_REF: {
				ActionDefinition ref = ((ActionRef) o).getRef();
				if (ref != null) { result = ref.getName(); }
				break;
			}
			case GamlPackage.SKILL_REF: {
				SkillFakeDefinition ref = ((SkillRef) o).getRef();
				if (ref != null) { result = ref.getName(); }
				break;
			}
			case GamlPackage.EQUATION_REF: {
				EquationDefinition ref = ((EquationRef) o).getRef();
				if (ref != null) { result = ref.getName(); }
				break;
			}
			case GamlPackage.TYPE_REF: {
				TypeDefinition ref = ((TypeRef) o).getRef();
				if (ref != null) { result = ref.getName(); }
				break;
			}
		}

		if (result == null || result.isBlank()) {
			final ICompositeNode cc = NodeModelUtils.getNode(o);
			if (cc != null) { result = NodeModelUtils.getTokenText(cc); }
		}
		return result;
	}

	/**
	 * Gets the factory for building eObjects
	 *
	 * @return the factory
	 */
	public GamlFactory getFactory() { return (GamlFactory) GamlPackage.eINSTANCE.getEFactoryInstance(); }

	/**
	 * Save an eObject into a string
	 *
	 * @param expr
	 *            the expr
	 * @return the string
	 */

	public String toString(final EObject expr) {
		if (expr == null) return null;
		if (expr instanceof Statement) return getNameOf(expr);
		if (expr instanceof Facet) return ((Facet) expr).getName();

		if (!(expr instanceof Expression)) return expr.toString();
		final StringBuilder serializer = new StringBuilder(100);
		serialize(serializer, (Expression) expr);
		return serializer.toString();
	}

	/**
	 * Serialize an expression
	 *
	 * @param serializer
	 *            a string builder to which the expression should be appended
	 * @param expr
	 *            the expr
	 */
	private void serialize(final StringBuilder serializer, final Expression expr) {
		switch (expr) {
			case null -> {
			}
			case If i -> {
				serializer.append(LEFT_PAREN);
				serialize(serializer, i.getLeft());
				serializer.append(RIGHT_PAREN).append(i.getOp()).append(LEFT_PAREN);
				serialize(serializer, i.getRight());
				serializer.append(RIGHT_PAREN).append(COLON);
				serialize(serializer, i.getIfFalse());
			}
			case StringLiteral sl -> serializer.append(sl.getOp());
			case TerminalExpression te -> serializer.append(te.getOp());
			case Point p -> {
				serializer.append(LEFT_BRACE).append(LEFT_PAREN);
				serialize(serializer, p.getLeft());
				serializer.append(RIGHT_PAREN).append(p.getOp()).append(LEFT_PAREN);
				serialize(serializer, p.getRight());
				serializer.append(RIGHT_PAREN);
				if (p.getZ() != null) {
					serializer.append(',').append(LEFT_PAREN);
					serialize(serializer, p.getZ());
					serializer.append(RIGHT_PAREN);
				}
				serializer.append(RIGHT_BRACE);
			}

			case Unary u -> {
				serializer.append(u.getOp()).append(LEFT_PAREN);
				serialize(serializer, u.getRight());
				serializer.append(RIGHT_PAREN);
			}
			case Function f -> function(serializer, f);
			case Access access -> {
				serialize(serializer, access.getLeft());
				serializer.append(DOT);
				serialize(serializer, access.getRight());
			}
			case Array a -> array(serializer, a.getExprs().getExprs(), false);
			case VariableRef v -> {
				serializer.append(getKeyOf(v));
			}
			case TypeRef t -> {
				serializer.append(getKeyOf(t));
			}
			case SkillRef s -> {
				serializer.append(getKeyOf(s));
			}
			case ActionRef a -> {
				serializer.append(getKeyOf(a));
			}
			case UnitName u -> {
				serializer.append(getKeyOf(u));
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + expr);

		}
	}

	/**
	 * Serializes a function
	 *
	 * @param serializer
	 *            a string builder to which the function should be appended
	 * @param expr
	 *            the expr
	 */
	private void function(final StringBuilder serializer, final Function expr) {
		final List<? extends EObject> args = getExprsOf(expr.getRight());
		final String opName = getKeyOf(expr.getLeft());
		final int size = args.size();
		switch (size) {
			case 1 -> {
				serializer.append(opName).append(LEFT_PAREN);
				serialize(serializer, (Expression) args.get(0));
				serializer.append(RIGHT_PAREN);
			}
			case 2 -> {
				serializer.append(LEFT_PAREN);
				serialize(serializer, (Expression) args.get(0));
				serializer.append(RIGHT_PAREN).append(opName).append(LEFT_PAREN);
				serialize(serializer, (Expression) args.get(1));
				serializer.append(RIGHT_PAREN);
			}
			default -> {
				serializer.append(opName);
				serializer.append(LEFT_PAREN);
				array(serializer, args, true);
				serializer.append(RIGHT_PAREN);
			}
		}

	}

	/**
	 * Serializes a list of arguments
	 *
	 * @param serializer
	 *            a string builder to which the args should be appended
	 * @param args
	 *            the args
	 * @param arguments
	 *            the arguments
	 */
	private void array(final StringBuilder serializer, final List<? extends EObject> args, final boolean arguments) {
		// if arguments is true, parses the list to transform it into a map of
		// args
		// (starting at 1); Experimental right now
		// serializer.append("[");
		final int size = args.size();
		for (int i = 0; i < size; i++) {
			final Expression e = (Expression) args.get(i);
			if (arguments) { serializer.append("arg").append(i).append("::"); }
			serialize(serializer, e);
			if (i < size - 1) { serializer.append(COMMA); }
		}
		// serializer.append("]");
	}

	/**
	 * Gets the statement equal to or based on this object (if the object is a type ref and the statement the definition
	 * beginning with this type ref)
	 *
	 * @param o
	 *            the o
	 * @return the statement
	 */

	public Statement getStatement(final EObject o) {
		if (o instanceof Statement s) return s;
		if (o instanceof TypeRef t && t.eContainer() instanceof S_Definition s && s.getTkey() == o) return s;
		return null;
	}

	/**
	 * Gets the first surrounding statement that includes this object (or the object itself if it is a statement)
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param o
	 *            the o
	 * @return the surrounding statement
	 * @date 2 janv. 2024
	 */

	public Statement getSurroundingStatement(final EObject o) {
		if (o == null) return null;
		if (o instanceof Statement) return (Statement) o;
		return getSurroundingStatement(o.eContainer());
	}

	/**
	 * Creates the gaml definition.
	 *
	 * @param t
	 *            the t
	 * @param eClass
	 *            the e class
	 * @return the gaml definition
	 */
	public GamlDefinition createGamlDefinition(final String t, final EClass eClass) {
		GamlDefinition stub = (GamlDefinition) getFactory().create(eClass);
		stub.setName(t);
		return stub;
	}

}

/*******************************************************************************************************
 *
 * GamlExpressionCompiler.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;
import static gama.api.constants.IKeyword.AS;
import static gama.api.constants.IKeyword.EACH;
import static gama.api.constants.IKeyword.EXPERIMENT;
import static gama.api.constants.IKeyword.IS;
import static gama.api.constants.IKeyword.IS_SKILL;
import static gama.api.constants.IKeyword.MY;
import static gama.api.constants.IKeyword.MYSELF;
import static gama.api.constants.IKeyword.NULL;
import static gama.api.constants.IKeyword.OF;
import static gama.api.constants.IKeyword.POINT;
import static gama.api.constants.IKeyword.SELF;
import static gama.api.constants.IKeyword.SPECIES;
import static gama.api.constants.IKeyword.SUPER;
import static gama.api.constants.IKeyword.TRUE;
import static gama.api.constants.IKeyword.UNKNOWN;
import static gama.api.constants.IKeyword._DOT;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;

import gama.api.GAMA;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.ast.ISyntacticFactory;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVarDescriptionProvider;
import gama.api.compilation.factories.IExpressionDescriptionFactory;
import gama.api.compilation.factories.IExpressionFactory;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.compilation.validation.IValidationContext;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaMapFactory;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionCompiler;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.ParametricType;
import gama.api.gaml.types.Signature;
import gama.api.gaml.types.Types;
import gama.api.runtime.IExecutionContext;
import gama.api.utils.collections.Collector;
import gaml.compiler.gaml.Access;
import gaml.compiler.gaml.ActionRef;
import gaml.compiler.gaml.ArgumentPair;
import gaml.compiler.gaml.Array;
import gaml.compiler.gaml.BinaryOperator;
import gaml.compiler.gaml.BooleanLiteral;
import gaml.compiler.gaml.DoubleLiteral;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.EquationRef;
import gaml.compiler.gaml.Expression;
import gaml.compiler.gaml.ExpressionList;
import gaml.compiler.gaml.Function;
import gaml.compiler.gaml.If;
import gaml.compiler.gaml.IntLiteral;
import gaml.compiler.gaml.Parameter;
import gaml.compiler.gaml.Point;
import gaml.compiler.gaml.ReservedLiteral;
import gaml.compiler.gaml.SkillFakeDefinition;
import gaml.compiler.gaml.SkillRef;
import gaml.compiler.gaml.StringEvaluator;
import gaml.compiler.gaml.StringLiteral;
import gaml.compiler.gaml.TypeDefinition;
import gaml.compiler.gaml.TypeInfo;
import gaml.compiler.gaml.TypeRef;
import gaml.compiler.gaml.Unary;
import gaml.compiler.gaml.Unit;
import gaml.compiler.gaml.UnitName;
import gaml.compiler.gaml.VarDefinition;
import gaml.compiler.gaml.VariableRef;
import gaml.compiler.gaml.ast.SyntacticModelElement;
import gaml.compiler.gaml.descriptions.DoDescription;
import gaml.compiler.gaml.descriptions.ExperimentDescription;
import gaml.compiler.gaml.descriptions.ModelDescription;
import gaml.compiler.gaml.descriptions.PlatformSpeciesDescription;
import gaml.compiler.gaml.descriptions.StringBasedExpressionDescription;
import gaml.compiler.gaml.resource.GamlResource;
import gaml.compiler.gaml.resource.GamlResourceServices;
import gaml.compiler.gaml.util.GamlSwitch;

/**
 * The GamlExpressionCompiler transforms GAML language constructs (strings or XText Expression objects) into executable
 * IExpression objects. This class serves as the core compiler for GAML expressions, handling parsing, type checking,
 * and compilation of various GAML language constructs including:
 *
 * <ul>
 * <li>Variable references and field access</li>
 * <li>Function calls and operator expressions</li>
 * <li>Type casting and validation</li>
 * <li>Iterator expressions with 'each' variable handling</li>
 * <li>Literal values (integers, floats, strings, booleans)</li>
 * <li>Complex expressions (arrays, points, conditionals)</li>
 * </ul>
 *
 * <h2>Performance Characteristics:</h2>
 * <p>
 * This class implements several performance optimizations:
 * </p>
 * <ul>
 * <li>High-performance Guava cache with automated eviction for context-independent expressions</li>
 * <li>Size-based eviction (LRU) and time-based expiration to prevent memory leaks</li>
 * <li>Cache statistics monitoring for performance analysis and optimization</li>
 * <li>Efficient data structures for iterator context management</li>
 * <li>Thread-safe caching mechanisms for multi-threaded environments</li>
 * <li>Resource pooling to minimize object creation overhead</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * <p>
 * This class is designed to be thread-safe for read operations. The expression cache uses Guava's thread-safe cache
 * implementation to allow safe concurrent access. However, compilation operations should be synchronized externally if
 * needed in multi-threaded contexts.
 * </p>
 *
 * <h2>Usage Pattern:</h2>
 * <p>
 * Typically invoked through an IExpressionFactory (the default being GAML.getExpressionFactory()). The compiler
 * maintains compilation context including current species, types manager, and validation context to ensure proper
 * scoping and type resolution.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see IExpressionCompiler
 * @see IExpression
 * @see IExpressionFactory
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlExpressionCompiler extends GamlSwitch<IExpression> implements IExpressionCompiler<Expression> {

	/** Maximum size for the expression cache to prevent unbounded memory growth */
	private static final int MAX_CACHE_SIZE = 1000;

	/** Maximum idle time for cached expressions (in minutes) */
	private static final int CACHE_EXPIRE_MINUTES = 30;

	/** The iterator contexts stack. Using ArrayDeque for better performance than LinkedList. */
	private final Deque<IVarExpression> iteratorContexts = new ArrayDeque<>();

	/** The current expression description. Used to disable reentrant parsing (Issue 782) */
	private IExpressionDescription currentExpressionDescription;

	/** The current types manager for type resolution in the current compilation context */
	private ITypesManager currentTypesManager;

	/**
	 * High-performance Guava cache for constant synthetic expressions with automated eviction. Features: - Size-based
	 * eviction (LRU when cache exceeds MAX_CACHE_SIZE) - Time-based eviction (entries expire after CACHE_EXPIRE_MINUTES
	 * of inactivity) - Thread-safe concurrent access - Automatic cache statistics and monitoring
	 */
	private static final Cache<String, IExpression> constantSyntheticExpressions =
			CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE)
					.expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES).recordStats().build();

	/**
	 * The current parsing context (IDescription) in which the compiler operates. If none is given, the global context
	 * of the current simulation is returned (via simulation.getModel().getDescription()) if available. Otherwise, only
	 * simple expressions (containing constants) can be parsed.
	 */
	private IDescription currentContext;

	static {
		GAML.OPERATORS.put(MY, GamaMapFactory.createUnordered());
	}

	@Override
	public IExpression compile(final IExpressionDescription s, final IDescription parsingContext) {
		// Cf. Issue 782. Returns the expression if an expression needs its
		// compiled version to be compiled.
		if (s.isConst() || s == getCurrentExpressionDescription()) return s.getExpression();

		setCurrentExpressionDescription(s);
		final EObject o = s.getTarget();

		if (o == null && s instanceof StringBasedExpressionDescription) {
			final IExecutionContext context =
					GAMA.getExperiment() == null ? null : GAMA.getRuntimeScope().getExecutionContext();
			return compile(s.toString(), parsingContext, context);
		}

		final IDescription previous = setContext(parsingContext);
		try {
			return compile(o);
		} finally {
			setContext(previous);
			setCurrentExpressionDescription(null);
		}
	}

	@Override
	public IExpression compile(final String expression, final IDescription parsingContext,
			final IExecutionContext tempContext) {
		final IDescription previous = setContext(parsingContext);
		try {
			// Check cache first for performance
			IExpression result = constantSyntheticExpressions.getIfPresent(expression);
			if (result != null) return result;

			final EObject o = getEObjectOf(expression, tempContext);
			result = compile(o);

			// Cache context-independent expressions - Guava handles eviction automatically
			if (result != null && result.isContextIndependant()) {
				constantSyntheticExpressions.put(expression, result);
			}
			return result;
		} finally {
			setContext(previous);
			setCurrentExpressionDescription(null);
		}
	}

	/**
	 * Compiles an EObject into an IExpression.
	 *
	 * @param s
	 *            the EObject to compile (can be null for error handling)
	 * @return the compiled expression, or null if compilation fails
	 */
	private IExpression compile(final EObject s) {
		// No error since null expressions come from previous (more focused) errors and not from the parser itself.
		if (s == null) return null;

		final IExpression expr = doSwitch(s.eClass().getClassifierID(), s);
		if (expr != null && getContext() != null) { getContext().document(s, expr); }
		return expr;
	}

	/**
	 * Creates a skill expression for the given skill name.
	 *
	 * @param name
	 *            the name of the skill
	 * @return an expression representing the skill constant
	 */
	private IExpression skill(final String name) {
		return getFactory().createConst(name, Types.SKILL);
	}

	/**
	 * Compiles a unary operation expression. Handles special cases like 'my', species casting, and field access.
	 *
	 * @param op
	 *            the unary operator
	 * @param e
	 *            the operand expression
	 * @return the compiled unary expression, or null if compilation fails
	 */
	private IExpression unary(final String op, final Expression e) {
		if (op == null) return null;

		final IExpression expr = compile(e);
		if (expr == null) return null;

		if (MY.equals(op)) {
			final IVarDescriptionProvider desc = getContext().getDescriptionDeclaringVar(MYSELF);
			if (desc instanceof IDescription) {
				// We are in a remote context, so 'my' refers to the calling agent
				final IExpression myself = desc.getVarExpr(MYSELF, false);
				final IDescription species = myself.getGamlType().getSpecies();
				final IExpression var = species.getVarExpr(EGaml.getInstance().getKeyOf(e), true);
				return getFactory().createOperator(_DOT, (IDescription) desc, e, myself, var);
			}
			// Otherwise, we ignore 'my' since it refers to 'self'
			return expr;
		}

		// The unary "unit" operator should let the value of its child pass through
		if ("#".equals(op)) return expr;

		if (isSpeciesName(op)) return getFactory().createAs(getContext(), expr, getSpeciesContext(op).getSpeciesExpr());

		// Check for field getter
		IArtefactProto proto = expr.getGamlType().getGetter(op);
		if (proto != null) {
			// It can only be a field as 'actions' are not defined on simple objects
			final TypeFieldExpression fieldExpr =
					(TypeFieldExpression) GAML.getExpressionFactory().createOperator(proto, getContext(), e, expr);
			if (getContext() != null) { getContext().document(e, expr); }
			return fieldExpr;
		}

		return getFactory().createOperator(op, getContext(), e, expr);
	}

	/**
	 * Creates a type casting expression that converts the given expression to the specified type. This method handles
	 * parametric types and validates type compatibility.
	 *
	 * @param type
	 *            the target type for casting
	 * @param toCast
	 *            the expression to be cast
	 * @param typeObject
	 *            the original type object from the AST (for error reporting)
	 * @return the casting expression, or null if the cast is invalid
	 */
	private IExpression casting(final IType type, final IExpression toCast, final Expression typeObject) {
		if (toCast == null) return null;

		final IType castingType = type.typeIfCasting(toCast);

		// Extract type information for parametric types
		TypeInfo typeInfo = null;
		if (typeObject instanceof TypeRef) {
			typeInfo = ((TypeRef) typeObject).getParameter();
		} else if (typeObject instanceof Function) { typeInfo = ((Function) typeObject).getType(); }

		// Determine key and content types for parametric types
		IType keyType = castingType.getKeyType();
		IType contentsType = castingType.getContentType();

		if (typeInfo != null) {
			IType kt = fromTypeRef((TypeRef) typeInfo.getFirst());
			IType ct = fromTypeRef((TypeRef) typeInfo.getSecond());

			// Handle single parameter case
			if (ct == null || ct == Types.NO_TYPE) {
				ct = kt;
				kt = null;
			}

			// Update types if specified
			if (ct != null && ct != Types.NO_TYPE) { contentsType = ct; }
			if (kt != null && kt != Types.NO_TYPE) { keyType = kt; }
		}

		final IType result = GamaType.from(castingType, keyType, contentsType);

		return getFactory().createAs(getContext().getSpeciesContext(), toCast,
				getFactory().createTypeExpression(result));
	}

	/**
	 * Resolves a TypeRef AST node to an IType, handling parametric types and type validation. This method supports
	 * nested type references for model aliases and validates parameter counts.
	 *
	 * @param object
	 *            the TypeRef object to resolve
	 * @return the resolved IType, or Types.NO_TYPE if resolution fails
	 */
	IType fromTypeRef(final TypeRef object) {
		if (object == null) return null;

		// Get primary type name
		String primary = EGaml.getInstance().getKeyOf(object);
		if (primary == null) {
			primary = object.getRef().getName();
		} else if (ISyntacticFactory.SPECIES_VAR.equals(primary)) { primary = SPECIES; }

		final IType t = currentTypesManager.get(primary);

		// Validate type name
		if (t == Types.NO_TYPE && !UNKNOWN.equals(primary)) {
			getContext().error(primary + " is not a valid type name", IGamlIssue.NOT_A_TYPE, object, primary);
			return t;
		}

		// Handle model_alias<species> case
		if (t.isAgentType() && t.getSpecies().isModel()) {
			final TypeInfo parameter = object.getParameter();
			if (parameter == null) return t;

			final TypeRef first = (TypeRef) parameter.getFirst();
			if (first == null) return t;

			// Switch to model's type manager for nested resolution
			final ITypesManager savedTypesManager = currentTypesManager;
			try {
				currentTypesManager = t.getSpecies().getModelDescription().getTypesManager();
				return fromTypeRef(first);
			} finally {
				currentTypesManager = savedTypesManager;
			}
		}

		// Document the type resolution
		getContext().document(object, t);
		if (t.isAgentType()) return t;

		// Handle parametric types
		final TypeInfo parameter = object.getParameter();
		if (parameter == null) return t;

		final int numberOfParameter = t.getNumberOfParameters();
		if (numberOfParameter == 0) {
			// Emit a warning for non-parametric types with parameters
			getContext().warning(t + " is not a parametric type. Type parameters will be ignored",
					IGamlIssue.WRONG_TYPE, object);
			return t;
		}

		// Process type parameters
		final TypeRef first = (TypeRef) parameter.getFirst();
		if (first == null) return t;

		final TypeRef second = (TypeRef) parameter.getSecond();
		if (second == null) {
			if (numberOfParameter == 2) {
				// Warning for missing second parameter
				getContext().warning(t + " expects two type parameters", IGamlIssue.WRONG_TYPE, object);
			}
			return GamaType.from(t, t.getKeyType(), fromTypeRef(first));
		}

		if (numberOfParameter == 1) {
			// Error for too many parameters
			getContext().error(t + " expects only one type parameter", IGamlIssue.WRONG_TYPE, object);
			return null;
		}

		return GamaType.from(t, fromTypeRef(first), fromTypeRef(second));
	}

	/**
	 * Compiles a binary operation, handling special cases for operators and iterator contexts. This method manages
	 * iterator variables like 'each' and supports n-ary operators.
	 *
	 * @param op
	 *            the binary operator
	 * @param left
	 *            the left operand expression
	 * @param originalExpression
	 *            the original expression AST node
	 * @return the compiled binary expression, or null if compilation fails
	 */
	private IExpression binary(final String op, final IExpression left, final Expression originalExpression) {
		if (left == null || op == null) return null;

		if (!GAML.OPERATORS.containsKey(op)) {
			getContext().error("Unknown operator: " + op, IGamlIssue.UNKNOWN_ACTION, originalExpression.eContainer(),
					op);
			return null;
		}

		Expression rightMember = originalExpression;

		// Handle iterator operators - initialize context-sensitive "each" variable
		final boolean isIterator = GAML.ITERATORS.contains(op);
		String argName = IKeyword.EACH;

		if (isIterator) {
			// Find the name of 'each' if redefined
			if (rightMember instanceof ExpressionList params) {
				final List<Expression> exprs = EGaml.getInstance().getExprsOf(params);
				if (!exprs.isEmpty()) {
					final Expression arg = exprs.get(0);
					if (arg instanceof Parameter p) {
						argName = EGaml.getInstance().getKeyOf(p);
						rightMember = p.getRight();
					} else {
						rightMember = arg;
					}
				}
			}

			// Set up iterator context
			IType leftType = left.getGamlType();
			IType eachType = leftType.isContainer() ? leftType.getContentType() : leftType;
			iteratorContexts.push(new EachExpression(argName, eachType));
		}

		try {
			// Handle n-ary operators
			if (rightMember instanceof ExpressionList el) {
				final List<Expression> list = EGaml.getInstance().getExprsOf(el);
				final int size = list.size();
				if (size > 1) {
					final IExpression[] compiledArgs = new IExpression[size + 1];
					compiledArgs[0] = left;
					for (int i = 0; i < size; i++) { compiledArgs[i + 1] = compile(list.get(i)); }
					return getFactory().createOperator(op, getContext(), rightMember, compiledArgs);
				}
			}

			// Compile the right-hand expression
			final IExpression right = compile(rightMember);

			// Create iterator operator or regular binary operator
			if (isIterator) {
				IExpression eachName = getFactory().createConst(argName, Types.STRING);
				return getFactory().createOperator(op, getContext(), originalExpression.eContainer(), eachName, left,
						right);
			}

			return getFactory().createOperator(op, getContext(), originalExpression.eContainer(), left, right);

		} finally {
			// Clean up iterator context
			if (isIterator) { iteratorContexts.pop(); }
		}
	}

	/**
	 * Creates an action call expression with the specified parameters.
	 *
	 * @param name
	 *            the name of the action
	 * @param callee
	 *            the object on which the action is called
	 * @param args
	 *            the argument list for the action
	 * @param action
	 *            the action description for validation
	 * @return the compiled action call expression
	 */
	private IExpression action(final String name, final IExpression callee, final ExpressionList args,
			final IActionDescription action) {
		final Arguments arguments = parseArguments(action, args, getContext(), true);
		return getFactory().createAction(name, getContext(), action, callee, arguments);
	}

	/**
	 * Handles compilation of binary operator expressions. Delegates to the binary method with extracted operator and
	 * operands.
	 *
	 * @param object
	 *            the binary operator AST node
	 * @return the compiled binary operator expression
	 */
	@Override
	public IExpression caseBinaryOperator(final BinaryOperator object) {
		return binary(object.getOp(), object.getLeft(), object.getRight());
	}

	/**
	 * Compiles a binary expression with special handling for specific operators. This method handles special operators
	 * like 'of', 'as', and 'is' that require special compilation logic different from regular binary operators.
	 *
	 * @param op
	 *            the binary operator ('of', 'as', 'is', or others)
	 * @param e1
	 *            the left operand expression
	 * @param right
	 *            the right operand expression
	 * @return the compiled binary expression, or null if compilation fails
	 */
	private IExpression binary(final String op, final Expression e1, final Expression right) {
		return switch (op) {
			// if the expression is " var of agents ", we must compile it apart
			case OF -> compileFieldExpr(right, e1);
			// if the operator is "as", the right-hand expression should be a
			// casting type
			case AS -> {
				final String type = EGaml.getInstance().getKeyOf(right);
				IType t = getType(type);
				if (t != null) { yield casting(t, compile(e1), right); }
				getContext().error(
						"'as' must be followed by a type, species or skill name. " + type + " is neither of these.",
						IGamlIssue.NOT_A_TYPE, right, type);
				yield null;
			}
			// if the operator is "is", the right-hand expression should be a type
			case IS -> {
				final IExpression left = compile(e1);
				final String type = EGaml.getInstance().getKeyOf(right);
				if (isTypeName(type)) {
					yield getFactory().createOperator(IS, getContext(), right.eContainer(), left,
							getFactory().createConst(type, Types.STRING));
				}
				if (isSkillName(type)) {
					yield getFactory().createOperator(IS_SKILL, getContext(), right.eContainer(), left,
							getFactory().createConst(type, Types.SKILL));
				}
				getContext().error(
						"'is' must be followed by a type, species or skill name. " + type + " is neither of these.",
						IGamlIssue.NOT_A_TYPE, right, type);
				yield null;
			}
			default -> binary(op, compile(e1), right);
		};
	}

	/**
	 * Retrieves the species description for the given species name from the current context. This method looks up
	 * species definitions in the current model context.
	 *
	 * @param e
	 *            the name of the species to look up
	 * @return the species description, or null if not found
	 */
	private ISpeciesDescription getSpeciesContext(final String e) {
		return getContext().getSpeciesDescription(e);
	}

	/**
	 * Determines whether the given string represents a valid species name in the current model. This excludes
	 * experiment descriptions to ensure proper type distinction.
	 *
	 * @param s
	 *            the string to check
	 * @return true if the string is a valid species name (excluding experiments), false otherwise
	 */
	private boolean isSpeciesName(final String s) {
		final IModelDescription m = getContext().getModelDescription();
		if (m == null) // can occur when building the kernel
			return false;
		final ISpeciesDescription sd = m.getSpeciesDescription(s);
		return sd != null && !(sd instanceof IExperimentDescription);
	}

	/**
	 * Determines whether the given string represents a valid skill name. This method checks against the global skill
	 * registry.
	 *
	 * @param s
	 *            the string to check
	 * @return true if the string is a registered skill name, false otherwise
	 */
	private boolean isSkillName(final String s) {
		return GamaSkillRegistry.INSTANCE.hasSkill(s);
	}

	/**
	 * Determines whether the given string represents a valid type name in the current context. This method excludes
	 * experiment types to ensure proper type validation.
	 *
	 * @param s
	 *            the string to check
	 * @return true if the string is a valid type name (excluding experiment types), false otherwise
	 */
	private boolean isTypeName(final String s) {
		final IType t = currentTypesManager.get(s, null);
		if (t == null) return false;
		final ISpeciesDescription sd = t.getSpecies();
		if (sd != null && sd.isExperiment()) return false;
		return true;
	}

	/**
	 * Retrieves the type corresponding to the given type name from the current types manager. This method excludes
	 * experiment types to ensure proper type resolution.
	 *
	 * @param s
	 *            the type name to resolve
	 * @return the resolved type, or null if not found or if it's an experiment type
	 */
	private IType getType(final String s) {
		final IType t = currentTypesManager.get(s, null);
		if (t == null) return null;
		final ISpeciesDescription sd = t.getSpecies();
		if (sd != null && sd.isExperiment()) return null;
		return t;
	}

	/**
	 * Compiles field access expressions for named experiments within model contexts. This method handles the special
	 * case where experiments are accessed by their plain names within model species expressions.
	 *
	 * @param leftExpr
	 *            the left-hand expression (typically a model reference)
	 * @param name
	 *            the name of the experiment to access
	 * @return the compiled experiment field expression, or null if invalid
	 */
	private IExpression compileNamedExperimentFieldExpr(final Expression leftExpr, final String name) {
		final IExpression owner = compile(leftExpr);
		if (owner == null) return null;
		final IType type = owner.getGamlType();
		if (type.isParametricFormOf(Types.SPECIES)) {
			final ISpeciesDescription sd = type.getContentType().getSpecies();
			if (sd instanceof IModelDescription md && md.hasExperiment(name))
				return getFactory().createConst(name, GamaType.from(md.getExperiment(name)));
		}
		getContext().error("Only experiments can be accessed using their plain name", IGamlIssue.UNKNOWN_FIELD);
		return null;
	}

	/**
	 * Compiles field access expressions (e.g., agent.field, object.method) handling both simple type fields and agent
	 * variable/action access. This method supports:
	 * <ul>
	 * <li>Type field access for simple objects (e.g., point.x, color.red)</li>
	 * <li>Agent variable access (e.g., agent.speed, agent.location)</li>
	 * <li>Agent action calls (e.g., agent.move(), agent.die())</li>
	 * <li>Special cases for matrix dot product and experiment access</li>
	 * </ul>
	 *
	 * @param leftExpr
	 *            the expression representing the owner object/agent
	 * @param fieldExpr
	 *            the expression representing the field or method being accessed
	 * @return the compiled field access expression, or null if compilation fails
	 */
	private IExpression compileFieldExpr(final Expression leftExpr, final Expression fieldExpr) {
		// If the owner cannot be determined (or leads to a previous error) we quit
		final IExpression owner = compile(leftExpr);
		if (owner == null) return null;
		// We gather the name of the "field"
		final String var = EGaml.getInstance().getKeyOf(fieldExpr);
		final IType type = owner.getGamlType();
		// hqnghi 28-05-14 search input variable from model, not experiment
		if (type instanceof ParametricType pt && pt.getGamlType().id() == IType.SPECIES
				&& pt.getContentType().getSpecies() instanceof ModelDescription md && md.hasExperiment(var))
			return getFactory().createConst(var, GamaType.from(md.getExperiment(var)));
		// end-hqnghi
		// If the owner has no species...
		final ITypeDescription species = type.getSpecies();
		if (species == null) {
			// It can only be a field as 'actions' are not defined on simple objects, except for matrices, where it can
			// also represent the dot product
			final IArtefactProto proto = type.getGetter(var);
			if (proto == null) {
				// Special case for matrices
				if (type.id() == IType.MATRIX)

					return binary(".", owner, fieldExpr);

				getContext().error("Unknown field '" + var + "' for type " + type, IGamlIssue.UNKNOWN_FIELD, leftExpr,
						var, type.toString());
				return null;
			}
			final TypeFieldExpression expr =
					(TypeFieldExpression) getFactory().createOperator(proto, getContext(), fieldExpr, owner);
			if (getContext() != null) { getContext().document(fieldExpr, expr); }
			return expr;
		}
		// We are now dealing with an agent. In that case, it can be either an attribute or an action call
		if (fieldExpr instanceof VariableRef) {
			IExpression expr = species.getVarExpr(var, true);
			if (expr == null) {
				if (species instanceof ModelDescription md && md.hasExperiment(var)) {
					expr = getFactory()
							.createTypeExpression(GamaType.from(Types.SPECIES, Types.INT, md.getTypeNamed(var)));
				} else if (species instanceof PlatformSpeciesDescription psd && GAMA.isInHeadLessMode())
					// Special case (see #2259 for headless validation of GUI preferences)
					return psd.getFakePrefExpression(var);
				else {
					getContext().error(
							"Unknown variable '" + var + "' in " + species.getKeyword() + " " + species.getName(),
							IGamlIssue.UNKNOWN_VAR, fieldExpr.eContainer(), var, species.getName());
					return null;
				}
				// special case for #3621. We cast the "simulation" and "simulations" variables of "experiment"
				// A more correct fix would have been to make `experiment` a parametric type that explicitly refers to
				// the species of the model as its contents type though...
			} else if (IKeyword.SIMULATION.equals(var) && expr.getGamlType().equals(Types.get(IKeyword.MODEL))) {
				IModelDescription md = getContext().getModelDescription();
				if (md != null) { expr = getFactory().createAs(currentContext, expr, md.getGamlType()); }
			} else if (IKeyword.SIMULATIONS.equals(var)
					&& expr.getGamlType().getContentType().equals(Types.get(IKeyword.MODEL))) {
				IModelDescription md = getContext().getModelDescription();
				if (md != null) { expr = getFactory().createAs(currentContext, expr, Types.LIST.of(md.getGamlType())); }
			}
			getContext().document(fieldExpr, expr);
			return getFactory().createOperator(_DOT, getContext(), fieldExpr, owner, expr);
		}
		if (fieldExpr instanceof Function) {
			final IActionDescription action = species.getAction(var);
			if (action != null) {
				final ExpressionList list = ((Function) fieldExpr).getRight();
				final IExpression call = action(var, owner, list, action);
				getContext().document(fieldExpr, call); // ??
				return call;
			}
		}
		return null;

	}

	// KEEP
	// private IExpression getWorldExpr() {
	// // if (world == null) {
	// final IType tt = getContext().getModelDescription()
	// ./* getWorldSpecies(). */getType();
	// final IExpression world = getFactory().createVar(WORLD_AGENT_NAME, tt,
	// true, IVarExpression.WORLD,
	// getContext().getModelDescription());
	// // }
	// return world;
	// }

	/**
	 * Sets the compilation context and updates the types manager accordingly. This method manages the compilation scope
	 * by setting the current description context and ensuring the appropriate types manager is used for type
	 * resolution.
	 *
	 * @param context
	 *            the new compilation context, or null to use the global GAML context
	 * @return the previous context for restoration purposes
	 */
	private IDescription setContext(final IDescription context) {
		final IDescription previous = currentContext;
		currentContext = context == null ? GAML.getModelContext() : context;
		currentTypesManager = Types.builtInTypes;
		if (currentContext != null) {
			final IModelDescription md = currentContext.getModelDescription();
			if (md != null) {
				final ITypesManager tm = md.getTypesManager();
				if (tm != null) { currentTypesManager = tm; }
			}
		}
		return previous;
	}

	/**
	 * Retrieves the current compilation context.
	 *
	 * @return the current description context used for compilation and validation
	 */
	private IDescription getContext() { return currentContext; }

	/**
	 * Retrieves the validation context from the current compilation context. The validation context is used for error
	 * reporting and validation during compilation.
	 *
	 * @return the current validation context, or null if no context is available
	 */
	private IValidationContext getValidationContext() {
		if (currentContext == null) return null;
		return currentContext.getValidationContext();
	}

	/**
	 * Parses and validates arguments for action calls, supporting multiple argument formats. This method handles
	 * various argument syntaxes:
	 * <ul>
	 * <li>Named arguments: [a1::v1, a2::v2] or (a1:v1, a2:v2)</li>
	 * <li>Positional arguments: (v1, v2)</li>
	 * <li>Mixed formats with parameter binding</li>
	 * </ul>
	 *
	 * @param action
	 *            the action description containing expected argument information
	 * @param o
	 *            the EObject containing the argument expressions (Array or ExpressionList)
	 * @param command
	 *            the description context for error reporting
	 * @param compileArgValues
	 *            whether to compile argument values during parsing
	 * @return the parsed arguments map, or null if parsing fails
	 */
	@Override
	public Arguments parseArguments(final IActionDescription action, final EObject o, final IDescription command,
			final boolean compileArgValues) {
		if (o == null) return null;
		boolean completeArgs = false;
		List<Expression> parameters = null;
		EGaml egaml = EGaml.getInstance();
		if (o instanceof Array array) {
			parameters = egaml.getExprsOf(array.getExprs());
		} else if (o instanceof ExpressionList) {
			parameters = egaml.getExprsOf(o);
			completeArgs = true;
		} else {
			command.error("Arguments must be written [a1::v1, a2::v2], (a1:v1, a2:v2) or (v1, v2)");
			return null;
		}
		final Arguments argMap = new Arguments();
		int index = 0;

		for (final Expression exp : parameters) {
			String arg = null;
			IExpressionDescription ed = null;
			IExpressionDescriptionFactory builder = GAML.getExpressionDescriptionFactory();
			if (exp instanceof ArgumentPair p) {
				arg = egaml.getKeyOfArgumentPair(p);
				ed = builder.createFromEObject(p.getRight());
			} else if (exp instanceof Parameter p) {
				arg = egaml.getKeyOfParameter(p);
				ed = builder.createFromEObject(p.getRight());
			} else if (exp instanceof BinaryOperator bo && "::".equals(bo.getOp())) {
				arg = egaml.getKeyOf(bo.getLeft());
				ed = builder.createFromEObject(bo.getRight());
			} else if (completeArgs) {
				final List<String> args = action == null ? null : action.getArgNames();
				if (args != null && action != null && index == args.size()) {
					command.error("Wrong number of arguments. Action " + action.getName() + " expects " + args);
					return argMap;
				}
				arg = args == null ? String.valueOf(index++) : args.get(index++);
				ed = builder.createFromEObject(exp);
			}
			if (ed != null && compileArgValues) { ed.compile(command); }
			argMap.put(arg, ed);
		}
		return argMap;
	}

	/**
	 * Handles compilation of skill reference expressions. Converts a skill reference AST node into a skill constant
	 * expression.
	 *
	 * @param object
	 *            the skill reference AST node
	 * @return the compiled skill expression
	 */
	@Override
	public IExpression caseSkillRef(final SkillRef object) {
		return skill(EGaml.getInstance().getKeyOf(object));
	}

	/**
	 * Handles compilation of action reference expressions. This method resolves action names to action descriptions,
	 * checking both the current species and enclosing contexts. It validates that the action exists and has no required
	 * arguments (for direct action references without parentheses).
	 *
	 * @param object
	 *            the action reference AST node
	 * @return the compiled action reference expression, or null if the action is not found or invalid
	 */
	@Override
	public IExpression caseActionRef(final ActionRef object) {
		final String s = EGaml.getInstance().getKeyOf(object);
		final ISpeciesDescription sd = getContext().getSpeciesContext();
		// Look in the species and its ancestors
		IActionDescription ad = sd.getAction(s);
		// If it is not found, maybe it is in the host ?
		if (ad == null) {
			boolean isExp = sd instanceof IExperimentDescription;
			// If we are in an experiment, we cannot call an action defined in the model (see #
			if (!isExp) {
				IDescription host = sd.getEnclosingDescription();
				if (host != null) { ad = host.getAction(s); }
			}

			if (ad == null) {
				if (isExp) {
					getContext().error("The action " + s + " must be defined in the experiment",
							IGamlIssue.UNKNOWN_ACTION, object);
				} else {
					getContext().error("The action " + s + " is unknown", IGamlIssue.UNKNOWN_ACTION, object);
				}
				return null;
			}
		}
		if (ad.getArgNames().size() > 0) {
			getContext().error("Impossible to call an action that requires arguments", IGamlIssue.UNKNOWN_ARGUMENT,
					object);
			return null;
		}
		return new DenotedActionExpression(ad);
	}

	/**
	 * Handles compilation of variable reference expressions. Resolves variable names and delegates to appropriate case
	 * handling methods.
	 *
	 * @param object
	 *            the variable reference AST node
	 * @return the compiled variable expression
	 */
	@Override
	public IExpression caseVariableRef(final VariableRef object) {
		final String s = EGaml.getInstance().getNameOfRef(object);
		if (s == null) return caseVarDefinition(object.getRef());
		return caseVar(s, object);
	}

	/**
	 * Handles compilation of type reference expressions. Converts type references to either species expressions (for
	 * agent types) or type expressions.
	 *
	 * @param object
	 *            the type reference AST node
	 * @return the compiled type expression, or null if the type is invalid
	 */
	@Override
	public IExpression caseTypeRef(final TypeRef object) {
		final IType t = fromTypeRef(object);
		if (t == null) return null;
		if (t.isAgentType()) return t.getSpecies().getSpeciesExpr();
		return getFactory().createTypeExpression(t);
	}

	/**
	 * Handles compilation of equation reference expressions. Converts equation references to string constant
	 * expressions containing the equation name.
	 *
	 * @param object
	 *            the equation reference AST node
	 * @return the compiled string constant expression containing the equation name
	 */
	@Override
	public IExpression caseEquationRef(final EquationRef object) {
		return getFactory().createConst(EGaml.getInstance().getNameOfRef(object), Types.STRING);
	}

	/**
	 * Handles compilation of unit name expressions from AST nodes. Validates unit names and handles deprecation
	 * warnings.
	 *
	 * @param object
	 *            the unit name AST node
	 * @return the compiled unit expression, or null if the unit name is invalid
	 */
	@Override
	public IExpression caseUnitName(final UnitName object) {
		final String s = EGaml.getInstance().getNameOfRef(object);
		IExpression exp = caseUnitName(s);
		if (exp == null) {
			getContext().error(s + " is not a unit or constant name.", IGamlIssue.NOT_A_UNIT, object, (String[]) null);
			return null;
		}
		if (exp.isDeprecated()) {
			getContext().warning(s + " is deprecated.", IGamlIssue.DEPRECATED, object, (String[]) null);
		}
		return exp;
	}

	/**
	 * Resolves a unit name string to its corresponding unit expression. This method looks up unit names in the GAML
	 * units registry and returns special expressions for temporal units like #month or #year.
	 *
	 * @param name
	 *            the unit name to resolve
	 * @return the unit expression if found, null otherwise
	 */
	public IExpression caseUnitName(final String name) {
		// Make sure we return "special" expressions like #month or #year -- see #3590
		return GAML.UNITS.get(name);
	}

	/**
	 * Handles compilation of variable definition expressions. Treats variable definitions as skill references.
	 *
	 * @param object
	 *            the variable definition AST node
	 * @return the compiled skill expression
	 */
	@Override
	public IExpression caseVarDefinition(final VarDefinition object) {
		return skill(object.getName());
	}

	/**
	 * Handles compilation of type definition expressions. Delegates to variable case handling for type definition
	 * names.
	 *
	 * @param object
	 *            the type definition AST node
	 * @return the compiled expression for the type definition
	 */
	@Override
	public IExpression caseTypeDefinition(final TypeDefinition object) {
		return caseVar(object.getName(), object);
	}

	/**
	 * Handles compilation of skill fake definition expressions. Delegates to variable case handling for skill fake
	 * definition names.
	 *
	 * @param object
	 *            the skill fake definition AST node
	 * @return the compiled expression for the skill fake definition
	 */
	@Override
	public IExpression caseSkillFakeDefinition(final SkillFakeDefinition object) {
		return caseVar(object.getName(), object);
	}

	/**
	 * Handles compilation of reserved literal expressions (e.g., true, false, null). Extracts the literal value and
	 * delegates to variable case handling.
	 *
	 * @param object
	 *            the reserved literal AST node
	 * @return the compiled expression for the reserved literal
	 */
	@Override
	public IExpression caseReservedLiteral(final ReservedLiteral object) {
		return caseVar(EGaml.getInstance().getKeyOf(object), object);
	}

	/**
	 * Handles compilation of conditional (if-then-else) expressions. Converts if expressions into ternary operators
	 * using '?' and ':' operators.
	 *
	 * @param object
	 *            the if expression AST node
	 * @return the compiled ternary conditional expression
	 */
	@Override
	public IExpression caseIf(final If object) {
		final IExpression ifFalse = compile(object.getIfFalse());
		final IExpression alt =
				getFactory().createOperator(":", getContext(), object, compile(object.getRight()), ifFalse);
		return getFactory().createOperator("?", getContext(), object, compile(object.getLeft()), alt);
	}

	/**
	 * Handles compilation of argument pair expressions (name::value syntax). Creates a binary operation using the '::'
	 * operator for named arguments.
	 *
	 * @param object
	 *            the argument pair AST node
	 * @return the compiled argument pair expression
	 */
	@Override
	public IExpression caseArgumentPair(final ArgumentPair object) {
		return binary("::", caseVar(EGaml.getInstance().getKeyOf(object), object), object.getRight());
	}

	/**
	 * Handles compilation of unit expressions (value with unit, e.g., 5#m, 10#s). Converts unit expressions into
	 * multiplication operations where the unit is converted to its numeric value.
	 *
	 * @param object
	 *            the unit expression AST node
	 * @return the compiled multiplication expression
	 */
	@Override
	public IExpression caseUnit(final Unit object) {
		// We simply return a multiplication, since the right member (the
		// "unit") will be translated into its float value
		return binary("*", object.getLeft(), object.getRight());
	}

	/**
	 * Handles compilation of unary expressions (e.g., -x, !condition, #red). Delegates to the unary method for proper
	 * operator handling.
	 *
	 * @param object
	 *            the unary expression AST node
	 * @return the compiled unary expression
	 */
	@Override
	public IExpression caseUnary(final Unary object) {
		return unary(EGaml.getInstance().getKeyOf(object), object.getRight());
	}

	/**
	 * Handles compilation of dot access expressions (e.g., agent.field, object.method). Distinguishes between named
	 * experiment field access and regular field access.
	 *
	 * @param object
	 *            the access expression AST node with dot operator
	 * @return the compiled dot access expression, or null if compilation fails
	 */
	public IExpression caseDot(final Access object) {
		final Expression right = object.getRight();
		if (right instanceof StringLiteral sl) return compileNamedExperimentFieldExpr(object.getLeft(), sl.getOp());
		if (right != null) return compileFieldExpr(object.getLeft(), right);
		return null;
	}

	/**
	 * Handles compilation of access expressions with various operators ([], at, etc.). This method supports:
	 * <ul>
	 * <li>Array/list indexing with type validation</li>
	 * <li>Matrix access with special 1 or 2 index support</li>
	 * <li>Special handling for integrated values in equations</li>
	 * <li>Range access using pairs for lists</li>
	 * </ul>
	 *
	 * @param object
	 *            the access expression AST node
	 * @return the compiled access expression using internal operators
	 */
	@Override
	public IExpression caseAccess(final Access object) {
		if (".".equals(object.getOp())) return caseDot(object);
		final IExpression container = compile(object.getLeft());
		// If no container is defined, return a null expression
		if (container == null) return null;
		final IType contType = container.getGamlType();
		final boolean isMatrix = Types.MATRIX.isAssignableFrom(contType);
		final IType keyType = contType.getKeyType();
		final List<? extends Expression> list = EGaml.getInstance().getExprsOf(object.getRight());
		try (final Collector.AsList<IExpression> result = Collector.getList()) {
			final int size = list.size();

			for (int i = 0; i < size; i++) {
				final Expression eExpr = list.get(i);
				final IExpression e = compile(eExpr);
				if (e != null) {
					final IType elementType = e.getGamlType();
					// See Issue #3099
					if (size == 1 && Types.PAIR.isAssignableFrom(elementType)
							&& Types.LIST.isAssignableFrom(contType)) {
						if (Types.INT == elementType.getKeyType() && Types.INT == elementType.getContentType())
							return getFactory().createOperator("internal_between", getContext(), object, container, e);
					}
					if (keyType != Types.NO_TYPE && !keyType.isAssignableFrom(elementType)
							&& (!isMatrix || elementType.id() != IType.INT)) {
						getContext().warning("a " + contType.toString() + " should not be accessed using a "
								+ elementType.toString() + " index", IGamlIssue.WRONG_TYPE, eExpr);
					}
					// if (!(isMatrix && elementType.id() == IType.INT && size > 1)) {
					//
					// }
					result.add(e);
				}
			}
			if (size > 2) {
				final String end = !isMatrix ? " only 1 index" : " 1 or 2 indices";
				getContext().warning("a " + contType.toString() + " should be accessed using" + end,
						IGamlIssue.DIFFERENT_ARGUMENTS, object);
			}

			final IExpression indices = getFactory().createList(result.items());

			IExpression varDiff = null;
			if (container instanceof IVarExpression.Agent && ((IVarExpression.Agent) container).getOwner() != null) {
				varDiff = ((IVarExpression.Agent) container).getVar();

				final ISpeciesDescription species =
						((IVarExpression.Agent) varDiff).getDefinitionDescription().getSpeciesContext();
				if (species != null) {
					final Iterable<IDescription> equations = species.getChildrenWithKeyword(IKeyword.EQUATION);
					for (final IDescription equation : equations) {
						if (equation.manipulatesVar(varDiff.getName()))
							return getFactory().createOperator("internal_integrated_value", getContext(), object,
									((IVarExpression.Agent) container).getOwner(), varDiff);

					}
				}
			}

			return getFactory().createOperator("internal_at", getContext(), object, container, indices);
		}
	}

	/**
	 * Handles compilation of array literal expressions [elem1, elem2, ...]. Automatically determines whether to create
	 * a list or map based on content:
	 * <ul>
	 * <li>Creates a map if all elements are argument pairs (key::value)</li>
	 * <li>Creates a list otherwise</li>
	 * </ul>
	 *
	 * @param object
	 *            the array literal AST node
	 * @return the compiled list or map expression, or null if any element fails to compile
	 */
	@Override
	public IExpression caseArray(final Array object) {
		final List<? extends Expression> list = EGaml.getInstance().getExprsOf(object.getExprs());
		// Awkward expression, but necessary to fix Issue #2612
		final boolean allPairs = !list.isEmpty() && Iterables.all(list,
				each -> each instanceof ArgumentPair || "::".equals(EGaml.getInstance().getKeyOf(each)));
		final Iterable<IExpression> result = Iterables.transform(list, this::compile);
		if (Iterables.any(result, t -> t == null)) return null;
		return allPairs ? getFactory().createMap(result) : getFactory().createList(result);
	}

	/**
	 * Handles compilation of point literal expressions {x, y} or {x, y, z}. Supports both 2D and 3D point creation
	 * using the POINT operator.
	 *
	 * @param object
	 *            the point literal AST node
	 * @return the compiled point expression, or null if any coordinate fails to compile
	 */
	@Override
	public IExpression casePoint(final Point object) {
		final Expression z = object.getZ();
		if (z == null) return binary(POINT, object.getLeft(), object.getRight());
		final IExpression[] exprs = new IExpression[3];
		exprs[0] = compile(object.getLeft());
		exprs[1] = compile(object.getRight());
		exprs[2] = compile(z);
		if (exprs[0] == null || exprs[1] == null || exprs[2] == null) return null;
		return getFactory().createOperator(POINT, getContext(), object, exprs);
	}

	/**
	 * Handles compilation of expression lists. Only the first expression is compiled, with warnings emitted for
	 * multiple expressions since sequences are not expected in most contexts.
	 *
	 * @param object
	 *            the expression list AST node
	 * @return the compiled first expression, or null if the list is empty
	 */
	@Override
	public IExpression caseExpressionList(final ExpressionList object) {
		final List<Expression> list = EGaml.getInstance().getExprsOf(object);
		if (list.isEmpty()) return null;
		if (list.size() > 1) {
			getContext().warning(
					"A sequence of expressions is not expected here. Only the first expression will be evaluated",
					IGamlIssue.UNKNOWN_ARGUMENT, object);
		}
		return compile(list.get(0));
	}

	/**
	 * Handles compilation of function call expressions. This method tries multiple resolution strategies in order:
	 * <ol>
	 * <li>Type casting functions (if operator name matches a type)</li>
	 * <li>Action calls (if operator name matches an action in current context)</li>
	 * <li>Regular operator expressions (unary, binary, or n-ary)</li>
	 * </ol>
	 *
	 * @param object
	 *            the function call AST node
	 * @return the compiled function expression, or null if no valid interpretation is found
	 */
	@Override
	public IExpression caseFunction(final Function object) {
		if (object == null) return null;
		final String op = EGaml.getInstance().getKeyOf(object.getLeft());
		IExpression result = tryCastingFunction(op, object);
		if (result != null) return result;
		result = tryActionCall(op, object);
		if (result != null) return result;
		final List<Expression> args = EGaml.getInstance().getExprsOf(object.getRight());
		return switch (args.size()) {
			case 0 -> {
				getContext().error("Unknown operator or action: " + op, IGamlIssue.UNKNOWN_ACTION, object);
				yield null;
			}
			case 1 -> unary(op, args.get(0));
			case 2 -> binary(op, args.get(0), args.get(1));
			default -> getFactory().createOperator(op, getContext(), object,
					toArray(transform(args, this::compile), IExpression.class));
		};
	}

	/**
	 * Attempts to interpret a function call as a type casting operation. This method checks if the function name
	 * corresponds to a type name and, if so, treats the function call as a casting operation rather than a function
	 * call. It handles precedence rules where explicit operators override casting.
	 *
	 * @param op
	 *            the function/operator name to check
	 * @param object
	 *            the function AST node for error reporting
	 * @return the casting expression if successful, null if not a casting operation
	 */
	private IExpression tryCastingFunction(final String op, final Function object) {
		// If the operator is not a type name, no match
		IType t = getType(op);
		if (t == null) {
			// We nevertheless emit a warning if the operator name contains parametric type information
			if (object.getType() != null) {
				getContext().warning(
						op + " is not a type name: parameter types are not expected and will not be evaluated",
						IGamlIssue.UNKNOWN_ARGUMENT, object.getType());
			}
			return null;
		}
		final List<Expression> args = EGaml.getInstance().getExprsOf(object.getRight());
		return switch (args.size()) {
			case 0 -> null;
			case 1 -> {
				IExpression expr = compile(args.get(0));
				// If a unary function has been redefined with the type name as name and this specific argument,
				// it takes precedence over the regular casting
				if (getFactory().hasExactOperator(op, expr)) { yield null; }
				yield casting(t, expr, object);
			}
			default -> {
				Iterable<IExpression> exprs = transform(args, this::compile);
				// If more than one argument, we need to check if there are operators that match. If yes, we return null
				if (getFactory().hasOperator(op, new Signature(toArray(exprs, IExpression.class)))) { yield null; }
				yield casting(t, getFactory().createList(exprs), object);
			}
		};

	}

	/**
	 * Attempts to interpret a function call as an action call on the current agent. This method looks for actions in
	 * the current species context and handles super calls for action inheritance. It also handles special cases for
	 * experiment contexts and model actions.
	 *
	 * @param op
	 *            the function/action name to resolve
	 * @param object
	 *            the function AST node for context
	 * @return the action call expression if successful, null if no action is found
	 */
	private IExpression tryActionCall(final String op, final Function object) {
		ISpeciesDescription species = getContext().getSpeciesContext();
		if (species == null) return null;
		final boolean isSuper = getContext() instanceof DoDescription st && st.isSuperInvocation();
		IActionDescription action = isSuper ? species.getParent().getAction(op) : species.getAction(op);
		if (action == null) {
			// Not found: see #3530
			if (species instanceof ExperimentDescription && getContext().isIn(IKeyword.OUTPUT)) {
				species = species.getModelDescription();
			}
			action = isSuper ? species.getParent().getAction(op) : species.getAction(op);
		}
		if (action == null) return null;
		final ExpressionList params = object.getRight();
		return action(op, caseVar(isSuper ? SUPER : SELF, object), params, action);
	}

	/**
	 * Handles compilation of integer literal expressions. Parses decimal integer values and creates integer constant
	 * expressions.
	 *
	 * @param object
	 *            the integer literal AST node
	 * @return the compiled integer constant expression, or null if malformed
	 */
	@Override
	public IExpression caseIntLiteral(final IntLiteral object) {
		try {
			final Integer val = Integer.parseInt(EGaml.getInstance().getKeyOf(object), 10);
			return getFactory().createConst(val, Types.INT);
		} catch (final NumberFormatException e) {
			getContext().error("Malformed integer: " + EGaml.getInstance().getKeyOf(object), IGamlIssue.UNKNOWN_NUMBER,
					object);
			return null;
		}
	}

	/**
	 * Handles compilation of floating-point literal expressions. Supports standard decimal notation and scientific
	 * notation with robust parsing. Uses fallback parsing for complex number formats.
	 *
	 * @param object
	 *            the double literal AST node
	 * @return the compiled float constant expression, or null if malformed
	 */
	@Override
	public IExpression caseDoubleLiteral(final DoubleLiteral object) {

		String s = EGaml.getInstance().getKeyOf(object);

		if (s == null) return null;
		try {
			return getFactory().createConst(Double.parseDouble(s), Types.FLOAT);
		} catch (final NumberFormatException e) {
			try {
				final NumberFormat nf = NumberFormat.getInstance(Locale.US);
				// More robust, but slower parsing used in case
				// Double.parseDouble() cannot handle it
				// See Issue 1025. Exponent notation is capitalized, and '+' is
				// removed beforehand
				s = s.replace('e', 'E').replace("+", "");
				return getFactory().createConst(nf.parse(s).doubleValue(), Types.FLOAT);
			} catch (final ParseException ex) {
				getContext().error("Malformed float: " + s, IGamlIssue.UNKNOWN_NUMBER, object);
				return null;
			}
		}

	}

	/**
	 * Handles compilation of string literal expressions. Creates string constant expressions from quoted string
	 * literals.
	 *
	 * @param object
	 *            the string literal AST node
	 * @return the compiled string constant expression
	 */
	@Override
	public IExpression caseStringLiteral(final StringLiteral object) {
		return getFactory().createConst(EGaml.getInstance().getKeyOf(object), Types.STRING);
	}

	/**
	 * Handles compilation of boolean literal expressions (true/false). Creates boolean constant expressions with
	 * case-insensitive parsing.
	 *
	 * @param object
	 *            the boolean literal AST node
	 * @return the compiled boolean constant expression, or null if the value is null
	 */
	@Override
	public IExpression caseBooleanLiteral(final BooleanLiteral object) {
		final String s = EGaml.getInstance().getKeyOf(object);
		if (s == null) return null;
		return TRUE.equalsIgnoreCase(s) ? getFactory().getTrue() : getFactory().getFalse();
	}

	/**
	 * Default case handler for unrecognized AST nodes. Emits compilation errors for nodes that cannot be handled by
	 * other case methods.
	 *
	 * @param object
	 *            the unrecognized AST node
	 * @return null (compilation failure)
	 */
	@Override
	public IExpression defaultCase(final EObject object) {
		final IValidationContext vc = getValidationContext();
		if (vc != null && !vc.hasErrors()) {
			// In order to avoid too many "useless errors"
			getContext().error("Cannot compile: " + object, IGamlIssue.GENERAL, object);
		}
		return null;
	}

	/**
	 * Handles compilation of variable references by name, supporting various special variables and resolving variables
	 * in the current context. This method handles:
	 * <ul>
	 * <li>Special variables: experiment, gama, each, null, self, super</li>
	 * <li>Iterator context variables</li>
	 * <li>Variables declared in the current species context</li>
	 * <li>Species names as type expressions</li>
	 * <li>Type names as type expressions</li>
	 * <li>Skill names as skill constants</li>
	 * <li>Action, behavior, and aspect references</li>
	 * <li>Event layer names (with deprecation warnings)</li>
	 * </ul>
	 *
	 * @param varName
	 *            the name of the variable to resolve
	 * @param object
	 *            the AST node for error reporting
	 * @return the compiled variable expression, or null if not found
	 */
	private IExpression caseVar(final String varName, final EObject object) {
		if (varName == null) {
			getContext().error("Unknown variable", IGamlIssue.UNKNOWN_VAR, object);
			return null;
		}
		switch (varName) {
			case EXPERIMENT:
				return CurrentExperimentExpression.create();
			case IKeyword.GAMA:
				return GAMA.getPlatformAgent();
			case EACH:
				return getEachExpr(object);
			case NULL:
				return GAML.getExpressionFactory().getNil();
			case SELF:
				return returnSelfOrSuper(SELF, object, false);
			case SUPER:
				return returnSelfOrSuper(SUPER, object, true);
		}

		// check if the var has been declared in an iterator context
		for (final IVarExpression it : iteratorContexts) { if (it.getName().equals(varName)) return it; }
		IDescription context = getContext();
		final IVarDescriptionProvider temp_sd = context == null ? null : context.getDescriptionDeclaringVar(varName);

		if (temp_sd != null) {
			if (!(temp_sd instanceof ISpeciesDescription)) return temp_sd.getVarExpr(varName, false);
			final ISpeciesDescription remote_sd = getContext().getSpeciesContext();
			if (remote_sd != null) {
				final ISpeciesDescription found_sd = (ISpeciesDescription) temp_sd;
				if (remote_sd != temp_sd && !remote_sd.isBuiltIn() && !remote_sd.hasMacroSpecies(found_sd)) {
					getContext().error("The variable " + varName + " is not accessible in this context ("
							+ remote_sd.getName() + "), but in the context of " + found_sd.getName()
							+ ". It should be preceded by 'myself.'", IGamlIssue.UNKNOWN_VAR, object, varName);
				}
			}
			// See Issue #3085. We give priority to the variables sporting species names unless they represent the
			// species within the agents
			if (!isSpeciesName(varName)) return temp_sd.getVarExpr(varName, false);
		}

		// See Issue #3085
		if (isSpeciesName(varName)) {
			final ISpeciesDescription sd = getSpeciesContext(varName);
			return sd == null ? null : sd.getSpeciesExpr();
		}
		IType t = getType(varName);
		if (t != null) return getFactory().createTypeExpression(t);
		if (isSkillName(varName)) return skill(varName);
		if (context != null) {

			// An experimental possibility is that the variable refers to a
			// an action (used like a variable, see Issue 853) or also any
			// behavior or aspect
			final ISpeciesDescription sd = context.getSpeciesContext();
			if (sd.hasAction(varName, false)) return new DenotedActionExpression(sd.getAction(varName));
			if (sd.hasBehavior(varName)) return new DenotedActionExpression(sd.getBehavior(varName));
			if (sd.hasAspect(varName)) return new DenotedActionExpression(sd.getAspect(varName));

			// A last possibility is to offer some transition guidance to users who used to write event layer names as
			// labels (neither as string or constant). For INSTANCE : mouse_move instead of "mouse_move" or #mouse_move.
			// For that, we emit simply a warning (not an error) and we return the corresponding constant.

			if (IEventLayerDelegate.MOUSE_EVENTS.contains(varName)
					|| IEventLayerDelegate.KEYBOARD_EVENTS.contains(varName)) {
				IExpression exp = this.caseUnitName(varName);
				if (exp != null) {
					context.warning("The usage of the event name (" + varName
							+ ") is now deprecated and should be replaced either by a string ('" + varName
							+ "') or a constant (#" + varName + ")", IGamlIssue.UNKNOWN_VAR, object, varName);
					return exp;
				}
			}
			getContext().error(varName + " is not defined or accessible in this context. Check its name or declare it",
					IGamlIssue.UNKNOWN_VAR, object, varName);
		}
		return null;

	}

	/**
	 * Creates self or super expressions with appropriate typing based on the current species context. This method
	 * resolves the species hierarchy for super references and creates variable expressions with the correct type and
	 * variable kind.
	 *
	 * @param name
	 *            the variable name ("self" or "super")
	 * @param object
	 *            the AST node for error reporting
	 * @param isSuper
	 *            true if creating a super expression, false for self
	 * @return the compiled self or super expression, or null if species context cannot be determined
	 */
	private IExpression returnSelfOrSuper(final String name, final EObject object, final boolean isSuper) {
		final ISpeciesDescription sd = getContext().getSpeciesContext();
		if (sd == null) {
			getContext().error("Unable to determine the species of " + name, IGamlIssue.GENERAL, object);
			return null;
		}
		IType type = isSuper ? sd.getParent().getGamlType() : sd.getGamlType();
		return getFactory().createVar(name, type, true, isSuper ? IVarExpression.SUPER : IVarExpression.SELF, null);
	}

	/**
	 * Parses a string expression into an EObject by creating a temporary GAML resource. This method wraps the string in
	 * a dummy assignment statement to leverage the GAML parser for creating AST nodes from string expressions.
	 *
	 * @param string
	 *            the string expression to parse
	 * @param tempContext
	 *            the execution context for error reporting
	 * @return the parsed EObject, or null if parsing fails
	 * @throws GamaRuntimeException
	 *             if parsing errors occur
	 */
	private EObject getEObjectOf(final String string, final IExecutionContext tempContext) throws GamaRuntimeException {
		EObject result = null;
		final String s = "dummy <- " + string;
		final GamlResource resource = GamlResourceServices.getTemporaryResource(getContext());
		try {
			final InputStream is = new ByteArrayInputStream(s.getBytes());
			try {
				resource.loadSynthetic(is, tempContext);
			} catch (final Exception e1) {
				e1.printStackTrace();
				return null;
			}

			if (resource.hasErrors()) {
				final Resource.Diagnostic d = resource.getErrors().get(0);
				throw GamaRuntimeException.error(d.getMessage(), tempContext.getScope());
			}
			final EObject e = resource.getContents().get(0);
			if (e instanceof StringEvaluator) { result = ((StringEvaluator) e).getExpr(); }

			return result;
		} finally {
			GamlResourceServices.discardTemporaryResource(resource);
		}
	}

	/**
	 * Compiles a block of GAML statements from a string into a list of description objects. This method creates a
	 * temporary synthetic action containing the statements and parses them into individual description objects for
	 * execution. It handles both statement blocks and single expressions.
	 *
	 * @param string
	 *            the string containing GAML statements to compile
	 * @param actionContext
	 *            the action context for compilation
	 * @param tempContext
	 *            the temporary execution context for error handling
	 * @return the list of compiled descriptions, or null if compilation fails
	 * @throws GamaRuntimeException
	 *             if parsing or compilation errors occur
	 */
	@Override
	public List<IDescription> compileBlock(final String string, final IDescription actionContext,
			final IExecutionContext tempContext) throws GamaRuntimeException {
		final String s = "__synthetic__ {" + string + "}";
		final GamlResource resource = GamlResourceServices.getTemporaryResource(getContext());
		try (final Collector.AsList<IDescription> result = Collector.getList()) {
			final InputStream is = new ByteArrayInputStream(s.getBytes());
			try {
				resource.loadSynthetic(is, tempContext);
			} catch (final Exception e1) {
				e1.printStackTrace();
				return null;
			} finally {}
			if (resource.hasErrors()) {
				final Resource.Diagnostic d = resource.getErrors().get(0);
				throw GamaRuntimeException.error(d.getMessage(), tempContext.getScope());
			}
			final SyntacticModelElement elt = (SyntacticModelElement) resource.getSyntacticContents();
			// We have a problem -- can be simply an empty block or an expression
			if (!elt.hasChildren() && elt.hasFacet(IKeyword.FUNCTION)) {
				// Compile the expression is the best way to know if this expression is correct
				elt.getExpressionAt(IKeyword.FUNCTION).compile(actionContext);
			}
			elt.visitChildren(e -> {
				final IDescription desc = GAML.getDescriptionFactory().create(e, actionContext, null);
				result.add(desc);
			});
			return result.items();
		} finally {
			GamlResourceServices.discardTemporaryResource(resource);
		}
	}

	//
	// end-hqnghi
	//

	/**
	 * Retrieves the current 'each' variable expression from the iterator context stack. This method is used within
	 * iterator operations to access the current iteration variable.
	 *
	 * @param object
	 *            the AST node for error reporting
	 * @return the current 'each' variable expression, or null if not in an iterator context
	 */
	public IVarExpression getEachExpr(final EObject object) {
		final IVarExpression p = iteratorContexts.peek();
		if (p == null) {
			getContext().error("'each' is not accessible in this context", IGamlIssue.UNKNOWN_VAR, object);
			return null;
		}
		return p;
	}

	/**
	 * Retrieves the currently active expression description being compiled. This is used to prevent reentrant parsing
	 * issues.
	 *
	 * @return the current expression description, or null if none is active
	 */
	private IExpressionDescription getCurrentExpressionDescription() { return currentExpressionDescription; }

	/**
	 * Sets the currently active expression description during compilation. This is used to track compilation state and
	 * prevent reentrant parsing.
	 *
	 * @param currentExpressionDescription
	 *            the expression description being compiled, or null to clear
	 */
	private void setCurrentExpressionDescription(final IExpressionDescription currentExpressionDescription) {
		this.currentExpressionDescription = currentExpressionDescription;
	}

	/**
	 * Retrieves the GAML expression factory used for creating expression objects.
	 *
	 * @return the current expression factory instance
	 */
	private IExpressionFactory getFactory() { return GAML.getExpressionFactory(); }

	/**
	 * Disposes of this compiler instance, cleaning up all resources and clearing caches. This method should be called
	 * when the compiler is no longer needed to free memory.
	 */
	@Override
	public void dispose() {
		// Clear instance-specific resources
		this.currentContext = null;
		this.currentTypesManager = null;
		this.currentExpressionDescription = null;
		this.iteratorContexts.clear();

		// Note: We don't clear the static cache here as it may be shared across compiler instances
		// The cache has its own automated eviction and will be cleaned up when appropriate
	}

	/**
	 * Clears the global expression cache. This should be called when memory usage needs to be minimized or when the
	 * GAML context has changed significantly.
	 */
	public static void clearExpressionCache() {
		constantSyntheticExpressions.invalidateAll();
	}

	/**
	 * Returns the current size of the expression cache.
	 *
	 * @return the number of cached expressions
	 */
	public static long getCacheSize() { return constantSyntheticExpressions.size(); }

	/**
	 * Returns the maximum allowed cache size.
	 *
	 * @return the maximum cache size
	 */
	public static long getMaxCacheSize() { return MAX_CACHE_SIZE; }

	/**
	 * Returns cache statistics including hit rate, miss rate, and eviction count. This provides insight into cache
	 * performance for monitoring and optimization.
	 *
	 * @return the cache statistics object with performance metrics
	 */
	public static com.google.common.cache.CacheStats getCacheStats() { return constantSyntheticExpressions.stats(); }

	/**
	 * Returns the cache expiration time in minutes.
	 *
	 * @return the number of minutes after which unused cache entries expire
	 */
	public static int getCacheExpireMinutes() { return CACHE_EXPIRE_MINUTES; }

}

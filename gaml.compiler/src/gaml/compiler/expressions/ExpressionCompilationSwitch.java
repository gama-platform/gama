/*******************************************************************************************************
 *
 * ExpressionCompilationSwitch.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;
import static gama.annotations.constants.IKeyword.AS;
import static gama.annotations.constants.IKeyword.EACH;
import static gama.annotations.constants.IKeyword.EXPERIMENT;
import static gama.annotations.constants.IKeyword.IS;
import static gama.annotations.constants.IKeyword.IS_SKILL;
import static gama.annotations.constants.IKeyword.NULL;
import static gama.annotations.constants.IKeyword.OF;
import static gama.annotations.constants.IKeyword.POINT;
import static gama.annotations.constants.IKeyword.SELF;
import static gama.annotations.constants.IKeyword.SPECIES;
import static gama.annotations.constants.IKeyword.SUPER;
import static gama.annotations.constants.IKeyword.TRUE;
import static gama.annotations.constants.IKeyword.UNKNOWN;
import static gama.annotations.constants.IKeyword._DOT;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.artefacts.IArtefact;
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
import gama.api.compilation.validation.IValidationContext;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ParametricType;
import gama.api.gaml.types.Signature;
import gama.api.gaml.types.Types;
import gama.api.utils.collections.Collector;
import gama.dev.DEBUG;
import gaml.compiler.EGaml;
import gaml.compiler.descriptions.DoDescription;
import gaml.compiler.descriptions.ExperimentDescription;
import gaml.compiler.descriptions.ModelDescription;
import gaml.compiler.descriptions.PlatformSpeciesDescription;
import gaml.compiler.gaml.Access;
import gaml.compiler.gaml.ActionRef;
import gaml.compiler.gaml.Array;
import gaml.compiler.gaml.BinaryOperator;
import gaml.compiler.gaml.BooleanLiteral;
import gaml.compiler.gaml.DoubleLiteral;
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
import gaml.compiler.gaml.StringLiteral;
import gaml.compiler.gaml.TypeDefinition;
import gaml.compiler.gaml.TypeInfo;
import gaml.compiler.gaml.TypeRef;
import gaml.compiler.gaml.Unary;
import gaml.compiler.gaml.Unit;
import gaml.compiler.gaml.UnitName;
import gaml.compiler.gaml.VarDefinition;
import gaml.compiler.gaml.VariableRef;
import gaml.compiler.gaml.util.GamlSwitch;

/**
 * ExpressionCompilationSwitch is a stateful switch class that handles the compilation of GAML AST nodes into executable
 * expressions. This class extends GamlSwitch and maintains a ExpressionCompilationContext instance to track all mutable
 * state during compilation.
 *
 * <h2>Architecture:</h2>
 * <p>
 * This class is designed to be instantiated per compilation session with its own ExpressionCompilationContext. All case
 * methods have access to the context via the instance field, eliminating the need for ThreadLocal or passing context
 * through method parameters.
 * </p>
 *
 * <h2>Lifecycle:</h2>
 * <p>
 * - Created: Once per compilation session by GamlExpressionCompiler - Used: To compile all expressions in that session
 * - Disposed: After compilation completes, context is cleared
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 2.0
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class ExpressionCompilationSwitch extends GamlSwitch<IExpression> {

	/** Cached EGaml singleton instance to avoid repeated getInstance() calls */
	private static final EGaml EGAML = EGaml.getInstance();

	/** The Constant FACTORY. */
	private static final IExpressionFactory FACTORY = GamlExpressionFactory.getInstance();

	/** Cached NumberFormat for US locale to reduce object allocation in double parsing */
	private static final NumberFormat US_NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

	/** The compilation context containing all mutable state for this compilation session */
	private final ExpressionCompilationContext context;

	/**
	 * Creates a new compilation switch with the given context.
	 *
	 * @param context
	 *            the compilation context to use
	 */
	public ExpressionCompilationSwitch(final ExpressionCompilationContext context) {
		this.context = context;
	}

	/**
	 * Compiles an EObject into an IExpression.
	 *
	 * @param s
	 *            the EObject to compile (can be null for error handling)
	 * @return the compiled expression, or null if compilation fails
	 */
	public IExpression compile(final EObject s) {
		// No error since null expressions come from previous (more focused) errors and not from the parser itself.
		if (s == null) return null;
		final IExpression expr = doSwitch(s.eClass().getClassifierID(), s);
		if (expr != null && context.getContext() != null) { context.getDocumentationContext().document(s, expr); }
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
		return FACTORY.createSkillConstant(name);
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
		// Early validation
		if (op == null) return null;

		final IExpression expr = compile(e);
		if (expr == null) return null;

		// The unary "unit" operator should let the value of its child pass through
		if ("#".equals(op)) return expr;

		// Handle species casting
		if (isSpeciesName(op))
			return FACTORY.createAs(context.getContext(), expr, getSpeciesContext(op).getSpeciesExpr());

		// Check for field getter
		final IArtefact proto = expr.getGamlType().getGetter(op);
		if (proto != null) {
			// It can only be a field as 'actions' are not defined on simple objects
			final IExpression fieldExpr = FACTORY.createOperator(proto, context.getContext(), e, expr);
			if (context.getContext() != null) { context.getDocumentationContext().document(e, expr); }
			return fieldExpr;
		}

		return FACTORY.createOperator(op, context.getContext(), e, expr);
	}

	/**
	 * Creates a type casting expression.
	 */
	private IExpression casting(final IType type, final IExpression toCast, final Expression typeObject) {
		if (toCast == null) return null;

		final IType castingType = type.typeIfCasting(toCast);
		final TypeParameters typeParams = extractTypeParameters(typeObject, castingType);
		final IType result = GamaType.from(castingType, typeParams.keyType, typeParams.contentType);

		return FACTORY.createAs(context.getContext().getTypeContext(), toCast, FACTORY.createTypeExpression(result));
	}

	/** Helper class to hold resolved type parameters. */
	private static record TypeParameters(IType keyType, IType contentType) {}

	/**
	 * Extracts and resolves type parameters from a type expression.
	 */
	private TypeParameters extractTypeParameters(final Expression typeObject, final IType castingType) {
		IType keyType = castingType.getKeyType();
		IType contentType = castingType.getContentType();

		TypeInfo typeInfo = null;
		if (typeObject instanceof TypeRef) {
			typeInfo = ((TypeRef) typeObject).getParameter();
		} else if (typeObject instanceof Function) { typeInfo = ((Function) typeObject).getType(); }

		if (typeInfo != null) {
			IType kt = fromTypeRef((TypeRef) typeInfo.getFirst());
			IType ct = fromTypeRef((TypeRef) typeInfo.getSecond());

			if (ct == null || ct == Types.NO_TYPE) {
				ct = kt;
				kt = null;
			}

			if (ct != null && ct != Types.NO_TYPE) { contentType = ct; }
			if (kt != null && kt != Types.NO_TYPE) { keyType = kt; }
		}

		return new TypeParameters(keyType, contentType);
	}

	/**
	 * Resolves a TypeRef AST node to an IType.
	 */
	IType fromTypeRef(final TypeRef object) {
		if (object == null) return null;

		String primary = EGAML.getKeyOf(object);
		if (primary == null) {
			primary = object.getRef().getName();
		} else if (ISyntacticFactory.SPECIES_VAR.equals(primary)) { primary = SPECIES; }

		final IType t = context.getTypesManager().get(primary);

		if (t == Types.NO_TYPE && !UNKNOWN.equals(primary)) {
			context.getContext().error(primary + " is not a valid type name", IGamlIssue.NOT_A_TYPE, object, primary);
			return t;
		}

		// if (t.isAgentType() && t.getSpecies().isModel()) {
		// final IType modelSpeciesType = resolveModelSpeciesType(object, t);
		// if (modelSpeciesType != null) return modelSpeciesType;
		// }

		context.getDocumentationContext().document(object, t);
		if (t.isAgentType()) return t;

		return resolveParametricType(object, t);
	}
	//
	// /**
	// * Resolves nested species types within model type managers.
	// */
	// private IType resolveModelSpeciesType(final TypeRef object, final IType modelType) {
	// final TypeInfo parameter = object.getParameter();
	// if (parameter == null) return modelType;
	//
	// final TypeRef first = (TypeRef) parameter.getFirst();
	// if (first == null) return modelType;
	//
	// final IDescription savedContext = context.getContext();
	// try {
	// context.setContext(modelType.getSpecies().getModelDescription());
	// return fromTypeRef(first);
	// } finally {
	// context.setContext(savedContext);
	// }
	// }

	/**
	 * Resolves parametric type information from TypeRef.
	 */
	private IType resolveParametricType(final TypeRef object, final IType baseType) {
		final TypeInfo parameter = object.getParameter();
		if (parameter == null) return baseType;

		final int numberOfParameter = baseType.getNumberOfParameters();
		if (numberOfParameter == 0) {
			context.getContext().warning(baseType + " is not a parametric type. Type parameters will be ignored",
					IGamlIssue.WRONG_TYPE, object);
			return baseType;
		}

		final TypeRef first = (TypeRef) parameter.getFirst();
		if (first == null) return baseType;

		final TypeRef second = (TypeRef) parameter.getSecond();
		if (second == null) {
			if (numberOfParameter == 2) {
				context.getContext().warning(baseType + " expects two type parameters", IGamlIssue.WRONG_TYPE, object);
			}
			return GamaType.from(baseType, baseType.getKeyType(), fromTypeRef(first));
		}

		if (numberOfParameter == 1) {
			context.getContext().error(baseType + " expects only one type parameter", IGamlIssue.WRONG_TYPE, object);
			return null;
		}

		return GamaType.from(baseType, fromTypeRef(first), fromTypeRef(second));
	}

	/**
	 * Compiles a binary operation.
	 */
	private IExpression binary(final String op, final IExpression left, final Expression originalExpression) {
		if (left == null || op == null) return null;

		if (!GAML.containsOperatorNamed(op)) {
			context.getContext().error("Unknown operator: " + op, IGamlIssue.UNKNOWN_ACTION,
					originalExpression.eContainer(), op);
			return null;
		}

		final boolean isIterator = GAML.isIterator(op);
		if (isIterator) return compileIteratorBinary(op, left, originalExpression);

		Expression rightMember = originalExpression;

		if (rightMember instanceof ExpressionList el) {
			final List<Expression> list = EGAML.getExprsOf(el);
			final int size = list.size();
			if (size > 1) {
				final IExpression[] compiledArgs = new IExpression[size + 1];
				compiledArgs[0] = left;
				for (int i = 0; i < size; i++) { compiledArgs[i + 1] = compile(list.get(i)); }
				return FACTORY.createOperator(op, context.getContext(), rightMember, compiledArgs);
			}
		}

		final IExpression right = compile(rightMember);
		return FACTORY.createOperator(op, context.getContext(), originalExpression.eContainer(), left, right);
	}

	/**
	 * Compiles iterator-based binary operations.
	 */
	private IExpression compileIteratorBinary(final String op, final IExpression left,
			final Expression originalExpression) {
		Expression rightMember = originalExpression;
		String argName = IKeyword.EACH;

		if (rightMember instanceof ExpressionList params) {
			final List<Expression> exprs = EGAML.getExprsOf(params);
			if (!exprs.isEmpty()) {
				final Expression arg = exprs.get(0);
				if (arg instanceof Parameter p) {
					argName = EGAML.getKeyOf(p);
					rightMember = p.getRight();
				} else {
					rightMember = arg;
				}
			}
		}

		final IType leftType = left.getGamlType();
		final IType eachType = leftType.isContainer() ? leftType.getContentType() : leftType;
		context.pushIteratorContext(new EachExpression(argName, eachType));

		try {
			final IExpression right = compile(rightMember);
			final IExpression eachName = FACTORY.createConst(argName, Types.STRING);
			return FACTORY.createOperator(op, context.getContext(), originalExpression.eContainer(), eachName, left,
					right);
		} finally {
			context.popIteratorContext();
		}
	}

	/**
	 * Creates an action call expression.
	 */
	private IExpression action(final String name, final IExpression callee, final ExpressionList args,
			final IActionDescription action) {
		final Arguments arguments = parseArguments(action, args, context.getContext(), true);
		return FACTORY.createAction(name, context.getContext(), action, callee, arguments);
	}

	/**
	 * Compiles a binary expression with special handling for specific operators.
	 */
	private IExpression binary(final String op, final Expression e1, final Expression right) {
		return switch (op) {
			case OF -> compileFieldAccess(right, e1, null);
			case _DOT -> compileFieldAccess(e1, right, null);
			case AS -> {
				final String type = EGAML.getKeyOf(right);
				IType t = getType(type);
				if (t != null) { yield casting(t, compile(e1), right); }
				context.getContext().error(
						"'as' must be followed by a type, species or skill name. " + type + " is neither of these.",
						IGamlIssue.NOT_A_TYPE, right, type);
				yield null;
			}
			case IS -> {
				final IExpression left = compile(e1);
				final String type = EGAML.getKeyOf(right);
				if (isTypeName(type)) {
					yield FACTORY.createOperator(IS, context.getContext(), right.eContainer(), left,
							FACTORY.createConst(type, Types.STRING));
				}
				if (isSkillName(type)) {
					yield FACTORY.createOperator(IS_SKILL, context.getContext(), right.eContainer(), left,
							FACTORY.createSkillConstant(type));
				}
				context.getContext().error(
						"'is' must be followed by a type, species or skill name. " + type + " is neither of these.",
						IGamlIssue.NOT_A_TYPE, right, type);
				yield null;
			}
			default -> binary(op, compile(e1), right);
		};
	}

	/**
	 * Gets the species context.
	 *
	 * @param e
	 *            the e
	 * @return the species context
	 */
	private ISpeciesDescription getSpeciesContext(final String e) {
		return context.getContext().getSpeciesDescription(e);
	}

	/**
	 * Checks if is species name.
	 *
	 * @param s
	 *            the s
	 * @return true, if is species name
	 */
	private boolean isSpeciesName(final String name) {
		final IModelDescription m = context.getContext().getModelDescription();
		if (m == null) return false;
		final ISpeciesDescription sd = m.getSpeciesDescription(name);
		return sd != null && !(sd instanceof IExperimentDescription);
	}

	/**
	 * Checks if is skill name.
	 *
	 * @param s
	 *            the s
	 * @return true, if is skill name
	 */
	private boolean isSkillName(final String s) {
		return GamaSkillRegistry.INSTANCE.hasSkill(s);
	}

	/**
	 * Checks if is type name.
	 *
	 * @param s
	 *            the s
	 * @return true, if is type name
	 */
	private boolean isTypeName(final String name) {
		final IType t = context.getTypesManager().get(name, null);
		if (t == null) return false;
		final ITypeDescription td = t.getSpecies();
		if (td instanceof ISpeciesDescription sd && sd.isExperiment()) return false;
		return true;
	}

	/**
	 * Gets the type.
	 *
	 * @param s
	 *            the s
	 * @return the type
	 */
	private IType getType(final String name) {
		final IType t = context.getTypesManager().get(name, null);
		if (t == null) return null;
		final ITypeDescription td = t.getSpecies();
		if (td instanceof ISpeciesDescription sd && sd.isExperiment()) return null;
		return t;
	}

	/**
	 * Compile field access.
	 *
	 * @param ownerExpr
	 *            the owner expr
	 * @param fieldExpr
	 *            the field expr
	 * @param fieldName
	 *            the field name
	 * @return the i expression
	 */
	public IExpression compileFieldAccess(final Expression ownerExpr, final Expression fieldExpr,
			final String fieldName) {
		final IExpression owner = compile(ownerExpr);
		if (owner == null) return null;

		final String var = fieldName != null ? fieldName : EGAML.getKeyOf(fieldExpr);
		final IType type = owner.getGamlType();

		if (fieldName != null && type.isParametricFormOf(Types.SPECIES)) {
			final ITypeDescription sd = type.getContentType().getSpecies();
			if (sd instanceof IModelDescription md && md.hasExperiment(fieldName))
				return FACTORY.createConst(fieldName, GamaType.from(md.getExperiment(fieldName)));
		}

		if (type instanceof ParametricType pt && pt.getGamlType().id() == IType.SPECIES
				&& pt.getContentType().getSpecies() instanceof IModelDescription md && md.hasExperiment(var))
			return FACTORY.createConst(var, GamaType.from(md.getExperiment(var)));

		final ITypeDescription species = type.getSpecies();
		if (species == null) return compileReadOnlyTypeField(ownerExpr, fieldExpr, var, type, owner);

		return compileAgentFieldOrAction(fieldExpr, owner, var, species);
	}

	/**
	 * Compile simple type field.
	 *
	 * @param ownerExpr
	 *            the owner expr
	 * @param fieldExpr
	 *            the field expr
	 * @param fieldName
	 *            the field name
	 * @param ownerType
	 *            the owner type
	 * @param compiledOwner
	 *            the compiled owner
	 * @return the i expression
	 */
	private IExpression compileReadOnlyTypeField(final Expression ownerExpr, final Expression fieldExpr,
			final String fieldName, final IType ownerType, final IExpression compiledOwner) {
		final IArtefact proto = ownerType.getGetter(fieldName);
		if (proto == null) {
			if (ownerType.id() == IType.MATRIX && fieldExpr != null) return binary(".", compiledOwner, fieldExpr);
			context.getContext().error("Unknown field '" + fieldName + "' for type " + ownerType,
					IGamlIssue.UNKNOWN_FIELD, ownerExpr, fieldName, ownerType.toString());
			return null;
		}
		final IExpression expr = FACTORY.createOperator(proto, context.getContext(), fieldExpr, compiledOwner);
		if (context.getContext() != null && fieldExpr != null) {
			context.getDocumentationContext().document(fieldExpr, expr);
		}
		return expr;
	}

	/**
	 * Compile agent field or action.
	 *
	 * @param fieldExpr
	 *            the field expr
	 * @param owner
	 *            the owner
	 * @param varName
	 *            the var name
	 * @param species
	 *            the species
	 * @return the i expression
	 */
	private IExpression compileAgentFieldOrAction(final Expression fieldExpr, final IExpression owner,
			final String varName, final ITypeDescription species) {
		if (fieldExpr instanceof VariableRef) return compileAgentVariable(fieldExpr, owner, varName, species);
		if (fieldExpr instanceof Function)
			return compileAgentOrObjectAction((Function) fieldExpr, owner, varName, species);

		return null;
	}

	/**
	 * Compile agent or object action.
	 *
	 * @param function
	 *            the function
	 * @param target
	 *            the target
	 * @param name
	 *            the name
	 * @param species
	 *            the species
	 * @return the i expression
	 */
	public IExpression compileAgentOrObjectAction(final Function function, final IExpression target, final String name,
			final ITypeDescription species) {
		final IActionDescription action = species.getAction(name);
		if (action != null) {
			final ExpressionList list = function.getRight();
			final IExpression call = action(name, target, list, action);
			// Use function.getLeft() (the action-name node, always in the real resource) rather than function
			// itself, which may be a synthetic detached node when called from the deprecated do-facet form.
			context.getDocumentationContext().document(function.getLeft(), call);
			return call;
		}
		return null;
	}

	/**
	 * Compiles an action call for the deprecated facet-based {@code do} form ({@code do action arg1: v1 arg2: v2;})
	 * without touching the EMF parse tree.
	 *
	 * <p>
	 * Unlike {@link #compileAgentOrObjectAction} which reads arguments from a {@link Function}'s
	 * {@link ExpressionList} (requiring the value expressions to be re-parented into synthetic EMF nodes),
	 * this method builds the {@link Arguments} map directly from the original {@link Facet} objects. Each facet's
	 * value expression ({@code f.getExpr()}) is read by reference — no containment change is made — so no EMF
	 * change-notification is fired and no ghost re-validation can be triggered.
	 * </p>
	 *
	 * @param nameExpr
	 *            the action-name expression node (the real {@code Expression} from the {@code S_Do} statement,
	 *            used as the documentation anchor)
	 * @param target
	 *            the pre-compiled receiver expression ({@code self}, {@code super}, or an explicit agent)
	 * @param facets
	 *            the raw facet list from the parsed {@code S_Do} statement; may be empty but must not be {@code null}
	 * @param species
	 *            the type description in which to look up the action
	 * @return the compiled {@link ActionCallOperator}, or {@code null} if the action cannot be found
	 */
	public IExpression compileActionCallFromFacets(final Expression nameExpr, final IExpression target,
			final EList<gaml.compiler.gaml.Facet> facets, final ITypeDescription species) {
		final String name = EGAML.getKeyOf(nameExpr);
		final IActionDescription action = species.getAction(name);
		if (action == null) return null;
		// Build Arguments directly from Facet nodes — read-only, no EMF reparenting.
		final Arguments arguments = new Arguments();
		final List<String> expectedArgs = action.getArgNames();
		final IExpressionDescriptionFactory builder = GAML.getExpressionDescriptionFactory();
		int positionalIndex = 0;
		for (final gaml.compiler.gaml.Facet f : facets) {
			final Expression valueExpr = f.getExpr();
			if (valueExpr == null) continue;
			String argName = f.getKey();
			if (argName != null && argName.endsWith(":")) { argName = argName.substring(0, argName.length() - 1); }
			if (argName == null || argName.isEmpty()) {
				// Positional argument
				if (expectedArgs != null && positionalIndex < expectedArgs.size()) {
					argName = expectedArgs.get(positionalIndex);
				} else {
					argName = String.valueOf(positionalIndex);
				}
				positionalIndex++;
			}
			// createFromEObject reads valueExpr by reference only — no containment change.
			final IExpressionDescription ed = builder.createFromEObject(valueExpr);
			if (ed != null) { ed.setExpression(compile(valueExpr)); }
			arguments.put(argName, ed);
		}
		final IExpression call = FACTORY.createAction(name, context.getContext(), action, target, arguments);
		context.getDocumentationContext().document(nameExpr, call);
		return call;
	}

	/**
	 * Compile agent variable.
	 *
	 * @param fieldExpr
	 *            the field expr
	 * @param owner
	 *            the owner
	 * @param varName
	 *            the var name
	 * @param species
	 *            the species
	 * @return the i expression
	 */
	private IExpression compileAgentVariable(final Expression fieldExpr, final IExpression owner, final String varName,
			final ITypeDescription species) {
		IExpression expr = species.getVarExpr(varName, true);

		if (expr == null) {
			if (species instanceof ModelDescription md && md.hasExperiment(varName)) {
				expr = FACTORY.createTypeExpression(GamaType.from(Types.SPECIES, Types.INT, md.getTypeNamed(varName)));
			} else if (species instanceof PlatformSpeciesDescription psd && GAMA.isInHeadLessMode())
				return psd.getFakePrefExpression(varName);
			else {
				context.getContext().error(
						"Unknown variable '" + varName + "' in " + species.getKeyword() + " " + species.getName(),
						IGamlIssue.UNKNOWN_VAR, fieldExpr.eContainer(), varName, species.getName());
				return null;
			}
		}

		expr = applyCastForSimulationVariables(varName, expr);

		context.getDocumentationContext().document(fieldExpr, expr);
		return FACTORY.createOperator(_DOT, context.getContext(), fieldExpr, owner, expr);
	}

	/**
	 * Apply cast for simulation variables.
	 *
	 * @param varName
	 *            the var name
	 * @param expr
	 *            the expr
	 * @return the i expression
	 */
	private IExpression applyCastForSimulationVariables(final String varName, final IExpression expr) {
		final IModelDescription md = context.getContext().getModelDescription();
		if (md == null) return expr;

		if (IKeyword.SIMULATION.equals(varName) && expr.getGamlType().equals(Types.get(IKeyword.MODEL)))
			return FACTORY.createAs(context.getContext(), expr, md.getGamlType());

		if (IKeyword.SIMULATIONS.equals(varName)
				&& expr.getGamlType().getContentType().equals(Types.get(IKeyword.MODEL)))
			return FACTORY.createAs(context.getContext(), expr, Types.LIST.of(md.getGamlType()));

		return expr;
	}

	/**
	 * Parses and validates arguments for action calls.
	 */
	public Arguments parseArguments(final IActionDescription action, final EObject o, final IDescription command,
			final boolean compileArgValues) {
		final List<Expression> parameters = o instanceof Array array ? EGAML.getExprsOf(array.getExprs())
				: o instanceof ExpressionList ? EGAML.getExprsOf(o) : null;
		if (parameters == null) return null;

		final boolean completeArgs = o instanceof ExpressionList;
		final List<String> expectedArgs = action == null ? null : action.getArgNames();
		final Arguments argMap = new Arguments();
		final IExpressionDescriptionFactory builder = GAML.getExpressionDescriptionFactory();

		int positionalIndex = 0;
		for (final Expression exp : parameters) {
			final ArgInfo argInfo =
					extractArgumentInfo(exp, completeArgs, expectedArgs, positionalIndex, action, command);
			if (argInfo == null) return argMap;

			if (argInfo.incrementIndex) { positionalIndex++; }

			IExpressionDescription ed = builder.createFromEObject(argInfo.valueExpr);
			if (ed != null && compileArgValues) {
				// We keep the same compilation context
				ed.setExpression(compile(argInfo.valueExpr));
				// ed.compile(command); would switch the compilation context
			}
			argMap.put(argInfo.name, ed);
		}

		return argMap;
	}

	/**
	 * Parses the arguments for constructor.
	 *
	 * @param target
	 *            the target
	 * @param o
	 *            the o
	 * @param command
	 *            the command
	 * @param compileArgValues
	 *            the compile arg values
	 * @return the arguments
	 */
	public Arguments parseArgumentsForConstructor(final ITypeDescription target, final List<Expression> parameters) {
		if (parameters == null) return null;
		final Collection<String> expectedArgs = target == null ? null : target.getAttributeNames();
		final Arguments argMap = new Arguments();
		final IExpressionDescriptionFactory builder = GAML.getExpressionDescriptionFactory();

		for (final Expression exp : parameters) {
			final ArgInfo argInfo = extractConstructorArgumentInfo(exp, expectedArgs);
			if (argInfo == null) return argMap;
			IExpressionDescription ed = builder.createFromEObject(argInfo.valueExpr);
			if (ed != null) {
				// We keep the same compilation context
				ed.setExpression(compile(argInfo.valueExpr));
				// ed.compile(command); would switch the compilation context
			}
			argMap.put(argInfo.name, ed);
		}

		return argMap;
	}

	/**
	 * The Class ArgInfo.
	 */
	private static class ArgInfo {

		/** The name. */
		final String name;

		/** The value expr. */
		final Expression valueExpr;

		/** The increment index. */
		final boolean incrementIndex;

		/**
		 * Instantiates a new arg info.
		 *
		 * @param name
		 *            the name
		 * @param valueExpr
		 *            the value expr
		 * @param incrementIndex
		 *            the increment index
		 */
		ArgInfo(final String name, final Expression valueExpr, final boolean incrementIndex) {
			this.name = name;
			this.valueExpr = valueExpr;
			this.incrementIndex = incrementIndex;
		}
	}

	/**
	 * Extract argument info.
	 *
	 * @param exp
	 *            the exp
	 * @param completeArgs
	 *            the complete args
	 * @param expectedArgs
	 *            the expected args
	 * @param positionalIndex
	 *            the positional index
	 * @param action
	 *            the action
	 * @param command
	 *            the command
	 * @return the arg info
	 */
	private ArgInfo extractArgumentInfo(final Expression exp, final boolean completeArgs,
			final List<String> expectedArgs, final int positionalIndex, final IActionDescription action,
			final IDescription command) {

		if (exp instanceof Parameter p) return new ArgInfo(EGAML.getKeyOfParameter(p), p.getRight(), false);
		if (exp instanceof BinaryOperator bo && "::".equals(bo.getOp()))
			return new ArgInfo(EGAML.getKeyOf(bo.getLeft()), bo.getRight(), false);

		if (completeArgs) {
			if (expectedArgs != null && positionalIndex >= expectedArgs.size()) {
				command.error("Wrong number of arguments. Action " + action.getName() + " expects " + expectedArgs);
				return null;
			}
			final String argName =
					expectedArgs == null ? String.valueOf(positionalIndex) : expectedArgs.get(positionalIndex);
			return new ArgInfo(argName, exp, true);
		}

		return null;
	}

	/**
	 * Extract constructor argument info.
	 *
	 * @param exp
	 *            the exp
	 * @param expectedArgs
	 *            the expected args
	 * @param action
	 *            the action
	 * @param command
	 *            the command
	 * @return the arg info
	 */
	private ArgInfo extractConstructorArgumentInfo(final Expression exp, final Collection<String> expectedArgs) {
		String key = null;
		Expression value = null;
		if (exp instanceof Parameter p) {
			key = EGAML.getKeyOfParameter(p);
			value = p.getRight();
		} else if (exp instanceof BinaryOperator bo && "::".equals(bo.getOp())) {
			key = EGAML.getKeyOf(bo.getLeft());
			value = bo.getRight();
		}
		if (key == null) {
			this.context.getContext().error("Impossible to compile argument");
			return null;
		}
		if (expectedArgs != null && !expectedArgs.contains(key)) {
			this.context.getContext().error("Not an attribute: " + key);
			return null;
		}

		return new ArgInfo(key, value, false);
	}

	// ==================== Case Methods (Override from GamlSwitch) ====================

	@Override
	public IExpression caseBinaryOperator(final BinaryOperator object) {
		return binary(object.getOp(), object.getLeft(), object.getRight());
	}

	@Override
	public IExpression caseUnary(final Unary object) {
		return unary(EGAML.getKeyOf(object), object.getRight());
	}

	@Override
	public IExpression caseUnit(final Unit object) {
		return binary("*", object.getLeft(), object.getRight());
	}

	/**
	 * Case dot.
	 *
	 * @param object
	 *            the object
	 * @return the i expression
	 */
	public IExpression caseDot(final Access object) {
		final Expression right = object.getRight();
		if (right instanceof StringLiteral sl) return compileFieldAccess(object.getLeft(), null, sl.getOp());
		if (right != null) return compileFieldAccess(object.getLeft(), right, null);
		return null;
	}

	@Override
	public IExpression caseAccess(final Access object) {
		if (".".equals(object.getOp())) return caseDot(object);
		final IExpression container = compile(object.getLeft());
		if (container == null) return null;
		final IType contType = container.getGamlType();
		final boolean isMatrix = Types.MATRIX.isAssignableFrom(contType);
		final IType keyType = contType.getKeyType();
		final List<? extends Expression> list = EGAML.getExprsOf(object.getRight());
		try (final Collector.AsList<IExpression> result = Collector.getList()) {
			final int size = list.size();

			for (int i = 0; i < size; i++) {
				final Expression eExpr = list.get(i);
				final IExpression e = compile(eExpr);
				if (e != null) {
					final IType elementType = e.getGamlType();
					if (size == 1 && Types.PAIR.isAssignableFrom(elementType)
							&& Types.LIST.isAssignableFrom(contType)) {
						if (Types.INT == elementType.getKeyType() && Types.INT == elementType.getContentType())
							return FACTORY.createOperator("internal_between", context.getContext(), object, container,
									e);
					}
					if (keyType != Types.NO_TYPE && !keyType.isAssignableFrom(elementType)
							&& (!isMatrix || elementType.id() != IType.INT)) {
						context.getContext().warning("a " + contType.toString() + " should not be accessed using a "
								+ elementType.toString() + " index", IGamlIssue.WRONG_TYPE, eExpr);
					}
					result.add(e);
				}
			}
			if (size > 2) {
				final String end = !isMatrix ? " only 1 index" : " 1 or 2 indices";
				context.getContext().warning("a " + contType.toString() + " should be accessed using" + end,
						IGamlIssue.DIFFERENT_ARGUMENTS, object);
			}

			final IExpression indices = FACTORY.createList(result.items());

			IExpression varDiff = null;
			if (container instanceof IVarExpression.Agent && ((IVarExpression.Agent) container).getOwner() != null) {
				varDiff = ((IVarExpression.Agent) container).getVar();

				final ITypeDescription species =
						((IVarExpression.Agent) varDiff).getDefinitionDescription().getTypeContext();
				if (species != null) {
					final Iterable<IDescription> equations = species.getChildrenWithKeyword(IKeyword.EQUATION);
					for (final IDescription equation : equations) {
						if (equation.manipulatesVar(varDiff.getName()))
							return FACTORY.createOperator("internal_integrated_value", context.getContext(), object,
									((IVarExpression.Agent) container).getOwner(), varDiff);
					}
				}
			}

			return FACTORY.createOperator("internal_at", context.getContext(), object, container, indices);
		}
	}

	@Override
	public IExpression caseArray(final Array object) {
		final List<? extends Expression> list = EGAML.getExprsOf(object.getExprs());
		final boolean allPairs = !list.isEmpty() && Iterables.all(list, each -> "::".equals(EGAML.getKeyOf(each)));
		final Iterable<IExpression> result = Iterables.transform(list, this::compile);
		if (Iterables.any(result, t -> t == null)) return null;
		return allPairs ? FACTORY.createMap(result) : FACTORY.createList(result);
	}

	@Override
	public IExpression casePoint(final Point object) {
		final Expression z = object.getZ();
		if (z == null) return binary(POINT, object.getLeft(), object.getRight());
		final IExpression[] exprs = new IExpression[3];
		exprs[0] = compile(object.getLeft());
		exprs[1] = compile(object.getRight());
		exprs[2] = compile(z);
		if (exprs[0] == null || exprs[1] == null || exprs[2] == null) return null;
		return FACTORY.createOperator(POINT, context.getContext(), object, exprs);
	}

	@Override
	public IExpression caseExpressionList(final ExpressionList object) {
		final List<Expression> list = EGAML.getExprsOf(object);
		if (list.isEmpty()) return null;
		if (list.size() > 1) {
			context.getContext().warning(
					"A sequence of expressions is not expected here. Only the first expression will be evaluated",
					IGamlIssue.UNKNOWN_ARGUMENT, object);
		}
		return compile(list.get(0));
	}

	@Override
	public IExpression caseFunction(final Function object) {
		if (object == null) return null;
		final String op = EGAML.getKeyOf(object.getLeft());
		IExpression result = tryCastingFunction(op, object);
		if (result != null) return result;
		result = tryActionCall(op, object);
		if (result != null) return result;
		final List<Expression> args = EGAML.getExprsOf(object.getRight());
		return switch (args.size()) {
			case 0 -> {
				context.getContext().error("Unknown operator or action: " + op, IGamlIssue.UNKNOWN_ACTION, object);
				yield null;
			}
			case 1 -> unary(op, args.get(0));
			case 2 -> binary(op, args.get(0), args.get(1));
			default -> FACTORY.createOperator(op, context.getContext(), object,
					toArray(transform(args, this::compile), IExpression.class));
		};
	}

	/**
	 * Try casting function.
	 *
	 * @param op
	 *            the op
	 * @param object
	 *            the object
	 * @return the i expression
	 */
	private IExpression tryCastingFunction(final String op, final Function object) {
		IType t = getType(op);
		if (t == null) {
			if (object.getType() != null) {
				context.getContext().warning(
						op + " is not a type name: parameter types are not expected and will not be evaluated",
						IGamlIssue.UNKNOWN_ARGUMENT, object.getType());
			}
			return null;
		}
		final List<Expression> args = EGaml.getInstance().getExprsOf(object.getRight());
		if (allExpressionsAreParameters(args)) return tryConstructor(object, t.getSpecies(), args);
		return switch (args.size()) {
			case 1 -> {
				IExpression expr = compile(args.get(0));
				if (FACTORY.hasExactOperator(op, expr)) { yield null; }
				yield casting(t, expr, object);
			}
			default -> {
				Iterable<IExpression> exprs = transform(args, this::compile);
				if (FACTORY.hasOperator(op, new Signature(toArray(exprs, IExpression.class)))) { yield null; }
				yield casting(t, FACTORY.createList(exprs), object);
			}
		};
	}

	/**
	 * @param species
	 * @param args
	 * @return
	 */
	private IExpression tryConstructor(final Function object, final ITypeDescription species,
			final List<Expression> args) {
		if (species.isAbstract()) {
			context.getContext().error("Cannot instantiate abstract type: " + species.getName(), IGamlIssue.GENERAL,
					object);
			return null;
		}
		return new Constructor(species, parseArgumentsForConstructor(species, args));
	}

	/**
	 * @param args
	 * @return
	 */
	private boolean allExpressionsAreParameters(final List<Expression> args) {
		if (args.size() == 0) return true;
		for (Expression expr : args) { if (!(expr instanceof Parameter)) return false; }
		return true;
	}

	/**
	 * Try action call.
	 *
	 * @param op
	 *            the op
	 * @param object
	 *            the object
	 * @return the i expression
	 */
	private IExpression tryActionCall(final String op, final Function object) {
		ITypeDescription species = context.getContext().getTypeContext();
		if (species == null) return null;
		final boolean isSuper = context.getContext() instanceof DoDescription st && st.isSuperInvocation();
		IActionDescription action = isSuper ? species.getParent().getAction(op) : species.getAction(op);
		if (action == null) {
			if (species instanceof ExperimentDescription && context.getContext().isIn(IKeyword.OUTPUT)) {
				species = species.getModelDescription();
			}
			action = isSuper ? species.getParent().getAction(op) : species.getAction(op);
		}
		if (action == null) return null;
		final ExpressionList params = object.getRight();
		// Use object.getLeft() (the real action-name Expression, always in the live resource) rather than
		// object itself as the anchor for the self/super documentation — object may be a synthetic detached
		// Function node when compiled via the deprecated do-facet form.
		return action(op, caseVar(isSuper ? SUPER : SELF, object.getLeft()), params, action);
	}

	@Override
	public IExpression caseIntLiteral(final IntLiteral object) {
		try {
			final Integer val = Integer.parseInt(EGAML.getKeyOf(object), 10);
			return FACTORY.createConst(val, Types.INT);
		} catch (final NumberFormatException e) {
			context.getContext().error("Malformed integer: " + EGAML.getKeyOf(object), IGamlIssue.UNKNOWN_NUMBER,
					object);
			return null;
		}
	}

	@Override
	public IExpression caseDoubleLiteral(final DoubleLiteral object) {
		String s = EGAML.getKeyOf(object);
		if (s == null) return null;
		try {
			return FACTORY.createConst(Double.parseDouble(s), Types.FLOAT);
		} catch (final NumberFormatException e) {
			try {
				s = s.replace('e', 'E').replace("+", "");
				return FACTORY.createConst(US_NUMBER_FORMAT.parse(s).doubleValue(), Types.FLOAT);
			} catch (final ParseException ex) {
				context.getContext().error("Malformed float: " + s, IGamlIssue.UNKNOWN_NUMBER, object);
				return null;
			}
		}
	}

	@Override
	public IExpression caseStringLiteral(final StringLiteral object) {
		return FACTORY.createConst(EGAML.getKeyOf(object), Types.STRING);
	}

	@Override
	public IExpression caseBooleanLiteral(final BooleanLiteral object) {
		final String s = EGAML.getKeyOf(object);
		if (s == null) return null;
		return TRUE.equalsIgnoreCase(s) ? FACTORY.getTrue() : FACTORY.getFalse();
	}

	@Override
	public IExpression defaultCase(final EObject object) {
		final IValidationContext vc = context.getContext() == null ? null : context.getContext().getValidationContext();
		if (vc != null && !vc.hasErrors()) {
			context.getContext().error("Cannot compile: " + object, IGamlIssue.GENERAL, object);
		}
		return null;
	}

	@Override
	public IExpression caseSkillRef(final SkillRef object) {
		return skill(EGAML.getKeyOf(object));
	}

	@Override
	public IExpression caseActionRef(final ActionRef object) {
		final String s = EGAML.getKeyOf(object);
		final ITypeDescription td = context.getContext().getTypeContext();
		IActionDescription ad = td.getAction(s);
		if (ad == null) {
			boolean isExp = td instanceof IExperimentDescription;
			if (!isExp) {
				IDescription host = td.getEnclosingDescription();
				if (host != null) { ad = host.getAction(s); }
			}
			if (ad == null) {
				if (isExp) {
					context.getContext().error("The action " + s + " must be defined in the experiment",
							IGamlIssue.UNKNOWN_ACTION, object);
				} else {
					context.getContext().error("The action " + s + " is unknown", IGamlIssue.UNKNOWN_ACTION, object);
				}
				return null;
			}
		}
		if (ad.getArgNames().size() > 0) {
			context.getContext().error("Impossible to call an action that requires arguments",
					IGamlIssue.UNKNOWN_ARGUMENT, object);
			return null;
		}
		return new DenotedActionExpression(ad);
	}

	@Override
	public IExpression caseVariableRef(final VariableRef object) {
		final String s = EGAML.getNameOfRef(object);
		if (s == null) return caseVarDefinition(object.getRef());
		return caseVar(s, object);
	}

	@Override
	public IExpression caseTypeRef(final TypeRef object) {
		final IType t = fromTypeRef(object);
		if (t == null) return null;
		if (t.isAgentType()) {
			ITypeDescription td = t.getSpecies();
			if (td instanceof ISpeciesDescription sd) return sd.getSpeciesExpr();
		}
		return FACTORY.createTypeExpression(t);
	}

	@Override
	public IExpression caseEquationRef(final EquationRef object) {
		return FACTORY.createConst(EGAML.getNameOfRef(object), Types.STRING);
	}

	@Override
	public IExpression caseUnitName(final UnitName object) {
		final String s = EGAML.getNameOfRef(object);
		IExpression exp = caseUnitName(s);
		if (exp == null) {
			context.getContext().error(s + " is not a unit or constant name.", IGamlIssue.NOT_A_UNIT, object,
					(String[]) null);
			return null;
		}
		if (exp.isDeprecated()) {
			context.getContext().warning(s + " is deprecated.", IGamlIssue.DEPRECATED, object, (String[]) null);
		}
		return exp;
	}

	/**
	 * Case unit name.
	 *
	 * @param name
	 *            the name
	 * @return the i expression
	 */
	public IExpression caseUnitName(final String name) {
		return GAML.getUnit(name);
	}

	@Override
	public IExpression caseVarDefinition(final VarDefinition object) {
		return skill(object.getName());
	}

	@Override
	public IExpression caseTypeDefinition(final TypeDefinition object) {
		return caseVar(object.getName(), object);
	}

	@Override
	public IExpression caseSkillFakeDefinition(final SkillFakeDefinition object) {
		return caseVar(object.getName(), object);
	}

	@Override
	public IExpression caseReservedLiteral(final ReservedLiteral object) {
		return caseVar(EGAML.getKeyOf(object), object);
	}

	@Override
	public IExpression caseIf(final If object) {
		final IExpression ifFalse = compile(object.getIfFalse());
		final IExpression alt =
				FACTORY.createOperator(":", context.getContext(), object, compile(object.getRight()), ifFalse);
		return FACTORY.createOperator("?", context.getContext(), object, compile(object.getLeft()), alt);
	}

	/**
	 * Handles compilation of variable references by name.
	 */
	IExpression caseVar(final String varName, final EObject object) {

		if ("my_elector".equals(varName)) {

			DEBUG.OUT("");

		}

		if (varName == null) {
			context.getContext().error("Unknown variable", IGamlIssue.UNKNOWN_VAR, object);
			return null;
		}

		final IExpression specialVar = resolveSpecialVariable(varName, object);
		if (specialVar != null) return specialVar;

		final IVarExpression iterVar = context.findIteratorVariable(varName);
		if (iterVar != null) return iterVar;

		final IDescription ctx = context.getContext();
		final IVarDescriptionProvider temp_sd = ctx == null ? null : ctx.getDescriptionDeclaringVar(varName);

		if (temp_sd != null) {
			final IExpression contextVar = resolveContextVariable(varName, object, temp_sd);
			if (contextVar != null) return contextVar;
		}

		return resolveOtherReferences(varName, object, ctx);
	}

	/**
	 * Resolve special variable.
	 *
	 * @param varName
	 *            the var name
	 * @param object
	 *            the object
	 * @return the i expression
	 */
	private IExpression resolveSpecialVariable(final String varName, final EObject object) {
		return switch (varName) {
			case EXPERIMENT -> CurrentExperimentExpression.create();
			case IKeyword.GAMA -> GAMA.getPlatformAgent();
			case EACH -> getEachExpr(object);
			case NULL -> GAML.getExpressionFactory().getNil();
			case SELF -> returnSelfOrSuper(SELF, object, false);
			case SUPER -> returnSelfOrSuper(SUPER, object, true);
			default -> null;
		};
	}

	/**
	 * Resolve context variable.
	 *
	 * @param varName
	 *            the var name
	 * @param object
	 *            the object
	 * @param temp_sd
	 *            the temp sd
	 * @return the i expression
	 */
	private IExpression resolveContextVariable(final String varName, final EObject object,
			final IVarDescriptionProvider temp_sd) {
		if (!(temp_sd instanceof ISpeciesDescription)) return temp_sd.getVarExpr(varName, false);

		final ITypeDescription remote_sd = context.getContext().getTypeContext();
		if (remote_sd != null) {
			final ISpeciesDescription found_sd = (ISpeciesDescription) temp_sd;
			if (remote_sd != temp_sd && !remote_sd.isBuiltIn() && remote_sd instanceof ISpeciesDescription rsd
					&& !rsd.hasMacroSpecies(found_sd)) {
				context.getContext()
						.error("The variable " + varName + " is not accessible in this context (" + remote_sd.getName()
								+ "), but in the context of " + found_sd.getName()
								+ ". It should be preceded by 'myself.'", IGamlIssue.UNKNOWN_VAR, object, varName);
			}
		}

		if (!isSpeciesName(varName)) return temp_sd.getVarExpr(varName, false);

		return null;
	}

	/**
	 * Resolve other references.
	 *
	 * @param varName
	 *            the var name
	 * @param object
	 *            the object
	 * @param ctx
	 *            the ctx
	 * @return the i expression
	 */
	private IExpression resolveOtherReferences(final String varName, final EObject object, final IDescription ctx) {
		if (isSpeciesName(varName)) {
			final ISpeciesDescription sd = getSpeciesContext(varName);
			return sd == null ? null : sd.getSpeciesExpr();
		}

		final IType t = getType(varName);
		if (t != null) return FACTORY.createTypeExpression(t);

		if (isSkillName(varName)) return skill(varName);

		if (ctx != null) {
			final IExpression actionOrBehavior = resolveActionBehaviorAspect(varName, ctx);
			if (actionOrBehavior != null) return actionOrBehavior;

			final IExpression eventName = resolveDeprecatedEventName(varName, object, ctx);
			if (eventName != null) return eventName;

			context.getContext().error(
					varName + " is not defined or accessible in this context. Check its name or declare it",
					IGamlIssue.UNKNOWN_VAR, object, varName);
		}

		return null;
	}

	/**
	 * Resolve action behavior aspect.
	 *
	 * @param varName
	 *            the var name
	 * @param ctx
	 *            the ctx
	 * @return the i expression
	 */
	private IExpression resolveActionBehaviorAspect(final String varName, final IDescription ctx) {
		final ITypeDescription td = ctx.getTypeContext();
		if (td.hasAction(varName, false)) return new DenotedActionExpression(td.getAction(varName));
		if (td instanceof ISpeciesDescription sd) {
			if (sd.hasBehavior(varName)) return new DenotedActionExpression(sd.getBehavior(varName));
			if (sd.hasAspect(varName)) return new DenotedActionExpression(sd.getAspect(varName));
		}
		return null;
	}

	/**
	 * Resolve deprecated event name.
	 *
	 * @param varName
	 *            the var name
	 * @param object
	 *            the object
	 * @param ctx
	 *            the ctx
	 * @return the i expression
	 */
	private IExpression resolveDeprecatedEventName(final String varName, final EObject object, final IDescription ctx) {
		if (IEventLayerDelegate.MOUSE_EVENTS.contains(varName)
				|| IEventLayerDelegate.KEYBOARD_EVENTS.contains(varName)) {
			final IExpression exp = this.caseUnitName(varName);
			if (exp != null) {
				ctx.warning("The usage of the event name (" + varName
						+ ") is now deprecated and should be replaced either by a string ('" + varName
						+ "') or a constant (#" + varName + ")", IGamlIssue.UNKNOWN_VAR, object, varName);
				return exp;
			}
		}
		return null;
	}

	/**
	 * Return self or super.
	 *
	 * @param name
	 *            the name
	 * @param object
	 *            the object
	 * @param isSuper
	 *            the is super
	 * @return the i expression
	 */
	private IExpression returnSelfOrSuper(final String name, final EObject object, final boolean isSuper) {
		final ITypeDescription sd = context.getContext().getTypeContext();
		if (sd == null) {
			context.getContext().error("Unable to determine the species or class of " + name, IGamlIssue.GENERAL,
					object);
			return null;
		}
		IType type = isSuper ? sd.getParent().getGamlType() : sd.getGamlType();
		return FACTORY.createVar(name, type, true,
				isSuper ? IVarExpression.Category.SUPER : IVarExpression.Category.SELF, null);
	}

	/**
	 * Gets the each expr.
	 *
	 * @param object
	 *            the object
	 * @return the each expr
	 */
	public IVarExpression getEachExpr(final EObject object) {
		final IVarExpression p = context.peekIteratorContext();
		if (p == null) {
			context.getContext().error("'each' is not accessible in this context", IGamlIssue.UNKNOWN_VAR, object);
			return null;
		}
		return p;
	}

}

/*******************************************************************************************************
 *
 * ExpressionDescriptionFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.factories;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static one.util.streamex.IntStreamEx.range;

import java.util.concurrent.ExecutionException;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.cache.Cache;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.factories.IExpressionDescriptionFactory;
import gama.api.constants.IKeyword;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.types.Types;
import gama.api.utils.StringUtils;
import gaml.compiler.gaml.BooleanLiteral;
import gaml.compiler.gaml.DoubleLiteral;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.IntLiteral;
import gaml.compiler.gaml.StringLiteral;
import gaml.compiler.gaml.Unary;
import gaml.compiler.gaml.UnitName;
import gaml.compiler.gaml.descriptions.BasicExpressionDescription;
import gaml.compiler.gaml.descriptions.BlockExpressionDescription;
import gaml.compiler.gaml.descriptions.ConstantExpressionDescription;
import gaml.compiler.gaml.descriptions.EcoreBasedExpressionDescription;
import gaml.compiler.gaml.descriptions.LabelExpressionDescription;
import gaml.compiler.gaml.descriptions.StringBasedExpressionDescription;
import gaml.compiler.gaml.resource.GamlResourceServices;
import gaml.compiler.gaml.util.GamlSwitch;

/**
 * Centralized factory for creating all types of expression descriptions with unified caching mechanisms.
 * <p>
 * This factory consolidates all creation logic and caching that was previously distributed across individual
 * description classes, providing better performance and maintainability.
 * </p>
 */
public class ExpressionDescriptionFactory extends GamlSwitch<IExpressionDescription>
		implements IExpressionDescriptionFactory {

	/** The singleton instance. */
	static ExpressionDescriptionFactory INSTANCE;

	/** The Constant MIN_INT for pre-computed integer descriptions. */
	private final static int MIN_INT = -1000;

	/** The Constant MAX_INT for pre-computed integer descriptions. */
	private final static int MAX_INT = 1000;

	/** Pre-computed integer descriptions for performance optimization. */
	private static ConstantExpressionDescription[] INT_DESCRIPTIONS;

	/** General cache for constant expression descriptions. */
	private final static Cache<Object, ConstantExpressionDescription> CACHE = newBuilder().maximumSize(5000).build();

	/**
	 * Singleton instance representing null constant expression descriptions.
	 * <p>
	 * This field provides a cached, reusable constant expression description for null values, improving performance by
	 * avoiding repeated instantiation of the same null expression throughout the compilation process. Lazily
	 * initialized in {@link #getNull()}.
	 * </p>
	 */
	private static ConstantExpressionDescription NULL_EXPR_DESCRIPTION;

	/** Singleton instance for the boolean true constant expression description. */
	private static ConstantExpressionDescription TRUE_EXPR_DESCRIPTION;

	/** Singleton instance for the boolean false constant expression description. */
	private static ConstantExpressionDescription FALSE_EXPR_DESCRIPTION;

	/**
	 * Gets the single INSTANCE of ExpressionDescriptionFactory.
	 *
	 * @return single INSTANCE of ExpressionDescriptionFactory
	 */
	public static ExpressionDescriptionFactory getInstance() {
		if (INSTANCE == null) { INSTANCE = new ExpressionDescriptionFactory(); }
		return INSTANCE;
	}

	/**
	 * Converts an integer literal EObject to a constant expression description.
	 *
	 * <p>
	 * Handles number format errors gracefully by defaulting to 0 if the literal cannot be parsed as a valid integer.
	 * </p>
	 *
	 * @param object
	 *            the IntLiteral EObject containing the integer value
	 * @return a ConstantExpressionDescription wrapping the parsed integer value
	 */
	@Override
	public ConstantExpressionDescription caseIntLiteral(final IntLiteral object) {
		if (object == null) return createConstant(0);

		ConstantExpressionDescription ed;
		try {
			final String operand = object.getOp();
			ed = createConstant(operand != null ? Integer.parseInt(operand) : 0);
		} catch (final NumberFormatException e) {
			// Default to 0 if parsing fails
			ed = createConstant(0);
		}
		Resource r = object.eResource();
		if (r != null) { GamlResourceServices.getResourceDocumenter().setGamlDocumentation(r.getURI(), object, ed); }
		return ed;
	}

	/**
	 * Converts a double literal EObject to a constant expression description.
	 *
	 * <p>
	 * Handles number format errors gracefully by defaulting to 0.0 if the literal cannot be parsed as a valid double.
	 * </p>
	 *
	 * @param object
	 *            the DoubleLiteral EObject containing the double value
	 * @return a ConstantExpressionDescription wrapping the parsed double value
	 */
	@Override
	public ConstantExpressionDescription caseDoubleLiteral(final DoubleLiteral object) {
		if (object == null) return createConstant(0.0);
		ConstantExpressionDescription ed;
		try {
			final String operand = object.getOp();
			ed = createConstant(operand != null ? Double.parseDouble(operand) : 0.0);
		} catch (final NumberFormatException e) {
			// Default to 0.0 if parsing fails
			ed = createConstant(0.0);
		}
		Resource r = object.eResource();
		if (r != null) {
			GamlResourceServices.getResourceDocumenter().setGamlDocumentation(r.getURI(), object, ed.getExpression());
		}
		return ed;
	}

	/**
	 * Converts a boolean literal EObject to a constant expression description.
	 *
	 * @param object
	 *            the BooleanLiteral EObject containing the boolean value
	 * @return a ConstantExpressionDescription wrapping the boolean value, defaulting to false if object is null
	 */
	@Override
	public ConstantExpressionDescription caseBooleanLiteral(final BooleanLiteral object) {
		if (object == null) return createConstant(false);
		final String operand = object.getOp();
		final boolean boolValue = IKeyword.TRUE.equals(operand);
		final ConstantExpressionDescription ed = createConstant(boolValue);
		Resource r = object.eResource();
		if (r != null) {
			GamlResourceServices.getResourceDocumenter().setGamlDocumentation(r.getURI(), object, ed.getExpression());
		}
		return ed;
	}

	/**
	 * Converts a string literal EObject to a constant expression description.
	 *
	 * @param object
	 *            the StringLiteral EObject containing the string value
	 * @return a ConstantExpressionDescription wrapping the string value, or empty string if object is null
	 */
	@Override
	public ConstantExpressionDescription caseStringLiteral(final StringLiteral object) {
		if (object == null) return createConstant("");

		final String operand = object.getOp();
		final ConstantExpressionDescription ed = createConstant(operand != null ? operand : "");
		Resource r = object.eResource();
		if (r != null) {
			GamlResourceServices.getResourceDocumenter().setGamlDocumentation(r.getURI(), object, ed.getExpression());
		}
		return ed;
	}

	/**
	 * Converts a unit name EObject to its corresponding unit expression description.
	 *
	 * <p>
	 * Units in GAML represent physical quantities like meters, seconds, etc. This method looks up the unit definition
	 * from the global units registry.
	 * </p>
	 *
	 * @param object
	 *            the UnitName EObject containing the unit identifier
	 * @return the corresponding unit expression description, or null if the unit is not found
	 */
	@Override
	public IExpressionDescription caseUnitName(final UnitName object) {
		if (object == null) return null;
		final String unitKey = EGaml.getInstance().getKeyOf(object);
		return unitKey != null ? GAML.UNITS.get(unitKey) : null;
	}

	/**
	 * Default case handler for EObjects that don't have specialized conversion methods.
	 *
	 * <p>
	 * Creates an EcoreBasedExpressionDescription that can handle generic EMF objects and convert them to expressions
	 * during the compilation process.
	 * </p>
	 *
	 * @param object
	 *            the EObject to convert
	 * @return an EcoreBasedExpressionDescription wrapping the object
	 */
	@Override
	public IExpressionDescription defaultCase(final EObject object) {
		return object != null ? new EcoreBasedExpressionDescription(object) : null;
	}

	/**
	 * Creates a block expression description from a syntactic element.
	 *
	 * <p>
	 * Block expressions represent code blocks that can be executed as expressions, typically used for complex
	 * statements that need to be treated as values.
	 * </p>
	 *
	 * @param element
	 *            the syntactic element representing the code block
	 * @return a BlockExpressionDescription wrapping the syntactic element
	 * @throws IllegalArgumentException
	 *             if the element is null
	 */
	@Override
	public IExpressionDescription createBlock(final ISyntacticElement element) {
		if (element == null) throw new IllegalArgumentException("Syntactic element cannot be null");
		return new BlockExpressionDescription(element);
	}

	/**
	 * Creates an expression description from an EObject using the visitor pattern.
	 *
	 * <p>
	 * This is the main entry point for converting parsed expressions. The method uses the doSwitch mechanism to
	 * dispatch to the appropriate case method based on the EObject's concrete type.
	 * </p>
	 *
	 * @param expr
	 *            the EObject representing the parsed expression
	 * @return the corresponding IExpressionDescription, or null if expr is null
	 */
	@Override
	public IExpressionDescription createFromEObject(final EObject expr) {
		if (expr == null) return null;
		final IExpressionDescription result = doSwitch(expr);
		if (result != null) { result.setTarget(expr); }
		return result;
	}

	/**
	 * Converts a unary expression EObject to its corresponding expression description.
	 *
	 * <p>
	 * Handles special unary operators, particularly the '#' operator which is used for various GAML constructs.
	 * </p>
	 *
	 * @param object
	 *            the Unary EObject containing the unary operation
	 * @return the converted expression description, or null for unsupported operators
	 */
	@Override
	public IExpressionDescription caseUnary(final Unary object) {
		if (object == null) return null;
		final String op = EGaml.getInstance().getKeyOf(object);
		if ("#".equals(op) && object.getRight() != null) return doSwitch(object.getRight());
		return null;
	}

	/**
	 * Returns the singleton null constant expression description.
	 * <p>
	 * This method implements lazy initialization for the null expression description singleton, creating it only when
	 * first requested. Subsequent calls return the same cached instance for optimal memory usage and performance.
	 * </p>
	 *
	 * @return the singleton ConstantExpressionDescription representing null values
	 */
	private ConstantExpressionDescription getNull() {
		if (NULL_EXPR_DESCRIPTION == null) { NULL_EXPR_DESCRIPTION = new ConstantExpressionDescription(null); }
		return NULL_EXPR_DESCRIPTION;
	}

	/**
	 * Returns the singleton true constant expression description.
	 *
	 * @return the singleton ConstantExpressionDescription representing boolean true value
	 */
	private ConstantExpressionDescription getTrue() {
		if (TRUE_EXPR_DESCRIPTION == null) { TRUE_EXPR_DESCRIPTION = new ConstantExpressionDescription(true); }
		return TRUE_EXPR_DESCRIPTION;
	}

	/**
	 * Returns the singleton false constant expression description.
	 *
	 * @return the singleton ConstantExpressionDescription representing boolean false value
	 */
	private ConstantExpressionDescription getFalse() {
		if (FALSE_EXPR_DESCRIPTION == null) { FALSE_EXPR_DESCRIPTION = new ConstantExpressionDescription(false); }
		return FALSE_EXPR_DESCRIPTION;
	}

	@Override
	public ConstantExpressionDescription createConstant(final Object val) {
		if (val == null) return getNull();
		try {
			return CACHE.get(val, () -> new ConstantExpressionDescription(val));
		} catch (final ExecutionException e) {
			return null;
		}
	}

	@Override
	public ConstantExpressionDescription createConstant(final Integer val) {
		if (val == null) return getNull();
		initIntDescriptions();
		if (val >= MIN_INT && val < MAX_INT) return INT_DESCRIPTIONS[val - MIN_INT];
		try {
			return CACHE.get(val, () -> new ConstantExpressionDescription(val, Types.INT));
		} catch (final ExecutionException e) {
			return null;
		}
	}

	/**
	 *
	 */
	private void initIntDescriptions() {
		if (INT_DESCRIPTIONS == null) {
			INT_DESCRIPTIONS = range(MIN_INT, MAX_INT).mapToObj(i -> new ConstantExpressionDescription(i, Types.INT))
					.toArray(ConstantExpressionDescription.class);
		}
	}

	@Override
	public ConstantExpressionDescription createConstant(final Double val) {
		if (val == null) return getNull();
		try {
			return CACHE.get(val, () -> new ConstantExpressionDescription(val, Types.FLOAT));
		} catch (final ExecutionException e) {
			return null;
		}
	}

	@Override
	public ConstantExpressionDescription createConstant(final Boolean val) {
		return val == null ? getNull() : val ? getTrue() : getFalse();
	}

	@Override
	public ConstantExpressionDescription createConstantNoCache(final Object val) {
		return val == null ? getNull() : new ConstantExpressionDescription(val);
	}

	@Override
	public IExpressionDescription createLabel(final String val) {
		return val == null ? getNull() : new LabelExpressionDescription(val);
	}

	@Override
	public IExpressionDescription createBasic(final IExpression expr) {
		return expr == null ? getNull() : new BasicExpressionDescription(expr);
	}

	@Override
	public IExpressionDescription createStringBased(final String string) {
		if (string == null) return getNull();
		final String s = string.trim();
		switch (s) {
			case IKeyword.NULL:
			case "null":
				return getNull();
			case IKeyword.FALSE:
				return getFalse();
			case IKeyword.TRUE:
				return getTrue();
			default:
				break;
		}
		if (StringUtils.isGamaString(s)) return createLabel(StringUtils.toJavaString(s));
		return new StringBasedExpressionDescription(string);
	}

}

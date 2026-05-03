/*******************************************************************************************************
 *
 * OrdinalAttributeDeclaration.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.variables;

import static gama.api.gaml.types.Cast.asFloat;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.object.IObject;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.scope.InScope;
import gama.api.types.date.GamaDateFactory;
import gama.api.types.date.IDate;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;

/**
 * Represents a numeric variable declaration with automatic value clamping based on min, max, and step constraints.
 *
 * <p>
 * OrdinalAttributeDeclaration extends {@link AttributeDeclaration} to provide specialized handling for numeric types
 * (int, float, point, date) that can be constrained to a specific range. Values are automatically clamped when they
 * fall outside the defined min/max bounds, ensuring data integrity without throwing errors.
 * </p>
 *
 * <h2>Supported Types</h2>
 * <ul>
 * <li><b>int</b> - Integer values with integer min/max/step</li>
 * <li><b>float</b> - Floating-point values with float min/max/step</li>
 * <li><b>point</b> - 2D/3D coordinates with point min/max/step</li>
 * <li><b>date</b> - Temporal values with date min/max and duration step</li>
 * </ul>
 *
 * <h2>Clamping Behavior</h2>
 * <p>
 * When a value is set that falls outside the min/max range:
 * <ul>
 * <li>Values below min are set to min</li>
 * <li>Values above max are set to max</li>
 * <li>No exception is thrown; the clamping is silent</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Integer with Range</h3>
 *
 * <pre>{@code
 * species MySpecies {
 *     int speed <- 5 min: 0 max: 10;  // speed is always between 0 and 10
 * }
 * }</pre>
 *
 * <h3>Float Parameter with Step</h3>
 *
 * <pre>{@code
 * experiment MyExperiment type: gui {
 *     parameter "Temperature" var: temp min: 0.0 max: 50.0 step: 0.5;
 * }
 * }</pre>
 *
 * <h3>Point with Bounds</h3>
 *
 * <pre>{@code
 * species MySpecies {
 *     point position <- {50, 50} min: {0, 0} max: {100, 100};
 * }
 * }</pre>
 *
 * <h3>Date with Range</h3>
 *
 * <pre>{@code
 * global {
 *     date start_date <- date("2020-01-01");
 *     date end_date <- date("2020-12-31");
 *     date current <- date("2020-06-01") min: start_date max: end_date;
 * }
 * }</pre>
 *
 * <h3>Dynamic Range (Evaluated Each Time)</h3>
 *
 * <pre>{@code
 * species MySpecies {
 *     float energy <- 100.0 max: max_energy;  // max can change dynamically
 *     float max_energy <- 100.0;
 * }
 * }</pre>
 *
 * @param <T>
 *            the comparable type for min/max values (Integer, Double, IPoint, IDate)
 * @param <Step>
 *            the comparable type for step values
 *
 * @see AttributeDeclaration for base variable functionality
 *
 * @author Alexis Drogoul
 * @since GAMA 1.0
 */
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.NEW_VAR_ID,
				optional = false,
				doc = @doc ("The name of the attribute")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("The type of the attribute, either 'int', 'float', 'point' or 'date'")),
				@facet (
						name = IKeyword.INIT,
						// AD 02/16 TODO Allow to declare ITypeProvider.OWNER_TYPE here
						type = { IType.INT, IType.FLOAT, IType.POINT, IType.DATE },
						optional = true,
						doc = @doc ("The initial value of the attribute. Same as '<-'")),
				@facet (
						name = "<-",
						internal = true,
						// AD 02/16 TODO Allow to declare ITypeProvider.OWNER_TYPE here
						type = { IType.INT, IType.FLOAT, IType.POINT, IType.DATE },
						optional = true,
						doc = @doc ("The initial value of the attribute. Same as 'init:'")),
				@facet (
						name = IKeyword.UPDATE,
						type = { IType.INT, IType.FLOAT, IType.POINT, IType.DATE },
						optional = true,
						doc = @doc ("An expression that will be evaluated each cycle to compute a new value for the attribute")),
				@facet (
						name = IKeyword.FUNCTION,
						type = { IType.INT, IType.FLOAT, IType.POINT, IType.DATE },
						optional = true,
						doc = @doc ("Used to specify an expression that will be evaluated each time the attribute is accessed. Equivalent to '->'. This facet is incompatible with both 'init:' and 'update:'")),
				@facet (
						name = "->",
						internal = true,
						type = { IType.INT, IType.FLOAT, IType.POINT, IType.DATE },
						optional = true,
						doc = @doc ("Used to specify an expression that will be evaluated each time the attribute is accessed. Equivalent to 'function:'. This facet is incompatible with both 'init:' and 'update:' and 'on_change:' (or the equivalent final block)")),
				@facet (
						name = IKeyword.CONST,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Indicates whether this attribute can be subsequently modified or not")),
				@facet (
						name = IKeyword.ON_CHANGE,
						type = IType.NONE,
						optional = true,
						doc = @doc ("Provides a block of statements that will be executed whenever the value of the attribute changes")),
				@facet (
						name = IKeyword.MIN,
						type = { IType.INT, IType.FLOAT, IType.POINT, IType.DATE },
						optional = true,
						doc = @doc ("The minimum value this attribute can take. The value will be automatically clamped if it is lower.")),
				@facet (
						name = IKeyword.MAX,
						type = { IType.INT, IType.FLOAT, IType.POINT, IType.DATE },
						optional = true,
						doc = @doc ("The maximum value this attribute can take. The value will be automatically clampled if it is higher.")),
				@facet (
						name = IKeyword.STEP,
						type = { IType.INT, IType.FLOAT, IType.POINT, IType.DATE },
						optional = true,
						doc = @doc ("A discrete step (used in conjunction with min and max) that constrains the values this variable can take")),
				@facet (
						name = IKeyword.AMONG,
						type = IType.LIST,
						optional = true,
						doc = @doc ("A list of constant values among which the attribute can take its value")) },
		omissible = IKeyword.NAME)
@symbol (
		kind = ISymbolKind.NUMBER,
		with_sequence = false,
		concept = { IConcept.ATTRIBUTE, IConcept.ARITHMETIC })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL, ISymbolKind.CLASS })
@doc ("Declaration of an attribute of a species or an experiment; this type of attributes accepts "
		+ "min:, max: and step: facets, automatically clamping the value if it is lower than min or higher than max.")
public class OrdinalAttributeDeclaration<T extends Comparable, Step extends Comparable> extends AttributeDeclaration {

	/** The max. */
	private final IExpression min, max, step;

	/** The max val. */
	private InScope<T> minVal, maxVal;

	/** The step val. */
	private InScope<Step> stepVal;

	/**
	 * Constructs a new OrdinalAttributeDeclaration from its description.
	 *
	 * <p>
	 * This constructor extracts the min, max, and step facets and pre-compiles them if they are constant expressions.
	 * Pre-compilation improves performance by avoiding repeated evaluation of constant bounds during runtime.
	 * </p>
	 *
	 * <p>
	 * The constructor sets up type-specific clamping functions based on the variable's declared type (int, float,
	 * point, or date).
	 * </p>
	 *
	 * @param sd
	 *            the variable description containing min, max, and step facets
	 * @throws GamaRuntimeException
	 *             if the variable type is not supported for numeric constraints
	 *
	 * @see AttributeDeclaration#Variable(IDescription)
	 */
	@SuppressWarnings ("unchecked")
	public OrdinalAttributeDeclaration(final IDescription sd) throws GamaRuntimeException {
		super(sd);
		min = getFacet(IKeyword.MIN);
		max = getFacet(IKeyword.MAX);
		step = getFacet(IKeyword.STEP);
		if (min != null && min.isConst()) {
			switch (type.id()) {
				case IType.INT:
					minVal = scope -> (T) Cast.asInt(scope, min.value(scope));
					break;
				case IType.FLOAT:
					minVal = scope -> (T) Cast.asFloat(scope, min.value(scope));
					break;
				case IType.POINT:
					minVal = scope -> (T) GamaPointFactory.castToPoint(scope, min.value(scope));
					break;
				case IType.DATE:
					minVal = scope -> (T) GamaDateFactory.castToDate(scope, min.value(scope));
			}
		} else {
			minVal = null;
		}
		if (max != null && max.isConst()) {
			switch (type.id()) {
				case IType.INT:
					maxVal = scope -> (T) Cast.asInt(scope, max.value(scope));
					break;
				case IType.FLOAT:
					maxVal = scope -> (T) Cast.asFloat(scope, max.value(scope));
					break;
				case IType.POINT:
					maxVal = scope -> (T) GamaPointFactory.castToPoint(scope, max.value(scope));
					break;
				case IType.DATE:
					maxVal = scope -> (T) GamaDateFactory.castToDate(scope, max.value(scope));
			}
		} else {
			maxVal = null;
		}
		if (step != null && step.isConst()) {
			switch (type.id()) {
				case IType.INT:
					stepVal = scope -> (Step) Cast.asInt(scope, step.value(scope));
					break;
				case IType.FLOAT:
					stepVal = scope -> (Step) Cast.asFloat(scope, step.value(scope));
					break;
				case IType.POINT:
					stepVal = scope -> (Step) GamaPointFactory.castToPoint(scope, step.value(scope));
					break;
				case IType.DATE:
					// Step for dates are durations expressed in seconds ?
					stepVal = scope -> (Step) Cast.asFloat(scope, step.value(scope));
			}
		} else {
			stepVal = null;
		}
	}

	/**
	 * Coerces a value to this variable's type and clamps it to the min/max range.
	 *
	 * <p>
	 * This method overrides {@link AttributeDeclaration#coerce} to add automatic value clamping. After type conversion,
	 * the value is checked against min and max constraints and silently clamped if it falls outside the valid range.
	 * </p>
	 *
	 * <p>
	 * The clamping is type-specific:
	 * <ul>
	 * <li>int: Uses integer comparison</li>
	 * <li>float: Uses floating-point comparison</li>
	 * <li>point: Uses component-wise comparison via smallerThan/biggerThan</li>
	 * <li>date: Uses temporal comparison</li>
	 * </ul>
	 *
	 * @param agent
	 *            the agent owning this variable
	 * @param scope
	 *            the current execution scope
	 * @param v
	 *            the value to coerce and clamp
	 * @return the coerced and clamped value
	 * @throws GamaRuntimeException
	 *             if the value cannot be converted or the type is unsupported
	 *
	 * @see AttributeDeclaration#coerce(IAgent, IScope, Object)
	 * @see #checkMinMax(IAgent, IScope, Integer)
	 * @see #checkMinMax(IAgent, IScope, Double)
	 * @see #checkMinMax(IAgent, IScope, IPoint)
	 * @see #checkMinMax(IAgent, IScope, IDate)
	 */
	@Override
	protected Object coerce(final IObject agent, final IScope scope, final Object v) throws GamaRuntimeException {
		final Object val = super.coerce(agent, scope, v);
		return switch (val) {
			case Integer i -> checkMinMax(agent, scope, i);
			case Double d -> checkMinMax(agent, scope, d);
			case IDate date -> checkMinMax(agent, scope, date);
			case IPoint point -> checkMinMax(agent, scope, point);
			default -> throw GamaRuntimeException.error("Impossible to create " + getName(), scope);
		};
	}

	/**
	 * Clamps an integer value to the defined min/max range.
	 *
	 * <p>
	 * If the value is less than min, returns min. If the value is greater than max, returns max. Otherwise returns the
	 * value unchanged.
	 * </p>
	 *
	 * <p>
	 * Min and max expressions are evaluated each time if they are non-constant, allowing for dynamic bounds.
	 * </p>
	 *
	 * @param agent
	 *            the agent owning this variable
	 * @param scope
	 *            the current execution scope
	 * @param f
	 *            the integer value to clamp
	 * @return the clamped integer value
	 * @throws GamaRuntimeException
	 *             if min/max evaluation fails
	 */
	protected Integer checkMinMax(final IObject agent, final IScope scope, final Integer f)
			throws GamaRuntimeException {
		if (min != null) {
			final Integer m = minVal == null ? Cast.asInt(scope, scope.evaluate(min, agent).getValue())
					: (Integer) minVal.run(scope);
			if (f < m) return m;
		}
		if (max != null) {
			final Integer m = maxVal == null ? Cast.asInt(scope, scope.evaluate(max, agent).getValue())
					: (Integer) maxVal.run(scope);
			if (f > m) return m;
		}
		return f;
	}

	/**
	 * Check min max.
	 *
	 * @param agent
	 *            the agent
	 * @param scope
	 *            the scope
	 * @param f
	 *            the f
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected Double checkMinMax(final IObject agent, final IScope scope, final Double f) throws GamaRuntimeException {
		if (min != null) {
			final Double fmin =
					minVal == null ? asFloat(scope, scope.evaluate(min, agent).getValue()) : (Double) minVal.run(scope);
			if (f < fmin) return fmin;
		}
		if (max != null) {
			final Double fmax = maxVal == null ? Cast.asFloat(scope, scope.evaluate(max, agent).getValue())
					: (Double) maxVal.run(scope);
			if (f > fmax) return fmax;
		}
		return f;
	}

	/**
	 * Check min max.
	 *
	 * @param agent
	 *            the agent
	 * @param scope
	 *            the scope
	 * @param f
	 *            the f
	 * @return the gama point
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected IPoint checkMinMax(final IObject agent, final IScope scope, final IPoint f) throws GamaRuntimeException {
		if (f == null) return null;
		if (min != null) {
			final IPoint fmin = (IPoint) (minVal == null
					? GamaPointFactory.castToPoint(scope, scope.evaluate(min, agent).getValue()) : minVal.run(scope));
			if (f.smallerThan(fmin)) return fmin;
		}
		if (max != null) {
			final IPoint fmax = (IPoint) (maxVal == null
					? GamaPointFactory.castToPoint(scope, scope.evaluate(max, agent).getValue()) : maxVal.run(scope));
			if (f.biggerThan(fmax)) return fmax;
		}
		return f;
	}

	/**
	 * Check min max.
	 *
	 * @param agent
	 *            the agent
	 * @param scope
	 *            the scope
	 * @param f
	 *            the f
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected IDate checkMinMax(final IObject agent, final IScope scope, final IDate f) throws GamaRuntimeException {
		if (f == null) return null;
		if (min != null) {
			final IDate fmin = (IDate) (minVal == null
					? GamaDateFactory.castToDate(scope, scope.evaluate(min, agent).getValue()) : minVal.run(scope));
			if (f.compareTo(fmin) < 0) return fmin;
		}
		if (max != null) {
			final IDate fmax = (IDate) (maxVal == null
					? GamaDateFactory.castToDate(scope, scope.evaluate(max, agent).getValue()) : maxVal.run(scope));
			if (f.compareTo(fmax) > 0) return fmax;
		}
		return f;
	}

	@Override
	public T getMinValue(final IScope scope) {
		return minVal == null ? null : minVal.run(scope);
	}

	@Override
	public T getMaxValue(final IScope scope) {
		return maxVal == null ? null : maxVal.run(scope);
	}

	@Override
	public Step getStepValue(final IScope scope) {
		return stepVal == null ? null : stepVal.run(scope);
	}

	@Override
	public boolean acceptsSlider(final IScope scope) {
		return min != null && max != null && step != null;
	}

}

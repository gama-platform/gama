package gaml.compiler.parameters;

import java.lang.reflect.Proxy;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.factories.IExpressionFactory;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.GAML;
import gama.api.gaml.symbols.IParameter.DisplayUnit;
import gama.api.gaml.types.GamaFloatType;
import gama.api.gaml.types.GamaStringType;
import gama.api.gaml.types.Types;
import gama.core.experiment.parameters.ExperimentParameter;
import gaml.compiler.expressions.ConstantExpression;
import gaml.compiler.expressions.UnitConstantExpression;
import gaml.compiler.descriptions.StringBasedExpressionDescription;

/** Standalone regression checks for unit metadata, independent of SWT and the running experiment. */
public final class ParameterUnitTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static ExperimentParameter parameter(final IExpression unit) {
		return parameter(unit, unit == null ? null : unit.serializeToGaml(false));
	}

	private static ExperimentParameter parameter(final IExpression unit, final String source) {
		final IVariableDescription[] variable = new IVariableDescription[1];
		final IModelDescription model = (IModelDescription) Proxy.newProxyInstance(
				IModelDescription.class.getClassLoader(), new Class<?>[] { IModelDescription.class },
				(p, method, args) -> {
					if (method.getName().equals("getAttribute")) return variable[0];
					throw new UnsupportedOperationException(method.getName());
				});
		variable[0] = (IVariableDescription) Proxy.newProxyInstance(IVariableDescription.class.getClassLoader(),
				new Class<?>[] { IVariableDescription.class }, (p, method, args) -> switch (method.getName()) {
					case "getGamlType" -> Types.FLOAT;
					case "getName" -> "Duration";
					case "getLitteral" -> IKeyword.VAR.equals(args[0]) ? "my_duration" : null;
					case "getFacetExpr" -> IKeyword.UNIT.equals(((String[]) args[0])[0]) ? unit : null;
					case "getFacet" -> unit != null && IKeyword.UNIT.equals(args[0])
							? new StringBasedExpressionDescription(source) : null;
					case "getModelDescription" -> model;
					case "getUnderlyingElement" -> null;
					case "isUnmodifiable", "isDefinedInExperiment", "hasFacet" -> false;
					default -> throw new UnsupportedOperationException(method.getName());
				});
		return new ExperimentParameter(variable[0]);
	}

	public static void main(final String[] args) {
		try {
			runChecks();
		} catch (final Throwable failure) {
			failure.printStackTrace();
			System.exit(1);
		}
		// GAMA's process-wide background services outlive the standalone checks.
		System.exit(0);
	}

	private static void runChecks() {
		Types.cache(Types.addRegularType("float", new GamaFloatType(Types.getBuiltInTypeManager()), "gama.api"));
		Types.cache(Types.addRegularType("string", new GamaStringType(Types.getBuiltInTypeManager()), "gama.api"));
		GAML.registerExpressionFactory((IExpressionFactory) Proxy.newProxyInstance(
				IExpressionFactory.class.getClassLoader(), new Class<?>[] { IExpressionFactory.class },
				(p, method, arguments) -> {
					if (method.getName().equals("createExpr") && arguments[0] instanceof String source) {
						return switch (source) {
							case "#km/#h" -> new ConstantExpression(1000d / 3600d, Types.FLOAT);
							case "#h" -> new ConstantExpression(3600d, Types.FLOAT);
							default -> null;
						};
					}
					if (method.getName().equals("createConst")) return new ConstantExpression(arguments[0], Types.FLOAT);
					throw new UnsupportedOperationException(method.getName());
				}));
		final var hours = parameter(new UnitConstantExpression(3600d, Types.FLOAT, "h", "", null));
		check(hours.getDisplayUnit(null).equals(new DisplayUnit("h", 3600d)), "Unit identity and factor are preserved");
		check(hours.getUnitLabel(null).equals("h"), "The label is not the evaluated number");
		hours.setValueNoCheckNoNotification(7200d);
		hours.getDisplayUnit(null);
		check(hours.value(null).equals(7200d), "Reading display metadata never converts the model value");

		final var copy = new ExperimentParameter(null, hours);
		check(copy.getDisplayUnit(null).equals(hours.getDisplayUnit(null)), "Parameter copies retain display units");
		copy.setUnitLabel("seconds");
		check(copy.getDisplayUnit(null).equals(new DisplayUnit("seconds", 1d)), "A label override disables conversion");

		final var label = parameter(new ConstantExpression("items", Types.STRING));
		check(label.getDisplayUnit(null).equals(new DisplayUnit("items", 1d)), "Text labels do not trigger conversion");
		check(parameter(new ConstantExpression("#h", Types.STRING)).getDisplayUnit(null)
				.equals(new DisplayUnit("h", 3600d)), "Numeric strings trigger conversion");
		final var compound = parameter(new ConstantExpression(1000d / 3600d, Types.FLOAT), "#km/#h");
		check(compound.getDisplayUnit(null).equals(new DisplayUnit("km/h", 1000d / 3600d)),
				"The original expression is retained even after constant folding");
		check(parameter(new ConstantExpression("#km/#h", Types.STRING)).getDisplayUnit(null)
				.equals(compound.getDisplayUnit(null)), "Quoted and unquoted numeric expressions have the same conversion");
		check(parameter(null).getDisplayUnit(null) == null, "Absent units do not expose generated bound descriptions");

		for (final double factor : new double[] { 0d, -1d, Double.NaN, Double.POSITIVE_INFINITY }) {
			final var invalid = parameter(new UnitConstantExpression(factor, Types.FLOAT, "invalid", "", null));
			try {
				invalid.getDisplayUnit(null);
				throw new AssertionError("Invalid factor accepted: " + factor);
			} catch (final GamaRuntimeException expected) {
				check(expected.getMessage().contains("Invalid display unit"), "Explicit invalid-factor diagnostic");
			}
		}
	}
}

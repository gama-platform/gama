/*******************************************************************************************************
 *
 * ChartOutputFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;

/**
 * Factory for creating chart output instances using registered or default chart engine.
 */
public class ChartOutputFactory {

	/** The active chart engine provider. Defaults to JFreeChart engine. */
	private static IChartEngine engine = ChartJFreeChartOutput::createChartOutput;

	/**
	 * Registers a custom chart engine.
	 *
	 * @param customEngine the chart engine provider to use
	 */
	public static void setEngine(final IChartEngine customEngine) {
		if (customEngine != null) { engine = customEngine; }
	}

	/**
	 * Gets the active chart engine.
	 *
	 * @return active chart engine
	 */
	public static IChartEngine getEngine() {
		return engine;
	}

	/**
	 * Creates a ChartOutput for the specified name and type expression.
	 *
	 * @param scope the current scope
	 * @param name the chart name
	 * @param typeexp the chart type expression
	 * @return created ChartOutput instance
	 */
	public static ChartOutput createChartOutput(final IScope scope, final String name, final IExpression typeexp) {
		return engine.createChartOutput(scope, name, typeexp);
	}

}

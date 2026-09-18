/*******************************************************************************************************
 *
 * IChartEngine.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
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
 * Pluggable interface for chart rendering engines in GAMA platform.
 */
@FunctionalInterface
public interface IChartEngine {

	/**
	 * Creates a ChartOutput instance for the given chart name and GAML type expression.
	 *
	 * @param scope the scope
	 * @param name chart name
	 * @param typeexp expression defining chart type (e.g. "series", "histogram", "pie", "xy", etc.)
	 * @return ChartOutput implementation instance
	 */
	ChartOutput createChartOutput(IScope scope, String name, IExpression typeexp);

}

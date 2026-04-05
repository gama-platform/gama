/*******************************************************************************************************
 *
 * IChartDataSource.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

import gama.api.runtime.scope.IScope;

/**
 *
 */
public interface IChartDataSource {

	/** The Constant DATA_TYPE_NULL. */
	int DATA_TYPE_NULL = 0;
	/** The Constant DATA_TYPE_DOUBLE. */
	int DATA_TYPE_DOUBLE = 1;
	/** The Constant DATA_TYPE_LIST_DOUBLE_12. */
	int DATA_TYPE_LIST_DOUBLE_12 = 2;
	/** The Constant DATA_TYPE_LIST_DOUBLE_3. */
	int DATA_TYPE_LIST_DOUBLE_3 = 3;
	/** The Constant DATA_TYPE_LIST_DOUBLE_N. */
	int DATA_TYPE_LIST_DOUBLE_N = 4;
	/** The Constant DATA_TYPE_LIST_LIST_DOUBLE_12. */
	int DATA_TYPE_LIST_LIST_DOUBLE_12 = 5;
	/** The Constant DATA_TYPE_LIST_LIST_DOUBLE_3. */
	int DATA_TYPE_LIST_LIST_DOUBLE_3 = 6;
	/** The Constant DATA_TYPE_LIST_LIST_DOUBLE_N. */
	int DATA_TYPE_LIST_LIST_DOUBLE_N = 7;
	/** The Constant DATA_TYPE_LIST_LIST_LIST_DOUBLE. */
	int DATA_TYPE_LIST_LIST_LIST_DOUBLE = 8;
	/** The Constant DATA_TYPE_POINT. */
	int DATA_TYPE_POINT = 9;
	/** The Constant DATA_TYPE_LIST_POINT. */
	int DATA_TYPE_LIST_POINT = 10;
	/** The Constant DATA_TYPE_LIST_LIST_POINT. */
	int DATA_TYPE_LIST_LIST_POINT = 11;
	/** The Constant DATA_TYPE_MATRIX_DOUBLE. */
	int DATA_TYPE_MATRIX_DOUBLE = 12;
	/** The Constant DATA_TYPE_MATRIX_POINT. */
	int DATA_TYPE_MATRIX_POINT = 13;
	/** The Constant DATA_TYPE_MATRIX_LIST_DOUBLE. */
	int DATA_TYPE_MATRIX_LIST_DOUBLE = 14;

	/**
	 * @param scope
	 * @param b
	 */
	void setCumulative(IScope scope, boolean b);

	/**
	 * @param scope
	 * @param b
	 */
	void setUseSize(IScope scope, boolean b);

	/**
	 * @param b
	 */
	void setUseXErrValues(boolean b);

	/**
	 * @param b
	 */
	void setisBoxAndWhiskerData(boolean b);

	/**
	 * @param scope
	 * @param b
	 */
	void setCumulativeY(IScope scope, boolean b);

}
/*******************************************************************************************************
 *
 * Distribution.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.math.BigDecimal;
import java.util.Arrays;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.misc.IContainer;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.gaml.operators.Maths;

/**
 * The Class Distribution.
 */
@SuppressWarnings ({ "rawtypes" })
public class Distribution {

	private static class ExponentialBinsResult {
		final double newminInt;
		final double step;
		final int twoExponent;
		final int startMultiplier;

		ExponentialBinsResult(final double newminInt, final double step, final int twoExponent, final int startMultiplier) {
			this.newminInt = newminInt;
			this.step = step;
			this.twoExponent = twoExponent;
			this.startMultiplier = startMultiplier;
		}
	}

	private static ExponentialBinsResult computeExponentialBins(final double[] doublelist, final int nbBarres) {
		final double[] sortedList = doublelist.clone();
		Arrays.sort(sortedList);
		final double min = sortedList[0];
		final double max = sortedList[sortedList.length - 1];

		if (min == max) {
			return new ExponentialBinsResult(min, Math.pow(2, 0), 0, (int) min);
		}

		final float minInt = (float) min;
		final float maxInt = (float) max;
		double N = Math.log10((maxInt - minInt) / (double) (nbBarres - 1)) / Math.log10(2);
		int twoExponent = (int) N;
		double step = Math.pow(2, twoExponent);
		double newminInt = step * (int) (minInt / step);
		int startMultiplier = (int) (minInt / step);

		if (newminInt > min) {
			newminInt = step * (int) (minInt / step - 1);
			startMultiplier = (int) (minInt / step - 1);
		}
		if (newminInt + nbBarres * step <= max) {
			N = N + 1;
			twoExponent = (int) N;
			step = Math.pow(2, twoExponent);
			newminInt = step * (int) (minInt / step);
			startMultiplier = (int) (minInt / step);
			if (newminInt > min) {
				newminInt = step * (int) (minInt / step - 1);
				startMultiplier = (int) (minInt / step - 1);
			}
		}

		return new ExponentialBinsResult(newminInt, step, twoExponent, startMultiplier);
	}

	private static double[] buildThresholdsAndLegend(final double minVal, final double step, final int nbBarres, final String[] legend) {
		final double[] thresholds = new double[nbBarres + 1];
		double preval = minVal;
		double postval = 0;
		for (int i = 0; i < nbBarres; i++) {
			thresholds[i] = preval;
			postval = preval;
			preval = preval + step;
			legend[i] = "[" + postval + ":" + preval + "]";
		}
		return thresholds;
	}

	private static int[][] populate2dDistribution(final int nbBarresx, final int nbBarresy, final int len,
			final double[] doublelistorx, final double[] doublelistory,
			final double[] thresholdsx, final double[] thresholdsy) {
		final int[][] distribInts = new int[nbBarresx][nbBarresy];
		int nx, ny;
		for (int k = 0; k < len; k++) {
			nx = 0;
			ny = 0;
			while (thresholdsx[nx + 1] < doublelistorx[k] && nx + 2 < nbBarresx) {
				nx++;
			}
			while (thresholdsy[ny + 1] < doublelistory[k] && ny + 2 < nbBarresy) {
				ny++;
			}
			distribInts[nx][ny]++;
		}
		return distribInts;
	}

	private static IMap<String, Object> build2dResult(final IScope scope, final int nbBarresx, final int[][] distribInts,
			final int[] distribParamsx, final String[] distribLegendx,
			final int[] distribParamsy, final String[] distribLegendy) {
		final IList[] mytlist = new IList[nbBarresx];
		for (int i = 0; i < nbBarresx; i++) {
			mytlist[i] = GamaListFactory.create(scope, Types.INT, distribInts[i]);
		}
		final IList vallist = GamaListFactory.create(scope, Types.LIST, mytlist);

		final IMap<String, Object> result = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		result.addValueAtIndex(scope, "values", vallist);
		result.addValueAtIndex(scope, "legendx", GamaListFactory.create(scope, Types.STRING, distribLegendx));
		result.addValueAtIndex(scope, "parlistx", GamaListFactory.create(scope, Types.INT, distribParamsx));
		result.addValueAtIndex(scope, "legendy", GamaListFactory.create(scope, Types.STRING, distribLegendy));
		result.addValueAtIndex(scope, "parlisty", GamaListFactory.create(scope, Types.INT, distribParamsy));
		return result;
	}

	/**
	 * Compute distrib 2 d.
	 *
	 * @param scope the scope
	 * @param lvaluex the lvaluex
	 * @param lvaluey the lvaluey
	 * @param nbBarresx the nb barresx
	 * @param vminx the vminx
	 * @param vmaxx the vmaxx
	 * @param nbBarresy the nb barresy
	 * @param vminy the vminy
	 * @param vmaxy the vmaxy
	 * @return the i map
	 */
	public static IMap computeDistrib2d(final IScope scope, final IList lvaluex, final IList lvaluey,
			final int nbBarresx, final double vminx, final double vmaxx, final int nbBarresy, final double vminy,
			final double vmaxy) {
		int len = lvaluex.length(scope);
		final int leny = lvaluey.length(scope);
		len = Math.min(len, leny);
		final double[] doublelistorx = new double[len];
		final double[] doublelistory = new double[len];
		final String[] distribLegendx = new String[nbBarresx];
		final String[] distribLegendy = new String[nbBarresy];

		for (int i = 0; i < len; i++) {
			doublelistorx[i] = Cast.asFloat(scope, lvaluex.get(i));
			doublelistory[i] = Cast.asFloat(scope, lvaluey.get(i));
		}

		final double stepx = (vmaxx - vminx) / nbBarresx;
		final double stepy = (vmaxy - vminy) / nbBarresy;

		final double[] thresholdsx = buildThresholdsAndLegend(vminx, stepx, nbBarresx, distribLegendx);
		final double[] thresholdsy = buildThresholdsAndLegend(vminy, stepy, nbBarresy, distribLegendy);

		final int[][] distribInts = populate2dDistribution(nbBarresx, nbBarresy, len, doublelistorx, doublelistory, thresholdsx, thresholdsy);

		return build2dResult(scope, nbBarresx, distribInts, new int[] { 0, 0 }, distribLegendx, new int[] { 0, 0 }, distribLegendy);
	}

	/**
	 * Compute distrib 2 d.
	 *
	 * @param scope the scope
	 * @param lvaluex the lvaluex
	 * @param lvaluey the lvaluey
	 * @param nbBarresx the nb barresx
	 * @param nbBarresy the nb barresy
	 * @return the i map
	 */
	public static IMap computeDistrib2d(final IScope scope, final IList lvaluex, final IList lvaluey,
			final int nbBarresx, final int nbBarresy) {
		int len = lvaluex.length(scope);
		final int leny = lvaluey.length(scope);
		len = Math.min(len, leny);
		final double[] doublelistorx = new double[len];
		final double[] doublelistory = new double[len];
		final String[] distribLegendx = new String[nbBarresx];
		final String[] distribLegendy = new String[nbBarresy];

		for (int i = 0; i < len; i++) {
			doublelistorx[i] = Cast.asFloat(scope, lvaluex.get(i));
			doublelistory[i] = Cast.asFloat(scope, lvaluey.get(i));
		}

		final ExponentialBinsResult resX = computeExponentialBins(doublelistorx, nbBarresx);
		final ExponentialBinsResult resY = computeExponentialBins(doublelistory, nbBarresy);

		final double[] thresholdsx = buildThresholdsAndLegend(resX.newminInt, resX.step, nbBarresx, distribLegendx);
		final double[] thresholdsy = buildThresholdsAndLegend(resY.newminInt, resY.step, nbBarresy, distribLegendy);

		final int[][] distribInts = populate2dDistribution(nbBarresx, nbBarresy, len, doublelistorx, doublelistory, thresholdsx, thresholdsy);

		return build2dResult(scope, nbBarresx, distribInts,
				new int[] { resX.twoExponent, resX.startMultiplier }, distribLegendx,
				new int[] { resY.twoExponent, resY.startMultiplier }, distribLegendy);
	}

	/**
	 * Distribution 2 d of.
	 *
	 * @param scope the scope
	 * @param valuesx the valuesx
	 * @param valuesy the valuesy
	 * @param nbbarsx the nbbarsx
	 * @param nbbarsy the nbbarsy
	 * @return the i map
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = { "distribution2d_of" },
			can_be_const = false,
			index_type = IType.STRING,
			content_type = IType.LIST,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC, IConcept.CHART })
	@doc (
			value = "Discretize two lists of values into n bins (computes the bins from a numerical variable into n (default 10) bins. Returns a distribution map with the values (values key), the interval legends (legend key), the distribution parameters (params keys, for cumulative charts).",
			comment = "",
			examples = { @example (
					value = "distribution2d_of([1,1,2,12.5],10)",
					equals = "map(['values'::[2,1,0,0,0,0,1,0,0,0],'legend'::['[0.0:2.0]','[2.0:4.0]','[4.0:6.0]','[6.0:8.0]','[8.0:10.0]','[10.0:12.0]','[12.0:14.0]','[14.0:16.0]','[16.0:18.0]','[18.0:20.0]'],'parlist'::[1,0]])",
					isExecutable = false) },
			see = "as_map")
	@no_test
	public static IMap Distribution2dOf(final IScope scope, final IContainer valuesx, final IContainer valuesy,
			final Integer nbbarsx, final Integer nbbarsy) throws GamaRuntimeException {

		if (valuesx == null) { return GamaMapFactory.create(Types.STRING, Types.LIST); }
		final IList lvaluex = GamaListFactory.castToList(scope, valuesx);
		if (lvaluex.length(scope) < 1) { return GamaMapFactory.create(Types.STRING, Types.LIST); }

		int nbBarresx = 10;
		nbBarresx = nbbarsx.intValue();

		final IList lvaluey = GamaListFactory.castToList(scope, valuesy);
		if (lvaluey.length(scope) < 1) { return GamaMapFactory.create(Types.STRING, Types.LIST); }

		int nbBarresy = 10;
		nbBarresy = nbbarsy.intValue();

		return computeDistrib2d(scope, lvaluex, lvaluey, nbBarresx, nbBarresy);

	}

	/**
	 * Distribution 2 d of.
	 *
	 * @param scope the scope
	 * @param valuesx the valuesx
	 * @param valuesy the valuesy
	 * @param nbbarsx the nbbarsx
	 * @param startvaluex the startvaluex
	 * @param endvaluex the endvaluex
	 * @param nbbarsy the nbbarsy
	 * @param startvaluey the startvaluey
	 * @param endvaluey the endvaluey
	 * @return the i map
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = { "distribution2d_of" },
			can_be_const = false,
			index_type = IType.STRING,
			content_type = IType.LIST,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC, IConcept.CHART })
	@doc (
			value = "Discretize two lists of values into n bins (computes the bins from a numerical variable into n (default 10) bins. Returns a distribution map with the values (values key), the interval legends (legend key), the distribution parameters (params keys, for cumulative charts).",
			comment = "",
			examples = { @example (
					value = "distribution2d_of([1,1,2,12.5],10)",
					equals = "map(['values'::[2,1,0,0,0,0,1,0,0,0],'legend'::['[0.0:2.0]','[2.0:4.0]','[4.0:6.0]','[6.0:8.0]','[8.0:10.0]','[10.0:12.0]','[12.0:14.0]','[14.0:16.0]','[16.0:18.0]','[18.0:20.0]'],'parlist'::[1,0]])",
					isExecutable = false) },
			see = "as_map")
	@no_test
	public static IMap Distribution2dOf(final IScope scope, final IContainer valuesx, final IContainer valuesy,
			final Integer nbbarsx, final Double startvaluex, final Double endvaluex, final Integer nbbarsy,
			final Double startvaluey, final Double endvaluey) throws GamaRuntimeException {

		if (valuesx == null) { return GamaMapFactory.create(Types.STRING, Types.LIST); }
		final IList lvaluex = GamaListFactory.castToList(scope, valuesx);
		if (lvaluex.length(scope) < 1) { return GamaMapFactory.create(Types.STRING, Types.LIST); }

		int nbBarresx = 10;
		nbBarresx = nbbarsx.intValue();

		final IList lvaluey = GamaListFactory.castToList(scope, valuesy);
		if (lvaluey.length(scope) < 1) { return GamaMapFactory.create(Types.STRING, Types.LIST); }

		int nbBarresy = 10;
		nbBarresy = nbbarsy.intValue();
		double vminx = 0.0d;
		vminx = startvaluex.doubleValue();
		double vmaxx = 1.0d;
		vmaxx = endvaluex.doubleValue();
		double vminy = 0.0d;
		vminy = startvaluey.doubleValue();
		double vmaxy = 1.0d;
		vmaxy = endvaluey.doubleValue();

		return computeDistrib2d(scope, lvaluex, lvaluey, nbBarresx, vminx, vmaxx, nbBarresy, vminy, vmaxy);

	}

	/**
	 * Distribution 2 d of.
	 *
	 * @param scope the scope
	 * @param valuesx the valuesx
	 * @param valuesy the valuesy
	 * @return the i map
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = { "distribution2d_of" },
			can_be_const = false,
			index_type = IType.STRING,
			content_type = IType.LIST,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC, IConcept.CHART })
	@doc (
			value = "Discretize two lists of values into n bins (computes the bins from a numerical variable into n (default 10) bins. Returns a distribution map with the values (values key), the interval legends (legend key), the distribution parameters (params keys, for cumulative charts). Parameters can be (list), (list, nbbins) or (list,nbbins,valmin,valmax)",
			masterDoc = true,
			comment = "",
			examples = { @example (
					value = "distribution2d_of([1,1,2,12.5])",
					equals = "map(['values'::[2,1,0,0,0,0,1,0,0,0],'legend'::['[0.0:2.0]','[2.0:4.0]','[4.0:6.0]','[6.0:8.0]','[8.0:10.0]','[10.0:12.0]','[12.0:14.0]','[14.0:16.0]','[16.0:18.0]','[18.0:20.0]'],'parlist'::[1,0]])",
					isExecutable = false) },
			see = "as_map")
	@no_test
	public static IMap Distribution2dOf(final IScope scope, final IContainer valuesx, final IContainer valuesy)
			throws GamaRuntimeException {

		if (valuesx == null) { return GamaMapFactory.create(Types.STRING, Types.LIST); }
		final IList lvaluex = GamaListFactory.castToList(scope, valuesx);
		if (lvaluex.length(scope) < 1) { return GamaMapFactory.create(Types.STRING, Types.LIST); }
		final IList lvaluey = GamaListFactory.castToList(scope, valuesy);
		if (lvaluey.length(scope) < 1) { return GamaMapFactory.create(Types.STRING, Types.LIST); }

		final int nbBarres = 10;

		return computeDistrib2d(scope, lvaluex, lvaluey, nbBarres, nbBarres);

	}

	/**
	 * Compute distrib.
	 *
	 * @param scope the scope
	 * @param lvalue the lvalue
	 * @param nbBarres the nb barres
	 * @return the i map
	 */
	public static IMap computeDistrib(final IScope scope, final IList lvalue, final int nbBarres) {
		final int len = lvalue.length(scope);
		final double[] doublelist = new double[lvalue.length(scope)];
		final int[] distribInts = new int[nbBarres];
		final int[] distribParams = new int[2];
		final String[] distribLegend = new String[nbBarres];

		for (int i = 0; i < lvalue.length(scope); i++) {
			doublelist[i] = Cast.asFloat(scope, lvalue.get(i));
		}
		Arrays.sort(doublelist);
		final double min = doublelist[0];
		final double max = doublelist[len - 1];
		int twoExponent = 0;
		int startMultiplier = 0;

		double newminInt = 0;
		double deuxpuissancek = 0;

		if (min == max) {
			twoExponent = 0;
			startMultiplier = (int) min;
			deuxpuissancek = (float) Math.pow(2, twoExponent);
			newminInt = (int) min;

		}

		else {

			final double intermin = min;
			final double intermax = max;

			final float minInt = (float) intermin;
			final float maxInt = (float) intermax;
			double N = Math.log10((maxInt - minInt) / (double) (nbBarres - 1)) / Math.log10(2);
			twoExponent = (int) N;
			deuxpuissancek = (float) Math.pow(2, twoExponent);
			newminInt = deuxpuissancek * (int) (minInt / deuxpuissancek);
			startMultiplier = (int) (minInt / deuxpuissancek);
			if (newminInt > min) {
				newminInt = deuxpuissancek * (int) (minInt / deuxpuissancek - 1);
				startMultiplier = (int) (minInt / deuxpuissancek - 1);
			}
			if (newminInt + nbBarres * deuxpuissancek <= max) {
				N = N + 1;
				twoExponent = (int) N;
				deuxpuissancek = (float) Math.pow(2, twoExponent);
				newminInt = deuxpuissancek * (int) (minInt / deuxpuissancek);
				startMultiplier = (int) (minInt / deuxpuissancek);
				if (newminInt > min) {
					newminInt = deuxpuissancek * (int) (minInt / deuxpuissancek - 1);
					startMultiplier = (int) (minInt / deuxpuissancek - 1);
				}
			}

		}

		double preval = newminInt;
		double postval = 0;
		int nba = 0;
		int nbaprec = 0;
		for (int i = 0; i < nbBarres; i++) {
			if (i != 0) {
				preval = preval + deuxpuissancek;
			}
			postval = preval + deuxpuissancek;
			while (nba < len && doublelist[nba] < postval) {
				nba++;
			}

			distribInts[i] = nba - nbaprec;
			nbaprec = nba;
			distribLegend[i] = "[" + preval + ":" + postval + "]";
		}

		distribParams[0] = twoExponent;
		distribParams[1] = startMultiplier;

		final IMap<String, Object> result = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		final IList vallist = GamaListFactory.create(scope, Types.INT, distribInts);
		final IList parlist = GamaListFactory.create(scope, Types.INT, distribParams);
		final IList leglist = GamaListFactory.create(scope, Types.STRING, distribLegend);
		result.addValueAtIndex(scope, "values", vallist);
		result.addValueAtIndex(scope, "legend", leglist);
		result.addValueAtIndex(scope, "parlist", parlist);

		return result;

	}

	/**
	 * Compute distrib.
	 *
	 * @param scope the scope
	 * @param lvalue the lvalue
	 * @param nbBarres the nb barres
	 * @param vmin the vmin
	 * @param vmax the vmax
	 * @return the i map
	 */
	public static IMap computeDistrib(final IScope scope, final IList lvalue, final int nbBarres, final double vmin,
			final double vmax) {
		final int len = lvalue.length(scope);
		final double[] doublelist = new double[lvalue.length(scope)];

		final int[] distribInts = new int[nbBarres];
		final int[] distribParams = new int[2];
		final String[] distribLegend = new String[nbBarres];

		final double deuxpuissancek = (vmax - vmin) / nbBarres;
		final double newminInt = vmin;

		for (int i = 0; i < lvalue.length(scope); i++) {
			doublelist[i] = Cast.asFloat(scope, lvalue.get(i));
		}
		Arrays.sort(doublelist);

		final int scale = BigDecimal.valueOf(deuxpuissancek).scale();

		double preval = newminInt;
		double postval = 0;
		int nba = 0;
		int nbaprec = 0;
		for (int i = 0; i < nbBarres; i++) {
			if (i != 0) {
				preval = preval + deuxpuissancek;
			}
			postval = preval + deuxpuissancek;
			while (nba < len && doublelist[nba] < postval) {
				nba++;
			}

			distribInts[i] = nba - nbaprec;
			nbaprec = nba;
			distribLegend[i] = "[" + Maths.round(preval, scale + 8) + ":" + Maths.round(postval, scale + 8) + "]";

		}

		distribParams[0] = 0;
		distribParams[1] = 0;

		final IMap<String, Object> result = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		final IList vallist = GamaListFactory.create(scope, Types.INT, distribInts);
		final IList parlist = GamaListFactory.create(scope, Types.INT, distribParams);
		final IList leglist = GamaListFactory.create(scope, Types.STRING, distribLegend);
		result.addValueAtIndex(scope, "values", vallist);
		result.addValueAtIndex(scope, "legend", leglist);
		result.addValueAtIndex(scope, "parlist", parlist);

		return result;

	}

	/**
	 * Distribution of.
	 *
	 * @param scope the scope
	 * @param values the values
	 * @param nbbars the nbbars
	 * @return the i map
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = { "distribution_of" },
			can_be_const = false,
			index_type = IType.STRING,
			content_type = IType.LIST,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC, IConcept.CHART })
	@doc (
			value = "Discretize a list of values into n bins (computes the bins from a numerical variable into n (default 10) bins. Returns a distribution map with the values (values key), the interval legends (legend key), the distribution parameters (params keys, for cumulative charts).",
			comment = "",
			examples = { @example (
					value = "distribution_of([1,1,2,12.5],10)",
					equals = "map(['values'::[2,1,0,0,0,0,1,0,0,0],'legend'::['[0.0:2.0]','[2.0:4.0]','[4.0:6.0]','[6.0:8.0]','[8.0:10.0]','[10.0:12.0]','[12.0:14.0]','[14.0:16.0]','[16.0:18.0]','[18.0:20.0]'],'parlist'::[1,0]])",
					isExecutable = false) },
			see = "as_map")
	@no_test
	public static IMap DistributionOf(final IScope scope, final IContainer values, final Integer nbbars)
			throws GamaRuntimeException {

		if (values == null) { return GamaMapFactory.create(Types.STRING, Types.LIST); }
		final IList lvalue = GamaListFactory.castToList(scope, values);
		if (lvalue.length(scope) < 1) { return GamaMapFactory.create(Types.STRING, Types.LIST); }

		int nbBarres = 10;
		nbBarres = nbbars.intValue();

		return computeDistrib(scope, lvalue, nbBarres);

	}

	/**
	 * Distribution of.
	 *
	 * @param scope the scope
	 * @param values the values
	 * @return the i map
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = { "distribution_of" },
			can_be_const = false,
			index_type = IType.STRING,
			content_type = IType.LIST,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC, IConcept.CHART })
	@doc (
			value = "Discretize a list of values into n bins (computes the bins from a numerical variable into n (default 10) bins. Returns a distribution map with the values (values key), the interval legends (legend key), the distribution parameters (params keys, for cumulative charts). Parameters can be (list), (list, nbbins) or (list,nbbins,valmin,valmax)",
			masterDoc = true,
			comment = "",
			examples = { @example (
					value = "distribution_of([1,1,2,12.5])",
					equals = "map(['values'::[2,1,0,0,0,0,1,0,0,0],'legend'::['[0.0:2.0]','[2.0:4.0]','[4.0:6.0]','[6.0:8.0]','[8.0:10.0]','[10.0:12.0]','[12.0:14.0]','[14.0:16.0]','[16.0:18.0]','[18.0:20.0]'],'parlist'::[1,0]])",
					isExecutable = false) },
			see = "as_map")
	@no_test
	public static IMap DistributionOf(final IScope scope, final IContainer values) throws GamaRuntimeException {

		if (values == null) { return GamaMapFactory.create(Types.STRING, Types.LIST); }
		final IList lvalue = GamaListFactory.castToList(scope, values);
		if (lvalue.length(scope) < 1) { return GamaMapFactory.create(Types.STRING, Types.LIST); }

		final int nbBarres = 10;

		return computeDistrib(scope, lvalue, nbBarres);

	}

	/**
	 * Distribution of.
	 *
	 * @param scope the scope
	 * @param values the values
	 * @param nbbars the nbbars
	 * @param startvalue the startvalue
	 * @param endvalue the endvalue
	 * @return the i map
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = { "distribution_of" },
			can_be_const = false,
			index_type = IType.STRING,
			content_type = IType.LIST,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC, IConcept.CHART })
	@doc (
			value = "Discretize a list of values into n bins (computes the bins from a numerical variable into n (default 10) bins. Returns a distribution map with the values (values key), the interval legends (legend key), the distribution parameters (params keys, for cumulative charts).",
			masterDoc = false,
			comment = "",
			examples = { @example (
					value = "distribution_of([1,1,2,12.5])",
					equals = "map(['values'::[2,1,0,0,0,0,1,0,0,0],'legend'::['[0.0:2.0]','[2.0:4.0]','[4.0:6.0]','[6.0:8.0]','[8.0:10.0]','[10.0:12.0]','[12.0:14.0]','[14.0:16.0]','[16.0:18.0]','[18.0:20.0]'],'parlist'::[1,0]])",
					isExecutable = false) },
			see = "as_map")
	@no_test
	public static IMap DistributionOf(final IScope scope, final IContainer values, final Integer nbbars,
			final Double startvalue, final Double endvalue) throws GamaRuntimeException {

		if (values == null) { return GamaMapFactory.create(Types.STRING, Types.LIST); }
		final IList lvalue = GamaListFactory.castToList(scope, values);
		if (lvalue.length(scope) < 1) { return GamaMapFactory.create(Types.STRING, Types.LIST); }

		int nbBarres = 10;
		nbBarres = nbbars.intValue();
		double vmin = 0.0d;
		vmin = startvalue.doubleValue();
		double vmax = 1.0d;
		vmax = endvalue.doubleValue();

		return computeDistrib(scope, lvalue, nbBarres, vmin, vmax);

	}

}

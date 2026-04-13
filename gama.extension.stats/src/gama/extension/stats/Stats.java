/*******************************************************************************************************
 *
 * Stats.java, in gama.extension.stats, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.stats;

import static gama.gaml.operators.Containers.collect;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import com.google.common.collect.Ordering;

import cern.colt.list.DoubleArrayList;
import cern.jet.math.Arithmetic;
import cern.jet.stat.Descriptive;
import cern.jet.stat.Gamma;
import cern.jet.stat.Probability;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.annotations.validator;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.utils.StringUtils;
import gama.api.utils.collections.Collector;
import gama.api.utils.files.FileUtils;
import gama.core.util.matrix.GamaField;
import gama.gaml.operators.Containers;
import gama.gaml.operators.Containers.ComparableValidator;

/**
 * Written by drogoul Modified on 15 janv. 2011
 *
 * @todo Description
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class Stats {

	/**
	 * The Class DataSet.
	 */
	private static class DataSet {

		/** The Constant DEFAULT_CAPACITY. */
		private static final int DEFAULT_CAPACITY = 50;

		/** The Constant GROWTH_RATE. */
		private static final double GROWTH_RATE = 1.5d;

		/** The data set. */
		double[] dataSet;

		/** The data set size. */
		int size;

		/**
		 * Instantiates a new data set.
		 */
		DataSet() {
			this(DEFAULT_CAPACITY);
		}

		/**
		 * Instantiates a new data set.
		 *
		 * @param capacity
		 *            the capacity
		 */
		DataSet(final int capacity) {
			dataSet = new double[capacity];
			size = 0;
		}

		/**
		 * Adds the.
		 *
		 * @param value
		 *            the value
		 */
		void add(final double value) {
			if (size == dataSet.length) {
				double[] newDataSet = new double[(int) (dataSet.length * GROWTH_RATE)];
				System.arraycopy(dataSet, 0, newDataSet, 0, dataSet.length);
				dataSet = newDataSet;
			}
			dataSet[size++] = value;
		}

		/**
		 * Adds the all.
		 *
		 * @param scope
		 *            the scope
		 * @param container
		 *            the container
		 */
		void addAll(final IScope scope, final IContainer container) {
			for (final Object o : container.iterable(scope)) { add(Cast.asFloat(scope, o)); }
		}

		/**
		 * To array.
		 *
		 * @return the double[]
		 */
		double[] toArray() {
			if (size == dataSet.length) return dataSet;
			double[] result = new double[size];
			System.arraycopy(dataSet, 0, result, 0, size);
			return result;
		}

		/**
		 * To double array list.
		 *
		 * @return the double array list
		 */
		DoubleArrayList toDoubleArrayList() {
			return new DoubleArrayList(toArray());
		}

	}

	/**
	 * To double array.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the double[]
	 */
	public static double[] toDoubleArray(final IScope scope, final IContainer data) {
		final DataSet ds = new DataSet(data.length(scope));
		ds.addAll(scope, data);
		return ds.toArray();
	}

	/**
	 * To double array list.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the double array list
	 */
	public static DoubleArrayList toDoubleArrayList(final IScope scope, final IContainer data) {
		final DataSet ds = new DataSet(data.length(scope));
		ds.addAll(scope, data);
		return ds.toDoubleArrayList();
	}

	/**
	 * Binomial.
	 *
	 * @param scope
	 *            the scope
	 * @param n
	 *            the n
	 * @param k
	 *            the k
	 * @return the double
	 */
	@operator (
			value = "binomial",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the binomial coefficient n over k, which is n! / (k! * (n-k)!).",
			examples = { @example (
					value = "binomial(10, 2)",
					equals = "45.0") })
	public static Double opBinomial(final IScope scope, final Integer n, final int k) {
		return Arithmetic.binomial(n, k);
	}

	/**
	 * Op gamma.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @return the double
	 */
	@operator (
			value = "gamma",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the value of the gamma function at a.",
			examples = { @example (
					value = "gamma(5)",
					equals = "24.0") })
	public static Double opGamma(final IScope scope, final Double a) {
		return Gamma.gamma(a);
	}

	/**
	 * Op log gamma.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @return the double
	 */
	@operator (
			value = "log_gamma",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the value of the log-gamma function at a.",
			examples = { @example (
					value = "log_gamma(5)",
					equals = "3.178053830347945") })
	public static Double opLogGamma(final IScope scope, final Double a) {
		return Gamma.logGamma(a);
	}

	/**
	 * Incomplete beta.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "incomplete_beta",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the value of the incomplete beta function with parameters a and b at x.",
			examples = { @example (
					value = "incomplete_beta(0.5, 0.5, 0.5)",
					equals = "0.5") })
	public static Double opIncompleteBeta(final IScope scope, final Double a, final Double b, final Double x) {
		return Probability.incompleteBeta(a, b, x);
	}

	/**
	 * Incomplete gamma.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "incomplete_gamma",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the value of the incomplete gamma function with parameter a at x.",
			examples = { @example (
					value = "incomplete_gamma(0.5, 0.5)",
					equals = "0.6826894921370859") })
	public static Double opIncompleteGamma(final IScope scope, final Double a, final Double x) {
		return Probability.incompleteGamma(a, x);
	}

	/**
	 * Incomplete gamma complement.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "incomplete_gamma_complement",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the value of the complement of the incomplete gamma function with parameter a at x.",
			examples = { @example (
					value = "incomplete_gamma_complement(0.5, 0.5)",
					equals = "0.3173105078629141") })
	public static Double opIncompleteGammaComplement(final IScope scope, final Double a, final Double x) {
		return Probability.incompleteGammaComplement(a, x);
	}

	/**
	 * Normal area.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "normal_area",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the area under the normal distribution curve from minus infinity to x.",
			examples = { @example (
					value = "normal_area(0.0)",
					equals = "0.5") })
	public static Double opNormalArea(final IScope scope, final Double x) {
		return Probability.normal(x);
	}

	/**
	 * Normal inverse.
	 *
	 * @param scope
	 *            the scope
	 * @param area
	 *            the area
	 * @return the double
	 */
	@operator (
			value = "normal_inverse",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the value x such that the area under the normal distribution curve from minus infinity to x is equal to the given area.",
			examples = { @example (
					value = "normal_inverse(0.5)",
					equals = "0.0") })
	public static Double opNormalInverse(final IScope scope, final Double area) {
		return Probability.normalInverse(area);
	}

	/**
	 * Chi square.
	 *
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "chi_square",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the area under the chi-square distribution curve with v degrees of freedom from zero to x.",
			examples = { @example (
					value = "chi_square(2, 5.991)",
					equals = "0.9499849971432544") })
	public static Double opChiSquare(final IScope scope, final Double v, final Double x) {
		return Probability.chiSquare(v, x);
	}

	/**
	 * Chi square inverse.
	 *
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 * @param area
	 *            the area
	 * @return the double
	 */
	@operator (
			value = "chi_square_inverse",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the value x such that the area under the chi-square distribution curve with v degrees of freedom from zero to x is equal to the given area.",
			examples = { @example (
					value = "chi_square_inverse(2, 0.95)",
					equals = "5.991464547107983") })
	public static Double opChiSquareInverse(final IScope scope, final Double v, final Double area) {
		return Probability.chiSquareInverse(v, area);
	}

	/**
	 * F distribution.
	 *
	 * @param scope
	 *            the scope
	 * @param v1
	 *            the v 1
	 * @param v2
	 *            the v 2
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "f_distribution",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the area under the F-distribution curve with v1 and v2 degrees of freedom from zero to x.",
			examples = { @example (
					value = "f_distribution(2, 2, 19.0)",
					equals = "0.95") })
	public static Double opFDistribution(final IScope scope, final Double v1, final Double v2, final Double x) {
		return Probability.fDistribution(v1, v2, x);
	}

	/**
	 * F distribution inverse.
	 *
	 * @param scope
	 *            the scope
	 * @param v1
	 *            the v 1
	 * @param v2
	 *            the v 2
	 * @param area
	 *            the area
	 * @return the double
	 */
	@operator (
			value = "f_distribution_inverse",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the value x such that the area under the F-distribution curve with v1 and v2 degrees of freedom from zero to x is equal to the given area.",
			examples = { @example (
					value = "f_distribution_inverse(2, 2, 0.95)",
					equals = "19.00000000000003") })
	public static Double opFDistributionInverse(final IScope scope, final Double v1, final Double v2,
			final Double area) {
		return Probability.fDistributionInverse(v1, v2, area);
	}

	/**
	 * Student T.
	 *
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "student_t",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the area under the Student's T-distribution curve with v degrees of freedom from minus infinity to x.",
			examples = { @example (
					value = "student_t(2, 2.92)",
					equals = "0.9500054484347245") })
	public static Double opStudentT(final IScope scope, final Double v, final Double x) {
		return Probability.studentT(v, x);
	}

	/**
	 * Student T inverse.
	 *
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 * @param area
	 *            the area
	 * @return the double
	 */
	@operator (
			value = "student_t_inverse",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the value x such that the area under the Student's T-distribution curve with v degrees of freedom from minus infinity to x is equal to the given area.",
			examples = { @example (
					value = "student_t_inverse(2, 0.95)",
					equals = "2.919985580355516") })
	public static Double opStudentTInverse(final IScope scope, final Double v, final Double area) {
		return Probability.studentTInverse(v, area);
	}

	/**
	 * Mean.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the double
	 */
	@operator (
			value = "mean",
			can_be_const = true,
			type = IType.FLOAT,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the mean of the elements of the operand.",
			examples = { @example (
					value = "mean([1, 2, 3, 4])",
					equals = "2.5") })
	public static Double opMean(final IScope scope, final IContainer data) {
		if (data == null || data.isEmpty(scope)) return 0.0;
		return Descriptive.mean(toDoubleArrayList(scope, data));
	}

	/**
	 * Geometric mean.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the double
	 */
	@operator (
			value = "geometric_mean",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the geometric mean of the elements of the operand.",
			examples = { @example (
					value = "geometric_mean([1, 2, 3, 4])",
					equals = "2.213363839400643") })
	public static Double opGeometricMean(final IScope scope, final IContainer data) {
		if (data == null || data.isEmpty(scope)) return 0.0;
		return Descriptive.geometricMean(toDoubleArrayList(scope, data));
	}

	/**
	 * Harmonic mean.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the double
	 */
	@operator (
			value = "harmonic_mean",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the harmonic mean of the elements of the operand.",
			examples = { @example (
					value = "harmonic_mean([1, 2, 3, 4])",
					equals = "1.9200000000000004") })
	public static Double opHarmonicMean(final IScope scope, final IContainer data) {
		if (data == null || data.isEmpty(scope)) return 0.0;
		return Descriptive.harmonicMean(data.length(scope), Descriptive.sumOfInversions(toDoubleArrayList(scope, data),
				0, data.length(scope) - 1));
	}

	/**
	 * Median.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the double
	 */
	@operator (
			value = "median",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the median of the elements of the operand.",
			examples = { @example (
					value = "median([1, 2, 3, 4])",
					equals = "2.5") })
	public static Double opMedian(final IScope scope, final IContainer data) {
		if (data == null || data.isEmpty(scope)) return 0.0;
		final DoubleArrayList list = toDoubleArrayList(scope, data);
		list.sort();
		return Descriptive.median(list);
	}

	/**
	 * Variance.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the double
	 */
	@operator (
			value = "variance",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the variance of the elements of the operand.",
			examples = { @example (
					value = "variance([1, 2, 3, 4])",
					equals = "1.25") })
	public static Double opVariance(final IScope scope, final IContainer data) {
		if (data == null || data.isEmpty(scope)) return 0.0;
		final DoubleArrayList list = toDoubleArrayList(scope, data);
		return Descriptive.sampleVariance(list, Descriptive.mean(list)) * (list.size() - 1) / list.size();
	}

	/**
	 * Standard deviation.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the double
	 */
	@operator (
			value = "standard_deviation",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the standard deviation of the elements of the operand.",
			examples = { @example (
					value = "standard_deviation([1, 2, 3, 4])",
					equals = "1.118033988749895") })
	public static Double opStandardDeviation(final IScope scope, final IContainer data) {
		if (data == null || data.isEmpty(scope)) return 0.0;
		return Math.sqrt(opVariance(scope, data));
	}

	/**
	 * Correlation.
	 *
	 * @param scope
	 *            the scope
	 * @param data1
	 *            the data 1
	 * @param data2
	 *            the data 2
	 * @return the double
	 */
	@operator (
			value = "correlation",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the correlation of the elements of the two operands.",
			examples = { @example (
					value = "correlation([1, 2, 3, 4], [1, 2, 3, 4])",
					equals = "1.0") })
	public static Double opCorrelation(final IScope scope, final IContainer data1, final IContainer data2) {
		if (data1 == null || data1.isEmpty(scope) || data2 == null || data2.isEmpty(scope)) return 0.0;
		final DoubleArrayList list1 = toDoubleArrayList(scope, data1);
		final DoubleArrayList list2 = toDoubleArrayList(scope, data2);
		return Descriptive.correlation(list1, Descriptive.standardDeviation(Descriptive.sampleVariance(list1,
				Descriptive.mean(list1))), list2, Descriptive.standardDeviation(Descriptive.sampleVariance(list2,
						Descriptive.mean(list2))));
	}

	/**
	 * Covariance.
	 *
	 * @param scope
	 *            the scope
	 * @param data1
	 *            the data 1
	 * @param data2
	 *            the data 2
	 * @return the double
	 */
	@operator (
			value = "covariance",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the covariance of the elements of the two operands.",
			examples = { @example (
					value = "covariance([1, 2, 3, 4], [1, 2, 3, 4])",
					equals = "1.25") })
	public static Double opCovariance(final IScope scope, final IContainer data1, final IContainer data2) {
		if (data1 == null || data1.isEmpty(scope) || data2 == null || data2.isEmpty(scope)) return 0.0;
		final DoubleArrayList list1 = toDoubleArrayList(scope, data1);
		final DoubleArrayList list2 = toDoubleArrayList(scope, data2);
		return Descriptive.covariance(list1, list2) * (list1.size() - 1) / list1.size();
	}

	/**
	 * T test.
	 *
	 * @param scope
	 *            the scope
	 * @param data1
	 *            the data 1
	 * @param data2
	 *            the data 2
	 * @return the double
	 */
	@operator (
			value = "t_test",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns the p-value of a two-sample t-test.",
			examples = { @example (
					value = "t_test([1, 2, 3, 4], [1, 2, 3, 4])",
					equals = "1.0") })
	public static Double opTTest(final IScope scope, final IContainer data1, final IContainer data2) {
		if (data1 == null || data1.isEmpty(scope) || data2 == null || data2.isEmpty(scope)) return 1.0;
		return new TTest().tTest(toDoubleArray(scope, data1), toDoubleArray(scope, data2));
	}

	/**
	 * K-Means clustering.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @param k
	 *            the k
	 * @return the list of clusters
	 */
	@operator (
			value = "kmeans",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns a list of clusters using the K-Means algorithm.",
			examples = { @example (
					value = "kmeans([{1, 2}, {3, 4}, {10, 10}], 2)",
					equals = "[[{1.0, 2.0}, {3.0, 4.0}], [{10.0, 10.0}]]") })
	public static IList<IList<IPoint>> opKMeans(final IScope scope, final IContainer data, final Integer k) {
		if (data == null || data.isEmpty(scope)) return GamaListFactory.create();
		final List<DoublePoint> points = new ArrayList<>();
		for (final Object o : data.iterable(scope)) {
			final IPoint p = Cast.asPoint(scope, o);
			points.add(new DoublePoint(new double[] { p.getX(), p.getY(), p.getZ() }));
		}
		final KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(k);
		final List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);
		final IList<IList<IPoint>> result = GamaListFactory.create(Types.LIST.of(Types.POINT));
		for (final CentroidCluster<DoublePoint> cluster : clusters) {
			final IList<IPoint> c = GamaListFactory.create(Types.POINT);
			for (final DoublePoint dp : cluster.getPoints()) {
				final double[] p = dp.getPoint();
				c.add(GamaPointFactory.create(p[0], p[1], p[2]));
			}
			result.add(c);
		}
		return result;
	}

	/**
	 * DBSCAN clustering.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @param eps
	 *            the eps
	 * @param minPts
	 *            the min pts
	 * @return the list of clusters
	 */
	@operator (
			value = "dbscan",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Returns a list of clusters using the DBSCAN algorithm.",
			examples = { @example (
					value = "dbscan([{1, 2}, {1.1, 2.1}, {10, 10}], 0.5, 2)",
					equals = "[[{1.0, 2.0}, {1.1, 2.1}]]") })
	public static IList<IList<IPoint>> opDBSCAN(final IScope scope, final IContainer data, final Double eps,
			final Integer minPts) {
		if (data == null || data.isEmpty(scope)) return GamaListFactory.create();
		final List<DoublePoint> points = new ArrayList<>();
		for (final Object o : data.iterable(scope)) {
			final IPoint p = Cast.asPoint(scope, o);
			points.add(new DoublePoint(new double[] { p.getX(), p.getY(), p.getZ() }));
		}
		final DBSCANClusterer<DoublePoint> clusterer = new DBSCANClusterer<>(eps, minPts);
		final List<Cluster<DoublePoint>> clusters = clusterer.cluster(points);
		final IList<IList<IPoint>> result = GamaListFactory.create(Types.LIST.of(Types.POINT));
		for (final Cluster<DoublePoint> cluster : clusters) {
			final IList<IPoint> c = GamaListFactory.create(Types.POINT);
			for (final DoublePoint dp : cluster.getPoints()) {
				final double[] p = dp.getPoint();
				c.add(GamaPointFactory.create(p[0], p[1], p[2]));
			}
			result.add(c);
		}
		return result;
	}

	/**
	 * OLS Regression.
	 *
	 * @param scope
	 *            the scope
	 * @param y
	 *            the y
	 * @param x
	 *            the x
	 * @return the regression model
	 */
	@operator (
			value = "regress",
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC, IConcept.REGRESSION })
	@doc (
			value = "Returns an OLS regression model.",
			examples = { @example (
					value = "regress([1, 2, 3, 4], [[1, 1], [1, 2], [1, 3], [1, 4]])",
					isExecutable = false) })
	public static GamaRegression opRegress(final IScope scope, final IContainer y, final IContainer x) {
		if (y == null || y.isEmpty(scope) || x == null || x.isEmpty(scope)) return null;
		final double[] yArray = toDoubleArray(scope, y);
		final double[][] xArray = new double[x.length(scope)][];
		int i = 0;
		for (final Object o : x.iterable(scope)) { xArray[i++] = toDoubleArray(scope, (IContainer) o); }
		final OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		regression.newSampleData(yArray, xArray);
		return new GamaRegression(regression);
	}

	/**
	 * Performs a Sobol sensitivity analysis on the provided data.
	 */
	@operator (
			value = "sobolAnalysis",
			type = IType.STRING,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return a string containing the Report of the sobol analysis for the corresponding data (path, map or matrix) and save this report in a txt/csv file.")
	public static String sobolAnalysis(final IScope scope, final Object data, final String report_path,
			final int nb_parameters) {
		final File f_report = new File(FileUtils.constructAbsoluteFilePath(scope, report_path, false));
		Sobol sob;
		if (data instanceof String path) {
			final File f = new File(FileUtils.constructAbsoluteFilePath(scope, path, false));
			sob = new Sobol(f, nb_parameters, scope);
		} else if (data instanceof IMap map) {
			sob = new Sobol(convertToDoubleMap(scope, map), nb_parameters, scope);
		} else if (data instanceof IMatrix matrix) {
			sob = new Sobol(matrixToMap(scope, matrix), nb_parameters, scope);
		} else
			throw GamaRuntimeException.error("sobolAnalysis expects a path (string), a map or a matrix", scope);

		sob.evaluate();
		sob.saveResult(f_report);
		return sob.buildReportString(FilenameUtils.getExtension(f_report.getPath()));
	}

	/**
	 * Performs a Morris sensitivity analysis on the provided data.
	 */
	@operator (
			value = "morrisAnalysis",
			type = IType.STRING,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return a string containing the Report of the morris analysis for the corresponding data (path, map or matrix)")
	public static String morrisAnalysis(final IScope scope, final Object data, final int nb_levels,
			final int nb_parameters) {
		Morris momo;
		String ext = "csv";
		if (data instanceof String path) {
			final File f = new File(FileUtils.constructAbsoluteFilePath(scope, path, false));
			momo = new Morris(f, nb_parameters, nb_levels, scope);
			ext = FilenameUtils.getExtension(path);
		} else if (data instanceof IMap map) {
			momo = new Morris(convertToDoubleMap(scope, map), nb_parameters, nb_levels, scope);
		} else if (data instanceof IMatrix matrix) {
			momo = new Morris(matrixToMap(scope, matrix), nb_parameters, nb_levels, scope);
		} else
			throw GamaRuntimeException.error("morrisAnalysis expects a path (string), a map or a matrix", scope);

		momo.evaluate();
		return momo.buildReportString(ext);
	}

	/**
	 * Performs a stochasticity analysis.
	 */
	@operator (
			value = "stochanalyse",
			type = IType.STRING,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return the result of the stochasticity analysis for the corresponding data (path, map or matrix)")
	public static String stochanalyse(final IScope scope, final int replicat, final double threshold, final Object data,
			final int nb_parameters) {

		if (data instanceof String path) {
			String new_path = scope.getExperiment().getWorkingPath() + "/" + path;
			return Stochanalysis.stochasticityAnalysis_From_CSV(replicat, threshold, new_path, nb_parameters, scope);
		}

		IMap<String, IList<Double>> mapData;
		if (data instanceof IMap m) {
			mapData = m;
		} else if (data instanceof IMatrix matrix) {
			mapData = matrixToMap(scope, matrix);
		} else
			throw GamaRuntimeException.error("stochanalyse expects a path (string), a map or a matrix", scope);

		int nbCols = mapData.size();
		int nbRows = mapData.values().iterator().next().size();
		List<String> listNames = new ArrayList<>(mapData.keySet());

		IList<IMap<String, Object>> MySample = GamaListFactory.create(Types.MAP);
		IMap<String, IList<Double>> Outputs = GamaMapFactory.create();

		for (int idx = 0; idx < nbCols; idx++) {
			String name = listNames.get(idx);
			if (idx >= nb_parameters) { Outputs.put(name, GamaListFactory.create()); }
		}

		for (int row = 0; row < nbRows; row++) {
			IMap<String, Object> temp_map = GamaMapFactory.create();
			for (int idx = 0; idx < nbCols; idx++) {
				String name = listNames.get(idx);
				Double val = Cast.asFloat(scope, mapData.get(name).get(row));
				if (idx < nb_parameters) {
					temp_map.put(name, val);
				} else {
					Outputs.get(name).add(val);
				}
			}
			MySample.add(temp_map);
		}

		return Stochanalysis.stochasticityAnalysis_From_Data(replicat, threshold, MySample, Outputs, scope);
	}

	@operator (
			value = "rolling_vc",
			type = IType.LIST,
			content_type = IType.FLOAT,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return the list of rolling coefficient of variance according to the number of observations, </br> i.e. value at index i is the coefficient of variance for the first i observations.")
	public static IList<Double> rollingVC(final IScope scope, final IList<Double> data) {
		return Stochanalysis.coefficientOfVariance(scope, data);
	}

	@operator (
			value = "rolling_se",
			type = IType.LIST,
			content_type = IType.FLOAT,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return the list of standard error according to the number of observations, </br> i.e. value at index i is the standard error for the first i observations.")
	public static IList<Double> rollingSE(final IScope scope, final IList<Double> data) {
		return Stochanalysis.standardError(scope, data);
	}

	@operator (
			value = "power_test",
			type = IType.INT,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return the number of observation to satisfy power test given a critical effect size, tAlpha and tBeta."
					+ "</br>see reference: https://rseri.me/publication/b016/B016.pdf (accessible as of 04/2026).")
	public static Integer powerTestCSE(final IScope scope, final IList<Double> data, final double tAlpha,
			final double tBeta, final double criticalEffectSize) {
		return Stochanalysis.ces(scope, data, tAlpha, tBeta, criticalEffectSize);
	}

	/**
	 * Helper to convert matrix to map of columns.
	 */
	private static IMap<String, IList<Double>> matrixToMap(final IScope scope, final IMatrix matrix) {
		IMap<String, IList<Double>> map = GamaMapFactory.create();
		for (int j = 0; j < matrix.getCols(scope); j++) {
			IList<Double> col = GamaListFactory.create(Types.FLOAT);
			for (int i = 0; i < matrix.getRows(scope); i++) { col.add(Cast.asFloat(scope, matrix.get(scope, j, i))); }
			map.put("col" + j, col);
		}
		return map;
	}

	/**
	 * Helper to convert a GAML map to a rigid Map<String, List<Double>>.
	 */
	private static Map<String, List<Double>> convertToDoubleMap(final IScope scope, final IMap<?, ?> map) {
		if (map == null || map.isEmpty()) throw GamaRuntimeException.error("Data map is empty or null", scope);
		Map<String, List<Double>> result = new LinkedHashMap<>();
		map.forEach((k, v) -> {
			if (v instanceof List<?> list) {
				List<Double> doubles = new ArrayList<>();
				for (Object o : list) { doubles.add(Cast.asFloat(scope, o)); }
				result.put(Cast.asString(scope, k), doubles);
			}
		});
		return result;
	}

	/**
	 * Compute the residuals for the regression
	 *
	 * @param scope
	 *            the scope
	 * @param regression
	 *            the regression
	 * @return the list of residuals
	 */
	@operator (
			value = "residuals",
			type = IType.LIST,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC, IConcept.REGRESSION })
	@doc (
			value = "Return the list of residuals for a given regression model",
			examples = { @example (
					value = "residuals(my_regression)",
					isExecutable = false) })
	@no_test
	public static IList<Double> residuals(final IScope scope, final GamaRegression regression) {
		return regression.getResiduals();
	}

	/**
	 *
	 *
	 * @param scope
	 * @param size
	 * @param sumOfSquares
	 * @return
	 */
	@operator (
			value = "rms",
			can_be_const = true,
			type = IType.FLOAT,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	public static Double opRms(final IScope scope, final Integer size, final Double sumOfSquares) {
		if (size == null || size == 0) return 0.0;
		return Math.sqrt(sumOfSquares / size);
	}

	/**
	 * Skewness.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the double
	 * @throws GamaRuntimeException
	 */
	@operator (
			value = "skewness",
			can_be_const = true,
			type = IType.FLOAT,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "returns skewness value computed from the operand list of values",
			special_cases = "if the length of the list is lower than 3, returns NaN",
			examples = { @example (
					value = "skewness ([1,2,3,4,5])",
					equals = "0.0") })
	public static Double opSkewness(final IScope scope, final IContainer data) throws GamaRuntimeException {
		if (data == null || data.length(scope) < 3) return Double.NaN;
		final Skewness sk = new Skewness();
		return sk.evaluate(toDoubleArray(scope, data));
	}

	/**
	 * Kurtosis.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the double
	 * @throws GamaRuntimeException
	 */
	@operator (
			value = "kurtosis",
			can_be_const = true,
			type = IType.FLOAT,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "returns kurtosis value computed from the operand list of values",
			special_cases = "if the length of the list is lower than 4, returns NaN",
			examples = { @example (
					value = "kurtosis ([1,2,3,4,5])",
					equals = "-1.3") })
	public static Double opKurtosis(final IScope scope, final IContainer data) throws GamaRuntimeException {
		if (data == null || data.length(scope) < 4) return Double.NaN;
		final Kurtosis ku = new Kurtosis();
		return ku.evaluate(toDoubleArray(scope, data));
	}

	/**
	 * Rank.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @return the list
	 */
	@operator (
			value = "rank",
			can_be_const = true,
			type = IType.LIST,
			content_type = IType.INT,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "returns a list containing the rank of each element of the operand list",
			examples = { @example (
					value = "rank ([1,5,2,3,4])",
					equals = "[0,4,1,2,3]") })
	public static IList opRank(final IScope scope, final IContainer data) {
		if (data == null || data.isEmpty(scope)) return GamaListFactory.create();
		final double[] values = toDoubleArray(scope, data);
		final Integer[] indices = new Integer[values.length];
		for (int i = 0; i < indices.length; i++) { indices[i] = i; }
		Arrays.sort(indices, (i1, i2) -> Double.compare(values[i1], values[i2]));
		final Integer[] ranks = new Integer[values.length];
		for (int i = 0; i < ranks.length; i++) { ranks[indices[i]] = i; }
		return GamaListFactory.createWithoutChecks(Arrays.asList(ranks));
	}

	/**
	 * Max by.
	 *
	 * @param scope
	 *            the scope
	 * @param eachName
	 *            the each name
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the object
	 */
	@operator (
			value = "max_by",
			type = ITypeProvider.FIRST_CONTENT_TYPE,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "returns the element of the container that maximizes the filter",
			examples = { @example (
					value = "max_by([1, 2, 3, 4], each)",
					equals = "4") })
	@validator (ComparableValidator.class)
	public static Object opMaxBy(final IScope scope, final String eachName, final IContainer c,
			final IExpression filter) {
		return Containers.stream(scope, c).map(Containers.with(scope, eachName, filter)).maxBy(Function.identity())
				.orElse(null);
	}

}

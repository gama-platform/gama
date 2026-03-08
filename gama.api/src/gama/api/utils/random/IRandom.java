/*******************************************************************************************************
 *
 * IRandom.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.random;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import gama.annotations.constants.IKeyword;
import gama.api.types.geometry.IPoint;

/**
 *
 */
public interface IRandom {

	/** The Constant DOC. */
	String DOC = "The random number generator to use. Four different ones are at the disposal of the modeler: " + "'"
			+ IKeyword.MERSENNE + "'"
			+ " represents the default generator, based on the Mersenne-Twister algorithm. Very reliable, fast and deterministic (that is, using the same seed and the same sequence of calls, it will return the same stream of pseudo-random numbers). This algorithm is however not safe to use in simulations where agents can behave in parallel; "
			+ "'threaded'"
			+ " is a very fast generator, based on the DotMix algorithm, that can be safely used in parallel simulations as it creates one instance per thread. However, determinism cannot be guaranteed and this algorithm does not accept a seed as each instance will compute its own;"
			+ "'" + IKeyword.PARALLEL + "'"
			+ " is a version of the Mersenne-Twister algorithm that can be safely used in parallel simulations by preventing a concurrent access to its internal state. Determinism is guaranteed (in terms of generation, but not in terms of execution, as the sequence in which the threads will access it cannot be determined) and it performs a bit slower than its base version."
			+ "'" + IKeyword.JAVA + "'"
			+ " invokes the standard generator provided by the JDK, deterministic and thread-safe, albeit slower than all the other ones";

	/**
	 * Sets the usage.
	 *
	 * @param usage
	 *            the new usage
	 */
	void setUsage(Integer usage);

	/**
	 * Gets the usage.
	 *
	 * @return the usage
	 */
	Integer getUsage();

	/**
	 * Creates a new Gaussian Generator object.
	 *
	 * @param mean
	 *            the mean
	 * @param stdv
	 *            the stdv
	 *
	 * @return the gaussian generator
	 */
	double createGaussian(double mean, double stdv);

	/**
	 * Creates the seed.
	 *
	 * @param s
	 *            the s
	 * @param length
	 *            the length
	 * @return the byte[]
	 */
	byte[] generateSeed(int length);

	/**
	 * Sets the seed.
	 *
	 * @param newSeed
	 *            the new seed
	 * @param init
	 *            the init
	 */
	void setSeed(Double newSeed, boolean init);

	/**
	 * Sets the generator.
	 *
	 * @param newGen
	 *            the new generator
	 */
	void setGenerator(String newGen, boolean init);

	/**
	 * Shuffle in place.
	 *
	 * @param list
	 *            the list
	 */
	void shuffleInPlace(Collection list);

	/**
	 * Shuffle in place.
	 *
	 * @param <T>
	 *            the generic type
	 * @param a
	 *            the a
	 */
	<T> void shuffleInPlace(T[] a);

	/**
	 * Shuffle in place.
	 *
	 * @param a
	 *            the a
	 */
	void shuffleInPlace(double[] a);

	/**
	 * Shuffle in place.
	 *
	 * @param a
	 *            the a
	 */
	void shuffleInPlace(int[] a);

	/**
	 * Shuffle in place.
	 *
	 * @param a
	 *            the a
	 */
	void shuffleInPlace(short[] a);

	/**
	 * Shuffle in place.
	 *
	 * @param a
	 *            the a
	 */
	void shuffleInPlace(char[] a);

	/**
	 * Shuffle in place.
	 *
	 * @param list
	 *            the list
	 */
	void shuffleInPlace(List list);

	/**
	 * Shuffle.
	 *
	 * @param string
	 *            the string
	 * @return the string
	 */
	String shuffle(String string);

	/**
	 * @return an uniformly distributed int random number in [from, to]
	 */
	int between(int min, int max);

	/**
	 * Between.
	 *
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @return the double
	 */
	double between(double min, double max);

	/**
	 * @return an uniformly distributed int random number in [min, max] respecting the step
	 */
	int between(int min, int max, int step);

	/**
	 * Between.
	 *
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param step
	 *            the step
	 * @return the double
	 */
	double between(double min, double max, double step);

	/**
	 * Next.
	 *
	 * @return the double
	 */
	double next();

	/**
	 * @return
	 */
	Double getSeed();

	/**
	 * @return
	 */
	String getRngName();

	/**
	 * Gets the generator.
	 *
	 * @return the generator
	 */
	Random getGenerator();

	/**
	 * One of.
	 *
	 * @param <K>
	 *            the key type
	 * @param c
	 *            the c
	 * @return the k
	 */
	<K> K oneOf(Collection<K> c);

	/**
	 * One of.
	 *
	 * @param <K>
	 *            the key type
	 * @param c
	 *            the c
	 * @return the k
	 */
	<K> K oneOf(List<K> c);

	/**
	 * One of.
	 *
	 * @param <K>
	 *            the key type
	 * @param c
	 *            the c
	 * @return the k
	 */
	<K> K oneOf(K[] c);

	/**
	 * One of.
	 *
	 * @param c
	 *            the c
	 * @return the int
	 */
	int oneOf(int[] c);

	/**
	 * One of.
	 *
	 * @param c
	 *            the c
	 * @return the double
	 */
	double oneOf(double[] c);

	/**
	 * One of.
	 *
	 * @param c
	 *            the c
	 * @return true, if successful
	 */
	boolean oneOf(boolean[] c);

	/**
	 * Between.
	 *
	 * @param pMin
	 *            the min
	 * @param pMax
	 *            the max
	 * @param pStep
	 *            the step
	 * @return the gama point
	 */
	IPoint between(IPoint pMin, IPoint pMax, IPoint pStep);

	/**
	 * Between.
	 *
	 * @param pMin
	 *            the min
	 * @param pMax
	 *            the max
	 * @return the i point
	 */
	IPoint between(IPoint pMin, IPoint pMax);

	/**
	 * @param distribution
	 * @return
	 */
	int choiceIn(List<Double> distribution);

	/**
	 * @param <T>
	 * @param distribution
	 * @return
	 */
	<T> T choiceIn(Map<T, Double> distribution);

}
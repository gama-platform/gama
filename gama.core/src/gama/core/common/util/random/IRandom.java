/*******************************************************************************************************
 *
 * IRandom.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.util.random;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import gama.core.metamodel.shape.IPoint;

/**
 *
 */
public interface IRandom {

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

}
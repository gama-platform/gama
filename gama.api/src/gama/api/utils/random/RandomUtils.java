/*******************************************************************************************************
 *
 * RandomUtils.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.random;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import gama.api.GAMA;
import gama.api.constants.Generators;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.utils.MathUtils;
import gama.api.utils.prefs.GamaPreferences;

/**
 * The Class RandomUtils.
 */

/**
 * The Class RandomUtils.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class RandomUtils implements IRandom {

	/** The Constant SEED_SOURCE. */
	private static final SecureRandom SEED_SOURCE = new SecureRandom();

	/** The seed. */
	protected Double seed;
	/** The generator name. */
	private String generatorName;
	/** The generator. */
	private IGamaRNG generator;

	static {
		// Initialize the default random generator
		GAMA.setRandomGenerator(new RandomUtils());
	}

	/**
	 * Instantiates a new random utils.
	 *
	 * @param seed
	 *            the seed.
	 * @param rng
	 *            the rng
	 */
	public RandomUtils(final Double seed, final String rng) {
		setSeed(seed, false);
		setGenerator(rng, true);
	}

	/**
	 * Instantiates a new random utils.
	 *
	 * @param rng
	 *            the rng
	 */
	public RandomUtils(final String rng) {
		this(GamaPreferences.External.CORE_SEED_DEFINED.getValue() ? GamaPreferences.External.CORE_SEED.getValue()
				: null, rng);
	}

	/**
	 * Instantiates a new random utils.
	 */
	public RandomUtils() {
		this(GamaPreferences.External.CORE_RNG.getValue());
	}

	/**
	 * Inits the generator.
	 */
	private void initGenerator() {
		generator = switch (Generators.get(generatorName)) {
			case JAVA -> new JavaRNG(this);
			case THREADED -> new ThreadLocalRNG(this);
			case PARALLEL -> new ParallelMersenneTwisterRNG(this);
			default -> new MersenneTwisterRNG(this);
		};

	}

	/**
	 * Sets the usage.
	 *
	 * @param usage
	 *            the new usage
	 */
	@Override
	public void setUsage(final Integer usage) {
		generator.setUsage(usage);
	}

	/**
	 * Gets the usage.
	 *
	 * @return the usage
	 */
	@Override
	public Integer getUsage() { return generator.getUsage(); }

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
	@Override
	public double createGaussian(final double mean, final double stdv) {
		return generator.nextGaussian() * stdv + mean;
	}

	/**
	 * Creates the seed.
	 *
	 * @param s
	 *            the s
	 * @param length
	 *            the length
	 * @return the byte[]
	 */
	@Override
	public byte[] generateSeed(final int length) {
		Double realSeed = seed;
		if (realSeed < 0) { realSeed *= -1; }
		if (realSeed < 1) { realSeed *= Long.MAX_VALUE; }
		long l = Double.doubleToRawLongBits(realSeed);
		final byte[] result = new byte[length];
		switch (length) {
			case 4:
				for (int i1 = 0; i1 < 4; i1++) {
					result[i1] = (byte) (l & 0xff);
					l >>= 8;
				}
				break;
			case 8:
				for (int i = 0; i < 8; i++) {
					result[i] = (byte) l;
					l >>= 8;
				}
				break;
			case 16:
				for (int i = 0; i < 8; i++) {
					result[i] = result[i + 8] = (byte) (l & 0xff);
					l >>= 8;
				}
		}
		return result;
	}

	/**
	 * Sets the seed.
	 *
	 * @param newSeed
	 *            the new seed
	 * @param init
	 *            the init
	 */
	@Override
	public void setSeed(final Double newSeed, final boolean init) {
		seed = newSeed;
		if (seed == null) { seed = SEED_SOURCE.nextDouble(); }
		if (init) { initGenerator(); }
	}

	/**
	 * Sets the generator.
	 *
	 * @param newGen
	 *            the new generator
	 */
	@Override
	public void setGenerator(final String newGen, final boolean init) {
		generatorName = newGen;
		if (init) { initGenerator(); }
	}

	/**
	 * Shuffle in place.
	 *
	 * @param list
	 *            the list
	 */
	@Override
	public void shuffleInPlace(final Collection list) {
		if (list == null) return;
		final int size = list.size();
		if (size < 2) return;
		final Object[] a = list.toArray(new Object[size]);
		list.clear();
		shuffleInPlace(a);
		list.addAll(Arrays.asList(a));
	}

	/**
	 * Shuffle in place.
	 *
	 * @param <T>
	 *            the generic type
	 * @param a
	 *            the a
	 */
	@Override
	public <T> void shuffleInPlace(final T[] a) {
		for (int i = 0; i < a.length; i++) {
			final int change = between(i, a.length - 1);
			final T helper = a[i];
			a[i] = a[change];
			a[change] = helper;
		}
	}

	/**
	 * Shuffle in place.
	 *
	 * @param a
	 *            the a
	 */
	@Override
	public void shuffleInPlace(final double[] a) {
		for (int i = 0; i < a.length; i++) {
			final int change = between(i, a.length - 1);
			final double helper = a[i];
			a[i] = a[change];
			a[change] = helper;
		}
	}

	/**
	 * Shuffle in place.
	 *
	 * @param a
	 *            the a
	 */
	@Override
	public void shuffleInPlace(final int[] a) {
		for (int i = 0; i < a.length; i++) {
			final int change = between(i, a.length - 1);
			final int helper = a[i];
			a[i] = a[change];
			a[change] = helper;
		}
	}

	/**
	 * Shuffle in place.
	 *
	 * @param a
	 *            the a
	 */
	@Override
	public void shuffleInPlace(final short[] a) {
		for (int i = 0; i < a.length; i++) {
			final int change = between(i, a.length - 1);
			final short helper = a[i];
			a[i] = a[change];
			a[change] = helper;
		}
	}

	/**
	 * Shuffle in place.
	 *
	 * @param a
	 *            the a
	 */
	@Override
	public void shuffleInPlace(final char[] a) {
		for (int i = 0; i < a.length; i++) {
			final int change = between(i, a.length - 1);
			final char helper = a[i];
			a[i] = a[change];
			a[change] = helper;
		}
	}

	/**
	 * Shuffle in place.
	 *
	 * @param list
	 *            the list
	 */
	@Override
	public void shuffleInPlace(final List list) {
		for (int i = list.size(); i > 1; i--) {
			final int i1 = i - 1;
			final int j = between(0, i - 1);
			final Object tmp = list.get(i1);
			list.set(i1, list.get(j));
			list.set(j, tmp);
		}
	}

	/**
	 * Shuffle.
	 *
	 * @param string
	 *            the string
	 * @return the string
	 */
	@Override
	public String shuffle(final String string) {
		final char[] c = string.toCharArray();
		shuffleInPlace(c);
		return String.copyValueOf(c);
	}

	/**
	 * @return an uniformly distributed int random number in [from, to]
	 */
	@Override
	public int between(final int min, final int max) {
		return (int) (min + (long) ((1L + max - min) * next()));
	}

	/**
	 * Between.
	 *
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @return the double
	 */
	@Override
	public double between(final double min, final double max) {
		// uniformly distributed double random number in [min, max]
		return min + (max + Double.MIN_VALUE - min) * next();
	}

	/**
	 * @return an uniformly distributed int random number in [min, max] respecting the step
	 */
	@Override
	public int between(final int min, final int max, final int step) {
		final int nbSteps = (max - min) / step;
		return min + between(0, nbSteps) * step;
	}

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
	@Override
	public double between(final double min, final double max, final double step) {
		// uniformly distributed double random number in [min, max] respecting
		// the step
		final double val = between(min, max);
		final int nbStep = (int) ((val - min) / step);
		final double valSup = Math.min(max, min + (nbStep + 1.0) * step);
		final double valMin = min + nbStep * step;
		final int precision = BigDecimal.valueOf(step).scale() + 5;

		final double high = MathUtils.round(valSup, precision);
		final double low = MathUtils.round(valMin, precision);
		return val - low < high - val ? low : high;
	}

	/**
	 * Next.
	 *
	 * @return the double
	 */
	@Override
	public double next() {
		return generator.nextDouble();
	}

	/**
	 * @return
	 */
	@Override
	public Double getSeed() { return seed; }

	/**
	 * @return
	 */
	@Override
	public String getRngName() { return generatorName; }

	/**
	 * Gets the generator.
	 *
	 * @return the generator
	 */
	@Override
	public Random getGenerator() { return generator.getRandomGenerator(); }

	/**
	 * One of.
	 *
	 * @param <K>
	 *            the key type
	 * @param c
	 *            the c
	 * @return the k
	 */
	@Override
	public <K> K oneOf(final Collection<K> c) {
		if (c == null || c.isEmpty()) return null;
		return (K) oneOf(c.toArray());
	}

	/**
	 * One of.
	 *
	 * @param <K>
	 *            the key type
	 * @param c
	 *            the c
	 * @return the k
	 */
	@Override
	public <K> K oneOf(final List<K> c) {
		if (c == null || c.isEmpty()) return null;
		return c.get(between(0, c.size() - 1));
	}

	/**
	 * One of.
	 *
	 * @param <K>
	 *            the key type
	 * @param c
	 *            the c
	 * @return the k
	 */
	@Override
	public <K> K oneOf(final K[] c) {
		if (c == null || c.length == 0) return null;
		return c[between(0, c.length - 1)];

	}

	/**
	 * One of.
	 *
	 * @param c
	 *            the c
	 * @return the int
	 */
	@Override
	public int oneOf(final int[] c) {
		if (c == null || c.length == 0) return -1;
		return c[between(0, c.length - 1)];
	}

	/**
	 * One of.
	 *
	 * @param c
	 *            the c
	 * @return the double
	 */
	@Override
	public double oneOf(final double[] c) {
		if (c == null || c.length == 0) return -1;
		return c[between(0, c.length - 1)];
	}

	/**
	 * One of.
	 *
	 * @param c
	 *            the c
	 * @return true, if successful
	 */
	@Override
	public boolean oneOf(final boolean[] c) {
		if (c == null || c.length == 0) return false;
		return c[between(0, c.length - 1)];
	}

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
	@Override
	public IPoint between(final IPoint pMin, final IPoint pMax, final IPoint pStep) {
		double x = between(pMin.getX(), pMax.getX(), pStep.getX());
		double y = between(pMin.getY(), pMax.getY(), pStep.getY());
		double z = between(pMin.getZ(), pMax.getZ(), pStep.getZ());
		return GamaPointFactory.create(x, y, z);
	}

	/**
	 * Between.
	 *
	 * @param pMin
	 *            the min
	 * @param pMax
	 *            the max
	 * @return the i point
	 */
	@Override
	public IPoint between(final IPoint pMin, final IPoint pMax) {
		double x = between(pMin.getX(), pMax.getX());
		double y = between(pMin.getY(), pMax.getY());
		double z = between(pMin.getZ(), pMax.getZ());
		return GamaPointFactory.create(x, y, z);
	}

	/**
	 * Returns a random index in a list where elements represents probabilities to pick this index.
	 *
	 * @param distribution
	 *            the distribution
	 * @return the integer
	 */
	@Override
	public int choiceIn(final List<? extends Number> distribution) {
		if (distribution == null || distribution.isEmpty()) return -1;
		double total = distribution.stream().mapToDouble(Number::doubleValue).sum();
		if (total == 0) return -1;
		double rand = next();
		double cumulative = 0;
		for (int i = 0; i < distribution.size(); i++) {
			cumulative += distribution.get(i).doubleValue() / total;
			if (rand < cumulative) return i;
		}
		return -1;
	}

	/**
	 * Returns a random key in the map where values represents probabilities to pick this key.
	 *
	 * @param <V>
	 *            the value type
	 * @param distribution
	 *            the distribution
	 * @return the v
	 */
	@Override
	public <V> V choiceIn(final Map<V, ? extends Number> distribution) {
		if (distribution == null || distribution.isEmpty()) return null;
		double total = distribution.values().stream().mapToDouble(Number::doubleValue).sum();
		if (total == 0) return null;
		double rand = next();
		double cumulative = 0;
		for (Map.Entry<V, ? extends Number> entry : distribution.entrySet()) {
			cumulative += entry.getValue().doubleValue() / total;
			if (rand < cumulative) return entry.getKey();
		}
		return null;
	}

}

/**
 * 
 */
package gama.api.constants;

import java.util.Arrays;
import java.util.List;

/**
 * Enumeration of random number generators available in GAMA.
 * <p>
 * This enum defines the different random number generators that can be used in GAMA simulations,
 * each with specific characteristics regarding performance, thread-safety, and determinism.
 * The choice of generator can significantly impact simulation behavior, especially in parallel
 * or multi-threaded contexts.
 * </p>
 * 
 * @author GAMA Development Team
 * @since GAMA 1.0
 */
public enum Generators {
	/** The mersenne. */
	MERSENNE(IKeyword.MERSENNE,
			" represents the default generator, based on the Mersenne-Twister algorithm. Very reliable, fast and deterministic (that is, using the same seed and the same sequence of calls, it will return the same stream of pseudo-random numbers). This algorithm is however not safe to use in simulations where agents can behave in parallel; "),
	/** The parallel. */
	PARALLEL(IKeyword.PARALLEL,
			" is a version of the Mersenne-Twister algorithm that can be safely used in parallel simulations by preventing a concurrent access to its internal state. Determinism is guaranteed (in terms of generation, but not in terms of execution, as the sequence in which the threads will access it cannot be determined) and it performs a bit slower than its base version; "),
	/** The java. */
	JAVA(IKeyword.JAVA,
			" invokes the standard generator provided by the JDK, deterministic and thread-safe, albeit slower than all the other ones; "),
	/** The threaded. */
	THREADED("threaded",
			" is a very fast generator, based on the DotMix algorithm, that can be safely used in parallel simulations as it creates one instance per thread. However, determinism cannot be guaranteed and this algorithm does not accept a seed as each instance will compute its own; ");

	/** The name. */
	private final String name;

	/**
	 * Constructs a generator with the specified name and documentation.
	 *
	 * @param name the name of the generator
	 * @param doc the documentation describing the generator's behavior
	 */
	Generators(final String name, final String doc) {
		this.name = name;
	}

	/**
	 * Gets the name of this generator.
	 *
	 * @return the generator name
	 */
	public String getName() { return name; }

	// ****** Reverse Lookup ************//

	/**
	 * Gets the generator enum value corresponding to the given name.
	 *
	 * @param url the generator name to look up
	 * @return the matching Generators enum value, or null if not found
	 */
	public static Generators get(final String url) {
		return Arrays.stream(values()).filter(env -> env.name.equals(url)).findFirst().orElse(null);
	}

	/**
	 * Names.
	 *
	 * @return the list
	 */
	public static List<String> names() {
		return Arrays.stream(values()).map(e -> e.name).toList();
	}
}
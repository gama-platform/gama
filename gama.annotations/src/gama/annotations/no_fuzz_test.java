/*******************************************************************************************************
 *
 * no_fuzz_test.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * no_fuzz_test should be used to indicate that no fuzz test must be generated for a GAML operator (or for a GAML
 * type, regarding the casts to this type). Fuzz tests are generated at compile time for every operator: they call it
 * with edge values (0, -1, infinities, NaN, empty strings and containers, nil...) and check that it either returns or
 * raises a proper GAML error.
 *
 * It is meant for the operators that cannot be called blindly: the ones that act on the outside world (files, network,
 * user interface...) and the ones that, for some values, never return or exhaust the memory, as there is no timeout in
 * the test runner. The reason is mandatory, so that the second kind remains visible as something to fix.
 *
 * Operators annotated with {@link no_test} are not fuzz tested either.
 */
@Retention (RetentionPolicy.SOURCE)
@Target ({ ElementType.METHOD, ElementType.TYPE })
public @interface no_fuzz_test {

	/**
	 * The reason why the artefact cannot be fuzz tested.
	 *
	 * @return the reason
	 */
	String value();
}

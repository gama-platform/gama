/**
 *
 */
package gama.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import gama.annotations.support.Reason;

/**
 * no_test should be used to indicate that a GAML artefact does not need to be provided with tests (either with
 * the @test annotation or with @example). It will prevent the compiler from producing warnings in that case.
 *
 * @author drogoul
 *
 */
@Retention (RetentionPolicy.SOURCE)
public @interface no_test {

	/**
	 * Value.
	 *
	 * @return the reason
	 */
	Reason value() default Reason.NONE;
}
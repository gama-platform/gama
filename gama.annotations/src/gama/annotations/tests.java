/**
 *
 */
package gama.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The Interface tests.
 */
@Retention (RetentionPolicy.RUNTIME)
public @interface tests {
	/**
	 * Value.
	 *
	 * @return the test[]
	 */
	test[] value() default {};
}
/**
 *
 */
package gama.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Container for repeatable {@link test} annotations. Each entry generates an independent GAML test statement.
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
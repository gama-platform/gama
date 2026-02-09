/**
 *
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface experiment.
 */
@Retention (RetentionPolicy.CLASS)
@Target (ElementType.TYPE)
@Inherited
public @interface experiment {

	/**
	 * The keyword that will allow to open this display in GAML (in "display type: keyword").
	 *
	 * @return
	 */
	String value();
}
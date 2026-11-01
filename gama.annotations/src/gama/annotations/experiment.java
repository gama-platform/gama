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
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
@Inherited
public @interface experiment {

	/**
	 * The keyword identifying this experiment type in GAML (in "experiment name type: keyword").
	 *
	 * @return the experiment type keyword
	 */
	String value();
}
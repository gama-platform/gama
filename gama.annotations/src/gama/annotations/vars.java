/**
 *
 */
package gama.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * The class vars. Used to describe the variables defined by a @species, a @skill or the implementation class of a @type
 *
 * @author drogoul
 * @since 2 juin 2012
 *
 */
@Retention (RetentionPolicy.RUNTIME)
public @interface vars {

	/**
	 * Value.
	 *
	 * @return an Array of var instances, each representing a variable
	 * @see variable
	 */
	variable[] value();
}
/**
 * 
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Written by gaudou Modified on 24 mars 2014
 *
 * Used to annotate fields that are used as constants in GAML.
 *
 */
@Retention (RetentionPolicy.RUNTIME)
@Target ({ ElementType.FIELD })
public @interface constant {

	/**
	 * @return an array of strings, each representing a category in which this constant can be classified (for
	 *         documentation indexes)
	 */

	String[] category() default {};

	/**
	 * @return an array of strings, each representing this GAML word we can use to find the constant in the website
	 *         search feature.
	 */

	String[] concept() default {};

	/**
	 * @return a string representing the basic keyword for the constant. Does not need to be unique throughout GAML
	 *
	 */
	String value();

	/**
	 * @return an Array of strings, each representing a possible alternative name for the constant. Does not need to
	 *         be unique throughout GAML.
	 *
	 **/
	String[] altNames() default {};

	/**
	 * Doc.
	 *
	 * @return the documentation attached to this constant.
	 * @see doc
	 */
	doc[] doc() default {};
}
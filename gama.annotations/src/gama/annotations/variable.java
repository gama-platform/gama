/**
 *
 */
package gama.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * The class var. Used to describe a single variable or field.
 *
 * @author drogoul
 * @since 2 juin 2012
 *
 */
@Retention (RetentionPolicy.RUNTIME)
@Target ({})
public @interface variable {

	/**
	 * Name.
	 *
	 * @return The name of the variable as it can be used in GAML.
	 */
	String name();

	/**
	 * Type.
	 *
	 * @return The textual representation of the type of the variable (see IType)
	 */
	int type();

	/**
	 * Of.
	 *
	 * @return The int representation of the content type of the variable (see IType#defaultContentType())
	 */
	int of() default 0;

	/**
	 * Index.
	 *
	 * @return The int representation of the index type of the variable (see IType#defaultKeyType())
	 */
	int index() default 0;

	/**
	 * Constant
	 *
	 * @return whether or not this variable should be considered as non modifiable
	 */
	boolean constant() default false;

	/**
	 * Init
	 *
	 * @return the initial value of this variable as a String that will be interpreted by GAML
	 */
	String init() default "";

	/**
	 * Depends_on.
	 *
	 * @return an array of String representing the names of the variables on which this variable depends (so that they
	 *         are computed before)
	 */
	String[] depends_on() default {};

	/**
	 * internal.
	 *
	 * @return whether this var is for internal use only.
	 */
	boolean internal() default false;

	/**
	 * Doc.
	 *
	 * @return the documentation attached to this variable
	 * @see doc
	 */
	doc[] doc() default {};
}
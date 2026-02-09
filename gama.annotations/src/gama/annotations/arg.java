/**
 *
 */
package gama.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * The class arg. Describes an argument passed to an action.
 *
 * @author drogoul
 * @since 2 juin 2012
 *
 */
@Retention (RetentionPolicy.CLASS)
@Inherited
public @interface arg {

	/**
	 * Name.
	 *
	 * @return the name of the argument as it can be used in GAML
	 */
	String name()

	default "";

	/**
	 * Type.
	 *
	 * @return An array containing the textual representation of the types that can be taken by the argument (see IType)
	 */
	int type()

	default 0;

	/**
	 * Optional.
	 *
	 * @return whether this argument is optional or not
	 * @change AD 31/08/13 : the default is now true.
	 */
	boolean optional()

	default true;

	/**
	 * Doc.
	 *
	 * @return the documentation attached to this argument
	 * @see doc
	 */
	doc[] doc() default {};
}
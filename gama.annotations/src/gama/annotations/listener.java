/**
 * 
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The class listener. Indicates that a method is to be used as a listener for a variable, even if this variable is
 * not defined in the vars of this class. Allows to "listen" to the evolution of the variables managed by other
 * skills. If the var doesn't exist, this annotation has no effect.
 *
 * @see vars
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface listener {

	/**
	 * Value.
	 *
	 * @return the name of the variable for which the annotated method is to be considered as a listener.
	 */
	String value();
}
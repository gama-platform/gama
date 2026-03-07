/**
 *
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gama.annotations.constants.IKeyword;

/**
 *
 * The class facets. Describes a list of facet used by a symbol (a statement, a declaration) in GAML. Can only be
 * declared in classes annotated with symbol
 *
 * @author drogoul
 * @since 2 juin 2012
 *
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
@Inherited
public @interface facets {

	/**
	 * Value.
	 *
	 * @return an Array of @facet, each representing a facet name, type..
	 */
	facet[] value();

	/**
	 * Ommissible.
	 *
	 * @return the facet that can be safely omitted by the modeler (provided its value is the first following the
	 *         keyword of the statement).
	 */
	String omissible() default IKeyword.NAME;

}
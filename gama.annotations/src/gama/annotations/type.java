/**
 *
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gama.annotations.support.ISymbolKind;

/**
 *
 * The class type. Allows to declare a new datatype in GAML. Should annotate a class that implements IType<...> or
 * subclasses GamaType<...>
 *
 * @author drogoul
 * @since 2 juin 2012
 *
 */
@Retention (RetentionPolicy.RUNTIME)
@Inherited
@Target (ElementType.TYPE)
public @interface type {

	/**
	 * Name.
	 *
	 * @return a String representing the type name in GAML
	 */
	String name();

	/**
	 * @return an array of strings, each representing this GAML word we can use to find the type in the website search
	 *         feature.
	 */

	String[] concept() default {};

	/**
	 * @return the unique identifier for this type. User-added types can be chosen between IType.AVAILABLE_TYPE and
	 *         IType.SPECIES_TYPE (exclusive)
	 */
	int id();

	/**
	 * @return the list of Java Classes this type is "wrapping" (i.e. representing). The first one is the one that will
	 *         be used preferentially throughout GAMA. The other ones are to ensure compatibility, in operators, with
	 *         compatible Java classes (for instance, List and GamaList).
	 */
	@SuppressWarnings ("rawtypes")
	Class[] wraps();

	/**
	 * @return the kind of Variable used to store this type. see ISymbolKind.Variable.
	 */
	int kind()

	default ISymbolKind.Variable.REGULAR;

	/**
	 * internal.
	 *
	 * @return whether this type is for internal use only.
	 */
	boolean internal()

	default false;

	/**
	 * @return an array of strings, each representing a category in which this constant can be classified (for
	 *         documentation indexes)
	 */

	String[] category() default {};

	/**
	 * Doc.
	 *
	 * @return the documentation attached to this type
	 * @see doc
	 */
	doc[] doc() default {};

}
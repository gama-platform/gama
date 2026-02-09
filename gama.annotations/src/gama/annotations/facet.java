/**
 *
 */
package gama.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * The class facet. Describes a facet in a list of facets
 *
 * @see facets
 * @author drogoul
 * @since 2 juin 2012
 *
 */
@Retention (RetentionPolicy.CLASS)
public @interface facet {

	/**
	 * Name.
	 *
	 * @return the name of the facet. Must be unique within a symbol.
	 */
	String name();

	/**
	 * Type.
	 *
	 * @return The int values of the different types that can be taken by this facet.
	 * @see gama.gaml.types.IType
	 */

	int[] type();

	/**
	 * Of.
	 *
	 * @return The int representation of the content type of the facet (see IType#defaultContentType()). Only applies to
	 *         the types considered as containers
	 */
	int of()

	/**
	 * Index.
	 *
	 * @return the int
	 */
	default 0;

	/**
	 * Index.
	 *
	 * @return The int representation of the index type of the facet (see IType#defaultKeyType()). Only applies to the
	 *         types considered as containers
	 */
	int index()

	/**
	 * Values.
	 *
	 * @return the string[]
	 */
	default 0;

	/**
	 * Values.
	 *
	 * @return the values that can be taken by this facet. The value of the facet expression will be chosen among the
	 *         values described here
	 */
	String[] values() default {};

	/**
	 * Optional.
	 *
	 * @return whether or not this facet is optional or mandatory.
	 */

	boolean optional()

	/**
	 * Internal.
	 *
	 * @return true, if successful
	 */
	default false;

	/**
	 * internal.
	 *
	 * @return whether this facet is for internal use only.
	 */
	boolean internal()

	/**
	 * Doc.
	 *
	 * @return the doc[]
	 */
	default false;

	/**
	 * Doc.
	 *
	 * @return the documentation associated to the facet.
	 * @see doc
	 */
	doc[] doc() default {};

	/**
	 * RemoteContext.
	 *
	 * @return Indicates that the context of this facet is actually the one denoted by the statement it is attached to.
	 *         i.e. `self` will represent an agent of the species denoted by the statement, while `myself` will
	 *         represent the agent calling the statement
	 */

	boolean remote_context() default false;
}
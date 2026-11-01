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
@Retention (RetentionPolicy.RUNTIME)
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
	 * @return The accepted expression type IDs, or exactly one variable type descriptor when
	 *         {@link #declares_new_temp()} is true. Declaring facets also accept
	 *         {@link gama.annotations.support.ITypeProvider#STATEMENT_TYPE} and
	 *         {@link gama.annotations.support.ITypeProvider#INFERRED_TYPE}.
	 */
	int[] type();

	/**
	 * Whether this facet declares a new temporary identifier rather than accepting an expression.
	 * Its variable type is described by {@link #type()}, {@link #of()}, and {@link #index()}, which may use
	 * {@link gama.annotations.support.ITypeProvider#STATEMENT_TYPE} or
	 * {@link gama.annotations.support.ITypeProvider#INFERRED_TYPE}.
	 * Fixed types use their ordinary IDs; for example, {@code type = IType.LIST, of = IType.FLOAT}
	 * declares a {@code list<float>}. The declaration flag does not imply a list or another container.
	 * The statement implementation remains responsible for creating and assigning the runtime variable
	 * in the matching scope. This flag replaces the removed {@code IType.NEW_TEMP_ID} marker.
	 *
	 * @return true for an identifier declaration; false for an ordinary facet
	 */
	boolean declares_new_temp() default false;

	/**
	 * Of.
	 *
	 * @return The content type ID. On a declaring facet, this may also be a contextual statement/inference
	 *         provider. Only applies to container types; it does not make the outer type a container.
	 */
	int of() default 0;

	/**
	 * Index.
	 *
	 * @return The key type ID. On a declaring facet, this may also be a contextual statement/inference
	 *         provider. Only applies to container types, for example {@code index = IType.STRING} for a map.
	 */
	int index() default 0;

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
	boolean optional() default false;

	/**
	 * internal.
	 *
	 * @return whether this facet is for internal use only.
	 */
	boolean internal() default false;

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
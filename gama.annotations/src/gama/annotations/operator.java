/**
 *
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gama.annotations.support.ITypeProvider;

/**
 * Written by drogoul Modified on 9 august 2010
 *
 * Used to annotate methods that can be used as operators in GAML.
 *
 */
@Retention (RetentionPolicy.RUNTIME)
@Target ({ ElementType.METHOD, ElementType.TYPE })
public @interface operator {

	/**
	 * @return an array of strings, each representing this GAML word we can use to find the operator in the website
	 *         search feature.
	 */
	String[] concept() default {};

	/**
	 * @return an array of strings, each representing a category in which this operator can be classified (for
	 *         documentation indexes)
	 */
	String[] category() default {};

	/**
	 * @return an Array of strings, each representing a possible keyword for the operator. Does not need to be unique
	 *         throughout GAML
	 *
	 */
	String[] value();

	/**
	 * @return true if this operator should be treated as an iterator (i.e. allows the special variable "each" to be
	 *         used inside)
	 */
	boolean iterator() default false;

	/**
	 * @return whether or not the operator can be evaluated as a constant if its child (resp. children) is (resp. are)
	 *         constant.
	 */
	boolean can_be_const() default false;

	/**
	 * @return the type of the content if the returned value is a container. Can be directly a type in IType or one of
	 *         the constants declared in ITypeProvider (in which case, the content type is searched using this
	 *         provider).
	 * @see IType
	 * @see ITypeProvider
	 */
	int content_type() default ITypeProvider.NONE;

	/**
	 * @return the content type of the content if the returned value is a container of container (ex. a list of list).
	 *         Can be directly a type in IType or one of the constants declared in ITypeProvider (in which case, the
	 *         content type is searched using this provider).
	 * @see IType
	 * @see ITypeProvider
	 */
	int content_type_content_type() default ITypeProvider.NONE;

	/**
	 * @return the type of the index if the returned value is a container. Can be directly a type in IType or one of the
	 *         constants declared in ITypeProvider (in which case, the index type is searched using this provider).
	 * @see IType
	 * @see ITypeProvider
	 */
	int index_type() default ITypeProvider.NONE;

	/**
	 * @return if the argument is a container, return the types expected for its contents. Should be an array of
	 *         IType.XXX.
	 * @see IType
	 * @see ITypeProvider
	 */
	int[] expected_content_type() default {};

	/**
	 *
	 * @return the type of the expression if it cannot be determined at compile time (i.e. when the return type is
	 *         "Object"). Can be directly a type in IType or one of the constants declared in ITypeProvider (in which
	 *         case, the type is searched using this provider).
	 * @see IType
	 * @see ITypeProvider
	 */
	int type() default ITypeProvider.NONE;

	/**
	 * internal.
	 *
	 * @return whether this operator is for internal use only.
	 */
	boolean internal() default false;

	/**
	 * Doc.
	 *
	 * @return the documentation attached to this operator.
	 * @see doc
	 */
	doc[] doc() default {};
}
/**
 *
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gama.annotations.support.ITypeProvider;

/**
 * The Interface file.
 */
@Retention (RetentionPolicy.RUNTIME)
@Target ({ ElementType.TYPE })
@Inherited
public @interface file {

	/**
	 * The name of this type of files. This name will be used to generate two operators: name+"_file" and "is_"+name.
	 * The first operator may have variants taking one or several arguments, depending on the constructors defined in
	 * the class.
	 *
	 * @return a (human-understandable) string describing this type of files, suitable for use in composed operator
	 *         names (e.g. "shape", "image"...)
	 *
	 */
	String name();

	/**
	 * @return an array of strings, each representing this GAML word we can use to find the operators related to this
	 *         file in the website search feature.
	 */

	String[] concept() default {};

	/**
	 * The list of file extensions allowed for this type of files. These extensions will be used to check the validity
	 * of the file path, but also to generate the correct type of file when a path is passed to the generic "file"
	 * operator.
	 *
	 * @return an array of extensions (without the '.' delimiter) or an empty array if no specific extensions are
	 *         associated to this type of files (e.g. ["png","jpg","jpeg"...])
	 */
	String[] extensions();

	/**
	 * @return the type of the content of the buffer. Can be directly a type in IType or one of the constants declared
	 *         in ITypeProvider (in which case, the content type is searched using this provider).
	 * @see IType
	 * @see ITypeProvider
	 */
	int buffer_content()

	default ITypeProvider.NONE;

	/**
	 * @return the type of the index of the buffer. Can be directly a type in IType or one of the constants declared in
	 *         ITypeProvider (in which case, the index type is searched using this provider).
	 * @see IType
	 * @see ITypeProvider
	 */
	int buffer_index()

	default ITypeProvider.NONE;

	/**
	 *
	 * @return the type of the buffer. Can be directly a type in IType or one of the constants declared in ITypeProvider
	 *         (in which case, the type is searched using this provider).
	 * @see IType
	 * @see ITypeProvider
	 */
	int buffer_type()

	default ITypeProvider.NONE;

	/**
	 * @return an array of strings, each representing a category in which this constant can be classified (for
	 *         documentation indexes)
	 */

	String[] category() default {};

	/**
	 * Doc.
	 *
	 * @return the doc[]
	 */
	doc[] doc() default {};
}
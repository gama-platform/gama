/**
 *
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gama.annotations.support.ISymbolKind;

/**
 * The Interface symbol. Represents a "symbol" in GAML, i.e. either a statement or a declaration (variable, species,
 * model, etc.). Elements annotated by this annotation should indicate what kind of symbol they represent
 *
 * @see ISymbolKind
 */
@Retention (RetentionPolicy.CLASS)
@Target (ElementType.TYPE)
public @interface symbol {

	/**
	 * Name.
	 *
	 * @return an Array of strings, each representing a possible keyword for a GAML statement.
	 *
	 */
	String[] name() default {};

	/**
	 * @return an array of strings, each representing this GAML word we can use to find the statement in the website
	 *         search feature.
	 */

	String[] concept() default {};

	/**
	 * Kind.
	 *
	 * @return the kind of the annotated symbol.
	 * @see #ISymbolKind
	 */
	int kind();

	/**
	 * WithScope.
	 *
	 * @return Indicates if the statement (usually a sequence) defines its own scope. Otherwise, all the temporary
	 *         variables defined in it are actually defined in the super-scope
	 */
	boolean with_scope() default true;

	/**
	 * WithSequence.
	 *
	 * @return Indicates wether or not a sequence can ou should follow the symbol denoted by this class.
	 */
	boolean with_sequence();

	/**
	 * WithArgs.
	 *
	 * @return Indicates wether or not the symbol denoted by this class will accept arguments
	 */
	boolean with_args() default false;

	/**
	 * RemoteContext.
	 *
	 * @return Indicates that the context of this statement is actually an hybrid context: although it will be executed
	 *         in a remote context, any temporary variables declared in the enclosing scopes should be passed on as if
	 *         the statement was executed in the current context.
	 */

	boolean remote_context() default false;

	/**
	 * Breakable.
	 *
	 * @return true, if this statement can be interrupted by 'break'. False by default.
	 */
	boolean breakable() default false;

	/**
	 * Continuable.
	 *
	 * @return true, if this statement can be interrupted by 'continue'. False by default.
	 */
	boolean continuable() default false;

	/**
	 * Doc.
	 *
	 * @return the documentation attached to this symbol.
	 * @see doc
	 */
	doc[] doc() default {};

	/**
	 * internal.
	 *
	 * @return whether this symbol is for internal use only.
	 */
	boolean internal() default false;

	/**
	 *
	 * @return Indicates that this statement must be unique in its super context (for example, only one return is
	 *         allowed in the body of an action).
	 */
	boolean unique_in_context() default false;

	/**
	 *
	 * @return Indicates that only one statement with the same name should be allowed in the same super context
	 */
	boolean unique_name() default false;

	/**
	 * @return an array of strings, each representing a category in which this constant can be classified (for
	 *         documentation indexes)
	 */
	String[] category() default {};

}
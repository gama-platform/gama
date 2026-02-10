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
 * The class inside. Used in conjunction with symbol. Provides a way to tell where this symbol should be located in a
 * model (i.e. what its parents should be). Either direct symbol names (in symbols) or generic symbol kinds can be used
 *
 * @see symbol
 * @see ISymbolKind
 * @author drogoul
 * @since 2 juin 2012
 *
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
@Inherited
public @interface inside {

	/**
	 * Symbols.
	 *
	 * @return the string[]
	 */
	String[] symbols() default {};

	/**
	 * Kinds.
	 *
	 * @return the int[]
	 */
	int[] kinds() default {};
}
/**
 * 
 */
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for signalling an implementation of gama.core.common.interfaces.IDisplaySurface as a potential
 * display surface for GAMA models
 *
 * @author drogoul
 *
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
@Inherited
public @interface display {

	/**
	 * The keyword that will allow to open this display in GAML (in "display type: keyword").
	 *
	 * @return
	 */
	String[] value();
}
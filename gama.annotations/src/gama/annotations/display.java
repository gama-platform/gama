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

	/**
	 * The Eclipse view ID of the concrete view that hosts this display surface. When non-empty,
	 * {@code LayeredDisplayOutput.getViewId()} will return this value instead of falling through to
	 * the built-in defaults. Leave empty (the default) to use the platform's built-in routing.
	 *
	 * @return the Eclipse view ID, or an empty string if not applicable
	 */
	String viewId() default "";

	/**
	 * Whether this display type renders in 3D. 3D displays use OpenGL and support
	 * camera controls, lighting, and depth rendering. Default is false.
	 *
	 * @return true if the display type is 3D
	 */
	boolean is3D() default false;
}
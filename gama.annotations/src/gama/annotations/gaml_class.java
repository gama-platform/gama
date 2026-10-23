/*******************************************************************************************************
 *
 * gaml_class.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers a Java implementation of a non-agent GAML class.
 *
 * Implementations must implement IObject. Concrete implementations must declare a public constructor taking (IScope, IClass).
 * Attributes and actions can be declared with @vars, @getter, @setter and @action. The closest registered
 * Java superclass becomes the GAML parent; otherwise the parent is object.
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
public @interface gaml_class {

	String name();

	String[] concept() default {};

	doc[] doc() default {};
}

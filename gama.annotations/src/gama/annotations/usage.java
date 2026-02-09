/**
 *
 */
package gama.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * The class @usage. This replaces @special_cases and @examples, and unifies the doc for operators, statements and
 * others.
 *
 * An @usage can also be used for defining a template for a GAML structure, and in that case requires the following to
 * be defined :
 *
 * A name (attribute "name"), optional, but better
 *
 * A description (attribute "value"), optional
 *
 * A menu name (attribute "menu"), optional
 *
 * A hierarchical path within this menu (attribute "path"), optional
 *
 * A pattern (attribute "pattern" or concatenation of the @example present in "examples" that define "isPattern" as
 * true)
 *
 * (see <code>org.eclipse.jface.text.templates.Template</code>) These templates are then classified and accessible
 * during runtime for editing models
 *
 *
 * @author Benoit Gaudou & Alexis Drogoul
 * @since 19 juin 2013 + 12/2014
 *
 */

@Retention (RetentionPolicy.RUNTIME)
// @Target({ ElementType.TYPE, ElementType.METHOD })
// @Inherited
public @interface usage {

	/** The Constant GENERAL. */
	static final String GENERAL = "General";

	/** The Constant STATEMENT. */
	static final String STATEMENT = "Statement";

	/** The Constant OPERATOR. */
	static final String OPERATOR = "Operator";

	/** The Constant MODEL. */
	static final String MODEL = "Model";

	/** The Constant SPECIES. */
	static final String SPECIES = "Species";

	/** The Constant EXPERIMENT. */
	static final String EXPERIMENT = "Experiment";

	/** The Constant DEFINITION. */
	static final String DEFINITION = "Attribute";

	/** The Constant CUSTOM. */
	static final String CUSTOM = "Custom";

	/** The Constant NULL. */
	static final String NULL = "";

	/**
	 * Value, the description of the usage.
	 *
	 * Note that for usages aiming at defining templates, the description is displayed on a tooltip in the editor. The
	 * use of the path allows to remove unecessary explanations. For instance, instead of writing : description= "This
	 * template illustrates the use of a complex form of the "create " statement, which reads agents from a shape file
	 * and uses the tabular data of the file to initialize their attributes"
	 *
	 * choose: name="Create agents from shapefile" menu=STATEMENT; path={"Create", "Complex forms"} description= "Read
	 * agents from a shape file and initialize their attributes"
	 *
	 * If no description is provided, GAMA will try to grab it from the context where the template is defined (in the
	 * documentation, for example)
	 *
	 *
	 * @return a String representing one usage of the keyword
	 */
	String value();

	/**
	 * Define the top-level menu where this template should appear. Users are free to use other names than the provided
	 * constants if necessary (i.e. "My templates"). When no menu is defined, GAMA tries to guess it from the context
	 * where the template is defined
	 */
	String menu()

	default NULL;

	/**
	 * The path indicates where to put this template in the menu. For instance, the following annotation:
	 *
	 * @template { menu = STATEMENT; path = {"Control", "If"} }
	 *
	 *           will put the template in a menu called "If", within "Control", within the top menu "Statement" When no
	 *           path is defined, GAMA will try to guess it from the context where the template is defined (i.e. keyword
	 *           of the statement, etc.)
	 *
	 */
	String[] path() default {};

	/**
	 * The name of the template should be both concise (as it will appear in a menu) and precise (to remove ambiguities
	 * between templates).
	 */
	String name()

	default NULL;

	/**
	 * Examples
	 *
	 * @return An array of String representing some examples or use-cases about how to use this element, related to the
	 *         particular usage above
	 */
	example[] examples() default {};

	/**
	 * Pattern. Alternatively, the contents of the usage can be descried using a @pattern (rather than an array
	 * of @example). The formatting of this string depends entirely on the user (e.g. including \n and \t for
	 * indentation, for instance).
	 */

	String pattern() default NULL;
}
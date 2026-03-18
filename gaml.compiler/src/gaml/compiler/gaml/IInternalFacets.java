/**
 *
 */
package gaml.compiler.gaml;

/**
 *
 */
public interface IInternalFacets {

	/** The origin. */
	String ORIGIN = "**origin**";

	/** The no type inference keyword. Used to flag declarations that have a type explicitly set */
	String NO_TYPE_INFERENCE = "**no_type_inference**";

	/** The gaml issue. */
	String GAML_ERROR = "**gaml_error**";

	/** The gaml warning. */
	String GAML_WARNING = "**gaml_warning**";

	/** The duplicate facet. */
	String DUPLICATE_FACET = "**duplicate_facet**";

}

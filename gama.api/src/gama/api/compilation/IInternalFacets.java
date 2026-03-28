/**
 *
 */
package gama.api.compilation;

/**
 *
 */
public interface IInternalFacets {

	/** The origin. */
	String ORIGIN = "__origin__";

	/** The no type inference keyword. Used to flag declarations that have a type explicitly set */
	String NO_TYPE_INFERENCE = "__no_type_inference__";

	/** The gaml issue. */
	String GAML_ERROR = "__gaml_error__";

	/** The gaml warning. */
	String GAML_WARNING = "__gaml_warning__";

	/** The duplicate facet. */
	String DUPLICATE_FACET = "__duplicate_facet__";

	/** The synthetic. */
	String SYNTHETIC = "__synthetic__";

	/** The synthetic resources prefix. */
	String SYNTHETIC_RESOURCES_PREFIX = "__synthetic__";

	/** The synthetic do target. */
	String INTERNAL_TARGET = "__target__";

	/** The synthetic do target species. */
	String SYNTHETIC_DO_TARGET_SPECIES = "__target_species__";

	/** The internal. */
	String INTERNAL = "__internal__";

	/** The internal function. */
	String INTERNAL_FUNCTION = "__function__";

	/** The internal name. */
	String INTERNAL_NAME = "__name__";

}

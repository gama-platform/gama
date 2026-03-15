/*******************************************************************************************************
 *
 * ContainerVariable.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.variables;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.types.IType;
import gama.api.gaml.variables.ContainerVariable.ContainerVarValidator;

/**
 * Represents a container variable declaration in GAMA, specifically for list, map, matrix, and other container types.
 *
 * <p>
 * ContainerVariable extends {@link Variable} to provide specialized handling for container types that require type
 * parameters. It supports the 'of' facet to specify the content type and the 'index' facet to specify the key type (for
 * maps and matrices).
 * </p>
 *
 * <h2>Container Types</h2>
 * <ul>
 * <li><b>list&lt;T&gt;</b> - Ordered collection of elements of type T</li>
 * <li><b>map&lt;K,V&gt;</b> - Key-value pairs with keys of type K and values of type V</li>
 * <li><b>matrix&lt;T&gt;</b> - 2D array with elements of type T</li>
 * <li><b>container&lt;T&gt;</b> - Generic container of elements of type T</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>List Variable</h3>
 *
 * <pre>{@code
 * species MySpecies {
 *     list<int> numbers <- [1, 2, 3, 4, 5];
 *     list<agent> neighbors <- [];
 * }
 * }</pre>
 *
 * <h3>Map Variable</h3>
 *
 * <pre>{@code
 * species MySpecies {
 *     map<string, int> scores <- ["Alice"::100, "Bob"::95];
 *     map<point, float> grid_values <- map([]);
 * }
 * }</pre>
 *
 * <h3>Matrix Variable</h3>
 *
 * <pre>{@code
 * global {
 *     matrix<float> elevation_data <- matrix_file("elevation.asc");
 * }
 * }</pre>
 *
 * <h3>Container with Update</h3>
 *
 * <pre>{@code
 * species MySpecies {
 *     list<agent> visible_agents update: agents at_distance 10;
 * }
 * }</pre>
 *
 * @see Variable for base variable functionality
 * @see NumberVariable for numeric variables with constraints
 *
 * @author Alexis Drogoul
 * @since GAMA 1.0
 */
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.NEW_VAR_ID,
				optional = false,
				doc = @doc ("The name of the attribute")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("The type of the attribute")),
				@facet (
						name = IKeyword.INIT,
						// AD 02/16 TODO Allow to declare ITypeProvider.OWNER_TYPE here
						type = IType.NONE,
						optional = true,
						doc = @doc ("The initial value of the attribute. Same as <- ")),
				@facet (
						name = "<-",
						internal = true,
						// AD 02/16 TODO Allow to declare ITypeProvider.OWNER_TYPE here
						type = IType.NONE,
						optional = true,
						doc = @doc ("The initial value of the attribute. Same as init:")),
				@facet (
						name = IKeyword.UPDATE,
						// AD 02/16 TODO Allow to declare ITypeProvider.OWNER_TYPE here
						type = IType.NONE,
						optional = true,
						doc = @doc ("An expression that will be evaluated each cycle to compute a new value for the attribute")),
				@facet (
						name = IKeyword.FUNCTION,
						// AD 02/16 TODO Allow to declare ITypeProvider.OWNER_TYPE here
						type = IType.NONE,
						optional = true,
						doc = @doc ("Used to specify an expression that will be evaluated each time the attribute is accessed. Equivalent to '->'. This facet is incompatible with both 'init:', 'update:' and 'on_change:' (or the equivalent final block)")),
				@facet (
						name = "->",
						internal = true,
						type = { IType.INT, IType.FLOAT, IType.POINT, IType.DATE },
						optional = true,
						doc = @doc ("Used to specify an expression that will be evaluated each time the attribute is accessed. Equivalent to 'function:'. This facet is incompatible with both 'init:' and 'update:' and 'on_change:' (or the equivalent final block)")),
				@facet (
						name = IKeyword.CONST,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Indicates whether this attribute can be subsequently modified or not")),
				@facet (
						name = IKeyword.ON_CHANGE,
						type = IType.NONE,
						optional = true,
						doc = @doc ("Provides a block of statements that will be executed whenever the value of the attribute changes")),

				@facet (
						name = IKeyword.OF,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("The type of the contents of this container attribute")),
				@facet (
						name = IKeyword.INDEX,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("The type of the key used to retrieve the contents of this attribute")), },
		omissible = IKeyword.NAME)
@symbol (
		kind = ISymbolKind.CONTAINER,
		with_sequence = false,
		concept = { IConcept.CONTAINER })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL, ISymbolKind.CLASS })
@doc ("Declaration of an attribute of a species or an experiment")
@validator (ContainerVarValidator.class)
public class ContainerVariable extends Variable {

	/**
	 * Validator for container variable descriptions.
	 *
	 * <p>
	 * This validator extends {@link Variable.VarValidator} to apply the same validation rules as regular variables.
	 * Container-specific validation (e.g., checking 'of' and 'index' facet compatibility) is handled during type
	 * resolution.
	 * </p>
	 *
	 * @see Variable.VarValidator
	 */
	public static class ContainerVarValidator extends VarValidator {

		/**
		 * Validates a container variable description by delegating to the parent validator.
		 *
		 * @param vd
		 *            the variable description to validate
		 *
		 * @see Variable.VarValidator#validate(IDescription)
		 */
		@Override
		public void validate(final IDescription vd) {
			super.validate(vd);
		}
	}

	/**
	 * Constructs a new ContainerVariable from its description.
	 *
	 * <p>
	 * This constructor calls the parent {@link Variable} constructor which extracts all standard facets. The
	 * container-specific 'of' and 'index' facets are processed during type resolution to build the parameterized
	 * container type.
	 * </p>
	 *
	 * @param sd
	 *            the variable description containing facets including 'of' and 'index'
	 *
	 * @see Variable#Variable(IDescription)
	 */
	public ContainerVariable(final IDescription sd) {
		super(sd);
	}

}

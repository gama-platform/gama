/*******************************************************************************************************
 *
 * ContainerVariable.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.variables;

import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.gaml.types.IType;
import gama.api.gaml.variables.ContainerVariable.ContainerVarValidator;
import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;

/**
 * The Class ContainerVariable.
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
						doc = @doc ("The type of the key used to retrieve the contents of this attribute")), 
		},
		omissible = IKeyword.NAME)
@symbol (
		kind = ISymbolKind.Variable.CONTAINER,
		with_sequence = false,
		concept = { IConcept.CONTAINER })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL })
@doc ("Declaration of an attribute of a species or an experiment")
@validator (ContainerVarValidator.class)
public class ContainerVariable extends Variable {

	/**
	 * The Class ContainerVarValidator.
	 */
	public static class ContainerVarValidator extends VarValidator {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription vd) {
			super.validate(vd);
		}
	}

	/**
	 * Instantiates a new container variable.
	 *
	 * @param sd
	 *            the sd
	 */
	public ContainerVariable(final IDescription sd) {
		super(sd);
	}

}

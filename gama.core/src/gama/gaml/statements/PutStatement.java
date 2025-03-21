/*******************************************************************************************************
 *
 * PutStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaPair;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.graph.IGraph;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolSerializer;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.statements.PutStatement.PutSerializer;
import gama.gaml.statements.PutStatement.PutValidator;
import gama.gaml.types.IType;

/**
 * Written by drogoul Modified on 6 févr. 2010
 *
 * @todo Description
 *
 */

@facets (
		value = { @facet (
				name = IKeyword.AT,
				type = IType.NONE,
				optional = true,
				doc = @doc ("the key or index at which to put the new value is specified by `container[index]`")),
				@facet (
						name = IKeyword.KEY,
						type = IType.NONE,
						optional = true,
						doc = @doc ("the key or index at which to put the new value is specified by `container[index]`")),
				@facet (
						name = IKeyword.ALL,
						type = IType.NONE,
						optional = true,
						doc = @doc ("when no index is specified between the square brackets, the put assignement applies to all elements and changes their value to the one provided")),
				@facet (
						name = IKeyword.ITEM,
						type = IType.NONE,
						optional = true,
						doc = @doc ("the right member of the put assignment ('cont[index] <- expr;') is an expression expr that evaluates to the element(s) to be put in the container")),
				@facet (
						name = IKeyword.IN,
						type = { IType.CONTAINER, IType.SPECIES, IType.AGENT, IType.GEOMETRY },
						optional = false,
						doc = @doc ("the left member of the put assignment ('cont[index] <- expr;') is an expression cont that evaluates to a container (list, map, matrix). It makes no sense for graphs ")) },
		omissible = IKeyword.ITEM)
@symbol (
		name = IKeyword.PUT,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.CONTAINER, IConcept.MAP, IConcept.MATRIX, IConcept.LIST })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER },
		symbols = IKeyword.CHART)
@validator (PutValidator.class)
@doc (
		value = "A statement used to put items into containers at specific keys or indices. It can be written using the classic syntax (`put ... in: ...`) or a compact one, which is now preferred."
				+ "\n- To put an element in a container at a given index, use `container[index] <- element;` (classic form: `put element in: container at: index;`) "
				+ "\n- To put an element in a container at all indices (i.e. replace all values by the element),  use `container[] <- element` (classic form: `put element in: container all: true;`)",
		usages = { @usage (
				value = "The allowed  configurations are the following ones:",
				examples = { @example (
						value = "expr_container[index] <- expr; // put or replace expr at index in the container",
						isExecutable = false),
						@example (
								value = "expr_container[] <- expr;  // put expr at every index in the container (replace all values)",
								isExecutable = false) }),
				@usage (
						value = "In the case of a list, the position should be an integer in the bounds of the list. The facet all: is used to replace all the elements of the list by the given value.",
						examples = { @example (
								var = "putList",
								value = "[1,2,3,4,5]",
								returnType = "list<int>",
								equals = "[1,2,3,4,5]"),
								@example (
										value = "putList[1] <- -10;",
										var = "putList",
										equals = "[1,-10,3,4,5]"),
								@example (
										value = "putList[] <- 10;",
										var = "putList",
										equals = "[10,10,10,10,10]") }),
				@usage (
						value = "In the case of a matrix, the position should be a point in the bounds of the matrix. If no position is provided, it is used to replace all the elements of the matrix by the given value.",
						examples = { @example (
								var = "putMatrix",
								value = "matrix([[0,1],[2,3]])",
								returnType = "matrix<int>",
								equals = "matrix([[0,1],[2,3]])"),
								@example (
										value = "putMatrix[{1,1}] <- -10;",
										var = "putMatrix",
										equals = "matrix([[0,1],[2,-10]]);"),
								@example (
										value = "putMatrix[] <- 10;",
										var = "putMatrix",
										equals = "matrix([[10,10],[10,10]])") }),
				@usage (
						value = "In the case of a map, the position should be one of the key values of the map. Notice that if the given key value does not exist in the map, a	new pair key::value will be added to the map. The facet all is used to replace the value of all the pairs of the map.",
						examples = { @example (
								var = "putMap",
								value = "[\"x\"::4,\"y\"::7]",
								returnType = "map<string,int>",
								equals = "[\"x\"::4,\"y\"::7]"),
								@example (
										value = "putMap['y'] <- -10;",
										var = "putMap",
										equals = "[\"x\"::4,\"y\"::-10]"),
								@example (
										value = "putMap['z'] <- -20;",
										var = "putMap",
										equals = "[\"x\"::4,\"y\"::-10, \"z\"::-20]"),
								@example (
										value = "putMap[] <- -30 ;",
										var = "putMap",
										equals = "[\"x\"::-30,\"y\"::-30, \"z\"::-30]") }) })
@serializer (PutSerializer.class)
public class PutStatement extends AddStatement {

	/**
	 * The Class PutSerializer.
	 */
	public static class PutSerializer extends SymbolSerializer<SymbolDescription> {

		@Override
		protected void serialize(final SymbolDescription cd, final StringBuilder sb, final boolean includingBuiltIn) {
			final IExpression item = cd.getFacetExpr(ITEM);
			final IExpression list = cd.getFacetExpr(TO);
			// IExpression allFacet = f.getExpr(ALL);
			final IExpression at = cd.getFacetExpr(AT);
			sb.append(list.serializeToGaml(includingBuiltIn));
			sb.append('[');
			if (at != null) { sb.append(at.serializeToGaml(includingBuiltIn)); }
			sb.append(']');
			sb.append(" <- ");
			sb.append(item.serializeToGaml(includingBuiltIn)).append(';');
		}
	}

	/**
	 * The Class PutValidator.
	 */
	public static class PutValidator extends ContainerValidator {

		@Override
		public void validate(final IDescription cd) {
			final IExpression index = cd.getFacetExpr(AT, KEY);
			final IExpression whole = cd.getFacetExpr(ALL);
			if (whole != null && whole.getGamlType().id() != IType.BOOL) {
				cd.error("Put cannot be used to add several values", IGamlIssue.CONFLICTING_FACETS, ALL);
				return;
			}
			final boolean all = whole == null ? false : !FALSE.equals(whole.literalValue());
			if (!all && index == null) {
				cd.error("Put needs a valid index (facets 'at:' or 'key:') ", IGamlIssue.MISSING_FACET,
						cd.getUnderlyingElement(), AT, "0");
			} else {
				super.validate(cd);
			}
		}

	}

	/**
	 * Instantiates a new put statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public PutStatement(final IDescription desc) {
		super(desc);
		setName("put in " + list.serializeToGaml(false));
	}

	@Override
	protected Object buildValue(final IScope scope, final IGraph container) {
		// if ( asAllValues ) { return container.buildValues(scope, (IContainer)
		// this.item.value(scope), containerType);
		// }
		// AD: Added to fix issue 1043: the value computed by maps is a pair
		// (whose key is never used afterwards). However,
		// when casting an existing pair to the key type/content type of the
		// map, this would produce wrong values for the
		// contents of the pair (or the list with 2 elements).
		// O1/02/14: Not useful anymore
		// if ( this.list.getType().id() == IType.MAP ) { return
		// container.buildValue(scope,
		// new GamaPair(null, this.item.value(scope))); }
		return container.buildValue(scope, this.item.value(scope));
	}

	@Override
	protected void apply(final IScope scope, final Object object, final Object position,
			final IContainer.Modifiable container) throws GamaRuntimeException {
		if (!asAll) {
			// if (!container.checkBounds(scope, position, false)) throw GamaRuntimeException
			// .error("Index " + position + " out of bounds of " + list.serialize(false), scope);
			// Issue #3099
			if (container instanceof IList && position instanceof GamaPair) {
				((IList<Object>) container).replaceRange(scope, (GamaPair) position, object);
			} else {
				container.setValueAtIndex(scope, position, object);
			}
		} else {
			container.setAllValues(scope, object);
		}
	}
}

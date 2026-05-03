/*******************************************************************************************************
 *
 * PutStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.serializer;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.graph.IGraph;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.types.pair.IPair;
import gama.gaml.statements.PutStatement.PutSerializer;
import gama.gaml.statements.PutStatement.PutValidator;

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
		value = """
				A statement used to put items into containers at specific keys or indices. It can be written using the classic syntax (`put ... in: ...`) or a compact one, which is now preferred.\

				- To put an element in a container at a given index, use `container[index] <- element;` (classic form: `put element in: container at: index;`) \

				- To put an element in a container at all indices (i.e. replace all values by the element),  use `container[] <- element` (classic form: `put element in: container all: true;`)""",
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
	public static class PutSerializer implements ISymbolSerializer {

		@Override
		public void serialize(final IDescription cd, final StringBuilder sb, final boolean includingBuiltIn) {
			final IExpression item = cd.getFacetExpr(ITEM);
			final IExpression list = cd.getFacetExpr(TO);
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
		return container.buildValue(scope, this.item.value(scope));
	}

	@Override
	protected void apply(final IScope scope, final Object object, final Object position,
			final IContainer.Modifiable container) throws GamaRuntimeException {
		if (!asAll) {
			if (container instanceof IList && position instanceof IPair) {
				((IList<Object>) container).replaceRange(scope, (IPair) position, object);
			} else {
				container.setValueAtIndex(scope, position, object);
			}
		} else {
			container.setAllValues(scope, object);
		}
	}
}

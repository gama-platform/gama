/*******************************************************************************************************
 *
 * AbstractContainerStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.statements.AbstractContainerStatement.ContainerValidator;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.IGraph;
import gama.api.types.misc.IContainer;

/**
 * Abstract base class for statements that manipulate containers (add, remove, put).
 * 
 * <p>
 * This class provides the foundation for container manipulation statements in GAML, handling the complex validation
 * and execution logic for adding, removing, or updating elements in lists, maps, matrices, and graphs.
 * </p>
 * 
 * <h2>Supported Operations</h2>
 * <ul>
 * <li><b>add:</b> Adds elements to a container</li>
 * <li><b>remove:</b> Removes elements from a container</li>
 * <li><b>put:</b> Sets elements at specific positions/keys</li>
 * </ul>
 * 
 * <h2>Container Types</h2>
 * <p>
 * Works with all GAML container types:
 * </p>
 * <ul>
 * <li>Lists: ordered collections</li>
 * <li>Maps: key-value associations</li>
 * <li>Matrices: 2D arrays</li>
 * <li>Graphs: nodes and edges</li>
 * <li>Agent attributes: dynamic property maps</li>
 * </ul>
 * 
 * <h2>Validation</h2>
 * <p>
 * The {@link ContainerValidator} performs extensive compile-time validation:
 * </p>
 * <ul>
 * <li>Type compatibility between items and container content type</li>
 * <li>Type compatibility between indices and container key type</li>
 * <li>Proper usage of 'all:' facet for batch operations</li>
 * <li>Detection of attempts to modify fixed-length containers</li>
 * <li>Special handling for graph operations (edges, nodes)</li>
 * </ul>
 * 
 * <h2>Example GAML Usage</h2>
 * <pre>
 * {@code
 * // Add to list
 * add new_agent to: agent_list;
 * 
 * // Remove from list
 * remove first(agent_list) from: agent_list;
 * 
 * // Put in map
 * put "value" at: "key" in: my_map;
 * 
 * // Add all from another container
 * add all: other_list to: my_list;
 * 
 * // Add edge to graph
 * add edge: (node1, node2) to: my_graph weight: 5.0;
 * }
 * </pre>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see AbstractStatement
 * @see IContainer
 */
@validator (ContainerValidator.class)
@SuppressWarnings ({ "rawtypes" })
public abstract class AbstractContainerStatement extends AbstractStatement {

	/**
	 * Validator for container manipulation statements.
	 * 
	 * <p>
	 * Performs complex validation including type checking, facet compatibility verification, and special case
	 * handling for graphs and agent variables.
	 * </p>
	 */
	public static class ContainerValidator implements IDescriptionValidator {

		/**
		 * Validates a container manipulation statement description.
		 * 
		 * <p>
		 * This method normalizes facets, checks type compatibility, and emits warnings for potentially problematic
		 * usages.
		 * </p>
		 *
		 * @param cd
		 *            the statement description to validate
		 */
		@Override
		public void validate(final IDescription cd) {

			final IExpression item = findItem(cd);
			final IExpression list = findList(cd);
			final IExpression index = findIndex(cd);
			final IExpression whole = findAll(cd);

			/**
			 * After these operations, the statement should be provided with: ITEM: the object to add/remove/put ALL: a
			 * boolean indicating whether (or not) to treat the operation as "all" TO: the container to change AT: the
			 * index at which the operation should be done (if any)
			 *
			 * All other facets are then meaningless. As a consequence, ITEM is never null except in the case of the
			 * "remove all: true from: ..."
			 */

			final String keyword = cd.getKeyword();
			final boolean all = whole == null ? false : !FALSE.equals(whole.literalValue());
			if (item == null && !all && !REMOVE.equals(keyword) || list == null) {
				cd.error("The assignment appears uncomplete", IGamlIssue.GENERAL);
				return;
			}
			if (ADD.equals(keyword) || REMOVE.equals(keyword)) {
				final IType containerType = list.getGamlType();
				if (containerType.isFixedLength()) {
					cd.error("Impossible to add/remove to/from " + list.serializeToGaml(false), IGamlIssue.WRONG_TYPE);
					return;
				}
			}
			/**
			 * Warnings for agent variables
			 */
			if (index != null && list.getGamlType().isAgentType() && index.isConst()) {
				final String s = index.literalValue();
				final ISpeciesDescription sd = list.getGamlType().getSpecies();
				if (sd.hasAttribute(s)) {
					if (PUT.equals(keyword)) {
						cd.warning(
								"Attribute '" + s + "' will not be modified by this statement. Use '"
										+ list.serializeToGaml(false) + "." + s + "' instead",
								IGamlIssue.WRONG_CONTEXT);
					} else if (REMOVE.equals(keyword)) {
						cd.warning("Attribute '" + s + "' cannot be removed. ", IGamlIssue.WRONG_CONTEXT);
					}
				}

			}

			// TODO Add an error if list is a VarExpression and is not
			// modifiable
			// TODO Add an error if both item and whole are != null and whole is
			// not a boolean
			// TODO Add an error if both index and all are != null and item ==
			// null
			// TODO Add an error if graph and index not instance of pair
			// TODO Change the warning for graph indexes (like for maps?)
			validateIndexAndContentTypes(keyword, cd, all);

		}

		/**
		 * Normalizes the 'all:' facet.
		 * 
		 * <p>
		 * If 'all:' receives a container value instead of a boolean, that value is moved to 'item:' and 'all:' is set
		 * to true.
		 * </p>
		 *
		 * @param cd
		 *            the statement description
		 * @return the normalized 'all:' expression
		 */
		private IExpression findAll(final IDescription cd) {
			// If a container/value is passed to ALL, then it is copied to ITEM
			// and ALL is set to "true"
			final IExpressionDescription wholeDesc = cd.getFacet(ALL);
			final IExpression whole = wholeDesc == null ? null : wholeDesc.getExpression();
			if (whole != null && whole.getGamlType().id() != IType.BOOL) {
				cd.setFacetExprDescription(ITEM, wholeDesc);
				cd.removeFacets(ALL);
				cd.setFacet(ALL, GAML.getExpressionFactory().getTrue());
			}
			return whole;
		}

		/**
		 * Normalizes index/key facets.
		 * 
		 * <p>
		 * Merges 'at:', 'key:', and 'index:' facets into a single 'at:' facet for uniform handling.
		 * </p>
		 *
		 * @param cd
		 *            the statement description
		 * @return the normalized index expression
		 */
		private IExpression findIndex(final IDescription cd) {
			final IExpressionDescription indexDesc = cd.getFacet(AT, KEY, INDEX);
			final IExpression index = indexDesc == null ? null : indexDesc.getExpression();
			if (index != null) {
				cd.setFacetExprDescription(AT, indexDesc);
				cd.removeFacets(KEY, INDEX);
			}
			return index;
		}

		/**
		 * Normalizes container target facets.
		 * 
		 * <p>
		 * Merges 'to:', 'from:', and 'in:' facets into a single 'to:' facet for uniform handling.
		 * </p>
		 *
		 * @param cd
		 *            the statement description
		 * @return the normalized container expression
		 */
		private IExpression findList(final IDescription cd) {
			final IExpressionDescription listDesc = cd.getFacet(TO, FROM, IN);
			final IExpression list = listDesc == null ? null : listDesc.getExpression();
			if (list != null) {
				cd.setFacetExprDescription(TO, listDesc);
				cd.removeFacets(FROM, IN);
			}
			return list;
		}

		/**
		 * Normalizes item facets and handles special graph operations.
		 * 
		 * <p>
		 * This method:
		 * </p>
		 * <ul>
		 * <li>Merges 'item:', 'edge:', 'vertex:', and 'node:' facets</li>
		 * <li>Transforms edge/node facets into proper edge()/node() operator calls</li>
		 * <li>Handles weight facets for graph operations</li>
		 * </ul>
		 *
		 * @param cd
		 *            the statement description
		 * @return the normalized item expression
		 */
		private IExpression findItem(final IDescription cd) {
			// 17/02/14: We change the facets to simplify the writing of
			// statements. EDGE and VERTEX are removed
			final IExpressionDescription itemDesc = cd.getFacet(ITEM, EDGE, VERTEX, NODE);
			IExpression item = itemDesc == null ? null : itemDesc.getExpression();
			if (item != null) {
				if (cd.hasFacet(EDGE)) {
					if (cd.hasFacet(WEIGHT)) {
						item = GAML.getExpressionFactory().createOperator("edge", cd, cd.getFacet(EDGE).getTarget(),
								item, cd.getFacetExpr(WEIGHT));
					} else {
						item = GAML.getExpressionFactory().createOperator("edge", cd, cd.getFacet(EDGE).getTarget(),
								item);
					}
					cd.removeFacets(EDGE, WEIGHT);
				} else if (cd.hasFacet(VERTEX) || cd.hasFacet(NODE)) {
					final boolean isNode = cd.hasFacet(NODE);
					if (cd.hasFacet(WEIGHT)) {
						item = GAML.getExpressionFactory().createOperator("node", cd,
								isNode ? cd.getFacet(NODE).getTarget() : cd.getFacet(VERTEX).getTarget(), item,
								cd.getFacetExpr(WEIGHT));
					} else {
						item = GAML.getExpressionFactory().createOperator("node", cd,
								isNode ? cd.getFacet(NODE).getTarget() : cd.getFacet(VERTEX).getTarget(), item);
					}
					cd.removeFacets(VERTEX, NODE, WEIGHT);
				}
				// itemDesc.setExpression(item);
				cd.setFacet(ITEM, item);
			}
			return item;
		}

		/**
		 * @param list
		 * @param item
		 * @param index
		 * @param whole
		 */
		public void validateIndexAndContentTypes(final String keyword, final IDescription cd, final boolean all) {
			final IExpression item = cd.getFacetExpr(ITEM);
			final IExpression list = cd.getFacetExpr(TO);
			// IExpression whole = cd.getFacets().getExpr(ALL);
			final IExpression index = cd.getFacetExpr(AT);

			if (list instanceof IOperator bo && "internal_between".equals(bo.getName())) {
				// Corresponds to a wrong usage of the range with add, remove operators
				cd.error("Ranges of indices can only be used in conjunction with `put` or `<-`",
						IGamlIssue.CONFLICTING_FACETS, IKeyword.AT);
				return;
			}

			if (!REMOVE.equals(keyword)) {
				if (item == null) // we are in the case "remove all: true from...". Nothing to
					// validate
					return;
				final IType<?> contentType = list.getGamlType().getContentType();
				boolean isAll = false;
				IType<?> valueType;
				if (!PUT.equals(keyword) && all && item.getGamlType().isTranslatableInto(Types.CONTAINER)) {
					isAll = true;
					valueType = item.getGamlType().getContentType();
				} else {
					valueType = item.getGamlType();
				}

				if (contentType != Types.NO_TYPE && !valueType.isTranslatableInto(contentType)
						&& !Types.isEmptyContainerCase(contentType, item)) {
					StringBuilder message =
							new StringBuilder("The type of the elements of ").append(list.serializeToGaml(false))
									.append(" (").append(contentType).append(") does not match with the type of the ");
					if (isAll) {
						message.append("elements of the argument");
					} else {
						message.append("argument");
					}
					message.append(" (").append(valueType).append("). ");
					if (isAll) {
						message.append("These elements will be casted to ").append(contentType).append(". ");
					} else {
						message.append("The argument will be casted to ").append(contentType).append(". ");
					}
					cd.warning(message.toString(), IGamlIssue.SHOULD_CAST, IKeyword.ITEM,
							isAll ? list.getGamlType().toString() : contentType.toString());
				}
				final IType<?> keyType = list.getGamlType().getKeyType();
				if (index != null && keyType != Types.NO_TYPE && !index.getGamlType().isTranslatableInto(keyType)) {
					if (Types.LIST.isAssignableFrom(list.getGamlType())
							&& Types.PAIR.of(Types.INT, Types.INT).equals(index.getGamlType()))
						return;
					// These indices are accepted for matrices (int and list<int>)
					if (!Types.MATRIX.isAssignableFrom(list.getGamlType()) || index.getGamlType() != Types.INT
							&& !index.getGamlType().equals(Types.LIST.of(Types.INT))) {
						cd.warning(
								"The type of the index of " + list.serializeToGaml(false) + " (" + keyType
										+ ") does not match with the type of " + index.serializeToGaml(false) + " ("
										+ index.getGamlType() + "). The latter will be casted to " + keyType,
								IGamlIssue.SHOULD_CAST, IKeyword.AT, keyType.toString());
					}
				}
			}
		}
	}

	/** Expression for the item(s) to add/remove/put. */
	protected IExpression item;
	
	/** Expression for the index/key where to perform the operation. */
	protected IExpression index;
	
	/** Expression for the container to modify. */
	protected IExpression list;
	
	/** Expression indicating batch operation mode. */
	protected IExpression all;

	/** Indicates if all items from a container should be processed (true when all: receives a container). */
	protected final boolean asAll;

	/** Indicates if items should be treated as a collection of values to add/remove individually. */
	protected final boolean asAllValues;

	/** Indicates if indices should be treated as a collection of indices. */
	protected final boolean asAllIndexes;

	/** Indicates if the container expression directly yields a modifiable container (vs. an agent/shape). */
	final boolean isDirect;
	
	/** Indicates if the container is a graph (requires special handling for edges/nodes). */
	final boolean isGraph;

	/**
	 * Constructs a new container manipulation statement.
	 * 
	 * <p>
	 * This constructor extracts and normalizes all facets, determines operation mode flags, and identifies the
	 * container type for specialized handling.
	 * </p>
	 *
	 * @param desc
	 *            the statement description
	 */
	public AbstractContainerStatement(final IDescription desc) {
		super(desc);

		item = getFacet(IKeyword.ITEM);
		index = getFacet(IKeyword.AT);
		all = getFacet(IKeyword.ALL);
		list = getFacet(IKeyword.TO);

		asAll = all != null && IKeyword.TRUE.equals(all.literalValue());
		asAllValues = asAll && item != null && item.getGamlType().isTranslatableInto(Types.CONTAINER);
		asAllIndexes = asAll && index != null && index.getGamlType().isTranslatableInto(Types.CONTAINER);
		final IType<?> t = list.getGamlType();
		isDirect = t.isContainer();
		isGraph = t.isTranslatableInto(Types.GRAPH);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		// We then identify the container
		final IContainer.Modifiable container = identifyContainer(scope);

		final Object position = identifyIndex(scope, container);
		final Object object = identifyValue(scope, container);
		// And apply the operation (add, put or remove)
		apply(scope, object, position, container);
		// Added fix for Issue 1048 (dont change the value of temp variables
		// If the list is an attribute of an agent, we change its value
		if (isDirect && list instanceof IVarExpression.Agent) {
			((IVarExpression) list).setVal(scope, container, false);
		}
		// The defaut return value is the changed container
		return container;

	}

	/**
	 * Identify value.
	 *
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 * @return the object
	 */
	protected Object identifyValue(final IScope scope, final IContainer.Modifiable container) {
		if (item == null) return null;
		// For the moment, only graphs need to recompute their objects
		// GamaFloatMatrix and GamaField need too, as GAML happily accepts int ...

		if (container.getGamlType().id() == IType.MATRIX) return Cast.asFloat(scope, item.value(scope));
		if (isGraph) return buildValue(scope, (IGraph) container);
		return item.value(scope);
	}

	/**
	 * Identify index.
	 *
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 * @return the object
	 */
	protected Object identifyIndex(final IScope scope, final IContainer.Modifiable container) {
		if (index == null) return null;
		if (isGraph) return buildIndex(scope, (IGraph) container);
		return index.value(scope);
	}

	/**
	 * Builds the value.
	 *
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 * @return the object
	 */
	protected Object buildValue(final IScope scope, final IGraph container) {
		if (asAllValues) return container.buildValues(scope, (IContainer.Modifiable) this.item.value(scope));
		return container.buildValue(scope, this.item.value(scope));
	}

	/**
	 * Builds the index.
	 *
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 * @return the object
	 */
	protected Object buildIndex(final IScope scope, final IGraph container) {
		if (asAllIndexes) return container.buildIndexes(scope, (IContainer.Modifiable) this.index.value(scope));
		return container.buildIndex(scope, this.index.value(scope));
	}

	/**
	 * @throws GamaRuntimeException
	 * @return the container to which this command will be applied
	 */
	private IContainer.Modifiable identifyContainer(final IScope scope) throws GamaRuntimeException {
		final Object cont = list.value(scope);
		if (isDirect) return (IContainer.Modifiable) cont;
		if (cont instanceof IShape) return ((IShape) cont).getOrCreateAttributes();
		throw GamaRuntimeException.warning("Cannot use " + list.serializeToGaml(false) + ", of type "
				+ list.getGamlType().toString() + ", as a container", scope);
	}

	/**
	 * Method to add, remove or put one individual item
	 *
	 * @param scope
	 * @param object
	 * @param position
	 * @param container
	 * @throws GamaRuntimeException
	 */
	protected abstract void apply(IScope scope, Object object, Object position, IContainer.Modifiable container)
			throws GamaRuntimeException;

}
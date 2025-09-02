/*******************************************************************************************************
 *
 * AbstractContainerStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.core.util.graph.IGraph;
import gama.core.util.matrix.GamaFloatMatrix;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IExpressionFactory;
import gama.gaml.expressions.IVarExpression;
import gama.gaml.expressions.operators.BinaryOperator;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractContainerStatement.ContainerValidator;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Written by drogoul Modified on 24 ao�t 2010
 *
 * @todo Description
 *
 */
@validator (ContainerValidator.class)
@SuppressWarnings ({ "rawtypes" })
public abstract class AbstractContainerStatement extends AbstractStatement {

	/**
	 * The Class ContainerValidator.
	 */
	public static class ContainerValidator implements IDescriptionValidator {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
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
				final TypeDescription sd = list.getGamlType().getSpecies();
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
		 * Find all.
		 *
		 * @param cd
		 *            the cd
		 * @return the i expression
		 */
		private IExpression findAll(final IDescription cd) {
			// If a container/value is passed to ALL, then it is copied to ITEM
			// and ALL is set to "true"
			final IExpressionDescription wholeDesc = cd.getFacet(ALL);
			final IExpression whole = wholeDesc == null ? null : wholeDesc.getExpression();
			if (whole != null && whole.getGamlType().id() != IType.BOOL) {
				cd.setFacetExprDescription(ITEM, wholeDesc);
				cd.removeFacets(ALL);
				cd.setFacet(ALL, IExpressionFactory.TRUE_EXPR);
			}
			return whole;
		}

		/**
		 * Find index.
		 *
		 * @param cd
		 *            the cd
		 * @return the i expression
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
		 * Find list.
		 *
		 * @param cd
		 *            the cd
		 * @return the i expression
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
		 * Find item.
		 *
		 * @param cd
		 *            the cd
		 * @return the i expression
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

			if (list instanceof BinaryOperator && "internal_between".equals(((BinaryOperator) list).getName())) {
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

	/** The all. */
	protected IExpression item, index, list, all;

	/** The as all indexes. */
	final boolean asAll, asAllValues, asAllIndexes;
	// Identifies whether or not the container is directly modified by the
	/** The is graph. */
	// statement or if it is a shape or an agent
	final boolean isDirect, isGraph;

	// The "real" container type
	// final IContainerType containerType;

	// private static final IType attributesType = Types.MAP.of(Types.STRING,
	// Types.NO_TYPE);

	/**
	 * Instantiates a new abstract container statement.
	 *
	 * @param desc
	 *            the desc
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
		if (container instanceof GamaFloatMatrix) return Cast.asFloat(scope, item.value(scope));
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
		if (asAllValues) return container.buildValues(scope, (IContainer) this.item.value(scope));
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
		if (asAllIndexes) return container.buildIndexes(scope, (IContainer) this.index.value(scope));
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
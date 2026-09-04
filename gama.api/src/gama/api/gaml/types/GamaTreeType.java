/*******************************************************************************************************
 *
 * GamaTreeType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.constants.IKeyword;
import gama.annotations.doc;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.annotations.type;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.tree.GamaTreeFactory;
import gama.api.types.tree.ITree;
import gama.api.utils.collections.GamaTree;

/**
 * Type representing hierarchical trees in GAML.
 */
@type (
		name = IKeyword.TREE,
		id = IType.TREE,
		wraps = { ITree.class, GamaTree.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE, IConcept.CONTAINER },
		doc = @doc ("Specialized container representing a hierarchical tree structure of nodes."))
public class GamaTreeType extends GamaContainerType<ITree> {

	/**
	 * Constructs a new tree type.
	 *
	 * @param typesManager
	 *            the types manager
	 */
	public GamaTreeType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	public ITree cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaTreeFactory.castToTree(scope, obj, param, copy);
	}

	@Override
	public int getNumberOfParameters() { return 1; }

	@Override
	public boolean canCastToConst() {
		return false;
	}

}

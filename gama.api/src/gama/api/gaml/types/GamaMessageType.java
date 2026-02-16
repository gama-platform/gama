/*******************************************************************************************************
 *
 * GamaMessageType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.message.GamaMessageFactory;
import gama.api.types.message.IMessage;

/**
 * The Class GamaMessageType.
 */
@type (
		name = GamaMessageType.MESSAGE_STR,
		id = IType.MESSAGE,
		wraps = { IMessage.class },
		kind = ISymbolKind.Variable.REGULAR,
		doc = @doc ("Represents the messages exchanged between agents"))
public class GamaMessageType extends GamaType<IMessage> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaMessageType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/** The Constant MESSAGE_STR. */
	public static final String MESSAGE_STR = "message";

	@Override
	public IMessage getDefault() { return null; }

	@Override
	protected boolean acceptNullInstances() {
		return true;
	}

	@Override
	@doc ("Returns a message built from the argument. If the argument is already a message returns it, otherwise returns a message with the current agent as the sender and the argument as the contents ")
	public IMessage cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaMessageFactory.castToMessage(scope, scope.getAgent(), obj);
	}

	@Override
	public boolean canCastToConst() {
		return false;
	}
}

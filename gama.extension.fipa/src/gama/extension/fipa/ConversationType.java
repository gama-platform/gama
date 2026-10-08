/*******************************************************************************************************
 *
 * ConversationType.java, in gama.extension.fipa, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.fipa;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;

/**
 * The Class ConversationType.
 */
@type (
		name = ConversationType.CONVERSATION_STR,
		id = ConversationType.CONV_ID,
		wraps = { Conversation.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE, IConcept.FIPA },
		doc = @doc ("Represents a list of the messages exchanged by agents"))
public class ConversationType extends GamaContainerType<Conversation> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public ConversationType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/** The Constant CONVERSATION_STR. */
	public final static String CONVERSATION_STR = "conversation";

	/** The Constant CONV_ID. */
	public final static int CONV_ID = 98;

	@Override

	@doc ("Converts the operand into a conversation (if applicable) or retrieves the conversation of the message passed. Otherwise returns nil")
	public Conversation cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentType, final boolean copy) throws GamaRuntimeException {
		return staticCast(scope, obj, param);
	}

	@Override
	public IType<FIPAMessage> getContentType() { return Types.get(FIPAMessage.class); }

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param object
	 *            the object
	 * @return the conversation
	 */
	public static Conversation staticCast(final IScope scope, final Object val, final Object object) {
		if (val instanceof Conversation) return (Conversation) val;
		if (val instanceof FIPAMessage) return ((FIPAMessage) val).getConversation();
		// ???
		return null;
	}

	@Override
	public boolean canCastToConst() {
		return false;
	}
}

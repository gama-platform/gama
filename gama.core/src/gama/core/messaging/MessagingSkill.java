/*******************************************************************************************************
 *
 * MessagingSkill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.messaging;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.gaml.skills.GamlSkill;
import gama.gaml.types.IType;

/**
 * The Class MessagingSkill.
 */
@skill (
		name = MessagingSkill.SKILL_NAME,

		concept = {IConcept.COMMUNICATION, IConcept.SKILL })
@doc ("A simple skill that provides agents with a mailbox than can be filled with messages")
@vars ({ @variable (
		name = "mailbox",
		type = IType.LIST,
		of = IType.MESSAGE,
		doc = @doc ("The list of messages that can be consulted by the agent")) })
public class MessagingSkill extends GamlSkill {

	/** The Constant SKILL_NAME. */
	public static final String SKILL_NAME = "messaging";

	/** The Constant MAILBOX. */
	public static final String MAILBOX = "mailbox";

	/** The Constant MAILBOX_ATTRIBUTE. */
	public static final String MAILBOX_ATTRIBUTE = "messaging_skill_mailbox";

	/**
	 * Gets the mailbox.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @return the mailbox
	 */
	@getter (
			value = MAILBOX,
			initializer = true)
	public GamaMailbox getMailbox(final IScope scope, final IAgent agent) {
		GamaMailbox mailbox = (GamaMailbox) agent.getAttribute(MAILBOX_ATTRIBUTE);
		if (mailbox == null) {
			mailbox = createNewMailbox();
			agent.setAttribute(MAILBOX_ATTRIBUTE, mailbox);
		}
		return mailbox;
	}

	/**
	 * Sets the mailbox.
	 *
	 * @param agent
	 *            the agent
	 * @param mailbox
	 *            the mailbox
	 */
	@setter (MAILBOX)
	public void setMailbox(final IAgent agent, final GamaMailbox mailbox) {
		// not allowed
	}

	/**
	 * Prim send message.
	 *
	 * @param scope
	 *            the scope
	 * @return the gama message
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "send",
			args = { @arg (
						name = IKeyword.TO,
						type = IType.NONE,
						optional = true,
						doc = @doc ("The agent, or server, to which this message will be sent to")),
					@arg (
						name = GamaMessage.CONTENTS,
						type = IType.NONE,
						optional = false,
						doc = @doc ("The contents of the message, an arbitrary object")
					) },
			doc = @doc(
				value = "Action used to send a message (that can be of any kind of object) to an agent or a server.",
				examples = {@example("do send to:dest contents:\"This message is sent by \" + name + \" to \" + dest;")}))
	public GamaMessage primSendMessage(final IScope scope) throws GamaRuntimeException {
		final IAgent sender = scope.getAgent();
		Object receiver = scope.getArg("to", IType.NONE);
		if (receiver == null) { receiver = sender; }
		final Object contents = effectiveContents(scope, scope.getArg(GamaMessage.CONTENTS, IType.NONE));
		if (contents == null) return null;
		final GamaMessage message = createNewMessage(scope, sender, receiver, contents);
		effectiveSend(scope, message, receiver);
		return message;
	}

	/**
	 * Effective contents.
	 *
	 * @param scope
	 *            the scope
	 * @param contents
	 *            the contents
	 * @return the object
	 */
	protected Object effectiveContents(final IScope scope, final Object contents) {
		return contents;
	}

	/**
	 * Creates the new mailbox.
	 *
	 * @return the gama mailbox
	 */
	protected GamaMailbox createNewMailbox() {
		return new GamaMailbox();
	}

	/**
	 * Creates the new message.
	 *
	 * @param scope
	 *            the scope
	 * @param sender
	 *            the sender
	 * @param receivers
	 *            the receivers
	 * @param contents
	 *            the contents
	 * @return the gama message
	 */
	protected GamaMessage createNewMessage(final IScope scope, final Object sender, final Object receivers,
			final Object contents) {
		return new GamaMessage(scope, sender, receivers, contents);
	}

	/**
	 * Effective send.
	 *
	 * @param scope
	 *            the scope
	 * @param message
	 *            the message
	 * @param receiver
	 *            the receiver
	 */
	@SuppressWarnings ({ "rawtypes", "unchecked" })
	protected void effectiveSend(final IScope scope, final GamaMessage message, final Object receiver) {
		if (receiver instanceof IAgent agent) {
			if (agent.isInstanceOf(SKILL_NAME, false)) {
				final GamaMailbox<GamaMessage> mailbox =
						(GamaMailbox<GamaMessage>) agent.getAttribute(MAILBOX_ATTRIBUTE);
				mailbox.addMessage(scope, message);
			}
		} else if (receiver instanceof IList) {
			for (final Object o : ((IList) receiver).iterable(scope)) { effectiveSend(scope, message.copy(scope), o); }
		}
	}
}

/*******************************************************************************************************
 *
 * Connector.java, in gama.extension.network, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.network.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.message.IMessage;
import gama.extension.network.skills.INetworkSkill;
import gama.extension.serialize.binary.BinarySerialisation;

/**
 * The Class Connector.
 *
 * <h3>Thread-safety design</h3>
 * <p>
 * The previous design used a single {@code lockGroupManagment} object and a magic-integer dispatch method
 * ({@code pushAndFetchthreadSafe}) to serialise two conceptually distinct operations:
 * </p>
 * <ol>
 * <li><b>Pushing</b> an incoming message into agents' mailboxes — guards {@link #receivedMessage} and
 * {@link #boxFollower} together.</li>
 * <li><b>Fetching</b> (atomically swapping) the entire mailbox map — also guards {@link #receivedMessage}.</li>
 * </ol>
 * <p>
 * Both operations share the same state ({@link #receivedMessage}), so they must still share the same lock to remain
 * mutually exclusive. The refactoring replaces the magic-integer dispatch with two clearly named methods
 * ({@link #pushMessage} and {@link #fetchAllMessages}) that each acquire the same dedicated {@link ReentrantLock}. This
 * makes the locking intent explicit and removes the unused code-paths that the old {@code switch} reserved for future
 * constants that were never added.
 * </p>
 * <p>
 * {@link #topicSuscribingPending} is replaced with a {@link CopyOnWriteArrayList} so that iteration (which dominates)
 * is lock-free; writes (subscribe/unsubscribe) pay the copy cost only on changes.
 * </p>
 */
public abstract class Connector implements IConnector {

	// -------------------------------------------------------------------------
	// Fields
	// -------------------------------------------------------------------------

	/** The connection parameter. */
	protected Map<String, String> connectionParameter;

	/**
	 * Maps a group/topic name to the list of agents that follow it. Written only during {@link #joinAGroup} /
	 * {@link #leaveTheGroup} (single-threaded control flow), so a plain HashMap is sufficient.
	 */
	protected Map<String, ArrayList<IAgent>> boxFollower;

	/**
	 * Maps each agent to its incoming message queue. All compound read-modify-write operations on this map are guarded
	 * by {@link #messageLock}.
	 */
	protected Map<IAgent, LinkedList<ConnectorMessage>> receivedMessage;

	/** Maps network agent names to local agent objects. */
	protected Map<String, IAgent> localMemberNames;

	/**
	 * Topics for which a subscribe request is pending. {@link CopyOnWriteArrayList} is used because iteration (checking
	 * pending topics) is far more frequent than writes (add/remove on connect/disconnect), and it removes the need for
	 * a {@link java.util.Collections#synchronizedList} wrapper.
	 */
	protected List<String> topicSuscribingPending;

	/** The is connected. */
	protected boolean isConnected = false;

	/**
	 * Dedicated lock that serialises the two compound operations on {@link #receivedMessage}: {@link #pushMessage}
	 * (deliver to mailboxes) and {@link #fetchAllMessages} (atomic swap). A named field — rather than
	 * {@code synchronized(this)} — makes the locking scope explicit.
	 */
	private final ReentrantLock messageLock = new ReentrantLock();

	/** The force network use. */
	boolean forceNetworkUse = false;

	/** message is raw or composite. */
	private boolean isRaw = false;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Instantiates a new connector.
	 */
	protected Connector() {
		boxFollower = new HashMap<>();
		topicSuscribingPending = new CopyOnWriteArrayList<>();
		connectionParameter = new HashMap<>();
		receivedMessage = new HashMap<>();
		localMemberNames = new HashMap<>();
		forceNetworkUse = false;
	}

	// -------------------------------------------------------------------------
	// Configuration / life-cycle
	// -------------------------------------------------------------------------

	@Override
	public void forceNetworkUse(final boolean b) {
		this.forceNetworkUse = b;
	}

	@Override
	public void configure(final String parameterName, final String value) {
		this.connectionParameter.put(parameterName, value);
	}

	/**
	 * Gets the configuration parameter.
	 *
	 * @param name
	 *            the parameter name
	 * @return the parameter value, or {@code null} if absent
	 */
	protected String getConfigurationParameter(final String name) {
		return this.connectionParameter.get(name);
	}

	/** Sets the connector status to connected. */
	protected void setConnected() {
		this.isConnected = true;
	}

	// -------------------------------------------------------------------------
	// Message routing — the two formerly magic-dispatch operations
	// -------------------------------------------------------------------------

	/**
	 * Delivers {@code message} to all agents that follow {@code groupName} (or the "ALL" group as a fallback). This
	 * operation is mutually exclusive with {@link #fetchAllMessages()} because both operate on
	 * {@link #receivedMessage}.
	 *
	 * @param groupName
	 *            the destination group / topic name
	 * @param message
	 *            the message to deliver
	 */
	private void pushMessage(final String groupName, final ConnectorMessage message) {
		messageLock.lock();
		try {
			final ArrayList<IAgent> bb = this.boxFollower.get(groupName) == null ? this.boxFollower.get("ALL")
					: this.boxFollower.get(groupName);
			if (bb == null) return;
			for (final IAgent agt : bb) {
				final LinkedList<ConnectorMessage> messages = receivedMessage.get(agt);
				if (messages != null) { messages.add(message); }
			}
		} finally {
			messageLock.unlock();
		}
	}

	/**
	 * Atomically swaps the current mailbox map with a fresh empty one and returns the old map so that callers can drain
	 * it without contending with concurrent deliveries.
	 *
	 * <p>
	 * This operation is mutually exclusive with {@link #pushMessage} because both operate on {@link #receivedMessage}.
	 * </p>
	 *
	 * @return the previous mailbox map, keyed by agent, with all pending messages
	 */
	@Override
	public Map<IAgent, LinkedList<ConnectorMessage>> fetchAllMessages() {
		messageLock.lock();
		try {
			final Map<IAgent, LinkedList<ConnectorMessage>> newBox = new HashMap<>();
			for (final IAgent agt : this.receivedMessage.keySet()) { newBox.put(agt, new LinkedList<>()); }
			final Map<IAgent, LinkedList<ConnectorMessage>> allMessages = this.receivedMessage;
			this.receivedMessage = newBox;
			return allMessages;
		} finally {
			messageLock.unlock();
		}
	}

	// -------------------------------------------------------------------------
	// IConnector implementation
	// -------------------------------------------------------------------------

	@Override
	public List<ConnectorMessage> fetchMessageBox(final IAgent agent) {
		final List<ConnectorMessage> currentMessage = receivedMessage.get(agent);
		this.receivedMessage.put(agent, new LinkedList<>());
		return currentMessage;
	}

	/**
	 * Store message.
	 *
	 * @param sender
	 *            the sender identifier
	 * @param topic
	 *            the topic / group name
	 * @param content
	 *            the raw message content
	 * @throws GamaNetworkException
	 *             the gama network exception
	 */
	public void storeMessage(final String sender, final String topic, final String content)
			throws GamaNetworkException {
		final ConnectorMessage msg = MessageFactory.unPackNetworkMessage(sender, topic, content);
		if (!this.localMemberNames.containsKey(msg.getSender())) { pushMessage(msg.getReceiver(), msg); }
	}

	@Override
	public void send(final IAgent sender, final String receiver, final IMessage content) {
		if (!this.forceNetworkUse && this.boxFollower.containsKey(receiver)) {
			final ConnectorMessage msg =
					new LocalMessage((String) sender.getAttribute(INetworkSkill.NET_AGENT_NAME), receiver, content);
			pushMessage(receiver, msg);
			// Fix for #3335
			return;
		}

		if (!this.localMemberNames.containsKey(receiver)) {
			if (!isRaw()) {
				if (content.getSender() instanceof IAgent) {
					content.setSender(sender.getAttribute(INetworkSkill.NET_AGENT_NAME));
				}
				final NetworkMessage msg =
						MessageFactory.buildNetworkMessage((String) sender.getAttribute(INetworkSkill.NET_AGENT_NAME),
								receiver, BinarySerialisation.saveToString(sender.getScope(), content));
				this.sendMessage(sender, receiver, MessageFactory.packMessage(msg));
			} else {
				this.sendMessage(sender, receiver, content.getContents(sender.getScope()).toString());
			}
		}
	}

	@Override
	public void close(final IScope scope) throws GamaNetworkException {
		releaseConnection(scope);
		topicSuscribingPending.clear();
		boxFollower.clear();
		receivedMessage.clear();
		isConnected = false;
	}

	@Override
	public void leaveTheGroup(final IAgent agt, final String groupName) {
		this.unsubscribeGroup(agt, groupName);
		final ArrayList<IAgent> members = this.boxFollower.get(groupName);
		if (members != null) {
			members.remove(agt);
			if (members.size() == 0) { this.boxFollower.remove(groupName); }
		}
	}

	@Override
	public void joinAGroup(final IAgent agt, final String groupName) {
		if (!this.receivedMessage.containsKey(agt)) { this.receivedMessage.put(agt, new LinkedList<>()); }

		ArrayList<IAgent> agentBroadcast = this.boxFollower.get(groupName);

		if (agentBroadcast == null) {
			this.subscribeToGroup(agt, groupName);
			agentBroadcast = new ArrayList<>();
			this.boxFollower.put(groupName, agentBroadcast);
		}
		if (!agentBroadcast.contains(agt)) {
			agentBroadcast.add(agt);
			this.subscribeToGroup(agt, groupName);
		}
	}

	@Override
	public void connect(final IAgent agent) throws GamaNetworkException {
		final String netAgent = (String) agent.getAttribute(INetworkSkill.NET_AGENT_NAME);
		if (!this.forceNetworkUse && !this.localMemberNames.containsKey(netAgent)) {
			this.localMemberNames.put(netAgent, agent);
		}
		if (!this.isConnected) { connectToServer(agent); }
		if (this.receivedMessage.get(agent) == null && !isRaw()) { joinAGroup(agent, netAgent); }
	}

	@Override
	public boolean isRaw() { return isRaw; }

	@Override
	public void setRaw(final boolean isRaw) { this.isRaw = isRaw; }

	// -------------------------------------------------------------------------
	// Abstract hooks
	// -------------------------------------------------------------------------

	/**
	 * Connect to server.
	 *
	 * @param agent
	 *            the agent
	 * @throws GamaNetworkException
	 *             the gama network exception
	 */
	protected abstract void connectToServer(IAgent agent) throws GamaNetworkException;

	/**
	 * Checks if is alive.
	 *
	 * @param agent
	 *            the agent
	 * @return true, if is alive
	 * @throws GamaNetworkException
	 *             the gama network exception
	 */
	protected abstract boolean isAlive(final IAgent agent) throws GamaNetworkException;

	/**
	 * Subscribe to group.
	 *
	 * @param agt
	 *            the agt
	 * @param boxName
	 *            the box name
	 * @throws GamaNetworkException
	 *             the gama network exception
	 */
	protected abstract void subscribeToGroup(IAgent agt, String boxName) throws GamaNetworkException;

	/**
	 * Unsubscribe group.
	 *
	 * @param agt
	 *            the agt
	 * @param boxName
	 *            the box name
	 * @throws GamaNetworkException
	 *             the gama network exception
	 */
	protected abstract void unsubscribeGroup(IAgent agt, String boxName) throws GamaNetworkException;

	/**
	 * Release connection.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaNetworkException
	 *             the gama network exception
	 */
	protected abstract void releaseConnection(final IScope scope) throws GamaNetworkException;

	/**
	 * Send message.
	 *
	 * @param sender
	 *            the sender
	 * @param receiver
	 *            the receiver
	 * @param content
	 *            the content
	 * @throws GamaNetworkException
	 *             the gama network exception
	 */
	protected abstract void sendMessage(final IAgent sender, final String receiver, final String content)
			throws GamaNetworkException;

}

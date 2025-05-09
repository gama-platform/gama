/*******************************************************************************************************
 *
 * NetworkSkill.java, in gama.extension.network, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.network.skills;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.core.messaging.GamaMailbox;
import gama.core.messaging.GamaMessage;
import gama.core.messaging.MessagingSkill;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.dev.DEBUG;
import gama.extension.network.common.ConnectorMessage;
import gama.extension.network.common.IConnector;
import gama.extension.network.httprequest.HTTPRequestConnector;
import gama.extension.network.mqtt.MQTTConnector;
import gama.extension.network.serial.ArduinoConnector;
import gama.extension.network.tcp.TCPConnector;
import gama.extension.network.udp.UDPConnector;
import gama.extension.network.websocket.WebSocketConnector;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;

/**
 * The Class NetworkSkill.
 */
@vars ({ @variable (
		name = INetworkSkill.NET_AGENT_NAME,
		type = IType.STRING,
		doc = @doc ("Net ID of the agent")),
		@variable (
				name = INetworkSkill.NET_AGENT_GROUPS,
				type = IType.LIST,
				doc = @doc ("The set of groups the agent belongs to")),
		@variable (
				name = INetworkSkill.NET_AGENT_SERVER,
				type = IType.LIST,
				doc = @doc ("The list of all the servers to which the agent is connected")) })
@skill (
		name = INetworkSkill.NETWORK_SKILL,
		concept = { IConcept.NETWORK, IConcept.COMMUNICATION, IConcept.SKILL })
@doc ("The " + INetworkSkill.NETWORK_SKILL
		+ " skill provides new features to let agents exchange message through network. "
		+ "Sending and receiving data is done with the " + MessagingSkill.SKILL_NAME + " skill's actions.")
public class NetworkSkill extends MessagingSkill {

	static {
		DEBUG.OFF();
	}

	/** The Constant REGISTERED_AGENTS. */
	final static String REGISTERED_AGENTS = "registered_agents";

	/** The Constant REGISTRED_SERVER. */
	final static String REGISTERED_SERVER = "registered_servers";

	/**
	 * System exec.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	@action (
			name = "execute",
			args = { @arg (
					name = "command",
					type = IType.STRING,
					doc = @doc ("command to execute")) },
			doc = @doc (
					deprecated = "use 'command' instead",
					value = "Action that executes a command in the OS, as if it is executed from a terminal.",
					returns = "The error message if any"))
	public String systemExec(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String commandToExecute = (String) scope.getArg("command", IType.STRING);

		return gama.gaml.operators.System.console(scope, commandToExecute);

		// // String res = "";
		//
		// Process p;
		// try {
		// p = Runtime.getRuntime().exec(commandToExecute);
		//
		// final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		// return stdError.readLine();
		// } catch (final IOException e) {
		//
		// e.printStackTrace();
		// }
		// return "";

	}

	/**
	 * Connect to server.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("unchecked")
	@action (
			name = INetworkSkill.CONNECT,
			args = { @arg (
					name = INetworkSkill.PROTOCOL,
					type = IType.STRING,
					doc = @doc ("protocol type (MQTT (by default), TCP, UDP, websocket, arduino): the possible value ares '"
							+ INetworkSkill.UDP_SERVER + "', '" + INetworkSkill.UDP_CLIENT + "', '"
							+ INetworkSkill.TCP_SERVER + "', '" + INetworkSkill.TCP_CLIENT + "', '"
							+ INetworkSkill.WEBSOCKET_SERVER + "', '" + INetworkSkill.WEBSOCKET_CLIENT + "', '"
							+ INetworkSkill.HTTP_REQUEST + "', '" + INetworkSkill.ARDUINO
							+ "', otherwise the MQTT protocol is used.")),
					@arg (
							name = INetworkSkill.PORT,
							type = IType.INT,
							doc = @doc ("Port number")),
					@arg (
							name = INetworkSkill.RAW,
							type = IType.BOOL,
							doc = @doc ("message type raw or rich")),
					@arg (
							name = INetworkSkill.WITHNAME,
							type = IType.STRING,
							optional = true,
							doc = @doc ("ID of the agent (its name) for the simulation")),
					@arg (
							name = INetworkSkill.LOGIN,
							type = IType.STRING,
							optional = true,
							doc = @doc ("login for the connection to the server")),
					@arg (
							name = INetworkSkill.PASSWORD,
							type = IType.STRING,
							optional = true,
							doc = @doc ("password associated to the login")),
					@arg (
							name = INetworkSkill.FORCE_NETWORK_USE,
							type = IType.BOOL,
							optional = true,
							doc = @doc ("force the use of the network even interaction between local agents")),
					@arg (
							name = INetworkSkill.SERVER_URL,
							type = IType.STRING,
							optional = true,
							doc = @doc ("server URL (localhost or a server URL)")),
					@arg (
							name = INetworkSkill.MAX_DATA_PACKET_SIZE,
							type = IType.INT,
							optional = true,
							doc = @doc ("For UDP connection, it sets the maximum size of received packets (default = 1024bits).")) },
			doc = @doc (
					value = "Action used by a networking agent to connect to a server or to create a server.",
					examples = { @example (" do connect with_name:\"any_name\";"),
							@example (" do connect to:\"localhost\" port:9876 with_name:\"any_name\";"),
							@example (" do connect to:\"localhost\" protocol:\"MQTT\" port:9876 with_name:\"any_name\";"),
							@example (" do connect to:\"localhost\" protocol:\"udp_server\" port:9876 with_name:\"Server\";"),
							@example (" do connect to:\"localhost\" protocol:\"udp_client\" port:9876 with_name:\"Client\";"),
							@example (" do connect to:\"localhost\" protocol:\"udp_server\" port:9877 size_packet: 4096;"),
							@example (" do connect to:\"localhost\" protocol:\"tcp_client\" port:9876;"),
							@example (" do connect to:\"localhost\" protocol:\"tcp_server\" port:9876 raw:true;"),
							@example (" do connect to: \"https://openlibrary.org\" protocol: \"http\" port: 443 raw: true;"),
							@example (" do connect protocol: \"arduino\";"), }))
	public boolean connectToServer(final IScope scope) throws GamaRuntimeException {
		if (!scope.getExperiment().hasAttribute(REGISTERED_SERVER)) { this.startSkill(scope); }
		final IAgent agt = scope.getAgent();
		final String serverURL = (String) scope.getArg(INetworkSkill.SERVER_URL, IType.STRING);
		final String login = (String) scope.getArg(INetworkSkill.LOGIN, IType.STRING);
		final String password = (String) scope.getArg(INetworkSkill.PASSWORD, IType.STRING);
		final String networkName = (String) scope.getArg(INetworkSkill.WITHNAME, IType.STRING);
		final String protocol = (String) scope.getArg(INetworkSkill.PROTOCOL, IType.STRING);
		final Boolean force_local = (Boolean) scope.getArg(INetworkSkill.FORCE_NETWORK_USE, IType.BOOL);
		final Boolean raw_package = (Boolean) scope.getArg(INetworkSkill.RAW, IType.BOOL);
		final Integer port = (Integer) scope.getArg(INetworkSkill.PORT, IType.INT);
		final String packet_size = (String) scope.getArg(INetworkSkill.MAX_DATA_PACKET_SIZE, IType.STRING);

		// Fix to Issue #2618
		final String serverKey = createServerKey(serverURL, port);

		final Map<String, IConnector> myConnectors = this.getRegisteredServers(scope);
		IConnector connector = myConnectors.get(serverKey);
		if (connector == null) {
			switch (protocol) {
				case INetworkSkill.UDP_SERVER:
					DEBUG.OUT("create UDP server");
					connector = new UDPConnector(scope, true);
					connector.configure(IConnector.SERVER_URL, serverURL);
					connector.configure(IConnector.SERVER_PORT, "" + port);
					connector.configure(IConnector.PACKET_SIZE, packet_size);
					break;

				case INetworkSkill.UDP_CLIENT:
					DEBUG.OUT("create UDP client");
					connector = new UDPConnector(scope, false);
					connector.configure(IConnector.SERVER_URL, serverURL);
					connector.configure(IConnector.SERVER_PORT, "" + port);
					connector.configure(IConnector.PACKET_SIZE, "" + packet_size);
					break;

				case INetworkSkill.WEBSOCKET_SERVER:
					DEBUG.OUT("create WebSocket server");
					connector = new WebSocketConnector(scope, true, raw_package);
					connector.configure(IConnector.SERVER_URL, serverURL);
					connector.configure(IConnector.SERVER_PORT, "" + port);
					break;

				case INetworkSkill.WEBSOCKET_CLIENT:
					DEBUG.OUT("create WebSocket client");
					connector = new WebSocketConnector(scope, false, raw_package);
					connector.configure(IConnector.SERVER_URL, serverURL);
					connector.configure(IConnector.SERVER_PORT, "" + port);
					break;

				case INetworkSkill.TCP_SERVER:
					DEBUG.OUT("create TCP serveur");
					connector = new TCPConnector(scope, true, raw_package);
					connector.configure(IConnector.SERVER_URL, serverURL);
					connector.configure(IConnector.SERVER_PORT, "" + port);
					break;

				case INetworkSkill.TCP_CLIENT:
					DEBUG.OUT("create TCP client");
					connector = new TCPConnector(scope, false, raw_package);
					connector.configure(IConnector.SERVER_URL, serverURL);
					connector.configure(IConnector.SERVER_PORT, "" + port);
					break;

				case INetworkSkill.ARDUINO:
					connector = new ArduinoConnector(scope);
					connector.configure(IConnector.SERVER_URL, serverURL);
					connector.configure(IConnector.SERVER_PORT, "" + port);
					break;

				case INetworkSkill.HTTP_REQUEST:
					connector = new HTTPRequestConnector(scope);
					connector.configure(IConnector.SERVER_URL, serverURL);
					connector.configure(IConnector.SERVER_PORT, "" + port);
					break;

				case null, default:
					connector = new MQTTConnector(scope, raw_package);
					DEBUG.OUT("create MQTT server " + login + " " + password);
					if (serverURL == null) {
						break; // If no server given we fallback on the default connection
					}
					connector.configure(IConnector.SERVER_URL, serverURL);
					connector.configure(IConnector.SERVER_PORT, port == 0 ? "1883" : port.toString());

					if (login != null) { connector.configure(IConnector.LOGIN, login); }
					if (password != null) { connector.configure(IConnector.PASSWORD, password); }
					break;
			}

			if (force_local != null) { connector.forceNetworkUse(force_local); }
			// Fix to Issue #2618
			myConnectors.put(serverKey, connector);

		}

		if (agt.getAttribute(INetworkSkill.NET_AGENT_NAME) == null) {
			agt.setAttribute(INetworkSkill.NET_AGENT_NAME, networkName);
		}

		List<String> serverList = (List<String>) agt.getAttribute(INetworkSkill.NET_AGENT_SERVER);
		if (serverList == null) {
			serverList = new ArrayList<>();
			agt.setAttribute(INetworkSkill.NET_AGENT_SERVER, serverList);
		}
		DEBUG.OUT("connector " + connector);
		try {
			connector.connect(agt);
		} catch (Exception e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(e, scope), false);
			return false;
		}

		serverList.add(serverKey);

		// register connected agent to global groups;
		for (final String grp : INetworkSkill.DEFAULT_GROUP) {
			this.joinAGroup(scope, agt, grp);
			DEBUG.OUT(grp);
			// connector.joinAGroup(agt, grp);
		}
		return true;
	}

	/**
	 * Disconnect.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@action (
			name = INetworkSkill.DISCONNECT,
			doc = @doc (
					value = "Disconnects from all the servers previously connected to. Will return true if everything went well, false in case of an error."))
	public Boolean disconnect(final IScope scope) {
		final Map<String, IConnector> connectors = this.getRegisteredServers(scope);
		boolean no_problem = true;
		var to_remove = new ArrayList<String>();
		for (var entry : connectors.entrySet()) {
			try {
				var connector = entry.getValue();
				connector.close(scope);
				connector.leaveTheGroup(scope.getAgent(), REGISTERED_AGENTS);
				to_remove.add(entry.getKey());
			} catch (Exception ex) {
				no_problem = false;
			}
		}

		for (var connector : to_remove) { connectors.remove(connector); }

		return no_problem;

	}

	/**
	 * Creates the server key.
	 *
	 * @param serverURL
	 *            the server URL
	 * @param port
	 *            the port
	 * @return the string
	 */
	private static String createServerKey(final String serverURL, final Integer port) {
		return serverURL + "@@" + port;
	}

	/**
	 * Fetch message.
	 *
	 * @param scope
	 *            the scope
	 * @return the gama message
	 */
	@SuppressWarnings ("unchecked")
	@action (
			name = INetworkSkill.FETCH_MESSAGE,
			doc = @doc (
					value = "Fetch the first message from the mailbox (and remove it from the mailing box). If the mailbox is empty, it returns a nil message.",
					examples = { @example ("message mess <- fetch_message();"), @example ("""
							loop while:has_more_message(){\s
								message mess <- fetch_message();
								write message.contents;
							}""") }))
	public GamaMessage fetchMessage(final IScope scope) {
		final IAgent agent = scope.getAgent();
		final GamaMailbox<GamaMessage> box = getMailbox(scope, agent);
		GamaMessage msg = null;
		if (!box.isEmpty()) {
			msg = box.get(0);
			box.remove(0);
		}
		return msg;
	}

	/**
	 * Checks for more message.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	@SuppressWarnings ("unchecked")
	@action (
			name = INetworkSkill.HAS_MORE_MESSAGE_IN_BOX,
			doc = @doc (
					value = "Check whether the mailbox contains any message.",
					examples = { @example ("bool mailbox_contain_messages <- has_more_message();"), @example ("""
							loop while:has_more_message(){\s
								message mess <- fetch_message();
								write message.contents;
							}""") }))
	public boolean hasMoreMessage(final IScope scope) {
		final IAgent agent = scope.getAgent();
		final GamaMailbox<GamaMessage> box = getMailbox(scope, agent);
		return !box.isEmpty();
	}

	/**
	 * Register to group.
	 *
	 * @param scope
	 *            the scope
	 */
	@action (
			name = INetworkSkill.REGISTER_TO_GROUP,
			args = {

					@arg (
							name = INetworkSkill.WITHNAME,
							type = IType.STRING,
							optional = false,
							doc = @doc ("name of the group")) },
			doc = @doc (
					value = "Allow an agent to join a group of agents in order to broadcast messages to other members "
							+ "or to receive messages sent by other members. Note that all members of the group called : \"ALL\".",
					examples = { @example ("do join_group with_name:\"group name\";"),
							@example ("do join_group with_name:\"group name\";\n"
									+ "do send to:\"group name\" contents:\"I am new in this group\";") }))
	public boolean registerToGroup(final IScope scope) {
		final IAgent agent = scope.getAgent();
		final String groupName = (String) scope.getArg(INetworkSkill.WITHNAME, IType.STRING);
		if (groupName != null) {
			joinAGroup(scope, agent, groupName);
			return true;
		}
		return false;
	}

	/**
	 * Gets the groups.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @return the groups
	 */
	@SuppressWarnings ("unchecked")
	private IList<String> getGroups(final IScope scope, final IAgent agent) {
		IList<String> groups = Cast.asList(scope, agent.getAttribute(INetworkSkill.NET_AGENT_GROUPS));
		if (groups == null) {
			groups = GamaListFactory.create();
			agent.setAttribute(INetworkSkill.NET_AGENT_GROUPS, groups);
		}
		return groups;

	}

	/**
	 * Join A group.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param groupName
	 *            the group name
	 */
	public void joinAGroup(final IScope scope, final IAgent agent, final String groupName) {
		final IList<String> groups = getGroups(scope, agent);
		groups.add(groupName);

		final Collection<IConnector> connectors = getRegisteredServers(scope).values();
		for (final IConnector con : connectors) { con.joinAGroup(agent, groupName); }
	}

	/**
	 * Leave the group.
	 *
	 * @param scope
	 *            the scope
	 */
	@action (
			name = INetworkSkill.LEAVE_THE_GROUP,
			args = { @arg (
					name = INetworkSkill.WITHNAME,
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the group the agent wants to leave")) },
			doc = @doc (
					value = "leave a group of agents. The leaving agent will not receive any "
							+ "message from the group. Otherwise, it can send messages to the left group",
					examples = { @example (" do leave_group with_name:\"my_group\";") }))
	public boolean leaveTheGroup(final IScope scope) {
		final IAgent agent = scope.getAgent();
		final String groupName = (String) scope.getArg(INetworkSkill.WITHNAME, IType.STRING);
		if (groupName == null) return false;
		final IList<String> groups = getGroups(scope, agent);

		groups.remove(groupName);
		final Collection<IConnector> connectors = getRegisteredServers(scope).values();
		for (final IConnector con : connectors) { con.leaveTheGroup(agent, groupName); }
		return true;
	}

	@SuppressWarnings ({ "unchecked", "rawtypes" })
	@Override
	protected void effectiveSend(final IScope scope, final GamaMessage message, final Object receiver) {
		if (receiver instanceof IList) {
			for (final Object o : ((IList) receiver).iterable(scope)) { effectiveSend(scope, message.copy(scope), o); }
		}
		String destName = receiver.toString();
		if (receiver instanceof final IAgent mReceiver && getRegisteredAgents(scope).contains(receiver)) {
			destName = (String) mReceiver.getAttribute(INetworkSkill.NET_AGENT_SERVER);
		}

		final IAgent agent = scope.getAgent();
		final List<String> serverNames = (List<String>) agent.getAttribute(INetworkSkill.NET_AGENT_SERVER);
		final Map<String, IConnector> connections = getRegisteredServers(scope);
		for (final String servName : serverNames) { connections.get(servName).send(agent, destName, message); }
	}

	/**
	 * Fetch messages of agents.
	 *
	 * @param scope
	 *            the scope
	 */
	@action (
			name = INetworkSkill.FETCH_MESSAGE_FROM_NETWORK,
			doc = @doc (
					value = "Fetch all messages from network to mailbox. Use this in specific case only, this action is done at the end of each step. ",
					examples = {
							@example ("""
									do fetch_message_from_network;//forces gama to get all the new messages since the begining of the cycle
									loop while: has_more_message(){\s
										message mess <- fetch_message();
										write message.contents;
									}""") }))
	public boolean fetchMessagesOfAgents(final IScope scope) {

		for (final IConnector connection : getRegisteredServers(scope).values()) {
			final Map<IAgent, LinkedList<ConnectorMessage>> messages = connection.fetchAllMessages();
			for (final IAgent agt : messages.keySet()) {
				@SuppressWarnings ("unchecked") final GamaMailbox<GamaMessage> mailbox =
						(GamaMailbox<GamaMessage>) agt.getAttribute(MAILBOX_ATTRIBUTE);

				// to be check....
				/*
				 * if (!(connection instanceof MQTTConnector)) { mailbox.clear(); }
				 */
				var list = messages.containsKey(agt) ? messages.get(agt) : null;
				if (list == null) return true;
				for (final ConnectorMessage msg : messages.get(agt)) {
					mailbox.addMessage(scope, msg.getContents(scope));
				}
			}
		}
		return true;
	}

	/**
	 * Gets the registered agents.
	 *
	 * @param scope
	 *            the scope
	 * @return the registered agents
	 */
	@SuppressWarnings ("unchecked")
	protected List<IAgent> getRegisteredAgents(final IScope scope) {
		return (List<IAgent>) scope.getExperiment().getAttribute(REGISTERED_AGENTS);
	}

	/**
	 * Gets the registered servers.
	 *
	 * @param scope
	 *            the scope
	 * @return the registered servers
	 */
	@SuppressWarnings ("unchecked")
	protected Map<String, IConnector> getRegisteredServers(final IScope scope) {
		return (Map<String, IConnector>) scope.getExperiment().getAttribute(REGISTERED_SERVER);
	}

	/**
	 * Initialize.
	 *
	 * @param scope
	 *            the scope
	 */
	private void initialize(final IScope scope) {
		scope.getExperiment().setAttribute(REGISTERED_AGENTS, new ArrayList<>());
		scope.getExperiment().setAttribute(REGISTERED_SERVER, new HashMap<>());
	}

	/**
	 * Start skill.
	 *
	 * @param scope
	 *            the scope
	 */
	protected void startSkill(final IScope scope) {
		initialize(scope);
		registerSimulationEvent(scope);
	}

	/**
	 * Register simulation event.
	 *
	 * @param scope
	 *            the scope
	 */
	private void registerSimulationEvent(final IScope scope) {
		scope.getSimulation().postEndAction(scope1 -> {
			fetchMessagesOfAgents(scope1);
			return null;
		});

		scope.getSimulation().postDisposeAction(scope1 -> {
			closeAllConnection(scope1);
			return null;
		});
	}

	/**
	 * Close all connection.
	 *
	 * @param scope
	 *            the scope
	 */
	private void closeAllConnection(final IScope scope) {
		for (final IConnector connection : getRegisteredServers(scope).values()) { connection.close(scope); }
		this.initialize(scope);
	}

}

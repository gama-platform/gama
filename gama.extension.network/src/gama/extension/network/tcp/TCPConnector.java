/*******************************************************************************************************
 *
 * TCPConnection.java, in gama.network, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.network.tcp;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import gama.core.messaging.GamaMessage;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.extension.network.common.CommandMessage;
import gama.extension.network.common.Connector;
import gama.extension.network.common.GamaNetworkException;
import gama.extension.network.common.IConnector;
import gama.extension.network.common.MessageFactory;
import gama.extension.network.common.CommandMessage.CommandType;
import gama.extension.network.common.MessageFactory.MessageType;
import gama.extension.network.common.socket.AbstractProtocol;
import gama.extension.network.common.socket.SocketService;

/**
 * The Class TCPConnection.
 */
public class TCPConnector extends Connector {

	/** The tcp server. */
	public static final String _TCP_SERVER = "__tcp_server";

	/** The tcp client. */
	public static final String _TCP_CLIENT = "__tcp_client";


	/** The socket. */
	private SocketService socket;

	/** The is server. */
	private final boolean isServer;

	/** The remote box name. */
	private final ArrayList<String> remoteBoxName;

	/**
	 * Instantiates a new TCP connection.
	 *
	 * @param scope    the scope
	 * @param isServer the is server
	 */
	public TCPConnector(final IScope scope, final boolean isServer, final boolean isRaw) {
		this.isServer = isServer;
		this.setRaw(isRaw);
		this.remoteBoxName = new ArrayList<>();
	}

	/**
	 * Extract and apply command.
	 *
	 * @param sender  the sender
	 * @param message the message
	 */
	protected void extractAndApplyCommand(final String sender, final String message) {
		final CommandMessage mm = MessageFactory.unPackCommandMessage(sender, message);
		final String sttr = mm.getPlainContents();
		if (mm.getCommand().equals(CommandType.NEW_GROUP)) {
			this.remoteBoxName.add(sttr);
		}

		if (mm.getCommand().equals(CommandType.REMOVE_GROUP)) {
			this.remoteBoxName.remove(sttr);
		}
	}

	@Override
	protected void connectToServer(final IAgent agent) throws GamaNetworkException {

		final String server = this.getConfigurationParameter(SERVER_URL);
		final int port = Integer.parseInt(this.getConfigurationParameter(SERVER_PORT));
		if (this.isServer) {
			socket = new ServerService(agent, port, this);
		} else {
			socket = new ClientService(server, port, this);
		}
		try {
			socket.startService();
		} catch (final Exception e) {
			e.printStackTrace();
			throw GamaNetworkException.cannotBeConnectedFailure(agent.getScope()); 
		}
		this.setConnected();
	}

	@Override
	protected boolean isAlive(final IAgent agent) throws GamaNetworkException {
		return socket.isOnline();
	}

	@Override
	protected void subscribeToGroup(final IAgent agt, final String boxName) throws GamaNetworkException {
		if (!this.localMemberNames.containsKey(boxName)) {
			this.remoteBoxName.add(boxName);
		}
		if (!this.isRaw()) {
			final CommandMessage cmd = MessageFactory.buildCommandMessage(socket.getLocalAddress(),
					socket.getRemoteAddress(), CommandType.NEW_GROUP, boxName);
			this.sendMessage(agt, socket.getRemoteAddress(), MessageFactory.packMessage(cmd));
		}
	}

	@Override
	protected void unsubscribeGroup(final IAgent agt, final String boxName) throws GamaNetworkException {
		this.remoteBoxName.remove(boxName);
		if (!this.isRaw()) {
			final CommandMessage cmd = MessageFactory.buildCommandMessage(socket.getLocalAddress(),
					socket.getRemoteAddress(), CommandType.REMOVE_GROUP, boxName);
			this.sendMessage(agt, socket.getRemoteAddress(), MessageFactory.packMessage(cmd));
		}
	}

	@Override
	protected void releaseConnection(final IScope scope) throws GamaNetworkException {
		socket.stopService();
		socket = null;
		this.isConnected = false;
	}

	@Override
	protected void sendMessage(final IAgent sender, final String receiver, final String content)
			throws GamaNetworkException {
		try {
			if (socket != null) {
				socket.sendMessage(content,receiver);
			}
		} catch (final IOException e) {
			
			e.printStackTrace();
		}
	}

	@Override
	public SocketService getSocketService() {
		return socket;
	}

}

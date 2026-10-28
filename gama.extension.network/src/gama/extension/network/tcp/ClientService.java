/*******************************************************************************************************
 *
 * ClientService.java, in gama.network, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.network.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import gama.dev.DEBUG;
import gama.extension.network.common.Connector;
import gama.extension.network.common.IConnector;
import gama.extension.network.common.MessageFactory;
import gama.extension.network.common.MessageFactory.MessageType;
import gama.extension.network.common.socket.SocketService;

/**
 * The Class ClientService.
 */
public class ClientService extends Thread implements SocketService {

	private static final Pattern ESCAPED_CHARACTERS = Pattern.compile("@a@|@n@|@b@@r@|@r@");
	
	/** The socket. */
	protected Socket socket;
	
	/** The server. */
	protected final String server;
	
	/** The port. */
	protected final int port;
	
	/** The receiver. */
	protected BufferedReader receiver;
	
	/** The sender. */
	protected PrintWriter sender;
	
	/** The is alive. */
	protected volatile boolean isAlive;
	
	protected IConnector connector;

	/**
	 * Instantiates a new client service.
	 *
	 * @param server the server
	 * @param port the port
	 * @param connector the connector
	 */
	public ClientService(final String server, final int port, final IConnector connector) {
		// this.modelConnector = connector;
		this.port = port;
		this.server = server;
		this.connector = connector;
	}

	/**
	 * Instantiates a new client service.
	 *
	 * @param server the server
	 * @param port the port
	 * @param connector the connector
	 */
	public ClientService(final Socket sk, final IConnector connector) {
		this.socket=sk;
		this.server = sk.getInetAddress().getHostAddress();
		this.port = sk.getPort();
		this.connector = connector;
	}

	@Override
	public String getRemoteAddress() {
		if (socket == null) return null;
		return this.socket.getInetAddress() + ":" + this.port;
	}

	@Override
	public String getLocalAddress() {
		if (socket == null) return null;
		return this.socket.getLocalAddress() + ":" + this.port;
	}

	@Override
	public void startService() throws UnknownHostException, IOException {
		if(socket==null) {
			socket = new Socket(this.server, this.port);
		}
		receiver = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		sender = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
		isAlive = true;

		this.start();

	}

	@Override
	public void stopService() {
		this.isAlive = false;
		if (sender != null) { sender.close(); }
		try {
			if (socket != null) {
				socket.close();				
			}
			if (receiver != null) { receiver.close(); }
		} catch (final IOException e) {
			
			e.printStackTrace();
		}
	}

	@Override
	public boolean isOnline() {
		return isAlive;
	}

	@Override
	public void run() {
		try {
			while (this.isAlive) {
				String msg = receiver.readLine();
				if (msg == null) break;
				if (!connector.isRaw()) {
					if (msg.isEmpty()) continue; // Older clients append an extra line delimiter.
					msg = decodeMessage(msg);
				}
				receivedMessage(this.socket.toString(), msg);
			}
		} catch (final SocketTimeoutException e) {
			DEBUG.LOG("Socket timeout");
		} catch (final SocketException e) {
			DEBUG.LOG("Socket closed");
		} catch (final IOException e1) {
			DEBUG.LOG("Socket error" + e1);
		} finally {
			stopService();
		}

	} 

	@Override
	public void receivedMessage(final String sender, final String message) {
		final MessageType mte = MessageFactory.identifyMessageType(message);
		if (mte.equals(MessageType.COMMAND_MESSAGE)) {
			//TODO: check if it is really only for tcp
			((TCPConnector)connector).extractAndApplyCommand(sender, message); 
		} else { 
			final String r = connector.isRaw() ? message : MessageFactory.unpackReceiverName(message);
			((Connector)connector).storeMessage(sender, r, message);
		}
	}

	@Override
	public synchronized void sendMessage(final String message, final String receiver) throws IOException {
		
		if (socket == null || !isOnline()) return;
		
		//If raw connection we do not append an end of line nor escape anything
		if (connector.isRaw()) {
			sender.print(message);
		} else {
			sender.print(encodeMessage(message));
			sender.print('\n');
		}
		sender.flush();
		if (sender.checkError()) throw new IOException("Unable to send TCP message to " + getRemoteAddress());
	}

	static String encodeMessage(final String message) {
		return message.replace("@", "@a@").replace("\b\r", "@b@@r@")
				.replace("\n", "@n@").replace("\r", "@r@");
	}

	static String decodeMessage(final String message) {
		// Decode in one pass so literal escape tokens are not interpreted a second time.
		return ESCAPED_CHARACTERS.matcher(message).replaceAll(match -> switch (match.group()) {
			case "@a@" -> "@";
			case "@n@" -> "\n";
			case "@r@" -> "\r";
			case "@b@@r@" -> "\b\r";
			default -> throw new IllegalArgumentException("Unknown TCP escape: " + match.group());
		});
	}
	
	@Override
	public void sendMessage(final String message) throws IOException {
		sendMessage(message, null);
	}

}

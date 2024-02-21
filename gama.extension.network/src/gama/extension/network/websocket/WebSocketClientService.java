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
package gama.extension.network.websocket;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import gama.extension.network.common.IConnector;
import gama.extension.network.common.MessageFactory;
import gama.extension.network.common.MessageFactory.MessageType;
import gama.extension.network.tcp.ClientService;

/**
 * The Class ClientService.
 */
public class WebSocketClientService extends ClientService {

	protected GamaClient client;

	public WebSocketClientService(Socket sk, IConnector connector) {
		super(sk, connector);
	}

	public WebSocketClientService(String server, int port, IConnector connector) {
		super(server, port, connector);
	}

	@Override
	public void startService() throws UnknownHostException, IOException {
		if (socket == null) {
			try {
				var address = new URI("ws://" + this.server + ":" + this.port);
				client = new GamaClient(address, this);
				client.connectBlocking();
				socket = client.getSocket();
				
			} catch (URISyntaxException e) {
				
				e.printStackTrace();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
		isAlive = true;

		this.start();

	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while (this.isAlive) {
		}

	}

	@Override
	public void receivedMessage(final String sender, final String message) {
		final MessageType mte = MessageFactory.identifyMessageType(message);
		if (mte.equals(MessageType.COMMAND_MESSAGE)) {
			((WebSocketConnector)connector).extractAndApplyCommand(sender, message);
		} else { 
			final String r = ((WebSocketConnector)connector).isRaw() ? message : MessageFactory.unpackReceiverName(message);
			((WebSocketConnector)connector).storeMessage(sender, r, message);
		}
	}

	@Override
	public void stopService() {
		this.isAlive = false;
		if (sender != null) {
			sender.close();
		}
		try {
			if (receiver != null) {
				receiver.close();
			}
			socket.close();
		} catch (final IOException e) {
			
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void sendMessage(String message, String receiver) throws IOException {
		sendMessage(message);
	}
	
	@Override
	public void sendMessage(String message) throws IOException {
		if (socket == null || !isOnline()) return;
		
		sender = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);		
		
		String msg = message;
		
		//If raw connection we do not append an end of line nor escape anything
		if (! connector.isRaw()) {
			msg = message	.replaceAll("\n", "@n@")
							.replaceAll("\b\r", "@b@@r@")
						+ 	"\n";
		}
		client.send(msg);
		
	}
	
}

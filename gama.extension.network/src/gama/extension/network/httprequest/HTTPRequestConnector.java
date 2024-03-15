/*******************************************************************************************************
 *
 * HTTPRequestConnector.java, in gama.network, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.network.httprequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;

import gama.core.messaging.GamaMailbox;
import gama.core.messaging.GamaMessage;
import gama.core.messaging.MessagingSkill;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.util.GamaList;
import gama.core.util.IMap;
import gama.extension.network.common.Connector;
import gama.extension.network.common.GamaNetworkException;
import gama.extension.network.common.socket.SocketService;
import gama.extension.network.httprequest.utils.Utils;

import java.net.http.HttpResponse;

/**
 * The Class HTTPRequestConnector.
 */
public class HTTPRequestConnector extends Connector {

	/** The timeout. */
	public static final Integer DEFAULT_TIMEOUT = 5000;

	/** The default host. */
	public static final String DEFAULT_HOST = "localhost";

	/** The default port. */
	public static final String DEFAULT_PORT = "80";

	/** The ss thread. */
	// MultiThreadedArduinoReceiver ssThread;

	String host;

	/** The port. */
	String port;

	/** The request. */
	//
	private HttpRequest request;

	/**
	 * Instantiates a new HTTPRequest connector.
	 *
	 * @param scope
	 *            the scope
	 */
	public HTTPRequestConnector(final IScope scope) {}

	@Override
	protected void connectToServer(final IAgent agent) throws GamaNetworkException {
		String host_tmp = this.getConfigurationParameter(SERVER_URL);
		String port_tmp = this.getConfigurationParameter(SERVER_PORT);

		host = host_tmp == null ? DEFAULT_HOST : host_tmp;
		port = port_tmp == null ? DEFAULT_PORT : port_tmp;
	}

	@Override
	protected boolean isAlive(final IAgent agent) throws GamaNetworkException {
		
		return false;
	}

	@Override
	protected void subscribeToGroup(final IAgent agt, final String boxName) throws GamaNetworkException {
		

	}

	@Override
	protected void unsubscribeGroup(final IAgent agt, final String boxName) throws GamaNetworkException {
		

	}

	@Override
	protected void releaseConnection(final IScope scope) throws GamaNetworkException {
		

	}

	@SuppressWarnings({ "rawtypes", "unchecked" }) 	
	@Override
	public void send(final IAgent sender, final String receiver, final GamaMessage content) {
		Object cont = content.getContents(sender.getScope());
		try {
			if (!(cont instanceof GamaList listContent)) throw GamaNetworkException.cannotSendMessage(null,
					"The content expected to be sent is well formatted, a list [method,body,headers] is expected.");
			URI uri = null;
			try {
				uri = Utils.buildURI(host, "" + port, receiver);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			Builder requestBuilder = HttpRequest.newBuilder().uri(uri);
			// Management of the content. 
			// The various cases are the following ones:
			// - [method] or [method,headers] if method ="GET" or "DELETE"
			// - [method,body] or [method,body,headers] otherwise ("POST", "PUT")
			
			IMap<String, String> headers = null;
			String body = "";
			
			// Element at 0 is the HTTP Method
			String method = (String) listContent.get(0);

			if("GET".equals(method) || "DELETE".equals(method)) {
				// either no headers or headers at location 1 of the listContent
				headers = listContent.size() > 1 ? (IMap<String, String>) listContent.get(1) : null;
				if(listContent.size() > 2) {
					throw GamaNetworkException.cannotSendMessage(null, ""+uri+". GET/DELETE HTTP method are expecting [method] or [method,headers] only. No body.");
				}				
			} else {  // "POST" / "PUT"
				if(listContent.size() > 1) {
					body = (String) listContent.get(1);		
					//	body = Jsoner.serialize(listContent.get(1));					
				} else {
					throw GamaNetworkException.cannotSendMessage(null, ""+uri+". POST/PUT HTTP method are expecting a body.");
				}
				headers = listContent.size() > 2 ? (IMap<String, String>) listContent.get(2) : null;
			}
	
			if (headers != null) { for (String key : headers.keySet()) { requestBuilder.header(key, headers.get(key)); } }

			request = switch (method) {
				case "GET" -> requestBuilder.GET().build();
				case "POST" -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
				case "PUT" -> requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body)).build();
				case "DELETE" -> requestBuilder.DELETE().build();
				default -> throw GamaNetworkException.cannotSendMessage(null, "Bad HTTP action");
			};

			this.sendMessage(sender, receiver, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void sendMessage(final IAgent sender, final String receiver, final String content)
			throws GamaNetworkException {

		try {

			HttpResponse<String> response =
					HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

			// Manage the response of the request
			IMap<String, Object> responseMap = Utils.formatResponse(response);

			@SuppressWarnings ("unchecked") GamaMailbox<GamaMessage> mailbox =
					(GamaMailbox<GamaMessage>) sender.getAttribute(MessagingSkill.MAILBOX_ATTRIBUTE);
			if (mailbox == null) {
				mailbox = new GamaMailbox<>();
				sender.setAttribute(MessagingSkill.MAILBOX_ATTRIBUTE, mailbox);
			}

			GamaMessage msg = new GamaMessage(sender.getScope(), "HTTP", sender.getName(), responseMap);

			mailbox.add(msg);

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public SocketService getSocketService() { return null; }

}

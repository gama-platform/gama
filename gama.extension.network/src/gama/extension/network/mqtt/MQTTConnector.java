/*******************************************************************************************************
 *
 * MQTTConnector.java, in gama.network, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.network.mqtt;

import java.util.Calendar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.dev.DEBUG;
import gama.extension.network.common.Connector;
import gama.extension.network.common.GamaNetworkException;
import gama.extension.network.common.socket.SocketService;

/**
 * The Class MQTTConnector.
 */
public final class MQTTConnector extends Connector {

	static {
		DEBUG.OFF();
	}
	
	/** The default user. */
	public static final String DEFAULT_USER = "gama_demo";
	
	/** The default local name. */
	public static final String DEFAULT_LOCAL_NAME = "gama-" + Calendar.getInstance().getTimeInMillis() + "@";
	
	/** The default password. */
	public static final String DEFAULT_PASSWORD = "gama_demo";
	
	/** The default host. */
	public static final String DEFAULT_HOST = "vmpams.ird.fr";
	
	/** The default port. */
	public static final String DEFAULT_PORT = "1935";

	/** The send connection. */
	protected MqttClient sendConnection = null;
	
	/** The simulation scope. */
	protected IScope simulationScope;

	/**
	 * Instantiates a new MQTT connector.
	 *
	 * @param scope the scope
	 */
	public MQTTConnector(final IScope scope, boolean isRaw) {
		this.simulationScope = scope;
		setRaw(isRaw);
	}

	/**
	 * The Class Callback.
	 */
	class Callback implements MqttCallback {
		@Override
		public void connectionLost(final Throwable arg0) {
			throw GamaNetworkException.cannotBeConnectedFailure(GAMA.getSimulation().getScope());
		}

		@Override
		public void deliveryComplete(final IMqttDeliveryToken arg0) {
			DEBUG.OUT("message sended");
		}

		@Override
		public void messageArrived(final String topic, final MqttMessage message) throws Exception {
			final String body = message.toString();
			storeMessage("unknown", topic, body);
		}
	}

	@Override
	protected void releaseConnection(final IScope scope) {
		try {
			if( (sendConnection != null) && (sendConnection.isConnected()) ) {
				sendConnection.disconnect();
				sendConnection = null;
			}
		} catch (final MqttException e) {
			throw GamaNetworkException.cannotBeDisconnectedFailure(scope);
		}
	}

	@Override
	protected void sendMessage(final IAgent sender, final String receiver, final String content) {
		final MqttMessage mm = new MqttMessage(content.getBytes());
		try {
			DEBUG.OUT("is connected "+sendConnection.isConnected());
			sendConnection.publish(receiver, mm);
		} catch (final MqttException e) {
			DEBUG.OUT(GamaNetworkException.cannotSendMessage(sender.getScope(), receiver));
			throw GamaNetworkException.cannotSendMessage(sender.getScope(), receiver);
		}
	}

	@Override
	protected void subscribeToGroup(final IAgent agt, final String boxName) {
		try {
		
			sendConnection.subscribe(boxName);
		} catch (final MqttException e) {
			e.printStackTrace();
			throw GamaNetworkException.cannotSubscribeToTopic(agt.getScope(), e.toString());
		}

	}

	@Override
	public void unsubscribeGroup(final IAgent agt, final String boxName) throws GamaNetworkException {
		try {
			sendConnection.unsubscribe(boxName);
		} catch (final MqttException e) {
			throw GamaNetworkException.cannotUnsuscribeToTopic(simulationScope, boxName);
		}
	}
	
	@Override
	protected boolean isAlive(final IAgent agent) throws GamaNetworkException {
		return sendConnection.isConnected();
	}

	@Override
	protected void connectToServer(final IAgent agent) throws GamaNetworkException {
		if (sendConnection == null) {
			String server = this.getConfigurationParameter(SERVER_URL);
			String port = this.getConfigurationParameter(SERVER_PORT);
			String userName = this.getConfigurationParameter(LOGIN);
			String password = this.getConfigurationParameter(PASSWORD);
			String localName = this.getConfigurationParameter(LOCAL_NAME);

			server = server == null ? DEFAULT_HOST : server;
			port = port == null ? DEFAULT_PORT : port;
			userName = userName == null ? DEFAULT_USER : userName;
			password = password == null ? DEFAULT_PASSWORD : userName;
			localName = localName == null ? DEFAULT_LOCAL_NAME + server : localName;

			DEBUG.OUT("url "+ "tcp://" + server + ":" + port);
			
			try {
				sendConnection = new MqttClient("tcp://" + server + ":" + port, localName, new MemoryPersistence());
				final MqttConnectOptions connOpts = new MqttConnectOptions();
				connOpts.setCleanSession(true);
				sendConnection.setCallback(new Callback());
				connOpts.setCleanSession(true);
				connOpts.setKeepAliveInterval(30);
				connOpts.setUserName(userName);
				connOpts.setPassword(password.toCharArray());
				sendConnection.connect(connOpts);
				DEBUG.OUT("is connected  start "+sendConnection.isConnected());
				
			} catch (final MqttException e) {
				throw GamaNetworkException.cannotBeConnectedFailure(simulationScope);
			}

		}

	}

	@Override
	public SocketService getSocketService() {
		
		return null;
	}
}

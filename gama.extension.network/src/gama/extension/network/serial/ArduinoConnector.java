/*******************************************************************************************************
 *
 * ArduinoConnector.java, in gama.network, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.network.serial;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.extension.network.common.Connector;
import gama.extension.network.common.GamaNetworkException;
import gama.extension.network.common.socket.SocketService;

/**
 * The Class ArduinoConnector.
 */
public class ArduinoConnector extends Connector {

	/** The arduino. */
	MyArduino arduino;

	/** The port. */
	String PORT = "";

	/** The baud. */
	int BAUD = 9600;

	/** The ss thread. */
	MultiThreadedArduinoReceiver ssThread;

	/**
	 * Instantiates a new arduino connector.
	 *
	 * @param scope
	 *            the scope
	 */
	public ArduinoConnector(final IScope scope) {}

	@Override
	protected void connectToServer(IAgent agent) throws GamaNetworkException {
		MyPortDropdownMenu portList = new MyPortDropdownMenu();
		portList.refreshMenu();

		// cu.usbmodem1441012
		for (int i = 0; i < portList.getItemCount(); i++) {
			DEBUG.LOG(portList.getItemAt(i));
			if (portList.getItemAt(i).contains("cu.usbmodem")) {
				DEBUG.LOG(portList.getItemAt(i));
				PORT = portList.getItemAt(i);
			}
		}
		if ("".equals(PORT)) { PORT = this.getConfigurationParameter(SERVER_URL); }
		try {
			arduino = new MyArduino(PORT, BAUD);
		} catch (Exception ex) {
			GAMA.reportError(agent.getScope(),
					GamaRuntimeException.warning(
							"Cannot connect Arduino to Port: " + PORT + " exception: " + ex.getMessage(),
							agent.getScope()),
					false);
			return;
		}

		if (arduino == null) {// TODO: probably useless now that the exception is caught but I cannot test properly
			GAMA.reportError(agent.getScope(),
					GamaRuntimeException.warning("Cannot connect Arduino to Port: " + PORT, agent.getScope()), false);
			return;
		} else if (arduino.openConnection()) { DEBUG.LOG("CONNECTION OPENED"); }

		ssThread = new MultiThreadedArduinoReceiver(agent, 100, arduino);
		ssThread.start();
	}

	@Override
	protected boolean isAlive(IAgent agent) throws GamaNetworkException {
		return true;

		// return false;
	}

	@Override
	protected void subscribeToGroup(IAgent agt, String boxName) throws GamaNetworkException {}

	@Override
	protected void unsubscribeGroup(IAgent agt, String boxName) throws GamaNetworkException {}

	@Override
	protected void releaseConnection(IScope scope) throws GamaNetworkException {
		if (ssThread != null) { ssThread.interrupt(); }
		if (arduino != null) {
			arduino.closeConnection();
			DEBUG.LOG("CONNECTION CLOSED");
		}

	}

	@Override
	protected void sendMessage(IAgent sender, String receiver, String content) throws GamaNetworkException {

	}

	@Override
	public SocketService getSocketService() {

		return null;
	}

}

/**
* Name: External GAMA Controller
* Author: Gama Development Team
* Description: Opens a web-based GAMA-server controller in the default browser. The controller HTML page
*   (connector.html) communicates with the GAMA headless server via WebSocket to start, pause, stop, and
*   inspect simulations externally. Demonstrates how GAMA can be operated remotely from a web interface
*   without using the Eclipse-based GUI.
* Tags: network, WebSocket, GAMA_server, controller, headless, web, external, browser
*/
model ExternalController

global {
}

experiment start {

	init {
		// we just open the html page to control gama-server in the default web browser
		try {
			string cmd <- "open " + project_path + "scripts/connector/connector.html";
			write cmd;
			write command(cmd);
		}

		catch {
			string cmd <- "start \"\" " + project_path + "scripts/connector/connector.html";
			write cmd;
			write command(cmd);
		}

		do die();
	}

}
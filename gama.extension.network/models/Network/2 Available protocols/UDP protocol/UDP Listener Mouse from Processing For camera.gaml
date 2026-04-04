/**
* Name: UDP Listener - Mouse from Processing (Camera Control)
* Author: Arnaud Grignard, Benoit Gaudou, Nicolas Marilleau
* Description: Like 'UDP Listener - Mouse from Processing' but maps the received XY coordinates to the
*   3D display camera position/target rather than to an agent location. This allows an external Processing
*   sketch to control the GAMA camera in real time, enabling remote camera navigation for presentations
*   or interactive 3D exploration of simulation results.
* Tags: network, UDP, socket, listener, Processing, camera, 3d, interactive, real_time, communication
*/
model SocketUDP_Server_Mouse_Listener_For_Camera

global skills: [network] {
	int port <- 9877;
	string url <- "localhost";
	point cam_loc <- {0, 0};

	init {
		write "After having launched this model, run the program UDPMouseLocationSender / UDPMouseLocationSender.pde with Processing 3. ";
		write "Processing 3 can be found here: https://processing.org/";
		write "Run the GAMA simulation, move on Processing and move the mouse on the gray small screen and observe the camera in GAMA" color: #red;
		do connect to: url protocol: "udp_server" port: port size_packet: 1024;
		create observedAgents number: 10;
	}

	reflex fetch when: has_more_message() {
		loop while: has_more_message() {
			message s <- fetch_message();
			list coordinates <- string(s.contents) split_with (";");
			location <- {int(coordinates[0]), int(coordinates[1])};
			cam_loc <- {int(coordinates[0]), int(coordinates[1])};
		}

	}

}

species observedAgents {
	int size <- rnd(10);

	aspect default {
		draw cube(size) color: #red border: #black;
	}

}

experiment Server_testdd type: gui {
	output {
		display d type: 3d {
			camera #default target: cam_loc dynamic: true;
			species observedAgents;
		}

	}

}

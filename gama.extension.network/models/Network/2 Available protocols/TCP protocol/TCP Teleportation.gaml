/**
* Name: TCP Teleportation
* Author: Nicolas Marilleau
* Description: Demonstrates agent teleportation across multiple simulation instances using TCP. The space
*   is divided across 3 parallel simulations; each manages a local region. When an agent reaches a buffer
*   zone boundary, it is serialized and sent via TCP to the adjacent simulation, then recreated there. This
*   pattern implements a distributed spatial simulation without a shared global topology.
* Tags: network, TCP, multi_simulation, teleportation, distributed, socket, agent, protocol
*/
model PongTeleportation

global {
	int numberOfSimulation <- 3;
	int simulation_id <- 0;
	string prefixName <- "SIMULATION_";
	geometry shape <- rectangle(200, 100);

	init {
		name <- prefixName + simulation_id;
		create Pong number: 10 {
			myColor <- rnd_color(255);
		}

		create Buffer with: (zone:0);
		create Buffer with: (zone:1);
	}

}

species Buffer skills: [network] {
	int zone;
	string next_agent;

	init {
		name <- "buffer_" + world.name + string(zone);
		next_agent <- "buffer_" + (world.prefixName + ((1 + simulation_id) mod numberOfSimulation)) + string((zone + 1) mod 2);
		shape <- rectangle(10, 100);
		if (zone = 0) {
			location <- point(5, 50);
		} else {
			location <- point(195, 50);
		}

		if (simulation_id = 0) {
			do connect to: "localhost" with_name: name protocol: "tcp_server" port: 3001;
			do join_group with_name: "server_group";
		} else {
			do connect to: "localhost" with_name: name protocol: "tcp_client" port: 3001;
			do join_group with_name: "buffer";
			write "my name " + name + " " + next_agent;
		}

	}

	reflex teleport {
		list<Pong> to_move <- Pong where (each.last_zone = -1 and each overlaps shape);
		loop ping over: to_move {
			write "send agent";
			map<string, unknown> msg <- map(["name"::ping.name, "mcolor"::ping.myColor, "location"::(ping.location - {self.location.x, 0})]);
			string smsg <- serialize(msg);
			do send to: next_agent contents: msg;
			ask ping {
				do die;
			}

		} 

	}

	reflex enable_teleport {
		list<Pong> internal <- Pong where (each.last_zone = self.zone and !(self.shape overlaps each.location));
		ask internal {
			last_zone <- -1;
		}

	}

	reflex retrieve_agent when: has_more_message() {
		loop while: has_more_message() {
			message msg <- fetch_message();
			map<string, unknown> details <- map(msg.contents);
			create Pong with: (name: details["name"], myColor:details["mcolor"], location:details["location"]) {
				write "received agent";
				location <- {myself.location.x, location.y};
				last_zone <- myself.zone;
			}

		}

	}

	aspect default {
		draw shape color: #pink;
	}

}

species Pong {
	rgb myColor;
	int last_zone <- -1;

	reflex pongMove {
		location <- location + {1, 0};
	}

	aspect default {
		draw circle(2) color: myColor;
	}

}

experiment start {
//definition of a minimal duration for each cycle. As the model is very simple, it can run too fast to observe the results, so we slow it down.
	float minimum_cycle_duration <- 0.05;

	init {
		int nb_simul <- 3;
		simulation_id <- 0;
		seed <- 1.0;
		loop i from: 1 to: nb_simul - 1 {
			create simulation with: (simulation_id:i, seed:1 + i, numberOfSimulation:nb_simul);
		}

	}

	output {
		layout horizontal([0::5000, 1::5000, 2::5000]) tabs: true editors: false;
		display map {
			species Pong;
			species Buffer transparency: 0.5;
		}

	}

}
/**
* Name: Base Model (Serialization)
* Author: Alexis Drogoul
* Description: The base road-network model imported by the serialization demonstration models. Defines
*   roads, people, and a weighted-graph topology loaded from GIS shapefiles. Not intended to be run
*   directly — it exists as a shared foundation for 'Backward Experiment Formats', 'Create Simulation
*   From File', and the 'Serialize Operators' models so they all exercise the same underlying simulation.
* Tags: serialization, base_model, road_network, graph, agent_movement, import
*/


model BaseModel

global {
	map<road, float> roads_weight;
	graph road_network;
	float slow_coeff <- 3.0;
	
	init {
		//This road will be slow
		create road {
			shape <- line ([{10,50},{90,50}]);
			slow <- true;
		}
		//The others will be faster
		create road {
			shape <- line ([{10,50},{10,10}]);
			slow <- false;
		}
		create road {
			shape <- line ([{10,10},{90,10}]);
			slow <- false;
		}
		create road {
			shape <- line ([{90,10},{90,50}]);
			slow <- false;
		}
		
		//Weights map of the graph for those who will know the shortest road by taking into account the weight of the edges
		roads_weight <- road as_map (each:: each.shape.perimeter * (each.slow ? slow_coeff : 1.0));
		road_network <- as_edge_graph(road);
		
		//people with information about the traffic
		create people {
			color <- #blue;
			size <- 2.0;
			roads_knowledge <- roads_weight;
		}
		
		//people without information about the traffic
		create people {
			color <- #yellow;
			size <- 1.0;
			roads_knowledge <- road as_map (each:: each.shape.perimeter);
		}
		
		people(0).other <- people(1);
		people(1).other <- people(0);
		
	}
}

species road {
	bool slow;
	aspect geom {
		draw shape color: slow ? #red : #green;
	}
}
	
species people skills: [moving] {
	map<road, float> roads_knowledge;
	point the_target;
	rgb color;
	float size;
	path path_to_follow;
	people other;
	
	init {
		the_target <- {90,50};
		location <- {10,50};
	}
		
	reflex movement when: location != the_target{
		if (path_to_follow = nil) {
			
			//Find the shortest path using the agent's own weights to compute the shortest path
			path_to_follow <- path_between(road_network with_weights roads_knowledge, location,the_target);
		}
		//the agent follows the path it computed but with the real weights of the graph
		do follow path:path_to_follow speed: 5.0 move_weights: roads_weight;
	}
		
	aspect base {
		draw circle(size) color: color;
		draw line(self,other) color: #black;
	}
}

experiment Base virtual: true {
	float minimum_cycle_duration <- 0.1;
	output {
		layout #split;
		display map {
			species road aspect: geom;
			species people aspect: base;
		}
	}
}



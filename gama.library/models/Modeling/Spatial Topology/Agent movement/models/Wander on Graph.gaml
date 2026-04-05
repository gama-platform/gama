/***
* Name: Wander on Graph
* Author: Patrick Taillandier
* Description: Demonstrates the 'wander' action of the 'moving' skill when applied to a graph topology.
*   Instead of moving in free space, agents wander along the road network edges. The probability of choosing
*   each outgoing road at an intersection is specified by a map (road → probability), allowing biased random
*   walks (e.g., preferring main roads). This is useful for modeling agents with no specific destination but
*   a preference for certain routes, such as tourists exploring a city or animals following familiar paths.
* Tags: agent_movement, graph, wander, skill, road_network, random_walk, probability
***/

model WanderonGraph

global {
	
	graph network;
	
	//map that gives the probability to choose a road
	map<road,float> proba_use_road;
	
	init {
		create road with: (shape:line([{10,10}, {40,10}]));
		create road with: (shape:line([{40,10}, {40,40}]));
		create road with: (shape:line([{40,10}, {80,10}]));
		create road with: (shape:line([{80,10}, {80,40}]));
		create road with: (shape:line([{40,40}, {80,40}]));
		create road with: (shape:line([{80,40}, {80,80}]));
		create road with: (shape:line([{80,80}, {10,80}]));
		create road with: (shape:line([{80,80}, {50,50}]));
		create road with: (shape:line([{50,50}, {10,80}]));
		create road with: (shape:line([{10,80}, {10,10}]));
		
		
		create people number: 50 with: (location:any_location_in(one_of(road)));
		
		//directed graph build from the road agents
		network <- directed(as_edge_graph(road));
		
		//the map of probability to choose a road is build from the proba_use attribute of roads
		proba_use_road <- road as_map (each::each.proba_use);
	}
}

species road {
	// probability for a agent to choose this road
	float proba_use <- rnd(0.1, 1.0);
	
	aspect default {
		draw shape +(proba_use/2.0)  color: #gray end_arrow: 2.0;
	}
}

species people skills: [moving]{
	rgb color <- rnd_color(255);
	aspect default {
		draw circle(1.0) color:color border: #black;
	}
	
	reflex move {
		// move randomly on the network, using proba_use_road to define the probability to choose a road.
		do wander on: network proba_edges: proba_use_road ;
	}
}

experiment WanderonGraph type: gui {
	float minimum_cycle_duration <- 0.05;
	output {
		display map type:2d{
			species road;
			species people;
		}
	}
}

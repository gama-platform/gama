/**
* Name: Graph From Bug (Distance Graph)
* Author: Arnaud Grignard
* Description: Builds a spatial proximity graph from the positions of bug agents in the base "Common Bug Species"
*   model using the 'as_distance_graph' operator. A mirror species (node) is used to represent the bugs as graph
*   nodes. The resulting graph connects any two nodes that are within a specified distance threshold of each other.
*   The graph is displayed in a separate 3D view alongside the original 2D simulation, providing an alternative
*   relational perspective on the spatial structure of the agent population.
* Tags: graph, mirror, 3d, distance_graph, multi_level, spatial, network
*/

model SpatialGraph
//Import the model Common Bug Species model
import '../includes/Common Bug Species.gaml'


global { 
	//Graph that will be computed at each step linking the bug agents according to their distance
	graph myGraph;
	//Minimal distance to consider two nodes agents (ie the bug) as connected
	float distance min: 1.0 <- 10.0;
	
	//Reflex to update the graph when cycle is greater than 0. Important because the mirroring has one step late from
	//the original species, and at step 0, the mirroring species aren't created
	reflex updateGraph when:(cycle>0){
		//Kill all the edge agent to create a new graph
		ask edge_agent {
			do die();
		}
		//Create a new graph using the distance to compute the edges
		myGraph <- as_distance_graph(node_agent, distance, edge_agent);
	}
}
//Species node_agent mirroring the bug species
species node_agent mirrors: list(bug) {
	//Each location will be the one of the bug at the previous step
	point location <- target.location update: target.location;
	aspect base {
		draw sphere(1.1) color: #green; 
	}
}
//Species to represent the edge of the graph 
species edge_agent {
	aspect base {
		draw shape color: #green;
	}
}

experiment spatialGraph type: gui {
	
	parameter 'Distance' var:distance  min: 1.0 <- 10.0 category: 'Model';
	
	float minimum_cycle_duration <- 0.05#s;
	
	output {	
		display graph_view type: 3d {
			camera 'default' location: {-15.7912,153.2715,67.8712} target: {50.0,50.0,0.0};
	 	    species bug aspect:base;
			species node_agent aspect: base position:{0,0,0.1};
			species edge_agent aspect: base position:{0,0,0.1};
		}
	}
}

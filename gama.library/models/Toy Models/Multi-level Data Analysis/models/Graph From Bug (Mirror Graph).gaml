/**
* Name: Graph From Bug (Mirror Graph)
* Author: Arnaud Grignard
* Description: Builds a spatial proximity graph from bug agents with a custom node-level 'related_to' method.
*   A mirror species (node) reflects the bugs as graph nodes. Unlike the simple distance graph, here the node
*   species overrides the 'related_to' method to define its own relational logic — which nodes are connected
*   to which. This gives full control over the graph structure beyond a simple distance threshold. The resulting
*   graph is rendered in a dedicated 3D display, demonstrating multi-level agent analysis in GAMA.
* Tags: graph, mirror, 3d, multi_level, spatial, network, related_to, custom
*/


model Graph

//Import the model Common Bug Species model
import '../includes/Common Bug Species.gaml'

global {
	//Distance to link two bugs
	int distance min: 1 <- 25;
	//variable to start the animation of the model
	int startAnimation min: 1 <- 25;
	//Variable to save the time animation
	int timeAnim <- 0;
	//Reflex to update the time of animation
	reflex updateAnimation 
	{
		if (time > startAnimation) 
		{
			timeAnim <- int(time - startAnimation);
		}
	}
}
//Species node_agent mirroring the bug species, represented as graph node
species node_agent mirrors: list(bug) parent: graph_node edge_species: edge_agent {
	//Their location is the one of the target location
	point location <- target.location update: target.location;
	
	//Action to know if an agent is related to another agent considering their distance
	bool related_to (node_agent other) {
		using topology(target) {
			return (target.location distance_to other.target.location) < distance;
		}

	}

	aspect base {
		draw sphere(2) color: #green;
	}

	aspect dynamic {
		int degree <- (my_graph) degree_of (self);
		draw sphere(1 + (degree / 5.0)) color: #blue;
	}

}
//Species edge to represent the edges of the graph
species edge_agent parent: base_edge {
	rgb color;
	aspect base {
		draw shape color: #green;
	}
	
	aspect dynamic {
		geometry ss <- line([{ self.source.location.x, self.source.location.y, self.source.location.z }, { self.target.location.x, self.target.location.y, self.target.location.z }]);
		float val <- 255.0 * (ss.perimeter / distance);
		rgb cc <- hsb(val, 1.0, 1.0);
		draw ss + 0.1 color: cc border: cc;
	}

}

experiment AdvancedView type: gui {
	
	
	parameter 'Distance' var:distance category: 'Model';
	parameter 'Start Animation ' var:startAnimation category: 'Animation View';
	float minimum_cycle_duration <- 0.05#s;
	
	output {
		display graph_plus_bug_layered type: 3d {
			camera 'default' location: {-9.2599,157.3601,111.6399} target: {52.0223,44.4922,0.0}; 
			species bug aspect: base;
			species node_agent aspect: base position: { 0, 0, 0.2 };
			species edge_agent aspect: base position: { 0, 0, 0.2 };
			species node_agent aspect: dynamic position: { 0, 0, 0.4 };
			species edge_agent aspect: dynamic position: { 0, 0, 0.4 };
		}
	}
}



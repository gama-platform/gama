/**
* Name: Graph Loading Layout
* Author: Patrick Taillandier
* Description: Shows how to load a graph from an external GraphML file and apply layout algorithms to
*   reposition its nodes. The model loads a Barabasi-scale-free graph from a '.graphml' file and then
*   applies several graph layout algorithms (force-directed, circular, radial, grid) as selectable parameters.
*   Layouts are computed once and the result is displayed. This is the reference for working with external
*   graph file formats and for making imported graphs visually readable.
* Tags: graph, load_file, graphml, layout, force_directed, visualization, network
*/

model graphloadinglayout

global {
	graph<agent,agent> the_graph ;
	string barabasi_file <- "../includes/simple.graphml";
	geometry shape <- rectangle(500,500);
	string layout_type;
	int layout_time <- 1000 min: 0 max: 10000;
	float coeff_force <- 0.8 min: 0.1 max: 1.0;
	float cooling_coefficient <- 0.1 min: 0.01 max: 0.5; 
	float coeff_nb_places <- 1.2 min: 0.0 max: 2.0; 
	float normalizationFactor <- 0.5 min: 0.1 max: 2.0; 
	float theta <- 0.5 min: 0.1 max: 2.0; 
	
	
	//The operator load_graph_from_file generates the graph from the file, and chose the vertices as agents of node_agent 
	//species, and edges as edge_agent agents
	init {
		the_graph <- graphml_file("../includes/simple.graphml", node_agent, edge_agent).contents;
	}
	
	//In case the layout type is forcedirected or random, the reflex will change at each step the layout of the graph
	reflex layout_graph {
		switch layout_type {
			match "Force" {
				the_graph <- layout_force(the_graph, world.shape,coeff_force , cooling_coefficient, layout_time);
			}
			match "Force FR" {
				the_graph <- layout_force_FR(the_graph, world.shape,normalizationFactor,layout_time);
			}
			match "Force FR Indexed" {
				the_graph <- layout_force_FR_indexed(the_graph, world.shape,theta,normalizationFactor,layout_time);
			}
			match "Circular" {
				the_graph <- layout_circle(the_graph, world.shape, false);
			}
			match "Grid" {
				the_graph <- layout_grid(the_graph, world.shape,coeff_nb_places);
	 		}
		}	
	}
}

species edge_agent {
	aspect default {	
		draw shape color: #black;
	}
}

species node_agent {
	aspect default {	
		draw circle(2) color: #red;
	}
}

experiment loadgraph type: gui {
	float minimum_cycle_duration <- 0.5;
	parameter "Max number of iterations" var:layout_time;
	parameter "Force coefficient" category: "Force"var:coeff_force;
	parameter "Decreasing coefficient of the temperature" category: "Force" var:cooling_coefficient; 
	parameter "Coefficient for the number of places to locate the vertices" category:"Grid" var:coeff_nb_places; 
	parameter "Normalization factor" category: "Force_FR" var:normalizationFactor; 
	parameter "Theta" category:"Force_FR_indexed" var:theta; 
	parameter "Layout type" var: layout_type among:["Force FR","Force FR Indexed" , "Force", "Circular", "Grid"] init:"Force FR";
	output {
		display map type: 3d{
			species edge_agent ;
			species node_agent ;
		}
	}
}

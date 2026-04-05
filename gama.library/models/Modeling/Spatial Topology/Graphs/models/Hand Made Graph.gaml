/**
* Name: Hand Made Graph
* Author: Patrick Taillandier
* Description: Demonstrates three ways to construct a graph manually in GAML without relying on a shapefile
*   or agent positions. Method 1: pass a list of edges to 'as_edge_graph'. Method 2: use the 'add' operator
*   with 'node:' or 'edge:' facets to insert nodes and edges one by one. Method 3: modify a graph in place
*   by adding nodes and edges after construction. Two display experiments show each approach side by side.
*   This is the primary reference for building custom graph topologies programmatically in GAMA.
* Tags: graph, node, edge, manual, construction, add, as_edge_graph, topology
*/

@no_warning
model handMadeGraph

global {
	graph<geometry, geometry> the_graph1 ;
	graph<geometry, geometry> the_graph2;
	
	init {
		the_graph1 <- as_edge_graph([edge({10,5}, {20,3}), edge({10,5}, {30,30}),edge({30,30}, {80,35}),edge({80,35}, {40,60}),edge({80,35}, {10,5}), node ({50,50})]);	
		
		the_graph2 <- graph<geometry, geometry>([]);
		//first way to add nodes and edges
		the_graph2 << node({50,50}) ;
		the_graph2 << edge({10,10},{90,50});
		
		//second way to add nodes and edges
		the_graph2 <- the_graph2 add_node {10,40} ;
		the_graph2 <- the_graph2 add_edge ({35,50}:: {50,50}) ;
	}
	
}

species edge_agent {
	aspect default {	
		draw shape color: #black;
	}
}

species node_agent {
	aspect default {	
		draw circle(1) color: #red;
	}
}

experiment create_graph type: gui {
	
	output {
		display graph1 type: 3d{
			graphics "the graph 1" {
				loop e over: the_graph1.edges {
					draw e color: #blue; 
				}
				loop n over: the_graph1.vertices {
					draw circle(2) at: point(n) color: #blue; 
				}
			}
		}
		display graph2 type: 3d{
			graphics "the graph 2" {
				loop e over: the_graph2.edges {
					draw e color: #red; 
				}
				loop n over: the_graph2.vertices {
					draw circle(2) at: point(n) color: #red; 
				}
			}
		}
	}
}

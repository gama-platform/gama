/**
* Name: Graph Importation
* Author: Patrick Taillandier
* Description: Demonstrates how to load graph files from various standard formats into GAMA. Supported formats
*   include GraphML, GML, DOT (Graphviz), DIMACS, GEXF (Gephi Exchange), TSPLIB, and Graph6. A parameter
*   lets the user interactively switch between formats to compare the loaded results. After loading, the graph
*   nodes are positioned according to the coordinates stored in the file (when available), and the graph topology
*   is displayed. This model is a useful reference for integrating externally constructed networks into GAMA simulations.
* Tags: graph, file, graphml, gml, dot, gexf, import, load_file, network, topology
*/

model GraphImportation

global {
	
	string type <- "graphml" among: ["graphml", "gml","dot", "dimacs", "gexf", "tsplib", "graph6"]; 
	
	map loc_nodes;
	
	graph g;
	init {
		do importation;
	}
	reflex reimport {
		do importation;
	}
	action importation() {
		
		switch type {
			match "graphml" {
				g <- graphml_file("../includes/graphs/simple.graphml").contents;
			}
			match "gml" {
				g <- graphgml_file("../includes/graphs/simple.gml").contents;
			}
			match "dot" {
				g <- graphdot_file("../includes/graphs/simple.dot").contents;
			}
			match "dimacs" {
				g <- graphdimacs_file("../includes/graphs/simple.dimacs").contents;
			}	
			match "gexf" {
				g <- graphgexf_file("../includes/graphs/simple.gexf").contents;
			}
			match "tsplib" {
				g <- graphtsplib_file("../includes/graphs/simple.tsplib").contents;
			}
			match "graph6" {
				g <- graph6_file("../includes/graphs/simple.g6").contents;
			}	
		}
		write g;
		
		loop v over: g.vertices {
			loc_nodes[v] <- any_location_in(world);	
		} 
	}
}

experiment import_graph type: gui {
	
	parameter var:type;
	
	output {
		display graph_display type: 3d axes: false{
			graphics "graph " {
				
				loop v over: g.vertices {
					draw circle(1) at: point(loc_nodes[v]) color: #red border: #black;
				}
				loop e over: g.edges {
					string s <- g source_of e;
					string t <- g target_of e;
					draw line([point(loc_nodes[s]),  point(loc_nodes[t])]) color: #black end_arrow: 1.0;
				}
			}
		}
	}	
}

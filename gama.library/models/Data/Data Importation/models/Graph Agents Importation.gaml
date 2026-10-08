/**
* Name: Graph Agents Importation
* Author: K. Johnson, Benoit Gaudou
* Description: Shows how to load a graph file and import both its structure and the attributes stored on nodes
*   and edges. GAMA supports loading graphs in GraphML format (and others) and mapping graph nodes and edges to
*   dedicated agent species. The loaded graph attributes (stored in the file as XML properties) are read and
*   attached to the corresponding agent attributes using the 'with' facet of the 'create' statement. This model
*   demonstrates how to preserve rich graph metadata during import for use in subsequent analyses.
* Tags: graph, file, graphml, import, load_file, attributes, nodes, edges
*/

model GraphAttributesImportation

global {
	
	string type <- "graphml" among: ["graphml"]; 
	
	graph<node_graph,edge_graph> g;
	
	init {
		do importation();		
	}
	
	reflex reimport {
		do importation();
	}
	
	action importation() {	
		// Up to now, attributes importation are only available for graphml	datafiles.
		switch type {
			match "graphml" {
				// att and attEdges are the name of the attribute off the nodde_graph and edge_graph species
	   			g <- graphml_file("../includes/graphs/agents-attributes.graphml", node_graph, edge_graph,"att","attEdges").contents; 
			}
		}
			
		ask node_graph {
	   		do init_agt();
		}
		ask edge_graph {
			do init_agt();
		}
		
		write g;		
	}
}

species edge_graph {   
	float prob <- 0.0;
	rgb my_color <- #green;
    map<string,string> attEdges;	
    
    action init_agt (){
    	name <- attEdges["name"]; 
    	prob <- float(attEdges["prob"]);		
    }
}

species node_graph {
    map<string,string> att;
    
    action init_agt() {
    	location <- {att["xpoint"] as float,att["ypoint"] as float}; 		
    }
}

experiment import_graph type: gui {
	
	parameter var:type;
	
	output {
		display graph_display type: 3d axes: false{
			graphics "graph " {
				
				loop v over: g.vertices {
					draw circle(1) at: v.location color: #red border: #black;
				}
				loop e over: g.edges {
					node_graph s <- g source_of e;
					node_graph t <- g target_of e;
					draw line([s.location, t.location]) color: #black end_arrow: 1.0;
				}
			}
		}
	}	
}

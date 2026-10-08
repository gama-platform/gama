/***
* Name: Waterflow River Water Unit
* Author: Benoit Gaudou
* Description: Models water flow through a river network using discrete 'water unit' agents that travel along
*   the river graph from source to outlet. At each step, a source point creates a new water unit agent which
*   then follows the graph edges downstream until it reaches the outlet point and is removed. This agent-based
*   approach makes individual water parcels visible and traceable. It complements the exchange-based graph
*   model by providing an alternative, agent-centric perspective on the same river network data.
* Tags: shapefile, gis, graph, gui, hydrology, water_flow, river, agent, water_unit
***/

model Waterflowriverwaterunit

global {
	file river_shape_file <- shape_file("../includes/rivers.shp");
	file poi_file <- shape_file("../includes/poi.shp");

	geometry shape <- envelope(river_shape_file) + 500;
	
	graph the_river;

	init {
		create river from: river_shape_file;
		create poi from: poi_file;
		
		the_river <- as_edge_graph(river);
	}
	
	reflex c_water {
		create water {
			location <- one_of(poi where (each.type = "source")).location;
			target <- one_of(poi where (each.type = "outlet")) ;
		}
	}
}

species poi {
	string type;
	
	aspect default {
		draw circle(500) color: (type="source") ? #green : #red border: #black;		
	}	
}

species river {
	aspect default {
		draw shape + 30 color: #blue;		
	}
}

species water skills: [moving] {
	poi target ;

	reflex move {
		do goto (target: target, on: the_river, speed: 100.0);
	}	
	
	aspect default {
		draw circle(500) color: #blue border: #black;
	}
}

experiment flow type: gui {
	output {
	 	display "Water Unit" type:2d{ 
			species river ; 
			species water;	
			species poi;			
		}
	}
}

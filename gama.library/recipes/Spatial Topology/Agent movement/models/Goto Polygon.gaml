/**
* Name: Goto Polygon
* Author: Patrick Taillandier
* Description: Shows how to generate a navigable road graph by skeletonizing polygon geometries loaded from
*   a shapefile. The 'skeletonize' operator extracts the medial axis of each polygon, producing a set of
*   center-line segments that form the road network. Agents then use this auto-generated graph to navigate
*   toward a common target. This technique is useful when the available GIS data contains navigable areas
*   as filled polygons (e.g., city blocks, floor plans) rather than explicit road lines.
* Tags: graph, agent_movement, shapefile, skill, shortest_path, skeleton, polygon, gis
*/

model polygon
global {
	//Import of the shapefile containing the different polygons
	file shape_file_in <- file('../includes/gis/squareHole.shp') ;
	graph the_graph;
	
	geometry shape <- envelope(shape_file_in);
	
	init {    
		create objects from: shape_file_in ;
		objects the_object <- first(objects);
		
		//triangulation of the object to get the different triangles of the polygons
		list<geometry> triangles <- list(triangulate(the_object, 0.01));
		
		loop trig over: triangles {
			create triangle_obj {
				shape <- trig;
			}
		}
		
		//creation of a list of skeleton from the object 
		list<geometry> skeletons <- list(skeletonize(the_object, 0.01));
		
		//Split of the skeletons list according to their intersection points
		list<geometry> skeletons_split  <- split_lines(skeletons);
		loop sk over: skeletons_split {
			create skeleton {
				shape <- sk;
			}
		}
		
		//Creation of the graph using the edges resulting of the splitted skeleton
		 the_graph <- as_edge_graph(skeleton);
		 
		 
		create goal  {
			 location <- any_location_in (one_of(skeleton)); 
		}
		create people number: 100 {
			 target <- one_of (goal) ; 
			 location <- any_location_in (one_of(skeleton));
		} 
	}
}

species objects  {
	aspect default {
		draw shape color: #gray ;
	}
}

species triangle_obj  {
	rgb color <- rgb(150 +rnd(100),150 + rnd(100),150 + rnd(100));
	aspect default {
		draw shape color: color border:#black; 
	}
}

species skeleton  {
	aspect default {
		draw shape + 0.1 color: #red ;
	}
}
	
species goal {
	aspect default {
		draw circle(2) color:#red border:#black;
	}
}

species people skills: [moving] {
	goal target;
	path my_path; 
	
	reflex goto {
		do goto(on:the_graph, target:target, speed:1.0);
	}
	aspect default {
		draw circle(1) color: #green border:#black;
	}
}

experiment goto_polygon type: gui {
	float minimum_cycle_duration <- 0.1;
	
	output {
		display objects_display {
			species objects aspect: default ;
			species triangle_obj aspect: default ;
			species skeleton aspect: default ;
			species people aspect: default ;
			species goal aspect: default ;
		}
	}
}


/***
* Name: Generate Pedestrian Paths
* Author: Patrick Taillandier
* Description: A preprocessing model that generates the pedestrian path network and free-space polygons
*   required by 'Complex environment - walk.gaml'. Loads wall shapefiles, computes the navigable free
*   space using Voronoi decomposition of the obstacle boundaries, then saves the resulting path graph and
*   free-space polygons to shapefiles. Run this model once before running the complex environment model.
* Tags: pedestrian, gis, shapefile, graph, agent_movement, skill, transport, preprocessing, voronoi
***/

model generate_pedestrian_path

global {
	
	file wall_shapefile <- file("../includes/walls.shp");
	
	
	geometry shape <- envelope(wall_shapefile);
	bool display_free_space <- false;
	float P_shoulder_length <- 0.45;
	
	float simplification_dist <- 0.5; //simplification distance for the final geometries
	
	bool add_points_open_area <- true;//add points to open areas
 	bool random_densification <- false;//random densification (if true, use random points to fill open areas; if false, use uniform points), 
 	float min_dist_open_area <- 0.1;//min distance to considered an area as open area, 
 	float density_open_area <- 0.01; //density of points in the open areas (float)
 	bool clean_network <-  true; 
	float tol_cliping <- 1.0; //tolerance for the cliping in triangulation (float; distance), 
	float tol_triangulation <- 0.1; //tolerance for the triangulation 
	float min_dist_obstacles_filtering <- 0.0;// minimal distance to obstacles to keep a path (float; if 0.0, no filtering), 
	
	float min_distance_free_space <- 2.0;
	float max_distance_free_space <- 10.0;
	
	geometry open_area ;
	
	init {
		open_area <- copy(shape);
		create wall from:wall_shapefile {
			open_area <- open_area -(shape buffer (P_shoulder_length/2.0));
		}
		list<geometry> generated_lines <- generate_pedestrian_network([],[open_area],add_points_open_area,random_densification,min_dist_open_area,density_open_area,clean_network,tol_cliping,tol_triangulation,min_dist_obstacles_filtering,simplification_dist);
		
		create pedestrian_path from: generated_lines {
			do initialize (bounds:[open_area], distance: max(min_distance_free_space, min(max_distance_free_space,(wall closest_to self) distance_to self)), masked_by: [wall] ,distance_extremity: 1.0);
			if (free_space = nil or free_space.area = 0) {
				free_space <- shape +min_distance_free_space;
			}
		}
		save pedestrian_path to: "../includes/pedestrian paths.shp" format:"shp";
		save open_area to: "../includes/open area.shp" format:"shp";
		save pedestrian_path collect each.free_space to: "../includes/free spaces.shp" format:"shp";
	}
}

species pedestrian_path skills: [pedestrian_road]{
	rgb color <- rnd_color(255);
	aspect default {
		draw shape  color: color;
	}
	aspect free_area_aspect {
		if(display_free_space and free_space != nil) {
			draw free_space color: #cyan border: #black;
		}
	}
}

species wall {
	aspect default {
		draw shape + (P_shoulder_length/2.0) color: #gray border: #black;
	}
}

experiment normal_sim type: gui {
	
	parameter var:display_free_space;
	parameter var:P_shoulder_length;
	
	output {
		display map type: 3d axes:false{
			species wall refresh: false;
			graphics "open_area" {
				draw open_area color: #lightpink;
			}
			species pedestrian_path aspect:free_area_aspect transparency: 0.5 ;
			species pedestrian_path refresh: true;
		}
	}
}

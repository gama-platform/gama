/***
* Name: pedestrian_complex_environment
* Author: Patrick Taillandier
* Description: show how to use the pedestrian skill for complex envorinment - require to generate pedestrian paths before - see model "Generate Pedestrian path.gaml" 
* Tags: pedestrian, gis, shapefile, graph, agent_movement, skill, transport
***/

model pedestrian_complex_environment

global {
	
	file wall_shapefile <- file("../includes/walls.shp");
	
	shape_file free_spaces_shape_file <- shape_file("../includes/free spaces.shp");
	shape_file open_area_shape_file <- shape_file("../includes/open area.shp");
	shape_file pedestrian_paths_shape_file <- shape_file("../includes/pedestrian paths.shp");

	
	graph network;
	
	geometry shape <- envelope(wall_shapefile);
	
	bool display_free_space <- false;
	bool display_force <- false;
	bool display_target <- false;
	bool display_circle_min_dist <- true;
	float P_shoulder_length <- 0.45;
	float P_proba_detour <- 0.5;
	bool P_avoid_other <- true;
	float P_obstacle_consideration_distance <- 3.0;
	float P_pedestrian_consideration_distance <- 3.0;
	float P_tolerance_target <- 0.1;
	bool P_use_geometry_target <- true;
	
	
	string P_model_type <- "simple" among: ["simple", "advanced"];	
	float P_A_pedestrian_SFM_advanced <- 0.16 ;
	float P_A_obstacles_SFM_advanced <- 1.9 ;
	float P_B_pedestrian_SFM_advanced <- 0.1 ;
	float P_B_obstacles_SFM_advanced <- 1.0 ;
	float P_relaxion_SFM_advanced  <- 0.5 ;
	float P_gama_SFM_advanced <- 0.35 ;
	float P_lambda_SFM_advanced <- 0.1 ;
	float P_minimal_distance_advanced <- 0.25;	
	float P_n_prime_SFM_simple <- 3.0 ;
	float P_n_SFM_simple <- 2.0 ;
	float P_lambda_SFM_simple <- 2.0 ;
	float P_gama_SFM_simple <- 0.35 ;
	float P_relaxion_SFM_simple <- 0.54 ;
	float P_A_pedestrian_SFM_simple <-4.5;
	
	float step <- 0.1;
	int nb_people <- 250;

	geometry open_area ;
	
	init {
		open_area <- first(open_area_shape_file.contents);
		create wall from:wall_shapefile;
		create pedestrian_path from: pedestrian_paths_shape_file {
			list<geometry> fs <- free_spaces_shape_file overlapping self;
			free_space <- fs first_with (each covers shape); 
		}
		

		network <- as_edge_graph(pedestrian_path);
		
		ask pedestrian_path {
			do build_intersection_areas pedestrian_graph: network;
		}
	
		create people number:nb_people{
			location <- any_location_in(one_of(open_area));
			obstacle_consideration_distance <-P_obstacle_consideration_distance;
			pedestrian_consideration_distance <-P_pedestrian_consideration_distance;
			shoulder_length <- P_shoulder_length;
			avoid_other <- P_avoid_other;
			proba_detour <- P_proba_detour;
			
			use_geometry_waypoint <- P_use_geometry_target;
			tolerance_waypoint<- P_tolerance_target;
			pedestrian_species <- [people];
			obstacle_species<-[wall];
			
			pedestrian_model <- P_model_type;
			
		
			if (pedestrian_model = "simple") {
				A_pedestrians_SFM <- P_A_pedestrian_SFM_simple;
				relaxion_SFM <- P_relaxion_SFM_simple;
				gama_SFM <- P_gama_SFM_simple;
				lambda_SFM <- P_lambda_SFM_simple;
				n_prime_SFM <- P_n_prime_SFM_simple;
				n_SFM <- P_n_SFM_simple;
			} else {
				A_pedestrians_SFM <- P_A_pedestrian_SFM_advanced;
				A_obstacles_SFM <- P_A_obstacles_SFM_advanced;
				B_pedestrians_SFM <- P_B_pedestrian_SFM_advanced;
				B_obstacles_SFM <- P_B_obstacles_SFM_advanced;
				relaxion_SFM <- P_relaxion_SFM_advanced;
				gama_SFM <- P_gama_SFM_advanced;
				lambda_SFM <- P_lambda_SFM_advanced;
				minimal_distance <- P_minimal_distance_advanced;
			
			}
		}	
	}
	
	reflex stop when: empty(people) {
		do pause;
	}
	
}

species pedestrian_path skills: [pedestrian_road]{
	aspect default { 
		draw shape  color: #gray;
	}
	aspect free_area_aspect {
		if(display_free_space and free_space != nil) {
			draw free_space color: #lightpink border: #black;
		}
		
	}
}

species wall {
	geometry free_space;
	float high <- rnd(10.0, 20.0);
	
	aspect demo {
		draw shape border: #black depth: high texture: ["../includes/top.png","../includes/texture5.jpg"];
	}
	
	aspect default {
		draw shape + (P_shoulder_length/2.0) color: #gray border: #black;
	}
}

species people skills: [pedestrian]{
	rgb color <- rnd_color(255);
	float speed <- gauss(5,1.5) #km/#h min: 2 #km/#h;

	reflex move  {
		if (final_waypoint = nil) {
			do compute_virtual_path pedestrian_graph:network target: any_location_in(open_area) ;
		}
		do walk ;
	}	
	
	aspect default {
		
		if display_circle_min_dist and minimal_distance > 0 {
			draw circle(minimal_distance).contour color: color;
		}
		
		draw triangle(shoulder_length) color: color rotate: heading + 90.0;
		
		if display_target and current_waypoint != nil {
			draw line([location,current_waypoint]) color: color;
		}
		if  display_force {
			loop op over: forces.keys {
				if (species(agent(op)) = wall ) {
					draw line([location, location + point(forces[op])]) color: #red end_arrow: 0.1;
				}
				else if ((agent(op)) = self ) {
					draw line([location, location + point(forces[op])]) color: #blue end_arrow: 0.1;
				} 
				else {
					draw line([location, location + point(forces[op])]) color: #green end_arrow: 0.1;
				}
			}
		}	
	}
}


experiment normal_sim type: gui {
	
	
	parameter "display free space" var:display_free_space;
	parameter "display force" var:display_force;
	parameter "display target" var:display_target; 
	parameter "display circle_min_dist" var:display_circle_min_dist;
	parameter "P shoulder_length" var:P_shoulder_length;
	parameter "P proba_detour" var:P_proba_detour;
	parameter "P avoid_other" var:P_avoid_other;
	parameter "P obstacle_consideration_distance" var:P_obstacle_consideration_distance;
	parameter "P pedestrian_consideration_distance" var:P_pedestrian_consideration_distance;
	parameter "P tolerance_target" var:P_tolerance_target;
	parameter "P use_geometry_target" var:P_use_geometry_target;


	parameter "P model_type" var:P_model_type among: ["simple", "advanced"]; 

	parameter "P A_pedestrian_SFM_advanced" var:P_A_pedestrian_SFM_advanced   category: "SFM advanced";
	parameter "P A_obstacles_SFM_advanced" var:P_A_obstacles_SFM_advanced   category: "SFM advanced";
	parameter "P B_pedestrian_SFM_advanced" var:P_B_pedestrian_SFM_advanced   category: "SFM advanced";
	parameter "P B_obstacles_SFM_advanced" var:P_B_obstacles_SFM_advanced   category: "SFM advanced";
	parameter "P relaxion_SFM_advanced" var:P_relaxion_SFM_advanced    category: "SFM advanced";
	parameter "P gama_SFM_advanced" var:P_gama_SFM_advanced category: "SFM advanced";
	parameter "P lambda_SFM_advanced" var:P_lambda_SFM_advanced   category: "SFM advanced";
	parameter "P minimal_distance_advanced" var:P_minimal_distance_advanced   category: "SFM advanced";

	parameter "P n_prime_SFM_simple" var:P_n_prime_SFM_simple   category: "SFM simple";
	parameter "P n_SFM_simple" var:P_n_SFM_simple   category: "SFM simple";
	parameter "P lambda_SFM_simple" var:P_lambda_SFM_simple   category: "SFM simple";
	parameter "P gama_SFM_simple" var:P_gama_SFM_simple   category: "SFM simple";
	parameter "P relaxion_SFM_simple" var:P_relaxion_SFM_simple   category: "SFM simple";
	parameter "P A_pedestrian_SFM_simple" var:P_A_pedestrian_SFM_simple   category:"SFM simple";
	
	
	float minimum_cycle_duration <- 0.02;
	output {
		display map type: 3d{
			species wall refresh: false;
			species pedestrian_path aspect:free_area_aspect transparency: 0.5 ;
			species pedestrian_path refresh: false;
			species people;
		}
	}
}

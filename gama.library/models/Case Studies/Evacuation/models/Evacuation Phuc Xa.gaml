/**
* Name: Mainxp
* Author: Patrick Taillandier
*/

model Mainxp

global schedules: people_ordered{
	
	shape_file buildings_shape_file <- shape_file("../includes/buildings.shp");
	shape_file evacuation_shape_file <- shape_file("../includes/evacuation_points.shp");
	shape_file roads_shape_file <- shape_file("../includes/roads.shp");
	shape_file intersections_shape_file <- shape_file("../includes/intersections.shp");
	
	
	string qualitativePalette <- "Set1" among:["Accents","Paired","Set3","Set2","Set1","Dark2","Pastel2","Pastel1"];
	list<rgb> palette_evac <- brewer_colors(qualitativePalette, length(evacuation_shape_file));

	float alpha <-1.0;
	float beta <- 1.0;
	float TIME_MAX <- 60.0 #mn;
	
	float coeff_change_path <- 0.01 ;
	float tj_threshold <- 0.75;
	float lane_width <- 0.7;
	
	float display_coeff <- 2.0;
	float step <-0.5#s;
	
	geometry shape <- envelope(buildings_shape_file)  ;
	graph road_network;
	
	float priority_min_road;

	list<moving_agent> people_ordered;
	
	int init_num_people;
	float change_road_lane_perimeters <- 0 #m;
				
	
	list<moving_agent> people -> {(pedestrian as list) + (car as list) + (motorbike as list) + (bicycle as list)};
	
	string model_description_1 <- "This model is an agent-based simulation developed to analyze mass evacuation strategies\n in a densely populated district exposed to flood risks (Phúc Xá, Hanoi)";
	string model_description_2 <- "It is built using the GAMA platform and the ESCAPE framework, and specifically accounts\n for mixed and non-normative traffic types (pedestrians, motorbikes, bicycles, cars), reflecting the Vietnamese context";
	string model_description_3 <-"Agents represent residents seeking to evacuate through the nearest exits,\n with the ability to change routes in response to congestion.";
	string model_description_4 <-"Several scenarios are tested, including different mobility types, rerouting behaviors,\n spatial departure strategies, and minor changes to the road network. ";
	string model_description_5 <- "Results show that accounting for mixed traffic and using spatially distributed evacuation strategies\n can significantly reduce total evacuation time.";
	string model_description_6 <- "Chapuis, K., Minh-Duc, P., Brugière, A., Zucker, J. D., Drogoul, A., Tranouez, P., ... & Taillandier, P. (2022).\n Exploring multi-modal evacuation strategies for a landlocked population using large-scale agent-based simulations.\n International Journal of Geographical Information Science, 36(9), 1741-1783.";
	
	init {
		create intersection from: intersections_shape_file;
		create building from: buildings_shape_file with: (num_car: int(get(("car"))),num_moto: int(get(("moto"))),num_bicycle: int(get(("bicycle"))),num_pedestrian: int(get(("pedest"))), urban_block_id:int(get("block")));
		create evacuation_point from: evacuation_shape_file {
			closest_intersection <- intersection[inters];
			closest_intersection.evacuation <- self;
			location <- closest_intersection.location;
			id <- -1;
		}
		evacuation_point current_ev <- evacuation_point closest_to {world.shape.width, world.shape.height};
		int i <- 0;
		loop while: not empty(evacuation_point where (each.id = -1)) {
			current_ev.id <- i;
			i <- i +1;
			list<evacuation_point> pts <- evacuation_point where (each.id = -1);
			if not empty(pts) {
				current_ev <- pts closest_to current_ev;
			}
		}
		create road from: roads_shape_file;
		ask building {
			closest_intersection <- intersection[inters];
			closest_evac <- evacuation_point[evacua];
		}
		ask road {	
			linked_road <- linked = -1 ? nil : road[linked];
			num_lanes <- lanes;
			geom_display <- shape + (num_lanes * lane_width);
			capacity <- round(num_lanes * shape.perimeter);
		}
		
		if (change_road_lane_perimeters > 0) {
			
			list<road> rds <- (road sort_by (-1 * each.use));
			float dist;
			loop while: dist < change_road_lane_perimeters {
				road r <- first(rds);
				rds >> r;
				r.num_lanes <- r.num_lanes + 1;
				dist <- dist + r.shape.perimeter;
			}
		}
		road_network <- (as_driving_graph(road,intersection) with_shortest_path_algorithm #NBAStar) use_cache false;
		priority_min_road <- road min_of each.priority;
			
		
		
		ask building {
			if num_bicycle > 0 {
				create bicycle number: num_bicycle with: (home:self);
			}
			if num_car > 0 {
				create car number: num_car with: (home:self);
			}
			if num_moto > 0 {
				create motorbike number: num_moto with: (home:self);
			}
			if num_pedestrian > 0 {
				create pedestrian number: num_pedestrian with: (home:self);
			}
		}
		
		
		float distance_max <- building max_of each.distance;
		
		ask people {
			evacuation_time <- TIME_MAX * beta *((alpha * rnd(1.0)) + (1 - alpha) * home.distance/distance_max);
		}
		
		people_ordered <- shuffle(people);
		init_num_people <- length(people_ordered);
	}
	
	reflex update_priority_graph {
		
		people_ordered <- [];
		ask shuffle(road) sort_by each.priority {
			list<moving_agent> people_on <- shuffle(list<moving_agent>(all_agents));
			people_ordered <- people_ordered + (people_on sort_by (each.distance_to_goal/ (100 * world.shape.width) - each.segment_index_on_road)) ;
		}
		people_ordered <- people_ordered + shuffle(people- people_ordered);
		
	}
	
	reflex end_sim when:  empty(people) or  time > 15000{
		do pause;
	}

}

species evacuation_point schedules:[]{
	int id;
	int inters;
	map<string, int> nb_arrived;
	intersection closest_intersection;
	
	rgb color <- palette_evac[int(self)];
	aspect default {
		draw sphere(10.0) color: color;
		
	}
	
}

species building schedules:[]{
	rgb color <- #gray;
	int evacua;
	int inters;
	int num_car;
	int num_bicycle;
	int num_moto;
	int num_pedestrian;
	
	evacuation_point closest_evac;
	intersection closest_intersection;
	int urban_block_id;
	float distance;
	aspect default {
		draw shape color: color border: #black depth: 10;
	}
}

species road skills: [road_skill] {
	geometry geom_display;
	int linked;
	int use;
	int lanes;
	float vehicles <- 0.0 update: (all_agents sum_of (moving_agent(each).vehicle_length));
	bool traffic_jam <- false update: (vehicles/ capacity) > tj_threshold;
	int capacity;
	float priority;
	
	
	aspect default {
		draw shape + (lanes * lane_width) color:  #lightgray ;
		//draw shape at: location + {0,0,0.5} color: traffic_jam ? #red : #green end_arrow: 2.0;
	}
	
}

species intersection skills: [intersection_skill] schedules:[]{
	int index <- int(self);
	evacuation_point evacuation;
	aspect default {
		draw square(1.0) color: #magenta;
	}
}
species moving_agent skills: [driving] schedules:[] {
	rgb color <- rnd_color(255);
	bool leave <- false ;
	evacuation_point evac_pt;
	date leaving_date ;
	float proba_use_linked_road <- 1.0;
	float proba_respect_priorities <- 0.5;
	int linked_lane_limit <- 1;
	int lane_change_limit <- -1;
	
	bool parked <- false;

	list<road> roads_with_traffic_jam;
	
	float priority;
	float evacuation_time;
	building home;
	intersection target_node ;
	float time_before_parking <- 0.0;
	float time_stuck <- 0.0;
	
	float politeness_factor_init ;
	float vehicle_length_init;
	float proba_use_linked_road_init <- 1.0;
	float safety_distance_coeff_init;
	float max_acceleration_init;
	float time_headway_init;
	float acc_gain_threshold_init <- 0.2;
	float min_safety_distance_init <- 0.5;
	float proba_respect_priorities_init;
	
	int zone_target;
	init {
		do reset_properties;
	}
	
	action reset_properties {
		 politeness_factor <- politeness_factor_init ;
		 vehicle_length <- vehicle_length_init;
		 proba_use_linked_road <- proba_use_linked_road_init;
		  max_acceleration <- max_acceleration_init;	
		 time_headway <- time_headway_init;
		 safety_distance_coeff <- safety_distance_coeff_init;
		 acc_gain_threshold <- acc_gain_threshold_init;
		min_safety_distance <- min_safety_distance_init;
		proba_respect_priorities <- proba_respect_priorities_init;
	}
	
	action to_park {
		proba_use_linked_road <- 0.0;
	}
	
	
	action choose_evacuation_point(intersection source) {
		map<road,float> weights <- roads_with_traffic_jam as_map (each::(each.shape.perimeter * world.shape.width));
		road_network <- road_network with_weights weights;
		using topology (road_network) {
			evac_pt <-  evacuation_point with_min_of (source distance_to each.closest_intersection) ;
		}
		weights <- roads_with_traffic_jam as_map (each::(each.shape.perimeter));
		
		color <-evac_pt.color;
		target_node <- evac_pt.closest_intersection;
	}
	
	action compute_path_traffic_jam(intersection source) {
		map<road,float> weights <- roads_with_traffic_jam as_map (each::(each.shape.perimeter * world.shape.width));
		road_network <- road_network with_weights weights;
	
		do compute_path(graph: road_network, source: source, target: target_node);
		weights <- roads_with_traffic_jam as_map (each::(each.shape.perimeter));
		road_network <- road_network with_weights weights;
	
	}
	action initialize {
		location <- (home.location);
	
		intersection current_node <- home.closest_intersection;
		roads_with_traffic_jam <- (list<road>(current_node.roads_in + current_node.roads_out)) where each.traffic_jam; 
		evac_pt <- home.closest_evac;
		
		color <-evac_pt.color;
		target_node <- evac_pt.closest_intersection;
		if current_node = target_node {
			do die;
		}
		do compute_path_traffic_jam(current_node);
	 		
	}
	
	float external_factor_impact(road new_road, float remaining_time) { 
		intersection current_node <- intersection(new_road.source_node);
		if (current_node.evacuation != nil) {
			evac_pt <-intersection(new_road.source_node).evacuation;
			final_target <- nil;
			return -1.0;
		}
		list<road> rds <- (list<road>(current_node.roads_in + current_node.roads_out));
		loop rd over: rds {
			if (rd.traffic_jam)  {
				if  not (rd in roads_with_traffic_jam) {
					roads_with_traffic_jam  << rd;
				}
				
			} else {
				roads_with_traffic_jam >> rd;
			}
		}
		
	if (species(self) != car) and (new_road.traffic_jam) and flip(coeff_change_path *  (self distance_to evac_pt)) and length(intersection(new_road.source_node).roads_out ) > 1 and (new_road.target_node != evac_pt.closest_intersection) {
			if (current_road != nil) {
				do unregister;
				
			}
			do choose_evacuation_point(current_node);
			
			if new_road.source_node = target_node {
				final_target <- nil;
				return -1.0;
			}
			 do compute_path_traffic_jam(current_node);
			return -1.0;	
			
		}
		return remaining_time;
	}
	
	action arrived(evacuation_point evac) {
		evac.nb_arrived[string(species(self))] <- evac.nb_arrived[string(species(self))] + 1;
		if current_road != nil {
			do unregister;
			
		}
		do die;
	}
	
	reflex time_to_leave when: not leave and  time > evacuation_time {
		leave <- true;
		leaving_date <- copy(current_date);
		do initialize;
	}
	reflex move_to_target when:leave and (final_target != nil ) {
		do drive;
		if parked {
			if real_speed > 0.0 {
				parked <- false;
				time_stuck <- 0.0;
				do reset_properties;
			}
		} else if time_before_parking > 0.0 {
			if (real_speed = 0.0) and using_linked_road  and (moving_agent(leading_vehicle) != nil ) and not dead(leading_vehicle) and not moving_agent(leading_vehicle).using_linked_road and (moving_agent(leading_vehicle).vehicle_length_init >= vehicle_length_init){
				time_stuck <- time_stuck + step;
			}else {
				time_stuck <- 0.0;
			}
			if (time_stuck > time_before_parking ) {
				parked <- true;
				do to_park;
				do force_move(road(current_road).num_lanes - 1,max_acceleration,step);
			}
		}
		if final_target = nil {
			do arrived(evac_pt);
		}
	}
	aspect default {
		if (current_road != nil) {
			point pos <- compute_position();
			draw rectangle(vehicle_length  * display_coeff, lane_width * num_lanes_occupied * display_coeff) 
				at: pos color: color rotate: heading;// border: #black depth: 1.0;
			draw triangle(lane_width * num_lanes_occupied * display_coeff ) 
				at: pos color: parked ? #red : color rotate: heading + 90 border: #black;
		}
	}
	aspect demo {
		if (current_road != nil) {
			point pos <- compute_position();
			draw circle(vehicle_length_init  /1.5 ) at: pos color: rgb(color.red, color.green, color.blue, 0.5);
			
			draw rectangle(vehicle_length_init , lane_width * num_lanes_occupied) 
				at: pos color: color rotate: heading;// border: #black depth: 1.0;
			draw triangle(lane_width * num_lanes_occupied ) 
				at: pos color: color rotate: heading + 90 ;//border: #black;depth: 1.5;
		}
	}
	
	point compute_position {
		// Shifts the position of the vehicle perpendicularly to the road,
		// in order to visualize different lanes
		if (current_road != nil) {
			float dist <- (road(current_road).num_lanes - lowest_lane -
				mean(range(num_lanes_occupied - 1)) - 0.5) * lane_width;
			if violating_oneway {
				dist <- -dist;
			}
		 	point shift_pt <- {cos(heading + 90) * dist, sin(heading + 90) * dist};	
		
			return location + shift_pt;
		} else {
			return {0, 0};
		}
	}
	
	
	
}


species car parent: moving_agent schedules:[]{
	float vehicle_length_init <- 3.8#m;
	int num_lanes_occupied <- 2;
	float max_speed <- 160 #km/#h;
	float max_acceleration_init <- rnd(3.0,5.0);	
	float time_headway_init <- gauss(1.25,0.25) min: 0.5;
	float politeness_factor_init <- 0.25 ;
	float acc_bias <- 0.0;
	int linked_lane_limit <- 0;
	float proba_use_linked_road_init <- 0.0;
	
	float proba_respect_priorities_init <- 0.0;
	float acc_gain_threshold_init <- 0.2;
	float min_safety_distance_init <- 0.5;
	
	float max_safe_deceleration <- 4.0;
	float time_before_parking <- #max_float;
	
}

species pedestrian parent: moving_agent schedules:[]{
	float vehicle_length_init <- 0.28#m;
	int num_lanes_occupied <- 1;
	float max_speed <- gauss(1.34,0.26) min: 0.5;
	float max_acceleration_init <- rnd(1.1,1.6);
	float safety_distance_coeff_init <- 0.2;
	float time_headway_init <- gauss(0.5,0.1) min: 0.25;
	float politeness_factor_init <- 0.0;
	float max_safe_deceleration <- 2.0;
	float acc_bias <- 0.0;
	float lane_change_cooldown <- 0.0;
	
	
	float proba_respect_priorities_init <- 0.0;
	float acc_gain_threshold_init <- 0.01;
	float min_safety_distance_init <- 0.2;
	
	float time_before_parking <- rnd(5.0, 10.0);
	
	
	aspect default {
		if (current_road != nil) {
			point pos <- compute_position();
			draw triangle(vehicle_length *3) rotate: heading + 90 at: pos color: color  border:  parked ? #red :#black ;//depth: 1.0;
		}
	}
	
	aspect demo {
		if (current_road != nil) {
			point pos <- compute_position();
			draw circle(vehicle_length_init ) at: pos  color: rgb(color.red, color.green, color.blue, 0.5);
			
			draw circle(vehicle_length_init ) 
				at: pos color: color ;// border: #black depth: 1.0;
		}
	}
}


species bicycle parent: moving_agent schedules:[]{
	float vehicle_length_init <- 1.71#m;
	int num_lanes_occupied <- 1;
	float max_speed <- gauss(13.48,4.0) #km/#h min: 5 #km/#h;
	float max_acceleration_init <- rnd(0.8,1.2);
	float safety_distance_coeff_init <- 0.2;
	float time_headway_init <- gauss(1.0,0.25) min: 0.25;
	float politeness_factor_init <- 0.05;
	float max_safe_deceleration <- 2.0;
	float acc_bias <- 0.0;
	float lane_change_cooldown <- 0.0;
	
	
	float proba_respect_priorities_init <- 0.0;
	float acc_gain_threshold_init <- 0.05;
	float min_safety_distance_init <- 0.2;
	
	
	float time_before_parking <- rnd(10.0, 20.0);
	
}


species motorbike parent: moving_agent schedules:[]{

	float vehicle_length_init <- 1.9#m;
	int num_lanes_occupied <- 1;
	float max_speed <-70 #km/#h ;
	float max_acceleration_init <- rnd(2.8,5.0);
	float safety_distance_coeff_init <- 0.2;
	float time_headway_init <- gauss(1.09,0.5) min: 0.25;
	float politeness_factor_init <- 0.1;
	float max_safe_deceleration <- 3.0;
	float acc_bias <- 0.0;
	float lane_change_cooldown <- 0.0;
	
	
	float proba_respect_priorities_init <- 0.0;
	float acc_gain_threshold_init <- 0.1;
	float min_safety_distance_init <- 0.2;
	
	
	float time_before_parking <-  rnd(10.0, 30.0);
	
}

experiment main type: gui {
	float minimum_cycle_duration <- 0.01;
	action _init_ {
		create simulation with: (alpha:1.0, beta:1.0, coeff_change_path:0.01, tj_threshold:0.75);
	}
	output {
		layout  tabs:false editors: false consoles: false navigator: false;
		display map type: opengl axes: false background: #black{
			species road refresh: false;
			species intersection;
			species evacuation_point transparency: 0.3  refresh: false;
			species building refresh: false;
			species car ;
			species motorbike ;	
			species bicycle ;	
			species pedestrian ;
			
			 overlay position: { 5, 5 } size: { 320 #px, 60 #px } background: #gray transparency: 0.1 border: #black rounded: true
            {
            	draw "Evacuation: " + (init_num_people - length(people))+ "/" + init_num_people + " people" font: font(20) color: #white at: { 10#px, 35#px } ;

            }
            
            graphics "model_description" {
            	draw (rectangle(800, 300).contour + 2) at: {0, 1000} color: #red;
            	draw model_description_1 font: font(10) color: #white at: {-370, 900};
            	draw model_description_2 font: font(10) color: #white at: {-370, 940};
            	draw model_description_3 font: font(10) color: #white at: {-370, 980};
            	draw model_description_4 font: font(10) color: #white at: {-370, 1020};
            	draw model_description_5 font: font(10) color: #white at: {-370, 1060};
            	draw model_description_6 font: font(10) color: #white at: {-370, 1100};
            }
		}
		
	}
}

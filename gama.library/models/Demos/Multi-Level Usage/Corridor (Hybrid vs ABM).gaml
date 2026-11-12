/**
* Name: Corridor - Hybrid vs ABM Multi-Level Architecture
* Author: Ngoc Anh, Jean-Daniel Zucker
* Description: Compares two modeling approaches for pedestrian corridor transit using multi-level architecture.
*   One corridor captures entering pedestrians and computes their transit time mathematically (hybrid model):
*   pedestrians are invisible while inside and are released at the exit after the computed travel time. The
*   other corridor lets pedestrians navigate freely (pure ABM). Running both side by side illustrates the
*   trade-off between computational efficiency (hybrid) and spatial accuracy (ABM) in pedestrian simulation.
*   Teal marks individual agents in the ABM lane; amber marks the hybrid lane.
*   The hybrid corridor's occupancy bar represents captured agents, not individual positions.
* Tags: multi_level, agent_movement, hybrid, ABM, corridor, pedestrian, capture, release
*/

model pedestrian_corridor_Hybrid_vs_ABM

global 
{
	
	//Parameters of the environment
	int environment_width <- 200 const: true;
	int environment_height <- 100 const: true;
	geometry shape <- rectangle(environment_width, environment_height);	
	rgb backdrop <- rgb(18, 26, 40);
	rgb lettering <- rgb(220, 231, 242);
	rgb abm_color <- rgb(83, 218, 196);
	rgb hybrid_color <- rgb(244, 191, 100);
	
	
	float pedestrian_speed <- 2.0;
 
 	//Parameters of the corridors
	int corridor_width <- 160 ;
	int corridor_height <- (int(environment_height * 0.1));

	point corridor_location_0 <- {environment_width / 2, environment_height / 4};
	geometry corridor_shape_0 <- ( (rectangle ({corridor_width, corridor_height})) at_location corridor_location_0) ;

	point corridor_location_1 <- {environment_width / 2, environment_height * 0.75};
	geometry corridor_shape_1 <- ( (rectangle ({corridor_width, corridor_height})) at_location corridor_location_1) ;

	//Parameters of pedestrian generation
	int new_pedestian_generate_frequency <- 2 update: rnd(10) + 1;
	point pedestrian_source_0 <- {0, corridor_location_0.y} ;
	point pedestrian_source_1 <- {0, corridor_location_1.y} ;
	 
	 
	 
	init 
	{
		create corridor number: 2 returns: new_corridors;
		
		ask (new_corridors at 0) 
		{
			do init_corridor(corridor_shape: corridor_shape_0, is_hybrid: false);
		}

		ask (new_corridors at 1) 
		{
			do init_corridor(corridor_shape: corridor_shape_1, is_hybrid: true); 
		}
	}

	//Generation of pedestrians according to the frequency of generation : one for each corridor
	reflex generate_pedestrians when: ( (cycle mod new_pedestian_generate_frequency) = 0 ) 
	{ 
		create pedestrian number: 2 returns: new_pedestrians;
		
		ask (new_pedestrians at 0) 
		{
			do init_location(loc: pedestrian_source_0);
		}
		
		ask (new_pedestrians at 1) 
		{
			do init_location(loc: pedestrian_source_1);
		}
	}	
}


//Species for the pedestrians which can move
species pedestrian skills: [moving] {
	corridor last_corridor;
	point target_location;
	float speed <- pedestrian_speed;
	rgb color;
	
	//Initialisation of the target location according to its generation location
	action init_location (point loc) {
		location <- loc;
		target_location <- {environment_width, location.y};
		int i <- rnd(255);
		color <- rgb(i,i,i);
	}

	//Reflex to move the agent to its target location and make it die once it reached its target
	reflex move_left 
	{
		do move(heading: (self) towards (target_location)); 
		
		if ( (target_location.x - location.x) <= speed ) 
		{
			do die();
		}
	}
	 
}

//Species for the corridor which can capture pedestrians
species corridor  {
	bool capture_pedestrians;
	
	
	action init_corridor (geometry corridor_shape, bool is_hybrid) 
	{
		shape <- corridor_shape;
		capture_pedestrians <- is_hybrid;
	}

	float max_speed <- pedestrian_speed;
	float macro_length min: 0.0 <- float(corridor_width); // the length of macro_patch
	
	//Species to model the pedestrian captured by the corridor
	species captured_pedestrian parent: pedestrian schedules: [] 
	{
		float released_time;  
		
		aspect default { }
	}
	
	init 
	{ 
		create corridor_info_drawer(target : self);
	}
	//Reflex to capture the pedestrians overlapping the corridor ie entering the corridor 
	reflex aggregate when: capture_pedestrians 
	{
		list<pedestrian> tobe_captured_pedestrians <- (pedestrian overlapping shape) where ( (each.last_corridor != self) and ((each.location).x < (self.location).x) ) ; 
		
		if !(empty (tobe_captured_pedestrians)) 
		{
			capture tobe_captured_pedestrians as: captured_pedestrian returns: cps { 
				last_corridor <- myself;
			}
			if !(empty (cps)) 
			{
					//Computation of the time when the pedestrians captured will be released according to the speed
					// and the time they should make to pass the corridor if they move by themselves
					float group_outgoing_time <- time + (corridor_width / (pedestrian_speed) ); 
					loop cp over: cps 
					{
							cp.released_time <- group_outgoing_time;
	 				}
			}
		
		}
	}
	
	//Reflex to release the pedestrians when we consider they were enough time inside the corridor 
	reflex disaggregate  
	{
		list tobe_released_pedestrians <- members where (time >= (captured_pedestrian (each)).released_time);
		
		if !(empty (tobe_released_pedestrians)) 
		{
			
			release tobe_released_pedestrians as: pedestrian in: world returns: released_pedestrians;
			
			loop rp over: released_pedestrians 
			{
				rp.speed <- pedestrian_speed;
				rp.location <- {((environment_width / 2) + (corridor_width / 2)), ((corridor_shape_1).location).y};
			}
		}
	}

}
 
species corridor_info_drawer 
{
	corridor target;
	
	aspect base 
	{
		rgb tint <- target.capture_pedestrians ? hybrid_color : abm_color;
		point label_position <- {target.location.x - target.shape.width / 2, target.location.y + 13};
		if target.capture_pedestrians 
		{
			draw "HYBRID / timed capture & release" color: tint at: label_position
				anchor: #left_center font: font("Helvetica", 12, #bold);
			draw "Captured: " + length(target.members) color: lettering
				at: {target.location.x + target.shape.width / 2, label_position.y}
				anchor: #right_center font: font("Helvetica", 11);
		} 
		else 
		{
			draw "ABM / individual movement" color: tint at: label_position
				anchor: #left_center font: font("Helvetica", 12, #bold);
			draw "Walking: " + (pedestrian count (each.location.y < environment_height / 2)) color: lettering
				at: {target.location.x + target.shape.width / 2, label_position.y}
				anchor: #right_center font: font("Helvetica", 11);
		}
	}
}

experiment default_experiment type: gui autorun: true
{
	float minimum_cycle_duration <- 10 #msec;
	output 
	{
		display default_display background: backdrop type: 2d
		{
			graphics direction {
				loop lane over: [corridor_location_0, corridor_location_1] {
					draw line([{3, lane.y}, {17, lane.y}]) color: lettering end_arrow: 2;
					draw line([{183, lane.y}, {197, lane.y}]) color: lettering end_arrow: 2;
				}
			}
			species corridor {
				rgb tint <- capture_pedestrians ? hybrid_color : abm_color;
				draw shape color: rgb(tint, 0.08) border: rgb(tint, 0.65) width: 1.5;
				if (capture_pedestrians and !empty(members)) {
					float occupied_width <- min(float(corridor_width), float(length(members)) * 2.0);
					draw rectangle(occupied_width, 1.5)
						at: {location.x - corridor_width / 2 + occupied_width / 2, location.y + 3.5}
						color: rgb(tint, 0.6);
				}
			}
			species pedestrian {
				rgb tint <- location.y < environment_height / 2 ? abm_color : hybrid_color;
				draw circle(1.3) color: tint border: rgb(lettering, 0.6);
			}
			species corridor_info_drawer aspect: base;
			overlay coordinates: #screen position: {12, 12} size: {340, 64} background: backdrop rounded: true {
				draw "TWO LEVELS / TWO TRANSIT MODELS" at: {12#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
				draw "Teal: ABM / Amber: hybrid occupancy (not positions)" at: {12#px, 47#px}
					color: lettering font: font("Helvetica", 11);
			}
		}
	}
}
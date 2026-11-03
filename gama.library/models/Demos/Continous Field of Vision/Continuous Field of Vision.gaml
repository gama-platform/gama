/**
* Name: Continuous Field of Vision
* Author: Patrick Taillandier
* Description: Demonstrates how to compute the field of vision of an agent in the presence of obstacles using
*   the 'masked_by' operator. The operator takes a geometry (the maximum vision cone) and a list of obstacle
*   agents, and returns the visible sub-geometry after masking out areas hidden behind obstacles. The resulting
*   visible area is displayed in real time as the agent moves. This is the standard approach for implementing
*   line-of-sight perception in GAMA.
*   A translucent amber cone shows the potential field of vision; teal marks the area actually visible.
*   Obstacles are extruded for readability only: perception remains a two-dimensional calculation.
* Tags: perception, spatial_computation, masked_by, field_of_vision, obstacle, line_of_sight, agent_movement
*/

model fieldofvision

global {
	//number of obstacles
	int nb_obstacles <- 20;
	
	//perception distance
	float perception_distance <- 40.0;
	
	//precision used for the masked_by operator (default value: 120): the higher the most accurate the perception will be, but it will require more computation
	int precision <- 600;
	bool show_full_cone <- true;
	bool show_target <- true;
	float obstacle_height <- 2.5;
	rgb ground <- rgb(23, 33, 47);
	rgb walls <- rgb(107, 124, 143);
	rgb wall_edges <- rgb(166, 185, 201);
	rgb visible <- rgb(70, 214, 188);
	rgb hidden <- rgb(231, 173, 86);
	rgb agent_color <- rgb(239, 247, 250);
	
	//space where the agent can move.
	geometry free_space <- copy(shape);
	init {
		create obstacle number:nb_obstacles {
			shape <- rectangle(2+rnd(20), 2+rnd(20));
			free_space <- free_space - shape;
		}
		
		create people  {
			location <- any_location_in(free_space);
		}
	}
}

species obstacle {
	aspect default {
		draw shape color: walls border: wall_edges depth: obstacle_height;
	}
}
species people skills: [moving]{
	//zone of perception
	geometry perceived_area;
	
	//the target it wants to reach
	point target ;
	
	reflex move {
		if (target = nil ) {
			if (perceived_area = nil) or (perceived_area.area < 2.0) {
				//if the agent has no target and if the perceived area is empty (or too small), it moves randomly inside the free_space
				do wander(bounds: free_space);
			} else {
				//otherwise, it computes a new target inside the perceived_area .
				target <- any_location_in(perceived_area);
			}
		} else {
			//if it has a target, it moves towards this target
			do goto(target: target);
			
			//if it reaches its target, it sets it to nil (to choose a new target)
			if (location = target)  {
				target <- nil;
			}
		}
	}
	//computation of the perceived area
	reflex update_perception {
		//the agent perceived a cone (with an amplitude of 60°) at a distance of  perception_distance (the intersection with the world shape is just to limit the perception to the world)
		perceived_area <- (cone(heading-30,heading+30) intersection world.shape) intersection circle(perception_distance); 
		
		//if the perceived area is not nil, we use the masked_by operator to compute the visible area from the perceived area according to the obstacles
		if (perceived_area != nil) {
			perceived_area <- perceived_area masked_by (obstacle,precision);

		}
	}
	
	aspect body {
		draw circle(1.7) at: location + {0, 0, 0.3} color: rgb(visible, 0.15);
		draw triangle(1.6) at: location + {0, 0, 0.4} rotate: 90 + heading
			color: agent_color border: visible depth: 0.4;
	}
	aspect perception {
		if (show_full_cone) {
			geometry full_cone <- (cone(heading - 30, heading + 30) intersection world.shape)
				intersection circle(perception_distance);
			draw full_cone color: rgb(hidden, 0.12) border: rgb(hidden, 0.4);
		}
		if (perceived_area != nil) {
			draw perceived_area at: perceived_area.location + {0, 0, 0.05}
				color: rgb(visible, 0.32) border: rgb(visible, 0.8);
		}
		if (show_target and target != nil) {
			draw line([location + {0, 0, 0.15}, target + {0, 0, 0.15}])
				color: rgb(agent_color, 0.45) width: 1.5;
			draw circle(0.9) at: target + {0, 0, 0.2} color: hidden wireframe: true width: 2;
			draw circle(0.25) at: target + {0, 0, 0.2} color: hidden;
		}
	}
}

experiment fieldofvision type: gui {
	
	parameter "Number of obstacles" var: nb_obstacles category: "Environment";
	
	parameter "Perception distance" var: perception_distance category: "Perception";
	
	parameter "Mask precision" var: precision category: "Perception";
	parameter "Show potential vision cone" var: show_full_cone category: "Visualization";
	parameter "Show target and direction" var: show_target category: "Visualization";
	parameter "Obstacle height (display only)" var: obstacle_height min: 0.0 max: 8.0 category: "Visualization";
	
	float minimum_cycle_duration <- 0.05;
	output synchronized: true {
		display view type: 3d background: ground axes: false camera: #from_up_front {
			light #ambient intensity: 170;
			species people aspect: perception;
			species obstacle;
			species people aspect: body;
			overlay position: {14#px, 14#px} size: {228#px, 94#px} background: ground rounded: true {
				draw "LINE OF SIGHT" at: {14#px, 22#px} color: agent_color font: font("Helvetica", 12, #bold);
				draw square(9#px) at: {20#px, 44#px} color: visible;
				draw "Visible area" at: {36#px, 48#px} color: agent_color font: font("Helvetica", 12);
				draw square(9#px) at: {20#px, 70#px} color: hidden;
				draw "Potential cone / target" at: {36#px, 74#px} color: agent_color font: font("Helvetica", 12);
			}
		}
	}
}

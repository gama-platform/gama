/**
* Name: Boids 3D Motion
* Author: 
* Description: This model shows the movement of boids following a goal, and creating a flock .  
*	The goal agent and the boids will move within the 3D space.
* Tags: gui, skill, 3d
*/
model boids_3D 
global torus: torus_environment{ 
	//Number of boids to create
	int number_of_agents <- 100 min: 1 max: 500;
	//Number of obstacles to create
	int number_of_obstacles <- 0 min: 0;
	//Size of the boids
	int boids_size <- 50 min: 1;
	//Maximal speed of the boids
	float maximal_speed <- 15.0 min: 0.1 max: 15.0;
	//Factor for the boids flock
	int cohesion_factor <- 100; 
	int alignment_factor <- 100; 
	float minimal_distance <- 10.0; 
	//MAximal angle of turn for the boids
	int maximal_turn <- 90 min: 0 max: 359; 
	//environment parameters
	int width_and_height_of_environment <- 800;  
	int z_max <- 400;  
	bool torus_environment <- false; 
	//Experiment parameter
	bool apply_cohesion <- true;
	bool apply_alignment <- true;   
	bool apply_separation <- true;   
	bool apply_goal <- true; 
	bool apply_wind <- true;     
	//Wind variable
	point wind_vector <- {0,0,0};   
	//Duration of the goal
	int goal_duration <- 30 update: (goal_duration - 1); 
	//Location of the goal
	point goal <- {rnd (width_and_height_of_environment - 2) + 1, rnd (width_and_height_of_environment -2) + 1 ,(rnd(z_max - 2) + 1)}; 
	gif_file bird0_gif_file <- gif_file("../images/bird.gif");

	geometry shape <- cube(width_and_height_of_environment);
	init {
		//Creation of the boids agents that will be placed randomly within the environment
		create boids number: number_of_agents { 
			location <- {rnd (width_and_height_of_environment - 2) + 1, rnd (width_and_height_of_environment -2) + 1 , (rnd(z_max - 2) + 1)};
		} 
		//Creation of the goal
		create boids_goal {
			location <- goal;
		}
	}
}

//Species boids_goal that will represent the goal agent, using the skill moving
species boids_goal skills: [moving3D] {
	float range init: 20.0;
	
	//Reflex to make the goal agent wander in a certain amplitude and a certain speed, 
	//Respecting the minimal and maximal z values
	reflex wander { 
		do  wander amplitude: 45.0 speed: 20.0; 
		if (location.z) < 0 {
			location <- {location.x,location.y,0};
		} else if (location.z) > z_max {
			location <- {location.x,location.y,z_max};
		}
		goal <- location;
	}
	
	aspect default { 
		draw sphere(10) color: #red ;
	}
} 

//Species boids that will represent the boids agents, using the skill moving
species boids skills: [moving3D] {
	//Attribute for the speed of the boids
	float speed max: maximal_speed <- maximal_speed;
	//Range of sensing of the boids
	float range <- minimal_distance * 2;
	point velocity <- {0,0, 0} ;
	
	//List of the others boids in the range distance of the agent
	list others update: ((boids at_distance range)  - self);
	//Mass center of the "flock" represented as the other boids in the sensing range
	point mass_center update:  (length(others) > 0) ? (mean (others collect (each.location)) )  : location;
	
	//Reflex to apply separation
	reflex separation when: apply_separation {
		point acc <- {0,0,0};
		loop boid over: (boids at_distance (minimal_distance))  {
			acc <- acc - ((location of boid) - location);
		}  
		velocity <- velocity + acc;
	}
	//Reflex to apply alignment
	reflex alignment when: apply_alignment {
		point acc <- (length(others) > 0) ? (mean (others collect (each.velocity))) : {0.0,0.0,0.0};
		acc <- acc - velocity;
		velocity <- velocity + (acc / alignment_factor);
	}
	//Reflex to apply cohesion
	reflex cohesion when: apply_cohesion {
		point acc <- mass_center - location;
		acc <- acc / cohesion_factor;
		velocity <- velocity + acc; 
	}
	//Action to make the agent location within the environment
	action bounding {
		if (location.z) < 0 {
			location <- {location.x,location.y,0};
		} else if (location.z) > z_max {
			location <- {location.x,location.y,z_max};
		}
	}
	//Reflex to make the agent follow the goal
	reflex follow_goal when: apply_goal {
		velocity <- velocity + ((goal - location) / cohesion_factor);
	}
	//Reflex to apply the wind by using the vector of wind
	reflex wind when: apply_wind {
		velocity <- velocity + wind_vector;
	}
	//Action to make the agent moving
	action do_move {  
		if (((velocity.x) as int) = 0) and (((velocity.y) as int) = 0) and (((velocity.z) as int) = 0) {
			velocity <- {(rnd(4)) -2, (rnd(4)) - 2,  ((rnd(4)) - 2)} ; 
		}
		point old_location <- location;
		do goto target: location + velocity;
		velocity <- location - old_location;
	}
	//Reflex to move the agent, calling both bounding and do_move action
	reflex movement {
		do bounding;
		do do_move;
	}
	
	aspect sphere {
		draw sphere(10) color: #green;
	}
	
	aspect image {
		draw bird0_gif_file size: boids_size rotate: heading::(location - boids_goal[0].location) color: #black ;      
	}
}


experiment "3D" type: gui {
	
	parameter 'Number of agents' var: number_of_agents;
	parameter 'Number of obstacles' var:number_of_obstacles;
	parameter 'Boids size' var:boids_size;
	parameter 'Maximal speed' var:maximal_speed;
	parameter 'Cohesion Factor' var:cohesion_factor;
	parameter 'Alignment Factor' var:alignment_factor;
	parameter 'Minimal Distance' var:minimal_distance;
	parameter 'Maximal Turn' var:maximal_turn;
	parameter 'Width/Height of the Environment' var:width_and_height_of_environment;
	parameter 'Z max of the Environment' var:z_max;
	parameter 'Toroidal Environment ?' var:torus_environment;
	
	parameter 'Apply Cohesion ?' var: apply_cohesion;
	parameter 'Apply Alignment ?' var: apply_alignment;   
	parameter 'Apply Separation ?' var: apply_separation;  
	parameter 'Apply Wind ?' var: apply_wind ;    
	parameter 'Follow Goal ?' var:apply_goal;
	parameter 'Direction of the wind' var:wind_vector;
	
	output synchronized: true {
		
		display Sky1 type:3d {
			species boids aspect: image;
			species boids_goal;	
		}
		

	}
}

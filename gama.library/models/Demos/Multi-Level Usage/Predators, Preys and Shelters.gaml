/**
* Name: Predators, Preys and Shelters - Multi-Level Architecture
* Author: Gama Development Team
* Description: A multi-level predator-prey model with shelters that can capture and hide prey. Prey and
*   predator agents move randomly. When a prey detects a predator it flees toward the nearest shelter.
*   A shelter 'captures' fleeing prey (changing their species to 'prey_in_shelter'), making them invisible
*   and unchaseble for a period. After the dwell time they are released in an 'invisible' state for another
*   duration before resuming normal wandering. This demonstrates multi-level architecture as a mechanism
*   for modeling temporary hiding/refuge behaviors.
*   Teal discs are wandering prey, amber discs are fleeing, muted rings are temporarily invisible,
*   and coral arrows are predators. Blue shelter outlines show their current occupancy.
*   Sheltered prey and perception ranges can be shown independently.
* Tags: multi_level, agent_movement, capture, release, predator, prey, shelter, refuge
*/

model preys_predators_shelters

global { 
	rgb backdrop <- rgb(18, 26, 40);
	rgb lettering <- rgb(220, 231, 242);
	bool show_sheltered_preys <- true;
	bool show_perception <- false;
	//Parameters for the prey species
	rgb prey_color <- rgb(83, 218, 196) const: true;
	float prey_perception <- 20.0;
	float prey_size <- 2.0 const: true;
	float prey_speed <- 1.0;
	rgb prey_flee_color <- rgb(244, 191, 100);
	float prey_invisible_speed <- 3 * prey_speed;
	rgb prey_invisible_color <- rgb(133, 153, 177);
	int prey_in_shelter_max_time min: 1 init: 200;
	int prey_invisible_max_time min: 1 max: 100 init: 70; 
	int number_of_preys min: 1 max: 1000 init: 500;
	
	//Parameters for the predator species
	rgb predator_color <- rgb(255, 139, 112) const: true;
	float predator_perception <- 3.0;
	float predator_size <- 4.0;
	float predator_speed <- 1.5;
	int number_of_predators min: 1 max: 100 init: 30; 
	geometry shape <- square(400);
	
	rgb predator_in_shelter_color <- rgb(188, 216, 255) const: true;
	 
	//Parameters for the shelter species
	rgb shelter_color <- rgb(91, 174, 245) const: true;
	float shelter_speed <- 1.5 const: true;
	geometry shelter_shape <- square (50.0);
	int number_of_shelter <- 2 const: true;
	
	
	init {
		create prey number: number_of_preys;
		create predator number: number_of_predators; 
		create shelter number: number_of_shelter {
			shape <- shelter_shape at_location any_point_in(world);
		}
	}
}
//Species prey which can move using the skill moving and its operators
species prey skills: [moving] control: fsm {
	geometry shape <- square (prey_size);
	rgb color <- prey_color;
	
	//List of all predators inside the perception of the prey
	list nearby_predators update:  predator overlapping (shape + prey_perception) ;
	int invisible_time min: 1 <- int(time);

	shelter nearest_shelter;		

	//State to make the prey move randomly when there isn't any predator, if so, change the state to flee
	state move_around initial: true {
		enter {
			color <- prey_color;
		}
		do wander(speed: prey_speed); 
		
		transition to: flee_predator when: !(empty (nearby_predators)); 
	}
	
	//State to make the prey move to the closest shelter to flee from the predator, if no predator are perceived, change the state to move aroung
	state flee_predator {
		enter {
			color <- prey_flee_color;
			nearest_shelter <- shelter closest_to self;
		}
		if !(empty (nearby_predators)) { do move(heading: (self) towards (nearest_shelter), speed: prey_speed);}
		
		transition to: move_around when: (empty (nearby_predators));
	}
	//State to make the prey invisible during a certain time when it is released by the shelters
	state invisible {
		enter {
			color <- prey_invisible_color;
			invisible_time <- int(time);
			heading <- rnd (360.0) ;
		}

		do move(speed:prey_invisible_speed, heading: heading) ;
		transition to: move_around when: ( (time - invisible_time) > prey_invisible_max_time );
	}
	
	aspect default {
		if (show_perception) {
			draw circle(prey_perception) color: rgb(color, 0.025) border: rgb(color, 0.12);
		}
		draw circle(prey_size) color: state = 'invisible' ? rgb(color, 0.15) : color
			border: state = 'invisible' ? color : rgb(lettering, 0.4);
	}
}

//Species predator which can move using the skill moving
species predator skills: [moving] schedules: shuffle (list (predator)) {
	geometry shape <- square (predator_size);
	prey target_prey update: self.choose_target_prey();
	
	//Change the target prey according to the prey who aren't fleeing if it doesn't have any yet
	prey choose_target_prey () {
		if ( (target_prey = nil) or (dead (target_prey) ) ) {
			return one_of ( (list (prey)) where (each.state = 'move_around') );
		}
		
		return target_prey;
	}
	//Reflex to move randomly when no prey are perceived
	reflex move_around when: (target_prey = nil) { do wander(speed: predator_speed); }
	
	//Reflex to make the predator chase a prey
	reflex chase_prey when: (target_prey != nil) { do move(heading: self towards target_prey, speed: predator_speed);}
	
	aspect default {
		draw triangle(predator_size * 2) rotate: heading + 90 color: predator_color border: rgb(lettering, 0.6);
	} 
} 

//Species shelter that will capture prey agents
species shelter skills: [moving]  frequency: 2 {
	geometry shape <- (square (50.0)) at_location {250, 250};
	
	//List of all preys which are being chased and inside the shelter but not captured yet
	list<prey> chased_preys update: (prey) where ( (each.shape intersects shape) and (each.state = 'flee_predator') );
	
	//Capture all the chased preys inside the shelter and change their species to prey_in_shelter
	reflex capture_chased_preys when: !(empty (chased_preys)) { 
		capture chased_preys as: prey_in_shelter {
			state <- 'in_shelter'; 
			shape <- ( triangle (4.0) ) at_location location;
		}
	}
	
	//Release all the prey_in_shelter after a certain time and change their state to invisible after making their species returned to prey
	reflex release_member_preys {
		list<prey_in_shelter> to_be_released <- (prey_in_shelter) where ( (time - each.in_shelter_time) > prey_in_shelter_max_time );
		 
		release to_be_released in: world as: prey { 
			state <- 'invisible';
			shape <-  at_location (square (prey_size), self.location);   
		}
	} 
	
	//Subspecies prey_in_shelter that will represent the prey agents captured by the shelter
	species prey_in_shelter parent: prey frequency: 2 schedules: ( ( int ( (length (prey_in_shelter)) / 2 ) ) among (list (prey_in_shelter)) ) {
		int in_shelter_time <- int(time);
		
		state in_shelter {
			do wander(speed: shelter_speed);
		}
		
		aspect default {
			if (show_sheltered_preys) {
				draw circle(prey_size) color: rgb(predator_in_shelter_color, 0.65);
			}
		} 
	}
	
	aspect default {
		draw shape color: rgb(shelter_color, 0.12) border: shelter_color width: 2;
		draw "Sheltered: " + length(members) color: lettering
			at: {max(32.0, min(world.shape.width - 32, location.x)), max(8.0, location.y - shape.height / 2 - 7)}
			anchor: #center font: font("Helvetica", 11, #bold);
	}
}

experiment default_experiment type: gui {
	parameter "Show sheltered prey" var: show_sheltered_preys category: "Appearance";
	parameter "Show prey perception" var: show_perception category: "Appearance";
	output {
		display default_display background: backdrop type: 3d axes: false camera: #from_above map_order: true {
			species shelter coordinates: #world aspect: default {
				// Nested species inherit the host's world context; only the historical Z offset is set here.
				species prey_in_shelter aspect: default position: {0, 0, 0.1};
			}
			species prey coordinates: #world aspect: default position: {0, 0, 0.2};
			species predator coordinates: #world aspect: default position: {0, 0, 0.3};
			overlay coordinates: #screen position: {14, 14} size: {300, 170} background: backdrop rounded: true {
				draw "PREDATORS / PREY / SHELTERS" at: {12#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
				draw "Teal discs: wandering / Amber: fleeing" at: {12#px, 48#px}
					color: prey_color font: font("Helvetica", 11);
				draw "Coral arrows: predators / Rings: invisible" at: {12#px, 72#px}
					color: predator_color font: font("Helvetica", 11);
				draw "Blue areas: shelters / Pale dots: sheltered" at: {12#px, 96#px}
					color: shelter_color font: font("Helvetica", 11);
				draw "Outside: " + length(prey) + "   Sheltered: " + sum(shelter collect length(each.members))
					at: {12#px, 124#px} color: lettering font: font("Helvetica", 11);
				draw "Fleeing: " + (prey count (each.state = 'flee_predator')) + "   Predators: " + length(predator)
					at: {12#px, 148#px} color: prey_flee_color font: font("Helvetica", 11);
			}
		}
	}
}
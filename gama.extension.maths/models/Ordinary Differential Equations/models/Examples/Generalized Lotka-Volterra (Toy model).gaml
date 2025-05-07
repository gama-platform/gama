/**
 *  Author: Tri Nguyen-Huu
 *  Description: A generalized Lotka-Volterra model. 
 * The left column and upper row allow to add or remove animal species. 
 * Each animal species has a population that evolves by default independantly from others, according to a logistic law (with a carrying capacity).
 * The buttons in the matrix can be pressed in order to add interactions, "+" denoting a positive action from the upper species towards the left species, 
 * and a "-" denotes a negative interaction. A positive interaction of species "A" towards "B" means that a high density of species high increases the
 * density of species B (for example A is predated by B). A negative interaction of species "A" towards "B" means that a high density of species high 
 * decreases the density of species B (for example B is predated by A).
 * 
 * Common Lotka-Volterra interactions between two spices A and B ares:
 * mutalism: A -> B: +, B -> A: +
 * A predating B: A -> B: -, B -> A: +
 * competition: A -> B: -, B -> A: -
 */

model GeneralizedLotkaVolterra

global {
	// behaviour parameters
	bool saturation <- true;
	bool externalFoodForPredators <- false;
	
	// number of species
	int maxSpecies <- 8;
	
	// vizualisation parameters
	int fontSize <- 9;
	float edgeSpacing <- 10.0;
	bool graphType <- true;
	int xRange <- 200;
	bool showCumulatedGraph <- true;
	
	// names generator parameters
	string language <- "english" among: ["french","english"];

	map<string,list<list<string>>> animal_names <- map(
										"french"::[["Goé","land"],["Ga","zelle"],["Tama","noir"],["La","pin"],["Cou","cou"],["Ca","nard"],["Cha","mois"],["Ecu","reuil"],["Elé","phant"],
										["Droma","daire"],["Pé","lican"],["Sou","ris"],["Pou","let"],["Perro","quet"],["Rossi","gnol"],["Gre","nouille"],["Phaco","chère"],["Maque","reau"],
										["Sar","dine"],["Mou","ton"],["Ser","pent"],["Tor","tue"],["Pu","tois"]],
										"english"::[["Chee","tah"],["Gi","raffe"],["Ele","phant"],["Ra","bbit"],["Squi","rrel"],
										["Chame","leon"],["Bumble","bee"],["Bu","ffalo"],["Ze","bra"],
										["Rattle","snake"],["Bea","ver"],["Sala","mander"],["Hippo","potamus"],["Pa","rrot"],
										["Rhino","ceros"],["Kanga","roo"],["Leo","pard"],["Alli","gator"],
										["Go","rilla"],["Croco","dile"],["Platy","pus"],["Octo","pus"],["Porcu","pine"]]
										);

	// color palette
	list<rgb> color_list <- shuffle([
			rgb(255,222,0),
			rgb(255,108,44),
			rgb(255,80,87),
			rgb(295,85,180),
			rgb(27,63,148),
			rgb(0,167,143),
			rgb(0,174,239),
			rgb(30,181,58)
	]);
	
	// interaction types and corresponding colors
	list<string> possible_type <-["neutral","positive","negative"];
	map<string,rgb> typeColor <- ["neutral"::rgb(200,200,200),"negative"::rgb(250,65,65),"positive"::rgb(150,217,100)];
	
	image_file arrow <- image_file("../../includes/arrow.png");
	
	// model variables
	list<animal> speciesList <- list_with(maxSpecies, nil);
	graph the_graph <- [] ;
	map<pair<animal,animal>,string> edge_type <- [];
	
	geometry shape <- square(1000);
	
	// numerical scheme parameter
	float hKR4 <- 0.01;
	
	init{
		// create the colors for the different animal species
		if (maxSpecies > 8){
			color_list <- list_with(maxSpecies, rgb(0,0,0));
			loop i from: 0 to: maxSpecies-1{
				color_list[i] <- rgb(int(240/maxSpecies*i),int(240/maxSpecies*i),255);
			}
		}

		// initialize the scheduler
		create solver_and_scheduler;
	}
	
//	reflex test {
//		ask animal where (!dead(each) and length(each.interactionMap)>0){
//			write self.name + ' '+self.interactionMap;
//		}
//	}
	
	// update the interaction graph
	reflex layout_graph {
		the_graph <- directed(layout_circle(the_graph,rectangle(world.shape.width * 0.7, world.shape.height*0.7),false));
	}
	
	// apply actions for the buttons that have been pressed since the last time step
	action buttonPressed {
		// identify which button has been pressed
		// **********************************************************************//
		// previously this part of the code was in the solve statement           //
		// button pressed where flagged has pressed, and the flag was removed    //
		// after updating.                                                       //
		// can changes by pushing button make the solver crash ?                 //
		// **********************************************************************//
		button selectedButton <- first(button overlapping (circle(1) at_location #user_location));
		if selectedButton != nil {
			// perform action depending on the button type 
			if (selectedButton.buttonType = 'species'){
				// action for species buttons 
				// if the buttion is in the row, switch to the button in the corresponding column
				if (selectedButton.grid_x > 0) {
					ask selectedButton.oppositeButton {do speciesButtonAction;}
				}else{
					ask selectedButton {do speciesButtonAction;}
				}
			}else{
				// action for buttons in the main matrix
				if (selectedButton.active) and (selectedButton.grid_x != selectedButton.grid_y){
					ask selectedButton {do interactionButtonAction;}
				}	
			}				
		}
	}
}

// definition of species

species animal{
	float t;
	float pop;
	float r;
	float k;
	rgb color;
	map<animal,string> interactionMap;
	
	bool isPredator;
	
	// list of interactions with this animal species, sorted by type
	list<animal> positive_species <-[];
	list<animal> negative_species <-[];
	map<animal,float> interaction_coef <- [];
	
	action change_type(animal ani, string type){
		remove ani from: positive_species;
		remove ani from: negative_species;
		remove key: ani from: interaction_coef;
		if type = "positive" { positive_species <+ ani;}
		if type = "negative" { negative_species <+ ani;}
		if type != "neutral" {interaction_coef <+ ani::(rnd(100)/100);}
	}
	
	// find a predation relationship exists
	action updateInteractionsTypes{
		interactionMap <- [];
		loop ani over: positive_species{
			if (self in ani.positive_species){
				interactionMap << ani::"mutualism";
			}else if (self in ani.negative_species){
				interactionMap << ani::"predator";
			}else {
				interactionMap << ani::"positive";
			}
		}
		loop ani over: negative_species{
			if (self in ani.positive_species){
				interactionMap << ani::"prey";
			}else if (self in ani.negative_species){
				interactionMap << ani::"competition";
			}else {
				interactionMap << ani::"negative";
			}
		}
		isPredator <- "predator" in interactionMap.values;
//		write "updated "+name+": "+interactionMap;
	}
	
	// part of the ODE system
	equation dynamics simultaneously: [animal]{
		diff(pop,t) = r*pop * (1 - pop/k 
			+ int(saturation) * (
			sum((positive_species where (!dead(each))) collect(interaction_coef[each]*each.pop/k)) 
			- sum((negative_species where (!dead(each))) collect(interaction_coef[each]*each.pop/k)))
		);	
    }
        
 	// aspect for the interaction graph
	aspect interactionGraphAspect{
		draw circle(30) color: color;
		draw name anchor: #left_center at: location+{38,-25,0} color: #black font:font("SansSerif", 10, #bold);
	}
	
	
		aspect interactionGraphAspect{
		draw circle(30) color: color;
		draw name anchor: #left_center at: location+{38,-25,0} color: #black font:font("SansSerif", 10, #bold);		
	}
}


species solver_and_scheduler{
	float t;
	float dummy;
	list<float> pop <- list_with(maxSpecies,0.0);
	list<float> cumulatedPop <- list_with(maxSpecies,0.0);
	list<int> lastValues <- list_with(xRange,0);
	list<int> lastCumValues <- list_with(xRange,0);
	
	
	// Master equation. 
	// Do nothing, but needed in order to make the scheduler able to solve the
	// equation system.
	equation dynamics simultaneously: [animal]{ 
		diff(dummy,t) = 0;		
    }
    
	// solve the ODE system assembled from all equations from animal species
	reflex solveEquation {
	    solve dynamics method: "rk4" step_size:0.01;
    }

	// compute statistics
	reflex updateCount{
		loop i from: 0 to: maxSpecies-1{
			pop[i] <- (speciesList[i] != nil)?speciesList[i].pop:0;
			cumulatedPop[i] <- i=0?pop[0]:cumulatedPop[i-1]+pop[i];
		}
		remove from: lastCumValues index: 0;
		lastCumValues << cumulatedPop[maxSpecies-1];
		remove from: lastValues index: 0;
		lastValues << max(pop);
	}
	
	
}


// buttons grid for the interaction matrix
grid button width:maxSpecies+1 height:maxSpecies+1 
{
	string type <- "neutral";
	bool active <- false;
	button oppositeButton <- button[self.grid_y,self.grid_x];
	string buttonType <- 'arrow';
	point buttonDimensions <- {shape.width * 0.8,shape.height * 0.8};
	point locationShift <-  (grid_x = 0)?{-0.1*shape.width,0}:{0,-0.1*shape.height};
	point cornerShift <- {2 #px, -3 #px};
	
	
	init{
		// sets the type of button action
		if (grid_x*grid_y > 0){
			buttonType <- (grid_x = grid_y)?'none':'interaction';
		}else if (grid_x > 0 or grid_y > 0){
			buttonType <- 'species';
		}
		// sets the dimensions of the button
		if (grid_x = 0 and grid_y > 0){
			buttonDimensions <- {shape.width, shape.height * 0.8};
			color <- color_list[grid_y - 1];
		}
		if (grid_y = 0 and grid_x > 0){
			buttonDimensions <- {shape.width * 0.8, shape.height};
			color <- color_list[grid_x - 1];
		}
		
	}
	
	// action for species modification button
	action speciesButtonAction{
		if (active) {
		// remove animal species and reset interactions
			animal species_to_be_removed <- speciesList[grid_y - 1];
			the_graph <- species_to_be_removed remove_node_from the_graph;
			ask button where ((each.grid_x = self.grid_y) or (each.grid_y = self.grid_y)) {
				self.type <- "neutral";
			}

			ask animal {
				remove species_to_be_removed from: self.positive_species;
				remove key: species_to_be_removed from: self.interaction_coef;
				do updateInteractionsTypes;
			}

			ask species_to_be_removed {
				do die;
			}

			speciesList[self.grid_y - 1] <- nil;
		} else {
		// add a new animal species
			create animal {
				name <- animal_names[language][rnd(length(animal_names[language]) - 1)][0] + animal_names[language][rnd(length(animal_names[language]) - 1)][1];
				speciesList[myself.grid_y - 1] <- self;
				r <- rnd(100) / 1000;
				k <- 30.0 + rnd(50);
				pop <- 1.0;
				self.color <- myself.color;
				the_graph <- the_graph add_node speciesList[myself.grid_y - 1];
			}

		}

		// change active level for the button and the corresponding interaction buttons
		active <- !active;
		oppositeButton.active <- !oppositeButton.active;
		ask button where (each.grid_x > 0 and each.grid_y > 0 and each.grid_x > 0) {
			self.active <- (speciesList[self.grid_x - 1] != nil) and (speciesList[self.grid_y - 1] != nil);
		}
	}
	
	// action for type of interaction modification button
	action interactionButtonAction{
		string new_type <- possible_type[mod(possible_type index_of(type)+1,length(possible_type))];
		// change the button to the new type
		type <- new_type;
		// add the interaction type to the concerned animal species (x->y)
		ask speciesList[grid_y - 1] {
			do change_type(speciesList[myself.grid_x - 1], new_type);
			do updateInteractionsTypes;
		}
		if (new_type != 'neutral'){
			add edge(speciesList[grid_x - 1], speciesList[grid_y - 1]) to: the_graph;
			add (speciesList[grid_x - 1]::speciesList[grid_y - 1])::new_type to: edge_type;	
		}else{
			write "before "+the_graph.edges;
			remove edge(speciesList[grid_x - 1], speciesList[grid_y - 1]) from: the_graph;
			write "after "+the_graph.edges;
		}
		ask speciesList[grid_x - 1]{
			do updateInteractionsTypes;
		}
	}
	
	
	aspect modern {
		if (buttonType = 'species'){
			// rectangle
			draw rectangle(buttonDimensions) at: location + locationShift  color: active?color:rgb(230,230,230);
			
			if !active {
				//question mark for inactive species
				point textShift <- (grid_x = 0)?{-0.15*shape.width,0,0.1}:{-0.05*shape.width, -0.1 * shape.height,0.1};
				draw "?" font:font("Arial", 40, #bold)  at: location + textShift anchor: #center color: #white;
			}
			// column
			if (grid_x = 0 and speciesList[grid_y - 1] != nil)  {
				draw speciesList[grid_y -1].name font:font("SansSerif", fontSize, #bold) anchor: #bottom_left at: location + locationShift + {-buttonDimensions.x/2,buttonDimensions.y/2,0.1} + cornerShift color: #white;
			} 
			// row
			if (grid_y = 0 and speciesList[grid_x - 1] != nil)  {
				draw speciesList[grid_x -1].name font:font("SansSerif", fontSize, #bold) rotate: -90 anchor: #bottom_left at: location + locationShift + {buttonDimensions.x/2,buttonDimensions.y/2,0.1} + {cornerShift.y,-cornerShift.x}  color: #white;
			}
			
		} else if (buttonType = 'arrow'){
			draw arrow size: shape.width *0.7;
		}else if (buttonType = 'interaction' and active){
			if (type = "neutral"){
				draw rectangle(shape.width * 0.8,shape.height * 0.8) color: rgb(230,230,230);// border: rgb(210,210,210);
			}else{
				draw rectangle(shape.width * 0.8,shape.height * 0.8) color: typeColor[type];
			}
				
			// draw a "+" or "-"
			if type != "neutral" {draw rectangle(shape.width * 0.55,shape.height * 0.15) color: #white at: location + {0,0,0.1};}
			if type = "positive" {draw rectangle(shape.width * 0.15,shape.height * 0.55) color: #white at: location + {0,0,0.1};}
		}else{
			draw rectangle(buttonDimensions) color: rgb(245,245,245) ;
		}
	}
}







experiment Simulation type: gui autorun: true  {
	// limit the speed of the simulation
	float minimum_cycle_duration <- 0.1;
	
	// Help section.
 	text "Click on '?'s to add animal species, then click on grey squares to change among 3 types of interactions:
'+' means that the upper species has a positive impact on the left species (e.g. the left one eats the top one). 
'-' is for negative impact.
grey is neutral." category: "Help";
	text "https://en.wikipedia.org/wiki/Generalized_Lotka-Volterra_equation" category: "Help" color: rgb(241, 196, 15);
 	category "Help" expanded: true color: rgb(46, 204, 113);
 	
 	// Language section
 	parameter "Language for animal names" var: language category: "Language";
 	
 	// Display options
 	parameter "Font size" var: fontSize category: "Display" min: 1 max: 15;
 	parameter "Graph edges spacing" var: edgeSpacing category: "Display" min: 0.0 max: 20.0;
 	
 	// output definition
 			
	output { 
		// set the window layout
		layout value: horizontal([0::50,vertical([1::50,2::50])::50]) tabs:false;
		
		// left display: interaction matrix
		display action_button name:"Species interactions" toolbar: false type:3d axes: false{
			camera 'default' location: {500.0,500.0231,1273.0} target: {500.0,500.0,0.0} locked: true;
//			camera name: 'myCamera' locked: true;
			light #default intensity: 120;
			species button aspect: modern;
			
			// event listener for mouse clicks
			event #mouse_down {ask simulation {do buttonPressed;}} 
			
			// window title
			overlay position: {0, 0} size: { 0 #px, 0 #px } background: #white transparency: 0.0{
				draw string("Interaction matrix") at: {10 #px,10 #px}  anchor: #top_left color: #black font: font("SansSerif", 15, #bold) ; 
            }
		}
		
		// top-right display: charts
		display "Graphs" name: "Charts" refresh: every(1#cycle) type: 3d toolbar: false axes: false{
			camera name: "myCamera" locked: true;
			
			// chart definition: cumulated population
			chart "Cumulated population size" type: series style: area background: #white 
			x_range: xRange x_tick_line_visible: false y_range: {0,30 + max(100,max(first(solver_and_scheduler).lastCumValues))} 
			y_label: "Population (cumulated)"
			visible: showCumulatedGraph series_label_position: none title_visible: false label_font: font("SansSerif", 16) {
				loop i from: 0 to: maxSpecies-1{
					data "species"+i value: first(solver_and_scheduler).cumulatedPop[i] color: color_list[i] marker: false;
				}
			}
			
			// chart definition: time series
			chart "Time series" type: series style: line background: #white 
			x_range: xRange x_tick_line_visible: false y_range: {0,30 + max(30,max(first(solver_and_scheduler).lastValues))} 
			y_label: "Population"
			visible: !showCumulatedGraph series_label_position: none title_visible: false label_font: font("SansSerif", 16) {
				loop i from: 0 to: maxSpecies-1{
					data "species"+i value: first(solver_and_scheduler).pop[i] color: color_list[i] marker: false thickness: 3;
				}
			}
			
			// mouse event listener
			event #mouse_down {showCumulatedGraph <- !showCumulatedGraph;} 
			
			// window title
			overlay position: {0, 0} size: {2000, 32#px} background: #white transparency: 0 rounded: false{
				draw string("Population evolution") at: {10 #px,10 #px}  anchor: #top_left color: #black font:font("SansSerif", 15, #bold) ; 
            }
           
		}
		
		// bottom-right display: interaction graph
		display "Interaction graph" toolbar: false type:3d axes: false {
			camera #default locked: true;
			graphics "edges" {
				list<pair> myEdges <- the_graph.edges;
				// draw the edges
				loop while: !empty(myEdges){
					pair<animal,animal> myEdge <- first(myEdges);
					remove myEdge from: myEdges;
					animal ani1 <- first(myEdge);
					animal ani2 <- last(myEdge);
					if ((ani2::ani1) in myEdges){
						// reciprocal interaction
						remove (ani2::ani1) from: myEdges;
						float angle <- ani1 towards ani2;
						point centre <- centroid(polyline([ani1.location,ani2.location]));
						if (ani1.interactionMap[ani2] = 'predator'){
							draw geometry(myEdge) + 8  color: #red;
							draw triangle(40) rotate: angle + 90 at: centre + {cos(angle),sin(angle),0}*12 color: #red;
						}
						if (ani1.interactionMap[ani2] = 'mutualism'){
							if (angle >= 90 and angle < 270){angle <- angle + 180;}
							draw geometry(myEdge) + 3  color: rgb(53,174,36);
							draw "Mutualism" rotate: angle at: centre + {sin(angle),-cos(angle),0}*edgeSpacing*3 
								anchor: #center color: #black font:font("SansSerif", 10, #bold);
							int textLength <- 110;
							draw triangle(40) rotate: angle + 90 at: centre +{cos(angle),sin(angle),0} * textLength +  {sin(angle),-cos(angle),0}*edgeSpacing*3 color: rgb(53,174,36);
							draw triangle(40) rotate: angle + 270 at: centre - {cos(angle),sin(angle),0} * textLength +  {sin(angle),-cos(angle),0}*edgeSpacing*3 color: rgb(53,174,36);
						}
						
					} else {
						// one way interaction
						float angle <- first(myEdge) towards last(myEdge);
						point centre <- centroid(polyline([first(myEdge).location,last(myEdge).location]));
						rgb myColor <- edge_type[myEdge] = "negative"?#red:rgb(53,174,36);
						draw geometry(myEdge) + 3  color: myColor;
						draw triangle(20) rotate: angle + 90 at: centre + {cos(angle),sin(angle),0}*12 color: myColor;
					}
				}
				
//				loop edge over: the_graph.edges {
//					// mandatory cast
//					pair<animal,animal> myEdge <- edge;
//					float angle <- first(myEdge) towards last(myEdge);
//					point centre <- centroid(polyline([first(myEdge).location,last(myEdge).location]));
//					rgb myColor <- edge_type[myEdge] = "negative"?#red:rgb(53,174,36);
//					draw geometry(edge) + 3 at: centre + {sin(angle),-cos(angle),0}*edgeSpacing color: myColor;
//					draw triangle(20) rotate: angle + 90 at: centre + {cos(angle),sin(angle),0}*12 + {sin(angle),-cos(angle),0}*edgeSpacing color: myColor;
//				}
 			}
 			species animal aspect: interactionGraphAspect;
 			
 			// window title
 			overlay position: { 0, 0} size: { 0 #px, 0 #px } background: #white transparency: 0.0{
				draw string("Interaction graph") at: {10 #px,10 #px} anchor: #top_left color: #black font:font("SansSerif", 15, #bold) ; 
            }
		}
	
	}
}

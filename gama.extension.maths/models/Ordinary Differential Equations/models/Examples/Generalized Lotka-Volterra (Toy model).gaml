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
	string growthFunctionType <- "Logistic";
	bool saturation <- true;
	bool externalFoodForPredators <- false;
	
	// number of species
	int maxSpecies <- 8;
	
	// vizualisation parameters
	float fontScale <- 1.0;
	float interactionMatrixcPixelValue <- 1.75;
	float interactionGraphPixelValue <- 3.53;
	float chartPixelValue <- 1.0;
	int matrixFontSize -> round(10 * 1.75/interactionMatrixcPixelValue);
	int questionMarkFontSize -> round(40 * 1.75/interactionMatrixcPixelValue);//floor(40 * fontScale);
//	int overlayFontSize -> floor(20 * 1.75/interactionMatrixcPixelValue);
	int overlayFontSize <- 15;
	int interactionGraphFontSize -> floor(10 * 3.53/interactionGraphPixelValue); // floor(10 * fontScale);
	int edgeFontSize -> floor(8 * 3.53/interactionGraphPixelValue); // floor(8 * fontScale);
	int labelFontSize -> floor(16 * fontScale);
	float edgeSpacing <- 10.0;
	bool graphType <- true;
	int xRangeMax <- 2000;
	int xRange <- 200;
	bool showCumulatedGraph <- true;
	
	// names generator parameters
	string language <- "French" among: ["French","English"];

	map<string,list<list<string>>> animal_names <- map(
										"French"::
											[["Goé","land"],["Ga","zelle"],["Tama","noir"],["La","pin"],["Cou","cou"],
											["Ca","nard"],["Cha","mois"],["Ecu","reuil"],["Elé","phant"],["Droma","daire"],
											["Pé","lican"],["Sou","ris"],["Pou","let"],["Perro","quet"],["Rossi","gnol"],
											["Gre","nouille"],["Phaco","chère"],["Maque","reau"],["Sar","dine"],["Mou","ton"],
											["Ser","pent"],["Tor","tue"],["Pu","tois"],["Anti","lope"], ["Go","rille"],
											["Bou","quetin"],["Co","chon"],["Vi","père"],["Cra","paud"],["Croco","dile"],
											["Mar","motte"],["Gi","rafe"],["Zè","bre"],["Hippo","potame"],["Kangou","rou"],
											["Léo","pard"],["Pin","gouin"],["Re","nard"],["San","glier"],["Vau","tour"]],
										"English"::
											[["Chee","tah"],["Gi","raffe"],["Ele","phant"],["Ra","bbit"],["Squi","rrel"],
											["Chame","leon"],["Bumble","bee"],["Bu","ffalo"],["Ze","bra"],
											["Rattle","snake"],["Bea","ver"],["Sala","mander"],["Hippo","potamus"],["Pa","rrot"],
											["Rhino","ceros"],["Kanga","roo"],["Alli","gator"],
											["Go","rilla"],["Croco","dile"],["Platy","pus"],["Octo","pus"],["Porcu","pine"],
											["Leo","pard"],["Tur","tle"],["Koa","la"],["Vi","per"],["Geo","duck"],
											["Mon","key"],["Ante","lope"],["Barra","cuda"],["Ca","mel"],["Dol","phin"],
											["Fal","con"],["Igua",",na"],["Jelly","fish"],["Mos","quito"],["Pen","guin"]]
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
		
//		write normalized_rotation(rotation_composition(38.0::{1,1,1},90.0::{1,0,0}))=normalized_rotation(115.22128507898108::{0.9491582126366207,0.31479943993669307,-0.0});
//		write gaml_type(rotation_composition(90::{1,0,0},90::{1,0,0})) = gaml_type(1.0::{0,0,0});
////		write is(gaml_type(rotation_composition(90::{1,0,0},90::{1,0,0})),list<float>);
//		write gaml_type([1,2,3]);
//		
//		
////		write is([1,2,3],list<int>);
////		
////		write is([1,2,3],list<int>);
//		
//		
//		
//		write gaml_type(inverse_rotation(38.0::{1,1,1})) = gaml_type(1.0::{0,0,0});
//		write gaml_type(normalized_rotation(-38.0::{1,1,1}));
//		write gaml_type(rotated_by(rectangle(5,10),90, {0,0,1})) = geometry;
//		write rectangle(10,5) - rotated_by(rectangle(5,10),90, {0,0,1}) = nil and rotated_by(rectangle(5,10),90, {0,0,1}) - rectangle(10,5) = nil;
//			
//			
//		

	
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
	
	// update the interaction graph
	reflex layout_graph {
		the_graph <- directed(layout_circle(the_graph,rectangle(world.shape.width * 0.7, world.shape.height*0.7),false));
	}
	
	action updateInteractionsTypes{
		//reset interaction map
		ask animal{
			interactionMap <- [];
		}
		
		loop ani1 over: animal{
			loop ani2 over: animal{
				if (ani2 in ani1.positive_species){
					if (ani1 in ani2.positive_species){
						ani1.interactionMap << ani2::"mutualism";
					}else if (ani1 in ani2.negative_species){
						ani1.interactionMap << ani2::"predator";
					}else {
						ani2.interactionMap << ani1::"positive";
					}
				}
				if (ani2 in ani1.negative_species){
					if (ani1 in ani2.positive_species){
						ani1.interactionMap << ani2::"prey";
					}else if (ani1 in ani2.negative_species){
						ani1.interactionMap << ani2::"competition";
					}else {
						ani2.interactionMap << ani1::"negative";
					}
				}
			}
		}
		
		
//		ask animal where (!dead(each)){
//			self.isPredator <- "predator" in self.interactionMap.values;
//			if (self.isPredator){
//				write self.name + "is a Predator";
//			}
//		}
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
	float saturationCoeff;
	rgb color;
	map<animal,string> interactionMap;
	bool isPredator -> "predator" in interactionMap.values;
	bool toBeRemoved <- false;
	
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
		if type != "neutral" {interaction_coef <+ ani::(0.5+rnd(100)/100);}
	}
	
	// compute the natural growth rate of the population
	float growth{
		if (isPredator and !externalFoodForPredators){
			return - r * pop; // predator dies out if there is no prey
		}else{
			if (growthFunctionType = "Logistic"){
				// the population grows with a logistic function
				return r * pop * (1 - pop/k);
			}else{
				// the population growth with a Malthusian growth
				return r * pop;
			}

		}
	}
	
	// compute the population increase due to predation/competition
	float populationVariationDueToInteractions{
		if (saturation){
			return 10*r/2*pop * sum((positive_species where (!dead(each))) collect(interaction_coef[each]*each.pop/k)) 
			- 0.1*pop/(1 + saturationCoeff * pop/k) * sum((negative_species where (!dead(each))) collect(interaction_coef[each]*each.pop));				
		}else{
			return 10*r/2*pop * sum((positive_species where (!dead(each))) collect(interaction_coef[each]*each.pop/k)) 
			- 0.1*pop * sum((negative_species where (!dead(each))) collect(interaction_coef[each]*each.pop/k));	
		}
	}
	
	
	// part of the ODE system
	equation dynamics simultaneously: [animal]{
		diff(pop,t) = growth() + populationVariationDueToInteractions();
    }
        
 	// aspect for the interaction graph
	aspect interactionGraphAspect{
		draw circle(30) color: color;
		draw name anchor: #left_center at: location+{38,-25,0} color: #black font:font("SansSerif", interactionGraphFontSize, #bold);
	}
}


species solver_and_scheduler{
	float t;
	float dummy;
	list<float> pop <- list_with(maxSpecies,0.0);
	list<float> cumulatedPop <- list_with(maxSpecies,0.0);
	list<int> lastValues <- list_with(xRangeMax,0);
	list<int> lastCumValues <- list_with(xRangeMax,0);
	
	
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
	
	reflex removeAnimals{
		ask animal where (each.toBeRemoved){
			do die;
		}
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
			}

			ask species_to_be_removed {
				// delay the removal in order to avoid conflict when solving the equation
				toBeRemoved <- true;
			}

			speciesList[self.grid_y - 1] <- nil;
			ask world {do updateInteractionsTypes;}
			
		} else {
		// add a new animal species
			create animal {
				// create an animal name from two parts of two existing animals names. Forces that the new name is 
				// not a real name (the two parts do not match)
				string namePart1 <- animal_names[language][rnd(length(animal_names[language]) - 1)][0];				
				string namePart2 <- rnd_choice(map<string,int>(animal_names[language] where (each[0] != namePart1) collect (each[1]::1)));
	
				name <- namePart1 + namePart2; // random nate
				speciesList[myself.grid_y - 1] <- self; // assign a place in the grid
				r <- rnd(100) / 1000; // random growth rate
				k <- 30.0 + rnd(50); // random carrying capacity
				saturationCoeff <- rnd(1.0,4.0); // random saturation coefficient:  as a prey
				pop <- 1.0; // initial population
				self.color <- myself.color; // color in the grid
				the_graph <- the_graph add_node speciesList[myself.grid_y - 1]; // update the interaction graph
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
		}
		if (new_type != 'neutral'){
			add edge(speciesList[grid_x - 1], speciesList[grid_y - 1]) to: the_graph;
			add (speciesList[grid_x - 1]::speciesList[grid_y - 1])::new_type to: edge_type;	
		}else{
			remove edge(speciesList[grid_x - 1], speciesList[grid_y - 1]) from: the_graph;
		}
		ask world {do updateInteractionsTypes;}
	}
	
	
	aspect modern {
		// update resolution to adapt the text font size in the interaction window
		
		if (int(self) = 0){
			interactionMatrixcPixelValue <- #px;
		}
		if (buttonType = 'species'){
			// rectangle
			draw rectangle(buttonDimensions) at: location + locationShift  color: active?color:rgb(230,230,230);
			
			if !active {
				//question mark for inactive species
				point textShift <- (grid_x = 0)?{-0.15*shape.width,0,0.1}:{-0.05*shape.width, -0.1 * shape.height,0.1};
				draw "?" font:font("Arial", questionMarkFontSize, #bold)  at: location + textShift anchor: #center color: #white;
			}
			// column
			if (grid_x = 0 and speciesList[grid_y - 1] != nil)  {
				draw speciesList[grid_y -1].name font:font("SansSerif", matrixFontSize, #bold) anchor: #bottom_left at: location + locationShift + {-buttonDimensions.x/2,buttonDimensions.y/2,0.1} + cornerShift color: #white;
			} 
			// row
			if (grid_y = 0 and speciesList[grid_x - 1] != nil)  {
				draw speciesList[grid_x -1].name font:font("SansSerif", matrixFontSize, #bold) rotate: -90 anchor: #bottom_left at: location + locationShift + {buttonDimensions.x/2,buttonDimensions.y/2,0.1} + {cornerShift.y,-cornerShift.x}  color: #white;
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
//	text "Bonjour le monde";
//	text "Sacrament, <li> y va-tu continuer </li><li>longtemps</li> ?";
//	category "Experiment" expanded: true;
	
 	text "Create your own ecosystem of animal species and visualize the effects of interactions on the population dynamics."
 		+"<p>Click on a '?' to add a new animal species, then click on grey squares to switch from no interaction to:</p>"
		+"<li>'+': the upper species has a positive impact on the left species (e.g. the upper species is a prey of the other one);</li>"
		+"<li>'-': negative impact (e.g. the upper species is a prey of the other one);</li>"
		//+ '<a href="https://www.google.com">link</a>'
		category: "Guidelines";
		
	text "https://en.wikipedia.org/wiki/Generalized_Lotka-Volterra_equation" category: "Guidelines" color: rgb(241, 196, 15);
 	category "Guidelines" expanded: true color: rgb(46, 204, 113);
 	
 	parameter "Growth function" var: growthFunctionType among: ["Exponential","Logistic"] category: "Behaviour";
 	parameter "Saturation" var: saturation category: "Behaviour";
 	parameter "Predators have access to external food" var: externalFoodForPredators category: "Behaviour";
 	parameter "X Range" var: xRange category: "Behaviour" min: 100 max: xRangeMax;
 	
 	
 	category "Behaviour" expanded: true color: rgb(52, 152, 219);
 	
 	// Preferences section
 	parameter "Language for animal names" var: language category: "Preferences";
  	parameter "Font scale" var: fontScale category: "Preferences" min: 0.1 max: 2.0;
 	 	
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
				draw string("Interaction matrix") at: {10 #px,10 #px}  anchor: #top_left color: #black font: font("SansSerif", overlayFontSize, #bold) ; 
            }
		}
		
		// top-right display: charts
		display "Charts" name: "Charts" refresh: every(1#cycle) type: 3d toolbar: false axes: false{
			camera name: "myCamera" locked: true;
			
			// chart definition: cumulated population
			chart "Cumulated population size" type: series style: area background: #white 
			x_range: xRange x_tick_line_visible: false y_range: {0,30 + max(100,max(copy_between(first(solver_and_scheduler).lastCumValues, xRangeMax-xRange, xRangeMax)))} 
			y_label: "Population (cumulated)"
			visible: showCumulatedGraph series_label_position: none title_visible: false label_font: font("SansSerif", labelFontSize) {
				loop i from: 0 to: maxSpecies-1{
					data "species"+i value: first(solver_and_scheduler).cumulatedPop[i] color: color_list[i] marker: false;
				}
			}
			
			// chart definition: time series
			chart "Time series" type: series style: line background: #white 
			x_range: xRange x_tick_line_visible: false y_range: {0,30 + max(30,max(copy_between(first(solver_and_scheduler).lastValues,xRangeMax - xRange, xRangeMax)))} 
			y_label: "Population"
			visible: !showCumulatedGraph series_label_position: none title_visible: false label_font: font("SansSerif", labelFontSize) {
				loop i from: 0 to: maxSpecies-1{
					data "species"+i value: first(solver_and_scheduler).pop[i] color: color_list[i] marker: false thickness: 3;
				}
			}
			
			// mouse event listener
			event #mouse_down {showCumulatedGraph <- !showCumulatedGraph;} 
			
			// window title
			overlay position: {0, 0} size: {2000, 32#px} background: #white transparency: 0 rounded: false{
				draw string("Population evolution") at: {10 #px,10 #px}  anchor: #top_left color: #black font:font("SansSerif", overlayFontSize, #bold) ; 
            }
           
		}
		
		// bottom-right display: interaction graph
		display "Interaction graph" toolbar: false type:3d axes: false {
			camera #default locked: true;
			graphics "edges" {
				interactionGraphPixelValue <- #px;
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
						float positiveAngle <- (angle >= 90 and angle < 270) ? angle + 180 : angle;
						point shift <- {sin(positiveAngle),-cos(positiveAngle),0}*edgeSpacing*3;
						point centre <- centroid(polyline([ani1.location,ani2.location]));
						font edgeFont <- font("SansSerif", edgeFontSize);
						float textLength <- 80.0;
						float triangleSize <- 30.0;
						
						if (ani1.interactionMap[ani2] = 'predator'){
							draw geometry(myEdge) + 6  color: #red;
							draw "Predation" rotate: positiveAngle at: centre + {sin(positiveAngle),-cos(positiveAngle),0}*edgeSpacing*3 
								anchor: #center color: #black font: edgeFont;
							draw triangle(triangleSize) rotate: angle + 90 at: centre + {cos(angle),sin(angle),0} * textLength +  shift color: #red;
						}
						if (ani1.interactionMap[ani2] = 'prey'){
							draw geometry(myEdge) + 6  color: #red;
							draw "Predation" rotate: positiveAngle at: centre + {sin(positiveAngle),-cos(positiveAngle),0}*edgeSpacing*3 
								anchor: #center color: #black font: edgeFont;
							draw triangle(triangleSize) rotate: angle + 270 at: centre - {cos(angle),sin(angle),0} * textLength +  shift color: #red;
						}
						if (ani1.interactionMap[ani2] = 'mutualism'){
							draw geometry(myEdge) + 6  color: rgb(53,174,36);
							draw "Mutualism" rotate: positiveAngle at: centre + {sin(positiveAngle),-cos(positiveAngle),0}*edgeSpacing*3 
								anchor: #center color: #black font: edgeFont;
							draw triangle(triangleSize) rotate: angle + 90 at: centre + {cos(angle),sin(angle),0} * textLength + shift color: rgb(53,174,36);
							draw triangle(triangleSize) rotate: angle + 270 at: centre - {cos(angle),sin(angle),0} * textLength + shift color: rgb(53,174,36);
						}
						if (ani1.interactionMap[ani2] = 'competition'){
							textLength <- textLength * 1.1;
							draw geometry(myEdge) + 6  color: #orange;
							draw "Competition" rotate: positiveAngle at: centre + {sin(positiveAngle),-cos(positiveAngle),0}*edgeSpacing*3 
								anchor: #center color: #black font: edgeFont;
							draw triangle(triangleSize) rotate: angle + 90 at: centre + {cos(angle),sin(angle),0} * textLength + shift color: #orange;
							draw triangle(triangleSize) rotate: angle + 270 at: centre - {cos(angle),sin(angle),0} * textLength + shift color: #orange;
						}
						
					} else {
						// one way interaction
						float angle <- first(myEdge) towards last(myEdge);
						point centre <- centroid(polyline([first(myEdge).location,last(myEdge).location]));
						rgb myColor <- edge_type[myEdge] = "negative"?rgb(255,0,0,0.4):rgb(53,174,36,0.4);
						draw geometry(myEdge) + 2  color: myColor;
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
				draw string("Interaction graph") at: {10 #px,10 #px} anchor: #top_left color: #black font:font("SansSerif", overlayFontSize, #bold) ; 
            }
		}
	
	}
}

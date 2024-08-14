model segregation_base

global {
	//Different colors for the group
	rgb color_1 <- rgb ("yellow");
	rgb color_2 <- rgb ("red");
	rgb color_3 <- rgb ("blue");
	rgb color_4 <- rgb ("orange");
	rgb color_5 <- rgb ("green");
	rgb color_6 <- rgb ("pink");   
	rgb color_7 <- rgb ("magenta");
	rgb color_8 <- rgb ("cyan");
    list colors <- [color_1, color_2, color_3, color_4, color_5, color_6, color_7, color_8] of: rgb;

	
	//Number of groups
	int number_of_groups <- 2 max: 8;
	//Density of the people
	float density_of_people <- 0.7 min: 0.01 max: 0.99;
	//Percentage of similar wanted for segregation
	float percent_similar_wanted <- 0.5 min: float (0) max: float (1);
	//Dimension of the grid
	int dimensions <- 40 max: 200 min: 10;
	//Neighbours distance for the perception of the agents
	int neighbours_distance <- 2 max: 10 min: 1;
	//Number of people agents
	int number_of_people <- 0;
	//Number of happy people
	int sum_happy_people <- 0 update: all_people count (each.is_happy);
	//Number of similar neighbours
	int sum_similar_neighbours <- 0 update: sum (all_people collect each.similar_nearby);
	//Number of neighbours
	int sum_total_neighbours <- 1 update: sum (all_people collect each.total_nearby) min: 1;
	//List of all the places
	list<agent> all_places;
	//List of all the people
	list<base> all_people;  
	
	//Action to write the description of the model in the console
	action description {
		write 		"Description. \n"
				+ 	"Thomas Schelling model of residential segregation is a classic study of the effects of local decisions on global dynamics. Agents with mild preferences for same-type neighbors, but without preferences for segregated neighborhoods, can wind up producing complete segregation.\n"
				+	"In this model, agents populate a grid with a given *density*. They are in two different states : happy when the percentage of same-color neighbours is above their *desired percentage of similarity*; unhappy otherwise. In the latter case, they change their location randomly until they find a neighbourhood that fits their desire. \n"
				+ 	"In addition to the previous parameter, one can adjust the *distance of perception* (i.e.  the distance at which they consider other agents as neighbours) of the agents to see how it affects the global process. ";
	}
	//Initialization of the model
	init {
		//Write the description of the model 
		do description;
		//Initialization of the places
		do initialize_places;
		//Computation of the number of people according to the density of people
		number_of_people <- int( length (all_places) * density_of_people);
		//Initialization of the people
		do initialize_people;
	}
	//Action to initialize places defined in the subclasses
	action initialize_places virtual: true;
	//Action to initialize people in the subclasses
	action initialize_people virtual: true;
}

//Species base representing the people agents
species base {
	rgb color;
	//List of all the neighbours agents
	list<base> my_neighbours;
	//computation of the similar neighbours
	int similar_nearby -> 
		(my_neighbours count (each.color = color))
	;
	//Computation of the total neighbours nearby
	int total_nearby -> 
		length (my_neighbours)
	;
	//Boolean to know if the agent is happy or not
	bool is_happy -> similar_nearby >= (percent_similar_wanted * total_nearby ) ;
}

experiment base_exp virtual:true{
	
	parameter "Color of group 1:" category: "User interface" var:color_1;
	parameter "Color of group 2:" category: "User interface" var:color_2;
	parameter "Color of group 3:" category: "User interface" var:color_3;
	parameter "Color of group 4:" category: "User interface" var:color_4;
	parameter "Color of group 5:" category: "User interface" var:color_5;
	parameter "Color of group 6:" category: "User interface" var:color_6;
	parameter "Color of group 7:" category: "User interface" var:color_7;
	parameter "Color of group 8:" category: "User interface" var:color_8;	
	
	parameter "Number of groups:" var:number_of_groups category: "Population";
	parameter "Density of people:" var:density_of_people category: "Population";
	parameter "Desired percentage of similarity:" var:percent_similar_wanted category: "Population";
	parameter "Width and height of the environment:" var:dimensions category: "Environment";
	parameter "Distance of perception:" var:neighbours_distance category: "Population";

}




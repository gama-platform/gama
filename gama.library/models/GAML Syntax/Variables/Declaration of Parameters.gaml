/**
 * Name: Parameters
 * A very simple models that showcases the scope of parameters, between simulation parameter and experiment parameters
 * Author: Alexis Drogoul
 * Tags: variables, attributes, parameters
 */

model Parameters


global {

	int attribute_3 <- 10000;
	int attribute_2 <- 1000;
}

experiment base_experiment {
	// This parameter definition in an experiment will be found in all the experiments that inherit from it
	parameter "Experiment attribute directly declared as parameter" var:attribute_2;	
}

experiment "Show Parameters" parent:base_experiment{

	int attribute_1 <- 100 on_change: {write attribute_1;};
	

	// This attribute sports the same name as one in simulation, and it cannot become a parameter unless the parameter: facet is declared directly on it
	int attribute_3 <- 100;

	// This direct definition of parameters allows to declare the seed of the experiment as a parameter (otherwise the seed of the simulation is used by default)
	float seed;
	parameter "The random seed of the experiment" var:seed <- 100.0;
	
	
	
	// The following parameter targets an experiment attribute
	parameter "Experiment attribute used by a parameter" var: attribute_1;
	// The following parameter targets the seed of the random number generation. The lookup for this attribute begins in the simulation (not in the experiment). 
	parameter "The random seed of the simulation" var: seed <- 0.0;
	// Like the seed, the following parameter targets attribute_3, and the one in the simulation will be used by default. 
	parameter "Simulation attribute used by a parameter" var: attribute_3;

	
}
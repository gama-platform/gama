/**
 * Name: Declaration of Parameters
 * Author: Alexis Drogoul
 * Description: A model showcasing the scope and visibility rules of parameters in GAML. Parameters are global
 *   variables that are exposed to the user interface for interactive modification before or during a simulation run.
 *   This model illustrates: declaring parameters directly in an experiment, inheriting parameters from a virtual
 *   (base) experiment, the difference between simulation-level attributes and experiment-level parameters, and
 *   how multiple experiments can share a common set of parameters through virtual experiment inheritance.
 * Tags: variables, attributes, parameters, experiment, scope, inheritance, virtual
 */

model Parameters


global {

	int attribute_3 <- 10000;
	int attribute_2 <- 1000;
	float my_duration <- 2#h;
	float my_distance <- 1500#m;
	float my_amount <- 2.5;
	float my_speed <- 10#m/#s;
	float my_share <- 0.3;
}

experiment base_experiment virtual:true {
	// This parameter definition in an experiment will be found in all the experiments that inherit from it
	parameter "Experiment attribute directly declared as parameter" var:attribute_2;	
}

experiment "Show Parameters" parent:base_experiment{

	float attribute_1 <- 100.0 on_change: {write attribute_1;};
	

	// This attribute sports the same name as one in simulation, and it cannot become a parameter unless the parameter: facet is declared directly on it
	int attribute_3 <- 100;

	// This direct definition of parameters allows to declare the seed of the experiment as a parameter (otherwise the seed of the simulation is used by default)
	float seed;
	parameter "The random seed of the experiment" var:seed <- 100.0 unit: "is the seed";
	
	
	
	// The following parameter targets an experiment attribute
	parameter "Int attribute used by a parameter" var: attribute_1 unit: "(attribute_1)";
	// The following parameter targets the seed of the random number generation. The lookup for this attribute begins in the simulation (not in the experiment). 
	parameter "The random seed of the simulation" var: seed <- 0.0;
	// Like the seed, the following parameter targets attribute_3, and the one in the simulation will be used by default. 
	parameter "Simulation attribute used by a parameter" var: attribute_3;

	// Without a unit facet this slider displays the stored value in seconds.
	parameter "Duration (slider)" var: my_duration min: 1#h max: 10#h step: 25#mn unit: #h; 
	// Text editors keep the original expression editable and show the conversion beside it.
	parameter "Distance (text)" var: my_distance unit: #km;
	// A string supplies a label without converting the value.
	parameter "Amount" var: my_amount min: 0.0 max: 10.0 step: 0.1 unit: "items";
	// Compound expressions work directly or inside a string, e.g. unit: "#km/#h".
	parameter "Speed" var: my_speed min: 0.0 max: 100.0 step: 0.25 unit: #km/#h;
	// #percent displays a fraction as a percentage (0.3 is shown as 30 %), whereas the string "%" is only a label.
	parameter "Share" var: my_share min: 0.0 max: 1.0 step: 0.05 unit: #percent;

}

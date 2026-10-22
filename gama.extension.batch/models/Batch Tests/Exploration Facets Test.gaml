/**
* Name: Exploration Facets Test
* Author: Gemini CLI
* Description: CI Tests for exploration statement facets (LHS, Morris, Sobol, etc.)
*/

model exploration_facets_test

global {
	float p1 <- 0.0;
	float p2 <- 0.0;
	float nb_preys <- 0.0;
	
	reflex simulate {
		// Dummy logic
	}
}

// 1. Test LHS Facets
experiment LHS_Facet_Test title: "LHS Facet Test" type: batch until: cycle >= 100 {
	parameter "p1" var: p1 min: 0.0 max: 1.0;
	parameter "p2" var: p2 min: 0.0 max: 1.0;
	
	// Testing new ESE-LHS facets
	method exploration sampling: "latinhypercube" 
	                   sample: 10 
	                   outer_iter: 20 
	                   inner_iter: 50;
}

// 2. Test Morris Facets
experiment Morris_Facet_Test title: "Morris Facet Test" type: batch until: cycle >= 100 {
	parameter "p1" var: p1 min: 0.0 max: 1.0;
	parameter "p2" var: p2 min: 0.0 max: 1.0;
	
	method morris levels: 4 sample: 4 outputs: ["nb_preys"] report: "morris_report.txt";

}

// 3. Test With Facet (Direct Data)
experiment With_Facet_Test title: "With Facet Test" type: batch until: cycle >= 100 {
	parameter "p1" var: p1;
	
	list<map<string, object>> my_plan <- list<map<string, object>>([["p1"::0.1], ["p1"::0.5], ["p1"::0.9]]);
	
	method exploration with: my_plan;
}

// 4. Test Default Sample Size
experiment Default_Sample_Test title: "Default Sample Test" type: batch until: cycle >= 100 {
	parameter "p1" var: p1 min: 0.0 max: 1.0;
	
	method exploration sampling: "latinhypercube" sample: 132;
}

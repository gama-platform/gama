/**
* Name: SpeciesOperatorsTest
* Author: Antigravity
* Description: Tests operations comparing agents by species and filtering populations
* Tags: species, agent, filter, test
*/

model SpeciesOperatorsTestModel

species parent_species { }
species child_species parent: parent_species { }
species other_species { }

experiment SpeciesOperatorsTest type: test autorun: true {
	
	test "Species Filtering and of_species" {
		create parent_species number: 2;
		create child_species number: 3;
		create other_species number: 1;
		
		// List of all agents
		list<agent> all_ags <- list(agents);

		// every created agent is reachable through the built-in 'agents'
		assert length(all_ags) >= 6;
		assert all_ags contains_all list(parent_species);
		assert all_ags contains_all list(child_species);
		assert all_ags contains_all list(other_species);
		
		// Filter by strict species
		list<agent> parents_only <- all_ags of_species parent_species;
		assert length(parents_only) = 2; // only the strict parent_species, not children
		
		// Filter by generic species (includes inherited)
		list<agent> parents_and_children <- all_ags of_generic_species parent_species;
		assert length(parents_and_children) = 5;
		
		// Direct population count
		assert length(child_species) = 3;
		assert length(other_species) = 1;
	}
	
	test "Agent Species Checking" {
		create child_species number: 1 returns: ags;
		agent ag <- ags[0];
		
		assert species_of(ag) = child_species;
		assert (ag is child_species) = true;
		assert (ag is parent_species) = true; // Polymorphism
		assert (ag is other_species) = false;
	}
}

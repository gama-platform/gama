/**
* Name: Serialize Operators - Save Simulation to File
* Author: Benoit Gaudou
* Description: Demonstrates saving the complete simulation state to a binary file using the 'serialize'
*   operator and the 'save' statement. The serialization captures all global variables, agent populations,
*   and their attributes at a given cycle. The resulting file can be loaded by 'Serialize Operators -
*   Restore simulation' or 'Create Simulation From File' to resume execution from the saved state.
* Tags: serialization, save_file, binary, snapshot, simulation, serialize, checkpoint
*/

model SavingSimulation

global {
	string s <- "test";
	
	init {
		create people number: 1;
	}
}

species people {
	int t;
	list<int> lo <- [1,2,3];
}

experiment SaveSimulation type: gui {
	
	reflex save_simulation when: cycle mod 2 = 0 {
		write "================ START SAVE + self " + " - " + cycle ;		
		save simulation to: '../result/file.simulation'  ;
		write "================ END SAVE + self " + " - " + cycle ;					
	}
	
	reflex serialize_agent when: cycle mod 2 = 1 {
		write "================ Serialize simulation " + self + " - " + cycle;
		write serialize(self.simulation);
		write "================ END Serialize simulation " + self + " - " + cycle;				
	}
	
}

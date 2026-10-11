/**
* Name: User Control Architecture
* Author: Patrick Taillandier
* Description: Demonstrates the 'user_controlled' control architecture, which gives users direct step-by-step
*   control over individual agent actions during a simulation run. A species using 'control: user_controlled'
*   pauses execution at each step and presents the user with a set of buttons corresponding to the agent's
*   declared actions. The user chooses which action to execute. This architecture is useful for debugging
*   agent behavior or for building turn-based interactive simulations where human decisions drive agent actions.
* Tags: gui, architecture, user_controlled, interaction, control, agent, turn_based
*/


model user_control

global {

	int nbAgent <- 10;
	bool advanced_user_control <- false;
	init {
		create cell number: nbAgent {
			color <-#green;
		}
		create user;
	}
}

species cell {
	rgb color;	
	aspect default {
		draw circle(1) color: color;
	}
}

species user control:user_only {
   user_panel "Default" initial: true {
      transition to: "Basic Control" when: every (10#cycles) and !advanced_user_control;
      transition to: "Advanced Control" when: every(10#cycles) and advanced_user_control;
   }
   
   user_panel "Basic Control" {
      user_command "Kill one cell" {
         ask (one_of(cell)){
            do die();
         }
      }
      user_command "Create one cell" {
        create cell { 
			color <-#green; 
		}
      } 
      transition to: "Default" when: true;                    
   }
   user_panel "Advanced Control" {
      user_command "Kill cells" color: #red continue: true{
        user_input "Number" returns: number type: int <- 10;
        ask (number among cell){
           do die();
        }
      }
      user_command "Create cells" color: #green {
        user_input "Number" returns: number type: int <- 10;
        create cell number: number{ 
			color <-#green; 
		}
      } 
      transition to: "Default" when: true;        
   }
}


experiment Displays type: gui {
	parameter "advanced user control" var: advanced_user_control <- false;
	output { 
		display map { 
			species cell;
		}
	}
}
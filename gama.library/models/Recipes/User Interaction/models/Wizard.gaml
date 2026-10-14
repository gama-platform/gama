/**
* Name: Wizard
* Author: Patrick Taillandier
* Description: Demonstrates the 'wizard' action for displaying a multi-step input wizard dialog to the user.
*   A wizard organizes multiple input pages sequentially, with Next/Back navigation and a Finish button.
*   Each page can contain 'enter' (free-text/number) and 'choose' (dropdown) input elements. The wizard
*   returns a map of all collected values when the user clicks Finish, or an empty map if cancelled. This
*   is the recommended approach for collecting a structured set of configuration parameters from the user
*   at model initialization or during key simulation events.
* Tags: gui, wizard, dialog, interaction, input, multi_step, popup
*/

model Wizard_example

global {
	init {
		map results <-  wizard("My wizard", eval_finish,
			[ 
			wizard_page("page1","enter info page 1" ,[enter("file" , file), choose("shape", string, "circle", ["circle", "square"])], font("Helvetica", 14 , #bold)),
			wizard_page("page2","enter info page 2" ,[enter("var2",string), enter("to consider", bool, true)], font("Arial", 10 , #bold))
			] 
		);
		write sample(results);  
	}
	
	bool eval_finish(map<string,map> input_map) {
		write input_map;
		
		 return input_map["page2"]["var2"] != nil;
	}
}

experiment Wizard_example type: gui ;
/**
* Name: User Input Dialog
* Author: Patrick Taillandier
* Description: Shows how to display an input dialog box to collect values from the user at runtime using
*   'user_input_dialog'. Two types of input elements are supported: 'enter' (free text/number entry) and
*   'choose' (dropdown selection from a list of options). The dialog returns a map of the entered values
*   keyed by their labels. This is useful for requesting parameters from the user mid-simulation, such as
*   confirming scenario parameters before a critical event.
* Tags: gui, dialog, user_input_dialog, interaction, input, parameter, popup
*/

model Confirmdialog_example

global {
	init {
		//2 types of elements can be added: enter (enter a value) and choose (choose a value among a list of possible values)
		map  result <- user_input_dialog("Main title",[enter("Enter a value", 0.0) , choose("Choose a value",string,"value 1", ["value 1","value 2"])]);
		
		write sample(result);
	}
}

experiment UserInputdialog_example type: gui ;
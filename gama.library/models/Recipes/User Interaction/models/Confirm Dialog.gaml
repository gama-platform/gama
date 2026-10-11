/**
* Name: Confirm Dialog
* Author: Patrick Taillandier
* Description: Shows how to display a confirmation dialog box to the user using the 'user_confirm' action.
*   The dialog presents a title, a message, and OK/Cancel buttons. The action returns 'true' if the user
*   clicks OK and 'false' if they cancel. This is useful for asking the user to confirm a potentially
*   destructive or irreversible action before it is executed in the simulation.
* Tags: gui, dialog, user_confirm, interaction, popup
*/

model Confirmdialog_example

global {
	init {
		bool result <- user_confirm("Confirmation dialog box","Do you want to confirm?");
		write sample(result);
	}
}

experiment Confirmdialog_example type: gui ;
/**
* Name: Conditional Aspect Selection
* Author: Baptiste Lesquoy
* Description: Demonstrates how to dynamically switch between named agent aspects based on a condition, such
*   as a parameter or the simulation state. In this model, a 'dark_mode' boolean parameter controls whether
*   agents are displayed using their 'light' or 'dark' aspect. Conditional aspect selection allows models to
*   provide multiple visualization modes that the user can switch between interactively, without restarting
*   the simulation — useful for accessibility, presentation, or highlighting different data layers.
* Tags: aspect, display, visualization, conditional, parameter, dark_mode, gui
*/


model Conditional_aspect_selection

global {
	
	bool dark_mode <- false;
	font my_font <- font("Helvetica", 16, #bold);
	init {
		create dummy number:10;
	}
	
}

species dummy {
	
	aspect light {
		draw circle(2) color:#red;
	}
	aspect dark {
		draw circle(2) color:#darkred;
	}
}


experiment test {
	parameter "Toggle dark mode" var:dark_mode;
	output{
		display main background:dark_mode?#black:#white{
			species dummy {
				if dark_mode {
					draw dark;
				}
				else{
					draw light;
				}	
			}
			
			graphics "Instructions"{
				draw "Toggle the dark mode parameter and run a simulation step" at:{5,5} color:dark_mode?#white:#black font:my_font;				
			}
		}
	}
}


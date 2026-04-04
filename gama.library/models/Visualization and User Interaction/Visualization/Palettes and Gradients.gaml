/**
* Name: Palettes and Gradients
* Author: Alexis Drogoul
* Description: Shows how to create and use color palettes and smooth color gradients in GAMA displays.
*   Color gradients are defined by a list of key colors and interpolated between them. Palettes can be
*   built from ColorBrewer sequences or custom lists. The model demonstrates: linear and multi-stop gradients,
*   applying gradients to agent aspects based on numeric values (e.g., elevation, density), and combining
*   gradients with transparency. This is the primary reference for advanced color encoding in GAMA.
* Tags: color, palette, gradient, visualization, display, brewer, interpolation
*/


model PalettesandGradients

global {
	
	grid_file volcano <- grid_file("includes/vulcano_50.asc");
	field cells <- field(volcano);
	geometry shape <- square(200);
	init {
		write max(cells);
		write min(cells);
	}
}


experiment Palettes type: gui {
	output synchronized: true {
		layout #split;
		display "Brewer" type: 3d {
			mesh cells  color:(brewer_colors("Set3")) triangulation: true smooth: true;
		}


		display "One Color" type: 3d  {
			mesh cells  color: #green triangulation: true border: #yellow smooth: true;
		}
		
		
		display "Scale" type: 3d  {
			mesh cells  color: scale([#red::1, #yellow::2, #green::3, #blue::6]) triangulation: true smooth: true;
		}
		

		display "Texture " type: 3d { 
			mesh cells texture: file("includes/Texture.jpg") triangulation: true border: #black smooth: true;
			
		}
		display "Simple gradient" type: 3d { 
			mesh cells color: palette([#darkgreen, #darkgreen, #green, #green, #sienna, #sienna, #white]) triangulation: true border: #black ;
			
		}
	}

}
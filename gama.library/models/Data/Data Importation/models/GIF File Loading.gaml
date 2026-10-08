/**
* Name: GIF File Loading
* Author: Alexis Drogoul
* Description: Shows how to load animated GIF files in GAMA and use them as textures for agents or display them
*   directly in a simulation view. Animated GIFs contain multiple frames, and GAMA can cycle through those frames
*   automatically when the file is used as an agent's image aspect. This is useful for adding animated icons or
*   visual effects to a simulation display. The model creates a population of fish agents that each use an animated
*   GIF as their visual representation.
* Tags: image, display, gif, animation, texture, load_file, file
*/

model AnimatedGIFLoading

global {
	init {
		create fish number: 100;
	}
}

	species fish skills:[moving] {
		reflex r {
			do wander(amplitude:2.0, speed: 0.1);
		}
		
		aspect default {
			draw gif_file("../images/fish3.gif") size: {10,10} rotate: heading-45 ;
		}
	}


experiment "Ripples and Fish" type: gui {
	
	output synchronized: true{
		display Ripples  type: 3d camera: #from_up_front
		{
			species fish position: {0,0,0.05};
			graphics world transparency: 0.4{ 
				draw cube(100) scaled_by {1,1,0.08} texture:("../images/water2.gif") ;
			}
		}
	}
}


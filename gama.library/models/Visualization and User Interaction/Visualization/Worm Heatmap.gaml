/**
* Name: Worm Heatmap
* Author: Alexis Drogoul
* Description: A minimal demonstration of how to build a movement-density heatmap using a GAMA field. A 'worm'
*   agent moves randomly each step and updates the underlying field by incrementing the value of its current
*   cell. The field is rendered as a color gradient — low-traffic cells appear cool (blue) while frequently
*   visited cells appear warm (red). Over time the heatmap reveals the spatial distribution of the random walk.
*   This is the simplest possible field-based heatmap example in GAMA.
* Tags: heatmap, field, visualization, random_walk, display, color, gradient
*/


model WormHeatmap

global {

	int size <- 1000;
	field heatmap <- field(500,500);
	
	init {
		create worm number: size;
	}
}

species worm skills: [moving] {
	
	reflex wander {
		do wander amplitude: 5.0 speed: 0.01;
	}
	
	reflex mark {
			heatmap[location] <- (heatmap[location]) + 0.1;
	}
}
	


experiment "Show heatmap" type: gui {

	
	output {
		layout #split;
		
		display Heatmap type: 3d background: #black {
			// The display is 2D only, and defining a fixed palette makes the color slowly slide as the values increase
			mesh heatmap scale: 0 color: palette([#white, #orange, #red, #red]);
		}
		
		display Other type: 3d background: #black camera: #from_up_front{
			// Bumps appear due to the values increasing
			mesh heatmap scale: 0.1 color: brewer_colors("Reds") triangulation: true;
		}
	}
}
/**
* Name: Manual Snapshot
* Author: Alexis Drogoul
* Description: Shows how to programmatically capture a snapshot of a GAMA display and use it as an image
*   variable. The snapshot is taken using the 'snapshot' action on the display, returning an image that can
*   be saved to a file, copied to the system clipboard, or used as a texture for another display element.
*   Also demonstrates how to copy images to the clipboard for external use. Useful for automated screenshot
*   generation and for displaying a 'live preview' of one display inside another.
* Tags: image, snapshot, clipboard, display, save_file, visualization
*/
model ManualSnapshot

global {
	
	image background <- image(100,100, #white) ;

	init {
		create bug number: 100;
	}

}

species bug skills:[moving]{
	
	rgb color <- rnd_color(255);
	
	reflex {
		do wander (amplitude: 20.0 , speed: 0.1);
	}
	
	aspect default {
		draw circle(1) color: color;
	}
}

experiment "Snapshot Depth" type: gui {
	
	reflex when: (cycle > 1) and every(100 #cycle) {
		ask simulation {
			// We choose a neutral background
			background <- grayscale(brighter(snapshot("My Display")));
		}
	}
	
	
	output synchronized: true {
		display "My Display" type:3d axes: false{
			picture background refresh: true;
			species bug;
		}
	}

}

experiment "Save Snapshots" type: gui {
	
	int i <- 0;
	
	reflex when: (cycle > 1) and every(100 #cycle) {
		ask simulation {
			// We choose a neutral background
			save (snapshot("My Display")) to: "snapshots/snapshot" + myself.i + ".png";
		}
		i <- i + 1;
	}
	
	
	output synchronized: true {
		display "My Display" type:3d axes: false{
			species bug;
		}
	}

}

experiment "Copy Snapshot to Clipboard" type: gui {
	
	int i <- 0;
	
	init {
		write "press 'c' to copy the snapshot to your clipboard";
	}
	
	output synchronized: true {
		display "My Display" type:3d axes: false{
			species bug;
			event "c" {if (copy_to_clipboard(snapshot(simulation, "My Display"))) {write "Snapshot copied to the clipboard !" ;}}
		}
	}

}

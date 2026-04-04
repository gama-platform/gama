/**
* Name: SVG Manipulation
* Author: Alexis Drogoul
* Description: Shows how to work with SVG (Scalable Vector Graphics) files in GAMA. SVG files can be loaded
*   and their contents are returned as a list of geometries, one per SVG path element. These geometries can
*   be manipulated like any GAMA geometry (scaled, rotated, translated) and drawn in displays. The model
*   also shows how to convert an SVG geometry to a raster image using the 'image' operator. Useful for
*   importing complex vector artwork, maps, or diagrams as agent shapes.
* Tags: image, svg, geometry, vector, load_file, visualization, shape, aspect
*/

model SVGManipulation

global {
	
	
	svg_file geometries <- svg_file("../includes/geometries.svg"); // try different files, like city.svg, europe.svg, ant.svg... 
	geometry shape <- envelope(geometries);
	
	init {
		create shapes from: geometries;
		create images number: 30;
	}
	
	species shapes skills: [moving] {
		rgb color <- rnd_color(256);
		
		reflex {
			do wander speed: 0.01 amplitude: 10.0;
		}
		
		
		aspect default {
			draw shape color: color;
		}
	}
	
	
	species images {
		image im <- image(geometries, rnd(world.shape.width / 2)+1, rnd(world.shape.height / 2)+1);		
		aspect default {
			draw im size: {im.width, im.height};
		}
	}

}

experiment "Open me" type: gui {
	
	
	
	output {
	
		display "Loop on geometries" type: 3d {
			graphics g {
				loop gg over: geometries {
					draw gg border: #black width: 0.3;
				}
			}
		}
		
		display "Wireframe geometry" type: 3d {
			graphics g {
				draw geometry(geometries) wireframe: true border: #black width: 0.3;
			}
		}
		
		display "Geometries as agents" type: 3d {
			species shapes;
		}
		
		display "Image Full" type: 3d {
			picture image(geometries);
		}
		
		display "Images Small" type: 3d {
			species images;
		}
	
	
	}
}

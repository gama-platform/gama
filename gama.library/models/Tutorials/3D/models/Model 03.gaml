/**
* Name: 3D Tutorial - Model 03 - Moving Cells with Neighbors
* Author: Arnaud Grignard
* Description: Third step of the 3D tutorial. Extends the moving cells model with neighbor detection: each
*   cell changes color based on the number of neighboring cells within a given radius. Introduces the use of
*   3D lighting in GAMA displays (ambient, diffuse, and specular lights) and demonstrates how neighbor-based
*   interactions can produce emergent visual patterns in 3D.
* Tags: 3d, light, grid, neighbors, tutorial, interaction, color
*/

model Tuto3D

global {
	int nb_cells <- 100;
	int environment_size <- 100;
	geometry shape <- cube(environment_size);

	init {
		create cell number: nb_cells {
			location <- {rnd(environment_size), rnd(environment_size), rnd(environment_size)};
		}
	}
}

species cell skills: [moving3D] {
	rgb color;
	list<cell> neighbors;
	int offset;

	reflex move {
		do wander;
	}

	reflex compute_neighbors {
		neighbors <- cell select ((each distance_to self) < 10);
	}

	aspect default {
		draw sphere(environment_size * 0.01) color: #orange;
		loop pp over: neighbors {
			draw line([self.location, pp.location]);
		}
	}
}

experiment Tuto3D type: gui {
	parameter "Initial number of cells: " var: nb_cells min: 1 max: 1000 category: "Cells";
	output {
		display View1 type: 3d background: rgb(10, 40, 55) {
			camera 'default' location: {-89.9704,145.5689,125.2091} target: {117.2908,13.529,0.0};
			graphics "env" {
				draw cube(environment_size) color: #black wireframe: true;
			}
			species cell;
		}
	}
}

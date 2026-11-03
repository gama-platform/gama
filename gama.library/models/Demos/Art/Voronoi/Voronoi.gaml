/**
* Name: Voronoi
* Author: Gama Development Team
* Description: Moving seeds generate a clipped vector Voronoi diagram. Each cell is inset and smoothed
*   for display, forming a mosaic of translucent, gently undulating islands. The original polygons
*   remain available as an optional outline to distinguish the exact partition from its artistic rendering.
*   Pause seed motion to explore the geometry, or disable relief for a flat mosaic.
* Tags: voronoi, spatial, clustering, partition, proximity, geometry, smooth, 3d, transparency
*/
model voronoi

global {
	int num_points <- 24 min: 1 max: 250;
	int env_width <- 100 min: 10 max: 400;
	int env_height <- 100 min: 10 max: 400;
	string palette <- "Spectral" among: brewer_palettes(11);
	list<rgb> colors <- brewer_colors(palette, 11);
	bool moving_seeds <- true;
	bool relief <- true;
	bool show_seeds <- false;
	bool show_partition <- false;
	float transparency <- 0.18 min: 0.0 max: 0.8;
	float spacing <- 0.06 min: 0.0 max: 0.25;
	float smoothing <- 0.3 min: 0.0 max: 1.0;
	float elevation <- 8.0 min: 0.0 max: 20.0;
	rgb backdrop <- rgb(19, 28, 43);
	rgb outline <- rgb(202, 220, 232);
	geometry shape <- rectangle(env_width, env_height);

	init {
		create center number: num_points;
		do rebuild_cells;
	}

	reflex animate {
		if (moving_seeds) {
			ask center {
				do wander amplitude: 12.0;
			}
		}
		do rebuild_cells;
	}

	action rebuild_cells() {
		list<geometry> polygons <- voronoi(center collect (each.location), shape)
			collect (polygon(each.points));
		ask center {
			// Voronoi output order is not the seed order: match by containment.
			partition <- first(polygons where (each intersects location));
			if (partition != nil) {
				geometry inset <- partition scaled_by (1.0 - spacing);
				island <- smooth(inset, smoothing) intersection inset;
			}
		}
	}
}

species center skills: [moving] schedules: [] {
	rgb color <- colors[int(self) mod length(colors)];
	float speed <- 0.22;
	float phase <- rnd(360.0);
	geometry partition;
	geometry island;

	aspect glass {
		if (island != nil) {
			float altitude <- relief ? elevation * (0.65 + 0.35 * sin(cycle * 1.2 + phase)) : 0.0;
			float thickness <- relief ? 1.2 : 0.0;
			point anchor <- island.location;
			draw island at: {anchor.x, anchor.y, altitude} depth: thickness
				color: rgb(color, 1.0 - transparency) border: blend(color, #white, 0.65);
			if (show_seeds) {
				draw circle(0.45) at: {location.x, location.y, altitude + thickness + 0.1}
					color: #white;
			}
		}
		if (show_partition and partition != nil) {
			draw partition color: outline wireframe: true;
		}
	}
}

experiment voronoi type: gui autorun: true {
	parameter "Number of seeds" var: num_points category: "Mosaic" {
		if (num_points > length(center)) {
			create center number: num_points - length(center);
		} else if (num_points < length(center)) {
			ask (length(center) - num_points) among center {
				do die;
			}
		}
		do rebuild_cells;
	}
	parameter "Color palette" var: palette category: "Mosaic" {
		colors <- brewer_colors(palette, 11);
		ask center {
			color <- colors[int(self) mod length(colors)];
		}
	}
	parameter "Width" var: env_width category: "Mosaic";
	parameter "Height" var: env_height category: "Mosaic";
	parameter "Animate seeds" var: moving_seeds category: "Appearance";
	parameter "Cell spacing" var: spacing category: "Appearance";
	parameter "Smoothing (0 = rounded, 1 = tight)" var: smoothing category: "Appearance";
	parameter "Transparency" var: transparency category: "Appearance";
	parameter "Relief" var: relief category: "Appearance";
	parameter "Elevation" var: elevation category: "Appearance";
	parameter "Show seeds" var: show_seeds category: "Geometry" <- true;
	parameter "Show exact partition" var: show_partition category: "Geometry" <- true;

	float minimum_cycle_duration <- 0.05#s;
	output {
		layout #split navigator: false tray: false consoles: false editors: false;
		display "Voronoi - glass islands" background: backdrop type: 3d axes: false {
			camera 'default' location: {120.848,142.658,66.9886} target: {51.4698,55.4377,0.0};
			light #ambient intensity: 165;
			species center aspect: glass;
		}
	}
}

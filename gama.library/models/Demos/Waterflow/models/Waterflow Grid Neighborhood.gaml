/***
* Name: Waterflow Grid Neighborhood
* Author: Benoit Gaudou, Patrick Taillandier
* Description: A simplified water flow model where the river topology is represented by a grid whose flow
*   order is pre-computed from neighborhood relationships. The river cells are loaded from a raster image.
*   Before the simulation starts, each cell is assigned a flow order number by propagating downstream from
*   the source cells, ensuring water always moves from lower-order to higher-order cells. At each step,
*   cells pass water to their downstream neighbors according to this precomputed order. This model is
*   computationally efficient because the expensive topology computation happens only once at initialization.
* Tags: grid, gui, hydrology, water_flow, raster, neighborhood, topology, precomputation
***/

model Waterflowgridneighborhood

global {
	int image_size <- 20;
	file image_river_file <- image_file('../includes/river_image.png') ;
	
	list<cell> river;
	
	float entrance_water <- 255.0;
	
	
	init {
		ask cell {	
			color <- rgb( (image_river_file) at {grid_x,grid_y}) ;
			if(color = rgb(0,61,245)) {
				is_river_cell <- true;
				order <- image_size - 1 - grid_x;
				source <- order = 0;			
			}
		}
		river <- cell where(each.is_river_cell);
		
    }	
    
    
}

grid cell  width: image_size height: image_size schedules: reverse(river sort_by(each.order)){
	bool is_river_cell <- false;
	bool source <- false;
	int order <- -1;
	
	float water_volume;

	reflex water_flow {
		ask neighbors where(each.order > self.order) {
			water_volume <- water_volume + 0.9*myself.water_volume;
		}
		water_volume <- 0.1*water_volume;
	}
	
	reflex water_source when: source and every(20 #cycle) {
		water_volume <- water_volume + entrance_water;
	}
		
	
	aspect default {
		draw shape color: is_river_cell? rgb(0,0,2 * water_volume) : #lightgreen border: #grey;
	}
}

experiment Waterflowgridneighborhood type: gui {
	output synchronized: true{
		display flow type:2d{
			species cell;
		}		
	}
}


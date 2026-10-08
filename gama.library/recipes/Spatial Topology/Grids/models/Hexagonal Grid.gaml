/**
* Name: Hexagonal Grid
* Author: Patrick Taillandier
* Description: Demonstrates how to create a grid with hexagonal cell topology in GAMA. The model supports
*   both 'horizontal' (flat-top) and 'vertical' (pointy-top) hexagonal orientations, selectable as a
*   parameter. Each cell displays its grid coordinates. Hexagonal grids avoid the directional bias of square
*   grids (equal distance to all 6 neighbors) and are preferred for movement models, cellular automata, and
*   spatial analysis in many domains.
* Tags: grid, hexagon, topology, spatial, cellular_automaton, orientation
*/

model HexagonalGrid

global {
	string orientation <- "horizontal" among: ["horizontal", "vertical"];	
	
	init {
		ask cell {color <- #white;}
	}
	reflex show_neighborhood {
		ask cell {color <- #white;}
		ask one_of(cell) {
			color <- #red;
			ask neighbors {
				color <- #green;
			}
		}
	}
}

// the choices are 4,6 or 8 neighbors
grid cell height: 10 width: 10 neighbors: 6 horizontal_orientation: orientation = "horizontal";


experiment hexagonal type: gui{
	parameter var:orientation;
	float minimum_cycle_duration <- 0.5#s;
	output  synchronized:true{
		display view type: 2d{
			grid cell border: #black ;
		}
	}
}
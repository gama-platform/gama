/**
* Name: Weighted Shortest Path on Grid
* Author: Patrick Taillandier
* Description: Demonstrates shortest-path computation on a grid where cells have different traversal costs
*   (weights). Two algorithms are compared: A*, and Dijkstra (JPS and BF do not take into account weights). 
*   The agent path minimizes total cumulative cost rather than the number of steps. Cells with higher weights 
*   (e.g., difficult terrain, water, buildings) are more expensive to cross. This model is the weighted counterpart
*   to 'Shortest Path on Grid' and is the reference for realistic terrain-cost navigation in GAMA.
* Tags: grid, obstacle, shortest_path, weighted, cost, terrain, astar, dijkstra, pathfinding
*/

model Grid

global {
	
	/*2 alogirithms for the shortest path computation on a grid with weights:
	*      - A* : default algorithm: Very efficient for both Moore (8) and Von Neumann (4) neighborhoods. An introduction to A*: http://www.redblobgames.com/pathfinding/a-star/introduction.html
	*      - Dijkstra : Classic Dijkstra algorithm. An introduction to Dijkstra : http://www.redblobgames.com/pathfinding/a-star/introduction.html
	*/
	
	file dem <- file("../includes/vulcano_50.asc");
	geometry shape <- envelope(dem);
	string algorithm <- "Dijkstra" among: ["A*", "Dijkstra"];
	int neighborhood_type <- 8 among:[4,8];
	point source;
	point goal;
	path the_path;
	
	float height_factor <- 0.05; // used to reduced the height displayed
	
	init {  
		ask cell {grid_value <- grid_value * 5;}  
		float max_val <- cell max_of (each.grid_value);
		ask cell {
			float val <- 255 * (1 - grid_value / max_val);
			color <- rgb(val, val,val);
		}
		source <- (one_of (cell)).location;
		goal <- (one_of (cell)).location;

		using topology(cell) {
			the_path <- path_between((cell as_map (each::each.grid_value)), source, goal);	
		}
	} 
	
	reflex compute_path {
		source <- (one_of (cell)).location;
		goal <- (one_of (cell)).location;

		using topology(cell) {
			the_path <- path_between((cell as_map (each::each.grid_value)), source, goal);	
		}
	}
}

grid cell file: dem neighbors: neighborhood_type optimizer: algorithm;

	

experiment goto_grid type: gui {
	
	
	parameter var:algorithm;
	parameter var:neighborhood_type;
	
	float minimum_cycle_duration <- 300 #msec;
	
	output synchronized: true { // synchronized to make sure that we do not run into issue #3737
		display objects_display type: 3d antialias:false background:#lightgrey axes:false{ 
			camera 'default' location: {24.6963,59.7992,64.0919} target: {25.0,25.0,0.0};
			grid cell border: #black elevation:grid_value*height_factor triangulation:true;
			graphics "elements" {
	
				loop s over: the_path.segments {
					draw s color: #red at:{s.location.x, s.location.y, (cell overlapping s + cell(s.centroid).neighbors) max_of each.grid_value*height_factor + 0.05} ;
				}
				loop v over: the_path.vertices {
					draw triangle(0.5) color: #yellow border: #black at: {point(v).x, point(v).y, (cell(point(v)).neighbors) max_of each.grid_value*height_factor + 0.05};
				}
				draw circle(1) color: #green at: {source.x, source.y, (cell(source).neighbors max_of (each.neighbors max_of each.grid_value))*height_factor} border: #black;
				draw circle(1) color: #red at: {goal.x, goal.y, (cell(goal).neighbors max_of (each.neighbors max_of each.grid_value))*height_factor}  border: #black;
			}
		}
	}
}

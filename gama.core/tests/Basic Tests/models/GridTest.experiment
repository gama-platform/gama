/**
* Name: GridTest
* Author: Antigravity
* Description: Tests the grid topology and its specific cell properties
* Tags: grid, topology, test
*/

model GridTestModel

global {
	geometry shape <- square(100);
}

grid my_grid width: 10 height: 10 {
	float val <- rnd(1.0);
}

experiment GridTest type: test autorun: true {
	
	test "Grid Dimensions and Cell Access" {
		assert length(my_grid) = 100;
		
		// Access by index
		my_grid cell <- my_grid[5, 5];
		assert cell.grid_x = 5;
		assert cell.grid_y = 5;
		
		// Neighbors
		// grids default to a von Neumann (4) neighbourhood
		list<my_grid> neighs <- cell neighbors_at 1;
		assert length(neighs) = 4;

		// a cell on the border has fewer neighbours
		my_grid corner <- my_grid[0, 0];
		assert length(corner neighbors_at 1) = 2;
	}
	
	test "Grid Geometry Properties" {
		my_grid cell <- my_grid[0, 0];
		
		// Assuming world is 100x100 and grid is 10x10, cell size is 10x10
		assert cell.shape.area = 100.0;
		assert cell.location = {5.0, 5.0}; // Center of the first cell
	}
	
	test "Grid Casting and Iteration" {
		// grid cells are agents and can be collected
		list<float> values <- my_grid collect (each.val);
		assert length(values) = 100;
		
		// grid as a matrix
		matrix<my_grid> mat <- matrix(my_grid);
		assert mat.columns = 10;
		assert mat.rows = 10;
		assert mat[0, 0] = my_grid[0, 0];
	}
}

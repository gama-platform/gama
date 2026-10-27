/**
* Name: Grid Rendering
* Description: Visual regression for #1156. Open the experiment without running it, then pan,
*   zoom and rotate both displays. Every cell, including those along the left edge, must stay
*   filled and bordered. The environment frame is deliberately left visible to check depth
*   separation between it and this wide grid.
* Tags: grid, 3d, opengl, display
*/
model GridRendering

grid lane width: 200 height: 10 {
	rgb color <- #dodgerblue;
}

experiment "Grid Rendering" type: gui {
	output {
		layout #horizontal;
		display "OpenGL" type: opengl {
			grid lane border: #black;
		}
		display "OpenGL4" type: opengl4 {
			grid lane border: #black;
		}
	}
}

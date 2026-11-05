/**
* Name: Display Coordinates - Layers
* Description: The same populations appear in world and screen-positioned layers. Agent locations remain
*   in model units: screen placement is a frame choice, not a conversion of model data into pixels.
*   The second experiment combines a screen image, a flat mesh and a chart. Resize each inherited display.
* Tags: visualization, coordinates, species, agents, grid, image, mesh, chart
*/
model coordinate_layers

global {
    geometry shape <- square(100);
    field heights <- field(matrix([[0, 1, 2], [1, 2, 3], [2, 3, 4]]));

    init {
        create dot number: 12 {
            location <- {15 + (index mod 4) * 22, 20 + int(index / 4) * 25};
        }
    }
}

grid cell width: 10 height: 10 {
    rgb color <- (grid_x + grid_y) mod 2 = 0 ? #lightgray : #white;
}

species dot {
    aspect default {
        draw circle(3) color: #blue;
    }
}

experiment "1 - Spatial layers" type: gui {
    text "Purpose: draw the same grid and population in world layers and a fixed 200-by-200-pixel screen frame.<br/>Try: resize, zoom and pan each display; no simulation steps are needed.<br/>Expected: the background grid follows the camera, while the small copy stays at {16,100} pixels. Both copies keep the same relative dot locations: screen placement does not change agent data." category: "What this demonstrates";
    output {
        display "Java2D" type: java2D {
            grid cell coordinates: #world border: #gray;
            species dot coordinates: #world;
            // Both the grid and the dots are fitted to the same viewport-relative rectangle.
            grid cell coordinates: #screen position: {16, 100} size: {200, 200} border: #gray;
            agents "Screen dots" value: list(dot) coordinates: #screen position: {16, 100} size: {200, 200};
            graphics "Captions" coordinates: #screen {
                draw "World grid and dots behind; screen copy at {16,100}" at: {16, 26} color: #black;
                draw "Both copies use the same model-unit locations" at: {16, 56} color: #black;
                draw "Screen frame: 200 x 200 pixels" at: {16, 330} color: #black;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl {}
    }
}

experiment "2 - Image mesh and chart" type: gui {
    text "Purpose: apply screen placement to image, mesh and chart layers, not just graphics and overlays.<br/>Try: run to populate the chart, then resize and zoom each display.<br/>Expected: the image and flat mesh keep 200-by-140-pixel frames. The chart keeps a 180-pixel height but follows 90 percent of the viewport width. Camera movement should not move these frames." category: "What this demonstrates";
    output {
        display "Java2D" type: java2D {
            image "../images/Gama.jpg" coordinates: #screen position: {16, 60} size: {200, 140};
            mesh heights coordinates: #screen position: {240, 60} size: {200, 140}
                scale: 0 color: palette([#blue, #cyan, #yellow, #red]);
            chart "Cycle" type: series coordinates: #screen
                position: {16, 240} size: {0.9, 180} {
                data "Cycle" value: cycle;
            }
            graphics "Captions" coordinates: #screen {
                draw "Image: 200 x 140 pixels" at: {16, 32} color: #black;
                draw "Mesh: same frame size" at: {240, 32} color: #black;
                draw "Chart: 90 percent viewport width, 180 pixels high" at: {16, 224} color: #black;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl {}
    }
}

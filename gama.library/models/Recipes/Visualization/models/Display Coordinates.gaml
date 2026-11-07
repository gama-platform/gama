/**
* Name: Display Coordinates
* Description: The same moving blue marker is drawn in a 100-by-100 world and a fixed screen mini-map.
*   The upper-left HUD reports viewport dimensions and mouse pixels; the lower-left chart plots the marker's world X.
*   Resize both displays, then zoom and pan: only the main world moves, not the HUD, mini-map or chart frames.
*   Mini-map contents use world units and a uniform fit. Its unused white space is intentional, not clipping.
*   Open the companion "Display Coordinates - ..." models for focused, annotated comparisons.
* Tags: visualization, display, overlay, coordinates
*/
model display_coordinates

global {
    geometry shape <- square(100);
    point marker <- {50, 50} update: {50 + 30 * sin(cycle * 2), 50 + 20 * cos(cycle * 2)};
}

experiment "Display Coordinates" type: gui {
    text "Purpose: compare a camera-attached world with fixed screen-space information. The blue marker represents the same world location in the main view and the mini-map; the chart plots its X coordinate." category: "What this demonstrates";
    text "Try: run the simulation, resize each display, then zoom and pan independently.<br/>Expected: the main grey world follows the camera. The HUD, mini-map frame and chart stay fixed in the viewport. The mini-map preserves proportions, so unused white space is normal. Compare Java2D and OpenGL. For OpenGL4, open OpenGL Comparison - Coordinates." category: "What this demonstrates";
    output {
        display "Java2D" type: java2D {
            graphics "World" coordinates: #world {
                draw world.shape color: #lightgray border: #gray;
                draw line([{0, 50}, {100, 50}]) color: #white;
                draw line([{50, 0}, {50, 100}]) color: #white;
                draw circle(5) at: marker color: #blue;
                draw "WORLD: 100 x 100 model units" at: {50, 8}
                    anchor: #center font: font("Helvetica", 14) color: #black;
            }
            graphics "HUD" coordinates: #screen position: {16, 16} size: {0.52, 108}
                background: #white border: #gray {
                draw "Screen-space HUD" at: {12, 24} anchor: #left_center
                    font: font("Helvetica", 18) color: #black;
                draw ("Viewport (px): " + #display_width + " x " + #display_height)
                    at: {12, 52} anchor: #left_center color: #black;
                draw ("Mouse (pixels): " + #mouse_location_in_display)
                    at: {12, 74} anchor: #left_center color: #black;
                draw "Zoom/pan: only the main world should move"
                    at: {12, 96} anchor: #left_center color: #black;
            }
            overlay coordinates: #screen referential: #world
                position: {-0.26, 16} size: {0.24, 0.24} background: #white border: #gray {
                draw world.shape color: #lightgray border: #gray;
                draw circle(5) at: marker color: #blue;
                draw "World mini-map" at: {50, 10} anchor: #center color: #black;
            }
            chart "World marker X (0-100)" type: series coordinates: #screen
                position: {16, -0.30} size: {0.48, 0.28} x_range: 200 y_range: {0, 100} {
                data "X" value: marker.x color: #blue;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl {}
    }
}

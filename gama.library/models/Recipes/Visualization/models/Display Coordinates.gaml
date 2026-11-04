/**
* Name: Display Coordinates
* Description: Explicit world and screen placement, local HUD drawings and a world-content mini-map.
* Tags: visualization, display, overlay, coordinates
*/
model display_coordinates

global {
    geometry shape <- square(100);
}

experiment "Display Coordinates" type: gui {
    output {
        display "Java2D" type: java2D {
            graphics "World" coordinates: #world {
                draw square(100) at: {50, 50} color: #lightgray border: #gray;
                draw circle(10) at: {50, 50} color: #blue;
            }
            graphics "HUD" coordinates: #screen position: {16, 16} size: {0.4, 88}
                background: #white border: #gray {
                draw "Screen-space HUD" at: {12, 24} anchor: #left_center
                    font: font("Helvetica", 18) color: #black;
                draw ("Viewport: " + #display_width + " x " + #display_height)
                    at: {12, 58} anchor: #left_center color: #black;
                draw ("Mouse (pixels): " + #mouse_screen_location)
                    at: {12, 78} anchor: #left_center color: #black;
            }
            overlay coordinates: #screen content_coordinates: #world
                position: {0.74, 16} size: {0.24, 0.24} background: #white border: #gray {
                draw square(100) at: {50, 50} color: #lightgray border: #gray;
                draw circle(10) at: {50, 50} color: #blue;
            }
            chart "Cycle" type: series coordinates: #screen
                position: {16, -0.28} size: {0.42, 0.24} {
                data "Cycle" value: cycle;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl {}
    }
}

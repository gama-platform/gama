/**
* Name: Display Coordinates - Mouse
* Description: Compare live viewport, world and overlay-local pointer values in two independent displays.
*   Run, then move or click inside any display. A left press records both coordinates from the same event
*   and leaves a world marker. Zoom or pan to separate the world location from the screen location.
*   The live overlay-local value subtracts the fixed {16,16} overlay origin; it is not a new mouse unit.
* Tags: visualization, coordinates, mouse, events, overlay, inheritance
*/
model coordinate_mouse

global {
    geometry shape <- square(100);
    point clicked_world <- {50, 50};
    point clicked_screen <- {0, 0};
    int clicks <- 0;
}

experiment "Mouse in three referentials" type: gui {
    text "Purpose: compare viewport pixels, world coordinates and coordinates local to a screen overlay.<br/>Try: run, move the pointer and left-click inside any display, then zoom and pan.<br/>Expected: each click records world and screen positions from the same event and places a red world marker. The marker follows the world; the saved screen position remains the recorded pixel value." category: "What this demonstrates";
    text "The live overlay-local value simply subtracts its {16,16}-pixel origin from #mouse_location_in_display. It is not a new mouse unit. Each display supplies its own live pointer values; the last-click values and click count are shared simulation state." category: "What this demonstrates";
    output {
        display "Java2D" type: java2D {
            graphics "World" coordinates: #world {
                draw world.shape color: #lightgray border: #gray;
                draw circle(2) at: clicked_world color: #red;
            }
            event #mouse_listener {
                if #mouse_action = #mouse_down and #mouse_button = "left" {
                        clicked_world <- #mouse_location_in_world;
                        clicked_screen <- #mouse_location_in_display;
                        clicks <- clicks + 1;
                }
            }
            overlay coordinates: #screen position: {16, 16} size: {0.92, 250}
                background: #white border: #gray {
                draw ("Live viewport: " + #mouse_location_in_display) at: {12, 26} color: #blue;
                draw ("Live world: " + #mouse_location_in_world) at: {12, 56} color: #green;
                draw ("Local to this overlay: " + (#mouse_location_in_display - {16, 16}))
                    at: {12, 86} color: #purple;
                draw ("Last click world: " + clicked_world) at: {12, 126} color: #red;
                draw ("Same click viewport: " + clicked_screen) at: {12, 156} color: #red;
                draw ("Shared click count: " + clicks) at: {12, 186} color: #black;
                draw "The aliases keep their meanings in every referential"
                    at: {12, 222} color: #black;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl map_order: true {}
    }
}

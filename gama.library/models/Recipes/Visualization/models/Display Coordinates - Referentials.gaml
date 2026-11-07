/**
* Name: Display Coordinates - Referentials
* Description: Placement and drawing referential are independent. In a 200-by-100 world, identical
*   circles are drawn in screen pixels, stretched world units and uniformly fitted overlay world units.
*   The two lower frames have identical 240-by-160-pixel sizes. Their circle may look different:
*   graphics scales X and Y independently, whereas the overlay fits using one scale.
*   Red markers use 6#px; in the stretched frame only the shorter axis has radius 6 pixels.
*   Run to animate the world marker, then resize and zoom each display independently.
*   The second experiment demonstrates a world-positioned overlay following the world camera.
* Tags: visualization, display, referential, coordinates, minimap, scaling
*/
model coordinate_referentials

global {
    geometry shape <- rectangle(200, 100);
    point moving_point <- {100, 50};

    reflex move {
        moving_point <- {100 + 60 * cos(cycle * 3), 50 + 30 * sin(cycle * 3)};
    }
}

experiment "1 - Placement versus referential" type: gui {
    text "Purpose: separate where a layer is placed (coordinates:) from how its contents are interpreted (referential:). The world is 200 by 100 units; the two lower frames are both 240 by 160 pixels.<br/>Expected: the green graphics frame stretches world contents along X and Y, making the white circle elliptical. The purple overlay preserves the circle and leaves 40 pixels of unused space below." category: "What this demonstrates";
    text "Try: run to animate the blue marker, then resize, zoom and pan each display. Both lower frames stay fixed.<br/>The upper panel uses pixel contents. Red 6#px markers illustrate that one scalar pixel conversion cannot cancel unequal X/Y scales: only the uniform overlay keeps both radii at six pixels." category: "What this demonstrates";
    output {
        display "Java2D" type: java2D {
            graphics "World" coordinates: #world {
                draw world.shape color: #lightgray border: #gray;
                draw circle(10) at: moving_point color: #blue;
            }

            graphics "Screen contents" coordinates: #screen referential: #screen
                position: {16, 16} size: {500, 94} background: #white border: #gray {
                draw circle(10) at: {24, 28} color: #blue;
                draw "circle(10): 10 pixels, not 10 world units" at: {44, 34} color: #black;
                draw "at: {0.5,0.5} uses this panel's center" at: {12, 78} color: #black;
                draw circle(5) at: {0.5, 0.5} color: #red;
            }
            graphics "Stretched world contents" coordinates: #screen referential: #world
                position: {16, 140} size: {240, 160} background: #white border: #green {
                draw world.shape color: #lightgray border: #green;
                draw circle(20) at: {100, 50} color: #white border: #black;
                draw circle(10) at: moving_point color: #blue;
                draw circle(6#px) at: {20, 20} color: #red;
                draw "6px marker" at: {35, 20} color: #black;
            }
            overlay coordinates: #screen referential: #world
                position: {280, 140} size: {240, 160} background: #white border: #purple {
                draw world.shape color: #lightgray border: #purple;
                draw circle(20) at: {100, 50} color: #white border: #black;
                draw circle(10) at: moving_point color: #blue;
                draw circle(6#px) at: {20, 20} color: #red;
                draw "6px marker" at: {35, 20} color: #black;
            }
            graphics "Captions" coordinates: #screen {
                draw "graphics: independent XY scales" at: {16, 328} color: #green;
                draw "overlay: proportional fit, unused space below" at: {16, 354} color: #purple;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl map_order: true {}
    }
}

experiment "2 - A spatial overlay" type: gui {
    text "Purpose: demonstrate that an overlay can explicitly use world placement instead of its usual screen default.<br/>Try: zoom and pan each display; no simulation steps are needed.<br/>Expected: the purple overlay follows the world camera. Its frame is 100 by 50 model units, not pixels. Only the explanatory caption remains fixed on screen." category: "What this demonstrates";
    output {
        display "Java2D" type: java2D {
            graphics "World" coordinates: #world {
                draw world.shape color: #lightgray border: #gray;
            }
            // Both the frame and the drawings use world units; this is not a fixed HUD.
            overlay coordinates: #world position: {40, 20} size: {100, 50}
                background: #white border: #purple {
                draw world.shape color: #lightgray border: #purple;
                draw circle(20) at: {100, 50} color: #blue;
            }
            graphics "Fixed caption" coordinates: #screen {
                draw "Pan and zoom: the purple overlay follows the world camera"
                    at: {16, 26} color: #black;
                draw "Its frame is 100 x 50 world units, not pixels"
                    at: {16, 54} color: #purple;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl map_order: true {}
    }
}

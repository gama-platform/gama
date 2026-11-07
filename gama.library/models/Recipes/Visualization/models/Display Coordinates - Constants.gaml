/**
* Name: Display Coordinates - Constants
* Description: Inspect #px and display dimensions in screen and world drawing contexts. Zoom the world,
*   then resize one inherited display: screen dimensions describe that display's viewport, not its sibling.
*   A global number computed with #px is already evaluated; it does not carry its original unit expression.
* Tags: visualization, coordinates, constants, pixels, zoom, inheritance
*/
model coordinate_constants

global {
    geometry shape <- square(100);
    float stored_pixel_value <- 20#px;
}

experiment "Contextual constants" type: gui {
    text "Purpose: show that #px depends on the drawing context, but display dimensions always describe the viewport.<br/>Try: zoom the world and resize both displays independently.<br/>Expected: screen #px stays 1; world #px uses the world-to-pixel conversion. Both dimension readings match their display's viewport and do not change with zoom. The red circle keeps its pixel radius." category: "What this demonstrates";
    text "The displayed global 20#px value was evaluated before drawing. It is an ordinary stored number: moving it into a screen overlay does not reevaluate its original expression or preserve unit metadata." category: "What this demonstrates";
    output {
        display "Java2D" type: java2D {
            graphics "World readings" coordinates: #world {
                draw world.shape color: #lightgray border: #gray;
                draw ("World #px = " + #px) at: {4, 55} color: #blue;
                draw ("Viewport from world context = " + #display_width + " x " + #display_height)
                    at: {4, 65} color: #blue;
                draw circle(10#px) at: {50, 80} color: #blue;
            }
            graphics "Viewport" coordinates: #screen
                size: {#display_width, #display_height} border: #red {
                draw "Explicit full viewport: dimensions are independent of referential"
                    at: {16, -16} color: #red;
            }
            overlay coordinates: #screen position: {16, 16} size: {0.9, 148}
                background: #white border: #black {
                draw ("Screen #px = " + #px) at: {12, 26} color: #black;
                draw ("Viewport = " + #display_width + " x " + #display_height)
                    at: {12, 54} color: #black;
                draw ("Global 20#px, stored before drawing = " + stored_pixel_value)
                    at: {12, 82} color: #black;
                draw "Stored values are not reevaluated in a new referential"
                    at: {12, 110} color: #black;
                draw circle(10#px) at: {0.9, 120} color: #red;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl map_order: true {}
    }
}

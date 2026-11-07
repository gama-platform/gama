/**
* Name: Display Coordinates - Units
* Description: Screen values strictly below 1 are fractions, whereas exactly 1 (int or float) is a pixel.
*   Colored dots mark local x positions 0.5, 1, 1.0 and 0.5#px inside the same 400-pixel-wide panel.
*   The red and orange dots align at x=1; the blue and purple dots align at x=200.
*   The lower orange panel intentionally has a one-pixel frame and overflowing contents.
* Tags: visualization, coordinates, pixels, units, fractions, overlay, clipping
*/
model coordinate_units

experiment "Screen units and boundaries" type: gui {
    text "Purpose: expose the strict screen-coordinate threshold in a 400-pixel-wide panel.<br/>Expected: the blue dot at x=0.5 and purple dot at x=0.5#px both have local x=200 pixels. The red dot at x=1 and orange dot at x=1.0 both have local x=1 pixel. #px is numerically 1 here; it does not carry unit metadata." category: "What this demonstrates";
    text "Try: resize the displays; no simulation steps are needed. The reference panel stays 400 pixels wide.<br/>Intrinsic circle radii are pixels even below 1; rectangular draw sizes use the threshold. The lower orange layer has a one-pixel frame, but its circle and caption deliberately overflow because layers do not implicitly clip their contents." category: "What this demonstrates";
    output {
        display "Java2D" type: java2D {
            // An omitted size is full viewport; an explicit size {1,1} is not.
            graphics "Full viewport" coordinates: #screen border: #gray {
                draw "Full frame: size omitted" at: {16, -16} color: #black;
            }
            graphics "400-pixel reference" coordinates: #screen position: {24, 24} size: {400, 310}
                background: #white border: #blue {
                draw "Local x values in a 400-pixel panel" at: {12, 24} color: #black;
                draw circle(4) at: {0.5, 58} color: #blue;
                draw "0.5 = 200 pixels" at: {214, 62} color: #blue;
                draw circle(4) at: {1, 100} color: #red;
                draw "1 = 1 pixel" at: {16, 104} color: #red;
                draw circle(4) at: {1.0, 142} color: #orange;
                draw "1.0 = 1 pixel too" at: {16, 146} color: #orange;
                // #px is numerically 1; the multiplication leaves no unit metadata.
                draw circle(4) at: {0.5#px, 184} color: #purple;
                draw "0.5#px = 0.5 = 200 pixels" at: {12, 222} color: #purple;
                // Unlike at:, intrinsic geometry lengths are pixels, even below 1.
                draw circle(0.5) at: {380, 100} color: #black;
                draw square(1) size: {0.2, 16} at: {0.5, 250} color: #green;
                draw "size: {0.2,16} = 80 x 16 pixels" at: {12, 290} color: #green;
            }
            graphics "One-pixel frame" coordinates: #screen position: {24, 366} size: {1, 1}
                background: #orange border: #orange {
                draw circle(8) at: {20, 20} color: #orange;
                draw "size: {1,1}; drawings are NOT clipped" at: {40, 26} color: #black;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl map_order: true {}
    }
}

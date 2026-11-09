/**
* Name: Display Coordinates - Text
* Description: A font stored globally is reused unchanged in world and screen drawings. Nine anchor values
*   align text around red dots in a fixed pixel panel. The font size 16 is nominal, not a viewport fraction
*   or a model-unit length. Rotate/pan/zoom the world, then resize and compare Java2D and OpenGL.
*   Font metrics and perspective text can differ between backends; anchors do not move the layer frame.
*   OpenGL Comparison - Rendering contains the dedicated OpenGL/OpenGL4 text parity experiment.
* Tags: visualization, coordinates, font, text, anchor, perspective
*/
model coordinate_text

global {
    geometry shape <- square(100);
    font shared_font <- font("SansSerif", 16);
    list<string> names <- ["top_left", "top_center", "top_right", "left_center", "center",
        "right_center", "bottom_left", "bottom_center", "bottom_right"];
    list<point> anchors <- [#top_left, #top_center, #top_right, #left_center, #center,
        #right_center, #bottom_left, #bottom_center, #bottom_right];
}

experiment "Font and anchor semantics" type: gui {
    text "Purpose: reuse one nominal 16-point font in world and screen drawings, and compare the nine text anchors.<br/>Expected: each label aligns its named edge or center with its red dot. The lower blue label is centered in its panel using at: {0.5,0.5}; its font size does not become a fraction or a model-unit length." category: "What this demonstrates";
    text "Try: resize, zoom, pan and compare Java2D and OpenGL; no simulation steps are needed. The screen panels stay fixed in pixels, while the world label follows the world camera. Use a viewport large enough for the fixed 570-pixel-wide panels. Font metrics can differ between backends; anchors position text, not the layer frame." category: "What this demonstrates";
    output {
        display "Java2D" type: java2D {
            graphics "World text" coordinates: #world {
                draw world.shape color: #lightgray border: #gray;
                draw "Shared nominal font: 16" at: {50, 80} anchor: #center
                    font: shared_font color: #blue;
            }
            graphics "Anchor panel" coordinates: #screen position: {16, 16} size: {570, 290}
                background: #white border: #black {
                draw "All nine labels use the same font object" at: {12, 26}
                    font: shared_font color: #black;
                loop i from: 0 to: 8 {
                    point dot <- {100 + (i mod 3) * 185, 90 + int(i / 3) * 72};
                    draw circle(3) at: dot color: #red;
                    draw names[i] at: dot anchor: anchors[i] font: shared_font color: #black;
                }
            }
            graphics "Relative text location" coordinates: #screen position: {16, -110}
                size: {570, 94} background: #white border: #blue {
                draw circle(3) at: {0.5, 0.5} color: #red;
                draw "at: {0.5,0.5}, anchor: #center" at: {0.5, 0.5}
                    anchor: #center font: shared_font color: #blue;
                draw "Relative position; font size is still nominal 16" at: {12, 82}
                    font: shared_font color: #black;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl map_order: true {}
    }
}

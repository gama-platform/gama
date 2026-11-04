/**
* Name: Display Coordinates - Text
* Description: A font stored globally is reused unchanged in world and screen drawings. Nine anchor values
*   align text around red dots in a fixed pixel panel. The font size 16 is nominal, not a viewport fraction
*   or a model-unit length. Rotate/pan/zoom the world, then resize and compare the three renderers.
*   Font metrics and perspective text can differ between backends; anchors do not move the layer frame.
*   The parity experiment compares multiline spacing, italic bearings, borders, wireframes and extrusion.
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

experiment "Text rendering parity" type: gui {
    text "Purpose: compare multiline spacing, italic anchors, glyph holes, borders, wireframes and extrusion across Java2D, OpenGL and OpenGL4.<br/>Try: compare the three panels, then zoom, pan and rotate the world; no simulation steps are needed. Use a viewport large enough for the fixed 650-by-395-pixel panel." category: "What this demonstrates";
    text "Expected: the first block preserves an empty line; CRLF creates two lines. Each italic label is anchored at its red dot, including when rotated. O and B keep their holes and separated letters stay separate. Java2D intentionally ignores extrusion depth; the OpenGL renderers should close each letter's front/back caps. Font substitution can change exact glyph shapes." category: "What this demonstrates";
    output {
        display "Java2D parity" type: java2D {
            graphics "World outlines" coordinates: #world {
                draw world.shape color: #lightgray border: #gray;
                draw "O B i j" at: {50, 80} anchor: #center font: font("Serif", 38, #italic)
                    color: #blue border: #black depth: 2;
                draw "World extrusion (flat in Java2D)" at: {50, 92} anchor: #center
                    font: shared_font color: #black;
            }
            graphics "Text parity panel" coordinates: #screen position: {16, 16} size: {650, 395}
                background: #white border: #black {
                draw "Multiline, bearings and outlines" at: {16, 24}
                    font: shared_font color: #black;
                draw "First line\n\nThird line: one empty line above" at: {16, 55}
                    anchor: #top_left font: shared_font color: #blue;
                draw "CRLF line one\r\nCRLF line two" at: {360, 55}
                    anchor: #top_left font: shared_font color: #blue;
                draw "Italic visual bounds: each red dot is a bottom-left anchor" at: {16, 154}
                    font: shared_font color: #black;
                loop i from: 0 to: 2 {
                    point dot <- {30 + 210 * i, 205};
                    draw circle(3) at: dot color: #red;
                    draw "jiffy (gyp)" at: dot anchor: #bottom_left font: font("Serif", 28, #italic)
                        color: #blue rotate: 15 * i;
                }
                draw "Separated glyphs, holes, border and wireframe" at: {16, 255}
                    font: shared_font color: #black;
                draw "O B i j" at: {30, 310} font: font("Serif", 34)
                    color: #blue border: #black;
                draw "O B i j" at: {245, 310} font: font("Serif", 34)
                    color: #blue wireframe: true;
                draw "O B i j" at: {460, 310} font: font("Serif", 34)
                    color: #blue border: #black depth: 3;
                draw "Java2D ignores depth; OpenGL renderers should close the caps around every letter."
                    at: {16, 370} font: font("SansSerif", 12) color: #black;
            }
        }
        display "OpenGL parity" parent: "Java2D parity" type: opengl {}
        display "OpenGL4 parity" parent: "Java2D parity" type: opengl4 {}
    }
}

experiment "Font and anchor semantics" type: gui {
    text "Purpose: reuse one nominal 16-point font in world and screen drawings, and compare the nine text anchors.<br/>Expected: each label aligns its named edge or center with its red dot. The lower blue label is centered in its panel using at: {0.5,0.5}; its font size does not become a fraction or a model-unit length." category: "What this demonstrates";
    text "Try: resize, zoom, pan and compare the three renderers; no simulation steps are needed. The screen panels stay fixed in pixels, while the world label follows the world camera. Use a viewport large enough for the fixed 570-pixel-wide panels. Font metrics can differ between backends; anchors position text, not the layer frame." category: "What this demonstrates";
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
        display "OpenGL" parent: "Java2D" type: opengl {}
        display "OpenGL4" parent: "Java2D" type: opengl4 {}
    }
}

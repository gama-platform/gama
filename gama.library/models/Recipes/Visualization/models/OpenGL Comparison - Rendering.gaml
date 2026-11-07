/**
* Name: OpenGL Comparison - Rendering
* Description: Paired OpenGL/OpenGL4 scenes for text, geometry, transparency, lighting, textures,
*   grids, agents, meshes and charts. Each OpenGL4 display inherits the complete OpenGL scene.
*   Use equally sized viewports and matching cameras; these are visual comparisons, not benchmarks.
*   See OpenGL Comparison - Coordinates for coordinate semantics and OpenGL Comparison - Ants
*   for the imported Ant Foraging scenes.
* Tags: visualization, opengl, opengl4, comparison, text, texture, grid, mesh, chart, lighting
*/
model opengl_rendering_comparison

global {
    geometry shape <- square(100);
    font shared_font <- font("SansSerif", 16);
    field heights <- field(matrix([[0, 1, 2, 1, 0], [1, 4, 6, 4, 1],
        [2, 6, 10, 6, 2], [1, 4, 6, 4, 1], [0, 1, 2, 1, 0]]));
    float signal <- 50.0 update: 50 + 30 * sin(cycle * 3);

    init {
        create marker number: 12;
    }
}

grid lane width: 200 height: 10 {
    rgb color <- #dodgerblue;
}

species marker {
    point location <- {15 + (index mod 4) * 22, 20 + int(index / 4) * 25, 2};

    reflex move {
        location <- {15 + (index mod 4) * 22 + 4 * sin(cycle * 3),
            20 + int(index / 4) * 25, 2};
    }

    aspect default {
        draw sphere(2) color: #orange border: #black;
    }
}

experiment "1 - Text and outlines" type: gui {
    text "Purpose: compare multiline spacing, italic anchors, glyph holes, borders, wireframes and extruded glyph faces.<br/>Try: compare equal-sized views, then resize and zoom; no simulation steps are needed. Give each viewport at least 690 by 450 logical pixels to see the fixed panel. Experiment 7 isolates world extrusion and billboards without a covering panel." category: "What to compare";
    text "Expected: LF preserves the empty line and CRLF creates two lines. Red dots mark the italic labels' bottom-left anchors. O and B keep their holes and separated letters stay separate.<br/>Known differences: plain OpenGL4 text uses JOGL font resolution, which can substitute a different typeface than AWT. Font shapes, exact widths and antialiasing are not promised to be pixel-identical." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            graphics "Text panel" coordinates: #screen position: {16, 16} size: {650, 395}
                background: #white border: #black {
                draw "Multiline, bearings and outlines" at: {16, 24} font: shared_font color: #black;
                draw "First line\n\nThird line: one empty line above" at: {16, 55}
                    anchor: #top_left font: shared_font color: #blue;
                draw "CRLF line one\r\nCRLF line two" at: {360, 55}
                    anchor: #top_left font: shared_font color: #blue;
                draw "Italic visual bounds: red dots are bottom-left anchors" at: {16, 154}
                    font: shared_font color: #black;
                loop i from: 0 to: 2 {
                    point dot <- {30 + 210 * i, 205};
                    draw circle(3) at: dot color: #red;
                    draw "jiffy (gyp)" at: dot anchor: #bottom_left font: font("Serif", 28, #italic)
                        color: #blue rotate: 15 * i;
                }
                draw "Separated glyphs, holes, border and wireframe" at: {16, 255}
                    font: shared_font color: #black;
                draw "O B i j" at: {30, 310} font: font("Serif", 34) color: #blue border: #black;
                draw "O B i j" at: {245, 310} font: font("Serif", 34) color: #blue wireframe: true;
                draw "O B i j" at: {460, 310} font: font("Serif", 34) color: #blue border: #black depth: 3;
                draw "Fixed screen panel; experiment 7 isolates world extrusion and billboards."
                    at: {16, 370} font: font("SansSerif", 12) color: #black;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "2 - Geometry and transparency" type: gui {
    text "Purpose: compare filled, holed, concave and extruded geometry, wireframes, depth ordering and alpha blending.<br/>Try: inspect without running, then rotate and zoom both cameras to similar viewpoints. Run to rotate the blue cube." category: "What to compare";
    text "Expected: the ring has a clear hole; the L-shaped polygon is not filled across its missing corner. Front and back surfaces should remain closed when rotated. The translucent orange square reveals the blue square beneath it. Transparent surfaces are order-dependent; this experiment does not claim order-independent transparency." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            graphics "Shapes" {
                draw square(100) at: {50, 50, -1} color: #white border: #gray;
                draw (square(20) - square(10)) at: {22, 25} color: #blue border: #black depth: 4;
                draw polygon([{0, 0}, {20, 0}, {20, 8}, {8, 8}, {8, 20}, {0, 20}])
                    at: {55, 25} color: #green border: #black depth: 4;
                draw cube(16) at: {82, 25, 8} color: #blue rotate: cycle::{1, 1, 1};
                draw square(24) at: {24, 65, 1} color: #blue;
                draw square(24) at: {32, 70, 2} color: rgb(255, 140, 0, 128);
                draw sphere(10) at: {62, 70, 10} color: #orange border: #black;
                draw cube(18) at: {86, 70, 9} color: #blue wireframe: true;
            }
            overlay {
                draw "Top: hole / concavity / rotating cube" at: {16, 26} color: #black;
                draw "Bottom: alpha overlap / sphere / wireframe" at: {16, 50} color: #black;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "3 - Lighting and textures" type: gui {
    text "Purpose: compare normals and shading on textured and untextured primitives under the same point light.<br/>Try: inspect first, then run to move the light; rotate the cameras and compare equivalent views." category: "What to compare";
    text "Expected: all objects remain visible. The moving point light changes the lit spheres and cube, while the unlit textured square keeps its brightness. The GAMA image must have the same orientation, not a vertical flip. Exact lighting intensity can differ between the fixed-function and shader backends; do not treat this as a photometric reference." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            light #ambient intensity: 80;
            light #default intensity: 0;
            light "Moving point" type: #point location: {50 + 35 * cos(cycle * 2), 50, 45}
                intensity: #white show: true dynamic: true;
            graphics "Lit objects" {
                draw square(100) at: {50, 50, -1} color: #lightgray;
                draw sphere(12) at: {25, 30, 12} color: #white;
                draw sphere(12) at: {72, 30, 12} texture: "../includes/wood.jpg";
                draw cube(20) at: {28, 73, 10} texture: "../images/Gama.jpg";
                draw square(24) at: {72, 73, 1} texture: "../images/Gama.jpg" lighted: false;
            }
            overlay {
                draw "Lit: white sphere / wood sphere / GAMA cube" at: {16, 26} color: #black;
                draw "Unlit reference: flat GAMA square (bottom right)" at: {16, 50} color: #black;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "4 - Wide grid and agents" type: gui {
    text "Purpose: preserve the wide-grid regression case (#1156) and compare moving agent geometry over a grid.<br/>Try: inspect before running, pan/zoom/rotate, then run. Compare cameras at similar angles." category: "What to compare";
    text "Expected: all 200 columns and 10 rows stay filled and bordered, including the left edge. Orange spheres must remain above the grid. Both displays share the same twelve agents; this is a correctness comparison, not a renderer performance benchmark." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            grid lane border: #black;
            species marker;
            overlay {
                draw "200 x 10 cells; inspect the left edge while rotating" at: {16, 26} color: #black;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "5 - Mesh and image" type: gui {
    text "Purpose: compare an elevated triangulated field with a raster image inset.<br/>Try: rotate the world to inspect the mesh peak and triangles, then resize and zoom. No steps are needed." category: "What to compare";
    text "Expected: both meshes have the same central peak, palette and triangle edges. The 160-by-110-pixel GAMA inset stays upright and fixed at the top right. Neither the image nor the mesh should disappear after drawing the caption." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            mesh heights scale: 2 triangulation: true border: #black
                color: palette([#blue, #cyan, #yellow, #red]);
            image "../images/Gama.jpg" coordinates: #screen position: {-176, 16} size: {160, 110};
            overlay {
                draw "5 x 5 field, central peak, triangulation" at: {16, 26} color: #black;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "6 - Chart raster quality" type: gui {
    text "Purpose: compare chart text, thin strokes, markers and texture scaling at identical frame sizes.<br/>Try: run, then pause and compare equal-sized viewports. Resize both views. The upper chart is 420 by 210 logical pixels; the lower chart follows viewport width but stays 180 pixels high. Allow at least 460 by 480 pixels per view." category: "What to compare";
    text "Expected: both backends plot the same shared signal with a 0-to-100 Y axis; curves, legend and axis labels stay readable and upright. Neither chart follows the world camera.<br/>OpenGL chart quality is also controlled by the saved graphical-resolution preference: set it to 1 for full resolution. This is not a Java2D comparison or an FPS benchmark." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            chart "Fixed pixel chart" type: series position: {16, 16} size: {420, 210}
                x_range: 200 y_range: {0, 100} {
                data "Signal" value: signal color: #blue;
                data "Reference" value: 50 color: #red marker: false;
            }

            chart "Viewport width, fixed height" type: series position: {16, 250} size: {0.9, 180}
                x_range: 200 y_range: {0, 100} {
                data "Signal" value: signal color: #blue;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "7 - World text and billboards" type: gui {
    text "Purpose: compare world-oriented text, closed extrusion caps and camera-facing labels without a covering screen panel.<br/>Try: rotate, zoom and pan both views to similar camera angles; no steps are needed." category: "What to compare";
    text "Expected: the blue O and B keep holes in their front and back caps and their extruded sides remain closed. The green plain text follows the world plane. The red billboard stays camera-facing, but its position follows the world camera.<br/>Known differences: legacy OpenGL billboards use restricted GLUT bitmap fonts and anchor handling. Plain OpenGL4 text uses JOGL font substitution; bordered/extruded text uses AWT outlines. These are comparison targets, not a claim of full typography parity." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            graphics "World text" {
                draw world.shape color: #lightgray border: #gray;
                draw "O B i j" at: {50, 60, 2} anchor: #center font: font("Serif", 38, #italic)
                    color: #blue border: #black depth: 4;
                draw "Plain world text" at: {50, 85, 2} anchor: #center
                    font: shared_font color: #green;
                draw "Billboard label" at: {25, 30, 8} font: shared_font color: #red perspective: false;
            }
            overlay {
                draw "Rotate: inspect caps, holes and camera-facing label" at: {16, 26} color: #black;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

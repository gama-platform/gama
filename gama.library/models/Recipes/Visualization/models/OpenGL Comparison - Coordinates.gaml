/**
* Name: OpenGL Comparison - Coordinates
* Description: Dedicated OpenGL/OpenGL4 counterparts of the Display Coordinates tutorials.
*   Every experiment has just two displays: OpenGL defines the scene and OpenGL4 inherits it.
*   Compare equal-sized viewports before interpreting size differences. Cameras are independent.
*   See OpenGL Comparison - Rendering and OpenGL Comparison - Ants for non-coordinate scenes.
* Tags: visualization, opengl, opengl4, comparison, coordinates, referential, overlay, mouse
*/
model opengl_coordinate_comparison

global {
    geometry shape <- square(100);
    point moving_point <- {50, 50} update: {50 + 30 * sin(cycle * 2), 50 + 20 * cos(cycle * 2)};
    float stored_pixel_value <- 20#px;
    point clicked_world <- {50, 50};
    point clicked_screen <- {0, 0};
    int clicks <- 0;
    font shared_font <- font("SansSerif", 16);
    list<string> names <- ["top_left", "top_center", "top_right", "left_center", "center",
        "right_center", "bottom_left", "bottom_center", "bottom_right"];
    list<point> anchors <- [#top_left, #top_center, #top_right, #left_center, #center,
        #right_center, #bottom_left, #bottom_center, #bottom_right];
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

experiment "1 - World HUD minimap and chart" type: gui {
    text "Purpose: compare a camera-attached world with a fixed screen HUD, world-content mini-map and chart.<br/>Try: run, then resize, zoom and pan. Use equally sized viewports to compare the two renderers.<br/>Expected: only the main world follows the camera. Mini-map and main-view markers represent the same world location. The chart plots world X, from 0 to 100." category: "What to compare";
    text "The chart and mini-map use viewport-relative frame sizes: unequal viewport heights intentionally produce unequal heights. The mini-map fits its world uniformly from the upper-left; unused white space is expected. Exact font shapes can differ between backends." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            graphics "World" {
                draw world.shape color: #lightgray border: #gray;
                draw line([{0, 50}, {100, 50}]) color: #white;
                draw line([{50, 0}, {50, 100}]) color: #white;
                draw circle(5) at: moving_point color: #blue;
                draw "WORLD: 100 x 100 model units" at: {50, 8}
                    anchor: #center font: font("Helvetica", 14) color: #black;
            }
            graphics "HUD" coordinates: #screen position: {16, 16} size: {0.52, 108}
                background: #white border: #gray {
                draw "Screen-space HUD" at: {12, 24} anchor: #left_center
                    font: font("Helvetica", 18) color: #black;
                draw ("Viewport: " + #display_width + " x " + #display_height)
                    at: {12, 52} anchor: #left_center color: #black;
                draw ("Mouse pixels: " + #mouse_location_in_display)
                    at: {12, 74} anchor: #left_center color: #black;
                draw "Only the main world follows the camera" at: {12, 96}
                    anchor: #left_center color: #black;
            }
            overlay referential: #world position: {-0.26, 16} size: {0.24, 0.24}
                background: #white border: #gray {
                draw world.shape color: #lightgray border: #gray;
                draw circle(5) at: moving_point color: #blue;
                draw "World mini-map" at: {50, 10} anchor: #center color: #black;
            }
            chart "World marker X (0-100)" kind: series position: {16, -0.30} size: {0.48, 0.28}
                x_range: 200 y_range: {0, 100} {
                data "X" value: moving_point.x color: #blue;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "2 - Placement and world-content fitting" type: gui {
    text "Purpose: compare fixed pixels, viewport fractions, negative positions and world-content fitting.<br/>Try: resize, zoom and pan; no steps are needed. Allow at least 600 by 480 pixels per view.<br/>Expected: the top blue frame keeps its pixel size; the top-right red frame keeps a 16-pixel outer margin. The bottom green frame scales with the viewport." category: "What to compare";
    text "The two middle frames are both 240 by 140 pixels. A square world is stretched by graphics (purple circle becomes elliptical), but uniformly fitted by the overlay (circle stays round). Neither frame follows the world camera. Drawings may overflow: frames do not imply clipping." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            graphics "World" {
                draw world.shape color: #lightgray border: #gray;
            }
            graphics "Pixels" coordinates: #screen position: {16, 16} size: {240, 76}
                background: #white border: #blue {
                draw "Fixed 240 x 76 pixels" at: {12, 28} color: #blue;
                draw "Top-left margin: 16 pixels" at: {12, 54} color: #black;
            }
            graphics "Opposite edge" coordinates: #screen position: {-256, 16} size: {240, 76}
                background: #white border: #red {
                draw "Fixed 240 x 76 pixels" at: {12, 28} color: #red;
                draw "Right margin: 16 pixels" at: {12, 54} color: #black;
            }
            graphics "Stretched world" coordinates: #screen referential: #world
                position: {16, 130} size: {240, 140} background: #white border: #green {
                draw world.shape color: #lightgray border: #green;
                draw circle(20) at: {50, 50} color: #purple;
                draw circle(6#px) at: {15, 15} color: #red;
            }
            overlay referential: #world position: {280, 130} size: {240, 140}
                background: #white border: #purple {
                draw world.shape color: #lightgray border: #purple;
                draw circle(20) at: {50, 50} color: #purple;
                draw circle(6#px) at: {15, 15} color: #red;
            }
            graphics "Captions" coordinates: #screen {
                draw "Stretched graphics" at: {16, 298} color: #green;
                draw "Uniform overlay" at: {280, 298} color: #purple;
            }
            graphics "Fractions" coordinates: #screen position: {0.1, -0.25} size: {0.8, 0.2}
                background: #white border: #green {
                draw "80% viewport width; center dot uses local fractions"
                    at: {12, 26} color: #black;
                draw circle(6) at: {0.5, 0.5} color: #green;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "3 - Screen threshold and contextual constants" type: gui {
    text "Purpose: compare the strict screen threshold and contextual #px/display values.<br/>Try: resize and zoom; no steps are needed. Allow at least 480 by 500 pixels per view.<br/>Expected: in the 400-pixel panel, x=0.5 and x=0.5#px both mean 200 pixels; x=1 and x=1.0 mean one pixel. Intrinsic circle radii are pixels even below one. The one-pixel orange frame intentionally allows overflowing drawings." category: "What to compare";
    text "Screen #px remains 1. The world blue circle keeps a 10-pixel radius while its world-unit conversion changes on zoom. The stored global 20#px is an already evaluated number, not an expression that changes referential. Viewport readings must come from the display currently drawing, not its sibling." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            graphics "World constants" {
                draw world.shape color: #lightgray border: #gray;
                draw circle(10#px) at: {85, 85} color: #blue;
                draw ("World #px = " + #px) at: {55, 95} color: #blue;
            }
            graphics "Threshold" coordinates: #screen position: {24, 24} size: {400, 310}
                background: #white border: #blue {
                draw "Screen x values in a 400-pixel panel" at: {12, 24} color: #black;
                draw circle(4) at: {0.5, 58} color: #blue;
                draw "0.5 = 200 pixels" at: {214, 62} color: #blue;
                draw circle(4) at: {1, 100} color: #red;
                draw "1 = 1 pixel" at: {16, 104} color: #red;
                draw circle(4) at: {1.0, 142} color: #orange;
                draw "1.0 = 1 pixel too" at: {16, 146} color: #orange;
                draw circle(4) at: {0.5#px, 184} color: #purple;
                draw "0.5#px = 0.5 = 200 pixels" at: {12, 222} color: #purple;
                draw circle(0.5) at: {380, 100} color: #black;
                draw square(1) size: {0.2, 16} at: {0.5, 250} color: #green;
                draw "size: {0.2,16} = 80 x 16 pixels" at: {12, 290} color: #green;
            }
            graphics "Constants" coordinates: #screen {
                draw ("Screen #px=" + #px + "; viewport=" + #display_width + " x " + #display_height)
                    at: {24, 360} color: #black;
                draw ("Global 20#px stored before drawing: " + stored_pixel_value)
                    at: {24, 390} color: #black;
            }
            graphics "One-pixel frame" coordinates: #screen position: {24, 420} size: {1, 1}
                background: #orange border: #orange {
                draw circle(8) at: {20, 20} color: #orange;
                draw "One-pixel frame; overflowing contents" at: {40, 26} color: #black;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "4 - Spatial layers in screen frames" type: gui {
    text "Purpose: compare grid, species and agents layers in world and screen placement.<br/>Try: resize, zoom and pan; no steps are needed.<br/>Expected: the background grid and its twelve dots follow the camera. The two fixed 200-by-200-pixel copies show identical dot locations. Spatial data remains in model units; frame placement does not rewrite locations." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            grid cell border: #gray;
            species dot;
            grid cell coordinates: #screen position: {16, 100} size: {200, 200} border: #gray;
            species dot coordinates: #screen position: {16, 100} size: {200, 200};
            agents "Agent-list copy" value: list(dot) coordinates: #screen
                position: {240, 100} size: {200, 200};
            overlay {
                draw "World grid behind; fixed copies use the same dot agents" at: {16, 26} color: #black;
                draw "Grid + species" at: {16, 330} color: #black;
                draw "agents layer" at: {240, 330} color: #black;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "5 - Image mesh and chart screen frames" type: gui {
    text "Purpose: compare fixed image and mesh frames alongside a viewport-width chart.<br/>Try: run to populate the chart, then resize and zoom. Allow at least 480 by 450 pixels per view.<br/>Expected: image and mesh stay 200 by 140 pixels; the chart stays 180 pixels high and follows 90 percent of the viewport width. All three are independent of the world camera." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            image "../images/Gama.jpg" coordinates: #screen position: {16, 60} size: {200, 140};
            mesh heights coordinates: #screen position: {240, 60} size: {200, 140}
                scale: 0 color: palette([#blue, #cyan, #yellow, #red]);
            chart "Cycle" kind: series position: {16, 240} size: {0.9, 180} {
                data "Cycle" value: cycle;
            }
            overlay {
                draw "Image: 200 x 140 pixels" at: {16, 32} color: #black;
                draw "Mesh: same frame size" at: {240, 32} color: #black;
                draw "Chart: 90% viewport width, 180 pixels high" at: {16, 224} color: #black;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "6 - Text anchors and pointer events" type: gui {
    text "Purpose: compare all nine text anchors, relative text positions and pointer coordinate snapshots.<br/>Try: run, left-click either view, then pan/zoom and resize. Allow at least 610 by 580 pixels per view.<br/>Expected: labels meet their red dots at the named anchors. The blue relative label stays centered. A click records world and screen coordinates from the same event; its red world marker follows the camera. Both displays share the click count." category: "What to compare";
    text "The overlay-local mouse value subtracts {16,320} from viewport pixels; it is not a new mouse unit. Each display supplies its own live readings. All labels reuse the same nominal 16-point font, but backend font substitution may change exact glyphs." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            graphics "World" {
                draw world.shape color: #lightgray border: #gray;
                draw circle(2) at: clicked_world color: #red;
                draw "Shared nominal font: 16" at: {50, 80} anchor: #center
                    font: shared_font color: #blue;
            }
            event #mouse_listener {
                if #mouse_action = #mouse_down and #mouse_button = "left" {
                    ask simulation {
                        clicked_world <- #mouse_location_in_world;
                        clicked_screen <- #mouse_location_in_display;
                        clicks <- clicks + 1;
                    }
                }
            }
            graphics "Anchors" coordinates: #screen position: {16, 16} size: {570, 290}
                background: #white border: #black {
                draw "All nine labels use the same font object" at: {12, 26}
                    font: shared_font color: #black;
                loop i from: 0 to: 8 {
                    point dot_position <- {100 + (i mod 3) * 185, 90 + int(i / 3) * 72};
                    draw circle(3) at: dot_position color: #red;
                    draw names[i] at: dot_position anchor: anchors[i] font: shared_font color: #black;
                }
            }
            overlay position: {16, 320} size: {570, 160} background: #white border: #gray {
                draw ("Live viewport: " + #mouse_location_in_display + "; world: " + #mouse_location_in_world)
                    at: {12, 26} color: #black;
                draw ("Local: " + (#mouse_location_in_display - {16, 320})) at: {12, 56} color: #purple;
                draw ("Click world: " + clicked_world) at: {12, 86} color: #red;
                draw ("Same click pixels: " + clicked_screen + "; count: " + clicks)
                    at: {12, 116} color: #red;
            }
            graphics "Relative label" coordinates: #screen position: {16, 496} size: {570, 60}
                background: #white border: #blue {
                draw circle(3) at: {0.5, 0.5} color: #red;
                draw "at: {0.5,0.5}, anchor: #center" at: {0.5, 0.5}
                    anchor: #center font: shared_font color: #blue;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "7 - World overlay and omitted sizes" type: gui {
    text "Purpose: compare a world-positioned overlay and a graphics layer with its default world placement and omitted size.<br/>Try: resize, zoom and pan; no steps are needed.<br/>Expected: the purple overlay follows the camera and occupies 40 by 30 model units. Its blue circle is a world drawing, not a ten-pixel symbol. The caption and red bottom-right dot stay on screen in a full-viewport graphics frame with size omitted." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            graphics "Default world frame" {
                draw world.shape color: #lightgray border: #gray;
            }
            overlay coordinates: #world position: {30, 30} size: {40, 30}
                background: #white border: #purple {
                draw world.shape color: #lightgray border: #purple;
                draw circle(10) at: {50, 50} color: #blue;
            }
            graphics "Full screen frame" coordinates: #screen {
                draw "Purple overlay: world placement, follows camera" at: {16, 26} color: #purple;
                draw "Omitted size fills the reference extent" at: {16, 54} color: #black;
                draw circle(6#px) at: {-16, -16} color: #red;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

experiment "8 - Default full chart frame" type: gui {
    text "Purpose: compare the default chart placement and omitted size.<br/>Try: run, pause and resize equally sized views.<br/>Expected: each chart fills its viewport, does not follow the camera and displays the same data. An omitted size means full extent, unlike size: {1,1}, which would mean a one-pixel screen frame." category: "What to compare";
    output {
        layout #horizontal;
        display "OpenGL" type: opengl antialias: true {
            chart "Cycle" kind: series {
                data "Cycle" value: cycle;
            }
        }
        display "OpenGL4" parent: "OpenGL" type: opengl4 {}
    }
}

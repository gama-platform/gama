/**
* Name: Display Coordinates - Placement
* Description: Compare fixed pixels, viewport fractions and opposite-edge offsets, then compare identical
*   numbers in default-world and explicit screen layers. Resize, zoom and pan; no simulation steps are required.
*   Default graphics and explicit world frames are identical. Overlays and charts default to screen.
* Tags: visualization, display, coordinates, screen, world, defaults, placement
*/
model coordinate_placement

global {
    geometry shape <- square(100);
}

experiment "1 - Viewport placement" type: gui {
    text "Purpose: compare pixel positions, viewport fractions and negative offsets in screen layers.<br/>Try: resize, zoom and pan each display; no simulation steps are needed.<br/>Expected: the blue panel keeps its pixel size and top-left margin, the green panel scales with the viewport, and the red panel keeps a 16-pixel bottom/right margin. Only the grey world follows the camera." category: "What this demonstrates";
    output {
        display "Java2D" type: java2D {
            graphics "World" coordinates: #world {
                draw world.shape color: #lightgray border: #gray;
                draw circle(5) at: {50, 50} color: #blue;
            }
            graphics "Fixed pixels" coordinates: #screen position: {16, 16} size: {230, 76}
                background: #white border: #blue {
                draw "position: {16,16}" at: {12, 26} color: #blue;
                draw "size: {230,76} pixels" at: {12, 54} color: #black;
            }
            graphics "Fractions" coordinates: #screen position: {0.5, 0.3} size: {0.45, 0.25}
                background: #white border: #green {
                draw "position: {0.5,0.3}" at: {12, 26} color: #green;
                draw circle(6) at: {0.5, 0.5} color: #green;
                draw "Local center" at: {0.5, -12} anchor: #bottom_center color: #black;
            }
            // A negative position places the origin, not the right/bottom corner of the frame.
            graphics "Opposite edge" coordinates: #screen position: {-230, -90} size: {214, 74}
                background: #white border: #red {
                draw "origin: {-230,-90}" at: {12, 26} color: #red;
                draw "16-pixel outer margin" at: {12, 54} color: #black;
            }
        }
        display "OpenGL" parent: "Java2D" type: opengl map_order: true {}
    }
}

experiment "2 - Defaults versus explicit modes" type: gui {
    text "Purpose: compare identical position {0.5,0.5} and size {0.4,0.4} values under different defaults.<br/>Expected: default graphics and explicit world graphics match: their frames are only 0.4 world units wide/high and can look almost invisible. Explicit screen graphics and default overlays instead occupy 40 percent of the viewport, starting at its center." category: "What this demonstrates";
    text "Try: resize, zoom and pan the views.<br/>In the last view, referential: #world changes only the overlay contents: the world circle is uniformly fitted into the screen frame. This differs from the 10-pixel circle in the screen-content views. Drawings may overflow their frames; there is no implicit clipping." category: "What this demonstrates";
    output {
        layout #horizontal;
        // Omitting coordinates on graphics is exactly equivalent to coordinates: #world.
        display "Default graphics - world units" type: java2D {
            graphics "Same numbers" position: {0.5, 0.5} size: {0.4, 0.4}
                background: #lightgray border: #red {
                draw circle(10) at: {50, 50} color: #red;
            }

        }
        // Model units: a 0.4-by-0.4-world-unit frame, not 40 percent.
        display "World - tiny model-unit rectangle" type: java2D {
            graphics "Same numbers" coordinates: #world position: {0.5, 0.5} size: {0.4, 0.4}
                background: #lightgray border: #red {
                draw circle(10) at: {50, 50} color: #red;
            }
        }
        // Viewport fractions: the frame starts halfway across the visible viewport.
        display "Screen - viewport fractions" type: java2D {
            graphics "Same numbers" coordinates: #screen position: {0.5, 0.5} size: {0.4, 0.4}
                background: #lightgray border: #green {
                draw circle(10) at: {50, 50} color: #green;
            }
        }
        display "Default overlay - screen pixels" type: java2D {
            overlay position: {0.5, 0.5} size: {0.4, 0.4}
                background: #lightgray border: #green {
                draw circle(10) at: {50, 50} color: #green;
            }
        }
        display "Default overlay - world contents" type: java2D {
            overlay referential: #world position: {0.5, 0.5} size: {0.4, 0.4}
                background: #lightgray border: #blue {
                draw circle(10) at: {50, 50} color: #blue;
            }
        }
    }
}

experiment "3 - Full frames by default" type: gui {
    text "Purpose: demonstrate omitted coordinates and size facets. Graphics default to world placement; overlays and charts default to screen placement. An omitted size fills the reference extent, unlike size: {1,1}.<br/>Try: resize, zoom and pan, then run to populate the chart.<br/>Expected: the grey world follows the camera, while the overlay caption and red bottom-right dot stay on screen. The separate chart fills its viewport." category: "What this demonstrates";
    output {
        display "Full world and screen frames" type: java2D {
            graphics "Default world" {
                draw world.shape color: #lightgray border: #gray;
                draw circle(5) at: {50, 50} color: #blue;
            }
            // No coordinates, referential or size: the overlay covers the viewport in pixels.
            overlay {
                draw "Default overlay: screen, full viewport" at: {16, 26} color: #black;
                draw circle(6#px) at: {-16, -16} color: #red;
            }
        }
        display "OpenGL full frames" parent: "Full world and screen frames" type: opengl map_order: true {}
        display "Full chart frame" type: 3d {
            // Charts also default to screen placement and a full viewport-sized frame.
            chart "Cycle" type: series {
                data "Cycle" value: cycle;
            }
        }
    }
}

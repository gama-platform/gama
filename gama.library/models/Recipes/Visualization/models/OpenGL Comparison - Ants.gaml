/**
* Name: OpenGL Comparison - Ants
* Description: Reuse the Ant Foraging experiments unchanged, adding an OpenGL4 sibling to each
*   OpenGL scene. Both displays share agents, parameters and simulation time. The imported demo
*   remains the single source for the ant behavior, terrain, icons, trails and 3D assets.
* Tags: visualization, opengl, opengl4, comparison, ants, svg, texture, terrain
*/
model opengl_ants_comparison

import "../../Ants (Foraging and Sorting)/models/Ant Foraging.gaml"


experiment "1 - Classic comparison" type: gui parent: Classic {
    text "Purpose: compare the original Ant Foraging scene with exactly the same agents in OpenGL4.<br/>Try: run, enable icons and state labels, then compare equal-sized views at the same zoom.<br/>Expected: terrain, signal patches, ant locations and icon sizes agree. Each camera remains independent; displayed FPS is not a fair benchmark with both renderers active." category: "What to compare";
    output {
        display "OpenGL4" parent: Ants type: opengl4 {}
    }
}

experiment "2 - SVG and HUD comparison" type: gui parent: "With Inspector" {
    text "Purpose: compare the demo's SVG ants, world-positioned inset layers, translucent trails and screen HUD.<br/>Try: run, resize and zoom both displays.<br/>Expected: SVG ants keep the requested size, terrain and trails remain aligned, and the legend stays fixed on screen. Font substitution can change exact HUD glyphs. The inherited inspector shows the same agents as both views." category: "What to compare";
    output {
        display "OpenGL4" parent: Ants type: opengl4 {}
    }
}

experiment "3 - Terrain and 3D ants comparison" type: gui parent: "3D View" {
    text "Purpose: compare the original textured elevated grid, signal trails and imported 3D ant geometry.<br/>Try: run, rotate and zoom both views to matching angles.<br/>Expected: terrain orientation, ant positions and trail alignment agree; surfaces should not disappear during rotation. Lighting details can differ between rendering pipelines." category: "What to compare";
    output {
        display "OpenGL4" parent: Ants3D type: opengl4 {}
    }
}

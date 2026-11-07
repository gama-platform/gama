# Explicit display-layer coordinates

Use `coordinates: #world` for a spatial layer and `coordinates: #screen` for
a viewport-attached panel. Use `referential:` only when a graphics or overlay
layer's drawings need a different coordinate system from its frame. These
are layer facets, not display facets.

## Graphics: the world's aspect or a screen information layer

**A `graphics` layer is not inherently an overlay.** It is a free drawing
layer, conceptually the **aspect of the world**: it lets the model draw
geometry, images and text without introducing a species just for their
appearance. Its normal role is spatial: backgrounds, world boundaries,
routes, annotations and other drawings that belong to the simulation.
For that role, use `coordinates: #world`, which is also the default.
Drawings use model units and follow the world camera when it pans, zooms
or rotates.

The same layer is also useful for information that belongs to the **display
rather than the world**: a title, legend, status panel or other HUD.
For that role, explicitly use `coordinates: #screen`. The frame stays
attached to the viewport and its drawings use screen coordinates by default,
just like a screen overlay. Choosing `graphics` does not force information
into world coordinates, and choosing `#screen` does not make the information
part of the simulated world.

| What the drawings represent | Recommended graphics placement | Camera behavior |
|---|---|---|
| The world's appearance, spatial annotations or model geometry | `coordinates: #world` (default) | Follows the world camera |
| Information for the viewer, independent of world position | `coordinates: #screen` | Stays attached to the viewport |

Both roles can coexist in one display:

```gaml
display "World and information" {
    // The world's aspect: positions and lengths are model units.
    graphics "World appearance" coordinates: #world {
        draw world.shape color: #lightgray border: #gray;
        draw circle(5) at: {50, 50} color: #blue;
    }
    // Viewer information: this panel stays fixed while the world moves.
    graphics "Status" coordinates: #screen position: {16, 16} size: {280, 72}
        background: #white border: #gray {
        draw "Simulation status" at: {12, 26} color: #black;
        draw ("Cycle: " + cycle) at: {12, 54} color: #blue;
    }
}
```

Decide from the **meaning of the drawing**, not whether it contains text:
a label marking a world location belongs in world graphics, whereas a
numerical status readout belongs in screen graphics or a screen overlay.
Do not compensate for camera movement using hand-written `#px` conversions
when the intended role is a fixed screen information layer.

`graphics coordinates: #screen` and `overlay` therefore overlap in their
information-display role, but are not interchangeable in every respect.
Graphics defaults to world placement; overlay defaults to screen placement.
Multiple named graphics layers can organize separate information panels,
whereas a display supports one overlay. When either has screen placement
and `referential: #world`, graphics fits world contents using independent
X/Y scales, while overlay uses a uniform fit. These distinctions concern
layer behavior, not a rule that only overlays may draw screen information.
See **Placement versus contents** for that less common mixed case.

## Runnable examples

The following models are in **Recipes / Visualization**. Start with
`Display Coordinates.gaml`, then open the focused recipes. Except for the
default-mode comparison, the experiments provide inherited Java2D and
OpenGL displays using the same declarations. OpenGL4 comparisons are grouped
in the dedicated `OpenGL Comparison - ...` models listed below. Give each display
enough room (approximately 600 by 450 logical pixels) to read the panels;
fixed-pixel panels intentionally do not shrink to fit a smaller viewport.
The rendering comparison separates its fixed text panel from world-space
extrusion so that the panel does not hide the objects being inspected.

Chart-bearing library displays now explicitly use `type: 3d` rather than
Java2D, including mixed chart/image/mesh panels and the full-chart-frame
example. Existing OpenGL and OpenGL4 displays retain their backends.
Historical display names such as `"Java2D"` are kept so `parent:` references
remain valid; for chart-bearing examples, these names no longer identify the
backend. Examples without charts retain their Java2D/OpenGL comparisons.

| Model | Experiments and questions to explore |
|---|---|
| [Display Coordinates](https://github.com/gama-platform/gama/blob/main/gama.library/models/Recipes/Visualization/models/Display%20Coordinates.gaml) | Overview: one moving blue marker in the world and mini-map, a fixed screen HUD and a chart of the marker's world X |
| [Display Coordinates - Placement](https://github.com/gama-platform/gama/blob/main/gama.library/models/Recipes/Visualization/models/Display%20Coordinates%20-%20Placement.gaml) | `1 - Viewport placement`: pixels, fractions and negative offsets; `2 - Defaults versus explicit modes`: default graphics, explicit world/screen, default overlays and a world-content mini-map; `3 - Full frames by default`: omitted sizes on graphics, overlay and chart |
| [Display Coordinates - Units](https://github.com/gama-platform/gama/blob/main/gama.library/models/Recipes/Visualization/models/Display%20Coordinates%20-%20Units.gaml) | `Screen units and boundaries`: exactly 1, `1.0`, `0.5#px`, mixed rectangular sizes and intentional overflow |
| [Display Coordinates - Referentials](https://github.com/gama-platform/gama/blob/main/gama.library/models/Recipes/Visualization/models/Display%20Coordinates%20-%20Referentials.gaml) | `1 - Placement versus referential`: pixel drawings versus stretched or uniformly fitted world drawings; `2 - A spatial overlay`: world placement follows the camera |
| [Display Coordinates - Constants](https://github.com/gama-platform/gama/blob/main/gama.library/models/Recipes/Visualization/models/Display%20Coordinates%20-%20Constants.gaml) | `Contextual constants`: live screen/world values, stored values and independent viewports |
| [Display Coordinates - Mouse](https://github.com/gama-platform/gama/blob/main/gama.library/models/Recipes/Visualization/models/Display%20Coordinates%20-%20Mouse.gaml) | `Mouse in three referentials`: viewport, world and panel-local coordinates, including paired callback snapshots |
| [Display Coordinates - Text](https://github.com/gama-platform/gama/blob/main/gama.library/models/Recipes/Visualization/models/Display%20Coordinates%20-%20Text.gaml) | `Font and anchor semantics`: one shared font object, nine anchors and fractional placement. Multiline and outline comparisons are in `OpenGL Comparison - Rendering.gaml` |
| [Display Coordinates - Layers](https://github.com/gama-platform/gama/blob/main/gama.library/models/Recipes/Visualization/models/Display%20Coordinates%20-%20Layers.gaml) | `1 - Spatial layers`: world/screen copies of a grid and agents; `2 - Image mesh and chart`: screen rectangles around other layer types |

Resize one display without resizing the others, then zoom and pan the world.
Screen frames must follow their own viewport, not the frontmost display;
world drawings must continue to follow their camera. Run the simulation for
the moving mini-map marker, input callbacks and evolving chart data.

## Compatibility and defaults

Every coordinate-recipe experiment includes a **What this demonstrates**
category in the Parameters view. It explains the purpose, interactions to try,
expected visual results and intentional differences or limitations. These
instructions remain available even when a display cannot render; source
comments and on-screen captions provide additional detail rather than being
the only explanation.

`coordinates:` selects the placement and size convention of a layer:

```gaml
graphics "World" coordinates: #world {
    draw circle(10) at: {50, 50};
}
overlay coordinates: #screen position: {16, 16} size: {0.4, 80} {
    draw "Simulation information" at: {12, 24};
}
```

Both `#screen` and `#world` are string constants. The facet must be constant.
It is available on graphics, overlay, species, agents, grid, mesh, image/picture
and chart layers. The declaration, not the display backend, selects its meaning.

**Only `#world` and `#screen` exist. There is no legacy/hybrid mode.**
Omitting the facet chooses a default by layer type:

| Layer | Default placement | Default contents |
|---|---|---|
| Graphics | `#world` | Inherits placement |
| Overlay | `#screen` | Inherits placement |
| Species, agents, grid, mesh, image/picture | `#world` | World data/locations |
| Chart | `#screen` | Chart-managed data and axes |

Layer frames, including overlays, default to `background: nil`: no background
is painted behind their contents. Earlier overlays implicitly used black;
specify `background: #black` to keep that appearance, or another colour for
a visible panel. Display backgrounds and chart-background preferences are
unchanged.

`referential:` is available on graphics and overlays and can be supplied
without `coordinates:`. Thus `overlay referential: #world` is a screen-placed
world-content mini-map. A graphics layer still needs `coordinates: #screen`
before it can request screen contents.

This is an intentional breaking change, not automatic interpretation of old
models. A historical `size: {1,1}` no longer means full extent: omit `size:`
for a full frame. Fractional spatial placements must be expressed in world
units or explicitly select screen placement. Overlay intrinsic geometry
sizes now mean pixels by default. See the migration section before adapting
external models.

## Screen placement

Screen placement uses the visible display viewport, not the world extent.
The origin is the upper-left corner; X increases rightwards and Y downwards.
Dimensions and positions are in logical display pixels, not physical monitor
pixels. Camera movement, world zoom and world rotation do not move the frame.

Each X/Y component is interpreted independently:

| Magnitude | Meaning |
|---|---|
| Strictly less than 1 | Fraction of the reference width or height |
| At least 1 | Logical pixels |

For positions, a negative component is measured from the opposite edge:
`-20` is 20 pixels from the right/bottom and `-0.25` is three quarters of the
way across/down the reference rectangle. This positions the layer's origin;
it does not automatically subtract the layer width or apply an anchor.
Layer sizes must be nonnegative and finite. Positions must be finite.

Examples for an 800-by-600 viewport:

| Declaration | Pixel result |
|---|---|
| `position: {0.5, 20}` | `{400, 20}` |
| `position: {-20, -0.25}` | `{780, 450}` |
| `size: {0.25, 100}` | `{200, 100}` |
| `size: {1, 1}` or `size: {1.0, 1.0}` | `{1, 1}` |

Integer and floating-point spellings have the same meaning: point components
are stored as doubles. **Exactly 1 is one pixel, not the whole extent.**
When `size:` is omitted, the frame has the full viewport dimensions.
To explicitly request those dimensions, write
`size: {#display_width, #display_height}`.
The rectangle is reevaluated when the layer refreshes. A frozen layer still
follows its existing refresh policy.

The position's Z component is not an extra screen-depth coordinate. Screen
layers are composited using display-layer ordering. Do not use world Z values
to position a HUD relative to the camera.
In OpenGL, only the final projected depth of screen contents is flattened.
Agent elevation, mesh heights and extrusion therefore do not cause near/far
clipping. Object-level 3D transforms are still applied before projection;
this does not introduce depth sorting within a screen layer.

## World placement

In `coordinates: #world` (including the spatial-layer default), every X/Y component of `position:` and
`size:` is a model-unit value. There is no fraction threshold, and a negative
position remains a negative world displacement, not an opposite-edge offset.
For example, `{0.5, 1}` means half a world unit and one world unit.

An omitted size covers the environment's world extent. Spatial contents retain
their normal model coordinates, camera transforms and renderer-specific 3D
behavior. An explicitly world-positioned overlay is a spatial graphics layer:
unlike a traditional overlay, it follows the world camera.
For both OpenGL backends, the bookkeeping pixel rectangle uses the world's
aspect-ratio fit within the logical viewport, multiplied by `#zoom`. It is not
the projected bounding box of the visible scene: panning and camera rotation
do not make its dimensions negative. The drawings themselves still follow
the camera projection.

## Placement versus contents

`coordinates:` describes the layer rectangle. For `graphics` and `overlay`,
`referential:` can independently select the interpretation of drawings:

```gaml
overlay coordinates: #screen referential: #world
    position: {0.72, 16} size: {0.25, 0.25} {
    draw circle(10) at: {50, 50};
}
```

Graphics and overlays default their contents to the placement mode, whether
that mode is written explicitly or selected by the layer-type default.
A default overlay has screen contents; a world overlay has world contents.

For screen contents, `draw ... at:` and rectangular `size:` use the layer's
width and height as their references, with the same threshold and opposite-edge
position rules. Intrinsic geometry lengths, such as `circle(10)`, are pixels:
they are not fractions. An omitted `at:` retains the drawing's usual location.
Anchors continue to act on individual drawings; they do not reposition the
layer frame.

For world contents, locations and geometry lengths remain model units. A
screen overlay fits them into its frame using one uniform scale, based on the
environment extent, aligned to the upper-left corner. It does not recenter or
clip the content. Other screen-positioned spatial layers use their existing
per-axis layer scaling. Do not rewrite agent `location` values as fractions.
No new clipping is introduced: drawings may intentionally overflow a frame.
The existing limit of one overlay per display is unchanged; multiple named
screen graphics layers can also be used for HUD drawings.

World placement with screen contents is not supported. Neither are
nonzero rotations of screen layers; rotate individual drawings instead.
Nested species layers inherit the host layer's context and cannot specify an
independent placement mode.

### Chart resolution and typography

Charts default to `coordinates: #screen`. Their layout uses the layer's
logical-pixel width and height, preserving rectangular aspect ratios. For
Java2D, OpenGL and OpenGL4, the chart bitmap is rendered at the current drawing
target's native pixel density, including HiDPI/Retina scaling. Java2D uses the
actual graphics transform, including offscreen/export targets; OpenGL uses the
native framebuffer scale. Font sizes, line widths, layer
placement and chart axes do not change when raster resolution changes.

The **Chart raster scale** preference in **Displays / Default Chart Properties**
controls resolution relative to that native density for all three backends: `1` means native
resolution, `1.5` (the default for new preferences) and `2` supersample, and
values down to `0.1` reduce resolution for speed. Existing saved values are
retained, but now scale a rectangular, device-aware raster rather than a
square based on the smaller layer dimension. Each raster is limited to 4096
pixels per dimension and 8,388,608 pixels in total; larger charts reduce their
raster scale uniformly without changing their logical layout. Two RGBA raster
buffers use at most 64 MiB per chart, excluding GPU textures and chart data.

Use the chart facet `quality:` to override this preference for one chart:

```gaml
chart "Population" type: series quality: 2 {
    data "Population" value: length(my_species);
}
```

`quality:` accepts a finite integer or float from `0.1` to `2.0` inclusive,
with the same native-resolution scale as the preference, in Java2D, OpenGL and
OpenGL4. Its expression is evaluated once at chart initialization. Omitting
it keeps the chart linked to the preference; an explicit value does not alter
the preference or other charts. Invalid values raise an error, rather than
being clamped. Device density and the raster memory limits still apply.

Chart rasterization always uses antialiasing, independently of display/scene
antialiasing. Java2D scales the bitmap into the logical chart frame with
bicubic interpolation; its other drawings retain the display's existing
antialiasing and interpolation settings. OpenGL chart textures use trilinear mipmap filtering to retain the
benefits of supersampling when reduced on screen, without changing filtering
for ordinary image textures. World-placed charts still follow the camera; their
raster size uses the layer's existing logical-pixel bookkeeping rectangle, not an adaptive
perspective-projected bound. For fixed dashboard charts, keep screen placement.

Default tick and legend fonts now follow the **Default chart label font**
preferences (11 logical pixels by default), alongside axis labels. Titles
remain bold with their separate font preferences. Chart padding and legend
spacing separate text from the plot; existing palette, grid, background and
line-thickness preferences remain in effect. Explicit `tick_font`,
`legend_font`, `label_font` and `title_font` facets override these defaults.

### Worked example: one world, two mini-maps

The referentials recipe uses a 200-by-100-unit environment and two
240-by-160-pixel frames:

| Screen frame | Scale | World point `{100,50}` in local pixels | World `circle(20)` |
|---|---|---|---|
| `graphics ... referential: #world` | X: 1.2; Y: 1.6 | `{120,80}` | Ellipse, radii 24 and 32 pixels |
| `overlay ... referential: #world` | Uniform: 1.2 | `{120,60}` | Circle, radius 24 pixels |

The overlay leaves 40 pixels of unused space below its 240-by-120-pixel
world drawing; this is not a centered fit. Its origin is the frame origin.
Neither declaration changes the agents' locations. A moving world point
can be drawn directly in the main view and both mini-maps.

`#px` in these world-content drawings uses the content scale, not the
camera's full-viewport scale. For a stretched frame it uses the smaller
axis scale: `circle(6#px)` has radii 6 and 8 pixels in the graphics example,
but 6 and 6 pixels in the uniform overlay. A scalar pixel-to-world
conversion cannot cancel two different axis scales.

## Overlay UI controls (initial pointer implementation)

`ui_button`, `ui_switch`, `ui_radio` and `ui_slider` can be declared directly inside an
overlay with screen placement and screen contents. They are drawn using the
same graphics pipeline as the overlay, in Java2D, OpenGL and OpenGL4.
See the `Display UI Controls.gaml` recipe and these additional recipes in
`Recipes/Visualization/models`:

* `Display UI Controls - Interactions.gaml`: simulation actions change other
  controls' bindings, labels, accents and availability.
* `Display UI Controls - Four Styles.gaml`: four simultaneous simulations share
  one panel definition, with Liquid Glass-inspired, GAMA FlatButton, minimalist
  and Midnight Arcade styles supplied as simulation attributes. The glass style
  uses transparency, not native backdrop blur.
* `Display UI Controls - Simulation Toolbar.gaml`: experiment actions for
  play/pause, step, synchronization, reload and stop, with recreated toolbar
  icons and a real minimum-cycle-duration slider.

```gaml
overlay position: {16, 16} size: {240, 160} {
    ui_button "Reset" at: {12, 12} action: reset_controls;
    ui_switch "Running" var: running at: {12, 52};
    ui_slider "Speed" value: speed min: 0.0 max: 100.0 step: 1.0
        at: {12, 92} size: {216, 40} {
        speed <- ui_value;
    }
}
```

`var:` binds a persistent writable boolean (switch or toggle button) or numeric variable (slider),
not a temporary drawing variable.
The binding is assigned before an optional handler runs. `value:` instead
reads an expression and requires a change handler; no assignment is automatic.
These facets are mutually exclusive. A momentary button needs `action:` or a final block;
switches, toggle buttons and sliders accept `on_change:` or a final block, not both.
Handler facets accept an argument-free action or an inline block.
Inline handlers execute in the experiment context, like `event` blocks.
Named global actions execute on the display's owning simulation; named experiment
actions execute on the experiment. In multi-simulation experiments, the experiment's
`simulation` attribute can refer to a different simulation from the display's owner.
For inline handlers, store `self` in a typed global attribute and `ask` that attribute,
as in the Four Styles recipe, instead of assuming `ask simulation` targets this display.
The typed local `ui_value` (`bool` for switches and toggle buttons, `float` for sliders) is available
in a value control's inline handler or final block. It is not declared in the
model, a separately declared named action or the enclosing overlay.

`at:` and `size:` use the overlay's screen dimensions: magnitudes below 1
are fractions, otherwise logical pixels. Negative positions count from the
opposite edge. The default size is `{160,28}` and the default anchor is
`#top_left`; `anchor:` follows drawing anchors (Y = 1 means top).
All controls accept these appearance and interaction facets:

| Facet | Meaning | Default |
|---|---|---|
| `background:` | Background colour; `nil` means transparent | `nil` |
| `color:` | Text colour, or `#adaptive` for scene-dependent black/white text | `#adaptive`, including when inactive |
| `accent:` | Slider tracks, switch indicators and button interaction highlights | GAMA neutral: `rgb(102, 114, 126)` |
| `border:` | Border colour; explicit `nil` means no border | `accent:` for buttons (momentary or toggle); `nil` for switches, radios and sliders |
| `rounded:` | Capsule outline when true; FlatButton-style corners (8 logical-pixel arc) when false | `true` |
| `active:` | Accepts pointer interaction; `false` leaves the control visible | `true` |
| `font:` | Label font | Helvetica, 14 |

An explicit `color:` remains unchanged when inactive. `active:` replaces the
initial implementation's `enabled:` facet.

`#adaptive` is the default text colour for buttons, switches and sliders,
including toggle buttons. Omitting `color:` is equivalent to
`color: #adaptive`; an explicit colour overrides this behaviour.
Previously, omitted colours used dark slate (`rgb(34, 43, 61)`) or muted
slate when inactive (`rgb(110, 117, 129)`); specify those colours explicitly
if needed. At render time, each label samples the actual scene pixels
under its text bounds and composites any defined overlay background, then
its own control background, using their rendered opacity. Omitted or `nil`
backgrounds add nothing; opaque backgrounds replace the underlying pixels,
while translucent backgrounds blend with them. Backgrounds only affect pixels
inside their frame or rounded control shape, so overflowing text still uses
the pixels beneath it. Hovered, pressed and checked control fills are included.
Images, shadows and other overlay contents do not influence the choice.
Sliders resolve their label and numeric value independently.
The renderer chooses black or white from the composited
relative luminance, with a small hysteresis band to avoid flickering around
the contrast crossover. It updates on every rendered frame, including camera
changes, and remains adaptive when inactive.

Sampling uses overlay-local logical pixels, mapped to the backing image or
native OpenGL viewport. Java2D and image displays preserve a pre-overlay scene;
OpenGL and OpenGL4 share one bounded scene readback per adaptive overlay, not
one readback per label. Explicit constant non-adaptive colours keep the
existing rendering path without a scene capture when no other control needs
adaptive text. Java2D also prepares a scene capture
for dynamic control text colours, so they can switch to `#adaptive` without
evaluating the expression twice. `#adaptive` has no fixed RGB components: it
is supported for control text and `draw` text, not `accent:`, `border:`,
`background:` or non-text drawings.

Ordinary text can explicitly request the same black/white contrast selection:

```gaml
draw "A readable label" at: {20, 40} color: #adaptive;
```

This works in world and screen coordinates, in Java2D, OpenGL and OpenGL4.
It samples the pixels already rendered beneath the text at that point in the
layer/drawing order, including frame backgrounds and earlier drawings. Camera,
layer and text transforms determine the sampling area. Adaptive foregrounds
are unlit; an explicit ordinary border color remains unchanged.

**Ordinary text does not default to `#adaptive`.** The contrast calculation
samples at most 24 by 24 pixels, but OpenGL must resolve and read back scene
pixels for each adaptive draw string (each line for multiline text). Readbacks
can stall the GPU, especially for large populations with many labels. Sampled
images are bounded to 256 pixels on their longest side, but this does not
eliminate synchronization cost. Static layers containing adaptive text must
also redraw their text colors when the scene changes. Use an explicit color
for high-volume labels. Java2D samples its image-backed frame without a GPU
readback; dynamic draw colors prepare that frame so they can switch to
`#adaptive` without reevaluating the color expression.

```gaml
ui_button "Reset" action: reset_controls;
ui_switch "Running" var: running;
ui_slider "Speed" var: speed min: 0.0 max: 100.0;
ui_button "Fixed text colour" color: #black action: reset_controls;
```

An omitted or `nil` background draws no control fill or shadow, including
while hovered, pressed or checked. Labels, borders, slider tracks and switch
indicators remain visible. Set an explicit translucent `background:` to
enable the glass-style fill and shadow; rounded corners and interaction
indicators are independent of that background.

Controls use fully rounded capsule edges by default. `rounded: false` uses
the FlatButton geometry: a rectangle with an 8 logical-pixel corner arc,
clamped for very small controls. The fill, shadow and border
and adaptive-text background follow this outline; switch indicators and slider
thumbs retain their usual shapes. Contents remain inset in both modes. Horizontal
padding is at least 18 logical pixels on normal-sized controls and grows
with their height; compact widths reduce it proportionally. Slider thumbs
stay inside this padding at both ends, and pointer values use the same
padded track as the rendering. These insets do not change the declared `size:`
or input bounds.

`accent:` applies to all three controls, including non-image toggle buttons. It is
evaluated on each redraw, independently of `color:` and `background:`.
Its alpha is preserved when deriving highlights; inactive indicators
use a lower opacity of the same accent colour. `accent: nil` is invalid.
For example:

```gaml
ui_button "Reset" accent: #teal action: reset_controls;
ui_switch "Running" var: running accent: #teal;
ui_slider "Speed" var: speed min: 0.0 max: 100.0 accent: #teal;
```

Buttons are momentary by default. `toggle: true` makes a button remain checked
until its next activation, using a boolean binding instead of hidden widget state:

```gaml
ui_button "Running" toggle: true var: running border: #gray
    background: #white color: #black active: true;
ui_button "Running" toggle: true value: running on_change: {
    running <- ui_value;
};
```

`toggle:` must be constant. Toggle buttons use `on_change:`, not `action:`,
and update their binding on release inside the button, before invoking the handler.
The accent-coloured border indicates the hovered, pressed or checked state.

Inline button handlers run on the display's experiment, so they can use its
actions directly rather than targeting whichever experiment is frontmost:

```gaml
ui_button "Play / Pause" {
    if status = "PAUSED" { do play; } else { do pause; }
}
ui_button "Step" { do step_once; }
ui_button "Sync / async" {
    if is_synchronized { do desynchronize; } else { do synchronize; }
}
ui_button "Reload" { do reload; }
ui_button "Stop" { do die; }
```

`step_once` requests one cycle; synchronization controls whether execution waits
for outputs. `reload` recreates the current run, and `die` closes this experiment
and its views, returning to modeling. Play/Pause and Step affect all simulations
controlled by this experiment. Image facets remain supported.
The example uses a combined Play/Pause icon and a fixed Sync/async icon; these
icons do not mirror the native toolbar's checked or running-state appearance.
For duration, bind a slider to the experiment's `minimum_cycle_duration`
(seconds); this changes real scheduling, not the model's simulated `step` duration.

Buttons accept `image:`, `image_pressed:`, `image_hover:` and, when `toggle: true`,
`image_checked:`. Each accepts an image provider, an image file or a path,
or `nil` to omit that image. Declaring any image facet makes the button image-only,
even if its current value is `nil`: no label, background, shadow or border is drawn.
Appearance facets (`color:`, `font:`, `accent:`, `background:`, `border:` and
`rounded:`) are ignored. The selected image is centered in `size:` and scaled
proportionally to fit, without stretching, padding, tinting or rounded clipping.
Its own transparency is preserved. The full declared rectangle remains clickable;
`active:`, toggle bindings and handlers continue to work normally.
While active, a supplied pressed image has priority over hover, then checked,
then the default image. An omitted checked image falls back to `image_pressed:`,
then `image:`. If `image_hover:` is omitted or evaluates to `nil`, hovering
multiplies the checked/default fallback image by `#lightgray`, preserving alpha
and leaving the original image untouched. An explicit pressed image still takes
priority. To keep the original hover appearance, supply that image explicitly
as `image_hover:`. Inactive buttons use only their checked/default appearance.
If no image resolves for the current state, the button is invisible but retains
its declared interaction bounds.

Slider limits default to 0 and 100. An omitted step is continuous for float
bindings and 1 for integer bindings; an explicit step must be positive.
Integer bindings also require integer bounds and steps. Steps are measured from
the minimum, with the maximum also reachable.

`ui_radio` displays a label and a switch-style circle on the right: a soft
accent ring with a white inset when unchecked, and a full-accent ring with a
centered accent dot when checked. It requires a writable boolean `var:` and
a non-empty string `group:`. Group names are local to an overlay, not shared
across overlays or displays.

```gaml
ui_radio "Icons" var: use_icons group: "ant_display" at: {12, 12};
ui_radio "Agent states" var: display_state group: "ant_display" at: {12, 52};
```

On release inside a radio, its binding becomes `true` and all other radios in
the same group become `false`, including inactive radios. Clicking the selected
radio does not uncheck it. All bindings are updated before the changed radios'
optional `on_change:` handlers (or final blocks) run; `ui_value` contains each
radio's new boolean value. Unchanged radios do not invoke a handler.
An initial group with no selection is allowed. Initialize at most one binding
to `true`; model-side writes are not automatically normalized. Only radios
present in the overlay's current drawing participate in selection. Use a
different boolean variable for each option; `value:` is not supported.
The radio's outer control is borderless and transparent by default, like a
switch; its circle remains visible independently of `border:` and `background:`.

This first implementation uses left-pointer press/release. Buttons activate
on release inside their bounds; switches and toggle buttons toggle on release inside; sliders
preview while dragging and commit on release, including outside their bounds.
Leaving the display or losing focus cancels capture without committing.
Changing a captured control's bounds, range, value or active state also cancels
the pending change.
Control drags do not pan the display or move the camera. Generic input
listeners still observe the original input stream. No callback runs while
the control is being drawn. Keyboard focus, keyboard activation and
accessibility support are not yet implemented; this is not a replacement for
native accessible application controls.

## Contextual constants

`#display_width` and `#display_height` always return the visible viewport
dimensions in logical pixels, independent of zoom, layer placement and contents.
In graphical scopes, including `event` blocks, they refer to that scope's
display. Outside graphics they refer to the frontmost display, or return `0`
when none exists. They never describe an overlay's local size.

This replaces the historical world-context interpretation as the zoomed world's
pixel extent. Multiplying by `#zoom` scales a viewport length; it does not
necessarily recover that historical extent because aspect-ratio fitting,
letterboxing and camera projection also affect the world.

`#px` remains contextual: it is `1` in a screen evaluation context and converts
a logical pixel to model units in a world context.

For placement facets, the context is the layer's `coordinates:`. For drawing
contents, it is the content mode. Thus a screen-placed world-content mini-map
uses `#px = 1` in its frame facets but a pixel-to-world conversion in
its drawings. Its display dimensions remain viewport dimensions in both.

An `event` block does not inherit a drawing layer's referential. Pair viewport
dimensions with `#mouse_location_in_display` for viewport calculations, and
use `#mouse_location_in_world` for world calculations. Point arithmetic does
not automatically convert one coordinate system to the other.

Constants are contextual **when expressions are evaluated**. A value computed
earlier in a global variable is an ordinary stored number or point; moving it
into a screen layer does not reevaluate its original expression.

Unit metadata is not retained by ordinary numbers or point components.
In particular, `0.5#px` evaluates to `0.5` in a screen context and is then
indistinguishable from a 50-percent positional component. Use unambiguous
pixel values of magnitude at least 1 for screen `at:`, `position:` and
rectangular `size:`. This convention does not provide subpixel absolute
placement through those facets.

### Worked example: numbers are not unit-bearing values

In the units recipe, the reference panel is exactly 400 pixels wide:

| Drawing expression | Local X in pixels |
|---|---|
| `at: {0.5, 58}` | 200 |
| `at: {1, 100}` | 1 |
| `at: {1.0, 142}` | 1 |
| `at: {0.5#px, 184}` | 200 |

The two pairs line up vertically. A rectangle with
`size: {0.2,16}` is 80 by 16 pixels in that panel. By contrast,
`circle(0.5)` has a half-pixel intrinsic radius, not a 50-percent radius.
The separate `size: {1,1}` frame is one pixel wide and high, but its
drawings intentionally extend outside it.

The constants recipe stores `20#px` globally before drawing. That stored
world-context number is not necessarily 20 when subsequently used as a
screen position. Keep contextual expressions in the layer where they are
meant to be evaluated; do not cache pixel-to-world conversions globally
for reuse by displays with different dimensions.

## Mouse coordinates

The new names make their fixed meanings explicit:

| Constant | Meaning | Existing equivalent |
|---|---|---|
| `#mouse_location_in_world` | World/model location | `#user_location`, `#user_location_in_world` |
| `#mouse_location_in_display` | Display-viewport pixel location, from the top-left | `#user_location_in_display` |

The old `#mouse_world_location` and `#mouse_screen_location` names remain aliases
for compatibility; new models should use the consistent `*_location_in_*` names.
Both OpenGL backends normalize native mouse events to logical viewport pixels
using the native surface scale, so mouse positions and overlay hit areas agree
on HiDPI displays. Camera picking still uses physical framebuffer pixels internally.

These constants do not change meaning with `coordinates:`. A screen mouse
location is relative to the viewport, not automatically to an overlay origin.
Subtract the overlay origin before using it as a local drawing location.

Input callback snapshots take precedence. Otherwise a graphical scope reads
its own display surface; scopes without a graphics surface retain the GUI's
last-known mouse fallback. World mouse coordinates in 3D retain the existing
projection convention; they do not become a picked point on arbitrary
geometry.

For the mouse recipe's overlay at `{16,16}`, a viewport pointer at
`{116,76}` has local coordinates `{100,60}`. Panning changes the world
coordinate beneath that pointer, not this subtraction. A click records
both mouse aliases from the same event; all displays show the shared
recorded values, while their live values refer to their own surfaces.
Local coordinates may be negative outside the panel. If reused in a
screen `at:` facet, they still pass through the usual threshold and
opposite-edge rules; the mouse constant does not carry special units.

## Fonts and backend expectations

Font sizes retain their existing nominal typographic meaning:
`font("Helvetica", 18)` is not 18 percent in a screen layer or 18 world units
in a world layer. Font objects remain reusable between contexts. The actual
glyph box is not the nominal size. Perspective text, world scaling, font
metrics, HiDPI and backend-specific rendering remain distinct concerns; this
change does not introduce a new font-unit system or promise pixel-identical
glyphs across renderers.

The text recipe draws the same `font("SansSerif",16)` in world and screen
contexts and places nine anchor labels around red reference dots. The dot
identifies the requested `at:` position; the anchor identifies which point
of the text box meets it. An anchor such as `#bottom_right` is not a
layer-placement anchor and does not change the panel's position. A
fractional `at:` can move a label when a panel resizes without changing
the font's nominal size.

Perspective text anchors use the visible
glyph-outline bounds, including italic bearings and descenders. Leading and
trailing whitespace do not enlarge those bounds. Text rotation in Java2D
now uses the requested anchor position as its pivot, as in OpenGL, rather
than the center of the text rectangle. These rules also apply when the
coordinate mode is selected by default.

Multiline text uses font line metrics for the distance between lines, not
the number of world units in a glyph's pixel height. Empty lines consume
one line step, and both LF and CRLF line endings are supported. Each line
uses the requested anchor independently; this is not paragraph/block
alignment. Rendering does not change the supplied `at:` point. A screen
frame therefore cannot accidentally change the placement of a later draw
which reuses the same point.

### Text parity baseline

The following describes implemented paths, not completed visual acceptance:

| Feature | Java2D | OpenGL | OpenGL4 |
|---|---|---|---|
| Perspective-text anchors | Visible AWT glyph bounds | Visible AWT outline bounds | Graph glyph bounds for plain flat text; AWT bounds for decorated/extruded text |
| Multiline spacing | Font line height in display pixels | Same nominal line height converted to model units | Shared OpenGL scene-building path |
| Border and wireframe | Glyph outline | AWT outline | AWT outline for perspective text, instead of silently drawing a filled graph glyph |
| Extrusion | Flat text; depth has no effect | AWT outline for caps and sides | The same AWT outline for caps and sides; GLU resolves separate letters and holes |
| Non-perspective labels | Same 2D text path | Historical GLUT bitmap path: restricted fonts/sizes and no general anchor support | Graph-font billboard path |
| Font fallback | AWT/system | AWT/system, except GLUT bitmaps | System/bundled graph fonts for plain flat text; AWT for outline text |

Font substitution can still make plain and decorated OpenGL4 labels differ.
OpenGL's bitmap labels, complex-script shaping, HiDPI rasterization and
paragraph alignment are not claimed to have full parity. The text experiment in
`OpenGL Comparison - Rendering.gaml` exposes the outline and multiline changes without
requiring renderer-specific sizes in the model.

The coordinate pipeline is shared by Java2D, OpenGL and OpenGL4, including image
rendering. Visual acceptance should compare their behavior under resizing,
zoom, panning, HiDPI and inherited displays. Screen layer rotation remains
unsupported as described above.

In the overview recipe, the main grey world should move with the camera.
The HUD, mini-map frame and chart must remain at their viewport positions.
The two blue markers represent the same world location, with different
scales. The mini-map's uniform fit can leave white space below or beside its
square world when its frame is not square. The chart plots X in model units
from 0 to 100, not an unbounded cycle counter.

Screen rendering uses an independent orthographic projection and identity
model-view matrix; both stacks are restored before the next layer. OpenGL
chart textures retain the frame's aspect ratio when applying the chart-quality
resolution setting, rather than stretching a square chart into a rectangle.

## Library and wiki migration

The eight recipes above demonstrate the explicit contract. Existing models
have also been reviewed across the main library, extension models and the
historical `gama.library/recipes` directory. Every existing overlay now has
an explicit placement mode. Top-level layers with a `position:` or `size:`
facet have an explicit mode as well, except for the recipes intentionally
demonstrating omitted facets. Unpositioned declarations use the
world/screen defaults above; their omitted sizes fill the reference extent.

### Dedicated OpenGL/OpenGL4 comparisons

The `Display Coordinates*.gaml` tutorials compare Java2D with OpenGL. OpenGL4
comparisons are grouped separately in `Recipes/Visualization/models`:

| Model | Experiments |
|---|---|
| `OpenGL Comparison - Coordinates.gaml` | World/HUD/mini-map/chart overview; pixels, fractions and world-content fitting; threshold and contextual constants; grid/species/agents screen frames; image/mesh/chart frames; nine text anchors and pointer events; world overlay and omitted frame sizes; full-viewport chart |
| `OpenGL Comparison - Rendering.gaml` | Multiline text and italic bearings; holes, concave polygons, depth and transparency; lighting and textures; the wide-grid regression and moving agents; elevated mesh and image orientation; fixed/relative chart raster quality; separate world extrusion and billboard labels |
| `OpenGL Comparison - Ants.gaml` | Imported Ant Foraging classic, SVG/HUD and textured-terrain/3D-ant experiments, each with an OpenGL4 sibling |

Each comparison experiment launches only an OpenGL/OpenGL4 pair. The OpenGL4
display inherits the OpenGL scene instead of duplicating its layers or data.
The ant comparison imports the existing demo rather than copying its behavior
or assets; the original demo uses OpenGL. The standalone Grid Rendering recipe
remains an OpenGL regression example; its paired counterpart is in the rendering
comparison model.

Experiment-panel text explains the purpose, actions to try, expected results,
minimum viewport size for fixed panels, and known renderer differences.
Use equally sized viewports and matching camera angles before judging parity.
These are shared-simulation visual comparisons, not performance benchmarks.
They do not promise identical font substitution, transparency ordering or
photometric shading, and do not replace visual acceptance.

The only positioned nested species declaration, `prey_in_shelter` in
`Predators, Preys and Shelters.gaml`, intentionally inherits its host's world
context; independent nested modes are unsupported. The placement recipe
demonstrates that default graphics match explicit world graphics and default
overlays match screen graphics, with no third interpretation.

### Decisions for existing models

| Models | Interpretation and migration |
|---|---|
| Circle, field of vision, Sugarscape, Schelling variants, flood, learning, multi-level demos and physics recipes | Pixel legends become `coordinates: #screen`; numeric frame distances are pixels. Existing `#px` drawing expressions remain valid because the unit is now 1. |
| Traffic and Pollution, Clean Line Network, ABValuation, generalized Lotka-Volterra | Small or zero-size overlay frames deliberately allow text and legends to overflow. Explicit screen placement preserves that choice; there is no new clipping or automatic resizing of the frame to its contents. |
| Evacuation Phuc Xa, Multi-simulation and physics recipes | Bare overlay positions `{5,5}` are clarified as a five-pixel HUD margin, not a five-world-unit displacement. Evacuation's map description remains a separate world drawing. |
| Schelling charts, Vote, Soccer, integration-step comparison, chart recipes, predator-prey and road-traffic tutorials, ODE solver comparison | Split charts use screen fractions. Historical full-width `1`/`1.0` becomes `#display_width`, not one pixel. Chart data and axis coordinates are unchanged. |
| Boids 3D Analysis | Both mini-maps use screen placement and a world referential. Their frame occupies one third of each viewport dimension; drawings use the original XY location rather than `location / 3`. Symbol size compensates for the frame scale. Mini-map symbols are flat plan-view triangles, not extruded 3D objects. |
| Ant Foraging | The inset terrain, trails and ants remain camera-attached world layers. Historical 5-percent offsets and 90-percent sizes are expressed as fractions of `world.shape.width`/`height`; the legend alone is screen-based. |
| Moving3D, MODAVI, graph-from-bug demos | Side-by-side views, spatial displacements and Z ordering remain world-based. The migration does not flatten them into HUDs. |
| Tetris, Pacman, Flappy Bird | Scoreboards and text compositions remain attached to their world layouts. Fractional frame sizes are converted to world extents; Tetris's historical height of `1` is written explicitly as the full world height to preserve its old interpretation. |
| Grid DEM, Building Heatmap, GIF File Loading | Terrain and image/agent depth ordering remain spatial. DEM frame XY sizes are world extents, while existing Z scale factors and offsets are retained. |
| Switch And Match, Color Brewer | Images, result symbols and palette swatches form world-coordinate compositions, not viewport HUDs. The 20-percent image frames are expressed in world units. |
| Clustering extension | The headings are centered in a full-width 50-pixel screen band, replacing the former mixture of a half-world offset, a pixel frame and world-valued text positions. |
| Units And Constants | The HUD demonstrates viewport dimensions and `#px = 1` in screen mode; the separate mouse/camera drawing remains world-based. Its comments no longer describe screen dimensions as zoom-dependent. |

Two details need particular care when adapting other models. First, moving
the `{5,5}` HUD margin into pixels and centering the clustering headings are
intentional clarifications of layout, not numerical equivalences to their
old model-unit expressions. Second, ABValuation's continuously movable
slider indicators can have an absolute X position strictly between 0 and 1.
They use geometry `at_location` with no drawing `at:` facet, retaining
subpixel geometry coordinates without interpreting them as fractions.
Other drawings retain their existing unit expressions and font choices.

Migrate intent, not syntax mechanically:

1. Classify each layer's placement and contents separately.
2. Replace historical full-extent components of `1` with an omitted size or
   explicit contextual display dimensions.
3. Keep world-agent positions in model units; remove pixel-to-world
   conversions only from genuinely screen-space expressions.
4. Review stored `#px` values, negative offsets and intentional overflow.
5. Compare inherited displays independently; do not use the frontmost display
   as the source for viewport dimensions or mouse positions.

For a genuinely pixel-based historical HUD, the migration is:

```gaml
// Historical source only: there is no compatibility mode for the old interpretation.
overlay position: {0.02,0.02} size: {0.96,100#px} {
    draw "Status" at: {12#px,26#px};
}

// After: fractional viewport placement, pixel height and local pixel drawings.
overlay coordinates: #screen position: {0.02,0.02} size: {0.96,100} {
    draw "Status" at: {12,26};
}
```

Do not make the same replacement for a mini-map drawing agent `location`
values: select `referential: #world` instead. Review hand-written factors
such as `location / 3` together with the new frame scale, rather than
scaling twice. Likewise, keep a map and its hover calculations in world
coordinates when moving only its status panel into screen coordinates.
Intentional overflow, world-depth drawings and rounded-frame behavior
need explicit review before migrating an existing overlay.

The display-construction wiki needs coordinated updates to the layer overview,
overlays, charts, graphical aspects, positioning/scaling, display constants,
mouse events, text/fonts and migration examples. This document is the
in-repository reference for that update; publishing the external wiki is a
separate step. Future overlay controls and callback semantics are not included
in this first coordinate-system change.

| Wiki topic | Required change and example |
|---|---|
| Display/layer overview | Separate the viewport, the layer frame and drawing contents; start with the overview and placement recipes |
| Position and size facets | Document layer-type defaults, the breaking removal of hybrid values, the strict screen threshold, negative origins and omitted sizes; use placement and units |
| Overlays and graphics | Present graphics as the world's aspect by default and a screen information layer when requested; explain `referential:`, proportional fit versus per-axis scaling, overflow and the one-overlay limit; use the world/status example above and referentials |
| Spatial layers and charts | Keep agent locations and mesh/grid data in world coordinates while placing their frame on screen; use layers |
| Display constants | Show evaluation-time context and stored-value pitfalls; use constants |
| Mouse events | Document both explicit aliases, source-display lookup, callback snapshots and local subtraction; use mouse |
| Text and graphical aspects | Keep fonts separate from coordinate units and explain drawing anchors; use text |
| Migration guide | Publish the before/after HUD example, full-size replacement rule and mini-map precautions above |

The deliberately unsupported combinations described in this reference remain
unsupported; the recipes are not a promise of arbitrary nested coordinate
transforms, screen-layer rotation or new interaction widgets.

## Renderer limitations and reduction roadmap

Compilation of a model is not evidence that its layout renders correctly.
The following work separates renderer defects from deliberately unsupported
coordinate combinations. Visual acceptance remains necessary on Java2D,
OpenGL and OpenGL4; a successful Java build does not replace it.

### Immediate corrections

Screen layers restore the incoming OpenGL depth-test state, as well as both
matrix stacks. In particular, a final overlay must not disable depth testing
for the camera's rotation globe and expose all its back-facing wire lines.
The globe is shown only during an unlocked CMD/CTRL + left-button interaction;
mouse release, canvas exit and focus loss clear the interaction, even if a
modifier-key release was delivered outside the display.

Legacy OpenGL screen-layer picking retains the cursor's selection volume
when replacing the world projection. OpenGL4 screen layers keep their normal
screen projection for colour-buffer picking at the cursor. Screen projection
flattens final depth without flattening the objects' transforms, so elevated
agents and extruded drawings are not cut off at Z = -1 or Z = 1.

Frame backgrounds already include layer transparency; the renderers apply
it once, independently of the preceding layer's opacity. Contents retain
their own colour alpha and layer opacity. Java2D applies the same rendering
hints to the world and screen renderers, including image interpolation.
Its text path uses layout-aware shaping and bidirectional ordering while
retaining visual-bound anchors, rotations, empty multiline rows and outlines.

Java2D previously ignored `size:` when drawing a geometry, including a
geometry obtained from an SVG. A large native SVG consequently stayed large
even when an aspect requested `{5,7}`. The geometry draw delegate now fits
the geometry uniformly into the requested XY bounds, before rotation and
translation, without resizing the stored source geometry. This follows the
existing OpenGL geometry-fitting rule. It is not a change to `#px`, nor a
reason to shrink the Ant Foraging model's requested dimensions.

OpenGL4's graph-font path previously updated the general OpenGL matrix stack
instead of the font renderer's own matrices, and calculated a font-size
factor without applying it. Font rendering now uses its own projection,
model-view, translation, scale and glyph-bound anchor offsets. Billboard
labels project their position through the layer and camera before drawing
in screen space; the internal viewport-text entry point keeps its direct
bottom-left pixel coordinates. Extruded faces reuse the already positioned
outer transform instead of applying location and anchor twice. Font-cache
keys also distinguish the screen and world fallback families.

The graph-font path also owns a core-profile vertex-array object. Without
a bound VAO, text could fail with `GL_INVALID_OPERATION`. Graph text now
uses single-pass rendering into the active scene framebuffer, relying on
the display's existing multisampling for edge smoothing. JOGL's two-pass
text mode composites into the context's default framebuffer, which loses
text when a different scene target is active; restoring bindings afterward
does not repair that misplaced draw. Surrounding rendering state is
restored after text drawing, including on failure.

The OpenGL bundle exports `com.jogamp.math.geom` so that OpenGL4 can access
the glyph bounds (`AABBox`) under OSGi. A missing export aborts scene
rendering at the first text object even though standalone classpath probes
can load the same class. Bounded text diagnostics can be enabled with the
VM argument `-Dgama.opengl4.debug.text=true`; they are disabled by default.

The follow-up text pass now renders OpenGL4 extruded caps and sides from
the same AWT outlines, rather than combining graph-font caps with potentially
different AWT sides. GLU replaces the one-outer-ring tessellation, so
disconnected glyphs, dots and holes are no longer treated as holes inside
the largest letter. Side-normal buffers include every contour vertex,
reset at each contour, and correctly handle negative coordinates. The
same path also handles perspective borders, textures and wireframes.

These are shared rendering fixes, not per-model compensations. Visual-bound
anchors and Java2D outline decorations now apply to both written and default
coordinate modes. Their visual acceptance is still pending.

### Simplification after removing hybrid coordinates

The shared frame resolver now has two paths only. Neither construction nor
rendering inspects whether an expression happens to contain `#px`. Overlay
data no longer captures a pre-evaluation pixel ratio or stores per-axis pixel
flags. OpenGL consumes the already evaluated layer position and size instead
of evaluating position and size expressions again while preparing a frame.
This also prevents side-effecting or random facet expressions from producing
a different placement during rendering.

Unrotated world layers skip the two pivot translations and rotation matrix
operation. Undecorated world frames skip matrix setup entirely; decorated
frames evaluate background color once. Unrotated Java2D text skips transform
copies and transformed-shape allocation. These are reductions in work and
allocation, not a measured frame-rate claim; no timing-sensitive cache is
introduced, so resizing, zoom and dynamic facets remain live.

### Ordered follow-up work

| Priority / dependency | Scope | Acceptance criterion and compatibility boundary |
|---|---|---|
| 1. Immediate acceptance | Check Ant Foraging in Java2D and the text, placement and referential recipes in all three backends. Include text before and after textured objects, screen HUDs with nonzero origins, world labels and extruded text. | Ant SVGs fit the requested bounds before rotation; text is visible, correctly oriented and anchored; subsequent objects render normally. Also resize, zoom and pan. Do not mark visual defects closed on compilation alone. |
| 2. After priority 1 | Establish a renderer parity table for geometry/SVG, raster images, text and exports. Exercise rotations, anchors, `size:` and inherited non-square frames. | Document intentional differences: geometry fitting is proportional, whereas image dimensions can specify an exact rectangle. Do not silently change either contract to obtain identical pictures. Measure resulting bounds, not only expression values. Preserve source assets between repeated draws. |
| 3. After the parity table | Align text metrics and HiDPI handling. Cover font substitution, multiline baselines, spaces, accents, nine anchors, extrusion and perspective versus billboard text. | Distinguish nominal font size, glyph bounds, logical pixels and framebuffer pixels. Keep placement stable across display scales and exports; document fallback fonts and tolerances rather than promising pixel-identical rasterization. Match extruded face and side outlines before claiming complete extrusion parity. |
| 4. After frame parity | Consolidate forward and inverse frame transforms for drawing and mouse interaction. Cover screen/world overlays, uniform mini-map fit, per-axis spatial-layer scaling and independent displays. | A known layer-local point mapped to the display and back must round-trip within a stated numeric tolerance, including after resize, zoom and pan. Events must use the originating display, never whichever display is frontmost. Existing world-mouse aliases retain their meaning. |
| 5. After shared inverse transforms | Evaluate screen-layer rotation, independent nested-species frames and world-placed/screen-content combinations separately. | Specify the pivot, inheritance, pixel sizing and picking semantics before enabling each combination. Retain the current validation errors until both rendering and inverse interaction agree. No broad removal of validation restrictions. |
| 6. Independent design, implementation after frame parity | Consider opt-in frame clipping and multiple overlays. | Preserve today's intentional overflow and single-overlay models. Clipping must affect rendering and interaction consistently, including rounded frames and exports; explicit stacking must not change existing depth order. These features require separate API decisions, not a coordinate-mode workaround. |
| 7. After priorities 1-4 | Introduce the first overlay controls and finish the display-construction wiki migration. | Buttons, sliders and switches use the same local screen coordinates, font rules and inverse mouse transform as drawings. Define callbacks and variable binding separately. Publish the reference and backend limitations together with the recipes; do not make controls depend on every optional feature in priorities 5-6. |

Priorities 2-4 should reduce inconsistencies before adding new coordinate
combinations. Priorities 5-6 are possible extensions, not commitments or
prerequisites for a useful first set of overlay controls.

# Explicit display-layer coordinates

## Compatibility and defaults

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

**Omitting `coordinates:` preserves the historical behavior.** It does not mean
`#world`. Historical layer placement mixes fractions, model units and `#px`,
and overlays have additional historical rules. Existing models are not
automatically migrated.

There is deliberately no new implicit default in this first implementation.
For new declarations, explicitly use `#world` for spatial layers and `#screen`
for HUDs and charts. A later change of implicit defaults requires a separate
compatibility decision and library migration.

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

## World placement

In explicit `coordinates: #world`, every X/Y component of `position:` and
`size:` is a model-unit value. There is no fraction threshold, and a negative
position remains a negative world displacement, not an opposite-edge offset.
For example, `{0.5, 1}` means half a world unit and one world unit.

An omitted size covers the environment's world extent. Spatial contents retain
their normal model coordinates, camera transforms and renderer-specific 3D
behavior. An explicitly world-positioned overlay is a spatial graphics layer:
unlike a traditional overlay, it follows the world camera.

## Placement versus contents

`coordinates:` describes the layer rectangle. For `graphics` and `overlay`,
`referential:` can independently select the interpretation of drawings:

```gaml
overlay coordinates: #screen referential: #world
    position: {0.72, 16} size: {0.25, 0.25} {
    draw circle(10) at: {50, 50};
}
```

With explicit coordinates, graphics and overlays default their contents to
the placement mode. A screen overlay therefore defaults to screen contents;
an explicit world overlay defaults to world contents.

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

`referential:` requires explicit `coordinates:`. World placement with
screen contents is not supported in this first implementation. Neither are
nonzero rotations of screen layers; rotate individual drawings instead.
Nested species layers inherit the host layer's context and cannot specify an
independent placement mode.

## Contextual constants

In a screen evaluation context:

* `#px` is `1`.
* `#display_width` and `#display_height` are the visible viewport dimensions.

For placement facets, the context is the layer's `coordinates:`. For drawing
contents, it is the content mode. Thus a screen-placed world-content mini-map
uses screen constants in its frame facets but historical world constants in
its drawings.

Outside a screen context, these constants retain their historical values:
`#px` converts a logical pixel to model units, and display dimensions refer to
the pixel extent of the displayed world, which can differ from the viewport
under zoom.

Constants are contextual **when expressions are evaluated**. A value computed
earlier in a global variable is an ordinary stored number or point; moving it
into a screen layer does not reevaluate its original expression.

Unit metadata is not retained by ordinary numbers or point components.
In particular, `0.5#px` evaluates to `0.5` in a screen context and is then
indistinguishable from a 50-percent positional component. Use unambiguous
pixel values of magnitude at least 1 for screen `at:`, `position:` and
rectangular `size:`. This convention does not provide subpixel absolute
placement through those facets.

## Mouse coordinates

The new names make their fixed meanings explicit:

| Constant | Meaning | Existing equivalent |
|---|---|---|
| `#mouse_world_location` | World/model location | `#user_location`, `#user_location_in_world` |
| `#mouse_screen_location` | Display-viewport pixel location | `#user_location_in_display` |

These constants do not change meaning with `coordinates:`. A screen mouse
location is relative to the viewport, not automatically to an overlay origin.
Subtract the overlay origin before using it as a local drawing location.

Input callback snapshots take precedence. Otherwise a graphical scope reads
its own display surface; scopes without a graphics surface retain the GUI's
last-known mouse fallback. World mouse coordinates in 3D retain the existing
projection convention; they do not become a picked point on arbitrary
geometry.

## Fonts and backend expectations

Font sizes retain their existing nominal typographic meaning:
`font("Helvetica", 18)` is not 18 percent in a screen layer or 18 world units
in a world layer. Font objects remain reusable between contexts. The actual
glyph box is not the nominal size. Perspective text, world scaling, font
metrics, HiDPI and backend-specific rendering remain distinct concerns; this
change does not introduce a new font-unit system or promise pixel-identical
glyphs across renderers.

The coordinate pipeline is shared by Java2D, OpenGL and OpenGL4, including image
rendering. Visual acceptance should compare their behavior under resizing,
zoom, panning, HiDPI and inherited displays. Screen layer rotation remains
unsupported as described above.

## Library and wiki migration

The `Display Coordinates.gaml` recipe demonstrates explicit world graphics,
screen contents, a world-content mini-map and a screen chart in inherited
Java2D/OpenGL displays. `Legend - Overlay.gaml` is migrated to an explicit
screen overlay. Other library models keep their historical declarations.

Migrate intent, not syntax mechanically:

1. Classify each layer's placement and contents separately.
2. Replace historical full-extent components of `1` with an omitted size or
   explicit contextual display dimensions.
3. Keep world-agent positions in model units; remove pixel-to-world
   conversions only from genuinely screen-space expressions.
4. Review stored `#px` values, negative offsets and intentional overflow.
5. Compare inherited displays independently; do not use the frontmost display
   as the source for viewport dimensions or mouse positions.

The display-construction wiki needs coordinated updates to the layer overview,
overlays, charts, graphical aspects, positioning/scaling, display constants,
mouse events, text/fonts and migration examples. This document is the
in-repository reference for that update; publishing the external wiki is a
separate step. Future overlay controls and callback semantics are not included
in this first coordinate-system change.

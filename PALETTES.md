# Vintage color palettes

The Swiss-balanced splash artwork inspires three optional families of eight
categorical inks. **Swiss** is the default scheme for simulation identities in
the UI and automatically colored chart series. Existing saved preferences and
explicit model colors are retained.

| Name | Character | Colors, in order (hex RGB) |
|---|---|---|
| Swiss | Scientific poster: blue, vermilion, olive and ochre | `28516a c75d36 70805a d7ab53 8c6584 4f9290 9a432e 77756b` |
| Warm Amber | Sun-warmed paper, terracotta and muted blue | `426e85 ce784b a28432 797b4c 9f4f40 659591 996c83 645951` |
| Mineral | Cooler slate, teal and dusty rose | `456d86 61948c a96b53 8a789b 9a8b48 526955 a65e73 6a737b` |

These are **categorical**, not sequential or diverging scales: position does not
encode magnitude or a midpoint. For quantitative maps, use an appropriate
ColorBrewer sequential/diverging palette instead. The eight colors cycle when
used for more than eight simulations or automatically colored chart series.
They are not certified color-vision-deficiency-safe; keep meaningful labels,
shapes or line styles and check contrast against the actual display background.

## Preferences and defaults

- **Interface > Simulations > Default color scheme for simulations in UI**:
  Swiss, Warm Amber and Mineral appear alongside Vivid, Diverging, Qualitative
  and the other existing schemes.
- **Displays > Default Chart Properties > Default color palette for chart
  series**: the same three palettes are available. An explicit `color:` on a
  chart's `data` continues to take precedence.
- **Displays > Default Rendering Properties**: the default agent fill is Swiss
  blue (`#28516a`), and the default highlight is burnt orange (`#c75d36`).
  Species/drawing colors and saved rendering preferences are not overwritten.

These defaults apply to unset preferences or after restoring defaults; GAMA
does not migrate an existing selected scheme. Select Swiss explicitly to try it
in an existing workspace. The default simulation and chart background is now
Box2D's cream paper (`#f8f6f0`), also used by the static splash artwork.
Saved display/chart preferences and explicit model backgrounds still take precedence.

## UI backgrounds

**Theme > UI > Use custom theme backgrounds (requires restart)** is off by
default: **System** retains the original Eclipse/SWT backgrounds. Enable
**Custom** to choose separate light and dark UI backgrounds. The initial choices
are cream paper (`#f8f6f0`) and dark charcoal (`#262b2a`). The switch and both
colors require restarting GAMA; saving does not partially recolor the running UI.
Switch back to System and restart to remove the override.

The override covers workbench surfaces, Home/launcher, parameter/preferences
panels, console backgrounds and documentation popups. It does not recolor the
static splash image, explicitly colored models or the independently configured
editor palette. Default display/chart backgrounds remain separate rendering
preferences in **Displays**, not UI theme settings.

## Using the inks in GAML

`vintage_colors(name)` returns a fresh, modifiable palette of eight `rgb` values.
Names are case-sensitive: `"Swiss"`, `"Warm Amber"` and `"Mineral"`.
An unknown name raises a runtime error; these names are not ColorBrewer palettes
and are not passed to `brewer_colors`.

```gaml
global {
    list<rgb> inks <- vintage_colors("Swiss");
    rgb paper <- rgb(248, 246, 240);
}

species particle {
    aspect default {
        draw circle(1) color: inks[0] border: rgb(36, 40, 35);
    }
}

experiment Poster type: gui {
    output {
        display Scene background: paper {
            species particle;
        }
    }
}
```

For a complete composition, use cream paper `#f8f6f0`, charcoal text/axes
`#39423d`, restrained borders, and a few categorical inks. Avoid artificially
fading text to imitate printing: the vintage feel should not cost legibility.

## Editor configurations

**Theme > Syntax highlighting configurations > Configuration** offers Swiss,
Warm Amber and Mineral alongside **GAMA 2026** and **GAMA 2025**. Each includes
both light and dark variants, keeping the usual bold keywords/types and italic
comments/reserved words. Choose a configuration and save preferences to apply
it; the active configuration is not changed automatically.

Swiss emphasizes blue keywords and terracotta facets; Warm Amber emphasizes
terracotta keywords and ochre numbers; Mineral uses slate keywords, teal calls
and dusty-violet types. Each palette now includes a background for **both** light
and dark themes, editable under **Theme > Syntax coloring (light/dark theme) >
Background**. Background changes apply on saving, without restarting, alongside
the syntax colors and styles. They are independent of the UI background switch.

GAMA 2026 and GAMA 2025 retain white light backgrounds and dark gray companions.
The three vintage palettes use cream paper in light mode, with charcoal (Swiss),
warm brown-charcoal (Warm Amber) or blue-charcoal (Mineral) dark backgrounds.
An existing single editor background is carried over to the active theme when
the new per-theme preferences are first created.

Configuration import/export includes both backgrounds in format version 2.
Version 1 files still load with white/light and charcoal/dark backgrounds;
malformed version 2 backgrounds are rejected. Existing per-category customization
and configuration import/export remain available.

The editable source files live in `gama.ui.shared/syntax/`.

## First library examples

Three demos use the Swiss paper-and-ink treatment without changing their
simulation rules, parameters, random draws or output data:

- `Demos/Life/Life Using Grid Agents.gaml`: paper dead cells, blue surviving
  cells, ochre births and terracotta deaths; all four colors remain parameters.
- `Demos/Epidemiology/models/Susceptible Infected Recovered (SIR).gaml`:
  consistent olive susceptible, terracotta infected and blue immune agents
  and chart series, with a lightly ruled paper grid.
- `Demos/Predator Prey/Lotka-Volterra (Simple).gaml`: blue/terracotta time series
  and a blue phase trajectory with a terracotta equilibrium marker.

# GAMA 2026 — Migration Guide

> **Who is this for?** Model authors, plugin developers and contributors upgrading to GAMA 2026.  
> **What's covered?** Every breaking change in Java APIs, GAML syntax, and architecture — with before/after examples throughout.

---

## Quick Navigation

| Section | Topic |
|---|---|
| [Part 0](#part-0-changelog-at-a-glance) | Changelog at a glance |
| [Part 1](#part-1-whats-new-and-why-it-matters) | What's new and why it matters |
| [Part 2](#part-2-migration-reference) | Step-by-step migration reference |
| [Presentation variables](#presentation-variables-in-experiments-migrate-to-output-locals) | Output locals, ownership, lifetime and inheritance |

---

# Part 0: Changelog at a Glance

A high-level summary of every area that changed. Use this to quickly assess what affects your plugin.

## Core Technology & Architecture

| Change | Impact |
|---|---|
| Upgraded to **JDK 25** and **Eclipse 2026-03** | Rebuild required; modern Java syntax now available |
| New **`gama.api`** module | Public interfaces decoupled from `gama.core` implementation |
| **Factory pattern** enforced for all core types | `new GamaList()`, `new GamaPoint()` etc. are gone |
| **`Cast` utility class** reduced | Casting delegated to individual `IType` implementations |
| **`AbstractPopulation`** centralises population logic | `createAgentAt` renamed to `createAgentAtIndex` |
| **`IAgent`** now extends `IDelegatingShape` | Attribute mutation via containers is restricted |
| **Apache Fory** replaces FST for binary serialization | FST binary saves must be regenerated; no legacy reader is provided |
| **`IType.NEW_TEMP_ID` removed** | Use `@facet(declares_new_temp = true)` and explicit variable type metadata |
| Expanded built-in **`gama` platform agent** | Additive GAML actions; no migration is required unless you choose to use them |

### Binary serialization compatibility

GAMA 2026 uses Apache Fory exclusively for binary serialization and experiment recording. Existing
FST-produced simulation files and serialized objects cannot be read. Regenerate them from their source
models using GAMA 2026. If their state must be retained, export the necessary model data in a portable
format from the older GAMA version before upgrading.

Remove uses of `gama.pref_binary_serializer` from models and `-Dgama.binary.serializer` from launch
arguments. Backend selection and the `gama.extension.serialize.fst` Java packages have been removed.
Fory is supplied by `gama.dependencies`; plugins must not embed a second copy.

## GAML Language

| Change | Impact |
|---|---|
| New **`class`** and **`object`** keywords | New OOP constructs available in models |
| Rewritten Xtext parser & AST | Faster compilation; some syntax constructs changed |
| Arrow braces removed | `-> { ... }` becomes `-> ...` |
| `with:` argument list syntax changed | `[a::1]` becomes `(a:1)` |
| `diffuse var:` and `transition to:` removed | Use `diffuse` and `transition` directly |
| `image` layer renamed to `picture` | Update display blocks |
| Displays/experiments need explicit `title:` | Identifier and UI label are now separate |
| `overlay` position/size unified in 2D and 3D | Contents drawn in model units may need `#px` |
| Display dimensions always mean logical viewport pixels | Review former zoom-dependent world-extent calculations; prefer explicit mouse-coordinate aliases |
| Optional OpenGL map ordering | Add `map_order: true` to suitable flat displays or enable the rendering preference |
| OBJ geometry buffers contain one polygon per face | Review models that relied on the former incomplete intermediate polygons |
| Chart bodies re-evaluated at every step | Dynamic legends create new series; duplicate legends no longer merge |
| Explicit `nil` for lists and maps is preserved | Use `[]` or `map([])` when an empty container is intended |
| Grid `collect` preserves spatial structure | Use `list(cell) collect expression` when a flat list is required |
| Grid attribute brackets are typed matrix reads and writes | Replace `matrix_with(cell, "attribute")` with `cell[attribute]` and `set_values` with `cell[attribute] <- matrix;` |
| Attribute brackets also support ordinary species | `species[attribute]` reads a typed list; assigning a list requires exactly one element per current agent |
| Built-in action arguments check container content/key types | Declare spatial networks as `graph<geometry, geometry>` rather than bare `graph`; review list/map argument types too |
| Explicit output owners and persistent presentation locals (draft PR) | Keep model/controller state as attributes; move output-only styling into `output` instead of relying on implicit experiment lookup |

## Engine, UI, and Graphics

| Change | Impact |
|---|---|
| New **OpenGL 4.1** display plugin (`gama.ui.display.opengl4`) | Modern shader-based rendering; legacy OpenGL 2 pipeline removed |
| New **launching overlay** | Visual improvement only, no code changes needed |
| Major model-editing addition: **Previous versions** | Browse, compare and restore saved workspace versions; no GAML migration required |
| Improved **concurrency** support | Internal threading improved; some scheduling APIs changed |
| **Copilot integration extracted** to `gama.copilot` | Install from its dedicated p2 site; no Copilot feature/module in the core reactor |

---

# Part 1: What's New and Why It Matters

## Major addition to model editing: Previous versions

GAMA now brings saved model versions directly into the editing workflow. The dedicated **Previous versions**
view lets you revisit an earlier implementation, compare it with the current file, or restore it without
leaving the workbench or managing manual copies.

Open **Previous versions...** from a workspace file's navigator context menu. In a GAML editor, open the
top-right options menu and choose **Show previous versions**, immediately after **Show/Hide minimap**.
The same entry becomes **Hide previous versions** while the view is open.

Versions are always grouped by date, and the view follows the selected workspace file or active editor.
It refreshes automatically at opening, when a workspace editor becomes prominent, and when the file is saved
or otherwise changes. Opening a revision or comparison editor does not redirect the history being followed.
The compact view has no toolbar, repeated filename or column heading; its clock, calendar and version icons
match GAMA's light and dark themes.

Open a version to compare it with the current file, or use its context menu:

- **Open in text editor** opens the version separately for inspection.
- **Compare with current** displays the differences against the current workspace file.
- **Replace current** restores the selected version after overwrite confirmation, preserving the replaced
  contents in local history. This action is not offered for the current revision itself.

**No model migration is needed.** The former **Local history...** navigator item is simply renamed.
History is unlimited by application default: Eclipse's age, count and file-size retention limits are disabled,
but explicit workspace retention preferences still take precedence. Disk usage can therefore grow.
This feature uses the versions retained in the current workspace, not Git commits or an external backup;
it cannot recover versions already discarded. A file with no saved history can still open the view.
See the [changelog](CHANGELOG_2026.md#major-addition-previous-versions-for-model-editing) for the complete addition.

## Platform-agent operations

The built-in `gama` agent can now act as a small automation and diagnostics interface, including from the interactive
console when an experiment is active. Its additive actions inspect preferences, change or reset them, open model
experiments, run test experiments, validate workspace models or GAML snippets, and launch `.gamx` playable packages.
See the [platform-agent guide](https://github.com/gama-platform/gama/wiki/PlatformAgent) for GAML examples, execution
requirements, and side effects.

No existing models or Java plug-ins need to change. Preference updates are in-memory by default and restore when the
active experiment closes; pass `persist: true` only when the preference should remain changed afterwards. Opening an
experiment confirms replacement of an active experiment by default; `confirm: false` deliberately bypasses that
confirmation for scripted chaining. Model validation and test execution are synchronous actions and may take time in a
large workspace or model.

## Optional Copilot integration and pre-workbench initialization

GitHub Copilot remains optional, but its wrapper feature, integration plug-in,
dependency documentation and publishing workflow now belong to
[gama.copilot](https://github.com/gama-platform/gama.copilot). Packaged GAMA installs
`gama.feature.copilot` through **Support > Install GAMA extensions... > Install Copilot extension**, using the dedicated
p2 site <https://gama-platform.org/gama.copilot/>; use **Support > Check for updates...**
after installation. Review the normal license and trust prompts. GitHub sign-in and
Copilot entitlement are still handled by the upstream extension.
The dedicated site contains only GAMA's wrapper and integration plug-in.
p2 selects a compatible Copilot version from Microsoft's repository; platform
dependencies remain on **Eclipse 2026-03**. Installation requires access to all
three sites, and a newer Copilot requiring a newer Eclipse baseline cannot be
installed merely because it is the latest upstream release.

**Build migration:** remove references to the former core `gama.feature.copilot`
directory and `-Pcopilot` profile from custom build scripts. Build the standalone
repository using its documented Maven procedure. Custom products that explicitly
include `gama.feature.copilot` must resolve it from the dedicated p2 site rather
than expecting a feature module in the core reactor. Ordinary GAMA builds have no
Copilot dependency. Generic GAML editor support for external providers remains in
the core repository; report integration-specific issues in the standalone tracker.

**Optional integration API:** `gama.ui.application.startup` accepts contributions
of the following form:

```xml
<extension point="gama.ui.application.startup">
   <startup class="example.integration.StartupPreferences"/>
</extension>
```

The class must implement `java.lang.Runnable` and have a public no-argument
constructor. GAMA invokes it synchronously on the UI thread after creating the
display, before workspace selection or workbench creation. Keep initialization
brief and independent of contribution order; do not access the workspace or
workbench at this stage. Contribution failures are logged without preventing other
contributions from running. The Copilot integration uses this hook to seed its
configuration preferences without overwriting existing user settings.

## 1. JDK 25 & Eclipse 2026-03

GAMA 2026 moves entirely to **Java 25** and **Eclipse 2026-03 DSL**. This unlocks modern language features that are now used throughout the engine:

- **Pattern matching for `instanceof`** — eliminates redundant casts:
  ```java
  // Old
  if (obj instanceof MyClass) { MyClass myVar = (MyClass) obj; ... }
  // New
  if (obj instanceof MyClass myVar) { ... }
  ```
- **Records, sealed classes, switch expressions** — used internally for cleaner data modelling.
- **Superior concurrency** — virtual threads and structured concurrency improve parallel simulation scheduling.

> **Action needed:** Recompile all plugins against JDK 25. No source changes are required solely for this upgrade, but you will benefit from updating your own code too.

---

## 2. New OOP Constructs in GAML: `class` and `object`

GAML now supports **`class`** (a lightweight, purely data-oriented structure with no lifecycle or spatial footprint) alongside the existing **`species`** (full agents with location, shape, and scheduling).

**When to use which:**

| Construct | Use when… |
|---|---|
| `species` | Agents need a location, shape, or are scheduled |
| `class` | You only need structured data (like a Java POJO) |
| `object` | You need a single instance of a `class` |

This is the first time GAML supports structured data types that are not agents — no migration is required unless you want to take advantage of this.

### Java-backed built-in classes

Plugins can contribute non-agent GAML classes using `@gaml_class`. Unlike an ordinary operator,
a class constructor accepts zero arguments: `counter c <- counter();`.

```java
import gama.annotations.*;
import gama.api.gaml.types.IType;
import gama.api.kernel.object.IClass;
import gama.api.runtime.scope.IScope;
import gama.core.object.GamlObject;

@gaml_class(name = "counter", doc = @doc("A non-agent counter."))
@vars(@variable(name = "value", type = IType.INT, init = "0",
        doc = @doc("The current count.")))
public class Counter extends GamlObject {
    private int value;

    public Counter(final IScope scope, final IClass clazz) {
        super(clazz);
    }

    @getter("value")
    public int getValue() { return value; }

    @setter("value")
    public void setValue(final int value) { this.value = value; }

    @action(name = "increment", doc = @doc("Increment this counter."))
    public Integer increment(final IScope scope) { return ++value; }
}
```

The existing annotation processor generates the registration and factory; the plugin must contribute
its generated additions through `gaml.extension`, as for species. Concrete implementations must be
public, implement `IObject`, and provide a public `(IScope, IClass)` constructor. Nested implementations
must be static. Extending `GamlObject` requires a dependency on `gama.core`; implementing `IObject`
directly requires only `gama.api`.

Java construction finishes **before** GAML attributes are initialized. Call `super(clazz)`, not the
older initializing `GamlObject(IScope, IClass, Map)` constructor, and do not initialize GAML attributes
yourself. Named arguments, `init` expressions, and type defaults are applied afterward through the
annotated setters. A getter marked `initializer = true` can provide the initial Java value when no
argument or `init` expression is given.

```gaml
counter c <- counter();
counter initialized <- counter(value: 10);
int result <- initialized.increment();

class labeled_counter parent: counter {
    string label <- "example";
}
```

The nearest registered Java superclass becomes the GAML parent; otherwise it is `object`.
Attributes, actions, and the Java instance factory are inherited by model-defined subclasses.
Abstract Java implementations can be registered without a factory, but cannot be instantiated,
including through a GAML-only subclass. A concrete Java subclass must supply the implementation.
Native classes can also be used directly as Java operator parameter and return types without a
separate `@type` declaration.

Java class and species descriptions now expose their factory through the same `getConstructor()`
accessor, replacing `getObjectConstructor()` and `getAgentConstructor()`. Both factories implement
`IConstructor<T, A, B>`: `IObjectConstructor` creates an object from `(IScope, IClass)`, while
`IAgentConstructor` creates an agent from `(IPopulation<?>, Integer)`. Species descriptions narrow
the return type to `IAgentConstructor`; its existing primitive-index `createOneAgent(population, index)`
method and constructor method references remain supported.

### Persistent shell contexts

The built-in `context` class keeps a shell alive between commands. The new
`command(string, context)` form returns the same context, whose state (current directory,
variables...) is preserved:

```gaml
context c <- context();
c <- command("cd ../includes", c);
c <- command("ls", c);
write c.result + " (" + c.status + ") in " + c.directory.path;
```

`commands` and `results` record the history, `result` and `status` describe the last command,
and `directory` is a `folder` that can also be assigned. Unlike the string forms of `command`,
a failing command does not raise an error. `c.close()` terminates the shell; the next command
starts a new one in the same directory.

---

## 3. Cleaner, Faster GAML Syntax

The Xtext grammar (`Gaml.xtext`) has been extracted into its own dedicated plugin (`gaml.grammar`) and the AST has been formalised. The result is significantly faster model compilation.

Several syntax shortcuts have also been cleaned up. See [Part 2, Section 1](#1-gaml-language-syntax-changes) for the full before/after reference.

---

## 4. New OpenGL 4.1 Display Plugin

The new **`gama.ui.display.opengl4`** plugin is a ground-up rewrite of the rendering engine, built on the **OpenGL 4.1 Core Profile**:

- All rendering now uses **shaders** (`.vert`/`.frag` files), **VAOs**, and **VBOs**.
- The legacy OpenGL 2 fixed-function pipeline (`glBegin`/`glEnd`, `glMatrix*`) is gone.
- The **JOML** library replaces the legacy matrix stack.
- Result: higher frame rates, better polygon throughput, lower GPU memory usage.

> **Action needed for display plugin authors:** Rendering code using the legacy OpenGL 2 API must be rewritten against the new shader-based pipeline.

---

## 5. New Architecture: the `gama.api` Module

The single biggest architectural change is the introduction of **`gama.api`** — a dedicated module containing only public interfaces and contracts, with zero implementation code.

**Before (GAMA 2025 and earlier):**
- Public interfaces and their implementations lived side by side in `gama.core`.
- Plugin developers ended up importing implementation classes directly.

**After (GAMA 2026):**
- `gama.api` contains only interfaces (`IAgent`, `IPoint`, `IList`, …).
- `gama.core` contains the implementations.
- Plugin developers **only depend on `gama.api`**.

This makes the platform significantly more stable: implementation classes can change freely without breaking plugin code.

See [Part 2, Section 5](#5-package-mapping-gamaapi-module) for the full import mapping.

---

## 6. Asynchronous File Saving

`save` has an opt-in `async: true` facet (#740). The first implementation covers images and
display snapshots: the value is computed synchronously, then encoded and written on a background
thread. The same mechanism can be used to speed up other file formats.

No migration is required. To support asynchronous saves in an extension's save delegate:

```java
@Override
public boolean supportsAsync() { return true; }

@Override
public void save(IScope scope, IExpression item, Object value, File file, SaveOptions options)
        throws IOException {
    if (!options.async()) { writeNow(value, file); return; }
    MyData copy = copyMutableState(scope, value); // synchronous, while the scope is valid
    MyAsyncWriter.enqueue(copy, file);            // ordered, bounded, reports its own errors
}
```

The background task must not use `IScope` and must not read agents or other mutable model
values. Preserve write order, bound queued work, and report failures explicitly. `save` rejects
`async: true` when `supportsAsync()` returns `false`, which is the default.
`SaveOptions.withAsync(boolean)` creates options with the flag set; the former canonical
constructor remains available and keeps saves synchronous. See
`gama.extension.image.AsyncImageWriter` for a reference implementation and the
[changelog](CHANGELOG_2026.md#extension-api-asynchronous-save-delegates) for the requirements.

---

# Part 2: Migration Reference

Work through each section that applies to your plugin. Each section is self-contained.

---

## 1. GAML Language Syntax Changes

These changes affect `.gaml` model files. Simple syntax replacements can often be applied globally;
changes involving ownership, state or presentation variables require reviewing their uses.

### Presentation variables in experiments: migrate to output locals

**Development status:** implemented on the branch of draft
[PR #1313](https://github.com/gama-platform/gama/pull/1313), including commit
[`828cff24c`](https://github.com/gama-platform/gama/commit/828cff24c50f24b1e173f9d1c3418ac09a2c13cf).
This feature is not yet merged or released.

Experiment attributes often exist only to configure displays: fonts, colors, text positions,
line widths, palettes or other presentation settings. Such definitions can now be placed
directly inside `output` and `permanent`. They become **persistent lexical
locals**, not attributes of the experiment or simulation.

Declarations directly inside `display` are not supported. Put even single-display
presentation variables in the enclosing output. Display bodies contain layers and display
configuration; `image name;` always denotes a layer there, not a declaration.
Ordinary temporary declarations inside graphics blocks and event handlers remain supported.

This also provides a migration path for the stricter experiment/simulation boundaries:
plain `output { ... }` means `output of: simulation { ... }`; it no longer implicitly reads
experiment-only attributes. Do not move styling into `global` merely to satisfy that lookup
if it is only used by outputs.

Parameter declarations use the same owner selector: omitted `of:` means simulation;
`parameter "Controller setting" of: experiment var: setting;` explicitly targets an
experiment attribute. The two owners keep separate parameter namespaces, including
homonymous built-ins such as `seed`.

#### Before and after

The following historical pattern relied on experiment attributes being implicitly visible
inside simulation-facing output code:

```gaml
global {}

experiment styled type: gui {
    font label_font <- font("Helvetica", 14);
    rgb accent <- #teal;

    output {
        display main background: accent {
            graphics labels {
                draw "Hello" font: label_font color: accent;
            }
        }
    }
}
```

When these values are presentation-only, move both the font and the color into the output:

```gaml
global {}

experiment styled type: gui {
    output {
        font label_font <- font("Helvetica", 14);
        rgb accent <- #teal;

        display main background: accent {
            graphics labels {
                draw "Hello" font: label_font color: accent;
            }
        }
        display summary {
            graphics labels {
                draw "Summary" font: label_font;
            }
        }
    }
}
```

Both locals are available to both displays, including their header facets, layers and
inline handlers. Neither declaration creates
`experiment.label_font`, `simulation.label_font` or `self.accent`.

#### Choose storage and ownership deliberately

| Definition | Visibility and storage |
|---|---|
| Experiment attribute | Controller/model state owned by the experiment; use explicit qualification from simulation outputs |
| Output local | Visible to that output block's child displays, monitors, inspectors, browse outputs and other child declarations |
| Local inside a layer, action or handler | Ordinary temporary variable with its existing execution lifetime, not a persistent presentation local |

The enclosing output selects the initialization and execution owner:

| Output spelling | Owner and lifetime |
|---|---|
| `output { ... }` or `output of: simulation { ... }` | Independent output/local instances for each simulation |
| `output of: experiment { ... }` | The declaring experiment instance, shared across its simulations |
| `permanent { ... }` | Legacy spelling of experiment-owned output, with the same local-variable rules |

Choose the owner based on what the output does, not just on where its fonts used to be
declared. A simulation visualization should normally remain simulation-owned after moving
its styling. An experiment-wide dashboard may use `of: experiment`.
`of:` does not select a particular simulation instance. Experiment-owned species layers
cannot implicitly borrow a simulation's population; keep those layers in
`output of: simulation` unless the population is genuinely owned by the experiment.

If a value is also used by experiment actions, model behavior, parameter declarations or
external consumers, keep it as an attribute. For example, a parameter controlling a font
size still needs an attribute target; a parameter declared outside the output cannot
bind an output local:

```gaml
experiment adjustable type: gui {
    int label_size <- 14;
    parameter "Label size" of: experiment var: label_size min: 8 max: 40;

    output {
        display main {
            graphics labels {
                draw "Hello" font: font("Helvetica", experiment.label_size);
            }
        }
    }
}
```

Likewise, keeping an experiment-owned font attribute and using
`font: experiment.label_font` is valid. Moving an attribute is optional, not an automatic
rename or a blanket requirement to remove all experiment presentation attributes.

#### Initialization, mutation and refresh

Direct declarations are initialized **once per output instance**, in declaration
order and in the selected owner's context, before evaluating its facets and child outputs.
This makes output locals available in display header facets such as `background: accent`.
An initializer may use an earlier local from the same output block.
Do not rely on forward references or dependency sorting.

Experiment-owned output locals are prepared before early layout-facet evaluation.
Consequently, they are initialized after experiment attributes but **before the
experiment's GAML `init` behavior**, rather than waiting for the first rendered frame.
Do not initialize such locals from state that is only established by that behavior.
Display refreshes and reopenings do not rerun the enclosing output's declarations.
Recreating the owner or output instance creates new local storage and reevaluates
the initializers. In particular, experiment-owned locals are not reset just because
another simulation is created.

An initializer is not a dynamic getter. A local computed from `cycle`, a random draw,
an attribute or a parameter retains its initial value until explicitly assigned.
Declarations support ordinary typed initialization and assignment, not attribute facets
such as `update:` or `function:`. Keep changing expressions in drawing/refresh facets,
or update the persistent local from code inside its lexical scope.

Inline overlay controls may bind persistent locals through `var:` and modify them from
their handlers. Ordinary transient locals remain invalid control bindings. Assignments
are visible to the other child outputs sharing that output local.

Typed defaults and nil semantics are unchanged:

```gaml
output {
    list<int> numbers;          // a new empty list, retained by this output
    map<string,int> scores;     // a new empty map, retained by this output
    list<int> missing <- nil;   // nil, not an empty list
    display main {
        graphics labels { draw string(length(numbers)); }
    }
}
```

Independent storage does not imply a deep copy of every initializer result. An initializer
that explicitly reads a shared mutable object can still introduce an alias.

#### Remote layers and callbacks

Persistent locals remain available through nested `remote_context` statements, including
`ask`, remote inline drawing code and event/control callbacks. Entering a remote context
changes `self` to the target agent, not the lexical binding of a presentation local.
The closest lexical declaration wins over an unqualified target attribute with the same
name; use `self.attribute` to explicitly access the target's attribute.

```gaml
// With particle declared in the model:
output {
    int selected_count <- 0;
    display main {
        event 'a' {
            selected_count <- 0;
            ask particle {
                selected_count <- selected_count + 1;
            }
        }
    }
}
```

Separately declared species actions/aspects, global reflexes and experiment actions do not
gain access to the caller's output locals. Move inline presentation code with its
declarations, or retain explicitly accessible attributes for code outside the output tree.
Sharing storage also does not make a parallel read-modify-write expression an atomic
reduction.

#### Experiment inheritance and overrides

Experiment inheritance copies output **declarations**, not a running parent's
local values. Each child initializes its own storage in its selected owner context.
Inherited expressions are rebound to the copied lexical declarations, rather than keeping
references to the parent's live output scope.

If the child defines its own output block, the existing merge rules apply: its same-name
local declaration replaces the inherited declaration; missing parent declarations and
outputs are copied into the child. A duplicate declaration within one block remains an
error. For example, a child can override the size used by an inherited font and display:

```gaml
experiment base type: gui {
    output of: experiment {
        int label_size <- 14;
        font label_font <- font("Helvetica", label_size);
        display main {
            graphics labels { draw "Hello" font: label_font; }
        }
    }
}

experiment larger parent: base type: gui {
    output of: experiment {
        int label_size <- 20;
    }
}
```

The inherited font initializer in `larger` uses its local size `20`; the parent's
declarations are unchanged. The two owner contexts are inherited/merged separately:
simulation output and experiment output are not one shared namespace.

The merge keeps the child's declarations first and appends missing inherited declarations.
There is no dependency-based reordering: a new child initializer cannot assume that a
later appended inherited local is already available. Redeclare required prerequisites
before dependent child declarations. Keep override types compatible with the inherited
expressions that use them; a different type can invalidate those expressions.

Explicit **display inheritance** (`display child parent: base`) is distinct from experiment
inheritance. It inherits display facets and layers, without creating display-local storage.
Displays in the same output share its locals; inherited layers keep their declaration-site
bindings.

#### Migration checklist

1. Find experiment attributes used only by output declarations, layers and inline callbacks.
2. Move presentation-only values into their output block, including values used by only
   one display. Move any existing direct display declarations there too.
3. Keep parameters, controller/model state and values used by separately declared
   actions/aspects as attributes, with explicit owner/receiver access where needed.
4. Review initializer order, one-time versus dynamic evaluation, and early experiment layout
   preparation before `init`.
5. Review inherited overrides, local name collisions and qualified accesses that still
   assume an agent attribute.

See the [changelog entry](CHANGELOG_2026.md#draft-follow-up-persistent-output-presentation-locals)
and [PR #1313](https://github.com/gama-platform/gama/pull/1313) for implementation status.

### Explicit container types for built-in action arguments

**Breaking change for model authors:** built-in actions now check the content and key types
declared for their container arguments. A bare `graph` declaration is no longer sufficient
when passing a network to an action expecting `graph<geometry, geometry>`, such as
`wander`'s `on` argument, driving's `compute_path`/`drive_random` graph arguments, or
pedestrian network actions.

```gaml
// Before
graph road_network;

// After
graph<geometry, geometry> road_network;
```

The first parameter is the vertex type; the second is the edge type. Use geometry types
for these spatial networks even when vertices and edges are intersection and road agents:
agents are compatible with geometry types. Graph construction is unchanged, for example:

```gaml
graph<geometry, geometry> road_network;
init {
    road_network <- as_driving_graph(road, intersection);
}
```

Update every declaration that carries the network, including globals, species attributes,
local variables and your own action parameters or return types. A later assignment of a
spatial graph does not refine a variable declared only as `graph`. An argument-type error
at a call site should therefore be fixed at the relevant declaration, not just by changing
the graph-building expression.

This is not a blanket change to all graphs: non-spatial graphs should retain their actual
vertex and edge types. The same stricter checking applies to other annotated container
arguments; declare compatible list element and map key/value types where required.

### Lists and maps: absence is not emptiness (#765)

An omitted initializer creates a new empty list or map. An explicit `nil` initializer, later assignment,
or cast preserves `nil`. These rules apply equally to locals, globals and species attributes, including
parameterized types.
Explicit nil values passed through `create ... with:` or object constructor arguments follow the same rule.

```gaml
list<int> numbers;               // a new empty list<int>
map<string,int> scores;          // a new empty map<string,int>
list<int> missing <- nil;        // nil, not []
numbers <- nil;                 // now nil
numbers <- [];                  // explicitly restore an empty list
scores <- map([]);              // explicitly create an empty map
// list(nil), map(nil), and to_list(nil) all return nil
```

An empty container is never equal to `nil`. Test `value = nil` for absence before using operations
that require a container; indexing a nil container reports a runtime error. Each omitted initializer
allocates an independent container, even for repeated executions of the same local declaration.

**Java contributors:** use `IType.createDefaultValue(scope)` for omitted initialization, not
`cast(scope, null, ...)`. `getDefault()` remains the type's fallback/nullability value.
The Java construction helpers `GamaListFactory.castToList` and `GamaMapFactory.castToMap` still
convert Java null to an empty container; they are not the GAML casts.

### Species attribute brackets and grid matrices

Attribute brackets work on any statically known species. Ordinary species produce typed lists:

```gaml
list<float> energies <- animal[energy];
animal[energy] <- energies collect (each * 0.95);
```

The list follows the species' current population order, just like `animal collect each.energy`;
it is not indexed by agent identifiers. Assignment requires a non-nil list of compatible elements
with exactly one element per current agent. Length and content type are checked before any write.
An empty population reads as an empty typed list and accepts an empty list.
Agents and values are captured before invoking setters, so setter side effects do not reorder the assignment.
The population is resolved in the caller's host context, as with ordinary species iteration.
Inherited attributes and species aliases are supported when their species type is statically known.
Grids retain the spatial matrix behavior described below.

```gaml
// Before
matrix<float> roads <- matrix_with(cell, "road");
cell set_values ("road", roads);
list<float> flat_roads <- cell collect each.road;

// After
matrix<float> roads <- cell[road];
cell[road] <- roads;
list<float> flat_roads <- list(cell) collect each.road;
// Alternatively, flatten the spatial snapshot explicitly:
list<float> flattened <- list(cell collect each.road);
```

`collect` on a statically known grid species returns a matrix with the grid's column and row
counts. `each` is a cell agent, and `result[col, row]` contains the expression's value for
the cell at `grid_x = col`, `grid_y = row`. These are zero-based grid indices, not world
coordinates. Expressions are evaluated in row-major order, once per existing cell; missing
cells receive the result content type's default value. The matrix is a snapshot, not a live
view, and container-valued attributes are not deep-copied.

The matrix element type is inferred from the expression: integer, float, boolean, color,
string and container-valued expressions retain their types. Direct accesses such as
`cell collect each.road` (including named iterators, `cell collect (c: c.road)`) use optimized
attribute extraction, as does the equivalent `cell[road]`. Arbitrary expressions use normal
per-cell evaluation, for example `cell collect (each.road * each.capacity)`.

Float attribute snapshots are backed internally by a field while retaining the inferred
`matrix<float>` type. Thus `field r <- field(cell[road]);` reuses the extracted field without
copying its values. The equivalent direct-attribute `collect` uses the same representation.
Extraction still creates a snapshot, not a live view of the grid. Field spatial accesses use
the usual field world-coordinate conventions; `[col, row]` continues to use grid indices.

In `species[attribute]`, a bare identifier naming an attribute resolves against the statically
known species, even if a caller variable has the same name. The result is a
`list<attribute_type>`, or `matrix<attribute_type>` for grids. Unknown names are compiled as ordinary index expressions and
reported as errors if they are not valid caller variables either. Numeric and coordinate
indexing are unchanged: `cell[0]` and `cell[col, row]` still select cells.
To index when names collide, qualify the caller's attribute (for example, `road[self.linked]`
inside a road agent), parenthesize the index (`road[(linked)]`), or copy it to an intermediate
variable whose name is not an attribute of the indexed species (`int road_index <- linked;`
followed by `road[road_index]`). Brackets on lists, maps and matrices keep their existing meaning.

Lists of cells, filtered grids (`cell where ...`), ordinary species, and species expressions
without a statically known grid type still produce lists. Cast the source to a list to
preserve the old flat behavior, especially before list-only operators such as
`remove_duplicates` or functions taking lists. On an existing matrix, `collect` also preserves
dimensions; it does not flatten the matrix.

Assignments to species attribute brackets check that the attribute is writable and accepts the
supplied list or matrix element type. The right-hand expression is evaluated once in the caller's context
before writing. Use separate assignments for separate attributes:

```gaml
cell[road] <- roads;
cell[color] <- colors;
```

Every matrix must have exactly the grid's dimensions; nil matrices and mismatched dimensions
raise an error before any attribute is changed. Matrix positions map directly to grid indices.
Missing cells are skipped. Writes use normal attribute setters, preserving type conversion,
constraints and change notifications, with cells visited in row-major order. Each assignment
is checked independently; errors from an individual setter do not roll back earlier writes.
Reads are snapshots: modifying a separately stored list or matrix does not update the agents until it
is assigned back. Container-valued attributes are shallowly copied, not deep-copied.

The string-based grid `matrix_with` and `set_values` operators are removed. The dimensions-based iterator
`{columns, rows} matrix_with expression` is unchanged: its `each` denotes a coordinate point,
not a cell agent.

### 1.1 Arrow (`->`) expressions — braces removed

The outer curly braces around the body of `->` expressions are no longer valid.

```gaml
// Before
action my_action -> { do something; };

// After
action my_action -> do something;
```

### 1.2 `diffuse var:` and `transition to:` — keyword removed

The `var:` and `to:` facets were redundant and have been removed.

```gaml
// Before
diffuse var: heat;
transition to: idle;

// After
diffuse heat;
transition idle;
```

### 1.3 `image` display layer → `picture`

The `image` keyword inside `display` blocks has been renamed to `picture` to better reflect its purpose and avoid confusion with the `image` type.

```gaml
// Before
display my_disp {
    image "background.jpg";
}

// After
display my_disp {
    picture "background.jpg";
}
```

### 1.4 Display and experiment identifiers — explicit `title:` required

Displays and experiments now enforce a strict separation between the **programmatic identifier** (used in code) and the **UI label** (shown to the user).

```gaml
// Before
display "3 Simulations" { ... }

// After
display _3_Simulations title: "3 Simulations" { ... }
```

> **Rule of thumb:** The name immediately after `display` or `experiment` must now be a valid identifier (no spaces, no special characters). Use `title:` for the human-readable label.

### 1.5 `with:` argument list syntax

The map-literal syntax for argument passing has been replaced by a cleaner parenthesised syntax.

```gaml
// Before
do my_action with: [agents::ag, values::[1, 2, 3]];

// After
do my_action with: (agents: ag, values: [1, 2, 3]);
```

**Key differences:**
- Outer `[...]` → `(...)`
- Double-colon `::` → single colon `:`

---

### 1.6 `overlay` layers — consistent position, size and contents

The `position` and `size` facets of `overlay` are now interpreted identically by 2D (java2D) and 3D (opengl) displays, and follow their documentation (issues #354, #589, #656, #997):

- Each coordinate is interpreted independently: a coordinate written with `#px` is a number of pixels of the view, a value in [0,1] (in [-1,1] for the position) is a fraction of the view, and a larger value is in model units. For instance, in `size: {2000, 32#px}`, only the height is in pixels.
- Negative positions are counted from the right (resp. bottom) border of the view.
- Small pixel sizes (e.g. `{5#px, 5#px}`) are no longer turned into a fraction of the view in 3D displays, and 3D overlays are no longer shrunk by 10%.
- Overlays are recomputed when the view is resized (e.g. after switching to full screen).
- Inside the overlay, `#px` is always one pixel of the view, whatever the size of the overlay, and model units are scaled as in the view (previously, in 2D displays, they were scaled according to the width of the overlay).
- Pixels here are logical window pixels, not physical backing pixels. OpenGL uses the native surface's actual scale when moving between monitors with different DPI; a `40#px` element keeps its logical size, although its physical pixel count can change.

```gaml
overlay position: {10#px, 10#px} size: {200#px, 100#px} background: #white {
    draw "Cycle: " + cycle at: {20#px, 30#px} color: #black;
}
```

> **Action needed:** none for overlays whose contents are drawn with `#px`. Models that draw the contents with model units in an overlay smaller than the view (e.g. `at: location / 3`) may need to express these coordinates in pixels.

---

### Display dimensions and mouse coordinates

`#display_width` and `#display_height` always describe the **visible viewport in logical pixels**,
independent of zoom, layer coordinate mode and overlay size. In graphical scopes, including `event`
callbacks, they use the originating display. Outside graphics they use the frontmost display, or
return `0` when none exists. Dimensions are read when the expression is evaluated; unlike mouse
positions, they are not captured in the event snapshot.

**Breaking behavior change:** in world contexts these constants previously described the displayed
world's pixel extent. Review calculations that relied on that extent or its variation with zoom.
Viewport dimensions multiplied by `#zoom` are not generally equivalent to the old values:
aspect-ratio fitting, letterboxing and camera projection also affect the world.
`#px` still depends on the evaluation context: it is `1` in screen contexts and converts a
logical pixel to model units in world contexts. Stored numbers do not retain coordinate metadata.

Prefer these explicit mouse names in new or migrated models:

| Referential | Preferred name | Other supported names |
|---|---|---|
| World/model units | `#mouse_location_in_world` | `#user_location`, `#user_location_in_world`, legacy `#mouse_world_location` |
| Viewport logical pixels, from the top-left | `#mouse_location_in_display` | `#user_location_in_display`, legacy `#mouse_screen_location` |

The legacy mouse names remain aliases; replacing their spelling is optional and does not change
behavior. Mouse-event callbacks read the originating event's captured position with every alias.
Viewport mouse positions are not overlay-local positions.

For example, normalize a mouse position within the viewport using matching units:

```gaml
event #mouse_move {
    point mouse = #mouse_location_in_display;
    if (#display_width > 0 and #display_height > 0) {
        write {mouse.x / #display_width, mouse.y / #display_height};
    }
}
```

Use `#mouse_location_in_world` instead for comparisons with agent locations or world geometry.
An event block does not inherit a drawing layer's referential, and ordinary point arithmetic does
not convert between world and viewport coordinates. For layer placement and contents, see the
[display-coordinate guide](gama.documentation/files/input/markdown/DisplayCoordinates.md).

---

### Map order for OpenGL displays

Flat maps and map-like environments can avoid depth-buffer conflicts between overlapping
elements by opting into **map ordering**. Both OpenGL backends then draw world layers,
traces and drawings in their existing traversal order, with later fragments covering or
blending over earlier fragments instead of competing for depth.

To enable it for a specific display, add the facet to the display declaration:

```gaml
display my_map type: opengl map_order: true {
    species roads;
    species buildings;
    species people;
}
```

Alternatively, enable **Displays > OpenGL Rendering Properties > In OpenGL, draw world
layers in map order instead of using 3D depth** in preferences. This applies to OpenGL
displays without an explicit `map_order:` facet, including existing models without code
changes. The preference defaults to `false`. An explicit facet takes precedence:
use `map_order: false` on a 3D display to retain depth-based rendering when the global
preference is enabled. Java2D displays are unaffected.

**Action needed to use the improvement:** choose the display facet for portable,
per-model behavior or the preference for a local default. Review drawing order first:
a later background or polygon can cover an earlier agent, regardless of elevation.
Map ordering disables depth testing and writes for world drawings, including faces and
mesh cells within a drawing; it is therefore unsuitable for scenes that depend on
physical 3D occlusion. It does not change model coordinates or camera projection, and
camera clipping still applies. Screen layers retain their existing handling. Picking
follows map order, with backend-specific limitations described in the
[display-coordinate guide](gama.documentation/files/input/markdown/DisplayCoordinates.md#map-ordering-in-opengl).

Even without map ordering, legacy OpenGL orthographic displays now use the same camera-depth
direction as perspective and OpenGL4. Existing overlapping geometry may consequently look
different; review displays that relied on the former depth behavior.

---

### OBJ geometry conversion

When an OBJ file is read as GAML geometry, its face buffer now contains **one complete polygon
per OBJ face**. Previously it also contained incomplete polygons created while collecting each
face's vertices. Models that relied on the former geometry count or those intermediate polygons
must be updated; models that only draw the OBJ file directly do not need a syntax change.
OBJ bounds are now obtained without constructing all face geometries, and invalidating the file
clears its parsed data so the next access reloads it.

---

### 1.7 Charts — body re-evaluated at every step, legends no longer identify series

The body of a `chart` was only executed once, when the chart was created; only the `value` of its data was then re-evaluated. It is now re-evaluated at every step (#403):

```gaml
chart "Rewards" type: series {
    if (cycle > 1) { data "Instant reward" value: reward color: #blue; }   // now appears at cycle 2
    data "period " + (cycle div 10) value: reward;                       // a new series every 10 cycles
    loop g over: groups { data g.name value: g.reward; }                 // follows the current groups
}
```

- A series is identified by the statement declaring it and by its current legend. A new legend creates a new series.
- A series that is not declared at some step is **frozen** in `series`, `xy`, `scatter` and `heatmap` charts (its history is kept and it resumes if it is declared again), and **removed** in `pie`, `histogram`, `radar` and `box_whisker` charts.
- Legends are only displayed names (#785): two `data "a"` statements now produce two series (previously, the second replaced the first), and duplicated category labels are no longer merged.

> **Action needed:** none for charts whose body only contains `data`/`datalist` statements with constant legends. Charts whose legends depend on changing expressions (e.g. `data "value at " + cycle ...`) now create a new series at each change: use a constant legend to keep a single series. Models relying on duplicated legends to merge series must use a single `data` statement.

---

## 2. Java Annotations — Import Path Changed

All GAML-related Java annotations have moved to **top-level** interfaces in `gama.annotations`. Update your imports accordingly.

```java
// Before
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.skill;

// After
import gama.annotations.operator;
import gama.annotations.skill;
```

**Complete list of affected annotations:**

`@action`, `@arg`, `@constant`, `@display`, `@doc`, `@example`, `@experiment`, `@facet`, `@facets`, `@file`, `@getter`, `@inside`, `@listener`, `@no_test`, `@operator`, `@setter`, `@skill`, `@species`, `@symbol`, `@test`, `@type`, `@usage`, `@variable`, `@vars`

**Also note:** `IKeyword` has moved:

```java
// Before
import gama.core.common.interfaces.IKeyword;

// After
import gama.annotations.constants.IKeyword;
```

### Temporary-declaring facets: replacing `NEW_TEMP_ID`

`IType.NEW_TEMP_ID` has been removed. Declaring a temporary identifier and describing its
type are now independent: set `declares_new_temp = true` on the `@facet`, then use `type`,
`of` (container contents), and `index` (container keys) to describe the variable.

```java
// Before: the compiler needed statement-specific rules to determine the variable type.
@facet(name = IKeyword.RETURNS, type = IType.NEW_TEMP_ID, optional = true)

// After: a fixed list<float>, as used by benchmark returns:.
@facet(name = IKeyword.RETURNS, declares_new_temp = true,
       type = IType.LIST, of = IType.FLOAT, optional = true)

// After: a list of the statement's selected species, as used by create returns:.
@facet(name = IKeyword.RETURNS, declares_new_temp = true,
       type = IType.LIST, of = ITypeProvider.STATEMENT_TYPE, optional = true)
```

Use `gama.api.gaml.types.IType` and `gama.annotations.support.ITypeProvider`.
Every declaring facet must specify **exactly one** outer type. `of` alone does not imply
a list: use `type = IType.LIST` explicitly. A fixed map, for example, uses
`type = IType.MAP, index = IType.STRING, of = IType.FLOAT`.

Two contextual providers can be used in any of `type`, `of`, and `index`:

| Provider | Meaning | Existing uses |
|---|---|---|
| `STATEMENT_TYPE` | The computed type of the statement, including its species/target facets | Contents of the result lists from `create`, `capture`, `release`, and `migrate` |
| `INFERRED_TYPE` | The statement's declared type, or its existing value/iteration inference when no type is declared | `let`, `loop`, `arg`, and `user_input` temporary variables |

`INFERRED_TYPE` retains nested container parameters, such as `list<list<int>>` or
`map<string, list<float>>`. Do not substitute `OWNER_TYPE`: that existing provider refers
to an owning species/operand, not the statement's computed type. Other negative provider
IDs are not supported on temporary-declaring facets.

The annotation controls identifier handling and **static typing**, not runtime assignment.
A statement implementation must still create and populate its temporary in the matching
runtime scope. For a result that must remain visible after a sequence, initialize the
variable in the enclosing frame before entering the sequence's own scope.

**Action needed:** migrate affected annotations, rebuild `gama.annotations` and
`gama.processor`, then clean/rebuild dependent bundles so their generated `GamlAdditions`
use the new metadata. Custom implementations/callers of `IArtefactFactory.createFacetArtefact`
must pass the new `declaresNewTemp` boolean; custom facet artefacts must expose their raw
content/key descriptors through `getContentTypeProvider()` and `getKeyTypeProvider()`.
Ordinary expression facets keep `declares_new_temp = false` (the default).

---

## 3. `IAgent` Interface Changes

`IAgent` has been redesigned and moved to the `gama.api` module.

### What changed

| Aspect | Before | After |
|---|---|---|
| Package | `gama.core.metamodel.agent.IAgent` | `gama.api.kernel.agent.IAgent` |
| Extends | `IShape`, `IAttributed` | `IDelegatingShape` |
| Container type | `IContainer.Addressable<String, Object>` | `IContainer.ToGet<String, Object>` |
| `getLocation()` return type | `GamaPoint` | `IPoint` |
| `getGeometry()` return type | `GamaShape` | `IShape` |
| `getPeers()` | Mutable list | Read-only list |
| `setPeers()` | Concrete implementation | Empty default method |

### Practical impact

- **Do not mutate agent attributes through the container interface.** Use proper variable evaluation via `scope.evaluate(expression, agent)`.
- **Geometry access is now interface-typed.** If you need a concrete `GamaPoint`, cast explicitly — but prefer working with `IPoint`.

---

## 4. Population API Changes (`AbstractPopulation`)

### New base class

All population implementations should now extend `gama.core.metamodel.population.AbstractPopulation`, which centralises common logic that was previously duplicated.

### `IPopulation` API changes

| Member | Change |
|---|---|
| `IsLiving` (nested predicate class) | **Removed** — inline the predicate in your own code |
| `createAgentAt(...)` | **Renamed** to `createAgentAtIndex(...)` |
| `createOneAgent(...)` | **Added** as a default method |
| `fireAgentsAdded(...)` | No longer a default method — must be implemented explicitly |

---

## 5. Package Mapping: `gama.api` Module

> **General rule:** Replace `gama.core` with `gama.api` in your import statements, then fix any remaining errors using the detail tables below.

### 5.1 GAML Types → `gama.api.gaml.types.*`

`IType`, `ITyped`, `ITypesManager`, `Signature`, `GamaActionType`, `GamaAgentType`, `GamaBoolType`, `GamaColorType`, `GamaContainerType`, `GamaDateType`, `GamaDirectoryType`, `GamaFieldType`, `GamaFileType`, `GamaFloatType`, `GamaFontType`, and all other `Gama*Type` classes.

### 5.2 Java Data Structures → `gama.api.types.*`

| Category | Interfaces & Classes |
|---|---|
| Colors | `IColor`, `GamaColor`, `GamaColorFactory` |
| Dates | `IDate`, `GamaDate`, `GamaDateInterval`, `GamaDateFactory` |
| Files | `IGamaFile`, `GamaFile`, `GenericFile` |
| Fonts | `IFont`, `GamaFont`, `GamaFontFactory` |
| Geometry | `IShape`, `IPoint`, `IDelegatingShape`, `GamaPoint`, `GamaPointFactory`, `GamaShapeFactory`, `IShapeFactory` |
| Graphs | `IGraph`, `IPath`, `GraphObject`, `GamaGraphFactory`, `GamaPathFactory` |
| Lists | `IList`, `GamaList`, `GamaPairList`, `GamaListFactory` |
| Maps | `IMap`, `GamaMap`, `GamaMapFactory` |
| Matrices | `IMatrix`, `IField`, `GamaMatrixFactory` |
| Messages | `IMessage`, `GamaMessageFactory` |
| Pairs | `IPair`, `GamaPairFactory` |
| Topology | `ITopology`, `AmorphousTopology`, `GamaTopologyFactory` |

### 5.3 Expressions and AST → `gama.api.gaml.*` & `gama.api.compilation.*`

`IExpression`, `IStatement`, `IDescription`, `ISymbolDescriptionFactory`, `IExpressionFactory`, `ISyntacticFactory`

### 5.4 UI and Displays → `gama.api.ui.*`

`IGui`, `IDialogFactory`, `IStatusDisplayer`, `IConsoleListener`, `IDisplaySurface`, `IGraphics`, `IDisplayData`, `IChart`, `ILayer`, `ICameraDefinition`, `IDrawingAttributes`

### 5.5 Core Utilities → `gama.api.utils.*`

`FileUtils`, `GamaPreferences`, `StringUtils`, `JavaUtils`, `MathUtils`, `CsvReader`, `CsvWriter`, `IJson`, `IJsonObject`, `GamaRNG`, `RandomUtils`

---

## 6. Factory Pattern — Replace Direct Constructors

Direct instantiation of core GAMA types is **no longer allowed**. You must use the provided factory classes.

> **Why?** Factories enforce correct type metadata and scope-awareness that constructors cannot guarantee.

| Type | Before | After |
|---|---|---|
| `IList` | `new GamaList<>()` | `GamaListFactory.create(Types.STRING)` |
| `IMap` | `new GamaMap<>()` | `GamaMapFactory.create(Types.STRING, Types.INT)` |
| `IPoint` | `new GamaPoint(1.0, 2.0)` | `GamaPointFactory.create(1.0, 2.0)` |
| `IShape` (circle) | `GamaGeometryType.buildCircle(10, pt)` | `GamaShapeFactory.buildCircle(10, pt)` |
| `IColor` | `new GamaColor(255, 0, 0)` | `GamaColorFactory.get(255, 0, 0)` |
| `IGraph` | `new GamaSpatialGraph(...)` | `GamaGraphFactory.createFromList(scope, edges, true)` |

**Note:** When creating typed collections, always provide the element type(s) using constants from the `Types` class (e.g. `Types.STRING`, `Types.INT`, `Types.AGENT`).

---

## 7. Casting — From `Cast` to `IType`

The static `Cast` utility class has been significantly reduced. Type-specific casting methods have been removed and are now handled by the `IType` implementations directly.

> **What remains in `Cast`:** Primitive conversions — `asInt`, `asFloat`, `asString`, `asAgent` — are still available at `gama.api.gaml.types.Cast`.

| Type | Before | After |
|---|---|---|
| `IPoint` | `Cast.asPoint(scope, obj)` | `Types.POINT.cast(scope, obj, null, false)` |
| `IList` | `Cast.asList(scope, obj)` | `Types.LIST.cast(scope, obj, null, false)` |
| `IMap` | `Cast.asMap(scope, obj, copy)` | `Types.MAP.cast(scope, obj, null, copy)` |
| `IShape` | `Cast.asGeometry(scope, obj)` | `Types.GEOMETRY.cast(scope, obj, null, false)` |
| `IMatrix` | `Cast.asMatrix(scope, obj)` | `Types.MATRIX.cast(scope, obj, null, false)` |

**Parameter guide for `IType.cast(scope, obj, param, copy)`:**

| Parameter | Meaning |
|---|---|
| `scope` | The current execution scope |
| `obj` | The object to cast |
| `param` | An optional type parameter (usually `null`) |
| `copy` | `true` to return a copy, `false` to allow returning the original |

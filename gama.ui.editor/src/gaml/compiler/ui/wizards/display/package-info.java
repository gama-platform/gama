/**
 * Detached display and kind-specific layer forms shared by text and graphical editors.
 *
 * <p>DisplayWizard retains its existing constructors and editor context/suggestion callbacks. Its Layers tab
 * organizes ordered LayerDraft entries: Add opens LayerWizard in Create mode, Edit opens the same kind-locked
 * form in Apply mode. Double-click edits supported rows. Duplicate assigns a new identity; moves preserve identity,
 * source fragments and attached comments. Child Cancel changes nothing in the display draft, and display Cancel
 * discards even accepted child edits. Only the owning editor applies the accepted DisplayDraft, in one undo transaction.
 *
 * <p>LayerWizard extends DeclarationWizard and accepts a layer validator, species/grid suppliers, a candidate context
 * callback and an aspect supplier. The context is a complete model containing the candidate layer, supplied as
 * ActionBodyContext with an empty suffix. DisplayWizard automatically forwards its existing callbacks with the child
 * draft substituted into a detached candidate display. Compilation/adapter validation runs only on Validate or
 * acceptance, never on keystrokes. Existing unchanged layers avoid recompiling pre-existing code on acceptance;
 * adapter staleness/conflict checks still run. Without a context, a minimal synthetic OpenGL display is used.
 *
 * <p>Supported forms follow the core layer declarations: Species, Agents, Grid, Image, Mesh, Graphics and Overlay,
 * plus a Text convenience form for a single text draw. Camera, Light and Rotation are display definitions rather
 * than rendered layers, but the compiler classifies them as layer declarations, so they also have dedicated forms.
 * Facet fields come from registered compiler metadata or the runtime annotations. Model-aware suggestions never
 * restrict arbitrary GAML expressions. Graphics/Overlay/Species/Agents/Light retain editable block contents.
 *
 * <p>LayerDraft.body() is block contents only; source() is the original complete declaration with comments.
 * The seven-argument constructor adds body; the former six-argument constructor is retained for compatibility
 * and initializes an empty body. Adapters copying a block draft must preserve body(), just as they preserve facets.
 * DisplaySource patches only changed header tokens or the body range; no-op preserves exact source bytes.
 * Chart, Event and unknown/plugin declarations stay RAW, are excluded from creation/editing, and can still be
 * explicitly moved, duplicated or removed without changing their retained source. Legacy headers that cannot be
 * represented unambiguously also remain RAW rather than being repaired or silently rewritten.
 */
package gaml.compiler.ui.wizards.display;

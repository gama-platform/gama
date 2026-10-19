/**
 * Shared species, grid, class and model declaration settings, independent of Graphiti and diagram model types.
 *
 * <p>The top rows contain Name/Virtual and Parent/Skills. Parallel accepts a boolean or
 * integer threshold; an empty value retains the platform preference. A collapsed Advanced
 * disclosure contains Control/Mirrors, Schedules/Frequency and Parallel. Compact native SWT text fields, read-only choice
 * combos and checkboxes follow standard Eclipse wizard conventions. Expression fields preserve
 * raw source without evaluation. A compact skills menu supports multiple independent checked choices.
 * Implicit agent/reflex/false defaults do not create facets on an unchanged acceptance.
 * Additional existing facets are preserved verbatim, but not exposed in a generic Facets tab.
 * Grid mode adds width/height expressions and 4/6/8 neighbors, replaces mirroring with grid-only
 * file/files and geometry/cache checkboxes, and includes grid-skill attribute metadata.
 * New grids start at 100 by 100 cells; default neighbors is 4 and cache defaults to true.
 * Individual shapes initially shows false when absent and saves an explicit false on acceptance, because the runtime
 * default is true (or inherited). Existing explicit values and expressions remain unchanged until edited.
 * Virtual and the grid options are checkbox-only. Existing expressions remain unchanged until a checkbox is edited;
 * edited literals matching verified defaults are omitted (inherited grid defaults are not guessed).
 * The legacy use_regular_agents facet is hidden and retained verbatim.
 * Existing file/cell-size grids retain absent dimensions unchanged.
 * A grid with a legacy mirrors facet is rejected explicitly, not silently repaired.
 *
 * <p>Attributes appear as an initially expanded disclosure section and merge local declarations with metadata
 * from parent, skills and control. Standard Add/Remove buttons sit right of the list, above the
 * declaration fields. Labels and controls provide hover explanations. Name and From headers toggle
 * ascending/descending display sorting without changing draft order. Attribute types use a type-ahead
 * filter over available types; minimum and maximum appear only for int and float.
 * Mirroring adds only target, typed as the selected mirror species. Inherited entries are read-only
 * until Redefine is checked, and only explicit local/redefined declarations enter the draft.
 * Unchecking Redefine removes the override, never the inherited metadata.
 * Local origins track the species name. Entering an existing attribute name selects its row without
 * creating a duplicate; protected inherited names remain read-only. Function expressions are retained
 * unchanged but are not exposed in the form.
 * The form edits species structure and attributes only; init bodies and reflex ordering are retained
 * unchanged in the draft and are not shown here.
 * SpeciesWizardContext provides read-only metadata; editor adapters augment the built-in provider
 * with model-local ancestry. No platform description or live model is edited by the form.
 * Skill/control attributes use GamaSkillRegistry.getEffectiveVariablesForSkill, including registered Java
 * superclass/interface additions and matching species compilation
 * even when the named skill is an annotation-only subclass (such as driving).
 * Its mirrorNames hook supplies mirror candidates independently of parent-cycle exclusions and
 * remains available through context composition.
 * Parent choices include runtime-registered built-in species in both modes, except model/experiment
 * descriptions and the internal scheduler. builtInParentNames() and builtInParent(name) expose the
 * same applicability rules to adapters. The parentNames hook filters the combined candidates for
 * local inheritance cycles, independently of Mirrors. Existing external parent facets remain intact.
 *
 * <p>The GAML context menu starts with Edit, enabled for a selected species declaration, followed
 * by a separator. References in other models and Search in GAML Reference form a separated group after Paste;
 * Preferences appears last with a dedicated sliders icon.
 * Run As, Debug As, Source, Refactorings and Code Actions submenus are excluded from the text popup.
 * Add species, Add grid species and Add class follow the experiment templates in the toolbar "+" menu;
 * Add action appears last there after a separator. These creation actions are not in the text popup.
 * Existing source bodies remain read-only: header token edits preserve all declarations, unsupported constructs and
 * comments. Use Rename refactoring when references must be renamed too. Syntax and duplicate local names are checked before applying; semantic
 * diagnostics remain the normal text-editor validator's responsibility.
 *
 * <p>Adapters open SpeciesWizard with an immutable SpeciesDraft and apply getResult() only after
 * successful OK, inside their own undo transaction. Cancel never mutates the input. The adapter's
 * validator runs on OK, not on every keystroke. Changes to selection refresh only attribute metadata.
 *
 * <p>Class mode shows only Name, Virtual, Parent and Attributes. Classes belong to the world and
 * inherit only from classes, with implicit root object; they are never parents of species or grids.
 * Adapters supply classBuiltIns(), not the agent/skill/control metadata provider, and augment it
 * with local class ancestry. classParentNames()/classParent(name) expose root class descriptions;
 * SpeciesDraft.parent("object") reads class inheritance without changing the agent default of parent().
 * Class actions remain editor-owned and untouched by the draft, like all other existing body contents.
 * The source adapter creates classes at model level and edits only header tokens. Class hover help
 * remains available before semantic compilation, including on incomplete declarations.
 *
 * <p>Model mode edits the global species: Container/File name, Model name, a collapsed Advanced
 * section with Skills/Control and Schedules/Frequency, the open Attributes section (including built-in model
 * attributes) and a collapsed Documentation section with Author, Tags and Documentation. There is no
 * Parent, Mirrors or Parallel. Documentation is kept in the conventional model header comment
 * (Name, description lines, Author, Tags) through ModelDocumentation, so it is read back when editing
 * again. NewModelWizard hosts the same form via createForm/FormHost as the New Model File wizard;
 * the location is editable only there. Edit on a global declaration in the GAML editor opens the
 * form with a read-only location and header-only changes (model name, global facets, documentation);
 * attributes, init and other global contents stay unchanged.
 */
package gaml.compiler.ui.wizards.species;

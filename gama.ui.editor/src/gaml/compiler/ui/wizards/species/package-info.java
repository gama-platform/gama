/**
 * Shared species, grid, class and model declaration settings, independent of Graphiti and diagram model types.
 *
 * <p>All shared declaration dialogs extend DeclarationWizard: an explicit setCreationMode(true) selects
 * Validate/Cancel/Create; the default edit mode selects Validate/Cancel/Apply. Button IDs are unchanged
 * (VALIDATE_ID and IDialogConstants.OK_ID). Validation never accepts a result or mutates the editor.
 * The constructors, createForm/FormHost and typed result APIs remain compatible for graphical adapters.
 * WizardWidgets supplies common rows, text/choice fields, checkboxes, sizing and accessibility. Identity
 * rows order Name, Title, Type/Kind, Parent and Virtual where applicable; Skills and Control share a capabilities row.
 * Controls omitted by a declaration kind are not invented or exposed. Raw GAML values and defaults remain
 * declaration-specific; sharing the control does not change their semantics.
 *
 * <p>The species top rows contain Name/Parent/Virtual and Skills/Control. Parallel accepts a boolean or
 * integer threshold; an empty value retains the platform preference. A collapsed Advanced
 * disclosure contains Mirrors, Schedules/Frequency and Parallel. Compact native SWT text fields, read-only choice
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
 * Add action appears last there after a separator. With selected text, it opens the shared action form with that
 * text prefilled as code. Include selection, next to Returns, defaults to checked: accepting inserts the action
 * and replaces the selected statements with a do call as one undoable edit. Unchecking it leaves the selection
 * untouched and creates an independent action; both code buffers survive toggling. Cancel leaves the document
 * unchanged, and concurrent document changes block acceptance. Only complete sibling executable statements can
 * be moved; partial tokens/comments, drawing/output code and return/break/continue are rejected explicitly.
 * Arguments are not inferred: required arguments entered in the form are passed from same-name caller variables,
 * and defaulted arguments are omitted. Contextual compilation checks both the action and its caller before applying;
 * dependencies on caller-local variables must be resolved in the signature/code. A typed action's result is discarded
 * by the generated do call. The legacy extract-action entry point delegates to this same form; there is no second
 * extraction menu command. These creation actions are not in the text popup.
 * Existing source bodies remain read-only: header token edits preserve all declarations, unsupported constructs and
 * comments. Use Rename refactoring when references must be renamed too. Syntax and duplicate local names are checked before applying; semantic
 * diagnostics remain the normal text-editor validator's responsibility.
 *
 * <p>Adapters open SpeciesWizard with an immutable SpeciesDraft and apply getResult() only after
 * successful Create/Apply, inside their own undo transaction. Cancel never mutates the input. The adapter's
 * validator runs on Validate and acceptance, not on every keystroke. Changes to selection refresh only attribute metadata.
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
 * <p>Model mode edits the global species: Name, Container/File name, Skills/Control on one row, a collapsed Advanced
 * section with Schedules/Frequency, the open Attributes section (including built-in model
 * attributes) and a collapsed Documentation section with Author, Tags and Documentation. There is no
 * Parent, Mirrors or Parallel. Documentation is kept in the conventional model header comment
 * (Name, description lines, Author, Tags) through ModelDocumentation, so it is read back when editing
 * again. NewModelWizard hosts the same form via createForm/FormHost as the New Model File wizard;
 * the location is editable only there. That Eclipse-owned wizard retains the native Cancel/Finish footer without
 * a separate page-level Validate button; Finish performs validation before creating the file.
 * Edit on a global declaration in the GAML editor opens the
 * form with a read-only location and header-only changes (model name, global facets, documentation);
 * attributes, init and other global contents stay unchanged.
 */
package gaml.compiler.ui.wizards.species;

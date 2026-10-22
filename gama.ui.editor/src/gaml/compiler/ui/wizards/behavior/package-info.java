/**
 * Detached declaration editors shared with diagram adapters. The text-editor entry point is Add reflex... and only
 * opens reflex, init and abort headers. These lifecycle kinds share the Reflex Editor and may switch among themselves.
 * State, Task, Plan, Perceive and Rule Editors are diagram-only entry points, locked to the draft's kind even when a
 * caller supplies a broader selectable-kind set. Both BehaviorWizard constructors retain their signatures; the
 * constructor without a selectable-kind set offers the draft's convertible kinds.
 * Lifecycle headers show Name, Kind and When on one row, following the common Name-first identity layout.
 * When is always an inline GAML expression displaying true
 * when absent. Untouched omitted or explicit conditions are retained; an edited true or empty condition is omitted.
 *
 * All editors retain the same facet, body, validation and transactional result handling. BehaviorSource continues to
 * parse and render every supported kind losslessly; restricting text-editor UI does not restrict compiler support or
 * diagram adapters. Adapters remain responsible for undoable application of accepted results.
 */
package gaml.compiler.ui.wizards.behavior;

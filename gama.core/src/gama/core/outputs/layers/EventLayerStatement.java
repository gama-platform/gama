/*******************************************************************************************************
 *
 * EventLayerStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.util.ArrayList;
import java.util.List;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.annotations.validator;
import gama.api.compilation.IInternalFacets;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.statements.ActionStatement;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.IExecutable;
import gama.api.runtime.SystemInfo;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IOutput;
import gama.api.ui.layers.ILayerStatement;
import gama.api.ui.layers.KeyCombination;
import gama.core.outputs.layers.EventLayerStatement.EventLayerValidator;

/**
 * Written by Marilleau Modified on 16 novembre 2012
 *
 * @todo Description
 *
 */
@symbol (
		name = IKeyword.EVENT,
		kind = ISymbolKind.LAYER,
		with_sequence = true,
		concept = { IConcept.GUI })
@inside (
		symbols = { IKeyword.DISPLAY })
@facets (
		value = { @facet (
				name = ILayerStatement.Event.TRIGGER,
				type = { IType.STRING },
				optional = false,
				doc = @doc ("The trigger: a mouse-event constant, a keyboard-event constant, a single character, or an explicit keyboard combination such as 'ctrl+shift+a', 'alt+arrow_up', #ctrl + 'a', or #cmd + '+' + #enter. Keyboard constants include #arrow_down, #arrow_up, #arrow_left, #arrow_right, #escape, #tab, #enter, #page_up, #page_down, #ctrl, #cmd, #shift and #alt. Mouse constants include #mouse_up, #mouse_down, #mouse_click, #mouse_move, #mouse_exit, #mouse_enter, #mouse_menu and #mouse_drag.")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.STRING,
						optional = true,
						doc = @doc ("Type of device used to generate events. Defaults to 'default', which encompasses keyboard and mouse")),
				@facet (
						name = IKeyword.ACTION,
						type = IType.ACTION,
						optional = true,
						doc = @doc ("The identifier of the action to be executed in the context of the simulation. This action needs to be defined in 'global' or in the current experiment, without any arguments. The location of the mouse in the world can be retrieved in this action with the pseudo-constant #user_location")) },
		omissible = ILayerStatement.Event.TRIGGER)
@validator (EventLayerValidator.class)
@doc (
		value = "`" + IKeyword.EVENT
				+ "` allows to interact with the simulation by capturing mouse or key events and doing an action. The name of this action can be defined with the 'action:' facet, in which case the action needs to be defined in 'global' or in the current experiment, without any arguments."
				+ " The location of the mouse in the world can be retrieved in this action with the pseudo-constant #user_location. The statements to execute can also be defined in the block at the end of this statement, in which case they will be executed in the context of the experiment. "
				+ "Keyboard combinations can be written as strings, e.g. 'ctrl+shift+a', 'alt+arrow_up' or 'cmd+enter'. "
				+ "Modifiers (ctrl, shift, alt, cmd) are case-insensitive and must match exactly; modifier order does not matter. "
				+ "For example, 'ctrl+a' does not match CTRL+SHIFT+A; declare 'ctrl+shift+a' for that combination. "
				+ "Existing constants retain their values: #ctrl + 'a' evaluates to 'ctrla', a supported compact spelling of 'ctrl+a'. "
				+ "Likewise #ctrl + #shift + 'a' means 'ctrl+shift+a'. Explicit separators such as #ctrl + '+a' or #alt + '+' + #arrow_up are recommended for readability. "
				+ "The aliases control, command, meta, win and windows are accepted; cmd/win denotes the Command key on macOS and the Windows/Meta key elsewhere, not CTRL. "
				+ "Letters in combinations are case-insensitive; use shift explicitly for shifted letters. Use 'space' for the space key and 'ctrl++' for CTRL with plus. "
				+ "Punctuation follows the character produced by the keyboard layout; include shift in the declaration if producing that character requires it (e.g. 'ctrl+shift++'). "
				+ "The final key can be a single character, space, arrow_up, arrow_down, arrow_left, arrow_right, escape, tab, enter, page_up or page_down. "
				+ "A combination requires at least one modifier and one non-modifier key; duplicate modifiers, unknown keys and mouse-event combinations are errors. "
				+ "Combinations run on key press (including auto-repeat) alongside existing single-key events and built-in shortcuts, without overriding them. "
				+ "Click the display to give it keyboard focus. No key-release event is introduced; holding a key may repeatedly execute its block. "
				+ "Standalone #ctrl, #cmd, #shift and #alt events retain their existing behavior; adding combination events does not change legacy character or special-key dispatch. "
				+ "Shortcuts intercepted by the operating system or workbench may not reach the display. On Windows, CTRL-based events receive an additional warning about conflicts with OpenGL camera interaction. "
				+ "OpenGL camera controls use CMD only on macOS and CTRL only on Windows/Linux; macOS CTRL is not a camera modifier. CTRL-click on macOS retains its contextual-menu behavior. "
				+ "An action defined in 'global' must be called on the simulation from this block (e.g. `ask simulation { do myAction(); }`).",
		usages = { @usage (
				value = "The general syntax is:",
				examples = { @example (
						value = "event [event_type] action: myAction;",
						isExecutable = false) }),
				@usage (
						value = "Keyboard combinations use one or more modifiers and a final key. These forms can be used inside a display; event blocks execute in the experiment context:",
						examples = { @example (
								value = "event 'ctrl+shift+a' { write 'CTRL+SHIFT+A'; }",
								isExecutable = false),
								@example (
										value = "event #ctrl + 'a' { write 'CTRL+A (compact constant syntax)'; }",
										isExecutable = false),
								@example (
										value = "event #alt + '+' + #arrow_up { write 'ALT+UP'; }",
										isExecutable = false),
								@example (
										value = "event #cmd + '+enter' { write 'Command/Windows+Enter'; }",
										isExecutable = false),
								@example (
										value = "event #ctrl + #shift + 'b' { write 'CTRL+SHIFT+B'; }",
										isExecutable = false),
								@example (
										value = "event 'shift+space' { write 'SHIFT+SPACE'; }",
										isExecutable = false),
								@example (
										value = "event 'ctrl+shift++' { write 'CTRL+SHIFT+plus'; }",
										isExecutable = false) }),
				@usage (
						value = "For instance:",
						examples = { @example (
								value = "global {",
								isExecutable = false),
								@example (
										value = "   // ... ",
										isExecutable = false),
								@example (
										value = "   action myAction () {",
										isExecutable = false),
								@example (
										value = "      point loc <- #user_location; // contains the location of the mouse in the world",
										isExecutable = false),
								@example (
										value = "      list<agent> selected_agents <- agents inside (10#m around loc); // contains agents clicked by the event",
										isExecutable = false),
								@example (
										value = "      ",
										isExecutable = false),
								@example (
										value = "      // code written by modelers",
										isExecutable = false),
								@example (
										value = "   }",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false),
								@example (
										value = "",
										isExecutable = false),
								@example (
										value = "experiment Simple type:gui {",
										isExecutable = false),
								@example (
										value = "   display my_display {",
										isExecutable = false),
								@example (
										value = "      event #mouse_up action: myAction;",
										isExecutable = false),
								@example (
										value = "   }",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) },
		see = { IKeyword.DISPLAY, IKeyword.AGENTS, IKeyword.CHART, "graphics", IKeyword.GRID_LAYER,
				IKeyword.IMAGE_LAYER, IKeyword.OVERLAY, IKeyword.SPECIES_LAYER, })
public class EventLayerStatement extends AbstractLayerStatement implements ILayerStatement.Event {

	/**
	 * The Class EventLayerValidator.
	 */
	public static class EventLayerValidator implements IDescriptionValidator<IStatementDescription> {

		@Override
		public void validate(final IStatementDescription description) {
			IExpressionDescription nameDesc = description.getFacet(TRIGGER);
			final String name = nameDesc != null ? nameDesc.getExpression().literalValue() : null;
			if (name == null) {
				description.error("Impossible to find this trigger", IGamlIssue.UNKNOWN_ACTION, ACTION);
				return;
			}
			KeyCombination combination = null;
			if (KeyCombination.isCombination(name)) {
				try {
					combination = KeyCombination.parse(name);
				} catch (final IllegalArgumentException e) {
					description.error(e.getMessage(), IGamlIssue.UNKNOWN_ARGUMENT, TRIGGER);
					return;
				}
			} else if (name.length() > 1) { // If it is not a char
				StringBuilder error = new StringBuilder();
				boolean foundEventName = false;
				for (final IEventLayerDelegate delegate : GamaAdditionRegistry.getEventLayerDelegates()) {
					error.append(delegate.getEvents()).append(" ");
					if (delegate.getEvents().contains(name)) { foundEventName = true; }
				}
				if (!foundEventName) {
					description.error("No event can be triggered for '" + name + "'. Acceptable values are "
							+ error.append(" or a character").toString(), IGamlIssue.UNKNOWN_ARGUMENT, NAME);
					return;
				}
				if (SystemInfo.isLinux() && IKeyword.MOUSE_EXIT.equals(name)) {
					description.warning("'" + IKeyword.MOUSE_EXIT + "' may not fire reliably on Linux systems",
							IGamlIssue.GENERAL, TRIGGER);
				}
			}
			if (SystemInfo.isWindows() && (IEventLayerDelegate.CONTROL_MODIFIER.equals(name)
					|| combination != null && (combination.modifiers() & KeyCombination.CTRL) != 0)
					&& OpenGLSpecificLayerValidator.isOpenGL(description.getEnclosingDescription())) {
				description.warning(
						"On Windows, CTRL-based keyboard events may conflict with camera interaction in OpenGL displays; built-in shortcuts remain active and may intercept the event",
						IGamlIssue.GENERAL, TRIGGER);
			} else if (combination != null) {
				final boolean command = (combination.modifiers()
						& (SystemInfo.isMac() ? KeyCombination.CMD : KeyCombination.CTRL)) != 0;
				final boolean displayKey = switch (combination.key()) {
					case "arrow_up", "arrow_down", "arrow_left", "arrow_right", " ", "+", "-", "4", "6", "8", "2", "k" -> true;
					default -> false;
				};
				if ("escape".equals(combination.key())
						|| command && List.of("o", "t", "p", "r", "x").contains(combination.key())
						|| displayKey && OpenGLSpecificLayerValidator.isOpenGL(description.getEnclosingDescription())) {
					description.warning(
							"This key combination may also activate a built-in display or workbench shortcut, which remains active and may intercept the event",
							IGamlIssue.GENERAL, TRIGGER);
				}
			}

			final String actionName = description.getLitteral(ACTION);
			if (actionName != null) {
				if (actionName.contains(IInternalFacets.SYNTHETIC)) {
					description.warning(
							"This use of 'action' is deprecated. Move the sequence to execute at the end of the 'event' statement instead.",
							IGamlIssue.DEPRECATED, ACTION);
				}
				IActionDescription sd = description.getModelDescription().getAction(actionName);
				if (sd == null) {
					// we look into the experiment
					final IDescription superDesc = description.getTypeContext();
					sd = superDesc.getAction(actionName);
				}
				if (sd == null) {
					description.error("Action '" + actionName + "' is not defined in neither 'global' nor 'experiment'",
							IGamlIssue.UNKNOWN_ACTION, ACTION);
				} else if (sd.getPassedArgs().size() > 0) {
					description.error("Action '" + actionName
							+ "' cannot have arguments. Use '#user_location' inside to obtain the location of the mouse, and compute the selected agents in the action using GAML spatial operators",
							IGamlIssue.DIFFERENT_ARGUMENTS, ACTION);
				}

			}
		}
	}

	/** The executes in simulation. */
	private boolean executesInSimulation;

	/** The type. */
	private final IExpression type;

	/** The action name. */
	private String actionName;

	/** The trigger. */
	private final String trigger;

	/** The action. */
	private ActionStatement action;

	/**
	 * Instantiates a new event layer statement.
	 *
	 * @param desc
	 *            the desc
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public EventLayerStatement(final IDescription desc) throws GamaRuntimeException {
		super(/* context, */desc);
		executesInSimulation = false;
		if (description.hasFacet(IKeyword.ACTION)) {
			actionName = description.getLitteral(IKeyword.ACTION);
			final IActionDescription sd = description.getTypeContext().getAction(actionName);
			executesInSimulation = sd == null;
		}
		trigger = description.getLitteral(TRIGGER);

		type = getFacet(IKeyword.TYPE);
	}

	/**
	 * Gets the executer.
	 *
	 * @param scope
	 *            the scope
	 * @return the executer
	 */
	@Override
	public IAgent getExecuter(final IScope scope) {
		return executesInSimulation ? scope.getSimulation() : scope.getExperiment();
	}

	/**
	 * Executes in simulation.
	 *
	 * @return true, if successful
	 */
	public boolean executesInSimulation() {
		return executesInSimulation;
	}

	/**
	 * Gets the executable.
	 *
	 * @param scope
	 *            the scope
	 * @return the executable
	 */
	@Override
	public IExecutable getExecutable(final IScope scope) {
		if (action != null) return action;
		IAgent agent = getExecuter(scope);
		return agent == null ? null : agent.getSpecies().getAction(actionName);
	}

	@Override
	public boolean _init(final IScope scope) throws GamaRuntimeException {

		final Object source = getSource(scope);

		for (final IEventLayerDelegate delegate : GamaAdditionRegistry.getEventLayerDelegates()) {
			if (delegate.acceptSource(scope, source)) { delegate.createFrom(scope, source, this); }
		}
		return true;
	}

	/**
	 * Gets the type.
	 *
	 * @param output
	 *            the output
	 * @return the type
	 */
	@Override
	public LayerType getType(final IOutput output) {
		return LayerType.EVENT;
	}

	@Override
	public String toString() {
		return "Event layer: " + this.getFacet(TRIGGER).literalValue();
	}

	/**
	 * Method _step()
	 *
	 * @see gama.core.outputs.layers.AbstractLayerStatement#_step(gama.api.runtime.scope.IScope)
	 */
	@Override
	protected boolean _step(final IScope scope) {
		return true;
	}

	/**
	 * Gets the source.
	 *
	 * @param scope
	 *            the scope
	 * @return the source
	 */
	private Object getSource(final IScope scope) {
		return type == null ? IKeyword.DEFAULT : type.value(scope);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		final List<IStatement> statements = new ArrayList<>();
		for (final ISymbol c : commands) { if (c instanceof IStatement) { statements.add((IStatement) c); } }
		if (!statements.isEmpty()) {
			actionName = "inline";
			final IDescription d =
					GAML.getDescriptionFactory().create(IKeyword.ACTION, getDescription(), IKeyword.NAME, "inline");
			action = new ActionStatement(d);
			action.setChildren(statements);
		}
	}

	/**
	 * Gets the trigger.
	 *
	 * @return the trigger
	 */
	@Override
	public String getTrigger() { // TODO Auto-generated method stub
		return trigger;
	}

}

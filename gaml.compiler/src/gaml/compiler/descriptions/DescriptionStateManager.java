/**
 *
 */
package gaml.compiler.descriptions;

import java.util.EnumSet;

/**
 *
 */
public abstract class DescriptionStateManager extends DescriptionErrorManager {

	/**
	 * The Enum Flag.
	 */
	protected enum Flag {

		/** The Abstract. */
		IsAbstract,

		/** The Validated. */
		IsValidated,

		/** The Synthetic. */
		IsSynthetic,

		/** The Starting date defined. */
		IsStartingDateDefined,

		/** The Is control. */
		IsControl,

		/** The Control finalized. */
		IsControlFinalized,

		/** The Can use minimal agents. */
		CanUseMinimalAgents,

		/** The Is super invocation. */
		IsSuperInvocation,

		/** The Breakable. */
		IsBreakable,

		/** The Continuable. */
		IsContinuable,

		/** The Built in. */
		IsBuiltIn,

		/** The Globat. */
		IsGlobal,

		/** The Unmodifiable. */
		IsUnmodifiable,

		/** The Updatable. */
		IsUpdatable,

		/** The Is parameter. */
		IsParameter,

		/** The is mirror. */
		IsMirror,

		/** The is grid. */
		isGrid,

		/** The is contextual type. */
		isContextualType,

		/** The is function. */
		isFunction,

		/** The is memorize. */
		isMemorize,

		/** The is batch. */
		isBatch,
		/** The Is create. */
		IsCreate,

		/**
		 * The No type inference. A flag that signifies that type inference should not be used when computing the type
		 * of the description. Type inference is useful when the type of the description is not known at the time of
		 * parsing, but it should not be used when the type is known. See #385
		 */
		NoTypeInference,
		/** The is void. */
		isVoid
	}

	/**
	 * State flags for this description stored in a compact EnumSet.
	 *
	 * <p>
	 * <strong>Memory Optimization:</strong> EnumSet uses a single long value for up to 64 flags, providing O(1)
	 * operations with minimal memory overhead (~16 bytes vs ~40+ for HashSet).
	 * </p>
	 *
	 * <p>
	 * Common flags include: BuiltIn, Validated, Synthetic, Abstract, NoTypeInference. See {@link Flag} enum for
	 * complete list.
	 * </p>
	 */
	private final EnumSet<Flag> state = EnumSet.noneOf(Flag.class);

	// ---- State management

	/**
	 * Sets a state flag for this description.
	 *
	 * @param flag
	 *            the flag to set
	 */
	protected void set(final Flag flag) {
		state.add(flag);
	}

	/**
	 * Sets a state flag conditionally based on the given condition.
	 *
	 * @param flag
	 *            the flag to set
	 * @param condition
	 *            if true, the flag is set; otherwise, it is unset
	 */
	protected void setIf(final Flag flag, final boolean condition) {
		if (condition) {
			set(flag);
		} else {
			unSet(flag);
		}
	}

	/**
	 * Removes a state flag from this description.
	 *
	 * @param flag
	 *            the flag to unset
	 */
	protected void unSet(final Flag flag) {
		state.remove(flag);
	}

	/**
	 * Checks if a specific flag is set for this description.
	 *
	 * @param flag
	 *            the flag to check
	 * @return true if the flag is set, false otherwise
	 */
	protected boolean isSet(final Flag flag) {
		return state.contains(flag);
	}

	// Helper functions

	/**
	 * Checks if this description is for a built-in symbol.
	 *
	 * @return true if this is a built-in symbol, false otherwise
	 */
	@Override
	public boolean isBuiltIn() { return state.contains(Flag.IsBuiltIn); }

	/**
	 * Checks if this description is synthetic (generated programmatically).
	 *
	 * @return true if this is a synthetic description, false otherwise
	 */
	public boolean isSynthetic() { return state.contains(Flag.IsSynthetic); }

	/**
	 * Checks if is class.
	 *
	 * @return true, if is class
	 */
	@Override
	public boolean isClass() { return false; }

	/**
	 * Checks if is species.
	 *
	 * @return true, if is species
	 */
	@Override
	public boolean isSpecies() { return false; }

	/**
	 * Checks if is experiment.
	 *
	 * @return true, if is experiment
	 */
	@Override
	public boolean isExperiment() { return false; }

	/**
	 * Checks if is model.
	 *
	 * @return true, if is model
	 */
	@Override
	public boolean isModel() { return false; }

	/**
	 * Checks if is skill.
	 *
	 * @return true, if is skill
	 */
	public boolean isSkill() { return false; }

	/**
	 * Checks if is statement.
	 *
	 * @return true, if is statement
	 */
	public boolean isStatement() { return false; }

	/**
	 * Checks if this description is abstract (i.e. it declares at least one abstract action).
	 *
	 * @return true if this description is abstract, false otherwise
	 */
	@Override
	public boolean isAbstract() { return state.contains(Flag.IsAbstract); }

	/**
	 * Checks if this description has already been validated.
	 *
	 * @return true if this description has been validated, false otherwise
	 */
	public boolean isValidated() { return state.contains(Flag.IsValidated); }

	/**
	 * Checks if a starting date has been defined for this description.
	 *
	 * @return true if a starting date is defined, false otherwise
	 */
	public boolean isStartingDateDefined() { return state.contains(Flag.IsStartingDateDefined); }

	/**
	 * Checks if this description represents a control structure (e.g. loop, if, ask).
	 *
	 * @return true if this is a control structure, false otherwise
	 */
	public boolean isControl() { return state.contains(Flag.IsControl); }

	/**
	 * Checks if the control structure represented by this description has been finalized.
	 *
	 * @return true if the control structure is finalized, false otherwise
	 */
	public boolean isControlFinalized() { return state.contains(Flag.IsControlFinalized); }

	/**
	 * Checks if the agents produced by this description can use the minimal agent implementation, which skips some
	 * overhead when no advanced features are required.
	 *
	 * @return true if minimal agents can be used, false otherwise
	 */
	public boolean canUseMinimalAgents() {
		return state.contains(Flag.CanUseMinimalAgents);
	}

	/**
	 * Checks if this description represents a super invocation (i.e. a call to a parent species action).
	 *
	 * @return true if this is a super invocation, false otherwise
	 */
	public boolean isSuperInvocation() { return state.contains(Flag.IsSuperInvocation); }

	/**
	 * Checks if this description is breakable, meaning it can be interrupted by a {@code break} statement.
	 *
	 * @return true if this description is breakable, false otherwise
	 */
	public boolean isBreakable() { return state.contains(Flag.IsBreakable); }

	/**
	 * Checks if this description is continuable, meaning it can be interrupted by a {@code continue} statement.
	 *
	 * @return true if this description is continuable, false otherwise
	 */
	public boolean isContinuable() { return state.contains(Flag.IsContinuable); }

	/**
	 * Checks if this description is global, i.e. it belongs to or represents the world agent.
	 *
	 * @return true if this description is global, false otherwise
	 */
	public boolean isGlobal() { return state.contains(Flag.IsGlobal); }

	/**
	 * Checks if this description is unmodifiable (read-only), meaning its value cannot be changed after initialization.
	 *
	 * @return true if this description is unmodifiable, false otherwise
	 */
	public boolean isUnmodifiable() { return state.contains(Flag.IsUnmodifiable); }

	/**
	 * Checks if this description is updatable, meaning it can be refreshed or recomputed at each simulation step.
	 *
	 * @return true if this description is updatable, false otherwise
	 */
	public boolean isUpdatable() { return state.contains(Flag.IsUpdatable); }

	/**
	 * Checks if this description represents an experiment or model parameter.
	 *
	 * @return true if this description is a parameter, false otherwise
	 */
	public boolean isParameter() { return state.contains(Flag.IsParameter); }

	/**
	 * Checks if this description represents a mirror species, which mirrors the population of another species.
	 *
	 * @return true if this description is a mirror, false otherwise
	 */
	public boolean isMirror() { return state.contains(Flag.IsMirror); }

	/**
	 * Checks if this description represents a grid species.
	 *
	 * @return true if this description is a grid, false otherwise
	 */
	public boolean isGrid() { return state.contains(Flag.isGrid); }

	/**
	 * Checks if this description uses a contextual type, i.e. a type that depends on the enclosing context rather than
	 * being statically declared.
	 *
	 * @return true if this description has a contextual type, false otherwise
	 */
	public boolean isContextualType() { return state.contains(Flag.isContextualType); }

	/**
	 * Checks if this description represents a functional attribute (one whose value is recomputed on each access).
	 *
	 * @return true if this description is a function, false otherwise
	 */
	public boolean isFunction() { return state.contains(Flag.isFunction); }

	/**
	 * Checks if this description is marked as memorize, meaning its value is saved and restored across simulation
	 * steps.
	 *
	 * @return true if this description is memorized, false otherwise
	 */
	public boolean isMemorize() { return state.contains(Flag.isMemorize); }

	/**
	 * Checks if this description belongs to or represents a batch experiment.
	 *
	 * @return true if this description is a batch, false otherwise
	 */
	public boolean isBatch() { return state.contains(Flag.isBatch); }

	/**
	 * Checks if this description represents a {@code create} statement.
	 *
	 * @return true if this description is a create statement, false otherwise
	 */
	public boolean isCreate() { return state.contains(Flag.IsCreate); }

	/**
	 * Checks if the return type of this description is void, i.e. the absence of return value.
	 *
	 * @return true, if is void
	 */
	public boolean isVoid() { return state.contains(Flag.isVoid); }

	/**
	 * Checks if this description represents an action invocation (e.g., a "do" statement). Default implementation
	 * returns false; subclasses may override.
	 *
	 * @return true if this is an invocation, false otherwise
	 */
	@Override
	public boolean isInvocation() { return false; }

	/**
	 * Checks if type inference is disabled for this description. When set, the declared type is used as-is and no
	 * automatic type inference is performed. See issue #385.
	 *
	 * @return true if type inference is disabled, false otherwise
	 */
	public boolean isNoTypeInference() { return state.contains(Flag.NoTypeInference); }

	/**
	 * Checks if is experiment parameter.
	 *
	 * @return true, if is experiment parameter
	 */
	public boolean isExperimentParameter() { return false; }

}

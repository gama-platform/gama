package gaml.compiler.ui.wizards.behavior;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Behaviour declarations supported by the shared form, with the semantics of their compiler declarations:
 * {@code reflex} (ReflexStatement), {@code state} (fsm), {@code task} (weighted tasks), {@code plan} and
 * {@code perceive} (BDI behaviours), {@code rule} (BDI rule, a single statement without code) and the lifecycle
 * variants of reflexes, {@code init} (run after creation) and {@code abort} (run before the agent dies), which share
 * the reflex declaration (optional name, {@code when} facet, no cardinality limit).
 */
public enum BehaviorKind {

	REFLEX("reflex", false, true),
	STATE("state", true, true),
	TASK("task", true, true),
	PLAN("plan", false, true),
	PERCEIVE("perceive", false, true),
	RULE("rule", false, false),
	INIT("init", false, true),
	ABORT("abort", false, true);

	/** The kinds sharing the reflex declaration, convertible into each other. */
	public static final Set<BehaviorKind> LIFECYCLE = Collections.unmodifiableSet(EnumSet.of(REFLEX, INIT, ABORT));

	private final String keyword;
	private final boolean nameRequired;
	private final boolean hasBody;

	BehaviorKind(final String keyword, final boolean nameRequired, final boolean hasBody) {
		this.keyword = keyword;
		this.nameRequired = nameRequired;
		this.hasBody = hasBody;
	}

	/** The GAML keyword. */
	public String keyword() { return keyword; }

	/** Whether the compiler requires a name (non-optional {@code name} facet). */
	public boolean nameRequired() { return nameRequired; }

	/** Whether the declaration has a block of statements ({@code with_sequence}); false for {@code rule}. */
	public boolean hasBody() { return hasBody; }

	/** The kinds this one can be converted to without losing anything (itself included). */
	public Set<BehaviorKind> convertibleKinds() {
		return LIFECYCLE.contains(this) ? LIFECYCLE : Collections.unmodifiableSet(EnumSet.of(this));
	}

	/** The kind of a keyword, or null when it is not a supported behaviour. */
	public static BehaviorKind of(final String keyword) {
		for (BehaviorKind kind : values()) if (kind.keyword.equals(keyword)) return kind;
		return null;
	}

	@Override
	public String toString() { return keyword; }
}

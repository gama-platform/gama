/**
* Name: Class Definition and Instantiation
* Author: Alexis Drogoul
* Description:
*   First in a series of short focused models exploring the class/object
*   additions to GAML introduced in GAMA 2026.
*
*   This model concentrates on the two most fundamental operations:
*     1. Declaring a class with attributes and typed actions (methods).
*     2. Instantiating objects using the constructor syntax ClassName(attr: val, …).
*
*   Key take-aways:
*     • A "class" block defines a pure data/logic type — no agent, no population.
*     • Attributes are declared exactly like species variables, with optional
*       default values.
*     • A constructor call ClassName(attr1: v1, attr2: v2) creates a new object
*       and initialises only the listed attributes; the rest keep their defaults.
*     • Objects are ordinary GAML values: they can be stored in variables, put
*       in lists, passed as arguments, and returned from actions.
*     • The built-in root class "object" is the implicit parent of every class
*       that does not declare an explicit parent.
*
* Tags: class, object, instantiation, constructor, GAML, OOP
*/
model ClassDefinitionAndInstantiation

// ─────────────────────────────────────────────────────────────────────────────
// 1. Class declaration
//    The "class" keyword introduces a new object type.  Attributes use the
//    same "<type> <name> <- <default>" syntax as species variables.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A simple class modelling a 2-D point in space. Compared to 'point' this class provides a mutable structure
 *
 * This class has no explicit parent, so it implicitly extends the built-in
 * {@code object} root class.  It contains two numeric attributes and two
 * typed actions (methods) that operate on them.
 *
 * Attributes:
 * <ul>
 *   <li>{@code x} – the horizontal coordinate, defaulting to 0.0.</li>
 *   <li>{@code y} – the vertical coordinate, defaulting to 0.0.</li>
 * </ul>
 */
class point2d {

	/** Horizontal coordinate.  Defaults to 0.0 if not supplied at construction time. */
	float x <- 0.0;

	/** Vertical coordinate.  Defaults to 0.0 if not supplied at construction time. */
	float y <- 0.0;

	/**
	 * Returns the Euclidean distance from this point to another point {@code other}.
	 *
	 * @param other  the target point
	 * @return the Euclidean distance as a float
	 */
	float distance_to(point2d other) {
		return sqrt((x - other.x) ^ 2 + (y - other.y) ^ 2);
	}

	/**
	 * Returns a human-readable representation of this point in the form "(x, y)".
	 *
	 * @return a string of the form "(x, y)"
	 */
	string to_string() {
		return "(" + x + ", " + y + ")";
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. A second, independent class
//    Classes can hold attributes of other user-defined class types, showing
//    that object composition works naturally in GAML.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A named, coloured segment defined by two {@code point2d} end-points.
 *
 * Demonstrates object composition: a class whose attributes are themselves
 * instances of another user-defined class.
 *
 * Attributes:
 * <ul>
 *   <li>{@code label}  – a textual name for the segment, defaulting to {@code "seg"}.</li>
 *   <li>{@code start}  – the first end-point (a {@code point2d} object).</li>
 *   <li>{@code end_pt} – the second end-point (a {@code point2d} object).</li>
 *   <li>{@code color}  – display colour, defaulting to {@code #gray}.</li>
 * </ul>
 */
class segment2d {

	/** A textual name identifying this segment. */
	string label <- "seg";

	/** The start end-point of the segment.  Defaults to the origin. */
	point2d start  <- point2d();

	/** The end end-point of the segment.  Defaults to the origin. */
	point2d end_pt <- point2d();

	/** Display colour for this segment. */
	rgb color <- #gray;

	/**
	 * Returns the Euclidean length of this segment by delegating to
	 * {@code point2d.distance_to}.
	 *
	 * @return the length as a float
	 */
	float length() {
		return start.distance_to(end_pt);
	}

	/**
	 * Returns a human-readable description: the label plus the coordinates
	 * of both end-points and the computed length.
	 *
	 * @return a descriptive string
	 */
	string describe() {
		return label + ": " + start.to_string() + " → " + end_pt.to_string()
		       + "  length=" + (length() with_precision 2);
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Global – exercises instantiation
// ─────────────────────────────────────────────────────────────────────────────

global {

	init {

		// ── Constructor syntax ──────────────────────────────────────────────
		// ClassName(attr1: v1, attr2: v2)
		// Only the listed attributes are overridden; the rest keep defaults.

		write "══ 1. Basic instantiation ══════════════════════════════════";

		// Point with both coordinates supplied
		point2d p1 <- point2d(x: 3.0, y: 4.0);
		write "p1 = " + p1.to_string();           // (3.0, 4.0)

		// Point with only x supplied — y stays 0.0
		point2d p2 <- point2d(x: 6.0);
		write "p2 = " + p2.to_string();           // (6.0, 0.0)

		// Default point — both x and y stay 0.0
		point2d origin <- point2d();
		write "origin = " + origin.to_string();   // (0.0, 0.0)

		// ── Calling actions on the objects ─────────────────────────────────

		write "══ 2. Calling actions on objects ═══════════════════════════";

		write "distance(p1, p2)    = " + (p1.distance_to(p2) with_precision 3);
		write "distance(p1, origin)= " + (p1.distance_to(origin) with_precision 3);

		// ── Object composition ──────────────────────────────────────────────

		write "══ 3. Object composition ═══════════════════════════════════";

		segment2d seg_a <- segment2d(
			label:  "A",
			start:  point2d(x: 0.0, y: 0.0),
			end_pt: point2d(x: 3.0, y: 4.0),
			color:  #blue
		);
		write seg_a.describe();

		segment2d seg_b <- segment2d(
			label:  "B",
			start:  point2d(x: 1.0, y: 1.0),
			end_pt: point2d(x: 4.0, y: 5.0),
			color:  #red
		);
		write seg_b.describe();

		// ── Objects in a list ───────────────────────────────────────────────

		write "══ 4. Objects in a list ════════════════════════════════════";

		list<point2d> pts <- [
			point2d(x: 1.0, y: 0.0),
			point2d(x: 2.0, y: 0.0),
			point2d(x: 4.0, y: 0.0),
			point2d(x: 7.0, y: 0.0)
		];

		write "Points: " + (pts collect each.to_string());

		// Compute consecutive distances
		list<float> gaps;
		loop i from: 0 to: length(pts) - 2 {
			gaps <+ (pts[i].distance_to(pts[i + 1]) with_precision 2);
		}
		write "Consecutive gaps: " + gaps;

		// ── Checking that an uninitialized object variable is nil ──────────

		write "══ 5. Nil objects ═══════════════════════════════════════════";
		point2d maybe_nil;
		write "maybe_nil is nil? " + (maybe_nil = nil);   // true

		// Safe nil check before use
		if maybe_nil != nil {
			write maybe_nil.to_string();
		} else {
			write "Skipping nil object — safe guard works correctly.";
		}
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// Experiment
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Minimal GUI experiment — the model produces only console output.
 * Run it and observe the output in the Console view.
 */
experiment "Class Definition and Instantiation" type: gui {
	output {
		// No display needed; all output goes to the console via write
	}
}

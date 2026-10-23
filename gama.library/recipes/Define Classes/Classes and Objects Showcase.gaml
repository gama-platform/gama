/**
* Name: Classes and Objects Showcase
* Author: Alexis Drogoul
* Description:
*   A comprehensive showcase of the new GAML class/object system introduced in GAMA 2026.
*   Classes are a new first-class citizen alongside species: they describe pure data/logic
*   objects that have NO spatial location, NO population, and are NOT agents.  They are
*   plain value objects, similar to classes in traditional OOP languages.
*
*   Topics illustrated in this model:
*     1. class declaration — attributes + typed actions (methods)
*     2. virtual (abstract) classes — cannot be instantiated, define a contract
*     3. virtual (abstract) actions — must be implemented by every concrete subclass
*     4. concrete inheritance — a subclass extending a virtual or concrete parent
*     5. multi-level inheritance — grandchild → child → abstract root
*     6. super / invoke — calling the parent implementation from a child
*     7. instantiation — creating objects with the class-name(…) constructor syntax
*     8. dot-notation — accessing attributes and calling actions on an object
*     9. isInstanceOf — runtime type introspection on objects
*    10. agents using objects — a species that owns and manipulates class instances
*    11. lists of objects — collections of objects created and sorted by agents
*    12. virtual experiments — shared parameters across multiple experiments
*
* Tags: class, object, inheritance, virtual, abstract, instantiation, OOP, GAML
*/
model ClassesAndObjectsShowcase 
 
// ════════════════════════════════════════════════════════════════════════════
// 1.  ABSTRACT BASE CLASS  –  shape
//     virtual: true  →  cannot be instantiated; acts as the root of the
//     geometry hierarchy.  Every concrete subclass must implement area().
// ════════════════════════════════════════════════════════════════════════════

/**
 * Abstract root class for all geometric shapes.
 * Declares the shared {@code label} and {@code color_name} attributes, and
 * the abstract action {@code area()} that every concrete subclass must
 * implement.  Being virtual, {@code shape} cannot be instantiated directly.
 */
class shape_abstract_class virtual: true {

	/** Human-readable label for this shape. */
	string label <- "shape";

	/** Fill colour used when drawing the shape (as a colour name string). */
	string color_name <- "gray";

	/**
	 * Returns the area of this shape.
	 * Virtual: every concrete subclass must provide its own body.
	 */
	float area() virtual: true;

	/**
	 * Returns a printable description of this shape.
	 * Concrete: shared by all subclasses; may be overridden.
	 * Calls {@code area()} polymorphically so the right formula is always used.
	 */
	string describe() {
		return label + " [color=" + color_name + ", area=" + (area() with_precision 2) + "]";
	}
}

// ════════════════════════════════════════════════════════════════════════════
// 2.  CONCRETE SUBCLASS  –  rectangle   (parent: shape)
// ════════════════════════════════════════════════════════════════════════════

/**
 * A concrete rectangle.  Extends {@code shape} and provides a body for
 * {@code area()}.  Adds {@code width}, {@code height} and {@code perimeter()}.
 * Overrides {@code describe()} using {@code super.describe()}.
 */
class rectangle_concrete_class parent: shape_abstract_class {

	/** Width of the rectangle. */
	float width  <- 1.0;

	/** Height of the rectangle. */
	float height <- 1.0;

	/** Overrides the parent label to reflect the concrete type. */
	string label <- "rectangle";

	/**
	 * Concrete implementation: area = width × height.
	 */
	float area() {
		return width * height;
	}

	/**
	 * Returns the perimeter of this rectangle (2 × (width + height)).
	 * Specific to {@code rectangle} and its subclasses.
	 */
	float perimeter() {
		return 2.0 * (width + height);
	}

	/**
	 * Overrides {@code shape.describe()} to append perimeter information.
	 * Uses {@code super.describe()} to avoid duplicating the parent logic.
	 */
	string describe() {
		return super.describe() + " perimeter=" + (perimeter() with_precision 2);
	}
}

// ════════════════════════════════════════════════════════════════════════════
// 3.  CONCRETE GRANDCHILD  –  square   (parent: rectangle → shape)
//     Demonstrates three-level inheritance and the use of super.
// ════════════════════════════════════════════════════════════════════════════

/**
 * A square is a special rectangle whose width and height are always equal.
 * Demonstrates three-level inheritance: square → rectangle → shape.
 *
 * Uses {@code super.area()} to delegate to the rectangle formula,
 * and {@code super.describe()} to build its description from the parent chain.
 * Also carries its own {@code side} attribute for readability.
 */
class square_concrete_class parent: rectangle_concrete_class {

	/** Side length — kept in sync with width/height at creation time. */
	float side <- 1.0;

	/** Overrides the parent label. */
	string label <- "square";

	/**
	 * Delegates to {@code rectangle.area()} via {@code super}.
	 * The formula is identical (width × height) but the call is explicit.
	 */
	float area() {
		return super.area();
	}

	/**
	 * Prepends "SQUARE" to the parent description chain.
	 * {@code super.describe()} resolves to {@code rectangle.describe()},
	 * which itself calls {@code super.describe()} → {@code shape.describe()}.
	 */
	string describe() {
		return "SQUARE " + super.describe();
	}
}

// ════════════════════════════════════════════════════════════════════════════
// 4.  SECOND CONCRETE SUBCLASS  –  circle_shape   (parent: shape)
//     Illustrates that multiple classes can share the same abstract root.
// ════════════════════════════════════════════════════════════════════════════

/**
 * A concrete circle.  Extends {@code shape} independently of
 * {@code rectangle}, showing that multiple concrete classes can share the
 * same abstract parent.  Adds {@code radius} and {@code circumference()}.
 */
class circle_concrete_class parent: shape_abstract_class {

	/** Radius of the circle. */
	float radius <- 1.0;

	/** Overrides label. */
	string label <- "circle";

	/**
	 * Concrete implementation: area = π × r².
	 */
	float area() {
		return #pi * radius ^ 2;
	}

	/**
	 * Returns the circumference: 2 × π × radius.
	 * This action is specific to circles and not part of the {@code shape} contract.
	 */
	float circumference() {
		return 2.0 * #pi * radius;
	}
}

// ════════════════════════════════════════════════════════════════════════════
// 5.  STANDALONE UTILITY CLASS  –  stats_record
//     A general-purpose value-object class unrelated to the shape hierarchy.
//     Parent is implicitly the built-in root class {@code object}.
// ════════════════════════════════════════════════════════════════════════════

/**
 * A plain value-object used by agents to accumulate running statistics.
 * Has no explicit parent, so it directly extends the built-in {@code object}
 * root class.  Demonstrates that classes are not restricted to geometry.
 *
 * Attributes track count, total, min and max; actions allow adding a sample
 * and querying the mean or a formatted summary.
 */
class stats_record {

	/** Label identifying what is being measured. */
	string metric    <- "none";

	/** Number of samples collected so far. */
	int    count     <- 0;

	/** Running sum of all observed values. */
	float  total     <- 0.0;

	/** Minimum observed value (initialised to +∞). */
	float  min_val   <- #infinity;

	/** Maximum observed value (initialised to −∞). */
	float  max_val   <- -#infinity;

	/**
	 * Adds a new sample {@code v} to the running statistics, updating
	 * count, total, min_val and max_val accordingly.
	 *
	 * @param v  the new observed value
	 */
	action add_sample(float v) {
		count   <- count + 1;
		total   <- total + v;
		if v < min_val { min_val <- v; }
		if v > max_val { max_val <- v; }
	}

	/**
	 * Returns the arithmetic mean of all samples, or 0 if no sample has
	 * been added yet.
	 */
	float mean() {
		return count = 0 ? 0.0 : total / count;
	}

	/**
	 * Returns a human-readable one-line summary of the statistics.
	 */
	string summary() {
		return metric + ": n=" + count
			+ "  mean=" + (mean()    with_precision 2)
			+ "  min="  + (min_val  with_precision 2)
			+ "  max="  + (max_val  with_precision 2);
	}
}

// ════════════════════════════════════════════════════════════════════════════
// 6.  GLOBAL
//     Exercises the class/object system from the global initialisation block.
// ════════════════════════════════════════════════════════════════════════════

global {

	/** Number of painter agents to create. */
	int nb_painters <- 10;

	/** World boundaries — a square of side 100. */
	geometry shape <- square(100);

	init {
		// ── 6a. Direct instantiation in the global scope ────────────────────
		//        Syntax: ClassName(attribute: value, …)

		rectangle_concrete_class r1 <- rectangle_concrete_class(width: 4.0, height: 3.0, color_name: "blue");
		write "─── Instantiation examples ───────────────────────────────";
		write "r1 → " + r1.describe();
		write "     area      = " + r1.area();
		write "     perimeter = " + r1.perimeter();

		square_concrete_class s1 <- square_concrete_class(side: 5.0, width: 5.0, height: 5.0, color_name: "red");
		write "s1 → " + s1.describe();
		write "     area      = " + s1.area();

		circle_concrete_class c1 <- circle_concrete_class(radius: 3.0, color_name: "green");
		write "c1 → " + c1.describe();
		write "     circumference = " + (c1.circumference() with_precision 2);

		// ── 6b. Runtime type introspection ──────────────────────────────────
		//        isInstanceOf(class)  — works on objects just like on agents

		write "─── isInstanceOf checks ──────────────────────────────────";
		write "r1 isInstanceOf rectangle  → " + (r1 is rectangle_concrete_class);
		write "r1 isInstanceOf shape      → " + (r1 is shape_abstract_class);
		write "s1 isInstanceOf rectangle  → " + (s1 is rectangle_concrete_class);  // true: square IS a rectangle
		write "s1 isInstanceOf shape      → " + (s1 is shape_abstract_class);      // true: transitively
		write "c1 isInstanceOf rectangle  → " + (c1 is rectangle_concrete_class);  // false
		write "c1 isInstanceOf shape      → " + (c1 is shape_abstract_class);      // true

		// ── 6c. Polymorphic list of objects ─────────────────────────────────

		list<shape_abstract_class> all_shapes <- [
			rectangle_concrete_class(width: 2.0,  height: 5.0,  color_name: "cyan"),
			circle_concrete_class(radius: 2.5, color_name: "magenta"),
			square_concrete_class(side: 4.0, width: 4.0, height: 4.0, color_name: "yellow"),
			rectangle_concrete_class(width: 6.0,  height: 1.5,  color_name: "orange")
		];

		write "─── Polymorphic list sorted by area (ascending) ──────────";
		list<shape_abstract_class> by_area <- all_shapes sort_by each.area();
		loop sh over: by_area {
			//write "  " + sh.describe();
		}

		// ── 6d. stats_record as a standalone value object ───────────────────

		stats_record sr <- stats_record(metric: "random samples");
		loop i from: 1 to: 20 {
			sr. add_sample(rnd(10.0)); 
		}
		write "─── Stats record after 20 random samples ─────────────────";
		write "  " + sr.summary();

		// ── 6e. Create painter agents ─────────────────────────────────────────
		create painter number: nb_painters;
	}
}

// ════════════════════════════════════════════════════════════════════════════
// 7.  SPECIES  –  painter
//     An agent that owns a list of shape objects and a stats_record.
//     Illustrates agents and class instances coexisting and cooperating.
// ════════════════════════════════════════════════════════════════════════════

/**
 * A painter agent that maintains a dynamic collection of geometric shape
 * objects and a {@code stats_record} accumulator.
 *
 * Key demonstrations:
 * <ul>
 *   <li>Agent attributes typed with a user-defined class ({@code stats_record})
 *       and a list of instances of a virtual class ({@code list<shape>}).</li>
 *   <li>Calling actions on objects via dot-notation from within an agent.</li>
 *   <li>Using {@code isInstanceOf} to filter a list of polymorphic objects.</li>
 *   <li>Dynamic creation and removal of objects during simulation.</li>
 * </ul>
 */
species painter skills: [moving] {

	/** The personal shape collection of this painter. */
	list<shape_abstract_class> my_shapes <- [];

	/** Running statistics over the areas of all shapes ever added. */
	stats_record my_stats <- stats_record(metric: name);

	/** Maximum number of shapes held at any one time. */
	int max_shapes <- 5;

	// ── Initialisation ─────────────────────────────────────────────────────

	init {
		loop i from: 1 to: (2 + rnd(max_shapes - 2)) {
			do add_random_shape();
		}
	}

	// ── Actions ────────────────────────────────────────────────────────────

	/**
	 * Creates one randomly chosen shape (rectangle, circle or square),
	 * appends it to {@code my_shapes}, and records its area in
	 * {@code my_stats}.
	 */
	action add_random_shape() {
		shape_abstract_class new_shape;
		int kind <- rnd(2);
		if kind = 0 {
			new_shape <- rectangle_concrete_class(
				width:      1.0 + rnd(8.0),
				height:     1.0 + rnd(8.0),
				color_name: "blue"
			);
		} else if kind = 1 {
			new_shape <- circle_concrete_class(
				radius:     0.5 + rnd(4.5),
				color_name: "red"
			);
		} else {
			float s <- 1.0 + rnd(5.0);
			new_shape <- square_concrete_class(side: s, width: s, height: s, color_name: "green");
		}
		my_shapes <+ new_shape;
		// Ask the stats_record object to record the new area
		my_stats.add_sample(new_shape.area()); 
	}

	/**
	 * Returns the sum of the areas of all shapes in the collection.
	 * Calls {@code area()} polymorphically on each shape object.
	 */
	float total_area() {
		return my_shapes sum_of each.area();
	}

	/**
	 * Returns how many shapes in the collection are circles,
	 * using {@code isInstanceOf} for runtime type testing.
	 */
	int count_circles() {
		return length(my_shapes select (each is circle_concrete_class));
	}

	// ── Reflexes ───────────────────────────────────────────────────────────

	/** Move randomly each cycle. */
	reflex move {
		do wander(amplitude: 60.0);
	}

	/**
	 * Every 5 cycles: discard the smallest shape and add a new random one.
	 * Demonstrates that objects can be freely removed from and added to lists.
	 */
	reflex refresh when: (cycle mod 5) = 0 {
		if !empty(my_shapes) {
			shape_abstract_class smallest <- my_shapes with_min_of each.area();
			my_shapes >>- smallest;
		}
		do add_random_shape();
		if length(my_shapes) > max_shapes {
			my_shapes <- max_shapes last my_shapes;
		}
	}

	// ── Aspect ─────────────────────────────────────────────────────────────

	/**
	 * Draws the painter as a circle whose radius reflects its total shape
	 * area (scaled down), with the circle count printed above.
	 */
	aspect default {
		float r <- max(1.0, sqrt(total_area()) * 0.5);
		draw circle(r) color: rnd_color(200) border: #black;
		draw string(count_circles()) at: location + {0, -r - 0.5}
			color: #white size: 2.0;
	}
}

// ════════════════════════════════════════════════════════════════════════════
// 8.  VIRTUAL EXPERIMENT  –  base_params
//     Abstract: defines shared parameters used by both concrete experiments.
// ════════════════════════════════════════════════════════════════════════════

/**
 * Abstract experiment that centralises the model parameter declarations.
 * Cannot be launched directly; both concrete experiments below inherit from it
 * via {@code parent: base_params}.
 */
experiment base_params virtual: true {
	parameter "Number of painters" var: nb_painters min: 1 max: 100 category: "Setup";
}

// ════════════════════════════════════════════════════════════════════════════
// 9.  CONCRETE EXPERIMENTS  –  inherit from base_params
// ════════════════════════════════════════════════════════════════════════════

/**
 * Main GUI experiment.
 * Displays the painter agents and a live chart of shape statistics.
 * Inherits the "Number of painters" parameter from {@code base_params}.
 */
experiment "Classes and Objects" type: gui parent: base_params {

	float minimum_cycle_duration <- 0.05;

	output {
		layout #split;

		display "Painters" type: 2d antialias: true background: #black {
			species painter;
		}

		display "Shape statistics" type: 2d {
			chart "Shape stats per cycle" type: series background: #white {
				data "Total shapes held"
					value: painter sum_of length(each.my_shapes)
					color: #steelblue;
				data "Total circles"
					value: painter sum_of each.count_circles()
					color: #firebrick;
				data "Mean total area (÷10)"
					value: (empty(painter) ? 0.0
						: mean(painter collect each.total_area())) / 10.0
					color: #forestgreen;
			}
		}
	}
}

/**
 * Diagnostic experiment.
 * Prints detailed object-level information to the console every 10 cycles,
 * showcasing calls to actions on objects stored inside agents.
 * Inherits the "Number of painters" parameter from {@code base_params}.
 */
experiment "Object diagnostics" type: gui parent: base_params {

	float minimum_cycle_duration <- 0.2;

	/**
	 * Prints a per-agent summary every 10 cycles.
	 * Calls {@code my_stats.summary()} — an action on a class instance stored
	 * inside an agent — to illustrate the agent/object collaboration.
	 */
	reflex diagnose when: (cycle mod 10) = 0 and cycle > 0 {
		write "═══ Cycle " + cycle + " ══════════════════════════════════════";
		ask painter {
			write "  " + name
				+ "  shapes=" + length(my_shapes)
				+ "  circles=" + count_circles()
				+ "  total_area=" + (total_area() with_precision 1);
			write "    stats → " + my_stats.summary();
		}
	}

	output {
		display "Painters" type: 2d antialias: true background: #black {
			species painter;
		}
	}
}

/**
* Name: Inheritance
* Author: Alexis Drogoul
* Description:
*   Third in a series of short focused models exploring the class/object
*   additions to GAML introduced in GAMA 2026.
*
*   This model concentrates on class inheritance:
*     1. Declaring a parent class with "parent: ClassName".
*     2. Inheriting attributes and actions from the parent.
*     3. Overriding (redefining) actions in a child class.
*     4. Calling the parent implementation using "super.action()".
*     5. Calling an inherited procedure using "invoke action(args)".
*     6. Multi-level inheritance (grandparent → parent → child).
*     7. Runtime type testing with the "is" operator.
*     8. Storing objects of different sub-types in a single list.
*
*   Key take-aways:
*     • "parent: C" makes the new class extend class C.
*     • All attributes and actions of C are available in the child.
*     • Redefining an action with the same signature overrides it.
*     • super.action(args) calls the parent's version of a typed action.
*     • invoke action(args) calls the parent's version of a void procedure.
*     • "obj is ClassName" returns true when obj is an instance of that class
*       or any of its subclasses (Liskov / polymorphism).
*
* Tags: class, object, inheritance, override, super, invoke, polymorphism, GAML, OOP
*/
model ClassInheritance

// ─────────────────────────────────────────────────────────────────────────────
// 1. Root class  –  vehicle
//    Defines the shared contract: name, speed, a typed summary() action, and
//    a typed action move() that can be overridden.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Base class for all vehicles.  Holds the common attributes and provides
 * default implementations of {@code move()} and {@code summary()}.
 *
 * Attributes:
 * <ul>
 *   <li>{@code name}          – vehicle identifier, defaulting to "vehicle".</li>
 *   <li>{@code speed}         – current speed in km/h, defaulting to 0.0.</li>
 *   <li>{@code max_speed}     – maximum allowed speed in km/h, defaulting to 120.0.</li>
 *   <li>{@code distance_km}   – total distance travelled, defaulting to 0.0.</li>
 * </ul>
 */
class vehicle {

	/** Identifier for this vehicle. */
	string name <- "vehicle";

	/** Current speed in km/h. */
	float speed <- 0.0;

	/** Maximum speed this vehicle can reach, in km/h. */
	float max_speed <- 120.0;

	/** Cumulative distance travelled since creation, in km. */
	float distance_km <- 0.0;

	/**
	 * Moves the vehicle for one time-step, adding {@code speed} km to
	 * {@code distance_km}.  Child classes may override this.
	 *
	 * @return the distance covered in this step
	 */
	float move() {
		distance_km <- distance_km + speed;
		return speed;
	}

	/**
	 * Accelerates the vehicle by {@code delta} km/h, capped at
	 * {@code max_speed}.  Uses {@code invoke} internally to show how
	 * procedures can be structured; not overridden by children here.
	 *
	 * @param delta  the speed increase in km/h (ignored if ≤ 0)
	 */
	action accelerate(float delta) {
		if delta > 0.0 {
			speed <- min(speed + delta, max_speed);
		}
	}

	/**
	 * Returns a one-line summary of the vehicle state.
	 * Child classes override this to append type-specific information.
	 *
	 * @return descriptive string
	 */
	string summary() {
		return "[vehicle] " + name
		       + "  speed=" + (speed with_precision 1)
		       + "  dist="  + (distance_km with_precision 1);
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. First child  –  car   (parent: vehicle)
//    Adds fuel, overrides move() to consume fuel, overrides summary().
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A car.  Extends {@code vehicle} with a fuel tank.  Overrides
 * {@code move()} to consume fuel, and stops when the tank is empty.
 * Overrides {@code summary()} to append fuel information, using
 * {@code super.summary()} to avoid duplicating the base text.
 *
 * Attributes (in addition to those inherited from {@code vehicle}):
 * <ul>
 *   <li>{@code fuel}             – remaining fuel in litres, defaulting to 50.0.</li>
 *   <li>{@code consumption_per_km} – litres consumed per km, defaulting to 0.08.</li>
 * </ul>
 */
class car parent: vehicle {

	/** Override the default name. */
	string name <- "car";

	/** Remaining fuel in litres. */
	float fuel <- 50.0;

	/** Fuel consumption in litres per km. */
	float consumption_per_km <- 0.08;

	/**
	 * Overrides {@code vehicle.move()}.
	 * Checks fuel before moving; stops (speed = 0) if the tank is empty.
	 * Calls {@code super.move()} to perform the actual distance update.
	 *
	 * @return distance covered (0.0 if out of fuel)
	 */
	float move() {
		if fuel <= 0.0 {
			speed <- 0.0;
			return 0.0;
		}
		float d <- super.move();                       // parent handles distance
		fuel <- max(0.0, fuel - d * consumption_per_km);
		return d;
	}

	/**
	 * Returns a refuel action, topping up the tank to {@code capacity}.
	 *
	 * @param capacity  the full-tank capacity in litres
	 */
	action refuel(float capacity) {
		fuel <- capacity;
	}

	/**
	 * Overrides {@code vehicle.summary()} to append fuel information.
	 * Calls {@code super.summary()} to reuse the base text.
	 *
	 * @return descriptive string
	 */
	string summary() {
		return super.summary() + "  fuel=" + (fuel with_precision 1) + "L";
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Second child  –  bicycle   (parent: vehicle)
//    Much simpler: no fuel, lower max_speed, overrides summary only.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A bicycle.  Extends {@code vehicle} with a much lower {@code max_speed}
 * cap and a rider name.  Does NOT override {@code move()} — the parent
 * implementation is used as-is.  Overrides {@code summary()}.
 *
 * Attributes (in addition to those inherited from {@code vehicle}):
 * <ul>
 *   <li>{@code rider} – the name of the rider, defaulting to "nobody".</li>
 * </ul>
 */
class bicycle parent: vehicle {

	/** Override defaults for a bicycle. */
	string name <- "bicycle";

	/** Bicycles are slower than cars. */
	float max_speed <- 30.0;

	/** Name of the rider. */
	string rider <- "nobody";

	/**
	 * Overrides {@code vehicle.summary()} to include the rider name.
	 * Inherits {@code move()} unchanged from {@code vehicle}.
	 *
	 * @return descriptive string
	 */
	string summary() {
		return super.summary() + "  rider=" + rider;
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. Grandchild  –  sports_car   (parent: car → vehicle)
//    Multi-level inheritance, overrides move() again, overrides summary().
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A sports car.  Extends {@code car} (which itself extends {@code vehicle}),
 * demonstrating three-level inheritance.
 *
 * It has a higher {@code max_speed}, a turbo-boost attribute, and overrides
 * {@code move()} to apply a boost multiplier when turbo is active, then
 * delegates to the parent via {@code super.move()}.
 *
 * Attributes (in addition to those inherited from {@code car} and {@code vehicle}):
 * <ul>
 *   <li>{@code turbo_active}  – whether the turbo-boost is currently on.</li>
 *   <li>{@code boost_factor}  – speed multiplier when turbo is active, defaulting to 1.5.</li>
 * </ul>
 */
class sports_car parent: car {

	/** Override defaults. */
	string name <- "sports_car";

	/** Sports cars go faster. */
	float max_speed <- 250.0;

	/** Whether the turbo-boost is currently engaged. */
	bool turbo_active <- false;

	/** Multiplier applied to the effective speed when turbo is on. */
	float boost_factor <- 1.5;

	/**
	 * Overrides {@code car.move()}.
	 * When turbo is active, temporarily multiplies the speed by
	 * {@code boost_factor} before delegating to the parent move
	 * (which in turn calls its own super chain).
	 * The boosted speed is only in effect for this single step.
	 *
	 * @return the actual distance covered
	 */
	float move() {
		if turbo_active {
			float normal_speed <- speed;
			speed <- min(speed * boost_factor, max_speed);
			float d <- super.move();   // car.move() checks fuel and calls vehicle.move()
			speed <- normal_speed;    // restore original speed
			return d;
		}
		return super.move();
	}

	/**
	 * Overrides {@code car.summary()} to append turbo status.
	 *
	 * @return descriptive string
	 */
	string summary() {
		return super.summary() + "  turbo=" + turbo_active;
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Global — exercises inheritance
// ─────────────────────────────────────────────────────────────────────────────

global {

	init {

		// ── 5a. Create instances of each concrete class ─────────────────────

		write "══ 1. Instantiation ════════════════════════════════════════";

		car c <- car(name: "sedan", speed: 80.0, fuel: 40.0,
		             consumption_per_km: 0.07, max_speed: 180.0);
		bicycle b <- bicycle(name: "city-bike", speed: 15.0, rider: "Alice");
		sports_car sc <- sports_car(name: "gt500", speed: 120.0,
		                             fuel: 60.0, max_speed: 250.0,
		                             turbo_active: true, boost_factor: 1.8);

		write c.summary();
		write b.summary();
		write sc.summary();

		// ── 5b. Polymorphic list — each call dispatches to the right override ─

		write "══ 2. Polymorphic dispatch via list<vehicle> ═══════════════";

		list<vehicle> fleet <- [c, b, sc];

		// Each summary() call dispatches to the overriding class
		loop v over: fleet {
			write v.summary();
		}

		// ── 5c. Shared action (accelerate) inherited without override ────────

		write "══ 3. Shared inherited action (accelerate) ═════════════════";

		loop v over: fleet {
			v.accelerate(20.0);
			write "after accelerate(20): " + v.summary();
		}

		// ── 5d. Simulating several steps — observing fuel depletion ──────────

		write "══ 4. Simulating 10 steps ══════════════════════════════════";

		loop times: 10 {
			loop v over: fleet {
				v.move();
			}
		}
		loop v over: fleet {
			write "after 10 steps: " + v.summary();
		}

		// ── 5e. Runtime type testing with "is" ──────────────────────────────

		write "══ 5. Runtime type testing with 'is' ═══════════════════════";

		write "c  is car?       " + (c  is car);         // true
		write "c  is vehicle?   " + (c  is vehicle);     // true (parent)
		write "sc is sports_car?" + (sc is sports_car);  // true
		write "sc is car?       " + (sc is car);         // true (sc extends car)
		write "sc is vehicle?   " + (sc is vehicle);     // true (transitive)
		write "b  is car?       " + (b  is car);         // false (sibling)

		// Selective filter
		list<vehicle> only_cars <- fleet select (each is car);
		write "Vehicles that are (at least) a car: "
		      + (only_cars collect each.name);

		// ── 5f. invoke — calling a parent void procedure ─────────────────────

		write "══ 6. invoke — explicitly calling a parent procedure ═══════";
		// In sports_car, calling a parent void action (e.g. refuel) inherited from car
		// using invoke ensures the parent-chain implementation runs, not a potential
		// override in a deeper subclass.  Here no override exists, so both are identical.
		sc.refuel(60.0);    // call via normal dispatch
		write "sc after refuel(60): " + sc.summary();
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// Experiment
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Minimal GUI experiment — all output goes to the console.
 */
experiment "Inheritance" type: gui {
	output {
		// No display needed; all output goes to the console via write
	}
}

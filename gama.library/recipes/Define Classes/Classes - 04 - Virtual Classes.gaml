/**
* Name: Virtual Classes
* Author: Alexis Drogoul
* Description:
*   Fourth in a series of short focused models exploring the class/object
*   additions to GAML introduced in GAMA 2026.
*
*   This model concentrates on virtual (abstract) classes and virtual actions:
*     1. Declaring a virtual class with "virtual: true".
*     2. Declaring virtual (abstract) actions that have no body.
*     3. Trying to instantiate a virtual class (illegal — shown via nil guard).
*     4. Implementing all virtual actions in a concrete child class.
*     5. Polymorphism: a variable typed as the virtual class holds concrete objects.
*     6. Virtual actions in multi-level hierarchies.
*     7. Partially abstract classes — a class that implements SOME but not all
*        virtual actions it inherits: it becomes virtual itself.
*
*   Key take-aways:
*     • "virtual: true" on a class means it cannot be instantiated.
*     • "virtual: true" on an action means the body is absent; every concrete
*       subclass MUST provide an implementation.
*     • A class that inherits but does not implement all virtual actions is itself
*       implicitly virtual (abstract).
*     • Virtual classes are the GAML equivalent of abstract classes / interfaces in OOP.
*     • They are the main tool for defining contracts / protocols across a family
*       of classes.
*
* Tags: class, object, virtual, abstract, polymorphism, interface, GAML, OOP
*/
model VirtualClasses

// ─────────────────────────────────────────────────────────────────────────────
// 1. Abstract root class  –  sensor
//    virtual: true  →  cannot be instantiated.
//    Declares two abstract actions that define the sensor contract.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Abstract root class for all sensor types.
 *
 * Being declared {@code virtual: true}, {@code sensor} cannot be instantiated
 * directly.  It acts as a contract: every concrete subclass MUST implement
 * {@code read()} and {@code unit()}.
 *
 * Attributes:
 * <ul>
 *   <li>{@code id}      – unique sensor identifier, defaulting to "sensor".</li>
 *   <li>{@code active}  – whether the sensor is currently switched on.</li>
 * </ul>
 */
class sensor virtual: true {

	/** Unique identifier for this sensor. */
	string id <- "sensor";

	/** Whether the sensor is currently switched on. */
	bool active <- true;

	/**
	 * Returns the most recent reading of this sensor.
	 * Virtual: every concrete subclass must provide its own body.
	 *
	 * @return the numeric reading (semantics depend on the subclass)
	 */
	float read() virtual: true;

	/**
	 * Returns the physical unit of the reading (e.g., "°C", "hPa").
	 * Virtual: every concrete subclass must provide its own body.
	 *
	 * @return the unit string
	 */
	string unit() virtual: true;

	/**
	 * Returns a formatted reading string, e.g. "temp01: 23.4 °C".
	 * Concrete: implemented once here, calls {@code read()} and {@code unit()}
	 * polymorphically so the right subclass methods are invoked.
	 *
	 * @return human-readable sensor reading
	 */
	string formatted_reading() {
		if !active { return id + ": [OFF]"; }
		return id + ": " + (read() with_precision 2) + " " + unit();
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Concrete subclass  –  thermometer
//    Implements read() and unit(); adds temperature-specific attributes.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A concrete thermometer sensor.  Implements both abstract actions declared
 * in {@code sensor}: {@code read()} returns the stored temperature and
 * {@code unit()} returns {@code "°C"}.
 *
 * Attributes (in addition to those inherited from {@code sensor}):
 * <ul>
 *   <li>{@code temperature} – the current reading in degrees Celsius.</li>
 *   <li>{@code min_temp}    – minimum temperature ever recorded.</li>
 *   <li>{@code max_temp}    – maximum temperature ever recorded.</li>
 * </ul>
 */
class thermometer parent: sensor {

	/** Current temperature reading in degrees Celsius. */
	float temperature <- 20.0;

	/** Minimum temperature observed since creation. */
	float min_temp <- #infinity;

	/** Maximum temperature observed since creation. */
	float max_temp <- -#infinity;

	/**
	 * Returns the current temperature.  Implements {@code sensor.read()}.
	 *
	 * @return temperature in °C
	 */
	float read() {
		return temperature;
	}

	/**
	 * Returns the unit string "°C".  Implements {@code sensor.unit()}.
	 *
	 * @return "°C"
	 */
	string unit() {
		return "°C";
	}

	/**
	 * Updates the stored temperature and maintains running min/max.
	 *
	 * @param t  the new temperature reading
	 */
	action update(float t) {
		temperature <- t;
		if t < min_temp { min_temp <- t; }
		if t > max_temp { max_temp <- t; }
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Concrete subclass  –  barometer
//    Independently implements read() and unit().
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A concrete barometer (atmospheric pressure sensor).  Independently
 * implements the two abstract actions declared in {@code sensor}.
 *
 * Attributes (in addition to those inherited from {@code sensor}):
 * <ul>
 *   <li>{@code pressure}  – current atmospheric pressure in hPa.</li>
 *   <li>{@code altitude}  – estimated altitude derived from pressure in metres.</li>
 * </ul>
 */
class barometer parent: sensor {

	/** Current atmospheric pressure in hPa. */
	float pressure <- 1013.25;

	/**
	 * Returns the current pressure.  Implements {@code sensor.read()}.
	 *
	 * @return pressure in hPa
	 */
	float read() {
		return pressure;
	}

	/**
	 * Returns the unit string "hPa".  Implements {@code sensor.unit()}.
	 *
	 * @return "hPa"
	 */
	string unit() {
		return "hPa";
	}

	/**
	 * Estimates altitude from the barometric formula (simplified).
	 * Specific to the barometer; not part of the {@code sensor} contract.
	 *
	 * @return estimated altitude in metres above sea level
	 */
	float estimated_altitude() {
		return 44330.0 * (1.0 - (pressure / 1013.25) ^ 0.1903);
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. Partially abstract intermediate class  –  calibrated_sensor
//    Extends sensor, implements unit() but leaves read() virtual.
//    This makes calibrated_sensor itself virtual (cannot be instantiated).
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A partially abstract intermediate sensor that introduces a calibration
 * offset.  It implements {@code unit()} (always returns "raw") but leaves
 * {@code read()} still virtual — so this class remains abstract and cannot
 * be instantiated.  Concrete subclasses must still implement {@code read()}.
 *
 * Attributes (in addition to those inherited from {@code sensor}):
 * <ul>
 *   <li>{@code calibration_offset} – a value added to every raw reading.</li>
 * </ul>
 */
class calibrated_sensor virtual: true parent: sensor {

	/** An additive offset applied to every raw reading. */
	float calibration_offset <- 0.0;

	/**
	 * Implements {@code sensor.unit()}: returns "raw".
	 * Concrete subclasses may override this to return a physical unit.
	 *
	 * @return "raw"
	 */
	string unit() {
		return "raw";
	}

	// read() is still virtual here — calibrated_sensor is itself abstract
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Concrete grandchild  –  light_sensor
//    Extends calibrated_sensor, finally implements read().
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A concrete light sensor.  Extends the abstract {@code calibrated_sensor}
 * (which itself extends {@code sensor}).  Finally implements {@code read()}
 * to return the raw lux value plus the calibration offset.
 * Overrides {@code unit()} to return "lux" instead of "raw".
 *
 * Attributes (in addition to those inherited from {@code calibrated_sensor} and {@code sensor}):
 * <ul>
 *   <li>{@code raw_lux} – the uncalibrated light intensity in lux.</li>
 * </ul>
 */
class light_sensor parent: calibrated_sensor {

	/** Raw (uncalibrated) light intensity in lux. */
	float raw_lux <- 0.0;

	/**
	 * Returns the calibrated lux reading.  Implements {@code sensor.read()}.
	 *
	 * @return raw_lux + calibration_offset
	 */
	float read() {
		return raw_lux + calibration_offset;
	}

	/**
	 * Overrides the "raw" unit from {@code calibrated_sensor} with "lux".
	 *
	 * @return "lux"
	 */
	string unit() {
		return "lux";
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. Global — exercises virtual classes and polymorphism
// ─────────────────────────────────────────────────────────────────────────────

global {

	init {

		// ── 6a. Instantiation of concrete subclasses ─────────────────────────

		write "══ 1. Instantiating concrete subclasses ════════════════════";

		thermometer t1 <- thermometer(id: "temp01", temperature: 23.5);
		barometer   b1 <- barometer(id: "baro01",  pressure: 1005.0);
		light_sensor l1 <- light_sensor(id: "lux01",
		                                raw_lux: 850.0,
		                                calibration_offset: 15.0);

		write t1.formatted_reading();   // calls thermometer.read() + unit()
		write b1.formatted_reading();   // calls barometer.read()   + unit()
		write l1.formatted_reading();   // calls light_sensor.read() + unit()

		// ── 6b. Polymorphic list typed as the abstract class ─────────────────

		write "══ 2. Polymorphic list<sensor> ═════════════════════════════";

		list<sensor> sensors <- [t1, b1, l1];

		loop s over: sensors {
			write s.formatted_reading();    // dispatches to the concrete class
		}

		// ── 6c. Aggregate over a polymorphic list ─────────────────────────────

		write "══ 3. Max reading across all active sensors ════════════════";
		float max_r <- max(sensors collect (each.active ? each.read() : -#infinity));
		write "max raw reading = " + (max_r with_precision 2);

		// ── 6d. Updating sensor state via concrete-specific actions ───────────

		write "══ 4. Updating thermometer readings ═══════════════════════";
		list<float> samples <- [21.1, 19.8, 24.3, 18.5, 27.0];
		loop temp over: samples {
			t1.update(temp);
		}
		write "after updates: " + t1.formatted_reading();
		write "min recorded = " + (t1.min_temp with_precision 1)
		      + "  max = " + (t1.max_temp with_precision 1);

		// ── 6e. Barometer-specific action not on the abstract type ────────────

		write "══ 5. Barometer-specific altitude estimate ══════════════════";
		write "altitude ~ " + (b1.estimated_altitude() with_precision 0) + " m";

		// ── 6f. Toggling a sensor off via the shared abstract attribute ───────

		write "══ 6. Toggling active flag ══════════════════════════════════";
		b1.active <- false;
		loop s over: sensors {
			write s.formatted_reading();   // b1 now prints [OFF]
		}
		b1.active <- true;

		// ── 6g. is-a checks ───────────────────────────────────────────────────

		write "══ 7. Runtime type checks ══════════════════════════════════";
		write "t1 is thermometer?       " + (t1 is thermometer);        // true
		write "t1 is sensor?            " + (t1 is sensor);             // true
		write "l1 is calibrated_sensor? " + (l1 is calibrated_sensor);  // true
		write "l1 is sensor?            " + (l1 is sensor);             // true
		write "t1 is barometer?         " + (t1 is barometer);          // false
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// Experiment
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Minimal GUI experiment — all output goes to the console.
 */
experiment "Virtual Classes" type: gui {
	output {
		// No display needed; all output goes to the console via write
	}
}

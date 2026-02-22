/*******************************************************************************************************
 *
 * IExperimentDisplayable.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import com.google.common.primitives.Ints;

import gama.api.runtime.scope.IScope;
import gama.api.utils.interfaces.IColored;
import gama.api.utils.interfaces.INamed;

/**
 * Interface for objects that can be displayed in experiment views.
 * 
 * <p>This interface defines the contract for model elements (parameters, monitors, outputs, etc.)
 * that appear in GAMA's experiment user interface. It combines naming, coloring, and ordering
 * capabilities to provide a consistent display experience.</p>
 * 
 * <h2>Main Responsibilities:</h2>
 * <ul>
 *   <li>Provide display title and name</li>
 *   <li>Support unit labels for values</li>
 *   <li>Define display order for UI arrangement</li>
 *   <li>Categorize items as experiment or simulation level</li>
 *   <li>Support color coding for visual differentiation</li>
 * </ul>
 * 
 * <h2>Categories:</h2>
 * <p>Items are categorized as either "Experiment" or "Simulation" level, which helps
 * organize them in the UI. Experiment-level items are typically parameters and controls
 * that affect the entire experiment, while simulation-level items are specific to
 * individual simulation instances.</p>
 * 
 * <h2>Ordering:</h2>
 * <p>Items implement {@link Comparable} based on their order value, allowing the UI to
 * arrange them consistently. Lower order values appear first in displays.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * public class MyParameter implements IExperimentDisplayable {
 *     public String getTitle() { return "Population Size"; }
 *     public String getUnitLabel(IScope scope) { return "individuals"; }
 *     public int getOrder() { return 1; }
 *     public boolean isDefinedInExperiment() { return true; }
 * }
 * }</pre>
 * 
 * @author The GAMA Development Team
 * @since GAMA 1.0
 */
public interface IExperimentDisplayable extends INamed, IColored, Comparable<IExperimentDisplayable> {

	/**
	 * The default category for simulation-level items.
	 * 
	 * <p>Items with this category are specific to individual simulations and may vary
	 * across different simulation instances within the same experiment.</p>
	 */
	String DEFAULT_SIMULATION_CATEGORY = "Simulation";

	/**
	 * The default category for experiment-level items.
	 * 
	 * <p>Items with this category are global to the experiment and typically control
	 * parameters that affect all simulations.</p>
	 */
	String DEFAULT_EXPERIMENT_CATEGORY = "Experiment";

	/**
	 * Gets the display title for this item.
	 * 
	 * <p>The title is the main text displayed to users in the UI. It should be
	 * descriptive and human-readable.</p>
	 *
	 * @return the display title
	 */
	String getTitle();

	/**
	 * Gets the unit label to display alongside the value.
	 * 
	 * <p>Unit labels provide context for numeric values, such as "meters", "seconds",
	 * or "individuals". They are typically shown in parentheses or smaller text
	 * next to the value.</p>
	 *
	 * @param scope the scope providing context for unit evaluation
	 * @return the unit label, or null if no unit applies
	 */
	String getUnitLabel(IScope scope);

	/**
	 * Sets the unit label for this item.
	 * 
	 * <p>The default implementation does nothing. Override this method if the item
	 * supports dynamic unit labels that can be changed at runtime.</p>
	 *
	 * @param label the new unit label
	 */
	default void setUnitLabel(final String label) {}

	/**
	 * Gets the display order for this item.
	 * 
	 * <p>The order determines the position of this item relative to other items in
	 * the UI. Items with lower order values appear first. Items with the same order
	 * may be arranged alphabetically or by other criteria.</p>
	 *
	 * @return the display order (typically a positive integer)
	 */
	int getOrder();

	/**
	 * Compares this item to another for ordering purposes.
	 * 
	 * <p>The default implementation compares based on the {@link #getOrder()} value,
	 * with lower values coming first.</p>
	 *
	 * @param p the other item to compare to
	 * @return a negative integer, zero, or a positive integer as this item's order
	 *         is less than, equal to, or greater than the specified item's order
	 */
	@Override
	default int compareTo(final IExperimentDisplayable p) {
		return Ints.compare(getOrder(), p.getOrder());
	}

	/**
	 * Checks if this item is defined at the experiment level.
	 * 
	 * <p>Experiment-level items are global to the entire experiment and typically
	 * control experiment-wide parameters. Simulation-level items are specific to
	 * individual simulation instances.</p>
	 *
	 * @return true if this item is defined in the experiment, false if it is
	 *         defined in the simulation
	 */
	boolean isDefinedInExperiment();

	/**
	 * Gets the category for this item.
	 * 
	 * <p>The default implementation returns {@link #DEFAULT_EXPERIMENT_CATEGORY} if
	 * {@link #isDefinedInExperiment()} is true, otherwise returns
	 * {@link #DEFAULT_SIMULATION_CATEGORY}.</p>
	 *
	 * @return the category name
	 */
	default String getCategory() {
		return isDefinedInExperiment() ? DEFAULT_EXPERIMENT_CATEGORY : DEFAULT_SIMULATION_CATEGORY;
	}
}

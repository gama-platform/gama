/*******************************************************************************************************
 *
 * IOutput.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.ISymbol;
import gama.api.runtime.IStepable;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.scope.IScoped;
import gama.api.ui.displays.IDisplayData;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.layers.ILayerStatement;

/**
 * Interface for simulation outputs in GAMA.
 * 
 * <p>This interface represents objects declared in GAML models that perform computations
 * and generate information to be displayed during simulations. Outputs are not directly
 * responsible for displaying information on screen - they only compute data and control
 * the lifecycle of their associated display surfaces or views.</p>
 * 
 * <h2>Output Types:</h2>
 * <p>GAMA supports various output types:</p>
 * <ul>
 *   <li><strong>Display:</strong> Graphical displays (2D/3D visualizations)</li>
 *   <li><strong>Monitor:</strong> Variable value displays</li>
 *   <li><strong>Inspect:</strong> Agent inspection views</li>
 *   <li><strong>File:</strong> File export outputs</li>
 *   <li><strong>Layout:</strong> UI layout configurations</li>
 * </ul>
 * 
 * <h2>Output Lifecycle:</h2>
 * <p>Outputs follow a specific lifecycle:</p>
 * <ol>
 *   <li><strong>Created:</strong> Output is instantiated from GAML</li>
 *   <li><strong>Opened:</strong> {@link #open()} creates the concrete display surface</li>
 *   <li><strong>Running:</strong> {@link #step(IScope)} computes data each cycle</li>
 *   <li><strong>Paused:</strong> {@link #setPaused(boolean)} temporarily halts updates</li>
 *   <li><strong>Closed:</strong> {@link #close()} releases resources</li>
 * </ol>
 * 
 * <h2>Computation vs Display:</h2>
 * <p>Since 2018, outputs have had reduced computational responsibilities:</p>
 * <ul>
 *   <li>{@link #step(IScope)} - Performs GAML-defined computations</li>
 *   <li>{@link #update()} - Refreshes the concrete display (delegated to surface)</li>
 * </ul>
 * 
 * <h2>Refresh Control:</h2>
 * <p>Outputs can control when they refresh using:</p>
 * <ul>
 *   <li>Refresh rate (every N cycles)</li>
 *   <li>Pause state</li>
 *   <li>Scope interruption state</li>
 *   <li>{@link #isRefreshable()} combines these factors</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * IOutput output = experiment.getOutputManager().getOutputWithId("my_display");
 * output.open();
 * output.step(scope);      // Compute data
 * output.update();         // Refresh display
 * output.setPaused(true);  // Pause updates
 * output.close();          // Release resources
 * }</pre>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @revised December 2015 - Simplified and documented interface
 * @revised March 2024 - Simplified hierarchy (no more separate display outputs)
 */
public interface IOutput extends ISymbol, IStepable, IScoped {

	/**
	 * The Interface Display.
	 */
	interface Display extends IOutput {
		/**
		 * Returns the GamaView associated with this output, if any
		 *
		 * @return an instance of IGamaView or null if no view is associated to this output
		 */

		IGamaView.Display getView();

		/**
		 * @return
		 */
		IDisplayData getData();

		/**
		 * @return
		 */
		Iterable<ILayerStatement> getLayers();

		/**
		 */
		void setSurface(IDisplaySurface swtOpenGLDisplaySurface);
	}

	/**
	 * The output should pause its operations when the parameter passed is true and resume them when it is false.
	 * Setting pause to true (resp. false) when the output is already paused (resp. resumed) should not have any effect.
	 *
	 * @param paused
	 *            true if the output should pause, false if it should resume
	 */
	void setPaused(boolean paused);

	/**
	 * Returns whether the output is paused or running.
	 *
	 * @return true if the output has been set to pause, false otherwise
	 */
	boolean isPaused();

	/**
	 * In response to this message, the output is supposed to open its concrete support. Sending open() to an already
	 * opened output should not have any effect.
	 */
	void open();

	/**
	 * Checks if is open.
	 *
	 * @return true, if is open
	 */
	boolean isOpen();

	/**
	 * In response to this message, the output is supposed to close its concrete support. A closed output cannot resume
	 * its operations unless 'open()' is called again.
	 */
	void close();

	/**
	 * Called by the output thread to perform the actual "refresh" of the concrete support of the output (whereas
	 * step(), from IStepable, performs the computations described in GAML, that will serve as a model for this
	 * refresh).
	 */
	void update() throws GamaRuntimeException;

	/**
	 * Returns the scope of the output, i.e. the scope it uses to perform its computations, independently of the main
	 * simulation scope. Access to this scope should be limited to a strict necessity
	 *
	 * @return the scope of the output, which should never be null when the output is open. It might be null otherwise
	 */
	@Override
	IScope getScope();

	/**
	 * Returns the original name of the output (as it has been declared by the modeler). This name can be changed later
	 * to accomodate different display configuration in the UI
	 *
	 * @return the string representing the original (unaltered) name of the output as defined by the modeler
	 */
	String getOriginalName();

	/**
	 * Returns the identifier (should be unique) of this output
	 *
	 * @return a string representing the unique identifier of this output
	 */
	String getId();

	/**
	 * Whether this output should (and can) be refreshed. It should not be paused, its scope should not be interrupted,
	 * and its refresh rate must be in sync with the current cycle
	 *
	 * @return true if the output can be refreshed, false otherwise
	 */

	boolean isRefreshable();

	/**
	 * Sets whether this output has been created by the user or from the model
	 *
	 * @param b
	 *            true if the user has created this output
	 */
	void setUserCreated(boolean b);

	/**
	 * If only one output of this kind is allowed (i.e. there can only be one instance of the corresponding concrete
	 * support), the output should return true
	 *
	 * @return true if only one view for this kind of output is possible, false otherwise
	 */
	boolean isUnique();

	/**
	 * Returns the identifier of the view to be opened in the UI. If this view should be unique, then this identifier
	 * will be used to retrieve it (or create it if it is not yet instantiated). Otherwise, the identifier and the name
	 * of the output are used in combination to create a new view.
	 *
	 * @return the identifier of the view that will be used as the concrete support for this output
	 */
	String getViewId();

	/**
	 * Returns whether the output has been described as 'virtual', i.e. not showable on screen and only used for display
	 * inheritance.
	 *
	 * @return
	 */
	boolean isVirtual();

	/**
	 * Checks if is auto save. This default method always returns false.
	 *
	 * @return true, if is auto save
	 */
	default boolean isAutoSave() { return false; }

}

/*******************************************************************************************************
 *
 * IOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import gama.core.common.interfaces.IGamaView;
import gama.core.common.interfaces.IScoped;
import gama.core.common.interfaces.IStepable;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.ISymbol;

/**
 * This interface represents the objects, declared in a model, which perform various types of computations and return
 * information supposed to be displayed or saved during simulations. Outputs are not in charge of displaying/outputting
 * information on a concrete support, only computing it. They however control whatever concrete support they represent
 * (opening, closing, pausing, updating and refreshing it).
 *
 * @update Since 2018, the role of ouputs in the computation has been reduced, so as to not weigh too much on the
 *         simulation thread. More computations are now taken in charge by the concrete implementations of the output
 *
 * @author Alexis Drogoul, IRD
 * @revised in Dec. 2015 to simplify and document the interface of outputs
 * @revised in Mar. 2024 to simplify the hierarchy of outputs (no more display outputs)
 */
public interface IOutput extends ISymbol, IStepable, IScoped {

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
	 * In response to this message, the output is supposed to open its concrete support, whether it is a view or a file.
	 * Sending open() to an already opened output should not have any effect.
	 */
	void open();

	/**
	 * Checks if is open.
	 *
	 * @return true, if is open
	 */
	boolean isOpen();

	/**
	 * In response to this message, the output is supposed to close its concrete support, whether it is a view or a
	 * file. A closed output cannot resume its operations unless 'open()' is called again.
	 */
	void close();

	/**
	 * The output should return its refresh rate in terms of the number of cycles of the simulation (min. is 1) between
	 * two updates
	 *
	 * @return an integer >=1 that represents how many cycles separate two updates
	 */
	int getRefreshRate();

	/**
	 * The output should set its refresh rate to the parameter passed. Mainly called by the interface
	 *
	 * @param rate,
	 *            an integer normally >= 1. The output is free to consider if a negative or null rate makes any sense
	 */
	void setRefreshRate(int rate);

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
	 * to accomoadate different display configuration in the UI
	 *
	 * @return the string representing the original (unaltered) name of the output as defined by the modeler
	 */
	String getOriginalName();

	/**
	 * Returns the identifier (should be unique) of this output
	 *
	 * @return a string representing the unique identifier of this output (especially important for UI outputs)
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
	 * If only one output of this kind is allowed in the UI (i.e. there can only be one instance of the corresponding
	 * view), the output should return true
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
	 * Checks if is auto save.
	 *
	 * @return true, if is auto save
	 */
	default boolean isAutoSave() { return false; }

	/**
	 * Returns the GamaView associated with this output
	 */

	IGamaView getView();

	/**
	 * Checks if is rendered.
	 *
	 * @return true, if is rendered
	 */
	boolean isRendered();

	/**
	 * Sets the rendered.
	 *
	 * @param b
	 *            the new rendered
	 */
	void setRendered(boolean b);

}

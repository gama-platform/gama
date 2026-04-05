/*******************************************************************************************************
 *
 * SyntacticExperimentElement.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ast;

import org.eclipse.emf.ecore.EObject;

import gama.api.gaml.symbols.Facets;

/**
 * Represents an experiment definition in the GAML Abstract Syntax Tree.
 * 
 * <p>Experiments are special top-level constructs in GAML that define how to execute and
 * interact with a model. They specify simulation parameters, outputs, and user interfaces.
 * Unlike regular species, experiments control the simulation lifecycle.</p>
 * 
 * <p><strong>Key Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Top-Level Only:</strong> Experiments must be defined at the model level</li>
 *   <li><strong>Simulation Control:</strong> Defines parameters, outputs, stopping conditions</li>
 *   <li><strong>UI Definition:</strong> Specifies displays, monitors, and user interactions</li>
 *   <li><strong>Multiple Types:</strong> Supports batch, gui, test, and memorize experiments</li>
 * </ul>
 * 
 * <p><strong>Experiment Types:</strong></p>
 * <ul>
 *   <li><strong>gui:</strong> Interactive experiments with visual displays</li>
 *   <li><strong>batch:</strong> Automated parameter exploration without UI</li>
 *   <li><strong>test:</strong> Unit testing experiments for model validation</li>
 *   <li><strong>memorize:</strong> Experiments with state serialization</li>
 * </ul>
 * 
 * <p><strong>Common Experiment Children:</strong></p>
 * <ul>
 *   <li>parameter - Defines adjustable simulation parameters</li>
 *   <li>output - Defines displays, charts, monitors</li>
 *   <li>permanent - Statements executed throughout the simulation</li>
 *   <li>reflex - Reflexes executed at experiment level</li>
 * </ul>
 * 
 * <p><strong>Example GAML:</strong></p>
 * <pre>{@code
 * experiment MyExperiment type: gui {
 *   parameter "Population" var: nb_agents min: 10 max: 1000;
 *   output {
 *     display "Main" {
 *       species my_species;
 *     }
 *   }
 * }
 * }</pre>
 * 
 * <p><strong>Inheritance Note:</strong> Extends {@link SyntacticStructuralElement} to inherit
 * child management and name caching, but overrides type checking methods to identify as
 * experiment rather than species.</p>
 *
 * @author drogoul
 * @since 9 sept. 2013
 * @see SyntacticStructuralElement
 * @see SyntacticExperimentModelElement
 * @see SyntacticModelElement
 */
public class SyntacticExperimentElement extends SyntacticStructuralElement {

	/**
	 * Constructs a new syntactic experiment element.
	 * 
	 * <p>Experiments are structural elements with a name and can contain children
	 * (parameters, outputs, etc.). The facets typically include the experiment type
	 * (gui, batch, test, memorize) and other configuration options.</p>
	 *
	 * @param keyword   the GAML keyword, typically "experiment"
	 * @param facets    the facets including type, name, and other experiment attributes
	 * @param statement the underlying EMF EObject from the parsed model
	 */
	SyntacticExperimentElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	/**
	 * Checks if this element represents a species definition.
	 * 
	 * <p>Although experiments extend structural elements (which are typically species),
	 * they are NOT species in the traditional sense. They have special semantics
	 * for controlling simulation execution rather than defining agent types.</p>
	 *
	 * @return false, as experiments are not species
	 */
	@Override
	public boolean isSpecies() { return false; }

	/**
	 * Checks if this element represents an experiment definition.
	 * 
	 * <p>This is the identifying characteristic of experiment elements and is used
	 * during AST traversal to locate and process experiments separately from other
	 * model components.</p>
	 *
	 * @return true, as this is an experiment element
	 */
	@Override
	public boolean isExperiment() { return true; }
}

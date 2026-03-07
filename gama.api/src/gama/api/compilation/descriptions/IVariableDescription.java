/*******************************************************************************************************
 *
 * IVariableDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import gama.annotations.constants.IKeyword;
import gama.api.additions.IGamaHelper;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;

/**
 * Description interface for GAML variables (agent attributes).
 * 
 * <p>
 * This interface extends {@link IDescription} to provide capabilities specific to variable (attribute)
 * declarations in GAML models. Variables represent agent state and can be defined at different levels
 * (global, species, experiment) with various properties controlling initialization, updates, and access.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * IVariableDescription enables:
 * </p>
 * <ul>
 *   <li><strong>Variable Metadata:</strong> Type, name, initialization, and update expressions</li>
 *   <li><strong>Dependency Tracking:</strong> Understanding which variables depend on others</li>
 *   <li><strong>Parameter Management:</strong> Variables as experiment or model parameters</li>
 *   <li><strong>Built-in Variables:</strong> Platform-provided attributes with Java implementations</li>
 *   <li><strong>Access Control:</strong> Modifiable vs. constant variables</li>
 *   <li><strong>Function Variables:</strong> Variables that compute values on access</li>
 * </ul>
 * 
 * <h2>Variable Categories</h2>
 * 
 * <h3>Regular Variables:</h3>
 * <pre>{@code
 * int energy <- 100;                    // Simple initialization
 * float speed <- 1.0 min: 0.0 max: 5.0; // With constraints
 * }</pre>
 * 
 * <h3>Updated Variables:</h3>
 * <pre>{@code
 * int age <- 0 update: age + 1;  // Automatically updated each step
 * }</pre>
 * 
 * <h3>Function Variables:</h3>
 * <pre>{@code
 * float distance <- location distance_to target; // Computed on access
 * }</pre>
 * 
 * <h3>Parameter Variables:</h3>
 * <pre>{@code
 * parameter "Population size" var: population_size <- 100 min: 1 max: 1000;
 * }</pre>
 * 
 * <h3>Constant Variables:</h3>
 * <pre>{@code
 * const int MAX_ENERGY <- 100; // Cannot be modified
 * }</pre>
 * 
 * <h2>Variable Properties</h2>
 * 
 * <ul>
 *   <li><strong>Type:</strong> Data type of the variable (int, float, string, species, etc.)</li>
 *   <li><strong>Initial Value:</strong> Expression to compute initial value ({@code init} or {@code <-})</li>
 *   <li><strong>Update Expression:</strong> Expression to recompute value each step ({@code update})</li>
 *   <li><strong>Function Expression:</strong> Expression to compute value on each access ({@code function})</li>
 *   <li><strong>Constraints:</strong> Min, max, step, among values</li>
 *   <li><strong>Modifiability:</strong> Can the variable be assigned to?</li>
 * </ul>
 * 
 * <h2>Dependencies</h2>
 * 
 * <p>
 * Variables can depend on other variables in their initialization or update expressions.
 * The {@link #getDependencies(Set, boolean, boolean)} method analyzes these dependencies:
 * </p>
 * <pre>{@code
 * // Variable dependencies
 * float max_speed <- 5.0;
 * float current_speed <- max_speed * 0.5; // Depends on max_speed
 * 
 * Collection<IVariableDescription> deps = 
 *     current_speed_desc.getDependencies(INIT_DEPENDENCIES_FACETS, false, false);
 * // Returns: [max_speed_desc]
 * }</pre>
 * 
 * <h2>Built-in Variables</h2>
 * 
 * <p>
 * Platform-provided variables (like {@code location}, {@code shape}, {@code name}) have Java
 * implementations accessible via helpers:
 * </p>
 * <ul>
 *   <li>{@link #getGetter()} - Reads the variable value</li>
 *   <li>{@link #getSetter()} - Writes the variable value</li>
 *   <li>{@link #getIniter()} - Initializes the variable</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IVariableDescription varDesc = ...;
 * 
 * // Get variable metadata
 * String name = varDesc.getName(); // "energy"
 * IType<?> type = varDesc.getType(); // Type.INT
 * 
 * // Check properties
 * if (varDesc.isUpdatable()) {
 *     // Variable has an update expression
 * }
 * 
 * if (varDesc.isNotModifiable()) {
 *     // Variable is constant
 * }
 * 
 * if (varDesc.isParameter()) {
 *     // Variable is an experiment parameter
 *     String paramName = varDesc.getParameterName();
 * }
 * 
 * // Get dependencies
 * Collection<IVariableDescription> deps = 
 *     varDesc.getDependencies(UPDATE_DEPENDENCIES_FACETS, true, false);
 * 
 * // For built-in variables
 * IGamaHelper getter = varDesc.getGetter();
 * if (getter != null) {
 *     // Read value using Java implementation
 *     Object value = getter.run(scope, agent);
 * }
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see IDescription
 * @see gama.api.gaml.types.IType
 * @see gama.api.additions.IGamaHelper
 */
public interface IVariableDescription extends IDescription {

	/** The dependencies. */
	Map<String, Collection<String>> dependencies = new HashMap<>();

	/** The Constant INIT_DEPENDENCIES_FACETS. */
	Set<String> INIT_DEPENDENCIES_FACETS = ImmutableSet.<String> builder()
			.add(IKeyword.INIT, IKeyword.MIN, IKeyword.MAX, IKeyword.STEP, IKeyword.SIZE, IKeyword.AMONG).build();

	/** The Constant UPDATE_DEPENDENCIES_FACETS. */
	Set<String> UPDATE_DEPENDENCIES_FACETS =
			ImmutableSet.<String> builder().add(IKeyword.UPDATE, IKeyword.VALUE, IKeyword.MIN, IKeyword.MAX).build();

	/** The Constant FUNCTION_DEPENDENCIES_FACETS. */
	Set<String> FUNCTION_DEPENDENCIES_FACETS = ImmutableSet.<String> builder().add(IKeyword.FUNCTION).build();

	/** The Constant PREF_DEFINITIONS. */
	Map<String, String> PREF_DEFINITIONS = new HashMap<>();

	/**
	 * Checks if is updatable.
	 *
	 * @return true, if is updatable
	 */
	boolean isUpdatable();

	/**
	 * @return
	 */
	boolean isParameter();

	/**
	 * @return
	 */
	String getBuiltInDoc();

	/**
	 * @return
	 */
	boolean isFunction();

	/**
	 * Checks if is synthetic species container.
	 *
	 * @return true, if is synthetic species container
	 */
	boolean isSyntheticSpeciesContainer();

	/**
	 * @param initDependenciesFacets
	 * @param b
	 * @param c
	 * @return
	 */
	Collection<IVariableDescription> getDependencies(Set<String> initDependenciesFacets, boolean b, boolean c);

	/**
	 * @return
	 */
	boolean isNotModifiable();

	/**
	 * @return
	 */
	boolean isDefinedInExperiment();

	/**
	 * @return
	 */
	IGamlDocumentation getShortDocumentation();

	/**
	 * @param vd
	 */
	void copyFrom(IVariableDescription vd);

	/**
	 * @param get
	 * @param init
	 * @param set
	 */
	void addHelpers(IGamaHelper<?> get, IGamaHelper<?> init, IGamaHelper<?> set);

	/**
	 * Adds the helpers.
	 *
	 * @param clazz
	 *            the clazz
	 * @param get
	 *            the get
	 * @param init
	 *            the init
	 * @param set
	 *            the set
	 */
	void addHelpers(Class<?> clazz, IGamaHelper<?> get, IGamaHelper<?> init, IGamaHelper<?> set);

	/**
	 * @param definitionClass
	 */
	void setDefinitionClass(Class<?> definitionClass);

	/**
	 * @return
	 */
	IVariableDescription getBuiltInAncestor();

	/**
	 * @param asField
	 * @return
	 */
	IExpression getVarExpr(boolean asField);

	/**
	 * Copy.
	 *
	 * @param into
	 *            the into
	 * @return the i variable description
	 */
	@Override
	IVariableDescription copy(IDescription into);

	/**
	 * @return
	 */
	String getParameterName();

	/**
	 * @return
	 */
	IGamaHelper getGetter();

	/**
	 * Gets the setter.
	 *
	 * @return the setter
	 */
	IGamaHelper getSetter();

	/**
	 * Gets the initer.
	 *
	 * @return the initer
	 */
	IGamaHelper getIniter();

	/**
	 * @return
	 */
	boolean isExperimentParameter();

}

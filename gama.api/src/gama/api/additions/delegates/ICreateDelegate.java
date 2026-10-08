/*******************************************************************************************************
 *
 * ICreateDelegate.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions.delegates;

import java.util.List;
import java.util.Map;

import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;

/**
 * Delegate interface for extending the GAMA 'create' statement with custom agent creation sources.
 * 
 * <p>This interface allows plugins to extend the GAMA language by providing custom sources
 * for agent creation beyond the traditional methods. When a 'create' statement is executed
 * in GAML, the platform queries registered ICreateDelegate implementations to find one that
 * can handle the specified source type.</p>
 * 
 * <h2>Delegate Selection Process</h2>
 * <p>During execution of a 'create' statement with a 'from:' facet:</p>
 * <ol>
 *   <li>The platform calls {@link #acceptSource(IScope, Object)} on each registered delegate</li>
 *   <li>The first delegate that returns true is selected</li>
 *   <li>The platform calls {@link #createFrom(IScope, List, Integer, Object, Arguments, IStatement.Create)}
 *       to populate agent initialization data</li>
 *   <li>If {@link #handlesCreation()} returns true, the delegate's {@link #createAgents(IScope, IPopulation, List, IStatement.Create, IStatement)}
 *       is called instead of the standard creation process</li>
 * </ol>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class CSVCreateDelegate implements ICreateDelegate {
 *     @Override
 *     public boolean acceptSource(IScope scope, Object source) {
 *         return source instanceof IFile && ((IFile) source).getExtension().equals("csv");
 *     }
 *     
 *     @Override
 *     public boolean createFrom(IScope scope, List<Map<String, Object>> inits, 
 *                               Integer max, Object source, Arguments init, 
 *                               IStatement.Create statement) {
 *         // Parse CSV and populate inits with agent attributes
 *         CSVFile file = (CSVFile) source;
 *         for (List<Object> row : file.getContents()) {
 *             Map<String, Object> agentInit = new HashMap<>();
 *             agentInit.put("location", row.get(0));
 *             agentInit.put("name", row.get(1));
 *             inits.add(agentInit);
 *         }
 *         return true;
 *     }
 *     
 *     @Override
 *     public IType<?> fromFacetType() {
 *         return Types.FILE;
 *     }
 * }
 * }</pre>
 * 
 * @author drogoul
 * @since 27 mai 2015
 * 
 * @see IDrawDelegate
 * @see ISaveDelegate
 */
public interface ICreateDelegate {

	/**
	 * Determines whether this delegate can handle agent creation from the specified source.
	 * 
	 * <p>This method is called during the execution of a 'create' statement to determine
	 * if this delegate is appropriate for the given source object. The first registered
	 * delegate that returns true will be used.</p>
	 *
	 * @param scope the current execution scope
	 * @param source the source object specified in the 'from:' facet of the create statement
	 * @return true if this delegate can create agents from the source, false otherwise
	 */
	boolean acceptSource(IScope scope, Object source);

	/**
	 * Populates the initialization data for agents to be created from the source.
	 * 
	 * <p>This method is responsible for reading data from the source and converting it
	 * into initialization maps that will be used to create and initialize agents. Each
	 * map in the {@code inits} list represents one agent to be created, with keys being
	 * attribute names and values being the initial values for those attributes.</p>
	 * 
	 * <p>The method should respect the {@code max} parameter if not null, limiting the
	 * number of agents created.</p>
	 *
	 * @param scope the current execution scope
	 * @param inits the list to be filled with initialization maps (one per agent)
	 * @param max the maximum number of agents to create, or null for no limit
	 * @param source the source object to read agent data from
	 * @param init additional initialization arguments from the create statement
	 * @param statement the create statement being executed
	 * @return true if all initialization data was successfully extracted, false otherwise
	 */
	boolean createFrom(IScope scope, List<Map<String, Object>> inits, Integer max, Object source, Arguments init,
			IStatement.Create statement);

	/**
	 * Returns the GAML type expected for the 'from:' facet of create statements.
	 * 
	 * <p>This type is used during compilation to validate that create statements
	 * are using compatible sources. It should not be null or {@link IType#NO_TYPE}
	 * to enable compile-time validation.</p>
	 *
	 * @return the GAML type representing the expected source type (e.g., Types.FILE, Types.LIST)
	 */
	IType<?> fromFacetType();

	/**
	 * Indicates whether this delegate handles the complete agent creation process.
	 * 
	 * <p>When this method returns true, the platform will call {@link #createAgents(IScope, IPopulation, List, IStatement.Create, IStatement)}
	 * instead of the standard agent creation process. This allows delegates to have
	 * full control over how agents are instantiated and initialized.</p>
	 * 
	 * <p>By default, this returns false, meaning the delegate only provides initialization
	 * data and the standard creation process is used.</p>
	 *
	 * @return true if this delegate handles complete agent creation, false for standard creation
	 */
	default boolean handlesCreation() {
		return false;
	}

	/**
	 * Creates agents directly, bypassing the standard creation process.
	 * 
	 * <p>This method is only called if {@link #handlesCreation()} returns true.
	 * It provides complete control over the agent creation and initialization process.
	 * Delegates can use this to implement custom creation logic, or they can call back
	 * to the standard creation process via the statement parameter.</p>
	 * 
	 * <p>The default implementation returns null, which is appropriate when
	 * {@link #handlesCreation()} returns false.</p>
	 *
	 * @param scope the current execution scope
	 * @param population the population in which to create agents
	 * @param inits the list of initialization maps prepared by {@link #createFrom}
	 * @param statement the create statement being executed
	 * @param sequence any sequence of statements to execute after creation
	 * @return the list of created agents, or null if not applicable
	 */
	default IList<? extends IAgent> createAgents(final IScope scope, final IPopulation<? extends IAgent> population,
			final List<Map<String, Object>> inits, final IStatement.Create statement, final IStatement sequence) {
		return null;
	}

}

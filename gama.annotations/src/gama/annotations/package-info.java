/**
 * The GAMA Annotation Framework - Comprehensive support for declarative model definition.
 * 
 * <p>This package provides the complete annotation infrastructure that enables GAMA's declarative 
 * modeling approach. The annotations allow developers to define agent behaviors, species characteristics, 
 * and simulation elements using Java annotations that are processed into GAML language constructs.</p>
 * 
 * <h2>Architecture Overview</h2>
 * 
 * <p>The annotation framework consists of several layers that work together to provide 
 * seamless integration between Java implementation and GAML modeling:</p>
 * 
 * <h3>Core Annotations Layer</h3>
 * <ul>
 *   <li><strong>{@link gama.annotations.action @action}</strong> - Defines executable behaviors for agents</li>
 *   <li><strong>{@link gama.annotations.getter @getter}</strong> - Defines property access methods</li>  
 *   <li><strong>{@link gama.annotations.setter @setter}</strong> - Defines property modification methods</li>
 *   <li><strong>{@link gama.annotations.variable @variable}</strong> - Defines agent attributes</li>
 *   <li><strong>{@link gama.annotations.vars @vars}</strong> - Groups multiple variable definitions</li>
 * </ul>
 * 
 * <h3>Structural Annotations Layer</h3>
 * <ul>
 *   <li><strong>{@link gama.annotations.species @species}</strong> - Defines agent types and templates</li>
 *   <li><strong>{@link gama.annotations.skill @skill}</strong> - Defines reusable behavioral components</li>
 *   <li><strong>{@link gama.annotations.symbol @symbol}</strong> - Defines GAML language constructs</li>
 *   <li><strong>{@link gama.annotations.operator @operator}</strong> - Defines computational operations</li>
 * </ul>
 * 
 * <h3>Documentation and Metadata Layer</h3>
 * <ul>
 *   <li><strong>{@link gama.annotations.doc @doc}</strong> - Comprehensive documentation framework</li>
 *   <li><strong>{@link gama.annotations.example @example}</strong> - Executable code examples</li>
 *   <li><strong>{@link gama.annotations.usage @usage}</strong> - Usage pattern documentation</li>
 *   <li><strong>{@link gama.annotations.facet @facet}</strong> - Parameter and option definitions</li>
 * </ul>
 * 
 * <h3>Support Infrastructure</h3>
 * <ul>
 *   <li><strong>{@link gama.annotations.support.IConcept IConcept}</strong> - Semantic keyword definitions</li>
 *   <li><strong>{@link gama.annotations.support.IOperatorCategory IOperatorCategory}</strong> - Operator classification</li>
 *   <li><strong>{@link gama.annotations.support.ISymbolKind ISymbolKind}</strong> - Symbol type definitions</li>
 * </ul>
 * 
 * <h2>Processing Pipeline</h2>
 * 
 * <p>The annotation framework operates through a sophisticated processing pipeline:</p>
 * 
 * <ol>
 *   <li><strong>Annotation Processing:</strong> Compile-time analysis and validation of annotations</li>
 *   <li><strong>Metadata Generation:</strong> Creation of runtime metadata for GAML integration</li>
 *   <li><strong>Documentation Generation:</strong> Automatic generation of user documentation</li>
 *   <li><strong>IDE Integration:</strong> Support for code completion and assistance</li>
 *   <li><strong>Runtime Registration:</strong> Dynamic registration of GAML elements</li>
 * </ol>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Defining a Simple Agent Species</h3>
 * <pre>{@code
 * @species(
 *     name = "animal",
 *     skills = { "moving" },
 *     doc = @doc("Basic animal agent with movement capability")
 * )
 * @vars({
 *     @variable(name = "energy", type = IType.FLOAT, init = "100.0", 
 *               doc = @doc("Current energy level of the animal")),
 *     @variable(name = "age", type = IType.INT, init = "0",
 *               doc = @doc("Age of the animal in simulation steps"))
 * })
 * public class Animal extends AbstractAgent {
 *     
 *     @getter("energy")
 *     public Double getEnergy(final IAgent agent) {
 *         return (Double) agent.getAttribute("energy");
 *     }
 *     
 *     @setter("energy") 
 *     public void setEnergy(final IAgent agent, final Double value) {
 *         agent.setAttribute("energy", Math.max(0.0, value));
 *     }
 *     
 *     @action(
 *         name = "eat",
 *         args = @arg(name = "food_amount", type = IType.FLOAT),
 *         doc = @doc(
 *             value = "Increases the animal's energy by consuming food",
 *             examples = @example("do eat food_amount: 10.0;")
 *         )
 *     )
 *     public Object eat(final IScope scope) throws GamaRuntimeException {
 *         Double foodAmount = scope.getFloatArg("food_amount");
 *         IAgent agent = getCurrentAgent(scope);
 *         Double currentEnergy = (Double) agent.getAttribute("energy");
 *         setEnergy(agent, currentEnergy + foodAmount);
 *         return null;
 *     }
 * }
 * }</pre>
 * 
 * <h3>Creating a Behavioral Skill</h3>
 * <pre>{@code
 * @skill(
 *     name = "foraging",
 *     concept = { IConcept.SKILL, IConcept.AGENT_MOVEMENT, IConcept.ECOLOGY },
 *     doc = @doc("Enables agents to search for and collect food resources")
 * )
 * public class ForagingSkill extends Skill {
 *     
 *     @action(
 *         name = "search_food",
 *         args = @arg(name = "search_radius", type = IType.FLOAT, optional = true),
 *         doc = @doc(
 *             value = "Searches for food within the specified radius",
 *             returns = "List of food items found, or empty list if none",
 *             examples = {
 *                 @example("list<food> nearby_food <- search_food(10.0);"),
 *                 @example("list<food> food <- search_food();")
 *             }
 *         )
 *     )
 *     public Object searchFood(final IScope scope) throws GamaRuntimeException {
 *         // Implementation here
 *         return GamaListFactory.create();
 *     }
 * }
 * }</pre>
 * 
 * <h2>Design Principles</h2>
 * 
 * <h3>Separation of Concerns</h3>
 * <p>The framework separates different aspects of model definition:</p>
 * <ul>
 *   <li><strong>Structure:</strong> What agents and skills exist</li>
 *   <li><strong>Behavior:</strong> What actions agents can perform</li>
 *   <li><strong>Data:</strong> What attributes agents possess</li>
 *   <li><strong>Documentation:</strong> How elements are described and used</li>
 * </ul>
 * 
 * <h3>Composition over Inheritance</h3>
 * <p>The skill system promotes composition by allowing agents to acquire behaviors 
 * through skill attachment rather than deep inheritance hierarchies.</p>
 * 
 * <h3>Declarative Configuration</h3>
 * <p>Annotations provide a declarative way to configure GAML elements, reducing 
 * boilerplate code and improving maintainability.</p>
 * 
 * <h3>Comprehensive Documentation</h3>
 * <p>The documentation system ensures that all GAML elements are properly documented 
 * with examples, usage patterns, and cross-references.</p>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <ul>
 *   <li><strong>Compile-time Processing:</strong> Most annotation processing occurs during compilation</li>
 *   <li><strong>Runtime Efficiency:</strong> Minimal runtime overhead for annotation-based elements</li>
 *   <li><strong>Memory Usage:</strong> Efficient metadata storage and lookup</li>
 *   <li><strong>Lazy Loading:</strong> Documentation and metadata loaded only when needed</li>
 * </ul>
 * 
 * <h2>Migration and Compatibility</h2>
 * 
 * <p>The annotation framework maintains backward compatibility while evolving:</p>
 * <ul>
 *   <li>Deprecated annotations are clearly marked with migration guidance</li>
 *   <li>New features are added through optional annotation properties</li>
 *   <li>Version-specific documentation helps with migration</li>
 *   <li>Legacy patterns are supported while new patterns are encouraged</li>
 * </ul>
 * 
 * @author GAMA Development Team
 * @version 2.0
 * @since GAMA 1.0
 */
package gama.annotations;
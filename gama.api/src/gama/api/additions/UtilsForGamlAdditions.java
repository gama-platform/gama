/*******************************************************************************************************
 *
 * UtilsForGamlAdditions.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;

import java.util.Arrays;
import java.util.Collections;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.constants.IKeyword;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;
import gama.api.kernel.PlatformAgent;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.date.IDate;
import gama.api.types.file.IGamaFile;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.IGraph;
import gama.api.types.graph.IPath;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.types.pair.IPair;
import gama.api.types.topology.ITopology;

/**
 * An abstract utility class providing convenient shorthand fields and helper methods for implementing
 * GAML additions. This class is designed to be extended by GamlAdditions classes that define operators,
 * actions, skills, and other GAML language extensions.
 * 
 * <p>The class provides:</p>
 * <ul>
 *   <li><strong>Type Shortcuts:</strong> Single-letter fields for common GAMA types (e.g., I for Integer, S for String)</li>
 *   <li><strong>Class Shortcuts:</strong> Two-letter fields for GAMA framework classes (e.g., IA for IAgent, GP for IPair)</li>
 *   <li><strong>Helper Methods:</strong> Convenience methods for creating arrays, types, and descriptions</li>
 *   <li><strong>Common Constants:</strong> Boolean and primitive type references</li>
 * </ul>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>This class significantly reduces boilerplate code when declaring GAML additions by providing
 * compact, readable shortcuts. Instead of writing lengthy class names and type references, developers
 * can use single or double-letter abbreviations.</p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * public class MyGamlAdditions extends UtilsForGamlAdditions implements IGamlAdditions {
 *     
 *     // Use shortcuts for operator declaration
 *     _operator("my_op", 
 *         _class(D),           // Returns Double.class
 *         _signature(I, I),    // Takes two Integers
 *         _doc("My operator")
 *     );
 *     
 *     // Create type references
 *     IType<?> stringType = T(S);     // Type for String
 *     String typeId = Ti(IA);          // Type ID for IAgent
 * }
 * }</pre>
 *
 * @author GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.gaml.types.Types
 * @see gama.api.gaml.GAML
 */
public abstract class UtilsForGamlAdditions {

	/** Empty integer array constant for convenience. */
	protected int[] AI = {};

	/** Empty string array constant for convenience. */
	protected String[] AS = {};

	/** Boolean false constant - shorthand for false. */
	protected boolean F = false;

	/** Boolean true constant - shorthand for true. */
	protected boolean T = true;

	/** The PRIMITIVE keyword constant from IKeyword. */
	protected String PRIM = IKeyword.PRIMITIVE;

	/** Shortcut for IAgent.class - represents the agent interface. */
	protected Class<?> IA = IAgent.class;

	/** Shortcut for PlatformAgent.class - represents the platform agent. */
	protected Class<?> PA = PlatformAgent.class;

	/** Shortcut for ITopology.class - represents the topology interface. */
	protected Class<?> IT = ITopology.class;

	/** Shortcut for ISpecies.class - represents the species interface. */
	protected Class<?> SP = ISpecies.class;

	/** Shortcut for IColor.class - represents the color interface. */
	protected Class<?> GC = IColor.class;

	/** Shortcut for IPair.class - represents the pair interface. */
	protected Class<?> GP = IPair.class;

	/** Shortcut for IShape.class - represents the shape/geometry interface. */
	protected Class<?> GS = IShape.class;

	/** Shortcut for Object.class - represents any Java object. */
	protected Class<?> O = Object.class;

	/** Shortcut for Boolean.class - represents the Boolean wrapper class. */
	protected Class<?> B = Boolean.class;

	/** Shortcut for Integer.class - represents the Integer wrapper class. */
	protected Class<?> I = Integer.class;

	/** Shortcut for Double.class - represents the Double wrapper class. */
	protected Class<?> D = Double.class;

	/** Shortcut for String.class - represents the String class. */
	protected Class<?> S = String.class;

	/** Shortcut for IExpression.class - represents the GAML expression interface. */
	protected Class<?> IE = IExpression.class;

	/** Shortcut for IShape.class - represents the shape/geometry interface (duplicate of GS). */
	protected Class<?> IS = IShape.class;

	/** Shortcut for IMap.class - represents the GAMA map interface. */
	protected Class<?> GM = IMap.class;

	/** Shortcut for IPoint.class - represents the point/coordinate interface. */
	protected Class<?> P = IPoint.class;

	/** Shortcut for IContainer.class - represents the container interface. */
	protected Class<?> IC = IContainer.class;

	/** Shortcut for IPoint.class - represents the point interface (duplicate of P). */
	protected Class<?> IL = IPoint.class;

	/** Shortcut for IList.class - represents the GAMA list interface. */
	protected Class<?> LI = IList.class;

	/** Shortcut for IMatrix.class - represents the matrix interface. */
	protected Class<?> IM = IMatrix.class;

	/** Shortcut for IGraph.class - represents the graph interface. */
	protected Class<?> GR = IGraph.class;

	/** Shortcut for IPath.class - represents the path interface. */
	protected Class<?> IP = IPath.class;

	/** Shortcut for IGamaFile.class - represents the GAMA file interface. */
	protected Class<?> GF = IGamaFile.class;

	/** Shortcut for IScope.class - represents the execution scope interface. */
	protected Class<?> SC = IScope.class;

	/** Shortcut for IDate.class - represents the GAMA date interface. */
	protected Class<?> GD = IDate.class;

	/** Shortcut for int.class - the primitive int type. */
	protected Class<?> i = int.class;

	/** Shortcut for double.class - the primitive double type. */
	protected Class<?> d = double.class;

	/** Shortcut for boolean.class - the primitive boolean type. */
	protected Class<?> b = boolean.class;

	/** Shortcut for the built-in types manager - provides access to GAMA's type system. */
	protected ITypesManager TM = Types.getBuiltInTypeManager();

	/**
	 * A utility wrapper class for holding child descriptions. This class is used to group multiple
	 * IDescription children for use in description creation methods. It provides a convenient way
	 * to pass variable-length child descriptions to the desc() methods.
	 */
	public static class Children {

		/** The collection of child descriptions wrapped by this instance. */
		private final Iterable<IDescription> children;

		/**
		 * Instantiates a new Children wrapper with the specified child descriptions.
		 * If no descriptions are provided or the array is null, an empty collection is created.
		 *
		 * @param descs
		 *            the child descriptions to wrap, or null/empty for no children
		 */
		public Children(final IDescription... descs) {
			if (descs == null || descs.length == 0) {
				children = Collections.emptyList();
			} else {
				children = Arrays.asList(descs);
			}
		}

		/**
		 * Gets the child descriptions wrapped by this Children instance.
		 *
		 * @return the iterable collection of child descriptions
		 */
		public Iterable<IDescription> getChildren() { return children; }

	}

	/**
	 * Creates a new IDescription with the specified keyword, child descriptions, and facets.
	 * This method is used to create complex descriptions that have nested child descriptions.
	 *
	 * @param keyword
	 *            the GAML keyword for this description (e.g., "species", "action", "aspect")
	 * @param children
	 *            the Children wrapper containing child descriptions to nest within this description
	 * @param facets
	 *            variable-length array of facet key-value pairs (alternating keys and values)
	 * @return the newly created IDescription instance
	 */
	protected IDescription desc(final String keyword, final Children children, final String... facets) {
		return GAML.getDescriptionFactory().create(keyword, null, children.getChildren(), facets);
	}

	/**
	 * Creates a new IDescription with the specified keyword and facets.
	 * This is a simplified version for creating descriptions without child descriptions.
	 *
	 * @param keyword
	 *            the GAML keyword for this description (e.g., "species", "action", "aspect")
	 * @param facets
	 *            variable-length array of facet key-value pairs (alternating keys and values)
	 * @return the newly created IDescription instance
	 */
	protected IDescription desc(final String keyword, final String... facets) {
		return GAML.getDescriptionFactory().create(keyword, facets);
	}

	/**
	 * Creates a VariableDescription with the specified type ID and facets.
	 * The type ID is converted to its string representation for description creation.
	 *
	 * @param keyword
	 *            the type ID constant from IType (e.g., IType.INT, IType.FLOAT)
	 * @param facets
	 *            variable-length array of facet key-value pairs (alternating keys and values)
	 * @return the newly created variable IDescription instance
	 */
	protected IDescription desc(final int keyword, final String... facets) {
		final IType t = Types.get(keyword);
		return desc(t.toString(), facets);
	}

	/**
	 * Creates a String array from the provided string arguments. This is a convenience method
	 * for creating string arrays in a more readable way when declaring GAML additions.
	 *
	 * @param strings
	 *            the strings to include in the array
	 * @return a String array containing the provided strings
	 */
	protected String[] S(final String... strings) {
		return strings;
	}

	/**
	 * Creates an int array from the provided integer arguments. This is a convenience method
	 * for creating int arrays in a more readable way when declaring GAML additions.
	 *
	 * @param integers
	 *            the integers to include in the array
	 * @return an int array containing the provided integers
	 */
	protected int[] I(final int... integers) {
		return integers;
	}

	/**
	 * Creates a FacetProto array from the provided facet prototype arguments. This is used
	 * for defining facet prototypes when declaring GAML statements or other language constructs.
	 *
	 * @param protos
	 *            the facet prototypes to include in the array
	 * @return a FacetProto array containing the provided prototypes
	 */
	protected IArtefactProto.Facet[] P(final IArtefactProto.Facet... protos) {
		return protos;
	}

	/**
	 * Creates a Class array from the provided class arguments. This is a convenience method
	 * for creating class arrays, commonly used for defining operator signatures and type parameters.
	 *
	 * @param classes
	 *            the classes to include in the array
	 * @return a Class array containing the provided classes
	 */
	protected Class[] C(final Class... classes) {
		return classes;
	}

	/**
	 * Gets the GAMA type corresponding to the specified Java class. This method converts
	 * a Java class reference to its corresponding GAMA type representation.
	 *
	 * @param c
	 *            the Java class to convert to a GAMA type
	 * @return the IType corresponding to the specified class
	 */
	protected IType<?> T(final Class<?> c) {
		return Types.get(c);
	}

	/**
	 * Gets the type ID (as a string) for the GAMA type corresponding to the specified Java class.
	 * This returns the numeric type ID converted to a string representation.
	 *
	 * @param c
	 *            the Java class to get the type ID for
	 * @return the string representation of the type ID
	 */
	protected String Ti(final Class<?> c) {
		return String.valueOf(Types.get(c).id());
	}

	/**
	 * Gets the type name (as a string) for the GAMA type corresponding to the specified Java class.
	 * This returns the human-readable type name (e.g., "int", "float", "agent").
	 *
	 * @param c
	 *            the Java class to get the type name for
	 * @return the string representation of the type name
	 */
	protected String Ts(final Class<?> c) {
		return Types.get(c).toString();
	}

	/**
	 * Gets the GAMA type corresponding to the specified type name string.
	 * This method looks up a type by its string name (e.g., "int", "agent", "list").
	 *
	 * @param c
	 *            the type name as a string
	 * @return the IType corresponding to the specified type name
	 */
	protected IType T(final String c) {
		return Types.get(c);
	}

	/**
	 * Gets the GAMA type corresponding to the specified type ID constant.
	 * This method retrieves a type using one of the numeric type ID constants from IType.
	 *
	 * @param c
	 *            the type ID constant (e.g., IType.INT, IType.FLOAT, IType.AGENT)
	 * @return the IType corresponding to the specified type ID
	 */
	protected IType T(final int c) {
		return Types.get(c);
	}

}

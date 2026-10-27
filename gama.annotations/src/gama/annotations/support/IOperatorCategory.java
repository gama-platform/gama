/*******************************************************************************************************
 *
 * IOperatorCategory.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.annotations.support;

/**
 * Written by gaudou Modified on 1 mars 2014
 *
 * Description: all the possible categories for operators
 *
 */
public interface IOperatorCategory {

	/** The Constant ARITHMETIC. */
	String ARITHMETIC = "Arithmetic operators";

	/** The Constant LOGIC. */
	String LOGIC = "Logical operators";

	/** The Constant RANDOM. */
	String RANDOM = "Random operators";

	/** The Constant STATISTICAL. */
	String STATISTICAL = "Statistical operators";

	/** The Constant COMPARISON. */
	String COMPARISON = "Comparison operators";

	/** The Constant CASTING. */
	String CASTING = "Casting operators";

	/** The Constant COLOR. */
	String COLOR = "Color-related operators";

	/** The Constant SYSTEM. */
	String SYSTEM = "System";

	/** The Constant EDP. */
	String EDP = "EDP-related operators";

	/** The Constant SPATIAL. */
	String SPATIAL = "Spatial operators";

	/** The Constant SHAPE. */
	String SHAPE = "Shape";

	/** The Constant THREED. */
	String THREED = "3D";

	/** The Constant SP_STATISTICAL. */
	String SP_STATISTICAL = "Spatial statistical operators";

	/** The Constant SP_QUERIES. */
	String SP_QUERIES = "Spatial queries operators";

	/** The Constant SP_PROPERTIES. */
	String SP_PROPERTIES = "Spatial properties operators";

	/** The Constant SP_RELATIONS. */
	String SP_RELATIONS = "Spatial relations operators";

	/** The Constant SP_TRANSFORMATIONS. */
	String SP_TRANSFORMATIONS = "Spatial transformations operators";

	/** The Constant ITERATOR. */
	String ITERATOR = "Iterator operators";

	/** The Constant CONTAINER. */
	String CONTAINER = "Containers-related operators";

	/** The Constant MATRIX. */
	String MATRIX = "Matrix-related operators";

	/** The Constant STRING. */
	String STRING = "Strings-related operators";

	/** The Constant LIST. */
	String LIST = "List-related operators";

	/** The Constant MAP. */
	String MAP = "Map-related operators";

	/** The Constant GRAPH. */
	String GRAPH = "Graphs-related operators";

	/** The Constant FILE. */
	String FILE = "Files-related operators";

	/** The Constant SPECIES. */
	String SPECIES = "Species-related operators";

	/** The Constant GRID. */
	String GRID = "Grid-related operators";

	/** The Constant PATH. */
	String PATH = "Path-related operators";

	/** The Constant DATE. */
	String DATE = "Date-related operators";

	/** The Constant TYPE. */
	String TYPE = "Types-related operators";

	/** The Constant POINT. */
	String POINT = "Points-related operators";

	/** The Constant WATER. */
	String WATER = "Water level operators";

	/** The Constant FIPA. */
	String FIPA = "FIPA-related operators";

	/** The Constant MAP_COMPARAISON. */
	String MAP_COMPARAISON = "Map comparaison operators";

	/** The Constant USER_CONTROL. */
	String USER_CONTROL = "User control operators";

	/** The Constant TIME. */
	String TIME = "Time-related operators";

	/** The Constant DEPRECATED. */
	String DEPRECATED = "DeprecatedOperators";

	/** The Constant DRIVING. */
	String DRIVING = "Driving operators";

	/** The Constant GENSTAR. */
	String GENSTAR = "Genstar operators";

	/** The Constant SPARQL. */
	String SPARQL = "SPARQL operators";

	/** The Constant IMAGE. */
	public static final String IMAGE			= "Image operators";

	/** The Constant DISPLAY. */
	public static final String DISPLAY			= "Display operators";

}

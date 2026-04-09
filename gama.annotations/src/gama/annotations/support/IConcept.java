/*******************************************************************************************************
 *
 * IConcept.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.annotations.support;

import gama.annotations.constants.IKeyword;

/**
 * The {@code IConcept} interface defines semantic keywords used throughout GAMA
 * for documentation, search, and
 * categorization purposes. These concepts provide a controlled vocabulary that
 * helps users discover related
 * functionality and enables consistent documentation across the platform.
 *
 * <p>
 * <strong>Architecture:</strong>
 * </p>
 * The concept system serves multiple purposes:
 * <ul>
 * <li><strong>Documentation Search:</strong> Users can find related elements by
 * concept keywords</li>
 * <li><strong>Semantic Grouping:</strong> Related functionality is grouped by
 * conceptual similarity</li>
 * <li><strong>Website Organization:</strong> Online documentation is structured
 * using these concepts</li>
 * <li><strong>IDE Integration:</strong> Development tools use concepts for
 * intelligent assistance</li>
 * <li><strong>Cross-referencing:</strong> Automatic generation of "see also"
 * relationships</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Patterns:</strong>
 * </p>
 * Concepts are used in annotation metadata to tag GAML elements:
 *
 * <pre>{@code
 * // Tagging a movement skill with relevant concepts
 * &#64;skill(
 *     name = "moving",
 *     concept = { IConcept.SKILL, IConcept.AGENT_MOVEMENT, IConcept.SPATIAL }
 * )
 *
 * // Tagging a spatial action
 * &#64;action(
 *     name = "goto",
 *     concept = { IConcept.ACTION, IConcept.AGENT_MOVEMENT, IConcept.PATHFINDING }
 * )
 *
 * // Tagging an ecological species
 * &#64;species(
 *     name = "predator",
 *     concept = { IConcept.SPECIES, IConcept.ECOLOGY, IConcept.PREDATION }
 * )
 * }</pre>
 *
 * <p>
 * <strong>Concept Categories:</strong>
 * </p>
 * The concepts are organized into several major categories:
 * <ul>
 * <li><strong>Structural:</strong> Basic GAMA elements (ACTION, SPECIES, SKILL,
 * etc.)</li>
 * <li><strong>Behavioral:</strong> Agent capabilities (AGENT_MOVEMENT,
 * COMMUNICATION, etc.)</li>
 * <li><strong>Spatial:</strong> Spatial concepts (SPATIAL, TOPOLOGY, GEOMETRY,
 * etc.)</li>
 * <li><strong>Domain:</strong> Application domains (ECOLOGY, TRANSPORT, SOCIAL,
 * etc.)</li>
 * <li><strong>Technical:</strong> Implementation aspects (ALGORITHM,
 * OPTIMIZATION, etc.)</li>
 * <li><strong>Data:</strong> Data handling (DATABASE, FILE, CHART, etc.)</li>
 * </ul>
 *
 * <p>
 * <strong>Best Practices:</strong>
 * </p>
 * <ul>
 * <li>Use multiple relevant concepts to improve discoverability</li>
 * <li>Include both general and specific concepts when appropriate</li>
 * <li>Prefer predefined constants over custom strings</li>
 * <li>Consider the end user's search perspective when selecting concepts</li>
 * <li>Keep concept lists focused and relevant</li>
 * </ul>
 *
 * <p>
 * <strong>Documentation Integration:</strong>
 * </p>
 * These concepts are automatically processed by GAMA's documentation system to:
 * <ul>
 * <li>Generate thematic indexes and cross-references</li>
 * <li>Power the search functionality on the GAMA website</li>
 * <li>Organize help content in the IDE</li>
 * <li>Create concept-based learning paths in tutorials</li>
 * </ul>
 *
 * @author GAMA Development Team
 * @since GAMA 1.0
 */
public interface IConcept {

	// ============================================================================
	// STRUCTURAL CONCEPTS - Fundamental GAMA elements
	// ============================================================================

	/**
	 * Identifies elements that represent executable behaviors in GAML. Used for
	 * actions, operators, and other
	 * executable elements.
	 */
	String ACTION = IKeyword.ACTION;

	// ============================================================================
	// BEHAVIORAL CONCEPTS - Agent capabilities and behaviors
	// ============================================================================

	/**
	 * Identifies elements related to agent spatial positioning and location
	 * management. Includes location
	 * getters/setters, spatial queries, and positioning operations.
	 */
	String AGENT_LOCATION = "agent_location";

	/**
	 * Identifies elements that enable agent movement and navigation. Covers
	 * movement actions, pathfinding, and
	 * mobility-related functionality.
	 */
	String AGENT_MOVEMENT = "agent_movement";

	// ============================================================================
	// TECHNICAL CONCEPTS - Algorithms and computational methods
	// ============================================================================

	/**
	 * Identifies elements that implement specific algorithms or computational
	 * methods. Used for optimization
	 * algorithms, AI techniques, and mathematical procedures.
	 */
	String ALGORITHM = "algorithm";

	/**
	 * Identifies elements related to agent internal structure and decision-making
	 * frameworks. Covers cognitive
	 * architectures, behavior trees, and decision systems.
	 */
	String ARCHITECTURE = "architecture";

	/**
	 * Identifies elements that perform mathematical calculations and operations.
	 * Includes mathematical operators,
	 * functions, and numerical computations.
	 */
	String ARITHMETIC = "arithmetic";

	/**
	 * File format identifier for ASCII text files. Used in file I/O operations and
	 * data import/export functionality.
	 */
	String ASC = "asc";

	// ============================================================================
	// DATA CONCEPTS - Data structures and properties
	// ============================================================================

	/**
	 * Identifies elements that represent properties or characteristics of agents or
	 * objects. Covers variables, getters,
	 * setters, and property management.
	 */
	String ATTRIBUTE = "attribute";

	/**
	 * Identifies elements related to automatic saving and persistence
	 * functionality. Used for simulation state
	 * management and data preservation.
	 */
	String AUTOSAVE = "autosave";

	/** The Constant BACKGROUND. */
	String BACKGROUND = "background";

	/** The Constant BATCH. */
	String BATCH = IKeyword.BATCH;

	/** The Constant BDI. */
	String BDI = "bdi";

	/** The Constant BEHAVIOR. */
	String BEHAVIOR = "behavior";

	/** The Constant CAMERA. */
	String CAMERA = "camera";

	/** The Constant CAST. */
	String CAST = "cast";

	/** The Constant CHART. */
	String CHART = "chart";

	/** The Constant CLUSTERING. */
	String CLUSTERING = "clustering";

	/** The Constant COLOR. */
	String COLOR = "color";

	/** The Constant COMODEL. */
	String COMODEL = "comodel";

	/** The Constant COMPARISON. */
	String COMPARISON = "comparison";

	/** The Constant COMMUNICATION. */
	String COMMUNICATION = "communication";

	/** The Constant CONDITION. */
	String CONDITION = "condition";

	/** The Constant CONSTANT. */
	String CONSTANT = "constant";

	/** The Constant CONTAINER. */
	String CONTAINER = "container";

	/** The Constant CSV. */
	String CSV = "csv";

	/** The Constant CYCLE. */
	String CYCLE = "cycle";

	/** The Constant DATE. */
	String DATE = "date";

	/** The Constant DATABASE. */
	String DATABASE = "database";

	/** The Constant DEM. */
	String DEM = "dem";

	/** The Constant DGS. */
	String DGS = "dgs";

	/** The Constant DIFFUSION. */
	String DIFFUSION = "diffusion";

	/** The Constant DIMENSION. */
	String DIMENSION = "dimension";

	/** The Constant DISPLAY. */
	String DISPLAY = "display";

	/** The Constant DISTRIBUTION. */
	String DISTRIBUTION = "distribution";

	/** The Constant DXF. */
	String DXF = "dxf";

	/** The Constant EDGE. */
	String EDGE = "edge";

	/** The Constant ELEVATION. */
	String ELEVATION = "elevation";

	/** The Constant ENUMERATION. */
	String ENUMERATION = "enumeration";

	/** The Constant EQUATION. */
	String EQUATION = "equation";

	/** The Constant EXPERIMENT. */
	String EXPERIMENT = "experiment";

	/** The Constant FACET. */
	String FACET = "facet";

	/** The Constant FILE. */
	String FILE = "file";

	/** The Constant FILTER. */
	String FILTER = "filter";

	/** The Constant FIPA. */
	String FIPA = "fipa";

	/** The Constant FSM. */
	String FSM = "fsm";

	/** The Constant GEOMETRY. */
	String GEOMETRY = "geometry";

	/** The Constant GIS. */
	String GIS = "gis";

	/** The Constant GLOBAL. */
	String GLOBAL = "global";

	/** The Constant GRAPH. */
	String GRAPH = "graph";

	/** The Constant GRAPH_WEIGHT. */
	String GRAPH_WEIGHT = "graph_weight";

	/** The Constant GML. */
	String GML = "gml";

	/** The Constant GRID. */
	String GRID = "grid";

	/** The Constant GRAPHIC. */
	String GRAPHIC = "graphic";

	/** The Constant GRAPHIC_UNIT. */
	String GRAPHIC_UNIT = "graphic_unit";

	/** The Constant GUI. */
	String GUI = "gui";

	/** The Constant HALT. */
	String HALT = "halt";

	/** The Constant HEADLESS. */
	String HEADLESS = "headless";

	/** The Constant HYDROLOGY. */
	String HYDROLOGY = "hydrology";

	/** The Constant IMAGE. */
	String IMAGE = "image";

	/** The Constant IMPORT. */
	String IMPORT = "import";

	/** The Constant INHERITANCE. */
	String INHERITANCE = "inheritance";

	/** The Constant INIT. */
	String INIT = "init";

	/** The Constant INSPECTOR. */
	String INSPECTOR = "inspector";

	/** The Constant LAYER. */
	String LAYER = "layer";

	/** The Constant LENGTH_UNIT. */
	String LENGTH_UNIT = "length_unit";

	/** The Constant LIGHT. */
	String LIGHT = "light";

	/** The Constant LIST. */
	String LIST = "list";

	String DATAFRAME = "dataframe";

	/** The Constant LOAD_FILE. */
	String LOAD_FILE = "load_file";

	/** The Constant LOGICAL. */
	String LOGICAL = "logical";

	/** The Constant LOOP. */
	String LOOP = "loop";

	/** The Constant MATRIX. */
	String MATRIX = "matrix";

	/** The Constant MATH. */
	String MATH = "math";

	/** The Constant MAP. */
	String MAP = "map";

	/** The Constant MIRROR. */
	String MIRROR = "mirror";

	/** The Constant MODEL. */
	String MODEL = "model";

	/** The Constant MONITOR. */
	String MONITOR = "monitor";

	/** The Constant MULTI_LEVEL. */
	String MULTI_LEVEL = "multi_level";

	/** The Constant MULTI_CRITERIA. */
	String MULTI_CRITERIA = "multi_criteria";

	/** The Constant MULTI_SIMULATION. */
	String MULTI_SIMULATION = "multi_simulation";

	/** The Constant NEIGHBORS. */
	String NEIGHBORS = "neighbors";

	/** The Constant NETWORK. */
	String NETWORK = "network";

	/** The Constant NIL. */
	String NIL = "nil";

	/** The Constant NODE. */
	String NODE = "node";

	/** The Constant OBJ. */
	String OBJ = "obj";

	/** The Constant OBSTACLE. */
	String OBSTACLE = "obstacle";

	/** The Constant OPENGL. */
	String OPENGL = "opengl";

	/** The Constant OPERATOR. */
	String OPERATOR = "operator";

	/** The Constant OPTIMIZATION. */
	String OPTIMIZATION = "optimization";

	/** The Constant OSM. */
	String OSM = "osm";

	/** The Constant OUTPUT. */
	String OUTPUT = "output";

	/** The Constant OVERLAY. */
	String OVERLAY = "overlay";

	/** The Constant PARAMETER. */
	String PARAMETER = "parameter";

	/** The Constant PAUSE. */
	String PAUSE = "pause";

	/** The Constant PERMANENT. */
	String PERMANENT = "permanent";

	/** The Constant PHYSICS_ENGINE. */
	String PHYSICS_ENGINE = "physics_engine";

	/** The Constant POINT. */
	String POINT = "point";

	/** The Constant PROBABILITY. */
	String PROBABILITY = "probability";

	/** The Constant PSEUDO_VARIABLE. */
	String PSEUDO_VARIABLE = "pseudo_variable";

	/** The Constant R. */
	String R = "r";

	/** The Constant RANDOM. */
	String RANDOM = "random";

	/** The Constant RANDOM_OPERATOR. */
	String RANDOM_OPERATOR = "random_operator";

	/** The Constant RASTER. */
	String RASTER = "raster";

	/** The Constant REGRESSION. */
	String REGRESSION = "regression";

	/** The Constant REFLEX. */
	String REFLEX = "reflex";

	/** The Constant REFRESH. */
	String REFRESH = "refresh";

	/** The Constant SAVE_FILE. */
	String SAVE_FILE = "save_file";

	/** The Constant SCHEDULER. */
	String SCHEDULER = "scheduler";

	/** The Constant SERIALIZE. */
	String SERIALIZE = "serialize";

	/** The Constant SHAPE. */
	String SHAPE = IKeyword.SHAPE;

	/** The Constant SHAPEFILE. */
	String SHAPEFILE = "shapefile";

	/** The Constant SHORTEST_PATH. */
	String SHORTEST_PATH = "shortest_path";

	/** The Constant SKILL. */
	String SKILL = IKeyword.SKILL;

	/** The Constant SOUND. */
	String SOUND = "sound";

	/** The Constant SPATIAL_COMPUTATION. */
	String SPATIAL_COMPUTATION = "spatial_computation";

	/** The Constant SPATIAL_RELATION. */
	String SPATIAL_RELATION = "spatial_relation";

	/** The Constant SPATIAL_TRANSFORMATION. */
	String SPATIAL_TRANSFORMATION = "spatial_transformation";

	/** The Constant SPECIES. */
	String SPECIES = IKeyword.SPECIES;

	/** The class. */
	String CLASS = IKeyword.CLASS;

	/** The Constant SPORT. */
	String SPORT = "sport";

	/** The Constant STATISTIC. */
	String STATISTIC = "statistic";

	/** The Constant STRING. */
	String STRING = "string";

	/** The Constant SURFACE_UNIT. */
	String SURFACE_UNIT = "surface_unit";

	/** The Constant SVG. */
	String SVG = "svg";

	/** The Constant SYSTEM. */
	String SYSTEM = "system";

	/** The Constant TASK_BASED. */
	String TASK_BASED = "task_based";

	/** The Constant TERNARY. */
	String TERNARY = "ternary";

	/** The Constant TEXT. */
	String TEXT = "text";

	/** The Constant TEXTURE. */
	String TEXTURE = "texture";

	/** The Constant TEST. */
	String TEST = IKeyword.TEST;

	/** The Constant THREED. */
	String THREED = "3d";

	/** The Constant TIF. */
	String TIF = "tif";

	/** The Constant TIME. */
	String TIME = "time";

	/** The Constant TIME_UNIT. */
	String TIME_UNIT = "time_unit";

	/** The Constant TOPOLOGY. */
	String TOPOLOGY = IKeyword.TOPOLOGY;

	/** The Constant TORUS. */
	String TORUS = IKeyword.TORUS;

	/** The Constant TRANSPORT. */
	String TRANSPORT = "transport";

	/** The Constant TXT. */
	String TXT = "txt";

	/** The Constant TYPE. */
	String TYPE = IKeyword.TYPE;

	/** The Constant UPDATE. */
	String UPDATE = IKeyword.UPDATE;

	/** The Constant VOLUME_UNIT. */
	String VOLUME_UNIT = "volume_unit";

	/** The Constant WEIGHT_UNIT. */
	String WEIGHT_UNIT = "weight_unit";

	/** The Constant WRITE. */
	String WRITE = "write";

	/** The Constant XML. */
	String XML = "xml";

	/** The Constant WORLD. */
	String WORLD = "world";
}

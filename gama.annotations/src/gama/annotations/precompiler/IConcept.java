/*******************************************************************************************************
 *
 * IConcept.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.annotations.precompiler;

/**
 * The Interface IConcept.
 */
public interface IConcept {
	// list of all the "concept" keywords used in the website.

	/*
	 * note : - some of those keywords refers directly to an action when this action is very useful (normally, when the
	 * user search an action, the result returned is the species/skill related) - GAML keywords can be tagged with those
	 * keywords by adding the attribute "keyword" in the declaration.
	 */

	/** The Constant ACTION. */
	String ACTION = "action";

	/** The Constant AGENT_LOCATION. */
	String AGENT_LOCATION = "agent_location";

	/** The Constant AGENT_MOVEMENT. */
	String AGENT_MOVEMENT = "agent_movement";

	/** The Constant ALGORITHM. */
	String ALGORITHM = "algorithm";

	/** The Constant ARCHITECTURE. */
	String ARCHITECTURE = "architecture";

	/** The Constant ARITHMETIC. */
	String ARITHMETIC = "arithmetic";

	/** The Constant ASC. */
	String ASC = "asc";

	/** The Constant ATTRIBUTE. */
	String ATTRIBUTE = "attribute";

	/** The Constant AUTOSAVE. */
	String AUTOSAVE = "autosave";

	/** The Constant BACKGROUND. */
	String BACKGROUND = "background";

	/** The Constant BATCH. */
	String BATCH = "batch";

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

	/** The constant CLASS. */
	String CLASS = "class";

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
	String SHAPE = "shape";

	/** The Constant SHAPEFILE. */
	String SHAPEFILE = "shapefile";

	/** The Constant SHORTEST_PATH. */
	String SHORTEST_PATH = "shortest_path";

	/** The Constant SKILL. */
	String SKILL = "skill";

	/** The Constant SOUND. */
	String SOUND = "sound";

	/** The Constant SPATIAL_COMPUTATION. */
	String SPATIAL_COMPUTATION = "spatial_computation";

	/** The Constant SPATIAL_RELATION. */
	String SPATIAL_RELATION = "spatial_relation";

	/** The Constant SPATIAL_TRANSFORMATION. */
	String SPATIAL_TRANSFORMATION = "spatial_transformation";

	/** The Constant SPECIES. */
	String SPECIES = "species";

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
	String TEST = "test";

	/** The Constant THREED. */
	String THREED = "3d";

	/** The Constant TIF. */
	String TIF = "tif";

	/** The Constant TIME. */
	String TIME = "time";

	/** The Constant TIME_UNIT. */
	String TIME_UNIT = "time_unit";

	/** The Constant TOPOLOGY. */
	String TOPOLOGY = "topology";

	/** The Constant TORUS. */
	String TORUS = "torus";

	/** The Constant TRANSPORT. */
	String TRANSPORT = "transport";

	/** The Constant TXT. */
	String TXT = "txt";

	/** The Constant TYPE. */
	String TYPE = "type";

	/** The Constant UPDATE. */
	String UPDATE = "update";

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

/*******************************************************************************************************
 *
 * IKeyword.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

/**
 * The class IKeyword. Defines most of the keywords used in GAMA and GAML.
 *
 * @author drogoul
 * @since 13 dec. 2011
 *
 */
public interface IKeyword {

	/** The Constant JAVA2D. */
	String _2D = "2d";

	/** The Constant OPENGL. */
	String _3D = "3d";

	/** The dot. */
	String _DOT = ".";

	/** The abort. */
	String ABORT = "abort";

	/** The action. */
	String ACTION = "action";

	/** The add. */
	String ADD = "add";

	/** The agent. */
	String AGENT = "agent";

	/** The agents. */
	String AGENTS = "agents";

	/** The aggregation. */
	String AGGREGATION = "aggregation";

	/** The all. */
	String ALL = "all";

	/** The alpha. */
	String ALPHA = "alpha";

	/** The among. */
	String AMONG = "among";

	/** The anchor. */
	String ANCHOR = "anchor";

	/** The angle. */
	String ANGLE = "angle";

	/** The annealing. */
	String ANNEALING = "annealing";

	/** The append horizontally. */
	String APPEND_HORIZONTALLY = "append_horizontally";

	/** The append vertically. */
	String APPEND_VERTICALLY = "append_vertically";

	/** The area. */
	String AREA = "area";

	/** The arg. */
	String ARG = "arg";

	/** The as. */
	String AS = "as";

	/** The ask. */
	String ASK = "ask";

	/** The aspect. */
	String ASPECT = "aspect";

	/** The at. */
	String AT = "at";

	/** The attributes. */
	String ATTRIBUTES = "attributes";

	/** The author. */
	String AUTHOR = "author";

	/** The autorun. */
	String AUTORUN = "autorun";

	/** The autosave. */
	String AUTOSAVE = "autosave";

	/** The avoid mask. */
	String AVOID_MASK = "avoid_mask";

	/** The axes. */
	String AXES = "axes";

	/** The background. */
	String BACKGROUND = "background";

	/** The bands. */
	String BANDS = "bands";

	/** The bar. */
	String BAR = "bar";

	/** The batch. */
	String BATCH = "batch";

	/** The batch outputs */
	String BATCH_OUTPUT = "results";

	/** The batch outputs */
	String BATCH_REPORT = "report";

	/** The batch outputs */
	String BATCH_VAR_OUTPUTS = "outputs";

	/** The benchmark. */
	String BENCHMARK = "benchmark";

	/** The beta^d coefficient */
	String BETAD = "betad";

	/** The bool. */
	String BOOL = "bool";

	/** The border. */
	String BORDER = "border";

	/** The bounds. */
	String BOUNDS = "bounds";

	/** The box whisker. */
	String BOX_WHISKER = "box_whisker";

	/** The break. */
	String BREAK = "break";

	/** The brighter. */
	String BRIGHTER = "brighter";

	/** The browse. */
	String BROWSE = "browse";

	/** The buffering. */
	String BUFFERING = "buffering";

	/** The camera. */
	String CAMERA = "camera";

	/** The capture. */
	String CAPTURE = "capture";

	/** The catch. */
	String CATCH = "catch";

	/** The category. */
	String CATEGORY = "category";

	/** The cell height. */
	String CELL_HEIGHT = "cell_height";

	/** The cell width. */
	String CELL_WIDTH = "cell_width";

	/** The center. */
	String CENTER = "center";

	/** The chart. */
	String CHART = "chart";

	/** The choose. */
	String CHOOSE = "choose";

	/** The color. */
	String COLOR = "color";

	/** The color blue. */
	String COLOR_BLUE = "blue";

	/** The color green. */
	String COLOR_GREEN = "green";

	/** The color red. */
	String COLOR_RED = "red";

	/** The condition. */
	String CONDITION = "condition";

	/** The const. */
	String CONST = "const";

	/** The container. */
	String CONTAINER = "container";

	/** The contents. */
	String CONTENTS = "contents";

	/** The continue. */
	String CONTINUE = "continue";

	/** The control. */
	String CONTROL = "control";

	/** The convolution. */
	String CONVOLUTION = "convolution";

	/** The create. */
	String CREATE = "create";

	/** The csv. */
	String CSV = "csv";

	/** The current state. */
	String CURRENT_STATE = "currentState";

	/** The Constant CYCLE. */
	String CYCLE = "cycle";

	/** The cycle length. */
	String CYCLE_LENGTH = "cycle_length";

	/** The darker. */
	String DARKER = "darker";

	/** The data. */
	String DATA = "data";

	/** The default. */
	String DEFAULT = "default";

	/** The depth. */
	String DEPTH = "depth";

	/** The description. */
	String DESCRIPTION = "description";

	/** The destination. */
	String DESTINATION = "destination";

	/** The dif2. */
	String DIF2 = "diff2";

	/** The diff. */
	String DIFF = "diff";

	/** The diffuse. */
	String DIFFUSE = "diffuse";

	/** The diffusion. */
	String DIFFUSION = "diffusion";

	/** The direction. */
	String DIRECTION = "direction";

	/** The directory. */
	String DIRECTORY = "directory";

	/** The disables. */
	String DISABLES = "disables";

	/** The display. */
	String DISPLAY = "display";

	/** The divide. */
	String DIVIDE = "/";

	/** The do. */
	String DO = "do";

	/** The dot. */
	String DOT = "dot";

	/** The draw. */
	String DRAW = "draw";

	/** The dynamic. */
	String DYNAMIC = "dynamic";

	/** The each. */
	String EACH = "each";

	/** The edge. */
	String EDGE = "edge";

	/** The edge species. */
	String EDGE_SPECIES = "edge_species";

	/** The elevation. */
	String ELEVATION = "elevation";

	/** The else. */
	String ELSE = "else";

	/** The enables. */
	String ENABLES = "enables";

	/** The end. */
	String END = "end";

	/** The enter. */
	String ENTER = "enter";

	/** The entities. */
	String ENTITIES = "entities";

	/** The environment. */
	String ENVIRONMENT = "environment";

	/** The equation. */
	String EQUATION = "equation";

	/** The equation left. */
	String EQUATION_LEFT = "left";

	/** The equation op. */
	/*
	 * Equations
	 */
	String EQUATION_OP = "=";

	/** The equation right. */
	String EQUATION_RIGHT = "right";

	/** The error. */
	String ERROR = "error";

	/** The event. */
	String EVENT = "event";

	/** The exists. */
	String EXISTS = "exists";

	/** The experiment. */
	String EXPERIMENT = "experiment";

	/** The exploded. */
	String EXPLODED = "exploded";

	/** The exploration. */
	String EXPLORATION = "exploration";

	/** The extension. */
	String EXTENSION = "extension";

	/** The extensions. */
	String EXTENSIONS = "extensions";

	/** Factorial sampling */
	String FACTORIAL = "factorial";

	/** The fading. */
	String FADING = "fading";

	/** The false. */
	String FALSE = "false";

	/** The field. */
	String FIELD = "field";

	/** The file. */
	String FILE = "file";

	/** The files. */
	String FILES = "files";

	/** The fill with. */
	String FILL_WITH = "fill_with";

	/** The fitness. */
	String FITNESS = "fitness";

	/** The float. */
	String FLOAT = "float";

	/** The focus on. */
	String FOCUS_ON = "focus_on";

	/** The folder. */
	String FOLDER = "folder";

	/** The font. */
	String FONT = "font";

	/** The footer. */
	String FOOTER = "footer";

	/** The format. */
	String FORMAT = "format";

	/** The frequency. */
	String FREQUENCY = "frequency";

	/** The from. */
	String FROM = "from";

	/** The fsm. */
	String FSM = "fsm";

	/** The fullscreen. */
	String FULLSCREEN = "fullscreen";

	/** The function. */
	String FUNCTION = "function";

	/** The gama. */
	String GAMA = "gama";

	/** The gap. */
	String GAP = "gap";

	/** The genetic. */
	String GENETIC = "genetic";

	/** The geometry. */
	String GEOMETRY = "geometry";

	/** The gis. */
	String GIS = "gis";

	/** The global. */
	String GLOBAL = "global";

	/** The gradient. */
	String GRADIENT = "gradient";

	/** The graph. */
	String GRAPH = "graph";

	/** The graphics. */
	String GRAPHICS = "graphics";

	/** The grayscale. */
	String GRAYSCALE = "grayscale";

	/** The grid. */
	String GRID = "grid";

	/** The grid population. */
	String GRID_LAYER = "display_grid";

	/** The grid value. */
	String GRID_VALUE = "grid_value";

	/** The grid x. */
	String GRID_X = "grid_x";

	/** The grid y. */
	String GRID_Y = "grid_y";

	/** The gui. */
	String GUI_ = "gui";

	/** The header. */
	String HEADER = "header";

	/** The headless ui. */
	// String HEADLESS_UI = "headless";

	/** The heading. */
	String HEADING = "heading";

	/** The heatmap. */
	String HEATMAP = "heatmap";

	/** The height. */
	String HEIGHT = "height";

	/** The highlight. */
	String HIGHLIGHT = "highlight";

	/** The hill climbing. */
	String HILL_CLIMBING = "hill_climbing";

	/** The histogram. */
	String HISTOGRAM = "histogram";

	/** The host. */
	String HOST = "host";

	/** The id. */
	String ID = "id";

	/** The if. */
	String IF = "if";

	/** The ignore. */
	String IGNORE = "ignore";

	/** The image. */
	String IMAGE = "image";

	/** The display image. */
	String IMAGE_LAYER = "image_layer";

	/** The in. */
	String IN = "in";

	/** The index. */
	String INDEX = "index";

	/** The init. */
	String INIT = "init";

	/** The inspect. */
	String INSPECT = "inspect";

	/** The int. */
	String INT = "int";

	/** The intensity. */
	String INTENSITY = "intensity";

	/** The internal. */
	String INTERNAL = "_internal_";

	/** The internal function. */
	String INTERNAL_FUNCTION = "internal_function";

	/** The invoke. */
	String INVOKE = "invoke";

	/** The is. */
	String IS = "is";

	/** The is light on. */
	String IS_LIGHT_ON = "light";

	/** The is skill. */
	String IS_SKILL = "is_skill";

	/** The isfolder. */
	String ISFOLDER = "is_folder";

	/** The item. */
	String ITEM = "item";

	/** The java. */
	String JAVA = "java";

	/** The Constant JAVA2D. */
	String JAVA2D = "java2D";

	/** The keep seed. */
	String KEEP_SEED = "keep_seed";

	/** The keep simulations. */
	String KEEP_SIMULATIONS = "keep_simulations";

	/** The key. */
	String KEY = "key";

	/** The keystone. */
	String KEYSTONE = "keystone";

	/** The layout. */
	String LAYOUT = "layout";

	/** The left. */
	String LEFT = "left";

	/** The legend. */
	String LEGEND = "legend";

	/** The let. */
	String LET = "let";

	/** Latin Hypercube Sampling */
	String LHS = "latinhypercube";

	/** The lighted. */
	String LIGHTED = "lighted";

	/** The line. */
	String LINE = "line";

	/** The linear attenuation. */
	String LINEAR_ATTENUATION = "linear_attenuation";

	/** The constant attenuation. */
	String CONSTANT_ATTENUATION = "constant_attenuation";

	/** The lines. */
	String LINES = "lines";

	/**
	 * TYPES
	 */
	String LIST = "list";

	/** The location. */
	String LOCATION = "location";

	/** The loop. */
	String LOOP = "loop";

	/** The map. */
	String MAP = "map";

	/** The mask. */
	String MASK = "mask";

	/** The match. */
	String MATCH = "match";

	/** The match between. */
	String MATCH_BETWEEN = "match_between";

	/** The match one. */
	String MATCH_ONE = "match_one";

	/** The match regex. */
	String MATCH_REGEX = "match_regex";

	/** The matrix. */
	String MATRIX = "matrix";

	/** The max. */
	String MAX = "max";

	/** The maximize. */
	String MAXIMIZE = "maximize";

	/** The members. */
	String MEMBERS = "members";

	/** The mersenne. */
	String MERSENNE = "mersenne";

	/** The mesh. */
	String MESH = "mesh";

	/** The message. */
	String MESSAGE = "message";

	/** The method. */
	String METHOD = "method";

	/** The migrate. */
	String MIGRATE = "migrate";

	/** The min. */
	String MIN = "min";

	/** The minimize. */
	String MINIMIZE = "minimize";

	/** The minus. */
	String MINUS = "-";

	/** The mirrors. */
	String MIRRORS = "mirrors";

	/** The mode. */
	String MODE = "mode";

	/** The model. */
	String MODEL = "model";

	/** The monitor. */
	String MONITOR = "monitor";

	/** The Morris method */
	String MORRIS = "morris";

	/** The mouse clicked. */
	String MOUSE_CLICK = "mouse_click";

	/** The mouse down. */
	String MOUSE_DOWN = "mouse_down";

	/** The mouse dragged. */
	String MOUSE_DRAG = "mouse_drag";

	/** The mouse entered. */
	String MOUSE_ENTER = "mouse_enter";

	/** The mouse exited. */
	String MOUSE_EXIT = "mouse_exit";

	/** The mouse menu. */
	String MOUSE_MENU = "mouse_menu";

	/** The mouse moved. */
	String MOUSE_MOVE = "mouse_move";

	/** The mouse up. */
	String MOUSE_UP = "mouse_up";

	/** The moving 3d skill. */
	String MOVING_3D_SKILL = "moving3D";

	/** The moving skill. */
	String MOVING_SKILL = "moving";

	/** The multiply. */
	String MULTIPLY = "*";

	/** The my. */
	String MY = "my";

	/** The mygraph. */
	String MYGRAPH = "my_graph";

	/** The myself. */
	String MYSELF = "myself";

	/** The name. */
	String NAME = "name";

	/** The neighbors. */
	String NEIGHBORS = "neighbors";

	/** The node. */
	String NODE = "node";

	/** The null. */
	String NULL = "nil";

	/** The number. */
	String NUMBER = "number";

	/** The of. */
	String OF = "of";

	/** The on. */
	String ON = "on";

	/** The on change. */
	String ON_CHANGE = "on_change";

	/** The Constant OPENGL. */
	String OPENGL = "opengl";

	/** The optional. */
	String OPTIONAL = "optional";

	/** The origin. */
	String ORIGIN = "**origin**";

	/** The no type inference keyword. Used to flag declarations that have a type explicitly set */
	String NO_TYPE_INFERENCE = "**no_type_inference**";

	/** The orthogonal sampling */
	String ORTHOGONAL = "orthogonal";

	/** The orthographic projection. */
	String ORTHOGRAPHIC_PROJECTION = "orthographic_projection";

	/** The output. */
	String OUTPUT = "output";

	/** The over. */
	String OVER = "over";

	/** The overlay. */
	String OVERLAY = "overlay";
	/** The overwrite. */
	String OVERWRITE = "overwrite";

	/** The pair. */
	String PAIR = "pair";

	/** The parallel. */
	// "
	String PARALLEL = "parallel";

	/** The parameter. */
	String PARAMETER = "parameter";

	/** The parameters. */
	String PARAMETERS = "parameters";

	/** The params. */
	String PARAMS = "params";

	/** The parent. */
	String PARENT = "parent";

	/** The path. */
	String PATH = "path";

	/** The pause sound. */
	String PAUSE_SOUND = "pause_sound";

	/** The peers. */
	String PEERS = "peers";

	/** The permanent. */
	String PERMANENT = "permanent";

	/** The perspective. */
	String PERSPECTIVE = "perspective";

	/** The pie. */
	String PIE = "pie";

	/** The pitch. */
	String PITCH = "pitch";

	/** The places. */
	String PLACES = "places";

	/** The platform. */
	String PLATFORM = "platform";

	/** The plus. */
	String PLUS = "+";

	/** The point. */
	String POINT = "point";

	/** The position. */
	String POSITION = "position";

	/** The pragma. */
	String PRAGMA = "pragma";

	/** The no experiment. */
	String PRAGMA_NO_EXPERIMENT = "no_experiment";

	/** The no info. */
	String PRAGMA_NO_INFO = "no_info";

	/** The no warning. */
	String PRAGMA_NO_WARNING = "no_warning";

	/** The pragma requires. */
	String PRAGMA_REQUIRES = "requires";

	/** The primitive. */
	String PRIMITIVE = "primitive";

	/** The propagation. */
	String PROPAGATION = "propagation";

	/** The proportion. */
	String PROPORTION = "proportion";

	/** The pso. */
	String PSO = "pso";

	/** The put. */
	String PUT = "put";

	/** The quadratic attenuation. */
	String QUADRATIC_ATTENUATION = "quadratic_attenuation";

	/** The quadtree. */
	String QUADTREE = "quadtree";

	/** The radar. */
	String RADAR = "radar";

	/** The radius. */
	String RADIUS = "radius";

	/** The range. */
	String RANGE = "range";

	/** The reactive tabu. */
	String REACTIVE_TABU = "reactive_tabu";

	/** The readable. */
	String READABLE = "readable";

	/** The real speed. */
	String REAL_SPEED = "real_speed";

	/** The record. */
	String RECORD = "record";

	/** The reflex. */
	String REFLEX = "reflex";

	/** The refresh. */
	String REFRESH = "refresh";

	/** The register. */
	String REGISTER = "register";

	/** The release. */
	String RELEASE = "release";

	/** The remove. */
	String REMOVE = "remove";

	/** The repeat. */
	String REPEAT = "repeat";

	/** The restore. */
	String RESTORE = "restore";

	/** The resume sound. */
	String RESUME_SOUND = "resume_sound";

	/** The return. */
	String RETURN = "return";

	/** The returns. */
	String RETURNS = "returns";

	/** The reverse axis. */
	String REVERSE_AXIS = "reverse_axes";

	/** The rewrite. */
	String REWRITE = "rewrite";

	/** The rgb. */
	String RGB = "rgb";

	/** The right. */
	String RIGHT = "right";

	/** The ring. */
	String RING = "ring";

	/** The rng. */
	String RNG = "rng";

	/** The roll. */
	String ROLL = "roll";

	/** The rotate. */
	String ROTATE = "rotate";

	/** The rotation. */
	String ROTATION = "rotation";

	/** The rounded. */
	String ROUNDED = "rounded";

	/** Saltelli */
	String SALTELLI = "saltelli";

	/** The save. */
	String SAVE = "save";

	/** The scale. */
	String SCALE = "scale";

	/** The scatter. */
	String SCATTER = "scatter";

	/** The schedules. */
	String SCHEDULES = "schedules";

	/** The seed. */
	String SEED = "seed";

	/** The segments. */
	String SEGMENTS = "segments";

	/** The selectable. */
	String SELECTABLE = "selectable";

	/** The self. */
	String SELF = "self";

	/** The series. */
	String SERIES = "series";

	/** The set. */
	String SET = "set";

	/** The shape. */
	String SHAPE = "shape";

	/** The simulation. */
	String SIMULATION = "simulation";

	/** The simulations. */
	String SIMULATIONS = "simulations";

	/** The simultaneously. */
	String SIMULTANEOUSLY = "simultaneously";

	/** The size. */
	String SIZE = "size";

	/** The skill. */
	String SKILL = "skill";

	/** The skills. */
	String SKILLS = "skills";

	/** The smooth. */
	String SMOOTH = "smooth";

	/** The sobol exploration method */
	String SOBOL = "sobol";

	/** The solve. */
	String SOLVE = "solve";

	/** The source. */
	String SOURCE = "source";

	/** The species. */
	String SPECIES = "species";

	/** The population. */
	String SPECIES_LAYER = "species_layer";

	/** The speed. */
	String SPEED = "speed";

	/** The spline. */
	String SPLINE = "spline";

	/** The stack. */
	String STACK = "stack";

	/** The start sound. */
	String START_SOUND = "start_sound";

	/** The state. */
	String STATE = "state";

	/** The states. */
	String STATES = "states";

	/** The status. */
	String STATUS = "status";

	/** The step. */
	String STEP = "step";

	/** The Stochasticity Analysis */
	String STO = "stochanalyse";

	/** The stop sound. */
	String STOP_SOUND = "stop_sound";

	/** The string. */
	String STRING = "string";

	/** The style. */
	String STYLE = "style";

	/** The super. */
	String SUPER = "super";

	/** The switch. */
	String SWITCH = "switch";

	/** The synthetic. */
	String SYNTHETIC = "__synthetic__";

	/** The synthetic resources prefix. */
	String SYNTHETIC_RESOURCES_PREFIX = "__synthetic__";

	/** The table. */
	String TABLE = "table";

	/** The tabu. */
	String TABU = "tabu";

	/** The target. */
	String TARGET = "target";

	/** The test. */
	String TEST = "test";

	/** The text. */
	String TEXT = "text";

	/** The texture. */
	String TEXTURE = "texture";

	/** The thrad skill. */
	String THREAD_SKILL = "thread";

	/** The three d. */
	String THREE_D = "3d";

	/** The time final. */
	String TIME_FINAL = "tf";

	/** The time initial. */
	String TIME_INITIAL = "t0";

	/** The times. */
	String TIMES = "times";

	/** The title. */
	String TITLE = "title";

	/** The to. */
	String TO = "to";

	/** The toolbar. */
	String TOOLBAR = "toolbar";

	/** The topology. */
	String TOPOLOGY = "topology";

	/** The torus. */
	String TORUS = "torus";

	/** The trace. */
	String TRACE = "trace";

	/** The transparency. */
	String TRANSPARENCY = "transparency";

	/** The triangulation. */
	String TRIANGULATION = "triangulation";

	/** The true. */
	String TRUE = "true";

	/** The try. */
	String TRY = "try";

	/** The type. */
	String TYPE = "type";

	/** Uniform sampling */
	String UNIFORM = "uniform";

	/** The unit. */
	String UNIT = "unit";

	/** The unknown. */
	String UNKNOWN = "unknown";

	/** The until. */
	String UNTIL = "until";

	/** The update. */
	String UPDATE = "update";

	/** The updates. */
	String UPDATES = "updates";

	/** The user command. */
	String USER_COMMAND = "user_command";

	/** The user confirm. */
	String USER_CONFIRM = "user_confirm";

	/** The user controlled. */
	String USER_CONTROLLED = "user_controlled";

	/** The user first. */
	String USER_FIRST = "user_first";

	/** The user init. */
	String USER_INIT = "user_init";

	/** The user input. */
	String USER_INPUT = "user_input";

	/** The user input dialog. */
	String USER_INPUT_DIALOG = "user_input_dialog";

	/** The user last. */
	String USER_LAST = "user_last";

	/** The user only. */
	String USER_ONLY = "user_only";

	/** The user panel. */
	String USER_PANEL = "user_panel";

	/** The using. */
	String USING = "using";

	/** The value. */
	String VALUE = "value";

	/** The values. */
	String VALUES = "values";

	/** The var. */
	String VAR = "var";

	/** The variation. */
	String VARIATION = "variation";

	/** The vars. */
	String VARS = "vars";

	/** The version. */
	String VERSION = "version";

	/** The vertex. */
	String VERTEX = "vertex";

	/** The virtual. */
	String VIRTUAL = "virtual";

	/** The visible. */
	String VISIBLE = "visible";

	/** The warning. */
	String WARN = "warn";

	/** The warning test. */
	String WARNING = "warning";

	/** The weight. */
	String WEIGHT = "weight";

	/** The when. */
	String WHEN = "when";

	/** The while. */
	String WHILE = "while";

	/** The whisker. */
	String WHISKER = "whisker";

	/** The width. */
	String WIDTH = "width";

	/** The wireframe. */
	String WIREFRAME = "wireframe";

	/** The with. */
	String WITH = "with";

	/** The wizard. */
	String WIZARD = "wizard";

	/** The wizard page. */
	String WIZARD_PAGE = "wizard_page";

	/** The world agent name. */
	String WORLD_AGENT_NAME = "world";

	/** The writable. */
	String WRITABLE = "writable";

	/** The write. */
	String WRITE = "write";

	/** The x. */
	String X = "x";

	/** The x labels. */
	String X_LABELS = "x_serie_labels";

	/** The x serie. */
	String X_SERIE = "x_serie";

	/** The xml. */
	String XML = "xml";

	/** The xy. */
	String XY = "xy";

	/** The y. */
	String Y = "y";

	/** The y labels. */
	String Y_LABELS = "y_serie_labels";

	/** The z. */
	String Z = "z";

	/** The zero. */
	String ZERO = "internal_zero_order_equation";

	/** The methods. */
	String[] METHODS =
			{ GENETIC, ANNEALING, HILL_CLIMBING, TABU, REACTIVE_TABU, EXPLORATION, PSO, SOBOL, MORRIS, STO, BETAD };

	/** The bottom. */
	String BOTTOM = "bottom";

	/** The top. */
	String TOP = "top";

}

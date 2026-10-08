/*******************************************************************************************************
 *
 * GamlCoreConstants.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.constants;

import java.awt.Font;

import gama.annotations.constant;
import gama.annotations.doc;
import gama.annotations.test;
import gama.annotations.tests;
import gama.annotations.support.IConcept;
import gama.annotations.support.IConstantCategory;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;

/**
 * Interface defining all core constants available in the GAML modeling language. This interface serves as a central
 * repository for built-in constants that are automatically available in all GAML models without requiring imports or
 * declarations.
 *
 * <p>
 * Constants defined in this interface are accessible in GAML using the '#' prefix (e.g., {@code #pi}, {@code #e},
 * {@code #infinity}). They cover various domains including:
 * </p>
 *
 * <h3>Constant Categories:</h3>
 * <ul>
 * <li><b>Mathematical Constants:</b> Fundamental mathematical values such as pi, e, infinity, NaN, and conversion
 * factors between radians and degrees</li>
 * <li><b>Numeric Limits:</b> Minimum and maximum values for floating-point and integer types</li>
 * <li><b>Graph Algorithms:</b> Identifiers for shortest path algorithms (Dijkstra, AStar, FloydWarshall, etc.) and
 * K-shortest path algorithms</li>
 * <li><b>Geometric Constants:</b> Buffer end cap styles (round, flat, square) for geometric operations</li>
 * <li><b>Layout Constants:</b> Display layout modes (none, stack, split, horizontal, vertical)</li>
 * <li><b>Font Styles:</b> Font face styles (bold, italic, plain) using AWT Font constants</li>
 * <li><b>Display Units:</b> Dynamic graphical units including mouse location, camera properties, zoom level, display
 * dimensions, and pixel size</li>
 * <li><b>Text Anchors:</b> Predefined anchor points for text positioning (center, top_left, bottom_right, etc.)</li>
 * <li><b>Runtime State:</b> Current error message and current date (now)</li>
 * </ul>
 *
 * <h3>Annotation-Based Documentation:</h3>
 * <p>
 * Each constant is annotated with {@code @constant} and {@code @doc} annotations that provide:
 * </p>
 * <ul>
 * <li>The constant name as used in GAML (value attribute)</li>
 * <li>Alternative names (altNames attribute)</li>
 * <li>Categorization for documentation and IDE support</li>
 * <li>Associated concepts for semantic grouping</li>
 * <li>Comprehensive documentation describing purpose and usage</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>
 * // Mathematical constants
 * float circumference <- 2 * #pi * radius;
 * float angle_deg <- angle_rad * #to_deg;
 *
 * // Shortest path algorithms
 * path shortest <- compute_path(graph: road_network, algorithm: #Dijkstra);
 * path alternative <- compute_path(graph: road_network, algorithm: #AStar);
 *
 * // Font styles
 * draw "Title" font: font("Arial", 24, #bold + #italic);
 *
 * // Display properties
 * geometry click_location <- {#user_location.x, #user_location.y};
 * float pixel_size <- #pixels;
 *
 * // Text anchors
 * draw "Label" anchor: #top_left;
 * draw "Center" anchor: #center;
 * </pre>
 *
 * <h3>Dynamic Constants:</h3>
 * <p>
 * Some constants like {@code user_location}, {@code camera_location}, {@code zoom}, {@code pixels}, and
 * {@code display_width} are dynamic and return different values depending on the current execution context,
 * particularly within display and graphics contexts. They provide runtime information about the simulation state and
 * user interaction.
 * </p>
 *
 * <h3>Implementation Notes:</h3>
 * <p>
 * This interface is not meant to be implemented by user code. It serves solely as a declaration point for constants
 * that are automatically discovered and registered by the {@link CoreConstantsSupplier} during GAMA initialization.
 * </p>
 *
 * @author GAMA Development Team
 * @see GamlCoreUnits
 * @see CoreConstantsSupplier
 * @see gama.annotations.constant
 * @since GAMA 1.0
 */
public interface GamlCoreConstants {

	/** The current error. */
	@constant (
			value = "current_error",
			altNames = {},
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.ACTION },
			doc = { @doc ("The text of the last error thrown during the current execution") }) String current_error =
					"";

	/**
	 * Mathematical constants
	 *
	 */
	@constant (
			value = "pi",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT, IConcept.MATH },
			doc = @doc ("The PI constant"))
	@tests ({
			@test ("#pi = 3.141592653589793"),
			@test ("#pi * #to_deg = 180.0")
	})
	double pi = Math.PI;

	/** The e. */
	@constant (
			value = "e",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT, IConcept.MATH },
			doc = @doc ("The e constant"))
	@tests ({
			@test ("#e = 2.718281828459045")
	})
	double e = Math.E;

	/** The to deg. */
	@constant (
			value = "to_deg",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the value to convert radians into degrees"))
	@tests ({
			// #to_deg and #to_rad are the inverse of each other
			@test ("#to_deg = 180 / #pi"),
			@test ("#to_deg * #to_rad = 1.0")
	})
	double to_deg = 180d / Math.PI;

	/** The to rad. */
	@constant (
			value = "to_rad",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the value to convert degrees into radians"))
	@tests ({
			@test ("#to_rad = #pi / 180"),
			@test ("180 * #to_rad = #pi")
	})
	double to_rad = Math.PI / 180d;

	/** The nan. */
	@constant (
			value = "nan",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding a Not-a-Number (NaN) value of type float (Java Double.NaN)"))
	@tests ({
			@test ("#nan is float"),
			@test ("not is_number(#nan)"),
			// contrary to IEEE 754, #nan is equal to itself in GAML
			@test ("#nan = #nan"),
			@test ("not (#nan != #nan)"),
			@test ("#nan != 0.0"),
			@test ("#nan != #infinity"),
			// but it is not ordered with anything
			@test ("not (#nan < 1.0)"),
			@test ("not (#nan > 1.0)"),
			@test ("not (#nan <= #nan)"),
			@test ("not (#nan >= #nan)"),
			@test ("not (#nan < #infinity)"),
			@test ("not (#nan > -#infinity)"),
			// 'is_finite' only rules out the infinities
			@test ("is_finite(#nan)"),
			@test ("not is_number(#nan + 1)"),
			@test ("not is_number(#nan - #nan)"),
			@test ("not is_number(#nan * 0)"),
			@test ("not is_number(#nan / 2)"),
			@test ("not is_number(sum([1.0, #nan, 3.0]))"),
			@test ("string(#nan) = \"NaN\""),
			@test ("float(\"NaN\") = #nan"),
			@test ("int(#nan) = 0"),
			@test ("round(#nan) = 0"),
			@test ("list<float> l <- [3.0, #nan, 1.0]; l contains #nan"),
			@test ("list<float> l2 <- [3.0, #nan, 1.0]; l2 index_of #nan = 1"),
			@test ("list<float> l3 <- [3.0, #nan, 1.0]; l3 count (not is_number(each)) = 1"),
			@test ("list<float> l4 <- [3.0, #nan, 1.0]; (l4 where is_number(each)) = [3.0, 1.0]"),
			@test ("list<float> sorted <- [3.0, #nan, 1.0, #infinity, -#infinity] sort_by each; sorted[1] = 1.0"),
			@test ("list<float> sorted2 <- [3.0, #nan, 1.0, #infinity, -#infinity] sort_by each; sorted2[2] = 3.0"),
			@test ("list<float> sorted3 <- [3.0, #nan, 1.0, #infinity, -#infinity] sort_by each; not is_number(sorted3[4])"),
			@test ("map<string, float> m <- [\"a\"::#nan, \"b\"::#infinity]; m[\"a\"] = #nan")
	})
	double nan =
					Double.NaN;

	/** The infinity. */
	@constant (
			value = "infinity",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the positive infinity of type float (Java Double.POSITIVE_INFINITY)"))
	@tests ({
			@test ("#infinity is float"),
			@test ("#infinity = #infinity"),
			@test ("#infinity != -#infinity"),
			@test ("#infinity != #max_float"),
			@test ("-#infinity = -#infinity"),
			// infinity is a number, but not a finite one
			@test ("is_number(#infinity)"),
			@test ("is_number(-#infinity)"),
			@test ("not is_finite(#infinity)"),
			@test ("not is_finite(-#infinity)"),
			// it bounds every other number
			@test ("#infinity > #max_float"),
			@test ("#infinity > #max_int"),
			@test ("-#infinity < -#max_float"),
			@test ("-#infinity < #min_int"),
			@test ("abs(-#infinity) = #infinity"),
			@test ("float positive <- #infinity; positive + 1 = #infinity"),
			@test ("10.0 ^ 400 = #infinity"),
			@test ("1e308 * 10 = #infinity"),
			@test ("float positive2 <- #infinity; -positive2 = -#infinity"),
			// infinity absorbs finite operands
			@test ("not is_finite(#infinity + 1)"),
			@test ("#infinity + 1 > #max_float"),
			@test ("not is_finite(#infinity - #max_float)"),
			@test ("not is_finite(#infinity * 2)"),
			@test ("#infinity * -1 < -#max_float"),
			@test ("sum([1.0, #infinity]) > #max_float"),
			@test ("1 / #infinity = 0.0"),
			// undetermined forms are not numbers
			@test ("not is_number(#infinity - #infinity)"),
			@test ("not is_number(#infinity * 0)"),
			@test ("not is_number(#infinity / #infinity)"),
			@test ("string(#infinity) = \"Infinity\""),
			@test ("string(-#infinity) = \"-Infinity\""),
			@test ("float(\"Infinity\") = #infinity"),
			@test ("float(\"-Infinity\") = -#infinity"),
			// casting to int saturates
			@test ("int(#infinity) = #max_int"),
			@test ("int(-#infinity) = #min_int"),
			@test ("round(#infinity) = #max_int"),
			@test ("floor(#infinity) = #max_int"),
			@test ("ceil(-#infinity) = #min_int"),
			// a result that is not a number can be compared to #nan
			@test ("#infinity - #infinity = #nan"),
			@test ("list<float> sorted <- [3.0, #nan, 1.0, #infinity, -#infinity] sort_by each; sorted[0] = -#infinity"),
			@test ("list<float> sorted2 <- [3.0, #nan, 1.0, #infinity, -#infinity] sort_by each; sorted2[3] = #infinity"),
			@test ("map<string, float> m <- [\"a\"::#nan, \"b\"::#infinity]; m[\"b\"] = #infinity"),
			@test ("rgb boosted <- #red * #infinity; boosted.red = 255"),
			@test ("rgb boosted2 <- #red * #infinity; boosted2.green = 0")
	})
	double infinity =
					Double.POSITIVE_INFINITY;

	/** The min float. */
	@constant (
			value = "min_float",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the smallest positive nonzero value of type float (Java Double.MIN_VALUE)"))
	@tests ({
			@test ("#min_float = 4.9E-324"),
			@test ("#min_float is float"),
			// #min_float is the smallest positive float, not the most negative one
			@test ("#min_float > 0.0"),
			@test ("#min_float < 1.0"),
			// nothing smaller can be represented
			@test ("#min_float / 2 = 0.0"),
			// ... which also means the smallest float is 'equal' to zero while being greater than it
			@test ("#min_float = 0.0")
	})
	double min_float =
					Double.MIN_VALUE;

	/** The max float. */
	@constant (
			value = "max_float",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the largest positive finite value of type float (Java Double.MAX_VALUE)"))
	@tests ({
			@test ("#max_float = 1.7976931348623157E308"),
			@test ("#max_float is float"),
			@test ("is_number(#max_float)"),
			@test ("is_finite(#max_float)"),
			@test ("is_finite(-#max_float)"),
			@test ("#max_float > #max_int"),
			@test ("-#max_float < #min_int"),
			// and that adding 1 to the biggest float goes unnoticed
			@test ("#max_float + 1 = #max_float"),
			@test ("not is_finite(#max_float * 2)"),
			@test ("#max_float * 2 > #max_float"),
			@test ("not is_finite(-#max_float * 2)"),
			@test ("-#max_float * 2 < -#max_float"),
			@test ("#max_float * 2 = #infinity"),
			@test ("-#max_float * 2 = -#infinity"),
			@test ("int(#max_float) = #max_int"),
			@test ("int(-#max_float) = #min_int")
	})
	double max_float =
					Double.MAX_VALUE;

	/** The min int. */
	@constant (
			value = "min_int",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the minimum value an int can have (Java Integer.MIN_VALUE)"))
	@tests ({
			@test ("#min_int = -2147483647 - 1"),
			@test ("#min_int is int"),
			@test ("#min_int < 0"),
			@test ("is_number(#min_int)"),
			@test ("#min_int - 1 = #max_int"),
			// the opposite of #min_int does not fit in an int
			@test ("-#min_int = #min_int"),
			@test ("abs(#min_int) = #min_int")
	})
	int min_int =
					Integer.MIN_VALUE;

	/** The max int. */
	@constant (
			value = "max_int",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the maximum value an int can have (Java Integer.MAX_VALUE)"))
	@tests ({
			@test ("#max_int = 2147483647"),
			@test ("#max_int is int"),
			@test ("#max_int > 0"),
			@test ("is_number(#max_int)"),
			@test ("is_finite(#max_int)"),
			@test ("#max_int + 1 = #min_int"),
			@test ("#max_int * 2 = -2"),
			// moving to floats avoids the overflow
			@test ("float(#max_int) + 1 = 2147483648.0"),
			@test ("float(#max_int) + 1 > #max_int"),
			// too big for an int: saturated
			@test ("int(\"9999999999999\") = #max_int")
	})
	int max_int =
					Integer.MAX_VALUE;

	/**
	 * Shortest Path algorithm constants
	 */
	@constant (
			value = "FloydWarshall",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("FloydWarshall shortest path computation algorithm")) String FloydWarshall = "FloydWarshall";

	/** The Bellmann ford. */
	@constant (
			value = "BellmannFord",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("BellmannFord shortest path computation algorithm")) String BellmannFord = "BellmannFord";

	/** The Dijkstra. */
	@constant (
			value = "Dijkstra",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("Dijkstra shortest path computation algorithm")) String Dijkstra = "Dijkstra";

	/** The A star. */
	@constant (
			value = "AStar",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("AStar shortest path computation algorithm")) String AStar = "AStar";

	/** The NBA star. */
	@constant (
			value = "NBAStar",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("NBAStar shortest path computation algorithm")) String NBAStar = "NBAStar";

	/** The NBA star approx. */
	@constant (
			value = "NBAStarApprox",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("NBAStarApprox shortest path computation algorithm")) String NBAStarApprox = "NBAStarApprox";

	/** The Delta stepping. */
	@constant (
			value = "DeltaStepping",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("DeltaStepping shortest path computation algorithm")) String DeltaStepping = "DeltaStepping";

	/** The CH bidirectional dijkstra. */
	@constant (
			value = "CHBidirectionalDijkstra",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("CHBidirectionalDijkstra shortest path computation algorithm")) String CHBidirectionalDijkstra =
					"CHBidirectionalDijkstra";

	/** The Bidirectional dijkstra. */
	@constant (
			value = "BidirectionalDijkstra",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("BidirectionalDijkstra shortest path computation algorithm")) String BidirectionalDijkstra =
					"BidirectionalDijkstra";

	/** The Transit node routing. */
	@constant (
			value = "TransitNodeRouting",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("TransitNodeRouting shortest path computation algorithm")) String TransitNodeRouting =
					"TransitNodeRouting";

	/** The Yen. */
	@constant (
			value = "Yen",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("Yen K shortest paths computation algorithm"))
	@tests ({
			@test ("#Yen = \"Yen\"")
	})
	String Yen = "Yen";

	/** The Bhandari. */
	@constant (
			value = "Bhandari",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("Bhandari K shortest paths computation algorithm"))
	@tests ({
			@test ("#Bhandari = \"Bhandari\"")
	})
	String Bhandari = "Bhandari";

	/** The Eppstein. */
	@constant (
			value = "Eppstein",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("Eppstein K shortest paths computation algorithm"))
	@tests ({
			@test ("#Eppstein = \"Eppstein\"")
	})
	String Eppstein = "Eppstein";

	/** The Suurballe. */
	@constant (
			value = "Suurballe",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("Suurballe K shortest paths computation algorithm"))
	@tests ({
			@test ("#Suurballe = \"Suurballe\"")
	})
	String Suurballe = "Suurballe";

	/**
	 * Buffer constants
	 */
	@constant (
			value = "round",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GEOMETRY, IConcept.CONSTANT },
			doc = @doc ("This constant represents a round line buffer end cap style"))
	@tests ({
			// buffer end cap styles
			@test ("#round = 1")
	})
	int round = 1;

	/** The flat. */
	@constant (
			value = "flat",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GEOMETRY, IConcept.CONSTANT },
			doc = @doc ("This constant represents a flat line buffer end cap style"))
	@tests ({
			@test ("#flat = 2")
	})
	int flat = 2;

	/** The square. */
	@constant (
			value = "square",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GEOMETRY, IConcept.CONSTANT },
			doc = @doc ("This constant represents a square line buffer end cap style"))
	@tests ({
			@test ("#square = 3")
	})
	int square = 3;

	/**
	 * Layout constants
	 *
	 */
	@constant (
			value = "none",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("This constant represents the absence of a predefined layout"))
	@tests ({
			@test ("#none = 0")
	})
	int none = 0;

	/** The stack. */
	@constant (
			value = "stack",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("This constant represents a layout where all display views are stacked"))
	@tests ({
			@test ("#stack = 1")
	})
	int stack = 1;

	/** The split. */
	@constant (
			value = "split",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("This constant represents a layout where all display views are split in a grid-like structure"))
	@tests ({
			@test ("#split = 2")
	})
	int split =
					2;

	/** The horizontal. */
	@constant (
			value = "horizontal",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("This constant represents a layout where all display views are aligned horizontally"))
	@tests ({
			@test ("#horizontal = 3")
	})
	int horizontal =
					3;

	/** The vertical. */
	@constant (
			value = "vertical",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("This constant represents a layout where all display views are aligned vertically"))
	@tests ({
			@test ("#vertical = 4")
	})
	int vertical =
					4;

	/**
	 * Font style constants
	 */

	@constant (
			value = "bold",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GRAPHIC, IConcept.TEXT },
			doc = @doc ("This constant allows to build a font with a bold face. Can be combined with #italic"))
	@tests ({
			@test ("font f1 <- font(\"Arial\", 14, #bold); f1.name = \"Arial\""),
			@test ("font f12 <- font(\"Arial\", 14, #bold); f12.size = 14.0"),
			@test ("font f13 <- font(\"Arial\", 14, #bold); f13.style = #bold"),
			@test ("font f14 <- font(\"Arial\", 14, #bold); font f3 <- font(\"Arial\", 14, #bold); f14 = f3"),
			@test ("#bold = 1"),
			// styles are combined by adding them
			@test ("#bold + #italic = 3"),
			@test ("font(\"Arial\", 12, #bold + #italic).style = 3")
	})
	int bold =
					Font.BOLD; /* 1 */

	/** The italic. */
	@constant (
			value = "italic",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GRAPHIC, IConcept.TEXT },
			doc = @doc ("This constant allows to build a font with an italic face. Can be combined with #bold"))
	@tests ({
			@test ("font f2 <- font(\"Helvetica\", 12.0, #italic); f2.size = 12.0"),
			@test ("font f22 <- font(\"Helvetica\", 12.0, #italic); f22.style = #italic"),
			@test ("font f1 <- font(\"Arial\", 14, #bold); font f23 <- font(\"Helvetica\", 12.0, #italic); f1 != f23"),
			@test ("#italic = 2")
	})
	int italic =
					Font.ITALIC; /* 2 */

	/** The plain. */
	@constant (
			value = "plain",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GRAPHIC, IConcept.TEXT },
			doc = @doc ("This constant allows to build a font with a plain face"))
	@tests ({
			@test ("#plain = 0"),
			@test ("font(\"Arial\", 12, #plain).style = #plain")
	})
	int plain = Font.PLAIN;
	/**
	 * Special units
	 */

	@constant (
			value = "user_location",
			altNames = { "user_location_in_world" },
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.DISPLAY },
			doc = @doc ("This unit permanently holds the mouse's location in the world's coordinates. If it is outside a display window, its last position is used."))
	@tests ({
			@test ("#user_location is point")
	})
	IPoint user_location =
					GamaPointFactory.create();

	/** The user location in display. */
	@constant (
			value = "user_location_in_display",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.DISPLAY },
			doc = @doc ("This unit permanently holds the mouse's location in the display's coordinates. If it is outside a display window, its last position is used.")) IPoint user_location_in_display =
					GamaPointFactory.create();

	/** The camera location. */
	@constant (
			value = "camera_location",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.GRAPHIC_UNIT, IConcept.THREED },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns the current position of the camera as a point")) IPoint camera_location =
					GamaPointFactory.create();

	/** The camera target. */
	@constant (
			value = "camera_target",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.GRAPHIC_UNIT, IConcept.THREED },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns the current target of the camera as a point")) IPoint camera_target =
					GamaPointFactory.create();

	/** The camera orientation. */
	@constant (
			value = "camera_orientation",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.GRAPHIC_UNIT, IConcept.THREED },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns the current orientation of the camera as a point")) IPoint camera_orientation =
					GamaPointFactory.create();

	/** The camera up vector. */
	@constant (
			value = "camera_up",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.GRAPHIC_UNIT, IConcept.THREED },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns the current up axis of the camera as a point")) IPoint camera_up =
					GamaPointFactory.create();

	/** The camera right vector. */
	@constant (
			value = "camera_right",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.GRAPHIC_UNIT, IConcept.THREED },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns the current right axis of the camera as a point")) IPoint camera_right =
					GamaPointFactory.create();

	
	/**
	 * Anchor constants
	 */
	@constant (
			value = "center",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the center of the text to draw"))
	@tests ({
			@test ("#center = {0.5, 0.5}"),
			@test ("#center is point")
	})
	IPoint center =
					GamaPointFactory.create(0.5, 0.5);

	/** The top left. */
	@constant (
			value = "top_left",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the top left corner of the text to draw"))
	@tests ({
			@test ("#top_left = {0.0, 1.0}")
	})
	IPoint top_left =
					GamaPointFactory.create(0, 1);

	/** The left center. */
	@constant (
			value = "left_center",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the center of the left side of the text to draw"))

	@tests ({
			@test ("#left_center = {0.0, 0.5}")
	})
	IPoint left_center = GamaPointFactory.create(0, 0.5);

	/** The bottom left. */
	@constant (
			value = "bottom_left",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the bottom left corner of the text to draw"))
	@tests ({
			@test ("#bottom_left = {0.0, 0.0}")
	})
	IPoint bottom_left =
					GamaPointFactory.create(0, 0);

	/** The bottom center. */
	@constant (
			value = "bottom_center",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the center of the bottom side of the text to draw"))
	@tests ({
			@test ("#bottom_center = {0.5, 0.0}")
	})
	IPoint bottom_center =
					GamaPointFactory.create(0.5, 0);

	/** The bottom right. */
	@constant (
			value = "bottom_right",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the bottom right corner of the text to draw"))
	@tests ({
			@test ("#bottom_right = {1.0, 0.0}")
	})
	IPoint bottom_right =
					GamaPointFactory.create(1, 0);

	/** The right center. */
	@constant (
			value = "right_center",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the center of the right side of the text to draw"))
	@tests ({
			@test ("#right_center = {1.0, 0.5}")
	})
	IPoint right_center =
					GamaPointFactory.create(1, 0.5);

	/** The top right. */
	@constant (
			value = "top_right",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the top right corner of the text to draw"))
	@tests ({
			@test ("#top_right = {1.0, 1.0}")
	})
	IPoint top_right =
					GamaPointFactory.create(1, 1);

	/** The top center. */
	@constant (
			value = "top_center",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the center of the top side of the text to draw"))
	@tests ({
			@test ("#top_center = {0.5, 1.0}")
	})
	IPoint top_center =
					GamaPointFactory.create(0.5, 1);

	/** The zoom. */
	@constant (
			value = "zoom",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.DISPLAY },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns the current zoom level of the display as a positive float, where 1.0 represent the neutral zoom (100%)"))
	@tests ({
			// without a display, the display-related constants keep their neutral values
			@test ("#zoom = 1.0")
	})
	double zoom =
					1;

	/** The fullscreen. */
	@constant (
			value = "fullscreen",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.DISPLAY },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns whether the display is currently fullscreen or not"))
	@tests ({
			@test ("#fullscreen = false")
	})
	boolean fullscreen =
					false;

	/** The hidpi. */
	@constant (
			value = "hidpi",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.DISPLAY },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns whether the display is currently in HiDPI mode or not"))
	@tests ({
			@test ("#hidpi = false")
	})
	boolean hidpi =
					false;

	/** The px. */
	@constant (
			value = "pixels",
			altNames = { "px" },
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GRAPHIC, IConcept.GRAPHIC_UNIT },
			doc = @doc ("This unit, only available when running aspects or declaring displays,  returns a dynamic value instead of a fixed one. px (or pixels), returns the value of one pixel on the current view in terms of model units."))
	@tests ({
			@test ("#pixels = 1.0")
	})
	double pixels =
					1d, px = pixels;

	/** The display width. */
	@constant (
			value = "display_width",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GRAPHIC, IConcept.GRAPHIC_UNIT },
			doc = @doc ("This constant is only accessible in a graphical context: display, graphics...")) double display_width =
					1;

	/** The display height. */
	@constant (
			value = "display_height",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GRAPHIC, IConcept.GRAPHIC_UNIT },
			doc = @doc ("This constant is only accessible in a graphical context: display, graphics...")) double display_height =
					1;

	/** The now. */
	@constant (
			value = "now",
			category = { IConstantCategory.TIME },
			concept = { IConcept.DATE, IConcept.TIME },
			doc = @doc ("This value represents the current date"))
	@tests ({
			@test ("#now is date"),
			@test ("#now > #epoch")
	})
	double now = 1;

}
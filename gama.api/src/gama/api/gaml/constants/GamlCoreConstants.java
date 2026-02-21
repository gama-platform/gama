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
			doc = @doc ("The PI constant")) double pi = Math.PI;

	/** The e. */
	@constant (
			value = "e",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT, IConcept.MATH },
			doc = @doc ("The e constant")) double e = Math.E;

	/** The to deg. */
	@constant (
			value = "to_deg",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the value to convert radians into degrees")) double to_deg = 180d / Math.PI;

	/** The to rad. */
	@constant (
			value = "to_rad",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the value to convert degrees into radians")) double to_rad = Math.PI / 180d;

	/** The nan. */
	@constant (
			value = "nan",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding a Not-a-Number (NaN) value of type float (Java Double.POSITIVE_INFINITY)")) double nan =
					Double.NaN;

	/** The infinity. */
	@constant (
			value = "infinity",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the positive infinity of type float (Java Double.POSITIVE_INFINITY)")) double infinity =
					Double.POSITIVE_INFINITY;

	/** The min float. */
	@constant (
			value = "min_float",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the smallest positive nonzero value of type float (Java Double.MIN_VALUE)")) double min_float =
					Double.MIN_VALUE;

	/** The max float. */
	@constant (
			value = "max_float",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the largest positive finite value of type float (Java Double.MAX_VALUE)")) double max_float =
					Double.MAX_VALUE;

	/** The min int. */
	@constant (
			value = "min_int",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the minimum value an int can have (Java Integer.MIN_VALUE)")) int min_int =
					Integer.MIN_VALUE;

	/** The max int. */
	@constant (
			value = "max_int",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT },
			doc = @doc ("A constant holding the maximum value an int can have (Java Integer.MAX_VALUE)")) int max_int =
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
			doc = @doc ("Yen K shortest paths computation algorithm")) String Yen = "Yen";

	/** The Bhandari. */
	@constant (
			value = "Bhandari",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("Bhandari K shortest paths computation algorithm")) String Bhandari = "Bhandari";

	/** The Eppstein. */
	@constant (
			value = "Eppstein",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("Eppstein K shortest paths computation algorithm")) String Eppstein = "Eppstein";

	/** The Suurballe. */
	@constant (
			value = "Suurballe",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.GRAPH, IConcept.CONSTANT },
			doc = @doc ("Suurballe K shortest paths computation algorithm")) String Suurballe = "Suurballe";

	/**
	 * Buffer constants
	 */
	@constant (
			value = "round",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GEOMETRY, IConcept.CONSTANT },
			doc = @doc ("This constant represents a round line buffer end cap style")) int round = 1;

	/** The flat. */
	@constant (
			value = "flat",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GEOMETRY, IConcept.CONSTANT },
			doc = @doc ("This constant represents a flat line buffer end cap style")) int flat = 2;

	/** The square. */
	@constant (
			value = "square",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GEOMETRY, IConcept.CONSTANT },
			doc = @doc ("This constant represents a square line buffer end cap style")) int square = 3;

	/**
	 * Layout constants
	 *
	 */
	@constant (
			value = "none",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("This constant represents the absence of a predefined layout")) int none = 0;

	/** The stack. */
	@constant (
			value = "stack",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("This constant represents a layout where all display views are stacked")) int stack = 1;

	/** The split. */
	@constant (
			value = "split",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("This constant represents a layout where all display views are split in a grid-like structure")) int split =
					2;

	/** The horizontal. */
	@constant (
			value = "horizontal",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("This constant represents a layout where all display views are aligned horizontally")) int horizontal =
					3;

	/** The vertical. */
	@constant (
			value = "vertical",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("This constant represents a layout where all display views are aligned vertically")) int vertical =
					4;

	/**
	 * Font style constants
	 */

	@constant (
			value = "bold",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GRAPHIC, IConcept.TEXT },
			doc = @doc ("This constant allows to build a font with a bold face. Can be combined with #italic")) int bold =
					Font.BOLD; /* 1 */

	/** The italic. */
	@constant (
			value = "italic",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GRAPHIC, IConcept.TEXT },
			doc = @doc ("This constant allows to build a font with an italic face. Can be combined with #bold")) int italic =
					Font.ITALIC; /* 2 */

	/** The plain. */
	@constant (
			value = "plain",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GRAPHIC, IConcept.TEXT },
			doc = @doc ("This constant allows to build a font with a plain face")) int plain = Font.PLAIN;
	/**
	 * Special units
	 */

	@constant (
			value = "user_location",
			altNames = { "user_location_in_world" },
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.DISPLAY },
			doc = @doc ("This unit permanently holds the mouse's location in the world's coordinates. If it is outside a display window, its last position is used.")) IPoint user_location =
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

	/**
	 * Anchor constants
	 */
	@constant (
			value = "center",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the center of the text to draw")) IPoint center =
					GamaPointFactory.create(0.5, 0.5);

	/** The top left. */
	@constant (
			value = "top_left",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the top left corner of the text to draw")) IPoint top_left =
					GamaPointFactory.create(0, 1);

	/** The left center. */
	@constant (
			value = "left_center",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the center of the left side of the text to draw"))

	IPoint left_center = GamaPointFactory.create(0, 0.5);

	/** The bottom left. */
	@constant (
			value = "bottom_left",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the bottom left corner of the text to draw")) IPoint bottom_left =
					GamaPointFactory.create(0, 0);

	/** The bottom center. */
	@constant (
			value = "bottom_center",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the center of the bottom side of the text to draw")) IPoint bottom_center =
					GamaPointFactory.create(0.5, 0);

	/** The bottom right. */
	@constant (
			value = "bottom_right",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the bottom right corner of the text to draw")) IPoint bottom_right =
					GamaPointFactory.create(1, 0);

	/** The right center. */
	@constant (
			value = "right_center",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the center of the right side of the text to draw")) IPoint right_center =
					GamaPointFactory.create(1, 0.5);

	/** The top right. */
	@constant (
			value = "top_right",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the top right corner of the text to draw")) IPoint top_right =
					GamaPointFactory.create(1, 1);

	/** The top center. */
	@constant (
			value = "top_center",
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.DISPLAY, IConcept.OUTPUT },
			doc = @doc ("Represents an anchor situated at the center of the top side of the text to draw")) IPoint top_center =
					GamaPointFactory.create(0.5, 1);

	/** The zoom. */
	@constant (
			value = "zoom",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.DISPLAY },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns the current zoom level of the display as a positive float, where 1.0 represent the neutral zoom (100%)")) double zoom =
					1;

	/** The fullscreen. */
	@constant (
			value = "fullscreen",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.DISPLAY },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns whether the display is currently fullscreen or not")) boolean fullscreen =
					false;

	/** The hidpi. */
	@constant (
			value = "hidpi",
			category = IConstantCategory.GRAPHIC,
			concept = { IConcept.GRAPHIC, IConcept.DISPLAY },
			doc = @doc ("This unit, only available when running aspects or declaring displays, returns whether the display is currently in HiDPI mode or not")) boolean hidpi =
					false;

	/** The px. */
	@constant (
			value = "pixels",
			altNames = { "px" },
			category = { IConstantCategory.GRAPHIC },
			concept = { IConcept.GRAPHIC, IConcept.GRAPHIC_UNIT },
			doc = @doc ("This unit, only available when running aspects or declaring displays,  returns a dynamic value instead of a fixed one. px (or pixels), returns the value of one pixel on the current view in terms of model units.")) double pixels =
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
			doc = @doc ("This value represents the current date")) double now = 1;

}
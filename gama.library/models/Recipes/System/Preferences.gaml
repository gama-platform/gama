/**
* Name: Preferences
* Author: Gama Development Team
* Description: Provides a comprehensive reference for all GAMA preferences accessible through the 'gama' built-in
*   agent. GAMA exposes its internal configuration as readable (and sometimes writable) attributes of the 'gama'
*   pseudo-agent. These preferences control aspects such as console output limits, chart quality, error handling,
*   display rendering, experiment behavior, and many more. This model prints the current value of every available
*   preference to the console, serving as a live documentation of the platform configuration. The super-advanced
*   container-casting, point-tolerance, and object-pooling preferences are hidden from the Preferences dialog and
*   should only be changed directly from GAML by users who understand their global side effects. Assigning them from
*   GAML updates and persists application-wide GAMA preferences; these are not model-local settings.
* Tags: system, preferences, configuration, gama, platform, settings
*/
// GAMA 0.0.0-SNAPSHOT __PREFS__ saved on 2026-11-11T08:42:20.276177

model preferences

experiment 'Display __PREFS__' type: gui {
init {
	//Append the name of simulations to their outputs
	write sample(gama.pref_append_simulation_name);

	//Custom default chart background color
	write sample(gama.pref_chart_background_color);

	//Default color palette for chart series
	write sample(gama.pref_chart_color_palette);

	//Continue to draw charts when in Modeling perspective
	write sample(gama.pref_chart_continue_drawing);

	//Default chart axes and gridlines color
	write sample(gama.pref_chart_grid_color);

	//Default chart label font face
	write sample(gama.pref_chart_label_font);

	//Default chart label font size
	write sample(gama.pref_chart_label_font_size);

	//Default thickness of series lines
	write sample(gama.pref_chart_line_thickness);

	//Chart background follows default display background
	write sample(gama.pref_chart_match_display_bg);

	//Pivot color when using 'Based on pivot color' scheme
	write sample(gama.pref_chart_pivot_color);

	//Chart raster scale (1 = native resolution, 2 = supersampling; higher uses more resources)
	write sample(gama.pref_chart_quality);

	//Default style for chart series
	write sample(gama.pref_chart_series_style);

	//Display grid lines on charts
	write sample(gama.pref_chart_show_gridlines);

	//Display markers on chart series
	write sample(gama.pref_chart_show_markers);

	//Default chart text color
	write sample(gama.pref_chart_text_color);

	//Default chart title font face
	write sample(gama.pref_chart_title_font);

	//Default chart title font size
	write sample(gama.pref_chart_title_font_size);

	//Max. number of characters to keep when paused (-1 = unlimited)
	write sample(gama.pref_console_buffer);

	//Max. number of characters to display (-1 = unlimited)
	write sample(gama.pref_console_size);

	//Wrap long lines (can slow down output)
	write sample(gama.pref_console_wrap);

	//Default separator for fields
	write sample(gama.pref_csv_separator);

	//Default separator for strings
	write sample(gama.pref_csv_string_qualifier);

	//Custom date pattern (https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns)
	write sample(gama.pref_date_custom_formatter);

	//Default date pattern for writing dates (i.e. string(date1))
	write sample(gama.pref_date_default_formatter);

	//Default starting date of models
	write sample(gama.pref_date_starting_date);

	//Default time step of models
	write sample(gama.pref_date_time_step);

	//Apply antialiasing
	write sample(gama.pref_display_antialias);

	//Default background color ('background' facet of 'display')
	write sample(gama.pref_display_background_color);

	//Default camera to use when none is specified
	write sample(gama.pref_display_camera);

	//Limit the number of frames per second if you experience stuttering in the rendering
	write sample(gama.pref_display_cap_fps);

	//Continue to draw displays when in Modeling perspective
	write sample(gama.pref_display_continue_drawing);

	//Default rendering method
	write sample(gama.pref_display_default2);

	//Default color of agents
	write sample(gama.pref_display_default_color);

	//Default font to use in 'draw'
	write sample(gama.pref_display_default_font);

	//Defaut shape of agents
	write sample(gama.pref_display_default_shape);

	//Default size of agents
	write sample(gama.pref_display_default_size);

	//Enable fast snapshots (uncomplete when the display is obscured by others but much faster)
	write sample(gama.pref_display_fast_snapshot);

	//Default highlight color
	write sample(gama.pref_display_highlight_color);

	//Set the sensitivity of the keyboard movements  (0 for slow, 1 for fast)
	write sample(gama.pref_display_keyboard_factor);

	//Set the default intensity of the lights (from 0, dark, to 255, light)
	write sample(gama.pref_display_light_intensity);

	//Default line width (facet 'width' of 'draw'). Note that this attribute is not supported by all OpenGL implementations
	write sample(gama.pref_display_line_width);

	//Max. number of frames per second
	write sample(gama.pref_display_max_fps);

	//Keep chart values in memory (to save them as CSV) or not (to lower memory usage)
	write sample(gama.pref_display_memorize_charts);

	//Max. number of cells of a field displayed in OpenGL (larger fields are downsampled; 0 = no limit)
	write sample(gama.pref_display_mesh_max_cells);

	//Set the sensitivity of the mouse/trackpad movements  (0 for slow, 1 fast)
	write sample(gama.pref_display_mouse_factor);

	//Disable acceleration for Java2D (necessary on some configurations)
	write sample(gama.pref_display_no_java2d_acceleration);

	//Use Numeric Keypad (2,4,6,8) for camera interaction
	write sample(gama.pref_display_numkeyscam);

	//Log display preparation diagnostics
	write sample(gama.pref_display_preparation_diagnostics);

	//Show errors thrown in displays and outputs
	write sample(gama.pref_display_show_errors);

	//Draw rotation axes
	write sample(gama.pref_display_show_rotation);

	//Number of slices of circular geometries in OpenGL (the higher the more resources consuming)
	write sample(gama.pref_display_slice_number);

	//Use GAMA image cache when building textures in OpenGL (potentially faster when running several simulations, but uses more memory)
	write sample(gama.pref_display_use_cache);

	//Default layout of display views
	write sample(gama.pref_display_view_layout);

	//Only display visible agents in OpenGL (faster, may create visual oddities)
	write sample(gama.pref_display_visible_agents);

	//Draw only the 'external' faces of objects in OpenGL
	write sample(gama.pref_display_visible_faces);

	//Set the zoom factor (0 for slow, 1 fast)
	write sample(gama.pref_display_zoom_factor);

	//Enable GAMA server mode
	write sample(gama.pref_enable_server);

	//Show execution errors
	write sample(gama.pref_errors_display);

	//Show errors in editors
	write sample(gama.pref_errors_in_editor);

	//Number of errors to display
	write sample(gama.pref_errors_number);

	//Stop simulation at first error
	write sample(gama.pref_errors_stop);

	//Treat warnings as errors
	write sample(gama.pref_errors_warnings_errors);

	//Ask to close the previous experiment when launching a new one
	write sample(gama.pref_experiment_ask_closing);

	//Automatically expand the parameters categories
	write sample(gama.pref_experiment_expand_params);

	//Scale of the step duration slider
	write sample(gama.pref_experiment_type_slider);

	//Browser to open to display web links (in documentation or text statements)
	write sample(gama.pref_external_browser);

	//Only display unsuccessful tests
	write sample(gama.pref_failed_tests);

	//Let GAMA find which CRS to use to project GIS data
	write sample(gama.pref_gis_auto_crs);

	//...or use the following EPSG code (the one that will also be used if no projection information is found)
	write sample(gama.pref_gis_default_crs);

	//...or use the following CRS (EPSG code)
	write sample(gama.pref_gis_initial_crs);

	//... or use this following CRS (EPSG code)
	write sample(gama.pref_gis_output_crs);

	//When no .prj file or CRS is supplied, consider GIS data to be already projected in this CRS
	write sample(gama.pref_gis_same_crs);

	//When no CRS is provided, save the GIS data with the current CRS
	write sample(gama.pref_gis_save_crs);

	//Connection timeout (in ms)
	write sample(gama.pref_http_connect_timeout);

	//Empty the local cache of files downloaded from the web
	write sample(gama.pref_http_empty_cache);

	//Read timeout (in ms)
	write sample(gama.pref_http_read_timeout);

	//Number of times to retry if connection cannot be established
	write sample(gama.pref_http_retry_number);

	//Write and parse #infinity as
	write sample(gama.pref_json_infinity_as_string);

	//In case of an int overflow, parse the item as a
	write sample(gama.pref_json_int_overflow_as_double);

	//Write and parse #nan as
	write sample(gama.pref_json_nan_as_string);

	//Keep the console contents between experiments
	write sample(gama.pref_keep_console);

	//When running out of memory in an experiment, GAMA should
	write sample(gama.pref_memory_action);

	//Interval (in seconds) at which memory should be monitored
	write sample(gama.pref_memory_frequency);

	//Emit a warning when the percentage of available memory is under
	write sample(gama.pref_memory_threshold);

	//Display monitors in the parameters view
	write sample(gama.pref_monitors_in_parameters);

	//Log OpenGL rendering diagnostics
	write sample(gama.pref_opengl_diagnostics);

	//In OpenGL, draw world layers in map order instead of using 3D depth
	write sample(gama.pref_opengl_map_order);

	//Increment factor (from 0, none, to 1, max)
	write sample(gama.pref_opengl_z_factor);

	//In OpenGL, add a small increment to the z ordinate of objects and layers to fight visual artefacts
	write sample(gama.pref_opengl_z_fighting);

	//Create spatial indexes lazily (experimental)
	write sample(gama.pref_optimize_quadtree);

	//Max. number of threads to use (available processors: 16)
	write sample(gama.pref_parallel_threads);

	//Default seed value (0 is undefined)
	write sample(gama.pref_rng_default_seed);

	//Define a default seed
	write sample(gama.pref_rng_define_seed);

	//Include random number generation parameters in the parameters view
	write sample(gama.pref_rng_in_parameters);

	//Default random number generator
	write sample(gama.pref_rng_name);

	//Default buffering strategy for the save statement
	write sample(gama.pref_save_buffering_strategy);

	//Maximum number of simultaneous commands an experiment can accept through GAMA server.
	write sample(gama.pref_server_command_queue_size);

	//Send console outputs to clients
	write sample(gama.pref_server_console);

	//Log all GAMA server exchanges
	write sample(gama.pref_server_log);

	//Server exchange log directory (restart required)
	write sample(gama.pref_server_log_directory);

	//Sets the TCP_NODELAY option to true for GAMA server
	write sample(gama.pref_server_no_delay);

	//Interval between two pings (-1 to disable)
	write sample(gama.pref_server_ping);

	//Port to which GAMA server is listening
	write sample(gama.pref_server_port);

	//Mapping and caching of shapefiles in memory (optimises access to shapefile data in exchange for increased memory usage). Disable this property if you are dealing with shapefiles that change frequently
	write sample(gama.pref_shapefiles_in_memory);

	//Pivot color of simulations
	write sample(gama.pref_simulation_color);

	//Default color scheme for simulations in UI
	write sample(gama.pref_simulation_colors);

	//Test experiment timeout (seconds)
	write sample(gama.pref_test_timeout);

	//In OpenGL, orient the textures according to the geometry on which they are displayed (may create visual oddities)
	write sample(gama.pref_texture_orientation);

	//Default buffering strategy for the write statement
	write sample(gama.pref_write_buffering_strategy);

}
}


experiment 'Set __PREFS__' type: gui {
init {
	//Append the name of simulations to their outputs
	gama.pref_append_simulation_name <- true;

	//Custom default chart background color
	gama.pref_chart_background_color <- #black;

	//Default color palette for chart series
	gama.pref_chart_color_palette <- 'Mineral';

	//Continue to draw charts when in Modeling perspective
	gama.pref_chart_continue_drawing <- true;

	//Default chart axes and gridlines color
	gama.pref_chart_grid_color <- rgb (210, 215, 220, 255);

	//Default chart label font face
	gama.pref_chart_label_font <- 'SansSerif';

	//Default chart label font size
	gama.pref_chart_label_font_size <- 11;

	//Default thickness of series lines
	gama.pref_chart_line_thickness <- 0.5;

	//Chart background follows default display background
	gama.pref_chart_match_display_bg <- true;

	//Pivot color when using 'Based on pivot color' scheme
	gama.pref_chart_pivot_color <- rgb (31, 120, 180, 255);

	//Chart raster scale (1 = native resolution, 2 = supersampling; higher uses more resources)
	gama.pref_chart_quality <- 1.012;

	//Default style for chart series
	gama.pref_chart_series_style <- 'line';

	//Display grid lines on charts
	gama.pref_chart_show_gridlines <- false;

	//Display markers on chart series
	gama.pref_chart_show_markers <- true;

	//Default chart text color
	gama.pref_chart_text_color <- rgb (44, 62, 80, 255);

	//Default chart title font face
	gama.pref_chart_title_font <- 'SansSerif';

	//Default chart title font size
	gama.pref_chart_title_font_size <- 14;

	//Max. number of characters to keep when paused (-1 = unlimited)
	gama.pref_console_buffer <- 20000;

	//Max. number of characters to display (-1 = unlimited)
	gama.pref_console_size <- 20000;

	//Wrap long lines (can slow down output)
	gama.pref_console_wrap <- false;

	//Default separator for fields
	gama.pref_csv_separator <- ',';

	//Default separator for strings
	gama.pref_csv_string_qualifier <- '"';

	//Custom date pattern (https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns)
	gama.pref_date_custom_formatter <- 'yyyy-MM-dd HH:mm:ss';

	//Default date pattern for writing dates (i.e. string(date1))
	gama.pref_date_default_formatter <- 'CUSTOM';

	//Default starting date of models
	gama.pref_date_starting_date <- date ('1970-01-01 07:00:00');

	//Default time step of models
	gama.pref_date_time_step <- 1.0;

	//Apply antialiasing
	gama.pref_display_antialias <- false;

	//Default background color ('background' facet of 'display')
	gama.pref_display_background_color <- #white;

	//Default camera to use when none is specified
	gama.pref_display_camera <- 'From above';

	//Limit the number of frames per second if you experience stuttering in the rendering
	gama.pref_display_cap_fps <- false;

	//Continue to draw displays when in Modeling perspective
	gama.pref_display_continue_drawing <- false;

	//Default rendering method
	gama.pref_display_default2 <- true;

	//Default color of agents
	gama.pref_display_default_color <- #yellow;

	//Default font to use in 'draw'
	gama.pref_display_default_font <- font('Helvetica',12,#plain);

	//Defaut shape of agents
	gama.pref_display_default_shape <- 'shape';

	//Default size of agents
	gama.pref_display_default_size <- 1.0;

	//Enable fast snapshots (uncomplete when the display is obscured by others but much faster)
	gama.pref_display_fast_snapshot <- false;

	//Default highlight color
	gama.pref_display_highlight_color <- #red;

	//Set the sensitivity of the keyboard movements  (0 for slow, 1 for fast)
	gama.pref_display_keyboard_factor <- 0.5;

	//Set the default intensity of the lights (from 0, dark, to 255, light)
	gama.pref_display_light_intensity <- 208;

	//Default line width (facet 'width' of 'draw'). Note that this attribute is not supported by all OpenGL implementations
	gama.pref_display_line_width <- 1.0;

	//Max. number of frames per second
	gama.pref_display_max_fps <- 60;

	//Keep chart values in memory (to save them as CSV) or not (to lower memory usage)
	gama.pref_display_memorize_charts <- true;

	//Max. number of cells of a field displayed in OpenGL (larger fields are downsampled; 0 = no limit)
	gama.pref_display_mesh_max_cells <- 1000000;

	//Set the sensitivity of the mouse/trackpad movements  (0 for slow, 1 fast)
	gama.pref_display_mouse_factor <- 0.5;

	//Disable acceleration for Java2D (necessary on some configurations)
	gama.pref_display_no_java2d_acceleration <- false;

	//Use Numeric Keypad (2,4,6,8) for camera interaction
	gama.pref_display_numkeyscam <- true;

	//Log display preparation diagnostics
	gama.pref_display_preparation_diagnostics <- false;

	//Show errors thrown in displays and outputs
	gama.pref_display_show_errors <- false;

	//Draw rotation axes
	gama.pref_display_show_rotation <- true;

	//Number of slices of circular geometries in OpenGL (the higher the more resources consuming)
	gama.pref_display_slice_number <- 16;

	//Use GAMA image cache when building textures in OpenGL (potentially faster when running several simulations, but uses more memory)
	gama.pref_display_use_cache <- true;

	//Default layout of display views
	gama.pref_display_view_layout <- 'Split';

	//Only display visible agents in OpenGL (faster, may create visual oddities)
	gama.pref_display_visible_agents <- false;

	//Draw only the 'external' faces of objects in OpenGL
	gama.pref_display_visible_faces <- false;

	//Set the zoom factor (0 for slow, 1 fast)
	gama.pref_display_zoom_factor <- 0.5;

	//Enable GAMA server mode
	gama.pref_enable_server <- true;

	//Show execution errors
	gama.pref_errors_display <- true;

	//Show errors in editors
	gama.pref_errors_in_editor <- true;

	//Number of errors to display
	gama.pref_errors_number <- 10;

	//Stop simulation at first error
	gama.pref_errors_stop <- true;

	//Treat warnings as errors
	gama.pref_errors_warnings_errors <- false;

	//Ask to close the previous experiment when launching a new one
	gama.pref_experiment_ask_closing <- true;

	//Automatically expand the parameters categories
	gama.pref_experiment_expand_params <- false;

	//Scale of the step duration slider
	gama.pref_experiment_type_slider <- true;

	//Browser to open to display web links (in documentation or text statements)
	gama.pref_external_browser <- true;

	//Only display unsuccessful tests
	gama.pref_failed_tests <- false;

	//Let GAMA find which CRS to use to project GIS data
	gama.pref_gis_auto_crs <- true;

	//...or use the following EPSG code (the one that will also be used if no projection information is found)
	gama.pref_gis_default_crs <- 32648;

	//...or use the following CRS (EPSG code)
	gama.pref_gis_initial_crs <- 4326;

	//... or use this following CRS (EPSG code)
	gama.pref_gis_output_crs <- 4326;

	//When no .prj file or CRS is supplied, consider GIS data to be already projected in this CRS
	gama.pref_gis_same_crs <- true;

	//When no CRS is provided, save the GIS data with the current CRS
	gama.pref_gis_save_crs <- true;

	//Connection timeout (in ms)
	gama.pref_http_connect_timeout <- 20000;

	//Empty the local cache of files downloaded from the web
	gama.pref_http_empty_cache <- true;

	//Read timeout (in ms)
	gama.pref_http_read_timeout <- 20000;

	//Number of times to retry if connection cannot be established
	gama.pref_http_retry_number <- 3;

	//Write and parse #infinity as
	gama.pref_json_infinity_as_string <- true;

	//In case of an int overflow, parse the item as a
	gama.pref_json_int_overflow_as_double <- true;

	//Write and parse #nan as
	gama.pref_json_nan_as_string <- true;

	//Keep the console contents between experiments
	gama.pref_keep_console <- true;

	//When running out of memory in an experiment, GAMA should
	gama.pref_memory_action <- true;

	//Interval (in seconds) at which memory should be monitored
	gama.pref_memory_frequency <- 2;

	//Emit a warning when the percentage of available memory is under
	gama.pref_memory_threshold <- 20;

	//Display monitors in the parameters view
	gama.pref_monitors_in_parameters <- true;

	//Log OpenGL rendering diagnostics
	gama.pref_opengl_diagnostics <- false;

	//In OpenGL, draw world layers in map order instead of using 3D depth
	gama.pref_opengl_map_order <- false;

	//Increment factor (from 0, none, to 1, max)
	gama.pref_opengl_z_factor <- 0.05;

	//In OpenGL, add a small increment to the z ordinate of objects and layers to fight visual artefacts
	gama.pref_opengl_z_fighting <- true;

	//Create spatial indexes lazily (experimental)
	gama.pref_optimize_quadtree <- true;

	//Max. number of threads to use (available processors: 16)
	gama.pref_parallel_threads <- 16;

	//Default seed value (0 is undefined)
	gama.pref_rng_default_seed <- 1.0;

	//Define a default seed
	gama.pref_rng_define_seed <- false;

	//Include random number generation parameters in the parameters view
	gama.pref_rng_in_parameters <- false;

	//Default random number generator
	gama.pref_rng_name <- 'mersenne';

	//Default buffering strategy for the save statement
	gama.pref_save_buffering_strategy <- 'no_buffering';

	//Maximum number of simultaneous commands an experiment can accept through GAMA server.
	gama.pref_server_command_queue_size <- 50;

	//Send console outputs to clients
	gama.pref_server_console <- true;

	//Log all GAMA server exchanges
	gama.pref_server_log <- false;

	//Server exchange log directory (restart required)
	gama.pref_server_log_directory <- '/Users/drogoul/.gama/server-logs';

	//Sets the TCP_NODELAY option to true for GAMA server
	gama.pref_server_no_delay <- false;

	//Interval between two pings (-1 to disable)
	gama.pref_server_ping <- 10000;

	//Port to which GAMA server is listening
	gama.pref_server_port <- 1001;

	//Mapping and caching of shapefiles in memory (optimises access to shapefile data in exchange for increased memory usage). Disable this property if you are dealing with shapefiles that change frequently
	gama.pref_shapefiles_in_memory <- false;

	//Pivot color of simulations
	gama.pref_simulation_color <- rgb (64, 224, 208, 0);

	//Default color scheme for simulations in UI
	gama.pref_simulation_colors <- 'Mineral';

	//Test experiment timeout (seconds)
	gama.pref_test_timeout <- 10;

	//In OpenGL, orient the textures according to the geometry on which they are displayed (may create visual oddities)
	gama.pref_texture_orientation <- true;

	//Default buffering strategy for the write statement
	gama.pref_write_buffering_strategy <- 'no_buffering';

}
}


/**
* Name: Benchmark - Displays and Charts
* Description: Measures synchronized output with a 2D view, an OpenGL view and a multi-series chart.
*   Use the GUI, keep all three displays visible, and do not interact during a run.
*   A single run progressively grows particles and chart series over six long plateaus.
*   Operations levels instead repeat geometry drawing; chart history is retained across plateaus.
*   See ../README.txt for display-size controls and the GAMA 2025 comparison protocol.
* Tags: benchmark, performance, display, 2d, 3d, opengl, chart
*/
model BenchmarkDisplaysAndCharts

global {
	geometry shape <- square(1000);
	int base_population <- 2000;
	int base_series <- 16;
	int base_draw_copies <- 1;
	int first_level <- 0;
	int last_level <- 5;
	int load_level;
	string load_axis <- "size";
	int population_size;
	int series_count;
	int draw_copies;
	int warmup_cycles <- 300;
	int measured_cycles <- 5000;
	int sample_cycles <- 250;
	int stage_started_cycle <- 0;
	string output_file <- "../results/displays.csv";
	string run_id;
	list<string> series_names;
	list<float> series_values;
	float last_boundary_at;
	float elapsed_ms <- 0.0;
	float window_ms <- 0.0;
	float max_cycle_ms <- 0.0;
	float peak_used_mb <- 0.0;
	int completed_cycles <- 0;
	int window_count <- 0;
	float work_per_cycle;
	float checksum;
	bool finished <- false;

	init {
		if not (["size", "operations", "both"] contains load_axis)
			or base_population < 1 or base_series < 1 or base_draw_copies < 1 {
			error "Invalid load request: axis size/operations/both and positive bases are required.";
		}
		if warmup_cycles < 0 or measured_cycles < 1 or sample_cycles < 1 {
			error "Warm-up must be non-negative; measured and sample cycle counts must be positive.";
		}
		rng <- "mersenne";
		seed <- 42.0;
		if first_level < 0 or last_level > 10 or last_level < first_level {
			error "Load levels must satisfy 0 <= first <= last <= 10.";
		}
		load_level <- first_level;
		run_id <- name + "_" + string(gama.machine_time);
		do configure_load();
	}

	reflex measure when: not finished {
		int stage_age <- cycle - stage_started_cycle;
		if stage_age = warmup_cycles {
			last_boundary_at <- gama.machine_time;
		}
		if stage_age > warmup_cycles {
			float dd <- gama.machine_time - last_boundary_at;
			elapsed_ms <- elapsed_ms + dd;
			window_ms <- window_ms + dd;
			max_cycle_ms <- max(max_cycle_ms, dd);
			completed_cycles <- completed_cycles + 1;
			window_count <- window_count + 1;
			if completed_cycles mod sample_cycles = 0 or completed_cycles = measured_cycles {
				do report(phase: "WINDOW");
				window_ms <- 0.0;
				window_count <- 0;
			}
			last_boundary_at <- gama.machine_time;
		}
		if completed_cycles = measured_cycles {
			checksum <- sum(particle collect (each.location.x + each.location.y + each.height));
			do report(phase: "RESULT");
			if load_level = last_level {
				finished <- true;
				do pause();
			} else {
				load_level <- load_level + 1;
				stage_started_cycle <- cycle;
				completed_cycles <- 0;
				elapsed_ms <- 0.0;
				max_cycle_ms <- 0.0;
				peak_used_mb <- 0.0;
				checksum <- 0.0;
				do configure_load();
				// With no warm-up, the next plateau starts in this same cycle.
				if warmup_cycles = 0 {
					last_boundary_at <- gama.machine_time;
				}
			}
		}
	}

	action configure_load() {
		int factor <- int(2 ^ load_level);
		int size_factor <- load_axis = "operations" ? 1 : factor;
		if float(base_population) * size_factor > 2147483647.0 or float(base_series) * size_factor > 2147483647.0
			or float(base_draw_copies) * (load_axis = "size" ? 1 : factor) > 2147483647.0 {
			error "Requested population, series or draw count exceeds the GAML integer range.";
		}
		population_size <- base_population * size_factor;
		series_count <- base_series * size_factor;
		draw_copies <- base_draw_copies * (load_axis = "size" ? 1 : factor);
		work_per_cycle <- float(population_size) * draw_copies * 2.0;
		do report(phase: "START");
		series_names <- range(0, series_count - 1) collect ("series_" + each);
		series_values <- range(0, series_count - 1) collect 0.0;
		create particle number: population_size - length(particle);
	}

	action report(string phase) {
		float used_mb <- (gama.max_memory - gama.free_memory) / 1000000.0;
		peak_used_mb <- max(peak_used_mb, used_mb);
		float ms_per_cycle <- completed_cycles = 0 ? 0.0 : elapsed_ms / completed_cycles;
		float window_mean <- window_count = 0 ? 0.0 : window_ms / window_count;
		float units_per_second <- elapsed_ms = 0.0 ? 0.0 : work_per_cycle * completed_cycles * 1000.0 / elapsed_ms;
		write "BENCHMARK," + phase + ",displays,level=" + load_level + ",axis=" + load_axis
			+ ",population=" + population_size + ",series=" + series_count + ",draw_copies=" + draw_copies
			+ ",cycles=" + completed_cycles + ",ms_per_cycle=" + ms_per_cycle
			+ ",window_ms_per_cycle=" + window_mean + ",max_cycle_ms=" + max_cycle_ms
			+ ",draws_per_second=" + units_per_second + ",heap_mb=" + used_mb + ",checksum=" + checksum
			buffering: "no_buffering";
		save [phase, run_id, gama.version, seed, load_axis, load_level, population_size, series_count, draw_copies,
			warmup_cycles, measured_cycles, completed_cycles, elapsed_ms, ms_per_cycle, window_mean,
			max_cycle_ms, units_per_second, used_mb, peak_used_mb, checksum]
			to: output_file format: "csv" header: false rewrite: false buffering: "no_buffering";
	}

	reflex update_series when: not finished {
		series_values <- range(0, series_count - 1) collect sin(cycle * 2.0 + each * 15.0);
	}
}

species particle parallel: false {
	float height <- rnd(2.0, 20.0);
	rgb color <- rgb(rnd(255), rnd(255), rnd(255));

	init {
		location <- {rnd(1000.0), rnd(1000.0)};
		shape <- square(6);
	}

	reflex move when: not finished {
		point next_location <- location + {1.0, 0.5};
		location <- {next_location.x >= 1000.0 ? next_location.x - 1000.0 : next_location.x,
			next_location.y >= 1000.0 ? next_location.y - 1000.0 : next_location.y};
		height <- 10.0 + 8.0 * sin(cycle + index);
	}

	aspect flat {
		loop i from: 0 to: draw_copies - 1 {
			draw shape at: location + {i mod 8, i div 8} color: color;
		}
	}

	aspect solid {
		loop i from: 0 to: draw_copies - 1 {
			draw shape at: location + {i mod 8, i div 8} color: color depth: height;
		}
	}
}

experiment Benchmark type: gui {
	float minimum_cycle_duration <- 0.0;

	parameter "First load level" var: first_level min: 0 max: 10;
	parameter "Last load level" var: last_level min: 0 max: 10;
	parameter "Load axis" var: load_axis among: ["size", "operations", "both"];
	parameter "Base particles" var: base_population min: 1;
	parameter "Base chart series" var: base_series min: 1;
	parameter "Base draws per particle and view" var: base_draw_copies min: 1;
	parameter "Warm-up cycles" var: warmup_cycles min: 0;
	parameter "Measured cycles" var: measured_cycles min: 1;
	parameter "Cycles per sample" var: sample_cycles min: 1;
	parameter "CSV output" var: output_file;

	output synchronized: true {
		layout #split;
		display "2D" type: 2d {
			species particle aspect: flat;
		}

		display "3D" type: opengl {
			species particle aspect: solid;
		}
		display "Charts" type: 3d {
			chart "Time series" type: series {
				datalist legend: series_names value: series_values;
			}
		}
	}
}

experiment Profile type: gui parent: Benchmark benchmark: true {}

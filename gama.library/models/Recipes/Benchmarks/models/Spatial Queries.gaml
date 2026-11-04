/**
* Name: Benchmark - Spatial Queries
* Description: Stresses a moving spatial index, radius/nearest queries and geometry intersections.
*   Size levels increase population at fixed world area and radius, so neighborhoods become denser.
*   Operations levels increase query count. See ../README.txt before running high levels.
* Tags: benchmark, performance, spatial, geometry, topology
*/
model BenchmarkSpatialQueries

global {
	geometry shape <- square(1000);
	int base_population <- 2000;
	int base_queries <- 100;
	int load_level <- 0;
	string load_axis <- "size";
	int population_size;
	int queries_per_cycle;
	float query_radius <- 20.0;
	int warmup_cycles <- 300;
	int measured_cycles <- 5000;
	int sample_cycles <- 250;
	string output_file <- "../results/spatial.csv";
	string run_id;
	float last_boundary_at;
	float elapsed_ms <- 0.0;
	float window_ms <- 0.0;
	float max_cycle_ms <- 0.0;
	float peak_used_mb <- 0.0;
	int completed_cycles <- 0;
	int window_count <- 0;
	float checksum <- 0.0;
	bool finished <- false;
	bool pause_at_end <- true;

	init {
		if load_level < 0 or load_level > 10 or not (["size", "operations", "both"] contains load_axis)
			or base_population < 1 or base_queries < 1 or query_radius <= 0.0 {
			error "Invalid load request: level 0..10, axis size/operations/both and positive bases/radius are required.";
		}
		if warmup_cycles < 0 or measured_cycles < 1 or sample_cycles < 1 {
			error "Warm-up must be non-negative; measured and sample cycle counts must be positive.";
		}
		rng <- "mersenne";
		seed <- 42.0;
		int factor <- int(2 ^ load_level);
		if float(base_population) * (load_axis = "operations" ? 1 : factor) > 2147483647.0
			or float(base_queries) * (load_axis = "size" ? 1 : factor) > 2147483647.0 {
			error "Requested population or query count exceeds the GAML integer range.";
		}
		population_size <- base_population * (load_axis = "operations" ? 1 : factor);
		queries_per_cycle <- base_queries * (load_axis = "size" ? 1 : factor);
		run_id <- name + "_" + string(gama.machine_time);
		do report(phase: "START");
		create site number: population_size;
	}

	reflex measure when: not finished {
		// Batch scheduling can replace the seed after init.
		if cycle = 0 {
			seed <- 42.0;
		}
		if cycle = warmup_cycles {
			last_boundary_at <- gama.machine_time;
		}
		if cycle > warmup_cycles {
			float duration <- gama.machine_time - last_boundary_at;
			elapsed_ms <- elapsed_ms + duration;
			window_ms <- window_ms + duration;
			max_cycle_ms <- max(max_cycle_ms, duration);
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
			finished <- true;
			do report(phase: "RESULT");
			if pause_at_end {
				do pause;
			}
		}
	}

	action report(string phase) {
		float used_mb <- (gama.max_memory - gama.free_memory) / 1000000.0;
		peak_used_mb <- max(peak_used_mb, used_mb);
		float ms_per_cycle <- completed_cycles = 0 ? 0.0 : elapsed_ms / completed_cycles;
		float window_mean <- window_count = 0 ? 0.0 : window_ms / window_count;
		float units_per_second <- elapsed_ms = 0.0 ? 0.0 : float(queries_per_cycle) * completed_cycles * 1000.0 / elapsed_ms;
		write "BENCHMARK," + phase + ",spatial,level=" + load_level + ",axis=" + load_axis
			+ ",population=" + population_size + ",queries=" + queries_per_cycle + ",radius=" + query_radius
			+ ",cycles=" + completed_cycles + ",ms_per_cycle=" + ms_per_cycle
			+ ",window_ms_per_cycle=" + window_mean + ",max_cycle_ms=" + max_cycle_ms
			+ ",queries_per_second=" + units_per_second + ",heap_mb=" + used_mb + ",checksum=" + checksum
			buffering: "no_buffering";
		save [phase, run_id, gama.version, seed, load_axis, load_level, population_size, queries_per_cycle, query_radius,
			warmup_cycles, measured_cycles, completed_cycles, elapsed_ms, ms_per_cycle, window_mean,
			max_cycle_ms, units_per_second, used_mb, peak_used_mb, checksum]
			to: output_file format: "csv" header: false rewrite: false buffering: "no_buffering";
	}

	reflex query when: not finished {
		loop times: queries_per_cycle {
			point target <- {rnd(1000.0), rnd(1000.0)};
			site nearest <- site closest_to target;
			list<site> nearby <- (target neighbors_at query_radius) of_species site;
			geometry disk <- circle(query_radius) at_location target;
			geometry clipped <- disk intersection shape;
			checksum <- checksum + (nearest distance_to target) + length(nearby) + clipped.area;
		}
	}
}

species site parallel: false {
	init {
		location <- {rnd(1000.0), rnd(1000.0)};
	}

	reflex move when: not finished {
		point next_location <- location + {0.75, 0.5};
		location <- {next_location.x >= 1000.0 ? next_location.x - 1000.0 : next_location.x,
			next_location.y >= 1000.0 ? next_location.y - 1000.0 : next_location.y};
	}
}

experiment Benchmark type: gui {
	float minimum_cycle_duration <- 0.0;

	parameter "Load level (factor 2^level)" var: load_level min: 0 max: 10;
	parameter "Load axis" var: load_axis among: ["size", "operations", "both"];
	parameter "Base population" var: base_population min: 1;
	parameter "Base queries per cycle" var: base_queries min: 1;
	parameter "Query radius" var: query_radius min: 1.0 max: 500.0;
	parameter "Warm-up cycles" var: warmup_cycles min: 0;
	parameter "Measured cycles" var: measured_cycles min: 1;
	parameter "Cycles per sample" var: sample_cycles min: 1;
	parameter "CSV output" var: output_file;
}

experiment Replications type: batch repeat: 3 keep_seed: true parallel: false
	until: finished {
	float minimum_cycle_duration <- 0.0;

	parameter "Load level (factor 2^level)" var: load_level among: [0, 1, 2, 3, 4, 5];
	parameter "Load axis" var: load_axis among: ["size"];
	parameter "Pause at completion" var: pause_at_end among: [false];
}

experiment Operations type: batch parent: Replications repeat: 3 keep_seed: true parallel: false until: finished {
	parameter "Load axis" var: load_axis among: ["operations"];
}

experiment Stress type: batch parent: Replications repeat: 1 keep_seed: true parallel: false until: finished {
	parameter "Load axis" var: load_axis among: ["both"];
}

experiment Profile type: gui parent: Benchmark benchmark: true {}

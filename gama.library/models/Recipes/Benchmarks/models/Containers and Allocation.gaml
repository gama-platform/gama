/**
* Name: Benchmark - Containers and Allocation
* Description: Measures list/map allocation, copying, append/remove, filtering, collecting, sorting and casting.
*   Size levels grow source containers and a survivor ring, stressing both allocation and retained heap.
*   Operations levels increase churn at fixed retained size. See ../README.txt before high levels.
* Tags: benchmark, performance, containers, list, map, allocation, garbage_collection
*/
model BenchmarkContainersAndAllocation

global {
	int base_size <- 4000;
	int base_operations <- 20;
	int base_survivors <- 64;
	int load_level <- 0;
	string load_axis <- "size";
	int container_size;
	int operations_per_cycle;
	int survivor_slots;
	int survivor_index <- 0;
	int warmup_cycles <- 300;
	int measured_cycles <- 5000;
	int sample_cycles <- 250;
	string output_file <- "../results/containers.csv";
	string run_id;
	list<int> source;
	list<list<int>> survivors;
	float last_boundary_at;
	float elapsed_ms <- 0.0;
	float window_ms <- 0.0;
	float max_cycle_ms <- 0.0;
	float peak_used_mb <- 0.0;
	int completed_cycles <- 0;
	int window_count <- 0;
	float work_per_cycle;
	float checksum <- 0.0;
	bool finished <- false;
	bool pause_at_end <- true;

	init {
		if load_level < 0 or load_level > 10 or not (["size", "operations", "both"] contains load_axis)
			or base_size < 10 or base_operations < 1 or base_survivors < 1 {
			error "Invalid load request: level 0..10, axis size/operations/both, size >= 10 and positive operations/slots are required.";
		}
		if warmup_cycles < 0 or measured_cycles < 1 or sample_cycles < 1 {
			error "Warm-up must be non-negative; measured and sample cycle counts must be positive.";
		}
		rng <- "mersenne";
		seed <- 42.0;
		int factor <- int(2 ^ load_level);
		int size_factor <- load_axis = "operations" ? 1 : factor;
		if float(base_size) * size_factor > 2147483647.0 or float(base_survivors) * size_factor > 2147483647.0
			or float(base_operations) * (load_axis = "size" ? 1 : factor) > 2147483647.0 {
			error "Requested container size, retention or operation count exceeds the GAML integer range.";
		}
		container_size <- base_size * size_factor;
		survivor_slots <- base_survivors * size_factor;
		operations_per_cycle <- base_operations * (load_axis = "size" ? 1 : factor);
		work_per_cycle <- float(container_size) * operations_per_cycle;
		run_id <- name + "_" + string(gama.machine_time);
		do report(phase: "START");
		source <- range(0, container_size - 1);
		loop times: survivor_slots {
			add [] to: survivors;
		}
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
		float units_per_second <- elapsed_ms = 0.0 ? 0.0 : work_per_cycle * completed_cycles * 1000.0 / elapsed_ms;
		write "BENCHMARK," + phase + ",containers,level=" + load_level + ",axis=" + load_axis
			+ ",size=" + container_size + ",operations=" + operations_per_cycle + ",survivors=" + survivor_slots
			+ ",cycles=" + completed_cycles + ",ms_per_cycle=" + ms_per_cycle
			+ ",window_ms_per_cycle=" + window_mean + ",max_cycle_ms=" + max_cycle_ms
			+ ",source_elements_per_second=" + units_per_second + ",heap_mb=" + used_mb + ",checksum=" + checksum
			buffering: "no_buffering";
		save [phase, run_id, gama.version, seed, load_axis, load_level, container_size, operations_per_cycle, survivor_slots,
			warmup_cycles, measured_cycles, completed_cycles, elapsed_ms, ms_per_cycle, window_mean,
			max_cycle_ms, units_per_second, used_mb, peak_used_mb, checksum]
			to: output_file format: "csv" header: false rewrite: false buffering: "no_buffering";
	}

	reflex allocate when: not finished {
		loop i from: 0 to: operations_per_cycle - 1 {
			list<int> values <- copy(source);
			add cycle to: values;
			remove index: 0 from: values;
			list<int> selected <- values where (each mod 3 = 0);
			list<int> transformed <- selected collect (each * 2 + 1);
			list<int> ordered <- transformed sort_by (-each);
			map<int, int> lookup <- ordered as_map (each::(each + 1));
			list<float> converted <- list<float>(ordered);
			checksum <- checksum + sum(converted) + length(lookup) + lookup[ordered[0]];
			survivors[survivor_index] <- ordered;
			survivor_index <- (survivor_index + 1) mod survivor_slots;
		}
	}
}

experiment Benchmark type: gui {
	float minimum_cycle_duration <- 0.0;

	parameter "Load level (factor 2^level)" var: load_level min: 0 max: 10;
	parameter "Load axis" var: load_axis among: ["size", "operations", "both"];
	parameter "Base source list size" var: base_size min: 10;
	parameter "Base operations per cycle" var: base_operations min: 1;
	parameter "Base retained list slots" var: base_survivors min: 1;
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

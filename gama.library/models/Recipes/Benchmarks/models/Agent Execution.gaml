/**
* Name: Benchmark - Agent Execution
* Description: Measures sequential population scheduling, attribute access, reflexes, action calls and arithmetic.
*   Replications doubles population at each level; Operations doubles calls; Stress doubles both.
*   Each run has 300 warm-up and 5000 measured cycles. See ../README.txt before running high levels.
* Tags: benchmark, performance, agents, scheduling, arithmetic
*/
model BenchmarkAgentExecution

global {
	int base_population <- 5000;
	int base_operations <- 10;
	int load_level <- 0;
	string load_axis <- "size" among: ["size", "operations", "both"];
	int population_size;
	int operations_per_agent;
	int warmup_cycles <- 300;
	int measured_cycles <- 5000;
	int sample_cycles <- 250;
	string output_file <- "../results/agents.csv";
	string run_id;
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
	bool pause_at_end <- true;

	init {
		if load_level < 0 or load_level > 10 or not (["size", "operations", "both"] contains load_axis)
			or base_population < 1 or base_operations < 1 {
			error "Invalid load request: level 0..10, axis size/operations/both and positive bases are required.";
		}
		if warmup_cycles < 0 or measured_cycles < 1 or sample_cycles < 1 {
			error "Warm-up must be non-negative; measured and sample cycle counts must be positive.";
		}
		rng <- "mersenne";
		seed <- 42.0;
		int factor <- int(2 ^ load_level);
		if float(base_population) * (load_axis = "operations" ? 1 : factor) > 2147483647.0
			or float(base_operations) * (load_axis = "size" ? 1 : factor) > 2147483647.0 {
			error "Requested population or operation count exceeds the GAML integer range.";
		}
		population_size <- base_population * (load_axis = "operations" ? 1 : factor);
		operations_per_agent <- base_operations * (load_axis = "size" ? 1 : factor);
		work_per_cycle <- float(population_size) * operations_per_agent;
		run_id <- name + "_" + string(gama.machine_time);
		do report(phase: "START");
		create worker number: population_size;
	}

	// Boundaries precede population stepping: the interval includes complete scheduled cycles.
	reflex measure when: not finished {
		// Batch scheduling can replace the seed after init.
		if cycle = 0 {
			seed <- 42.0;
		}
		if cycle = warmup_cycles {
			last_boundary_at <- gama.machine_time;
		}
		if cycle > warmup_cycles {
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
			finished <- true;
			checksum <- sum(worker collect each.value);
			do report(phase: "RESULT");
			if pause_at_end {
				experiment.pause();
			}
		}
	}

	action report(string phase) {
		float used_mb <- (gama.max_memory - gama.free_memory) / 1000000.0;
		peak_used_mb <- max(peak_used_mb, used_mb);
		float ms_per_cycle <- completed_cycles = 0 ? 0.0 : elapsed_ms / completed_cycles;
		float window_mean <- window_count = 0 ? 0.0 : window_ms / window_count;
		float units_per_second <- elapsed_ms = 0.0 ? 0.0 : work_per_cycle * completed_cycles * 1000.0 / elapsed_ms;
		write "BENCHMARK," + phase + ",agents,level=" + load_level + ",axis=" + load_axis
			+ ",population=" + population_size + ",calls=" + operations_per_agent
			+ ",cycles=" + completed_cycles + ",ms_per_cycle=" + ms_per_cycle
			+ ",window_ms_per_cycle=" + window_mean + ",max_cycle_ms=" + max_cycle_ms
			+ ",calls_per_second=" + units_per_second + ",heap_mb=" + used_mb + ",checksum=" + checksum
			buffering: "no_buffering";
		save [phase, run_id, gama.version, seed, load_axis, load_level, population_size, operations_per_agent,
			warmup_cycles, measured_cycles, completed_cycles, elapsed_ms, ms_per_cycle, window_mean,
			max_cycle_ms, units_per_second, used_mb, peak_used_mb, checksum]
			to: output_file format: "csv" header: false rewrite: false buffering: "no_buffering";
	}
}

species worker parallel: false {
	float value <- rnd(1.0);
	float bias <- rnd(1.0);

	reflex compute when: not finished {
		loop times: operations_per_agent {
			do integrate_value();
		}
	}

	action integrate_value() {
		value <- value * 0.999 + bias;
	}
}

experiment Benchmark type: gui {
	float minimum_cycle_duration <- 0.0;

	parameter "Load level (factor 2^level)" var: load_level min: 0 max: 10;
	parameter "Load axis" var: load_axis among: ["size", "operations", "both"];
	parameter "Base population" var: base_population min: 1;
	parameter "Base calls per agent" var: base_operations min: 1;
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
	
	init {
		ask simulations {
			pause_at_end <- false;
		}
	}

}

experiment Operations type: batch parent: Replications repeat: 3 keep_seed: true parallel: false until: finished {
	parameter "Load axis" var: load_axis <- "operations";
}

experiment Stress type: batch parent: Replications repeat: 1 keep_seed: true parallel: false until: finished {
	parameter "Load axis" var: load_axis <- "both";
}

experiment Profile type: gui parent: Benchmark benchmark: true {}

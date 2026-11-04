/**
* Name: Benchmark - Matrix Computation
* Description: Measures dense float-matrix arithmetic, transposition, reduction and indexed updates.
*   Size levels double matrix area; Operations doubles iterations; Stress increases both.
*   Dense temporaries deliberately put pressure on the heap. See ../README.txt before high levels.
* Tags: benchmark, performance, matrix, arithmetic, numerical
*/
model BenchmarkMatrixComputation

global {
	int base_side <- 256;
	int base_operations <- 10;
	int load_level <- 0;
	string load_axis <- "size" among: ["size", "operations", "both"];
	int matrix_size;
	int operations_per_cycle;
	int warmup_cycles <- 300;
	int measured_cycles <- 5000;
	int sample_cycles <- 250;
	string output_file <- "../results/matrices.csv";
	string run_id;
	matrix<float> values;
	matrix<float> forcing;
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
			or base_side < 2 or base_operations < 1 {
			error "Invalid load request: level 0..10, axis size/operations/both, side >= 2 and positive operations are required.";
		}
		if warmup_cycles < 0 or measured_cycles < 1 or sample_cycles < 1 {
			error "Warm-up must be non-negative; measured and sample cycle counts must be positive.";
		}
		rng <- "mersenne";
		seed <- 42.0;
		int factor <- int(2 ^ load_level);
		if float(base_side) * base_side * (load_axis = "operations" ? 1 : factor) > 2147483647.0
			or float(base_operations) * (load_axis = "size" ? 1 : factor) > 2147483647.0 {
			error "Requested matrix area or operation count exceeds the GAML integer range.";
		}
		matrix_size <- int(base_side * sqrt(load_axis = "operations" ? 1 : factor));
		operations_per_cycle <- base_operations * (load_axis = "size" ? 1 : factor);
		work_per_cycle <- float(matrix_size) * matrix_size * operations_per_cycle;
		run_id <- name + "_" + string(gama.machine_time);
		do report(phase: "START");
		values <- {matrix_size, matrix_size} matrix_with 0.0;
		forcing <- {matrix_size, matrix_size} matrix_with 0.0;
		loop x from: 0 to: matrix_size - 1 {
			loop y from: 0 to: matrix_size - 1 {
				values[{x, y}] <- rnd(1.0);
				forcing[{x, y}] <- (x + y + 1.0) / matrix_size;
			}
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
			do report(phase: "RESULT");
			if pause_at_end {
				do pause();
			}
		}
	}

	action report(string phase) {
		float used_mb <- (gama.max_memory - gama.free_memory) / 1000000.0;
		peak_used_mb <- max(peak_used_mb, used_mb);
		float ms_per_cycle <- completed_cycles = 0 ? 0.0 : elapsed_ms / completed_cycles;
		float window_mean <- window_count = 0 ? 0.0 : window_ms / window_count;
		float units_per_second <- elapsed_ms = 0.0 ? 0.0 : work_per_cycle * completed_cycles * 1000.0 / elapsed_ms;
		write "BENCHMARK," + phase + ",matrices,level=" + load_level + ",axis=" + load_axis
			+ ",side=" + matrix_size + ",operations=" + operations_per_cycle
			+ ",cycles=" + completed_cycles + ",ms_per_cycle=" + ms_per_cycle
			+ ",window_ms_per_cycle=" + window_mean + ",max_cycle_ms=" + max_cycle_ms
			+ ",cells_per_second=" + units_per_second + ",heap_mb=" + used_mb + ",checksum=" + checksum
			buffering: "no_buffering";
		save [phase, run_id, gama.version, seed, load_axis, load_level, matrix_size, operations_per_cycle,
			warmup_cycles, measured_cycles, completed_cycles, elapsed_ms, ms_per_cycle, window_mean,
			max_cycle_ms, units_per_second, used_mb, peak_used_mb, checksum]
			to: output_file format: "csv" header: false rewrite: false buffering: "no_buffering";
	}

	reflex compute when: not finished {
		loop times: operations_per_cycle {
			values <- (transpose(values) * 0.99 + forcing) / 1.01;
			int diagonal <- cycle mod matrix_size;
			values[{diagonal, diagonal}] <- values[{diagonal, diagonal}] + 0.001;
			checksum <- checksum + sum(values);
		}
	}
}

experiment Benchmark type: gui {
	float minimum_cycle_duration <- 0.0;

	parameter "Load level (factor 2^level)" var: load_level min: 0 max: 10;
	parameter "Load axis" var: load_axis among: ["size", "operations", "both"];
	parameter "Base matrix side" var: base_side min: 2;
	parameter "Base operations per cycle" var: base_operations min: 1;
	parameter "Warm-up cycles" var: warmup_cycles min: 0;
	parameter "Measured cycles" var: measured_cycles min: 1;
	parameter "Cycles per sample" var: sample_cycles min: 1;
	parameter "CSV output" var: output_file;
}

experiment Replications type: batch repeat: 3 keep_seed: true parallel: false
	until: finished {
	float minimum_cycle_duration <- 0.0;

	parameter "Load level (factor 2^level)" var: load_level among: [0, 1, 2, 3, 4, 5];
	parameter "Load axis" var: load_axis <- "size";
	parameter "Pause at completion" var: pause_at_end <- false;
}

experiment Operations type: batch parent: Replications repeat: 3 keep_seed: true parallel: false until: finished {
	parameter "Load axis" var: load_axis  <- "operations";
}

experiment Stress type: batch parent: Replications repeat: 1 keep_seed: true parallel: false until: finished {
	parameter "Load axis" var: load_axis <- "both";
}

experiment Profile type: gui parent: Benchmark benchmark: true {}

/**
* Name: BenchmarkBinarySerialisers
* Description: Measures the speed and size of Apache Fory binary serialisation for different GAMA values.
* Tags: serialize, benchmark
*/
model BenchmarkBinarySerialisers

global {
	int nb_agents <- 2000;
	int repetitions <- 20;
	int warmups <- 3;
}

species bench_agent {
	int amount <- rnd(1000);
	float ratio <- rnd(1.0);
	string label <- "agent" + rnd(1000);
	point where <- {rnd(100.0), rnd(100.0)};
	rgb colour <- rgb(rnd(255), rnd(255), rnd(255));
	list<int> numbers <- [1, 2, 3, rnd(10)];
	map<string, float> properties <- ["a"::rnd(1.0), "b"::rnd(1.0)];
}

experiment Benchmark type: gui {

	action measure (string title, unknown value) {
		string bytes <- "";
		loop times: warmups {
			bytes <- to_binary(value);
			unknown dummy <- from_binary(bytes);
		}
		float write_time <- 0.0;
		float read_time <- 0.0;
		
		float memory_before <- gama.free_memory;
		loop times: repetitions {
			float t0 <- gama.machine_time;
			bytes <- to_binary(value);
			float t1 <- gama.machine_time;
			unknown restored <- from_binary(bytes);
			float t2 <- gama.machine_time;
			write_time <- write_time + (t1 - t0);
			read_time <- read_time + (t2 - t1);
		}
		write "[" + title + "] size: " + length(bytes) + " chars | write: " + ((write_time / repetitions) with_precision 2)
		+ " ms | read: " + ((read_time / repetitions) with_precision 2) + " ms | free memory delta: "
		+ (((memory_before - gama.free_memory) / 1024 / 1024) with_precision 1) + " MB";
	}
	
	action serialize() {
		list<int> ints <- list_with(100000, rnd(1000000));
		list<string> strings <- list_with(20000, "text" + rnd(100000));
		map<string, float> numbers <- map<string, float>(list_with(20000, rnd(100)) as_map (("k" + rnd(100000))::each));
		create bench_agent number: nb_agents;
		write "Backend: Apache Fory";
		do measure("100000 ints", ints);
		do measure("20000 strings", strings);
		do measure("map of 20000 floats", numbers);
		do measure("list of " + nb_agents + " agents", list(bench_agent));
		ask bench_agent { do die(); }
	}

	user_command "Run benchmark" {
		do serialize();
	}

}

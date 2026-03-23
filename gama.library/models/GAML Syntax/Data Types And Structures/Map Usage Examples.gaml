/**
* Name: Map Usage Examples
* Author: GitHub Copilot
* Description: A collection of examples demonstrating how to use maps in GAML for various tasks.
* Tags: map, data_structure, loop
*/

model MapUsageExamples

global {
	init {
		do basic_usage;
		do modification_examples;
		do iteration_examples;
		do advanced_usage;
		do agent_usage;
	}
	
	action basic_usage() {
		write "=== Basic Usage ===";
		
		// 1. Creation
		map<string, int> ages <- ["Alice"::25, "Bob"::30, "Charlie"::35];
		write "Initial map: " + ages;
		
		// 2. Accessing values
		int alice_age <- ages["Alice"];
		write "Alice's age: " + alice_age;
		
		// Accessing with 'at'
		int bob_age <- ages at "Bob";
		write "Bob's age: " + bob_age;
		
		// Check if key exists
		bool has_david <- "David" in ages.keys;
		write "Has David? " + has_david;
		
		// Check if value exists
		bool has_30 <- 30 in ages.values;
		write "Has age 30? " + has_30;
	}
	
	action modification_examples() {
		write "\n=== Modification Examples ===";
		map<string, string> capitals <- ["France"::"Paris", "Japan"::"Tokyo"];
		write "Original: " + capitals;
		
		// Adding a new pair
		capitals["USA"] <- "Washington D.C.";
		// Or using add
		add "Berlin" at: "Germany" to: capitals;
		
		write "After adding USA and Germany: " + capitals;
		
		// Modifying an existing value
		capitals["Japan"] <- "Kyoto"; // Historical capital for example
		write "After modifying Japan: " + capitals;
		
		// Removing a key
		remove key: "Japan" from: capitals;
		write "After removing Japan: " + capitals;
	}
	
	action iteration_examples() {
		write "\n=== Iteration Examples ===";
		map<string, float> prices <- ["Apple"::1.2, "Banana"::0.8, "Orange"::1.5];
		
		// Loop over keys
		loop fruit over: prices.keys {
			write fruit + " costs " + prices[fruit];
		}
		
		// Loop over values
		loop price over: prices.values {
			write "Price found: " + price;
		}
		
		// Loop over pairs (key::value)
		loop item over: prices.pairs {
			write "Item: " + item.key + " -> " + item.value;
		}
	}
	
	action advanced_usage() {
		write "\n=== Advanced Usage ===";
		
		// Grouping agents or objects
		list<int> numbers <- [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
		
		// Group by even/odd
		map<bool, list<int>> grouped <- numbers group_by ((each mod 2) = 0);
		write "Grouped by even (true) / odd (false): " + grouped;
		
		// Counting occurrences using a map
		list<string> words <- ["apple", "banana", "apple", "orange", "banana", "apple"];
		map<string, int> counts <- [];
		
		loop w over: words {
			if (w in counts.keys) {
				counts[w] <- counts[w] + 1;
			} else {
				counts[w] <- 1;
			}
		}
		write "Word counts: " + counts;
		
		// Using as_map to transform a list
		list<string> names <- ["John", "Paul", "George", "Ringo"];
		map<string, int> name_lengths <- names as_map (each::length(each));
		write "Name lengths: " + name_lengths;
	}

	action agent_usage() {
		write "\n=== Agent Usage ===";
		create person number: 5;
		
		// Indexing agents by a property
		// This creates a map where the key is the name and the value is the agent itself
		map<string, person> people_by_name <- person index_by each.name;
		write "People indexed by name: " + people_by_name;
		
		// Accessing an agent via the map
		if (!empty(people_by_name)) {
			string first_name <- people_by_name.keys[0];
			person p <- people_by_name[first_name];
			write "Retrieved agent " + p + " using key " + first_name;
		}
	}
}

species person {
	init {
		name <- "Person_" + int(self);
	}
}

experiment RunExamples type: gui {
	output {
		display View {
			graphics "text" {
				draw "Check the console for output" at: {50, 50} color: #black font: font("Arial", 20, #bold);
			}
		}
	}
}
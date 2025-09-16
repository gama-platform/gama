/**
* Name: SPARQLquerytoagents
* Loads a query from a file that asks for french philosophers and their influences. Then instanciates philosopher agents and draw everything
* Author: baptiste lesquoy
* Tags: 
*/


model SPARQLquerytoagents


global {

	text_file query <- text_file("../includes/SPARQL/SPARQL query philosophers and influences.txt");
	string endpoint <- "https://dbpedia.org/sparql";
	string output_file <- "../includes/SPARQL/result.csv";
	
	
	init {
		// we use concatenate to rebuild the string of the query
		string full_query_text <- concatenate(query.contents, "\n");
		
		// running the query
		map<string, list<string>> result <- sparql_query(full_query_text, endpoint);
		
		// we check for errors
		if empty(result){
			write #current_error color:#red;
			return;
		}
		
		// Now we create agents based on the data we collected
		int nbPhilosophers <- length(first(result));		
		loop i from:0 to:nbPhilosophers-1 {
			create philosopher {
				full_name <- result["name"][i];
				date_of_birth <- date(result["birthDateStr"][i]);
				place_of_birth <- first(result["birthPlaces"][i] split_with ";");
				string infl <- result["influencedByList"][i];
				if infl != nil and !empty(infl) {
					influences <- (infl split_with ";") as_map (each::[]);// hack that could be removed when #767 is solved			
				}
				else {
					influences <- map([]);
				}
			}	
		}
		philosopher random_philosopher <- one_of(philosopher);
		write "Philosopher agents created, here is one: " + random_philosopher.full_name 
				+ " born in " + random_philosopher.place_of_birth 
				+ " the " + random_philosopher.date_of_birth
				+ " influenced by: " + random_philosopher.influences;
		
		write "Now we are going to create instances for their influences too";
		loop phi over:copy(philosopher){ // we do a copy to avoid looping over new ones
			loop influencer over:copy(phi.influences.keys){// we do a copy to not interfere with the original keys, even though not needed here
				philosopher inf <- first_with(philosopher, each.full_name = influencer);
				if inf != nil{
					phi.influences[influencer] <- inf;
				}				
				else {
					create philosopher{
						full_name <- influencer;
						phi.influences[influencer] <- self;
					}
				}
			}
			
		}
		
	}

}

species philosopher {
	point location <- {rnd(100), rnd(100), rnd(100)}; // picks a random point in 3d instead of 2d
	string full_name;
	date date_of_birth;
	string place_of_birth;
	map<string, philosopher> influences;
	rgb color <- rnd_color(255);
	
	aspect default{
		draw sphere(1) at:location color:color;
		draw full_name at:location color:(#black-color);
		//draw arrows from the influences
		loop influencer over:influences where (each!=nil){
			draw line(location, influencer.location) color:influencer.color begin_arrow:1;
		}
	}
}

experiment query{
	
	output{
		display main type:3d axes:false{
			camera 'default' location: {108.2297,164.9931,200.7902} target: {51.5473,24.6993,0.0};
			species philosopher;
		}
	}	
	
}



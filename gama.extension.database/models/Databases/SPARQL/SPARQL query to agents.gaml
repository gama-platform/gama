/**
* Name: SPARQLquerytoagents
* Loads a query from a file that asks for french philosophers and their influences. Then instanciates philosopher agents and draw everything as a graph
* Author: baptiste lesquoy
* Tags: 
*/


model SPARQLquerytoagents


global {

	text_file query <- text_file("../includes/SPARQL/SPARQL query philosophers and influences.txt");
	string endpoint <- "https://dbpedia.org/sparql";
	string output_file <- "../includes/SPARQL/result.csv";
	int depth <- 600;
	int width <- 300;
	int height <- 200;
	geometry shape <- rectangle(depth, width);
	
	
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
				// we place those in the center of the y and z axis, the influences will be anywhere
				location <- {0, width/2, height/2};
				
				full_name <- result["name"][i];
				date_of_birth <- date(result["birthDateStr"][i]);
				place_of_birth <- first(result["birthPlaces"][i] split_with ";");
				string infl <- result["influencedByList"][i];
				if infl != nil and !empty(infl) {
					influences <-map<string, philosopher>(((infl split_with ";") where (each != '')) as_map (each::[]));// hack that could be removed when #767 is solved			
				}
				else {
					influences <- map([]);
				}
			}	
		}
		
		philosopher random_philosopher <- one_of(philosopher);
		write "Philosopher agents created, here is one: ";
		write  		random_philosopher.full_name 
				+ "\n\tborn in " + random_philosopher.place_of_birth 
				+ "\n\ton the " + random_philosopher.date_of_birth.day + "/" + random_philosopher.date_of_birth.month + "/" + random_philosopher.date_of_birth.year
				+ "\n\tinfluenced by: " + random_philosopher.influences.keys color:#blue;
		
		loop phi over:copy(philosopher){ // we do a copy to avoid looping over new ones
			loop influencer_str over:copy(phi.influences.keys){// we do a copy to not interfere with the original keys, even though not needed here
				int idx_db <- influencer_str last_index_of " (";
				string inf_name <- influencer_str copy_between (0, idx_db);
				philosopher inf <- first_with(philosopher, each.full_name = inf_name);
				if inf != nil{
					phi.influences[influencer_str] <- inf;
				}				
				else {
					create philosopher{
						full_name <- inf_name;
						date_of_birth <- date(influencer_str copy_between(idx_db+2, length(influencer_str)-1));
						phi.influences[influencer_str] <- self;
					}	
				}
			}
		}
		
		// Finally we layout the philosophers so they are following a chronological order
		do layout_philosophers;
	}

	action layout_philosophers {
		
		// we are going to scale the dates on the x axis
		float min <- float(philosopher min_of each.date_of_birth);
		float max <- float(philosopher max_of each.date_of_birth);
		float span <- max-min;
		// let's set all the original philosopher to the center on x and y axis first
		ask philosopher{
			location <- { depth * ((float(date_of_birth) - min)/span) ^ 7,location.y, location.z};
		}		
	}
}

species philosopher {
	point location <- {rnd(depth), rnd(width), rnd(height)}; // picks a random point in 3d instead of 2d
	string full_name;
	date date_of_birth;
	string place_of_birth;
	map<string, philosopher> influences;
	rgb color <- rnd_color(255);
	
	aspect default{
		draw sphere(1) at:location color:color;
		int y_offset <- (index mod 2 = 0) ? 2 : -length(full_name)*2 - 2; // one out of two philosopher is written on the right of its sphere, the others on the left
		draw full_name at:location + {-1, y_offset,0} color:color rotate:90::{0,0,1};
		//draw arrows from the influences
		loop influencer over:influences where (each!=nil){
			float dist <- (location distance_to influencer.location);
			draw elliptical_arc(location, influencer.location,  dist / 8, max(int(dist), 2)) color:influencer.color begin_arrow:1;

		}
	}
}

experiment query{
	
	output{
		display main type:3d axes:false{
			camera 'default' location: {11.0046,474.4617,340.9168} target: {351.4035,61.5247,0.0};
			species philosopher;
		}
	}	
	
}


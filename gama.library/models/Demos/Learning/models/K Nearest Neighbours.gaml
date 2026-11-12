/**
* Name: K Nearest Neighbours
* Author: Minh Nguyen Dich Nhat
* Description: An interactive demonstration of the K-Nearest Neighbors (KNN) classification algorithm. Pre-existing
*   points of different color groups are placed in circular clusters. When the user clicks on any location, a new
*   point is generated there and its color is determined by a majority vote among its k nearest neighbors. The user
*   can also let points be generated dynamically at random positions. Parameters control the number of groups,
*   their radii, the initial number of points per group, and k (the number of neighbors). This model illustrates
*   how the KNN algorithm creates non-linear decision boundaries in a spatial context. A ring marks the last
*   clicked point; optional links highlight the neighbors that voted for its classification.
* Tags: machine_learning, KNN, classification, spatial, user_interaction, algorithm, clustering
*/

/**
 * There will be four parameters
 * The first one is "Number of group", it will specify the number of group in our model. Different groups have different colors.
 * The second one is "Radius", this parameter is the radius of the circles which contain the groups. 
 * The third one is "Initial number of point for each group". 
 * And the final one is "Number of neighbors", this will specify the number of neighbors that we want to use for our "k_nearest_neighbors" operator,
 * the "k_nearest_neighbors" have one parameter called k, this parameter specify the number of agents we want to compare with our agent. The 
 * "Number of neighbours" parameter is the parameter k in the "k_nearest_neighbors" operator.
 */


model KNearestNeighbors


global {

	map<special_point, rgb> store;
	rgb backdrop <- rgb(19, 27, 40);
	rgb lettering <- rgb(220, 230, 240);
	list<rgb> colours <- [rgb(69, 211, 183), rgb(255, 184, 107), rgb(112, 163, 255),
		rgb(222, 135, 205), rgb(188, 215, 111), rgb(179, 153, 245),
		rgb(255, 133, 139), rgb(109, 211, 231), rgb(222, 206, 155)];
	special_point last_query;
	list<special_point> neighbors <- [];
	bool show_neighbors <- true;
	bool automatic <- true;
    int init_amount <- 10;
    int nb_group <- 2;
    float radius <- 10.0;
    int k <- 10;
	init {
		loop i from: 0 to: nb_group - 1{
			special_point center;
			create special_point{
				color <- colours[i];
				center <- self;
			}
			geometry around <- circle(radius, center.location) intersection world.shape;
			create special_point number: init_amount - 1{
				location <- any_location_in(around);
				color <- center.color;
			}
		}
		store <- world.special_point as_map (each::each.color);
	}

	action createAgent(){
	    create special_point{
	    	location <- #user_location;
	    	color <- self k_nearest_neighbors (store, k);
			last_query <- self;
			neighbors <- first(min([k, length(store)]), store.keys sort_by (self distance_to each));
		    store <+ (self::color);
	    }
	}

	reflex auto when: automatic {
		create special_point{
	   		color <- self k_nearest_neighbors (store, k);
	   		store <+ self::color;
	   	}
	}
}

species special_point {
	float size <- 0.5;
	rgb color;
	aspect base{
		draw circle(size * 2.2) color: rgb(color, 0.1);
		draw circle(size) color: color border: rgb(lettering, 0.35);
	}
	
}

experiment KNearestNeighbors {
	parameter "Number of groups: " var:nb_group min: 2 max: 9;
	parameter "Radius: " var: radius min:10.0 max:30.0;
	parameter "Initial numbers of agent for each group: " var:init_amount min:1 max:50;
	parameter "Number of neighbours considered (K)" var:k min:1;
	parameter "Generate points automatically" var: automatic category: "Interaction";
	parameter "Show neighbors of last clicked point" var: show_neighbors category: "Appearance";
	output {
		display main type:2d background:backdrop {
			graphics classification {
				if (show_neighbors and last_query != nil) {
					loop neighbor over: neighbors {
						draw line([last_query.location, neighbor.location]) color: rgb(neighbor.color, 0.45);
						draw circle(1.1) at: neighbor.location color: neighbor.color wireframe: true;
					}
				}
			}
			species special_point aspect: base;
			graphics query_marker {
				if (last_query != nil) {
					draw circle(1.6) at: last_query.location color: lettering wireframe: true width: 2;
				}
			}
			event #mouse_down {ask simulation {do createAgent();}} 
			overlay coordinates: #screen position: {16, 16} size: {345, 118} background: backdrop rounded: true {
				draw "K-NEAREST NEIGHBORS" at: {14#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
				draw "Click to classify a new point" at: {14#px, 49#px} color: colours[0] font: font("Helvetica", 11);
				draw "K = " + string(k) + " / Points = " + string(length(special_point))
					at: {14#px, 75#px} color: lettering font: font("Helvetica", 11);
				draw "Ring: last query / Links: its voting neighbors" at: {14#px, 101#px} color: lettering font: font("Helvetica", 11);
			}
		}
	}
}
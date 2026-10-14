/**
* Name: MovingOnGraphsTest
* Description: Tests the moving skill on networks (goto on a graph, follow a path) and the bounds of wander
* Tags: moving, skill, graph, path, test
*/

model MovingOnGraphsTestModel

global {
	geometry shape <- square(100);
}

species walker skills: [moving] { }

experiment MovingOnGraphsTest type: test autorun: true {

	// an L-shaped network: {0, 0} -> {50, 0} -> {50, 50}
	graph network <- as_edge_graph([line([{0, 0}, {50, 0}]), line([{50, 0}, {50, 50}])]);

	test "Going to a target on a graph follows the edges" {		
		create walker(location:{0, 0}, speed:10.0) returns: created;
		walker w <- created[0];
		ask w { do goto(target: {50, 50}, on: myself.network); }
		// the walker moves along the first edge, not straight to the target
		assert w.location = {10, 0};
		assert w.current_path != nil;
		assert length(w.current_path.edges) = 2;
		loop times: 4 { ask w { do goto(target: {50, 50}, on: myself.network); } }
		assert w.location = {50, 0};
		ask w { do goto(target: {50, 50}, on: myself.network); }
		assert w.location = {50, 10};
		// once arrived it stays there
		loop times: 20 { ask w { do goto(target: {50, 50}, on: myself.network); } }
		assert w.location = {50, 50};
		ask created { do die(); }
	}

	test "Following a path" {
		create walker(location:{0, 0}, speed:10.0) returns: created;
		walker w <- created[0];
		path route <- path_between(network, {0, 0}, {50, 50});
		ask w { do follow(path: route); }
		assert w.location = {10, 0};
		loop times: 20 { ask w { do follow(path: route); } }
		assert w.location = {50, 50};
		ask created { do die(); }
	}

	test "An agent outside of the graph is brought onto it" {
		create walker(location: {10, 10}, speed: 5.0) returns: created;
		walker w <- created[0];
		ask w { do goto(target: {50, 50}, on: myself.network); }
		// it is now on one of the edges
		assert network.edges one_matches (geometry(each) covers w.location);
		ask created { do die(); }
	}

	test "Wandering stays within its bounds" {
		seed <- 3.0;
		geometry bounds <- square(20) at_location {50, 50};
		create walker(location:{50, 50}, speed: 5.0) returns: created;
		walker w <- created[0];
		list<point> visited <- [];
		loop times: 200 {
			ask w { do wander(bounds: bounds); }
			visited <+ w.location;
		}
		assert visited all_match (bounds covers each);
		// it does move
		assert length(remove_duplicates(visited)) > 100;
		ask created { do die(); }
	}

	test "Wandering without bounds stays in the world" {
		seed <- 4.0;
		create walker(location: {5, 5}, speed: 5.0) returns: created;
		walker w <- created[0];
		list<point> visited <- [];
		loop times: 200 {
			ask w { do wander(); }
			visited <+ w.location;
		}
		assert visited all_match (world.shape covers each);
		ask created { do die(); }
	}

	test "The speed is the distance covered at each step" {
		seed <- 5.0;
		create walker(location:{50, 50}, speed:3.0) returns: created;
		walker w <- created[0];
		list<float> steps <- [];
		loop times: 20 {
			point before <- w.location;
			ask w { do wander(); }
			steps <+ before distance_to w.location;
		}
		assert steps all_match (each <= 3.0 + 1e-9);
		ask created { do die(); }
	}
}

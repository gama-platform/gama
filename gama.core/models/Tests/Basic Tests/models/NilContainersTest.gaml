/**
* Name: NilContainersTest
* Description: Explicit nil differs from omitted initialization for lists and maps (#765).
* Tags: list, map, nil, variable, test
*/

model NilContainersTestModel

global {
	list global_list;
	map global_map;
	list<int> global_typed_list;
	map<string,int> global_typed_map;
	list global_absent_list <- nil;
	map global_absent_map <- nil;
	list<int> global_absent_typed_list <- nil;
	map<string,int> global_absent_typed_map <- nil;
}

species holder {
	list values;
	map entries;
	list<int> typed_values;
	map<string,int> typed_entries;
	list absent_values <- nil;
	map absent_entries <- nil;
	list<int> absent_typed_values <- nil;
	map<string,int> absent_typed_entries <- nil;
}

class container_data {
	list<int> values;
	map<string,int> entries;
}

experiment NilContainersTest type: test autorun: true {
	list experiment_list;
	map experiment_map;
	list experiment_absent_list <- nil;
	map experiment_absent_map <- nil;

	test "Omitted local initializers create empty containers" {
		list l;
		map m;
		list<int> tl;
		map<string,int> tm;
		assert l = [] and l != nil and nil != l;
		assert m = map([]) and m != nil and nil != m;
		assert tl = [] and string(type_of(tl)) = "list<int>";
		assert tm = map([]) and string(type_of(tm)) = "map<string, int>";
	}

	test "Explicit local nil and casts preserve absence" {
		list l <- nil;
		map m <- nil;
		list<int> tl <- nil;
		map<string,int> tm <- nil;
		assert l = nil and nil = l;
		assert m = nil and nil = m;
		assert tl = nil and nil = tl;
		assert tm = nil and nil = tm;
		assert list(nil) = nil and nil = list(nil);
		assert map(nil) = nil and nil = map(nil);
		assert list<int>(nil) = nil;
		assert map<string,int>(nil) = nil;
		assert to_list(nil) = nil;
		assert ([] = nil) = false;
		assert (map([]) = nil) = false;
	}

	test "Local reassignment can clear and restore containers" {
		list l <- [1];
		map m <- ["a"::1];
		list<int> tl <- [1];
		map<string,int> tm <- ["a"::1];
		l <- nil;
		m <- nil;
		tl <- nil;
		tm <- nil;
		assert l = nil and m = nil and tl = nil and tm = nil;
		l <- [];
		m <- map([]);
		tl <- [];
		tm <- map([]);
		assert l != nil and m != nil and tl != nil and tm != nil;
		assert empty(l) and empty(m) and empty(tl) and empty(tm);
	}

	test "Global attributes distinguish omitted and explicit nil" {
		assert global_list = [] and global_list != nil;
		assert global_map = map([]) and global_map != nil;
		assert string(type_of(global_typed_list)) = "list<int>" and empty(global_typed_list);
		assert string(type_of(global_typed_map)) = "map<string, int>" and empty(global_typed_map);
		assert global_absent_list = nil and global_absent_map = nil;
		assert global_absent_typed_list = nil and global_absent_typed_map = nil;
		global_list <- nil;
		global_map <- nil;
		global_typed_list <- nil;
		global_typed_map <- nil;
		assert global_list = nil and global_map = nil;
		assert global_typed_list = nil and global_typed_map = nil;
	}

	test "Species defaults are independent between agents and attributes" {
		create holder number: 2 returns: aa;
		holder a <- aa[0];
		holder b <- aa[1];
		assert a.values = [] and a.entries = map([]);
		assert a.absent_values = nil and a.absent_entries = nil;
		assert a.absent_typed_values = nil and a.absent_typed_entries = nil;
		assert string(type_of(a.typed_values)) = "list<int>";
		assert string(type_of(a.typed_entries)) = "map<string, int>";
		add 1 to: a.values;
		add 2 to: a.typed_values;
		a.entries["a"] <- 1;
		a.typed_entries["b"] <- 2;
		assert b.values = [] and b.typed_values = [];
		assert b.entries = map([]) and b.typed_entries = map([]);
		assert a.values = [1] and a.typed_values = [2];
		assert a.entries = ["a"::1] and a.typed_entries = ["b"::2];
		a.values <- nil;
		a.entries <- nil;
		a.typed_values <- nil;
		a.typed_entries <- nil;
		assert a.values = nil and a.entries = nil;
		assert a.typed_values = nil and a.typed_entries = nil;
	}

	test "Repeated local declarations allocate fresh defaults" {
		list<list> lists <- [];
		list<map> maps <- [];
		loop times: 3 {
			list<int> l;
			map<string,int> m;
			assert l = [] and m = map([]);
			add 1 to: l;
			m["a"] <- 1;
			add l to: lists;
			add m to: maps;
		}
		add 2 to: lists[0];
		maps[0]["b"] <- 2;
		assert lists[1] = [1] and lists[2] = [1];
		assert maps[1] = ["a"::1] and maps[2] = ["a"::1];
	}

	test "Explicit nil supplied to agent creation is not an omitted initializer" {
		create holder (values: nil, entries: nil, typed_values: nil, typed_entries: nil) returns: aa;
		assert aa[0].values = nil and aa[0].entries = nil;
		assert aa[0].typed_values = nil and aa[0].typed_entries = nil;
	}

	test "Objects distinguish omitted and explicit nil constructor arguments" {
		container_data a <- container_data();
		container_data b <- container_data(values: nil, entries: nil);
		assert a.values = [] and a.entries = map([]);
		assert b.values = nil and b.entries = nil;
	}

	test "Experiment attributes distinguish omitted and explicit nil" {
		assert experiment_list = [] and experiment_map = map([]);
		assert experiment_absent_list = nil and experiment_absent_map = nil;
	}

	test "Nested containers and conditional expressions preserve nil" {
		list<list<int>> lists <- [nil, [1]];
		map<string,list<int>> entries <- ["a"::nil, "b"::[1]];
		assert lists[0] = nil and lists[1] = [1];
		assert entries["a"] = nil and entries["b"] = [1];
		bool absent <- true;
		list<int> l <- absent ? nil : [1];
		map<string,int> m <- absent ? nil : ["a"::1];
		assert l = nil and m = nil;
		assert (absent ? [1] : nil) = [1];
	}

	test "Indexing nil reports a GAML error, not a Java NPE" {
		list<int> l <- nil;
		map<string,int> m <- nil;
		string problem;
		try { int value <- l[0]; } catch { problem <- #current_error; }
		assert problem != nil;
		assert !(problem contains "Java error") label: problem;
		problem <- nil;
		try { int value <- m["a"]; } catch { problem <- #current_error; }
		assert problem != nil;
		assert !(problem contains "Java error") label: problem;
	}
}

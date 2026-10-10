/**
* Name: GeometryRobustnessTest
* Description: Feeds the geometry operators with absurd values (negative, null, infinite, not a number, degenerate).
* 	An invalid input may be rejected (with a GAML error, or a nil geometry), but not with an internal Java
* 	exception; when it is accepted, the measures of the result must be non-negative numbers.
* 	NB: to_squares, to_rectangles and points_on with a null or negative size exhaust the memory or never return,
* 	which is why these cases cannot be part of the tests
* Tags: robustness, geometry, spatial, test
*/

experiment GeometryRobustnessTest type: test autorun: true {

	bool handled(string problem) {
		return problem = nil ? true : !(problem contains "Java error");
	}

	/** A geometry is sane if it is nil (rejected) or if its measures are non-negative numbers */
	bool sane(geometry g) {
		return g = nil ? true : (is_number(g.area) and g.area >= 0 and is_number(g.perimeter) and g.perimeter >= 0);
	}

	test "Shapes with a negative size" {
		string problem <- nil;
		list<geometry> shapes <- [];
		try {
			shapes <- [circle(-5), square(-3), rectangle(-2, 3), triangle(-1), hexagon(-1)];
		} catch { problem <- #current_error; }
		assert handled(problem);
		assert shapes all_match sane(each);
	}

	test "A circle with an infinite radius" {
		string problem <- nil;
		geometry g <- nil;
		try { g <- circle(#infinity); } catch { problem <- #current_error; }
		assert handled(problem);
		assert sane(g);
	}

	test "A circle with a radius that is not a number" {
		string problem <- nil;
		geometry g <- nil;
		try { g <- circle(#nan); } catch { problem <- #current_error; }
		assert handled(problem);
		assert sane(g);
	}

	test "A square with an infinite side" {
		string problem <- nil;
		geometry g <- nil;
		try { g <- square(#infinity); } catch { problem <- #current_error; }
		assert handled(problem);
		assert sane(g);
	}

	test "A rectangle with a side that is not a number" {
		string problem <- nil;
		geometry g <- nil;
		try { g <- rectangle(#nan, 2); } catch { problem <- #current_error; }
		assert handled(problem);
		assert sane(g);
	}

	test "3D shapes with a negative size" {
		string problem <- nil;
		geometry ball <- nil;
		try { ball <- sphere(-1); } catch { problem <- #current_error; }
		assert handled(problem);
		problem <- nil;
		geometry dice <- nil;
		try { dice <- cube(-2); } catch { problem <- #current_error; }
		assert handled(problem);
		problem <- nil;
		float volume <- 0.0;
		try { volume <- box(-1, -1, -1).volume; } catch { problem <- #current_error; }
		assert handled(problem);
		assert volume >= 0;
	}

	test "Degenerate lists of points" {
		string problem <- nil;
		list<geometry> shapes <- [];
		try {
			shapes <- [line([]), line([{0, 0}]), polygon([]), polygon([{0, 0}]), polygon([{0, 0}, {1, 1}]), polyline([{0, 0}, {0, 0}])];
		} catch { problem <- #current_error; }
		assert handled(problem);
		assert shapes all_match sane(each);
		assert polygon([{0, 0}, {1, 1}]).area = 0.0;
	}

	test "A geometry cannot be moved to a location that is not a number" {
		string problem <- nil;
		geometry g <- nil;
		try { g <- circle(5) at_location {#nan, 0}; } catch { problem <- #current_error; }
		assert handled(problem);
		assert problem != nil or g = nil or is_number(g.location.x);
	}

	test "Scaling by absurd factors" {
		assert (square(10) scaled_by 0).area = 0.0;
		// a negative factor mirrors the geometry
		assert (square(10) scaled_by -1).area = 100.0;
		string problem <- nil;
		list<geometry> scaled <- [];
		try { scaled <- [square(10) scaled_by #infinity, square(10) scaled_by #nan]; } catch { problem <- #current_error; }
		assert handled(problem);
		assert scaled all_match sane(each);
	}

	test "Rotating by absurd angles" {
		assert (square(10) rotated_by 720).area = 100.0;
		assert (square(10) rotated_by -90).area = 100.0;
		string problem <- nil;
		list<geometry> rotated <- [];
		try { rotated <- [square(10) rotated_by #infinity, square(10) rotated_by #nan]; } catch { problem <- #current_error; }
		assert handled(problem);
		assert rotated all_match sane(each);
	}

	test "Buffers with absurd distances" {
		// a buffer that consumes the whole geometry gives nothing
		assert (square(10) reduced_by 100) = nil;
		string problem <- nil;
		list<geometry> buffered <- [];
		try { buffered <- [square(10) enlarged_by -100, square(10) + #infinity, square(10) + #nan]; } catch { problem <- #current_error; }
		assert handled(problem);
		assert buffered all_match sane(each);
	}

	test "Dividing a point by zero raises an error, like for numbers" {
		string problem <- nil;
		try { point p <- {1, 2} / 0; } catch { problem <- #current_error; }
		assert problem != nil;
		assert handled(problem);
	}

	test "Decomposing an empty geometry" {
		assert empty(to_triangles(square(0)));
		assert empty(skeletonize(circle(0)));
		assert empty(voronoi([]));
		assert empty(to_sub_geometries(square(10), []));
		// the sizes of the sub geometries cannot be negative
		string problem <- nil;
		list<geometry> parts <- [];
		try { parts <- to_sub_geometries(square(10), [-1.0, 2.0]); } catch { problem <- #current_error; }
		assert handled(problem);
		assert problem != nil or parts all_match sane(each);
	}
}

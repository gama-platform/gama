/**
* Name: Chart legend regression
* Description: Manual regression coverage for hidden, sparse and vertically oriented histogram labels.
* Tags: test, chart, legend
*/

model chart_legend_regression

experiment "Chart legend regression" type: gui {
	init {
		gama.pref_cast_container_contents <- true;
	}
	output {
		display "Legend cases" {
			chart "Initial data enabled by default" type: histogram {
				datalist [1, 2, 3, 4] legend: "initial values";
			}
		}
		display "Legend cases2" {
			chart "Initial data disabled" type: histogram include_init: false {
				datalist [1, 2, 3, 4] legend: "after first cycle";
			}
		}
		display "Legend cases3" {
			chart "No legend" type: histogram {
				datalist [1, 2, 3, 4] legend: false;
			}
		}
		display "Legend cases4" {
			chart "Nil legend" type: histogram {
				datalist [1, 2, 3, 4] legend: nil;
			}
		}
		display "Legend cases5" {
			chart "Sparse labels" type: histogram {
				datalist range(0, 256)
					legend: range(0, 256) collect (each mod 15 = 0 ? string(each) : nil);
			}
		}
		display "Legend cases6" {
			chart "Vertical legend" type: histogram legend_orientation: vertical {
				datalist [1, 2, 3, 4] legend: ["first", "second", "third", "fourth"];
			}
		}
	}
}

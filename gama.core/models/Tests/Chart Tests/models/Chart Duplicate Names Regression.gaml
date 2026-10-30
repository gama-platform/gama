/**
* Name: Chart duplicate names regression
* Description: Manual regression coverage for issue #785 (data and x labels identified by position, not by displayed text).
* Each chart must show two distinct series both named "data1" (red and blue) and, for category charts, three categories
* including two labelled "a".
* Tags: test, chart, histogram, pie, radar, legend
*/

model chart_duplicate_names_regression

global {
	list<string> cats <- ["a", "b", "a"];
}

experiment "Duplicate names" type: gui {
	output {
		display "Series" type: 2d {
			chart "Series: two lines named data1" type: series {
				data "data1" value: cycle color: #red;
				data "data1" value: 2 * cycle color: #blue;
			}
		}
		display "Histogram" type: 2d {
			chart "Histogram: two series data1, three bars per series (a, b, a)" type: histogram x_serie_labels: cats {
				data "data1" value: [1, 2, 3] color: #red;
				data "data1" value: [3, 2, 1] color: #blue;
			}
		}
		display "Pie" type: 2d {
			chart "Pie: two sections data1 (red and blue)" type: pie {
				data "data1" value: 10 color: #red;
				data "data1" value: 20 color: #blue;
			}
		}
		display "Radar" type: 2d {
			chart "Radar: two series data1, three axes (a, b, a)" type: radar x_serie_labels: cats {
				data "data1" value: [1, 2, 3] color: #red;
				data "data1" value: [3, 2, 1] color: #blue;
			}
		}
	}
}

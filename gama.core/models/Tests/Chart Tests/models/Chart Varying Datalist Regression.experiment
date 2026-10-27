/**
* Name: Chart varying datalist regression
* Description: Manual regression coverage for issues #801 (colors of a datalist whose length changes) and #783 (y2_tick_values_visible).
* Every bar of the histogram and radar must stay green, whatever the cycle. The y2 axis of the series must show no tick values.
* Tags: test, chart, histogram, color, axis
*/

model chart_varying_datalist_regression

global {
	list<list<float>> histo <- [[1.0, 2, 3, 4], [1, 2, 3, 4], [1], [1], [1, 2, 3, 4], [1, 2, 3, 4], [1, 2, 3, 4]];
	list<string> all_labels <- ["a", "b", "c", "d"];
	int id -> cycle mod length(histo);
	list<float> values -> histo[id];
	list<string> labels -> copy_between(all_labels, 0, length(values));
	list<rgb> colors -> list_with(length(values), #green);
}

experiment "Varying datalist" type: gui {
	output {
		display "Histogram" type: 2d {
			chart "Histogram: all bars must be green" type: histogram {
				datalist value: values legend:labels  color: colors;
			}
		}
		display "Radar" type: 2d {
			chart "Radar: all series must be green" type: radar {
				datalist value: values legend: labels color: colors; 
			}
		}
		display "Second axis" type: 2d {
			chart "Y2 tick values hidden, Y tick values visible" type: series y2_tick_values_visible: false {
				data "main" value: cycle color: #blue;
				data "second" value: cycle * cycle color: #red use_second_y_axis: true;
			}
		}
	}
}

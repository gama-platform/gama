/**
* Name: Chart dynamic body regression
* Description: Manual regression coverage for issue #403 (the body of charts is re-evaluated every cycle).
* - "Conditional series": "Instant" appears at cycle 2; "Even cycles" is frozen on odd cycles and continues on even ones.
* - "Dynamic legend": a new series is created every 10 cycles ("period 0", "period 1"...), the previous ones are frozen.
* - "Pie": section "big" only exists when cycle mod 20 >= 10; it disappears otherwise.
* - "Histogram": bars "bar 1" to "bar n" (heights 10 to 10n), n changing every 5 cycles.
* Tags: test, chart, series, pie, histogram, dynamic
*/

model chart_dynamic_body_regression

global {
	int nb -> 1 + (cycle div 5) mod 5;
}

experiment "Dynamic body" type: gui {
	output {
		display "Conditional series" type: 2d {
			chart "Conditional series" type: series {
				data "Always" value: cycle color: #black;
				if (cycle > 1) {
					data "Instant" value: 2 * cycle color: #blue;
				}
				if (even(cycle)) {
					data "Even cycles" value: 3 * cycle color: #red;
				}
			}
		}
		display "Dynamic legend" type: 2d {
			chart "Dynamic legend" type: series {
				data "period " + (cycle div 10) value: cycle mod 10;
			}
		}
		display "Pie" type: 2d {
			chart "Pie" type: pie {
				data "small" value: 10 color: #green;
				if (cycle mod 20 >= 10) {
					data "big" value: 30 color: #orange;
				}
			}
		}
		display "Histogram" type: 2d {
			chart "Histogram: 1 to 5 bars, changing every 5 cycles" type: histogram {
				loop i from: 1 to: nb {
					data "bar " + i value: i * 10;
				}
			}
		}
	}
}

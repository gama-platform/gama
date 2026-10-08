/**
* Name: Long Series Chart Examples
* Author: Philippe Caillou
* Description: Demonstrates GAMA's ability to render series charts with a very large number of data points
*   (1000+ steps) without performance degradation. The chart plots sine, cosine, and ramp functions over long
*   time series. Use this model to test chart rendering performance and to understand how GAMA handles
*   memory and display for charts that accumulate data over extended simulation runs. Also shows how to
*   control whether history is kept or a sliding window is used.
* Tags: gui, chart, series, performance, long, time_series, visualization, output
*/
model long_series

global {
	int serie_length <- 1000;
	list<float> xlist <- [];
	list<float> coslist <- [];
	list<float> sinlist <- [];
	float base;

	reflex update_sinchart {
		loop i from: 0 to: serie_length {
			base <- float(serie_length * cycle + i);
			add base to: xlist;
			add cos(base / 1000) to: coslist;
			add sin(base / 1000) to: sinlist;
		}

	}

}

experiment "Long series" type: gui {
	output synchronized: true {
		display "long_series" type: 2d {
			chart "Long series values" type: series x_label: "#points to draw at each step" memorize: false {
				data "Cosinus" value: coslist color: #blue marker: false style: line;
				data "Sinus" value: sinlist color: #red marker: false style: line;
			}

		}

	}

}

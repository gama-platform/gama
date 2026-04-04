/**
* Name: Displays - Continuous and Phase Portrait (Lotka-Volterra)
* Author: Tri Nguyen-Huu, Huynh Quang Nghi
* Description: Illustrates two advanced chart display types for ODE results. Standard ABM charts show one
*   value per cycle (discrete). EBM/ODE solvers compute a continuous sub-step solution; this model shows
*   how to use 'type: series' with continuous curves and 'type: xy' for phase-portrait plots (predator
*   vs prey). The phase portrait reveals the periodic orbits characteristic of Lotka-Volterra dynamics.
* Tags: equation, math, ODE, lotka_volterra, display, phase_portrait, continuous, chart, visualization
*/
 
 
model Displays


global {	
	init {
		create LV_model with: (x:2.0, y:2.0);
	}
}

species LV_model { 
	float t;
	float x;
	float y;
	float h <- 0.1;
	float alpha <- 0.8;
	float beta <- 0.3;
	float gamma <- 0.2;
	float delta <- 0.85;
	
	equation eqLV {
		diff(x, t) = x * (alpha - beta * y);
		diff(y, t) = -y * (delta - gamma * x);
	}

	reflex solving {
		solve eqLV method: #rk4 step_size: h;
	}
}

experiment Displays type: gui {
	float minimum_cycle_duration <- 0.1#s;
	output {
		layout #split tabs: true;
		display D1 toolbar: false type: 2d {
			chart 'Time series' type: series background: rgb(47,47,47) color: #white y_label:"pop" x_tick_line_visible: false{
				data "x" value: first(LV_model).x color: rgb(52,152,219);
				data "y" value: first(LV_model).y color: rgb(41,128,185);
			}
		}
		display D2 toolbar: false type: 2d {
			chart 'Time series - continuous display' type: series background:  rgb(47,47,47) color: #white y_label:"pop" x_tick_line_visible: false{
		//	chart 'Time series - continuous display' type: series x_serie: first(LV_model).t[] background: #white y_label:"pop"{
				data "x" value: first(LV_model).x[] color: rgb(52,152,219) marker: false;
				data "y" value: first(LV_model).y[] color: rgb(41,128,185) marker: false;
			}
		}
		display D3 title: "Phase Portrait " toolbar: false  type: 2d {
			chart 'Phase Portrait' type: xy background:  rgb(47,47,47) color: #white x_label: "x" y_label:"y"{
				// Continuous display requires to pass a list of two values x and y
				data "y(x(t))" value: [first(LV_model).x,first(LV_model).y] color: rgb(243,156,18);
			}
		}
		display D4 title: "Phase Portrait - continuous display" toolbar: false type: 2d {
			chart 'Phase Portrait - continuous display' type: xy background:  rgb(47,47,47) color: #white x_label: "x" y_label:"y"{
				// Continuous display requires to pass a list of two values x and y
				data "y(x(t))" value: rows_list(matrix(first(LV_model).x[],first(LV_model).y[])) color: rgb(243,156,18) marker: false;
			}
		}

	}

}
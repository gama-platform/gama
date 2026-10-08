/***
* Name: Influence of the Integration Method (Lotka-Volterra)
* Author: Tri Nguyen-Huu, Huynh Quang Nghi, Benoit Gaudou
* Description: Compares two ODE integration methods on the Lotka-Volterra model: Runge-Kutta 4 (RK4) and
*   Euler forward. With a deliberately coarse step of h=0.1, Euler accumulates errors that cause unbounded
*   (physically wrong) population trajectories, while RK4 preserves the expected periodic orbits. Side-by-side
*   time series and phase portrait displays make the divergence visible over time.
* Tags: equation, math, ODE, lotka_volterra, runge_kutta, euler, integration_method, numerical_accuracy
***/

model LVInfluenceoftheIntegrationMethod

global {
	init {
		create LVRK4(x:2.0, y:2.0);
		create LVEuler(x:2.0, y:2.0);
	}

}

species LVRK4 {
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


species LVEuler {
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
		solve eqLV method: #Euler step_size: h;    
	}
	
	
	
	reflex end_simulation when: cycle > 126{
		ask world{do pause();}
	}

}

experiment examples type: gui {
	float minimum_cycle_duration <- 0.1#s;
	output {
		layout #split tabs: true;
		display LV_series title: "Time series" toolbar: false  type: 2d {
			chart 'Comparison Euler - RK4 (RK4 is more accurate)' type: series 
			x_serie: first(LVRK4).t[] y_label: "pop" background: rgb(47,47,47) color: #white x_tick_line_visible: false {
				data "x (rk4)" value: first(LVRK4).x[] color: rgb(52,152,219) marker: false thickness: 2;
				data "y (rk4)" value: first(LVRK4).y[] color: rgb(41,128,185) marker: false thickness: 2;
				data "x (Euler)" value: first(LVEuler).x[] color: rgb(243,156,18) marker: false thickness: 2;
				data "y (Euler)" value: first(LVEuler).y[] color: rgb(230,126,34) marker: false thickness: 2;
			}

		}
		display LV_phase_portrait title: "Phase portrait" toolbar: false  type: 2d {
			chart 'Comparison Euler - RK4 (RK4 is more accurate)' type: xy 
			background: rgb(47,47,47) color: #white x_label: "x" y_label: "y" x_tick_line_visible: false y_tick_line_visible: false{
				data "y(x(t)) rk4" value: rows_list(matrix(first(LVRK4).x[],first(LVRK4).y[])) color: rgb(52,152,219) marker: false thickness: 2;
				data "y(x(t)) Euler" value: rows_list(matrix(first(LVEuler).x[],first(LVEuler).y[])) color: rgb(243,156,18) marker: false thickness: 2;
			}

		}

	}

}
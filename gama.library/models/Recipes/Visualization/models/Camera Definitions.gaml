/**
* Name: Camera Definitions
* Author: Arnaud Grignard, Alexis Drogoul
* Description: Shows how to define and programmatically control cameras in a 3D GAMA display. Cameras are
*   defined by their position, target (look-at point), and up-vector. This model demonstrates: setting a
*   fixed camera position, animating the camera along a path, switching between multiple named cameras, and
*   using orthographic vs. perspective projections. Camera control is essential for creating cinematic
*   presentations, fixed-view analyses, and interactive 3D displays.
* Tags: 3d, camera, perspective, orthographic, animation, visualization, display
*/
model camera_locationition

global {

	init {
		create objects;
	}

}

species objects skills: [moving] {

	reflex move {
		do wander(amplitude: 20.0, speed: 1.0);
	}

	aspect default {
		draw sphere(5) at: location color: #white border: #gray;
	}

}

experiment Display type: gui autorun: true {
	point p -> first(objects).location;
	float factor <- 1.0;
	//parameter "Shared zoom" of: experiment var: factor min: 0.01 max: 10.0;
	float minimum_cycle_duration <- 0.01;
	output {
		float w <- shape.width; 
		float h <- shape.height;

		layout #split parameters: false consoles: false;
		display shared type: 3d virtual: true {
			overlay background: #transparent{
				ui_slider "Zoom" var: experiment.factor min: 0.01 max: 10.0 continuous: true;
			}
			picture "../includes/wood.jpg";
			species objects;
		}
		display "Changing every 500" parent: shared camera: [#from_up_front, #from_up_left, #from_up_right, #from_above, #from_front, #from_left, #from_right] at ((cycle / 500) mod 7) {
		}
		display "Fixed location" parent: shared camera: "fixed" {
			camera "fixed" locked: true location: {w / 2, h * 2, w / experiment.factor} target: {w / 2, h / 2, 0} dynamic: true;
		}
		display "Dynamic location" parent: shared camera: #default {
			camera #default location: {w * cos(cycle), w * sin(cycle), w / experiment.factor} target: {w / 2, h / 2, 0} dynamic: true;
		}
		display "Follow object" parent: shared {
			camera #default target: experiment.p distance: 150 / experiment.factor location: #from_above dynamic: true;
		}
		display "First person" type: opengl{
			picture "../includes/wood.jpg";
			camera #default dynamic: true location: {int(first(objects).location.x), int(first(objects).location.y), 5/experiment.factor} target:
			{cos(first(objects).heading) * first(objects).speed + int(first(objects).location.x), sin(first(objects).heading) * first(objects).speed + int(first(objects).location.y), 5/experiment.factor};
		}
		display "Camera & rotation" parent: shared {
			rotation angle: 1.0 axis: {0,1,0} dynamic: true;
			camera #default location: #from_right distance: 40 / experiment.factor target: {w, experiment.p.y, 0} dynamic: true;
		}
		display "Isometric" parent: shared {
			camera #default location: #isometric distance: 40 / experiment.factor target: {experiment.p.x, experiment.p.y, 0} dynamic: true;
		}


	}

} 
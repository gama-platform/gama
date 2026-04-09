/**
* Name: Event Layer
* Author: Arnaud Grignard, Patrick Taillandier, Jean-Daniel Zucker
* Description: Demonstrates how to use the 'event' display layer to trigger actions in response to user
*   interactions in a GAMA display. Two display experiments are provided: one that changes an agent's color
*   when the user clicks on it, and one that changes the agent's shape. The event layer listens for mouse
*   clicks and keyboard presses and calls the associated action on the targeted agent. This is the primary
*   mechanism for building interactive simulations where users can manipulate agents directly in the display.
* Tags: gui, event, mouse_click, interaction, display, user_input
*/
model event_layer_model


global
{
	int nbAgent <- 200;
	int radius <- 10;
	dummy pointClicked;

	init {
		create cell number: nbAgent
		{
			color <- #darkgreen;
		}
       create dummy(dummyRadius : radius) number:1 returns: temp ;
       pointClicked <- first(temp);
   }
	action change_color ()
	{
		list selected_agents <- cell overlapping (circle(radius) at_location #user_location);
		ask selected_agents
		{
			self.color <- self.color = #lightgreen ? #darkgreen : #lightgreen;
		}

	}

	action draw_clicked_area_in_view_color()
	{
		pointClicked.location <- #user_location;
		pointClicked.visibleViewColor <- true;
	}
	action draw_clicked_area_in_view_shape()
	{
		pointClicked.location <- #user_location;
		pointClicked.visibleViewShape <- true;
	}

	action hide_clicked_area()
	{
		pointClicked.visibleViewColor <- false;
		pointClicked.visibleViewShape <- false;
	}

	action change_shape ()
	{
		list<cell> selected_agents <- cell overlapping (circle(radius) at_location #user_location);
		ask selected_agents
		{
			is_square <- not (is_square);
		}

	}

}

species cell skills: [moving]
{
	rgb color;
	bool is_square <- false;
	reflex mm
	{
		do wander(amplitude: 30.0);
	}

	aspect default
	{
		draw is_square ? square(2) : circle(1) color: color;
	}

}

species dummy  {
	int dummyRadius <- 10;
	bool visibleViewColor <- false;
	bool visibleViewShape <- false;
	
	aspect aspect4ViewChangeColor {
		if visibleViewColor {draw circle(radius) color: #grey;}
	}
	
	aspect aspect4ViewChangeShape {
		if visibleViewShape {draw circle(radius) color: #grey;}
	}
	
}



experiment Displays type: gui
{
	float minimum_cycle_duration <-0.01;
	parameter "Radius of selection" var: radius ;	// The radius of the disk around the click 
	output synchronized:true
	{    
		layout horizontal([0::5000,1::5000]) tabs:true editors: false;
		display View_change_color type: 2d
		{
			species cell;
			species dummy transparency:0.9 aspect: aspect4ViewChangeColor;
			// event, launches the action change_color if the event mouse_down (ie. the user clicks on the layer event) is triggered
			// the action can be either in the experiment or in the global section. If it is defined in both, the one in the experiment will be chosen in priority
			event #mouse_down {ask simulation {do change_color();}}  
			event #mouse_move {ask simulation {do draw_clicked_area_in_view_color();}} 
			event #mouse_exit {ask simulation {do hide_clicked_area();}}
			// Shows how to manipulate the cells using keyboard events
			event #arrow_left {ask (cell) {location <- location - {1,0};}}
			event #arrow_right {ask (cell) {location <- location + {1,0};}}
			event #arrow_up {ask (cell) {location <- location - {0,1};}}
			event #arrow_down {ask (cell) {location <- location + {0,1};}}
			event #escape {ask (cell) {location <- rnd(point(100,100));}}
			
		}

		display View_change_shape type: 3d
		{
			light #ambient active: false;
			species cell;
			species dummy transparency:0.9 aspect: aspect4ViewChangeShape ;
			//event, launches the action change_shape if the event mouse_down (ie. the user clicks on the layer event) is triggered
			// The block is executed in the context of the experiment, so we have to ask the simulation to do it. 
			event #mouse_down {ask simulation {do change_shape();}}  
			event #mouse_move {ask simulation {do draw_clicked_area_in_view_shape();}} 
			event #mouse_exit {ask simulation {do hide_clicked_area();}} 
			// Shows how to manipulate the cells using keyboard events, disactivating the "regular" arrow / esc keys behavior
			event #arrow_left {ask (cell) {location <- location - {1,0};}}
			event #arrow_right {ask (cell) {location <- location + {1,0};}}
			event #arrow_up {ask (cell) {location <- location - {0,1};}}
			event #arrow_down {ask (cell) {location <- location + {0,1};}}
			event #escape {ask (cell) {location <- rnd(point(100,100));}}
		}

	}

}


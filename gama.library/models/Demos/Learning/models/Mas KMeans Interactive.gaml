/***
* Name: K-Means Interactive
* Author: Jean-Daniel Zucker
* Description: An interactive extension of the K-Means Clustering model that lets users place data points and
*   initial centroid positions manually using the event layer and mouse clicks. Select Points on the right
*   panel and click on the left display to place data points. Switch to Centers to place initial centroids:
*   their count determines the number of clusters. The running simulation continuously updates assignments.
*   Use Erase to remove a nearby point or center. The impact
*   of initial centroid placement on the final result can be explored interactively.
* Tags: machine_learning, clustering, k_means, interactive, user_interaction, event, algorithm
***/

model MASKmeansInteractive
import "Mas KMeans.gaml"

global {

	//current action type
	int action_type <- 0;
	list<point> clicks <- [];
	
	list<string> tool_names <- ["POINTS", "CENTERS", "ERASE", "INSPECT"];
	

	
	reflex pauseAtConvergence when: converged { }
	
	action activate_act() {
		button selected_but <- first(button overlapping (circle(1) at_location #user_location));
		if(selected_but != nil) {
			ask selected_but {
				if (action_type != id) {
					action_type<-id;
				} else {
					action_type<- -1;
				}
				
			}
		}
	}
	
	// We register the clicks asynchronously (when the user presses the mouse button)
	// No need to register the type of action as the simulation is supposed to run fast, so that 
	// the user doesnt have the time, in one step, to click two times and change the action type in-between.
	// See issue #3626 on GAMA platform Github
	action register_click() {
		clicks << #user_location;
	}
	
	// .. and we manage the points synchronously, in the normal scheduling of the model. The worst that can happen is 
	// to miss one click if the user clicks very fast. 
	reflex manage_clicks {
		loop p over: clicks {
			do cell_management(p);
		}
		clicks <- [];
	}
	
	action cell_management(point the_location) {
		//write agents;
		switch action_type {
			match 0 {
				create datapoints (location : the_location);
			}
			
			match 1 {
				create centroids (location : the_location);
				int K <- length(centroids);
				loop i from:0 to: K-1 { ask centroids[i] { color_kmeans <- cluster_palette[i mod length(cluster_palette)]; }}
		   }
				
			match 2 {
				list<agent> close_ag <- (datapoints+centroids) overlapping (circle(5) at_location the_location);
				if not empty(close_ag) {
					ask close_ag closest_to the_location {
						if (self is datapoints ) {
							centroids c <- datapoints(self).mycenter;
							if (c != nil) {
								c.mypoints >> self;
							}
						} else {
							ask centroids(self).mypoints {
								mycenter <- nil;
								color_kmeans <- rgb(158,183,197);
							}
						}
						do die();
					}
				}
				
			}
				
			
		}
	}

}


grid button width:2 height:2 
{
	int id <- int(self);
	aspect normal {
		rgb accent <- action_type = id ? cluster_palette[0] : rgb(100, 122, 145);
		draw rectangle(shape.width * 0.82, shape.height * 0.68) color: rgb(29, 41, 58) border: accent;
		point icon_position <- location - {0, 4};
		switch id {
			match 0 { draw circle(3) at: icon_position color: accent; }
			match 1 {
				draw circle(5) at: icon_position color: accent wireframe: true width: 2;
				draw cross(6, 1.4) at: icon_position color: lettering;
			}
			match 2 {
				draw line([icon_position - {4, 4}, icon_position + {4, 4}]) color: accent width: 2;
				draw line([icon_position - {4, -4}, icon_position + {4, -4}]) color: accent width: 2;
			}
			match 3 {
				draw circle(4) at: icon_position color: accent wireframe: true width: 2;
			}
		}
		draw tool_names[id] at: location + {0, 9} anchor: {0.5, 0.5}
			color: lettering font: font("Helvetica", 11, #bold);
	}
}

// To avoid displaying experiments coming from the inherited model MAS_KMEANS
experiment clustering2D type: gui virtual: true;
experiment clustering3D type: gui virtual: true;

experiment SelectPoints2Cluster2D type: gui autorun: true{
	parameter "Show assignment links" var: show_links category: "Appearance";
	output {
		layout horizontal([0.0::8000,1::2000]) tabs:true;
		
		display map type:2d background:backdrop {
			event #mouse_down {ask simulation {do register_click();}}
			species centroids aspect: kmeans_aspect2D;
			species datapoints aspect: kmeans_aspect2D;
			overlay position: {16#px, 16#px} size: {340#px, 118#px} background: backdrop rounded: true {
				draw "K-MEANS / CLUSTER LAB" at: {14#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
				draw (action_type >= 0 ? "Tool: " + tool_names[action_type] : "Select a tool on the right")
					at: {14#px, 49#px} color: cluster_palette[0] font: font("Helvetica", 11);
				draw string(length(datapoints)) + " points / " + string(length(centroids)) + " centers"
					at: {14#px, 75#px} color: lettering font: font("Helvetica", 11);
				draw (empty(centroids) ? "Place points, then add cluster centers" : "Within-cluster distance: " + string(with_precision(globalIntraDistance, 1)))
					at: {14#px, 101#px} color: lettering font: font("Helvetica", 11);
			}
		}
		//display the action buttons
		display action_buton background:backdrop title:"Clustering tools" type:2d	{
			species button aspect:normal ;
			event #mouse_down {ask simulation {do activate_act();}}   
		}

	}
}

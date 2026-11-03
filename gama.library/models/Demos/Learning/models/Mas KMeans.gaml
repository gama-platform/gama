/**
* Name: Agent-Based K-Means Clustering
* Author: Jean-Daniel Zucker, Patrick Taillandier, Arnaud Grignard
* Description: A step-by-step visual demonstration of the K-means clustering algorithm implemented as a
*   multi-agent system. Data points are represented as agents, and cluster centroids are also agents.
*   At each simulation step the algorithm alternates between: (1) assigning each point agent to the nearest
*   centroid; and (2) updating each centroid to the mean position of its assigned points. The model visualizes
*   convergence with colored points, highlighted centers, optional assignment links, and a phase legend.
*   Both planar and spatial experiments are available. See https://en.wikipedia.org/wiki/K-means_clustering for
*   the algorithm background.
* Tags: machine_learning, clustering, k_means, algorithm, visualization, multi_agent
*/
model MASKMEANS


global
{
// the number of classes to create (kmeans)
// It corresponds to the centroids
	int k ;
	// the number of points
	int N ;
	//number of dimensions
	int dimensions <- 2;
	float globalIntraDistance <- 0.0;
	bool converged <- false;
	rgb backdrop <- rgb(19, 27, 40);
	rgb lettering <- rgb(220, 230, 240);
	list<rgb> cluster_palette <- [rgb(69, 211, 183), rgb(255, 184, 107), rgb(112, 163, 255),
		rgb(222, 135, 205), rgb(188, 215, 111), rgb(179, 153, 245),
		rgb(255, 133, 139), rgb(109, 211, 231), rgb(222, 206, 155), rgb(158, 183, 197)];
	bool show_links <- true;
	init
	{
		//create datapoints agents
		create datapoints number: N
		{
			if (dimensions = 3)
			{
				location <- { rnd(100), rnd(100), rnd(100) };
			}

			if (dimensions = 2)
			{
				location <- { rnd(100), rnd(100) };
			}

		}
		//create centroid agents
		create centroids number: k
		{
			if (dimensions = 3)
			{
				location <- { rnd(100), rnd(100), rnd(100) };
			}

			if (dimensions = 2)
			{
				location <- { rnd(100), rnd(100) };
			}

		}
		int K <- length(centroids);
		if (K > 0) {loop i from:0 to: K-1 { ask centroids[i] { color_kmeans <- cluster_palette[i mod length(cluster_palette)]; }}}
	}
	
	reflex pauseAtConvergence when: converged { 
		do pause();
	}
	
	reflex assign_points_to_centroid when: even(cycle)
	{
	    // The "assignment" step is also referred to as expectation step,
		ask centroids
		{
			mypoints <- list<datapoints> ([]);
		}

		loop pt over: datapoints
		{
			ask pt
			{
				if not empty(centroids) {
					mycenter <- centroids closest_to self;
					color_kmeans <- mycenter.color_kmeans;
					add self to: mycenter.mypoints;
				}
			}

		}

	}

	reflex update_centroids when: not even(cycle)
	{
	// the "update step" as maximization step,
	// making this algorithm a variant of the generalized expectation-maximization algorithm.

		ask centroids where (not empty(each.mypoints))
		{
			location <- mean(mypoints collect each.location);
			float oldist <- myIntraDistance;
			myIntraDistance <- mypoints sum_of (each distance_to self);
			converged <- (oldist-myIntraDistance) with_precision(2) = 0;
		}
		
		globalIntraDistance <- centroids sum_of (each.myIntraDistance);
	}

}

species datapoints
{
	rgb color_kmeans <- rgb(158, 183, 197);
	centroids mycenter;
	aspect kmeans_aspect2D
	{
		draw circle(1.7) color: rgb(color_kmeans, 0.12);
		draw circle(0.75) color: color_kmeans border: rgb(lettering, 0.35);
	}

	aspect kmeans_aspect3D
	{
		draw sphere(0.85) color: color_kmeans;
	}

}

species centroids
{
	rgb color_kmeans <- rgb(158, 183, 197);
	list<datapoints> mypoints;
	float myIntraDistance <- 0.0;
	aspect kmeans_aspect2D
	{
		// explicitly loops over a copy of the points to avoid concurrency issues with the simulation
		if (show_links) {
			loop pt over: copy(mypoints) {
				draw line([location, pt.location]) color: rgb(color_kmeans, 0.18);
			}
		}
		draw circle(2.8) color: rgb(color_kmeans, 0.15);
		draw circle(2.1) color: color_kmeans wireframe: true width: 1.5;
		draw cross(2.8, 0.65) color: lettering border: backdrop;
	}

	aspect kmeans_aspect3D
	{
		if (show_links) {
			loop pt over: copy(mypoints) {
				draw line([location, pt.location]) color: rgb(color_kmeans, 0.25);
			}
		}
		draw sphere(2.5) color: color_kmeans;
		draw cube(3.2) color: lettering wireframe: true;
		
	}

}

experiment "Clustering 2D" type: gui
{
	parameter "Number of clusters" var: k init:4 min:1 max:10 category: "Clustering";
	parameter "Number of points" var: N init:500 min:1 category: "Clustering";
	parameter "Show assignment links" var: show_links category: "Appearance";
	output
	{
		
		display map_kmeans type:2d background:backdrop {
			species centroids aspect: kmeans_aspect2D;
			species datapoints aspect: kmeans_aspect2D;
			overlay position: {16#px, 16#px} size: {340#px, 118#px} background: backdrop rounded: true {
				draw "K-MEANS / CLUSTERING" at: {14#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
				draw (converged ? "Converged / paused" : (even(cycle) ? "Next: assign points to nearest center" : "Next: move centers to cluster means"))
					at: {14#px, 49#px} color: cluster_palette[0] font: font("Helvetica", 11);
				draw "Within-cluster distance: " + string(with_precision(globalIntraDistance, 1))
					at: {14#px, 75#px} color: lettering font: font("Helvetica", 11);
				draw "Dots: data / Crosses: cluster centers" at: {14#px, 101#px} color: lettering font: font("Helvetica", 11);
			}
		}

	}
}

experiment "Clustering 3D" type: gui 
{
	parameter "Number of clusters to split the data into" var: k init:4 min: 0 max: 10 category: "KMEANS";
	parameter "Number of points to be clustered" var: N init:1000 ;
	parameter "Number of dimensions (2D or 3D)" var: dimensions init: 3 min: 2 max: 3;
	parameter "Show assignment links" var: show_links category: "Appearance";
	
	// The display is explicitly synchronized to avoid concurrency issues (if the points are changed in the simulation while being displayed)
	output synchronized: true
	{
		display map_kmeans type: 3d background:backdrop axes:false
		{
			species centroids aspect: kmeans_aspect3D;
			species datapoints aspect: kmeans_aspect3D;
			overlay position: {16#px, 16#px} size: {320#px, 90#px} background: backdrop rounded: true {
				draw "K-MEANS / SPATIAL CLUSTERS" at: {14#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
				draw (converged ? "Converged / paused" : "Assign points, then recenter clusters")
					at: {14#px, 49#px} color: cluster_palette[0] font: font("Helvetica", 11);
				draw "Large markers: centers / Drag to orbit" at: {14#px, 75#px} color: lettering font: font("Helvetica", 11);
			}
		}

	}

}

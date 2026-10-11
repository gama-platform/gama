/**
* Name: Spatial Clustering by Distance
* Author: Patrick Taillandier, Jean-Daniel Zucker
* Description: Demonstrates distance-based spatial clustering using the 'simple_clustering_by_distance'
*   operator. Two agents are grouped into the same cluster if the distance between them is below a
*   configurable threshold. The resulting clusters are displayed as convex hulls connecting cluster members,
*   making the groupings visually clear. Also works on grid cells. Useful for identifying spatial hot-spots,
*   contact networks, or proximity-based social groups.
* Tags: clustering, statistics, spatial, distance, simple_clustering, grid, convex_hull, analysis
*/

model clustering

global {
	//define the maximal distance between people in the continuous environement (in meters): if the distance between 2 people is lower than this value, they will be in the same group
	float max_dist_people <- 20.0;
	
	//define the maximal distance between cells (in number of cells): if the distance between 2 cells is lower than this value, they will be in the same group
	int max_dist_cell <- 1;
	
	//probability for a cell to have vegetation
	float proba_vegetation <- 0.2;
	
	//create the people agents
	init {
		create people number:20; 
    }
    
    //reflex that builds the people clusters
    reflex people_clustering {
    	//clustering by using the simple clustering operator: two people agents are in the same groups if their distance is lower than max_dist_people (in meters)
    	//returns a list of lists (i.e. a list of groups, a group is a list of people agents)
    	list<list<people>> clusters <- list<list<people>>(simple_clustering_by_distance(people, max_dist_people));
        
        
        //We give a random color to each group (i.e. to each people agents of the group)
        loop cluster over: clusters {
        	rgb rnd_color <- rnd_color(255);
        	ask cluster {
        		color_cluster <- rnd_color;
        	}
        }
        
        ask group_people {do die();}
        //build the hierchical clustering (https://en.wikipedia.org/wiki/Hierarchical_clustering)
        list clustering_tree <- hierarchical_clustering (people, max_dist_people);
        
        //create groups from the results of the hierarchical clustering
        do create_groups(clustering_tree, nil);
    }
    
   // The action now returns the created group_people, making recursion easier
	action create_groups(list group_list, group_people parent_gp) type: group_people {
	    
	    // 1. Create the current node
	    create group_people returns: created_nodes {
	        parent <- parent_gp;
	        if (parent_gp != nil) {
	            add self to: parent_gp.sub_groups;
	        }
	    }
	    group_people current_gp <- first(created_nodes);
	
	    list<point> children_locations <- [];
	
	    // 2. Iterate over the group elements (there are exactly 2 thanks to your binary tree Java code)
	    loop el over: group_list {
	        if (el is people) {
	            // It's a tree leaf (an individual)
	            add people(el).location to: children_locations;
	            
	        } else if (el is list) {
	            // It's a branch (a sub-list), make the recursive call!
	            group_people child_gp <- create_groups(el as list, current_gp);
	            add child_gp.location to: children_locations;
	        }
	    }
	
	    // 3. Compute the geometry and location of this group
	    if (length(children_locations) = 2) {
	        // The group is placed exactly in the middle of its two children
	        current_gp.location <- mean(children_locations);
	        // Its shape is a line connecting its two children
	        current_gp.shape <- polyline(children_locations);
	        
	    } else if (length(children_locations) = 1) {
	        // Fallback just in case a group has only one child
	        current_gp.location <- first(children_locations);
	    }
	
	    // Return the created group so its own parent can connect to it
	    return current_gp;
	}
    
    //reflex that builds the cell clusters
    reflex forest_clustering {
    	list<list<vegetation_cell>> clusters <- list<list<vegetation_cell>>(simple_clustering_by_distance(vegetation_cell where (each.color = #green), max_dist_cell));
        loop cluster over: clusters {
        	create forest {
        		cells <- cluster;
        		shape <- union (cells); 
        	}
        }
        list clustering_tree <- hierarchical_clustering (people, max_dist_people);
    }
    
}
grid vegetation_cell width: 25 height: 25 neighbors: 4{
	rgb color <- flip (proba_vegetation) ? #green : #white;
}

species forest {
	list<vegetation_cell> cells;
	aspect default {
		draw shape.contour + 0.5 color: #red;
	}
}

species people {
	rgb color_cluster <- #black;
	rgb color_tree <- #black;
	aspect cluster {
		draw circle(2) color: color_cluster;
	}
	aspect tree {
		draw circle(2) color: color_tree;
	}
}

species group_people {
	list<group_people> sub_groups;
	group_people parent;
	aspect default {
		draw shape + 0.2 color: #red;
		if (parent != nil) {
			draw line ([location, parent.location]) end_arrow: 2 color: #red;
		}
	}
}

experiment clustering type: gui {
	parameter "Maximal distance for people clustering" var: max_dist_people min: 0.0 max: 100.0 category: "People";
	parameter "Maximal distance for vegetation cell clustering" var: max_dist_cell min: 0 max: 5 category: "Forest";
	parameter "Probability for vegetation cells" var: proba_vegetation min: 0.1 max: 1.0 category: "Forest";
	
	//permanent layout: horizontal([vertical([0::5000,1::5000])::5000,2::5000]) tabs:true;
	output {
		layout horizontal([vertical([0::5000,1::5000])::5000,2::5000]) tabs:true editors: false;
		display map_people_clusters {
			species people aspect: cluster;
		}
		display map_people_tree {
			species people aspect: tree;
			species group_people;
		}
		display map_forest_clusters type:2d antialias:false{
			grid vegetation_cell border: #black;
			species forest;
		}
	}
}

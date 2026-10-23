/***
* Name: Mirror Species
* Author: Gama Development Team
* Description: A simple introduction to mirror species in GAML. A mirror species is a species whose agents
*   each automatically follow (mirror) an agent of another species. Mirror agents can provide an alternative
*   visual representation of the mirrored agents — for instance showing them in a different display, with a
*   different shape, color, or position. This model demonstrates the basic declaration of a mirror species
*   using the 'mirrors' facet and shows how mirror agents track their target's position.
* Tags: mirror, display, visualization, species, representation
***/

model Mirrorsimple

global {
	
	int neigh_distance <- 10;
	
  	init{
    	create A number:100;    
  	}
}

species A skills:[moving] {
    reflex update{
        do wander();
    }
    aspect base{
        draw circle(1) color: #white border: #black;
    }
}
species B mirrors: A {
    point location <- target.location update: {target.location.x,target.location.y,target.location.z+5};    
   	list<B> neigh <- [] update: B at_distance neigh_distance;
   	
    aspect base {
        draw sphere(2) color: #blue;
        loop n over: neigh {
        	draw line(location, n.location) color: #black;
        }
    }
}

experiment mirroExp type: gui {
    output {
        display superposedView type: 3d{ 
          species A aspect: base;
          species B aspect: base transparency:0.5;
        }
    }
}

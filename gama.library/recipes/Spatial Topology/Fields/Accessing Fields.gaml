/**
* Name: Accessing Fields
* Author: Alexis Drogoul
* Description: Shows how to create GAMA fields (continuous raster-like data structures) and read/write their
*   values. One agent increments field values at its location each step; another agent decrements them. Values
*   are displayed as a colored gradient. This model covers: field creation with specified dimensions, reading
*   cell values at a point with 'field_value_at', writing values, and displaying the field as a color map.
*   Fields are more memory-efficient than grid species for large continuous spatial data.
* Tags: field, spatial, raster, read, write, continuous, visualization, display
*/
model AccessingFields

global torus: true{
	geometry shape <- square(2000);
	field terrain <- field(300, 300);

	init {
		create increaser(location:location);
		create decreaser(location:location);
	}

	species increaser skills: [moving] {
		geometry shape <- square(40);

		reflex move {
			do wander(amplitude: 2.0);
			loop s over: terrain cells_in self {
				terrain[geometry(s).location] <- terrain[geometry(s).location] + 1.0;
			}

		}
	}

	species decreaser skills: [moving] {
		geometry shape <- square(40);

		reflex move {
			do wander(amplitude: 2.0);
			loop s over: terrain cells_in self {
				terrain[geometry(s).location] <- terrain[geometry(s).location] - 1.0;
			}

		}

	}

}

experiment "Show" {
	list<rgb> palette <- brewer_colors(any(brewer_palettes(0)));
	output {
		display Field type: 3d {
			mesh terrain color: palette triangulation: true smooth: 4;
		}

	}

}

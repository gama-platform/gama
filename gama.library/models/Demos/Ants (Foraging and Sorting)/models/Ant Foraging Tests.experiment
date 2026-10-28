/**
* Name: Ant Foraging Tests
* Description: Checks that ants perceive pheromone trails before the grid evaporates them.
* Tags: ants, foraging, pheromone, scheduling, test
*/
model ant_foraging_tests

import "Ant Foraging.gaml"

experiment Trail_timing title: "Trail timing" type: test {
	test "Ants detect weak trails before evaporation" {
		ask ant { do die(); }
		ask ant_grid {
			road <- 0.0;
			food <- 0;
		}
		ant_grid trail <- ant_grid({gridsize - 10.0, gridsize / 2.0});
		trail.road <- 1.0;
		ant a <- ant(location: trail.location, speed: 0.0) ;
		ask simulation { do _step_(); }
		assert a.state = "followingRoad";
		assert trail.road = 0.0;
	}

	test "Fresh deposits evaporate during the same cycle" {
		ask ant { do die(); }
		ask ant_grid {
			road <- 0.0;
			food <- 0;
		}
		ant_grid trail <- ant_grid({gridsize - 10.0, gridsize / 2.0});
		create ant (location: trail.location, speed: 0.0, has_food: true);
		ask simulation { do _step_(); }
		assert trail.road = 100.0 - evaporation_per_cycle;
	}

	test "Evaporation respects the grid update frequency" {
		ask ant { do die(); }
		ask ant_grid { road <- 0.0; }
		grid_frequency <- 2;
		diffusion_rate <- 0.0;
		ant_grid trail <- ant_grid({gridsize - 10.0, gridsize / 2.0});
		trail.road <- 100.0;
		loop times: 3 {
			float previous <- trail.road;
			bool grid_updates <- (simulation.cycle mod grid_frequency) = 0;
			ask simulation { do _step_(); }
			assert trail.road = previous - (grid_updates ? evaporation_per_cycle : 0.0);
		}
		grid_frequency <- 1;
		diffusion_rate <- 1.0;
	}
}

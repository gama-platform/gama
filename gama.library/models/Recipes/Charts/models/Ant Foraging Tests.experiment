/**
* Name: Ant Foraging Charts Tests
* Description: Checks that ants perceive pheromone trails before the grid evaporates them.
* Tags: ants, foraging, pheromone, scheduling, test
*/
model ant_foraging_charts_tests

import "Ant Foraging (Charts examples).gaml"

experiment "Trail timing" type: test {
	test "Ants detect weak trails before evaporation" {
		ask ant { do die(); }
		ask ant_grid {
			road <- 0.0;
			food <- 0;
		}
		ant_grid trail <- ant_grid({gridsize - 10.0, gridsize / 2.0});
		trail.road <- 1.0;
		ant a <- ant (location: trail.location, speed: 0.0);
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
		create ant (location: trail.location, speed: 0.0, hasFood: true);
		ask simulation { do _step_(); }
		assert trail.road = 100.0 - evaporation_per_cycle;
	}
}

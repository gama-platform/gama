/***
* Name: BDI Tutorial - Step 1 - Skeleton Gold Miner
* Author: Mathieu Bourgais
* Description: First step of the BDI Gold Miner tutorial. Sets up the model skeleton: a market, mine
*   sites, and miner agents that move randomly without any BDI logic yet. Introduces the basic species
*   structure (market, mine, miner) and the environment used throughout all five tutorial steps. This is
*   the starting point before adding BDI plans, social links, emotions, and norms.
* Tags: simple_bdi, tutorial, gold_miner, species, skeleton, architecture
***/

model BDItutorial1

global {
	int nb_mines <- 10; 
	market the_market;
	geometry shape <- square(20 #km);
	float step <- 10#mn;	
	
	init {
		create market {
			the_market <- self;
		}
		create gold_mine number: nb_mines;
	}
}

species gold_mine {
	int quantity <- rnd(1,20);
	aspect default {
		draw triangle(200 + quantity * 50) color: (quantity > 0) ? #yellow : #gray border: #black;	
	}
}

species market {
	int golds;
	aspect default {
	  draw square(1000) color: #black ;
	}
}

experiment GoldBdi type: gui {

	output {
		display map type: 3d {
			species market ;
			species gold_mine ;
		}
	}
}

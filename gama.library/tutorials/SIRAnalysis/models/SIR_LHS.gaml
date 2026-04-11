model SIR

global {
    // Parameters
    int    nb_agents          <- 200  min: 50   max: 500;
    int    nb_infected_init   <- 5    min: 1    max: 50;
    float  infection_rate     <- 0.5  min: 0.0  max: 1.0;
    int    infection_distance <- 5    min: 1    max: 20;
    int    recovery_time      <- 50   min: 10   max: 200;

    // Output metrics
    int nb_susceptible <- 0;
    int nb_infected    <- 0;
    int nb_recovered   <- 0;

    init {
        create person number: nb_agents;
        ask nb_infected_init among (person as list) {
            status <- "infected";
            infection_timer <- recovery_time;
        }
    }

    reflex update_counts {
        nb_susceptible <- person count (each.status = "susceptible");
        nb_infected    <- person count (each.status = "infected");
        nb_recovered   <- person count (each.status = "recovered");
    }

    reflex stop_gui when: cycle >= 1000 or nb_infected = 0 {
        do halt;
    }
}

species person skills: [moving] {
    string status <- "susceptible";
    int infection_timer <- 0;
    float speed <- 1.0;

    reflex move {
        do wander();
    }

    reflex infect when: status = "infected" {
        ask (person at_distance infection_distance) where (each.status = "susceptible") {
            if flip(infection_rate) {
                status <- "infected";
                infection_timer <- recovery_time;
            }
        }
        infection_timer <- infection_timer - 1;
        if infection_timer <= 0 {
            status <- "recovered";
        }
    }

    aspect base {
        draw circle(1) at: location color:
    		(status = "infected") ? #red : ((status = "recovered") ? #blue : #green);
    }
}

// ── GUI experiment ─────────────────────────────────────────────────────────────
experiment SIR_gui type: gui {
    parameter "Number of agents"    var: nb_agents;
    parameter "Initially infected"  var: nb_infected_init;
    parameter "Infection rate"      var: infection_rate;
    parameter "Infection distance"  var: infection_distance;
    parameter "Recovery time"       var: recovery_time;

    output {
        display "Population" type: java2D {
            species person aspect: base;
        }
        display "SIR Chart" type: java2D {
            chart "SIR dynamics" type: series {
                data "Susceptible" value: nb_susceptible color: #green;
                data "Infected"    value: nb_infected    color: #red;
                data "Recovered"   value: nb_recovered   color: #blue;
            }
        }
        monitor "Susceptible" value: nb_susceptible;
        monitor "Infected"    value: nb_infected;
        monitor "Recovered"   value: nb_recovered;
        monitor "Cycle"       value: cycle;
    }
}

// ── Batch experiment with Latin Hypercube Sampling ─────────────────────────────
experiment SIR_LHS type: batch repeat: 1 keep_seed: false
    until: (cycle >= 1000 or nb_infected = 0) {

    parameter "Number of agents"    var: nb_agents          min: 50  max: 500;
    parameter "Initially infected"  var: nb_infected_init   min: 1   max: 50;
    parameter "Infection rate"      var: infection_rate     min: 0.0 max: 1.0;
    parameter "Infection distance"  var: infection_distance min: 1   max: 20;
    parameter "Recovery time"       var: recovery_time      min: 10  max: 200;

    method exploration sampling: "latinhypercube" sample: 100;

    reflex save_results {
        ask simulations {
            save [
                int(self),
                self.nb_agents,
                self.nb_infected_init,
                self.infection_rate,
                self.infection_distance,
                self.recovery_time,
                self.nb_recovered,
                self.cycle
            ]
            to:      "../results/lhs_results.csv"
            rewrite: false
            header:  true;
        }
    }
}
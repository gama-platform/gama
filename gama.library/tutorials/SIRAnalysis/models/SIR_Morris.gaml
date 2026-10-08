/**
 * SIR Model — Morris Sensitivity Analysis
 *
 * Morris method (OAT - One At a Time):
 *   For k parameters and r trajectories → r*(k+1) simulation runs
 *   Here: 10 trajectories * (5 params + 1) = 60 runs
 *
 * Three indices computed per parameter:
 *   mu      → mean of elementary effects (signed influence)
 *   mu_star → mean of |elementary effects| (overall importance, robust to non-monotonicity)
 *   sigma   → std of elementary effects (non-linearity / interaction with other params)
 *
 * Output files:
 *   morris_raw.csv    → one row per simulation run (parameter values + output)
 *   morris_report.txt → mu, mu_star, sigma per parameter (the sensitivity indices)
 */

model SIR_Morris

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
            status        <- "infected";
            infection_timer <- recovery_time;
        }
    }

    reflex update_counts {
        nb_susceptible <- person count (each.status = "susceptible");
        nb_infected    <- person count (each.status = "infected");
        nb_recovered   <- person count (each.status = "recovered");
    }

    // halt (not pause) so the batch scheduler is never blocked
    reflex stop when: cycle >= 1000 or nb_infected = 0 {
        do die();
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
                status          <- "infected";
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

// ── GUI experiment (for manual inspection) ────────────────────────────────────
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

// ── Morris batch experiment ────────────────────────────────────────────────────
//
//  Total runs = repeat * (nb_params + 1) * sample
//             = 1      * (5      + 1) * 50
//             = 300 runs
//
//  Increase `sample` (number of trajectories) for more robust estimates.
//  levels: number of grid levels for the OAT design (default 4, must be even).
//
experiment SIR_Morris type: batch
    repeat: 1
    keep_seed: false
    until: (cycle >= 1000 or nb_infected = 0) {

    parameter "Number of agents"    var: nb_agents          min: 50  max: 500;
    parameter "Initially infected"  var: nb_infected_init   min: 1   max: 50;
    parameter "Infection rate"      var: infection_rate     min: 0.0 max: 1.0;
    parameter "Infection distance"  var: infection_distance min: 1   max: 20;
    parameter "Recovery time"       var: recovery_time      min: 10  max: 200;

    // outputs : global variables to track as sensitivity targets
    // report  : aggregated Morris indices (mu, mu_star, sigma) — use .csv for machine-readable output
    // results : raw run-by-run data (parameter values + output per simulation)
    // levels  : number of grid levels in the OAT design (4 is standard)
    // sample  : number of trajectories r — total runs = r * (k+1)
    method morris
        outputs:  ["nb_recovered"]
        report:   "../results/morris_report.txt"
        results:  "../results/morris_raw.csv"
        levels:   10
        sample:   50;
}

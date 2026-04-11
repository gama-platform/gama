/**
 * SIR Model — Sobol Sensitivity Analysis
 *
 * Sobol method (variance-based global sensitivity analysis):
 *   Based on Saltelli (2002) — https://doi.org/10.1016/S0010-4655(02)00280-1
 *   Implemented via the MOEAFramework library (GPL).
 *
 * Sampling strategy (Saltelli scheme):
 *   Two independent base matrices A and B of size (sample × k) are drawn.
 *   k+2 matrices are derived from them → total runs = sample × (2×k + 2)
 *   Here: 100 × (2×5 + 2) = 100 × 12 = 1200 runs
 *
 *   Increasing `sample` improves index precision but scales linearly with cost.
 *   Recommended minimum: sample >= 500 for robust estimates (see Saltelli 2010).
 *   Here sample: 100 is kept small for quick testing — raise to 500+ for publication.
 *
 * Three indices computed per parameter:
 *   S1  → first-order index: direct contribution of parameter Xi to output variance
 *          ignoring all interactions. Sum of all S1 ≈ 1 for additive models.
 *   ST  → total-order index: contribution including ALL interactions involving Xi.
 *          ST >= S1 always. ST - S1 quantifies the interaction share.
 *   S2  → second-order index (pairwise): contribution of the Xi×Xj interaction
 *          beyond their individual S1 effects. Not always output by GAMA.
 *
 * Output files:
 *   sobol_raw.csv    → one row per simulation run (parameter values + output)
 *   sobol_report.csv → S1, ST (and S2 if available) per parameter
 *
 * NOTE: The report file may not be generated depending on your GAMA version due
 *       to a known bug in the exploration methods post-processing step.
 *       See: https://github.com/gama-platform/gama/issues
 *       The raw file should always be produced and can be used to recompute
 *       indices manually in the companion notebook using SALib.
 */

model SIR_Sobol

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
            status          <- "infected";
            infection_timer <- recovery_time;
        }
    }

    reflex update_counts {
        nb_susceptible <- person count (each.status = "susceptible");
        nb_infected    <- person count (each.status = "infected");
        nb_recovered   <- person count (each.status = "recovered");
    }

    // halt (not pause) — never blocks the batch scheduler
    reflex stop when: cycle >= 1000 or nb_infected = 0 {
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

// ── Sobol batch experiment ─────────────────────────────────────────────────────
//
//  Total runs = sample × (2×k + 2)
//             = 50    × (2×5 + 2)
//             = 600 runs
//
//  For robust S1/ST estimates, raise sample to 500 or 1000.
//  The Saltelli (2010) rule of thumb: sample >= 500 for k <= 10 parameters.
//
experiment SIR_Sobol type: batch
    repeat: 1
    keep_seed: false
    until: (cycle >= 1000 or nb_infected = 0) {

    parameter "Number of agents"    var: nb_agents          min: 50  max: 500;
    parameter "Initially infected"  var: nb_infected_init   min: 1   max: 50;
    parameter "Infection rate"      var: infection_rate     min: 0.0 max: 1.0;
    parameter "Infection distance"  var: infection_distance min: 1   max: 20;
    parameter "Recovery time"       var: recovery_time      min: 10  max: 200;

    // outputs : global variables to use as sensitivity targets
    // sample  : N in the Saltelli scheme — total runs = N × (2k+2)
    // report  : aggregated Sobol indices (S1, ST) — use .csv for machine-readable
    // results : raw run-by-run data (parameter values + output per simulation)
    method sobol
        outputs: ["nb_recovered"]
        sample:  20
        report:  "../results/sobol_report.csv"
        results: "../results/sobol_raw.csv";
}

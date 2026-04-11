/**
 * SIR Model — Beta^d Sensitivity Analysis
 *
 * Based on: Baucells & Borgonovo (2013), doi: 10.1007/s10588-021-09358-5
 *
 * WHAT BETAD MEASURES
 * -------------------
 * Unlike Sobol (variance-based) and Morris (elementary effects), betad is
 * DISTRIBUTION-based. It measures how much fixing one parameter shifts the
 * entire output distribution, not just its variance.
 *
 * Formally, the Beta^d index for parameter Xi is:
 *
 *   betad_i = E[ max|F(Y) - F(Y|Xi)| ]
 *
 * where F(Y) is the unconditional CDF of the output and F(Y|Xi) is the
 * CDF conditioned on a fixed value of Xi. The expectation is taken over
 * the distribution of Xi.
 *
 * This is the expected Kolmogorov-Smirnov distance between the
 * unconditional and conditional output distributions.
 *
 * WHY THIS MATTERS
 * ----------------
 * - Captures tail effects and distributional shifts invisible to variance
 * - Includes ALL interactions automatically (no S1/ST split needed)
 * - Robust to non-monotonic and non-linear models
 * - Complements Sobol and Morris: a parameter may have low S1 but high
 *   betad if it causes a distributional shift without changing the mean
 *
 * SAMPLING
 * --------
 * betad has no dedicated sampling algorithm. We use orthogonal sampling
 * (a Latin-square variant that ensures uniform marginal coverage).
 * Available: latinhypercube, orthogonal, uniform, factorial, saltelli, morris
 *
 * bootstrap: number of resamples for confidence interval estimation
 *            (set to 0 or omit to skip — increases run count significantly)
 *
 * TOTAL RUNS = sample = 200
 *
 * OUTPUT FILES
 * ------------
 *   betad_report.txt  — Beta^d index per parameter (human-readable)
 *   betad_raw.csv     — one row per run: parameter values + nb_recovered
 */

model SIR_Betad

global {
    int    nb_agents          <- 200  min: 50   max: 500;
    int    nb_infected_init   <- 5    min: 1    max: 50;
    float  infection_rate     <- 0.5  min: 0.0  max: 1.0;
    int    infection_distance <- 5    min: 1    max: 20;
    int    recovery_time      <- 50   min: 10   max: 200;

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

// ── Beta^d batch experiment ────────────────────────────────────────────────────
//
//  sample   : number of parameter points — total runs = sample
//  sampling : orthogonal gives uniform marginal coverage (recommended for betad)
//  bootstrap: confidence interval resamples — omit or set to 0 to skip
//
experiment SIR_Betad type: batch
    repeat:    1
    keep_seed: false
    until: (cycle >= 1000 or nb_infected = 0) {

    parameter "Number of agents"    var: nb_agents          min: 50  max: 500;
    parameter "Initially infected"  var: nb_infected_init   min: 1   max: 50;
    parameter "Infection rate"      var: infection_rate     min: 0.0 max: 1.0;
    parameter "Infection distance"  var: infection_distance min: 1   max: 20;
    parameter "Recovery time"       var: recovery_time      min: 10  max: 200;

    method betad
        outputs:  ["nb_recovered"]
        sample:   6
        sampling: orthogonal
        report:   "../results/betad_report.txt"
        results:  "../results/betad_raw.csv";
}

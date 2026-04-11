/**
 * SIR Model — Stochastic Analysis (stochanalyse)
 *
 * PURPOSE
 * -------
 * stochanalyse answers ONE question before any sensitivity analysis:
 *   "How many simulation replications do I need to get robust results?"
 *
 * It measures the contribution of the random seed to output variability.
 * If stochasticity dominates, adding more replications is necessary before
 * interpreting Sobol or Morris indices — otherwise those indices reflect
 * noise rather than parameter contributions.
 *
 * HOW IT WORKS
 * ------------
 * For each of the `sample` randomly drawn parameter points in the space:
 *   → run the simulation `repeat` times (different seeds)
 *   → compute correlation and error indices for 2 replicas, then 3, 4 ... repeat
 * Three indices are reported:
 *
 *   Correlation index  — average Pearson correlation between pairs of
 *                        replicate outputs. High value (→1) means replicas
 *                        agree → few replications needed.
 *
 *   CV index           — coefficient of variation across replicas.
 *                        Low value (→0) means low stochastic variability.
 *
 *   Neyman-Pearson     — estimates the minimum number of replications needed
 *                        to avoid Type I (false positive) and Type II
 *                        (false negative) errors at given confidence levels.
 *
 * TOTAL RUNS = sample × repeat = 10 × 20 = 200 runs
 *
 * OUTPUT
 * ------
 *   stoch_report.txt  — human-readable: indices per number of replicas
 *   stoch_raw.csv     — raw run data (parameter values + output per run)
 *
 * NOTE: Same potential report-file bug as Morris — if report is missing,
 *       the raw CSV still lets you compute CV manually.
 */

model SIR_Stochanalyse

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

// ── Stochastic Analysis experiment ────────────────────────────────────────────
//
//  repeat : number of replications per parameter point (m)
//           stochanalyse tests from 2 up to repeat replicas
//           → raise this to get a finer curve (e.g. repeat: 30)
//
//  sample : number of random parameter points to test (n)
//           each point is run repeat times with different seeds
//           → raise this for more robust index estimates (e.g. sample: 20)
//
//  Total runs = repeat × sample = 25 × 100 = 2500
//
experiment SIR_Stochanalyse type: batch
    repeat:     25
    keep_seed:  false
    until: (cycle >= 1000 or nb_infected = 0) {

    parameter "Number of agents"    var: nb_agents          min: 50  max: 500;
    parameter "Initially infected"  var: nb_infected_init   min: 1   max: 50;
    parameter "Infection rate"      var: infection_rate     min: 0.0 max: 1.0;
    parameter "Infection distance"  var: infection_distance min: 1   max: 20;
    parameter "Recovery time"       var: recovery_time      min: 10  max: 200;

    // outputs : variables to measure stochastic contribution on
    // report  : .txt → human-readable report showing indices vs nb of replicas
    // results : raw CSV with one row per run
    // sample  : number of parameter space points to probe
    method stochanalyse
        outputs: ["nb_recovered"]
        report:  "../results/stoch_report.txt"
        results: "../results/stoch_raw.csv"
        sampling: uniform
        sample:  100;

    // ── Permanent display: visible in GAMA after all runs complete ─────────────
    // Shows the distribution of nb_recovered across all replications,
    // which gives immediate visual intuition of stochastic spread.
    permanent {

        display "Stochastic spread" type: java2D {
            chart "nb_recovered distribution across all runs" type: histogram
                  background: #white {
                data "nb_recovered"
                    value: simulations collect each.nb_recovered
                    color: #steelblue;
            }
        }

    }
}

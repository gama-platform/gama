/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    PARTICLE LIFE SIMULATION                             ║
 * ║                                                                          ║
 * ║  Inspired by : OfficialCodeNoodles/Particle-Life-Simulation (Godot)     ║
 * ║                & hunar4321/particle-life                                 ║
 * ║                                                                          ║
 * ║  Compatible GAMA 2025-06        nb_types fixed at 5                     ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

model ParticleLife

global {

    // ════════════════════════════════════════════════════════════════════════
    // GENERAL PARAMETERS
    // ════════════════════════════════════════════════════════════════════════
    
    // World size
    geometry shape <- envelope(50, 50);

    // Number of types fixed at 5 to expose the 25 cells as sliders.
    int nb_types <- 5;

    /**
     * nb_particles  [int] - Total number of particles created at startup.
     * Distributed evenly among the 5 types (80 per type by default).
     * Recommended range : 50 - 5000
     */
    int nb_particles <- 2000;

    /**
     * max_radius  [float] - Maximum interaction radius (world units).
     * Only particles within this radius exert a force.
     * Recommended range : 5.0 - 20.0
     */
    float max_radius <- 5.0;

    /**
     * min_radius  [float] - Minimum repulsion radius (world units).
     * Below this distance, a universal repulsion prevents overlap.
     * Must be < max_radius. Recommended range : 0.2 - 2.0
     */
    float min_radius <- 1.5;

    /**
     * friction  [float] - Kinetic friction coefficient.
     * v <- v x friction at each step. 0.5=very dissipative, 0.99=near-conservative.
     * Recommended range : 0.1 - 0.5
     */
    float friction <- 0.17;

    /**
     * force_scale  [float] - Global force scaling factor.
     * Amplifies or attenuates all interactions simultaneously.
     * Recommended range : 0.1 - 0.8
     */
    float force_scale <- 0.3;

    /**
     * dt  [float] - Euler integration time step.
     * Stability rule : dt x force_scale < 0.99
     * Recommended range : 0.001 - 0.99
     */
    float dt <- 0.5;

    /**
     * wrap_borders  [bool] - Border handling mode.
     * true  -> toroidal (Pac-Man) : full spatial continuity.
     * false -> elastic bounce off walls.
     */
    bool wrap_borders <- true;

    /**
     * density_limit  [float] - Local density threshold above which
     * attraction is attenuated.
     *
     * Principle (inspired by the GPU shader of the source project) :
     *   local_density = sum over neighbors within max_radius :
     *     (1 - dist/max_radius)       if same type
     *     (1 - dist/max_radius) x 0.5 if different type
     *
     *   density_factor = 1 - clamp(local_density - density_limit, 0, 1)
     *   effective_attraction = attraction x density_factor
     *
     * Effect :
     *   low  (e.g. 1.0)  -> attraction cancels as soon as a small group forms :
     *                        thin membranes, diffuse structures
     *   high (e.g. 10.0) -> density has almost no effect : dense, compact
     *                        clusters (classic behaviour)
     *
     * Default value : 3.0
     * Recommended range : 0.5 - 50.0
     */
    float density_limit <- 20.0;

    // ════════════════════════════════════════════════════════════════════════
    // ATTRACTION MATRIX CELLS  (editable from the interface)
    //
    // Naming convention : A_i_j = force exerted by type j ON type i
    //   Value  +1.0 -> type i is strongly ATTRACTED toward type j
    //   Value   0.0 -> indifference
    //   Value  -1.0 -> type i strongly FLEES type j
    //
    // Types :  0=Red  1=Green  2=Blue  3=Yellow  4=Purple
    // ════════════════════════════════════════════════════════════════════════

    // Row 0 : how Red reacts to others
    float A_0_0 <- 0.0;
    float A_0_1 <- 0.0;
    float A_0_2 <- 0.0;
    float A_0_3 <- 0.0;
    float A_0_4 <- 0.0;

    // Row 1 : how Green reacts to others
    float A_1_0 <- 0.0;
    float A_1_1 <- 0.0;
    float A_1_2 <- 0.0;
    float A_1_3 <- 0.0;
    float A_1_4 <- 0.0;

    // Row 2 : how Blue reacts to others
    float A_2_0 <- 0.0;
    float A_2_1 <- 0.0;
    float A_2_2 <- 0.0;
    float A_2_3 <- 0.0;
    float A_2_4 <- 0.0;

    // Row 3 : how Yellow reacts to others
    float A_3_0 <- 0.0;
    float A_3_1 <- 0.0;
    float A_3_2 <- 0.0;
    float A_3_3 <- 0.0;
    float A_3_4 <- 0.0;

    // Row 4 : how Purple reacts to others
    float A_4_0 <- 0.0;
    float A_4_1 <- 0.0;
    float A_4_2 <- 0.0;
    float A_4_3 <- 0.0;
    float A_4_4 <- 0.0;

    // ════════════════════════════════════════════════════════════════════════
    // INTERNAL DATA
    // ════════════════════════════════════════════════════════════════════════

    matrix<float> attraction_matrix;

    list<rgb> type_colors <- [
        rgb(255, 80,  80),
        rgb(80,  200, 80),
        rgb(80,  120, 255),
        rgb(255, 220, 60),
        rgb(220, 80,  220)
    ];

    // ════════════════════════════════════════════════════════════════════════
    // INITIALISATION
    // ════════════════════════════════════════════════════════════════════════

    init {
        write "
╔══════════════════════════════════════════════════════════════════════════╗
║                    PARTICLE LIFE SIMULATION                             ║
║                                                                          ║
║  Inspired by : OfficialCodeNoodles/Particle-Life-Simulation (Godot)     ║
║                & hunar4321/particle-life                                 ║
║                                                                          ║
║  Principle :                                                             ║
║    Particles of N different types (distinguished by colour)              ║
║    interact via an attraction/repulsion matrix A[i][j].                  ║
║    Each particle is subject to the forces exerted by its neighbours      ║
║    within a given radius, and moves according to simple Euler            ║
║    integration. Complex emergent behaviours (swarms, spirals, cells)     ║
║    arise from these minimal local rules.                                 ║
║                                                                          ║
║  Physical model :                                                        ║
║    - Zone [0, min_radius[          : hard repulsion (anti-overlap)       ║
║    - Zone [min_radius, max_radius] : force from attraction_matrix[i,j]  ║
║    - Beyond max_radius             : no interaction                      ║
║    - Update rule :                                                       ║
║        v(t+1) = v(t) x friction + F x force_scale x dt                  ║
║        x(t+1) = x(t) + v(t+1) x dt                                      ║
║                                                                          ║
║  EDITABLE MATRIX FROM THE INTERFACE :                                    ║
║    Each cell A_i_j is exposed as a slider in the category               ║
║    'Matrix [Colour]' of the Parameters panel. Values range from -1.0    ║
║    (max repulsion) to +1.0 (max attraction). The internal matrix is      ║
║    synchronised each cycle via the sync_matrix reflex.                   ║
╚══════════════════════════════════════════════════════════════════════════╝
        ";

        do sync_matrix_from_vars();
        loop t from: 0 to: nb_types - 1 {
            create particle number: int(nb_particles / nb_types) {
                ptype    <- t;
                color    <- type_colors[t];
                location <- {rnd(world.shape.width), rnd(world.shape.height)};
                velocity <- {0.0, 0.0};
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ACTIONS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * sync_matrix_from_vars - Copies the 25 A_i_j variables into the internal matrix.
     * Called automatically each cycle by the sync_matrix reflex,
     * allowing slider changes to take effect immediately during the simulation.
     */
    action sync_matrix_from_vars() {
        attraction_matrix <- 0.0 as_matrix {nb_types, nb_types};
        attraction_matrix[0,0] <- A_0_0; attraction_matrix[0,1] <- A_0_1;
        attraction_matrix[0,2] <- A_0_2; attraction_matrix[0,3] <- A_0_3;
        attraction_matrix[0,4] <- A_0_4;
        attraction_matrix[1,0] <- A_1_0; attraction_matrix[1,1] <- A_1_1;
        attraction_matrix[1,2] <- A_1_2; attraction_matrix[1,3] <- A_1_3;
        attraction_matrix[1,4] <- A_1_4;
        attraction_matrix[2,0] <- A_2_0; attraction_matrix[2,1] <- A_2_1;
        attraction_matrix[2,2] <- A_2_2; attraction_matrix[2,3] <- A_2_3;
        attraction_matrix[2,4] <- A_2_4;
        attraction_matrix[3,0] <- A_3_0; attraction_matrix[3,1] <- A_3_1;
        attraction_matrix[3,2] <- A_3_2; attraction_matrix[3,3] <- A_3_3;
        attraction_matrix[3,4] <- A_3_4;
        attraction_matrix[4,0] <- A_4_0; attraction_matrix[4,1] <- A_4_1;
        attraction_matrix[4,2] <- A_4_2; attraction_matrix[4,3] <- A_4_3;
        attraction_matrix[4,4] <- A_4_4;
    }


    // ════════════════════════════════════════════════════════════════════════
    // GLOBAL REFLEXES
    // ════════════════════════════════════════════════════════════════════════

    // Synchronises the internal matrix from the sliders each cycle.
    // Allows modifications to be seen immediately.
    reflex sync_matrix {
        do sync_matrix_from_vars();
    }
	
	// Force and movement computation. "parallel" should be set to the
	// maximum number of available CPU cores.
    reflex update_all {
        ask particle parallel: true { do compute_force(); }
        ask particle parallel: true { do move(); }
    }
}

// ── Particle species ──────────────────────────────────────────────────────────
species particle {

    int   ptype;
    rgb   color;
    point velocity;
    point force_acc;
    // Local density computed each cycle (pass 1 of compute_force)
    float local_density;

    action compute_force() {
        force_acc     <- {0.0, 0.0};
        local_density <- 0.0;
        list<particle> neighbors <- particle at_distance max_radius;

        // ── Pass 1 : local density computation ────────────────────────────
        //   same type      -> full contribution  (1 - dist/max_radius)
        //   different type -> reduced contribution (x 0.5)
        loop other over: neighbors {
            if other != self {
                float dx <- other.location.x - location.x;
                float dy <- other.location.y - location.y;
                if wrap_borders {
                    if dx >  world.shape.width  / 2.0 { dx <- dx - world.shape.width;  }
                    if dx < -world.shape.width  / 2.0 { dx <- dx + world.shape.width;  }
                    if dy >  world.shape.height / 2.0 { dy <- dy - world.shape.height; }
                    if dy < -world.shape.height / 2.0 { dy <- dy + world.shape.height; }
                }
                float dist <- sqrt(dx * dx + dy * dy);
                if dist > 0 and dist < max_radius {
                    float contrib <- 1.0 - dist / max_radius;
                    if (density_limit != 0) {
	                    if other.ptype = ptype {
	                        local_density <- local_density + contrib;
	                    } else {
	                        local_density <- local_density + contrib * 0.5;
	                    } 
	                }
                }
            }
        }

        // ── Pass 2 : force computation with density attenuation ───────────
        loop other over: neighbors {
            if other != self {
                float dx <- other.location.x - location.x;
                float dy <- other.location.y - location.y;
                if wrap_borders {
                    if dx >  world.shape.width  / 2.0 { dx <- dx - world.shape.width;  }
                    if dx < -world.shape.width  / 2.0 { dx <- dx + world.shape.width;  }
                    if dy >  world.shape.height / 2.0 { dy <- dy - world.shape.height; }
                    if dy < -world.shape.height / 2.0 { dy <- dy + world.shape.height; }
                }
                float dist <- sqrt(dx * dx + dy * dy);
                if dist > 0 and dist < max_radius {
                    float fx <- 0.0;
                    float fy <- 0.0;
                    if dist < min_radius {
                        // Hard repulsion zone : prevents particle overlap
                        float repulsion <- (min_radius - dist) / min_radius;
                        fx <- -repulsion * (dx / dist);
                        fy <- -repulsion * (dy / dist);
                    } else {
                        float g <- attraction_matrix[ptype, other.ptype];
                        // Attenuate attraction when local density exceeds density_limit
                        if g > 0.0 and density_limit != 0{
                            float excess         <- max(0.0, local_density - density_limit);
                            float density_factor <- 1.0 - min(excess, 1.0);
                            g <- g * density_factor;
                        }
                        float norm_dist <- (dist - min_radius) / (max_radius - min_radius);
                        float strength  <- g * (1.0 - norm_dist);
                        fx <- strength * (dx / dist);
                        fy <- strength * (dy / dist);
                    }
                    force_acc <- {force_acc.x + fx, force_acc.y + fy};
                }
            }
        }
    }

    action move() {
        float new_vx <- velocity.x * friction + force_acc.x * force_scale * dt;
        float new_vy <- velocity.y * friction + force_acc.y * force_scale * dt;
        velocity <- {new_vx, new_vy};
        float new_x <- location.x + velocity.x * dt;
        float new_y <- location.y + velocity.y * dt;
        if wrap_borders {
            if new_x < 0                   { new_x <- new_x + world.shape.width;  }
            if new_x >= world.shape.width  { new_x <- new_x - world.shape.width;  }
            if new_y < 0                   { new_y <- new_y + world.shape.height; }
            if new_y >= world.shape.height { new_y <- new_y - world.shape.height; }
        } else {
            // Elastic bounce off walls
            if new_x < 0 or new_x >= world.shape.width {
                velocity <- {-velocity.x, velocity.y};
                new_x <- max(0.0, min(new_x, world.shape.width - 1.0));
            }
            if new_y < 0 or new_y >= world.shape.height {
                velocity <- {velocity.x, -velocity.y};
                new_y <- max(0.0, min(new_y, world.shape.height - 1.0));
            }
        }
        location <- {new_x, new_y};
    }

    aspect default {
        draw circle(0.1) color: color border: false;
    }
}

// ── Main experiment ───────────────────────────────────────────────────────────
experiment ParticleLife type: gui {

    // General parameters
    parameter "Number of types"     var: nb_types      min: 1    max: 5     category: "Model";
    parameter "Particles"           var: nb_particles  min: 2    max: 5000  category: "Model";
    parameter "Interaction radius"  var: max_radius    min: 0.0  max: 100.0 category: "Physics";
    parameter "Repulsion radius"    var: min_radius    min: 0.0  max: 5.0   category: "Physics";
    parameter "Friction"            var: friction      min: 0.0  max: 0.99  category: "Physics";
    parameter "Force scale"         var: force_scale   min: 0.0  max: 2.0   category: "Physics";
    parameter "Time step (dt)"      var: dt            min: 0.001 max: 0.99 category: "Physics";
    parameter "Toroidal borders"    var: wrap_borders                        category: "Physics";
    parameter "Density threshold"   var: density_limit min: 0.0  max: 100.0 category: "Physics";

    // ════════════════════════════════════════════════════════════════════════
    // ATTRACTION MATRIX - sliders editable during simulation
    //
    // Reading the label : "X <- Y" means "how X reacts to the presence of Y"
    //   +1.0 = X is strongly attracted to Y
    //    0.0 = X is indifferent to Y
    //   -1.0 = X strongly flees Y
    //
    // Types : Red=0  Green=1  Blue=2  Yellow=3  Purple=4
    // ════════════════════════════════════════════════════════════════════════

    parameter "Red    <- Red"     var: A_0_0 min: -1.0 max: 1.0 category: "Matrix Red";
    parameter "Red    <- Green"   var: A_0_1 min: -1.0 max: 1.0 category: "Matrix Red";
    parameter "Red    <- Blue"    var: A_0_2 min: -1.0 max: 1.0 category: "Matrix Red";
    parameter "Red    <- Yellow"  var: A_0_3 min: -1.0 max: 1.0 category: "Matrix Red";
    parameter "Red    <- Purple"  var: A_0_4 min: -1.0 max: 1.0 category: "Matrix Red";

    parameter "Green  <- Red"     var: A_1_0 min: -1.0 max: 1.0 category: "Matrix Green";
    parameter "Green  <- Green"   var: A_1_1 min: -1.0 max: 1.0 category: "Matrix Green";
    parameter "Green  <- Blue"    var: A_1_2 min: -1.0 max: 1.0 category: "Matrix Green";
    parameter "Green  <- Yellow"  var: A_1_3 min: -1.0 max: 1.0 category: "Matrix Green";
    parameter "Green  <- Purple"  var: A_1_4 min: -1.0 max: 1.0 category: "Matrix Green";

    parameter "Blue   <- Red"     var: A_2_0 min: -1.0 max: 1.0 category: "Matrix Blue";
    parameter "Blue   <- Green"   var: A_2_1 min: -1.0 max: 1.0 category: "Matrix Blue";
    parameter "Blue   <- Blue"    var: A_2_2 min: -1.0 max: 1.0 category: "Matrix Blue";
    parameter "Blue   <- Yellow"  var: A_2_3 min: -1.0 max: 1.0 category: "Matrix Blue";
    parameter "Blue   <- Purple"  var: A_2_4 min: -1.0 max: 1.0 category: "Matrix Blue";

    parameter "Yellow <- Red"     var: A_3_0 min: -1.0 max: 1.0 category: "Matrix Yellow";
    parameter "Yellow <- Green"   var: A_3_1 min: -1.0 max: 1.0 category: "Matrix Yellow";
    parameter "Yellow <- Blue"    var: A_3_2 min: -1.0 max: 1.0 category: "Matrix Yellow";
    parameter "Yellow <- Yellow"  var: A_3_3 min: -1.0 max: 1.0 category: "Matrix Yellow";
    parameter "Yellow <- Purple"  var: A_3_4 min: -1.0 max: 1.0 category: "Matrix Yellow";

    parameter "Purple <- Red"     var: A_4_0 min: -1.0 max: 1.0 category: "Matrix Purple";
    parameter "Purple <- Green"   var: A_4_1 min: -1.0 max: 1.0 category: "Matrix Purple";
    parameter "Purple <- Blue"    var: A_4_2 min: -1.0 max: 1.0 category: "Matrix Purple";
    parameter "Purple <- Yellow"  var: A_4_3 min: -1.0 max: 1.0 category: "Matrix Purple";
    parameter "Purple <- Purple"  var: A_4_4 min: -1.0 max: 1.0 category: "Matrix Purple";
    
    // ════════════════════════════════════════════════════════════════════════
    // ACTIONS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * sync_matrix_from_vars - Copies the 25 A_i_j variables into the internal matrix.
     * Called automatically each cycle by the sync_matrix reflex,
     * allowing slider changes to take effect immediately during the simulation.
     */
    action sync_matrix_from_vars() {
        attraction_matrix <- 0.0 as_matrix {nb_types, nb_types};
        attraction_matrix[0,0] <- A_0_0; attraction_matrix[0,1] <- A_0_1;
        attraction_matrix[0,2] <- A_0_2; attraction_matrix[0,3] <- A_0_3;
        attraction_matrix[0,4] <- A_0_4;
        attraction_matrix[1,0] <- A_1_0; attraction_matrix[1,1] <- A_1_1;
        attraction_matrix[1,2] <- A_1_2; attraction_matrix[1,3] <- A_1_3;
        attraction_matrix[1,4] <- A_1_4;
        attraction_matrix[2,0] <- A_2_0; attraction_matrix[2,1] <- A_2_1;
        attraction_matrix[2,2] <- A_2_2; attraction_matrix[2,3] <- A_2_3;
        attraction_matrix[2,4] <- A_2_4;
        attraction_matrix[3,0] <- A_3_0; attraction_matrix[3,1] <- A_3_1;
        attraction_matrix[3,2] <- A_3_2; attraction_matrix[3,3] <- A_3_3;
        attraction_matrix[3,4] <- A_3_4;
        attraction_matrix[4,0] <- A_4_0; attraction_matrix[4,1] <- A_4_1;
        attraction_matrix[4,2] <- A_4_2; attraction_matrix[4,3] <- A_4_3;
        attraction_matrix[4,4] <- A_4_4;
    }
    
    /**
     * randomize_matrix - Assigns random values to all matrix cells.
     * Available in the GAMA "Actions" panel during the simulation.
     */
    action randomize_matrix() {
        A_0_0<-rnd(-1.0,1.0); A_0_1<-rnd(-1.0,1.0); A_0_2<-rnd(-1.0,1.0); A_0_3<-rnd(-1.0,1.0); A_0_4<-rnd(-1.0,1.0);
        A_1_0<-rnd(-1.0,1.0); A_1_1<-rnd(-1.0,1.0); A_1_2<-rnd(-1.0,1.0); A_1_3<-rnd(-1.0,1.0); A_1_4<-rnd(-1.0,1.0);
        A_2_0<-rnd(-1.0,1.0); A_2_1<-rnd(-1.0,1.0); A_2_2<-rnd(-1.0,1.0); A_2_3<-rnd(-1.0,1.0); A_2_4<-rnd(-1.0,1.0);
        A_3_0<-rnd(-1.0,1.0); A_3_1<-rnd(-1.0,1.0); A_3_2<-rnd(-1.0,1.0); A_3_3<-rnd(-1.0,1.0); A_3_4<-rnd(-1.0,1.0);
        A_4_0<-rnd(-1.0,1.0); A_4_1<-rnd(-1.0,1.0); A_4_2<-rnd(-1.0,1.0); A_4_3<-rnd(-1.0,1.0); A_4_4<-rnd(-1.0,1.0);
        do sync_matrix_from_vars();
        write "New random matrix applied.";
    }

    /**
     * preset_chains - Each type attracts the next type (ring cycle).
     * Produces characteristic spirals and rotating chains.
     * Red->Green->Blue->Yellow->Purple->Red
     */
    action preset_chains() {
        A_0_0<- -0.3; A_0_1<-  0.9; A_0_2<-  0.0; A_0_3<-  0.0; A_0_4<-  0.0;
        A_1_0<-  0.0; A_1_1<- -0.3; A_1_2<-  0.9; A_1_3<-  0.0; A_1_4<-  0.0;
        A_2_0<-  0.0; A_2_1<-  0.0; A_2_2<- -0.3; A_2_3<-  0.9; A_2_4<-  0.0;
        A_3_0<-  0.0; A_3_1<-  0.0; A_3_2<-  0.0; A_3_3<- -0.3; A_3_4<-  0.9;
        A_4_0<-  0.9; A_4_1<-  0.0; A_4_2<-  0.0; A_4_3<-  0.0; A_4_4<- -0.3;
        do sync_matrix_from_vars();
        write "Preset 'Chains' applied.";
    }

    /**
     * preset_clusters - Each type attracts itself and repels the others.
     * Produces well-separated colour clusters.
     */
    action preset_clusters() {
        A_0_0<-  0.8; A_0_1<- -0.5; A_0_2<- -0.5; A_0_3<- -0.5; A_0_4<- -0.5;
        A_1_0<- -0.5; A_1_1<-  0.8; A_1_2<- -0.5; A_1_3<- -0.5; A_1_4<- -0.5;
        A_2_0<- -0.5; A_2_1<- -0.5; A_2_2<-  0.8; A_2_3<- -0.5; A_2_4<- -0.5;
        A_3_0<- -0.5; A_3_1<- -0.5; A_3_2<- -0.5; A_3_3<-  0.8; A_3_4<- -0.5;
        A_4_0<- -0.5; A_4_1<- -0.5; A_4_2<- -0.5; A_4_3<- -0.5; A_4_4<-  0.8;
        do sync_matrix_from_vars();
        write "Preset 'Clusters' applied.";
    }
    
    user_command "Random matrix"      action: randomize_matrix;
    user_command "Preset : Chains"    action: preset_chains;
    user_command "Preset : Clusters"  action: preset_clusters;
    

    output {
        display "Particle Life" type: 2d antialias: true background: #black {
            species particle aspect: default;
        }
        monitor "Average density"  value: with_precision(mean(particle collect each.local_density), 2);
        monitor "Average speed"    value: with_precision(mean(particle collect (sqrt(each.velocity.x^2 + each.velocity.y^2))), 2);
    }
}

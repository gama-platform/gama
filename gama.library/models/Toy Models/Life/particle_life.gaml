/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    PARTICLE LIFE SIMULATION                             ║
 * ║                                                                          ║
 * ║  Inspiré de : OfficialCodeNoodles/Particle-Life-Simulation (Godot)      ║
 * ║               & hunar4321/particle-life                                  ║
 * ║                                                                          ║
 * ║  Principe :                                                              ║
 * ║    Des particules de N types différents (distingués par couleur)         ║
 * ║    interagissent via une matrice d'attraction/répulsion A[i][j].         ║
 * ║    Chaque particule subit les forces exercées par ses voisines dans      ║
 * ║    un rayon donné, et se déplace selon une intégration d'Euler simple.   ║
 * ║    Des comportements émergents complexes (essaims, spirales, cellules)   ║
 * ║    naissent de ces règles locales minimales.                             ║
 * ║                                                                          ║
 * ║  Modèle physique :                                                       ║
 * ║    - Zone [0, min_radius[          : répulsion dure (anti-superposition) ║
 * ║    - Zone [min_radius, max_radius] : force selon attraction_matrix[i,j]  ║
 * ║    - Au-delà de max_radius         : aucune interaction                  ║
 * ║    - Mise à jour :                                                       ║
 * ║        v(t+1) = v(t) x friction + F x force_scale x dt                  ║
 * ║        x(t+1) = x(t) + v(t+1) x dt                                      ║
 * ║                                                                          ║
 * ║  MATRICE EDITABLE DEPUIS L'INTERFACE :                                   ║
 * ║    Chaque cellule A_i_j est exposee comme slider dans la categorie       ║
 * ║    "Matrice [Couleur]" du panneau Parametres. Les valeurs vont de -1.0   ║
 * ║    (repulsion max) a +1.0 (attraction max). La matrice interne est       ║
 * ║    synchronisee a chaque cycle via le reflex sync_matrix.                ║
 * ║                                                                          ║
 * ║  Compatible GAMA 1.9+        nb_types fixe a 5                          ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

model ParticleLife

global {

    // ════════════════════════════════════════════════════════════════════════
    // PARAMETRES GENERAUX
    // ════════════════════════════════════════════════════════════════════════
    
    // Taille du monde
    geometry shape <- envelope(50, 50);

    // Nombre de types fixe a 5 pour exposer les 25 cellules comme sliders.
    int nb_types <- 5;

    /**
     * nb_particles  [int] - Nombre total de particules crees au demarrage.
     * Reparties equitablement entre les 5 types (80 par type par defaut).
     * Plage recommandee : 50 - 5000
     */
    int nb_particles <- 2000;

    /**
     * max_radius  [float] - Rayon d'interaction maximal (unites monde).
     * Seules les particules dans ce rayon exercent une force.
     * Plage recommandee : 5.0 - 20.0
     */
    float max_radius <- 5.0;

    /**
     * min_radius  [float] - Rayon de repulsion minimale (unites monde).
     * En dessous, une repulsion universelle empeche la superposition.
     * Doit etre < max_radius. Plage recommandee : 0.2 - 2.0
     */
    float min_radius <- 1.5;

    /**
     * friction  [float] - Coefficient de friction cinetique.
     * v <- v x friction a chaque pas. 0.5=tres dissipatif, 0.99=quasi-conservatif.
     * Plage recommandee : 0.1 - 0.5
     */
    float friction <- 0.17;

    /**
     * force_scale  [float] - Facteur d'echelle global des forces.
     * Amplifie ou attenue toutes les interactions simultanement.
     * Plage recommandee : 0.1 - 0.8
     */
    float force_scale <- 0.3;

    /**
     * dt  [float] - Pas de temps de l'integration d'Euler.
     * Regle de stabilite : dt x force_scale < 0.99
     * Plage recommandee : 0.001 - 0.99
     */
    float dt <- 0.5;

    /**
     * wrap_borders  [bool] - Gestion des bords.
     * true  -> toroidal (Pac-Man) : continuite spatiale complete.
     * false -> rebond elastique sur les parois.
     */
    bool wrap_borders <- true;

    /**
     * density_limit  [float] - Seuil de densite locale au-dela duquel
     * l'attraction est attenuee.
     *
     * Principe (inspire du shader GPU du projet source) :
     *   local_density = somme sur les voisins dans max_radius :
     *     (1 - dist/max_radius)       si meme type
     *     (1 - dist/max_radius) x 0.5 si type different
     *
     *   density_factor = 1 - clamp(local_density - density_limit, 0, 1)
     *   attraction_effective = attraction x density_factor
     *
     * Effet :
     *   faible (ex. 1.0)  -> l'attraction s'annule des qu'un petit groupe
     *                         se forme : membranes fines, structures diffuses
     *   eleve  (ex. 10.0) -> la densite n'a presque pas d'effet : clusters
     *                         denses et compacts (comportement classique)
     *
     * Valeur par defaut : 3.0
     * Plage recommandee : 0.5 - 50.0
     */
    float density_limit <- 20.0;

    // ════════════════════════════════════════════════════════════════════════
    // CELLULES DE LA MATRICE D'ATTRACTION  (editables depuis l'interface)
    //
    // Nommage : A_i_j = force exercee par le type j SUR le type i
    //   Valeur  +1.0 -> type i est fortement ATTIRE vers type j
    //   Valeur   0.0 -> indifference
    //   Valeur  -1.0 -> type i FUIT fortement le type j
    //
    // Types :  0=Rouge  1=Vert  2=Bleu  3=Jaune  4=Violet
    // ════════════════════════════════════════════════════════════════════════

    // Ligne 0 : comment le Rouge reagit aux autres
    float A_0_0 <- 0.0;
    float A_0_1 <- 0.0;
    float A_0_2 <- 0.0;
    float A_0_3 <- 0.0;
    float A_0_4 <- 0.0;

    // Ligne 1 : comment le Vert reagit aux autres
    float A_1_0 <- 0.0;
    float A_1_1 <- 0.0;
    float A_1_2 <- 0.0;
    float A_1_3 <- 0.0;
    float A_1_4 <- 0.0;

    // Ligne 2 : comment le Bleu reagit aux autres
    float A_2_0 <- 0.0;
    float A_2_1 <- 0.0;
    float A_2_2 <- 0.0;
    float A_2_3 <- 0.0;
    float A_2_4 <- 0.0;

    // Ligne 3 : comment le Jaune reagit aux autres
    float A_3_0 <- 0.0;
    float A_3_1 <- 0.0;
    float A_3_2 <- 0.0;
    float A_3_3 <- 0.0;
    float A_3_4 <- 0.0;

    // Ligne 4 : comment le Violet reagit aux autres
    float A_4_0 <- 0.0;
    float A_4_1 <- 0.0;
    float A_4_2 <- 0.0;
    float A_4_3 <- 0.0;
    float A_4_4 <- 0.0;

    // ════════════════════════════════════════════════════════════════════════
    // DONNEES INTERNES
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
        do sync_matrix_from_vars;
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
     * sync_matrix_from_vars - Recopie les 25 variables A_i_j dans la matrice interne.
     * Appelee automatiquement a chaque cycle par le reflex sync_matrix,
     * ce qui permet de modifier les sliders en cours de simulation.
     */
    action sync_matrix_from_vars {
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
    // REFLEXES GLOBAUX
    // ════════════════════════════════════════════════════════════════════════

    // Synchronise la matrice interne depuis les sliders a chaque cycle.
    // Permet de voir l'effet des modifications immediatement.
    reflex sync_matrix {
        do sync_matrix_from_vars;
    }
	
	// Calcul des forces et du mouvement. "parallel" doit prendre la valeur 
	// maximale des coeurs disponnibles sur le processeur.
    reflex update_all {
        ask particle parallel: true { do compute_force; }
        ask particle parallel: true { do move; }
    }
}

// ── Espece particule ──────────────────────────────────────────────────────────
species particle {

    int   ptype;
    rgb   color;
    point velocity;
    point force_acc;
    // Densite locale calculee a chaque cycle (passe 1 de compute_force)
    float local_density;

    action compute_force {
        force_acc     <- {0.0, 0.0};
        local_density <- 0.0;
        list<particle> neighbors <- particle at_distance max_radius;

        // ── Passe 1 : calcul de la densite locale ─────────────────────────
        //   meme type      -> contribution pleine  (1 - dist/max_radius)
        //   type different -> contribution reduite (x 0.5)
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

        // ── Passe 2 : calcul des forces avec attenuation par densite ──────
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
                        float repulsion <- (min_radius - dist) / min_radius;
                        fx <- -repulsion * (dx / dist);
                        fy <- -repulsion * (dy / dist);
                    } else {
                        float g <- attraction_matrix[ptype, other.ptype];
                        // Attenuation de l'attraction si densite > density_limit
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

    action move {
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

// ── Experience principale ─────────────────────────────────────────────────────
experiment ParticleLife type: gui {

    // Parametres generaux
    parameter "Nombre de types" var: nb_types  min: 1    max: 5  category: "Modele";
    parameter "Particules"          var: nb_particles  min: 2    max: 5000  category: "Modele";
    parameter "Rayon interaction"   var: max_radius    min: 0.0  max: 100.0 category: "Physique";
    parameter "Rayon repulsion"     var: min_radius    min: 0.0   max: 5.0  category: "Physique";
    parameter "Friction"            var: friction      min: 0.0   max: 0.99  category: "Physique";
    parameter "Echelle des forces"  var: force_scale   min: 0.0   max: 2.0   category: "Physique";
    parameter "Pas de temps (dt)"   var: dt            min: 0.001  max: 0.99   category: "Physique";
    parameter "Bords toroidaux"     var: wrap_borders                        category: "Physique";
    parameter "Seuil de densite"    var: density_limit min: 0.0 max: 100.0   category: "Physique";

    // ════════════════════════════════════════════════════════════════════════
    // MATRICE D'ATTRACTION - sliders editables en cours de simulation
    //
    // Lecture du nom : "X <- Y" signifie "comment X reagit a la presence de Y"
    //   +1.0 = X est fortement attire par Y
    //    0.0 = X est indifferent a Y
    //   -1.0 = X fuit fortement Y
    //
    // Types : Rouge=0  Vert=1  Bleu=2  Jaune=3  Violet=4
    // ════════════════════════════════════════════════════════════════════════

    parameter "Rouge  <- Rouge"   var: A_0_0 min: -1.0 max: 1.0 category: "Matrice Rouge";
    parameter "Rouge  <- Vert"    var: A_0_1 min: -1.0 max: 1.0 category: "Matrice Rouge";
    parameter "Rouge  <- Bleu"    var: A_0_2 min: -1.0 max: 1.0 category: "Matrice Rouge";
    parameter "Rouge  <- Jaune"   var: A_0_3 min: -1.0 max: 1.0 category: "Matrice Rouge";
    parameter "Rouge  <- Violet"  var: A_0_4 min: -1.0 max: 1.0 category: "Matrice Rouge";

    parameter "Vert   <- Rouge"   var: A_1_0 min: -1.0 max: 1.0 category: "Matrice Vert";
    parameter "Vert   <- Vert"    var: A_1_1 min: -1.0 max: 1.0 category: "Matrice Vert";
    parameter "Vert   <- Bleu"    var: A_1_2 min: -1.0 max: 1.0 category: "Matrice Vert";
    parameter "Vert   <- Jaune"   var: A_1_3 min: -1.0 max: 1.0 category: "Matrice Vert";
    parameter "Vert   <- Violet"  var: A_1_4 min: -1.0 max: 1.0 category: "Matrice Vert";

    parameter "Bleu   <- Rouge"   var: A_2_0 min: -1.0 max: 1.0 category: "Matrice Bleu";
    parameter "Bleu   <- Vert"    var: A_2_1 min: -1.0 max: 1.0 category: "Matrice Bleu";
    parameter "Bleu   <- Bleu"    var: A_2_2 min: -1.0 max: 1.0 category: "Matrice Bleu";
    parameter "Bleu   <- Jaune"   var: A_2_3 min: -1.0 max: 1.0 category: "Matrice Bleu";
    parameter "Bleu   <- Violet"  var: A_2_4 min: -1.0 max: 1.0 category: "Matrice Bleu";

    parameter "Jaune  <- Rouge"   var: A_3_0 min: -1.0 max: 1.0 category: "Matrice Jaune";
    parameter "Jaune  <- Vert"    var: A_3_1 min: -1.0 max: 1.0 category: "Matrice Jaune";
    parameter "Jaune  <- Bleu"    var: A_3_2 min: -1.0 max: 1.0 category: "Matrice Jaune";
    parameter "Jaune  <- Jaune"   var: A_3_3 min: -1.0 max: 1.0 category: "Matrice Jaune";
    parameter "Jaune  <- Violet"  var: A_3_4 min: -1.0 max: 1.0 category: "Matrice Jaune";

    parameter "Violet <- Rouge"   var: A_4_0 min: -1.0 max: 1.0 category: "Matrice Violet";
    parameter "Violet <- Vert"    var: A_4_1 min: -1.0 max: 1.0 category: "Matrice Violet";
    parameter "Violet <- Bleu"    var: A_4_2 min: -1.0 max: 1.0 category: "Matrice Violet";
    parameter "Violet <- Jaune"   var: A_4_3 min: -1.0 max: 1.0 category: "Matrice Violet";
    parameter "Violet <- Violet"  var: A_4_4 min: -1.0 max: 1.0 category: "Matrice Violet";
    
    // ════════════════════════════════════════════════════════════════════════
    // ACTIONS
    // ════════════════════════════════════════════════════════════════════════
    /**
     * sync_matrix_from_vars - Recopie les 25 variables A_i_j dans la matrice interne.
     * Appelee automatiquement a chaque cycle par le reflex sync_matrix,
     * ce qui permet de modifier les sliders en cours de simulation.
     */
    action sync_matrix_from_vars {
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
     * randomize_matrix - Assigne des valeurs aleatoires a toutes les cellules.
     * Disponible dans le panneau "Actions" de GAMA pendant la simulation.
     */
    action randomize_matrix {
        A_0_0<-rnd(-1.0,1.0); A_0_1<-rnd(-1.0,1.0); A_0_2<-rnd(-1.0,1.0); A_0_3<-rnd(-1.0,1.0); A_0_4<-rnd(-1.0,1.0);
        A_1_0<-rnd(-1.0,1.0); A_1_1<-rnd(-1.0,1.0); A_1_2<-rnd(-1.0,1.0); A_1_3<-rnd(-1.0,1.0); A_1_4<-rnd(-1.0,1.0);
        A_2_0<-rnd(-1.0,1.0); A_2_1<-rnd(-1.0,1.0); A_2_2<-rnd(-1.0,1.0); A_2_3<-rnd(-1.0,1.0); A_2_4<-rnd(-1.0,1.0);
        A_3_0<-rnd(-1.0,1.0); A_3_1<-rnd(-1.0,1.0); A_3_2<-rnd(-1.0,1.0); A_3_3<-rnd(-1.0,1.0); A_3_4<-rnd(-1.0,1.0);
        A_4_0<-rnd(-1.0,1.0); A_4_1<-rnd(-1.0,1.0); A_4_2<-rnd(-1.0,1.0); A_4_3<-rnd(-1.0,1.0); A_4_4<-rnd(-1.0,1.0);
        do sync_matrix_from_vars;
        write "Nouvelle matrice aleatoire appliquee.";
    }

    /**
     * preset_chains - Chaque type attire le type suivant (cycle en anneau).
     * Produit des spirales et chaines tournantes caracteristiques.
     * Rouge->Vert->Bleu->Jaune->Violet->Rouge
     */
    action preset_chains {
        A_0_0<- -0.3; A_0_1<-  0.9; A_0_2<-  0.0; A_0_3<-  0.0; A_0_4<-  0.0;
        A_1_0<-  0.0; A_1_1<- -0.3; A_1_2<-  0.9; A_1_3<-  0.0; A_1_4<-  0.0;
        A_2_0<-  0.0; A_2_1<-  0.0; A_2_2<- -0.3; A_2_3<-  0.9; A_2_4<-  0.0;
        A_3_0<-  0.0; A_3_1<-  0.0; A_3_2<-  0.0; A_3_3<- -0.3; A_3_4<-  0.9;
        A_4_0<-  0.9; A_4_1<-  0.0; A_4_2<-  0.0; A_4_3<-  0.0; A_4_4<- -0.3;
        do sync_matrix_from_vars;
        write "Preset 'Chaines' applique.";
    }

    /**
     * preset_clusters - Chaque type s'attire lui-meme et repousse les autres.
     * Produit des amas bien separes par couleur.
     */
    action preset_clusters {
        A_0_0<-  0.8; A_0_1<- -0.5; A_0_2<- -0.5; A_0_3<- -0.5; A_0_4<- -0.5;
        A_1_0<- -0.5; A_1_1<-  0.8; A_1_2<- -0.5; A_1_3<- -0.5; A_1_4<- -0.5;
        A_2_0<- -0.5; A_2_1<- -0.5; A_2_2<-  0.8; A_2_3<- -0.5; A_2_4<- -0.5;
        A_3_0<- -0.5; A_3_1<- -0.5; A_3_2<- -0.5; A_3_3<-  0.8; A_3_4<- -0.5;
        A_4_0<- -0.5; A_4_1<- -0.5; A_4_2<- -0.5; A_4_3<- -0.5; A_4_4<-  0.8;
        do sync_matrix_from_vars;
        write "Preset 'Clusters' applique.";
    }
    
    user_command "Matrice aleatoire"  action: randomize_matrix;
    user_command "Preset : Chaines"   action: preset_chains;
    user_command "Preset : Clusters"  action: preset_clusters;
    

    output {
        display "Particle Life" type: 2d antialias: true background: #black {
            species particle aspect: default;
        }
        monitor "Densite moyenne"       value: with_precision(mean(particle collect each.local_density), 2);
        monitor "Vitesse moyenne"       value: with_precision(mean(particle collect (sqrt(each.velocity.x^2 + each.velocity.y^2))), 2);
    }
}

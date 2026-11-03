/**
* Name: Hydrological Model
* Author: Patrick Taillandier
* Description: A flood simulation model showing how water spreads across a terrain with dykes and buildings.
*   Space is discretized as a grid where each cell has an elevation and a water level. Water flows from
*   higher cells to lower neighbors based on hydrostatic pressure differences. Buildings are obstacles that
*   block water flow; dykes act as barriers that can be interactively destroyed by clicking on them in the
*   3D display. The model loads real GIS data (elevation grid, building and dyke shapefiles) and visualizes
*   the flood propagation in 3D, making it the primary GAMA reference for agent-based hydrological simulation.
*   Dry terrain uses muted elevation colors, water darkens with depth, and dykes turn from teal to red
*   as pressure rises. Vertical exaggeration affects only the display, not the hydrological calculations.
* Tags: shapefile, gis, grid, 3d, gui, hydrology, flood, dyke, water_flow, elevation, pressure
*/

model hydro

global {
	
   //Shapefile for the river
   file river_shapefile <- file("../includes/RedRiver.shp");
   //Shapefile for the dykes
   file dykes_shapefile <- file("../includes/Dykes.shp");
   //Shapefile for the buildings
   file buildings_shapefile <- file("../includes/Building.shp");
   
   //Data elevation file
   file dem_file <- file("../includes/mnt50.asc");  
   //Diffusion rate
   float diffusion_rate <- 0.6;
   //Height of the dykes
   float dyke_height <- 15.0;
   //Width of the dyke
   float dyke_width <- 15.0;
   float vertical_exaggeration <- 8.0;
   rgb paper <- rgb(242, 245, 241);
   rgb ink <- rgb(48, 62, 65);
   rgb shallow_water <- rgb(113, 194, 205);
   rgb deep_water <- rgb(25, 82, 126);
   rgb safe_dyke <- rgb(49, 132, 122);
   rgb warning_dyke <- rgb(224, 167, 69);
   rgb critical_dyke <- rgb(194, 74, 61);
   float lowest_altitude;
   float altitude_range;
    
   //Shape of the environment using the dem file
   geometry shape <- envelope(dykes_shapefile);
   
   //List of the drain and river cells
   list<cell> drain_cells;
   list<cell> river_cells;
   
   
  
   float step <- 1#h;
   
   init {
   	 //Initialization of the cells
     self.init_cells();
     //Initialization of the water cells
     self.init_water();
     //Initialization of the river cells
     river_cells <- cell where (each.is_river);
     //Initialization of the drain cells
      drain_cells <- cell where (each.is_drain);
      //Initialization of the obstacles (buildings and dykes)
      self.init_obstacles();
      //Set the height of each cell
      ask cell {
         obstacle_height <- compute_highest_obstacle();
         self.update_color();
      }
   }
   //Action to initialize the altitude value of the cell according to the dem file
   action init_cells() {
      ask cell  {
         altitude <- grid_value;
         neighbour_cells <- (self neighbors_at 1) ;
      }
      lowest_altitude <- cell min_of (each.altitude);
      altitude_range <- max(0.001, (cell max_of (each.altitude)) - lowest_altitude);
   }
   //action to initialize the water cells according to the river shape file and the drain
   action init_water() {
      geometry river <- geometry(river_shapefile);
      ask cell overlapping river  {
         water_height <- 10.0;
         is_river <- true;
         is_drain <- grid_y = matrix(cell).rows - 1;
      }
   }
   //initialization of the obstacles (the buildings and the dykes)
   action init_obstacles(){
      create buildings from: buildings_shapefile  {
         self.update_cells();
      }
      create dyke from: dykes_shapefile;
      ask dyke  {
          shape <-  shape + dyke_width;
           self.update_cells();
      }
   }
   //Reflex to add water among the water cells
   reflex adding_input_water {
   	  float water_input <- rnd(100)/100;
      ask river_cells {
         water_height <- water_height + water_input;
      }
   }
   //Reflex to flow the water according to the altitute and the obstacle
   reflex flowing {
      ask (cell sort_by ((each.altitude + each.water_height + each.obstacle_height))) {
      	already <- false;
         self.flow();
      }
   }
   //Reflex to update the color of the cell
   reflex update_cell_color {
      ask cell  {
      	 self.update_color();
      }
   }
   //Reflex for the drain cells to drain water
   reflex draining {
      ask drain_cells {
         water_height <- 0.0;
      }
   }
   
}
//Species which represent the obstacle
   species obstacle virtual: true {
   	  //height of the obstacle
      float height min: 0.0;
      //Color of the obstacle
      rgb color;
      //Pressure of the water
      float water_pressure update: compute_water_pressure();
      
      //List of cells concerned
      list<cell> cells_concerned ;
      //List of cells in the neighbourhood 
      list<cell> cells_neighbours;
      float base_altitude;
      
      //Action to compute the water pressure
      float compute_water_pressure() {
      	//If the obstacle doesn't have height, then there will be no pressure
         if (height = 0.0) {
            return 0.0;
         } else {
         	//The leve of the water is equals to the maximul level of water in the neighbours cells
            float water_level <- cells_neighbours max_of (each.water_height);
            //Return the water pressure as the minimal value between 1 and the water level divided by the height
            return min([1.0,water_level / height]);
         } 
      }
      
      //Action to update the cells
      action update_cells() {
      	//All the cells concerned by the obstacle are the ones overlapping the obstacle
         cells_concerned <- (cell overlapping self);
         base_altitude <- empty(cells_concerned) ? 0.0 : mean(cells_concerned collect (each.altitude));
        	ask cells_concerned {
        	//Add the obstacles to the obstacles of the cell
            add myself to: obstacles;
            water_height <- 0.0;
         }
         //Cells neighbours are all the neighbours cells of the cells concerned
         cells_neighbours <- cells_concerned + cells_concerned accumulate (each.neighbour_cells);
         //The height is now computed
      	 do compute_height();
         if (height > 0.0) {   
         	//We compute the water pressure again
            water_pressure <- compute_water_pressure();
         } else {water_pressure <- 0.0;}
      }
      action compute_height() {
      	
      }
      aspect geometry {
         draw shape color: rgb(191, 184, 169) border: rgb(137, 135, 125)
            at: {location.x, location.y, base_altitude * vertical_exaggeration}
            depth: height * vertical_exaggeration;
      }
   }
   //Species buildings which is derivated from obstacle
   species buildings parent: obstacle schedules: []   {
   	 //The building has a height randomly chosed between 2 and 10
      float height <- 2.0 + rnd(8);
   } 
   //Species dyke which is derivated from obstacle
   species dyke parent: obstacle {
   	
       int counter_wp <- 0;
       int breaking_threshold <- 24;
       aspect geometry {
          color <- water_pressure < 0.5
             ? blend(warning_dyke, safe_dyke, water_pressure * 2)
             : blend(critical_dyke, warning_dyke, (water_pressure - 0.5) * 2);
          draw shape color: color border: ink
             at: {location.x, location.y, base_altitude * vertical_exaggeration}
             depth: max(0.0, height) * vertical_exaggeration;
       }
      
      //Action to represent the break of the dyke
       action break(){
         ask cells_concerned  {
            do update_after_destruction(myself);
         }
         self.die();
      }
      //Action to compute the height of the dyke as the dyke_height without the mean height of the cells it overlaps
      action compute_height()
       {
      	   height <- dyke_height - mean(cells_concerned collect (each.altitude));
      
      }
      
      //Reflex to break the dynamic of the water
      reflex breaking_dynamic {
      	if (water_pressure = 1.0) {
      		counter_wp <- counter_wp + 1;
      		if (counter_wp > breaking_threshold) {
      			self.break();
      		}
      	} else {
      		counter_wp <- 0;
      	}
      }
      //user command which allows the possibility to destroy the dyke for the user
      user_command "Destroy dyke" action: break; 
   }
   //Grid cell to discretize space, initialized using the dem file
   grid cell file: dem_file neighbors: 8 frequency: 0  use_regular_agents: false use_individual_shapes: false use_neighbors_cache: false schedules: []  {
      //Altitude of the cell
      float altitude;
      //Height of the water in the cell
      float water_height <- 0.0 min: 0.0;
      //Height of the cell
      float height;
      //List of the neighbour cells
      list<cell> neighbour_cells ;
      //Boolean to know if it is a drain cell
      bool is_drain <- false;
      //Boolean to know if it is a river cell
      bool is_river <- false;
      //List of all the obstacles overlapping the cell
      list<obstacle> obstacles;
      //Height of the obstacles
      float obstacle_height <- 0.0;
      bool already <- false;
      
      //Action to compute the highest obstacle among the obstacles
      float compute_highest_obstacle() {
         if (empty(obstacles))
         {
            return 0.0; 
         } else {
            return obstacles max_of(each.height);
         }
      }
      //Action to flow the water 
      action flow (){
      	//if the height of the water is higher than 0 then, it can flow among the neighbour cells
         if (water_height > 0) {
         	//We get all the cells already done
            list<cell> neighbour_cells_al <- neighbour_cells where (each.already);
            //If there are cells already done then we continue
            if (!empty(neighbour_cells_al)) {
               //We compute the height of the neighbours cells according to their altitude, water_height and obstacle_height
               ask neighbour_cells_al {height <- altitude + water_height + obstacle_height;}
               //The height of the cell is equals to its altitude and water height
               height <-  altitude +  water_height;
               //The water of the cells will flow to the neighbour cells which have a height less than the height of the actual cell
               list<cell> flow_cells <- (neighbour_cells_al where (height > each.height)) ;
               //If there are cells, we compute the water flowing
               if (!empty(flow_cells)) {
                  loop flow_cell over: shuffle(flow_cells) sort_by (each.height){
                     float water_flowing <- max([0.0, min([(height - flow_cell.height), water_height * diffusion_rate])]); 
                     water_height <- water_height - water_flowing;
                     flow_cell.water_height <-flow_cell.water_height +  water_flowing;
                     height <- altitude + water_height;
                  }   
               }
            }
         }
         already <- true;
      }  
      //Update the color of the cell
      action update_color() { 
         float elevation_ratio <- (altitude - lowest_altitude) / altitude_range;
         rgb land <- blend(rgb(204, 194, 160), rgb(143, 166, 133), elevation_ratio);
         rgb water <- blend(deep_water, shallow_water, min(1.0, water_height / 12.0));
         color <- blend(water, land, min(1.0, water_height / 0.5));
         grid_value <- water_height + altitude;
      }
      //action to compute the destruction of the obstacle
      action update_after_destruction(obstacle the_obstacle){
         remove the_obstacle from: obstacles;
         obstacle_height <- compute_highest_obstacle();
        
      }
       
   }


experiment Run type: gui {
   parameter "Shapefile for the river" var:river_shapefile category:"Water data";
   parameter "Shapefile for the dykes" var:dykes_shapefile category:"Obstacles";
   parameter "Shapefile for the buildings" var:buildings_shapefile category:"Obstacles";
   parameter "Height of the dykes" var:dyke_height category:"Obstacles";
   parameter "Diffusion rate" var:diffusion_rate category:"Water dynamic";
   parameter "Vertical exaggeration (display only)" var: vertical_exaggeration min: 1.0 max: 20.0 category: "Visualization";
   output { 
 	  layout vertical([0::7778,1::2222]) tabs:false editors: false;
      display map type: 3d background: paper axes: false {
         camera 'default' location: {8352.5541,7067.0558,2635.9505} target: {5189.4135,3903.9151,0.0};
         light #ambient intensity: 150;
         grid cell elevation: grid_value * vertical_exaggeration triangulation: true;
         species buildings aspect: geometry;
         species dyke aspect: geometry ;
         overlay position: {16#px, 16#px} size: {245#px, 156#px} background: paper rounded: true {
            draw "FLOOD DEPTH" at: {14#px, 20#px} color: ink font: font("Helvetica", 12, #bold);
            draw square(10#px) at: {20#px, 45#px} color: shallow_water;
            draw "Shallow" at: {36#px, 49#px} color: ink font: font("Helvetica", 12);
            draw square(10#px) at: {130#px, 45#px} color: deep_water;
            draw "12 m +" at: {146#px, 49#px} color: ink font: font("Helvetica", 12);
            draw "DYKE PRESSURE" at: {14#px, 78#px} color: ink font: font("Helvetica", 12, #bold);
            draw square(10#px) at: {20#px, 103#px} color: safe_dyke;
            draw "Low" at: {36#px, 107#px} color: ink font: font("Helvetica", 12);
            draw square(10#px) at: {130#px, 103#px} color: critical_dyke;
            draw "Critical" at: {146#px, 107#px} color: ink font: font("Helvetica", 12);
            draw "Right-click a dyke to destroy it" at: {14#px, 140#px} color: ink font: font("Helvetica", 11);
         }
      }
      display chart_display refresh: every(24#cycles) type: 2d background: paper {
         chart "Dyke pressure" type: series background: paper axes: ink color: ink
            x_label: "Time (hours)" y_label: "Pressure / fraction of dykes" y_range: {0.0, 1.0}
            legend_font: font("Helvetica", 11) label_font: font("Helvetica", 12)
            title_font: font("Helvetica", 16, #bold) {
         	int nbDykes <-  max(1, length(dyke));
            data "Mean pressure" value: empty(dyke) ? 0.0 : mean(dyke collect (each.water_pressure)) style: line color: deep_water;
            data "At capacity" value: (dyke count (each.water_pressure = 1.0))/ nbDykes style: line color: critical_dyke;
            data "High (> 50%)" value: (dyke count (each.water_pressure > 0.5))/ nbDykes style: line color: warning_dyke;
            data "Low (< 25%)" value: (dyke count (each.water_pressure < 0.25))/ nbDykes style: line color: safe_dyke;
         }
      }
   }
}
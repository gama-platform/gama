/***
* Name: ABValuation
* Author: Cristian Jara-Figueroa - Arnaud Grignard 
* Description: This model illustrates how housing markets react to businesses and their location. 
* In contrast to the standard Alonso-Muth-Mills Model (AMM), this Agent Based version enables 
* users to understand the complexity of multiple employers within a given region.
* Tags: EconomHousing Market,
***/

model ABValuation

global{
	// Model mode
	bool updateUnitSize<-true;
	bool modeCar <- false;	
	
	// Grid parameters
	int grid_width<-16;
	int grid_height<-16;
	float cell_width<-100.0/grid_width;
	float cell_height<-100.0/grid_height;
	
	int firm_pos_1 <- int(0.5*grid_width);
	
	// Global model parameters
	float rentFarm<- 5.0;
	float buildingSizeGlobal <- 1.2;
	int unitsPerBuilding <- 5; 
	float globalWage <-1.0;
	float wageRatio<-7.0;
	float commutingCost <- 0.35;
	float commutingCostCar <- 0.3;
	float commutingCostCarFixed <- 0.5;
	float landUtilityParameter <- 10.0;
	float shareLowIncome <- 0.1;
	
	int nAgents <- int(0.95*(unitsPerBuilding)*((grid_width+1)*(grid_height+1)-1));
	
	// Update parameters (non-equilibrium)
	float rentSplit<- 0.75;
	float rentDelta <- 0.05;
	float sizeDelta <- 0.01;
	float randomMoveRate <- 0.001;
	float advertiseRatioGlobal <- 0.01;
	
	// Display parameters
	bool controlBool;
	string firmTypeToAdd<-'low';
	bool firmDeleteMode <- false;
	bool reflexPause<-false;
	
	rgb servicesColor<-rgb('#a50f15');
	rgb manufactoringColor<-rgb('#fc9272');
	
	reflex updateAgents when: (reflexPause=false) {
		if (length(firm where (each.skillType=0))!=0) {
			int lowSkilledTarget <- int(nAgents*shareLowIncome);			
			loop while: (length(worker where (each.skillType=0))<lowSkilledTarget) {
				worker toSwitch <- one_of(worker where (each.skillType=1));
				toSwitch.skillType <- 0;
				if (toSwitch.myFirm.skillType>toSwitch.skillType) {
					ask toSwitch {
						do forceFirmUpdate;
					}
				}
			}
			loop while: (length(worker where (each.skillType=0))>lowSkilledTarget) {
				worker toSwitch <- one_of(worker where (each.skillType=0));
				toSwitch.skillType <- 1;
			}
		} else {
			ask (worker where (each.skillType=0)) {
				self.skillType<-1;
			}
		}		
	}
	
	action create_firm {
		if (firmDeleteMode=false) {
			building toKill<- (building closest_to(#user_location));
			reflexPause<-true;
			create firm {
				myCity<-one_of(city);
				shape<-square(0.95*cell_width);
				if (firmTypeToAdd='high') {
					skillType<-1;
					color <- servicesColor;
				} else {
					skillType<-0;
					color <- manufactoringColor;
				}
				wage<-wageRatio*globalWage;	
				location <- toKill.location;
				nbWorkers<-0;
				advertiseRatio<-advertiseRatioGlobal;
				do advertise;
			}
			ask worker {
				if (myBuilding=toKill) {
					do forceBuildingUpdate;				
				}
			}
			ask toKill {
				do die;
			}
			reflexPause<-false;
		} else {
			if (length(firm)>1) {
				firm toKill<- (firm closest_to(#user_location));
				
				reflexPause<-true;
				list<firm> remainingLowSkilledFirms<-(firm where (each!=toKill and each.skillType=0));
				if (length(remainingLowSkilledFirms)=0) {
					ask (worker where (each.skillType=0)) {
						skillType<-1;
					}  
				}
				create building {
					myCity<-one_of(city);
					shape<-square(0.95*cell_width);
				
					rent <- rentFarm;
					buildingSize <- buildingSizeGlobal;
					vacant <- buildingSizeGlobal;
					unitSize <- buildingSizeGlobal/float(unitsPerBuilding);
				
					location <- toKill.location;
				}
				
				ask (worker where (each.myFirm=toKill)) {
					do forceFirmUpdate;
				}
				ask toKill {
					do die;
				}
				reflexPause<-false;
			}
		}
		
	}

	init{
		write "Number of units " +unitsPerBuilding*((grid_width+1)*(grid_height+1)-2);
		write "Number of workers "+nAgents;
		create city {
			maxRent<-rentFarm;
		}
		
		int i<-0;
		int j<-0;
		create building number:((grid_width+1)*(grid_height+1)-1){
			myCity<-one_of(city);
			shape<-square(0.95*cell_width);
			
			rent <- rentFarm;
			buildingSize <- buildingSizeGlobal;
			vacant <- buildingSizeGlobal;
			unitSize <- buildingSizeGlobal/float(unitsPerBuilding);
			
			location <- {cell_width*i,cell_height*j};
			i<-((i+1) mod (grid_width+1));			
			if (i=0) {
				j<-j+1;
			}
			if (i=firm_pos_1 and j=firm_pos_1) {
				i<-((i+1) mod (grid_width+1));
			}
		}
		
		create firm {
			myCity<-one_of(city);
			shape<-square(0.95*cell_width);
			skillType<-1;
			color <- servicesColor;
			wage<-globalWage;
			location <- {cell_width*firm_pos_1,cell_height*firm_pos_1};
			nbWorkers<-0;
			advertiseRatio<-advertiseRatioGlobal;
		}
		
		create worker number:nAgents {
			skillType<-1;
			list<firm> possibleFirms<-(firm where (each.skillType=skillType));
			if (length(possibleFirms)!=0) {
				myFirm <- one_of(possibleFirms);
				myFirm.nbWorkers <- myFirm.nbWorkers+1;
			} else {
				
			}
			
			list<building> possibleBuildings<-(building where (each.vacant>0));
			if (length(possibleBuildings)!=0) {
				myBuilding <- one_of(possibleBuildings);
				myBuilding.vacant <- myBuilding.vacant-myBuilding.unitSize;
			} else {
				
			}
			location <- any_location_in(myBuilding);
			if (myFirm.skillType=1) {
			  color<-rgb('#08519c');
		    } else {
			  color<-rgb('#6baed6');
		    }
		}
		
		ask one_of(city) {
			do updateCityParams;
		}
			
	}
	
}

species city {
	float maxRent;
	float maxDensity;
	float maxWage;
	float maxSupportedDensity;
	
	action updateCityParams{
		maxRent <- max(building collect each.rent);
		maxDensity <- max(building collect each.density);
		maxSupportedDensity <- max(building collect each.supportedDensity);
		maxWage <- max(firm collect each.wage);
	}
	
	reflex update when: (reflexPause=false) {
		do updateCityParams;
	}
}

species firm{
	int nbWorkers;
	int skillType;
	float wage;
	city myCity;
	float advertiseRatio;
	rgb color;
	
	action advertise {
		int nEmployees <- int(advertiseRatio*length(worker where (each.skillType>=skillType)));
		int i<-0;
		loop while: (i<nEmployees) {
			worker targetWorker <- one_of(worker where (each.skillType>=skillType));
			ask targetWorker {
				do attemptFirmUpdate(myself);
			}
			i<-i+1;
		}
	}

	aspect base{
		draw shape color:#blue;
	}
	
	reflex updateWage when: (reflexPause=false) {
		if (skillType=1) {
			wage <- wageRatio*globalWage;
		} else {
			wage <- globalWage;
		}
	}
	
	aspect threeD {
		draw shape color: color depth: 2+nbWorkers/nAgents*20;
	}
	aspect twoD {
		draw shape color: color;
	}
}

species building {
	city myCity;
	float buildingSize;
	
	float rent;
	float unitSize;
	float vacant;
	
	float density;
	float supportedDensity;
	float heightValue;
	rgb color;
	
	reflex lowerRent when: (reflexPause=false) {  
		if (vacant>=unitSize) {
			rent <- rent-rentDelta*rent;
			if (rent<rentFarm){
				rent <-rentFarm;
			}
		} 
		if (rent<rentFarm){
			rent <-rentFarm;
		}
	}
	
	reflex raiseUnitSize when: (updateUnitSize=true) {
		float newUnitSize;
		newUnitSize <- (1.0+sizeDelta)*unitSize;
		if (vacant>=(1.0+sizeDelta)*(buildingSize-vacant) and newUnitSize<=buildingSize) {
			vacant <- buildingSize-(1.0+sizeDelta)*(buildingSize-vacant); 
			unitSize <- newUnitSize;
		}
	}
	
	reflex updateParameters {
		density <- float(int((buildingSize-vacant)/(unitSize)));
		if (density<0) {
			density<-0.0;
		}
		
		supportedDensity <- buildingSize/unitSize;
		if (supportedDensity=0) {
			heightValue<-0.0;
		} else {
			heightValue<-supportedDensity;
		}
		heightValue<-0.3*heightValue;
		
	}

	aspect density_aspect {
		int colorValue <- int(220-220*density/myCity.maxDensity);
		draw shape color: rgb(colorValue,colorValue,colorValue);
	}
	
	aspect threeD{
		int colorValue<- 35+ int(220-220*log(rent+1.0)/log(myCity.maxRent+1.0));
		draw shape color: rgb(colorValue,colorValue,colorValue) depth: heightValue;
    }
    
    aspect twoD{
		int colorValue<- 35+ int(220-220*log(rent+1.0)/log(myCity.maxRent+1.0));
		draw shape color: rgb(colorValue,colorValue,colorValue);
    }
	
}

species worker {
	building myBuilding;
	firm myFirm;
	bool useCar;
	float currentUtility;
	int skillType;
	rgb color;

	float myUtility (building referenceBuilding, firm referenceFirm, bool useCarLocal, float myUnitSize<-nil) {
		float utility;
		float workDistance <- (referenceBuilding distance_to referenceFirm);
		if (myUnitSize=nil){
			utility <- referenceFirm.wage - commutingValue(workDistance,useCarLocal) - referenceBuilding.rent * referenceBuilding.unitSize + landUtilityParameter * log(referenceBuilding.unitSize);
		} else {
			utility <- referenceFirm.wage - commutingValue(workDistance,useCarLocal) - referenceBuilding.rent * myUnitSize + landUtilityParameter * log(myUnitSize);
		}
		return utility;
	}
	
	float commutingValue (float distance, bool useCarLocal) {
		float outValue;
		if (modeCar=true) {
			if (useCarLocal=false){
				outValue <- commutingCost*distance;
			} else {
				outValue <- commutingCostCarFixed+commutingCostCar*distance;
			}
		} else {
			outValue <- commutingCost*distance;
		}
		return outValue; 
	}
	
	action checkMyStuff {
		do checkSkillFirm;
		if (myFirm=nil){
			myFirm <- one_of(firm where (each.skillType=skillType));
		}
		if (myBuilding=nil){
			myBuilding <- one_of(building);
		}
	}
	
	action checkSkillFirm {
		if (length(firm where (each.skillType<=skillType))=0) {
			write "kill skilltype: "+skillType;
			skillType<-1;
		}
	}
	
	action forceFirmUpdate {
		firm newFirm;
		newFirm <- one_of(firm where (each.skillType<=skillType and each!=myFirm));
		do attemptFirmUpdate(newFirm);
	}
	
	action forceBuildingUpdate {
		bool updateSuccess<-false;
		building newBuilding;
		
		loop while: (updateSuccess=false) {
			newBuilding <- one_of(building);
			if (newBuilding!=myBuilding) {
				if (newBuilding.vacant>=newBuilding.unitSize) {
					myBuilding.vacant <- myBuilding.vacant + myBuilding.unitSize;
					newBuilding.vacant <- newBuilding.vacant - newBuilding.unitSize;
					myBuilding <- newBuilding;
					location <- any_location_in(myBuilding);
					updateSuccess<-true;
				}
			} 
		}
	}
		
	action attemptFirmUpdate (firm newFirm) {
		if (newFirm!=nil) {
			myFirm.nbWorkers<-myFirm.nbWorkers-1;
			newFirm.nbWorkers<-newFirm.nbWorkers+1;
			myFirm <- newFirm;
		}
		
	}
	
	action attemptBuildingUpdate (building newBuilding, float utilityChange<-0.0) {
		if (newBuilding.vacant<newBuilding.unitSize) {
			if (utilityChange!=0) {
				newBuilding.rent <- newBuilding.rent + rentSplit * utilityChange/newBuilding.unitSize;
			}
		} else {
			myBuilding.vacant <- myBuilding.vacant + myBuilding.unitSize;
			newBuilding.vacant <- newBuilding.vacant - newBuilding.unitSize;
			myBuilding <- newBuilding;
			location <- any_location_in(myBuilding);
		}
	}
	
	reflex updateUtility when: (reflexPause=false) {
		do checkMyStuff;
		float utility<-myUtility(myBuilding,myFirm,useCar);
		currentUtility<-utility;
	}
	
	reflex updateCommutingMode when: (reflexPause=false) {
		do checkMyStuff;
		float utilityCar<-myUtility(myBuilding,myFirm,true);
		float utilityNoCar<-myUtility(myBuilding,myFirm,false);
		if (utilityCar>utilityNoCar){
			useCar<-true;
		} else {
			useCar<-false;
		}
	}
	
	reflex updateBuilding when: (reflexPause=false) {
		do checkMyStuff;
		float utility <- myUtility(myBuilding,myFirm,useCar);
		building possibleBuilding <- one_of(building);
		float possibleUtility <- myUtility(possibleBuilding,myFirm,useCar);
		float utilityChange <- possibleUtility-utility;
		
		if (utilityChange>0.0){
			do attemptBuildingUpdate(possibleBuilding, utilityChange);
		}
	}
	
	reflex updateWork when: (reflexPause=false) {	
		do checkMyStuff;	
		float utility <- myUtility(myBuilding,myFirm,useCar);
		firm possibleFirm <- one_of(firm where (each.skillType<=self.skillType and each!=myFirm));
		if (possibleFirm!=nil) {
			float possibleUtility<- myUtility(myBuilding,possibleFirm,useCar);
			if (possibleUtility>utility){
				do attemptFirmUpdate(possibleFirm);
			}
		}
	}
	
	reflex updateBuildingRandom when: (reflexPause=false) {
		if (rnd(1.0)<randomMoveRate) {
			building possibleBuilding <- one_of(building);
			do attemptBuildingUpdate(possibleBuilding);
		}
	}
	
	reflex updateWorkRandom when: (reflexPause=false) {
		do checkMyStuff;
		if (rnd(1.0)<randomMoveRate) {
			firm possibleFirm;
			possibleFirm <- one_of(firm where (each.skillType<=self.skillType and each!=myFirm));
			do attemptFirmUpdate(possibleFirm);
		}
	}
	
	reflex updateUnitSize when: (updateUnitSize=true and reflexPause=false) {
		float utility;		
		float newUnitSizem;
		float newUnitSizep;
		float possibleUtilitym;
		float possibleUtilityp;
		bool updateFlag<-true;
		
		loop while: (updateFlag=true) {
			updateFlag<-false;
			
			do checkMyStuff;
			utility <- myUtility(myBuilding,myFirm,useCar);
			newUnitSizem <- (1.0-sizeDelta)*myBuilding.unitSize;
			newUnitSizep <- (1.0+sizeDelta)*myBuilding.unitSize;
			
			possibleUtilitym <- myUtility(myBuilding,myFirm,useCar,newUnitSizem);
			possibleUtilityp <- myUtility(myBuilding,myFirm,useCar,newUnitSizep);
			
			if (possibleUtilityp>utility and myBuilding.vacant>=(1.0+sizeDelta)*(myBuilding.buildingSize-myBuilding.vacant) and newUnitSizep<=myBuilding.buildingSize) {
				myBuilding.vacant <- myBuilding.buildingSize-(1.0+sizeDelta)*(myBuilding.buildingSize-myBuilding.vacant); 
				myBuilding.unitSize <- newUnitSizep;
				updateFlag<-true;
			} else {
				if (possibleUtilitym>utility) {
					myBuilding.vacant <- myBuilding.buildingSize-(1.0-sizeDelta)*(myBuilding.buildingSize-myBuilding.vacant); 
					myBuilding.unitSize <- newUnitSizem;
					updateFlag<-true;
				}
			}
			updateFlag<-false; // Force exit
		}
	}
	
	/*aspect threeD {
		int colorValue <- int(30+220*myFirm.wage/myFirm.myCity.maxWage);
		draw sphere(1.0) color: rgb(0,0,colorValue);
	}*/
	
	aspect base{
		draw circle(0.25) color:#green;					
	}
	
	aspect wage_aspect {
		draw circle(0.25) color: color;
	}
	
	aspect threeD {
		draw cylinder(0.2,0.75) at_location {location.x,location.y,rnd(myBuilding.heightValue)} color: color;	
	}
	
}

grid cell width: grid_width height: grid_height {
	aspect dark_aspect {
		draw shape color: #black;
	}
}

experiment ABValuationDemo type: gui autorun:true{
	parameter "Commuting cost" var: commutingCost min: 0.0 max: 1.0 step: 0.05; 
	parameter "Share of low income" var: shareLowIncome min: 0.0 max: 1.0 step: 0.05; 
	parameter "Housing Supply" var: updateUnitSize; 
	output { 
		display map_3D  type:opengl background: #black axes: false  toolbar:true 
		{
			camera 'default' location: {139.364,176.9895,71.3573} target: {40.9962,59.7593,0.0} dynamic:false;
			species cell aspect: dark_aspect;			
			species worker aspect:threeD;
			species firm aspect: threeD transparency: 0.25;
			species building aspect:threeD transparency: 0.35;
			event #mouse_down {ask simulation {do create_firm;}}  
			event "p" {if(commutingCost<1){commutingCost<-commutingCost+0.1;}}
			event "m" {if(commutingCost>0){commutingCost<-commutingCost-0.1;}}
			event "u" {if(shareLowIncome<0.9){shareLowIncome<-shareLowIncome+0.1;}}
			event "e" {if(shareLowIncome>0.0){shareLowIncome<-shareLowIncome-0.1;}}
			event "s" {updateUnitSize<-!updateUnitSize;}
			event "h" {firmTypeToAdd<-'high'; firmDeleteMode<-false;}
			event "l" {firmTypeToAdd<-'low'; firmDeleteMode<-false;}
			event "d" {firmDeleteMode<-true;}
			
			overlay position: { 5, 5 } size: { 180 #px, 100 #px } background: # black transparency: 0.0 border: #black rounded: true
            {   
            	float y1;
            	draw string("MULTI EMPLOYER HOUSING MARKET SIMULATION") at: { 10#px, 20#px } color: #white font: font("Helvetica", "bold" ,72); 
            	y1<-y1+20#px;
            	draw string("This model illustrates how housing markets react to businesses and their location. \nIn contrast to the standard Alonso-Muth-Mills Model (AMM), this Agent Based version \nenables users to understand the complexity of multiple employers within a given region.") at: { 10#px, 20#px+y1 } color: #white font: font("Helvetica", "bold" ,72); 
            	
            	float y <- 150#px;
            	draw string("Population (Income)") at: { 10#px, y-20#px } color: #white font: font("Helvetica", "bold" ,32);
                draw circle(10#px) at: { 20#px, y } color: rgb('#08519c') border: rgb('#08519c')-25;
                draw string("High") at: { 40#px, y + 4#px } color: #white font: font("Helvetica","plain", 18);
                y <- y + 25#px;
                draw circle(10#px) at: { 20#px, y } color: rgb('#6baed6') border: rgb('#6baed6')-25;
                draw string("Medium") at: { 40#px, y + 4#px } color: #white font: font("Helvetica", 18);
                y <- y + 25#px;
                draw string("Employement (Sector)") at: { 10#px, y } color: #white font: font("Helvetica", "bold" ,32);
                y <- y + 25#px;
                draw square(20#px) at: { 20#px, y } color: servicesColor border: servicesColor-25;
                draw string("Services") at: { 40#px, y + 4#px } color: #white font: font("Helvetica", 18);
                y <- y + 25#px;
                draw square(20#px) at: { 20#px, y } color: manufactoringColor border: manufactoringColor-25;
                draw string("Manufacturing") at: { 40#px, y + 4#px } color: #white font: font("Helvetica", 18);
                
                y <- y + 25#px;
                draw string("Housing (Cost)") at: { 10#px, y } color: #white font: font("Helvetica", "bold" ,32);
                y <- y + 25#px;
                draw square(20#px) at: { 20#px, y } color: rgb(50,50,50) border: rgb(50,50,50)-25;
                draw string("High") at: { 40#px, y + 4#px } color: #white font: font("Helvetica", 18);  
                
                y <- y + 25#px;
                draw square(20#px) at: { 20#px, y } color: #lightgray border: #lightgray-25;
                draw string("Low") at: { 40#px, y + 4#px } color: #white font: font("Helvetica", 18); 
                
                y <- y + 75#px;
                draw string("Commuting Cost") at: { 0#px, y + 4#px } color: #white font: font("Helvetica", 18);
                y <- y + 25#px;
                draw rectangle(200#px,2#px) at: { 50#px, y } color: #white;
                draw rectangle(2#px,10#px) at: { 5#px+commutingCost*90#px, y } color: #white;

                y <- y + 25#px;
                draw string("Share of low income") at: { 0#px, y + 4#px } color: #white font: font("Helvetica", 18);
                y <- y + 25#px;
                draw rectangle(200#px,2#px) at: { 50#px, y } color: #white;
                draw rectangle(2#px,10#px) at: { shareLowIncome*90#px, y } color: #white;
                
                y <- y + 25#px;
                float x<-0#px;
                draw string("Housing Supply") at: { 0#px + x , y + 4#px } color: #white font: font("Helvetica", 18);
                y <- y + 25#px;
                draw rectangle(200#px,2#px) at: { 50#px, y } color: #white;
                draw rectangle(2#px,10#px) at: { (updateUnitSize ? 0.25 :0.75)*100#px, y } color: #white;
                y<-y+15#px; 
                draw string("     Market Driven        Fixed") at: { 10#px + x , y + 4#px } color: #white font: font("Helvetica", 12);       	          	 
            }
		}
	}
	
}
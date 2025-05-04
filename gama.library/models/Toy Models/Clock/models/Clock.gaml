/**
* Name: Clock
* Author: JD ZUCKER
* Description: This model supports defining a clock whose minutes corresponds to the tick of the simulation 
* By default 1000 milliseconds=1s correspond to 1 cycle. 
* Tags: 6/5/2019
*/
model Clock

global {
	//Time value for a cycle by default 1s/cycle
	float stepDuration<-1000.0#ms min: 100.0#ms max: 600000#ms;
	//Background of the clock
	image_file clock_normal     const: true <- image_file("../images/clock.png");
	//Image for the big hand 
	image_file clock_big_hand   const: true <- image_file("../images/big_hand.png");
	//Image for the small hand
	image_file clock_small_hand const: true <- image_file("../images/small_hand.png");
	//Image for the clock alarm
	image_file clock_alarm 	  const: true <- image_file("../images/alarm_hand.png");
	//Zoom to take in consideration the zoom in the display, to better write the cycle values
	int zoom <- 4 min:2 max:10;
	//Postion of the clock
	float clock_x <- world.shape.width/2;
	float clock_y <- world.shape.height/2;
	
	//Alarm parameters
	int alarm_days <- 0 min:0 max:365;
	int alarm_hours <- 2 min:0 max:11;
	int alarm_minutes <- 0 min:0 max:59;
	int  alarm_seconds <- 0 min:0 max:59;
	bool alarm_am <- true;
	//Compute the number of cycles corresponding to the time of alarm
	int  alarmCycle <-  int((alarm_seconds+alarm_minutes*60+alarm_hours*3600 + (alarm_am ? 0 : 3600*12) + alarm_days*3600*24) * 1000#ms / stepDuration);
	
	//Time elapsed since the beginning of the experiment
	int timeElapsed <- 0 update:  int(cycle * stepDuration);
	string reflexType <-"";
	init {
		//Creation of the clock
		create clock number: 1 {
			location <- {clock_x,clock_y};
		}
	}
}
//Species that will represent the clock
species  clock { 
		float nb_minutes<-0.0 update: ((timeElapsed mod 3600#s))/60#s; //Mod with 60 minutes or 1 hour, then divided by one minute value to get the number of minutes
		float nb_hours<-0.0 update:((timeElapsed mod 86400#s))/3600#s;
		float nb_days <- 0.0 update:((timeElapsed mod 31536000#s))/86400#s;
		
		reflex update when: cycle=alarmCycle {
			 write "" + int(nb_hours) + ":" + int(nb_minutes) + ": Time to leave !" ; 
		}
		
		aspect default {
			draw clock_normal size: 10*zoom;
			draw string(" " + cycle + " cycles")  size:zoom/2 font:"times" color:#black at:{clock_x-5,clock_y+5};
			draw clock_big_hand rotate: nb_minutes*(360/60)  + 90  size: {7 * zoom, 2} at:location + {0,0,0.1}; //Modulo with the representation of a minute in ms and divided by 10000 to get the degree of rotation
			draw clock_small_hand rotate: nb_hours*(360/12)  + 90  size:{5*zoom, 2} at:location + {0,0,0.1};			
			draw clock_alarm rotate:      (alarmCycle/12000)  size: zoom/3 at:location + {0,0,0.1}; // Alarm time
			draw string( " " + int(nb_days) + " Days")  size:zoom/2 font:"times" color:#black at:{clock_x-5,clock_y+8};
			draw string( " " + int(nb_hours) + " Hours")  size:zoom/2 font:"times" color:#black at:{clock_x-5,clock_y+10};
			draw string( " " + int(nb_minutes) + " Minutes")  size:zoom/2 font:"times" color:#black at:{clock_x-5,clock_y+12};
			draw string( " " + timeElapsed + " Seconds")  size:zoom/2 font:"times" color:#black at:{clock_x-5,clock_y+14};
			 
		}
 
}

experiment Display type: gui autorun:true{
	float minimum_cycle_duration <- 0.001#s;
	parameter 'Zoom: ' var: zoom category: 'Init' ;
	parameter 'Milliseconds/cycle' var: stepDuration category: 'Init';
	parameter 'alarm Day' var: alarm_days;
	parameter 'alarm Hour' var: alarm_hours;
	parameter 'alarm Am' var: alarm_am;
	parameter 'alarm Minutes' var: alarm_minutes;
	parameter 'alarm Seconds' var: alarm_seconds;
	output {
		display ClockView type: 3d axes:false{ 
			species clock ;
		}
	}

}


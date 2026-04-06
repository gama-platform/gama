/**
* Name: Mosquitos
* Author: Huynh Quang Nghi
* Description: A simple standalone model showing mosquitos moving randomly. Designed to be used as a legacy
*   sub-model in the Flies and Mosquitos comodeling examples. Can be run independently to verify mosquito
*   behavior before coupling.
* Tags: comodel, mosquitos, random_movement, legacy, base_model
*/
model M

global
{
	geometry shape<-square(100);
	image_file icon<-image_file("./img/mosquito.png");
	int n <- 1;
	init
	{
		create Mosquito number: n;
	}

}

species Mosquito skills: [moving]
{
	geometry shape<-circle(1);
	int durability<- rnd(100);
	reflex dolive
	{	
		write "I can bite";
		do wander amplitude:rnd(30.0) speed:0.5;		
	}

	aspect default
	{
		draw icon size:4 color: # green rotate:heading+180;
	}

}

experiment Generic type: gui
{ 
	output
	{
		display "Mosquitos display"
		{
			species Mosquito aspect: default;
		}

	}

}



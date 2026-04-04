/**
* Name: Flies
* Author: Huynh Quang Nghi
* Description: A simple standalone model showing flies moving randomly. Designed to be used as a legacy
*   sub-model in the Flies and Mosquitos comodeling examples. Can be run independently to verify fly
*   behavior before coupling.
* Tags: comodel, flies, random_movement, legacy, base_model
*/


global
{
	geometry shape<-square(100);
	image_file icon<-image_file("./img/fly.gif");
	int n <- 1;
	init
	{
		create Fly number: n;
	}

}

species Fly skills: [moving]
{
	geometry shape<-circle(1);
	int durability<- rnd(100);
	reflex dolive
	{	
		write "I can fly";
		do wander amplitude:200.0;		
	}

	aspect default
	{
		draw icon size:4 color: # green rotate:heading+180;
	}

}

experiment Simple type: gui
{ 
	output
	{
		display "Flies display"
		{
			species Fly aspect: default;
		}

	}

}



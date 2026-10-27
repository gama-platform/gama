/**
* Name: _soccerbase
* Author: Julien
* Description: This model contains the parent classes of the model
* Tags:
*/

model soccerbase

species soccer_game {
	// contains the global informations of the game
	rgb back_color_team;
	rgb front_color_team;
	
	ball_sp ball; // the ball agent
	goal_sp front_goal; // contains the goal at the front of the field (y = 120)
	goal_sp back_goal; // contains the goal at the back of the field (y = 0)
	list<base_team> teams; // contains the 2 teams
	list<base_player> players; // contains all the players of the game
	
	base_team team_possession; // the last team which possess the ball. This value is used to determine if the behavior of the team has to be defensive or offensive.
	
	init {
		// create the entities ball and the 2 goals
		create ball_sp (location:world.location) returns:var_ball;
		ball <- first(var_ball);
		create goal_sp (location:{world.location.x,120},position:"front") returns:var_goal1;
		front_goal <- first(var_goal1);
		create goal_sp (location:{world.location.x,0},position:"back") returns:var_goal2;
		back_goal <- first(var_goal2);
	}
	
	action reinit_phase() {
		// this action is called when a goal has been scored : the players are placed with their initial position, and the ball is reset to the center
		ask players {
			do loose_ball();
			location <- init_pos;
			previous_pos <- init_pos;
			inactivity_time <- 0;
		}
		ask teams {
			called_player <- nil;
		}
		ball.location <- world.location;
		ball.speed <- 0.0;
	}
}

species base_player skills:[moving] {
	// ATTRIBUTES ////////////////////////////////////////////////
	
	// ATTRIBUTES ONLY USED IN THIS BASE CLASSE, SHOULD NEVER BEEN CALLED IN STRATEGY FILE
	float recuperation_ability <- 0.2; // a mark from 0 to 1 to be able to catch the ball if another player has it
	float speed_without_ball;
	float speed_with_ball;
	point previous_pos; // used to apply inertia
	float hesitation <- 0.1; // probability of not reacting during a cycle when the player is not directly involved in the ball action
	int marking_side <- 1; // side (left/right) on which the player stands when marking
	bool displacement_effectued<-false update:false; // we can apply only one displacement by step !
	// cycles during which the player cannot (re)take the ball after kicking it or losing it; avoids immediate re-catch loops
	int inactivity_time <- 0 update:(inactivity_time <= 0) ? 0 : inactivity_time - 1;
	
	// ATTRIBUTE USEFUL TO BE READ IN THE TEAM STRATEGY FILE (READ ONLY !)
	base_team team;
	soccer_game game;
	base_team ennemy_team <- nil update:first(game.teams where (each.position != team.position));
	ball_sp ball <- nil update:first(ball_sp);
	goal_sp own_goal <- nil update:first(goal_sp where (each.position = team.position));
	goal_sp ennemy_goal <- nil update:first(goal_sp where (each.position != team.position));
	// ratio of avancement of the ball (from the point of view of the current team)
	float ball_advancement <- 0.0 update:(team.position = "back") ? ball.location.y / 120 : 1 - ball.location.y / 120;
	
	bool possess_ball;
	point init_pos;
	point init_pos_in_percent;
	float distance_to_closest_ennemy_player <- 100.0 update:self distance_to closest_ennemy_player;
	// the number of ennemy players in a range of 15 meters
	int number_of_ennemy_player_in_range <- 0 update:length((game.players where (each.team != team)) where ((each intersects circle(15))=true));
	float distance_to_ball <- 100.0 update:(ball = nil) ? 100.0 : self distance_to ball;
	float distance_to_goal <- 100.0 update:(ennemy_goal = nil) ? 100.0 : self distance_to ennemy_goal;
	// the closest player of this team
	base_player closest_friend_player <- nil update:(ball = nil) ? base_player(nil) : first( (game.players where (each.team = team and each != self)) 
		where (each distance_to self = min( (game.players where (each.team = team and each != self)) collect (each distance_to self) ) ) 
	);
	// the closest ennemy player
	base_player closest_ennemy_player <- nil update:(ball = nil) ? base_player(nil) : first( (game.players where (each.team != team and each != self)) 
		where (each distance_to self = min( (game.players where (each.team != team and each != self)) collect (each distance_to self) ) ) 
	);
	// the player of this team wich has the best "position_mark"
	base_player best_position_player <- nil update:first((team.players where (each != self)) 
		where (each.position_mark = max((team.players where (each != self)) collect (each.position_mark)))
	);
	float current_speed<-1.0 update:(possess_ball) ? speed_with_ball : speed_without_ball;
	
	// ATTRIBUTES WICH CAN BE CHANGED FROM THE TEAM STRAGEGY FILE
	float position_mark <- 0.0 update:-distance_to_goal; // a mark attributed according to the position of the player (the higher the note is, the best the position is). 
	// By default, this mark is equal to -distance_to_goal.
	string status <- ""; // the current status of the player (can be useful to build the model)
	geometry influence_area <- nil update:circle(15); // the area of interest of the player. By default, this area is a circle 15m diameter centered in the player location.

	
	// CONSTRUCTOR /////////////////////////////////////////////
	init {
		init_pos <- location;
		previous_pos <- location;
		possess_ball <- false;
		// each player has his own pace, reaction time and marking side, to avoid synchronized, robotic movements
		float pace <- 0.85 + rnd(0.3);
		speed_with_ball <- 0.4 * pace;
		speed_without_ball <- 0.5 * pace;
		hesitation <- 0.05 + rnd(0.1);
		marking_side <- flip(0.5) ? 1 : -1;
	}
	
	// ACTIONS ////////////////////////////////////////////////////
	
	// ACTIONS TO CALL FROM THE STRATEGY FILE
	// action to run to a particular position
	action run_to(point target) {
		point real_target <- target;
		// a player who is not entitled to the ball never aims beyond the offside line, otherwise he oscillates around it
		if (!possess_ball and self != team.called_player) {
			if (team.position = "back" and target.y > team.offside_pos - 1) {
				real_target <- {target.x, team.offside_pos - 1};
			} else if (team.position = "front" and target.y < team.offside_pos + 1) {
				real_target <- {target.x, team.offside_pos + 1};
			}
		}
		if (!possess_ball and self != team.called_player) {
			// small noise on the destination, so that players never converge on the exact same point
			real_target <- real_target + {gauss(0.0, 0.7), gauss(0.0, 0.7)};
		}
		if (!displacement_effectued) {
				do goto (target:real_target, speed:current_speed);
			if (possess_ball) {
				ball.location <- location;
			}
			displacement_effectued <- true;
		}
		else {
			write "WARNING : only ONE action of displacement is allowed each step";
		}
	}
	
	// action to run to the ball
	action run_to_ball() {
		point targetPos;
		if (ball.ball_direction intersects circle(1)) {
			targetPos <- ball.location;
		}
		else {
			targetPos <- (ball.ball_direction closest_points_with self) at 0;
		}
		do run_to(targetPos);
	}
	
	// action to run to the ennemy goal
	action run_to_ennemy_goal() {
		do run_to( ennemy_goal.location );
	}
	
	// action to run to its own goal
	action run_to_own_goal() {
		do run_to( own_goal.location );
	}
	
	// action to mark a player
	action mark_player (base_player player) {
		float marking_distance <- 3.0 + rnd(1.5);
		float side_offset <- marking_side * (1.0 + rnd(1.5));
		// the marker stands between the marked player and his own goal (front team defends y=120, back team y=0);
		// standing on the other side makes two players marking each other cross and swap sides forever
		point target <- (team.position = "front")
			? {player.location.x+side_offset,player.location.y+marking_distance}
			: {player.location.x+side_offset,player.location.y-marking_distance};
		do run_to(target);
	}
	
	// action ot shoot the ball to the ennemy goal
	action shoot() {
		do loose_ball();
		ball.shoot_target <- ennemy_goal.location;
		ball.shoot_speed <- 3.0;
		ask ball {
			do shooted (speed_atr:shoot_speed, target_position:shoot_target);
		}
	}
	
	// action to pass the ball to an ally
	action pass_the_ball (base_player target_player) {
		do loose_ball();
		ball.shoot_target <- target_player.location;
		ball.shoot_speed <- target_player.distance_to_ball/15;
		ask ball {
			do shooted (target_position:shoot_target, speed_atr:shoot_speed);
		}
		team.called_player <- target_player;
	}
	
	// action to pass the ball to an ally
	action pass_the_ball_ahead (base_player target_player,float number_of_meter_ahead) {
		do loose_ball();
		float offset <- (team.position = "back") ? number_of_meter_ahead : -number_of_meter_ahead;
		ball.shoot_target <- {target_player.location.x,target_player.location.y+offset};
		ball.shoot_speed <- target_player.distance_to_ball/15;
		ask ball {
			do shooted (target_position:shoot_target, speed_atr:shoot_speed);
		}
		team.called_player <- target_player;
	}
	
	// ACTION AUTOMATICALLY CALLED IN THE BASE CLASSE
	// try to take the ball if it is close enough
	action try_to_take_ball() {
		// if no player has the ball
		if (!team.possess_ball and !ennemy_team.possess_ball) {
			// if the player is the one called (result of a pass)
			if (team.called_player = self) {
				do take_ball();
			}
			// if the player is not the one called (interception of the ball), probability to catch the ball inversly proportionnal with the speed of the ball
			else {
				if (flip(1/(1+2*ball.speed))) {
					do take_ball();
				}
			}
		}
		// the ball is possessed by the ennemy team
		else if (ennemy_team.possess_ball) {
			// try to catch the ball from the other player
			if flip(recuperation_ability) {
				do take_ball();
			}
		}
	}
	
	// action of taking the ball
	action take_ball() {
		if (ennemy_team.possess_ball) {
			ask ennemy_team.player_with_ball {
				do loose_ball();
			}
		}
		possess_ball <- true;
		ball.speed <- 0.0;
		team.called_player <- nil;
		team.player_with_ball <- self;
		team.possess_ball <- true;
		game.team_possession <- team;
	}
	
	// action of loosing the ball
	action loose_ball() {
		if (possess_ball) {
			inactivity_time <- 10;
		}
		possess_ball <- false;
		team.player_with_ball <- nil;
		team.possess_ball <- false;
	}
	
	// apply the inertia
	action apply_inertia() {
		point prev_pos <- location;
		point inertia_vect <- {(location.x-previous_pos.x)*0.7,(location.y-previous_pos.y)*0.7};
		float max_inertia <- current_speed;
		if (norm(inertia_vect) > max_inertia) {
			float inertia_x <-  sqrt(abs(max_inertia*max_inertia-inertia_vect.y*inertia_vect.y));
			float inertia_y <-  sqrt(abs(max_inertia*max_inertia-inertia_vect.x*inertia_vect.x));
			inertia_x <- (inertia_vect.x < 0) ? -inertia_x : inertia_x;
			inertia_y <- (inertia_vect.y < 0) ? -inertia_y : inertia_y;
			inertia_vect <- {inertia_x,inertia_y};
		}
		location <- location + inertia_vect;
		previous_pos <- prev_pos;
	}
	
	// useful functions
	// this function returns the real x if we pass a percentage : 0 is the extreme left point, 100 is the extreme right point.
	float getXPos(float x_ratio) {
		float result;
		if (team.position="back") {
			result <- 90-x_ratio*90;
		}
		else {
			result <- x_ratio*90;
		}
		return result;
	}
	
	// this function returns the real y if we pass a percentage : 0 is the extreme defensive point, 100 is the extreme attack point.
	float getYPos(float y_ratio) {
		float result;
		if (team.position="back") {
			result <- y_ratio*120;
		}
		else {
			result <- 120-y_ratio*120;
		}
		return result;
	}
	
	///////////////////////////////////////////////////////
	
	// The update function, calls the adequate behavior
	reflex update when:cycle>1 {
		do apply_inertia();
		// verify if it is a non-offside position
		if ( (((team.position = "back") and (location.y > team.offside_pos))
			or ((team.position = "front") and (location.y < team.offside_pos))) 
			and (!possess_ball) and (self != team.called_player)
		) {
			// offside position, go back to a correct position
			point target_pos <- {location.x,(team.position = "back") ? location.y-current_speed:location.y+current_speed};
			do run_to(target_pos);
			status <- "offside position !";
		}
		else if ((distance_to_ball < 2) and !possess_ball and inactivity_time = 0) {
			do try_to_take_ball();
		}
		else if (!possess_ball and self != team.called_player and self != team.closest_player_to_ball and flip(hesitation)) {
			status <- "hesitating";
		}
		else if (game.team_possession = team) {
			do offensive_behavior();
		}
		else {
			do defensive_behavior();
		}
	}
	
	// defensive behavior, need to be redefined in the strategy file.
	// this action is called when the last player who was holding the ball was a player of the ennemy team
	action defensive_behavior() virtual:true;
	// defensive behavior, need to be redefined in the strategy file.
	// this action is called when the last player who was holding the ball was a player of this team
	action offensive_behavior() virtual:true;
	
	
	// ASPECT ////////////////////////////////////////////////////////
	aspect player {
		// the player wich possess the ball is displayed with a square. It is displayed with a circle otherwise.
		if (possess_ball) {
			draw square(2) color:(team.position = "back") ? game.back_color_team : game.front_color_team;
		}
		else {
			draw circle(1) color:(team.position = "back") ? game.back_color_team : game.front_color_team;
		}
	}
}



species base_team {
	// ATTRIBUTES ////////////////////////////////////////////////
	
	// ATTRIBUTES ONLY USED IN THIS BASE CLASSE, SHOULD NEVER BEEN CALLED IN STRATEGY FILE
	float offside_pos <- 0.0 update: (position = "back") ? max((game.players where (each.team != self)) collect (each.location.y))
		: min((game.players where (each.team != self)) collect (each.location.y));
	
	// ATTRIBUTES USEFUL TO BE READ IN THE TEAM STRATEGY FILE (READ ONLY !)
	string position; // can be "front" or "back".
	list<base_player> players; // all the players of the team.
	soccer_game game;
	
	base_player closest_player_to_ball <- first(players) update: first( players with_min_of (each.distance_to_ball ) );
	base_player called_player;
	bool possess_ball <- false;// update: ! empty ( players where (each.possess_ball=true) );
	base_player player_with_ball <- nil;// update: first(players where (each.possess_ball = true));
	
	// ATTRIBUTES WICH CAN BE CHANGED FROM THE TEAM STRATEGY FILE
	list<point> player_init_position;
}

species ball_sp skills:[moving] {
	// The ball agent.
	float speed <- 0.0;
	point shoot_target;
	float shoot_speed;
	geometry ball_direction; // the direction of the ball is used to be followed by the player
	reflex update {
		speed <- speed*0.95;
		float future_speed <- speed;
		point tmpPos<-location;
		loop i from:0 to:10 {
			tmpPos <- {tmpPos.x+cos(heading)*speed,tmpPos.y+sin(heading)*speed};
			future_speed <- future_speed*0.9;
		}
		ball_direction <- line([location,tmpPos]);
		do wander (amplitude:1.0);
		
		// anticipation of the ball position to detect a goal
		if ((location.y+sin(heading)*speed) > 120) {
			write "back team scores a goal !!";
			ask first(soccer_game) {
				do reinit_phase();
			}
		}
		if ((location.y+sin(heading)*speed) < 0) {
			write "front team scores a goal !!";
			ask first(soccer_game) {
				do reinit_phase();
			}
		}
	}
	action shooted (point target_position, float speed_atr) {
		// action called when a player shoots the ball
		speed <- speed_atr;
		do goto (target:target_position);
	}
	
	aspect ball {
		draw circle(0.5) color:#white;
	}
}

species goal_sp {
	string position; // can be "front" or "back".
	
	init {
		create goal_keeper (position:position);
	}
	
	aspect goal {
		draw rectangle(7.32,1.0) color:#black;
	}
}

species goal_keeper {
	// the goal has a basic behavior : he tries to catch the ball when it is close enough, and when 
	string position; // can be "front" or "back".
	ball_sp ball <- nil update:first(ball_sp);
	
	reflex update when:cycle>0 {
		location <- {ball.location.x/90*12+(90-12)/2,location.y};
		if (ball distance_to self < 2) {
			if (flip(1/(1+2*ball.speed))) {
				first(soccer_game).team_possession <- first(first(soccer_game).teams where (each.position = position));
				ask (first(soccer_game).players where each.possess_ball) {
					do loose_ball();
				}
				ask ball {
					do shooted ({30+rnd(30),60},5.0);
				}
			}
		}
	}
	
	init {
		location <- {45,(position="front") ? 117 : 3};
	}
	
	action offensive_behavior() {
	}
	
	action defensive_behavior() {
	}
	
	aspect goal_keeper {
		draw circle(1) color:(position = "back") ? first(soccer_game).back_color_team : first(soccer_game).front_color_team;
	}
}
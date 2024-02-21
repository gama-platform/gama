/**
* Name: Errors
* Author: Alexis Drogoul
* Description: A model that demonstrates basic features of error throwing / error handling in GAML
* Tags: gaml, syntax, error
*/
model Errors

/**
 * In this experiment, a warning is raised by the model itself and should not interrupt the flow of execution
 */
experiment "Raise a normal warning"
{
	init
	{
		// To be sure, the global preference to consider warnings as errors is set to false
		gama.pref_errors_warnings_errors <- false;
		warn "This warning is reported but does not interrupt the execution";
	}

}

experiment "Raise a warning (turned to an error)"
{
	init
	{
		// If the global preference is set to consider warnings as errors, this will stop execution
		gama.pref_errors_warnings_errors <- true;
		warn "This warning is now considered as an error";
		write "This should not be written";
	}

}


/**
 * In this experiment, an error is raised by the model itself and should interrupt the flow of execution
 */
experiment "Raise an error"
{
	init
	{
		error "This error is generated by the model itself";
		write "If gama.pref_errors_stop is set to true, this will not be written";	
	}

}
/**
 * In this experiment, an error is raised by the model itself but it is caught and displayed in the console without being reported and stopping the execution. #current_error is a constant representing the text of the latest error thrown
 */
experiment "Raise and catch an error"
{
	init
	{
		try {
			error "This error is generated by the model itself";
		} catch {
			write #current_error + " but is not reported";
		}

	}
 
}


/**
* In this experiment, an error is provoked by making a division by zero but it is caught and displayed in the console without being reported and stopping the execution
 */
experiment "Provoke and catch an error"
{
	init
	{
		try {
			float error <- 1 / 0;
		} catch {
			write "A " + #current_error + " is caught";
		}

	}

}

/**
* In this experiment, an error is provoked by making a division by zero but the use of 'try' makes it silent (i.e. no 'catch' block) 
 */
experiment "Provoke a silent error"
{
	init
	{
		try {
			float error <- 1 / 0;
		} 
	}
}
/**
* Name: Test Web Address
* Author: Patrick Taillandier
* Description: Demonstrates the 'is_reachable' operator, which checks whether a given web address (hostname or URL)
*   is accessible from the simulation machine within a specified timeout. This is useful as a guard before attempting
*   to download data from the internet — if the address is not reachable, the model can fall back to local data or
*   display a clear error message rather than hanging indefinitely. The operator takes an address string and a
*   timeout in milliseconds, and returns true if a connection can be established.
* Tags: web, network, reachable, url, http, connectivity, timeout
*/

model WebUtils

global {
	string address_to_test <- "www.google.com";
	int time_out <- 200; // the time, in milliseconds, before the call aborts
	init {
		write "Is address \"" + address_to_test +"\" is reachable: " + (is_reachable(address_to_test, 200));			
	}
	
}

experiment testWebAddress type: gui ;

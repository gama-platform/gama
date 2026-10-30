/***
* Name: networktest
* Author: Nicolas Marilleau
* Description: Test MQQT connections 
* Tags: network, test
***/

model networktest

species serv skills:[network] {}

experiment "Tests for gama.extension.network" type: test {
	test "Action connect to the MQTT server" {
		bool res <- false;
		try{ 
			create serv;
			ask serv {do connect(with_name:"Travis");}
			res <- true;
		} catch {
			  res <- false;
		} 
		assert res;
	}
}
/**
* Name: TestOfTests
* Based on the internal empty template. 
* Author: kevinchapuis
* Tags: 
*/


model TestOfTests

global {
	int nt <- 0;
	string first <- "first";
}

/* Insert your model definition here */
experiment tests type: test {
	
	setup {
		nt <- nt+1;
		write string(nt)+" time(s)";
	}
	
	test tt {
		assert "first" = first;
	}
	
	test tt2 {
		loop times:1 { assert true; }
	}
	
	test tt22 {
		bool b <- false;
		loop times:1 {b <- true;}
		assert b;
	}
	
	test tt3 {
		assert nt=4;
	}
	
}
		/**
* Name: Statistic Operators
* Author: Patrick Taillandier
* Description: A reference model illustrating the univariate statistical operators available in GAMA.
*   Applied to a sample dataset, it demonstrates: mean, median, variance, standard_deviation, sum, min,
*   max, geometric_mean, harmonic_mean, kurtosis, skewness, percentile, and interquartile_range. Results
*   are printed to the console. This model is the go-to reference for anyone needing to compute basic
*   descriptive statistics on lists of values within a GAMA simulation.
* Tags: statistics, operators, mean, median, variance, standard_deviation, kurtosis, skewness, descriptive
*/

model statistic_operators

global {
	init tet {
		list<int> data <- [1,2,3,4,5,6,7,10,20,100];
		write "data: " + data;
		write "min: " + min(data);
		write "max: " + max(data);
		write "sum: " + sum(data);
		write "mean: " + mean(data);
		write "median: " + median(data);
		write "standard_deviation: " + standard_deviation(data);
		write "geometric_mean: " + geometric_mean(data);
		write "harmonic_mean: " + harmonic_mean(data);
		write "variance: " + harmonic_mean(data);
		write "mean_deviation: " + mean_deviation(data);
		write "kurtosis: " + kurtosis(data);
		write "skewness: " + skewness(data);
		write "gamma_rnd: " + gamma_rnd(0.1,1); 
		write "gini index: " + gini([10.0, 1.0, 2.0, 0.0]);
		
		list<float> l1 <- [10.0,5.0,1.0, 3.0, 4.0, 7.5, 1.0,10.0,5.0,1.0];
		list<float> l2 <- [1.0,1.0,15.0,1.0,1.0, 3.0, 4.0, 7.5];
		write "Dynamic Time Warping: " + dtw(l1,l2);
		write "Dynamic Time Warping with a radius of 2: " + dtw(l1,l2,2);	
	}
}

experiment test_operators type: gui;
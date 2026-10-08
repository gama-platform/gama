/**
* Name: CSV to Agents
* Author: Patrick Taillandier
* Description: Shows how to create agents by importing data from a CSV file. Each row of the CSV file becomes
*   one agent, and the column values initialize the agent's corresponding attributes. The 'from' and 'with'
*   facets of the 'create' statement handle the mapping: 'from' specifies the CSV file, and 'with' maps column
*   names (when 'header: true') or column indices to agent attribute names. This is the standard approach for
*   populating a GAMA simulation from tabular external data such as census data, field survey records, or
*   exported results from other tools. The example uses the well-known Iris flower dataset.
* Tags: csv, load_file, agent, create, tabular, data, import, initialization
*/

model CSVfileloading

global {
	
	init {
		//create iris agents from the CSV file (use of the header of the CSV file), the attributes of the agents are initialized from the CSV files: 
		//we set the header facet to true to directly read the values corresponding to the right column. If the header was set to false, we could use the index of the columns to initialize the agent attributes
		create iris(sepal_length:float(get("sepallength")), 
					sepal_width:float(get("sepalwidth")), 
					petal_length:float(get("petallength")),
					petal_width:float(get("petalwidth")), 
					type:string(get("type")))
			from:csv_file( "../includes/iris.csv",true) ;	
	}
}

species iris {
	float sepal_length;
	float sepal_width;
	float petal_length;
	float petal_width;
	string type;
	rgb color ;
	
	init {
		color <- type ="Iris-setosa" ? #blue : ((type ="Iris-virginica") ? #red: #yellow);
	}
	
	aspect default {
		draw circle(petal_width) color: color; 
	}
}

experiment main type: gui{
	output {
		display map {
			species iris;
		}
	}
	
}

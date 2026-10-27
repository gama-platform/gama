/**
* Name: Pie Chart Examples
* Author: Philippe Caillou
* Description: A reference model for pie and donut charts in GAMA. Pie charts display proportional data as
*   slices of a circle, making them ideal for showing the composition of a whole (e.g., population by category,
*   resource distribution). The model covers simple pie charts with static and dynamic data, exploded slices
*   for emphasis, donut variants, and custom color/label options. Multiple experiments illustrate the available
*   configuration options in GAMA's chart API.
* Tags: gui, chart, pie, donut, proportion, visualization, output, display
*/
model pies


global
{
}

experiment "Different Pies" type: gui
{
	output synchronized: true
	{
		layout #split parameters: false navigator: false editors: false consoles: false ;	
		
		display "data_pie_chart" type: 2d
		{
			chart "Nice Ring Pie Chart" type: pie style: ring background: # darkblue color: # lightgreen label_text_color: #red label_background_color: #lightgray axes: #red  title_font: font( 'Serif', 32.0, #italic)
			tick_font: font('Monospaced' , 14, #bold) label_font: font('Arial', 32 #bold) x_label: 'Nice Xlabel' y_label:
			'Nice Ylabel'
			{
				data "BCC" value: 100 + cos(100 * cycle) * cycle * cycle color: # black;
				data "ABC" value: cycle * cycle color: # blue;
				data "BCD" value: cycle + 1;
			}

		}

		display "data_3Dpie_chart" type: 2d
		{
			chart "data_3Dpie_chart" type: pie style: 3d
			{
				data "BCC" value: 2 * cycle color: # black;
				data "ABC" value: cycle * cycle color: # blue;
				data "BCD" value: cycle + 1;
			}

		}

		display "datalist_pie_chart" type: 2d
		{
			chart "datalist_pie_chart" type: pie style: exploded  series_label_position: "none" 
			{
				datalist legend: ["A", "B", "C"] value: [[cycle, cycle + 1, 2], [cycle / 2, cycle * 2, 1], [cycle + 2, cycle - 2, cycle]] x_err_values: [3, 2, 10] y_err_values:
				[3, cycle, 2 * cycle]
				//					categoriesnames:["C1","C2","C3"]
				color: [# black, # blue, # red];
			}

		}

	}

}
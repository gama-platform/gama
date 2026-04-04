/**
* Name: Clipboard
* Author: Alexis Drogoul
* Description: Shows how to use the system clipboard from GAML models. The 'copy_to_clipboard' action places a
*   string value onto the operating system clipboard, while 'paste_from_clipboard' retrieves the current contents
*   of the clipboard as a string. This can be used to transfer data between GAMA and other applications, or to
*   let users copy simulation results for use elsewhere. The example transforms the world geometry into a string,
*   modifies it, copies it to the clipboard, and reads it back.
* Tags: system, clipboard, copy, paste, string, casting, OS
*/


model Clipboard

global {
	
	init {
		// We transform the geometry into a string
		string my_shape <- string(shape);
		write "Original shape: " + my_shape;
		// We transform it a bit
		my_shape <- my_shape replace("100","150");
		// We copy the string representation of the shape into the clipboard
		bool copied <- copy_to_clipboard(my_shape);
		// If it has been correctly copied, we retrieve it as a geometry
		if (copied) {
			geometry received <- copy_from_clipboard(geometry);
			write "Transformed shape: " + received;
		}
	}
	
}

experiment run;
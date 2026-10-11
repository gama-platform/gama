

/**
* Name: Casting Images
* Author: Alexis Drogoul
* Description: Shows how to cast image values to and from popular formats in GAMA. Demonstrates conversion
*   between the 'image' type and other GAMA types: casting a matrix to an image, casting an image to a matrix
*   of colors, and casting strings (file paths) to images. This is the reference for type conversion involving
*   the image type.
* Tags: image, cast, matrix, color, type_conversion, load_file
*/


model CastingImages

/* Insert your model definition here */


global {
	image im <- #gama_logo;
	matrix<int> mat <- matrix(im);
	field f <- field(im);
}

experiment Show {
	
	output {
		layout #split;
		display im type: 3d {
			picture im;
		}
		
		display mat {
			picture mat;
		}
		
		display field type: 3d {
			mesh f scale: 0.05 triangulation: true;
		}
	}
	
}


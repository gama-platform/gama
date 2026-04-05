/**
* Name: URL Image Import
* Author: Alexis Drogoul
* Description: Shows how to load an image directly from a URL in GAMA. The 'image_file' operator accepts both
*   local file paths and HTTP/HTTPS URLs, downloading the image on the fly. This allows models to use up-to-date
*   remote images — such as satellite tiles, webcam snapshots, or web-hosted icons — without bundling them locally.
*   The model also demonstrates how to manipulate image pixel data as a matrix: it loads the GAMA platform logo
*   from GitHub, shuffles its pixels, and saves the shuffled result as a local PNG file.
* Tags: image, load_file, url, web, http, download, pixel, matrix, png
*/
model URLImageImport

global {
	image_file im <- image_file("https://raw.githubusercontent.com/wiki/gama-platform/gama/resources/images/general/GamaPlatform.png");
	geometry shape <- envelope(im);
	// We modify a bit the image 
	matrix<int> shuffle <- shuffle(im.contents); 
	// We create a file with the new contents
	image_file shuffled_copy <- image_file("../images/local_copy.png", shuffle);
	init {
		// And save it
		save shuffled_copy;
	}
}

experiment urlImage {
	output {
		display 'Original' background: #white {
			picture im ;
		}
		display 'Shuffled_copy' background: #white {
			picture  shuffled_copy;
		}

	}
}


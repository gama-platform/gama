/**
* Name: Zip Unzip
* Author: Patrick Taillandier
* Description: Demonstrates the 'zip' and 'unzip' operators for compressing and decompressing files and folders
*   in GAMA. The 'zip' operator takes a list of file/folder paths and creates a ZIP archive; 'unzip' extracts the
*   contents of a ZIP archive to a specified destination folder. These operators are useful for packaging simulation
*   outputs into a single archive for sharing, or for working with externally provided compressed datasets.
*   Both operators return a boolean indicating success.
* Tags: file, zip, unzip, compress, archive, IO
*/

model ZipUnzip

global {

	init {
		//zip fileA.txt and folderB into the archive.zip file
		bool zip_ok <- zip(["../includes/fileA.txt", "../includes/folderB"], "archive.zip");
		write "Zip operation is " + (zip_ok ? "ok" : "not ok");
		
		//unzip the archive.zip file into the results folder
		bool unzip_ok<- unzip( "archive.zip", "results");
		write "Unzip operation is " + (unzip_ok ? "ok" : "not ok");
	}
}

experiment ZipUnzip type: gui ;
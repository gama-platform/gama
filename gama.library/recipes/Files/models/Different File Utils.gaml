/**
* Name: File Utils
* Author: Patrick Taillandier, Tri Nguyen-Huu
* Description: Demonstrates the file utility operators available in GAMA for managing files and directories
*   on the local file system. The 'delete_file' operator removes a file or folder; 'rename_file' renames or
*   moves a file or folder; 'copy_file' duplicates a file or folder to a new location. These operations are
*   useful for organizing output files, cleaning up temporary results between runs, or preparing the file
*   system before a batch experiment begins. The model shows each operator in action with console output.
* Tags: file, utility, delete, rename, copy, folder, directory, IO
*/

model FileUtils

global {
	init {
		save "testA" to: "a_folder/fileA.txt";
		
		bool copy_file_ok <- copy_file("a_folder/fileA.txt","a_folder/fileB.txt");
		
		write "copy file is ok: " + copy_file_ok;
		
		bool delete_file_ok <- delete_file("a_folder/fileA.txt");
		
		write "delete file is ok: " + delete_file_ok;
		
		bool rename_file_ok <- rename_file("a_folder/fileB.txt","a_folder/fileA.txt");
		
		write "rename file is ok: " + rename_file_ok;
		
		bool delete_folder_ok <- delete_file("a_folder");
	
		write "delete folder is ok: " + delete_folder_ok;
		
		bool folder_exist_ok <- folder_exists("..");
		
		write "folder exists ok: " + folder_exist_ok;
		
		file current_folder <- folder(".");
		
		write "folder the model is in: "+current_folder;
		
		list folder_contents <- current_folder.contents;
		
		write "list of file inside the model folder: "+ folder_contents;
			
	}
	
}

experiment fileUtils type: gui ;

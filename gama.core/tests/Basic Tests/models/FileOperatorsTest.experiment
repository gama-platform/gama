/**
* Name: FileOperatorsTest
* Description: Tests the operators that manipulate files and folders (creation, copy, renaming, deletion,
* 	archives), the attributes of a file and the predicates on file types.
* 	Everything is written in a temporary folder next to the model, removed at the end of each test
* Tags: file, folder, system, test
*/

experiment FileOperatorsTest type: test autorun: true {

	string sandbox <- "file_operators_tmp";

	test "Folders" {
		assert not folder_exists(sandbox);
		string created <- new_folder(sandbox).path;
		assert folder_exists(sandbox);
		assert directory_exists(sandbox);
		// a folder is not a file
		assert not file_exists(sandbox);
		assert empty(folder(sandbox).contents);

		assert delete_file(sandbox);
		assert not folder_exists(sandbox);
	}

	test "Writing, copying, renaming and deleting a file" {
		string original <- sandbox + "/original.txt";
		string duplicate <- sandbox + "/duplicate.txt";
		string renamed <- sandbox + "/renamed.txt";
		save "first line\nsecond line" to: original format: "text";
		bool exists_after_save <- file_exists(original);
		assert exists_after_save;
		assert not folder_exists(original);

		bool copied <- copy_file(original, duplicate);
		assert copied;
		assert file_exists(duplicate);
		// the original is still there
		assert file_exists(original);
		assert text_file(duplicate).contents = ["first line", "second line"];

		bool was_renamed <- rename_file(duplicate, renamed);
		assert was_renamed;
		assert file_exists(renamed);
		assert not file_exists(duplicate);

		list<string> listed <- folder(sandbox).contents;
		assert length(listed) = 2;
		assert listed contains_all ["original.txt", "renamed.txt"];

		assert delete_file(renamed);
		assert not file_exists(renamed);
		// deleting what does not exist fails quietly
		assert not delete_file(renamed);

		// a folder is deleted with its contents
		assert delete_file(sandbox);
		assert not file_exists(original);
		assert not folder_exists(sandbox);
	}

	test "Saving again replaces or completes a file" {
		string target <- sandbox + "/log.txt";
		save "one" to: target format: "text";
		save "two" to: target format: "text";
		assert text_file(target).contents = ["two"];

		save "three" to: target format: "text" rewrite: false;
		assert length(text_file(target).contents) >= 1;
		assert concatenate(text_file(target).contents) = "twothree";

		assert delete_file(sandbox);
	}

	test "File attributes" {
		string target <- sandbox + "/attributes.txt";
		save "content" to: target format: "text";
		file f <- text_file(target);
		assert f.exists;
		assert f.name = "attributes.txt";
		assert f.extension = "txt";
		assert f.readable;
		assert f.writable;
		assert f.path ends_with "file_operators_tmp/attributes.txt";
		// relative paths are resolved from the folder of the model
		assert to_absolute_path(target) = f.path;
		assert f.contents = ["content"];

		assert delete_file(sandbox);
	}

	test "Reading a file that does not exist raises an error" {
		bool failed <- false;
		try {
			list<string> lines <- text_file("file_operators_tmp/missing.txt").contents;
		} catch {
			failed <- true;
		}
		assert failed;
		assert not file_exists("file_operators_tmp/missing.txt");
	}

	test "Archives" {
		string first_file <- sandbox + "/first.txt";
		string second_file <- sandbox + "/second.txt";
		string archive <- sandbox + "/archive.zip";
		string extracted <- sandbox + "/extracted";
		save "first" to: first_file format: "text";
		save "second" to: second_file format: "text";

		bool zipped <- zip([first_file, second_file], archive);
		assert zipped;
		assert file_exists(archive);

		bool unzipped <- unzip(archive, extracted);
		assert unzipped;
		assert folder_exists(extracted);
		assert text_file(extracted + "/first.txt").contents = ["first"];
		assert text_file(extracted + "/second.txt").contents = ["second"];

		assert delete_file(sandbox);
	}

	test "File types are recognised from their extension" {
		assert is_text("notes.txt");
		assert is_csv("data.csv");
		assert is_json("data.json");
		assert is_image("picture.png");
		assert is_image("picture.jpg");
		assert is_shape("roads.shp");
		assert is_gaml("model.gaml");

	}
}

/**
* Name: UnicodeRobustnessTest
* Description: Feeds the string operators, the serialisations and the files with characters outside of ASCII:
* 	accents, combining characters, emoji (outside of the basic multilingual plane), flags, other scripts.
* 	A character must never be cut in half, and the round trips must give back the original text
* Tags: robustness, string, unicode, test
*/

experiment UnicodeRobustnessTest type: test autorun: true {

	string emoji <- "😀a";
	string mixed <- "é😀日本語 Ωω\n\t\"'";

	test "Characters are never cut in half" {
		// reading a string character by character gives back the string
		string rebuilt <- "";
		loop i from: 0 to: length(emoji) - 1 {
			rebuilt <- rebuilt + (emoji at i);
		}
		assert rebuilt = emoji;
		// the first character is the whole emoji, not half of it
		assert emoji at 0 = "😀";
		assert first(emoji) = "😀";
		assert last(emoji) = "a";
		assert length(emoji) = 2;
		assert emoji at 1 = "a";
		assert emoji at 2 = "";
		assert emoji at -1 = "";
		assert last("a😀") = "😀";
		assert first("😀") = last("😀");
		assert length("😀😀") = 2;
		assert length("é日本語") = 4;
		assert length("é") = 2;
	}

	test "Search positions and slices count code points" {
		string text <- "😀a😀b";
		assert text index_of "a" = 1;
		assert text last_index_of "😀" = 2;
		assert text index_of "b" = 3;
		assert text index_of "missing" = -1;
		assert text last_index_of "" = length(text);
		assert copy_between(text, 0, 1) = "😀";
		assert copy_between(text, 1, 3) = "a😀";
		assert copy_between(text, text last_index_of "😀", length(text)) = "😀b";
		assert copy_between(text, -1, 99) = text;
		assert copy_between(text, 99, 100) = "";
		assert copy_between(text, 2, 1) = "";
		assert capitalize("😀a") = "😀a";
	}

	test "Reversing keeps the characters" {
		assert reverse(emoji) = "a😀";
		assert reverse(reverse(mixed)) = mixed;
	}

	test "Searching and replacing" {
		assert emoji contains "😀";

	}

	test "Serialisations give back the text" {
		assert from_json(to_json(mixed)) = mixed;
		assert from_binary(to_binary(mixed)) = mixed;
		assert from_gaml(to_gaml(mixed)) = mixed;
		assert deserialize(serialize(mixed)) = mixed;
	}

	test "Compression gives back the text" {
		assert decompress(compress(mixed)) = mixed;

	}

	test "Files keep the text" {
		save mixed to: "unicode_test_tmp.txt" format: "text";
		list<string> lines <- text_file("unicode_test_tmp.txt").contents;
		assert concatenate(lines) contains "é😀日本語";
		assert delete_file("unicode_test_tmp.txt");
	}

	test "Files can have non ASCII names" {
		save "content" to: "données_😀_tmp.txt" format: "text";
		assert file_exists("données_😀_tmp.txt");
		assert text_file("données_😀_tmp.txt").contents = ["content"];
		assert delete_file("données_😀_tmp.txt");
	}

}

/**
* Name: Serialization
* Based on the internal empty template. 
* Author: baptiste
* Tags: 
*/


model Serialization


experiment SerializationTest type:test {
	
	//to_json
	test to_json_int {
		assert to_json(1) = "1";
	}
	test to_json_float {
		assert to_json(1.2) = "1.2";
	}
	test to_json_empty_string {
		assert to_json("") = '""';
	}
	test to_json_string {
		assert to_json("abcd") = '"abcd"';
	}
	test to_json_empty_list {
		assert to_json([]) = "[]";
	}
	test to_json_list {
		assert to_json([1,"a",false]) = '[1,"a",false]';
	}
	test to_json_list_of_list {
		assert to_json([[1,2,3],[4,5,6]]) = '[[1,2,3],[4,5,6]]';
	}
	test to_json_nil {
		assert to_json(nil) = 'null';
	}
	test to_json_map {
		map my_var <- [
			"x"::"abc",
			"y"::#red,
			"z"::123,
			"123"::10.2,
			"a"::false
		];
		assert to_json(my_var) = '{"x":"abc","y":{"gaml_type":"rgb","red":255,"green":0,"blue":0,"alpha":255},"z":123,"123":10.2,"a":false}';
	}
	
	
	//from_json
	test from_json_int {
		assert from_json("1") = 1;
	}
	test from_json_float {
		assert from_json("1.2") = 1.2;
	}
	test from_json_empty {
		bool exception <- false;
		try{
			let l <- from_json('');
			
		}
		catch{
			exception <- true;
		}
		assert exception;
	}
	test from_json_empty_string {
		assert from_json('""') = "";
	}
	test from_json_string {
		assert from_json('"abcd"') = "abcd";
	}
	test from_json_empty_list {
		assert from_json('[]') = [];
	}
	test from_json_list {
		assert from_json('[1,"a",false]') = [1,"a",false];
	}
	test from_json_list_of_list {
		assert from_json('[[1,2,3],[4,5,6]]') = [[1,2,3],[4,5,6]];
	}
	test from_json_null {
		assert from_json("null") = nil;
	}
	test from_json_map {
		map my_var <- [
			"x"::"abc",
			"y"::#red,
			"z"::123,
			"123"::10.2,
			"a"::false
		];
		assert from_json('{"x":"abc","y":{"gaml_type":"rgb","red":255,"green":0,"blue":0,"alpha":255},"z":123,"123":10.2,"a":false}') = my_var;
	}
	
	
	
	//both together
	test serialize_and_deserialize{	
		map my_var <- ["x"::"abc","y"::#red,"z"::123,"123"::10.2,"a"::false, "e"::[1,2,3]];
		assert from_json(to_json(my_var)) = my_var;
		
	}
	
	//parameters: int overflow
	test int_overflow_to_float{
		gama.pref_json_int_overflow_as_double <- true;
		assert actual_type_of(from_json("123456789123456789123456789")) = type_of(1.0);
	}
	
	test int_overflow_to_string{
		gama.pref_json_int_overflow_as_double <- false;
		assert from_json("123456789123456789123456789") = "123456789123456789123456789";
	}
	
	//parameters: infinity
	test infinity_from_literal {
		gama.pref_json_infinity_as_string <- false;
		assert from_json('Infinity') = #infinity;
	}
	test infinity_from_literal_in_map {
		gama.pref_json_infinity_as_string <- false;
		assert from_json('{"x":Infinity}') = map(['x'::#infinity]);
	}
	
	test infinity_from_string_with_literal_option {
		gama.pref_json_infinity_as_string <- false;
		assert from_json('{"x":"Infinity"}') = map(['x'::"Infinity"]);	
	}
	
	test infinity_to_literal {
		gama.pref_json_infinity_as_string <- false;
		assert to_json(#infinity) = "Infinity";
	}
	test infinity_from_string {
		gama.pref_json_infinity_as_string <- true;
		assert from_json('"Infinity"') = #infinity;
	}
	test infinity_to_string {
		gama.pref_json_infinity_as_string <- true;
		assert to_json( map("x"::#infinity)) = '{"x":"Infinity"}';
	}
	
	test negative_infinity_from_literal{
		gama.pref_json_infinity_as_string <- false;
		assert from_json('-Infinity') = -#infinity;
	}
	test negative_infinity_from_literal_in_map {
		gama.pref_json_infinity_as_string <- false;
		assert from_json('{"x":-Infinity}') = map(['x'::-#infinity]);
	}
	test negative_infinity_to_literal {
		gama.pref_json_infinity_as_string <- false;
		assert to_json(-#infinity) = "-Infinity";
	}
	test negative_infinity_from_string {
		gama.pref_json_infinity_as_string <- true;
		assert from_json('"-Infinity"') = -#infinity;
	}
	test negative_infinity_to_string {
		gama.pref_json_infinity_as_string <- true;
		assert to_json( map("x"::-#infinity)) = '{"x":"-Infinity"}'; 
	}
	
	//parameters: nan
	test nan_from_literal {
		gama.pref_json_nan_as_string <- false;
		assert from_json('NaN') = #nan;
	}
	test nan_from_literal_in_map {
		gama.pref_json_nan_as_string <- false;
		assert from_json('{"x":NaN}') = map(['x'::#nan]);
	}
	test nan_to_literal {
		gama.pref_json_nan_as_string <- false;
		assert to_json(#nan) = "NaN";
	}
	test nan_from_string {
		gama.pref_json_nan_as_string <- true;
		assert from_json('"NaN"') = #nan;
	}
	test nan_from_string_in_map {
		gama.pref_json_nan_as_string <- true;
		assert from_json('{"x":"NaN"}') = map(["x"::#nan]);
	}
	test nan_from_string_with_literal_option {
		gama.pref_json_nan_as_string <- false;
		assert from_json('{"x":"NaN"}') = map(['x'::"NaN"]);	
	}
	test nan_to_string {
		gama.pref_json_nan_as_string <- true;
		assert to_json( map("x"::#nan)) = '{"x":"NaN"}';
	}


	// errors
	test from_infinity_literal_with_string_option {
		gama.pref_json_infinity_as_string <- true;
		bool exception <- false;
		try{
			map m <- from_json('{"x":Infinity}');	
		}catch{
			exception <- true;
		}
		assert exception;
	}
	test from_nan_literal_with_string_option {
		gama.pref_json_nan_as_string <- true;
		bool exception <- false;
		try{
			map m <- from_json('{"x":NaN}');	
		}catch{
			exception <- true;
		}
		assert exception;
	}
	
	test from_unknown_literal {
		bool exception <- false;
		try{
			let l <- from_json('{"x": UnknownLiteral}');
		}catch{
			exception <- true;
		}
		assert exception;
	}
	
	test from_unclosed_json {
		bool exception <- false;
		try{
			let l <- from_json('{"x": 123');
		}catch{
			exception <- true;
		}
		assert exception;
	}
	
		
	test from_forgotten_comma {
		bool exception <- false;
		try{
			let l <- from_json('{"x": 123 "y":123}');
		}catch{
			exception <- true;
		}
		assert exception;
	}
	
			
	test from_trailing_comma {
		bool exception <- false;
		try{
			let l <- from_json('{"x": 123, "y":123 , }');
		}catch{
			exception <- true;
		}
		assert exception;
	}

	test from_simple_quotes{
		bool exception <- false;
		try{
			let l <- from_json("{'x': 123, 'y':123 }");
		}catch{
			exception <- true;
		}
		assert exception;
	}
	
}

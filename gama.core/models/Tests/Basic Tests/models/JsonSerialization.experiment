/**
* Name: Serialization
* Based on the internal empty template. 
* Author: baptiste
* Tags: serialization, JSON, test
*/

model Serialization

experiment SerializationTest type:test {
	
	test from_json_empty {
		bool exception <- false;
		try{
			unknown l <- from_json('');
			
		}
		catch{
			exception <- true;
		}
		assert exception;
	}
	
	//parameters: int overflow
	test int_overflow_to_float {
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

	test serialization_preferences_at_runtime {
		bool infinity_as_string <- gama.pref_json_infinity_as_string;
		bool nan_as_string <- gama.pref_json_nan_as_string;
		loop as_string over: [false, true, false] {
			gama.pref_json_infinity_as_string <- as_string;
			gama.pref_json_nan_as_string <- as_string;
			string infinity_json <- as_string ? '"Infinity"' : 'Infinity';
			string negative_infinity_json <- as_string ? '"-Infinity"' : '-Infinity';
			string nan_json <- as_string ? '"NaN"' : 'NaN';
			assert to_json(#infinity) = infinity_json;
			assert to_json(-#infinity) = negative_infinity_json;
			assert to_json(#nan) = nan_json;
			assert to_json(#infinity, true) = infinity_json;
			assert to_json(-#infinity, true) = negative_infinity_json;
			assert to_json(#nan, true) = nan_json;
			assert to_json(map("x"::#infinity)) = '{"x":' + infinity_json + '}';
			assert to_json(map("x"::-#infinity)) = '{"x":' + negative_infinity_json + '}';
			assert to_json(map("x"::#nan)) = '{"x":' + nan_json + '}';
			assert to_json([#infinity, -#infinity, #nan]) = '[' + infinity_json + ',' + negative_infinity_json + ',' + nan_json + ']';
			if as_string {
				assert from_json('"Infinity"') = #infinity;
				assert from_json('"-Infinity"') = -#infinity;
				assert from_json('"NaN"') = #nan;
			} else {
				assert from_json('"Infinity"') = "Infinity";
				assert from_json('"-Infinity"') = "-Infinity";
				assert from_json('"NaN"') = "NaN";
			}
		}
		gama.pref_json_infinity_as_string <- infinity_as_string;
		gama.pref_json_nan_as_string <- nan_as_string;
	}

	// errors
	test from_infinity_literal_with_string_option {
		gama.pref_json_infinity_as_string <- true;
		bool exception <- false;
		try{
			map m <- map(from_json('{"x":Infinity}'));	
		}catch{
			exception <- true;
		}
		assert exception;
	}
	test from_nan_literal_with_string_option {
		gama.pref_json_nan_as_string <- true;
		bool exception <- false;
		try{
			map m <- map(from_json('{"x":NaN}'));	
		}catch{
			exception <- true;
		}
		assert exception;
	}
	
	test from_unknown_literal {
		bool exception <- false;
		try{
			unknown l <- from_json('{"x": UnknownLiteral}');
		}catch{
			exception <- true;
		}
		assert exception;
	}
	
	test from_unclosed_json {
		bool exception <- false;
		try{
			unknown l <- from_json('{"x": 123');
		}catch{
			exception <- true;
		}
		assert exception;
	}
		
	test from_forgotten_comma {
		bool exception <- false;
		try{
			unknown l <- from_json('{"x": 123 "y":123}');
		}catch{
			exception <- true;
		}
		assert exception;
	}
			
	test from_trailing_comma {
		bool exception <- false;
		try{
			unknown l <- from_json('{"x": 123, "y":123 , }');
		}catch{
			exception <- true;
		}
		assert exception;
	}

	test from_simple_quotes{
		bool exception <- false;
		try{
			unknown l <- from_json("{'x': 123, 'y':123 }");
		}catch{
			exception <- true;
		}
		assert exception;
	}
	
}

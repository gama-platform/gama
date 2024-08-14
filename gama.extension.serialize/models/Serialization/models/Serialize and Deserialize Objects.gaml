/**
* Name: SerializeandDeserializeObjects
* Shows how to serialize and deserialize arbitrary objects in binary format
* Author: A. Drogoul
* Tags: serialization
*/


model SerializeandDeserializeObjects

global {
	init {
		
		list objects <- [[1,2,3,4], "fff",rgb(100,100,100)];
		write "Base object list: " + objects;
		
		write "\nIndividually serialized and deserialized:";
		loop o over: objects {
			string s <- serialize(o);
			write "\nserializing " + o + " => " + s;
			write "deserialized: " + deserialize(s);
		}
	
		assert objects = deserialize(serialize(objects));
		write "\nserializing and deserializing a list containing all the objects returns a list strictly identical to the initial one";
	}  
}

experiment "Run";


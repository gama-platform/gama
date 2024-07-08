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
			write deserialize(s);
		}

		write "\nThe whole list serialized and deserialized: " + deserialize(serialize(objects));
	}  
}

experiment "Run";


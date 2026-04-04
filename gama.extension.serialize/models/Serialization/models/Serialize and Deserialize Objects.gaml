/**
* Name: Serialize and Deserialize Objects
* Author: Alexis Drogoul
* Description: Shows how to serialize arbitrary GAML objects (integers, floats, strings, lists, maps,
*   geometries) to a binary byte-array and deserialize them back. The 'serialize' operator encodes any
*   GAML value to bytes; 'deserialize' reconstructs it. Covers all primitive types and container types.
*   This is the low-level binary serialization reference — for JSON use 'Serialization to JSON', and for
*   full simulation snapshots use the 'Serialize Operators' models.
* Tags: serialization, binary, serialize, deserialize, objects, data_exchange
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


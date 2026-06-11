/**
* Name: Simple SQL Commands in SQLite
* Author: Truong Minh Thai
* Description: Demonstrates the core SQL operations available via the SQLSKILL in GAMA using an SQLite
*   database: CREATE TABLE, INSERT data, SELECT data, DELETE data, and DROP TABLE. The model uses an
*   empty SQLite file as the database. Each operation is executed using the 'executeUpdate' or 'select'
*   actions of the SQLSKILL. This is the minimal reference for SQLite integration in GAMA.
* Tags: database, SQL, SQLite, SQLSKILL, create, insert, select, delete, drop
*/
model SQLite_selectNUpdate

global {
	map<string, string> PARAMS <- ['dbtype'::'sqlite', 'database'::'../includes/emptyFile.db'];
	init {
		write "This model will work only if the corresponding database is installed" color: #red;

		create DB_Accessor;

		// Test of the connection to the database
		if (!first(DB_Accessor).testConnection(PARAMS)) {
			write "Connection impossible";
			do pause();
		}

		ask (DB_Accessor) {
			do executeUpdate(params: PARAMS, updateComm: "DROP TABLE IF EXISTS registration");
			do executeUpdate(params: PARAMS, updateComm: "CREATE TABLE registration" + "(id INTEGER PRIMARY KEY, " + " first TEXT NOT NULL, " + " last TEXT NOT NULL, " + " age INTEGER);");
			write "REGISTRATION table has been created.";
			do executeUpdate(params: PARAMS, updateComm: "INSERT INTO registration " + "VALUES(100, 'Zara', 'Ali', 18);");
			do executeUpdate(params: PARAMS, updateComm: "INSERT INTO registration " + "VALUES(?, ?, ?, ?);", values: [101, 'Mr', 'Mme', 45]);
			// 'insert' accepts three kinds of data:
			// - a list: a single row, one value per column in declaration order
			do insert(params: PARAMS, into: "registration", data: [102, 'Mahnaz', 'Fatma', 25]);
			// - a map: a single row, keys are the target columns
			do insert(params: PARAMS, into: "registration", data: ["id"::103, "first"::'Zaid tim', "last"::'Kha', "age"::33]);
			// - a dataframe: several rows inserted at once, in a single batch
			do insert(params: PARAMS, into: "registration", data: dataframe_with(
				["id", "first", "last", "age"],
				[[104, 'Bill', 'Clark', 40], [105, 'Zara', 'Ali', 22]]
			));
			write "Six records have been inserted.";
			write "Click on <<Step>> button to view selected data";
		}
	}
}

species DB_Accessor skills: [SQLSKILL] {
	reflex select {
		// 'select' now returns a dataframe: named, typed columns directly usable in GAML
		dataframe t <- select(PARAMS, "SELECT * FROM registration");
		write "Select before update: " + t.rows + " rows, columns: " + t.keys;
		write df_pretty_print(t);
	}

	reflex select_parametric {
		dataframe t <- self.select(params: PARAMS,
                            select: "SELECT * FROM registration WHERE age < ?;",
                            values: [26] );
		write "Parametric select (age < 26): " + t.rows + " rows";
		write df_pretty_print(t);
	}

	reflex update {
		do executeUpdate(params: PARAMS, updateComm: "UPDATE registration SET age = 30 WHERE id IN (100, 101)");
		do executeUpdate(params: PARAMS, updateComm: "DELETE FROM registration where id=103 ");
		dataframe t <- select(PARAMS, "SELECT * FROM registration");
		write "Select after update:";
		write df_pretty_print(t);
	}

	reflex drop {
		do executeUpdate(params: PARAMS, updateComm: "DROP TABLE registration");
		write "Registration table has been dropped." color: #red;
		write "Another simulation step will throw an exception as the database is not available anymore." color: #red;
	}
}

experiment simple_SQL_exp type: gui {}     
/**
* Name: Create and Insert Agents from and into a Database (MySQL)
* Author: Benoit Gaudou
* Description: Demonstrates a bidirectional workflow between GAMA agents and a MySQL database: agents are
*   initialized by querying meteorological data from the database, and each cycle the simulation results are
*   inserted back into a result table. Demonstrates how to use SELECT to populate agents and executeUpdate/insert
*   to persist simulation outputs. The pattern works with any DBMS — just change the PARAMS connection map.
*   Requires the meteo_DB database with the meteo_DB_dump.sql schema to be installed first.
* Tags: database, SQL, MySQL, AgentDB, create, insert, select, output, agents
*/
model create_agents_Insert_result_MySQL

global {
	string res_DB <- '`result_DB`';
	map<string, string> PARAMS <- ['host'::'localhost', 'dbtype'::'mysql', 'database'::'meteo_DB', 'port'::'8889', 'user'::'root', 'passwd'::'root'];
	string SQLquery_idPoint <- "SELECT `idPointgrille`, AVG(`RRmm`) AS RR, AVG(`Tmin`) AS Tmin, AVG(`Tmax`) AS Tmax, AVG(`Rglot`) AS Rglot, AVG(`ETPmm`) AS ETPmm
    			FROM meteo_table GROUP BY `idPointgrille`";
	init {
		write "This model will work only if the MySQL database server is installed." color: #red;
		write "In addition, the database \"meteo_db\" should have be created and the data imported inside. The SQL queries are available in the file ../includes/meteo_DB_dump.sql.";
		write "";
		
		create DB_accessor;
		ask DB_accessor {
			do executeUpdate(params: PARAMS, updateComm: "DROP TABLE IF EXISTS `result_DB`");
			do executeUpdate(params: PARAMS, updateComm: "CREATE TABLE `result_DB` (
										  `idPoint` varchar(16) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
										  `valRnd` float NOT NULL DEFAULT '0',
										  `cycle` int(16) NOT NULL DEFAULT '0'
										) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		}

		write first(DB_accessor).select (PARAMS, SQLquery_idPoint);

		create idPoint(name: "idPointgrille", RRmm:"RR", Tmin:"Tmin", Tmax:"Tmax", Rglot:"Rglot", ETPmm:"ETPmm") 
				from: first(DB_accessor).select(PARAMS, SQLquery_idPoint);
	}

	reflex endSimu when: (cycle = 10) {
//		ask DB_accessor {
//			write "Data: " + (select(PARAMS, "select * FROM " + res_DB + ";"));
//			do executeUpdate params: PARAMS updateComm: "DROP TABLE " + res_DB + ";";
//		}
//
//		write "DROP the table = " + res_DB;
		do pause(); 
	}

}

species idPoint {
	float RRmm;
	float Tmin;
	float Tmax;
	float Rglot;
	float ETPmm;
	float valRnd;
	
	reflex compute_new_random_value {
		valRnd <- float(rnd(RRmm + Tmin + Tmax + Rglot + ETPmm));
	}

	reflex store_valRnd {		
		ask (first(DB_accessor)) {
			do executeUpdate(
				params: PARAMS, 
				updateComm: "INSERT INTO " + res_DB + " VALUES(?, ?, ?);", 
				values: [myself.name, myself.valRnd, cycle]
			);
		}

		write " " + self + " inserts value " + valRnd;
	}
}

species DB_accessor skills: [SQLSKILL] {
	list listRes <- [];
	
	init {
		// Test of the connection to the database
		if (!testConnection(PARAMS)) {
			write "Connection impossible";
			ask (world) {
				do pause();
			}

		} else {
			write "Connection Database OK.";
		}

		write "" + (select(PARAMS,"SELECT * FROM meteo_table"));
		write "" + (select(PARAMS, SQLquery_idPoint));
	}

}

experiment createInsertMySQL type: gui {
}
   
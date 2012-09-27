package me.blablubbabc.BlaDB;

import me.blablubbabc.paintball.Paintball;

public class SQLPlayers {

	private static BlaSQLite sql;
	private static Paintball plugin;

	public SQLPlayers(BlaSQLite blasql, Paintball pl) {
		sql = blasql;
		plugin = pl;
	}

	//PLAYERDATA
	//GET

	//SET
	
	//REMOVE

	//ADD NEW
	public void addNewPlayer(String player) {
		sql.updateQuery("INSERT OR IGNORE INTO players(name,points,shots,hits,teamattacks,kills,deaths,wins,looses,money) VALUES('player',0,0,0,0,0,0,0,0,0);");
	}
	

}

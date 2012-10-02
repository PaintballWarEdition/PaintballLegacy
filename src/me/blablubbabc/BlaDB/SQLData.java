package me.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.SQLException;

import me.blablubbabc.paintball.Paintball;

public class SQLData {

	private static BlaSQLite sql;
	@SuppressWarnings("unused")
	private static Paintball plugin;
	
	public SQLData(BlaSQLite blasql, Paintball pl) {
		sql = blasql;
		plugin = pl;
	}

	public void createDefaultTables() {
		//various other datas
		sql.createDefaultTable("data", "key TEXT, valueInt INTEGER, valueString TEXT", "key");
	}
	
	//GET
	public boolean exists(String key) {
		ResultSet rs = sql.resultQuery("SELECT EXISTS(SELECT 1 FROM data WHERE key='"+key+"' LIMIT 1);");
		try {
			if(rs != null) {
				return (rs.getInt(1) == 1 ? true : false);
			} else return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	public int getInt(String key) {
		ResultSet rs = sql.resultQuery("SELECT valueInt FROM data WHERE key='"+key+"' LIMIT 1);");
		try {
			if(rs != null) {
				return rs.getInt("valueInt");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	public String getString(String key) {
		ResultSet rs = sql.resultQuery("SELECT valueString FROM data WHERE key='"+key+"' LIMIT 1);");
		try {
			if(rs != null) {
				return rs.getString("valueString");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}
	//SET
	public void setInt(String key, int number) {
		sql.updateQuery("UPDATE OR IGNORE data SET valueInt="+number+" WHERE key='"+key+"';");
	}
	public void setString(String key, String text) {
		sql.updateQuery("UPDATE OR IGNORE data SET valueString='"+text+"' WHERE key='"+key+"';");
	}
	//ADD
	public void addInt(String key, int number) {
		sql.updateQuery("INSERT OR IGNORE INTO data(key, valueInt, valueString) VALUES('"+key+"',"+number+",'');");
	}
	public void addString(String key, String text) {
		sql.updateQuery("INSERT OR IGNORE INTO data(key, valueInt, valueString) VALUES('"+key+"',0,'"+text+"');");
	}
	//REMOVE
	public void remove(String key) {
		sql.updateQuery("DELETE OR IGNORE FROM data WHERE key='"+key+"';");
	}
}

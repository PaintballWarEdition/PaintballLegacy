package me.blablubbabc.BlaDB;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BlaSQLite {
	
	private File databaseFile;

	public BlaSQLite(File databaseFile) {
		this.databaseFile = databaseFile;
	}
	
	/*public static void createdefaultdatabase()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        File db = new File(plugin.getDataFolder(), "database.db");
        if(!db.exists())
            try
            {
                db.getParentFile().mkdir();
                Connection con = DriverManager.getConnection((new StringBuilder("jdbc:sqlite:")).append(plugin.getDataFolder()).append("/database.db").toString());
                Statement st = con.createStatement();
                st.executeUpdate("CREATE TABLE IF NOT EXISTS regions(Id INTEGER PRIMARY KEY, region, world);");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS player(player, region);");
                st.close();
                con.close();
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
    }*/
}

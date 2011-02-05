package com.bukkit.jdev19.netstats;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

public class Database
{
	public Type database = null;
	public static Property stats;
	public int i = 0;
	
	public Database(Type database) {
		this.database = database;
		initialize();
	}
	
	private void initialize() {
		if (this.database.equals(Type.FLATFILE)) {
			new File(netstats.main_directory + "netstats.properties").renameTo(new File(netstats.flatfile));
			stats = new Property(netstats.flatfile);
		}
		else if (!checkTable()) {
			netstats.log.info("[Netstats] Creating database.");
			createTable();
		}
	}
	
	private Connection connection() throws ClassNotFoundException, SQLException
	{
		if (this.database.equals(Type.MySQL)) {
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection(netstats.mysql, netstats.mysql_user, netstats.mysql_pass);
		}
	}
	
	private boolean checkTable()
	{
		Connection conn = null;
		ResultSet rs = null;
		boolean result = false;
		try
		{
			conn = Connection();
			DatabaseMetaData dbm = conn.getMetaData();
			rs = dbm.getTables(null, null, "players", null);
			result = rs.next();
		} catch (SQLException ex) {
			netstats.log.severe("[Netstats]: Table check for " + (this.database.equals(Type.MySQL) ? "mysql") + " Failed : " + ex); j = 0;
			return j;
		} catch (ClassNotFoundException e)
		{
			netstats.log.severe("[Netstats]: Database connector not found for " + (this.database.equals(Type.MySQL) ? "mysql") + ": " + e); int j = 0;
			return j;
		}
		finally
		{
			try
			{
				if (rs != null) {
					rs.close();
				}
				if (conn != null)
					conn.close();
			}
			catch (SQLException ex) {
				netstats.log.severe("[Netstats]: Failed to close connection")
			}
		}
		
		return result;
	}
	
	private void createTable() {
		Connection conn = null;
		Statement st = null;
		try
		{
			conn = connection();
			st = conn.createStatement();
			
			if (this.database.equals(Type.MySQL))
				st.executeUpdate("CREATE TABLE `players` (`id` int(3) NOT NULL AUTO_INCREMENT , `name` text NOT NULL , `playtime` time NOT NULL , `status` tinyint(1) NOT NULL , `lastlogin` datetime NOT NULL , PRIMARY KEY (`id`));");
			else
				/*
				 * If other database type
				 */
		} catch (SQLException ex) { netstats.log.severe("[Netstats]: Could not create table for " + (this.database.equals(Type.MySQL) ? "mysql") + ": " + ex);
		return; } catch (ClassNotFoundException e) { netstats.log.severe("[Netstats]: Database connector not found for " + (this.database.equals(Type.MySQL) ? "mysql") + ": " + e);
		return;
		} finally {
			try {
				if (st != null) {
					st.close();
				}
				if (conn != null)
					conn.close();
			}
			catch (SQLException ex) {
				netstats.log.severe("[Netstats]: Failed to close connection");
			}
		}
	}
	
}
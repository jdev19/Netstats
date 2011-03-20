package com.sparkedia.jdev19.Netstats;

import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
	protected Netstats plugin;
	private Logger log;
	public int i = 0;
	private String pName;
	protected String host;
	protected String db;
	protected String username;
	protected String password;
	
	public Database(String host, String db, String username, String password, Netstats plugin) {
		this.plugin = plugin;
		this.pName = plugin.pName;
		this.log = plugin.log;
		this.host = host;
		this.db = db;
		this.username = username;
		this.password = password;
	}
	
	private Connection connection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection("jdbc:mysql://"+host+"/"+db, username, password);
	}
	
	// Check if a player has any data
	public boolean hasData(String name) {
		boolean has = false;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			con = connection();
			ps = con.prepareStatement("SELECT total FROM netstats WHERE player = ? LIMIT 1");
			ps.setString(1, name);
			rs = ps.executeQuery();
			has = rs.next();
		} catch (SQLException ex) {
			log.severe("["+pName+"]: Could not fetch data for mysql: "+ex);
			return false;
		} catch (ClassNotFoundException e) {
			log.severe("["+pName+"]: Database connector not found for mysql: "+e);
			return false;
		} finally {
			try	{
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				log.severe("["+pName+"]: Failed to close connection.");
			}
		}
		return has;
	}
	
	// Global update function which will be based off a string passed to it
	public void update(String sql) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = connection();
			ps = con.prepareStatement(sql);
			ps.executeUpdate();
		} catch (SQLException ex) {
			log.severe("["+pName+"]: Could not set data for "+ex);
			return;
		} catch (ClassNotFoundException e) {
			log.severe("["+pName+"]: Database connector not found for mysql: "+e);
			return;
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				log.severe("["+pName+"]: Failed to close connection.");
			}
		}
	}
	
	// Register the new user
	public void register(String name, long time, String ip) {
		Connection con = null;
		PreparedStatement ps = null;
		try	{
			con = connection();
			ps = con.prepareStatement("INSERT INTO netstats (id, player, enter, seen, total, logged, ip, broken, placed, deaths) VALUES(null, ?, ?, ?, 0, 1, ?, 0, 0, 0)");
			ps.setString(1, name);
			ps.setLong(2, time);
			ps.setLong(3, time);
			ps.setString(4, ip);
			ps.executeUpdate();
		} catch (SQLException ex) {
			log.severe("["+pName+"]: Could not set data for "+ex);
			return;
		} catch (ClassNotFoundException e) {
			log.severe("["+pName+"]: Database connector not found for "+e);
			return;
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				log.severe("["+pName+"]: Failed to close connection.");
			}
		}
	}
}
package com.sparkedia.jdev19.Netstats;

import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
	protected static final Logger log = Logger.getLogger("Minecraft");
	public Type database;
	public int i = 0;
	private String host;
	private String db;
	private String username;
	private String password;
	
	public Database(Type database, String host, String db, String username, String password) {
		this.database = database;
		this.host = host;
		this.db = db;
		this.username = username;
		this.password = password;
	}
	
	private Connection connection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection("jdbc:mysql://"+host+"/"+db, username, password);
	}

	public boolean hasData(String name) {
		boolean has = false;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			con = connection();
			ps = con.prepareStatement("SELECT total FROM players WHERE name = ? LIMIT 1");
			ps.setString(1, name);
			rs = ps.executeQuery();
			has = rs.next();
		} catch (SQLException ex) {
			log.severe("[Netstats]: Could not fetch data for mysql: "+ex);
			return false;
		} catch (ClassNotFoundException e) {
			log.severe("[Netstats]: Database connector not found for mysql: "+e);
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
				log.severe("[Netstats]: Failed to close connection");
			}
		}
		return has;
	}
	
	public void update(String name, long broken, long placed, long now, long total) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = connection();
			con = connection();
			ps = con.prepareStatement("UPDATE players SET broken = broken+?, placed = placed+?, total = total+?, enter = ? WHERE name = ?");
			ps.setLong(1, broken);
			ps.setLong(2, placed);
			ps.setLong(3, total);
			ps.setLong(4, now);
			ps.setString(5, name);
			ps.executeUpdate();
		} catch (SQLException ex) {
			log.severe("[Netstats]: Could not set data for "+ex);
			return;
		} catch (ClassNotFoundException e) {
			log.severe("[Netstats]: Database connector not found for mysql: "+e);
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
				log.severe("[Netstats]: Failed to close connection.");
			}
		}
	}
	
	public void join(String name, long time, String ip) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = connection();
			ps = con.prepareStatement("UPDATE players SET enter = ?, logout = ?, status = 1, ip = ? WHERE name = ?");
			ps.setLong(1, time);
			ps.setLong(2, time);
			ps.setString(3, ip);
			ps.setString(4, name);
			ps.executeUpdate();
		} catch (SQLException ex) {
			log.severe("[Netstats]: Could not set data for "+ex);
			return;
		} catch (ClassNotFoundException e) {
			log.severe("[Netstats]: Database connector not found for mysql: "+e);
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
				log.severe("[Netstats]: Failed to close connection.");
			}
		}
	}
	
	public void leave(String name, long broken, long placed, long now, long total) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = connection();
			ps = con.prepareStatement("UPDATE players SET logout = ?, status = 0, total = total+?, broken = broken+?, placed = placed+? WHERE name = ?");
			ps.setLong(1, now);
			ps.setLong(2, total);
			ps.setLong(3, broken);
			ps.setLong(4, placed);
			ps.setString(5, name);
			ps.executeUpdate();
		} catch (SQLException ex) {
			log.severe("[Netstats]: Could not set data for "+ex);
			return;
		} catch (ClassNotFoundException e) {
			log.severe("[Netstats]: Database connector not found for mysql: "+e);
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
				log.severe("[Netstats]: Failed to close connection.");
			}
		}
	}
	
	//register the new user
	public void register(String name, long time, String ip) {
		Connection con = null;
		PreparedStatement ps = null;
		try	{
			con = connection();
			ps = con.prepareStatement("INSERT INTO players (id, name, enter, logout, total, status, ip) VALUES(null, ?, ?, ?, 0, 1, ?)");
			ps.setString(1, name);
			ps.setLong(2, time);
			ps.setLong(3, time);
			ps.setString(4, ip);
			ps.executeUpdate();
		} catch (SQLException ex) {
			log.severe("[Netstats]: Could not set data for "+ex);
			return;
		} catch (ClassNotFoundException e) {
			log.severe("[Netstats]: Database connector not found for "+e);
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
				log.severe("[Netstats]: Failed to close connection.");
			}
		}
	}

	public static enum Type
	{
		MYSQL;
	}
}
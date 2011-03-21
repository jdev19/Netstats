package com.sparkedia.valrix.Netstats;

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
	protected String table;
	
	public Database(String host, String db, String username, String password, String table, Netstats plugin) {
		this.plugin = plugin;
		this.pName = plugin.pName;
		this.log = plugin.log;
		this.host = host;
		this.db = db;
		this.username = username;
		this.password = password;
		this.table = table;
		// If the table doesn't exist, build() will make it, otherwise it'll update it
		build();
	}
	
	private Connection connection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection("jdbc:mysql://"+host+'/'+db, username, password);
	}
	
	// Check if a player has any data
	public boolean hasData(String name) {
		boolean has = false;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			con = connection();
			ps = con.prepareStatement("SELECT total FROM "+table+" WHERE player=? LIMIT 1");
			ps.setString(1, name);
			rs = ps.executeQuery();
			has = rs.next();
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Could not fetch data, MySQL error for: "+ex);
			return false;
		} catch (ClassNotFoundException e) {
			log.severe('['+pName+"]: Database connector not found, MySQL error for: "+e);
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
				log.severe('['+pName+"]: Failed to close connection.");
			}
		}
		return has;
	}
	
	public void rename(String oldTable, String newTable) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = connection();
			String sql = "RENAME TABLE "+oldTable+" TO "+newTable+';';
			ps = con.prepareStatement(sql);
			ps.execute();
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Could not rename table, MySQL error for: "+ex);
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector not found, MySQL error for: "+ex);
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
				log.severe('['+pName+"]: Failed to close connection.");
			}
		}
	}
	
	// Global update function which will be based off a string passed to it
	public void update(String sql) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = connection();
			ps = con.prepareStatement("UPDATE "+table+" SET "+sql);
			ps.executeUpdate();
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Could not set data, MySQL error for: "+ex);
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector not found, MySQL error for: "+ex);
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
				log.severe('['+pName+"]: Failed to close connection.");
			}
		}
	}
	
	// Register the new user
	public void register(String name, long time, String ip) {
		Connection con = null;
		PreparedStatement ps = null;
		try	{
			con = connection();
			ps = con.prepareStatement("INSERT INTO "+table+" (id, player, enter, seen, total, logged, ip, broken, placed, deaths, mobskilled, playerskilled, joindate) VALUES(null, ?, ?, ?, 0, 1, ?, 0, 0, 0, 0, 0, ?)");
			ps.setString(1, name);
			ps.setLong(2, time);
			ps.setLong(3, time);
			ps.setString(4, ip);
			ps.setLong(5, time);
			ps.executeUpdate();
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Could not set data, MySQL error for: "+ex);
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector not found, MySQL error for: "+ex);
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
				log.severe('['+pName+"]: Failed to close connection.");
			}
		}
	}
	
	// Verify table meets newest standards (just like the config)
	public void build() {
		Connection con = null;
		PreparedStatement ps = null;
		try	{
			con = connection();
			// Create the table if it doesn't exist
			String sql = "CREATE TABLE IF NOT EXISTS "+table+" (";
			sql += "id int(11) NOT NULL AUTO_INCREMENT, ";
			sql += "player varchar(50) NOT NULL, ";
			sql += "enter bigint(20) NOT NULL DEFAULT '0', ";
			sql += "seen bigint(20) NOT NULL DEFAULT '0', ";
			sql += "total bigint(20) NOT NULL DEFAULT '0', ";
			sql += "logged tinyint(1) NOT NULL DEFAULT '0', ";
			sql += "ip varchar(40) NOT NULL, ";
			sql += "broken int(11) NOT NULL DEFAULT '0', ";
			sql += "placed int(11) NOT NULL DEFAULT '0', ";
			sql += "deaths int(11) NOT NULL DEFAULT '0', ";
			sql += "mobskilled int(11) NOT NULL DEFAULT '0', ";
			sql += "playerskilled int(11) NOT NULL DEFAULT '0', ";
			sql += "joindate bigint(20) NOT NULL DEFAULT '0', ";
			sql += "PRIMARY KEY (id)) ENGINE=MyISAM DEFAULT CHARSET=latin1";
			ps = con.prepareStatement(sql);
			ps.execute();
			
			// Now run the script to check if it's updated
			ps = con.prepareStatement("ALTER TABLE `"+table+"` ADD `mobskilled` int(11) NOT NULL DEFAULT '0' AFTER `deaths`");
			ps.execute();
			ps = con.prepareStatement("ALTER TABLE `"+table+"` ADD `playerskilled` int(11) NOT NULL DEFAULT '0' AFTER `mobskilled`");
			ps.execute();
			ps = con.prepareStatement("ALTER TABLE `"+table+"` ADD `joindate` bigint(20) NOT NULL DEFAULT '0' AFTER `playerskilled`");
			ps.execute();
		} catch (SQLException exc) {
			if (exc.getCause() == null) {
				try {
					ps = con.prepareStatement("ALTER TABLE `"+table+"` ADD `playerskilled` int(11) NOT NULL DEFAULT '0' AFTER `mobskilled`");
					ps.execute();
					ps = con.prepareStatement("ALTER TABLE `"+table+"` ADD `joindate` bigint(20) NOT NULL DEFAULT '0' AFTER `playerskilled`");
					ps.execute();
				} catch (SQLException ex) {
					if (ex.getCause() == null) {
						try {
							ps = con.prepareStatement("ALTER TABLE `"+table+"` ADD `joindate` bigint(20) NOT NULL DEFAULT '0' AFTER `playerskilled`");
							ps.execute();
						} catch (SQLException e) {
							if (e.getCause() != null) {
								log.severe('['+pName+"]: Could not set data, MySQL error for: "+e);
								return;
							}
						}
					} else {
						log.severe('['+pName+"]: Could not set data, MySQL error for: "+ex);
						return;
					}
				}
			} else {
				log.severe('['+pName+"]: Could not set data, MySQL error for: "+exc);
				return;
			}
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector not found, MySQL error for: "+ex);
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
				log.severe('['+pName+"]: Failed to close connection.");
			}
		}
	}
}
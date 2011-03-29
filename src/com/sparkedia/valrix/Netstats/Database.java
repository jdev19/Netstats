package com.sparkedia.valrix.Netstats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class Database {
	protected Netstats plugin;
	private Logger log;
	public int i = 0;
	private String pName;
	private String pFolder;
	protected String host;
	protected String db;
	protected String username;
	protected String password;
	protected String table;
	
	public Database(String host, String db, String username, String password, String table, Netstats plugin) {
		this.plugin = plugin;
		this.pName = plugin.pName;
		this.pFolder = plugin.pFolder;
		this.log = plugin.log;
		this.host = host;
		this.db = db;
		this.username = username;
		this.password = password;
		this.table = table;
		// If the table doesn't exist, build() will make it, otherwise it'll update it
		build();
	}

	private final static String getDateTime() {
		Calendar c = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("MMddhhmmss");  
		df.setTimeZone(c.getTimeZone());  
		return df.format(new Date());  
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
			log.severe('['+pName+"]: Severe database error. Saving error log to "+pFolder+"/logs");
			ErrorLog err = new ErrorLog(pFolder+"/logs/NetErr_"+getDateTime()+".log", plugin);
			err.setString("CraftBukkit Version", plugin.getServer().getVersion());
			err.setString(pName+" Version", plugin.getDescription().getVersion());
			err.setString("MySQL Error", ex.toString());
			try {
				err.setString("Offending Statement", rs.getStatement().toString());
			} catch (SQLException e) {
			}
			err.save();
			return false;
		} catch (ClassNotFoundException e) {
			log.severe('['+pName+"]: Database connector wasn't found. Make sure it's in your /lib/ folder.");
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
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
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
			ps.executeQuery();
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Failed to rename table "+oldTable+" to "+newTable+". Does "+oldTable+" exist?");
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector wasn't found. Make sure it's in your /lib/ folder.");
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
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
			}
		}
	}
	
	// Global update function which will be based off a string passed to it
	public void update(String sql) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = connection();
			sql = "UPDATE "+table+" SET "+sql;
			String[] sqls = sql.split(";");
			sql = sqls[0];
			ps = con.prepareStatement(sql);
			ps.executeUpdate();
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Severe database error. Saving error log to "+pFolder+"/logs");
			ErrorLog err = new ErrorLog(pFolder+"/logs/NetErr_"+getDateTime()+".log", plugin);
			err.setString("CraftBukkit Version", plugin.getServer().getVersion());
			err.setString(pName+" Version", plugin.getDescription().getVersion());
			err.setString("MySQL Error", ex.toString());
			err.setString("Offending Statement", sql);
			err.save();
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector wasn't found. Make sure it's in your /lib/ folder.");
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
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
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
			log.severe('['+pName+"]: Failed to register user to database. Saving error log to "+pFolder+"/logs");
			ErrorLog err = new ErrorLog(pFolder+"/logs/NetErr_"+getDateTime()+".log", plugin);
			err.setString("CraftBukkit Version", plugin.getServer().getVersion());
			err.setString(pName+" Version", plugin.getDescription().getVersion());
			err.setString("MySQL Error", ex.toString());
			err.save();
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector wasn't found. Make sure it's in your /lib/ folder.");
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
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
			}
		}
	}
	
	// Run a general query, this is for those few odd circumstances
	public void query(String sql) {
		Connection con = null;
		try {
			con = connection();
			String[] sqls = sql.split(";");
			sql = sqls[0];
			con.prepareStatement(sql).execute();
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Severe database error. Saving error log to "+pFolder+"/logs");
			ErrorLog err = new ErrorLog(pFolder+"/logs/NetErr_"+getDateTime()+".log", plugin);
			err.setString("CraftBukkit Version", plugin.getServer().getVersion());
			err.setString(pName+" Version", plugin.getDescription().getVersion());
			err.setString("MySQL Error", ex.toString());
			err.setString("Offending Statement", sql);
			err.save();
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector wasn't found. Make sure it's in your /lib/ folder.");
			return;
		} finally {
			try {
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
			}
		}
	}
	
	public void build() {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "";
		try	{
			// Return a list of the columns, we'll add the ones that aren't there
			con = connection();
			// Create the table if it doesn't exist
			sql = "CREATE TABLE IF NOT EXISTS "+table+" (";
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
			sql += "distance double NOT NULL DEFAULT '0', ";
			sql += "PRIMARY KEY (id)) ENGINE=MyISAM DEFAULT CHARSET=latin1";
			ps = con.prepareStatement(sql);
			ps.execute();
			con = connection();
			ps = con.prepareStatement("SELECT * FROM `"+table+"`");
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount()+1;
			// Add tables if they're not there
			for (int i = 1; i < count; i++) {
				if (i == (count-1) && rsmd.getColumnName(i).equals("deaths")) {
					con.prepareStatement("ALTER TABLE `"+table+"` ADD `mobskilled` int(11) NOT NULL DEFAULT '0' AFTER `deaths`").execute();
					con.prepareStatement("ALTER TABLE `"+table+"` ADD `playerskilled` int(11) NOT NULL DEFAULT '0' AFTER `mobskilled`").execute();
					con.prepareStatement("ALTER TABLE `"+table+"` ADD `joindate` bigint(20) NOT NULL DEFAULT '0' AFTER `playerskilled`").execute();
					con.prepareStatement("ALTER TABLE `"+table+"` ADD `distance` double NOT NULL DEFAULT '0' AFTER `joindate`").execute();
					break;
				}
				if (i == (count-1) && rsmd.getColumnName(i).equals("mobskilled")) {
					con.prepareStatement("ALTER TABLE `"+table+"` ADD `playerskilled` int(11) NOT NULL DEFAULT '0' AFTER `mobskilled`").execute();
					con.prepareStatement("ALTER TABLE `"+table+"` ADD `joindate` bigint(20) NOT NULL DEFAULT '0' AFTER `playerskilled`").execute();
					con.prepareStatement("ALTER TABLE `"+table+"` ADD `distance` double NOT NULL DEFAULT '0' AFTER `joindate`").execute();
					break;
				}
				if (i == (count-1) && rsmd.getColumnName(i).equals("playerskilled")) {
					con.prepareStatement("ALTER TABLE `"+table+"` ADD `joindate` bigint(20) NOT NULL DEFAULT '0' AFTER `playerskilled`").execute();
					con.prepareStatement("ALTER TABLE `"+table+"` ADD `distance` double NOT NULL DEFAULT '0' AFTER `joindate`").execute();
					break;
				}
				if (i == (count-1) && rsmd.getColumnName(i).equals("joindate")) {
					con.prepareStatement("ALTER TABLE `"+table+"` ADD `distance` double NOT NULL DEFAULT '0' AFTER `joindate`").execute();
					break;
				}
			}
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Severe database error. Saving error log to "+pFolder+"/logs");
			ErrorLog err = new ErrorLog(pFolder+"/logs/NetErr_"+getDateTime()+".log", plugin);
			err.setString("CraftBukkit Version", plugin.getServer().getVersion());
			err.setString(pName+" Version", plugin.getDescription().getVersion());
			err.setString("MySQL Error", ex.toString());
			err.save();
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector wasn't found. Make sure it's in your /lib/ folder.");
			return;
		} finally {
			try {
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
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
			}
		}
	}
}
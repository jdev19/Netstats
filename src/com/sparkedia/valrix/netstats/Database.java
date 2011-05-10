package com.sparkedia.valrix.netstats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
	protected Netstats plugin;
	private Logger log;
	public int i = 0;
	private String pName;
	private String logs;
	protected String host;
	protected String db;
	protected String username;
	protected String password;

	public Database(String host, String db, String username, String password, Netstats plugin) {
		this.plugin = plugin;
		this.pName = plugin.pName;
		this.logs = plugin.logs;
		this.log = plugin.log;
		this.host = host;
		this.db = db;
		this.username = username;
		this.password = password;
		build();
	}

	private final static String getDateTime() {
		Calendar c = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("MMddhhmmss");
		df.setTimeZone(c.getTimeZone());
		return df.format(new Date());
	}

	private Connection connection() throws ClassNotFoundException, SQLException {
		// Class.forName("com.mysql.jdbc.Driver");
		Class.forName("java.sql.Driver");
		return DriverManager.getConnection("jdbc:mysql://"+host+'/'+db, username, password);
	}

	// Check if a player has any data
	public boolean hasData(String name) {
		boolean has = false;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			con = connection();
			st = con.createStatement();
			rs = st.executeQuery("SELECT `total` FROM `"+plugin.table+"` WHERE player='"+name+"' LIMIT 1");
			has = rs.next();
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Severe database error. Saving error log to "+logs);
			ErrorLog err = new ErrorLog(logs+"/NetErr_"+getDateTime()+".log", plugin);
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
			log.severe('['+pName+"]: Database connector wasn't found.");
			return false;
		} finally {
			try {
				if (rs != null) rs.close();
				if (st != null) st.close();
				if (con != null) con.close();
			} catch (SQLException ex) {
				log.severe('['+pName+"]: Failed to close database connection. Is it already closed?");
			}
		}
		return has;
	}

	// Global update function which will be based off a string passed to it
	public void update(String sql) {
		Connection con = null;
		Statement st = null;
		try {
			con = connection();
			st = con.createStatement();
			sql = "UPDATE `"+plugin.table+"` SET "+sql;
			String[] sqls = sql.split(";");
			sql = sqls[0];
			st.execute(sql);
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Severe database error. Saving error log to "+logs);
			ErrorLog err = new ErrorLog(logs+"/NetErr_"+getDateTime()+".log", plugin);
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
				if (st != null) st.close();
				if (con != null) con.close();
			} catch (SQLException ex) {
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
			}
		}
	}

	// Register the new user
	public void register(String name, long time, String ip) {
		Connection con = null;
		Statement st = null;
		try {
			con = connection();
			st = con.createStatement();
			st.execute("INSERT INTO `"+plugin.table+"` (`id`, `player`, `enter`, `seen`, `total`, `logged`, `ip`, `broken`, `placed`, `deaths`, `mobskilled`, `playerskilled`, `joindate`) VALUES(null, '"+name+"', '"+time+"', '"+time+"', '0', '1', '"+ip+"', '0', '0', '0', '0', '0', '"+time+"')");
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Failed to register user to database. Saving error log to "+logs);
			ErrorLog err = new ErrorLog(logs+"/NetErr_"+getDateTime()+".log", plugin);
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
				if (st != null) st.close();
				if (con != null) con.close();
			} catch (SQLException ex) {
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
			}
		}
	}

	// Run a general query
	public void query(String sql) {
		Connection con = null;
		Statement st = null;
		try {
			con = connection();
			st = con.createStatement();
			String[] sqls = sql.split(";");
			sql = sqls[0];
			st.executeUpdate(sql);
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Severe database error. Saving error log to "+logs);
			ErrorLog err = new ErrorLog(logs+"/NetErr_"+getDateTime()+".log", plugin);
			err.setString("CraftBukkit Version", plugin.getServer().getVersion());
			err.setString(pName+" Version", plugin.getDescription().getVersion());
			err.setString("MySQL Error", ex.toString());
			err.setString("Offending Statement", sql);
			err.save();
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector wasn't found.");
			return;
		} finally {
			try {
				if (st != null) st.close();
				if (con != null) con.close();
			} catch (SQLException ex) {
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
			}
		}
	}
	
	// Run multiple queries at one to reduce database calls
	public void queries(String sqls) {
		Connection con = null;
		Statement st = null;
		String SQL = "";
		try {
			con = connection();
			st = con.createStatement();
			for (String sql : sqls.split(";")) {
				SQL = sql;
				st.executeUpdate(sql);
			}
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Severe database error. Saving error log to "+logs);
			ErrorLog err = new ErrorLog(logs+"/NetErr_"+getDateTime()+".log", plugin);
			err.setString("CraftBukkit Version", plugin.getServer().getVersion());
			err.setString(pName+" Version", plugin.getDescription().getVersion());
			err.setString("MySQL Error", ex.toString());
			err.setString("Offending Statement", SQL);
			err.save();
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector wasn't found.");
			return;
		} finally {
			try {
				if (st != null) st.close();
				if (con != null) con.close();
			} catch (SQLException ex) {
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
			}
		}
	}

	// Function to wipe the database
	public void wipe() {
		Connection con = null;
		Statement st = null;
		String sql = "";
		try {
			con = connection();
			st = con.createStatement();
			// Set nearly everything back to 0 (zero)
			sql = "UPDATE `"+plugin.table+"` SET enter=0, seen=0, total=0, logged=0, broken=0, placed=0, deaths=0, mobskilled=0, playerskilled=0, distance=0;";
			st.execute(sql);
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Severe database error. Saving error log to "+logs);
			ErrorLog err = new ErrorLog(logs+"/NetErr_"+getDateTime()+".log", plugin);
			err.setString("CraftBukkit Version", plugin.getServer().getVersion());
			err.setString(pName+" Version", plugin.getDescription().getVersion());
			err.setString("MySQL Error", ex.toString());
			err.setString("Offending Statement", sql);
			err.save();
			return;
		} catch (ClassNotFoundException ex) {
			log.severe('['+pName+"]: Database connector wasn't found.");
			return;
		} finally {
			try {
				if (st != null) st.close();
				if (con != null) con.close();
			} catch (SQLException ex) {
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
			}
		}
	}

	public void build() {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = "";
		try {
			// Try building the database
			if (db.trim().length() < 1) {
				con = connection();
				st = con.createStatement();
				db = "NetStats";
				plugin.conf.setString("database", db);
				plugin.conf.save();
				st.executeUpdate("CREATE DATABASE `"+db+"` DEFAULT CHARACTER SET `utf8`;");
			}
			con = connection();
			st = con.createStatement();
			// Create the table if it doesn't exist
			sql = "CREATE TABLE IF NOT EXISTS `"+plugin.table+"` ("
				+"`id` int(11) NOT NULL AUTO_INCREMENT,"
				+"`player` varchar(50) NOT NULL,"
				+"`enter` bigint(20) NOT NULL DEFAULT '0',"
				+"`seen` bigint(20) NOT NULL DEFAULT '0',"
				+"`total` bigint(20) NOT NULL DEFAULT '0',"
				+"`logged` tinyint(1) NOT NULL DEFAULT '0',"
				+"`ip` varchar(40) NOT NULL,"
				+"`broken` int(11) NOT NULL DEFAULT '0',"
				+"`placed` int(11) NOT NULL DEFAULT '0',"
				+"`deaths` int(11) NOT NULL DEFAULT '0',"
				+"`mobskilled` int(11) NOT NULL DEFAULT '0',"
				+"`playerskilled` int(11) NOT NULL DEFAULT '0',"
				+"`joindate` bigint(20) NOT NULL DEFAULT '0',"
				+"`distance` double NOT NULL DEFAULT '0',"
				+"PRIMARY KEY (`id`)"
				+") ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;";
			st.executeUpdate(sql);
			// Is Database charset in UTF-8? If not, we're going to change it.
			rs = st.executeQuery("SHOW VARIABLES LIKE \"character_set_database\"");
			int it = 1;
			String cs = "";
			String co = "";
			while (rs.next()) {
				if (rs.last()) {
					cs = rs.getString(it);
					break;
				}
				it++;
			}
			// Also make sure the collation is UTF-8
			rs = st.executeQuery("SHOW VARIABLES LIKE \"collation_database\"");
			it = 1;
			while (rs.next()) {
				if (rs.last()) {
					cs = rs.getString(it);
					break;
				}
				it++;
			}
			if (!cs.equalsIgnoreCase("utf8")|| !co.equalsIgnoreCase("utf8_general_ci")) {
				st.execute("ALTER DATABASE `"+db+"` DEFAULT CHARSET 'utf8' COLLATE 'utf8_general_ci';");
				st.execute("ALTER TABLE `"+plugin.table+"` DEFAULT CHARSET 'utf8' COLLATE 'utf8_general_ci';");
			}
			// ALTER END
		} catch (SQLException ex) {
			log.severe('['+pName+"]: Severe database error. Saving error log to "+logs);
			ErrorLog err = new ErrorLog(logs+"/NetErr_"+getDateTime()+".log", plugin);
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
				if (rs != null)	rs.close();
				if (st != null)	st.close();
				if (con != null) con.close();
			} catch (SQLException ex) {
				log.severe('['+pName+"]: Failed to close database connection connection. Is it already closed?");
			}
		}
	}
}
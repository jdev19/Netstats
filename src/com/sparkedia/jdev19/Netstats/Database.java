package com.sparkedia.jdev19.Netstats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
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
			ps = con.prepareStatement("SELECT total FROM players WHERE name = ?" + (this.database.equals(Type.SQLITE) ? "" : " LIMIT 1"));
			ps.setString(1, name);
			rs = ps.executeQuery();
			has = rs.next();
		} catch (SQLException ex) {
			System.out.println("[Netstats]: Could not fetch data for " + (this.database.equals(Type.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
			return false;
		} catch (ClassNotFoundException e) {
			System.out.println("[Netstats]: Database connector not found for " + (this.database.equals(Type.SQLITE) ? "sqlite" : "mysql") + ": " + e);
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
				System.out.println("[Netstats]: Failed to close connection");
			}
		}
		return has;
	}

	public void setData(String name, long time, String dest) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			con = connection();

			if (hasData(name)) {
				if (dest == "enter") {
					ps = con.prepareStatement("UPDATE players SET enter = ?, logout = ?, status = 1 WHERE name = ?");
					ps.setLong(1, time);
					ps.setLong(2, time);
					ps.setString(3, name);
					ps.executeUpdate();
				} else if (dest == "leave") {
					ps = con.prepareStatement("UPDATE players SET logout = ?, status = 0 WHERE name = ?");
					ps.setLong(1, time);
					ps.setString(2, name);
					ps.executeUpdate();
					ps = con.prepareStatement("SELECT enter, total FROM players WHERE name = ?" + (this.database.equals(Type.SQLITE) ? "" : " LIMIT 1"));
					ps.setString(1, name);
					rs = ps.executeQuery();
					if (rs.next()) {
						long enter = rs.getLong("enter");
						long total = rs.getLong("total");
						total = total+(time-enter);
						ps = con.prepareStatement("UPDATE players SET total = ? WHERE name = ?");
						ps.setLong(1, total);
						ps.setString(2, name);
						ps.executeUpdate();
					}
				}
			} else {
				ps = con.prepareStatement("INSERT INTO players (id, name, enter, logout, total, status) VALUES(null,?,?,?,0,1)");
				ps.setString(1, name);
				ps.setLong(2, time);
				ps.setLong(3, time);
				ps.executeUpdate();
			}
		} catch (SQLException ex) {
			System.out.println("[Netstats]: Could not set data for " + (this.database.equals(Type.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
			return;
		} catch (ClassNotFoundException e) {
			System.out.println("[Netstats]: Database connector not found for " + (this.database.equals(Type.SQLITE) ? "sqlite" : "mysql") + ": " + e);
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
				System.out.println("[Netstats]: Failed to close connection.");
			}
		}
	}

	public static enum Type
	{
		SQLITE, 
		MYSQL;
	}
}
package com.sparkedia.jdev19.Netstats;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerEvent;

public class NetPlayerListener extends PlayerListener {
	protected Netstats plugin;
	protected Database db;
	private String pFolder;
	private HashMap<String, Property> users;
	private HashMap<String, Object> config;
	
	public NetPlayerListener(Netstats plugin) {
		this.plugin = plugin;
		this.pFolder = plugin.pFolder;
		this.users = plugin.users;
		this.config = plugin.config;
		this.db = plugin.db;
	}
	
	public void onPlayerJoin(PlayerEvent e) {
		Player player = e.getPlayer();
		String name = player.getName();
		Property prop;
		String sql;
		// Use previous format of storing a user and their propfile to users
		if (!(new File(pFolder+"/players/"+name+".stats")).exists()) {
			users.put(name, new Property(pFolder+"/players/"+name+".stats", plugin));
			prop = users.get(name);
			prop.setInt("broken", 0);
			prop.setInt("placed", 0);
			prop.setInt("deaths", 0);
			prop.setLong("enter", 0);
			prop.setLong("seen", 0);
			prop.setLong("total", 0);
		} else {
			users.put(name, new Property(pFolder+"/players/"+name+".stats", plugin));
			prop = users.get(name);
		}
		// There's already some user data, let's save it and refresh their join data
		if (prop.getInt("broken") != 0 || prop.getInt("placed") != 0 || prop.getInt("deaths") != 0) {
			long now = System.currentTimeMillis();
			InetSocketAddress IP = player.getAddress();
			int port = IP.getPort();
			String ip = IP.toString().replace("/", "").replace(":"+port, "");
			sql = "UPDATE netstats SET ";
			sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+" " : "";
			sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+" " : "";
			sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+" " : "";
			sql += "enter="+now+" seen="+now+" status=1 ";
			sql += ((Boolean)config.get("trackIP")) ? "ip="+ip+" " : "";
			sql += "total="+prop.getLong("total")+" WHERE name="+name;
			db.update(sql);
			prop.setInt("broken", 0);
			prop.setInt("placed", 0);
			prop.setInt("deaths", 0);
			prop.setLong("enter", now);
			prop.setLong("seen", now);
		} else {
			// No previous data (good!), do everything like normal
			InetSocketAddress IP = player.getAddress();
			int port = IP.getPort();
			String ip = IP.toString().replace("/", "");
			ip = ip.replace(":"+port, "");
			long now = System.currentTimeMillis();
			// Player has been on the server before, else register them to database
			if (db.hasData(name)) {
				// UPDATE enter, seen, status, and ip
				sql = "UPDATE netstats SET enter="+now+" seen="+now+" status=1 ";
				sql += ((Boolean)config.get("trackIP")) ? "ip="+ip+" " : "";
				sql += "WHERE name="+name;
				db.update(sql);
				prop.setLong("enter", now);
				prop.setLong("seen", now);
			} else {
				db.register(name, now, ip);
				prop.setLong("enter", now);
				prop.setLong("seen", now);
			}
		}
	}
	
	public void onPlayerQuit(PlayerEvent e) {
		long now = System.currentTimeMillis();
		String name = e.getPlayer().getName();
		Property prop = users.get(name);
		prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
		String sql;
		if (users.containsKey(name)) {
			users.put(name, new Property(pFolder+"/players/"+name+".stats", plugin));
		}
		// Store all data to database
		sql = "UPDATE netstats SET ";
		sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+" " : "";
		sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+" " : "";
		sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+" " : "";
		sql += "total="+prop.getLong("total")+" ";
		sql += "status=0 WHERE name="+name;
		db.update(sql);
		prop.setInt("broken", 0);
		prop.setInt("placed", 0);
		prop.setInt("deaths", 0);
		users.remove(name);
	}
	
	public void onPlayerKick(PlayerEvent e) {
		long now = System.currentTimeMillis();
		String name = e.getPlayer().getName();
		Property prop = users.get(name);
		prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
		String sql;
		if (users.containsKey(name)) {
			users.put(name, new Property(pFolder+"/players/"+name+".stats", plugin));
		}
		// Store all data to database
		sql = "UPDATE netstats SET ";
		sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+" " : "";
		sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+" " : "";
		sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+" " : "";
		sql += "total="+prop.getLong("total")+" ";
		sql += "status=0 WHERE name="+name;
		db.update(sql);
		prop.setInt("broken", 0);
		prop.setInt("placed", 0);
		prop.setInt("deaths", 0);
		users.remove(name);
	}
}
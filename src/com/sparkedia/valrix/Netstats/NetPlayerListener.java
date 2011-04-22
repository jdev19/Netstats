package com.sparkedia.valrix.Netstats;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public class NetPlayerListener extends PlayerListener {
	protected Netstats plugin;
	protected Database db;
	private String players;
	private HashMap<String, Property> users;
	private LinkedHashMap<String, Object> config;
	private int action;
	
	public NetPlayerListener(Netstats plugin) {
		this.plugin = plugin;
		this.players = plugin.players;
		this.users = plugin.users;
		this.config = plugin.config;
		this.action = (Integer)config.get("actions");
		this.db = plugin.db;
	}

    @Override
	public void onPlayerMove(PlayerMoveEvent e) {
		if (!e.isCancelled()) {
			Player player = e.getPlayer();
			String name = player.getName();
			Vector from = e.getFrom().toVector();
			Vector to = e.getTo().toVector();
			double distance = to.distance(from);
			if (!users.containsKey(name)) {
				// They reloaded the plugins, time to re-set the player property files
				users.put(name, new Property(plugin.getCanonFile(players+'/'+name+".stats"), plugin));
				plugin.actions.put(name, (action/2));
			}
			Property prop = users.get(name);
			prop.setDouble("distance", prop.getDouble("distance")+distance);
		}
	}
	
    @Override
	public void onPlayerJoin(PlayerJoinEvent e) {
		long now = System.currentTimeMillis();
		InetSocketAddress IP = e.getPlayer().getAddress();
		String[] ips = IP.toString().split("/");
		String ip = ips[1].replace(":"+IP.getPort(), "");
		String name = e.getPlayer().getName();
		Property prop;
		String sql = "";
		String statfile = plugin.getCanonFile(players+'/'+name+".stats");
		// Use previous format of storing a user and their propfile to users
		if (!(new File(statfile)).exists()) {
			users.put(name, new Property(statfile, plugin));
			plugin.actions.put(name, 0);
			prop = users.get(name);
			prop.setLong("enter", 0);
			prop.setLong("seen", 0);
			prop.setLong("total", 0);
			prop.setInt("broken", 0);
			prop.setInt("placed", 0);
			prop.setInt("deaths", 0);
			prop.setInt("mobsKilled", 0);
			prop.setInt("playersKilled", 0);
			prop.setDouble("distance", 0);
			prop.save();
		} else {
			users.put(name, new Property(statfile, plugin));
			plugin.actions.put(name, 0);
			prop = users.get(name);
		}
		// There's already some user data, let's save it and refresh their join data
		if (prop.getInt("broken") > 0 || prop.getInt("placed") > 0 || prop.getInt("deaths") > 0 || prop.getDouble("distance") > 0 || prop.getLong("total") > 0) {
			sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+", " : "";
			sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+", " : "";
			sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+", " : "";
			sql += (prop.getInt("mobsKilled") > 0) ? "mobskilled=mobskilled+"+prop.getInt("mobsKilled")+", " : "";
			sql += (prop.getInt("playersKilled") > 0) ? "playerskilled=playerskilled+"+prop.getInt("playersKilled")+", " : "";
			sql += (prop.getDouble("distance") > 0) ? "distance=distance+"+prop.getDouble("distance")+", " : "";
			sql += "enter="+now+", seen="+now+", ";
			sql += ((Boolean)config.get("trackIP")) ? "ip='"+ip+"', " : "";
			sql += "total=total+"+(prop.getLong("total")+(now-prop.getLong("seen")))+", logged=1 WHERE player='"+name+"';";
			db.update(sql);
			prop.setInt("broken", 0);
			prop.setInt("placed", 0);
			prop.setInt("deaths", 0);
			prop.setInt("mobsKilled", 0);
			prop.setInt("playersKilled", 0);
			prop.setDouble("distance", 0);
			prop.setLong("enter", now);
			prop.setLong("seen", now);
			prop.setLong("total", 0);
			prop.save();
		} else {
			// No previous data (good!), do everything like normal
			// Player has been on the server before, else register them to database
			if (db.hasData(name)) {
				// UPDATE enter, seen, status, and ip
				sql += "enter="+now+", seen="+now;
				sql += ((Boolean)config.get("trackIP")) ? ", ip='"+ip+"'" : "";
				sql += ", logged=1 WHERE player='"+name+"';";
				db.update(sql);
				prop.setLong("enter", now);
				prop.setLong("seen", now);
				prop.save();
			} else {
				db.register(name, now, ip);
				prop.setLong("enter", now);
				prop.setLong("seen", now);
				prop.save();
			}
		}
	}
	
    @Override
	public void onPlayerQuit(PlayerQuitEvent e) {
		long now = System.currentTimeMillis();
		String name = e.getPlayer().getName();
		if (!users.containsKey(name)) {
			users.put(name, new Property(plugin.getCanonFile(players+'/'+name+".stats"), plugin));
		}
		Property prop = users.get(name);
		String sql = "";
		// Store all data to database
		sql += "seen="+now+", ";
		sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+", " : "";
		sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+", " : "";
		sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+", " : "";
		sql += (prop.getInt("mobsKilled") > 0) ? "mobskilled=mobskilled+"+prop.getInt("mobsKilled")+", " : "";
		sql += (prop.getInt("playersKilled") > 0) ? "playerskilled=playerskilled+"+prop.getInt("playersKilled")+", " : "";
		sql += (prop.getDouble("distance") > 0) ? "distance=distance+"+prop.getDouble("distance")+", " : "";
		sql += "seen="+now+", total=total+"+(prop.getLong("total")+(now-prop.getLong("seen")))+", logged=0 WHERE player='"+name+"';";
		db.update(sql);
		prop.setInt("broken", 0);
		prop.setInt("placed", 0);
		prop.setInt("deaths", 0);
		prop.setInt("mobsKilled", 0);
		prop.setInt("playersKilled", 0);
		prop.setDouble("distance", 0);
		prop.setLong("seen", 0);
		prop.setLong("total", 0);
		prop.save();
		users.remove(name);
		if (plugin.actions.containsKey(name)) {
			plugin.actions.remove(name);
		}
	}
	
	public void onPlayerKick(PlayerEvent e) {
		long now = System.currentTimeMillis();
		String name = e.getPlayer().getName();
		if (!users.containsKey(name)) {
			users.put(name, new Property(plugin.getCanonFile(players+'/'+name+".stats"), plugin));
		}
		Property prop = users.get(name);
		String sql = "";
		// Store all data to database
		sql += "seen="+now+", ";
		sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+", " : "";
		sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+", " : "";
		sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+", " : "";
		sql += (prop.getInt("mobsKilled") > 0) ? "mobskilled=mobskilled+"+prop.getInt("mobsKilled")+", " : "";
		sql += (prop.getInt("playersKilled") > 0) ? "playerskilled=playerskilled+"+prop.getInt("playersKilled")+", " : "";
		sql += (prop.getDouble("distance") > 0) ? "distance=distance+"+prop.getDouble("distance")+", " : "";
		sql += "seen="+now+", total=total+"+(prop.getLong("total")+(now-prop.getLong("seen")))+", logged=0 WHERE player='"+name+"';";
		db.update(sql);
		prop.setInt("broken", 0);
		prop.setInt("placed", 0);
		prop.setInt("deaths", 0);
		prop.setInt("mobsKilled", 0);
		prop.setInt("playersKilled", 0);
		prop.setDouble("distance", 0);
		prop.setLong("seen", now);
		prop.setLong("total", 0);
		prop.save();
		users.remove(name);
		if (plugin.actions.containsKey(name)) {
			plugin.actions.remove(name);
		}
	}
}
package com.sparkedia.valrix.Netstats;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class NetRepeater implements Runnable {
	protected Netstats plugin;
	protected Database db;
	private HashMap<String, Property> users;
	private HashMap<String, Integer> actions;
	
	public NetRepeater(Netstats plugin) {
		this.plugin = plugin;
		this.db = plugin.db;
		this.users = plugin.users;
		this.actions = plugin.actions;
	}

	public void run() {
		// Only do something if there are actually players online
		if (users.size() > 0) {
			long now = System.currentTimeMillis();
			Set<?> set = users.entrySet();
			Iterator<?> i = set.iterator();
			String name;
			String sql = "";
			Property prop;
			while (i.hasNext()) {
				Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
				name = (String)me.getKey();
				prop = (Property)me.getValue();
				// Update Property file
				prop = users.get(name);
				prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
				prop.setLong("seen", now);
				prop.save();
				// Update database
				sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+", " : "";
				sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+", " : "";
				sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+", " : "";
				sql += (prop.getInt("mobsKilled") > 0) ? "mobskilled=mobskilled+"+prop.getInt("mobsKilled")+", " : "";
				sql += (prop.getInt("playersKilled") > 0) ? "playerskilled=playerskilled+"+prop.getInt("playersKilled")+", " : "";
				sql += (prop.getDouble("distance") > 0) ? "distance=distance+"+prop.getDouble("distance")+", " : "";
				sql += "seen="+now+", total="+prop.getLong("total")+" WHERE player='"+name+"';";
				db.update(sql);
				actions.put(name, 0);
				// Reset everything (not time based) in property file since we just updated the DB
				prop.setInt("broken", 0);
				prop.setInt("placed", 0);
				prop.setInt("deaths", 0);
				prop.setInt("mobsKilled", 0);
				prop.setInt("playersKilled", 0);
				prop.setDouble("distance", 0);
				prop.save();
			}
		}
	}
	
	public void out() {
		long now = System.currentTimeMillis();
		Set<?> set = users.entrySet();
		Iterator<?> i = set.iterator();
		String name;
		String sql = "";
		Property prop;
		while (i.hasNext()) {
			Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
			name = (String)me.getKey();
			prop = (Property)me.getValue();
			// Update Property file
			prop = users.get(name);
			prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
			prop.setLong("seen", now);
			prop.save();
			// Update database
			sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+", " : "";
			sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+", " : "";
			sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+", " : "";
			sql += (prop.getInt("mobsKilled") > 0) ? "mobskilled=mobskilled+"+prop.getInt("mobsKilled")+", " : "";
			sql += (prop.getInt("playersKilled") > 0) ? "playerskilled=playerskilled+"+prop.getInt("playersKilled")+", " : "";
			sql += (prop.getDouble("distance") > 0) ? "distance=distance+"+prop.getDouble("distance")+", " : "";
			sql += "seen="+now+", total="+prop.getLong("total")+", logged=0 WHERE player='"+name+"';";
			db.update(sql);
			actions.put(name, 0);
			// Reset everything (not time based) in property file since we just updated the DB
			prop.setInt("broken", 0);
			prop.setInt("placed", 0);
			prop.setInt("deaths", 0);
			prop.setInt("mobsKilled", 0);
			prop.setInt("playersKilled", 0);
			prop.setDouble("distance", 0);
			prop.save();
			// Remove data as if they logged out
			users.remove(name);
			actions.remove(name);
		}
	}
}

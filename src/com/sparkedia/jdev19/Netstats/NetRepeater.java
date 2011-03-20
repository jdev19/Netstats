package com.sparkedia.jdev19.Netstats;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NetRepeater implements Runnable {
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
			String sql;
			Property prop;
			while (i.hasNext()) {
				Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
				name = (String)me.getKey();
				prop = (Property)me.getValue();
				// Update Property file
				prop = users.get(name);
				prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
				prop.setLong("seen", now);
				// Update database
				sql = "UPDATE netstats SET ";
				sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+", " : "";
				sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+", " : "";
				sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+", " : "";
				sql += "seen="+prop.getLong("seen")+", total="+prop.getLong("total")+" WHERE player='"+name+"';";
				db.update(sql);
				actions.put(name, 0);
				// Reset everything (not time based) in property file since we just updated the DB
				prop.setInt("broken", 0);
				prop.setInt("placed", 0);
				prop.setInt("deaths", 0);
			}
		}
	}
}

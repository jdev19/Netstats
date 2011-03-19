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
		Set<?> set = users.entrySet();
		Iterator<?> i = set.iterator();
		String name;
		Property prop;
		while (i.hasNext()) {
			Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
			name = (String)me.getKey();
			prop = (Property)me.getValue();
			// Update flatfile
			// TODO Continue...
			// Update database
			db.update(sql);
			actions.put(name, 0);
		}
	}
}

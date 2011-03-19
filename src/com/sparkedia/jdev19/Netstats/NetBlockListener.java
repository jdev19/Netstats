package com.sparkedia.jdev19.Netstats;

import java.util.HashMap;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class NetBlockListener extends BlockListener {
	protected Netstats plugin;
	private HashMap<String, Property> users;
	private HashMap<String, Integer> actions;
	private String pFolder;
	protected HashMap<String, Object> config;
	protected String host;
	protected String database;
	protected String username;
	protected String password;
	public int updateRate;
	protected Database db;
	
	public NetBlockListener(Netstats plugin) {
		this.plugin = plugin;
		this.pFolder = plugin.pFolder;
		this.config = plugin.config;
		this.actions = plugin.actions;
		this.updateRate = (Integer)config.get("updateRate");
		this.db = plugin.db;
	}
	
	public void onBlockBreak(BlockBreakEvent e) {
		String name = e.getPlayer().getName();
		if (!users.containsKey(name)) {
			// They reloaded the plugins, time to re-set the player property files
			users.put(name, new Property(pFolder+"/players/"+name+".stats", plugin));
		}
		Property prop = users.get(name);
		int count = actions.get(name)+1;
		prop.inc("broken"); // Add 1 to broken
		if (count == updateRate) {
			long now = System.currentTimeMillis();
			String sql = "UPDATE netstats SET ";
			sql += "broken="+prop.getInt("broken")+" ";
			sql += (prop.getInt("placed") > 0) ? "placed="+prop.getInt("placed")+" " : "";
			sql += (prop.getInt("deaths") > 0) ? "deaths="+prop.getInt("deaths")+" " : "";
			sql += "seen="+prop.getLong("seen")+" ";
			sql += "total="+(prop.getLong("total")+(now-prop.getLong("seen")));
			sql += " WHERE name="+name;
			db.update(sql);
			// Reset data data back to nothing except enter and total
			prop.setInt("broken", 0);
			prop.setInt("placed", 0);
			prop.setInt("deaths", 0);
			prop.setLong("seen", now);
			// Reset watched actions back to 0 (zero)
			actions.put(name, 0);
		} else {
			// Update timestamp
			long now = System.currentTimeMillis();
			prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
			prop.setLong("seen", now);
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent e) {
		String name = e.getPlayer().getName();
		if (!users.containsKey(name)) {
			// Plugin is reset, make sure to re-set the property files
			users.put(name, new Property(pFolder+"/players/"+name+".stats", plugin));
		}
		Property prop = users.get(name);
		int count = actions.get(name)+1;
		prop.inc("placed");
		if (count == updateRate) {
			long now = System.currentTimeMillis();
			String sql = "UPDATE netstats SET ";
			sql += "placed="+prop.getInt("placed")+" ";
			sql += (prop.getInt("broken") > 0) ? "broken="+prop.getInt("broken")+" " : "";
			sql += (prop.getInt("deaths") > 0) ? "deaths="+prop.getInt("deaths")+" " : "";
			sql += "seen="+prop.getLong("seen")+" ";
			sql += "total="+(prop.getLong("total")+(now-prop.getLong("seen")));
			sql += " WHERE name="+name;
			db.update(sql);
			// Reset data data back to nothing except enter and total
			prop.setInt("broken", 0);
			prop.setInt("placed", 0);
			prop.setInt("deaths", 0);
			prop.setLong("seen", now);
			// Reset watched actions back to 0 (zero)
			actions.put(name, 0);
		} else {
			// Update timestamp
			long now = System.currentTimeMillis();
			prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
			prop.setLong("seen", now);
		}
	}
}

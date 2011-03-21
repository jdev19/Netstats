package com.sparkedia.valrix.Netstats;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class NetEntityListener extends EntityListener {
	protected Netstats plugin;
	private String pFolder;
	private HashMap<String, Property> users;
	private HashMap<String, Integer> actions;
	public LinkedHashMap<String, Object> config;
	private int updateRate;
	
	public NetEntityListener(Netstats plugin) {
		this.plugin = plugin;
		this.users = plugin.users;
		this.actions = plugin.actions;
		this.pFolder = plugin.pFolder;
		this.config = plugin.config;
		this.updateRate = (Integer)config.get("updateRate");
	}
	
	public void onEntityDeath(EntityDeathEvent e) {
		Entity entity = e.getEntity();
		if (entity instanceof Player) {
			String name = ((Player)entity).getName();
			if (!users.containsKey(name)) {
				users.put(name, new Property(pFolder+"/players/"+name+".stats", plugin));
			}
			users.get(name).inc("deaths");
		}
	}
	
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		long now = System.currentTimeMillis();
		Entity victim = e.getEntity();
		Entity damager = e.getDamager();
		if (victim instanceof Monster && damager instanceof Player && (Boolean)config.get("trackMonsterKills")) {
			if (((Monster)victim).getHealth() <= 0) {
				String name = ((Player)damager).getName();
				// Player killed a monster, save it to player's stats file
				Property prop = users.get(name);
				String sql = "";
				prop.inc("mobsKilled");
				if (actions.get(name) == updateRate) {
					prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
					prop.setLong("seen", now);
					prop.save();
					// Update database
					sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+", " : "";
					sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+", " : "";
					sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+", " : "";
					sql += (prop.getInt("mobsKilled") > 0) ? "mobskilled=mobskilled+"+prop.getInt("mobsKilled")+", " : "";
					sql += (prop.getInt("playersKilled") > 0) ? "playerskilled=playerskilled+"+prop.getInt("playersKilled")+", " : "";
					sql += "seen="+prop.getLong("seen")+", total="+prop.getLong("total")+" WHERE player='"+name+"';";
					plugin.db.update(sql);
					actions.put(name, 0);
					// Reset everything (not time based) in property file since we just updated the DB
					prop.setInt("broken", 0);
					prop.setInt("placed", 0);
					prop.setInt("deaths", 0);
					prop.setInt("mobsKilled", 0);
					prop.setInt("playersKilled", 0);
					prop.save();
				}
			}
		} else if (victim instanceof Player && damager instanceof Player && (Boolean)config.get("trackPlayerKills")) {
			if (((Player)victim).getHealth() <= 0) {
				String name = ((Player)damager).getName();
				// Player killed a player, save it to player's stats file
				Property prop = users.get(name);
				String sql = "";
				prop.inc("playersKilled");
				if (actions.get(name) == updateRate) {
					prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
					prop.setLong("seen", now);
					prop.save();
					// Update database
					sql += (prop.getInt("broken") > 0) ? "broken=broken+"+prop.getInt("broken")+", " : "";
					sql += (prop.getInt("placed") > 0) ? "placed=placed+"+prop.getInt("placed")+", " : "";
					sql += (prop.getInt("deaths") > 0) ? "deaths=deaths+"+prop.getInt("deaths")+", " : "";
					sql += (prop.getInt("mobsKilled") > 0) ? "mobskilled=mobskilled+"+prop.getInt("mobsKilled")+", " : "";
					sql += (prop.getInt("playersKilled") > 0) ? "playerskilled=playerskilled+"+prop.getInt("playersKilled")+", " : "";
					sql += "seen="+prop.getLong("seen")+", total="+prop.getLong("total")+" WHERE player='"+name+"';";
					plugin.db.update(sql);
					actions.put(name, 0);
					// Reset everything (not time based) in property file since we just updated the DB
					prop.setInt("broken", 0);
					prop.setInt("placed", 0);
					prop.setInt("deaths", 0);
					prop.setInt("mobsKilled", 0);
					prop.setInt("playersKilled", 0);
					prop.save();
				}
			}
		}
	}
}

package com.sparkedia.jdev19.Netstats;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class NetEntityListener extends EntityListener {
	protected Netstats plugin;
	
	public NetEntityListener(Netstats plugin) {
		this.plugin = plugin;
	}
	
	public void onEntityDeath(EntityDeathEvent e) {
		Entity entity = e.getEntity();
		if (entity instanceof Player) {
			String name = ((Player)entity).getName();
			if (!plugin.users.containsKey(name)) {
				plugin.users.put(name, new Property(plugin.pFolder+"/players/"+name+".stats", plugin));
			}
			plugin.users.get(name).inc("deaths");
		}
	}
}

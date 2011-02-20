package com.sparkedia.jdev19.Netstats;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class NetEntityListener extends EntityListener {
	public Netstats plugin;
	
	public NetEntityListener(Netstats plugin) {
		this.plugin = plugin;
	}
	
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			int deaths = Netstats.userProp.getInt("deaths");
			deaths++;
			Netstats.userProp.setInt("deaths", deaths);
		}
	}
}

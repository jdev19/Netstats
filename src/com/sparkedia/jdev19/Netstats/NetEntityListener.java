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
	
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			if (Netstats.userProp == null) {
				Netstats.userProp = new Property("plugins/"+plugin.pName+"/players/"+((Player)entity).getName()+".stats", plugin);
			}
			int deaths = Netstats.userProp.getInt("deaths");
			deaths++;
			Netstats.userProp.setInt("deaths", deaths);
		}
	}
}

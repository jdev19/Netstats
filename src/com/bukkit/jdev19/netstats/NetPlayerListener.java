package com.bukkit.jdev19.netstats;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerEvent;

public class NetPlayerListener extends PlayerListener {
	public static netstats plugin;
	public NetPlayerListener(netstats instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerEvent event) {
		Player player = event.getPlayer();
		long date = System.currentTimeMillis();
		player.sendMessage("Welcome "+player.getName()+"! The date is: "+date);
	}
}

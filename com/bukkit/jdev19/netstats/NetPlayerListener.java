package com.bukkit.jdev19.netstats;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

public class NetPlayerListener extends PlayerListener {
	public static netstats plugin;
	public NetPlayerListener(netstats instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		player.sendMessage("Welcome "+player.getName()+"!");
	}
}

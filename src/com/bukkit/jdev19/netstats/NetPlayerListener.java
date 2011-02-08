package com.bukkit.jdev19.Netstats;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerEvent;

public class NetPlayerListener extends PlayerListener {
	public static Netstats plugin;
	private static Property propfile = new Property("plugins/Netstats/netstats.properties");
	private static String host = propfile.getString("host");
	private static String database = propfile.getString("database");
	private static String username = propfile.getString("username");
	private static String password = propfile.getString("password");
	private static Database db = new Database(Database.Type.MYSQL, host, database, username, password);
	
	
	public NetPlayerListener(Netstats instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerEvent event) {
		Player player = event.getPlayer();
		long time = System.currentTimeMillis();
		db.setData(player.getName(), time, "enter");
	}
	
	public void onPlayerQuit(PlayerEvent event) {
		Player player = event.getPlayer();
		long time = System.currentTimeMillis();
		db.setData(player.getName(), time, "leave");
	}
}

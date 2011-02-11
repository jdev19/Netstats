package com.sparkedia.jdev19.Netstats;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerEvent;

public class NetPlayerListener extends PlayerListener {
	public static Netstats plugin;
	private static Property propfile = Netstats.properties;
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
	
	public void onPlayerKick(PlayerEvent event) {
		Player player = event.getPlayer();
		long time = System.currentTimeMillis();
		db.setData(player.getName(), time, "leave");
	}
	
	public void onPlayerCommand(PlayerChatEvent event) {
		String[] split = event.getMessage().split(" ");
		Player player = event.getPlayer();
		if (split[0].equalsIgnoreCase("/netstats") && (split[1].equalsIgnoreCase("load") || (split[1].equalsIgnoreCase("-l")))) {
			player.sendMessage("Reloading config file...done.");
			propfile.load();
		}
	}
}

package com.sparkedia.jdev19.Netstats;

import java.net.InetSocketAddress;

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
		InetSocketAddress IP = player.getAddress();
		db.setData(player.getName(), time, "enter", IP);
	}
	
	public void onPlayerQuit(PlayerEvent event) {
		Player player = event.getPlayer();
		long time = System.currentTimeMillis();
		InetSocketAddress IP = player.getAddress();
		db.setData(player.getName(), time, "leave", IP);
	}
	
	public void onPlayerKick(PlayerEvent event) {
		Player player = event.getPlayer();
		long time = System.currentTimeMillis();
		InetSocketAddress IP = player.getAddress();
		db.setData(player.getName(), time, "leave", IP);
	}
}

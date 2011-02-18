package com.sparkedia.jdev19.Netstats;

import java.net.InetSocketAddress;

import org.bukkit.entity.Player;
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
		Netstats.userProp = new Property("plugins/Netstats/players/"+player.getName()+".stats");
		long time = System.currentTimeMillis();
		InetSocketAddress IP = player.getAddress();
		int port = IP.getPort();
		String ip = IP.toString().replace("/", "");
		ip = ip.replace(":"+port, "");
		if (db.hasData(player.getName())) {
			db.join(player.getName(), time, ip);
			Netstats.userProp.setLong("enter", time);
		} else {
			db.register(player.getName(), time, ip);
			Netstats.userProp.setLong("enter", time);
		}
	}
	
	public void onPlayerQuit(PlayerEvent event) {
		Player player = event.getPlayer();
		//grab propfile data and store it all
		long broken = Netstats.userProp.getLong("broken");
		long placed = Netstats.userProp.getLong("placed");
		long enter = Netstats.userProp.getLong("enter");
		long total = Netstats.userProp.getLong("total");
		int deaths = Netstats.userProp.getInt("deaths");
		long now = System.currentTimeMillis();
		total = total+(now-enter);
		db.leave(player.getName(), broken, placed, now, total, deaths);
	}
	
	public void onPlayerKick(PlayerEvent event) {
		Player player = event.getPlayer();
		//grab propfile data and store it all
		long broken = Netstats.userProp.getLong("broken");
		long placed = Netstats.userProp.getLong("placed");
		long enter = Netstats.userProp.getLong("enter");
		long total = Netstats.userProp.getLong("total");
		int deaths = Netstats.userProp.getInt("deaths");
		long now = System.currentTimeMillis();
		total = total+(now-enter);
		db.leave(player.getName(), broken, placed, now, total, deaths);
	}
}
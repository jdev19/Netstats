package com.sparkedia.jdev19.Netstats;

import java.io.File;
import java.net.InetSocketAddress;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerEvent;

public class NetPlayerListener extends PlayerListener {
	public Netstats plugin;
	private static String host = Netstats.properties.getString("host");
	private static String database = Netstats.properties.getString("database");
	private static String username = Netstats.properties.getString("username");
	private static String password = Netstats.properties.getString("password");
	private static Database db = new Database(Database.Type.MYSQL, host, database, username, password);
	
	
	public NetPlayerListener(Netstats plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerJoin(PlayerEvent event) {
		Player player = event.getPlayer();
		if (!(new File("plugins/Netstats/players/"+player.getName()+".stats")).exists()) {
			Netstats.userProp = new Property("plugins/Netstats/players/"+player.getName()+".stats");
			Netstats.userProp.setLong("broken", 0);
			Netstats.userProp.setLong("placed", 0);
			Netstats.userProp.setLong("total", 0);
			Netstats.userProp.setLong("deaths", 0);
		} else {
			Netstats.userProp = new Property("plugins/Netstats/players/"+player.getName()+".stats");
		}
		if (Netstats.userProp.getLong("broken") != 0 || Netstats.userProp.getLong("placed") != 0 || Netstats.userProp.getLong("total") != 0 || Netstats.userProp.getInt("deaths") != 0) {
			player.sendMessage("Previous data was found");
			long broken = Netstats.userProp.getLong("broken");
			long placed = Netstats.userProp.getLong("placed");
			long total = Netstats.userProp.getLong("total");
			int deaths = Netstats.userProp.getInt("deaths");
			db.smUpdate(player.getName(), broken, placed, total, deaths);
			Netstats.userProp.setLong("broken", 0);
			Netstats.userProp.setLong("placed", 0);
			Netstats.userProp.setLong("total", 0);
			Netstats.userProp.setLong("deaths", 0);
		}
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
		if (Netstats.userProp == null) {
			Netstats.userProp = new Property("plugins/Netstats/players/"+event.getPlayer().getName()+".stats");
		}
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
		Netstats.userProp.setLong("broken", 0);
		Netstats.userProp.setLong("placed", 0);
		Netstats.userProp.setLong("total", 0);
		Netstats.userProp.setInt("deaths", 0);
	}
	
	public void onPlayerKick(PlayerEvent event) {
		if (Netstats.userProp == null) {
			Netstats.userProp = new Property("plugins/Netstats/players/"+event.getPlayer().getName()+".stats");
		}
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
		Netstats.userProp.setLong("broken", 0);
		Netstats.userProp.setLong("placed", 0);
		Netstats.userProp.setLong("total", 0);
		Netstats.userProp.setInt("deaths", 0);
	}
}
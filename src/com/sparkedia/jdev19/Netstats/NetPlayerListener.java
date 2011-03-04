package com.sparkedia.jdev19.Netstats;

import java.io.File;
import java.net.InetSocketAddress;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerEvent;

public class NetPlayerListener extends PlayerListener {
	protected Netstats plugin;
	protected Database db;
	private String pName;
	
	public NetPlayerListener(Netstats plugin) {
		this.plugin = plugin;
		this.pName = plugin.pName;
		String host = Netstats.config.getString("host");
		String database = Netstats.config.getString("database");
		String username = Netstats.config.getString("username");
		String password = Netstats.config.getString("password");
		this.db = new Database(Database.Type.MYSQL, host, database, username, password, plugin);
	}
	
	public void onPlayerJoin(PlayerEvent event) {
		Player player = event.getPlayer();
		if (!(new File("plugins/Netstats/players/"+player.getName()+".stats")).exists()) {
			Netstats.userProp = new Property("plugins/"+pName+"/players/"+player.getName()+".stats", plugin);
			Netstats.userProp.setLong("broken", 0);
			Netstats.userProp.setLong("placed", 0);
			Netstats.userProp.setLong("total", 0);
			Netstats.userProp.setLong("deaths", 0);
		} else {
			Netstats.userProp = new Property("plugins/"+pName+"/players/"+player.getName()+".stats", plugin);
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
			Netstats.userProp = new Property("plugins/"+pName+"/players/"+event.getPlayer().getName()+".stats", plugin);
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
			Netstats.userProp = new Property("plugins/"+pName+"/players/"+event.getPlayer().getName()+".stats", plugin);
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
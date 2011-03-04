package com.sparkedia.jdev19.Netstats;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class NetBlockListener extends BlockListener {
	protected Netstats plugin;
	private String pName;
	protected Property config;
	protected String host;
	protected String database;
	protected String username;
	protected String password;
	public int updateRate;
	protected Database db;
	public int actions = 0;
	public int broken = 0;
	public int placed = 0;
	
	public NetBlockListener(Netstats plugin) {
		this.plugin = plugin;
		this.pName = plugin.pName;
		this.config = Netstats.config;
		this.host = config.getString("host");
		this.database = config.getString("database");
		this.username = config.getString("username");
		this.password = config.getString("password");
		this.updateRate = config.getInt("updateRate");
		this.db = new Database(Database.Type.MYSQL, host, database, username, password, plugin);
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		if (Netstats.userProp == null) {
			//They reloaded the plugins, time to re-set the player property files
			Netstats.userProp = new Property("plugins/"+pName+"/players/"+event.getPlayer().getName()+".stats", plugin);
		}
		actions++;
		broken++;
		if (actions == updateRate) {
			Player player = event.getPlayer();
			//update data before store
			Netstats.userProp.setLong("broken", broken);
			//save data from propfile and reset it
			long tbreaks = Netstats.userProp.getLong("broken");
			long tplaced = Netstats.userProp.getLong("placed");
			int deaths = Netstats.userProp.getInt("deaths");
			long now = System.currentTimeMillis();
			long total = Netstats.userProp.getLong("total");
			long enter = Netstats.userProp.getLong("enter");
			total = total+(now-enter);
			db.update(player.getName(), tbreaks, tplaced, now, total, deaths);
			//reset propfile data back to nothing
			Netstats.userProp.setLong("broken", 0);
			Netstats.userProp.setLong("placed", 0);
			Netstats.userProp.setLong("enter", now);
			Netstats.userProp.setLong("total", 0);
			Netstats.userProp.setInt("deaths", 0);
			//reset watched actions
			actions = 0;
			broken = 0;
			placed = 0;
		} else {
			//get data from propfile and set new data
			long enter = Netstats.userProp.getLong("enter");
			long total = Netstats.userProp.getLong("total");
			long now = System.currentTimeMillis();
			total = total+(now-enter);
			Netstats.userProp.setLong("total", total);
			Netstats.userProp.setLong("enter", now);
			Netstats.userProp.setLong("broken", broken);
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event) {
		if (Netstats.userProp == null) {
			//Plugins reset, make sure to re-set the property files
			Netstats.userProp = new Property("plugins/"+pName+"/players/"+event.getPlayer().getName()+".stats", plugin);
		}
		actions++;
		placed++;
		if (actions == updateRate) {
			Player player = event.getPlayer();
			//update before store
			Netstats.userProp.setLong("placed", placed);
			//save data from propfile and reset it
			long tbreaks = Netstats.userProp.getLong("broken");
			long tplaced = Netstats.userProp.getLong("placed");
			int deaths = Netstats.userProp.getInt("deaths");
			long now = System.currentTimeMillis();
			long total = Netstats.userProp.getLong("total");
			long enter = Netstats.userProp.getLong("enter");
			total = total+(now-enter);
			db.update(player.getName(), tbreaks, tplaced, now, total, deaths);
			//reset propfile data back to nothing
			Netstats.userProp.setLong("broken", 0);
			Netstats.userProp.setLong("placed", 0);
			Netstats.userProp.setLong("enter", now);
			Netstats.userProp.setLong("total", 0);
			Netstats.userProp.setInt("deaths", 0);
			//reset watched data
			actions = 0;
			broken = 0;
			placed = 0;
		} else {
			//get data from propfile and set new data
			long enter = Netstats.userProp.getLong("enter");
			long total = Netstats.userProp.getLong("total");
			long now = System.currentTimeMillis();
			total = total+(now-enter);
			Netstats.userProp.setLong("total", total);
			Netstats.userProp.setLong("enter", now);
			Netstats.userProp.setLong("placed", placed);
		}
	}
}

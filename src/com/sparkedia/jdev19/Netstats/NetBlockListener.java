package com.sparkedia.jdev19.Netstats;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class NetBlockListener extends BlockListener {
	public Netstats plugin;
	private static Property propfile = Netstats.properties;
	private static String host = propfile.getString("host");
	private static String database = propfile.getString("database");
	private static String username = propfile.getString("username");
	private static String password = propfile.getString("password");
	public int updateRate = propfile.getInt("updateRate");
	public int actions = 0;
	public int broken = 0;
	public int placed = 0;
	private static Database db = new Database(Database.Type.MYSQL, host, database, username, password);
	
	public NetBlockListener(Netstats plugin) {
		this.plugin = plugin;
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
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

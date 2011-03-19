package com.sparkedia.jdev19.Netstats;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class Netstats extends JavaPlugin {
	private NetPlayerListener playerListener;
	private NetBlockListener blockListener;
	private NetEntityListener entityListener;
	public Logger log = Logger.getLogger("Minecraft");
	public HashMap<String, Object> config;
	public HashMap<String, Property> users; // <Name, Propfile>
	public HashMap<String, Integer> actions;
	public String pName;
	public String pFolder;
	public Database db;
	
	public void onDisable() {
		PluginDescriptionFile pdf = this.getDescription();
		log.info("["+pName+"] v"+pdf.getVersion()+" has been disabled.");
	}
	
	public void onEnable() {
		pFolder = this.getDataFolder().toString();
		// Log that the plugin has been enabled
		PluginDescriptionFile pdf = this.getDescription();
		pName = pdf.getName();
		log.info("["+pName+"] v"+pdf.getVersion()+" has been enabled.");
		
		// Check if players folder exists or create it
		if (!(new File(pFolder+"/players").isDirectory())) {
			(new File(pFolder+"/players")).mkdir();
		}
		// Store the config file
		if (config == null) {
			//Does config exist, if not then make a new one and add defaults
			if (!(new File(pFolder+"/config.txt").exists())) {
				Property conf = new Property(pFolder+"/config.txt", this);
				conf.setString("host", "");
				conf.setString("database", "");
				conf.setString("username", "");
				conf.setString("password", "");
				conf.setInt("actions", 32);
				conf.setInt("updateRate", 1800); // About 90 seconds in ticks
				// Now set option tracking options
				conf.setBoolean("trackIP", true);
				conf.setBoolean("trackBroken", true);
				conf.setBoolean("trackPlaced", true);
				conf.setBoolean("trackDeaths", true);
				log.severe("["+pName+"] Your config isn't set up. Creating one and disabling "+pName+".");
				this.getPluginLoader().disablePlugin(this);
			} else {
				db = new Database((String)config.get("host"), (String)config.get("database"), (String)config.get("username"), (String)config.get("password"), this);
				Property conf = new Property(pFolder+"/config.txt", this);
				config.put("host", conf.getString("host"));
				config.put("database", conf.getString("database"));
				config.put("username", conf.getString("username"));
				config.put("password", conf.getString("password"));
				config.put("actions", conf.getInt("actions"));
				config.put("updateRate", conf.getInt("updateRate"));
				config.put("trackIP", conf.getBoolean("trackIP"));
				config.put("trackBroken", conf.getBoolean("trackBroken"));
				config.put("trackPlaced", conf.getBoolean("trackPlaced"));
				config.put("trackDeaths", conf.getBoolean("trackDeaths"));
			}
		}
		
		PluginManager pm = getServer().getPluginManager();

		// Register player events
		playerListener = new NetPlayerListener(this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_KICK, this.playerListener, Event.Priority.Normal, this);
		
		// Register block events
		if ((Boolean)config.get("trackBroken") || (Boolean)config.get("trackPlaced")) {
			blockListener = new NetBlockListener(this);
			if ((Boolean)config.get("trackBroken")) {
				pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Normal, this);
			}
			if ((Boolean)config.get("trackPlaced")) {
				pm.registerEvent(Event.Type.BLOCK_PLACED, this.blockListener, Event.Priority.Normal, this);
			}
		}

		// Register entity events (death)
		if ((Boolean)config.get("trackDeaths")) {
			entityListener = new NetEntityListener(this);
			pm.registerEvent(Event.Type.ENTITY_DEATH, this.entityListener, Event.Priority.Normal, this);
		}
		
		if ((Integer)config.get("updateRate") > 0) {
			int rate = (Integer)config.get("updateRate");
			this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new NetRepeater(this), rate, rate);
		}
	}
}

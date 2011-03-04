package com.sparkedia.jdev19.Netstats;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class Netstats extends JavaPlugin {
	private NetPlayerListener playerListener;
	private NetBlockListener blockListener;
	private NetEntityListener entityListener;
	protected static final Logger log = Logger.getLogger("Minecraft");
	public static Property config = null;
	public static Property userProp;
	public String pName;
	
	public void onDisable() {
		PluginDescriptionFile pdf = this.getDescription();
		log.info("["+pName+"] v"+pdf.getVersion()+" has been disabled.");
	}
	
	public void onEnable() {
		// Log that the plugin has been enabled
		PluginDescriptionFile pdf = this.getDescription();
		pName = pdf.getName();
		log.info("["+pName+"] v"+pdf.getVersion()+" has been enabled.");
		
		// Check if players folder exists or create it
		if (!(new File("plugins/"+pName+"/players").isDirectory())) {
			(new File("plugins/"+pName+"/players")).mkdir();
		}
		// Store the config file
		if (config == null) {
			//Does config exist, if not then make a new one and add defaults
			if (!(new File("plugins/"+pName+"/players").isDirectory())) {
				config = new Property("plugins/"+pName+"/config.txt", this);
				config.setString("host", "");
				config.setString("database", "");
				config.setString("username", "");
				config.setString("password", "");
				config.setInt("updateRate", 32);
			} else {
				config = new Property("plugins/"+pName+"/config.txt", this);
			}
		}
		
		playerListener = new NetPlayerListener(this);
		blockListener = new NetBlockListener(this);
		entityListener = new NetEntityListener(this);
		
		PluginManager pm = getServer().getPluginManager();
		// Register player events
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_KICK, this.playerListener, Event.Priority.Normal, this);
		// Register block events
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACED, this.blockListener, Event.Priority.Normal, this);
		// Register death event
		pm.registerEvent(Event.Type.ENTITY_DEATH, this.entityListener, Event.Priority.Normal, this);
	}
}

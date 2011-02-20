package com.sparkedia.jdev19.Netstats;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class Netstats extends JavaPlugin {
	private final NetPlayerListener playerListener = new NetPlayerListener(this);
	private final NetBlockListener blockListener = new NetBlockListener(this);
	private final NetEntityListener entityListener = new NetEntityListener(this);
	protected static final Logger log = Logger.getLogger("Minecraft");
	public static Property properties = new Property("plugins/Netstats/config.txt");
	public static Property userProp;
	
	public void onDisable() {
		PluginDescriptionFile pdf = this.getDescription();
		log.info("["+pdf.getName()+"] v"+pdf.getVersion()+" has been disabled.");
	}
	
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_KICK, this.playerListener, Event.Priority.Normal, this);
		
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACED, this.blockListener, Event.Priority.Normal, this);
		
		pm.registerEvent(Event.Type.ENTITY_DEATH, this.entityListener, Event.Priority.Normal, this);
		
		PluginDescriptionFile pdf = this.getDescription();
		log.info("["+pdf.getName()+"] v"+pdf.getVersion()+" has been enabled.");
		
		if (!(new File("plugins/Netstats").isDirectory())) {
			(new File("plugins/Netstats")).mkdir();
		}
		if (!(new File("plugins/Netstats/players").isDirectory())) {
			(new File("plugins/Netstats/players")).mkdir();
		}
	}
}

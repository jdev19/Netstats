package com.bukkit.jdev19.Netstats;

import java.io.File;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class Netstats extends JavaPlugin {
	private final NetPlayerListener playerListener = new NetPlayerListener(this);
	public static Property properties = new Property("plugins/Netstats/config.yml");
	
	public Netstats(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdf = this.getDescription();
		System.out.println("["+pdf.getName()+"] v"+pdf.getVersion()+" has been disabled.");
	}
	
	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_KICK, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND, this.playerListener, Event.Priority.Normal, this);
		
		PluginDescriptionFile pdf = this.getDescription();
		System.out.println("["+pdf.getName()+"] v"+pdf.getVersion()+" has been enabled.");
	}
}

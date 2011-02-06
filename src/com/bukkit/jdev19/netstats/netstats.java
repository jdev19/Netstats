package com.bukkit.jdev19.netstats;

import java.io.File;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class netstats extends JavaPlugin {
	private final NetPlayerListener playerListener = new NetPlayerListener(this);
	
	public netstats(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}
	
	@Override
	public void onDisable() {
		System.out.println("netstats Disabled");
	}
	
	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_COMMAND, this.playerListener, Event.Priority.Normal, this);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName()+" version "+pdfFile.getVersion()+" is enabled.");
	}
}

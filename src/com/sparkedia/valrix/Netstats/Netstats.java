package com.sparkedia.valrix.Netstats;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class Netstats extends JavaPlugin {
	private NetPlayerListener playerListener;
	private NetBlockListener blockListener;
	private NetEntityListener entityListener;
	public Logger log;
	public LinkedHashMap<String, Object> config;
	public HashMap<String, Property> users  = new HashMap<String, Property>(); // <Name, Propfile>
	public HashMap<String, Integer> actions = new HashMap<String, Integer>();
	public String pName;
	public String df; // Data Folder
	public String players;
	public String logs;
	public String lib;
	public Database db;
	private NetRepeater runner;
	private Boolean disabled = false;
	
	public String getCanonPath(String dir) {
		String cp = null;
		try {
			cp = new File(dir).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cp;
	}
	
	public String getCanonFile(String file) {
		String cf = null;
		try {
			cf = new File(file).getCanonicalFile().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cf;
	}
	
	public void onDisable() {
		if (!users.isEmpty()) {
			// There are still users logged in! Quick, save their data first!
			runner.out();
		}
		PluginDescriptionFile pdf = this.getDescription();
		log.info('['+pName+"] v"+pdf.getVersion()+" has been disabled.");
	}
	
	public void onEnable() {
		log = this.getServer().getLogger();
		// Log that the plugin has been enabled
		PluginDescriptionFile pdf = this.getDescription();
		pName = pdf.getName();
		log.info('['+pName+"] v"+pdf.getVersion()+" has been enabled.");
		
		df = getCanonPath(this.getDataFolder().toString());
		
		// First make sure the plugin data folder even exists
		if (!(new File(df)).isDirectory()) {
			new File(df).mkdir();
		}
		players = getCanonPath(df+"/players");
		logs = getCanonPath(df+"/logs");
		lib = getCanonPath(df+"/../../lib");
		String configFile = getCanonFile(df+"/config.txt");
		
		// Check if players folder exists or create it
		if (!(new File(players).isDirectory())) {
			new File(players).mkdir();
		}
		
		// Make the directory for error logs if it doesn't exist
		if (!(new File(logs).isDirectory())) {
			new File(logs).mkdir();
		}
		
		// Check if the /lib/ folder exists, this check will hopefully not be needed later
		if (!(new File(lib).isDirectory())) {
			new File(lib).mkdir();
		}
		
		// Check if MySQL connector exists, if not then download and install it
		if (!(new File(getCanonFile(lib+"/mysql-connector-java-bin.jar"))).exists()) {
			new Downloader("http://dl.dropbox.com/u/1449544/deps/mysql-connector-java-bin.jar", "mysql-connector-java-bin.jar", this);
		}
		
		//Does config exist, if not then make a new one and add defaults
		if (!(new File(configFile).exists())) {
			try {
				new File(configFile).createNewFile();
			} catch (IOException e) {
				log.severe('['+pName+"]: Couldn't create config file. Make sure your plugins directory has write access.");
			}
			Property conf = new Property(configFile, this);
			conf.setString("host", "");
			conf.setString("database", "");
			conf.setString("username", "");
			conf.setString("password", "");
			conf.setString("oldTable", "");
			conf.setString("newTable", ""); // Only one that truly matters, oldTable is reference
			conf.setInt("actions", 32);
			conf.setInt("updateRate", 90); // Time in seconds
			// Now set option tracking options
			conf.setString("com0", "Optional things to track. True = track, False = don't track");
			conf.setBoolean("trackIP", true);
			conf.setBoolean("trackBroken", true);
			conf.setBoolean("trackPlaced", true);
			conf.setBoolean("trackDeaths", true);
			conf.setBoolean("trackMonsterKills", true);
			conf.setBoolean("trackPlayerKills", true);
			conf.setBoolean("trackDistanceWalked", true);
			conf.save();
			log.severe('['+pName+"] Your config isn't set up. Creating one and disabling "+pName+'.');
			disabled = true;
		} else {
			// File exists, check if the database info is there
			Property conf = new Property(configFile, this);
			if (conf.isEmpty("host") || conf.isEmpty("database") || conf.isEmpty("username")) {
				log.severe('['+pName+"] Your database settings aren't set. Disabling "+pName+'.');
				disabled = true;
			} else {
				// Database info exists, build a temporary config in the newest format
				LinkedHashMap<String, Object> tmp = new LinkedHashMap<String, Object>();
				tmp.put("host", conf.getString("host"));
				tmp.put("database", conf.getString("database"));
				tmp.put("username", conf.getString("username"));
				tmp.put("password", conf.getString("password"));
				tmp.put("oldTable", conf.getString("oldTable"));
				tmp.put("newTable", conf.getString("newTable"));
				tmp.put("actions", conf.getInt("actions"));
				tmp.put("updateRate", conf.getInt("updateRate"));
				tmp.put("com0", "Optional things to track. True = track, False = don't track");
				tmp.put("trackIP", conf.getBoolean("trackIP"));
				tmp.put("trackBroken", conf.getBoolean("trackBroken"));
				tmp.put("trackPlaced", conf.getBoolean("trackPlaced"));
				tmp.put("trackDeaths", conf.getBoolean("trackDeaths"));
				tmp.put("trackMonsterKills", conf.getBoolean("trackMonsterKills"));
				tmp.put("trackPlayerKills", conf.getBoolean("trackPlayerKills"));
				tmp.put("trackDistanceWalked", conf.getBoolean("trackDistanceWalked"));
				// Check if old config matches new config format
				if (!conf.match(tmp)) {
					// They don't match, rebuild config
					conf.rebuild(tmp);
				}
				// Now config = tmp since it'll always be up-to-date
				config = tmp;
				// If newTable isn't empty
				if (!conf.isEmpty("newTable")) {
					// If oldTable is empty, use netstats otherwise use oldTable to be renamed
					String oldTable = (conf.isEmpty("oldTable")) ? "netstats" : conf.getString("oldTable");
					db = new Database((String)config.get("host"), (String)config.get("database"), (String)config.get("username"), (String)config.get("password"), oldTable, this);
					db.rename(oldTable, conf.getString("newTable"));
					config.put("newTable", conf.getString("newTable"));
					config.put("oldTable", conf.getString("newTable"));
				} else {
					config.put("oldTable", "netstats");
				}
				db = new Database((String)config.get("host"), (String)config.get("database"), (String)config.get("username"), (String)config.get("password"), (String)config.get("oldTable"), this);
				// First, the plugin is either reloading or is starting up, so set all users to being logged off
				db.query("UPDATE "+config.get("oldTable")+" SET logged=0");
				// If the plugin is just reloading, we need to set all online players back to being online
				Player[] p = this.getServer().getOnlinePlayers();
				for (int i = 0; i < p.length; i++) {
					db.query("UPDATE "+config.get("oldTable")+" SET logged=1 WHERE player = '"+p[i].getName()+"';");
				}
			}
		}
		if (!disabled) {
			runner = new NetRepeater(this);
			PluginManager pm = getServer().getPluginManager();

			// Register player events
			playerListener = new NetPlayerListener(this);
			pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Normal, this);
			
			if ((Boolean)config.get("trackDistanceWalked")) {
				pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
			}

			// Register block events
			if ((Boolean)config.get("trackBroken") || (Boolean)config.get("trackPlaced")) {
				blockListener = new NetBlockListener(this);
				if ((Boolean)config.get("trackBroken")) {
					pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
				}
				if ((Boolean)config.get("trackPlaced")) {
					pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Monitor, this);
				}
			}

			// Register entity events
			if ((Boolean)config.get("trackDeaths") || (Boolean)config.get("trackMonsterKills") || (Boolean)config.get("trackPlayerKills")) {
				entityListener = new NetEntityListener(this);
				if ((Boolean)config.get("trackDeaths")) {
					pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Monitor, this);
				}
				if ((Boolean)config.get("trackMonsterKills") || (Boolean)config.get("trackPlayerKills")) {
					pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Monitor, this);
				}
			}

			if ((Integer)config.get("updateRate") > 0) {
				int rate = ((Integer)config.get("updateRate")*20);
				this.getServer().getScheduler().scheduleSyncRepeatingTask(this, runner, rate, rate);
			}
		} else {
			this.getPluginLoader().disablePlugin(this);
		}
	}
}

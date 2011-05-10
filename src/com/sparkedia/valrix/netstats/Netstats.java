package com.sparkedia.valrix.netstats;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class Netstats extends JavaPlugin {
	private NetPlayerListener pl;
	private NetBlockListener bl;
	private NetEntityListener el;
	public Logger log;
	public LinkedHashMap<String, Object> config;
	public HashMap<String, Property> users  = new HashMap<String, Property>(); // <Name, Propfile>
	public HashMap<String, Integer> actions = new HashMap<String, Integer>();
	public String pName;
	public String df; // Data Folder
	public String players;
	public String logs;
	public Database db;
	public Property conf;
	private NetRepeater runner;
	private Boolean disabled = false;
	public String table = ""; // Active table
	
	// Function to generate a random string from the given charset based on the time
	private String randStr(int l) {
		String c = "!@$%&?1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random r = new Random((System.currentTimeMillis()+System.nanoTime())/2);
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < l; i++) {
            int p = r.nextInt(c.length());
            s.append(c.charAt(p));
        }
        return s.toString();
	}
	
	private void reload() {
		getPluginLoader().disablePlugin(this);
		getPluginLoader().enablePlugin(this);
	}
	
	private void mkDirs(String d) {
		for (String f : d.split(" ")) {
			if (!new File(f).exists()) {
				new File(f).mkdir();
			}
		}
	}

	@Override
	public void onDisable() {
		if (!users.isEmpty()) {
			// There are still users logged in! Quick, save their data first!
			try {
				runner.out();
			} catch(Exception e) {
				// just in case there's another error, set all users to logged=false
				db.update("logged=0");
			}
		}
		log.info('['+pName+"] has been disabled.");
	}
	
	@Override
	public void onEnable() {
		log = getServer().getLogger();
		// Log that the plugin has been enabled
		PluginDescriptionFile pdf = getDescription();
		pName = pdf.getName();
		
		df = getDataFolder().toString();
		players = df+"/players";
		logs = df+"/logs";
		String configFile = df+"/config.txt";
		mkDirs(df+' '+players+' '+logs);
		
		// Check if MySQL connector exists, if not then download and install it
		//if (!(new File(lib+"/mysql-connector-java-bin.jar")).exists())
			//new Downloader("", "", this);
		
		//Does config exist, if not then make a new one and add defaults
		if (!(new File(configFile).exists())) {
			try {
				new File(configFile).createNewFile();
			} catch (IOException e) {
				log.severe('['+pName+"]: Couldn't create config file. Make sure your plugins directory has write access.");
			}
			conf = new Property(configFile, this);
			conf.setString("admin", "");
			conf.setString("wipePass", randStr(10));
			conf.setString("host", "");
			conf.setString("database", "");
			conf.setString("username", "");
			conf.setString("password", "");
			conf.setString("#0", "Both oldTable and newTable are optional");
			conf.setString("oldTable", "");
			conf.setString("newTable", ""); // Only one that truly matters, oldTable is reference
			conf.setInt("actions", 32);
			conf.setInt("updateRate", 90); // Time in seconds
			// Now set option tracking options
			conf.setString("#1", "Optional things to track. True = track, False = don't track");
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
			conf = new Property(configFile, this);
			if (conf.isEmpty("host") || conf.isEmpty("username")) {
				log.severe('['+pName+"] Your database settings aren't set. Disabling "+pName+'.');
				disabled = true;
			} else {
				// Database info exists, build a temporary config in the newest format
				LinkedHashMap<String, Object> tmp = new LinkedHashMap<String, Object>();
				tmp.put("admin", conf.getString("admin"));
				tmp.put("wipePass", conf.getString("wipePass"));
				tmp.put("host", conf.getString("host"));
				tmp.put("database", conf.getString("database"));
				tmp.put("username", conf.getString("username"));
				tmp.put("password", conf.getString("password"));
				tmp.put("#0", "Both oldTable and newTable are optional");
				tmp.put("oldTable", conf.getString("oldTable"));
				tmp.put("newTable", conf.getString("newTable"));
				tmp.put("actions", conf.getInt("actions"));
				tmp.put("updateRate", conf.getInt("updateRate"));
				tmp.put("#1", "Optional things to track. True = track, False = don't track");
				tmp.put("trackIP", conf.getBoolean("trackIP"));
				tmp.put("trackBroken", conf.getBoolean("trackBroken"));
				tmp.put("trackPlaced", conf.getBoolean("trackPlaced"));
				tmp.put("trackDeaths", conf.getBoolean("trackDeaths"));
				tmp.put("trackMonsterKills", conf.getBoolean("trackMonsterKills"));
				tmp.put("trackPlayerKills", conf.getBoolean("trackPlayerKills"));
				tmp.put("trackDistanceWalked", conf.getBoolean("trackDistanceWalked"));
				tmp.put("revised", ((conf.keyExists("revised")) ? conf.getBoolean("revised") : false));
				// Check if old config matches new config format
				if (!conf.match(tmp)) conf.rebuild(tmp);
				// Generate wipe password
				String pass = randStr(10);
				conf.setString("wipePass", pass);
				conf.save();
				tmp.put("wipePass", pass);
				// Now config = tmp since it'll always be up-to-date
				config = tmp;
				// If newTable isn't empty
				String oldTable;
				if (!conf.isEmpty("newTable")) {
					// If oldTable is empty, use netstats otherwise use oldTable to be renamed
					oldTable = (conf.isEmpty("oldTable")) ? "netstats" : conf.getString("oldTable");
					String newTable = (conf.getString("newTable").equalsIgnoreCase("netstats") && !oldTable.equalsIgnoreCase("netstats")) ? "netstats" : conf.getString("newTable");
					table = newTable;
					db = new Database(conf.getString("host"), conf.getString("database"), conf.getString("username"), conf.getString("password"), this);
					db.queries("CREATE TABLE `"+table+"` LIKE `"+oldTable+"`;INSERT INTO `"+table+"` SELECT * FROM `"+oldTable+"`;DROP TABLE `"+oldTable+"`;");
					config.put("newTable", table);
					config.put("oldTable", ((table.equalsIgnoreCase("netstats")) ? "" : table));
					conf.setString("oldTable", ((table.equalsIgnoreCase("netstats")) ? "" : table));
					conf.setString("newTable", "");
					conf.save();
				} else {
					oldTable = (conf.isEmpty("oldTable")) ? "netstats" : conf.getString("oldTable");
					table = oldTable;
					db = new Database(conf.getString("host"), conf.getString("database"), conf.getString("username"), conf.getString("password"), this);
					config.put("oldTable", table);
				}
				// Do a one time revision if null or false
				if ((config.get("revised") == null) || !(new Boolean((Boolean)config.get("revised")))) {
					db.update("total=total/1000");
					conf.setBoolean("revised", true);
					conf.save();
				}
				// END REVISION
				// First, the plugin is either reloading or is starting up, so set all users to being logged off
				db.update("logged=0");
				// If the plugin is just reloading, we need to set all online players back to being online
				for (Player p : getServer().getOnlinePlayers()) db.update("logged=1 WHERE player='"+p.getName()+"';");
			}
		}
		if (!disabled) {
			runner = new NetRepeater(this);
			
			// Create server and start it as daemon to consume minimal resources
			//new NetServer(this);
			
			PluginManager pm = getServer().getPluginManager();

			// Register player events
			pl = new NetPlayerListener(this);
			pm.registerEvent(Event.Type.PLAYER_JOIN, pl, Event.Priority.Lowest, this);
			pm.registerEvent(Event.Type.PLAYER_QUIT, pl, Event.Priority.Lowest, this);
			pm.registerEvent(Event.Type.PLAYER_KICK, pl, Event.Priority.Lowest, this);
			
			if ((Boolean)config.get("trackDistanceWalked")) {
				pm.registerEvent(Event.Type.PLAYER_MOVE, pl, Event.Priority.Monitor, this);
			}

			// Register block events
			if ((Boolean)config.get("trackBroken") || (Boolean)config.get("trackPlaced")) {
				bl = new NetBlockListener(this);
				if ((Boolean)config.get("trackBroken")) {
					pm.registerEvent(Event.Type.BLOCK_BREAK, bl, Event.Priority.Monitor, this);
				}
				if ((Boolean)config.get("trackPlaced")) {
					pm.registerEvent(Event.Type.BLOCK_PLACE, bl, Event.Priority.Monitor, this);
				}
			}

			// Register entity events
			if ((Boolean)config.get("trackDeaths") || (Boolean)config.get("trackMonsterKills") || (Boolean)config.get("trackPlayerKills")) {
				el = new NetEntityListener(this);
				if ((Boolean)config.get("trackDeaths")) {
					pm.registerEvent(Event.Type.ENTITY_DEATH, el, Event.Priority.Monitor, this);
				}
				if ((Boolean)config.get("trackMonsterKills") || (Boolean)config.get("trackPlayerKills")) {
					pm.registerEvent(Event.Type.ENTITY_DAMAGE, el, Event.Priority.Monitor, this);
				}
			}

			if ((Integer)config.get("updateRate") > 0) {
				int rate = ((Integer)config.get("updateRate")*20);
				getServer().getScheduler().scheduleSyncRepeatingTask(this, runner, rate, rate);
			}
			
			// START commands
			// Never return false. This is only for an admin anyway and they'll know the commands
			getCommand("netstats").setExecutor(new CommandExecutor() {
				public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
					if (sender instanceof Player) {
						Player player = ((Player)sender);
						String name = player.getName();
						// Only allow the actual use of the commands to the admin defined in the config
						if (name.equals(config.get("admin").toString().trim())) {
							switch (args.length) {
								case 1:
									if (args[0].equalsIgnoreCase("pass")) player.sendMessage(ChatColor.DARK_PURPLE+"Netstats data wipe password: "+ChatColor.DARK_RED+config.get("wipePass"));
									return true;
								case 2:
									if (args[0].equalsIgnoreCase("wipe") && args[1].equals(config.get("wipePass"))) {
										// netstats wipe <pass>
										getServer().savePlayers();
										for (Player p : getServer().getOnlinePlayers()) {
											getServer().dispatchCommand(sender, "kick "+p.getName());
											new File(players+p.getName()+".stats").delete();
										}
										db.wipe();
										log.info('['+pName+"]: "+ChatColor.DARK_PURPLE+"All Netstats data has been reset. Reloading the server.");
										reload();
									}
									return true;
								default:
									return false;
							}
						}
					} else if (sender instanceof ConsoleCommandSender) {
						switch (args.length) {
							case 1:
								if (args[0].equalsIgnoreCase("pass")) sender.sendMessage(ChatColor.DARK_PURPLE+"Netstats data wipe password: "+ChatColor.DARK_RED+config.get("wipePass"));
								return true;
							case 2:
								if (args[0].equalsIgnoreCase("wipe") && args[1].equals(config.get("wipePass"))) {
									// netstats wipe <pass>
									getServer().savePlayers();
									for (Player p : getServer().getOnlinePlayers()) {
										getServer().dispatchCommand(sender, "kick "+p.getName());
										new File(players+p.getName()+".stats").delete();
									}
									db.wipe();
									log.info('['+pName+"]: "+ChatColor.DARK_PURPLE+"All Netstats data has been reset. Reloading the server.");
									reload();
								}
								return true;
							default:
								return false;
						}
					}
					return false;
				}
			});
			// END commands
			
			log.info('['+pName+"] v"+pdf.getVersion()+" has been enabled.");
		} else {
			getPluginLoader().disablePlugin(this);
		}
	}
}

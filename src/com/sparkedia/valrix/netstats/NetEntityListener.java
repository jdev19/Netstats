package com.sparkedia.valrix.netstats;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class NetEntityListener extends EntityListener {
	
	protected HashMap<Integer, PlayerAttack> attacks = new HashMap<Integer, PlayerAttack>();
	protected long damageTimeThreshold = 500; // ms between damage & death
	protected Netstats plugin;
	private String players;
	private HashMap<String, Property> users;
	private HashMap<String, Integer> actions;
	public LinkedHashMap<String, Object> config;
	private int action;
	
	public NetEntityListener(Netstats plugin) {
		this.plugin = plugin;
		this.users = plugin.users;
		this.actions = plugin.actions;
		this.players = plugin.players;
		this.config = plugin.config;
		this.action = (Integer)config.get("actions");
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent e) {
		Entity entity = e.getEntity();
		if (entity instanceof Player) {
			String name = ((Player)entity).getName();
			if (!users.containsKey(name)) {
				users.put(name, new Property(players+'/'+name+".stats", plugin));
			}
			// It was a player who died, add that to their stats
			users.get(name).inc("deaths");
		}
		// An entity just died, if it was attacked (didn't fall, drown, or burn to death)
		if (attacks.containsKey(entity.getEntityId())) {
			// and was attacked within a half-second ago
			if (attacks.get(entity.getEntityId()).attackTimeAgo()<=damageTimeThreshold) {
				// give killer credit for the kill
				entDeath(entity, attacks.get(entity.getEntityId()).getAttacker());
			}
			// remove all recorded attacks to this entity (garbage collection)
			attacks.remove(entity.getEntityId());
		}
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent e) {
		if (!e.isCancelled()) {
			if (e instanceof EntityDamageByEntityEvent) {
				// Entity was attacked by another entity physically
				Entity attacker = ((EntityDamageByEntityEvent)e).getDamager();
				entDamage(e.getEntity(), attacker);
			} else if (e instanceof EntityDamageByProjectileEvent) {
				// Entity was shot by an arrow
				Entity attacker = ((EntityDamageByProjectileEvent)e).getDamager();
				entDamage(e.getEntity(), attacker);
			} else {
				// Entity fell, was burned, is drowning, or suffocating
				newAttack(e.getEntity(), null);
			}
		}
	}
	
	void entDeath(Entity victim, Player damager) {
		if (damager != null) {
			long now = System.currentTimeMillis();
			if ((victim instanceof Monster) && ((Boolean)config.get("trackMonsterKills"))) {
				String name = damager.getName();
				// Player killed a monster, save it to player's stats file
				Property prop = users.get(name);
				if (!users.containsKey(name)) {
					users.put(name, new Property(players+'/'+name+".stats", plugin));
					actions.put(name, (action/2));
				}
				String sql = "";
				int count = actions.get(name)+1;
				prop.inc("mobsKilled");
				if (count == action) {
					// Update database
					sql += (prop.getInt("broken")>0) ? "broken=broken+"+prop.getInt("broken")+", " : "";
					sql += (prop.getInt("placed")>0) ? "placed=placed+"+prop.getInt("placed")+", " : "";
					sql += (prop.getInt("deaths")>0) ? "deaths=deaths+"+prop.getInt("deaths")+", " : "";
					sql += (prop.getInt("mobsKilled")>0) ? "mobskilled=mobskilled+"+prop.getInt("mobsKilled")+", " : "";
					sql += (prop.getInt("playersKilled")>0) ? "playerskilled=playerskilled+"+prop.getInt("playersKilled")+", " : "";
					sql += (prop.getDouble("distance")>0) ? "distance=distance+"+prop.getDouble("distance")+", " : "";
					sql += "seen="+now+", total=total+"+((prop.getLong("total")+(now-prop.getLong("seen")))/1000)+" WHERE player='"+name+"';";
					plugin.db.update(sql);
					actions.put(name, 0);
					// Reset everything (not time based) in property file since
					// we just updated the DB
					prop.setInt("broken", 0);
					prop.setInt("placed", 0);
					prop.setInt("deaths", 0);
					prop.setInt("mobsKilled", 0);
					prop.setInt("playersKilled", 0);
					prop.setDouble("distance", 0);
					prop.setLong("seen", now);
					prop.setLong("total", 0);
					prop.save();
				} else {
					prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
					prop.setLong("seen", now);
					prop.save();
					actions.put(name, count);
				}
			} else if ((victim instanceof Player) && ((Boolean)config.get("trackPlayerKills"))) {
				String name = damager.getName();
				// Player killed a player, save it to player's stats file
				if (!users.containsKey(name)) {
					users.put(name, new Property(players+'/'+name+".stats", plugin));
					actions.put(name, (action/2));
				}
				Property prop = users.get(name);
				String sql = "";
				int count = actions.get(name)+1;
				prop.inc("playersKilled");
				if (count == action) {
					// Update database
					sql += (prop.getInt("broken")>0) ? "broken=broken+"+prop.getInt("broken")+", " : "";
					sql += (prop.getInt("placed")>0) ? "placed=placed+"+prop.getInt("placed")+", " : "";
					sql += (prop.getInt("deaths")>0) ? "deaths=deaths+"+prop.getInt("deaths")+", " : "";
					sql += (prop.getInt("mobsKilled")>0) ? "mobskilled=mobskilled+"+prop.getInt("mobsKilled")+", " : "";
					sql += (prop.getInt("playersKilled")>0) ? "playerskilled=playerskilled+"+prop.getInt("playersKilled")+", " : "";
					sql += (prop.getDouble("distance")>0) ? "distance=distance+"+prop.getDouble("distance")+", " : "";
					sql += "seen="+now+", total=total+"+((prop.getLong("total")+(now-prop.getLong("seen")))/1000)+" WHERE player='"+name+"';";
					plugin.db.update(sql);
					actions.put(name, 0);
					// Reset everything (not time based) in property file since we just updated the DB
					prop.setInt("broken", 0);
					prop.setInt("placed", 0);
					prop.setInt("deaths", 0);
					prop.setInt("mobsKilled", 0);
					prop.setInt("playersKilled", 0);
					prop.setDouble("distance", 0);
					prop.setLong("seen", now);
					prop.setLong("total", 0);
					prop.save();
				} else {
					prop.setLong("total", prop.getLong("total")+(now-prop.getLong("seen")));
					prop.setLong("seen", now);
					prop.save();
					actions.put(name, count);
				}
			}
		}
	}
	
	void entDamage(Entity victim, Entity damager) {
		if (damager instanceof Player) {
			newAttack(victim, (Player)damager);
		}
	}
	
	public void newAttack(Entity attacker, Player player) {
		if (!attacks.containsKey(attacker.getEntityId())) {
			if (player != null) attacks.put(attacker.getEntityId(), new PlayerAttack(player));
		} else {
			attacks.get(attacker.getEntityId()).setAttack(player);
		}
	}
	
	public class PlayerAttack {
		long lastAttackTime;
		Player lastAttackPlayer;
		
		public PlayerAttack(Player attacker) {
			setAttack(attacker);
		}
		
		public long attackTimeAgo() {
			return lastAttackTime>0 ? System.currentTimeMillis()-lastAttackTime : 0;
		}
		
		public final void setAttack(Player attacker) {
			lastAttackPlayer = attacker;
			lastAttackTime = System.currentTimeMillis();
		}
		
		public Player getAttacker() {
			return lastAttackPlayer;
		}
	}
}

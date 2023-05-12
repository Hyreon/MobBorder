package top.hyreon.mobBorder;

import java.util.*;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class MobBorderListener implements Listener {

	static HashMap<Player, Integer> updaterIndex = new HashMap<>();
	static List<Entity> ghosts = new ArrayList<>();
	
	static MobBorderPlugin plugin;
	private static int clonesOfThisType = 1;

	MobBorderListener(MobBorderPlugin plugin) {
		MobBorderListener.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOW)
	public static void playerHurtEvent(EntityDamageByEntityEvent e) {
		Entity attacker = e.getDamager();
		Entity victim = e.getEntity();
		
		if (victim.getType() != EntityType.PLAYER) return; //this only concerns players getting hurt
		if (getPlayer(attacker) != null && !plugin.pvp()) return; //if pvp is off, then turn off player damage buff
		
		Player player = (Player) victim;
		
		int pLevel = player.getLevel();
		int mLevel = plugin.getLevelByLocation(attacker.getLocation());
		if (getPlayer(attacker) != null) {
			mLevel = Math.min(getPlayer(attacker).getLevel(),mLevel);
		}
		e.setDamage(e.getDamage() * plugin.getDamageBuff(mLevel,pLevel));
	}

	@EventHandler(priority = EventPriority.LOW)
	public static void mobHurtEvent(EntityDamageByEntityEvent e) {
		Entity attacker = e.getDamager();
		Entity victim = e.getEntity();
		Player player = getPlayer(attacker);
		
		if (player == null) return; //this only concerns player damage
		if (victim.getType() == EntityType.PLAYER && !plugin.pvp()) return;
		
		int pLevel = player.getLevel();
		int mLevel = plugin.getLevelByLocation(victim.getLocation());
		if (victim.getType() == EntityType.PLAYER) {
			mLevel = Math.min(getPlayer(attacker).getLevel(),mLevel);
		}
		e.setDamage(e.getDamage() / plugin.getHealthBuff(mLevel, pLevel));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void entityDamageEvent(EntityDamageEvent e) {
		if (e.getEntityType() == EntityType.PLAYER) return; //this only concerns mobs getting hurt
		if (!e.getEntityType().isAlive()) return;

		if (!plugin.resisting(e.getCause())) return; //we don't care about this damage source

		int pLevel = 0; //assume the attackers' level is 0
		if (e instanceof EntityDamageByEntityEvent) {
			//don't want to step on the toes of the other methods for PvE purposes
			if (getPlayer(((EntityDamageByEntityEvent) e).getDamager()) != null) return;

			Entity attacker = getAttacker(((EntityDamageByEntityEvent) e).getDamager());
			if (attacker != null) {
				//otherwise, if it's a mob vs mob deal, get the level
				pLevel = plugin.getLevelByLocation(attacker.getLocation());
			}
		}

		Entity victim = e.getEntity();

		int mLevel = plugin.getLevelByLocation(victim.getLocation());
		e.setDamage(e.getDamage() / plugin.getHealthBuff(mLevel, pLevel));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void entitySpawnEvent(EntitySpawnEvent e) {

		if (!e.getEntityType().isAlive()
				|| e.getEntityType() == EntityType.PLAYER
				|| plugin.ignoresMob(e.getEntityType())) return;

		Entity spawnee = e.getEntity();
		Player player = getNearest(spawnee);

		if (player == null) return;

		int mLevel = plugin.getLevelByLocation(spawnee.getLocation());
		int pLevel = player.getLevel();

		if (clonesOfThisType != 1) {
			ghosts.add(e.getEntity());
			if (e.getEntity() instanceof LivingEntity) ((LivingEntity)e.getEntity()).setRemoveWhenFarAway(true);
		}

		if (plugin.getCloneChance(mLevel, pLevel) > 0) {
			Random random = new Random();
			if (clonesOfThisType >= plugin.getMaxClones()) {
				resetCloneCounter();    //can't clone, so the next spawn isn't a clone
			} else if (random.nextDouble() < plugin.getCloneChance(mLevel, pLevel)) {
				clonesOfThisType++;
				e.getEntity().getWorld().spawnEntity(e.getLocation(), e.getEntityType());
			} else {
				resetCloneCounter();    //failed to clone, so the next spawn isn't a clone
			}
		} else {
			resetCloneCounter();    //can't clone, so the next spawn isn't a clone
		}

	}

	private static void resetCloneCounter() {
		clonesOfThisType = 1;
	}

	private static Player getNearest(Entity spawnee) {

		return spawnee.getWorld().getPlayers().stream()
				.min(Comparator.comparingDouble((p) -> p.getLocation().distanceSquared(spawnee.getLocation())))
				.orElse(null);

	}

	private static Entity getAttacker(Entity attacker) {

		//melee damage
		if (attacker.getType().isAlive()) return attacker;

		//wolf damage and other pets
		if (attacker instanceof Tameable) {
			Tameable tameable = (Tameable) attacker;
			if (tameable.isTamed() && tameable.getOwner() instanceof Entity) return (Entity) tameable.getOwner();
		}

		//bow damage and other projectiles
		if (attacker instanceof Projectile) {
			Projectile projectile = (Projectile) attacker;
			if (projectile.getShooter() instanceof Entity) return (Entity) projectile.getShooter();
		}

		//tnt damage
		if (attacker instanceof TNTPrimed) {
			TNTPrimed tnt = (TNTPrimed) attacker;
			if (tnt.getSource() != null) return tnt.getSource();
		}

		return null;

	}

	private static Player getPlayer(Entity attacker) {
		
		//melee damage
		if (attacker.getType() == EntityType.PLAYER) return (Player) attacker;
		
		//wolf damage and other pets
		if (attacker instanceof Tameable) {
			Tameable tameable = (Tameable) attacker;
			if (tameable.isTamed() && tameable.getOwner() instanceof Player) return (Player) tameable.getOwner();
		}
		
		//bow damage and other projectiles
		if (attacker instanceof Projectile) {
			Projectile projectile = (Projectile) attacker;
			if (projectile.getShooter() instanceof Player) return (Player) projectile.getShooter();
		}
		
		//tnt damage
		if (attacker instanceof TNTPrimed) {
			TNTPrimed tnt = (TNTPrimed) attacker;
			if (tnt.getSource() instanceof Player) return (Player) tnt.getSource();
		}
		
		return null;
	}
	
	@EventHandler
	public static void mobKillEvent(EntityDeathEvent e) {
		
		LivingEntity killedEntity = e.getEntity();
		Player killer = killedEntity.getKiller();

		if (killer == null) return; //must have been killed by a player
		if (killedEntity.getType() == EntityType.PLAYER) return;
		
		int mLevel = plugin.getLevelByLocation(killedEntity.getLocation());
		int pLevel = killer.getLevel();
		e.setDroppedExp((int) (e.getDroppedExp() * plugin.getYield(mLevel, pLevel)));

		if (ghosts.contains(e.getEntity())) e.getDrops().clear(); //no loot from ghosts

	}
	
	@EventHandler
	public static void onPlayerJoin(PlayerJoinEvent e) {
		
		if (plugin.getUpdateWaitTime() <= 0 || plugin.getWarningWaitTime() <= 0) return; //boss said just skip it l0l
		
		int dangerLevel = plugin.getLevelByLocation(e.getPlayer().getLocation());
		if (plugin.usingPlayerLevel()) dangerLevel -= e.getPlayer().getLevel();
		
		startDangerDetector(e.getPlayer(), dangerLevel);
		
		if (dangerLevel >= 0) startUpdates(e.getPlayer(), dangerLevel);

		startLastResortMeasures(e.getPlayer(), dangerLevel);
		
	}

	private static void startDangerDetector(Player player, int lastLevel) {
		
		new BukkitRunnable() {	
	        
            @Override
            public void run() {
            	
            	if (!player.isOnline()) return;
            	
                int currentLevel = plugin.getLevelByLocation(player.getLocation());
                if (plugin.usingPlayerLevel()) currentLevel -= player.getLevel();
                
                if (lastLevel <= 0 && currentLevel > 0) {
                	plugin.getPrinter().sendWarning(player);
                	startUpdates(player, currentLevel);
                } else if (lastLevel > 0 && currentLevel <= 0) {
                	plugin.getPrinter().sendSafeMessage(player);
                	stopUpdates(player);
                }
                
                startDangerDetector(player, currentLevel);
                
            }
            
        }.runTaskLater(plugin, (long) (plugin.getWarningWaitTime() * 20));
		
	}

	public static void stopUpdates(Player player) {
		plugin.getServer().getScheduler().cancelTask(updaterIndex.get(player));
	}
	
	public static void startUpdates(Player p, int lastLevel) {
		
		BukkitTask task = new BukkitRunnable() {
	        
            @Override
            public void run() {
            	
            	if (!p.isOnline()) return;
            	
                int relativeLevel = plugin.getPrinter().sendUpdate(p, lastLevel);
                
                if (relativeLevel > 0) {
                	startUpdates(p, relativeLevel);
                }
                
            }
            
        }.runTaskLater(plugin, (long) (plugin.getUpdateWaitTime() * 20));
        
        updaterIndex.put(p, task.getTaskId());
        
	}

	private static void startLastResortMeasures(Player p, int lastLevel) {

		BukkitTask task = new BukkitRunnable() {

			@Override
			public void run() {

				if (!p.isOnline()) return;

				int currentLevel = plugin.getLevelByLocation(p.getLocation());
				if (plugin.usingPlayerLevel()) currentLevel -= p.getLevel();

				if (plugin.allHostile(currentLevel)) {
					Collection<Entity> entities = p.getWorld().getNearbyEntities(p.getLocation(), 20, 20, 20);
					for (Entity entity : entities) {
						if (entity instanceof Creature) {
							((Creature) entity).setTarget(p);
						}
					}
				}

				double damage = plugin.passiveDamage(currentLevel) * plugin.getLastResortWaitTime();
				if (damage > 0) {
					p.damage(damage);
				}

				if (plugin.doSmite(currentLevel)) {
					p.setHealth(0);
				}

				startLastResortMeasures(p, currentLevel);

			}

		}.runTaskLater(plugin, (long) (plugin.getLastResortWaitTime() * 20));

		updaterIndex.put(p, task.getTaskId());

	}
	
}

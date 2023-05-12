package top.hyreon.mobBorder;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MobBorderPlugin extends JavaPlugin {

	public HashMap<World, Double> levelUpWeights = new HashMap<World, Double>();
	public HashMap<World, Double> orbUpWeights = new HashMap<World, Double>();
	public HashMap<World, Integer> centerLevels = new HashMap<World, Integer>();
	private HashMap<World, Double> safeAreas = new HashMap<World, Double>();
	private List<EntityDamageEvent.DamageCause> reducedDamage = new ArrayList<>();
	private Collection<EntityType> mobBlacklist = new ArrayList<>();
	
	public double damageBuff;
	public double healthBuff;
	private double cloneChance;
	private double speedBuff;
	private double keenBuff;
	public double experienceYield;
	public int maxClones;
	public double speedMax = 1;
	public double speedMin = 1;
	public double experienceMax = 1;
	public double experienceMin = 1;
	public double cloneChanceMax = 1;
	public boolean pvpMode;
	private boolean usingAttributes;
	private Printer printer;
	private double updateWaitTime;
	private double warningWaitTime;
	private double lastResortWaitTime;
	private boolean usingPlayerLevel;
	private int forcedAggressionLevel;
	private int passiveDamageLevel;
	private double passiveDamageRate;
	private int smiteLevel;
	private int forcedAggressionWarning;
	private int passiveDamageWarning;
	private int smiteWarning;

	@Override
	public void onEnable() {
		
		createConfig();
		FileConfiguration config = getConfig();
		
		getWorldSettings(config);
		
		damageBuff = config.getDouble("damage-buff");
		healthBuff = config.getDouble("health-buff");
		speedBuff = config.getDouble("speed-buff");
		keenBuff = config.getDouble("keen-buff");
		experienceYield = config.getDouble("experience-yield");
		cloneChance = config.getDouble("clone-chance");
		maxClones = config.getInt("clone-count-cap");
		pvpMode = config.getBoolean("affects-pvp");
		usingPlayerLevel = config.getBoolean("use-player-level");
		usingAttributes = config.getBoolean("allow-attribute-changes");
		updateWaitTime = config.getDouble("update-wait-time");
		warningWaitTime = config.getDouble("warning-wait-time");
		forcedAggressionWarning = config.getInt("forced-aggression-warning");
		passiveDamageWarning = config.getInt("passive-damage-warning");
		smiteWarning = config.getInt("forced-smite-warning");

		lastResortWaitTime = config.getDouble("last-resort-wait-time");
		forcedAggressionLevel = config.getInt("forced-aggression-level");
		passiveDamageLevel = config.getInt("passive-damage-level");
		passiveDamageRate = config.getDouble("passive-damage-rate");
		smiteLevel = config.getInt("forced-smite-level");

		if (cloneChance > 0) {
			cloneChanceMax = config.getDouble("clone-chance-cap");
			if (cloneChanceMax <= 0) cloneChanceMax = 1;
		}
		
		if (experienceYield > 0) {
			experienceMax = config.getDouble("experience-cap");
			if (experienceMax <= 0) experienceMax = Double.MAX_VALUE;
		} else if (experienceYield < 0) {
			experienceMin = config.getDouble("experience-cap");
			if (experienceMin <= 0) experienceMin = 0;
		}

		if (speedBuff > 0) {
			speedMax = config.getDouble("speed-cap");
			if (speedMax <= 0) speedMax = Double.MAX_VALUE;
		} else if (speedBuff < 0) {
			speedMin = config.getDouble("speed-cap");
			if (speedMin <= 0) speedMin = 0;
		}

		reducedDamage.addAll(
				config.getList("resisted-damage").stream().map(
						(obj) -> {
							try { return EntityDamageEvent.DamageCause.valueOf(obj.toString()); } catch (IllegalArgumentException e) { return null; }
						}).collect(Collectors.toList()
				)
		);

		mobBlacklist.addAll(
				config.getList("excluded-mobs").stream().map(
						(obj) -> {
							try { return EntityType.valueOf(obj.toString()); } catch (IllegalArgumentException e) { return null; }
						}).collect(Collectors.toList()
				)
		);
		
		if (config.getBoolean("use-subtitles")) {
			
			if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10")) {
				
				getLogger().warning("Tried to use title text on an unsupported version. Change the config to use chat text instead");
				printer = new TextPrinter(this);
				
			} else {
				printer = new TitlePrinter(this);
			}
		} else {
			printer = new TextPrinter(this);
		}
		
		Bukkit.getPluginCommand("mobbuff").setExecutor(new CommandGetMobBuff(this));

		getServer().getPluginManager().registerEvents(new MobBorderListener(this), this);

		if (usingAttributes) {
			if (Bukkit.getVersion().contains("1.8")) {
				getLogger().warning("Tried to use attribute modifiers on an unsupported version. Change the config to disable this");
			} else {
				getServer().getPluginManager().registerEvents(new AttributeApplier(this), this);
			}
		}


	}
	
	private void createConfig() {
	    try {
	        if (!getDataFolder().exists()) {
	            getDataFolder().mkdirs();
	        }
	        File file = new File(getDataFolder(), "config.yml");
	        if (!file.exists()) {
	        	getLogger().info("Doing first-time configuration (you will need to change the config.yml file for each world)");
	            saveDefaultConfig();
	        }
	    } catch (Exception e) {
	        e.printStackTrace();

	    }

	}

	private void getWorldSettings(FileConfiguration config) {
		
		ConfigurationSection worldSettings = config.getConfigurationSection("world-settings");
		
		//String defaultWorldName = getDefaultWorldName();
		
		Set<String> configWorldEntries = worldSettings.getKeys(false);
		
		for (World world: Bukkit.getWorlds()) {
			
			if (!configWorldEntries.contains(world.getName())) {
				getLogger().info("Config not found for world '"+world.getName()+"', using defaults for world type " + world.getEnvironment().toString());
				switch (world.getEnvironment()) {
					case NORMAL:
						centerLevels.put(world, 0);
						levelUpWeights.put(world, 160.0);
						safeAreas.put(world, 500.0);
						break;
					case NETHER:
						centerLevels.put(world, 10);
						levelUpWeights.put(world, 20.0);
						safeAreas.put(world, 262.5);
						break;
					case THE_END:
						centerLevels.put(world, 25);
						levelUpWeights.put(world, 160.0);
						safeAreas.put(world, 500.0);
						break;
				}
			} else {
				ConfigurationSection settings = worldSettings.getConfigurationSection(world.getName());
				centerLevels.put(world, settings.getInt("center-level"));
				orbUpWeights.put(world, settings.getDouble("blocks-per-point"));
				levelUpWeights.put(world, settings.getDouble("blocks-per-level"));
				safeAreas.put(world, settings.getDouble("safe-area"));
			}
		}
	}

//	private String getDefaultWorldName() {
//		
//		ArrayList<String> worldNames = new ArrayList<String>();
//		for (World world : Bukkit.getWorlds()) {
//			worldNames.add(world.getName());
//		}
//		
//		worldNameTest:
//		for (String baseWorldName : worldNames) {
//			for (String worldName : worldNames) {
//				if (!worldName.contains(baseWorldName)) {
//					continue worldNameTest;
//				}
//			}
//			return baseWorldName;
//		}
//		
//		return "world";
//	}

	@Override
	public void onDisable() {

	}

	public void reload() {

	}

	public int getLevelByLocation(Location location) {
		World world = location.getWorld();
		
		double baseRate = levelUpWeights.getOrDefault(world, 166.6667);
		double orbRate = orbUpWeights.getOrDefault(world, 0.0);
		int minLevel = centerLevels.getOrDefault(world, 0);
		double safeDistance = Math.max(0, safeAreas.getOrDefault(world, 500.0));
		
		Location center = world.getSpawnLocation();
		double distance = location.distance(center) - safeDistance;

		if (baseRate == 0) return minLevel;
		
		int level = levelOf(distance, orbRate) + (int) (distance / baseRate) + minLevel;
		if (level < 0) return 0;
		else return level;
	}

	/**
	 * Gets the level given a number of orbs.
	 * @param orbs The number of orbs obtained
	 * @param orbRate The reciprocal of the orbs obtained
	 * @return The XP level that a number of XP points would be worth.
	 */
	static private int levelOf(double orbs, double orbRate) {
		if (orbRate == 0) return 0;
		orbs /= orbRate;
		if (orbs <= 352) {
			//return orb eq. from levels 0 to 16
			return (int) Math.floor(Math.sqrt(orbs + 9) - 3);
		} else if (orbs <= 1507) {
			return (int) Math.floor(
					(
						(81+
							Math.sqrt(
									40 * orbs - 7839
							)
						) / (
								10
						)
					)
			);
			//return orb eq. from levels 17 to 31
		} else {
			return (int) Math.floor(
					(
							(325+
									Math.sqrt(
											72 * orbs - 54215
									)
							) / (
									18
							)
					)
			);
			//return orb eq. from levels 32 onwards
		}
	}

	public String getDisplayBuff(int mLevel, int pLevel) {
		int damageBuff = (int) Math.round(100*getDamageBuff(mLevel, pLevel));
		int healthBuff = (int) Math.round(100*getHealthBuff(mLevel, pLevel));
		int experienceYield = (int) Math.round(100*getYield(mLevel, pLevel));
		int speedBuff = (int) Math.round(100*getSpeedBuff(mLevel, pLevel));
		//int keenBuff = (int) Math.round(100*getKeenBuff(mLevel, pLevel));	 //not important enough
		if (damageBuff == 100 && healthBuff == 100 && experienceYield == 100)
			return "SAFE";
		else if (damageBuff == healthBuff && healthBuff == experienceYield && experienceYield == speedBuff)
			return "BUFF: "+damageBuff+"%";
		else if (damageBuff == healthBuff && (healthBuff == speedBuff || speedBuff == 100))
			return "STAT/EXP: "+damageBuff+"/"+experienceYield;
		else if (damageBuff == healthBuff)
			return "STR/EXP/SPD: "+damageBuff+"/"+experienceYield+"/"+speedBuff;
		else
			return "DMG/HP/EXP/SPD: "+damageBuff+"/"+healthBuff+"/"+experienceYield+"/"+speedBuff;
	}

	public double getDamageBuff(int mLevel, int pLevel) {
		if (!usingPlayerLevel) pLevel = 0;
		int relativeLevel = mLevel - pLevel;
		if (relativeLevel < 0) return 1;
		return (1 + damageBuff*relativeLevel);
	}

	public double getHealthBuff(int mLevel, int pLevel) {
		if (!usingPlayerLevel) pLevel = 0;
		int relativeLevel = mLevel - pLevel;
		if (relativeLevel < 0) return 1;
		return (1 + healthBuff*relativeLevel);
	}

	public double getKeenBuff(int mLevel, int pLevel) {
		if (!usingPlayerLevel) pLevel = 0;
		if (!usingAttributes) return 1;
		int relativeLevel = mLevel - pLevel;
		if (relativeLevel < 0) return 1;
		return (1 + keenBuff*relativeLevel);
	}

	public double getSpeedBuff(int mLevel, int pLevel) {
		if (!usingPlayerLevel) pLevel = 0;
		if (!usingAttributes) return 1;
		int relativeLevel = mLevel - pLevel;
		if (relativeLevel < 0) return 1;

		double localSpeedBuff = 1 + speedBuff*relativeLevel;

		if (localSpeedBuff < speedMin) return speedMin;
		else if (localSpeedBuff > speedMax) return speedMax;
		else return localSpeedBuff;
	}

	public double getCloneChance(int mLevel, int pLevel) {
		if (!usingPlayerLevel) pLevel = 0;
		int relativeLevel = mLevel - pLevel;
		if (relativeLevel < 0) return 1;
		double localCloneChance = (1 + cloneChance*relativeLevel);
		if (localCloneChance > cloneChanceMax) return cloneChanceMax;
		return localCloneChance;
	}

	public boolean allHostile(int currentLevel) {
		if (forcedAggressionLevel < 0) return false;
		return currentLevel >= forcedAggressionLevel;
	}

	public double passiveDamage(int currentLevel) {
		if (passiveDamageLevel < 0) return 0;
		if (currentLevel <= passiveDamageLevel) return 0;
		return (currentLevel - passiveDamageLevel) * passiveDamageRate;
	}

	public boolean doSmite(int currentLevel) {
		if (smiteLevel < 0) return false;
		return currentLevel >= smiteLevel;
	}

	public int getMaxClones() {
		return maxClones;
	}

	public double getYield(int mLevel, int pLevel) {
		int relativeLevel = mLevel - pLevel;
		if (relativeLevel < 0) return 1;
		
		double xpYield = 1 + experienceYield*relativeLevel;
		
		if (xpYield < experienceMin) return experienceMin;
		else if (xpYield > experienceMax) return experienceMax;
		else return xpYield;
	}

	public boolean pvp() {
		return pvpMode;
	}

	public double getUpdateWaitTime() {
		return updateWaitTime;
	}
	
	public double getWarningWaitTime() {
		return warningWaitTime;
	}

	public double getLastResortWaitTime() {
		return lastResortWaitTime;
	}

	public Printer getPrinter() {
		return printer;
	}

	public boolean usingAttributes() {
		return usingAttributes;
	}

	public boolean resisting(EntityDamageEvent.DamageCause cause) {
		return reducedDamage.contains(cause);
	}

	public boolean usingPlayerLevel() {
		return usingPlayerLevel;
	}

	public String getMajorWarning(int relativeLevel, int previousLevel) {
		if (relativeLevel < 0) relativeLevel = 0;

		if (relativeLevel >= forcedAggressionWarning && previousLevel < forcedAggressionWarning) return ChatColor.RED + "Forced aggression soon!";
		if (relativeLevel >= passiveDamageWarning && previousLevel < passiveDamageWarning) return ChatColor.RED + "Passive damage soon!";
		if (relativeLevel >= smiteWarning && previousLevel < smiteWarning) return ChatColor.RED + "" + ChatColor.BOLD + "Instant death soon!";
		else return "";
	}

	public boolean ignoresMob(EntityType entityType) {
		return mobBlacklist.contains(entityType);
	}
}

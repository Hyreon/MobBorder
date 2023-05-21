package top.hyreon.mobBorder;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class Printer {
	
	MobBorderPlugin plugin;
	
	Printer(MobBorderPlugin plugin) {
		this.plugin = plugin;
	}

	public int sendUpdate(Player p, int lastLevel) {

		int mLevel = plugin.getLevelByLocation(p.getLocation());
		if (mLevel == lastLevel) return mLevel;

		int pLevel = p.getLevel();
		int buffEffectiveLevel = mLevel;
		if (plugin.usingPlayerLevel()) buffEffectiveLevel -= pLevel;
		int lastResortEffectiveLevel = mLevel;
		if (plugin.isLastResortUsingPlayerLevel()) lastResortEffectiveLevel -= pLevel;
		int lastResortEffectiveLastLevel = lastLevel;
		if (plugin.isLastResortUsingPlayerLevel()) lastResortEffectiveLastLevel -= pLevel;

		if (buffEffectiveLevel <= 0) return 0;
		if (lastResortEffectiveLevel <= 0) lastResortEffectiveLevel = 0;
		if (lastResortEffectiveLastLevel <= 0) lastResortEffectiveLastLevel = 0;

		String displayBuff = plugin.getDisplayBuff(mLevel, pLevel);

		String majorWarning;

		ChatColor color;
		if (mLevel < lastLevel) {
			color = ChatColor.YELLOW;
			majorWarning = "";
		} else {
			color = ChatColor.RED;
			System.out.println(lastResortEffectiveLevel + " : " + lastResortEffectiveLastLevel);
			majorWarning = plugin.getMajorWarning(lastResortEffectiveLevel, lastResortEffectiveLastLevel);
		}


		sendDisplayBuff(p, majorWarning, color + displayBuff);

		return mLevel;

	}

	abstract public void sendDisplayBuff(Player p, String majorWarning, String displayBuff);
	abstract public void sendWarning(Player p);
	abstract public void sendSafeMessage(Player p);

}

package top.hyreon.mobBorder;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class Printer {
	
	MobBorderPlugin plugin;
	
	Printer(MobBorderPlugin plugin) {
		this.plugin = plugin;
	}

	public int sendUpdate(Player p, int lastLevel) {

		int pLevel = p.getLevel();
		int mLevel = plugin.getLevelByLocation(p.getLocation());
		int relativeLevel = mLevel;
		if (plugin.usingPlayerLevel()) relativeLevel -= pLevel;

		if (relativeLevel <= 0) return 0;
		if (relativeLevel == lastLevel) return relativeLevel;

		String displayBuff = plugin.getDisplayBuff(mLevel, pLevel);

		String majorWarning;

		ChatColor color;
		if (relativeLevel < lastLevel) {
			color = ChatColor.YELLOW;
			majorWarning = "";
		} else {
			color = ChatColor.RED;
			majorWarning = plugin.getMajorWarning(relativeLevel, lastLevel);
		}


		sendDisplayBuff(p, majorWarning, color + displayBuff);

		return relativeLevel;

	}

	abstract public void sendDisplayBuff(Player p, String majorWarning, String displayBuff);
	abstract public void sendWarning(Player p);
	abstract public void sendSafeMessage(Player p);

}

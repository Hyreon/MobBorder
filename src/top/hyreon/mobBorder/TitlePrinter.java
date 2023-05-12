package top.hyreon.mobBorder;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TitlePrinter extends Printer {
	
	TitlePrinter(MobBorderPlugin plugin) {
		super(plugin);
	}

	@Override
	public void sendWarning(Player p) {
		
		p.sendTitle(ChatColor.RED + "Danger!", ChatColor.RED + "Going too far for your level!", 5, 60, 15);
		
	}

	@Override
	public void sendDisplayBuff(Player p, String majorWarning, String s) {
		p.sendTitle(majorWarning, s, 10, 70, 20);
	}

	@Override
	public void sendSafeMessage(Player p) {
		
		p.sendTitle(ChatColor.AQUA + "Safe", ChatColor.AQUA + "No more mob buff.", 5, 45, 10);
		
	}

}

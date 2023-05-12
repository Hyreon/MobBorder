package top.hyreon.mobBorder;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TextPrinter extends Printer {

	TextPrinter(MobBorderPlugin plugin) {
		super(plugin);
	}

	@Override
	public void sendWarning(Player p) {
		
		String displayBuff = plugin.getDisplayBuff(plugin.getLevelByLocation(p.getLocation()), p.getLevel());
		
		p.sendMessage(ChatColor.RED + "Warning: Going too far for your level! " + displayBuff);
	}

	@Override
	public void sendDisplayBuff(Player p, String majorWarning, String s) {
		if (!majorWarning.isEmpty()) {
			p.sendMessage(majorWarning);
		}
		p.sendMessage(s);
	}

	@Override
	public void sendSafeMessage(Player p) {
		
		p.sendMessage(ChatColor.AQUA + "Back in safe territory. No more mob buff.");
		
	}

}

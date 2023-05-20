package top.hyreon.mobBorder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGetMobBuff implements CommandExecutor {

	MobBorderPlugin plugin;
	LanguageLoader lloader;

	CommandGetMobBuff(MobBorderPlugin plugin) {
		this.plugin = plugin;
		this.lloader = plugin.lloader;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String lbl, String[] args) {
		
		if (args.length > 0 && (args[0].equals("?") || args[0].equals("help"))) {

			String buff = lloader.get("help_buff");

			String counter;
			if (plugin.usingPlayerLevel()) {
				counter = lloader.get("help_player_buff");
			} else {
				counter = lloader.get("help_player_same");
			}

			String pvp;
			if (plugin.pvp()) {
				pvp = lloader.get("help_pvp");
			} else {
				pvp = lloader.get("help_no_pvp");
			}

			String xp;
			if (plugin.experienceYield > 0) {
				xp = lloader.get("help_xp_buff");
			} else if (plugin.experienceYield < 0) {
				xp = lloader.get("help_xp_nerf");
			} else {
				xp = lloader.get("help_xp");
			}

			sender.sendMessage(String.join("\n", buff, counter, pvp, xp));
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage("Cannot do as non-player");
			return true;
		}
		Player player = (Player) sender;
		int pLevel = player.getLevel();
		int mLevel = plugin.getLevelByLocation(player.getLocation());
		
		String mBuff = plugin.getDisplayBuff(mLevel,pLevel);
		player.sendMessage("Local mob level: "+mLevel+" ("+mBuff+")");
		return true;
	}

}

package top.hyreon.mobBorder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGetMobBuff implements CommandExecutor {

	MobBorderPlugin plugin;
	
	CommandGetMobBuff(MobBorderPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String lbl, String[] args) {
		
		if (args.length > 0 && (args[0].equals("?") || args[0].equals("help"))) {
			
			sender.sendMessage("The farther you venture out, the stronger mobs will become.");
			sender.sendMessage("Your vanilla experience level, however, makes you stronger.");
			if (plugin.pvp()) sender.sendMessage("It even works against other players!");
			sender.sendMessage("You can never be stronger than the local mobs.");
			if (!plugin.pvp()) sender.sendMessage("This does not affect PvP in any way.");
			if (plugin.experienceYield > 1) sender.sendMessage("Mobs stronger than you will drop bonus experience.");
			else if (plugin.experienceYield < 1) sender.sendMessage("Mobs stronger than you will drop less experience.");
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

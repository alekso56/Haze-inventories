package io.alekso56.bukkit.hazeinv.Commands;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UserCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		 if (!(sender instanceof Player)) {
	            sender.sendMessage("This command can only be used by players.");
	            return true;
	        }

	        Player player = (Player) sender;

	        if (args.length == 0) {
	            player.sendMessage("Usage: /oi <username/uuid> [worldName] [enderchest/true]");
	            return false;
	        }
	        OfflinePlayer target = null;
	        try {
	           UUID user = UUID.fromString(args[0]);
	           target = player.getServer().getPlayer(user);
	           if(target == null) {
	        	   target = Bukkit.getOfflinePlayer(user);
	           }
	        }catch(IllegalArgumentException  e) {
	        	player.getServer().getPlayer(args[0]);
	        	if(target == null) {
		        	  target = Bukkit.getOfflinePlayer(args[0]);
		        }
	        }
	        if(target.hasPlayedBefore() || target.isOnline()) {
	        	World world = Bukkit.getWorld(args[1]);
	        	if(world == null) {
	        		player.sendMessage("Cant find world.");
	        		return false;
	        	}
	        	if(args.length > 2) {
	        		//enderchest
	        		
	        	}else {
	        		//normal inv
	        		
	        	}
	        }
			return false;
	}
}

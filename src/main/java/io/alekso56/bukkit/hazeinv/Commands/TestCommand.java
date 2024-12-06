package io.alekso56.bukkit.hazeinv.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.Util.VanillaPlayer;

public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	Player player = (Player) sender;
    	VanillaPlayer adjuster = Core.instance.players.get(player);
    	adjuster.loadNextWorldChange(Bukkit.createInventory(null, 45));
        sender.sendMessage("Loaded empty inventory into empty load slot for next world transition");
        return true;
    }
}

package io.alekso56.bukkit.hazeinv.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.API.CircleAPI;
import io.alekso56.bukkit.hazeinv.Enums.Flag;
import io.alekso56.bukkit.hazeinv.Models.Circle;

public class PurgeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /hazpurge <circleByDate|byUserID> [circlename|uuid]");
            return false;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "circleByDate":
                if (args.length < 3) {
                    player.sendMessage("Please specify a circle name and time in days.");
                    return false;
                }
                handleCircleModifiedPurge(player, args[1], args[2]);
                break;
            case "byUserID":
                if (args.length < 2) {
                    player.sendMessage("Please specify a world name.");
                    return false;
                }
                handlePurgeByUserId(player, args[1]);
                break;
            default:
                player.sendMessage("Unknown subcommand.");
        }
        return true;
    }

	private void handleCircleModifiedPurge(Player player, String circlename, String days) {
		
	}

	private void handlePurgeByUserId(Player player, String uuid) {
		try {
			UUID id = UUID.fromString(uuid);
			
		}catch(Exception e) {
			player.sendMessage("Invalid uuid");
		}
	}
}

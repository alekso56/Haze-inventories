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

public class CircleCommand implements CommandExecutor, TabCompleter {

    private final List<String> subCommands = Arrays.asList("view", "create", "delete", "addworld", "removeworld", "addflag", "removeflag", "setoption");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /circle <view|create|delete|addworld|removeworld|addflag|removeflag|setoption> <worldName|circleName> [worldName|flag|option]");
            return false;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "view":
                handleViewCircles(player);
                break;
            case "create":
                handleCreateCircle(player);
                break;
            case "delete":
                if (args.length < 2) {
                    player.sendMessage("Please specify a circle name.");
                    return false;
                }
                handleDeleteCircle(player, args[1]);
                break;
            case "addworld":
                if (args.length < 3) {
                    player.sendMessage("Please specify a circle name and world name.");
                    return false;
                }
                handleAddWorld(player, args[1], args[2]);
                break;
            case "removeworld":
                if (args.length < 2) {
                    player.sendMessage("Please specify a world name.");
                    return false;
                }
                handleRemoveWorld(player, args[1]);
                break;
            case "addflag":
                if (args.length < 3) {
                    player.sendMessage("Please specify a circle name and flag.");
                    return false;
                }
                handleAddFlag(player, args[1], args[2]);
                break;
            case "removeflag":
                if (args.length < 3) {
                    player.sendMessage("Please specify a circle name and flag.");
                    return false;
                }
                handleRemoveFlag(player, args[1], args[2]);
                break;
            case "setoption":
                if (args.length < 4) {
                    player.sendMessage("Please specify a circle name, option, and value (true/false).");
                    return false;
                }
                handleSetOption(player, args[1], args[2], args[3]);
                break;
            default:
                player.sendMessage("Unknown subcommand.");
        }
        return true;
    }

    private void handleRemoveFlag(Player player, String circleName, String flagName) {
    	 Circle circle = CircleAPI.getFromName(UUID.fromString(circleName));
         
         // Check if the circle exists
         if (circle == null) {
             player.sendMessage("Circle " + circleName + " does not exist.");
             return;
         }
         
         try {
             Flag flag = Flag.valueOf(flagName.toUpperCase());
             circle.removeFlag(flag);
             player.sendMessage("Flag " + flagName + "removed on circle " + circleName + ".");
             
         } catch (IllegalArgumentException e) {
             player.sendMessage("Invalid flag. Available flags are: " + Arrays.toString(Flag.values()));
         }
	}

	private void handleAddFlag(Player player, String circleName, String flagName) {
        Circle circle = CircleAPI.getFromName(UUID.fromString(circleName));
        
        // Check if the circle exists
        if (circle == null) {
            player.sendMessage("Circle " + circleName + " does not exist.");
            return;
        }
        
        try {
            Flag flag = Flag.valueOf(flagName.toUpperCase());
            circle.addFlag(flag);
            player.sendMessage("Flag " + flagName + "set on circle " + circleName + ".");
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid flag. Available flags are: " + Arrays.toString(Flag.values()));
        }
    }


	private void handleRemoveWorld(Player player, String worldname) {
		World world = Bukkit.getWorld(worldname);
		if(world == null) {
		    player.sendMessage("World " + worldname + " does not exist.");
			return;
		}
		for(Circle circle : Core.instance.circles) {
			circle.getWorlds().remove(world.getUID());
		}
	}

	private void handleAddWorld(Player player, String circleName, String worldname) {
		Circle circlename = CircleAPI.getFromName(UUID.fromString(circleName));
		if(circlename == null) {
		    player.sendMessage("Circle " + circleName + " does not exist.");
			return;
		}
		World world = Bukkit.getWorld(worldname);
		if(world == null) {
		    player.sendMessage("World " + worldname + " does not exist.");
			return;
		}
		for(Circle circle : Core.instance.circles) {
			circle.getWorlds().remove(world.getUID());
		}
		circlename.getWorlds().add(world.getUID());
	}

	private void handleViewCircles(Player player) {
        player.sendMessage("Available circles with worlds, flags, and options:");
        for(Circle circle : Core.instance.circles) {
            List<String> worlds = circle.getWorlds().stream().map(t -> t.toString()).collect(Collectors.toList());
            Set<Flag> flags = circle.listFlags();
            player.sendMessage("Circle: " + circle + " - Worlds: " + worlds + " - Flags: " + flags + " - Options: PerGamemode:" + circle.isPerGameMode()+" ArmorOnly:"+circle.isSyncArmorOnly()+" EnderChest:"+circle.isSyncEnderChest()+" MainInventory:"+circle.isSyncMainInventory());
        }
    }

    private void handleCreateCircle(Player player) {
    	 Circle circle = new Circle();
    	 Core.instance.circles.add(circle);
    	 player.sendMessage("Circle " + circle.getCircleName() + " has been created with default options.");
    }

    private void handleDeleteCircle(Player player, String circleName) {
    	Circle circle = CircleAPI.getFromName(UUID.fromString(circleName));
    	 
        if (circle != null) {
        	Core.instance.circles.remove(circle);
            player.sendMessage("Circle " + circleName + " has been deleted.");
        } else {
            player.sendMessage("Circle " + circleName + " does not exist.");
        }
    }

    private void handleSetOption(Player player, String circleName, String optionName, String value) {
    	Circle circle = CircleAPI.getFromName(UUID.fromString(circleName));
        if (circle == null) {
            player.sendMessage("Circle " + circleName + " does not exist.");
            return;
        }
        List<String> options = Arrays.asList("isPerGameMode", "syncEnderChest", "syncMainInventory", "syncArmorOnly");
        if (!options.contains(optionName)) {
            player.sendMessage("Unknown option: " + optionName + ". Valid options: isPerGameMode, syncEnderChest, syncMainInventory, syncArmorOnly.");
            return;
        }

        boolean boolValue;
        try {
            boolValue = Boolean.parseBoolean(value);
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid value: " + value + ". Use true or false.");
            return;
        }

        switch(options.indexOf(optionName)) {
        case 0:
        	circle.setPerGameMode(boolValue);
        case 1:
        	circle.setSyncEnderChest(boolValue);
        case 2:
        	circle.setSyncMainInventory(boolValue);
        case 3:
        	circle.setSyncArmorOnly(boolValue);
        }
        player.sendMessage("Option " + optionName + " for circle " + circleName + " set to " + boolValue + ".");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    suggestions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            if (Arrays.asList("view", "delete", "addworld", "removeworld", "addflag", "removeflag", "setoption").contains(args[0].toLowerCase())) {
                for (Circle circle : Core.instance.circles) {
                    if (circle.getCircleName().toString().startsWith(args[1].toLowerCase())) {
                        suggestions.add(circle.toString());
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setoption")) {
            List<String> options = Arrays.asList("isPerGameMode", "syncEnderChest", "syncMainInventory", "syncArmorOnly");
            for (String option : options) {
                if (option.startsWith(args[2].toLowerCase())) {
                    suggestions.add(option);
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("setoption")) {
            suggestions.addAll(Arrays.asList("true", "false"));
        }

        return suggestions;
    }
}

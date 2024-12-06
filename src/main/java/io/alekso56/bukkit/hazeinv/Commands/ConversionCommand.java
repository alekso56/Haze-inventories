package io.alekso56.bukkit.hazeinv.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.conversion.PerWorldInventory;

public class ConversionCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("Usage: /hazconvert <PWI>");
			return false;
		}

		Bukkit.getScheduler().runTaskAsynchronously(Core.instance, ee -> {
			try {
				PerWorldInventory.convertPWI();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return true;
	}
}

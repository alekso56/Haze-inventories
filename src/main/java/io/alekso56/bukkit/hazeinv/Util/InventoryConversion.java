package io.alekso56.bukkit.hazeinv.Util;

import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.alekso56.bukkit.hazeinv.Models.Circle;
import net.minecraft.server.level.ServerPlayer;

public class InventoryConversion {
            VanillaPlayer getPlayer(Player pl,Circle circle) {
            	VanillaPlayer converted = ((VanillaPlayer) pl);
            	converted.current_circle = circle;
            	return  converted;
            }
}

package io.alekso56.bukkit.hazeinv.Util;

import java.util.UUID;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import io.alekso56.bukkit.hazeinv.Models.Circle;
import net.minecraft.server.level.ServerPlayer;

public class InventoryConversion {
            VanillaPlayer getPlayer(Player pl,Circle circle) {
            	VanillaPlayer converted = ((VanillaPlayer) pl);
            	converted.current_circle = circle;
            	return  converted;
            }
            
            Inventory fromWorld(World world, UUID player) {
        		
        		return null;
        	}
}

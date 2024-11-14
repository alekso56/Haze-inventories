package io.alekso56.bukkit.hazeinv.Util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import io.alekso56.bukkit.hazeinv.Models.Circle;

public class InventoryConversion {
            public static VanillaPlayer wrap(Player pl,Circle circle) {
            	CraftPlayer plo = (CraftPlayer)pl;
            	CraftServer server = (CraftServer) Bukkit.getServer();
            	VanillaPlayer converted = new VanillaPlayer(server,plo);
            	converted.current_circle = circle;
            	converted.previous_circle = circle;
            	return  converted;
            }
            
            public static Inventory fromWorld(World world, UUID player) {
        		
        		return null;
        	}
}

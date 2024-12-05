package io.alekso56.bukkit.hazeinv.API;

import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.Enums.LabelTag;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import io.alekso56.bukkit.hazeinv.Util.InventoryStorage;
import io.alekso56.bukkit.hazeinv.Util.VanillaPlayer;

public class PlayerAPI {
	public static Inventory getInventory(UUID player,Circle circle, GameMode mode) {
		return InventoryStorage.loadData(circle, player, false, LabelTag.getOf(mode));
	}
	public static boolean saveInventory(UUID player,Circle circle, GameMode mode,Inventory inv) {
		return InventoryStorage.saveData(circle, player, inv, false, LabelTag.getOf(mode));
	}
    public static Inventory getEnderChest(UUID player,Circle circle,GameMode mode) {
    	return InventoryStorage.loadData(circle, player, true, LabelTag.getOf(mode));
    }
	public static boolean saveEnderChest(UUID player,Circle circle, GameMode mode,Inventory inv) {
		return InventoryStorage.saveData(circle, player, inv, true, LabelTag.getOf(mode));
	}
	public static void setCanSaveThisWorld(Player player,boolean canSaveInventory) {
		VanillaPlayer playercore = Core.instance.players.get(player);
		if(canSaveInventory)
			playercore.enableSaving();
		else
			playercore.disableSaving();
	}
	/*Fires @PostInventoryChangeEvent when player switches worlds.
	 * Replaces the inventory RIGHT NOW.
	 * */
	public static void replaceInventoryWithEmptyTemporarily(Player player) {
		VanillaPlayer playercore = Core.instance.players.get(player);
		playercore.disableSaving();
		player.getInventory().clear();
		player.getEnderChest().clear();
	}
	/*Fires @PostInventoryChangeEvent when player switches worlds.
	 * Replaces the inventory next world change
	 * Disables saving
	 * */
	public static void replaceInventoryWithInventoryNextWorldChange(Player player,Plugin plugin,Inventory inv) {
		VanillaPlayer playercore = Core.instance.players.get(player);
		playercore.loadNextWorldChange(inv);
	}
	/*Fires @PostInventoryChangeEvent when player switches worlds.
	 * Replaces the inventory next world change
	 * Enables saving, your plugins inventory for this circle
	 * */
	public static void replaceInventoryNextWorldChange(Player player,Plugin plugin,String inventoryName) {
		VanillaPlayer playercore = Core.instance.players.get(player);
		playercore.loadNextWorldChange(plugin, inventoryName);
	}
}

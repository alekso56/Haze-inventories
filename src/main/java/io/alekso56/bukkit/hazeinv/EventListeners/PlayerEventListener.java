package io.alekso56.bukkit.hazeinv.EventListeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.API.CircleAPI;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import io.alekso56.bukkit.hazeinv.Util.InventoryConversion;
import io.alekso56.bukkit.hazeinv.Util.VanillaPlayer;

public class PlayerEventListener implements Listener {
	
	//deny if is syncing
	void OnInventoryCreativeEditEvent(InventoryCreativeEvent e){
		
	}
	
	@EventHandler
	void onPlayerChangedWorldEvent(PlayerChangedWorldEvent e) {
		World world = e.getPlayer().getWorld();
		Circle to_circle = CircleAPI.getFromWorld(world);
		VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
		adjuster.setPrevious_circle(adjuster.getCurrent_circle());
		adjuster.setCurrent_circle(to_circle);
		adjuster.loadData();
	}
	
	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
	    if(!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())){
	        VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
	        adjuster.saveData();
	        e.getPlayer().getInventory().clear();
	        e.getPlayer().getEnderChest().clear();
	        e.getPlayer().getActivePotionEffects().clear();
	        e.getPlayer().setLevel(0);
	    }
	}
	
	void onDeathEvent(PlayerDeathEvent e) {
		
	}
	
	//exploitable
	void onGamemodeChangeEvent(PlayerGameModeChangeEvent e) {
		
	}
	
	@EventHandler
	void onPlayerQuitEvent(PlayerQuitEvent e) {
		Core.instance.players.remove(e.getPlayer());
	}
	
	@EventHandler
	void onPlayerJoinEvent(PlayerJoinEvent e) {
        Core.instance.players.put(e.getPlayer(), InventoryConversion.wrap(e.getPlayer(), CircleAPI.getFromWorld(e.getPlayer().getWorld())));
	}
	
	void onPlayerRespawnEvent(PlayerRespawnEvent e) {
		
	}
	
	void onPlayerSpawnLocationEvent(PlayerSpawnLocationEvent e) {
		
	}

}

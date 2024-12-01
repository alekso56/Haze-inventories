package io.alekso56.bukkit.hazeinv.EventListeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.API.CircleAPI;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import io.alekso56.bukkit.hazeinv.Util.InventoryConversion;
import io.alekso56.bukkit.hazeinv.Util.LabelTag;
import io.alekso56.bukkit.hazeinv.Util.VanillaPlayer;

public class PlayerEventListener implements Listener {
	
	@EventHandler
	void OnInventoryCreativeEditEvent(InventoryCreativeEvent e){
		if(Core.isTimedOut(e.getWhoClicked().getUniqueId())) e.setCancelled(true);
	}
	
	@EventHandler
	void OnInventorySurvivalEditEvent(InventoryInteractEvent e){
		if(Core.isTimedOut(e.getWhoClicked().getUniqueId())) e.setCancelled(true);
	}
	
	@EventHandler
	void onPickupWhileTimedOut(EntityPickupItemEvent e) {
		if(Core.isTimedOut(e.getEntity().getUniqueId())) e.setCancelled(true);
	}
	
	@EventHandler
	void onPortalWithNonSharingCircles(EntityPortalEvent e) {
		Location from = e.getFrom();
		Location to = e.getTo();
		if(from != null && to != null) {
			Circle from_circle = CircleAPI.getFromWorld(from.getWorld());
			Circle to_circle = CircleAPI.getFromWorld(to.getWorld());
			if(!from_circle.getCircleName().equals(to_circle.getCircleName())) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	void onPlayerSpawn(PlayerSpawnLocationEvent e) {
		VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
        Circle previousCircle = Core.instance.getLastLogoutCircle(e.getPlayer().getUniqueId());
        if(previousCircle != null && !adjuster.getCurrent_circle().getCircleName().equals(previousCircle.getCircleName())) {
        	Core.timeout(e.getPlayer().getUniqueId());
        	VanillaPlayer player = Core.instance.players.get(e.getPlayer());
        	//maybe save bugged inventory to correct location, but that requires last gamemode before crash.
        	player.loadData(adjuster.getCurrent_circle().isPerGameMode() ? LabelTag.getOf(e.getPlayer().getGameMode()) : LabelTag.CIRCLE_SURVIVAL);
        }
	}
	@EventHandler
	void onPlayerChangedWorldEvent(PlayerChangedWorldEvent e) {
		World world = e.getPlayer().getWorld();
		Circle to_circle = CircleAPI.getFromWorld(world);
		VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
		adjuster.setPrevious_circle(adjuster.getCurrent_circle());
		adjuster.setCurrent_circle(to_circle);
		GameMode targetGameMode = Core.mwcore.getMVWorldManager().getMVWorld(world).getGameMode();
		adjuster.loadData(to_circle.isPerGameMode() ?LabelTag.getOf(targetGameMode) : LabelTag.CIRCLE_SURVIVAL);
		Core.instance.saveLastLogoutCircle(e.getPlayer().getUniqueId(), to_circle);
	}
	
	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
	    if(!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())){
	        VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
	        Core.timeout(e.getPlayer().getUniqueId());
	        e.getPlayer().getOpenInventory().close();
	        GameMode targetGameMode = Core.mwcore.getMVWorldManager().getMVWorld(e.getTo().getWorld()).getGameMode();
	        adjuster.saveData(adjuster.getPrevious_circle().isPerGameMode()? LabelTag.getOf(targetGameMode): LabelTag.CIRCLE_SURVIVAL);
	        e.getPlayer().getInventory().clear();
	        e.getPlayer().getEnderChest().clear();
	        e.getPlayer().getActivePotionEffects().clear();
	        e.getPlayer().setLevel(0);
	    }
	}
	
	//exploitable
	@EventHandler
	void onGamemodeChangeEvent(PlayerGameModeChangeEvent e) {
		VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
		if (adjuster.getCurrent_circle().isPerGameMode()) {
			Core.timeout(e.getPlayer().getUniqueId());
            e.getPlayer().getOpenInventory().close();
			adjuster.saveData(LabelTag.getOf(e.getPlayer().getGameMode()));
			e.getPlayer().getInventory().clear();
			e.getPlayer().getEnderChest().clear();
			e.getPlayer().getActivePotionEffects().clear();
			e.getPlayer().setLevel(0);
			
			adjuster.loadData(LabelTag.getOf(e.getNewGameMode()));
			Core.instance.saveLastLogoutCircle(e.getPlayer().getUniqueId(), adjuster.getCurrent_circle());
		}
	}
	
	@EventHandler
	void onPlayerQuitEvent(PlayerQuitEvent e) {
		VanillaPlayer removed = Core.instance.players.remove(e.getPlayer());
		Core.instance.saveLastLogoutCircle(e.getPlayer().getUniqueId(), removed.getCurrent_circle());
	}
	
	@EventHandler
	void onPlayerJoinEvent(PlayerJoinEvent e) {
        Core.instance.players.put(e.getPlayer(), InventoryConversion.wrap(e.getPlayer(),CircleAPI.getFromWorld(e.getPlayer().getWorld())));
	}

}

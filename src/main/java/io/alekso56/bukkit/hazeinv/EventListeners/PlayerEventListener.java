package io.alekso56.bukkit.hazeinv.EventListeners;

import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.API.CircleAPI;
import io.alekso56.bukkit.hazeinv.Enums.LabelTag;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import io.alekso56.bukkit.hazeinv.Util.InventoryConversion;
import io.alekso56.bukkit.hazeinv.Util.InventoryStorage;
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
	void onAdvancement(PlayerAdvancementDoneEvent e) {
		Player player = e.getPlayer();
		World world = player.getWorld();
		Circle from_circle = CircleAPI.getFromWorld(world);
		if(!from_circle.isAdvancementsPossible()) {
			Advancement advancement = e.getAdvancement();
	        for(String c: advancement.getCriteria()) {
	            player.getAdvancementProgress(advancement).revokeCriteria(c);
	        }
	        if(world.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS)) {
	        	world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
	        }
		}
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
		if(adjuster == null)return;
        Circle previousCircle = Core.instance.getLastLogoutCircle(e.getPlayer().getUniqueId());
        if(previousCircle != null && !adjuster.getCurrent_circle().getCircleName().equals(previousCircle.getCircleName())) {
        	Core.timeout(e.getPlayer().getUniqueId());
        	//maybe save bugged inventory to correct location, but that requires last gamemode before crash.
        	adjuster.loadData(adjuster.getCurrent_circle().isPerGameMode() ? LabelTag.getOf(e.getPlayer().getGameMode()) : LabelTag.CIRCLE_SURVIVAL);
        	e.getPlayer().sendMessage("PlayerSpawnCalled");
        }
	}
	@EventHandler
	void onPlayerChangedWorldEvent(PlayerChangedWorldEvent e) {
		World world = e.getPlayer().getWorld();
		Circle to_circle = CircleAPI.getFromWorld(world);
		VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
		adjuster.setPrevious_circle(adjuster.getCurrent_circle());
		adjuster.setCurrent_circle(to_circle);
		adjuster.enableSaving();
		if(adjuster.loadQueue != null && adjuster.loadTargetName != null) {
			InventoryStorage.saveData(to_circle, e.getPlayer().getUniqueId(), adjuster.loadQueue, false, LabelTag.PLUGIN.setName(adjuster.loadTargetName));
		}
		if(adjuster.loadTargetName != null) {
			adjuster.loadData(LabelTag.PLUGIN.setName(adjuster.loadTargetName) );
			adjuster.loadQueue = null;
			adjuster.loadTargetName = null;
		}else {
			GameMode targetGameMode = Core.mwcore.getMVWorldManager().getMVWorld(world).getGameMode();
			
			adjuster.loadData(to_circle.isPerGameMode() ?LabelTag.getOf(targetGameMode) : LabelTag.CIRCLE_SURVIVAL);
		}
		
		Core.instance.saveLastLogoutCircle(e.getPlayer().getUniqueId(), to_circle);
	}
	
	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
	    if(e.getTo() != null && !e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())){
	        VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
	        Core.timeout(e.getPlayer().getUniqueId());
	        e.getPlayer().getOpenInventory().close();
	        adjuster.saveData(adjuster.getCurrent_circle().isPerGameMode()? LabelTag.getOf(e.getPlayer().getGameMode()): LabelTag.CIRCLE_SURVIVAL);
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
			adjuster.loadData(true,LabelTag.getOf(e.getNewGameMode()));
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

package io.alekso56.bukkit.hazeinv.EventListeners;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
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
		if(e.getEntity().isDead())e.setCancelled(true);
	}
	
	@EventHandler
	void onDeath(PlayerDeathEvent e) {
		VanillaPlayer adjuster = Core.instance.players.get(e.getEntity());
		if(adjuster != null) {
			if(!e.getKeepLevel()) {
			    e.getEntity().setExp(e.getNewExp());
			}
			if(!e.getKeepInventory()) {
				e.getEntity().getInventory().clear();
			}
	        adjuster.saveData(adjuster.getCurrent_circle().isPerGameMode()? LabelTag.getOf(e.getEntity().getGameMode()): LabelTag.CIRCLE_SURVIVAL);
		}
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
	void onPortalWithNonSharingCircles(EntityPortalEnterEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		Location from = e.getLocation();
		if (from == null)
			return;
		boolean isEnd = from.getBlock().getType().equals(Material.END_PORTAL);

		if (isEnd) {
			Player player = (Player) e.getEntity();
			VanillaPlayer adjuster = Core.instance.players.get((Player) e.getEntity());
			if (!adjuster.canLoad) {
				return;
			}
			Core.timeout((player).getUniqueId());
			player.getOpenInventory().close();
			//Avoid getting stuck into the portal block and dying on loop
			from = from.add(1, 1, 0);
			player.teleport(from);
			player.setFallDistance(0);
			
			adjuster.saveData(adjuster.getCurrent_circle().isPerGameMode() ? LabelTag.getOf(player.getGameMode())
					: LabelTag.CIRCLE_SURVIVAL);
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
			player.getVelocity().zero();
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
	
	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerSpawn(PlayerSpawnLocationEvent e) {
		Core.instance.players.put(e.getPlayer(), InventoryConversion.wrap(e.getPlayer(),CircleAPI.getFromWorld(e.getPlayer().getWorld())));
		VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
        Circle previousCircle = Core.instance.getLastLogoutCircle(e.getPlayer().getUniqueId());
        if(previousCircle != null && !adjuster.getCurrent_circle().getCircleName().equals(previousCircle.getCircleName())) {
        	Core.timeout(e.getPlayer().getUniqueId());
        	//maybe save bugged inventory to correct location, but that requires last gamemode before crash.
        	adjuster.loadData(adjuster.getCurrent_circle().isPerGameMode() ? LabelTag.getOf(e.getPlayer().getGameMode()) : LabelTag.CIRCLE_SURVIVAL);
        	adjuster.hasPluginInventory = false;
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
			adjuster.loadQueue = null;
		}
		if(adjuster.loadQueue != null){
			adjuster.disableSaving();
			e.getPlayer().getInventory().clear();
			e.getPlayer().getInventory().setContents(Arrays.copyOf(adjuster.loadQueue.getContents(),41));
			adjuster.loadQueue = null;
			adjuster.hasPluginInventory = true;
		}else if(adjuster.loadTargetName != null) {
			adjuster.loadData(LabelTag.PLUGIN.setName(adjuster.loadTargetName) );
			adjuster.loadTargetName = null;
			adjuster.hasPluginInventory = true;
		}else {
			Boolean hasWorld = MultiverseCoreApi.get().getWorldManager().getWorld(world).isDefined();
			GameMode targetGameMode = hasWorld ? MultiverseCoreApi.get().getWorldManager().getWorld(world).get().getGameMode():GameMode.SURVIVAL;
			
			adjuster.loadData(to_circle.isPerGameMode() ?LabelTag.getOf(targetGameMode) : LabelTag.CIRCLE_SURVIVAL);
			adjuster.hasPluginInventory = false;
		}
		Core.instance.saveLastLogoutCircle(e.getPlayer().getUniqueId(), to_circle);
	}
	
	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
	    if(e.getTo() != null && !e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())){
	        VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
	        if(!adjuster.canLoad){
	        	e.setCancelled(true);
	        	e.getPlayer().sendMessage(ChatColor.RED+"Magic wand active, teleport disabled.");
	        }
	        Core.timeout(e.getPlayer().getUniqueId());
	        e.getPlayer().getOpenInventory().close();
	        adjuster.saveData(adjuster.getCurrent_circle().isPerGameMode()? LabelTag.getOf(e.getPlayer().getGameMode()): LabelTag.CIRCLE_SURVIVAL);
			for (PotionEffect effect : e.getPlayer().getActivePotionEffects()) {
				e.getPlayer().removePotionEffect(effect.getType());
	        }
			e.getPlayer().getVelocity().zero();
	    }
	}
	
	//exploitable
	@EventHandler
	void onGamemodeChangeEvent(PlayerGameModeChangeEvent e) {
		VanillaPlayer adjuster = Core.instance.players.get(e.getPlayer());
		if (adjuster != null && adjuster.getCurrent_circle().isPerGameMode() && !adjuster.hasPluginInventory && adjuster.canLoad) {
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
}

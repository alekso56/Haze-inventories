package io.alekso56.bukkit.hazeinv.EventListeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.elmakers.mine.bukkit.api.event.SpellInventoryEvent;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.Enums.LabelTag;
import io.alekso56.bukkit.hazeinv.Util.VanillaPlayer;

public class MagicEvent implements Listener{
     @EventHandler
     void onMagicInventoryLoad(SpellInventoryEvent e){
    	 VanillaPlayer adjuster = Core.instance.players.get(e.getMage().getPlayer());
    	 if(e.isOpening()) {
    		adjuster.saveData(adjuster.getCurrent_circle().isPerGameMode()? LabelTag.getOf(e.getMage().getPlayer().getGameMode()): LabelTag.CIRCLE_SURVIVAL);
    	    adjuster.disableSaving();
    	    adjuster.disableLoading();
    	 }else if(!adjuster.hasPluginInventory){
    		 adjuster.enableSaving();
    		 adjuster.enableLoading();
    	 }
     }
}

package io.alekso56.bukkit.hazeinv.Executors;

import org.bukkit.World;
import org.bukkit.entity.Player;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import io.alekso56.bukkit.hazeinv.Util.VanillaPlayer;

public class ExecuteInternalWorldTransfer {
	
	void toNewWorld(Player player,World world){
		VanillaPlayer craftplayer = (VanillaPlayer) player;
		Circle previous = Core.getPreviousLocation(craftplayer);
		if(previous != null) {
	         craftplayer.setCurrent_circle(previous);
	         
		}
	}

}

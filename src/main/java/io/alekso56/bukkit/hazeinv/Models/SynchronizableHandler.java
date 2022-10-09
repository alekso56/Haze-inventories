package io.alekso56.bukkit.hazeinv.Models;

import org.bukkit.entity.Player;

public abstract class SynchronizableHandler {
       String config_name = "";
       
       //update spesific player from data ie LOAD
       public boolean updatePlayer(Player player, PlayerData data) {
		 return false;
	   }
       
       //Update the stored json data. ie SAVE
       public void updatePlayerData(PlayerData data, Player player) {
    	   
       }
}

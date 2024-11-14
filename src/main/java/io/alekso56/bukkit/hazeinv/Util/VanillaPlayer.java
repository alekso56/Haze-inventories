package io.alekso56.bukkit.hazeinv.Util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.Events.PostInventoryChangeEvent;
import io.alekso56.bukkit.hazeinv.Events.PreInventoryChangeEvent;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class VanillaPlayer {
	
	Circle current_circle;
	Circle previous_circle;
    CraftPlayer player;
    CraftServer server;
	

	public VanillaPlayer(CraftServer server, CraftPlayer plo) {
	         this.player = plo;
	         this.server = server;
	}

	public Circle getCurrent_circle() {
		return current_circle;
	}

	public void setCurrent_circle(Circle current_circle) {
		this.current_circle = current_circle;
	}

	public Circle getPrevious_circle() {
		return previous_circle;
	}

	public void setPrevious_circle(Circle previous_circle) {
		this.previous_circle = previous_circle;
	}

    public void loadData() {
        @Nullable PlayerDataStorage storage = server.getHandle().playerIo;
        if(storage == null) {
        	Core.instance.log(Level.WARNING, "Failed to load player data, playerIo is not enabled.");
        	return;
        }
        try {
			@Nullable
			CompoundTag tag = NbtIo.read(InventoryStorage.getFileForPlayer(current_circle, player).toPath());
			if(tag == null) {
				tag = InventoryStorage.CreateDefaultSave();
			}
			tag = InventoryStorage.FilterInventoryLoad(tag,current_circle,player);
			player.getHandle().load(tag);	
			PostInventoryChangeEvent PostEvent = new PostInventoryChangeEvent(player, previous_circle, current_circle);
            Bukkit.getPluginManager().callEvent(PostEvent);
		} catch (IOException e) {
			Core.instance.log(Level.WARNING, "Failed to load player data for "+player.getUniqueId().toString());
		}
    }
	
    public void saveData() {
        ServerPlayer player_s = player.getHandle();
        
        try {
            PlayerDataStorage worldNBTStorage = player_s.server.getPlayerList().playerIo;

            CompoundTag playerData = new CompoundTag();
            PreInventoryChangeEvent PreEvent = new PreInventoryChangeEvent(player, previous_circle, current_circle);
            Bukkit.getPluginManager().callEvent(PreEvent);
            player_s.saveWithoutId(playerData);
            player.setExtraData(playerData); //writes bukkit related data to tags
            playerData = InventoryStorage.FilterInventorySave(playerData,current_circle,previous_circle,player);
            
            File file = new File(worldNBTStorage.getPlayerDir(), player_s.getEncodeId() + ".dat.tmp");
            if(file.exists()) {
            	file.delete();
            }
            file.createNewFile();
            
            DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
            File file1 = InventoryStorage.getFileForPlayer(current_circle, player);
            
            NbtIo.write(playerData, output);
            output.close();
            
            if (file1.exists() && !file1.delete() || !file.renameTo(file1)) {
                Core.instance.log(Level.WARNING, "Failed to save player data for "+player.getDisplayName());
            }

        } catch (Exception e) {
        	e.printStackTrace();
        	Core.instance.log(Level.WARNING, "Failed to save player data for "+player.getDisplayName());
        }
    }
        
}

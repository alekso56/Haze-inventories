package io.alekso56.bukkit.hazeinv.Util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.Enums.Flag;
import io.alekso56.bukkit.hazeinv.Events.PostInventoryChangeEvent;
import io.alekso56.bukkit.hazeinv.Events.PreInventoryChangeEvent;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class VanillaPlayer extends CraftPlayer {
	
	Circle current_circle;
	Circle previous_circle;

	public VanillaPlayer(CraftServer server, ServerPlayer entity) {
		super(server, entity);
	}
	
	@Override
    public void loadData() {
        @Nullable PlayerDataStorage storage = this.server.getHandle().playerIo;
        if(storage == null) {
        	Core.instance.log(Level.WARNING, "Failed to load player data, playerIo is not enabled.");
        	return;
        }
        ServerPlayer player = this.getHandle();
        File file1 = new File(storage.getPlayerDir(), player.getEncodeId() + ".dat");
        try {
			@Nullable
			CompoundTag tag = NbtIo.read(file1);
			tag = InventoryStorage.FilterInventoryLoad(tag,current_circle,this.getPlayer());
			player.load(tag);
			PostInventoryChangeEvent PostEvent = new PostInventoryChangeEvent((Player)this.getPlayer(), previous_circle, current_circle);
            Bukkit.getPluginManager().callEvent(PostEvent);
		} catch (IOException e) {
			Core.instance.log(Level.WARNING, "Failed to save player data for "+player.getDisplayName().getString());
		}
    }
	
	@Override
    public void saveData() {
        ServerPlayer player = this.getHandle();
        
        try {
            PlayerDataStorage worldNBTStorage = player.server.getPlayerList().playerIo;

            CompoundTag playerData = new CompoundTag();
            PreInventoryChangeEvent PreEvent = new PreInventoryChangeEvent((Player)this.getPlayer(), previous_circle, current_circle);
            Bukkit.getPluginManager().callEvent(PreEvent);
            player.save(playerData);
            setExtraData(playerData); //writes bukkit related data to tags
            playerData = InventoryStorage.FilterInventory(playerData,current_circle);
            File file = new File(worldNBTStorage.getPlayerDir(), player.getEncodeId() + ".dat.tmp");
            File file1 = new File(worldNBTStorage.getPlayerDir(), player.getEncodeId() + ".dat");
            NbtIo.write(playerData, new DataOutputStream(new FileOutputStream(file)));

            if (file1.exists() && !file1.delete() || !file.renameTo(file1)) {
                Core.instance.log(Level.WARNING, "Failed to save player data for "+player.getDisplayName().getString());
            }

        } catch (Exception e) {
        	Core.instance.log(Level.WARNING, "Failed to save player data for "+player.getDisplayName().getString());
        }
    }
        
}

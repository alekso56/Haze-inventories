package io.alekso56.bukkit.hazeinv.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R6.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R6.CraftServer;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;

import com.mojang.serialization.DataResult;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.Enums.LabelTag;
import io.alekso56.bukkit.hazeinv.Events.PostInventoryChangeEvent;
import io.alekso56.bukkit.hazeinv.Events.PreInventoryChangeEvent;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class VanillaPlayer {
	
	Circle current_circle;
	Circle previous_circle;
    CraftPlayer player;
    CraftServer server;
    boolean canSave = true;
    public org.bukkit.inventory.Inventory loadQueue;
    public String loadTargetName;
	public boolean hasPluginInventory = false;
	public boolean canLoad  = true;
	

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
	public void loadData(LabelTag type) {
		loadData(false,type);
	}
    public void loadData(boolean inventoryOnly, LabelTag type) {
        @Nullable PlayerDataStorage storage = server.getHandle().playerIo;
        if(storage == null) {
        	Core.instance.log(Level.WARNING, "Failed to load player data, playerIo is not enabled.");
        	return;
        }
        try {
        	File file = InventoryStorage.getFileForPlayer(current_circle, player.getUniqueId(),type);
			@Nullable
			CompoundTag tag = NbtIo.read(file.toPath());
			if(tag == null) {
				tag = InventoryStorage.CreateDefaultSave();
			}
			tag = InventoryStorage.FilterInventoryLoad(tag,current_circle,player.getUniqueId(),type);
			if(InventoryStorage.containsAndExists(tag, InventoryStorage.healthtag) && tag.getFloat(InventoryStorage.healthtag).isPresent()) {
				tag.putFloat(InventoryStorage.healthtag,  20);
			}
			if (inventoryOnly && tag.contains(InventoryStorage.inventory_tag)) {
				Optional<ListTag> invsize = tag.getList(InventoryStorage.inventory_tag);
				player.getHandle().getInventory().clearContent();
				
				Inventory Replacement_Inventory = new Inventory(player.getHandle(), player.getHandle().equipment);
				
				DataResult<List<ItemStackWithSlot>> result = ItemStackWithSlot.CODEC.listOf().parse(NbtOps.INSTANCE, invsize.get());
				
				//ValueInputContextHelper helpr = new ValueInputContextHelper(CraftRegistry.getMinecraftRegistry(), NbtOps.INSTANCE);
				List<ItemStackWithSlot> generate = result.getOrThrow();
				  for (ItemStackWithSlot entry : generate) {
				        int slot = entry.slot();
				        ItemStack stack = entry.stack();

				        // Make sure the slot index is valid
				        if (slot >= 0 && slot < Replacement_Inventory.getContainerSize()) {
				        	Replacement_Inventory.setItem(slot, stack);
				        }
				    }
				player.getHandle().getInventory().replaceWith(Replacement_Inventory);
			} else {
				ValueInput tagload = TagValueInput.create(ProblemReporter.DISCARDING, CraftRegistry.getMinecraftRegistry(), tag);
				player.getHandle().load(tagload);
			}
			PostInventoryChangeEvent PostEvent = new PostInventoryChangeEvent(player, previous_circle, current_circle);
            Bukkit.getPluginManager().callEvent(PostEvent);
		} catch (IOException e) {
			Core.instance.log(Level.WARNING, "Failed to load player data for "+player.getUniqueId().toString());
		}
    }
    public void saveData(LabelTag type) {
    	if(!this.canSave)return;
        ServerPlayer player_s = player.getHandle();
        
        try {
            PreInventoryChangeEvent PreEvent = new PreInventoryChangeEvent(player, previous_circle, current_circle);
            Bukkit.getPluginManager().callEvent(PreEvent);
            
            CompoundTag playerData; 

            try {
            	TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, CraftRegistry.getMinecraftRegistry());
                player_s.saveWithoutId(output);
                player.setExtraData(output);//writes bukkit related data to tags
                
                
                playerData = output.buildResult();
            } catch (Exception e) {
                e.printStackTrace();
                playerData = new CompoundTag(); // fallback
            }
            playerData = InventoryStorage.FilterInventorySave(playerData, current_circle, previous_circle, player.getUniqueId(), type);
            InventoryStorage.writeData(current_circle, player.getUniqueId(), type, playerData);

        } catch (Exception e) {
        	e.printStackTrace();
        	Core.instance.log(Level.WARNING, "Failed to save player data for "+player.getDisplayName());
        }
    }

	public void enableSaving() {
		this.canSave = true;
	}
	
	public void disableSaving() {
		this.canSave = false;
	}

	public boolean loadNextWorldChange(org.bukkit.inventory.Inventory inv) {
		if(this.loadQueue != null || inv == null)return false;
		this.loadQueue = inv;
		return true;
	}

	public boolean loadNextWorldChange(Plugin plugin, String inventoryName) {
		if(this.loadTargetName != null)return false;
		this.loadTargetName = plugin.getName()+"_"+inventoryName;
		return true;
	}

	public void disableLoading() {
		this.canLoad = false;
	}
	public void enableLoading() {
		this.canLoad = true;
	}
}

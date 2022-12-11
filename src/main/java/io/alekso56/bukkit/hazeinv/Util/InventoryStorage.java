package io.alekso56.bukkit.hazeinv.Util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.Enums.Flag;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

public class InventoryStorage {
	
	static final String inventory_tag = "Inventory";
	
	static final String ender_inventory_tag = "EnderItems";
	
	static final int[] armor_slots  = new int[]{ 103, 102, 101, 100}; //head,chest,leg,feet
	
	static final String Slotname = "Slot";
	
	
	static final String Last_location_Tag = "LastLocation";
	static final String MaxairTag = "MaxAir";
	static final String EconomyTag = "Economy";
	static final String airTag = "Air";
	static final String EXHAUSTIONtag = "foodExhaustionLevel";
	static final String xpLeveltag = "XpLevel";
	static final String xpProgresstag = "XpP"; //float
	static final String xpTotaltag = "XpTotal";
	static final String EnchantmentSeedtag = "XpSeed";
	static final String distancetag = "FallDistance";
	static final String firetag = "Fire";
	static final String foodtag = "foodLevel";
	static final String foodTicktag = "foodTickTimer";
	static final String healthtag = "Health";
	static final String effecttag = "ActiveEffects";
    static final String foodSaturationLevelTag = "foodSaturationLevel";
	
	static public PlayerInventory loadInventory(Player input) {
		
		return null;

	}

	static public Inventory loadEnderChest(Player input) {
		return null;
	}
	
	static File getFileForPlayer(Circle circle,Player player){
		File PlayerFolder = new File(Core.instance.getDataFolder(), "saveData"+File.separatorChar+player.getUniqueId());
		if(!PlayerFolder.exists())PlayerFolder.mkdirs();
		
		return new File(PlayerFolder,circle.getCircleName().toString()+".dat");
	}
	
	static File getGlobalForPlayer(Player player){
		File GlobalFolder = new File(Core.instance.getDataFolder(), "saveData"+File.separatorChar+"GLOBAL");
		if(!GlobalFolder.exists())GlobalFolder.mkdirs();
		
		return new File(GlobalFolder,player.getUniqueId().toString()+".dat");
	}
	
	static boolean containsAndExists(CompoundTag tag, String input) {
		return tag != null && tag.contains(input);
	}
	
	static CompoundTag CreateDefaultSave() {
		CompoundTag save = new CompoundTag();
		save.put(inventory_tag, new ListTag());
		save.put(ender_inventory_tag, new ListTag());
		save.putInt(airTag, 300);
		save.putFloat(EXHAUSTIONtag, 0); //max 4
		save.putInt(xpLeveltag, 0);
		save.putFloat(xpProgresstag, 0);
		save.putInt(xpTotaltag, 0);
		save.putInt(EnchantmentSeedtag, 0);
		save.putFloat(distancetag, 0);
		save.putShort(firetag, (short)-20);
		save.putInt(foodtag, 20);
		save.putInt(foodTicktag,0);
		save.putFloat(healthtag, 20);
		save.putFloat(foodSaturationLevelTag, 5);
		 //potion effects can be null
		return save;
	}
	
	
	//global filter LOAD ONLY!
	static CompoundTag FilterInventoryLoad(CompoundTag tag,Circle circle,Player player) {
		File GlobalFile = getGlobalForPlayer(player);
		@Nullable
		CompoundTag data = null;
		if (GlobalFile.exists()) {
			try {
				data = NbtIo.read(GlobalFile);
			} catch (IOException e) {
				Core.instance.getLogger().log(Level.WARNING, e.getMessage());
			}
		}else {
			data = CreateDefaultSave();
			try {
				NbtIo.write(data, GlobalFile);
			} catch (IOException e) {
				Core.instance.getLogger().log(Level.WARNING,e.getMessage());
			}
		}
		//synced to circle, not global, meaning inventory is global.
        if(circle.isSyncArmorOnly() && tag.contains(inventory_tag)){
			
			ListTag list = tag.getList(inventory_tag, CompoundTag.TAG_COMPOUND);
			ListTag replacements = data.contains(inventory_tag)?data.getList(inventory_tag, CompoundTag.TAG_COMPOUND):null;
			
		    Iterator<Tag> listerator = list.iterator();
			while(listerator.hasNext()) {
				
				CompoundTag realtag = (CompoundTag) listerator.next();
				
				if(realtag.contains(Slotname)) {
					
					int slot = realtag.getInt(Slotname);
					//Optional to check for ranges instead, but prefer this method to create a dynamic filter for future use.
					boolean isWhitelisted = false;
					for(int toCheck : armor_slots) {
						if(slot == toCheck) {
							isWhitelisted = true;
							break;
						}
					}
					if (!isWhitelisted) {
						// if slot is not in whitelist, take replacement with same slot from global save
						// instead.
						if (replacements != null && !replacements.isEmpty()) {
							boolean isReplaced = false;
							Iterator<Tag> replerator = replacements.iterator();
							while (listerator.hasNext()) {
								CompoundTag replacetag = (CompoundTag) replerator.next();
								if (replacetag.contains(Slotname)) {
									int slotrepl = replacetag.getInt(Slotname);
									if (slotrepl == slot) {
										realtag = replacetag;
										isReplaced = true;
										break;
									}
								}
							}
							if(!isReplaced) {
								// our item is not an armor item, and there wasn't any replacement from global
								listerator.remove();
							}
						} else {
							// our replacements are null, so this entry should be deleted
							listerator.remove();
						}
					}
				}
			}
			tag.remove(inventory_tag);
			tag.put(inventory_tag, list);
		} else if(!circle.canLoadMainInventory() && tag.contains(inventory_tag)) {
			//can't sync inventory with circle or armor items, therefore get all from global.
			   tag.remove(inventory_tag);
			   
			   tag.put(inventory_tag, containsAndExists(data, inventory_tag)? data.get(inventory_tag):new ListTag());
		}
		if(!circle.isSyncEnderChest()  && tag.contains(ender_inventory_tag)) {
			//can't sync inventory with circle or armor items, therefore get all from global.
			tag.remove(ender_inventory_tag);
			tag.put(ender_inventory_tag, containsAndExists(data, ender_inventory_tag)? data.get(ender_inventory_tag):new ListTag());
		}

		for(Flag flag : Flag.values()) {
			//the flag is not allowed, get from global instead!
			if(!circle.checkFlag(flag)) {
				switch(flag) {
				case AIR:
					tag.remove(airTag);
					
					tag.putInt(airTag,  containsAndExists(data, airTag)? data.getInt(airTag):300);
					break;
				case EXHAUSTION:
					tag.remove(EXHAUSTIONtag);
					
					tag.putFloat(EXHAUSTIONtag, containsAndExists(data, EXHAUSTIONtag)? data.getFloat(EXHAUSTIONtag):0);
					break;
				case EXPERIENCE:
					tag.remove(xpLeveltag);
					tag.remove(xpProgresstag);
					tag.remove(xpTotaltag);
					tag.remove(EnchantmentSeedtag);
					
					tag.putInt(xpLeveltag, containsAndExists(data, xpLeveltag)? data.getInt(xpLeveltag):0);
					tag.putFloat(xpProgresstag, containsAndExists(data, xpProgresstag)? data.getFloat(xpProgresstag):0);
					tag.putInt(xpTotaltag, containsAndExists(data, xpTotaltag)? data.getInt(xpTotaltag):0);
					tag.putInt(EnchantmentSeedtag, containsAndExists(data, EnchantmentSeedtag)? data.getInt(EnchantmentSeedtag):0);
					break;
				case FALL_DISTANCE:
					tag.remove(distancetag);
					
					tag.putFloat(distancetag, containsAndExists(data, distancetag)? data.getFloat(distancetag):0);
					break;
				case FIRE_TICKS:
					tag.remove(firetag);
					
					tag.putShort(firetag, containsAndExists(data, firetag)?data.getShort(firetag):(short)-20);
					break;
				case FOOD_LEVEL:
					tag.remove(foodtag);
					tag.remove(foodTicktag);
						
					tag.putInt(foodtag, containsAndExists(data, foodtag)? data.getInt(foodtag):20);
					tag.putInt(foodTicktag,containsAndExists(data, foodTicktag)? data.getInt(foodTicktag):0);
					break;
				case HEALTH:
					tag.remove(healthtag);
					
					tag.putFloat(healthtag, containsAndExists(data, healthtag)? data.getFloat(healthtag):20);
					break;
				case POTIONS:
					tag.remove(effecttag);
					
					tag.put(effecttag, containsAndExists(data, effecttag)? data.getList(effecttag, CompoundTag.TAG_COMPOUND):new ListTag());
					break;
				case SATURATION:
					tag.remove(foodSaturationLevelTag);
					 
				    tag.putFloat(foodSaturationLevelTag, containsAndExists(data, foodSaturationLevelTag)? data.getFloat(foodSaturationLevelTag):5);
					break;
				case ECONOMY:
					if(Core.getEcon().isEnabled()) {
					double past_money = Core.getEcon().getBalance(player);
					Core.getEcon().withdrawPlayer(player, past_money);
					if(containsAndExists(data, EconomyTag)) {
						Core.getEcon().depositPlayer(player, data.getInt(EconomyTag));
					}
					}
					break;
				case MAX_AIR:
					((LivingEntity) player).setMaximumAir(containsAndExists(data, MaxairTag)? data.getInt(MaxairTag):300);
					break;
				default:
					break;
				}
			}
		}
		return tag;
	}
}

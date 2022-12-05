package io.alekso56.bukkit.hazeinv.Util;

import java.util.Iterator;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import io.alekso56.bukkit.hazeinv.Enums.Flag;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class InventoryStorage {
	
	static final String inventory_tag = "Inventory";
	
	static final String ender_inventory_tag = "EnderItems";
	
	static final int[] armor_slots  = new int[]{ 103, 102, 101, 100}; //head,chest,leg,feet
	
	static final String Slotname = "Slot";
	
	static public PlayerInventory loadInventory(Player input) {
		
		return null;

	}

	static public Inventory loadEnderChest(Player input) {
		return null;
	}
	
	static CompoundTag FilterInventory(CompoundTag tag,Circle circle) {
		if(!circle.canLoadMainInventory() && tag.contains(inventory_tag)) {
			   tag.remove(inventory_tag);
			   tag.put(inventory_tag, new ListTag());
		}else if(circle.isSyncArmorOnly() && tag.contains(inventory_tag)){
			
			ListTag list = tag.getList(inventory_tag, CompoundTag.TAG_COMPOUND);
			
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
						}
					}
					if(!isWhitelisted) {
						listerator.remove();
					}
				}
			}
		}
		if(!circle.isSyncEnderChest()  && tag.contains(ender_inventory_tag)) {
			tag.remove(ender_inventory_tag);
			tag.put(ender_inventory_tag, new ListTag());
		}
		for(Flag flag : Flag.values()) {
			//the flag is not allowed, delete!
			if(!circle.checkFlag(flag)) {
				switch(flag) {
				//economy and max air is not set here
				case AIR:
					final String airTag = "Air";
					
					tag.remove(airTag);
					
					tag.putInt(airTag, 300);
					break;
				case EXHAUSTION:
					final String EXHAUSTIONtag = "foodExhaustionLevel";
					
					tag.remove(EXHAUSTIONtag);
					
					tag.putFloat(EXHAUSTIONtag, 0); //max 4
					break;
				case EXPERIENCE:
					final String xpLeveltag = "XpLevel";
					final String xpProgresstag = "XpP"; //float
					final String xpTotaltag = "XpTotal";
					final String EnchantmentSeedtag = "XpSeed";
					
					tag.remove(xpLeveltag);
					tag.remove(xpProgresstag);
					tag.remove(xpTotaltag);
					tag.remove(EnchantmentSeedtag);
					
					tag.putInt(xpLeveltag, 0);
					tag.putFloat(xpProgresstag, 0);
					tag.putInt(xpTotaltag, 0);
					tag.putInt(EnchantmentSeedtag, 0);
					break;
				case FALL_DISTANCE:
					final String distancetag = "FallDistance";
					
					tag.remove(distancetag);
					
					tag.putFloat(distancetag, 0);
					break;
				case FIRE_TICKS:
                    final String firetag = "Fire";
					
					tag.remove(firetag);
					
					tag.putShort(firetag, (short)-20);
					break;
				case FOOD_LEVEL:
					 final String foodtag = "foodLevel";
					 final String foodTicktag = "foodTickTimer";

					 tag.remove(foodtag);
					 tag.remove(foodTicktag);
						
					 tag.putInt(foodtag, 20);
					 tag.putInt(foodTicktag,0);
					break;
				case HEALTH:
                    final String healthtag = "Health";
					
					tag.remove(healthtag);
					
					tag.putFloat(healthtag, 20);;
					break;
				case POTIONS:
                    final String effecttag = "ActiveEffects";
					
					tag.remove(effecttag);
					
					//ReAdd not neccesary for this one
					break;
				case SATURATION:
					 final String foodSaturationLevelTag = "foodSaturationLevel";
					 
					 tag.remove(foodSaturationLevelTag);
					 
					 tag.putFloat(foodSaturationLevelTag, 5);
					break;
				default:
					break;
				}
			}
		}
		return tag;
	}
}

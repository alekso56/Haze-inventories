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
				case AIR:
					
					break;
				case ECONOMY:
					break;
				case EXHAUSTION:
					break;
				case EXPERIENCE:
					break;
				case FALL_DISTANCE:
					break;
				case FIRE_TICKS:
					break;
				case FOOD_LEVEL:
					break;
				case HEALTH:
					break;
				case MAX_AIR:
					break;
				case POTIONS:
					break;
				case SATURATION:
					break;
				default:
					break;
				}
			}
		}
		return tag;
	}
}

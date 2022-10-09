package io.alekso56.bukkit.hazeinv.Models;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class PlayerData {
	UUID user;
	String inventory_contents;
	String enderchest_contents;
	String armor_contents;
	long economy;
	int air;
	int max_air;
	int experience;
	int fire_ticks;
	float fall_distance;
	int food_level;
	double health;
	HashMap<String,Integer> potions = new HashMap<String,Integer>();
	float saturation;
	
	//Get object containing the current stats of the player.
	PlayerData(Player usr){
		user = usr.getUniqueId();
		air = usr.getRemainingAir();
		max_air = usr.getMaximumAir();
		experience = usr.getTotalExperience();
		fire_ticks = usr.getFireTicks();
		fall_distance = usr.getFallDistance();
		food_level = usr.getFoodLevel();
		health = usr.getHealth();
		saturation = usr.getSaturation();
		potions.clear();
		Collection<PotionEffect> pots = usr.getActivePotionEffects();
		Iterator<PotionEffect> poterator = pots.iterator();
		while(poterator.hasNext()) {
			PotionEffect pot = poterator.next();
			potions.put(pot.getType().getName(), pot.getDuration());
		}
	}
	
	//DIY object, you need to set all values before calling save.
	PlayerData(UUID userid) {
            user = userid;
	}
	
	boolean save(){
		
		return true;
	}

}

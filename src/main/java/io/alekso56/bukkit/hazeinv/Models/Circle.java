package io.alekso56.bukkit.hazeinv.Models;

import org.bukkit.World;

import io.alekso56.bukkit.hazeinv.Enums.Flag;

public class Circle {
       String worldName;
       boolean isPerGameMode = false;
       boolean syncEnderChest = true;
       boolean syncMainInventory = true;
       boolean syncArmorOnly = false;
       int flags = 0;
       
       Circle(World name) {
    	   worldName = name.getName();
    	   resetFlags();

	   }
       
       public boolean canLoadMainInventory() {
    	   return !syncArmorOnly && syncMainInventory;
       }
       
       //enables all flags
       public void resetFlags(){
    	   int total = 0;
    	   for(Flag target : Flag.values()) {
    		   total  |= (1 << target.ordinal());
    	   }
    	   flags = total;
       }
       
       public void addFlag(Flag type) {
    	   boolean[] changeSet = getFlags();
    	   changeSet[type.ordinal()] = true;
    	   saveFlags(changeSet);
       }
       
       public void removeFlag(Flag type) {
    	   boolean[] changeSet = getFlags();
    	   changeSet[type.ordinal()] = false;
    	   saveFlags(changeSet);
       }
       
       private void saveFlags(boolean[] input){
    	   int total = 0;
    	   for (int i = 0; i < input.length; ++i) {
    		   if (input[i]) total |= (1 << i);
    	   }
    	   flags = total;
       }
       
       private boolean[] getFlags() {
    	   boolean [] int_flags = new boolean[Flag.values().length];
    	   for (int i = 0; i < Flag.values().length; ++i) {
    	       int_flags[i] = (flags & (1 << i)) != 0;
    	   }
    	   return int_flags;
       }
}
